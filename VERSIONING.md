# Versioning policy

kRandom publishes its `io.github.frikit:krandom-*` artifacts with `MAJOR.MINOR.PATCH` version
numbers. Only the current release is maintained. A release makes no API, behavior, or
generated-output compatibility promise relative to earlier releases: read the
[changelog](CHANGELOG.md) before upgrading and adapt to changed or removed API when you adopt a new
version.

The latest stable release is `2.5.0`.
The repository development line is `2.6.0-SNAPSHOT`.

## Determinism

Within a given release, the same seed and configuration always produce the same output (see the
`fnv1a64-v1` string-seed derivation contract). Generated values can change between releases, so
tests that assert exact generated values must pin both the library version and the seed.

## Platform requirements

| Module | Requirement |
|:---|:---|
| All modules | Java 21 or later (toolchain-enforced at build time) |
| `krandom-spring-boot-starter` | Spring Boot 4.x |
| `krandom-kotlin-dsl`, `krandom-kotest-extensions` | built with the Kotlin version pinned in `gradle/libs.versions.toml` |
| `krandom-kotest-extensions` | the Kotest version pinned in `gradle/libs.versions.toml` |

## Where to look

- [CHANGELOG.md](CHANGELOG.md) — per-release notes (Keep a Changelog format).
- [GitHub Releases](https://github.com/frikit/krandom/releases) and
  [Maven Central](https://central.sonatype.com/artifact/io.github.frikit/krandom-core)
  — latest published version.
