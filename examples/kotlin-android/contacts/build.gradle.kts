import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.library")
}

val krandomVersion = rootProject.extra["krandomVersion"] as String

android {
    namespace = "io.github.frikit.krandom.examples.android.contacts"
    compileSdk = 36

    defaultConfig {
        minSdk = 24
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    testOptions {
        unitTests.all { test ->
            test.testLogging { events("passed", "skipped", "failed") }
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
    }
}

dependencies {
    testImplementation(platform("io.github.frikit:krandom-bom:$krandomVersion"))
    testImplementation("io.github.frikit:krandom-core")
    testImplementation("io.github.frikit:krandom-kotlin-dsl")
    testImplementation("junit:junit:4.13.2")
    // Android classes are stubs in local unit tests, so a mock stands in for a Uri.
    testImplementation("org.mockito:mockito-core:5.24.0")
}
