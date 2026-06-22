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

**Scalar generation** — krandom is **8x to 19x faster** than DataFaker and **26x to 155x faster** than JavaFaker:

| Benchmark | krandom (ops/s) | DataFaker (ops/s) | krandom advantage |
|:---|---:|---:|:---|
| firstName | 62,097,588 | 3,340,974 | 18.6x |
| email | 7,041,044 | 868,973 | 8.1x |
| streetAddress | 13,305,613 | 951,856 | 14.0x |

**Object population** — krandom's `ObjectGenerator` uses semantic field matching (realistic data per field name), which trades throughput for data quality:

| Benchmark | krandom (ops/s) | DataFaker (ops/s) | EasyRandom (ops/s) | Instancio (ops/s) |
|:---|---:|---:|---:|---:|
| single object | 4,897 | 364,781 | 120,098 | 111,937 |

> EasyRandom and Instancio fill fields with arbitrary random values. krandom produces realistic data for each field (real names, valid emails, real cities). The throughput gap is the cost of semantic realism.

### Reports

Benchmark reports are stored in [`docs/benchmarks/`](../docs/benchmarks/) with one file per run. Reports are generated monthly to track performance trends.

| Report | Date | JDK |
|:---|:---|:---|
| [DASHBOARD.md](../docs/benchmarks/DASHBOARD.md) | 2026-04-25 | Temurin 21.0.10+7 |

### Running competitor benchmarks

Full competitor suite:

```bash
./gradlew :benchmarks:jmh --args="Competitor -wi 3 -i 5 -f 1 -t 1"
```

Single benchmark class:

```bash
./gradlew :benchmarks:jmh --args="CompetitorScalarBenchmark -wi 3 -i 5 -f 1 -t 1"
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

## Latest Internal Run Snapshot

This is an ad-hoc local `profileGeneration` snapshot used to spot-check
internals on a developer machine. It is separate from the monthly competitor
reports archived in [`docs/benchmarks/`](../docs/benchmarks/), which are the
authoritative, methodology-documented numbers.

Date: **March 29, 2026 12:52:48 BST**
JDK: **Temurin OpenJDK 21.0.10+7 LTS**
Command: `./gradlew :benchmarks:profileGeneration`

| case | count | ops/sec | heap-delta-mb |
|:---|:---|---:|---:|
| first-name | 1,000 | 23,233,121.14 | 0.00 |
| first-name | 10,000 | 24,492,335.12 | 0.00 |
| first-name | 100,000 | 42,399,830.40 | 0.00 |
| first-name | 1,000,000 | 38,650,890.66 | 0.00 |
| first-name | 10,000,000 | 54,909,693.10 | 0.00 |
| regex-ssn | 1,000 | 528,052.81 | 0.96 |
| regex-ssn | 10,000 | 5,214,332.53 | 9.05 |
| regex-ssn | 100,000 | 4,581,577.79 | 31.20 |
| regex-ssn | 1,000,000 | 5,283,201.64 | 8.45 |
| regex-ssn | 10,000,000 | 5,700,535.48 | 47.96 |
| object-simple-user | 1,000 | 72,946.78 | 16.00 |
| object-simple-user | 10,000 | 136,025.97 | 150.00 |
| object-simple-user | 100,000 | 185,608.95 | -273.95 |
| object-simple-user | 1,000,000 | 192,028.49 | -99.97 |
| object-simple-user | 10,000,000 | 192,427.60 | -130.00 |

Notes:

- Numbers above are a single local snapshot, not a stability baseline.
- `heap-delta-mb` can be negative because of GC timing during long runs.
- The benchmark suite has expanded since this snapshot; rerun `:benchmarks:jmh` or `:benchmarks:profileGeneration` for current numbers on the new cases.
- Use the structural-only benchmark cases as the current throughput baseline when evaluating the cost of stricter semantic realism.
