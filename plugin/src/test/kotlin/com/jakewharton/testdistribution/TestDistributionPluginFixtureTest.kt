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
		"9.4.0",
		"9.3.0",
		"9.2.0",
		MINIMUM_GRADLE_VERSION,
	)
	private val gradleVersion: String,
) {
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

	@Test fun pluginJavaApplication() {
		val name = "plugin-java-application"
		val fixtureDir = File(fixturesDir, name)
		createRunner(fixtureDir, "installTest").build()

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

	@Test fun pluginJavaLibrary() {
		val name = "plugin-java-library"
		val fixtureDir = File(fixturesDir, name)
		createRunner(fixtureDir, "installTest").build()

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

	@Test fun pluginJavaLibraryWithKotlin() {
		val name = "plugin-java-library-with-kotlin"
		val fixtureDir = File(fixturesDir, name)
		createRunner(fixtureDir, "installTest").build()

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

	@Test fun pluginKotlinMpp() {
		val name = "plugin-kotlin-mpp"
		val fixtureDir = File(fixturesDir, name)
		createRunner(fixtureDir, "installJvmTest").build()

		val installDir = fixtureDir.resolve("build/install/jvmTest")
		assertThat(installDir).isDirectory()

		val binaryFile = installDir.resolve("bin/$name-test")
		assertThat(binaryFile.readText())
			.contains("""org.junit.runner.JUnitCore "com.example.AddTest" "com.example.SubTest"""")

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
