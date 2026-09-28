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
