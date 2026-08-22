package com.jakewharton.testdistribution

import java.io.File
import org.gradle.api.file.FileTree

interface GradleSupport {
	fun detectJunit4TestClassNames(
		testClasses: FileTree,
		testClassDirectories: List<File>,
		testClasspath: List<File>,
	): List<String>
}
