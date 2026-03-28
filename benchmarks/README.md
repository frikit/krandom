# Benchmarks

This module contains performance and observability workloads for kRandom.

## JMH microbenchmarks

Runs throughput benchmarks for:
- `ObjectGenerator` depth scenarios (2, 5, 10)
- `RegexGenerator` simple vs. complex patterns
- `FirstNameGenerator` across all built-in locales
- `Generators.ofObject(...)` vs direct object generator vs manual construction

Run:

```bash
./gradlew :benchmarks:jmh
```

## Macro profiling (100k / 1M loops)

Run:

```bash
./gradlew :benchmarks:profileGeneration
```

This prints ops/sec and heap delta for large generation loops and is intended for
CI-optional performance checks and release-note metrics.
