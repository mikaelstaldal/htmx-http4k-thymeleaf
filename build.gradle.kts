group = "nu.staldal"
version = "1.0-SNAPSHOT"

plugins {
    kotlin("jvm") version "2.1.0"
    application
    id("com.github.johnrengelman.shadow") version "8.1.1" // For creating JAR with dependencies
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

val mainClassName = "nu.staldal.htmxhttp4kthymeleaf.MainKt"

application {
    mainClass.set(mainClassName)
}

repositories {
    mavenCentral()

}

dependencies {
    // Kotlin standard library
    implementation("org.jetbrains.kotlin:kotlin-stdlib:2.1.0")

    // HTTP4k
    implementation(platform("org.http4k:http4k-bom:5.44.0.0"))
    implementation(platform("dev.forkhandles:forkhandles-bom:2.20.0.0"))
    implementation("org.http4k:http4k-core")
    implementation("org.http4k:http4k-htmx")
    implementation("org.http4k:http4k-template-thymeleaf")
    implementation("dev.forkhandles:result4k")

    // WebJars (Frontend Dependencies)
    implementation("org.webjars.npm:bootstrap:5.3.3")
    implementation("org.webjars.npm:missing.css:1.1.3")
    implementation("org.webjars.npm:sweetalert2:11.12.3")

    // Logging
    runtimeOnly("org.slf4j:slf4j-simple:2.0.13")
}

tasks.test {
    useJUnitPlatform()
}

tasks.named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar") {
    manifest {
        attributes(mapOf("Main-Class" to mainClassName)) // Set the `Main-Class` in manifest
    }
    archiveClassifier.set("jar-with-dependencies")
}
