---
layout: page
title: FAQ
permalink: /faq/
---

# FAQ

## Should I use `core` or `java-api`?

Today either is fine. `java-api` currently depends on `core` directly and exposes the same behavior.

## How do I keep generated data stable between test runs?

Use seeded generators (constructor overloads or `GeneratorConfig.seed(...)`) and reuse the same config.

## How do I apply locale consistently?

Build one `GeneratorConfig` with `locale(...)` and pass it to all locale-aware generators in the fixture pipeline.

## How do I generate structured records?

Use `Field` to bind provider names and `Schema` to generate single records or batches.

## How do I extend providers dynamically?

Use `ProviderHub.register(...)` and optional aliases via `registerAlias(...)`.

## Is Kotlin/Scala API parity complete?

Not yet. Current delivery focus is Java parity first.
