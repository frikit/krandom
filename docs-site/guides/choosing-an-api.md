---
layout: page
title: Choosing an API
permalink: /guides/choosing-an-api/
---

# Choosing an API

kRandom now has four main ways to generate data. Use the simplest one that matches the job.

## 1. Scalar generators

Use scalar generators when you need one value at a time and you already know the type you want.

Typical cases:

- IDs
- names
- emails
- phone numbers
- money values
- random selections

```java
int roll = Generators.ofInt(1, 7).generate();
String email = Generators.ofEmail().generate();
String city = Generators.ofCity().generate();
String label = Generators.ofProviderTemplate("{firstname}-##").generate();
```

Choose this path when:

- you are filling a few individual fields manually
- you want the most direct API
- you want lightweight provider-token strings without building a schema
- you do not need object-graph population

## 2. Semantic object generation

Use `ObjectGenerator<T>` when you already have a DTO / record / POJO and want kRandom to fill it with realistic defaults.

```java
GeneratorConfig cfg = GeneratorConfig.builder()
        .locale(Locale.US)
        .seed(42L)
        .build();

OrderDto order = Generators.ofObject(OrderDto.class, cfg).generate();
```

Choose this path when:

- you want the library to infer values from field names and types
- you want good defaults with minimal setup
- your target is an existing Java object model

## 3. Fluent fixture authoring

Use `ObjectFaker<T>` when defaults are close but you need explicit fixture rules.

```java
UserFixture user = Generators.ofObjectFaker(UserFixture.class)
        .ruleFor(UserFixture::getEmail, () -> "owner@example.test")
        .ruleFor(PropertyPath.of(UserFixture::getAddress).then(Address::getCity), () -> "Berlin")
        .strict()
        .generate();
```

Choose this path when:

- you need stable field overrides
- you want nested-path rules
- you want immutable, composable `ObjectModel<T>` definitions or post-processing hooks
- you want to populate existing mutable instances

Start with `ObjectGenerator<T>` first. Move to `ObjectFaker<T>` when you need deliberate fixture design rather than default realism.

## 4. Schema and output generation

Use `Field` + `Schema` when your target is a record stream or export format rather than a Java object type.

```java
Field field = Generators.ofField();
Schema orders = Generators.ofSchema(Map.of(
        "orderId", field.bind("code.uuid"),
        "email", field.bind("person.email"),
        "amount", field.bind("finance.money")
));

String jsonl = orders.toJsonLines(10);
String csv = orders.toCsv(10);
```

Choose this path when:

- you need JSONL / JSON / CSV / XML / SQL / YAML / TOML output
- you want generated `Map<String, Object>` rows
- you want template-driven payloads or token-based schemas

## Rule of thumb

- Need one value: use a scalar generator.
- Need a DTO filled quickly: use `ObjectGenerator<T>`.
- Need specific fixture control: use `ObjectFaker<T>`.
- Need row-oriented payloads or exports: use `Schema`.
