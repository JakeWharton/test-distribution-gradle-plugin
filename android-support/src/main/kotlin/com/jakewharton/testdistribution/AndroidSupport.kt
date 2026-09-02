package com.jakewharton.testdistribution

import com.android.build.api.variant.HostTest
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.testing.Test

interface AndroidSupport {
	fun withTestProvider(
		project: Project,
		hostTest: HostTest,
		handler: (TaskProvider<out Test>) -> Unit,
	)
}
