plugins {
    // JVM attribute rules and Maven POM variant derivation for the root-owned API and consumer
    // compatibility classpaths below; adds no tasks.
    `jvm-ecosystem`
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.spotless) apply false
    alias(libs.plugins.cyclonedx)
    alias(libs.plugins.nmcp.aggregation)
    alias(libs.plugins.pitest) apply false
}

val apiBaselineVersion = providers.gradleProperty("apiBaselineVersion")
val apiCompatibilityExcludes = layout.projectDirectory.file("config/api-compatibility-excludes.txt")
val apiEvolutionAllowlist = layout.projectDirectory.file("config/api-evolution-allowlist.txt")
val japicmpClasspath = configurations.create("japicmpClasspath") {
    isCanBeConsumed = false
    isCanBeResolved = true
    isTransitive = false
}

dependencies {
    add(
        japicmpClasspath.name,
        "com.github.siom79.japicmp:japicmp:${libs.versions.japicmp.get()}:jar-with-dependencies"
    )
}

allprojects {
    group = "io.github.frikit"
    version = (findProperty("releaseVersion") as String?) ?: rootProject.providers.gradleProperty("developmentVersion").get()

    configurations.configureEach {
        resolutionStrategy.failOnNonReproducibleResolution()
    }
}

apply(plugin = "com.diffplug.spotless")

configure<com.diffplug.gradle.spotless.SpotlessExtension> {
    format("markdown") {
        target("README.md", "docs/**/*.md", "docs-site/**/*.md", "examples/**/*.md")
        // Remove trailing whitespace
        trimTrailingWhitespace()
        // Ensure files end with newline
        endWithNewline()
        // Normalize line endings
        lineEndings = com.diffplug.spotless.LineEnding.UNIX
    }
}

// Modules published to Maven Central, read from `publishedModules` in gradle.properties so the build,
// scripts, and documentation checks share one list. Their POM, sources/javadoc jars, manifest,
// and signing setup live in buildSrc/src/main/kotlin/krandom-publishing-conventions.gradle.kts.
val publishedModules = providers.gradleProperty("publishedModules").get()
    .split(',')
    .map(String::trim)
    .filter(String::isNotEmpty)
    .toSet()
val apiModules = publishedModules - "bom"

// Root-owned resolvable configurations request the jars a Java consumer receives for `usage`
// (java-api: compile classpath; java-runtime: runtime classpath).
fun Configuration.requestJvmJars(usage: String) {
    attributes {
        attribute(Usage.USAGE_ATTRIBUTE, objects.named(usage))
        attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category.LIBRARY))
        attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, objects.named(LibraryElements.JAR))
        attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named(Bundling.EXTERNAL))
        attribute(
            TargetJvmEnvironment.TARGET_JVM_ENVIRONMENT_ATTRIBUTE,
            objects.named(TargetJvmEnvironment.STANDARD_JVM)
        )
    }
}

// Splits resolved graphs into the selected component's jar and the classpath of everything else.
fun jarAndDependencyClasspath(
    graphs: List<Configuration>,
    isSelected: (ComponentIdentifier) -> Boolean
): Pair<File, String> {
    val artifacts = graphs.flatMap { graph -> graph.incoming.artifacts.artifacts }
    val selected = artifacts
        .filter { artifact -> isSelected(artifact.id.componentIdentifier) }
        .map { artifact -> artifact.file }
        .distinct()
        .single()
    val dependencyClasspath = artifacts
        .filterNot { artifact -> isSelected(artifact.id.componentIdentifier) }
        .map { artifact -> artifact.file.absolutePath }
        .distinct()
        .joinToString(File.pathSeparator)
    return selected to dependencyClasspath
}

fun isReleasedModule(moduleName: String): (ComponentIdentifier) -> Boolean = { id ->
    id is ModuleComponentIdentifier && id.group == "io.github.frikit" && id.module == "krandom-$moduleName"
}

fun isProjectModule(moduleName: String): (ComponentIdentifier) -> Boolean = { id ->
    id is ProjectComponentIdentifier && id.projectPath == ":$moduleName"
}

val apiEvolutionTasks = mutableListOf<TaskProvider<JavaExec>>()
val releaseComponentGroup = group.toString()
val releaseModuleVersions = publishedModules.associateWith { moduleName ->
    project(":$moduleName").version.toString()
}
val releaseSbomDirectory = layout.buildDirectory.dir("reports/sbom")

dependencies {
    publishedModules.forEach { moduleName ->
        add("nmcpAggregation", project(":$moduleName"))
    }
}

