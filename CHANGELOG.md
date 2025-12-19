# Change log

## [Unreleased]
[Unreleased]: https://github.com/JakeWharton/test-distribution-gradle-plugin/compare/0.2.0...HEAD

Added:
- Support for Gradle 9.4.
- `org.gradle.java` plugin is now supported (the base plugin of the `java` and `java-library` plugins).
- `org.jetbrains.kotlin.jvm` plugin is now supported.

Changed:
- The minimum-supported Gradle version is now 9.1.

Fixed:
- Nothing yet!


## [0.2.0] - 2025-09-19
[0.2.0]: https://github.com/JakeWharton/test-distribution-gradle-plugin/releases/tag/0.2.0

Added:
- Initial support for `com.android.application` and `com.android.library` projects.

Fixed:
- Do not attempt to configure the new Android Kotlin multiplatform Gradle plugin's Kotlin target as if it were a "plain" Kotlin JVM target.


## [0.1.1] - 2025-09-19
[0.1.1]: https://github.com/JakeWharton/test-distribution-gradle-plugin/releases/tag/0.1.1

Fixed:
- Create a temporary directory (as opposed to a file) for class detection.


## [0.1.0] - 2025-07-24
[0.1.0]: https://github.com/JakeWharton/test-distribution-gradle-plugin/releases/tag/0.1.0

Initial release!

Only supports the Kotlin multiplatform plugin.
