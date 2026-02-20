# kRandom

![kt test + coverage](https://github.com/frikit/krandom/workflows/kt%20test%20+%20coverage/badge.svg)
[![codecov](https://codecov.io/github/frikit/krandom/graph/badge.svg?token=CpcHkmbzo7)](https://codecov.io/github/frikit/krandom)

kRandom is a random data generator written in Kotlin and Java. It covers a broad range of categories — primitives, user data, network addresses, game utilities, mathematical sequences, and object-graph population — primarily for use in tests and data-generation pipelines.

---

## Project structure

Multi-module Gradle project (Java 21 / Kotlin 2.1):

| Module | Purpose |
|:---|:---|
| `core` | All generators, validators, and utilities |
| `java-api` | Standalone Java API layer (in progress) |
| `kotlin-api` | Standalone Kotlin API layer (in progress) |

---

## Feature overview

### Primitive generators (Java API — `org.github.krandom.generator`)

| Type | Bounded | Seeded | Tested |
|:---:|:---:|:---:|:---:|
| `byte` | ✅ | ✅ | ✅ |
| `short` | ✅ | ✅ | ✅ |
| `int` | ✅ | ✅ | ✅ |
| `long` | ✅ | ✅ | ✅ |
| `float` | ✅ | ✅ | ✅ |
| `double` | ✅ | ✅ | ✅ |
| `char` | ✅ | ✅ | ✅ |
| `boolean` | ✅ | ✅ | ✅ |
| `String` | ✅ | ✅ | ✅ |
| `Enum<T>` | — | — | ✅ |

All primitive generators implement `Generator<T>` with `generate()`, `generateList(n)`, `stream()`, `map()`, and `filter()` default methods.

### Algorithm generators (Java API)

| Algorithm | Description | Tested |
|:---:|:---:|:---:|
| Fibonacci | Random or indexed Fibonacci numbers (indices 0–92, fits `long`) | ✅ |
| Luhn | 10-digit Luhn-valid strings (credit-card check-digit algorithm) | ✅ |

### Object generator (Java API)

`ObjectGenerator<T>` populates arbitrary POJO instances by reflecting over fields and resolving a `Generator` for each type. Configurable via `ObjectGeneratorConfig`:

- `maxDepth` — controls recursive population depth
- `ignoreErrors` — silently null out unresolvable fields instead of throwing
- Type-level and field-level generator overrides

### User-data generators (Kotlin layer — `org.github.krandom.user`)

| Field | Implemented | Tested |
|:---:|:---:|:---:|
| First name | ✅ | ✅ |
| Last name | ✅ | ✅ |
| Username | ✅ | ✅ |
| Age | ✅ | ✅ |
| Gender | ✅ | ✅ |
| Email | ✅ | ✅ |
| Title | ✅ | ✅ |
| Birthday | ✅ | ✅ |
| Social Security Number | ✅ | ✅ |

### Game utilities (Kotlin layer — `org.github.krandom.games`)

| Generator | Implemented | Tested |
|:---:|:---:|:---:|
| 4-sided dice | ✅ | ✅ |
| 6-sided dice | ✅ | ✅ |
| 8-sided dice | ✅ | ✅ |
| 10-sided dice | ✅ | ✅ |
| 12-sided dice | ✅ | ✅ |
| 20-sided dice | ✅ | ✅ |
| Coin flip | ✅ | ✅ |

### Number generators (Kotlin layer — `org.github.krandom.common.numbers`)

| Generator | Implemented | Tested |
|:---:|:---:|:---:|
| Natural numbers | ✅ | ✅ |
| Prime numbers | ✅ | ✅ |
| Composite numbers | ✅ | ✅ |

### Network generators (Kotlin layer — `org.github.krandom.network`)

| Generator | Implemented | Tested |
|:---:|:---:|:---:|
| IPv4 | ✅ | ✅ |

### String generators (Kotlin layer — `org.github.krandom.common.string`)

| Generator | Implemented | Tested |
|:---:|:---:|:---:|
| Hex hash (length 1–999) | ✅ | ✅ |

---

## Quick start

```java
// Primitive values
int roll    = Generators.ofInt(1, 7).generate();        // die [1..6]
String name = Generators.ofString().generate();
double d    = Generators.ofDouble(0.0, 1.0).generate();

// Lists
List<Long> ids = Generators.ofLong(1L, 1_000_000L).generateList(100);

// Algorithm generators
long fib    = Generators.ofFibonacci().generate();
String card = Generators.ofLuhn().generate();           // "4382916057"

// Generic lookup by type
Generator<Integer> g = Generators.forType(Integer.class);

// Object population
ObjectGenerator<Person> gen = new ObjectGenerator<>(Person.class);
Person person = gen.generate();

// Seeded, reproducible
IntGenerator seeded = Generators.ofInt(1, 100, 42L);
```

---

## Build

```bash
./gradlew build               # full build + test + coverage check
./gradlew :core:test          # tests only
./gradlew spotlessApply       # apply license headers and formatting
```

JaCoCo line and branch coverage enforced at ≥ 90%.

---

## Reference docs

- [`docs/easy-random-reference.md`](docs/easy-random-reference.md) — easy-random library deep-dive
- [`docs/chancejs-reference.md`](docs/chancejs-reference.md) — Chance.js API reference and feature comparison
- [`docs/lorem-reference.md`](docs/lorem-reference.md) — lorem (mdeanda) API reference and feature comparison
