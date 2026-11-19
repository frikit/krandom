# Determinism and compatibility

kRandom makes two promises that most changes can break without a single test failing in the place
you edited: **seeded output stays the same**, and **public API stays compatible**. This guide says
what each promise covers, how changes break it by accident, and what to do when a change is meant to
alter it. The user-facing policy is [VERSIONING.md](../../VERSIONING.md); this guide is how to keep it.

## What is promised

- **Seeded output.** Within a release, the same seed and configuration always give the same values.
  Within a major line, minor and patch releases keep those values, except to fix output that breaks a
  documented contract (a value that fails its own Bean Validation constraint, a string that does not
  match its declared format). Such a fix is listed under `Fixed` in the changelog with the values it
  affects. A new generator, option or setting leaves existing output unchanged unless the user opts in.
- **Recipes.** A release replays a `krandom-recipe/v2` recipe that an earlier release of its major line
  wrote. `GenerationRecipe` is the format; `GeneratorConfig.getGenerationRecipe()` writes it.
- **String seeds.** `fnv1a64-v1`, the derivation of a seed from text, never changes.
- **Public API.** The public types and members of the packages `krandom-core` exports, the packages of
  the Java integration modules, the public declarations of the Kotlin modules, and the `krandom.*`
  Spring properties. A minor or patch release only adds; removals and incompatible changes wait for a
  major release, after a deprecation in a minor release that names the replacement.

Anything in `io.github.frikit.krandom.internal`, or otherwise not exported, can change freely.

## How seeded output changes by accident

A seeded generator is a sequence of draws from a `java.util.Random`. Anything that changes the number,
order or meaning of those draws changes every value after it. The usual causes:

- **One more or one fewer draw.** A new branch that draws, a validation retry, a `filter` that now
  rejects a value, an early return that skips a draw.
- **Draws in a different order,** such as generating a field before another one it used to follow.
- **Changed data.** Adding, removing or reordering entries in an existing resource file under
  `core/src/main/resources/krandom/` changes which entry an index picks, for that locale and every
  locale that falls back to it.
- **Changed stream names.** A child stream's seed comes from its name
  (`GeneratorConfig.forChildStream`), so renaming a stream, or moving a draw from the parent stream to
  a child stream, changes values.
- **Unordered iteration.** Iterating a `HashMap` or `HashSet` while drawing makes the order depend on
  hashing. Use a `LinkedHashMap`, a sorted collection or a list.
- **Changed defaults.** A new default for any `GeneratorConfig.Builder` setting changes output for
  everyone who did not set it.
- **The hash.** FNV-1a is implemented in `GeneratorConfig`, `GenerationRecipe` and
  `internal/StreamSeeds.java`. A change to any one of them changes seeds everywhere.

Some values depend on the Java runtime rather than on kRandom: time-zone and locale data (TZDB, CLDR),
and the last bit of `Math` functions when `strictMathGaussian` is off. `VERSIONING.md` documents
these; do not try to pin them in tests.

## How to check that you changed nothing

1. **Before you edit,** find the tests that pin exact values for the code you are touching. Search
   for the generator's name together with a fixed seed (`seed(`, `seeded(`) in `core/src/test/`.
   Typical pin tests are `StreetAddressPinsTest`, `GenerationRecipeGoldenStreamTest`,
   `DefaultFlipPinsTest` and `SeedingDefaultsPinTest`.
2. **If nothing pins it,** add a pin test first, on the unchanged code: generate a handful of values
   for two or three seeds (and two locales for locale data) and assert them exactly. See it pass on the
   unchanged code before you make the change; it then shows that the values held. For values that also
   depend on the Java runtime (formatted money, zoned times), build the expected string with the same
   runtime API instead of hard-coding it, as `MoneyGeneratorTest` does with its `shown(...)` helper.
3. **Run the oracles.** `ReseedAsBuiltTest` checks that a reseeded generator gives what a freshly
   built one gives; `SchemaFreshBuildOracleTest` checks the same for `Schema` columns. Both pick up new
   generators that register a reseed hook.
