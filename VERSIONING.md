# Versioning and compatibility policy

Every published jar module is checked for public binary and source compatibility against the `apiBaselineVersion` configured in `gradle.properties`. Run `./gradlew checkApiContract`; compatibility reports are written under `build/reports/japicmp/`, and evolution reports under `build/reports/api-evolution/`. The evolution gate rejects added, removed, or structurally changed public elements unless the exact element is classified in `config/api-evolution-allowlist.txt`. The compatibility gate still checks those allowed elements for breakage. Annotation-only and behavioral changes require review because japicmp reports them but does not treat every such change as a modified element. Direct jar comparison also ignores missing third-party classes. Intentional major-version breaks require a reviewed migration path and a narrow entry in `config/api-compatibility-excludes.txt`; package-wide exclusions are forbidden.

kRandom follows [Semantic Versioning 2.0.0](https://semver.org) for all
published artifacts (`io.github.frikit:krandom-*`).

The latest stable release is `2.1.0`. The repository development line is
`3.0.0-SNAPSHOT`, compared against the released `2.1.0` public API. A snapshot version is not a
release promise; the [v3 preparation plan](docs/development/v3-preparation-plan.md) must approve a
concrete major-version contract and migration before 3.0.0 can reach GA.

## What a version number promises

- **Major** (`X.0.0`): may remove or change public API. Migration notes are
  published in the changelog and, for larger changes, as a guide under
  `docs/migration/`. The 1.x-to-2.0.0 migration path is documented in
  [`docs/migration/v1.6-to-v2.md`](docs/migration/v1.6-to-v2.md).
- **Minor** (`1.X.0`): additive only. Existing public API keeps compiling and
  behaving as documented. New generators, locales, and configuration options
  may be added.
- **Patch** (`1.0.X`): bug fixes only. No new API, no behavior changes beyond
  the documented fix.

A major release does not justify arbitrary cleanup. Every incompatible entry must be narrow,
reviewed, listed in the changelog, and paired with a compile-tested migration and rollback path.

The public API is everything exported by the published modules' packages and
documented in Javadoc. Internal packages and classes marked package-private
carry no compatibility guarantee.

## Seed and output stability

- Within a given release, the same seed and configuration always produce the
  same output (see the `fnv1a64-v1` string-seed derivation contract).
- **Patch releases** never change the value streams produced by a fixed seed.
- **Minor releases** may extend built-in datasets (names, cities, locales),
  which can change the concrete values a fixed seed produces. Tests should
  assert on shape and constraints, not on exact generated values, unless they
  pin both the library version and the seed.
- **Major releases** may change a recipe or stream only when the changelog and migration guide name
  the versioned transition and provide a reproducible before/after example.

## Platform support window

**Java 21 is the deliberate baseline for the 1.x, 2.x, and planned 3.x lines.** This is a
positioning decision, not an oversight: it will not be lowered within either
major line, and any future change to the Java baseline would only happen in a
major release.

### Compatibility table

| kRandom | Java | Spring Boot (`krandom-spring-boot-starter`) | Kotlin modules (`krandom-kotlin-dsl`, `krandom-kotest-extensions`) |
|:---|:---|:---|:---|
| 1.x | 21+ | 4.x | Kotlin version pinned in `gradle/libs.versions.toml` |
| 2.x | 21+ | 4.x | Kotlin version pinned in `gradle/libs.versions.toml` |
| 3.x | 21+ | 4.x | Kotlin version pinned in `gradle/libs.versions.toml` |

- **Java**: 21 or later (toolchain-enforced at build time).
- **Spring Boot**: the `krandom-spring-boot-starter` module targets Spring
  Boot 4.x; consumers must be on Spring Boot 4.x.
- **Kotlin**: `krandom-kotlin-dsl` and `krandom-kotest-extensions` are built
  with the Kotlin version pinned in `gradle/libs.versions.toml` and remain
  consumable from any Kotlin release compatible with that language level.

## Deprecation policy

- APIs are deprecated (with `@Deprecated` and a Javadoc replacement pointer)
  for at least one minor release before removal.
- Removal happens only in a major release.
- The changelog lists every deprecation and removal under its release entry.

## Where to look

- [CHANGELOG.md](CHANGELOG.md) — per-release notes (Keep a Changelog format).
- [GitHub Releases](https://github.com/frikit/krandom/releases) and
  [Maven Central](https://central.sonatype.com/artifact/io.github.frikit/krandom-core)
  — latest published version.
