package com.jakewharton.testdistribution

import com.android.build.api.artifact.ScopedArtifact
import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.api.variant.HasHostTests
import com.android.build.api.variant.ScopedArtifacts
import org.gradle.api.Project
import org.gradle.api.file.FileSystemLocation
import org.gradle.api.plugins.BasePluginExtension
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.application.CreateStartScripts
import org.gradle.api.tasks.bundling.Zip
import org.gradle.jvm.tasks.Jar

internal fun configureAndroidPlugin(project: Project, gradleSupport: GradleSupport) {
	val base = project.extensions.getByType(BasePluginExtension::class.java)

	val androidComponents = project.extensions.getByType(AndroidComponentsExtension::class.java)
	androidComponents.onVariants { variant ->
		if (variant !is HasHostTests) return@onVariants

		val variantUpperName = variant.name.replaceFirstChar(Char::uppercase)
		for ((testUpperName, hostTest) in variant.hostTests) {
			val testName = testUpperName.replaceFirstChar(Char::lowercase)
			val name = variant.name + testUpperName
			val nameUpper = variantUpperName + testUpperName

			val dummyClassesProvider = project.tasks.register(
				"internal${nameUpper}Classes",
				ScopedArtifactDummyTask::class.java,
			)
			hostTest.artifacts
				.forScope(ScopedArtifacts.Scope.ALL)
				.use(dummyClassesProvider)
				.toGet(
					ScopedArtifact.CLASSES,
					ScopedArtifactDummyTask::jars,
					ScopedArtifactDummyTask::classDirs,
				)

			val testClasses = project.objects.fileCollection()
				.from(dummyClassesProvider.flatMap(ScopedArtifactDummyTask::classDirs))
			val testJars = project.objects.fileCollection()
				.from(dummyClassesProvider.flatMap(ScopedArtifactDummyTask::jars))

			val testJarProvider = project.tasks.register("jar$nameUpper", Jar::class.java) {
				it.from(testClasses)
				it.archiveAppendix.set(variant.name)
				it.archiveClassifier.set(testName)
			}

			val testScriptsProvider =
				project.tasks.register("scripts$nameUpper", CreateStartScripts::class.java) {
					it.outputDir = project.layout.buildDirectory.dir("scripts/$nameUpper").get().asFile
					it.applicationName = base.archivesName.get() + "-test"

					it.classpath = project.objects.fileCollection()
						.from(testJarProvider.map { it.outputs.files })
						.from(testJars)

					it.mainClass.set(
						testClasses.elements.zip(testJars.elements) { classElements, jarElements ->
							val testFqcns = gradleSupport.detectJunit4TestClassNames(
								testClasses.asFileTree,
								classElements.map(FileSystemLocation::getAsFile),
								jarElements.map(FileSystemLocation::getAsFile),
							).sorted()
							"org.junit.runner.JUnitCore ${testFqcns.joinToString(" ") { """"$it"""" }}"
						},
					)
				}

			val installProvider =
				project.tasks.register("install${nameUpper}Distribution", Copy::class.java) {
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
