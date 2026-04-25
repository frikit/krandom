# Competitor Benchmark Report — 2026-04-25

## Environment

| Property | Value |
|:---|:---|
| Date | 2026-04-25 |
| JDK | Temurin OpenJDK 21.0.10+7-LTS |
| Architecture | aarch64 (Apple Silicon) |
| OS | macOS Darwin 25.4.0 |
| JMH | 1.37 |
| Blackhole | Compiler (auto-detected) |
| JMH args | `-wi 3 -i 5 -f 1 -t 1` |

## Library Versions

| Library | Maven coordinates | Version |
|:---|:---|:---|
| kRandom | `io.github.frikit:krandom-core` | 0.1.0-SNAPSHOT |
| DataFaker | `net.datafaker:datafaker` | 2.4.2 |
| JavaFaker | `com.github.javafaker:javafaker` | 1.0.2 |
| EasyRandom | `org.jeasy:easy-random-core` | 5.0.0 |
| Instancio | `org.instancio:instancio-core` | 5.2.0 |

## Scalar Value Generation

Generates a single value per invocation. Higher is better.

| Benchmark | krandom | DataFaker | JavaFaker | krandom vs DataFaker | krandom vs JavaFaker |
|:---|---:|---:|---:|:---|:---|
| firstName | **62,097,588** | 3,340,974 | 471,587 | **18.6x** | **131.7x** |
| email | **7,041,044** | 868,973 | 270,552 | **8.1x** | **26.0x** |
| streetAddress | **13,305,613** | 951,856 | 85,805 | **14.0x** | **155.1x** |

Units: ops/s (throughput, higher is better).

### Notes

- All generators seeded with `7L` for deterministic output.
- krandom uses direct array lookup for name generation; DataFaker and JavaFaker parse YAML locale files at runtime.
- JavaFaker is unmaintained (last release 2018) and included only as a legacy baseline. DataFaker is the active successor.

## Object Population

Populates a flat POJO (`ComparableUser`: 6 fields — `firstName`, `lastName`, `email`, `age`, `city`, `country`) per invocation. Higher is better.

| Benchmark | krandom | DataFaker | EasyRandom | Instancio |
|:---|---:|---:|---:|---:|
| single object | 4,897 | **364,781** | 120,098 | 111,937 |

Units: ops/s (throughput, higher is better).

### Notes

- **krandom** uses `ObjectGenerator` with full semantic field matching: it analyzes each field name (e.g. `firstName` → `FirstNameGenerator`, `email` → `EmailGenerator`) and produces realistic data through its registry. This is intentional — every field gets a domain-appropriate value.
- **DataFaker** result uses manual field assignment (`faker.name().firstName()`, etc.) because `faker.populate()` requires `@Fake` annotations. This measures DataFaker's scalar throughput applied to object construction, not reflection-based population.
- **EasyRandom** and **Instancio** use reflection to fill fields with random values of the correct type, but without semantic awareness (a `firstName` field gets an arbitrary random string, not a real name).
- The throughput gap is the cost of semantic realism. krandom's `ObjectGenerator` is designed for correctness-first test fixtures, not raw population speed. When speed is prioritized over realism, use krandom's scalar generators directly (see scalar results above).

## Bulk Generation

Generates a list of N objects per invocation. Higher is better.

| Benchmark | Batch size | krandom | DataFaker | EasyRandom | Instancio |
|:---|:---|---:|---:|---:|---:|
| bulk | 100 | 49 | 4,027 | 1,125 | **7,508** |
| bulk | 1,000 | 5 | 407 | 109 | **871** |

Units: ops/s (throughput, higher is better).

### Notes

- krandom uses `ObjectGenerator.generateList(N)` which applies full semantic matching per object (same overhead as the single-object benchmark, multiplied by N).
- DataFaker bulk is a loop of manual field assignment.
- Instancio and EasyRandom use their native bulk APIs (`Instancio.ofList()`, `easyRandom.objects()`).
- Scaling is roughly linear for all libraries (1000-batch is ~10x slower than 100-batch).

## Raw JMH Output

```
Benchmark                                         (batchSize)   Mode  Cnt         Score         Error  Units
CompetitorBulkBenchmark.dataFakerBulk                     100  thrpt    5      4026.605 ±     111.120  ops/s
CompetitorBulkBenchmark.dataFakerBulk                    1000  thrpt    5       406.713 ±       9.902  ops/s
CompetitorBulkBenchmark.easyRandomBulk                    100  thrpt    5      1125.480 ±      42.909  ops/s
CompetitorBulkBenchmark.easyRandomBulk                   1000  thrpt    5       109.329 ±       2.076  ops/s
CompetitorBulkBenchmark.instancioBulk                     100  thrpt    5      7508.122 ±     434.276  ops/s
CompetitorBulkBenchmark.instancioBulk                    1000  thrpt    5       871.027 ±      10.174  ops/s
CompetitorBulkBenchmark.krandomBulk                       100  thrpt    5        49.374 ±       0.440  ops/s
CompetitorBulkBenchmark.krandomBulk                      1000  thrpt    5         4.781 ±       0.241  ops/s
CompetitorObjectBenchmark.dataFakerObject                 N/A  thrpt    5    364780.912 ±   75982.091  ops/s
CompetitorObjectBenchmark.easyRandomObject                N/A  thrpt    5    120097.725 ±   20543.991  ops/s
CompetitorObjectBenchmark.instancioObject                 N/A  thrpt    5    111936.557 ±    5958.905  ops/s
CompetitorObjectBenchmark.krandomObject                   N/A  thrpt    5      4897.019 ±     499.549  ops/s
CompetitorScalarBenchmark.dataFakerEmail                  N/A  thrpt    5    868972.932 ±   41928.390  ops/s
CompetitorScalarBenchmark.dataFakerFirstName              N/A  thrpt    5   3340974.131 ±  157182.000  ops/s
CompetitorScalarBenchmark.dataFakerStreetAddress          N/A  thrpt    5    951856.043 ±   29764.718  ops/s
CompetitorScalarBenchmark.javaFakerEmail                  N/A  thrpt    5    270551.972 ±   12324.123  ops/s
CompetitorScalarBenchmark.javaFakerFirstName              N/A  thrpt    5    471586.886 ±   12905.220  ops/s
CompetitorScalarBenchmark.javaFakerStreetAddress          N/A  thrpt    5     85804.591 ±   13204.991  ops/s
CompetitorScalarBenchmark.krandomEmail                    N/A  thrpt    5   7041044.348 ± 1098235.686  ops/s
CompetitorScalarBenchmark.krandomFirstName                N/A  thrpt    5  62097587.985 ± 2240940.742  ops/s
CompetitorScalarBenchmark.krandomStreetAddress            N/A  thrpt    5  13305613.385 ± 1797316.259  ops/s
```

## Reproducing

```bash
./gradlew :benchmarks:jmh --args="Competitor -wi 3 -i 5 -f 1 -t 1"
```

Requires Java 21+. Set `JAVA_HOME` if your default JDK differs.