nmcpAggregation {
    centralPortal {
        username.set(providers.environmentVariable("CENTRAL_PORTAL_USERNAME").orElse(""))
        password.set(providers.environmentVariable("CENTRAL_PORTAL_PASSWORD").orElse(""))
        // USER_MANAGED: upload appears in the Central Portal UI for manual "Publish".
        publishingType.set(providers.environmentVariable("CENTRAL_PORTAL_PUBLISHING_TYPE").orElse("USER_MANAGED"))
    }
}

tasks.named<org.cyclonedx.gradle.CyclonedxDirectTask>("cyclonedxDirectBom") {
    enabled = false
}

subprojects {
    tasks.withType<org.cyclonedx.gradle.CyclonedxDirectTask>().configureEach {
        enabled = project.name in publishedModules
        if (enabled) {
            includeConfigs.set(if (project.name == "bom") listOf("classpath") else listOf("runtimeClasspath"))
            skipConfigs.set(listOf(".*test.*", ".*Test.*"))
            includeBuildEnvironment.set(false)
            includeMetadataResolution.set(true)
            componentGroup.set(project.group.toString())
            componentName.set("krandom-${project.name}")
            componentVersion.set(project.version.toString())
            jsonOutput.set(rootProject.layout.buildDirectory.file("reports/sbom/krandom-${project.name}.cdx.json"))
            xmlOutput.set(rootProject.layout.buildDirectory.file("reports/sbom/krandom-${project.name}.cdx.xml"))
        }
    }
}

val releaseSbomTasks = publishedModules.map { moduleName ->
    project(":$moduleName").tasks.named("cyclonedxDirectBom")
}

tasks.register("generateReleaseSboms") {
    group = "distribution"
    description = "Generates CycloneDX JSON/XML SBOMs for every published module."
    dependsOn(releaseSbomTasks)
}

tasks.register("verifyReleaseSboms") {
    group = "verification"
    description = "Generates and validates the CycloneDX SBOMs attached to a release."
    dependsOn("generateReleaseSboms")
    inputs.files(publishedModules.flatMap { moduleName ->
        listOf(
            layout.buildDirectory.file("reports/sbom/krandom-$moduleName.cdx.json"),
            layout.buildDirectory.file("reports/sbom/krandom-$moduleName.cdx.xml")
        )
    })

    doLast {
        val xmlFactory = javax.xml.parsers.DocumentBuilderFactory.newInstance().apply {
            isNamespaceAware = true
            setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
        }
        val xpath = javax.xml.xpath.XPathFactory.newInstance().newXPath()

        publishedModules.forEach { moduleName ->
            val expectedName = "krandom-$moduleName"
            val expectedVersion = releaseModuleVersions.getValue(moduleName)
            val jsonFile = releaseSbomDirectory.get().file("$expectedName.cdx.json").asFile
            val xmlFile = releaseSbomDirectory.get().file("$expectedName.cdx.xml").asFile

            check(jsonFile.isFile && jsonFile.length() > 0) { "Missing release SBOM: $jsonFile" }
            check(xmlFile.isFile && xmlFile.length() > 0) { "Missing release SBOM: $xmlFile" }

            @Suppress("UNCHECKED_CAST")
            val json = groovy.json.JsonSlurper().parse(jsonFile) as Map<String, Any?>
            check(json["bomFormat"] == "CycloneDX") { "$jsonFile is not a CycloneDX document" }
            check(json["specVersion"] == "1.6") { "$jsonFile must use CycloneDX 1.6" }
            @Suppress("UNCHECKED_CAST")
            val jsonMetadata = json["metadata"] as Map<String, Any?>
            @Suppress("UNCHECKED_CAST")
            val jsonComponent = jsonMetadata["component"] as Map<String, Any?>
            check(jsonComponent["group"] == releaseComponentGroup) { "$jsonFile has the wrong component group" }
            check(jsonComponent["name"] == expectedName) { "$jsonFile has the wrong component name" }
            check(jsonComponent["version"] == expectedVersion) { "$jsonFile has the wrong component version" }
            val componentNames = (json["components"] as? List<*>)
                .orEmpty()
                .mapNotNull { component -> (component as? Map<*, *>)?.get("name") as? String }
            check("logback-classic" !in componentNames) { "$jsonFile contains the test-only logging backend" }

            val xml = xmlFactory.newDocumentBuilder().parse(xmlFile)
            check(xml.documentElement.localName == "bom") { "$xmlFile is not a CycloneDX document" }
            check(xml.documentElement.namespaceURI == "http://cyclonedx.org/schema/bom/1.6") {
                "$xmlFile must use CycloneDX 1.6"
            }
            fun xmlComponentValue(name: String): String = xpath.evaluate(
                "/*[local-name()='bom']/*[local-name()='metadata']/*[local-name()='component']/*[local-name()='$name']/text()",
                xml
            )
            check(xmlComponentValue("group") == releaseComponentGroup) { "$xmlFile has the wrong component group" }
            check(xmlComponentValue("name") == expectedName) { "$xmlFile has the wrong component name" }
            check(xmlComponentValue("version") == expectedVersion) { "$xmlFile has the wrong component version" }
        }
    }
}

