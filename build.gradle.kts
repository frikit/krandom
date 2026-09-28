plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.spotless) apply false
    alias(libs.plugins.cyclonedx)
    alias(libs.plugins.nmcp.aggregation)
    alias(libs.plugins.pitest) apply false
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
val jarModules = publishedModules - "bom"

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

// Integration modules generate JaCoCo reports (build/reports/jacoco/test) for visibility. Only :core
// enforces the exact 100% gate (core/build.gradle.kts) and uploads to Codecov.
val coverageReportModules = jarModules - "core"
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
