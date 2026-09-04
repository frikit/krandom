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

Module coordinates (Maven Central group `io.github.frikit`):

- `io.github.frikit:krandom-core`
- `io.github.frikit:krandom-bom` (aligns all module versions)
- `io.github.frikit:krandom-jackson`
- `io.github.frikit:krandom-junit` (from `1.2.0`)
- `io.github.frikit:krandom-spring-boot-starter`
- `io.github.frikit:krandom-kotest-extensions`
- `io.github.frikit:krandom-kotlin-dsl`

The current version is `2.2.0`. Latest version is always shown on
[GitHub Releases](https://github.com/frikit/krandom/releases).

Gradle:

```kotlin
repositories {
    mavenCentral()
}

dependencies {
    implementation(platform("io.github.frikit:krandom-bom:2.2.0"))
    implementation("io.github.frikit:krandom-core")
    // Optional integrations:
    implementation("io.github.frikit:krandom-jackson")
    implementation("io.github.frikit:krandom-spring-boot-starter")
    testImplementation("io.github.frikit:krandom-kotest-extensions")
    testImplementation("io.github.frikit:krandom-kotlin-dsl")
    testImplementation("io.github.frikit:krandom-junit") // JUnit 5 seed extension, from 1.2.0
}
```

Maven:

```xml
<dependencyManagement>
  <dependencies>
    <dependency>
      <groupId>io.github.frikit</groupId>
      <artifactId>krandom-bom</artifactId>
      <version>2.2.0</version>
      <type>pom</type>
      <scope>import</scope>
    </dependency>
  </dependencies>
</dependencyManagement>

<dependencies>
  <dependency>
    <groupId>io.github.frikit</groupId>
    <artifactId>krandom-core</artifactId>
  </dependency>
</dependencies>
```

Import `io.github.frikit:krandom-bom` in Gradle or Maven and omit versions from individual kRandom modules. Use explicit `2.2.0` versions instead when consuming only one artifact without the BOM.

## First usage

```java
import io.github.frikit.krandom.generator.Generators;

int roll = Generators.ofInt(1, 7).generate();
String name = Generators.ofFullName().generate();
String ipv4 = Generators.ofIPv4().generate();
```

## One root config path

`GeneratorConfig` is the main configuration entry point for the library.

```java
import io.github.frikit.krandom.generator.GeneratorConfig;

import java.util.Locale;

GeneratorConfig cfg = GeneratorConfig.builder()
        .locale(Locale.US)   // optional, defaults to Locale.US
        .seed(42L)           // optional, enables deterministic replay
        .build();
```

Use that same config for scalar generators, object generation, fluent fixtures, schemas, and providers.

## Realistic object generation by default

```java
import io.github.frikit.krandom.generator.Generators;

OrderDto order = Generators.ofObject(OrderDto.class, cfg).generate();
```

The object path now uses semantic field-name defaults, object-level config from `GeneratorConfig`, and coherence rules for common business fields.

## Fluent fixture overrides

```java
UserFixture user = Generators.ofObjectFaker(UserFixture.class, cfg)
        .ruleFor("email", () -> "owner@example.test")
        .ruleFor("address.city", () -> "Berlin")
        .generate();
```

Use `ObjectGenerator<T>` first. Move to `ObjectFaker<T>` when you need explicit fixture design.

## Schema and export workflows

```java
Field field = Generators.ofField(cfg);
LinkedHashMap<String, SchemaValueProvider> fields = new LinkedHashMap<>();
fields.put("orderId", field.bind("code.uuid"));
fields.put("email", field.bind("person.email"));
fields.put("amount", field.bind("finance.money"));

Schema orders = Generators.ofSchema(cfg, fields);

String jsonl = orders.toJsonLines(10);
String csv = orders.toCsv(10);
```

## Seeded deterministic generation

```java
import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.user.EmailGenerator;

GeneratorConfig cfg = GeneratorConfig.builder()
        .seed(42L)
        .build();

EmailGenerator a = new EmailGenerator(cfg);
EmailGenerator b = new EmailGenerator(cfg);

// Same seed -> same sequence
String firstA = a.generate();
String firstB = b.generate();
```

The same root config can also drive deterministic object, fixture, template, provider, and schema generation.

## Run checks locally

```bash
./scripts/pre_commit_check.sh
```

This runs formatting, compile, tests, and coverage checks.

See also:

- [Jackson Integration]({{ '/guides/jackson-integration/' | relative_url }})
- [Spring Boot Starter]({{ '/guides/spring-boot-starter/' | relative_url }})
- [JUnit Extension]({{ '/guides/junit-extension/' | relative_url }})
- [Property Testing Integrations]({{ '/guides/property-testing-integrations/' | relative_url }})
- [Kotlin DSL]({{ '/guides/kotlin-dsl/' | relative_url }})
- [Migrating to 2.0.0]({{ '/guides/migration-to-v2/' | relative_url }})
