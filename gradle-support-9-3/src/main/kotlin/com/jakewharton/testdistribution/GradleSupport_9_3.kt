package com.jakewharton.testdistribution

import java.io.File
import java.nio.file.Files
import org.gradle.api.file.FileTree
import org.gradle.api.internal.tasks.testing.ClassTestDefinition
import org.gradle.api.internal.tasks.testing.TestDefinition
import org.gradle.api.internal.tasks.testing.TestDefinitionProcessor
import org.gradle.api.internal.tasks.testing.TestResultProcessor
import org.gradle.api.internal.tasks.testing.detection.ClassFileExtractionManager
import org.gradle.api.internal.tasks.testing.detection.DefaultTestScanner
import org.gradle.api.internal.tasks.testing.junit.JUnitDetector

@Suppress("ClassName")
class GradleSupport_9_3 : GradleSupport {
	override fun detectJunit4TestClassNames(
		testClasses: FileTree,
		testClassDirectories: List<File>,
		testClasspath: List<File>,
	): List<String> {
		val detector = JUnitDetector(
			ClassFileExtractionManager {
				Files.createTempDirectory("test-distribution-gradle-plugin").toFile().apply {
					deleteOnExit()
				}
			},
		)
		detector.setTestClasses(testClassDirectories)
		detector.setTestClasspath(testClasspath)

		val testFqcns = mutableListOf<String>()
		val testDefinitionProcessor = object : TestDefinitionProcessor<TestDefinition> {
			override fun processTestDefinition(testDefinition: TestDefinition?) {
				testFqcns += when (testDefinition) {
					is ClassTestDefinition -> testDefinition.testClassName
					else -> error("Sup")
				}
			}
			override fun startProcessing(resultProcessor: TestResultProcessor) {}

			override fun stop() {}
			override fun stopNow() {}
		}

		DefaultTestScanner(testClasses, emptySet(), detector, testDefinitionProcessor)
			.detect()

		return testFqcns
	}
}
