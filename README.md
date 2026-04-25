# kRandom

[![tests + coverage](https://github.com/frikit/krandom/actions/workflows/continuous-integration-workflow.yml/badge.svg)](https://github.com/frikit/krandom/actions/workflows/continuous-integration-workflow.yml)
[![codecov](https://codecov.io/github/frikit/krandom/graph/badge.svg?token=CpcHkmbzo7)](https://codecov.io/github/frikit/krandom)

kRandom is a Java 21 random and fake-data generation toolkit. The repository is centered on the Java core plus a Jackson integration module. Kotlin and Scala wrapper modules have been removed so the implementation surface stays focused and maintainable.

## Modules

| Module | Purpose |
|:---|:---|
| `core` | Main implementation: generators, object generation, schema DSL, provider hub |
| `jackson` | Jackson integration on top of `core` |
| `spring-boot-starter` | Spring Boot 3.x auto-configuration for `core` |
| `benchmarks` | JMH and macro-profile workloads, including competitor comparisons |
| `examples/` | Consumer examples for Java, Kotlin, and Scala build-tool combinations using `core` directly |
| `docs-site/` | Public documentation site source |
| `docs/` | Internal notes, parity tracking, benchmark reports, and implementation plans |

## Current status

- Java-first architecture.
- `core` is the only behavior source of truth.
- `jackson` and `spring-boot-starter` are published integration modules.
- `benchmarks` stays in-repo for performance profiling but is not a published consumer module.
- CI runs tests and coverage on Java 21.
- Local quality checks are standardized through `./scripts/pre_commit_check.sh`.
- Current release channel is GitHub Packages under `io.github.frikit`.

## What the core currently covers

- Primitive and numeric generators
- Text and lorem-style content
- Date/time and timezone values
- Network and internet generators
- Locale-aware user and address data
- Finance and identifier formats
- File/system/version values
- Selection combinators such as `pick`, `shuffle`, `weighted`, `repeat`, and `unique`
- Reflection-based object graph generation
- Schema-like record generation with `Field` and `Schema`
- Extensible provider lookup with `ProviderHub`

## Quick usage

```java
import org.github.krandom.generator.Generators;

int roll = Generators.ofInt(1, 7).generate();
String name = Generators.ofFullName().generate();
String email = Generators.ofEmail().generate();
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

`verify_examples_local.sh` publishes the current `krandom-core` artifact to Maven local and runs the consumer examples against that local snapshot. CI installs `sbt` and `mill` and runs the full matrix; locally the Scala examples are skipped unless those tools are installed or `KRANDOM_REQUIRE_SCALA_TOOLS=true` is set.

## Install

Current published namespace:

- Group: `io.github.frikit`
- Repository: `https://maven.pkg.github.com/frikit/krandom`

Published artifacts:

- `io.github.frikit:krandom-core:<version>`
- `io.github.frikit:krandom-jackson:<version>`
- `io.github.frikit:krandom-spring-boot-starter:<version>`

### Gradle (Kotlin DSL)

```kotlin
repositories {
    mavenCentral()
    maven {
        url = uri("https://maven.pkg.github.com/frikit/krandom")
        credentials {
            username = providers.gradleProperty("gpr.user")
                .orElse(providers.environmentVariable("GITHUB_ACTOR"))
                .orNull
            password = providers.gradleProperty("gpr.key")
                .orElse(providers.environmentVariable("GITHUB_TOKEN"))
                .orNull
        }
    }
}

dependencies {
    implementation("io.github.frikit:krandom-core:<version>")
}
```

### Maven

```xml
<repositories>
  <repository>
    <id>github</id>
    <url>https://maven.pkg.github.com/frikit/krandom</url>
  </repository>
</repositories>

<dependencies>
  <dependency>
    <groupId>io.github.frikit</groupId>
    <artifactId>krandom-core</artifactId>
    <version><!-- your version --></version>
  </dependency>
</dependencies>
```

GitHub Packages requires credentials through `GITHUB_ACTOR` / `GITHUB_TOKEN`, Gradle properties (`gpr.user`, `gpr.key`), or Maven `settings.xml`.

## Examples

Consumer examples live in [`examples/`](examples/). They are test-based examples rather than runnable demo apps, and they all depend directly on `io.github.frikit:krandom-core`.

- Java + Gradle
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

Manual release to GitHub Packages and GitHub Releases is handled by [`.github/workflows/release-github-packages.yml`](.github/workflows/release-github-packages.yml).

The workflow:

- validates a semver input
- builds and tests the repository
- publishes `krandom-core`, `krandom-jackson`, and `krandom-spring-boot-starter`
- creates a Git tag `v<version>`
- creates a GitHub Release with built JARs attached
