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
- `io.github.frikit:krandom-spring-boot-starter:<version>`

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
    // Optional integrations:
    implementation("io.github.frikit:krandom-jackson:<version>")
    implementation("io.github.frikit:krandom-spring-boot-starter:<version>")
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

After Maven Central release work is completed, the default install path will be `mavenCentral()` without GitHub Packages credentials.

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

- [Jackson Integration](guides/jackson-integration.md)
- [Spring Boot Starter](guides/spring-boot-starter.md)
