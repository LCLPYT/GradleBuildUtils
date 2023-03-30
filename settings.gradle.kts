pluginManagement {
    repositories {
        maven {
            name = "LCLPNetwork"
            url = uri("https://repo.lclpnet.work/repository/internal")
        }
        gradlePluginPortal()
    }
    plugins {
        id("gradle-build-utils").version("1.1.0")
    }
}

rootProject.name = "gradle-build-utils"

