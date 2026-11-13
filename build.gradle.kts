plugins {
    // JVM attribute rules and Maven POM variant derivation for the root-owned API baseline classpaths
    // below; adds no tasks.
    `jvm-ecosystem`
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.spotless) apply false
    alias(libs.plugins.cyclonedx)
    alias(libs.plugins.nmcp.aggregation)
    alias(libs.plugins.pitest) apply false
    alias(libs.plugins.errorprone) apply false
}

allprojects {
    group = "io.github.frikit"
    version =
        (findProperty("releaseVersion") as String?) ?: rootProject.providers.gradleProperty("developmentVersion").get()

    configurations.configureEach {
        resolutionStrategy.failOnNonReproducibleResolution()
    }
}

apply(plugin = "com.diffplug.spotless")

configure<com.diffplug.gradle.spotless.SpotlessExtension> {
    // Sources outside the Gradle subprojects (standalone consumer examples and the native-image smoke
    // program) follow the same formatter and license header as the modules; see `subprojects` below.
    java {
        target("examples/**/src/**/*.java", "scripts/**/*.java")
        targetExclude("**/build/**")
        palantirJavaFormat(
            libs.versions.palantir.java.format
                .get(),
        )
        formatAnnotations()
        licenseHeaderFile(file("LICENSE_HEADER"))
    }
    kotlin {
        target("examples/**/src/**/*.kt")
        targetExclude("**/build/**")
        ktlint(libs.versions.ktlint.get())
        licenseHeaderFile(file("LICENSE_HEADER"))
    }
    kotlinGradle {
        target("*.gradle.kts", "buildSrc/**/*.gradle.kts", "examples/**/*.gradle.kts")
        targetExclude("**/build/**")
        ktlint(libs.versions.ktlint.get())
    }
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
val publishedModules =
    providers
        .gradleProperty("publishedModules")
        .get()
        .split(',')
        .map(String::trim)
        .filter(String::isNotEmpty)
        .toSet()
val jarModules = publishedModules - "bom"

val releaseComponentGroup = group.toString()
val releaseModuleVersions =
    publishedModules.associateWith { moduleName ->
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

val releaseSbomTasks =
    publishedModules.map { moduleName ->
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
    inputs.files(
        publishedModules.flatMap { moduleName ->
            listOf(
                layout.buildDirectory.file("reports/sbom/krandom-$moduleName.cdx.json"),
                layout.buildDirectory.file("reports/sbom/krandom-$moduleName.cdx.xml"),
            )
        },
    )

    doLast {
        val xmlFactory =
            javax.xml.parsers.DocumentBuilderFactory.newInstance().apply {
                isNamespaceAware = true
                setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
            }
        val xpath =
            javax.xml.xpath.XPathFactory
                .newInstance()
                .newXPath()

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
            val componentNames =
                (json["components"] as? List<*>)
                    .orEmpty()
                    .mapNotNull { component -> (component as? Map<*, *>)?.get("name") as? String }
            check("logback-classic" !in componentNames) { "$jsonFile contains the test-only logging backend" }

            val xml = xmlFactory.newDocumentBuilder().parse(xmlFile)
            check(xml.documentElement.localName == "bom") { "$xmlFile is not a CycloneDX document" }
            check(xml.documentElement.namespaceURI == "http://cyclonedx.org/schema/bom/1.6") {
                "$xmlFile must use CycloneDX 1.6"
            }

            fun xmlComponentValue(name: String): String =
                xpath.evaluate(
                    "/*[local-name()='bom']/*[local-name()='metadata']/*[local-name()='component']/*[local-name()='$name']/text()",
                    xml,
                )
            check(xmlComponentValue("group") == releaseComponentGroup) { "$xmlFile has the wrong component group" }
            check(xmlComponentValue("name") == expectedName) { "$xmlFile has the wrong component name" }
            check(xmlComponentValue("version") == expectedVersion) { "$xmlFile has the wrong component version" }
        }
    }
}

// Public API compatibility (VERSIONING.md, docs/release-runbook.md): every published jar keeps the
// public API of the apiBaselineVersion release. Additions pass; a removal or an incompatible change
// fails `checkApiCompatibility`, which `check` runs.
// - Java modules: japicmp compares the jar with the released jar from Maven Central, together with the
//   dependency classpath of each, limited to public and protected API in the public-API packages: the
//   unqualified exports of the released krandom-core module descriptor and the one package of each
//   integration module. The JPMS module name must stay, and so must the export of each of those packages.
// - Kotlin modules: the Kotlin Gradle plugin dumps the ABI of the current code, leaving out `internal`
//   declarations that are public in bytecode, and every declaration of the baseline dump committed as
//   <module>/api/<module>-<apiBaselineVersion>.api must still be there with compatible modifiers.
val apiBaselineVersion = providers.gradleProperty("apiBaselineVersion").get()
val integrationApiPackages =
    mapOf(
        "jackson" to "io.github.frikit.krandom.jackson",
        "junit" to "io.github.frikit.krandom.junit",
        "spring-boot-starter" to "io.github.frikit.krandom.spring",
    )
val javaApiModules = setOf("core") + integrationApiPackages.keys
val kotlinApiModules = setOf("kotlin-dsl", "kotest-extensions")
check(javaApiModules + kotlinApiModules == jarModules) {
    "The API compatibility check must cover exactly the published jar modules: $jarModules"
}
val apiCompatibilityAdvice =
    "To fix it, keep each $apiBaselineVersion declaration as it was: retire one by deprecating it and naming its " +
        "replacement (only a major release removes it), and give a method added to an interface or abstract class " +
        "an implementation (docs/release-runbook.md, Public API compatibility)."

fun apiTaskSuffix(moduleName: String): String =
    moduleName
        .split('-')
        .joinToString("") { part -> part.replaceFirstChar(Char::uppercaseChar) }

val japicmpClasspath =
    configurations.create("japicmpClasspath") {
        isCanBeConsumed = false
        isCanBeResolved = true
        isTransitive = false
    }

dependencies {
    add(japicmpClasspath.name, "com.github.siom79.japicmp:japicmp:${libs.versions.japicmp.get()}:jar-with-dependencies")
}

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
            objects.named(TargetJvmEnvironment.STANDARD_JVM),
        )
    }
}

