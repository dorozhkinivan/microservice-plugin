plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.9.25"
    id("org.jetbrains.intellij") version "1.17.4"
}

group = "ru.itmo.ivandor"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

intellij {
    version.set("2024.1.7")
    type.set("IC")

    plugins.set(listOf("com.intellij.java", "org.jetbrains.kotlin"))
}

dependencies {
    implementation("org.junit.jupiter:junit-jupiter:5.8.1")
    testImplementation("junit:junit:4.13.2")
    implementation(kotlin("stdlib-jdk8"))
}

tasks {
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }

    patchPluginXml {
        sinceBuild.set("241")
        untilBuild.set("243.*")
    }

    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        token.set(System.getenv("PUBLISH_TOKEN"))
    }
}
