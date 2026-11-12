# Versioning policy

kRandom publishes its `io.github.frikit:krandom-*` artifacts with `MAJOR.MINOR.PATCH` version
numbers and, from 3.0.0, follows [Semantic Versioning](https://semver.org/spec/v2.0.0.html):

- A **major** release, such as 4.0.0, is the only kind that removes public API, changes it
  incompatibly, or raises the minimum Java version or the Spring Boot major version a module needs.
- A **minor** release, such as 3.1.0, adds features and keeps existing public API and behavior
  compatible.
- A **patch** release, such as 3.0.1, only fixes bugs.

The public API is:

- the public types and members in the packages that `krandom-core` exports from its
  `module-info.java`;
- the public types and members in `io.github.frikit.krandom.jackson`,
  `io.github.frikit.krandom.junit`, and `io.github.frikit.krandom.spring`, the packages of
  `krandom-jackson`, `krandom-junit`, and `krandom-spring-boot-starter`;
- the public declarations of `krandom-kotlin-dsl` and `krandom-kotest-extensions`;
- the `krandom.*` configuration properties of `krandom-spring-boot-starter`;
- the `krandom-recipe/v1` recipe format, so a 3.x release replays a recipe that an earlier 3.x
  release wrote, and the `fnv1a64-v1` string-seed derivation.

Packages that `krandom-core` does not export are internal and can change in any release.

Public API is deprecated in a minor release before a major release removes it: the deprecation names
its replacement, and the [changelog](CHANGELOG.md) lists it. Only the latest release is maintained,
and fixes are not backported to earlier ones. Releases before 3.0.0 made no compatibility promise, so
read the changelog when upgrading from 2.x.

The latest stable release is `3.0.1`.
The repository development line is `3.0.2-SNAPSHOT`.

## Determinism

Within a release, the same seed and configuration always produce the same output (see the
`fnv1a64-v1` string-seed derivation contract). Within a major release line, minor and patch releases
keep that output the same, with one exception: a release may change output that breaks a documented
contract, such as a value that fails its own Bean Validation constraints or a string that does not
match the format its schema names. The changelog lists every such change under Fixed, with the values
it affects. A new generator, option, or setting leaves existing output unchanged unless you opt in.
Tests that assert exact generated values should still pin both the library version and the seed.

A seed fixes kRandom's own draws, not the Java runtime under them:

- Values that read the runtime's time-zone or locale data follow its TZDB and CLDR versions, such as the
  zone IDs that `ZoneIdGenerator` draws from, the offsets of zoned values, and the amounts that
  `MoneyGenerator` formats. A `GenerationRecipe` records neither version, so another JDK can change these
  values for the same recipe.
- `NormalDistributionGenerator` draws with `Math.log`, `Math.sin`, and `Math.cos`, which Java lets differ in
  the last bit between platforms, so a Gaussian value can differ in its last bit on another JVM or CPU.
- A generator whose random source is a `SecureRandom`, from `GeneratorConfig.Builder.secureRandom()` or one
  that you supply, has no sequence to replay: `Seedable.reseed(long)` adds the seed to the
  `SecureRandom`'s state, as `SecureRandom.setSeed` does, rather than restarting it.

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
