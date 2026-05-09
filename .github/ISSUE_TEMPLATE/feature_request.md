---
name: Feature request
about: Propose a new generator, API, integration, or capability
title: "[feature] "
labels: enhancement
---

## Use case

What are you trying to do? Describe the problem before the solution — what
fixture, dataset, or test scenario does kRandom not support today?

## Proposed API or behavior

A short sketch of the API you'd like to call. Pseudocode is fine:

```java
// example
String x = Generators.ofYourNewThing().generate();
```

## Alternatives considered

Existing generators, providers, schemas, or integrations you tried first
and why they didn't fit.

## Scope

- Library this maps to (if any): DataFaker / JavaFaker / Easy Random /
  Instancio / Bogus / Faker.js / Mimesis / GoFakeit / Fake-rs / other.
- Locale-specific? If yes, which locales matter most.
- Should it live in `core` or in an existing/new integration module?

## Anything else

Links to references, sample data, related issues, etc.
