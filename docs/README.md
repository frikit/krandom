# Documentation Map

This directory contains maintained project documentation. Released implementation history is kept
in Git and release tags instead of duplicated as completed plans and dated review snapshots.

## Use kRandom

- [Public documentation site source](../docs-site/README.md)
- [Release runbook](release-runbook.md)
- [Locale contribution guide](locale-contribution-guide.md)
- [Generated provider catalog](reference/provider-catalog.md)
- [Benchmark methodology](benchmarks/METHODOLOGY.md) and
  [current dashboard](benchmarks/DASHBOARD.md)

## Migrate

- [k-random to kRandom](migration/k-random-to-krandom.md)
- [DataFaker to kRandom](migration/from-datafaker.md)
- [Easy Random to kRandom](migration/from-easyrandom.md)
- [Instancio to kRandom](migration/from-instancio.md)
- [JavaFaker to kRandom](migration/from-javafaker.md)

## Develop and plan

- [Contributing](../CONTRIBUTING.md) — workflow, quality gates, and maintenance tools
- [2.5.0 release plan](development/release-2.5.0-plan.md) — latest qualification and publication record
- [Product roadmap](development/market-leadership-roadmap.md) — priorities and release gates
- [Dependency reproducibility](development/dependency-reproducibility.md) — build-input policy and
  verification-metadata updates
- [Competitive landscape](competitive-landscape.md) — maintained high-level comparison

## Maintenance rules

- Public installation examples use `latestGaVersion` from `gradle.properties`.
- Repository-local consumer examples use `developmentVersion`.
- Generated references and benchmark reports state how to regenerate them.
- A completed execution plan is deleted after its durable decisions move into public guidance,
  policy, tests, or the current roadmap; Git history remains the archive. The latest release plan
  stays as the current publication record until the next release plan replaces it.
- Internal Markdown links and documentation facts are checked by the pre-commit gate and CI;
  docs-site routes are checked before commit and by the documentation workflow.