// japicmp receives the dependency classpath of both jars, so inherited external supertypes and
// annotations (JDK, Kotlin, Kotest, JUnit, Jackson, Spring) are analysed instead of skipped as
// missing classes. Each side unions the compile (java-api) and runtime graphs: JUnit publishes its
// org.apiguardian annotations only for compilation, and implementation dependencies only at runtime.
// compileOnly dependencies are absent from published metadata, yet japicmp must load the types they
// contribute to the API (for example the Spring test meta-annotations on @KrandomTest). Keep this in
// sync with the module build files: a missing entry fails the API check with "Could not load".
val apiCompileOnlyDependencies = mapOf(
    "spring-boot-starter" to listOf(libs.spring.boot.starter.test, libs.junit.jupiter.api)
)

fun apiGraphs(moduleName: String, role: String, dependencyNotation: Any): List<Configuration> =
    listOf("Compile" to Usage.JAVA_API, "Runtime" to Usage.JAVA_RUNTIME).map { (suffix, usage) ->
        configurations.create("${moduleName.replace("-", "")}Api$role$suffix") {
            isCanBeConsumed = false
            isCanBeResolved = true
            requestJvmJars(usage)
        }.also { graph ->
            dependencies.add(graph.name, dependencyNotation)
            if (usage == Usage.JAVA_API) {
                apiCompileOnlyDependencies[moduleName].orEmpty().forEach { compileOnly ->
                    dependencies.addProvider(graph.name, compileOnly)
                }
            }
        }
    }

// Released jar plus its published dependencies (old side); candidate jar plus current dependencies (new side).
val apiBaselineGraphs = apiModules.associateWith { moduleName ->
    apiGraphs(moduleName, "Baseline", "io.github.frikit:krandom-$moduleName:${apiBaselineVersion.get()}")
}
val apiCandidateGraphs = apiModules.associateWith { moduleName ->
    apiGraphs(moduleName, "Candidate", project(":$moduleName"))
}

