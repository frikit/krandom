# Instancio Feature Parity Analysis

## Library Overview

- **Name**: Instancio
- **Language**: Java
- **GitHub**: https://github.com/instancio/instancio
- **License**: Apache 2.0
- **Role**: The modern leader for **object-graph generation** — the competitor
  most aligned with krandom's `ObjectFaker`/`ObjectGenerator`.

**Last updated:** 2026-06-20 · backlog in [`GAP-TRACKER.md`](./GAP-TRACKER.md)

## Executive summary

Instancio's explicit goal is to "generate fully populated objects" and it
states realism "is **not** its goal" — most unit tests "just require the
presence of a value." krandom matches Instancio on the **core** (one-call graph
generation, reproducible seeds, JUnit 5 extension, native Bean Validation,
records) **and adds the realism Instancio deliberately omits** (locale-aware
names/addresses/finance, 35 locales, checksum national IDs, schema export).

The genuine gaps are **ergonomics**, not capability:
1. Type-safe method-reference **selectors** (`field(Pojo::getX)`) — krandom uses
   string / dotted-path field names.
2. Reusable **`Model<T>`** templates and conditional **`assign()`** rules.
3. **Data feeds** (populate objects from CSV/JSON) — krandom exports schemas but
   does not yet feed external data *into* object generation.

## 1. Object generation

| Feature | Instancio | krandom | Status |
|---|---|---|---|
| One-call create | `Instancio.create(X.class)` | `Generators.ofObject(X.class).generate()` / `new ObjectFaker<>(X.class).generate()` | ✅ |
| Collections of objects | `Instancio.ofList(X).size(n)` | `Generators.ofObject(X).generateList(n)` | ✅ |
| Streams | `Instancio.stream(X).limit(n)` | `Generators.ofObject(X).stream().limit(n)` | ✅ |
| Records | ✅ | ✅ (canonical constructor) | ✅ |
| `sealed` types / subtype mapping | `subtype(all(A), B)` | `GeneratorConfig.objectSubtype(A, B)` / `objectOverride(A, B::new)` | ✅ |
| Populate existing instance | `Instancio.ofObject(obj).populate()` | `ObjectFaker.populate(instance)` | ✅ |
| Generic type tokens | `new TypeToken<Pair<A,B>>(){}` | declared-type generation; explicit type-token API | ⚠️ partial |
| Java 21 sequenced collections | ✅ | declared collection types populated | ⚠️ verify |

## 2. Customization & selectors

| Feature | Instancio | krandom | Status |
|---|---|---|---|
| Fixed value | `set(field(X::getC), "White")` | `ruleFor("color", () -> "White")` | ✅ |
| Supplier | `supply(field(..), () -> v)` | `ruleFor("f", () -> v)` | ✅ |
| Built-in generator w/ range | `generate(field(..), gen -> gen.ints().range(a,b))` | `ruleFor("f", Generators.ofInt(a,b))` | ✅ |
| Ignore field | `ignore(field(..))` | `ObjectFaker.ignore("f")` / `objectExcludeField("f")` | ✅ |
| Nullable | `withNullable(field(..))` | `objectOptionalEmptyProbability(..)` / nullable generators | ⚠️ partial |
| On-complete callback | `onComplete(all(X), c)` | `afterGenerate(c)` / `postProcess(op)` | ✅ |
| **Predicate selectors** | `all(String.class)`, predicates | `FieldPredicates.*` / `TypePredicates.*` via `objectOverride(pred, gen)` | ✅ |
| **Type-safe method-ref selectors** | `field(Pojo::getX)` | string / dotted path `"a.b.field"` | ❌ **gap** |
| Reusable templates | `Model<T>` via `toModel()` | `ObjectFaker.profile(..)` / `useProfile(..)` | ⚠️ partial |
| Conditional rules | `assign(when(..).set(..))` | — | ❌ **gap** |

## 3. JUnit 5 integration

| Feature | Instancio | krandom | Status |
|---|---|---|---|
| Extension | `InstancioExtension` | `KrandomExtension` (`krandom-junit`) | ✅ |
| Pin/repro seed | `@Seed(123)` | `@KrandomSeed(123)` | ✅ |
| Seed reported on failure | ✅ | ✅ (report entry + `System.err` hint) | ✅ |
| Parameterized source | `@InstancioSource` | seeded param injection | ⚠️ partial |
| Settings injection | `@WithSettings` | injected `GeneratorConfig`/`Builder` params | ✅ |

## 4. Bean Validation & JPA

| Feature | Instancio | krandom | Status |
|---|---|---|---|
| Bean Validation-aware (`@Size/@Min/@Max/@Email/@Pattern/@Past/@Future/@NotBlank/@Positive`…) | ✅ | ✅ **native** in object generation | ✅ |
| JPA annotation-aware (`@Column(length)`, etc.) | ✅ (Hibernate) | Bean Validation yes; JPA-specific annotations | ⚠️ partial |

## 5. Advanced

| Feature | Instancio | krandom | Status |
|---|---|---|---|
| Custom generators / SPI | ✅ | ✅ `Generator<T>`, `ProviderHub`, `@Randomizer` | ✅ |
| Reproducible data | ✅ | ✅ seeded `GeneratorConfig` | ✅ |
| **Data feeds** (CSV/JSON → objects) | `applyFeed(..)` | schema **export** only (no import-feed) | ❌ **gap** |
| Realistic localized values | ✗ (out of scope by design) | ✅ 35 locales, names/addresses/finance/national IDs | ✅ **krandom-only** |
| Schema export (CSV/JSONL/XML/SQL) | ✗ | ✅ `Schema.*` | ✅ **krandom-only** |

## krandom strengths vs Instancio

1. **Realism Instancio refuses to do** — locale-aware names/addresses/phones/IBAN, 35 locales, checksum national IDs (incl. Codice Fiscale). This is the differentiator: krandom is *Instancio + DataFaker in one*.
2. **Schema export** (CSV/JSONL/XML/SQL) for seeding DBs / bulk fixtures.
3. **Ecosystem**: Spring Boot starter, Jackson module, Kotlin DSL, kotest, plus a DataFaker expression adapter.

## Gaps to close (→ GAP-TRACKER Phase 2 ergonomics)

- [ ] Type-safe method-reference selectors (`field(X::getY)`) alongside string paths
- [ ] Reusable `Model<T>`-style templates (beyond profiles)
- [ ] Conditional `assign(when/then)` rules
- [ ] Data feeds: populate objects from CSV/JSON sources
- [ ] Verify/advertise generic type-token + Java 21 sequenced-collection parity
