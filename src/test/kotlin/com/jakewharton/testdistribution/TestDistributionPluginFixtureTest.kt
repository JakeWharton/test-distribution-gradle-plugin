package com.jakewharton.testdistribution

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.containsAtLeast
import assertk.assertions.isDirectory
import java.io.File
import org.gradle.testkit.runner.GradleRunner
import org.junit.Test

class TestDistributionPluginFixtureTest {
	@Test fun pluginAndroidApplication() {
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
		File("gradle/wrapper").copyRecursively(File(gradleRoot, "wrapper"), true)
		return GradleRunner.create()
			.withProjectDir(fixtureDir)
			.withDebug(true) // Run in-process
			.withArguments("clean", *tasks, "--stacktrace", "--continue", versionProperty)
			.forwardOutput()
	}

	private val fixturesDir = File("src/test/fixtures")
	private val versionProperty = "-PtestDistributionVersion=${System.getProperty("testDistributionVersion")!!}"
}
