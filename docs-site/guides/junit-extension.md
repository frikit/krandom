---
layout: page
title: JUnit Extension
permalink: /guides/junit-extension/
---

# JUnit Extension

`krandom-junit` ships a JUnit Jupiter extension that fixes the kRandom seed per test and reports it
when a test fails, so a failure caused by random data is always reproducible.

```kotlin
testImplementation("io.github.frikit:krandom-junit:2.5.0")
```

## Seed reporting on failure

Register the extension and inject a seeded `GeneratorConfig` into any test:

```java
import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.Generators;
import io.github.frikit.krandom.junit.KrandomExtension;

@ExtendWith(KrandomExtension.class)
class OrderServiceTest {

    @Test
    void totalsAreNonNegative(GeneratorConfig config) {
        Order order = Generators.ofObject(Order.class, config).generate();
        assertTrue(service.total(order).signum() >= 0);
    }
}
```

Every test runs with a concrete per-test seed. When a test fails, the extension:

- publishes the seed as a JUnit report entry under the `krandom.seed` key, and
- publishes a redacted portable recipe under the `krandom.recipe` key, and
- prints a reproduction hint, the source of the recorded clock instant, and copyable Gradle and
  Maven replay commands to `System.err`:

```text
krandom: test 'totalsAreNonNegative(GeneratorConfig)' failed with seed 8155926046530546882. Annotate it with @KrandomSeed(8155926046530546882L) to reproduce this run.
krandom: recipe clock 2026-09-27T08:25:06.360709Z (Europe/London) was read when the failure was reported; the test ran with a live clock, so time-relative data may not replay exactly. Set krandom.junit.snapshot-clock=true for time-sensitive tests.
Replay with Gradle: KRANDOM_JUNIT_RECIPE=base64:... ./gradlew test --tests 'com.example.OrderServiceTest' --rerun
Replay with Maven: mvn test -Dtest='com.example.OrderServiceTest' -Dkrandom.junit.recipe=base64:...
Replay recipe:
format=krandom-recipe
...
```

## Replay without editing the test source

Copy the printed command when rerunning a CI failure. Both commands carry the complete, redacted
recipe as a shell-safe URL-safe Base64 value and select the failing test's top-level class
(`@Nested` classes run through it):

```bash
# Gradle: environment variables reach the forked test JVM; --rerun runs an up-to-date test task.
KRANDOM_JUNIT_RECIPE=base64:PASTE_THE_REPORTED_VALUE ./gradlew test --tests 'com.example.OrderServiceTest' --rerun

# Maven: Surefire forwards -D user properties to the forked test JVM.
mvn test -Dtest='com.example.OrderServiceTest' -Dkrandom.junit.recipe=base64:PASTE_THE_REPORTED_VALUE
```

Do not use `./gradlew test -Dkrandom.junit.recipe=...`: Gradle's `-D` sets a property on the Gradle
JVM, not on the forked test JVM, so the value never reaches the extension. In a multi-project
Gradle build, run the owning project's task (`./gradlew :orders:test --tests ...`) so projects
without a matching test do not fail the filter; in a multi-module Maven build, add
`-pl <module>` or `-Dsurefire.failIfNoSpecifiedTests=false`.

For a seed-only replay, use a decimal signed 64-bit value:

```bash
KRANDOM_JUNIT_SEED=8155926046530546882 ./gradlew test --tests 'com.example.OrderServiceTest' --rerun
mvn test -Dtest='com.example.OrderServiceTest' -Dkrandom.junit.seed=8155926046530546882
```

### Where settings are read

`krandom.junit.recipe`, `krandom.junit.seed`, `krandom.junit.class-recipe`, and
`krandom.junit.snapshot-clock` are read as JUnit Platform configuration parameters, so any of these
sources works:

- a JVM system property of the **test** JVM (Maven `-D`, Surefire `systemPropertyVariables`, or
  Gradle `tasks.test { systemProperty("krandom.junit.seed", "42") }`),
- a launcher configuration parameter, or
- an entry in `src/test/resources/junit-platform.properties`.

When none of them is set, the matching environment variable is used: `KRANDOM_JUNIT_RECIPE`,
`KRANDOM_JUNIT_SEED`, `KRANDOM_JUNIT_CLASS_RECIPE`, or `KRANDOM_JUNIT_SNAPSHOT_CLOCK`. Configure
only one effective replay value: a recipe and a seed together, from any sources, are a configuration
error; a class recipe combines with either. Replay values override `@KrandomSeed`, so a CI runner
can reproduce a failure without changing the test source.

Recipes retain a textual seed internally when one was used, so injected `GeneratorConfig` values
preserve that metadata. Failure output and report entries deliberately omit the original text and
contain only the derived numeric seed. Literal recipe text is also accepted for local tooling when
its line breaks are represented as `\n`.

## Pinning a seed with `@KrandomSeed`

`@KrandomSeed` pins the seed for a test method or a whole test class. It also registers the
extension by itself, so no separate `@ExtendWith` is needed:

