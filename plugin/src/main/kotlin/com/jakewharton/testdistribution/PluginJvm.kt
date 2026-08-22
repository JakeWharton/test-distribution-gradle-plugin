package com.jakewharton.testdistribution

import java.io.File
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.FileCollection
import org.gradle.api.file.FileSystemLocation
import org.gradle.api.plugins.BasePluginExtension
import org.gradle.api.plugins.JavaPlugin.COMPILE_TEST_JAVA_TASK_NAME
import org.gradle.api.plugins.JavaPlugin.JAR_TASK_NAME
import org.gradle.api.plugins.JavaPlugin.TEST_RUNTIME_CLASSPATH_CONFIGURATION_NAME
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.application.CreateStartScripts
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.bundling.Zip
import org.gradle.api.tasks.compile.JavaCompile
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation.Companion.TEST_COMPILATION_NAME

internal fun configureJavaPlugin(project: Project, gradleSupport: GradleSupport) {
	val base = project.extensions.getByType(BasePluginExtension::class.java)

	val name = "test"
	val nameUpper = name.replaceFirstChar(Char::uppercase)

	val testJarProvider = project.tasks.register("jar$nameUpper", Jar::class.java) {
		it.archiveClassifier.set("tests")
	}

	val testScriptsProvider = project.tasks.register("scripts$nameUpper", CreateStartScripts::class.java) {
		it.outputDir = project.layout.buildDirectory.dir("scripts/$name").get().asFile
		it.applicationName = base.archivesName.get() + "-test"
	}

	val installProvider = project.tasks.register("install${nameUpper}Distribution", Copy::class.java) {
		it.group = "distribution"
		it.description = "Installs $name as a distribution as-is."
		it.destinationDir = project.layout.buildDirectory.dir("install/$name").get().asFile
	}

	project.tasks.register("zip${nameUpper}Distribution", Zip::class.java) {
		it.group = "distribution"
		it.description = "Bundles $name as a distribution."

		it.from(installProvider)
		it.destinationDirectory.set(project.layout.buildDirectory.dir("dist"))
		it.archiveClassifier.set("tests")
	}

	project.afterEvaluate {
		if (project.pluginManager.hasPlugin("org.jetbrains.kotlin.jvm")) {
			finishKotlinPlugin(project, gradleSupport, testJarProvider, testScriptsProvider, installProvider)
		} else {
			finishJavaPlugin(project, gradleSupport, testJarProvider, testScriptsProvider, installProvider)
		}
	}
}

private fun finishKotlinPlugin(
	project: Project,
	gradleSupport: GradleSupport,
	testJarProvider: TaskProvider<Jar>,
	testScriptsProvider: TaskProvider<CreateStartScripts>,
	installProvider: TaskProvider<Copy>,
) {
	val kotlin = project.extensions.getByType(KotlinJvmExtension::class.java)
	val target = kotlin.target

	val mainJarProvider = project.tasks.named(target.artifactsTaskName)

	val testCompilation = target.compilations.named(TEST_COMPILATION_NAME)
	val testClasses = project.objects.fileCollection()
		.from(testCompilation.map { it.output.allOutputs })
	val testDependencies = project.objects.fileCollection()
		.from(
			testCompilation.map {
				it.runtimeDependencyFiles?.filter(File::isFile)
					?: it.project.files()
			},
		)

	configureTasks(
		project,
		gradleSupport,
		mainJarProvider,
		testClasses,
		testDependencies,
		testJarProvider,
		testScriptsProvider,
		installProvider,
	)
}

private fun finishJavaPlugin(
	project: Project,
	gradleSupport: GradleSupport,
	testJarProvider: TaskProvider<Jar>,
	testScriptsProvider: TaskProvider<CreateStartScripts>,
	installProvider: TaskProvider<Copy>,
) {
	val mainJarProvider = project.tasks.named(JAR_TASK_NAME)

	val testCompilation = project.tasks.named(COMPILE_TEST_JAVA_TASK_NAME)
	val testClasses = project.objects.fileCollection()
		.from(testCompilation.flatMap { (it as JavaCompile).destinationDirectory })
	val testDependencies = project.objects.fileCollection()
		.from(project.configurations.named(TEST_RUNTIME_CLASSPATH_CONFIGURATION_NAME))

	testJarProvider.configure {
		it.from(testClasses)
	}

	configureTasks(
		project,
		gradleSupport,
		mainJarProvider,
		testClasses,
		testDependencies,
		testJarProvider,
		testScriptsProvider,
		installProvider,
	)
}

private fun configureTasks(
	project: Project,
	gradleSupport: GradleSupport,
	mainJarProvider: TaskProvider<Task>,
	testClasses: FileCollection,
	testDependencies: FileCollection,
	testJarProvider: TaskProvider<Jar>,
	testScriptsProvider: TaskProvider<CreateStartScripts>,
	installProvider: TaskProvider<Copy>,
) {
	testScriptsProvider.configure {
		it.classpath = project.objects.fileCollection()
			.from(mainJarProvider.map { it.outputs.files })
			.from(testJarProvider.map { it.outputs.files })
			.from(testDependencies)

		it.mainClass.set(
			testClasses.elements.zip(testDependencies.elements) { classElements, dependencyElements ->
				val testFqcns = gradleSupport.detectTestClassNames(
					testClasses.asFileTree,
					classElements.map(FileSystemLocation::getAsFile),
					dependencyElements.map(FileSystemLocation::getAsFile),
				).sorted()
				"org.junit.runner.JUnitCore ${testFqcns.joinToString(" ") { """"$it"""" }}"
			},
		)
	}

	installProvider.configure {
		it.into("bin") {
			it.from(testScriptsProvider)
		}
		it.into("lib") {
			it.from(testJarProvider)
			it.from(mainJarProvider)
			it.from(testDependencies)
		}
	}
}
