# kRandom Project Review (Round 2)

Reviewed on 2026-03-28.

## Scope

- Full repository walk-through with focus on `core` architecture, locale/data registries, object generation, and build pipeline.
- Validation run: `./scripts/pre_commit_check.sh` passed fully (formatting, compile, tests, javadoc, coverage).
- Current quality gate result: 100.0% line coverage and 100.0% branch coverage.

## Findings (Prioritized)

### 1. High: `ObjectGenerator` can fail for custom concrete collection subtypes

- In `FieldGeneratorResolver`, unknown concrete `List` subtypes are materialized as `ArrayList` fallback (`toListType`):
  `core/src/main/java/org/github/krandom/generator/object/FieldGeneratorResolver.java:287-289`
- Unknown `Queue`/`Map` concrete subtypes are also fallback-converted (`toQueueType`, `toMapType`):
  `core/src/main/java/org/github/krandom/generator/object/FieldGeneratorResolver.java:294-312`
- Assignment happens reflectively and requires assignable runtime type:
  `core/src/main/java/org/github/krandom/generator/object/ObjectGenerator.java:201-207`
- Impact: a field declared as a custom concrete subtype (not one of the handled built-ins) can throw `ObjectGenerationException` even though the field type is valid.
- Improvement: instantiate declared concrete collection types when possible (no-arg constructor path), then populate; use fallback only for interfaces/abstract types.

### 2. Medium: Registry input validation is inconsistent across user data registries

- `FirstNameDataRegistry`, `LastNameDataRegistry`, `GenderDataRegistry`, `TitleDataRegistry`, `SuffixDataRegistry` validate only provider/locale non-null in `register(...)`:
  `core/src/main/java/org/github/krandom/generator/user/FirstNameDataRegistry.java:54-65`
  `core/src/main/java/org/github/krandom/generator/user/LastNameDataRegistry.java:54-65`
  `core/src/main/java/org/github/krandom/generator/user/GenderDataRegistry.java:54-65`
  `core/src/main/java/org/github/krandom/generator/user/TitleDataRegistry.java:65-78`
  `core/src/main/java/org/github/krandom/generator/user/SuffixDataRegistry.java:53-64`
- `ProfessionDataRegistry` and `StreetAddressDataRegistry` perform strict content validation (non-empty, non-blank, valid weights):
  `core/src/main/java/org/github/krandom/generator/user/ProfessionDataRegistry.java:132-155`
  `core/src/main/java/org/github/krandom/generator/location/StreetAddressDataRegistry.java:44-47,81-92`
- Downstream behavior is inconsistent when bad provider data is registered:
  - `FirstNameGenerator`/`LastNameGenerator` can fail on `nextInt(0)` with empty arrays:
    `core/src/main/java/org/github/krandom/generator/user/FirstNameGenerator.java:88-99`
    `core/src/main/java/org/github/krandom/generator/user/LastNameGenerator.java:73-75`
  - `TitleGenerator`/`SuffixGenerator` silently return empty string when array is empty:
    `core/src/main/java/org/github/krandom/generator/user/TitleGenerator.java:56-59`
    `core/src/main/java/org/github/krandom/generator/user/SuffixGenerator.java:74-77`
- Improvement: enforce one registry contract (validate at registration), and keep runtime generator behavior consistent.

### 3. Medium: Locale fallback policy is inconsistent across location generators

- `CountryDataRegistry` supports exact + language fallback (`en_CA` can resolve via `en`):
  `core/src/main/java/org/github/krandom/generator/location/CountryDataRegistry.java:84-106`
- `CityDataRegistry` and `StateDataRegistry` are exact-key only:
  `core/src/main/java/org/github/krandom/generator/location/CityDataRegistry.java:60-65,89-91`
  `core/src/main/java/org/github/krandom/generator/location/StateDataRegistry.java:60-65,89-91`
- `StreetAddressDataRegistry` is also exact-key only:
  `core/src/main/java/org/github/krandom/generator/location/StreetAddressDataRegistry.java:53-58,77-79`
- Impact: same locale can be accepted by one location generator and rejected by another, which is surprising at API level.
- Improvement: choose one policy (exact-only or exact+language fallback) for all location registries and enforce with shared tests.

### 4. Medium: Global mutable registries create cross-test and cross-context coupling

- Registries use static mutable maps and global `register(...)` mutation:
  - `FirstNameDataRegistry`: `REGISTRY` static map
    `core/src/main/java/org/github/krandom/generator/user/FirstNameDataRegistry.java:33-40`
  - `CityDataRegistry`: static `providers` map
    `core/src/main/java/org/github/krandom/generator/location/CityDataRegistry.java:29-35`
- No reset/snapshot API exists for isolating temporary overrides.
- Impact: custom provider registration affects entire JVM process; this makes test isolation and multi-tenant embedding harder.
- Improvement: add an optional scoped registry context (`GeneratorConfig`/`ProviderHub` bound), while preserving current global default for backward compatibility.

### 5. Low: Build reproducibility risk from unconditional `mavenLocal()`

- Global repository order prefers local artifacts before central:
  `build.gradle.kts:14-17`
- Impact: local cached artifacts can shadow central dependencies and hide integration issues.
- Improvement: gate `mavenLocal()` behind explicit opt-in property (for local dev only).

## Strengths

- Excellent quality gates: pre-commit script enforces formatting, compile, tests, javadocs, and coverage (`scripts/pre_commit_check.sh`).
- Coverage discipline is very strong (currently 100%/100%).
- Locale architecture improved with a clear shared source of truth in `SupportedLocale`:
  `core/src/main/java/org/github/krandom/generator/locale/SupportedLocale.java`
- Locale completeness is actively enforced by coverage tests:
  `core/src/test/java/org/github/krandom/generator/locale/SupportedLocaleCoverageTest.java`
- Modular project structure is clean (`core`, Java/Kotlin/Scala API modules, examples, docs).

## Recommended Improvement Plan

1. Stage 1: Fix collection subtype assignment in `ObjectGenerator` and add regression tests for custom concrete list/queue/map subclasses.
2. Stage 2: Standardize registry validation rules; reject invalid provider payloads at registration time across all user registries.
3. Stage 3: Unify location fallback behavior (exact-only or exact+language) and codify it with shared parameterized tests.
4. Stage 4: Introduce optional scoped registries/context to reduce global mutable-state coupling.
5. Stage 5: Make `mavenLocal()` opt-in to improve build reproducibility.