val apiCompatibilityTasks = apiModules.map { moduleName ->
    val taskSuffix = moduleName
        .split('-')
        .joinToString("") { part -> part.replaceFirstChar(Char::uppercaseChar) }
    val baselineGraphs = apiBaselineGraphs.getValue(moduleName)
    val candidateGraphs = apiCandidateGraphs.getValue(moduleName)

    val compatibilityTask = tasks.register<JavaExec>("check${taskSuffix}ApiCompatibility") {
        group = "verification"
        description = "Checks krandom-$moduleName against the ${apiBaselineVersion.get()} public API."
        dependsOn(":$moduleName:jar")
        inputs.files(baselineGraphs, candidateGraphs)
        classpath = japicmpClasspath
        mainClass.set("japicmp.JApiCmp")

        doFirst {
            val (oldJar, oldClasspath) = jarAndDependencyClasspath(baselineGraphs, isReleasedModule(moduleName))
            val (newJar, newClasspath) = jarAndDependencyClasspath(candidateGraphs, isProjectModule(moduleName))
            val reportDirectory = layout.buildDirectory.dir("reports/japicmp/$moduleName").get().asFile
            reportDirectory.mkdirs()

            val compatibilityArgs = mutableListOf(
                "--old", oldJar.absolutePath,
                "--new", newJar.absolutePath,
                "--old-classpath", oldClasspath,
                "--new-classpath", newClasspath,
                "-a", "public",
                "--only-modified",
                "--error-on-binary-incompatibility",
                "--error-on-source-incompatibility",
                "--html-file", reportDirectory.resolve("report.html").absolutePath,
                "--xml-file", reportDirectory.resolve("report.xml").absolutePath,
                "--report-only-filename"
            )
            val exclusions = apiCompatibilityExcludes.asFile
                .readLines()
                .map(String::trim)
                .filter { line -> line.isNotEmpty() && !line.startsWith('#') }
            if (exclusions.isNotEmpty()) {
                compatibilityArgs += listOf(
                    "--exclude", exclusions.joinToString(";"),
                    "--no-error-on-exclusion-incompatibility"
                )
            }
            args = compatibilityArgs
        }
    }

    apiEvolutionTasks += tasks.register<JavaExec>("check${taskSuffix}ApiEvolution") {
        group = "verification"
        description = "Rejects unclassified public API changes in krandom-$moduleName."
        dependsOn(":$moduleName:jar")
        inputs.files(baselineGraphs, candidateGraphs)
        classpath = japicmpClasspath
        mainClass.set("japicmp.JApiCmp")

        doFirst {
            val (oldJar, oldClasspath) = jarAndDependencyClasspath(baselineGraphs, isReleasedModule(moduleName))
            val (newJar, newClasspath) = jarAndDependencyClasspath(candidateGraphs, isProjectModule(moduleName))
            val reportDirectory = layout.buildDirectory.dir("reports/api-evolution/$moduleName").get().asFile
            reportDirectory.mkdirs()

            val evolutionArgs = mutableListOf(
                "--old", oldJar.absolutePath,
                "--new", newJar.absolutePath,
                "--old-classpath", oldClasspath,
                "--new-classpath", newClasspath,
                "-a", "public",
                "--only-modified",
                "--error-on-modifications",
                "--html-file", reportDirectory.resolve("report.html").absolutePath,
                "--xml-file", reportDirectory.resolve("report.xml").absolutePath,
                "--report-only-filename"
            )
            val allowedChanges = apiEvolutionAllowlist.asFile
                .readLines()
                .map(String::trim)
                .filter { line -> line.isNotEmpty() && !line.startsWith('#') }
            if (allowedChanges.isNotEmpty()) {
                evolutionArgs += listOf(
                    "--exclude", allowedChanges.joinToString(";"),
                    "--no-error-on-exclusion-incompatibility"
                )
                // japicmp drops excluded classes from its class pool, so a still-analysed subclass or
                // implementation of an allowlisted class fails with "Could not load". Tolerate exactly
                // those missing supertypes; checkApiCompatibility has no exclusions and loads them all.
                // japicmp 0.26.1's CLI parser loops forever on a repeated option, so pass one alternation.
                val allowedClasses = allowedChanges.filter { entry -> '#' !in entry && !entry.startsWith('@') }
                if (allowedClasses.isNotEmpty()) {
                    evolutionArgs += listOf(
                        "--ignore-missing-classes-by-regex",
                        allowedClasses.joinToString("|") { allowedClass -> Regex.escape(allowedClass) }
                    )
                }
            }
            args = evolutionArgs
        }
    }

    compatibilityTask
}

tasks.register("checkApiCompatibility") {
    group = "verification"
    description = "Checks all published jar modules against the configured GA baseline."
    dependsOn(apiCompatibilityTasks)
}

tasks.register("checkApiEvolution") {
    group = "verification"
    description = "Rejects public API changes not classified in the evolution allowlist."
    dependsOn(apiEvolutionTasks)
}

tasks.register("checkApiContract") {
    group = "verification"
    description = "Checks compatibility and rejects unclassified public API evolution."
    dependsOn("checkApiCompatibility", "checkApiEvolution")
}

val emptyApiJar = tasks.register<org.gradle.jvm.tasks.Jar>("emptyApiJar") {
    archiveFileName.set("empty-api.jar")
    destinationDirectory.set(layout.buildDirectory.dir("tmp/api-inventory"))
}

