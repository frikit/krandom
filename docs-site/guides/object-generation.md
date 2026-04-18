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

## When to use

- Integration tests with larger object graphs.
- Snapshot fixture generation.
- Property-style randomized object tests.
