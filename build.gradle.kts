import java.util.*

plugins {
    java
    `java-gradle-plugin`
    `maven-publish`
    id("gradle-build-utils").version("1.4.0")
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation("org.kohsuke:github-api:1.315")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.2")
}

val mavenGroup: String by project
val mavenArchivesName: String by project
val props: Properties = buildUtils.loadProperties("publish.properties")

base {
    group = mavenGroup
    archivesName.set(mavenArchivesName)

    version = buildUtils.gitVersion()
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

fun setupRepo(repositoryHandler: RepositoryHandler) {
    repositoryHandler.maven {

    }
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