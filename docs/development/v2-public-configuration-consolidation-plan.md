# v2 Public Configuration Consolidation Plan

**Created:** 2026-07-13
**Status:** Complete
**Parent work package:** [Master plan Step 3.1](v2-master-implementation-plan.md#step-31--simplify-the-v2-api-and-object-configuration)

## Purpose and boundary

This is the next small, repository-controlled v2 work package. It makes `GeneratorConfig` the
single owner of object-generation settings and proves the resulting configuration is structurally
immutable. It closes the two configuration actions in Step 3.1 without mixing them with the
separate facade-naming, namespace, or release-baseline decisions.

The work is deliberately narrow:

- Preserve the current public `GeneratorConfig` object-generation API and its documented
  precedence.
- Keep `ObjectGeneratorConfig` package-private. It may remain as a thin read-only adapter if that
  is the least risky migration path, but it must no longer store a second copy of object settings.
- Do not add a public configuration type merely to share internal code.
- Do not rename/split `Generators`, remove additional APIs, alter generation defaults, or change
  recipe semantics in this package.

## Audited starting point

- `GeneratorConfig` is the documented public root configuration and already holds object depth,
  pool, construction, nullability, semantic, exclusion, override, and subtype settings.
- The former package-private `ObjectGeneratorConfig` stored many settings again, tracked explicit
  builder values, merged them with `GeneratorConfig`, and projected them back through
  `toGeneratorConfig()`. It is now a read-only root-config view; its compatibility builder records
  only local fluent operations so a later root configuration can preserve the established internal
  call order without retaining another settings object.
- `ObjectGenerator` and `ObjectFaker` create that internal configuration around a
  `GeneratorConfig`; no consumer needs to construct it directly.
- Current constructors defensively copy many collections, and the existing reflection-guarded
  round-trip test protects `GeneratorConfig.toBuilder()`. The missing contract is a complete proof
  that mutable input cannot later change a built config and a clear statement that callbacks,
  generators, and factories remain caller-owned references.

### Configuration ownership mapping

| Setting family | Existing internal adapter path | Canonical root path | v2 precedence |
|:---|:---|:---|:---|
| Depth, pool, initialization, construction, error policy | Local fields plus explicit-value flags | `GeneratorConfig.Builder.object*` | Root value only |
| Semantic mode/registry, nullability, uniqueness, dates | Local fields plus explicit-value flags | `GeneratorConfig.Builder.object*` | Root value only |
| Type, field, predicate, and contextual overrides | Local maps/lists merged with root values | `GeneratorConfig.Builder.objectOverride(...)` | Root registration order |
| Field/type exclusions and subtypes | Local lists/maps merged with root values | `GeneratorConfig.Builder.objectExclude*` / `objectSubtype(...)` | Root registration order |
| Collections and executable callbacks | Copied collections; callback references retained | Same root configuration ownership rule | Copy values; retain callbacks by reference |

## Stage 1: Characterize configuration ownership and precedence

**Goal**: Freeze the behavior that the consolidation must preserve before moving state.

**Success Criteria**:

- Every object-generation setting has one row in a short internal mapping table: current
  `ObjectGeneratorConfig` source, canonical `GeneratorConfig` getter/builder method, and
  precedence rule.
- The intended v2 rule is explicit: a public `GeneratorConfig` is the sole stored configuration;
  any internal adapter reads it and does not merge a second set of values.
- The exact ownership rule is written before code changes: values and collections are copied;
  supplied `Generator`, `ContextualGenerator`, `Predicate`, random source, listener, and factory
  instances are retained by reference and are the caller's responsibility.

**Tests**:

- Add focused red tests for each setting family: depth/pool/construction, initialization and error
  policy, semantic/nullability/uniqueness/date policy, overrides, exclusions, and subtypes.
- For each family, compare object generation through the existing `ObjectGenerator` and
  `ObjectFaker` public entry points with the current internal-adapter path.
- Add a test that a configuration built from a supplied callback preserves callback identity rather
  than copying or invoking it during configuration.

**Status**: Complete

## Stage 2: Remove the duplicate mutable state

**Goal**: Make `GeneratorConfig` the only object-setting store while retaining current behavior.

**Success Criteria**:

- `ObjectGenerator` and `ObjectFaker` receive or derive one immutable `GeneratorConfig`.
- `ObjectGeneratorConfig`, if retained, contains only derived/runtime helpers and delegates every
  setting lookup to its root config; it has no independent builder state, explicit-value flags, or
  projection method that can disagree with the root.
- All override, exclusion, subtype, construction, semantic, and recipe paths read the same root
  values.
- No new public API is introduced and all pre-existing public construction paths keep working.

**Tests**:

- Turn the Stage 1 characterization matrix green without changing expected generated values for
  portable seeds.
- Verify `GeneratorConfig.toBuilder().build()` preserves every object setting and produces the
  same `GenerationRecipe` when the configuration is portable.
- Verify field/type/contextual override precedence and exclusion behavior through public object
  generation, not by testing private storage.
- Run the focused object, recipe, Kotlin immutable-object, Spring, and schema suites because each
  consumes object configuration.

**Status**: Complete

## Stage 3: Enforce structural immutability and document ownership

**Goal**: Ensure built configurations cannot change through caller mutation while retaining
documented callback ownership.

**Success Criteria**:

- Arrays, collections, maps, and builder accumulators supplied by callers are copied before they
  become observable through a built configuration or an internal adapter.
- Observable collection views are immutable and `toBuilder()` mutations cannot modify the source
  configuration.
- Javadocs on `GeneratorConfig`, object override/exclusion methods, and the object-generation
  guide distinguish copied values from caller-owned executable objects.
- No configuration getter exposes a mutable implementation collection.

**Tests**:

- Mutate every supported caller-supplied collection/array after `build()` and prove generated
  behavior, getters, and replay output remain unchanged.
- Assert immutable returned collection views reject mutation.
- Prove two builders derived from the same config can diverge without affecting the original or
  one another.
- Prove callback identity is retained but a callback is not executed during copying, validation,
  or recipe serialization.

**Status**: Complete

## Stage 4: Verify the public contract and record the result

**Goal**: Complete the narrow package with the same evidence expected of a v2 public-contract
change.

**Success Criteria**:

- Public Javadocs and the v2 migration/configuration guides describe `GeneratorConfig` as the
  canonical owner and document callback ownership precisely.
- API evolution tooling shows no accidental public surface expansion or removal.
- The two configuration actions in master Step 3.1 are marked complete with links to the tests and
  documentation that prove them.
- The package leaves the remaining Step 3.1 actions explicitly open: canonical facade names,
  any justified namespace split, and the new v2 API baseline.

**Tests**:

- Run the focused tests from Stages 1–3, then `zsh -lic 'java21 && ./scripts/pre_commit_check.sh'`.
- Run `zsh -lic 'java21 && ./scripts/verify_examples_local.sh'` because the canonical public
  configuration path and published artifacts are part of the contract.
- Inspect `git diff --check`, the generated API-evolution report, and the final migration examples
  before proposing the final documentation commit.

**Status**: Complete

## Suggested commit boundaries

1. `test(config): characterize object configuration ownership`
2. `refactor(config): make GeneratorConfig the object settings source`
3. `test(config): enforce immutable object configuration inputs`
4. `docs(config): document configuration ownership`

Each commit must be independently green. Do not combine this work with the remaining facade
redesign or schema-contract work; those are separate reviewable packages after this boundary is
settled.
