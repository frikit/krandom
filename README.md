# kRandom

[![tests + coverage](https://github.com/frikit/krandom/actions/workflows/continuous-integration-workflow.yml/badge.svg)](https://github.com/frikit/krandom/actions/workflows/continuous-integration-workflow.yml)
[![codecov](https://codecov.io/github/frikit/krandom/graph/badge.svg?token=CpcHkmbzo7)](https://codecov.io/github/frikit/krandom)

kRandom is a Java 21 random and fake-data generation toolkit.
The project is built around a Java core and includes thin wrappers for Kotlin and Scala, plus optional integrations and benchmarking modules.

## Current Status

- Active development, Java-first architecture.
- `core` is the behavior source of truth.
- CI runs tests + coverage on Java 21.
- Local quality checks are standardized via `./scripts/pre_commit_check.sh`.
- Team target is 100% line and branch coverage in core reports; enforced gate is 99%.
- Publishing currently targets GitHub Packages (`io.github.frikit:*` coordinates). Maven Central is not the active distribution channel yet.

## Repository Layout

| Module/Path | Purpose |
|:---|:---|
| `core` | Main implementation: generators, object generation, schema DSL, provider hub |
| `java-api` | Java facade module over `core` |
| `kotlin-api` | Kotlin wrapper API over `core` |
| `scala-api` | Scala 3 wrapper API over `core` |
| `jackson` | Jackson module (`Schema` serialization support) |
| `benchmarks` | JMH and macro generation profiling workloads |
| `examples/` | Consumer examples for Java/Kotlin/Scala across build tools |
| `docs-site/` | GitHub Pages documentation source |
| `docs/` | Internal plans, parity analysis, and implementation notes |

## What You Can Generate

Current core coverage includes:

- Primitive and numeric data
- Text and lorem-style content
- Date/time and timezone values
- Network/internet values (URLs, domains, IPs, HTTP helpers)
- Locale-aware user and address data
- Finance and identifier formats
- File/system/version values
- Selection combinators (`pick`, `shuffle`, `weighted`, `repeat`, `unique`)
- Object graphs via reflection-based object generation
- Schema-like record generation with `Field` and `Schema`
- Extensible provider lookup via `ProviderHub`

## Quick Usage

### Java

```java
import org.github.krandom.generator.GeneratorConfig;
import org.github.krandom.generator.Generators;
import org.github.krandom.generator.user.EmailGenerator;
import org.github.krandom.generator.user.FullNameGenerator;

GeneratorConfig config = GeneratorConfig.builder()
    .seed("demo-seed")
    .build();

int roll = Generators.ofInt(1, 7).generate();
String name = new FullNameGenerator(config).generate();
String email = new EmailGenerator(config).generate();
```

### Kotlin

```kotlin
import org.github.krandom.kotlinapi.KRandom

val roll = KRandom.int(1, 7, 42L).next()
val name = KRandom.fullName().next()
val email = KRandom.email().next()
```

### Scala

```scala
import org.github.krandom.scalaapi.ScalaGenerators

val roll = ScalaGenerators.int(1, 7, 42L).one
val name = ScalaGenerators.fullName().one
val email = ScalaGenerators.email().one
```

## Build and Verify Locally

```bash
./gradlew clean build
./scripts/pre_commit_check.sh
```

`pre_commit_check.sh` runs formatting, markdown checks, compilation, tests, Javadoc validation, and coverage verification.

Performance workloads:

```bash
./gradlew :benchmarks:jmh
./gradlew :benchmarks:profileGeneration
```

## Dependencies and Publishing

Current published artifact namespace:

- Group: `io.github.frikit`
- Repository: `https://maven.pkg.github.com/frikit/krandom`

Main artifacts:

- `io.github.frikit:krandom-core:<version>`
- `io.github.frikit:krandom-java-api:<version>`
- `io.github.frikit:krandom-kotlin-api:<version>`
- `io.github.frikit:krandom-scala-api_3:<version>`
- `io.github.frikit:krandom-jackson:<version>`

Gradle (Kotlin DSL):

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
    implementation("io.github.frikit:krandom-java-api:<version>")
}
```

Maven:

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
    <artifactId>krandom-java-api</artifactId>
    <version><!-- your version --></version>
  </dependency>
</dependencies>
```

For GitHub Packages, configure credentials (`GITHUB_ACTOR`/`GITHUB_TOKEN` or `gpr.user`/`gpr.key`).

## Examples and Docs

- Consumer examples: [`examples/`](examples/)
- Public docs site source: [`docs-site/`](docs-site/)
- Docs site URL: [https://frikit.github.io/krandom/](https://frikit.github.io/krandom/)
- Internal technical docs: [`docs/`](docs/)

## Automation

- CI tests + coverage: [`.github/workflows/continuous-integration-workflow.yml`](.github/workflows/continuous-integration-workflow.yml)
- Docs publishing: [`.github/workflows/github-pages.yml`](.github/workflows/github-pages.yml)
- Manual release to GitHub Packages + GitHub Release: [`.github/workflows/release-github-packages.yml`](.github/workflows/release-github-packages.yml)
- Performance profiling workflow: [`.github/workflows/performance-profile.yml`](.github/workflows/performance-profile.yml)
