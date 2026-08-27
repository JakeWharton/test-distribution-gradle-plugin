package com.jakewharton.testdistribution

import com.android.build.api.variant.HostTest
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.testing.Test

@Suppress("ClassName")
class AndroidSupport_9_5_alpha02 : AndroidSupport {
	@Suppress("UnstableApiUsage")
	override fun withTestProvider(project: Project, hostTest: HostTest, handler: (TaskProvider<out Test>) -> Unit) {
		hostTest.withTestTaskProvider(handler)
	}
}
