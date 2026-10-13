import net.ltgt.gradle.errorprone.errorprone

plugins {
    `java-library`
    jacoco
    alias(libs.plugins.pitest)
    alias(libs.plugins.errorprone)
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
    // Bean Validation is optional for consumers: core compiles against the API, detects it at
    // runtime (BeanValidationSupport#isAvailable) and ignores constraints when it is absent.
    compileOnly(libs.jakarta.validation.api)

    testImplementation(libs.jakarta.validation.api)
    testImplementation(libs.commons.validator)
    testImplementation(libs.hibernate.validator)
    testImplementation(libs.junit.jupiter.api)
    testImplementation(libs.junit.jupiter.params)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testRuntimeOnly(libs.junit.platform.launcher)
    // Logging backend + capture API (ListAppender) for the build's own tests only;
    // intentionally NOT shipped to consumers.
    testImplementation(libs.logback.classic)

    // Nullness verification only: Error Prone hosts NullAway, every other Error Prone check is off.
    errorprone(libs.errorprone.core)
    errorprone(libs.nullaway)
}

// Every core package is @NullMarked (JSpecify). NullAway verifies those contracts in JSpecify mode
// for the main sources; the test fixtures are deliberately unannotated and stay unchecked.
tasks.withType<JavaCompile>().configureEach {
    // Report every finding instead of javac's first 100.
    options.compilerArgs.addAll(listOf("-Xmaxerrs", "1000"))
    options.errorprone {
        disableAllChecks.set(true)
        error("NullAway")
        option("NullAway:AnnotatedPackages", "io.github.frikit.krandom")
        option("NullAway:JSpecifyMode", "true")
    }
}

tasks.compileTestJava {
    options.errorprone.enabled.set(false)
}

// Test fixtures keep their constructor parameter names so the object engine resolves constructor
// arguments by name, as it does for consumers that compile with -parameters.
tasks.compileTestJava {
    options.compilerArgs.add("-parameters")
}

tasks.test {
    useJUnitPlatform()
    jvmArgs("-Xmx512m")
    systemProperty("krandom.rootDir", rootProject.projectDir.absolutePath)
    finalizedBy(tasks.jacocoTestReport)
}

pitest {
    targetClasses.set(setOf(
        "io.github.frikit.krandom.generator.GenerationRecipe",
        "io.github.frikit.krandom.generator.GenerationRecipe$*",
        "io.github.frikit.krandom.generator.GeneratorConfig",
        "io.github.frikit.krandom.generator.GeneratorConfig$*",
        "io.github.frikit.krandom.generator.color.ColorGenerator",
        "io.github.frikit.krandom.generator.finance.CryptoAddressGenerator",
        "io.github.frikit.krandom.generator.identifier.UUIDGenerator",
        "io.github.frikit.krandom.generator.schema.Schema",
        "io.github.frikit.krandom.generator.schema.SchemaParser",
        "io.github.frikit.krandom.generator.schema.SchemaValueProvider",
        "io.github.frikit.krandom.generator.location.RegistryLookup",
        "io.github.frikit.krandom.generator.object.BeanValidationSupport",
        "io.github.frikit.krandom.generator.object.BuiltInProviderResolver",
        "io.github.frikit.krandom.generator.object.FieldGeneratorResolver",
        "io.github.frikit.krandom.generator.object.FieldGeneratorResolver$*",
        "io.github.frikit.krandom.generator.object.ObjectFaker",
        "io.github.frikit.krandom.generator.object.ObjectFieldStreamPlanner",
        "io.github.frikit.krandom.generator.object.ObjectGenerator",
        "io.github.frikit.krandom.generator.object.ObjectModel",
        "io.github.frikit.krandom.generator.object.PropertyPath",
        "io.github.frikit.krandom.generator.object.SemanticCoherenceAdjuster",
        "io.github.frikit.krandom.generator.object.SemanticCoherenceAdjuster$*",
        "io.github.frikit.krandom.generator.text.NextWordGenerator",
        "io.github.frikit.krandom.generator.user.EmailGenerator"
    ))
    targetTests.set(setOf(
        "io.github.frikit.krandom.generator.ChildStreamPolicyTest",
        "io.github.frikit.krandom.generator.ClockSnapshotTest",
        "io.github.frikit.krandom.generator.EnvironmentDeterminismTest",
        "io.github.frikit.krandom.generator.GenerationRecipe*",
        "io.github.frikit.krandom.generator.GeneratorConfigTest",
        "io.github.frikit.krandom.generator.GeneratorProfileTest",
        "io.github.frikit.krandom.generator.RandomSourceContractTest",
        "io.github.frikit.krandom.generator.color.ColorGeneratorTest",
        "io.github.frikit.krandom.generator.finance.Phase3FinanceGeneratorsTest",
        "io.github.frikit.krandom.generator.identifier.UUIDGeneratorTest",
        "io.github.frikit.krandom.generator.schema.*",
        "io.github.frikit.krandom.generator.location.RegistryLookupTest",
        "io.github.frikit.krandom.generator.object.*",
        "io.github.frikit.krandom.generator.text.NextWordGeneratorTest",
        "io.github.frikit.krandom.generator.user.EmailGeneratorTest"
    ))
    pitestVersion.set(libs.versions.pitest.core)
    junit5PluginVersion.set(libs.versions.pitest.junit5.plugin)
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
    toolVersion = libs.versions.jacoco.get()
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
