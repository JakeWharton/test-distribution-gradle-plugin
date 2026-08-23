package com.jakewharton.testdistribution

import java.io.File
import org.gradle.api.Project
import org.gradle.api.file.FileSystemLocation
import org.gradle.api.plugins.BasePluginExtension
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.application.CreateStartScripts
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.bundling.Zip
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget

internal fun configureKotlinMultiplatformPlugin(project: Project, gradleSupport: GradleSupport) {
	val base = project.extensions.getByType(BasePluginExtension::class.java)
	val kotlin = project.extensions.getByType(KotlinMultiplatformExtension::class.java)
	kotlin.targets.withType(KotlinJvmTarget::class.java).configureEach { target ->
		val mainJarProvider = project.tasks.named(target.artifactsTaskName)

		target.testRuns.configureEach { testRun ->
			val name = target.name + testRun.name.replaceFirstChar(Char::uppercase)
			val nameUpper = name.replaceFirstChar(Char::uppercase)

			val testClasses = testRun.executionSource.testClassesDirs
			val testDependencies = testRun.executionSource.classpath.filter(File::isFile)

			val testJarProvider = project.tasks.register("jar$nameUpper", Jar::class.java) {
				it.from(testClasses)
				it.archiveAppendix.set(target.name)
				it.archiveClassifier.set(testRun.name)
			}

			val testScriptsProvider = project.tasks.register("scripts$nameUpper", CreateStartScripts::class.java) {
				it.outputDir = project.layout.buildDirectory.dir("scripts/$name").get().asFile
				it.applicationName = base.archivesName.get() + "-" + testRun.name

				it.classpath = project.objects.fileCollection()
					.from(mainJarProvider.map { it.outputs.files })
					.from(testJarProvider.map { it.outputs.files })
					.from(testDependencies)

				it.mainClass.set(
					testClasses.elements.zip(testDependencies.elements) { classElements, dependencyElements ->
						val testFqcns = gradleSupport.detectJunit4TestClassNames(
							testClasses.asFileTree,
							classElements.map(FileSystemLocation::getAsFile),
							dependencyElements.map(FileSystemLocation::getAsFile),
						).sorted()
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
					it.from(testDependencies)
				}
				it.destinationDir = project.layout.buildDirectory.dir("install/$name").get().asFile
			}

			project.tasks.register("zip${nameUpper}Distribution", Zip::class.java) {
				it.group = "distribution"
				it.description = "Bundles $name as a distribution."

				it.from(installProvider)
				it.destinationDirectory.set(project.layout.buildDirectory.dir("dist"))
				it.archiveAppendix.set(target.name)
				it.archiveClassifier.set(testRun.name)
			}
		}
	}
}
