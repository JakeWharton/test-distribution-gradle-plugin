# Change log

## [Unreleased]
[Unreleased]: https://github.com/JakeWharton/test-distribution-gradle-plugin/compare/0.1.1...HEAD

Added:
- Initial support for `com.android.application` and `com.android.library` projects.

Changed:
- Nothing yet!

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
