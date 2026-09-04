---
layout: page
title: Primitives and Determinism
permalink: /guides/primitives-and-determinism/
---

# Primitives and Determinism

## Primitive generators

```java
int n = Generators.ofInt(10, 99).generate();
double p = Generators.ofDouble(0.0, 1.0).generate();
String token = Generators.ofString(
        StringGenerator.builder()
                .length(16)
                .charGenerator(CharGenerator.alphanumeric())
).generate();
```

## Reproducible sequences

Use seeded constructors or `GeneratorConfig`:

```java
IntGenerator a = Generators.ofInt(1, 100, 123L);
IntGenerator b = Generators.ofInt(1, 100, 123L);

assert a.generate() == b.generate();
assert a.generate() == b.generate();
```

Generator instances hold mutable random state. Treat them as per-test or per-thread objects, not shared
singletons. Share `GeneratorConfig` when you want common defaults, then create fresh generators from it
inside each test, fixture factory, or worker thread.

## Composition

`Generator.map(...)` transforms values lazily, and `Generator.filter(...)` retries until a generated value matches.
Filters are bounded by default, so an impossible predicate fails instead of hanging a test run. Raise or lower the cap
with `filter(predicate, maxAttempts)` when the predicate is deliberately rare:

```java
Generator<String> evenLabel = Generators.ofInt(0, 100)
        .filter(n -> n % 2 == 0)
        .map(n -> "even-" + n);

Generator<Integer> rare = Generators.ofInt(0, 1_000_000)
        .filter(n -> n == 42, 100_000);
```

## Selection helpers

```java
List<String> colors = List.of("red", "green", "blue");
String one = Generators.pick(colors).generate();
List<String> two = Generators.pickSet(colors, 2).generate();
List<String> shuffled = Generators.shuffle(colors).generate();
```

## Explicit replay snapshots (2.3+)

A seed does not freeze time. For temporal fixtures, capture the clock before generation and use
that same configuration for the recipe printed on failure:

```java
GeneratorConfig session = GeneratorConfig.builder().seed(42L).build().snapshotClock();
DateGenerator dates = new DateGenerator(session);
LocalDate fixture = dates.future(7);
String recipe = session.getGenerationRecipe().orElseThrow().serializeForDiagnostics();
```

`snapshotClock()` captures the configured instant and zone. The original configuration keeps its
live clock. Reuse the snapshot across the generation session; taking a new snapshot after failure
cannot recover an earlier clock. Custom callbacks and registries still do not yield a portable
recipe. Changes made to an injected builder must be included in the actual configuration you use
for generation and diagnostics.

## Independent object-field streams (2.3+)

```java
GeneratorConfig config = GeneratorConfig.builder()
    .seed(42L)
    .objectFieldStreamPolicy(ObjectFieldStreamPolicy.INDEPENDENT)
    .build()
    .snapshotClock();
```

`ObjectFieldStreamPolicy` is in `io.github.frikit.krandom.generator.object`. INDEPENDENT retains
named streams for seed-owned object members when custom rules, exclusions, or modules are
installed. An override of one field then leaves unrelated structural fields on their existing
streams. A numeric or textual seed is required. Dependent rules and semantic relationships may
intentionally change related values; arbitrary callbacks remain responsible for their own state.

The default LEGACY policy preserves 2.2 output and the existing portability boundary. The random
algorithm and child-seed derivation are unchanged. Portable recipes with the explicit policy include
`object.field-stream-policy`; absent settings retain legacy interpretation. Older readers reject
that new setting rather than silently changing replay. Customized configurations still return no
portable recipe. Roll back by selecting LEGACY or removing the option and regenerating the recipe.

Independent streams can cost more for customized fixtures because each member receives its own
resolver stream. In the 2.3.0 qualification's three-member structural record, customized throughput
was about 32k objects/sec with INDEPENDENT versus 101k with LEGACY, with 3.4 times the allocations.
Existing default throughput stayed within 1% of 2.2.0. These measurements are workload-specific;
choose stability when it matters and benchmark your own fixture before bulk-generation adoption.
