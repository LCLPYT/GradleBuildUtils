# GradleBuildUtils
A Gradle plugin for git versioning and other useful utility.

## Git Tag Versioning
The Git tag versioning feature allows for easy dynamic versioning using git tags.
Effectively, the tools runs:
```
git describe --tags --abbrev=0
```
which will retrieve the latest tag, no matter how many commits ago.

To create a new version, you can simply add a new Git tag:
```
git tag 1.0.1
```
The tool will also validate against a version pattern:
```js
/^[0-9]+\.[0-9]+\.[0-9]+(?:-[a-z0-9]+)?$/
```
Which means, you can use alpha-numerical version values of the schema `major.minor.patch-variant`, where the `-variant` part is optional.

## Use in your buildscript
### Kotlin
Add the following repository to your `settings.gradle.kts`:
```kotlin
pluginManagement {
    repositories {
        maven {
            name = "LCLPNetwork Maven"
            url = uri("https://repo.lclpnet.work/repository/internal")
        }
        gradlePluginPortal()
    }
}
```

In your `build.gradle.kts`, include the plugin at the top of the buildscript:
```kotlin
plugins {
    id("gradle-build-utils").version("1.0.0") // replace with your version
}
```

To use the git tag versioning feature, you can add:
```kotlin
val gitVersion: groovy.lang.Closure<String> by extra
version = gitVersion()
```

To load a Java properties file, you can use this tool:
```kotlin
val loadProperties: groovy.lang.Closure<java.util.Properties> by extra
val props = loadProperties("publish.properties")
```

### Groovy
Add the following repository to your `settings.gradle`:
```groovy
pluginManagement {
    repositories {
        maven {
            name = 'LCLPNetwork Maven'
            url = 'https://repo.lclpnet.work/repository/internal'
        }
        gradlePluginPortal()
    }
}
```

In your `build.gradle`, include the plugin at the top of the buildscript:
```groovy
plugins {
    id 'gradle-build-utils' version '1.0.0' // replace with your version
}
```

To use the git tag versioning feature, you can add:
```groovy
version = gitVersion.call()
```

To load a Java properties file, you can use this tool:
```groovy
def props = loadProperties.call('publish.properties')
```
