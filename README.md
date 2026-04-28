# kRandom

[![tests + coverage](https://github.com/frikit/krandom/actions/workflows/continuous-integration-workflow.yml/badge.svg)](https://github.com/frikit/krandom/actions/workflows/continuous-integration-workflow.yml)
[![codecov](https://codecov.io/github/frikit/krandom/graph/badge.svg?token=CpcHkmbzo7)](https://codecov.io/github/frikit/krandom)

kRandom is a Java 21 random and fake-data generation toolkit. The repository is centered on the Java core plus focused integration modules for Jackson, Spring Boot, property-based testing, and Kotlin DSL usage.

## Modules

| Module | Purpose |
|:---|:---|
| `core` | Main implementation: generators, object generation, schema DSL, provider hub |
| `jackson` | Jackson integration on top of `core` |
| `spring-boot-starter` | Spring Boot 3.x auto-configuration for `core` |
| `kotest-extensions` | Kotest `Arb` adapters for property-based tests |
| `jqwik-extensions` | jqwik `Arbitrary` adapters for property-based tests |
| `kotlin-dsl` | Kotlin DSL for object generation rules |
| `benchmarks` | JMH and macro-profile workloads, including competitor comparisons |
| `examples/` | Consumer examples for Java, Kotlin, and Scala build-tool combinations using `core` directly |
| `docs-site/` | Public documentation site source |
| `docs/` | Internal notes, parity tracking, benchmark reports, and implementation plans |

## Current status

- Java-first architecture.
- `core` is the only behavior source of truth.
- `jackson`, `spring-boot-starter`, `kotest-extensions`, `jqwik-extensions`, and `kotlin-dsl` are published integration modules.
- `benchmarks` stays in-repo for performance profiling but is not a published consumer module.
- CI runs tests and coverage on Java 21.
- Local quality checks are standardized through `./scripts/pre_commit_check.sh`.
- Maven Central release automation is being prepared under `io.github.frikit`; the local development version is currently `0.1.0-SNAPSHOT`.

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

## Choosing an API

- Use scalar generators like `Generators.ofInt(...)`, `Generators.ofEmail()`, or `Generators.ofCity()` when you need a few direct values.
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

> JDK 21.0.10, JMH 1.37, aarch64. See the [latest full report](docs/benchmarks/competitor-report-2026-04-25.md) for object-population and bulk-generation numbers, methodology notes, and raw JMH output.

krandom's `ObjectGenerator` trades throughput for semantic realism — every field is populated with a domain-appropriate value (real names, valid emails, real cities) rather than arbitrary random bytes.

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

Planned public artifacts (group `io.github.frikit`):

- `io.github.frikit:krandom-core:<version>`
- `io.github.frikit:krandom-jackson:<version>`
- `io.github.frikit:krandom-spring-boot-starter:<version>`
- `io.github.frikit:krandom-kotest-extensions:<version>`
- `io.github.frikit:krandom-jqwik-extensions:<version>`
- `io.github.frikit:krandom-kotlin-dsl:<version>`

### Gradle (Kotlin DSL)

```kotlin
dependencies {
    implementation("io.github.frikit:krandom-core:<version>")
}
```

### Gradle (Groovy DSL)

```groovy
dependencies {
    implementation 'io.github.frikit:krandom-core:<version>'
}
```

### Maven

```xml
<dependency>
  <groupId>io.github.frikit</groupId>
  <artifactId>krandom-core</artifactId>
  <version>${krandom.version}</version>
</dependency>
```

No special repository configuration will be needed after the Maven Central release is cut. Until then, use `./scripts/verify_examples_local.sh` or `./gradlew publishToMavenLocal -PreleaseVersion=<version>` for local consumer verification.

## Examples

Consumer examples live in [`examples/`](examples/). They are test-based examples rather than runnable demo apps, and they verify Maven-local consumption of core plus the published integration artifacts.

- Java + Gradle
- Java + Gradle integration modules
- Java + Maven
- Kotlin + Gradle
- Kotlin + Maven
- Scala + sbt
- Scala + Mill

## Docs

- Public docs source: [`docs-site/`](docs-site/)
- Docs URL: [https://frikit.github.io/krandom/](https://frikit.github.io/krandom/)
- Internal docs: [`docs/`](docs/)

GitHub Pages deployment is wired through [`.github/workflows/github-pages.yml`](.github/workflows/github-pages.yml).

## Releases

Release work is not finalized yet. The intended public distribution path is Maven Central-first under `io.github.frikit`; release workflows and docs must be reconciled before the first public Maven Central release is cut.
