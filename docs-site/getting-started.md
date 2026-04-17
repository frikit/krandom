---
layout: page
title: Getting Started
permalink: /getting-started/
---

# Getting Started (Java)

## Requirements

- Java 21+
- Gradle or Maven

## Dependency

Published coordinates:

- `io.github.frikit:krandom-core:<version>`
- `io.github.frikit:krandom-jackson:<version>`

Gradle:

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

Maven:

```xml
<repositories>
  <repository>
    <id>github</id>
    <url>https://maven.pkg.github.com/frikit/krandom</url>
  </repository>
</repositories>

<dependency>
  <groupId>io.github.frikit</groupId>
  <artifactId>krandom-core</artifactId>
  <version><!-- your version --></version>
</dependency>
```

GitHub Packages requires credentials.

## First usage

```java
import org.github.krandom.generator.Generators;

int roll = Generators.ofInt(1, 7).generate();
String name = Generators.ofFullName().generate();
String ipv4 = Generators.ofIPv4().generate();
```

## Seeded deterministic generation

```java
import org.github.krandom.generator.GeneratorConfig;
import org.github.krandom.generator.user.EmailGenerator;

GeneratorConfig cfg = GeneratorConfig.builder()
        .seed(42L)
        .build();

EmailGenerator a = new EmailGenerator(cfg);
EmailGenerator b = new EmailGenerator(cfg);

// Same seed -> same sequence
String firstA = a.generate();
String firstB = b.generate();
```

## Run checks locally

```bash
./scripts/pre_commit_check.sh
```

This runs formatting, compile, tests, and coverage checks.
