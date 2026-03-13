# kRandom

![kt test + coverage](https://github.com/frikit/krandom/workflows/kt%20test%20+%20coverage/badge.svg)
[![codecov](https://codecov.io/github/frikit/krandom/graph/badge.svg?token=CpcHkmbzo7)](https://codecov.io/github/frikit/krandom)

kRandom is a random and fake-data generation library built around a Java core, with Kotlin and Scala wrapper modules on top. It is aimed at tests, fixture generation, schema-driven record generation, and object graph population.

The project is currently Java-first:

- `core` contains the real implementation.
- `java-api` is a thin Java-facing artifact over `core`.
- `kotlin-api` provides Kotlin wrappers over the Java core.
- `scala-api` provides Scala 3 wrappers over the Java core.

## Modules

| Module | Purpose |
|:---|:---|
| `core` | Main implementation: generators, object generation, schema DSL, provider hub |
| `java-api` | Java artifact that depends on `core` |
| `kotlin-api` | Kotlin-native wrapper layer over `core` |
| `scala-api` | Scala 3 wrapper layer over `core` |
| `examples/` | Consumer examples for Java, Kotlin, and Scala across multiple build tools |
| `docs-site/` | GitHub Pages documentation site |

## Current capabilities

The Java core currently covers:

- Primitive and numeric generators
- Text and lorem-style generation
- Date/time generators
- Network and internet generators
- Location and address generators
- User/profile/name generators
- Finance, identifiers, and banking generators
- File/path/version/system generators
- Selection helpers such as pick, shuffle, weighted, repeat, and unique
- Object graph generation
- Schema-style record generation with `Field` and `Schema`
- Generic provider lookup and runtime extension through `ProviderHub`

Wrapper modules currently expose the Java core in more idiomatic Kotlin and Scala forms, but the source of truth for behavior remains Java.

## Quick start

### Java

```java
import org.github.krandom.generator.Generators;

int roll = Generators.ofInt(1, 7).generate();
String name = Generators.ofFullName().generate();
String email = Generators.ofEmail().generate();
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

## Build and verification

Main local commands:

```bash
./gradlew build
./gradlew test
./scripts/pre_commit_check.sh
```

`./scripts/pre_commit_check.sh` runs formatting, compilation, tests, Javadoc validation, and coverage verification. The current project standard is 100% line and branch coverage in the checked core reports, with the script enforcing a 99% minimum threshold.

## Install

Current release line:

- Version: `0.1.0`
- Group: `io.github.frikit`

Published artifacts:

- `io.github.frikit:krandom-core:0.1.0`
- `io.github.frikit:krandom-java-api:0.1.0`
- `io.github.frikit:krandom-kotlin-api:0.1.0`
- `io.github.frikit:krandom-scala-api:0.1.0`

Current package registry:

- GitHub Packages Maven registry: `https://maven.pkg.github.com/frikit/krandom`

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
    implementation("io.github.frikit:krandom-java-api:0.1.0")
}
```

### Gradle (Groovy DSL)

```groovy
repositories {
    mavenCentral()
    maven {
        url = uri('https://maven.pkg.github.com/frikit/krandom')
        credentials {
            username = findProperty('gpr.user') ?: System.getenv('GITHUB_ACTOR')
            password = findProperty('gpr.key') ?: System.getenv('GITHUB_TOKEN')
        }
    }
}

dependencies {
    implementation 'io.github.frikit:krandom-java-api:0.1.0'
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
    <artifactId>krandom-java-api</artifactId>
    <version>0.1.0</version>
  </dependency>
</dependencies>
```

Maven needs GitHub Packages credentials in `~/.m2/settings.xml` under server id `github`.

### sbt

```scala
resolvers += "GitHub Packages" at "https://maven.pkg.github.com/frikit/krandom"

libraryDependencies += "io.github.frikit" % "krandom-scala-api" % "0.1.0"
```

### Mill

```scala
def ivyDeps = Agg(
  ivy"io.github.frikit:krandom-scala-api:0.1.0"
)

override def repositoriesTask = T {
  super.repositoriesTask() ++ Seq(
    coursier.MavenRepository("https://maven.pkg.github.com/frikit/krandom")
  )
}
```

## Examples

Consumer examples live in [`examples/`](/Users/victorosipov/IdeaProjects/krandom/examples). They are test-based examples rather than runnable demo apps, so usage stays in test code and `main` contains only small fixture models.

Included combinations:

- Java + Gradle
- Java + Maven
- Kotlin + Gradle
- Kotlin + Maven
- Scala + sbt
- Scala + Mill

## Documentation site

Public documentation site source lives in [`docs-site/`](/Users/victorosipov/IdeaProjects/krandom/docs-site) and deploys through [`github-pages.yml`](/Users/victorosipov/IdeaProjects/krandom/.github/workflows/github-pages.yml).

Docs URL:

- [https://frikit.github.io/krandom/](https://frikit.github.io/krandom/)

One-time repository setup for GitHub Pages:

- Open `Settings -> Pages`
- Set `Build and deployment` to `GitHub Actions`

## Releases

The repository currently includes a manual GitHub Actions release workflow for GitHub Packages and GitHub Releases:

- Workflow: [`release-github-packages.yml`](/Users/victorosipov/IdeaProjects/krandom/.github/workflows/release-github-packages.yml)
- Trigger: `workflow_dispatch`
- Input: semver version such as `0.1.0`

That workflow:

- validates the version
- builds and tests all modules
- publishes artifacts to GitHub Packages
- creates a Git tag `v<version>`
- creates a GitHub Release with built JARs attached

## Maven Central

Maven Central migration is planned but not finished yet. The current plan is tracked in [`maven-central-release-plan.md`](/Users/victorosipov/IdeaProjects/krandom/docs/plans/maven-central-release-plan.md).

The target public no-auth coordinates are the same artifact names under `io.github.frikit`, but Central-specific requirements such as namespace verification, signing, and Central release automation still need to be completed.
