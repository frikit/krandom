---
layout: page
title: Performance and Observability
permalink: /guides/performance-and-observability/
---

# Performance and Observability

## Benchmark suite

kRandom ships a dedicated `benchmarks` module with JMH and macro profile workloads.

Run JMH:

```bash
./gradlew :benchmarks:jmh
```

Run macro profile (100k / 1M loops):

```bash
./gradlew :benchmarks:profileGeneration
```

## Latest benchmark summary

> Environment: local developer machine (Apple Silicon), Java 21, JMH 1.37, single fork.
> Run date: 2026-03-28.

| Benchmark | Result (ops/s) | Notes |
|---|---:|---|
| `ObjectGeneratorBenchmark.depth2ObjectGraph` | 340,624 | Shallow object graph |
| `ObjectGeneratorBenchmark.depth5ObjectGraph` | 214,856 | Medium object graph |
| `ObjectGeneratorBenchmark.depth10ObjectGraph` | 134,595 | Deep object graph |
| `RegexGeneratorBenchmark.simplePattern` | 6,655,776 | Simple regex generation |
| `RegexGeneratorBenchmark.complexPattern` | 2,722,606 | Complex regex generation |
| `FirstNameGeneratorBenchmark.firstName` | 60,457,463 (`en_US`) | Locale-parameterized run spans ~60.4M-61.1M ops/s |
| `ObjectFactoryBenchmark.generatorsFacadeObjectGenerator` | 195,359 | `Generators.ofObject(...)` path |
| `ObjectFactoryBenchmark.manualConstruction` | 23,100,118 | Manual baseline |

## Macro profile summary (100k / 1M loops)

| Workload | Count | Ops/s | Heap delta (MB) |
|---|---:|---:|---:|
| first-name | 100,000 | 35,887,314 | 0.00 |
| first-name | 1,000,000 | 54,737,036 | 0.00 |
| regex-ssn | 100,000 | 3,561,978 | 7.45 |
| regex-ssn | 1,000,000 | 4,831,820 | 9.68 |
| object-simple-user | 100,000 | 153,224 | 50.65 |
| object-simple-user | 1,000,000 | 197,102 | 38.02 |

## Optimization notes

- `RegexGenerator` now caches parsed pattern trees globally and reuses immutable parse structures.
- `CountryGenerator` now lazy-loads ISO alpha-3 country-code data on first use.
- `benchmarks/profileGeneration` provides CI-optional macro runs for startup + large throughput checks.

## Structural vs semantic object cost

The benchmark suite now keeps an explicit structural-only baseline next to the semantic object cases.

- `STRUCTURAL_ONLY`: use this as the raw throughput baseline for object generation.
- `RELAXED`: semantic defaults are enabled, but annotations and Bean Validation can still win.
- `STRICT`: semantic defaults win whenever a semantic match exists.

For JMH, compare the `ExpandedGenerationBenchmark` object cases directly.

For macro runs, compare:

- `object-structural-customer`
- `object-relaxed-customer`
- `object-semantic-customer`

If a workload cares more about throughput than realism, set:

```java
GeneratorConfig cfg = GeneratorConfig.builder()
        .objectSemanticMode(ObjectGenerationSemanticMode.STRUCTURAL_ONLY)
        .build();
```

That disables semantic field-name resolution and the coherence pass while keeping the same object-generation entry points.

## Performance budgets

The project currently tracks these practical budgets:

- scalar generators should stay in the high-throughput, low-allocation range and should not regress noticeably against existing first-name / regex baselines
- structural-only object generation is the baseline for object throughput comparisons
- relaxed and strict semantic object generation should be measured against that structural baseline before release work
- schema streaming writers should remain measurable separately from in-memory batch generation

These are release-readiness budgets, not hardcoded runtime limits. Re-run the benchmark suite after object-semantic or schema-output changes and compare against the structural-only baseline rather than against old point-in-time snapshot numbers alone.

## Capability matrix (versioned snapshot)

| Capability | kRandom | Java Faker | Chance.js |
|---|---|---|---|
| Java-first typed API | Yes | Yes | No |
| Deterministic seed support | Yes | Yes | Yes |
| Object graph generator | Yes (`ObjectGenerator`) | Partial (manual patterns) | No |
| Locale registry override API | Yes (`DataRegistryContext`) | Limited | N/A |
| Provider hub extensions | Yes (`ProviderHub`) | Limited | No |
| Built-in JMH benchmark module | Yes | No | No |
