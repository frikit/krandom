# kRandom Product Roadmap

**Reviewed:** 2026-09-24
**Status:** Active
**Stable release:** `2.5.0`
**Development line:** `2.6.0-SNAPSHOT`

kRandom should compete on trustworthy fixture generation, not on raw provider count. The durable
advantages are deterministic replay, coherent object fixtures, explicit safety contracts, honest
performance evidence, and a small extension boundary.

The latest delivery record is in [`release-2.5.0-plan.md`](release-2.5.0-plan.md). This roadmap sets
product priorities; it does not authorize a tag or publication.

## Product principles

1. **Replayable**: failures carry enough value-free information to reproduce the generation path.
2. **Semantically valid**: related fields agree, supported constraints are honored, and unsupported
   states fail with context.
3. **Test-safe**: sensitive identity, finance, payment, network, and contributed data have explicit
   validity and safety classifications.
4. **Measurable**: performance claims use equivalent workloads, retained raw results, and stated
   regression budgets.
5. **Controllable**: Java and Kotlin callers can target, compose, derive, and explain fixtures
   without hidden global state.
6. **Small at the center**: domain packs and integrations grow at the edge instead of bloating core.

## Current competitive evidence

Official documentation checked on 2026-08-27 confirms the comparison areas that matter:

- [DataFaker providers](https://www.datafaker.net/documentation/providers/) demonstrate a very
  broad catalog, while its [schema documentation](https://www.datafaker.net/documentation/schemas/)
  covers CSV, JSON, SQL, YAML, XML, Java object, and TOML transformations.
- [Instancio's user guide](https://www.instancio.org/user-guide/) documents typed, predicate,
  setter, element, and scoped selectors, reusable models, selector precedence, and JUnit
  integration.
- [Easy Random](https://github.com/j-easy/easy-random) remains a useful simplicity benchmark and
  describes itself as maintenance-only.

The maintained local summary is [`../competitive-landscape.md`](../competitive-landscape.md).
Old provider-by-provider research snapshots were removed because they mixed dated competitor facts
with current kRandom guidance.

## Ordered priorities

Only the current release line is maintained; a priority may change defaults or remove API when
the result is better for current users, and the changelog records every such change.

### P1 — Fixture control and diagnosis

- Finish one documented selector precedence and scope model across Java and Kotlin.
- Report unused, ambiguous, and shadowed fixture rules in strict mode.
- Produce a value-sanitized generation report explaining seed source, recipe, path, provider,
  matched rule, constraint, safety policy, and fallback decisions.
- Extend JUnit integration only where the replay lifecycle remains explicit and testable.

### P1 — Extension and data ecosystem

- Generalize verified local data packs without runtime network loading.
- Require source, license, checksum, safety class, bounded size, owner, and invariant tests for
  every contributed dataset.
- Add integration modules only after two consumers or one strategic pilot demonstrate demand.

### P2 — Adoption evidence

- Keep migration examples compiling in clean Maven, Gradle, sbt, Mill, JPMS, Kotlin, and Spring
  consumers.
- Track benchmark regressions on comparable environments and retain raw results; back model or
  stream refactoring with comparable-machine JMH evidence against the documented budgets.
- Measure real migrations and rollback cost before making broad leadership claims.
- Extend native-image evidence beyond core (records, reflection, localized resources, and a custom
  module) before claiming native support for the Spring and Kotlin modules.

## Release gates

A release candidate requires all of the following:

- exact 100% JaCoCo instruction, line, branch, complexity, method, and class coverage;
- measured critical-path mutation thresholds;
- Java 21 and current-JDK CI, native-image smoke, JPMS, and all consumer examples;
- a changelog entry for every user-visible change, including removed or changed API;
- release rehearsal, Central-only verification, SBOMs, provenance, and recoverable publication.

Public installation guidance follows the latest stable release.

## Non-goals

- Copying novelty, fandom, sport, food, or medical catalogs into core to inflate provider count.
- Runtime network loading, hidden telemetry, or global mutable registries.
- A new module, SPI, or abstraction without consumer evidence.
- A broad “number one” claim based on one benchmark, provider count, downloads, or stars.
