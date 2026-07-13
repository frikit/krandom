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
