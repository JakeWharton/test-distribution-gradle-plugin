@file:Suppress("InternalGradleApiUsage")

package com.jakewharton.testdistribution

import java.io.File
import java.nio.file.Files
import org.gradle.api.file.FileTree
import org.gradle.api.internal.tasks.testing.TestClassProcessor
import org.gradle.api.internal.tasks.testing.TestClassRunInfo
import org.gradle.api.internal.tasks.testing.TestResultProcessor
import org.gradle.api.internal.tasks.testing.detection.ClassFileExtractionManager
import org.gradle.api.internal.tasks.testing.detection.DefaultTestClassScanner
import org.gradle.api.internal.tasks.testing.junit.JUnitDetector

@Suppress("ClassName")
class GradleSupport_9_0 : GradleSupport {
	override fun detectTestClassNames(
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
		val testClassProcessor = object : TestClassProcessor {
			override fun processTestClass(testClass: TestClassRunInfo) {
				testFqcns += testClass.testClassName
			}

			override fun startProcessing(resultProcessor: TestResultProcessor) {}
			override fun stop() {}
			override fun stopNow() {}
		}

		DefaultTestClassScanner(testClasses, detector, testClassProcessor).run()

		return testFqcns
	}
}
