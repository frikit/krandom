---
layout: page
title: Performance and Observability
permalink: /guides/performance-and-observability/
---

# Performance and Observability

## Benchmark suite

kRandom ships a dedicated `benchmarks` module with JMH microbenchmarks and a macro profile runner.

Run the complete JMH suite:

```bash
./gradlew :benchmarks:jmh
```

Run the local macro profile:

```bash
./gradlew :benchmarks:profileGeneration
```

For a publication-grade report, use the repository wrapper:

```bash
./scripts/run_benchmarks.sh
```

The wrapper records the JDK, architecture, OS, JMH arguments, confidence intervals, allocation
profiles, and raw output. `./scripts/run_benchmarks.sh --quick` is only a local smoke check.

## Current accepted baseline

The accepted report was generated on 2026-08-26 with JDK 21.0.12.1, JMH 1.37, three forks, three
warmup iterations, five measurement iterations, one benchmark thread, and the GC profiler.

| Scalar workload | kRandom | DataFaker | JavaFaker |
|:---|---:|---:|---:|
| first name | 103,289,329 ops/s | 6,249,732 ops/s | 592,885 ops/s |
| email | 18,622,629 ops/s | 866,639 ops/s | 338,617 ops/s |
| street address | 22,917,208 ops/s | 1,834,284 ops/s | 114,079 ops/s |

Structural object generation and semantic fixture construction are different workloads. The
accepted report keeps them separate rather than presenting a misleading single leaderboard. See
the [benchmark dashboard](https://github.com/frikit/krandom/blob/main/docs/benchmarks/DASHBOARD.md)
and [methodology](https://github.com/frikit/krandom/blob/main/docs/benchmarks/METHODOLOGY.md) for
object, bulk, schema, allocation, and raw-result details.

## Structural versus semantic object cost

- `STRUCTURAL_ONLY` disables semantic field-name resolution and the coherence pass; use it as the
  raw object-throughput baseline.
- `RELAXED` enables semantic defaults while allowing annotations and Bean Validation constraints
  to win.
- `STRICT` makes a semantic match authoritative.

```java
GeneratorConfig config = GeneratorConfig.builder()
        .objectSemanticMode(ObjectGenerationSemanticMode.STRUCTURAL_ONLY)
        .build();
```

Compare the matching `ExpandedGenerationBenchmark` cases. Do not compare a structural generator
with a manually assembled semantic fixture as if they performed equivalent work.

## Regression budgets

Compare full runs only on the same otherwise-idle machine, JDK major version, architecture, OS
family, power mode, and dependency set.

- Investigate a throughput decrease of at least 10%; block an unexplained decrease of at least 20%.
- Investigate an allocation increase of at least 15%; block an unexplained increase of at least 25%.
- Measure schema streaming separately from in-memory batch generation.
- Record any accepted breach with the correctness, safety, or capability improvement that justifies
  it.

These budgets detect regressions; they are not cross-machine performance promises.

## Runtime diagnostics

Generation failures expose structured, value-sanitized context and an optional replay recipe. The
diagnostic path records types, operations, field paths, recipe identity, and cause class names; it
does not expose generated values or third-party exception messages. Configure a
`GenerationFailureListener` only when an application needs to collect those events.
