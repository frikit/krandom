# Benchmarks

This module contains performance workloads for kRandom:

- Proper JMH microbenchmarks (`@Benchmark`-based)
- A macro profile runner for large generation loops
- **Competitor benchmarks** — head-to-head comparison with DataFaker, EasyRandom, Instancio, and JavaFaker

## Competitor Benchmarks

Three benchmark classes compare krandom against other JVM fake-data libraries:

| Benchmark class | What it measures | Libraries compared |
|:---|:---|:---|
| `CompetitorScalarBenchmark` | Single value generation (firstName, email, streetAddress) | krandom, DataFaker, JavaFaker |
| `CompetitorObjectBenchmark` | POJO population (6-field flat object) | krandom, DataFaker, EasyRandom, Instancio |
| `CompetitorBulkBenchmark` | Batch generation (100 and 1000 objects) | krandom, DataFaker, EasyRandom, Instancio |

### Latest Results Summary

See the full report: [docs/benchmarks/DASHBOARD.md](../docs/benchmarks/DASHBOARD.md)

The latest publication-grade run was recorded on **2026-08-26** with JDK 21.0.12.1, three
forks, three warmup iterations, five measurement iterations, and allocation profiling. Treat it
as the first baseline for the current workload definitions; older reports used a different
protocol and are not regression-comparable.

**Scalar generation** (ops/s, higher is better):

| Benchmark | krandom (ops/s) | DataFaker (ops/s) | krandom advantage |
|:---|---:|---:|:---|
| firstName | 103,289,329 | 6,249,732 | 16.5x |
| email | 18,622,629 | 866,639 | 21.5x |
| streetAddress | 22,917,208 | 1,834,284 | 12.5x |

**Object population** keeps structural generation and semantic fixture construction separate:

| Workload | krandom | DataFaker | EasyRandom | Instancio |
|:---|---:|---:|---:|---:|
| structural object | 9,005 | — | 78,913 | 196,118 |
| semantic fixture | 3,202 | 506,915 | — | — |

DataFaker's manual fixture construction is a semantic workload. Easy Random and Instancio are
structural workloads in this suite. A blank cell means there is no equivalent workload, not a
zero score. See [METHODOLOGY.md](../docs/benchmarks/METHODOLOGY.md) for the full protocol and
regression budgets.

### Reports

Accepted benchmark reports are stored in [`docs/benchmarks/`](../docs/benchmarks/) with raw output
retained for each comparable full run. Generate a new report when preparing a release or
investigating a performance change; do not imply an automated cadence that the repository does not
enforce.

| Report | Date | JDK |
|:---|:---|:---|
| [DASHBOARD.md](../docs/benchmarks/DASHBOARD.md) | 2026-08-26 | 21.0.12.1 |

### Running competitor benchmarks

Full competitor suite:

```bash
./gradlew :benchmarks:jmh --args="Competitor -wi 3 -i 5 -f 3 -t 1 -prof gc"
```

Single benchmark class:

```bash
./gradlew :benchmarks:jmh --args="CompetitorScalarBenchmark -wi 3 -i 5 -f 3 -t 1 -prof gc"
```

## Internal Benchmarks

JMH benchmark classes for krandom internals:

- `FirstNameGeneratorBenchmark` — locale-parameterized first name throughput
- `RegexGeneratorBenchmark` — regex-based generation overhead
- `ObjectGeneratorBenchmark` — object graph depth scaling (depth 2/5/10)
- `ObjectFactoryBenchmark` — ObjectFaker fixture authoring
- `ExpandedGenerationBenchmark` — structural vs relaxed vs strict semantic modes
- `SchemaOutputBenchmark` — schema batch and streaming output (JSONL, CSV)

## Run JMH (Full Suite)

```bash
./gradlew :benchmarks:jmh
```

Run a subset with custom JMH args:

```bash
./gradlew :benchmarks:jmh --args="RegexGeneratorBenchmark -wi 3 -i 5 -f 1"
```

## Run Macro Profile

```bash
./gradlew :benchmarks:profileGeneration
```

## Latest Internal Results

The current internal workload scores and allocation profiles are published in
[the benchmark dashboard](../docs/benchmarks/DASHBOARD.md#internal-benchmarks). Use the full-run
protocol when establishing or comparing a release baseline; `profileGeneration` and quick runs
are local diagnostics only.
