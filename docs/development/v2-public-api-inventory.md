# kRandom v2 Public API Inventory

**Baseline:** `1.5.0`
**Development line:** `1.6.0-SNAPSHOT`
**Status:** Classified for the 1.6 bridge

## Machine-readable inventory

Run:

```bash
./gradlew generatePublicApiInventory
```

The task builds every published jar module and writes complete public class, constructor, method, field, annotation, and compatibility metadata to:

```text
build/reports/api-inventory/<module>/inventory.html
build/reports/api-inventory/<module>/inventory.xml
```

`./gradlew checkApiContract` separately compares the current jars with the Maven Central `apiBaselineVersion` from `gradle.properties`. It fails on binary/source incompatibility and on added, removed, or structurally changed public elements not classified in `config/api-evolution-allowlist.txt`. Annotation-only and behavioral changes still require human review.

## Classification rule

Every public API present in 1.5.0 has the default disposition **KEEP** unless it appears in an exception table below. That conservative default prevents a large v2 cleanup from becoming an excuse for arbitrary churn.

New public APIs are not accepted implicitly. They require a use case, tests, Javadocs, a changelog entry, an update to this inventory, and a narrow evolution-allowlist entry. The compatibility gate continues to inspect allowlisted elements for breakage.

The allowed dispositions are:

| Disposition | Meaning |
|:---|:---|
| **KEEP** | Stable v2 API; changes remain subject to compatibility policy |
| **DEPRECATE 1.6** | Keep as a thin delegate in 1.6 and remove in v2 |
| **REPLACE FIRST** | Do not deprecate until the named replacement exists |
| **INTERNALIZE v2** | Move out of the public surface only after migration evidence and compatibility review |
| **DECISION REQUIRED** | Resolve the named contract question before implementation |

## Published artifacts

| Artifact | Module/API identity | Disposition |
|:---|:---|:---|
| `krandom-bom` | Maven/Gradle platform; no Java classes | **KEEP** |
| `krandom-core` | `io.github.frikit.krandom` | **KEEP**, with exceptions below |
| `krandom-jackson` | `io.github.frikit.krandom.jackson` | **KEEP** |
| `krandom-junit` | `io.github.frikit.krandom.junit` | **KEEP** |
| `krandom-spring-boot-starter` | `io.github.frikit.krandom.spring.boot.starter` | **KEEP** |
| `krandom-kotest-extensions` | `io.github.frikit.krandom.kotest` | **KEEP**, behavior hardening required |
| `krandom-kotlin-dsl` | `io.github.frikit.krandom.kotlin.dsl` | **KEEP**, typed additions required |

The automatic module names are part of the compatibility contract. The BOM deliberately has no Java module identity.

## `Generators` facade exceptions

The facade currently has hundreds of public static declarations. Domain namespaces and generators remain the long-term discoverable API; v2 should not add another alias for an operation that already has one.

| Current API | Disposition | Canonical/replacement API | Reason |
|:---|:---|:---|:---|
| `constant(value)` | **DEPRECATE 1.6** | `ofConstant(value)` | Align with the established `ofX` factory family |
| `pickFrom(list)` | **DEPRECATE 1.6** | `pick(list)` | One concise selection verb is sufficient |
| `pickSetFrom(list, count)` | **DEPRECATE 1.6** | `pickSet(list, count)` | Align pick and pick-set factory naming |
| `pickset(list, count)` | **DEPRECATE 1.6** | `pickSet(list, count)` | Existing spelling violates Java casing |
| `shuffleOf(list)` | **DEPRECATE 1.6** | `shuffle(list)` | Redundant alias |
| `uniqueValues(generator)` | **DEPRECATE 1.6** | `unique(generator)` | Redundant alias |
| `ofUrl()` / `ofURL()` | **DECISION REQUIRED** | One acronym convention | Current overload sets differ |
| `ofUri()` / `ofURI()` | **DECISION REQUIRED** | One acronym convention | Current overload sets differ |
| `ofUuid()` | **DECISION REQUIRED** | Keep or add `ofUUID()` bridge | Must use the same acronym rule as URL/URI |

All other facade methods default to **KEEP** for the 1.6 bridge. Stage 3 may reduce the v2 facade only through a reviewed inventory update and an available 1.6 migration path.

## Registry exceptions

Public provider interfaces and lookup operations remain **KEEP**. Static global mutation is different: it leaks state between tests and cannot provide complete registry isolation.

| API family | Disposition | Required replacement |
|:---|:---|:---|
| Static `register(...)` methods on `*DataRegistry` and `NationalIdRegistry` | **REPLACE FIRST** | Context-scoped registration covering every provider family |
| Static registry reset/clear hooks, where public | **REPLACE FIRST** | Disposable scoped contexts |
| `registeredKeys()` | **KEEP**, fix semantics | Return an immutable snapshot, not a live view |
| Provider interfaces and locale keys | **KEEP** | Typed provider catalog becomes their single source of truth |

Global mutations must not be deprecated until the scoped replacement reaches feature parity. Once it does, the 1.6 bridge can deprecate the global entry points as thin legacy adapters.

## Object-generation exceptions

| API | Disposition | Required work |
|:---|:---|:---|
| `ObjectGenerator`, `ObjectFaker`, annotations, and public predicates | **KEEP** | Harden construction, constraints, type handling, and errors |
| Objenesis-backed constructor bypass behavior | **DECISION REQUIRED** | Safe/default construction policy plus explicit unsafe opt-in |
| `GeneratorConfig` object-generation methods | **KEEP** | Remove duplicated internal configuration state without changing the public path |
| `ObjectGeneratorConfig` | Internal implementation, not public API | Remove its references from public Javadocs before refactoring |

Public Javadocs currently leak the package-private `ObjectGeneratorConfig` type through `Fake`, `FakeRange`, `Exclude`, `FieldPredicates`, and `TypePredicates`. Those links must be rewritten to the public `GeneratorConfig`/`ObjectFaker` path.

## Integration exceptions

| API | Disposition | Contract work |
|:---|:---|:---|
| `Generator<T>.toArb()` and Kotest helpers | **KEEP** | Honor Kotest `RandomSource`; document/provide shrinking and edge cases |
| Kotlin string-based field rules | **KEEP as bridge** | Add typed `KProperty1` rules before deciding v2 removal |
| `@KrandomTest` | **KEEP** | Make it a standalone composed Spring test slice |
| `KrandomExtension` and `@KrandomSeed` | **KEEP** | Add portable recipe replay without source edits |
| Jackson helpers/module | **KEEP** | No current contract exception |

## APIs intentionally retained

- Generator classes remain constructible for consumers who prefer direct types over the facade.
- Domain namespaces remain the preferred discoverability layer.
- Migration adapters such as DataFaker expression support remain public until real usage data justifies deprecation.
- Provider interfaces remain public extension points.
- Schema export APIs remain public while their supported import/metadata subset is documented more precisely.

## Review checklist for a new public API

- [ ] A consumer use case cannot be served by composition or an existing namespace.
- [ ] The name and overloads follow one existing convention.
- [ ] Behavior, edge cases, thread use, and replay are tested.
- [ ] Javadocs reference only public supported types.
- [ ] The symbol is added to this inventory with a disposition.
- [ ] The compatibility/evolution reports are reviewed.
- [ ] The changelog and consumer examples are updated when relevant.

Until all boxes are satisfied, keep the implementation package-private.
