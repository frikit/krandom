---
layout: page
title: Object Generation
permalink: /guides/object-generation/
---

# Object Generation

`ObjectGenerator<T>` populates Java classes recursively.

## Basic usage

```java
ObjectGenerator<UserDto> gen = new ObjectGenerator<>(UserDto.class);
UserDto dto = gen.generate();
```

## With root GeneratorConfig

```java
GeneratorConfig cfg = GeneratorConfig.builder()
        .locale(Locale.US)
        .objectMaxDepth(2)
        .objectSemanticMode(ObjectGenerationSemanticMode.RELAXED)
        .objectNullProbability(0.10)
        .objectOptionalEmptyProbability(0.25)
        .objectDateRange(LocalDate.of(2020, 1, 1), LocalDate.of(2023, 12, 31))
        .build();

ObjectGenerator<OrderDto> orders = new ObjectGenerator<>(OrderDto.class, cfg);
OrderDto order = orders.generate();
```

## With custom config

```java
ObjectGeneratorConfig cfg = ObjectGeneratorConfig.builder()
        .maxDepth(2)
        .ignoreErrors(false)
        .build();

ObjectGenerator<OrderDto> orders = new ObjectGenerator<>(OrderDto.class, cfg);
OrderDto order = orders.generate();
```

Use `ObjectGeneratorConfig` when you need field overrides, exclusions, or other object-only controls on top of the shared root config.

Semantic modes:

- `RELAXED`: use semantic field names when available, but let annotations and bean validation win.
- `STRICT`: semantic field names win whenever a semantic match exists.
- `STRUCTURAL_ONLY`: disable field-name semantics and use type-based generation only.

## Fluent fixtures

Use `ObjectFaker<T>` when the default object graph is close but you want a few explicit root-field rules:

```java
ObjectFaker<UserDto> faker = new ObjectFaker<>(UserDto.class)
        .ruleFor("firstName", () -> "Ada")
        .ruleFor("lastName", () -> "Lovelace")
        .ruleFor("email",
                 user -> user.getFirstName().toLowerCase() + "."
                         + user.getLastName().toLowerCase() + "@example.com")
        .ignore("password");

UserDto user = faker.generate();
List<UserDto> users = faker.generateList(100);
```

Supported in this first fixture layer:

- root-level `ruleFor(...)` with plain generators
- dependent `ruleFor(...)` based on the generated root object
- `ignore(...)` for root fields
- `afterGenerate(...)` / `postProcess(...)`
- `populate(existing)` for mutable classes

`ObjectFaker<T>` reuses the same `ObjectGeneratorConfig`, semantic resolver, locale, seed, and override rules as `ObjectGenerator<T>`.

## When to use

- Integration tests with larger object graphs.
- Snapshot fixture generation.
- Property-style randomized object tests.
