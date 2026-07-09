# V2 Generation Failure Inventory

**Status:** Implementation in progress; Slices A through C, GFI-01, GFI-05, and GFI-09 complete
**Scope:** Core object generation, object faker rules, schema generation, and schema inference
**Audience:** Maintainers implementing Stage 2 of the v2 master plan

## Purpose

Generation failures currently use several incompatible behaviors: contextual exceptions, broad
fallbacks, partial values, silent omission, and deliberate capability probing. Treating every
`catch` as a bug would remove useful fallbacks; leaving the current mix in place makes genuine
failures difficult to diagnose.

This inventory classifies each behavior before the exception model changes. It is the source of
truth for Step 2.1 implementation and keeps the migration incremental.

## Classification rules

- **Probe:** Absence or invalid input is an expected lookup result, not a generation failure.
- **Policy fallback:** The operation may fall back only through an explicit construction or
  leniency policy.
- **Strict failure:** Generation must stop before returning a partial value.
- **Lenient failure:** A documented fallback may be returned and a sanitized diagnostic event must
  be emitted.
- **Boundary wrapper:** Preserve the cause and add missing context once. Do not repeatedly wrap an
  already-contextual generation failure.

Strict behavior remains the default. Generated values, exception messages from third-party code,
and personal-looking fixture content must not be copied into kRandom exception messages or
diagnostic events.

## Current behavior inventory

| ID | Location | Current behavior | Classification | V2 disposition |
|---|---|---|---|---|
| GFI-01 | `ObjectGenerator.generateWithPool` | Reports sanitized root construction context and unwraps constructor target failures to their original cause | Boundary wrapper | Complete; nested boundaries retain the construction category, operation, cause, and child depth |
| GFI-02 | `ObjectGenerator.populateClass` | Field assignment uses the central policy: strict throws contextual failure; lenient retains the initialized value and emits a sanitized diagnostic | Strict/lenient failure | Complete |
| GFI-03 | `ObjectGenerator.instantiate` | Missing no-arg constructor falls through to Objenesis | Policy fallback | Move constructor bypass behind the explicit unsafe-construction policy in Step 2.4 |
| GFI-04 | `FieldGeneratorResolver.registerSemantic` | Unsupported locale/provider falls back to structural generation | Probe | Keep the typed `UnsupportedOperationException` probe; do not broaden the catch |
| GFI-05 | `FieldGeneratorResolver.instantiateCollectionType` | Missing no-arg constructors retain the unsupported fallback; existing constructors that fail are reported at the field boundary | Policy fallback | Strict mode reports contextual construction failure; lenient mode returns `null` and preserves no partial value |
| GFI-06 | `FieldGeneratorResolver.addAllOrThrow` | Failed collection insertion uses the central policy after the queue compatibility fallback | Strict/lenient failure | Strict mode rejects the whole value; lenient mode returns `null`, never a partial collection |
| GFI-07 | `FieldGeneratorResolver` map insertion | Strict mode reports sanitized indexed context; lenient mode returns `null` for the whole map | Strict/lenient failure | Complete; no partial-map success |
| GFI-08 | `FieldGeneratorResolver.generateArray` | Strict mode reports the indexed path; lenient mode keeps the documented JVM default element and emits a sanitized diagnostic | Strict/lenient failure | Complete |
| GFI-09 | `FieldGeneratorResolver.annotationRandomizerFor` | Construction and execution failures carry the owner field path, generator type, operation, and original cause | Boundary wrapper | Preserve already-contextual failures without wrapping them again |
| GFI-10 | `FieldGeneratorResolver` nested generation | Strict mode composes root-relative paths while retaining child context; lenient mode returns `null` through the central policy | Strict/lenient failure | Complete |
| GFI-11 | `FieldGeneratorResolver` unsupported type branch | Direct unsupported fields use the central policy; strict reports context and lenient returns the type default | Strict/lenient failure | Complete; keep the erased nested `Object` bridge only until Step 2.2 replaces shallow generic resolution |
| GFI-12 | `SemanticCoherenceAdjuster` parsing helpers | Invalid URL, currency, and numeric candidates return no semantic match | Probe | Keep as normalization probes; these do not represent failed object generation |
| GFI-13 | `SemanticCoherenceAdjuster` reflective slots | Strict reads/writes report structured context; lenient reads return `null` and writes retain the previous value through the central policy | Strict/lenient failure | Complete; route policy diagnostics through the listener introduced later in Step 2.1 |
| GFI-14 | `BeanValidationSupport` hierarchy lookup | Missing methods continue interface/superclass search | Probe | Keep the narrow `NoSuchMethodException` control flow |
| GFI-15 | `ObjectFaker` nested include/rule/ignore operations | Assignment and reflection failures report root-relative target paths, declared type, depth, operation, and unwrapped cause | Boundary wrapper | Complete |
| GFI-16 | `Schema.generateAtIndex` | Provider failures report `SCHEMA_VALUE`, `GENERATE`, field path, record index, and original cause | Boundary wrapper | Complete |
| GFI-17 | `Schema.toJsonSchema` | Metadata-provider failures report `SCHEMA_METADATA`, `EXPORT_SCHEMA`, field path, no record index, and an operation-specific message | Boundary wrapper | Complete |
| GFI-18 | `SchemaParser.trySemanticResolve` | Explicit non-throwing membership checks distinguish missing references before binding | Probe | Complete; binding failures are no longer swallowed as lookup misses |
| GFI-19 | `JsonSchemaSupport` and schema record conversion | Record accessor failures report `REFLECTION`, `READ`, nested component path, owner/declared types, and the unwrapped cause | Boundary wrapper | Complete |
| GFI-20 | URL conversion helpers | Conversion failures preserve the cause | Strict failure | Keep the now-sanitized message; never include the generated URI |

