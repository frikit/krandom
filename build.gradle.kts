plugins {
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

// Modules published to Maven Central. Their POM, sources/javadoc jars, manifest,
// and signing setup live in buildSrc/src/main/kotlin/krandom-publishing-conventions.gradle.kts.
val publishedModules = setOf("bom", "core", "jackson", "junit", "spring-boot-starter", "kotest-extensions", "kotlin-dsl")
val apiModules = publishedModules - "bom"
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

val apiCompatibilityTasks = apiModules.map { moduleName ->
    val taskSuffix = moduleName
        .split('-')
        .joinToString("") { part -> part.replaceFirstChar(Char::uppercaseChar) }
    val baselineConfiguration = configurations.create("${moduleName.replace("-", "")}ApiBaseline") {
        isCanBeConsumed = false
        isCanBeResolved = true
        isTransitive = false
    }
    dependencies.add(
        baselineConfiguration.name,
        "io.github.frikit:krandom-$moduleName:${apiBaselineVersion.get()}"
    )

    val compatibilityTask = tasks.register<JavaExec>("check${taskSuffix}ApiCompatibility") {
        group = "verification"
        description = "Checks krandom-$moduleName against the ${apiBaselineVersion.get()} public API."
        dependsOn(":$moduleName:jar")
        classpath = japicmpClasspath
        mainClass.set("japicmp.JApiCmp")

        doFirst {
            val moduleProject = project(":$moduleName")
            val oldArtifacts = baselineConfiguration.resolvedConfiguration.resolvedArtifacts
            val oldJar = oldArtifacts.single { artifact ->
                artifact.moduleVersion.id.group == "io.github.frikit" &&
                    artifact.name == "krandom-$moduleName"
            }.file
            val newJar = moduleProject.tasks.named<org.gradle.jvm.tasks.Jar>("jar")
                .get()
                .archiveFile
                .get()
                .asFile
            val reportDirectory = layout.buildDirectory.dir("reports/japicmp/$moduleName").get().asFile
            reportDirectory.mkdirs()

            val compatibilityArgs = mutableListOf(
                "--old", oldJar.absolutePath,
                "--new", newJar.absolutePath,
                "-a", "public",
                "--only-modified",
                "--ignore-missing-classes",
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
        classpath = japicmpClasspath
        mainClass.set("japicmp.JApiCmp")

        doFirst {
            val moduleProject = project(":$moduleName")
            val oldArtifacts = baselineConfiguration.resolvedConfiguration.resolvedArtifacts
            val oldJar = oldArtifacts.single { artifact ->
                artifact.moduleVersion.id.group == "io.github.frikit" &&
                    artifact.name == "krandom-$moduleName"
            }.file
            val newJar = moduleProject.tasks.named<org.gradle.jvm.tasks.Jar>("jar")
                .get()
                .archiveFile
                .get()
                .asFile
            val reportDirectory = layout.buildDirectory.dir("reports/api-evolution/$moduleName").get().asFile
            reportDirectory.mkdirs()

            val evolutionArgs = mutableListOf(
                "--old", oldJar.absolutePath,
                "--new", newJar.absolutePath,
                "-a", "public",
                "--only-modified",
                "--ignore-missing-classes",
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

    tasks.register<JavaExec>("generate${taskSuffix}ApiInventory") {
        group = "documentation"
        description = "Generates the complete public API inventory for krandom-$moduleName."
        dependsOn(emptyApiJar, ":$moduleName:jar")
        classpath = japicmpClasspath
        mainClass.set("japicmp.JApiCmp")
        standardOutput = java.io.OutputStream.nullOutputStream()

        doFirst {
            val moduleProject = project(":$moduleName")
            val newJar = moduleProject.tasks.named<org.gradle.jvm.tasks.Jar>("jar")
                .get()
                .archiveFile
                .get()
                .asFile
            val reportDirectory = layout.buildDirectory.dir("reports/api-inventory/$moduleName").get().asFile
            reportDirectory.mkdirs()

            args = listOf(
                "--old", emptyApiJar.get().archiveFile.get().asFile.absolutePath,
                "--new", newJar.absolutePath,
                "-a", "public",
                "--ignore-missing-classes",
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