val apiInventoryTasks = apiModules.map { moduleName ->
    val taskSuffix = moduleName
        .split('-')
        .joinToString("") { part -> part.replaceFirstChar(Char::uppercaseChar) }

    val candidateGraphs = apiCandidateGraphs.getValue(moduleName)

    tasks.register<JavaExec>("generate${taskSuffix}ApiInventory") {
        group = "documentation"
        description = "Generates the complete public API inventory for krandom-$moduleName."
        dependsOn(emptyApiJar, ":$moduleName:jar")
        inputs.files(candidateGraphs)
        classpath = japicmpClasspath
        mainClass.set("japicmp.JApiCmp")
        standardOutput = java.io.OutputStream.nullOutputStream()

        doFirst {
            val (newJar, newClasspath) = jarAndDependencyClasspath(candidateGraphs, isProjectModule(moduleName))
            val reportDirectory = layout.buildDirectory.dir("reports/api-inventory/$moduleName").get().asFile
            reportDirectory.mkdirs()

            args = listOf(
                "--old", emptyApiJar.get().archiveFile.get().asFile.absolutePath,
                "--new", newJar.absolutePath,
                // The empty jar has no types to resolve; both sides share the candidate classpath.
                "--old-classpath", newClasspath,
                "--new-classpath", newClasspath,
                "-a", "public",
                "--html-file", reportDirectory.resolve("inventory.html").absolutePath,
                "--xml-file", reportDirectory.resolve("inventory.xml").absolutePath,
                "--report-only-filename"
            )
        }

        doLast {
            val reportDirectory = layout.buildDirectory.dir("reports/api-inventory/$moduleName").get().asFile
            val timestamp = Regex("""\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}\.\d{3}[+-]\d{4}""")
            listOf("inventory.html", "inventory.xml").forEach { reportName ->
                val report = reportDirectory.resolve(reportName)
                report.writeText(report.readText().replace(timestamp, "GENERATED_AT_RUNTIME"))
            }
        }
    }
}

tasks.register("generatePublicApiInventory") {
    group = "documentation"
    description = "Generates full HTML/XML public API inventories for every published jar module."
    dependsOn(apiInventoryTasks)
}

// External-consumer contract: a consumer and extension compiled against an earlier 2.x core must
// run unchanged on the candidate with identical output (scripts/verify_v2_consumer_compatibility.sh).
val v2ConsumerBaselineVersion = "2.2.0"
val v2ConsumerBaseline = configurations.create("v2ConsumerBaseline") {
    isCanBeConsumed = false
    isCanBeResolved = true
    isTransitive = false
    requestJvmJars(Usage.JAVA_RUNTIME)
}
val v2ConsumerCandidate = configurations.create("v2ConsumerCandidate") {
    isCanBeConsumed = false
    isCanBeResolved = true
    requestJvmJars(Usage.JAVA_RUNTIME)
}

dependencies {
    add(v2ConsumerBaseline.name, "io.github.frikit:krandom-core:$v2ConsumerBaselineVersion")
    add(v2ConsumerCandidate.name, project(":core"))
}

tasks.register<Exec>("verifyV2ConsumerCompatibility") {
    group = "verification"
    description = "Runs a consumer and extension compiled against krandom-core $v2ConsumerBaselineVersion on the candidate."
    val script = layout.projectDirectory.file("scripts/verify_v2_consumer_compatibility.sh")
    inputs.files(v2ConsumerBaseline, v2ConsumerCandidate, script, "scripts/compatibility/V2Consumer.java")
    // The script compiles and runs with java/javac from PATH; use the JDK that runs Gradle (21+).
    environment("PATH", listOf(File(System.getProperty("java.home"), "bin").absolutePath, System.getenv("PATH"))
        .joinToString(File.pathSeparator))

    doFirst {
        val (candidateJar, runtimeClasspath) = jarAndDependencyClasspath(listOf(v2ConsumerCandidate), isProjectModule("core"))
        commandLine(
            "bash",
            script.asFile.absolutePath,
            v2ConsumerBaseline.singleFile.absolutePath,
            candidateJar.absolutePath,
            runtimeClasspath
        )
    }
}

// Integration modules generate JaCoCo reports (build/reports/jacoco/test) for visibility. Only :core
// enforces the exact 100% gate (core/build.gradle.kts) and uploads to Codecov.
val coverageReportModules = apiModules - "core"
val jacocoToolVersion = libs.versions.jacoco.get()

subprojects {
    if (name in coverageReportModules) {
        apply(plugin = "jacoco")
        configure<JacocoPluginExtension> {
            toolVersion = jacocoToolVersion
        }
        tasks.withType<JacocoReport>().configureEach {
            reports {
                csv.required = true
                xml.required = true
                html.required = true
            }
        }
        tasks.withType<Test>().configureEach {
            finalizedBy("jacocoTestReport")
        }
    }
}

subprojects {
    apply(plugin = "com.diffplug.spotless")
    if (name in publishedModules) {
        apply(plugin = "krandom-publishing-conventions")
    }

    afterEvaluate {
        configure<com.diffplug.gradle.spotless.SpotlessExtension> {
            java {
                target("src/**/*.java")
                licenseHeaderFile(rootProject.file("LICENSE_HEADER"))
            }
            if (plugins.hasPlugin("org.jetbrains.kotlin.jvm")) {
                kotlin {
                    licenseHeaderFile(rootProject.file("LICENSE_HEADER"))
                }
            }
        }
    }
}
