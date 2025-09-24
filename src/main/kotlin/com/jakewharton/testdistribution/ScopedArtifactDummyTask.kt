package com.jakewharton.testdistribution

import org.gradle.api.DefaultTask
import org.gradle.api.file.Directory
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

/**
 * AGP APIs force us to wire a custom task with these properties. This dummy task facilitates that,
 * and then lets us use its input properties elsewhere to configure normal tasks.
 *
 * See https://issuetracker.google.com/issues/382215754
 */
@DisableCachingByDefault(because = "It does nothing")
internal abstract class ScopedArtifactDummyTask : DefaultTask() {
	@get:Input
	abstract val jars: ListProperty<RegularFile>

	@get:Input
	abstract val classDirs: ListProperty<Directory>

	@TaskAction
	fun run() {
	}
}
