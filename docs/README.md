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
- [1.x to 2.0.0](migration/v1.6-to-v2.md)
- [DataFaker to kRandom](migration/from-datafaker.md)
- [Easy Random to kRandom](migration/from-easyrandom.md)
- [Instancio to kRandom](migration/from-instancio.md)
- [JavaFaker to kRandom](migration/from-javafaker.md)

## Develop and plan

- [2.3.0 release plan](development/release-2.3.0-plan.md) — current executable plan
- [Major-version decision](development/v3-preparation-plan.md) — conditions for revisiting v3
- [Product roadmap](development/market-leadership-roadmap.md) — priorities and release gates
- [Dependency reproducibility](development/dependency-reproducibility.md) — build-input policy
- [Competitive landscape](competitive-landscape.md) — maintained high-level comparison

## Maintenance rules

- Public installation examples use `latestGaVersion` from `gradle.properties`.
- Repository-local consumer examples use `developmentVersion`.
- Generated references and benchmark reports state how to regenerate them.
- A completed execution plan is deleted after its durable decisions move into public guidance,
  policy, tests, or the current roadmap; Git history remains the archive.
- Internal Markdown links, documentation facts, and docs-site routes are checked before commit.