// japicmp receives the dependency classpath of both jars, so inherited external supertypes and
// annotations are analysed instead of failing as missing classes. Each side unions the compile
// (java-api) and runtime graphs: JUnit publishes its org.apiguardian annotations only for
// compilation, and implementation dependencies only at runtime. compileOnly dependencies are absent
// from published metadata, yet japicmp must load the types they contribute to the API (the Spring
// test meta-annotations on @KrandomTest). Keep this in sync with the module build files: a missing
// entry fails the check with "Could not load".
val apiCompileOnlyDependencies =
    mapOf(
        "spring-boot-starter" to listOf(libs.spring.boot.starter.test, libs.junit.jupiter.api),
    )

fun apiGraphs(
    moduleName: String,
    role: String,
    dependencyNotation: Any,
): List<Configuration> =
    listOf("Compile" to Usage.JAVA_API, "Runtime" to Usage.JAVA_RUNTIME).map { (suffix, usage) ->
        configurations
            .create("${moduleName.replace("-", "")}Api$role$suffix") {
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

// Splits resolved graphs into the selected component's jar and the classpath of everything else.
fun jarAndDependencyClasspath(
    graphs: List<Configuration>,
    isSelected: (ComponentIdentifier) -> Boolean,
): Pair<File, String> {
    val artifacts = graphs.flatMap { graph -> graph.incoming.artifacts.artifacts }
    val selected =
        artifacts
            .filter { artifact -> isSelected(artifact.id.componentIdentifier) }
            .map { artifact -> artifact.file }
            .distinct()
            .single()
    val dependencyClasspath =
        artifacts
            .filterNot { artifact -> isSelected(artifact.id.componentIdentifier) }
            .map { artifact -> artifact.file.absolutePath }
            .distinct()
            .joinToString(File.pathSeparator)
    return selected to dependencyClasspath
}

fun isReleasedModule(moduleName: String): (ComponentIdentifier) -> Boolean =
    { id ->
        id is ModuleComponentIdentifier && id.group == "io.github.frikit" && id.module == "krandom-$moduleName"
    }

fun isProjectModule(moduleName: String): (ComponentIdentifier) -> Boolean =
    { id -> id is ProjectComponentIdentifier && id.projectPath == ":$moduleName" }

// The JPMS name of a jar: its module descriptor's, else the Automatic-Module-Name manifest attribute.
fun jpmsModuleName(jar: File): String? =
    java.util.jar.JarFile(jar).use { file ->
        file.getEntry("module-info.class")?.let { entry ->
            file.getInputStream(entry).use { input ->
                java.lang.module.ModuleDescriptor
                    .read(input)
                    .name()
            }
        } ?: file.manifest?.mainAttributes?.getValue("Automatic-Module-Name")
    }

// Packages a jar exports to every module: the unqualified exports of its module descriptor or, for an
// automatic module without one, every package that holds a class.
fun exportedPackages(jar: File): Set<String> =
    java.util.jar.JarFile(jar).use { file ->
        val descriptor =
            file.getEntry("module-info.class")?.let { entry ->
                file.getInputStream(entry).use { input ->
                    java.lang.module.ModuleDescriptor
                        .read(input)
                }
            }
        descriptor
            ?.exports()
            ?.filterNot { export -> export.isQualified }
            ?.map { export -> export.source() }
            ?.toSortedSet()
            ?: file
                .entries()
                .asSequence()
                .map { entry -> entry.name }
                .filter { name -> name.endsWith(".class") && "/" in name && !name.startsWith("META-INF/") }
                .map { name -> name.substringBeforeLast('/').replace('/', '.') }
                .toSortedSet()
    }

fun xmlChildren(
    element: org.w3c.dom.Element,
    tagName: String,
): List<org.w3c.dom.Element> =
    (0 until element.childNodes.length)
        .map { index -> element.childNodes.item(index) }
        .filterIsInstance<org.w3c.dom.Element>()
        .filter { child -> child.tagName == tagName }

// Describes the API element that owns a japicmp compatibility change, such as
// `io.github.frikit.krandom.generator.GeneratorConfig#seed(long)` or `... superclass`.
fun japicmpSubject(owner: org.w3c.dom.Element): String {
    fun parameters(member: org.w3c.dom.Element): String =
        xmlChildren(member, "parameters")
            .flatMap { list -> xmlChildren(list, "parameter") }
            .joinToString(", ") { parameter -> parameter.getAttribute("type") }

    val qualifier =
        when (owner.tagName) {
            "superclass" -> " superclass ${owner.getAttribute("superclassOld")}"
            "interface" -> " interface ${owner.getAttribute("fullyQualifiedName")}"
            "returnType" -> " return type"
            "parameter" -> " parameter ${owner.getAttribute("type")}"
            "genericTemplate" -> " type parameter ${owner.getAttribute("name")}"
            "exception" -> " exception ${owner.getAttribute("name")}"
            "annotation" -> " annotation @${owner.getAttribute("fullyQualifiedName")}"
            else -> ""
        }
    var member: org.w3c.dom.Element = owner
    while (member.tagName !in setOf("class", "method", "constructor", "field")) {
        member = member.parentNode as org.w3c.dom.Element
    }
    var type = member
    while (type.tagName != "class") {
        type = type.parentNode as org.w3c.dom.Element
    }
    val className = type.getAttribute("fullyQualifiedName")
    val memberName =
        when (member.tagName) {
            "method" -> "#${member.getAttribute("name")}(${parameters(member)})"
            "constructor" -> "#<init>(${parameters(member)})"
            "field" -> "#${member.getAttribute("name")}"
            else -> ""
        }
    return "$className$memberName$qualifier"
}

// Incompatible changes in a japicmp XML report, one readable line each, and the number of additions.
fun japicmpFindings(report: File): Pair<List<String>, Int> {
    val document =
        javax.xml.parsers.DocumentBuilderFactory
            .newInstance()
            .apply { setFeature("http://apache.org/xml/features/disallow-doctype-decl", true) }
            .newDocumentBuilder()
            .parse(report)
    val changes = document.getElementsByTagName("compatibilityChange")
    val findings =
        (0 until changes.length)
            .map { index -> changes.item(index) as org.w3c.dom.Element }
            .mapNotNull { change ->
                val broken =
                    listOfNotNull(
                        "binary".takeIf { change.getAttribute("binaryCompatible") == "false" },
                        "source".takeIf { change.getAttribute("sourceCompatible") == "false" },
                    )
                if (broken.isEmpty()) {
                    null
                } else {
                    val owner = change.parentNode.parentNode as org.w3c.dom.Element
                    "${japicmpSubject(owner)}: ${change.getAttribute("type")} (breaks ${broken.joinToString(" and ")})"
                }
            }.distinct()
    val classes = document.getElementsByTagName("class")
    val additions =
        (0 until classes.length)
            .map { index -> classes.item(index) as org.w3c.dom.Element }
            .sumOf { type ->
                when (type.getAttribute("changeStatus")) {
                    "NEW" -> {
                        1
                    }

                    "MODIFIED" -> {
                        listOf("constructors" to "constructor", "methods" to "method", "fields" to "field")
                            .flatMap { (list, item) -> xmlChildren(type, list).flatMap { xmlChildren(it, item) } }
                            .count { member -> member.getAttribute("changeStatus") == "NEW" }
                    }

                    else -> {
                        0
                    }
                }
            }
    return findings to additions
}

val javaApiCompatibilityTasks =
    javaApiModules.map { moduleName ->
        val baselineGraphs =
            apiGraphs(moduleName, "Baseline", "io.github.frikit:krandom-$moduleName:$apiBaselineVersion")
        val candidateGraphs = apiGraphs(moduleName, "Candidate", dependencies.project(":$moduleName"))
        val reportDirectory = layout.buildDirectory.dir("reports/api-compatibility/$moduleName")

        tasks.register<JavaExec>("check${apiTaskSuffix(moduleName)}ApiCompatibility") {
            group = "verification"
            description = "Checks that krandom-$moduleName keeps the public API of $apiBaselineVersion (japicmp)."
            dependsOn(":$moduleName:jar")
            inputs.files(baselineGraphs, candidateGraphs)
            // No declared outputs: the check always runs, so every run prints its result line.
            classpath = japicmpClasspath
            mainClass.set("japicmp.JApiCmp")
            // The XML report decides the outcome (doLast); japicmp's own output goes to japicmp.txt.
            isIgnoreExitValue = true
            val japicmpOutput = java.io.ByteArrayOutputStream()
            standardOutput = japicmpOutput
            errorOutput = japicmpOutput
            val moduleProblems = mutableListOf<String>()

            doFirst {
                val (oldJar, oldClasspath) = jarAndDependencyClasspath(baselineGraphs, isReleasedModule(moduleName))
                val (newJar, newClasspath) = jarAndDependencyClasspath(candidateGraphs, isProjectModule(moduleName))
                val directory = reportDirectory.get().asFile
                directory.deleteRecursively()
                directory.mkdirs()

                moduleProblems.clear()
                val oldModuleName = jpmsModuleName(oldJar)
                val newModuleName = jpmsModuleName(newJar)
                if (newModuleName != oldModuleName) {
                    moduleProblems += "JPMS module name: $oldModuleName is now $newModuleName"
                }
                val apiPackages =
                    integrationApiPackages[moduleName]?.let(::setOf) ?: exportedPackages(oldJar).also { exported ->
                        check(exported.isNotEmpty()) { "krandom-$moduleName $apiBaselineVersion exports no package" }
                    }
                // An integration module is an automatic module that exports every package; an explicit
                // descriptor added later must still export its public-API package.
                val stillExported = exportedPackages(newJar)
                apiPackages.filterNot(stillExported::contains).forEach { lost ->
                    moduleProblems += "$lost: no longer exported by module $newModuleName"
                }

                args =
                    listOf(
                        "--old",
                        oldJar.absolutePath,
                        "--new",
                        newJar.absolutePath,
                        "--old-classpath",
                        oldClasspath,
                        "--new-classpath",
                        newClasspath,
                        "-a",
                        "protected",
                        // Exactly these packages: a sub-package that is not exported is not public API.
                        "--include",
                        apiPackages.joinToString(";"),
                        "--include-exclusively",
                        "--only-modified",
                        "--error-on-binary-incompatibility",
                        "--error-on-source-incompatibility",
                        "--reproducible-report",
                        "--html-file",
                        directory.resolve("report.html").absolutePath,
                        "--xml-file",
                        directory.resolve("report.xml").absolutePath,
                    )
            }

            doLast {
                val directory = reportDirectory.get().asFile
                directory.resolve("japicmp.txt").writeBytes(japicmpOutput.toByteArray())
                val xmlReport = directory.resolve("report.xml")
                val exitValue = executionResult.get().exitValue
                if (!xmlReport.isFile) {
                    throw GradleException(
                        "japicmp produced no report for krandom-$moduleName (exit $exitValue):\n" +
                            japicmpOutput.toString(Charsets.UTF_8),
                    )
                }
                val (findings, additions) = japicmpFindings(xmlReport)
                val problems = moduleProblems + findings
                if (problems.isEmpty() && exitValue != 0) {
                    throw GradleException(
                        "japicmp failed for krandom-$moduleName (exit $exitValue):\n" +
                            japicmpOutput.toString(Charsets.UTF_8),
                    )
                }
                if (problems.isNotEmpty()) {
                    throw GradleException(
                        buildString {
                            appendLine(
                                "krandom-$moduleName breaks the $apiBaselineVersion public API " +
                                    "(${problems.size} incompatible change${if (problems.size == 1) "" else "s"}):",
                            )
                            problems.forEach { problem -> appendLine("  $problem") }
                            appendLine(apiCompatibilityAdvice)
                            append("Report: ${directory.resolve("report.html")}")
                        },
                    )
                }
                logger.lifecycle(
                    "krandom-$moduleName: compatible with the $apiBaselineVersion public API " +
                        "($additions addition${if (additions == 1) "" else "s"}).",
                )
            }
        }
    }

// A class of a Kotlin ABI dump (the binary-compatibility-validator format): header and member lines.
data class KotlinAbiClass(
    val header: String,
    val members: List<String>,
)

fun kotlinAbiClasses(dump: File): Map<String, KotlinAbiClass> {
    val classes = linkedMapOf<String, KotlinAbiClass>()
    var header: String? = null
    val members = mutableListOf<String>()
    dump.readLines().forEach { line ->
        when {
            line.isBlank() -> {
                Unit
            }

            line == "}" -> {
                val classHeader = checkNotNull(header) { "Unbalanced '}' in $dump" }
                classes[classHeader.substringBefore(" : ").substringAfterLast(' ')] =
                    KotlinAbiClass(classHeader, members.toList())
                header = null
                members.clear()
            }

            line.startsWith("\t") -> {
                members += line.trim()
            }

            line.endsWith(" {") -> {
                header = line.removeSuffix(" {")
            }

            else -> {
                throw GradleException("Unexpected line in $dump: $line")
            }
        }
    }
    check(header == null) { "Unterminated class in $dump" }
    return classes
}

// Modifier changes a dependant compiled against the old declaration survives: wider visibility, a
// removed `final` or `abstract`. A changed `static`, a narrower visibility, or an added `final`,
// `abstract` or `synthetic` (hidden from source) breaks it; for classes every other token, such as
// `interface` or `annotation`, must stay too.
fun kotlinAbiModifiersCompatible(
    old: Set<String>,
    new: Set<String>,
    isClass: Boolean,
): Boolean {
    fun visibility(modifiers: Set<String>): Int =
        when {
            "public" in modifiers -> 2
            "protected" in modifiers -> 1
            else -> 0
        }
    val relaxable = setOf("public", "protected", "final", "abstract")
    return visibility(new) >= visibility(old) &&
        ("final" !in new || "final" in old) &&
        ("abstract" !in new || "abstract" in old) &&
        ("static" in new) == ("static" in old) &&
        ("synthetic" !in new || "synthetic" in old) &&
        (!isClass || new - relaxable == old - relaxable)
}

// Splits `public final fun name (desc)ret` or `public static final field NAME Ltype;` into its
// modifiers and its signature (`fun name (desc)ret`, `field NAME Ltype;`).
fun kotlinAbiMember(line: String): Pair<Set<String>, String> {
    val start =
        listOf(" fun ", " field ")
            .map { keyword -> line.indexOf(keyword) }
            .filter { index -> index >= 0 }
            .minOrNull() ?: throw GradleException("Unexpected member in Kotlin ABI dump: $line")
    return line.substring(0, start).split(' ').toSet() to line.substring(start + 1)
}

// Declarations of the baseline dump that the current dump removes or changes incompatibly. A member
// may move to a supertype in the current dump; a class may gain supertypes, never lose one. A type
// that code outside the module can implement, extend, or apply (any type that is not final) must not
// gain an abstract member: existing implementations would lack it, and so would existing usages of an
// annotation unless the new element has a default value, which the dump does not show.
fun kotlinAbiBreaks(
    baseline: Map<String, KotlinAbiClass>,
    current: Map<String, KotlinAbiClass>,
): List<String> {
    fun displayName(internalName: String) = internalName.replace('/', '.')

    fun headerModifiers(header: String) =
        header
            .substringBefore(" : ")
            .split(' ')
            .dropLast(1)
            .toSet()

    fun supertypes(header: String): Set<String> =
        header
            .substringAfter(" : ", missingDelimiterValue = "")
            .split(", ")
            .filter(String::isNotEmpty)
            .toSet()

    fun findMember(
        className: String,
        signature: String,
        visited: MutableSet<String> = mutableSetOf(),
    ): Set<String>? {
        val type = current[className] ?: return null
        if (!visited.add(className)) return null
        type.members
            .map(::kotlinAbiMember)
            .firstOrNull { (_, candidate) -> candidate == signature }
            ?.let { (modifiers, _) -> return modifiers }
        if (signature.startsWith("fun <init> ")) return null
        return supertypes(type.header).firstNotNullOfOrNull { supertype -> findMember(supertype, signature, visited) }
    }

    return baseline.flatMap { (className, oldClass) ->
        val newClass =
            current[className] ?: return@flatMap listOf("${displayName(className)}: removed `${oldClass.header}`")
        val headerBreaks =
            if (
                kotlinAbiModifiersCompatible(
                    headerModifiers(oldClass.header),
                    headerModifiers(newClass.header),
                    true,
                ) &&
                supertypes(newClass.header).containsAll(supertypes(oldClass.header))
            ) {
                emptyList()
            } else {
                listOf("${displayName(className)}: `${oldClass.header}` is now `${newClass.header}`")
            }
        val oldSignatures = oldClass.members.map { line -> kotlinAbiMember(line).second }.toSet()
        val newHeaderModifiers = headerModifiers(newClass.header)
        val consequence =
            if ("annotation" in newHeaderModifiers) {
                "which existing usages lack unless it has a default value"
            } else {
                "which existing implementations lack"
            }
        val newAbstractMembers =
            if ("final" in newHeaderModifiers) {
                emptyList()
            } else {
                newClass.members
                    .filter { line ->
                        val (modifiers, signature) = kotlinAbiMember(line)
                        "abstract" in modifiers && signature !in oldSignatures
                    }.map { line -> "${displayName(className)}: added `$line`, $consequence" }
            }
        headerBreaks + newAbstractMembers +
            oldClass.members.mapNotNull { line ->
                val (oldModifiers, signature) = kotlinAbiMember(line)
                val newModifiers = findMember(className, signature)
                when {
                    newModifiers == null -> {
                        "${displayName(className)}: removed `$line`"
                    }

                    !kotlinAbiModifiersCompatible(oldModifiers, newModifiers, false) -> {
                        "${displayName(className)}: `$line` is now `${newModifiers.joinToString(" ")} $signature`"
                    }

                    else -> {
                        null
                    }
                }
            }
    }
}

subprojects {
    if (name in kotlinApiModules) {
        pluginManager.withPlugin("org.jetbrains.kotlin.jvm") {
            configure<org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension> {
                @OptIn(org.jetbrains.kotlin.gradle.dsl.abi.ExperimentalAbiValidation::class)
                abiValidation {
                    // updateKotlinAbi writes the dump of the current code here, never over the committed
                    // baseline under api/.
                    referenceDumpDir.set(layout.buildDirectory.dir("api-compatibility/current"))
                    // checkKotlinAbi requires the dump to equal its reference, which would reject additions;
                    // the root checkApiCompatibility applies the VERSIONING.md rule to the baseline instead.
                    checkTaskProvider.configure { enabled = false }
                }
            }
        }
    }
}

fun kotlinCurrentAbiDump(moduleName: String): File =
    project(":$moduleName")
        .layout.buildDirectory
        .file("api-compatibility/current/$moduleName.api")
        .get()
        .asFile

fun kotlinBaselineAbiDump(moduleName: String): File =
    project(":$moduleName")
        .layout.projectDirectory
        .file("api/$moduleName-$apiBaselineVersion.api")
        .asFile

val kotlinApiCompatibilityTasks =
    kotlinApiModules.map { moduleName ->
        val currentDump = kotlinCurrentAbiDump(moduleName)
        val baselineDump = kotlinBaselineAbiDump(moduleName)

        tasks.register("check${apiTaskSuffix(moduleName)}ApiCompatibility") {
            group = "verification"
            description =
                "Checks that krandom-$moduleName keeps the public API of $apiBaselineVersion (Kotlin ABI dump)."
            dependsOn(":$moduleName:updateKotlinAbi")
            inputs.files(baselineDump, currentDump)
            // No declared outputs: the check always runs, so every run prints its result line.

            doLast {
                if (!baselineDump.isFile) {
                    throw GradleException(
                        "No $apiBaselineVersion API baseline for krandom-$moduleName at " +
                            "${baselineDump.relativeTo(rootDir)}: run ./gradlew updateKotlinApiBaseline on the " +
                            "$apiBaselineVersion release commit (docs/release-runbook.md).",
                    )
                }
                val baseline = kotlinAbiClasses(baselineDump)
                val current = kotlinAbiClasses(currentDump)
                val problems = kotlinAbiBreaks(baseline, current)
                if (problems.isNotEmpty()) {
                    throw GradleException(
                        buildString {
                            appendLine(
                                "krandom-$moduleName breaks the $apiBaselineVersion public API " +
                                    "(${problems.size} incompatible change${if (problems.size == 1) "" else "s"}):",
                            )
                            problems.forEach { problem -> appendLine("  $problem") }
                            appendLine(apiCompatibilityAdvice)
                            append("Baseline: ${baselineDump.relativeTo(rootDir)}; current: $currentDump")
                        },
                    )
                }
                val additions =
                    current.entries.sumOf { (className, type) ->
                        val old = baseline[className] ?: return@sumOf 1
                        val oldSignatures = old.members.map { line -> kotlinAbiMember(line).second }.toSet()
                        type.members.count { line -> kotlinAbiMember(line).second !in oldSignatures }
                    }
                logger.lifecycle(
                    "krandom-$moduleName: compatible with the $apiBaselineVersion public API " +
                        "($additions addition${if (additions == 1) "" else "s"}).",
                )
            }
        }
    }

tasks.register("checkApiCompatibility") {
    group = "verification"
    description = "Checks that every published jar keeps the public API of $apiBaselineVersion."
    dependsOn(javaApiCompatibilityTasks, kotlinApiCompatibilityTasks)
}

// The Java modules run on javaMinimumVersion (gradle.properties) or later. The JDK 21 toolchain that runs
// Error Prone, NullAway and PIT compiles their main and test sources with --release, against that version's
// API, which also sets the Java version that their published Gradle metadata requires; only core's
// java21Test sources raise it. Their tests run on that toolchain too, unless -PtestJavaVersion=<version>
// names another Java, as CI's test jobs do.
val javaMinimumVersion = providers.gradleProperty("javaMinimumVersion").get().toInt()
val testJavaVersion = providers.gradleProperty("testJavaVersion")
subprojects {
    if (name in javaApiModules) {
        tasks.withType<JavaCompile>().configureEach {
            options.release.convention(javaMinimumVersion)
        }
        pluginManager.withPlugin("java") {
            val toolchains = the<JavaToolchainService>()
            tasks.withType<Test>().configureEach {
                if (testJavaVersion.isPresent) {
                    javaLauncher.set(
                        toolchains.launcherFor {
                            languageVersion.set(JavaLanguageVersion.of(testJavaVersion.get()))
                        },
                    )
                    // A test that fails only on that Java shows its expected and actual values in the log.
                    testLogging.exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
                }
                doFirst {
                    val runtime = javaLauncher.get().metadata
                    logger.lifecycle("$path runs on Java ${runtime.javaRuntimeVersion} at ${runtime.installationPath}")
                }
            }
        }
    }
}

tasks.named("check") {
    dependsOn("checkApiCompatibility")
}

tasks.register("updateKotlinApiBaseline") {
    group = "verification"
    description =
        "Records the current Kotlin ABI as the $apiBaselineVersion baseline: on the release commit after a release, " +
        "or for a compatible change the check rejects (docs/release-runbook.md)."
    kotlinApiModules.forEach { moduleName -> dependsOn(":$moduleName:updateKotlinAbi") }
    val dumps =
        kotlinApiModules.map { moduleName ->
            kotlinCurrentAbiDump(moduleName) to
                kotlinBaselineAbiDump(moduleName)
        }

    doLast {
        dumps.forEach { (currentDump, baselineDump) ->
            baselineDump.parentFile.mkdirs()
            baselineDump.parentFile
                .listFiles { file -> file.name.endsWith(".api") }
                .orEmpty()
                .forEach { previous -> previous.delete() }
            currentDump.copyTo(baselineDump)
            logger.lifecycle("Recorded ${baselineDump.relativeTo(rootDir)}")
        }
    }
}

// Integration modules generate JaCoCo reports (build/reports/jacoco/test) and enforce the line and
// branch floors below so coverage cannot regress silently. Only :core enforces the exact 100% gate
// (core/build.gradle.kts) and uploads to Codecov.
val coverageReportModules = jarModules - "core"
val jacocoToolVersion = libs.versions.jacoco.get()

// Covered ratios measured on 2026-10-12 (line to branch); the spring-boot-starter line floor was
// re-measured after the 2026-10-13 reformat, which spread statements over more lines. The junit
// floors were raised on 2026-11-05, when its tests covered 99.6% of lines and 96.4% of branches, and
// the kotlin-dsl floors on 2026-11-05, after the sealed-type and value-class tests.
// Raise a floor when tests are added; never lower one without a changelog entry explaining why.
val integrationCoverageFloors =
    mapOf(
        "jackson" to ("1.00" to "1.00"),
        "junit" to ("0.98" to "0.94"),
        "spring-boot-starter" to ("0.95" to "0.82"),
        "kotest-extensions" to ("0.90" to "0.78"),
        "kotlin-dsl" to ("0.90" to "0.90"),
    )
check(integrationCoverageFloors.keys == coverageReportModules) {
    "integrationCoverageFloors must list exactly the integration modules: $coverageReportModules"
}

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
        val (lineFloor, branchFloor) = integrationCoverageFloors.getValue(name)
        // The module applies its JVM plugin after this block runs, so wire the tasks lazily.
        pluginManager.withPlugin("java-base") {
            // JaCoCo skips verification silently when no execution data exists; fail loudly instead.
            val executionData = layout.buildDirectory.file("jacoco/test.exec")
            val requireExecutionData =
                tasks.register("requireJacocoExecutionData") {
                    description = "Fails when the test task left no JaCoCo execution data to verify."
                    dependsOn("test")
                    doLast {
                        check(executionData.get().asFile.isFile) {
                            "No JaCoCo execution data at ${executionData.get().asFile}; the coverage floor cannot be verified"
                        }
                    }
                }
            tasks.withType<JacocoCoverageVerification>().configureEach {
                dependsOn(requireExecutionData)
                violationRules {
                    rule {
                        limit {
                            counter = "LINE"
                            value = "COVEREDRATIO"
                            minimum = lineFloor.toBigDecimal()
                        }
                        limit {
                            counter = "BRANCH"
                            value = "COVEREDRATIO"
                            minimum = branchFloor.toBigDecimal()
                        }
                    }
                }
            }
            tasks.named("check") {
                dependsOn("jacocoTestCoverageVerification")
            }
        }
    }
}

subprojects {
    apply(plugin = "com.diffplug.spotless")
    if (name in publishedModules) {
        apply(plugin = "krandom-publishing-conventions")
    }

    afterEvaluate {
        // Palantir Java Format for Java, ktlint for Kotlin sources and Gradle scripts; `.editorconfig`
        // holds the shared indentation and line-length settings. Formatting runs before the license
        // header step so the header is applied to the final layout.
        configure<com.diffplug.gradle.spotless.SpotlessExtension> {
            java {
                target("src/**/*.java")
                palantirJavaFormat(
                    libs.versions.palantir.java.format
                        .get(),
                )
                // Keeps type-use annotations such as @Nullable on the line of the type they annotate.
                formatAnnotations()
                licenseHeaderFile(rootProject.file("LICENSE_HEADER"))
            }
            if (plugins.hasPlugin("org.jetbrains.kotlin.jvm")) {
                kotlin {
                    ktlint(libs.versions.ktlint.get())
                    licenseHeaderFile(rootProject.file("LICENSE_HEADER"))
                }
            }
            kotlinGradle {
                ktlint(libs.versions.ktlint.get())
            }
        }
    }
}
