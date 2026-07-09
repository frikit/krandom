plugins {
    `java-library`
    jacoco
}

val coverageThreshold = "0.999".toBigDecimal()
val coverageCounters = listOf(
    // JaCoCo does not expose a STATEMENT counter; INSTRUCTION is the closest equivalent.
    "INSTRUCTION",
    "LINE",
    "BRANCH",
    "COMPLEXITY",
    "METHOD",
    "CLASS"
)

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

dependencies {
    api(libs.jspecify)
    implementation(libs.slf4j.api)
    implementation(libs.objenesis)
    implementation(libs.jakarta.validation.api)

    testImplementation(libs.commons.validator)
    testImplementation(libs.hibernate.validator)
    testImplementation(libs.junit.jupiter.api)
    testImplementation(libs.junit.jupiter.params)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testRuntimeOnly(libs.junit.platform.launcher)
    // Logging backend + capture API (ListAppender) for the build's own tests only;
    // intentionally NOT shipped to consumers.
    testImplementation(libs.logback.classic)
}

tasks.test {
    useJUnitPlatform()
    jvmArgs("-Xmx512m")
    finalizedBy(tasks.jacocoTestReport)
}

jacoco {
    toolVersion = "0.8.15"
    reportsDirectory = layout.buildDirectory.dir("jacoco")
}

tasks.jacocoTestReport {
    reports {
        csv.required = true
        xml.required = true
        html.required = true
        csv.outputLocation = layout.buildDirectory.file("jacoco/jacoco.csv")
        xml.outputLocation = layout.buildDirectory.file("jacoco/jacoco.xml")
        html.outputLocation = layout.buildDirectory.dir("jacoco/html")
    }
    sourceSets(sourceSets.main.get())
}

tasks.jacocoTestCoverageVerification {
    violationRules {
        rule {
            coverageCounters.forEach { coverageCounter ->
                limit {
                    counter = coverageCounter
                    value = "COVEREDRATIO"
                    minimum = coverageThreshold
                }
            }
        }
    }
}

tasks.javadoc {
    options {
        (this as StandardJavadocDocletOptions).apply {
            addStringOption("Xdoclint:all,-missing", "-Werror")
            encoding = "UTF-8"
            charSet = "UTF-8"
        }
    }
}

tasks.check {
    dependsOn(tasks.jacocoTestCoverageVerification)
    dependsOn(tasks.javadoc)
}

tasks.clean {
    doLast {
        listOf("${rootDir}/out/", "${rootDir}/logs/").forEach {
            println("Delete [$it]")
            file(it).deleteRecursively()
        }
    }
}
