# Benchmarks

This module contains performance workloads for kRandom:

- Proper JMH microbenchmarks (`@Benchmark`-based)
- A macro profile runner for large generation loops

## Current Benchmark Suite

JMH benchmark classes:

- `FirstNameGeneratorBenchmark`
- `RegexGeneratorBenchmark`
- `ObjectGeneratorBenchmark`
- `ObjectFactoryBenchmark`
- `ExpandedGenerationBenchmark`
- `SchemaOutputBenchmark`

`ExpandedGenerationBenchmark` now includes:

- structural-only semantic-customer baseline
- relaxed semantic customer generation
- strict semantic customer generation
- uniqueness-heavy semantic customer generation

Macro profile runner:

- `GenerationProfileRunner`
- Run sizes: `1,000`, `10,000`, `100,000`, `1,000,000`, `10,000,000`
- Output columns: `count`, `ops/sec`, `heap-delta-mb`
- Current macro cases include structural-only vs relaxed vs strict object generation, uniqueness-heavy generation, schema batch generation, and streaming JSONL/CSV writers

## Run JMH (Proper)

Run the full suite:

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

## Latest Run Snapshot

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
