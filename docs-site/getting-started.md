---
layout: page
title: Getting Started
permalink: /getting-started/
---

# Getting Started (Java)

## Requirements

- Java 21+
- Gradle or Maven

## Dependency options

Use one of these artifacts:

1. `org.github.krandom:core` (direct core API)
2. `org.github.krandom:java-api` (currently re-exports `core`)

Gradle:

```kotlin
dependencies {
    implementation("org.github.krandom:core:1.0-SNAPSHOT")
    // or:
    // implementation("org.github.krandom:java-api:1.0-SNAPSHOT")
}
```

Maven:

```xml
<dependency>
  <groupId>org.github.krandom</groupId>
  <artifactId>core</artifactId>
  <version>1.0-SNAPSHOT</version>
</dependency>
```

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
