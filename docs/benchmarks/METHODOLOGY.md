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
