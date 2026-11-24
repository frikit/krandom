# Versioning policy

kRandom publishes its `io.github.frikit:krandom-*` artifacts with `MAJOR.MINOR.PATCH` version
numbers and, from 3.0.0, follows [Semantic Versioning](https://semver.org/spec/v2.0.0.html):

- A **major** release, such as 4.0.0, is the only kind that removes public API, changes it
  incompatibly, or raises the minimum Java version, the Spring Boot major version, or the Kotlin
  language and API version that a module needs.
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
- the recipe format, so a release replays a recipe that an earlier release of its major line wrote:
  `krandom-recipe/v1` in 3.x, so a 3.x release replays a recipe that an earlier 3.x release wrote, and
  `krandom-recipe/v2` from 4.0.0, so a 4.x release replays a recipe that an earlier 4.x release wrote.
  From 4.0.0 a `krandom-recipe/v1` recipe is rejected rather than replayed with other values: the error
  names the `library-version` the recipe records, the 3.x release to replay it with;
- the `fnv1a64-v1` string-seed derivation.

Packages that `krandom-core` does not export are internal and can change in any release.

An incompatible change includes one that keeps every JVM signature but stops Kotlin source from
compiling. Widening the bound of a class's type parameter, such as from `Object` to `@Nullable Object`,
is one: Kotlin reads a star projection of the class, such as `ContextualGenerator<*>`, through that
bound, so a value read from it becomes `Any?`, and code that reads it as `Any` no longer compiles.
Compiled code is unaffected, as it calls the same methods and gets the same values, but only a major
release makes such a change. 3.1.0 widened the bounds of `ConstantGenerator`, `ContextualGenerator`,
`RepeatGenerator`, `UniqueGenerator` and `PropertySelector` as a recorded exception to this rule, as
the [changelog](CHANGELOG.md) states under 3.1.0, Changed.

Public API is deprecated in a minor release before a major release removes it: the deprecation names
its replacement, and the [changelog](CHANGELOG.md) lists it. 4.0.0 ended `krandom-recipe/v1` replay
without a 3.x deprecation, as a recorded exception to this rule that the changelog states under 4.0.0,
Changed. Only the latest release is maintained,
and fixes are not backported to earlier ones. Releases before 3.0.0 made no compatibility promise, so
read the changelog when upgrading from 2.x.

The latest stable release is `4.1.0`.
The repository development line is `4.1.0-SNAPSHOT`.

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
  From 3.2, `GeneratorConfig.Builder.strictMathGaussian(true)` draws with `StrictMath` instead, which gives the
  same bits everywhere. From 4.0.0 it is on by default, and `strictMathGaussian(false)` keeps the `Math`
  draws.
- A generator whose random source is a `SecureRandom`, from `GeneratorConfig.Builder.secureRandom()` or one
  that you supply, has no sequence to replay: `Seedable.reseed(long)` adds the seed to the
  `SecureRandom`'s state, as `SecureRandom.setSeed` does, rather than restarting it.

## Platform requirements

| Module | Requirement |
|:---|:---|
| All modules | Java 17 or later (from 3.1.0; 3.0.x needs Java 21) |
| `krandom-spring-boot-starter` | Spring Boot 4.x |
| `krandom-kotlin-dsl`, `krandom-kotest-extensions` | built with the Kotlin version pinned in `gradle/libs.versions.toml`; from 3.1.0 for JVM target 17 and for Kotlin 2.2 (language and API version 2.2, `kotlin-stdlib` 2.2.21), so Kotlin 2.1.0 or later can read them |
| `krandom-kotest-extensions` | the Kotest version pinned in `gradle/libs.versions.toml` |

From 3.1.0 every module runs on Java 17, and every later 3.x release keeps them there: the build compiles the
Java modules with `--release 17` and the Kotlin modules for JVM target 17, both against the Java 17 API, their
Gradle module metadata declares Java 17 (`org.gradle.jvm.version`), and CI runs the Java modules' tests and
the Android example's unit tests on Java 17. Building kRandom itself needs JDK 21 or later.

Likewise, a minor or patch release keeps the Kotlin language and API version that
`krandom-kotlin-dsl` and `krandom-kotest-extensions` are compiled for, 2.2 since 3.1.0: raising it
raises the Kotlin version that a consumer needs, so only a major release does.

## Where to look

- [CHANGELOG.md](CHANGELOG.md) — per-release notes (Keep a Changelog format).
- [GitHub Releases](https://github.com/frikit/krandom/releases) and
  [Maven Central](https://central.sonatype.com/artifact/io.github.frikit/krandom-core)
  — latest published version.
