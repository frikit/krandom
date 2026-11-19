# Testing and the gate

How kRandom is tested, what `scripts/pre_commit_check.sh` runs, and what to do when a step fails. The
thresholds quoted in [CONTRIBUTING.md](../../CONTRIBUTING.md#code-quality-gates) are the rules; the
build files are the source of truth for their values.

## Test sets

| Where | Framework | Runs | Purpose |
| --- | --- | --- | --- |
| `core/src/test/java` | JUnit Jupiter | `:core:test` | Unit and behaviour tests of the core; counts toward the 100% coverage gate. |
| `core/src/nullnessTest/java` | JUnit Jupiter, NullAway | with `:core:test` | Calls the public API as null-marked consumer code does; fails to compile if the API stops accepting what such code passes. |
| `core/src/java21Test/java` | JUnit Jupiter | `:core:java21Test`, only on a Java 21+ test JVM | Tests that need Java 21, such as virtual threads. Outside coverage and PIT. |
| `<module>/src/test` of `jackson`, `junit`, `spring-boot-starter` | JUnit Jupiter | `:<module>:test` | Integration-module tests; per-module coverage floors. |
| `<module>/src/test` of `kotest-extensions`, `kotlin-dsl` | Kotest (mostly `DescribeSpec`) | `:<module>:test` | Kotlin-module tests, including property tests. |
| `examples-e2e/` | JUnit Jupiter | in `build` | End-to-end form scenarios. |
| `benchmarks/` | JMH | `scripts/run_benchmarks.sh` | Performance; CI skips its tests. |
| `examples/` | each project's own | `scripts/verify_examples_local.sh` | Standalone consumer builds (Gradle, Maven, JPMS, Android, sbt, Mill) against the local snapshot. |

Java tests use JUnit Jupiter's own `Assertions`; there is no AssertJ. **jqwik is forbidden**: property
tests are written with Kotest in the Kotlin modules. Test classes end in `Test`, carry a class-level
`@DisplayName` in sentence form, and pin and oracle tests state their contract in Javadoc.

## Coverage and mutation testing

- **Core coverage is exactly 100%** on instructions, lines, branches, complexity, methods and classes
  (JaCoCo rules in `core/build.gradle.kts`). There are no exclusions; reach it with tests through
  behaviour, not by excluding code or adding test-only branches.
- **Integration modules** have line and branch floors in `integrationCoverageFloors` in the root
  `build.gradle.kts`. Raise a floor when you add tests; never lower one without a changelog entry.
- **PIT** runs on a list of critical classes and tests (`pitest { targetClasses / targetTests }` in
  `core/build.gradle.kts`) with mutation-score and line-coverage thresholds. A new class on a
  deterministic, safety or schema path belongs in `targetClasses`, and a new test class that should kill
  its mutants belongs in `targetTests`; otherwise PIT does not run it.

## Test patterns to reuse

| Pattern | Example | Use it when |
| --- | --- | --- |
| Pin test: exact values for fixed seeds | `generator/location/StreetAddressPinsTest`, `GenerationRecipeGoldenStreamTest` | You must prove seeded output did not change. See [determinism](determinism-and-compatibility.md#how-to-check-that-you-changed-nothing). |
| Off/on pins for a setting | `SeedingDefaultsPinTest`, `DefaultFlipPinsTest` | You add a setting that changes output when on. |
| Oracle: reseeded equals freshly built | `internal/ReseedAsBuiltTest`, `generator/schema/SchemaFreshBuildOracleTest` | You add a reseed hook or touch `Schema` column reuse. |
| Catalog sweep over every factory | `GeneratorCatalogEnvironmentTest`, `GeneratorCatalogNetworkSafetyTest` | Automatic: they reflect over `Generators`, so a new factory is swept without a change. List a factory's expected locale refusals in `GeneratorCatalogEnvironmentTest`. |
| Scalar, text and date-time factories | `BuiltInGeneratorCatalogTest` | You add a numeric, text or date-time factory. |
| Locale coverage | `generator/locale/SupportedLocaleCoverageTest`, `LocalizedVocabularyTest`, `ScopedVocabularyRegistryTest`, `VocabularyTest` | You add a vocabulary, a dataset or a locale. |
| Facade parity | `FacadeNamespaceParityTest` | You add a factory or a namespace method. |
| Docs as tests | `DocumentationSnippetsTest`, `ProviderCatalogDocumentationTest` | You change a documented snippet or the provider catalog. |

Paths are under `core/src/test/java/io/github/frikit/krandom/`.

## The gate

`./scripts/pre_commit_check.sh` needs `JAVA_HOME` set to a JDK 21 or later. In order, it:

1. checks the JDK;
2. **rewrites** formatting with `spotlessApply` and `spotlessMarkdownApply`, then checks both;
3. checks Markdown links (`verify_markdown_links.sh`), docs-site links (`verify_docs_site_links.sh`),
   documentation facts (`verify_documentation_facts.sh`) and pinned build inputs
   (`verify_build_input_pins.sh`);
4. compiles main and test code, then checks module boundaries (`verify_module_boundaries.sh`);
5. runs the public API check (`checkApiCompatibility`), which downloads the baseline jars;
6. generates and validates the release SBOMs (`verifyReleaseSboms`);
7. builds the core Javadoc;
8. runs every test, forced to rerun, and `java21Test`;
9. runs PIT on the core (`:core:pitest`);
10. verifies coverage for every module, prints the coverage tables, and only then fails if a module is
    under its rule.

`--fast` skips the SBOMs and PIT and reuses up-to-date test results. It still rewrites formatting. Use
it while iterating; run the full gate before every commit.

CI (`.github/workflows/continuous-integration-workflow.yml`) runs the same checks split into jobs:
tests and coverage on Java 17, 21 and 25, the API check, PIT, the GraalVM native-image smoke test,
and the consumer examples. The docs-site link check runs in the documentation workflow, which only
triggers on `docs-site/` changes, so run the gate locally after editing docs elsewhere that link
into the site.

## When the gate fails

| Step | Usual cause | Fix |
| --- | --- | --- |
| JDK check | `JAVA_HOME` is not JDK 21+ | Point `JAVA_HOME` at a JDK 21 or later. |
| Formatting | Unformatted code or docs | The gate already rewrote it; review and commit the result. |
| Markdown links | A relative link to a file that does not exist | Fix the path. Anchors are not checked, so check them by eye. |
| Docs-site links | A raw `.md` link or an unknown permalink in `docs-site/` | Use `{{ '/path/' \| relative_url }}` with an existing permalink. |
| Documentation facts | A fact in `gradle.properties` (locale counts, modules, versions, constraints, export formats, thresholds) no longer matches the docs or code, or a development guide names a path that does not exist | Update the fact and every document the script names together. Read the script's message; it names the file. |
| Build input pins | A dependency, plugin or action without a pinned version or checksum | Pin it; see [dependency reproducibility](dependency-reproducibility.md). |
| Dependency verification | A new artifact has no checksum in `gradle/verification-metadata.xml` | Run `scripts/update_verification_metadata.sh` and review the diff. |
| Compilation | Java 18+ API in a Java module, or a NullAway error | Use Java 17 API; add `@Nullable` where `null` is real, or handle it. |
| Module boundaries | A package split across two published jars | Keep each package in one module. |
| API check | A removed or changed public declaration | Restore it, or follow [removing or changing API](determinism-and-compatibility.md#removing-or-changing-api). |
| Javadoc | A doclint error: a broken `{@link}`, bad HTML, or a wrong `@param` (missing comments do not fail it) | Fix the reference or markup. Document new public API anyway. |
| Tests | A behaviour change, or a pinned value that moved | A moved pin is a finding: see [determinism](determinism-and-compatibility.md). |
| `ProviderCatalogDocumentationTest` | `ProviderCatalog` changed | Run `./gradlew :core:generateProviderCatalogDocumentation` and commit the result. |
| PIT | Surviving mutants or uncovered lines in a target class | Add tests that check the behaviour the mutant changes; review survivors by behaviour rather than chasing every one. |
| Coverage | An uncovered line or branch in the core | Test it through behaviour; remove code that no input can reach. |
