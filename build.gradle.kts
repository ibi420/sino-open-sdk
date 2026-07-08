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
    implementation("com.lambdapioneer.argon2kt:argon2kt:1.6.0")
    
    testImplementation(kotlin("test"))
    testImplementation("junit:junit:4.13.2")
}

kotlin {
    jvmToolchain(17)
}

tasks.test {
    useJUnit()
}
