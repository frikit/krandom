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
        StringGenerator.builder().length(16).alphanumeric()
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

## Selection helpers

```java
List<String> colors = List.of("red", "green", "blue");
String one = Generators.pickFrom(colors).generate();
List<String> two = Generators.pickSetFrom(colors, 2).generate();
List<String> shuffled = Generators.shuffleOf(colors).generate();
```
