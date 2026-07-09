# Versioning and compatibility policy

Every published jar module is checked for public binary and source compatibility against the `apiBaselineVersion` configured in `gradle.properties`. Run `./gradlew checkApiCompatibility`; module reports are written under `build/reports/japicmp/`. The gate compares each kRandom jar directly and ignores missing third-party classes, so behavioral changes and incompatibilities caused only by external dependency types still require review. Intentional major-version breaks require a reviewed migration path and a narrow entry in `config/api-compatibility-excludes.txt`; package-wide exclusions are forbidden.

kRandom follows [Semantic Versioning 2.0.0](https://semver.org) for all
published artifacts (`io.github.frikit:krandom-*`).

## What a version number promises

- **Major** (`X.0.0`): may remove or change public API. Migration notes are
  published in the changelog and, for larger changes, as a guide under
  `docs/migration/`.
- **Minor** (`1.X.0`): additive only. Existing public API keeps compiling and
  behaving as documented. New generators, locales, and configuration options
  may be added.
- **Patch** (`1.0.X`): bug fixes only. No new API, no behavior changes beyond
  the documented fix.

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

## Platform support window

**Java 21 is the deliberate baseline for the entire 1.x line.** This is a
positioning decision, not an oversight: it will not be lowered within 1.x,
and any future change to the Java baseline would only happen in a major
release.

### Compatibility table

| kRandom | Java | Spring Boot (`krandom-spring-boot-starter`) | Kotlin modules (`krandom-kotlin-dsl`, `krandom-kotest-extensions`) |
|:---|:---|:---|:---|
| 1.x | 21+ | 4.x | Kotlin version pinned in `gradle/libs.versions.toml` |

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
