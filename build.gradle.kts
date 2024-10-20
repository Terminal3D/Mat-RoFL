plugins {
    application
    kotlin("jvm") version "1.9.22"
    kotlin("plugin.serialization") version "1.8.10"
}

group = "org.example"
version = "1.0-SNAPSHOT"

application {
    mainClass.set("org.example.ApplicationKt")
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    implementation("io.ktor:ktor-server-core:2.3.3")
    implementation("io.ktor:ktor-server-netty:2.3.3")
    implementation("io.ktor:ktor-server-content-negotiation:2.3.3")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.3")
    implementation("io.ktor:ktor-server-call-logging:2.3.3")
    implementation("dk.brics:automaton:1.12-4")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(19)
}