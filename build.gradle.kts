plugins {
    java
    `java-gradle-plugin`
    `maven-publish`
    id("gradle-build-utils")
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.2")
}

val loadProperties: groovy.lang.Closure<java.util.Properties> by extra
val gitVersion: groovy.lang.Closure<String> by extra
val mavenGroup: String by project
val mavenArchivesName: String by project
val props = loadProperties("publish.properties")

base {
    group = mavenGroup
    archivesName.set(mavenArchivesName)
    version = gitVersion()
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8

    withSourcesJar()
}

gradlePlugin {
    plugins {
        create("gradleBuildUtils") {
            id = "gradle-build-utils"
            implementationClass = "work.lclpnet.build.GradleBuildUtilsPlugin"
        }
    }
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

publishing {
    repositories {
        maven {
            val env = System.getenv()
            if (listOf("DEPLOY_URL", "DEPLOY_USER", "DEPLOY_PASSWORD").all(env::containsKey)) {
                credentials {
                    username = env["DEPLOY_USER"]
                    password = env["DEPLOY_PASSWORD"]
                }
                url = uri(env["DEPLOY_URL"]!!)
            }
            else if (listOf("mavenHost", "mavenUser", "mavenPassword").all(props::containsKey)) {
                credentials {
                    username = props.getProperty("mavenUser")
                    password = props.getProperty("mavenPassword")
                }
                url = uri(props.getProperty("mavenHost")!!)
            } else {
                url = uri("file:///${project.projectDir}/repo")
            }
        }
    }
}