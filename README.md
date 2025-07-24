# Test Distribution Gradle PLugin

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
    classpath 'com.jakewharton.gradle:test-distribution-gradle-plugin:0.1.0'
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
    classpath 'com.jakewharton.gradle:test-distribution-gradle-plugin:0.2.0-SNAPSHOT'
  }
}

apply plugin: 'com.jakewharton.test-distribution'
```

</p>
</details>


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
