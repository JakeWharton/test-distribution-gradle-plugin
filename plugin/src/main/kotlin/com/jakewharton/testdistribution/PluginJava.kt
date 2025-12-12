package com.jakewharton.testdistribution

import org.gradle.api.Project
import org.gradle.api.plugins.BasePluginExtension
import org.gradle.api.plugins.JavaPlugin.COMPILE_TEST_JAVA_TASK_NAME
import org.gradle.api.plugins.JavaPlugin.JAR_TASK_NAME
import org.gradle.api.plugins.JavaPlugin.TEST_RUNTIME_CLASSPATH_CONFIGURATION_NAME
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.application.CreateStartScripts
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.bundling.Zip
import org.gradle.api.tasks.compile.JavaCompile

internal fun configureJavaPlugin(project: Project, gradleSupport: GradleSupport) {
	val base = project.extensions.getByType(BasePluginExtension::class.java)

	val name = "test"
	val nameUpper = name.replaceFirstChar(Char::uppercase)

	val mainJarProvider = project.tasks.named(JAR_TASK_NAME)

	val testCompilation = project.tasks.named(COMPILE_TEST_JAVA_TASK_NAME)
	val testClassesProvider = testCompilation.flatMap {
		(it as JavaCompile).destinationDirectory
	}
	val testDependenciesProvider = project.configurations.named(TEST_RUNTIME_CLASSPATH_CONFIGURATION_NAME)

	val testJarProvider = project.tasks.register("jar$nameUpper", Jar::class.java) {
		it.from(testClassesProvider)
		it.archiveClassifier.set("tests")
	}

	val testScriptsProvider = project.tasks.register("scripts$nameUpper", CreateStartScripts::class.java) {
		it.outputDir = project.layout.buildDirectory.dir("scripts/$name").get().asFile
		it.applicationName = base.archivesName.get() + "-test"

		it.classpath = project.objects.fileCollection()
			.from(mainJarProvider.map { it.outputs.files })
			.from(testJarProvider.map { it.outputs.files })
			.from(testDependenciesProvider)

		it.mainClass.set(
			testClassesProvider.zip(testDependenciesProvider) { testClasses, testDependencies ->
				val testFqcns = gradleSupport.detectTestClassNames(
					testClasses.asFileTree,
					testClasses.asFileTree.files.toList(),
					testDependencies.files.toList(),
				)
				"org.junit.runner.JUnitCore ${testFqcns.joinToString(" ") { """"$it"""" }}"
			},
		)
	}

	val installProvider = project.tasks.register("install${nameUpper}Distribution", Copy::class.java) {
		it.group = "distribution"
		it.description = "Installs $name as a distribution as-is."

		it.into("bin") {
			it.from(testScriptsProvider)
		}
		it.into("lib") {
			it.from(testJarProvider)
			it.from(mainJarProvider)
			it.from(testDependenciesProvider)
		}
		it.destinationDir = project.layout.buildDirectory.dir("install/$name").get().asFile
	}

	project.tasks.register("zip${nameUpper}Distribution", Zip::class.java) {
		it.group = "distribution"
		it.description = "Bundles $name as a distribution."

		it.from(installProvider)
		it.destinationDirectory.set(project.layout.buildDirectory.dir("dist"))
		it.archiveClassifier.set("tests")
	}
}
