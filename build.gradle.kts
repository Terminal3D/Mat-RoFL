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
    implementation("net.automatalib:automata-util:0.11.0")
    implementation("net.automatalib:automata-core:0.11.0")
    implementation("net.automatalib:automata-dot-visualizer:0.11.0")
    implementation("net.automatalib.distribution:automata-distribution:0.11.0")
    implementation("dk.brics:automaton:1.12-4")

}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(19)
}