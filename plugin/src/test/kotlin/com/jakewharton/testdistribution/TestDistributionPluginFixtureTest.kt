package com.jakewharton.testdistribution

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.containsAtLeast
import assertk.assertions.isDirectory
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import java.io.File
import org.gradle.testkit.runner.GradleRunner
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(TestParameterInjector::class)
class TestDistributionPluginFixtureTest(
	@param:TestParameter(
		LATEST_GRADLE_VERSION,
		"9.6.0",
		"9.5.0",
		"9.4.0",
		"9.3.0",
		"9.2.0",
		MINIMUM_GRADLE_VERSION,
	)
	private val gradleVersion: String,
) {
	@Test fun junit4PluginAndroidApplication() {
		val name = "junit4-plugin-android-application"
		val fixtureDir = File(fixturesDir, name)
		createRunner(fixtureDir, "installDebugUnitTestDistribution").build()

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

	@Test fun junit4PluginAndroidLibrary() {
		val name = "junit4-plugin-android-library"
		val fixtureDir = File(fixturesDir, name)
		createRunner(fixtureDir, "installDebugUnitTestDistribution").build()

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

	@Test fun junit4PluginJavaApplication() {
		val name = "junit4-plugin-java-application"
		val fixtureDir = File(fixturesDir, name)
		createRunner(fixtureDir, "installTestDistribution").build()

		val installDir = fixtureDir.resolve("build/install/test")
		assertThat(installDir).isDirectory()

		val binaryFile = installDir.resolve("bin/$name-test")
		assertThat(binaryFile.readText())
			.contains("""org.junit.runner.JUnitCore "com.example.AddTest"""")

		val libDir = installDir.resolve("lib")
		assertThat(libDir.list()).containsAtLeast(
			"$name.jar",
			"$name-tests.jar",
		)
	}

	@Test fun junit4PluginJavaLibrary() {
		val name = "junit4-plugin-java-library"
		val fixtureDir = File(fixturesDir, name)
		createRunner(fixtureDir, "installTestDistribution").build()

		val installDir = fixtureDir.resolve("build/install/test")
		assertThat(installDir).isDirectory()

		val binaryFile = installDir.resolve("bin/$name-test")
		assertThat(binaryFile.readText())
			.contains("""org.junit.runner.JUnitCore "com.example.AddTest"""")

		val libDir = installDir.resolve("lib")
		assertThat(libDir.list()).containsAtLeast(
			"$name.jar",
			"$name-tests.jar",
		)
	}

	@Test fun junit4PluginJavaLibraryWithKotlin() {
		val name = "junit4-plugin-java-library-with-kotlin"
		val fixtureDir = File(fixturesDir, name)
		createRunner(fixtureDir, "installTestDistribution").build()

		val installDir = fixtureDir.resolve("build/install/test")
		assertThat(installDir).isDirectory()

		val binaryFile = installDir.resolve("bin/$name-test")
		assertThat(binaryFile.readText())
			.contains("""org.junit.runner.JUnitCore "com.example.AddTest" "com.example.SubTest"""")

		val libDir = installDir.resolve("lib")
		assertThat(libDir.list()).containsAtLeast(
			"$name.jar",
			"$name-tests.jar",
		)
	}

	@Test fun junit4PluginKotlinMpp() {
		val name = "junit4-plugin-kotlin-mpp"
		val fixtureDir = File(fixturesDir, name)
		createRunner(fixtureDir, "installJvmTestDistribution").build()

		val installDir = fixtureDir.resolve("build/install/jvmTest")
		assertThat(installDir).isDirectory()

		val binaryFile = installDir.resolve("bin/$name-test")
		assertThat(binaryFile.readText())
			.contains("""org.junit.runner.JUnitCore "com.example.AddTest" "com.example.SubTest"""")

		val libDir = installDir.resolve("lib")
		assertThat(libDir.list()).containsAtLeast(
			"$name-jvm.jar",
			"$name-jvm-test.jar",
		)
	}

	@Test fun junit4PluginKotlinMppMultipleTestRuns() {
		val name = "junit4-plugin-kotlin-mpp-multiple-test-runs"
		val fixtureDir = File(fixturesDir, name)
		createRunner(fixtureDir, "installJvmTestDistribution", "installJvmIntegrationDistribution").build()

		val testInstallDir = fixtureDir.resolve("build/install/jvmTest")
		assertThat(testInstallDir).isDirectory()

		val testBinaryFile = testInstallDir.resolve("bin/$name-test")
		assertThat(testBinaryFile.readText())
			.contains("""org.junit.runner.JUnitCore "com.example.AddTest"""")

		val testLibDir = testInstallDir.resolve("lib")
		assertThat(testLibDir.list()).containsAtLeast(
			"$name-jvm.jar",
			"$name-jvm-test.jar",
		)

		val integrationInstallDir = fixtureDir.resolve("build/install/jvmIntegration")
		assertThat(integrationInstallDir).isDirectory()

		val integrationBinaryFile = integrationInstallDir.resolve("bin/$name-integration")
		assertThat(integrationBinaryFile.readText())
			.contains("""org.junit.runner.JUnitCore "com.example.AddTest"""")

		val integrationLibDir = integrationInstallDir.resolve("lib")
		assertThat(integrationLibDir.list()).containsAtLeast(
			"$name-jvm.jar",
			"$name-jvm-integration.jar",
		)
	}

	@Test fun junit4PluginKotlinMppTargetName() {
		val name = "junit4-plugin-kotlin-mpp-target-name"
		val fixtureDir = File(fixturesDir, name)
		createRunner(fixtureDir, "installDesktopTestDistribution").build()

		val installDir = fixtureDir.resolve("build/install/desktopTest")
		assertThat(installDir).isDirectory()

		val binaryFile = installDir.resolve("bin/$name-test")
		assertThat(binaryFile.readText())
			.contains("""org.junit.runner.JUnitCore "com.example.AddTest"""")

		val libDir = installDir.resolve("lib")
		assertThat(libDir.list()).containsAtLeast(
			"$name-desktop.jar",
			"$name-desktop-test.jar",
		)
	}

	@Test fun junit4PluginKotlinMppWithAndroid() {
		val name = "junit4-plugin-kotlin-mpp-with-android"
		val fixtureDir = File(fixturesDir, name)
		createRunner(fixtureDir, "installJvmTestDistribution").build()

		val installDir = fixtureDir.resolve("build/install/jvmTest")
		assertThat(installDir).isDirectory()

		val binaryFile = installDir.resolve("bin/$name-test")
		assertThat(binaryFile.readText())
			.contains("""org.junit.runner.JUnitCore "com.example.AddTest"""")

		val libDir = installDir.resolve("lib")
		assertThat(libDir.list()).containsAtLeast(
			"$name-jvm.jar",
			"$name-jvm-test.jar",
		)

		// TODO We should build Android as well.
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
				"--configuration-cache",
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
