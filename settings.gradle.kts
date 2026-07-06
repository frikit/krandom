pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

plugins {
    id("com.gradleup.nmcp.settings") version "1.6.1"
}

val currentJava = org.gradle.api.JavaVersion.current()
if (!currentJava.isCompatibleWith(org.gradle.api.JavaVersion.VERSION_21)) {
    throw org.gradle.api.GradleException(
        "kRandom requires Java 21+ to build. Current runtime: "
            + System.getProperty("java.version")
            + " at "
            + System.getProperty("java.home")
            + ". Set JAVA_HOME to a JDK 21 installation and retry."
    )
}

rootProject.name = "krandom"

include(":core")
include(":jackson")
include(":junit")
include(":spring-boot-starter")
include(":kotest-extensions")
include(":kotlin-dsl")
include(":benchmarks")
include(":examples-e2e")

nmcpSettings {
    centralPortal {
        username = System.getenv("CENTRAL_PORTAL_USERNAME") ?: ""
        password = System.getenv("CENTRAL_PORTAL_PASSWORD") ?: ""
        // USER_MANAGED: upload appears in the Central Portal UI for manual "Publish".
        // Flip to AUTOMATIC for hands-off subsequent releases.
        publishingType = System.getenv("CENTRAL_PORTAL_PUBLISHING_TYPE") ?: "USER_MANAGED"
    }
}
