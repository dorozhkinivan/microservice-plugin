plugins {
    kotlin("jvm") version "2.1.10"
    kotlin("plugin.serialization") version "2.1.10"
}

group = "ru.itmo.ivandor"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("io.ktor:ktor-client-cio:2.3.2")
    implementation("io.ktor:ktor-client-content-negotiation:2.3.0")
    implementation("io.ktor:ktor-server-content-negotiation:2.3.0")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.0")
    implementation("io.ktor:ktor-server-call-logging:2.3.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")

    // auth
    implementation("io.ktor:ktor-server-auth:2.3.0")
    implementation("io.ktor:ktor-server-auth-jwt:2.3.0")

    // server
    implementation("io.ktor:ktor-server-core:2.3.0")
    implementation("io.ktor:ktor-server-netty:2.3.0")
    implementation("io.ktor:ktor-server-host-common:2.3.0")

    // koin
    implementation("io.insert-koin:koin-core:3.2.2")
    implementation("io.insert-koin:koin-ktor:3.2.2")

    // logging
    implementation("org.slf4j:slf4j-api:1.6.1")
    implementation("org.slf4j:slf4j-simple:1.6.1")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}