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
include(":spring-boot-starter")
include(":kotest-extensions")
include(":jqwik-extensions")
include(":kotlin-dsl")
include(":benchmarks")
