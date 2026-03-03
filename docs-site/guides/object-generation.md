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

## With custom config

```java
ObjectGeneratorConfig cfg = ObjectGeneratorConfig.builder()
        .maxDepth(2)
        .ignoreErrors(false)
        .build();

ObjectGenerator<OrderDto> orders = new ObjectGenerator<>(OrderDto.class, cfg);
OrderDto order = orders.generate();
```

## When to use

- Integration tests with larger object graphs.
- Snapshot fixture generation.
- Property-style randomized object tests.
