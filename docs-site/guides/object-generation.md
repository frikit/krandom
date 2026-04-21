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
        .objectOverride(OrderDto.class, "status", () -> "PENDING")
        .objectExcludeField("internalToken")
        .build();

ObjectGenerator<OrderDto> orders = new ObjectGenerator<>(OrderDto.class, cfg);
OrderDto order = orders.generate();
```

`GeneratorConfig` is the public entry point, including for advanced object overrides and exclusions.

Semantic modes:

- `RELAXED`: use semantic field names when available, but let annotations and bean validation win.
- `STRICT`: semantic field names win whenever a semantic match exists.
- `STRUCTURAL_ONLY`: disable field-name semantics and use type-based generation only.

## Semantic field defaults

In `RELAXED` and `STRICT` modes, `ObjectGenerator<T>` recognizes common business field names after normalization.
`created_at`, `createdAt`, and `created-at` all resolve to the same semantic key.

Current built-in semantic coverage includes:

- strings such as `firstName`, `lastName`, `fullName`, `email`, `username`, `phoneNumber`, `streetAddress`, `city`, `postalCode`, `country`, `companyName`, `url`, `domain`, `uuid`, and `status`
- typed business fields such as `createdAt`, `updatedAt`, `birthDate`, `age`, `amount`, `balance`, `price`, `currency`, `id`, `active`, `latitude`, and `longitude`

String semantics reuse the same provider taxonomy as `ProviderHub`, so values such as `firstName`, `city`, `url`, `currencyCode`, and `uuid` resolve through providers like `person.first_name`, `address.city`, `internet.url`, `finance.currency`, and `code.uuid`. Locale and deterministic seed settings still come from the shared root `GeneratorConfig`.

Examples:

- `dateOfBirth`, `dob`, and `birth_date` map to the same birth-date semantic
- `accountId`, `customer_id`, and `identifier` map to the ID semantic
- `isEnabled` and `active` map to the active-flag semantic
- `lat` and `latitude` map to the latitude semantic

Configured object date ranges still apply to semantic temporal fields. If you set `objectDateRange(...)`, that range becomes the effective window for semantic fields such as `dob`, `createdAt`, and `updatedAt`.

Object generation also runs a lightweight coherence pass after sibling values are generated. Common pairs such as `firstName` + `lastName` -> `fullName`, `firstName` + `lastName` + `domain` -> `email`, `domain` -> `url`, `createdAt` / `updatedAt`, `birthDate` / `age`, and `active` / `status` are aligned automatically. Address-like clusters now also force `country` back to the configured locale when sibling fields such as `city`, `state`, or `postalCode` indicate a locale-backed address, and money-like clusters keep `price <= amount <= balance` while deriving missing sibling values when one amount is already present. In `RELAXED` mode, that pass still leaves annotated or Bean Validation-constrained target fields alone; explicit field and type overrides always win.

## Fluent fixtures

Use `ObjectFaker<T>` when the default object graph is close but you want a few explicit rules, including nested paths:

```java
ObjectFaker<UserDto> faker = new ObjectFaker<>(UserDto.class)
        .ruleFor("firstName", () -> "Ada")
        .ruleFor("lastName", () -> "Lovelace")
        .ruleFor("address.city", () -> "London")
        .ruleFor("email",
                 user -> user.getFirstName().toLowerCase() + "."
                         + user.getLastName().toLowerCase() + "@example.com")
        .ignore("password")
        .profile("minimal", configured -> configured.include("firstName", "email"))
        .useProfile("minimal");

UserDto user = faker.generate();
List<UserDto> users = faker.generateList(100);
```

Supported in the current fixture layer:

- path-aware `ruleFor(...)` with plain generators, including nested paths such as `address.city`
- path-aware dependent `ruleFor(...)` based on the generated root object
- path-aware `ruleForContext(...)` with nested owner/depth metadata
- path-aware `ignore(...)`, including nested paths such as `address.city`
- path-aware `include(...)` to whitelist root fields or prune nested payload slices such as `address.city`
- named `profile(...)` plus `useProfile(...)` for reusable rule bundles
- `afterGenerate(...)` / `postProcess(...)`
- `populate(existing)` for mutable classes

Nested `include(...)` paths keep the parent object alive and clear sibling fields below that path unless you also include the wider root object.

`ObjectFaker<T>` reuses the same root `GeneratorConfig`, semantic resolver, locale, seed, and override rules as `ObjectGenerator<T>`.

## When to use

- Integration tests with larger object graphs.
- Snapshot fixture generation.
- Property-style randomized object tests.
