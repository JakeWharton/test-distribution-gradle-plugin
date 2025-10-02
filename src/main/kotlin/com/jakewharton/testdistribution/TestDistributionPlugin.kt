package com.jakewharton.testdistribution

import com.android.build.api.artifact.ScopedArtifact
import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.api.variant.HasHostTests
import com.android.build.api.variant.ScopedArtifacts
import java.io.File
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.RegularFile
import org.gradle.api.plugins.AppliedPlugin
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

		val androidPluginHandler: (AppliedPlugin) -> Unit = {
			gotPlugin = true

			val base = project.extensions.getByType(BasePluginExtension::class.java)

			val androidComponents = project.extensions.getByType(AndroidComponentsExtension::class.java)
			androidComponents.onVariants { variant ->
				if (variant !is HasHostTests) return@onVariants

				val variantUpperName = variant.name.replaceFirstChar(Char::uppercase)
				for ((testUpperName, hostTest) in variant.hostTests) {
					val testName = testUpperName.replaceFirstChar(Char::lowercase)
					val name = variant.name + testUpperName
					val nameUpper = variantUpperName + testUpperName

					val dummyClassesProvider = project.tasks.register("internal${nameUpper}Classes", ScopedArtifactDummyTask::class.java)
					hostTest.artifacts
						.forScope(ScopedArtifacts.Scope.ALL)
						.use(dummyClassesProvider)
						.toGet(
							ScopedArtifact.CLASSES,
							ScopedArtifactDummyTask::jars,
							ScopedArtifactDummyTask::classDirs,
						)

					val testJarProvider = project.tasks.register("jar$nameUpper", Jar::class.java) {
						it.from(dummyClassesProvider.flatMap(ScopedArtifactDummyTask::classDirs))
						it.archiveAppendix.set(variant.name)
						it.archiveClassifier.set(testName)
					}

					val testScriptsProvider = project.tasks.register("scripts$nameUpper", CreateStartScripts::class.java) {
						it.outputDir = project.layout.buildDirectory.dir("scripts/$nameUpper").get().asFile
						it.applicationName = base.archivesName.get() + "-test"

						it.classpath = project.objects.fileCollection()
							.from(testJarProvider.map { it.outputs.files })
							.from(dummyClassesProvider.flatMap { it.jars })

						it.mainClass.set(
							dummyClassesProvider.flatMap {
								it.classDirs.zip(it.jars) { classDirs, jars ->
									val testClasses = project.objects.fileTree()
									for (classDir in classDirs) {
										testClasses.from(classDir)
									}

									val testFqcns = gradleSupport.detectTestClassNames(
										testClasses,
										testClasses.files.toList(),
										jars.map(RegularFile::getAsFile),
									)
									"org.junit.runner.JUnitCore ${testFqcns.joinToString(" ") { """"$it"""" }}"
								}
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
							it.from(dummyClassesProvider.flatMap { it.jars })
						}
						it.destinationDir = project.layout.buildDirectory.dir("install/$name").get().asFile
					}

					project.tasks.register("zip${nameUpper}Distribution", Zip::class.java) {
						it.group = "distribution"
						it.description = "Bundles $name as a distribution."

						it.from(installProvider)
						it.destinationDirectory.set(project.layout.buildDirectory.dir("dist"))
						it.archiveAppendix.set(variant.name)
						it.archiveClassifier.set(testName)
					}
				}
			}
		}
		project.pluginManager.withPlugin("com.android.application", androidPluginHandler)
		project.pluginManager.withPlugin("com.android.library", androidPluginHandler)

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
