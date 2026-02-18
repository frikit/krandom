plugins {
    alias(libs.plugins.kotlin.jvm) apply false
}

allprojects {
    group = "org.github.krandom"
    version = "1.0-SNAPSHOT"

    repositories {
        mavenLocal()
        mavenCentral()
    }
}
