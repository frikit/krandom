# kRandom

[![tests + coverage](https://github.com/frikit/krandom/actions/workflows/continuous-integration-workflow.yml/badge.svg)](https://github.com/frikit/krandom/actions/workflows/continuous-integration-workflow.yml)
[![codecov](https://codecov.io/github/frikit/krandom/graph/badge.svg?token=CpcHkmbzo7)](https://codecov.io/github/frikit/krandom)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.frikit/krandom-core?label=maven%20central)](https://central.sonatype.com/artifact/io.github.frikit/krandom-core)
[![License: MIT](https://img.shields.io/badge/license-MIT-green.svg)](LICENSE)

kRandom is a Java 21 random and fake-data generation toolkit. The repository is centered on the Java core plus focused integration modules for Jackson, Spring Boot, property-based testing, and Kotlin DSL usage.

## Modules

| Module | Purpose |
|:---|:---|
| `bom` | Maven/Gradle bill of materials for aligning published module versions |
| `core` | Main implementation: generators, object generation, schema DSL, provider hub |
| `jackson` | Jackson integration on top of `core` |
| `junit` | JUnit 5 extension: per-test seeds, `@KrandomSeed` pinning, failure-seed reporting |
| `spring-boot-starter` | Spring Boot 4.x auto-configuration for `core` (requires Spring Boot 4.x on the consumer) |
| `kotest-extensions` | Kotest `Arb` adapters for property-based tests |
| `kotlin-dsl` | Kotlin DSL for object generation rules |
| `benchmarks` | JMH and macro-profile workloads, including competitor comparisons |
| `examples/` | Consumer examples for Java, Kotlin, and Scala build-tool combinations using `core` directly |
| `docs-site/` | Public documentation site source |
| `docs/` | Internal notes, parity tracking, benchmark reports, and implementation plans |

## Current status

- Java-first architecture.
- `core` is the only behavior source of truth.
- `bom` aligns the versions of all published modules.
- `jackson`, `junit`, `spring-boot-starter`, `kotest-extensions`, and `kotlin-dsl` are published integration modules.
- `benchmarks` stays in-repo for performance profiling but is not a published consumer module.
- CI runs tests and coverage on Java 21.
- Local quality checks are standardized through `./scripts/pre_commit_check.sh`.
- Published to Maven Central under `io.github.frikit`. See [`docs/release-runbook.md`](docs/release-runbook.md) for the release process.

## What the core currently covers

- Primitive and numeric generators
- Text, lorem-style content, and provider-token templates
- Date/time, timezone, and legacy `Calendar` values
- Network and internet generators
- Locale-aware user/address data plus random supported `Locale` values
- Finance and identifier formats
- Geohash and coordinate-adjacent location data
- File/system/version values
- Selection combinators such as `pick`, `shuffle`, `weighted`, `repeat`, and `unique`
- Reflection-based object graph generation
- Schema-like record generation with `Field` and `Schema`
- Extensible provider lookup with `ProviderHub`

## Quick usage

```java
import io.github.frikit.krandom.generator.Generators;

int roll = Generators.ofInt(1, 7).generate();
String name = Generators.ofFullName().generate();
String email = Generators.ofEmail().generate();
String label = Generators.ofProviderTemplate("{firstname}-##").generate();
```

Kotlin consumers can use the same core API directly, or the fixture DSL from
`krandom-kotlin-dsl`:

```kotlin
import io.github.frikit.krandom.dsl.krandom

val user = krandom<User> {
    config { seed(42L) }
    rule("firstName") { "Ada" }
    exclude("password")
}
```

## Choosing an API

- Use scalar generators like `Generators.ofInt(...)`, `Generators.ofEmail()`, or `Generators.ofCity()` when you need a few direct values.
- Use the domain namespaces — `Generators.person()`, `Generators.finance()`, `Generators.location()`, `Generators.network()`, `Generators.text()`, `Generators.commerce()`, `Generators.identifier()`, `Generators.datetime()` — when you want a discoverable, IDE-friendly entry point per domain (each also accepts a `GeneratorConfig`).
- Use `ObjectGenerator<T>` when you want an existing DTO / record populated with realistic defaults.
- Use `ObjectFaker<T>` when you need explicit fixture rules, nested overrides, or reusable profiles.
- Use `Field` + `Schema` when you want row-style payloads or formatted export output such as CSV, JSONL, XML, or SQL.

The public docs now also include a dedicated guide: [Choosing an API](docs-site/guides/choosing-an-api.md).

## Performance

krandom's scalar generators are significantly faster than comparable JVM libraries. Full benchmark reports are run monthly and stored in [`docs/benchmarks/`](docs/benchmarks/).

**Scalar generation throughput** (single value per call, ops/s — higher is better):

| Benchmark | krandom | DataFaker | JavaFaker | krandom vs DataFaker |
|:---|---:|---:|---:|:---|
| firstName | **62,097,588** | 3,340,974 | 471,587 | 18.6x faster |
| email | **7,041,044** | 868,973 | 270,552 | 8.1x faster |
| streetAddress | **13,305,613** | 951,856 | 85,805 | 14.0x faster |

> JDK 21.0.10, JMH 1.37, aarch64. See the [latest full report](docs/benchmarks/DASHBOARD.md) for object-population and bulk-generation numbers, methodology notes, and raw JMH output.

krandom's `ObjectGenerator` trades throughput for semantic realism — every field is populated with a domain-appropriate value (real names, valid emails, real cities) rather than arbitrary random bytes.

## Randomness model

Unseeded generators use the JDK's fast `Random` by default, which is appropriate for fixture and fake-data generation. Use `GeneratorConfig.builder().seed(...)` when output must be reproducible, `GeneratorConfig.builder().random(myRandom)` when a client owns the PRNG instance, or `GeneratorConfig.builder().secureRandom()` when a consumer explicitly needs a `SecureRandom` source.

`Generator.filter(predicate)` is bounded by default and throws if no generated value matches after 10,000 attempts. Use `filter(predicate, maxAttempts)` when a domain-specific predicate is intentionally rare.

## Concurrency and determinism

A `Generator` instance is **not thread-safe**: it holds a single mutable PRNG. Sharing one instance across threads is memory-safe (the backing `Random` is synchronized) but interleaves the random sequence — which destroys reproducibility — and serializes callers on the PRNG. The rule for multi-threaded services is **one generator instance per thread**:

```java
// Each thread builds its own generator from a shared, immutable config.
GeneratorConfig config = GeneratorConfig.builder().locale(Locale.US).build();
Generator<String> emails = Generators.threadLocal(() -> Generators.ofEmail(config));
```

`Generators.threadLocal(Supplier)` gives each thread its own instance from the supplied factory. What *is* safe to share:

- **`GeneratorConfig`** is immutable and safe to share across threads as configuration (it is not itself a generator).
- **`ProviderHub`** registration is thread-safe; complete all registration before sharing the hub for lookups.
- **`ObjectGenerator` / `ObjectFaker` / `Schema`** are stateful per generation call and must be confined to one thread (one instance per thread).

**Determinism.** A fixed seed reproduces output only under **single-threaded** use of a given instance — concurrent calls race on PRNG call order. For reproducible per-thread data, derive a distinct seed per thread (e.g. `baseSeed ^ threadId`) and build one seeded config per thread. Seeded object-graph output is also stable across JDK builds and vendors: fields are populated in a name-sorted order rather than the JVM's unspecified reflection order.

## Object semantic aliases

`ObjectGenerator` resolves common field names such as `firstName`, `email`, and `createdAt` through semantic providers. Projects with their own vocabulary can extend that lookup with a `SemanticFieldRegistry`:

```java
GeneratorConfig config = GeneratorConfig.builder()
    .objectSemanticRegistry(SemanticFieldRegistry.defaults().toBuilder()
        .alias("email", "contactMail")
        .build())
    .build();
```

## Build and verify locally

Ensure `java -version` reports Java 21+ before running the local checks.

```bash
./gradlew clean build
./scripts/pre_commit_check.sh
./scripts/verify_examples_local.sh
```

`pre_commit_check.sh` runs formatting, markdown checks, compilation, tests, Javadoc validation, coverage verification, and now fails fast when Java 21+ is not active.

`verify_examples_local.sh` publishes all current `krandom-*` consumer artifacts to Maven local and runs the consumer examples against that local snapshot. CI installs `sbt` and `mill` and runs the full matrix; locally the Scala examples are skipped unless those tools are installed or `KRANDOM_REQUIRE_SCALA_TOOLS=true` is set.

## Install

Public artifacts on Maven Central (group `io.github.frikit`):

| Artifact | Automatic module name |
|:---|:---|
| `io.github.frikit:krandom-bom` | — (Maven/Gradle platform) |
| `io.github.frikit:krandom-core` | `io.github.frikit.krandom` |
| `io.github.frikit:krandom-jackson` | `io.github.frikit.krandom.jackson` |
| `io.github.frikit:krandom-junit` | `io.github.frikit.krandom.junit` |
| `io.github.frikit:krandom-spring-boot-starter` | `io.github.frikit.krandom.spring.boot.starter` |
| `io.github.frikit:krandom-kotest-extensions` | `io.github.frikit.krandom.kotest` |
| `io.github.frikit:krandom-kotlin-dsl` | `io.github.frikit.krandom.kotlin.dsl` |

The latest released version is `1.5.0` (the in-repo development build defaults to
`1.6.0-SNAPSHOT`). The released version is always
shown on [GitHub Releases](https://github.com/frikit/krandom/releases) and
[Maven Central](https://central.sonatype.com/artifact/io.github.frikit/krandom-core).

### Gradle (Kotlin DSL)

```kotlin
dependencies {
    implementation(platform("io.github.frikit:krandom-bom:1.6.0"))
    implementation("io.github.frikit:krandom-core")
}
```

### Gradle (Groovy DSL)

```groovy
dependencies {
    implementation platform('io.github.frikit:krandom-bom:1.6.0')
    implementation 'io.github.frikit:krandom-core'
}
```

### Maven

```xml
<dependencyManagement>
  <dependencies>
    <dependency>
      <groupId>io.github.frikit</groupId>
      <artifactId>krandom-bom</artifactId>
      <version>1.6.0</version>
      <type>pom</type>
      <scope>import</scope>
    </dependency>
  </dependencies>
</dependencyManagement>

<dependency>
  <groupId>io.github.frikit</groupId>
  <artifactId>krandom-core</artifactId>
</dependency>
```

For local consumer verification against an unpublished snapshot, use
`./scripts/verify_examples_local.sh` or
`./gradlew publishToMavenLocal -PreleaseVersion=<version>`.

## Examples

Consumer examples live in [`examples/`](examples/). They are test-based examples rather than runnable demo apps, and they verify Maven-local consumption of core plus the published integration artifacts.

- Java + Gradle
- Java + Gradle integration modules
- Java + Maven
- Kotlin + Gradle
- Kotlin + Maven
- Scala + sbt
- Scala + Mill

## Versioning and compatibility

kRandom follows [Semantic Versioning](https://semver.org). The full policy —
Java support window, Spring Boot compatibility, seed/output stability, and
deprecation rules — is documented in [VERSIONING.md](VERSIONING.md).

## Getting help

- [GitHub Issues](https://github.com/frikit/krandom/issues) for bug reports and feature requests
- [FAQ](docs-site/faq.md) for common questions, also published at [frikit.github.io/krandom](https://frikit.github.io/krandom/)

## Docs

- Public docs source: [`docs-site/`](docs-site/)
- Docs URL: [https://frikit.github.io/krandom/](https://frikit.github.io/krandom/)
- Internal docs: [`docs/`](docs/)
- k-random migration guide: [`docs/migration/k-random-to-krandom.md`](docs/migration/k-random-to-krandom.md)

GitHub Pages deployment is wired through [`.github/workflows/github-pages.yml`](.github/workflows/github-pages.yml).

## Releases

Releases are published to Maven Central via the Central Portal
(<https://central.sonatype.com>). The release workflow lives at
[`.github/workflows/release-maven-central.yml`](.github/workflows/release-maven-central.yml)
and is documented in [`docs/release-runbook.md`](docs/release-runbook.md).
See the latest version on [Maven Central](https://central.sonatype.com/artifact/io.github.frikit/krandom-core)
or the [GitHub Releases](https://github.com/frikit/krandom/releases) page.
