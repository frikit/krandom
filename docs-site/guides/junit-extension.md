---
layout: page
title: JUnit Extension
permalink: /guides/junit-extension/
---

# JUnit Extension

`krandom-junit` ships a JUnit 5 extension that fixes the kRandom seed per test and reports it
when a test fails, so a failure caused by random data is always reproducible.

```kotlin
testImplementation("io.github.frikit:krandom-junit:1.4.0")
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
- prints a reproduction hint to `System.err`:

```text
krandom: test 'totalsAreNonNegative(GeneratorConfig)' failed with seed 8155926046530546882.
Annotate it with @KrandomSeed(8155926046530546882L) to reproduce this run.
```

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
| `GeneratorConfig.Builder` | A builder pre-seeded with the test's seed, for adding locale or other options before `build()` |

All injections within one test share the same seed, so two injected configs generate identical
sequences.

```java
@Test
void localizedFixture(GeneratorConfig.Builder builder) {
    GeneratorConfig config = builder.locale(Locale.GERMANY).build();
    String city = Generators.ofCity(config).generate();
}
```

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
