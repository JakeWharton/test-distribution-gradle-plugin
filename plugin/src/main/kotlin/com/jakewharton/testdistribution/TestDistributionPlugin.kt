package com.jakewharton.testdistribution

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.util.GradleVersion

public class TestDistributionPlugin : Plugin<Project> {
	override fun apply(project: Project) {
		// HEY! If you update the minimum-supported Gradle version check to see if the Kotlin language version
		// can be bumped. See https://docs.gradle.org/current/userguide/compatibility.html#kotlin.
		val gradleVersion = GradleVersion.current()
		val gradleMinimum = GradleVersion.version("9.0")
		val gradleSupport = when {
			gradleVersion < gradleMinimum -> {
				error("Test distribution plugin requires $gradleMinimum or newer. Found $gradleVersion")
			}

			gradleVersion >= GradleVersion.version("9.3") -> GradleSupport_9_3()

			else -> GradleSupport_9_0()
		}

		var gotPlugin = false
		project.afterEvaluate {
			check(gotPlugin) {
				val name = if (project.path == ":") {
					"root project"
				} else {
					"project ${project.path}"
				}
				"No compatible language plugin applied for JVM test distribution ($name)"
			}
		}

		project.pluginManager.withPlugin("com.android.application") {
			gotPlugin = true
			configureAndroidPlugin(project, gradleSupport)
		}
		project.pluginManager.withPlugin("com.android.library") {
			gotPlugin = true
			configureAndroidPlugin(project, gradleSupport)
		}

		project.pluginManager.withPlugin("org.jetbrains.kotlin.multiplatform") {
			gotPlugin = true
			configureKotlinMultiplatformPlugin(project, gradleSupport)
		}

		project.pluginManager.withPlugin("org.gradle.java") {
			gotPlugin = true
			configureJavaPlugin(project, gradleSupport)
		}
	}
}
