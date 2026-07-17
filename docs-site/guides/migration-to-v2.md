---
layout: page
title: Migrating to 2.0.0
permalink: /guides/migration-to-v2/
---

# Migrating to 2.0.0

2.0.0 is a major release. Read this guide before upgrading a fixture suite, especially when it uses
removed facade aliases, mutable global data registration, reseeding, finance or identity generators,
or constructor-bypassing object generation.

## Upgrade path

1. Pin the `2.0.0` artifact version in a branch and compile your test suite.
2. Replace removed facade aliases with the canonical `Generators` methods.
3. Replace `Generator.reseed(...)` with the typed `Seedable` contract, or construct a new generator from a seeded `GeneratorConfig`.
4. Move custom locale/provider registration into `DataRegistryContext` on the `GeneratorConfig` that consumes it.
5. Make finance and identity fixture intent explicit with the relevant safety policy; defaults fail closed rather than silently generating realistic identifiers.
6. Review object fixtures that depended on constructor bypass and use a type factory or an explicit construction policy where needed.
7. Keep seeded assertions while upgrading, then run the full test suite against the released 2.0.0 coordinates.

## Complete migration reference

The repository's [1.x-to-2.0.0 migration guide](https://github.com/frikit/krandom/blob/main/docs/migration/v1.6-to-v2.md) is the canonical reference. It contains the exact alias mapping, safety-policy examples, object-construction guidance, JPMS `opens` requirement, and immutable Kotlin fixture notes.

The same guidance applies when moving directly from the latest 1.x release: resolve each removed or changed API reported by the 2.0.0 compiler before relying on the upgraded fixtures.