Resource-loading I/O failures, unavailable hash algorithms, and numeric bound-validation failures
are outside this generation-path migration because they already fail at the correct subsystem
boundary with specific causes.

## Minimum contextual failure model

Every genuine generation failure needs these fields:

| Field | Requirement |
|---|---|
| Category | Stable machine-readable reason such as construction, unsupported type, assignment, insertion, provider, or schema metadata |
| Operation | The action that failed: construct, generate, read, assign, insert, apply rule, align semantics, or export schema |
| Path | Root-relative field/component/index path; never a generated key or value |
| Owner type | Declaring or root type where the operation occurred |
| Declared type | Full declared type signature when available |
| Depth | Object recursion depth when applicable |
| Record index | Schema batch index when applicable |
| Cause | Original throwable, preserved without copying its message into sanitized diagnostics |
| Replay identity | Optional recipe identifier added by Step 2.7; no seed or generated value is required in the message |

Example sanitized message:

```text
Could not assign field 'Order.customer.address' (declared type Address, depth 2)
```

The exception may retain the original cause. A diagnostic listener receives the structured context
and cause class, not the generated value or third-party exception message.

## Compatibility path

1. Keep existing `ObjectGenerationException` and `SchemaGenerationException` constructors so the
   1.x bridge remains source and binary compatible.
2. Introduce the context object and operation/category types additively.
3. Let existing exceptions expose the context while retaining their current inheritance and
   sanitized message shape.
4. Add context at the first boundary that knows it. Later boundaries propagate an already
   contextual failure instead of wrapping it again.
5. Introduce one internal strict/lenient policy before exposing any new public policy API.

## Incremental implementation order

### Slice A — Context without behavior change

- Add category, operation, and path value types.
- Attach context to existing object/schema exceptions.
- Test cause preservation and sanitized formatting.

### Slice B — Eliminate partial containers

- Fix GFI-06 through GFI-08.
- Test map, queue, collection, and array failures with complete indexed paths.
- Verify strict mode never returns a partially populated container.

### Slice C — Centralize leniency

- Route GFI-02, GFI-10, GFI-11, and GFI-13 through one policy.
- Preserve current opt-in `objectIgnoreErrors(true)` behavior through the compatibility bridge.
- Specify each lenient fallback explicitly.

### Slice D — Schema and diagnostics

- Migrate GFI-15 through GFI-19. **Complete.**
- Add the sanitized diagnostic listener. **Complete.**
- Add replay identity after Step 2.7 defines the recipe contract.

## Verification gate

Step 2.1 is complete only when:

- strict container, assignment, constructor, reflection, schema, and custom-generator failures
  report complete paths;
- no strict failure returns a partial fixture;
- every lenient fallback has a direct assertion and diagnostic event;
- messages and events contain no generated fixture values; and
- the full pre-commit gate remains green.
