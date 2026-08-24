---
layout: page
title: Object Generation
permalink: /guides/object-generation/
---

# Object Generation

`ObjectGenerator<T>` populates Java classes recursively.

## Configuration ownership

Use one immutable `GeneratorConfig` as the source of object-generation settings. Values supplied
through configuration collections or varargs are copied when the configuration is built; registered
generators, predicates, listeners, random sources, and factories remain caller-owned references and
are not invoked while the configuration is assembled. Treat those executable objects as immutable
for the lifetime of a configuration, or create a new configuration when they need to change.

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
There is no separate public object-only config path to learn: object defaults, overrides, exclusions,
semantic controls, and registry wiring all live on the root `GeneratorConfig` builder.

## Construction policy

`SAFE_CONSTRUCTORS` is the default. It runs constructors instead of manufacturing partially
initialized instances, and it preserves field initializers unless
`objectOverrideDefaultInitialization(true)` is selected.

| Target | Safe construction path | If unavailable |
|:---|:---|:---|
| Java record | Canonical constructor | Contextual construction failure |
| Mutable class with a no-argument constructor | No-argument constructor, then mutable fields | Contextual construction/access failure |
| Class with exactly one declared constructor | Generated constructor arguments, then mutable fields | Contextual construction/access failure |
| Class with multiple declared constructors | No implicit choice | Register a type factory |
| Interface or abstract root | No reflective allocation | Register a type factory |
| Local, anonymous, non-static inner, enum, annotation, array, or primitive root | Unsupported | Use a supported wrapper/factory design |

A type override is also a root factory and runs before reflection. Contextual factories win over
plain factories and receive `"$root"`, the requested type, and depth zero:

```java
GeneratorConfig config = GeneratorConfig.builder()
    .objectOverride(PaymentMethod.class, CardPayment::new)
    .build();

PaymentMethod payment = new ObjectGenerator<>(PaymentMethod.class, config).generate();
```

Factories must return a non-null value assignable to the registered type. They are the preferred
escape hatch for third-party, abstract, or deliberately immutable types.

`UNSAFE_CONSTRUCTOR_BYPASS` is a temporary compatibility option for classes that relied on the
legacy Objenesis fallback:

```java
GeneratorConfig legacy = GeneratorConfig.builder()
    .objectConstructionPolicy(ObjectConstructionPolicy.UNSAFE_CONSTRUCTOR_BYPASS)
    .build();
```

Unsafe bypass can skip constructor invariants and initializers. It still needs reflective access to
mutable fields, so it is not a JPMS workaround.

### Named modules and `opens`

`krandom-core` has the explicit module name `io.github.frikit.krandom`. A named consumer needs one
qualified `opens` clause for each package whose non-public constructors or mutable fields kRandom
populates:

```java
module com.example.fixtures {
    requires io.github.frikit.krandom;

    opens com.example.fixtures.model to io.github.frikit.krandom;
}
```

An exported package is not automatically open for reflection. If the clause is missing, object
generation fails with `REFLECTION` context and an error containing the exact directive to add. A
root factory avoids reflective construction and therefore does not require `opens` for that value.

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

Object generation also runs a lightweight coherence pass after sibling values are generated. Common pairs such as `firstName` + `lastName` -> `fullName`, `firstName` + `lastName` + `domain` -> `email`, `domain` -> `url`, `createdAt` / `updatedAt`, `birthDate` / `age`, and `active` / `status` are aligned automatically. Address-like clusters now reuse one locale-backed address snapshot so `streetAddress`, `city`, `state`, `postalCode`, and `country` stay consistent with each other, and money-like clusters keep `price <= amount <= balance` while deriving missing sibling values when one amount is already present. In `RELAXED` mode, that pass still leaves annotated or Bean Validation-constrained target fields alone; explicit field and type overrides always win.

## Bean Validation

Object population natively composes 21 Jakarta Validation constraints across fields, getters,
records, and interface accessors. See the [Bean Validation support matrix]({{ '/guides/bean-validation/' | relative_url }})
for exact target types, intersection rules, precedence, and exclusions.

## Fluent fixtures

Use `ObjectFaker<T>` when the default object graph is close but you want explicit rules. JavaBean
getter and record-accessor references provide refactor-safe root paths; `PropertyPath.then(...)`
composes nested paths without string literals:

```java
ObjectFaker<UserDto> faker = new ObjectFaker<>(UserDto.class)
        .ruleFor(UserDto::getFirstName, () -> "Ada")
        .ruleFor(UserDto::getLastName, () -> "Lovelace")
        .ruleFor(PropertyPath.of(UserDto::getAddress).then(Address::getCity), () -> "London")
        .ruleFor(UserDto::getEmail,
                 user -> user.getFirstName().toLowerCase() + "."
                         + user.getLastName().toLowerCase() + "@example.com")
        .ignore(UserDto::getPassword)
        .profile("minimal", configured -> configured.include("firstName", "email"))
        .useProfile("minimal");

UserDto user = faker.generate();
List<UserDto> users = faker.generateList(100);
```

Supported in the current fixture layer:

- path-aware `ruleFor(...)` with plain generators, including nested paths such as `address.city`
- type-safe getter and record-accessor rules, with nested `PropertyPath` composition
- path-aware dependent `ruleFor(...)` based on the generated root object
- path-aware `ruleForContext(...)` with nested owner/depth metadata
- path-aware `ignore(...)`, including nested paths such as `address.city`
- path-aware `include(...)` to whitelist root fields or prune nested payload slices such as `address.city`
- named `profile(...)` plus `useProfile(...)` for reusable rule bundles
- `afterGenerate(...)` / `postProcess(...)`
- `populate(existing)` for mutable classes
- `strict()` to validate the complete explicit rule/include/ignore decision before generation

Use `ObjectModel<T>` when a fixture definition must be immutable, reusable, and composable:

```java
ObjectModel<UserDto> names = ObjectModel.of(UserDto.class)
        .configure(faker -> faker
                .ruleFor(UserDto::getFirstName, () -> "Ada")
                .ruleFor(UserDto::getLastName, () -> "Lovelace"));

ObjectModel<UserDto> contact = ObjectModel.of(UserDto.class)
        .configure(faker -> faker.ruleFor(
                UserDto::getEmail,
                user -> user.getFirstName().toLowerCase() + "@example.com"));

UserDto user = names.and(contact).generate(config);
```

Each model creates a fresh `ObjectFaker`; stateful field generators should be constructed inside
the model configuration callback rather than captured and shared.

Nested `include(...)` paths keep the parent object alive and clear sibling fields below that path unless you also include the wider root object.

`ObjectFaker<T>` reuses the same root `GeneratorConfig`, semantic resolver, locale, seed, and override rules as `ObjectGenerator<T>`.

## When to use

- Integration tests with larger object graphs.
- Snapshot fixture generation.
- Property-style randomized object tests.
