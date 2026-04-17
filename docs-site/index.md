---
layout: page
title: Home
permalink: /
---

# kRandom Java Documentation

kRandom is a Java random-data generation library built for test fixtures, seedable fake data, and object graph population.

This site is focused on the Java side only.

## What you can do

- Generate primitive, text, date/time, network, finance, file, commerce, and user/profile data.
- Build seeded deterministic generators for repeatable tests.
- Populate POJOs with `ObjectGenerator`.
- Build record batches with schema-style APIs (`Field` + `Schema`).
- Use `ProviderHub` for generic provider lookup/aliases/runtime extension.

## Quick links

- [Getting Started](getting-started.md)
- [Generator Catalog](generator-catalog.md)
- [Examples](examples.md)
- [Guides](guides.md)
- [FAQ](faq.md)

## Design direction (current)

- Java is the source-of-truth implementation.
- Consumer integrations build directly on `core`.
- `jackson` is the only additional published integration module in this repository.
