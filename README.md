# Test Distribution Gradle Plugin

A Gradle plugin which creates distributions of JVM unit tests.

Compile your JVM unit tests once and then ship them to multiple platforms and/or run them on different JVMs.


## Usage

Add the dependency and apply the plugin:

```groovy
buildscript {
  repositories {
    mavenCentral()
  }
  dependencies {
    classpath 'com.jakewharton.gradle:test-distribution-gradle-plugin:0.4.0'
  }
}

apply plugin: 'com.jakewharton.test-distribution'
```

<details>
<summary>Snapshots of the development version are available in the Central Portal Snapshots repository.</summary>
<p>

```groovy
buildscript {
  repositories {
    mavenCentral()
    maven {
      url 'https://central.sonatype.com/repository/maven-snapshots/'
    }
  }
  dependencies {
    classpath 'com.jakewharton.gradle:test-distribution-gradle-plugin:0.5.0-SNAPSHOT'
  }
}

apply plugin: 'com.jakewharton.test-distribution'
```

</p>
</details>

### Test engines

This plugin supports both JUnit 4 and the JUnit Platform.
It will automatically detect the engine in use and generate a compatible script.

However, some engines do require additional support.

#### JUnit Platform

Traditionally the `junit-platform-launcher` artifact is declared on the `testRuntimeOnly` configuration.
While this provides sufficient hooks for Gradle execution, it does not contain a traditional `main` method.
Replace that artifact with `junit-platform-console` (which itself depends on `junit-platform-launcher`) to allow both script and Gradle/IDE execution to work.


### Compatibility

This plugin relies on Gradle internal APIs.
As such, certain versions of the plugin only work with certain versions of Gradle.

| Gradle        | Plugin        |
|---------------|---------------|
| 9.4.0 - 9.7.1 | 0.4.0         |
| 9.3.0         | 0.3.0 - 0.4.0 |
| 9.1.0 - 9.2.1 | 0.2.0 - 0.4.0 |
| 9.0.0         | 0.2.0         |
| 8.10 - 8.14.3 | 0.1.0 - 0.2.0 |

Gradle versions newer than those listed may be supported, but have not been tested.


## License

    Copyright 2025 Jake Wharton

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
