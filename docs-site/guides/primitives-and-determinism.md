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

## Selection helpers

```java
List<String> colors = List.of("red", "green", "blue");
String one = Generators.pickFrom(colors).generate();
List<String> two = Generators.pickSetFrom(colors, 2).generate();
List<String> shuffled = Generators.shuffleOf(colors).generate();
```
