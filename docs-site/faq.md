---
layout: page
title: FAQ
permalink: /faq/
---

# FAQ

## Which module should I use?

Use `core` for generation APIs and `jackson` only when you need Jackson integration.

## How do I keep generated data stable between test runs?

Use seeded generators (constructor overloads or `GeneratorConfig.seed(...)`) and reuse the same config.

## How do I apply locale consistently?

Build one `GeneratorConfig` with `locale(...)` and pass it to all locale-aware generators in the fixture pipeline.

## How do I generate structured records?

Use `Field` to bind provider names and `Schema` to generate single records or batches.

## How do I extend providers dynamically?

Use `ProviderHub.register(...)` and optional aliases via `registerAlias(...)`.

## Can Kotlin or Scala consume the library?

Yes. Use the Java `core` artifact directly from Kotlin or Scala. Dedicated wrapper modules are not part of the current repository.
