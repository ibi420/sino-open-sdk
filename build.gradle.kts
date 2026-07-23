plugins {
    kotlin("jvm") version "1.9.23"
}

group = "com.sino"
version = "1.0.0"

repositories {
    mavenCentral()
    google()
}

dependencies {
    implementation("org.bouncycastle:bcprov-jdk18on:1.78.1")
    
    testImplementation(kotlin("test"))
    testImplementation("junit:junit:4.13.2")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

tasks.test {
    useJUnit()
}
