plugins {
    id("application")
    kotlin("jvm") version "2.1.10"
    kotlin("plugin.serialization") version "2.1.10"
    id("io.ktor.plugin") version "2.3.0" // Check if it's latest
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


    // clickhouse
    implementation("com.clickhouse:clickhouse-jdbc:0.8.4") {
        exclude(group = "org.slf4j")
    }

    // connection pool
    implementation("com.zaxxer:HikariCP:3.4.5")

    // tests
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.0.0")
    testImplementation("io.mockk:mockk-jvm:1.13.2")
    testImplementation("io.ktor:ktor-client-mock")
    testImplementation("io.ktor:ktor-client-mock-jvm")
}

tasks.test {
    jvmArgs("-Dnet.bytebuddy.experimental=true")
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}

application {
    mainClass.set("ru.itmo.ivandor.MainKt")
}

ktor {
    fatJar {
        archiveFileName.set("fat.jar")
    }
}