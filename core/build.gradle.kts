plugins {
    `java-library`
    jacoco
    alias(libs.plugins.pitest)
}

val coverageThreshold = "1.0".toBigDecimal()
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
    systemProperty("krandom.rootDir", rootProject.projectDir.absolutePath)
    finalizedBy(tasks.jacocoTestReport)
}

pitest {
    targetClasses.set(setOf(
        "io.github.frikit.krandom.generator.schema.Schema",
        "io.github.frikit.krandom.generator.schema.SchemaParser",
        "io.github.frikit.krandom.generator.schema.SchemaValueProvider",
        "io.github.frikit.krandom.generator.location.RegistryLookup",
        "io.github.frikit.krandom.generator.object.BeanValidationSupport",
        "io.github.frikit.krandom.generator.object.BuiltInProviderResolver",
        "io.github.frikit.krandom.generator.object.ObjectFaker",
        "io.github.frikit.krandom.generator.object.ObjectGenerator",
        "io.github.frikit.krandom.generator.object.ObjectModel",
        "io.github.frikit.krandom.generator.object.PropertyPath"
    ))
    targetTests.set(setOf(
        "io.github.frikit.krandom.generator.schema.*",
        "io.github.frikit.krandom.generator.location.RegistryLookupTest",
        "io.github.frikit.krandom.generator.object.*"
    ))
    junit5PluginVersion.set("1.2.3")
    outputFormats.set(setOf("HTML", "XML"))
    timestampedReports.set(false)
    threads.set(1)
    mutationThreshold.set(85)
    coverageThreshold.set(98)
}

val providerCatalogDocumentationFile = rootProject.layout.projectDirectory.file("docs/reference/provider-catalog.md")

tasks.register<JavaExec>("generateProviderCatalogDocumentation") {
    group = "documentation"
    description = "Generates the built-in provider reference from ProviderCatalog."
    dependsOn(tasks.named("testClasses"))
    classpath = sourceSets.test.get().runtimeClasspath
    mainClass.set("io.github.frikit.krandom.generator.provider.ProviderCatalogDocumentation")
    args(providerCatalogDocumentationFile.asFile.absolutePath)
}

tasks.register("printRuntimeClasspath") {
    group = "help"
    description = "Prints the core runtime classpath for standalone smoke fixtures."
    doLast {
        println(configurations.runtimeClasspath.get().asPath)
    }
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
