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
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType

public class TestDistributionPlugin : Plugin<Project> {
	override fun apply(project: Project) {
		// HEY! If you update the minimum-supported Gradle version check to see if the Kotlin language version
		// can be bumped. See https://docs.gradle.org/current/userguide/compatibility.html#kotlin.
		val gradleVersion = GradleVersion.current()
		val gradleSupport = when {
			gradleVersion >= GradleVersion.version("8.10") -> GradleSupport_8_10()
			else -> {
				error("JVM test distribution plugin requires Gradle 8.10 or newer. Found $gradleVersion")
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

		project.pluginManager.withPlugin("org.jetbrains.kotlin.multiplatform") {
			gotPlugin = true

			val base = project.extensions.getByType(BasePluginExtension::class.java)
			val kotlin = project.extensions.getByType(KotlinMultiplatformExtension::class.java)
			kotlin.targets.configureEach { target ->
				if (target.platformType != KotlinPlatformType.jvm) return@configureEach

				val name = target.name + "Test"
				val nameUpper = name.replaceFirstChar(Char::uppercase)

				val mainJarProvider = project.tasks.named(target.artifactsTaskName)

				val testCompilation = target.compilations.named(TEST_COMPILATION_NAME)
				val testClassesProvider = testCompilation.map { it.output.allOutputs }
				val testDependenciesProvider = testCompilation.map {
					it.runtimeDependencyFiles?.filter(File::isFile)
						?: project.files()
				}

				val testJarProvider = project.tasks.register("jar$nameUpper", Jar::class.java) {
					it.from(testClassesProvider)
					it.archiveAppendix.set(target.name)
					it.archiveClassifier.set("tests")
				}

				val testScriptsProvider = project.tasks.register("scripts$nameUpper", CreateStartScripts::class.java) {
					it.outputDir = project.layout.buildDirectory.dir("scripts/$name").get().asFile
					it.applicationName = base.archivesName.get() + "-test"

					// The classpath property is not lazy, so we need explicit dependencies here.
					it.dependsOn(mainJarProvider)
					it.dependsOn(testJarProvider)
					it.dependsOn(testDependenciesProvider)
					// However, this 'plus' result will be live, and can still be set at configuration time.
					@Suppress("EagerGradleConfiguration") // See comments above.
					val classpath = mainJarProvider.get().outputs.files
						.plus(testJarProvider.get().outputs.files)
						.plus(testDependenciesProvider.get())
					it.classpath = classpath

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
