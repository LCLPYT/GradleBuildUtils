import java.util.Properties;

plugins {
    java
    `java-gradle-plugin`
    `maven-publish`
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.2")
}

val props = loadPublishProps()

val mavenGroup: String by project
val mavenArchivesName: String by project

base {
    group = mavenGroup
    archivesName.set(mavenArchivesName)
    version = "1.0.0"
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
    publications {
        create<MavenPublication>("mavenJava") {
            artifactId = mavenArchivesName
            group = mavenGroup

            from(components["java"])

            pom {
                name.set("gradle-build-utils")
                description.set("A gradle plugin to help with git versioning and config loading")
            }
        }
    }

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

fun loadPublishProps(): Properties {
    val props = Properties()
    val propsFile = File(project.projectDir, "publish.properties")
    if (propsFile.exists()) props.load(propsFile.bufferedReader())

    return props
}