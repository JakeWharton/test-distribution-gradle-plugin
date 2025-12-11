package com.jakewharton.testdistribution

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.containsAtLeast
import assertk.assertions.isDirectory
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import java.io.File
import org.gradle.testkit.runner.GradleRunner
import org.gradle.util.GradleVersion
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(TestParameterInjector::class)
class TestDistributionPluginFixtureTest(
	@param:TestParameter(LATEST_GRADLE_VERSION, MINIMUM_GRADLE_VERSION)
	private val gradleVersion: String,
) {
	private fun supportsAgp9(): Boolean {
		return gradleVersion == LATEST_GRADLE_VERSION ||
			GradleVersion.version(gradleVersion) >= GradleVersion.version("9.1")
	}

	@Test fun pluginAndroidApplication() {
		assumeTrue(supportsAgp9())

		val name = "plugin-android-application"
		val fixtureDir = File(fixturesDir, name)
		createRunner(fixtureDir, "installDebugUnitTest").build()

		val installDir = fixtureDir.resolve("build/install/debugUnitTest")
		assertThat(installDir).isDirectory()

		val binaryFile = installDir.resolve("bin/$name-test")
		assertThat(binaryFile.readText())
			.contains("""org.junit.runner.JUnitCore "com.example.AddTest"""")

		val libDir = installDir.resolve("lib")
		assertThat(libDir.list()).containsAtLeast(
			"classes.jar",
			"$name-debug-unitTest.jar",
		)
	}

	@Test fun pluginAndroidLibrary() {
		assumeTrue(supportsAgp9())

		val name = "plugin-android-library"
		val fixtureDir = File(fixturesDir, name)
		createRunner(fixtureDir, "installDebugUnitTest").build()

		val installDir = fixtureDir.resolve("build/install/debugUnitTest")
		assertThat(installDir).isDirectory()

		val binaryFile = installDir.resolve("bin/$name-test")
		assertThat(binaryFile.readText())
			.contains("""org.junit.runner.JUnitCore "com.example.AddTest"""")

		val libDir = installDir.resolve("lib")
		assertThat(libDir.list()).containsAtLeast(
			"classes.jar",
			"$name-debug-unitTest.jar",
		)
	}

	@Test fun pluginKotlinMpp() {
		val name = "plugin-kotlin-mpp"
		val fixtureDir = File(fixturesDir, name)
		createRunner(fixtureDir, "installJvmTest").build()

		val installDir = fixtureDir.resolve("build/install/jvmTest")
		assertThat(installDir).isDirectory()

		val binaryFile = installDir.resolve("bin/$name-test")
		assertThat(binaryFile.readText())
			.contains("""org.junit.runner.JUnitCore "com.example.AddTest"""")

		val libDir = installDir.resolve("lib")
		assertThat(libDir.list()).containsAtLeast(
			"$name-jvm.jar",
			"$name-jvm-tests.jar",
		)
	}

	@Test fun pluginKotlinMppTargetName() {
		val name = "plugin-kotlin-mpp-target-name"
		val fixtureDir = File(fixturesDir, name)
		createRunner(fixtureDir, "installDesktopTest").build()

		val installDir = fixtureDir.resolve("build/install/desktopTest")
		assertThat(installDir).isDirectory()

		val binaryFile = installDir.resolve("bin/$name-test")
		assertThat(binaryFile.readText())
			.contains("""org.junit.runner.JUnitCore "com.example.AddTest"""")

		val libDir = installDir.resolve("lib")
		assertThat(libDir.list()).containsAtLeast(
			"$name-desktop.jar",
			"$name-desktop-tests.jar",
		)
	}

	@Test fun pluginKotlinMppWithAndroid() {
		assumeTrue(supportsAgp9())

		val name = "plugin-kotlin-mpp-with-android"
		val fixtureDir = File(fixturesDir, name)
		createRunner(fixtureDir, "installJvmTest").build()

		val installDir = fixtureDir.resolve("build/install/jvmTest")
		assertThat(installDir).isDirectory()

		val binaryFile = installDir.resolve("bin/$name-test")
		assertThat(binaryFile.readText())
			.contains("""org.junit.runner.JUnitCore "com.example.AddTest"""")

		val libDir = installDir.resolve("lib")
		assertThat(libDir.list()).containsAtLeast(
			"$name-jvm.jar",
			"$name-jvm-tests.jar",
		)

		// TODO We should build Android as well.
	}

	@Test fun pluginKotlinMppWithBurst() {
		val name = "plugin-kotlin-mpp-with-burst"
		val fixtureDir = File(fixturesDir, name)
		createRunner(fixtureDir, "installJvmTest").build()

		val installDir = fixtureDir.resolve("build/install/jvmTest")
		assertThat(installDir).isDirectory()

		val binaryFile = installDir.resolve("bin/$name-test")
		assertThat(binaryFile.readText())
			.contains("""org.junit.runner.JUnitCore "com.example.AddTest_Language" "com.example.AddTest_Library"""")

		val libDir = installDir.resolve("lib")
		assertThat(libDir.list()).containsAtLeast(
			"$name-jvm.jar",
			"$name-jvm-tests.jar",
		)
	}

	private fun createRunner(fixtureDir: File, vararg tasks: String): GradleRunner {
		val gradleRoot = File(fixtureDir, "gradle").also { it.mkdir() }
		File("../gradle/wrapper").copyRecursively(File(gradleRoot, "wrapper"), true)
		return GradleRunner.create()
			.apply {
				if (gradleVersion != LATEST_GRADLE_VERSION) {
					withGradleVersion(gradleVersion)
				}
			}
			.withProjectDir(fixtureDir)
			.withDebug(true) // Run in-process
			.withArguments(
				"clean",
				*tasks,
				"--stacktrace",
				"--continue",
				"--no-configuration-cache", // https://github.com/JakeWharton/test-distribution-gradle-plugin/issues/10
				"--no-build-cache",
				VERSION_PROPERTY,
				VALIDATE_KOTLIN_METADATA,
			)
			.forwardOutput()
	}
}

private val fixturesDir = File("src/test/fixtures")
private const val VERSION_PROPERTY = "-PtestDistributionVersion=$PLUGIN_VERSION"
private const val LATEST_GRADLE_VERSION = "latest"
private const val VALIDATE_KOTLIN_METADATA = "-Porg.gradle.kotlin.dsl.skipMetadataVersionCheck=false"
