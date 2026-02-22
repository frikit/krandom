plugins {
    alias(libs.plugins.kotlin.jvm)
    jacoco
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation(libs.kotlin.stdlib)
    implementation(libs.slf4j.api)
    runtimeOnly(libs.logback.classic)

    testImplementation(libs.commons.validator)
    testImplementation(libs.kotest.runner.junit5)
    testImplementation(libs.kotest.assertions.core)
    testImplementation(libs.junit.jupiter.api)
    testImplementation(libs.junit.jupiter.params)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testRuntimeOnly(libs.junit.platform.launcher)
}

tasks.test {
    useJUnitPlatform {
        includeEngines("kotest", "junit-jupiter")
    }
    jvmArgs("-Xmx512m")
    finalizedBy(tasks.jacocoTestReport)
}

jacoco {
    toolVersion = "0.8.12"
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
            limit {
                counter = "LINE"
                value = "COVEREDRATIO"
                minimum = "0.99".toBigDecimal()
            }
            limit {
                counter = "BRANCH"
                value = "COVEREDRATIO"
                minimum = "0.99".toBigDecimal()
            }
        }
    }
}

tasks.check {
    dependsOn(tasks.jacocoTestCoverageVerification)
}

tasks.clean {
    doLast {
        listOf("${rootDir}/out/", "${rootDir}/logs/").forEach {
            println("Delete [$it]")
            file(it).deleteRecursively()
        }
    }
}
