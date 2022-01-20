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

base {
    group = "work.lclpnet.build"
    version = "1.0.0"
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

gradlePlugin {
    plugins {
        create("gradleBuildUtils") {
            id = "work.lclpnet.build.gradle-build-utils"
            implementationClass = "work.lclpnet.build.GradleBuildUtilsPlugin"
        }
    }
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}