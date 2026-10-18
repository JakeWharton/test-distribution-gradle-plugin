package com.jakewharton.testdistribution

import java.io.StringWriter
import org.gradle.api.file.FileCollection
import org.gradle.api.file.FileSystemLocation
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.testing.Test
import org.gradle.api.tasks.testing.junit.JUnitOptions
import org.gradle.api.tasks.testing.junitplatform.JUnitPlatformOptions
import org.gradle.jvm.application.scripts.ScriptGenerator

internal enum class TestFramework {
	Junit4,
	JunitPlatform,
}

internal fun Provider<out Test>.toTestFramework(): Provider<TestFramework> {
	return flatMap(Test::getTestFrameworkProperty)
		.map { testFramework ->
			@Suppress("InternalGradleApiUsage") // Don't know any other way to get this.
			when (val options = testFramework.options) {
				is JUnitOptions -> TestFramework.Junit4
				is JUnitPlatformOptions -> TestFramework.JunitPlatform
				else -> error("Unsupported test framework: ${options::class.java}")
			}
		}
}

internal fun computeMain(
	gradleSupport: GradleSupport,
	testFramework: Provider<TestFramework>,
	testClasses: FileCollection,
	testJars: FileCollection,
	testJarProvider: TaskProvider<Jar>,
): Provider<String> {
	return testFramework.flatMap { options ->
		when (options) {
			TestFramework.Junit4 -> {
				testClasses.elements.zip(testJars.elements) { testClassLocations, testJarLocations ->
					val testFqcns = gradleSupport.detectJunit4TestClassNames(
						testClasses.asFileTree,
						testClassLocations.map(FileSystemLocation::getAsFile),
						testJarLocations.map(FileSystemLocation::getAsFile),
					).sorted()
					"org.junit.runner.JUnitCore ${testFqcns.joinToString(" ") { """"$it"""" }}"
				}
			}

			TestFramework.JunitPlatform -> testJarProvider.flatMap(Jar::getArchiveFileName)
				.map { fileName ->
					buildList {
						add("org.junit.platform.console.ConsoleLauncher")
						add("execute")
						add("--disable-banner")
						add("--scan-classpath")
						add(""""$APP_HOME_MARKER/lib/$fileName"""")
					}.joinToString(separator = " ")
				}
		}
	}
}

internal fun ScriptGenerator.decorateScript(appHome: String): ScriptGenerator = ScriptGenerator { details, writer ->
	val script = StringWriter().also { generateScript(details, it) }.toString()
	writer.write(script.replace(APP_HOME_MARKER, appHome))
}

private const val APP_HOME_MARKER = "__TDGP_APP_HOME__"
