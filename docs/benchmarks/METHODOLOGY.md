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

- **3.0.1, `SchemaOutputBenchmark`.** In a `Schema` with a portable seeded configuration, a `Field`
  reference, a reference that `registerProvider(...)` adds, a template's `#` and `?`, and a column that
  `SchemaParser` resolves by its name now draw from each value's own named stream, as the `Schema`
  documentation promises, so that adding a column no longer changes the values of the others. Each such
  value builds its generator from a seed drawn from its stream. On one machine
  ([raw output](results/2026-11-11-schema-output-3.0.1.txt)), the four workloads gave, before the fix,
  with it, and once each bundled registry's locale keys were read once and each value's configuration
  reused one builder, in operations a second: `largeSchemaBatch` 933, 88 and 194;
  `largeSchemaJsonLines` 748, 87 and 183; `streamingCsv` 864, 85 and 189; and `streamingJsonLines` 798,
  88 and 183. `largeSchemaBatch` allocated 3.7 MB, 61.7 MB and then 30.5 MB an operation. A 79%
  throughput decrease and an 8.3-fold allocation increase remain, outside both budgets. They are
  accepted because the fix restores the documented independence of schema columns. Reaching the budgets
  with the same values needs the composite generators to reset their streams for each value, which is
  new public API, so it waits for a minor release.
