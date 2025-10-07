package com.jakewharton.testdistribution

import java.io.File
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.BasePluginExtension
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.application.CreateStartScripts
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.bundling.Zip
import org.gradle.util.GradleVersion
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation.Companion.TEST_COMPILATION_NAME
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget

public class TestDistributionPlugin : Plugin<Project> {
	override fun apply(project: Project) {
		// HEY! If you update the minimum-supported Gradle version check to see if the Kotlin language version
		// can be bumped. See https://docs.gradle.org/current/userguide/compatibility.html#kotlin.
		val gradleVersion = GradleVersion.current()
		val gradleMinimum = GradleVersion.version("9.0")
		val gradleSupport = when {
			gradleVersion >= gradleMinimum -> GradleSupport_9_0()
			else -> {
				error("Test distribution plugin requires $gradleMinimum or newer. Found $gradleVersion")
			}
		}

		var gotPlugin = false
		project.afterEvaluate {
			check(gotPlugin) {
				val name = if (project.path == ":") {
					"root project"
				} else {
					"project ${project.path}"
				}
				"No compatible language plugin applied for JVM test distribution ($name)"
			}
		}

		project.pluginManager.withPlugin("com.android.application") {
			gotPlugin = true
			configureAndroidPlugin(project, gradleSupport)
		}
		project.pluginManager.withPlugin("com.android.library") {
			gotPlugin = true
			configureAndroidPlugin(project, gradleSupport)
		}

		project.pluginManager.withPlugin("org.jetbrains.kotlin.multiplatform") {
			gotPlugin = true

			val base = project.extensions.getByType(BasePluginExtension::class.java)
			val kotlin = project.extensions.getByType(KotlinMultiplatformExtension::class.java)
			kotlin.targets.withType(KotlinJvmTarget::class.java).configureEach { target ->
				val name = target.name + "Test"
				val nameUpper = name.replaceFirstChar(Char::uppercase)

				val mainJarProvider = project.tasks.named(target.artifactsTaskName)

				val testCompilation = target.compilations.named(TEST_COMPILATION_NAME)
				val testClassesProvider = testCompilation.map { it.output.allOutputs }
				val testDependenciesProvider = testCompilation.map {
					it.runtimeDependencyFiles.filter(File::isFile)
				}

				val testJarProvider = project.tasks.register("jar$nameUpper", Jar::class.java) {
					it.from(testClassesProvider)
					it.archiveAppendix.set(target.name)
					it.archiveClassifier.set("tests")
				}

				val testScriptsProvider = project.tasks.register("scripts$nameUpper", CreateStartScripts::class.java) {
					it.outputDir = project.layout.buildDirectory.dir("scripts/$name").get().asFile
					it.applicationName = base.archivesName.get() + "-test"

					it.classpath = project.objects.fileCollection()
						.from(mainJarProvider.map { it.outputs.files })
						.from(testJarProvider.map { it.outputs.files })
						.from(testDependenciesProvider)

					it.mainClass.set(
						testClassesProvider.zip(testDependenciesProvider) { testClasses, testDependencies ->
							val testFqcns = gradleSupport.detectTestClassNames(
								testClasses.asFileTree,
								testClasses.files.toList(),
								testDependencies.files.toList(),
							)
							"org.junit.runner.JUnitCore ${testFqcns.joinToString(" ") { """"$it"""" }}"
						},
					)
				}

				val installProvider = project.tasks.register("install${nameUpper}Distribution", Copy::class.java) {
					it.group = "distribution"
					it.description = "Installs $name as a distribution as-is."

					it.into("bin") {
						it.from(testScriptsProvider)
					}
					it.into("lib") {
						it.from(testJarProvider)
						it.from(mainJarProvider)
						it.from(testDependenciesProvider)
					}
					it.destinationDir = project.layout.buildDirectory.dir("install/$name").get().asFile
				}

				project.tasks.register("zip${nameUpper}Distribution", Zip::class.java) {
					it.group = "distribution"
					it.description = "Bundles $name as a distribution."

					it.from(installProvider)
					it.destinationDirectory.set(project.layout.buildDirectory.dir("dist"))
					it.archiveAppendix.set(target.name)
					it.archiveClassifier.set("tests")
				}
			}
		}
	}
}
