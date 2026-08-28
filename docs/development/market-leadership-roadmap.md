# kRandom Product Roadmap

**Reviewed:** 2026-08-27
**Status:** Active
**Stable release:** `2.2.0`
**Development line:** `3.0.0-SNAPSHOT`

kRandom should compete on trustworthy fixture generation, not on raw provider count. The durable
advantages are deterministic replay, coherent object fixtures, explicit safety contracts, honest
performance evidence, and a small extension boundary.

The executable v3 sequence is in [`v3-preparation-plan.md`](v3-preparation-plan.md). This roadmap
sets product priorities; it does not authorize a tag, publication, or breaking API change.

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

### P0 — Establish the v3 contract

- Complete the 2.1 post-release baseline and documentation cleanup.
- Choose one migration-worthy v3 contract with consumer evidence and a reversible rollout.
- Publish the API diff and migration outline before implementation.

### P1 — Fixture control and diagnosis

- Finish one documented selector precedence and scope model across Java and Kotlin.
- Report unused, ambiguous, and shadowed fixture rules in strict mode.
- Produce a value-sanitized generation report explaining seed source, recipe, path, provider,
  matched rule, constraint, safety policy, and fallback decisions.
- Extend JUnit integration only where the replay lifecycle remains explicit and testable.

### P1 — Extension and data ecosystem

- Generalize verified local data packs without runtime network loading.
- Publish an extension compatibility kit against the oldest supported release and the development
  line.
- Require source, license, checksum, safety class, bounded size, owner, and invariant tests for
  every contributed dataset.
- Add integration modules only after two consumers or one strategic pilot demonstrate demand.

### P2 — Adoption evidence

- Keep migration examples compiling in clean Maven, Gradle, sbt, Mill, JPMS, Kotlin, and Spring
  consumers.
- Track benchmark regressions on comparable environments and retain raw results.
- Measure real migrations and rollback cost before making broad leadership claims.

## Release gates

A v3 release candidate requires all of the following:

- one approved, documented major-version contract;
- an exact public API diff against 2.2.0;
- compile-tested migration examples and rollback guidance;
- exact 100% JaCoCo instruction, line, branch, complexity, method, and class coverage;
- measured critical-path mutation thresholds;
- Java 21 and current-JDK CI, native-image smoke, JPMS, and all consumer examples;
- release rehearsal, Central-only verification, SBOMs, provenance, and recoverable publication.

Until those gates pass, documentation must say that v3 is in preparation and `2.2.0` remains the
latest stable release.

## Non-goals

- Copying novelty, fandom, sport, food, or medical catalogs into core to inflate provider count.
- Runtime network loading, hidden telemetry, or global mutable registries.
- A new module, SPI, or abstraction without consumer evidence.
- A broad “number one” claim based on one benchmark, provider count, downloads, or stars.
- Breaking working APIs only to make the version number look significant.
