package com.jakewharton.testdistribution

import org.gradle.api.file.FileCollection
import org.gradle.api.file.FileSystemLocation
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.testing.Test
import org.gradle.api.tasks.testing.junit.JUnitOptions
import org.gradle.api.tasks.testing.junitplatform.JUnitPlatformOptions

internal enum class TestFramework {
	Junit4,
	JunitPlatform,
}

internal fun Provider<out Test>.toTestFramework(): Provider<TestFramework> = map {
	when (it.options) {
		is JUnitOptions -> TestFramework.Junit4
		is JUnitPlatformOptions -> TestFramework.JunitPlatform
		else -> error("Unsupported test framework: ${it.options::class.java}")
	}
}

internal fun computeMain(
	gradleSupport: GradleSupport,
	testFramework: Provider<TestFramework>,
	testClasses: FileCollection,
	testJars: FileCollection,
): Provider<String> {
	return zip(
		testFramework,
		testClasses.elements,
		testJars.elements,
	) { options, testClassLocations, testJarLocations ->
		when (options) {
			TestFramework.Junit4 -> {
				val testFqcns = gradleSupport.detectJunit4TestClassNames(
					testClasses.asFileTree,
					testClassLocations.map(FileSystemLocation::getAsFile),
					testJarLocations.map(FileSystemLocation::getAsFile),
				).sorted()
				"org.junit.runner.JUnitCore ${testFqcns.joinToString(" ") { """"$it"""" }}"
			}

			TestFramework.JunitPlatform -> buildList {
				add("org.junit.platform.console.ConsoleLauncher")
				add("execute")
				add("--disable-banner")
				add("--scan-classpath")
				for (testJarLocation in testJarLocations) {
					add("--classpath")
					add(testJarLocation.asFile)
				}
				for (testClassLocation in testClassLocations) {
					add("--classpath")
					add(testClassLocation.asFile)
				}
			}.joinToString(separator = " ")
		}
	}
}

private fun <I1 : Any, I2 : Any, I3 : Any, O : Any> zip(
	provider1: Provider<I1>,
	provider2: Provider<I2>,
	provider3: Provider<I3>,
	handler: (I1, I2, I3) -> O,
): Provider<O> {
	return provider1.zip(provider2, ::Pair)
		.zip(provider3) { (i1, i2), i3 ->
			handler(i1, i2, i3)
		}
}
