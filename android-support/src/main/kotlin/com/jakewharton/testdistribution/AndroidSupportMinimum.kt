package com.jakewharton.testdistribution

import com.android.build.api.variant.HostTest
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.testing.Test

class AndroidSupportMinimum : AndroidSupport {
	override fun withTestProvider(
		project: Project,
		hostTest: HostTest,
		handler: (TaskProvider<out Test>) -> Unit,
	) {
		handler(project.tasks.named("test${hostTest.name.replaceFirstChar(Char::uppercase)}", Test::class.java))
	}
}
