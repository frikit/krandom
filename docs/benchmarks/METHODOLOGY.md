# Benchmark Methodology

krandom benchmarks answer separate questions rather than collapsing unlike workloads into one
leaderboard:

- **Scalar generation** measures one provider value per invocation.
- **Structural object generation** populates a type without semantic field-name routing.
- **Semantic fixture generation** applies provider-backed field semantics and coherence rules.
- **Bulk generation** reports structural and semantic workloads independently.
- **Schema/export generation** measures serialization and streaming work separately from object
  creation.

DataFaker's manual fixture construction is a semantic fixture workload. Easy Random and Instancio
are structural object-generation workloads in this suite. A blank competitor cell means the suite
does not contain an equivalent workload; it is not a zero score.

## Workload fairness

Per-invocation benchmarks measure generation, not setup. Every library reuses state prepared once
per JMH thread: krandom reuses its `ObjectGenerator`, DataFaker its `Faker`, Easy Random its
`EasyRandom` instance, and Instancio a prebuilt `Model` created with `Instancio.of(type).toModel()`
and passed to `Instancio.create(model)`. Instancio's `Instancio.create(type)` shortcut would rebuild
its API specification on every call and is not used for per-object scores. Bulk benchmarks build
the Instancio list specification once per batch, so that setup is amortized over the batch size.

`FieldStreamsBenchmark` reports every stream policy with `STRUCTURAL_ONLY` semantics, which isolates
stream planning, and with the default `RELAXED` semantics that ordinary callers get, which includes
semantic field-name routing. Compare policies within one semantic mode; the gap between the two
modes is the semantic-routing cost, not a stream-policy cost.

## Publication protocol

A publishable run uses three forks, three warmup iterations, five measurement iterations, one
benchmark thread, and the JMH GC profiler. The raw output therefore includes confidence intervals
and allocation data. `./scripts/run_benchmarks.sh --quick` uses one fork and is only a local smoke
check; its numbers must not replace the published dashboard.

Run comparisons on the same otherwise-idle machine, JDK major version, architecture, OS family,
power mode, and benchmark dependency set. Review the source whenever a dependency version changes.

## Regression budgets

Compare the median score across forks with the previous accepted full run from the same benchmark
environment:

- investigate a throughput decrease of at least 10%;
- block an unexplained throughput decrease of at least 20%;
- investigate an allocation increase of at least 15%;
- block an unexplained allocation increase of at least 25%.

Budgets detect regressions; they are not cross-machine performance promises. Any accepted breach
must document the behavioral or safety improvement that justifies it.

## Accepted breaches

None is open.

- **3.0.1, `SchemaOutputBenchmark`; closed in 3.2.0.** In a `Schema` with a portable seeded configuration, a `Field`
  reference, a reference that `registerProvider(...)` adds, a template's `#` and `?`, and a column that
  `SchemaParser` resolves by its name now draw from each value's own named stream, as the `Schema`
  documentation promises, so that adding a column no longer changes the values of the others. Each such
  value builds its generator from a seed drawn from its stream. On one machine
  ([raw output](results/2026-11-11-schema-output-3.0.1.txt)), the four workloads gave, before the fix,
  with it, and once each bundled registry's locale keys were read once and each value's configuration
  reused one builder, in operations a second: `largeSchemaBatch` 933, 88 and 194;
  `largeSchemaJsonLines` 748, 87 and 183; `streamingCsv` 864, 85 and 189; and `streamingJsonLines` 798,
  88 and 183. `largeSchemaBatch` allocated 3.7 MB, 61.7 MB and then 30.5 MB an operation. In 3.0.2, the
  child stream that a composite generator gives each of its children copies a configuration without
  modules instead of rebuilding it, with the same values. In one sitting on the same machine
  ([raw output](results/2026-11-12-schema-output-3.0.2.txt)), that took the four workloads from 197,
  186, 194 and 189 to 264, 245, 259 and 253 operations a second, and `largeSchemaBatch` from 30.8 MB to
  20.6 MB an operation. In 3.0.3, a street-address generator reads its locale's secondary units once
  instead of each time it is built, with the same values. In one sitting on the same machine
  ([raw output](results/2026-11-12-schema-output-3.0.3.txt)), that took the four workloads from 240,
  218, 229 and 229 to 478, 399, 449 and 428 operations a second, and `largeSchemaBatch` from 20.7 MB to
  12.5 MB an operation. Against the runs before the fix, a 49% throughput decrease and a 3.4-fold
  allocation increase remained, outside both budgets, accepted because the fix restores the documented
  independence of schema columns. In 3.2.0, each value of a common built-in `Field` reference reseeds one
  generator kept for its column and thread instead of building one, and gets the values a build gives; the
  benchmark's twelve columns are all such references. In two sittings on the same machine ([raw
  output](results/2026-11-13-schema-output-3.2.0.txt)), the first in the order `main` before the change,
  the change, the change, `main`, that took the four workloads from 518, 423, 469 and 462 to 978,
  696, 786 and 794 operations a second, and their allocation from 12.5, 12.9, 12.5 and 12.5 MB to 4.0, 4.5,
  3.9 and 4.0 MB an operation. The second sitting ran the change beside `e6ba573d`, the commit before the
  3.0.1 fix, in the order `e6ba573d`, the change, the change, `e6ba573d`: 988, 702, 806 and 796 operations
  a second against 928, 749, 870 and 802, with 4.0, 4.4, 3.9 and 4.0 MB against 3.6, 4.1, 3.7 and 3.6 MB
  an operation (means of two runs). Compared as the budgets ask, by the median of each commit's six fork
  scores, the change is +6.7%, −6.6%, −8.6% and −0.6% against `e6ba573d`, with 10.5%, 9.4%, 8.1% and 10.5%
  more allocation. Every workload is within both budgets, and inside the lines that call for an
  investigation, so the breach is closed. References that 3.2.0 does not reseed, and every reference of a configuration with
  modules or its own `DataRegistryContext`, still build a generator for each value; this benchmark does
  not measure them.
