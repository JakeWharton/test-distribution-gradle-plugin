package com.jakewharton.testdistribution

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.containsAtLeast
import assertk.assertions.isDirectory
import java.io.File
import org.gradle.testkit.runner.GradleRunner
import org.junit.Test

class TestDistributionPluginFixtureTest {
	@Test fun pluginKotlinMpp() {
		val fixtureDir = File(fixturesDir, "plugin-kotlin-mpp")
		createRunner(fixtureDir, "installJvmTest").build()

		val installDir = fixtureDir.resolve("build/install/jvmTest")
		assertThat(installDir).isDirectory()

		val binaryFile = installDir.resolve("bin/plugin-kotlin-mpp-test")
		assertThat(binaryFile.readText()).contains("""org.junit.runner.JUnitCore "com.example.AddTest"""")

		val libDir = installDir.resolve("lib")
		assertThat(libDir.list()).containsAtLeast(
			"plugin-kotlin-mpp-jvm.jar",
			"plugin-kotlin-mpp-jvm-tests.jar",
		)
	}

	@Test fun pluginKotlinMppTargetName() {
		val fixtureDir = File(fixturesDir, "plugin-kotlin-mpp-target-name")
		createRunner(fixtureDir, "installDesktopTest").build()

		val installDir = fixtureDir.resolve("build/install/desktopTest")
		assertThat(installDir).isDirectory()

		val binaryFile = installDir.resolve("bin/plugin-kotlin-mpp-target-name-test")
		assertThat(binaryFile.readText()).contains("""org.junit.runner.JUnitCore "com.example.AddTest"""")

		val libDir = installDir.resolve("lib")
		assertThat(libDir.list()).containsAtLeast(
			"plugin-kotlin-mpp-target-name-desktop.jar",
			"plugin-kotlin-mpp-target-name-desktop-tests.jar",
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
