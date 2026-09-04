---
layout: page
title: FAQ
permalink: /faq/
---

# FAQ

## Which module should I use?

Use `krandom-core` for generation APIs. Add `krandom-jackson` for Jackson serialization support,
`krandom-junit` for the JUnit seed extension, `krandom-spring-boot-starter` for Spring Boot,
`krandom-kotest-extensions` for Kotest adapters, or `krandom-kotlin-dsl` for Kotlin fixture rules.
Import `krandom-bom:2.3.0` to align multiple kRandom modules.

## How do I keep generated data stable between test runs?

Build and reuse a seeded configuration, for example
`GeneratorConfig.builder().seed(42L).build()`.

## How do I apply locale consistently?

Build one `GeneratorConfig` with `locale(...)` and pass it to all locale-aware generators in the fixture pipeline.

## How do I generate structured records?

Use `Field` to bind provider names and `Schema` to generate single records or batches.

## How do I extend providers dynamically?

Use an explicitly installed, configuration-scoped `KRandomModule` when the same contribution must
work through provider lookup, schemas, safety metadata, and semantic object fields. Use
`ProviderHub.register(...)` only for a short-lived hub-local provider.

## Can Kotlin or Scala consume the library?

Yes. Kotlin and Scala can use the Java `krandom-core` artifact directly. Kotlin projects can also use `krandom-kotlin-dsl` for typed fixture rules, while Kotest projects can use `krandom-kotest-extensions` for property-testing adapters. See [Kotlin DSL]({{ '/guides/kotlin-dsl/' | relative_url }}) and [Property Testing Integrations]({{ '/guides/property-testing-integrations/' | relative_url }}).
