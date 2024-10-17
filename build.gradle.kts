plugins {
    kotlin("jvm") version "1.9.22"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    implementation("dk.brics:automaton:1.12-4")

}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(19)
}