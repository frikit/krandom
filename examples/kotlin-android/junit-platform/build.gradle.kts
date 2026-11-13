import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.library")
}

val krandomVersion = rootProject.extra["krandomVersion"] as String

android {
    namespace = "io.github.frikit.krandom.examples.android.junitplatform"
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
            // krandom-junit is a JUnit Jupiter extension, so these unit tests run on the JUnit Platform.
            test.useJUnitPlatform()
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
    testImplementation("io.github.frikit:krandom-junit")
    testImplementation("io.github.frikit:krandom-kotlin-dsl")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:6.1.3")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:6.1.3")
}
