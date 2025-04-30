plugins {
    // Optional: only if you're using the Kotlin DSL or build plugins at root level
    // id("org.jetbrains.kotlin.jvm") version "1.9.10"
}

buildscript {
    repositories {
        google()
        mavenCentral()
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}