```java
import io.github.frikit.krandom.junit.KrandomSeed;

@KrandomSeed(8155926046530546882L)
@Test
void totalsAreNonNegative(GeneratorConfig config) {
    // exact replay of the failing run
}
```

Rules:

- A method-level annotation overrides a class-level one; `@Nested` classes inherit the
  enclosing class's seed.
- String seeds are supported via `@KrandomSeed(text = "checkout-flow")` and derive the numeric
  seed with the same `fnv1a64-v1` algorithm as `GeneratorConfig.builder().seed(String)`.
- Setting both `value` and `text` (or neither) is a configuration error and fails the test.

## Injectable parameters

| Parameter type | Resolves to |
|:---|:---|
| `GeneratorConfig` | A config pre-seeded with the test's seed |
| `GeneratorConfig.Builder` | A builder copied from the test's recipe/config, for adding locale or other options before `build()` |

All injections within one test share the same seed, so two injected configs generate identical
sequences.

```java
@Test
void localizedFixture(GeneratorConfig.Builder builder) {
    GeneratorConfig config = builder.locale(Locale.GERMANY).build();
    String city = Generators.ofCity(config).generate();
}
```

## Test-method and class-scoped configurations

Where a parameter is declared decides which configuration it receives:

| Declared on | Receives |
|:---|:---|
| a `@Test`/`@ParameterizedTest` method, `@BeforeEach`/`@AfterEach` | the test method's configuration |
| the test-class constructor (default `PER_METHOD` lifecycle) | the test method's configuration, so fields initialised in the constructor match the test's seed |
| `@BeforeAll`/`@AfterAll` methods, or the constructor under `@TestInstance(PER_CLASS)` | one class-scoped configuration per test class |

The class-scoped configuration uses the class-level `@KrandomSeed` (searching enclosing classes for
`@Nested` tests), a replay override, or a random seed. It is shared by that class's class-level
callbacks and never leaks into a test method: every test resolves its own seed, so a method-level
`@KrandomSeed` always applies. When a test fails, the failure output also lists the seed of every
class-scoped configuration of its enclosing classes, and the replay commands restore them through
`KRANDOM_JUNIT_CLASS_RECIPE` (Maven: `-Dkrandom.junit.class-recipe`), so data created in
`@BeforeAll` replays together with the test's own data:

```bash
KRANDOM_JUNIT_CLASS_RECIPE='com.example.OrderServiceTest=base64:...' KRANDOM_JUNIT_RECIPE=base64:... ./gradlew test --tests 'com.example.OrderServiceTest' --rerun
```

The value lists `<test class>=base64:<recipe>` entries separated by commas, one for each enclosing
class with a class-scoped configuration, keyed by canonical class name (`com.example.OuterTest.Inner`
for a `@Nested` class). A listed class's class-scoped configuration uses its entry instead of the
other replay overrides or `@KrandomSeed`; test methods and unlisted classes are unaffected. To pin
the data in source instead, annotate each class with its reported class-scoped seed and keep the
test's own `@KrandomSeed`.

## Composing with the Spring Boot starter

JUnit extensions stack, so the extension combines with the starter's `@KrandomTest` slice: keep
the auto-configured beans for application wiring and use the injected `GeneratorConfig`
parameter for per-test deterministic fixtures.

```java
@KrandomTest
@ExtendWith(KrandomExtension.class)
class UserFixtureTest {

    @Autowired
    KrandomObjectFakerFactory factory;

    @Test
    void generateUser(GeneratorConfig config) {
        // factory-driven beans + a per-test reproducible config
    }
}
```

See also: [Testing Integrations]({{ '/guides/testing-integrations/' | relative_url }}) and
[Migration from Instancio]({{ '/guides/migration-from-instancio/' | relative_url }}).

## Captured test clocks (2.3+)

By default injected configurations keep a **live** clock, and the failure recipe records the clock
instant at which the failure was reported, not the instant at which the test generated its data.
The failure output says so (`was read when the failure was reported`). Time-relative values such as
`future(7)` dates or ages can therefore differ on replay when the test ran for a while or crossed a
day boundary.

For time-sensitive tests, set `krandom.junit.snapshot-clock=true` to capture one clock per
configuration before parameter injection. The injected configuration and the failure recipe then
use the same instant and zone, and the failure output states `was captured before parameter
injection`. The default `false` retains live clocks; other values are rejected. Set it once in
`src/test/resources/junit-platform.properties`:

```properties
krandom.junit.snapshot-clock=true
```

or per build: Gradle `tasks.test { systemProperty("krandom.junit.snapshot-clock", "true") }`, Maven
Surefire `systemPropertyVariables`, or the `KRANDOM_JUNIT_SNAPSHOT_CLOCK=true` environment variable.

The extension cannot track later edits to an injected builder. If you change its clock, rules,
locale, or other settings, capture/report the resulting configuration yourself. A seed alone does
not reproduce a moving clock. A replay-recipe override already has a fixed clock, which the output
reports as `was fixed by the replay recipe`.