4. A pinned value that changes is a finding, not a test to update. Never edit an expected value to
   make a test pass unless the change is approved, as below.

## How to make an intended change

- **A contract fix** (the output was invalid): fix it, update the pins with the new values, and list
  it under `### Fixed` in the changelog with the generator, the affected values and an example.
- **An option of one generator** that the caller turns on explicitly, and that leaves the draws of
  everyone else alone: add a method that returns a new, configured generator, as
  `DoubleGenerator.withPrecision(int)` and `CnpjGenerator.withAlphanumericFormat()` do. Existing callers
  never call it, so their output cannot change, and no configuration or recipe change is needed. Pin
  that the default output is unchanged and test the new one.
- **A new behaviour across generators** that would change existing output: add it behind a new
  `GeneratorConfig.Builder` setting whose default keeps today's output. Then:
  - Record the setting in the recipe so replay keeps it, as an **optional** key whose absence means
    the old behaviour, the way `GenerationRecipe` reads `object.ignore-errors` with `applyBoolean`.
    `GenerationRecipe.toGeneratorConfig()` rejects a recipe that misses a required setting, so a new
    required key would stop every older recipe of the major line from replaying.
  - Pin both the off and the on values, as `SeedingDefaultsPinTest` does.
  - Expose it where users configure kRandom: the Kotlin DSL's `config { }` and a `krandom.*` Spring
    property, if the other settings of its kind are there.
  - Turning it on by default is a change of output, so only a major release does it. The four seeding
    settings that were opt-ins in 3.2 and became defaults in 4.0 show that path; 4.0's new recipe
    version also made them required keys, which only a new recipe version may do.
- **Data changes** to an existing dataset change output for that locale, so by the policy above they
  are a change of output. Ask before making one, unless it only fixes invalid entries.

## Public API

`./gradlew checkApiCompatibility` (part of `check` and of the gate) compares the current build with the
release named by `apiBaselineVersion` in `gradle.properties`:

- the Java modules' jars, with japicmp, against the baseline jars from Maven Central;
- the Kotlin modules' ABI dumps against the committed `<module>/api/<module>-<baseline>.api` files.

Additions pass; a removed or incompatibly changed declaration fails, one line per declaration. The
report is under `build/reports/api-compatibility/<module>/`. The check does not see everything:

- **Nullness and type arguments** are invisible to it. Removing `@Nullable` from a parameter, or
  narrowing a generic bound, can break callers without failing the check. Read the report's list of
  new and modified members and judge them yourself.
- **Kotlin source compatibility.** A change that keeps every JVM signature can still stop Kotlin
  source from compiling, for example widening a type parameter's bound to a nullable type, which
  changes what a star projection reads as. `VERSIONING.md` treats that as a breaking change.
- **Behaviour.** A method that keeps its signature but changes what it returns or throws is a change
  for users even though the check passes.

### Removing or changing API

1. In a minor release, deprecate it with `@Deprecated` and Javadoc that names the replacement, and list
   the deprecation in the changelog.
2. Remove it only in a major release. While `developmentVersion` is a major `N.0.0-SNAPSHOT`, every
   break that the release plan names goes into `<module>/api/accepted-breaks-N.0.txt`, exactly as the
   check prints it, in the same pull request. An unlisted break fails, and so does a listed break that
   no longer occurs. The [release runbook](../release-runbook.md#accepted-breaks) describes the files and
   how they are deleted after the release.
3. List the removal under `### Removed` in the changelog, and update the migration guide for that major
   release under `docs/migration/`.

## Changelog

Every change a user can notice gets an entry under `## [Unreleased]` in `CHANGELOG.md`, in the section
that fits (`Added`, `Changed`, `Deprecated`, `Removed`, `Fixed`, `Security`). Write it for a user, not
for a reviewer: what they can now do or what changed for them, with the API names and, for output
changes, the values. Read a few existing entries first and match their style. Changes with no effect
on users (tests, build internals) need no entry, except dependency updates that reach users through the
published POMs.
