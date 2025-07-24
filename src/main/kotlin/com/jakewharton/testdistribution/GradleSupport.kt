package com.jakewharton.testdistribution

import java.io.File
import org.gradle.api.file.FileTree

internal interface GradleSupport {
	fun detectTestClassNames(
		testClasses: FileTree,
		testClassDirectories: List<File>,
		testClasspath: List<File>,
	): List<String>
}
