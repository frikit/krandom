# V2 Recursive Type Model

**Status:** Implementation in progress; Slices A through D complete, Slice E pending
**Scope:** Object fields, Java records, constructor parameters, containers, schema metadata, and Kotlin hand-off

## Purpose

Stage 2.2 replaces type erasure inside nested fixture generation with one recursive representation.
The objective is narrow: generate values that match their complete declared type or fail with the
complete path and signature. The model must not guess `Object` when reflection supplied more
specific information.

## Baseline resolution paths

| Entry point | Type information available | Baseline loss |
|---|---|---|
| Mutable Java field | `Field.getGenericType()` and `Field.getType()` | `FieldGeneratorResolver.typeArg` retains only plain `Class<?>` arguments |
| Java record component | `RecordComponent.getGenericType()` and `getType()` | Same `typeArg` erasure as mutable fields |
| Nested optional/collection/map | Parent `ParameterizedType` | Parameterized, wildcard, and type-variable arguments become `Object.class` |
| Java array | Raw array `Class<?>` | Ordinary arrays work; generic arrays and bound type variables have no model |
| Nested object | Raw class passed to a child `ObjectGenerator` | Declaring-type bindings are not carried into inherited generic fields |
| Schema record metadata | `RecordComponent` exposes generic and raw types | `JsonSchemaSupport.record` calls `component.getType()`, so nested generic metadata is erased |
| ObjectFaker nested path | Reflective field/record raw class | Generic path segments are not retained |
| Kotlin integration | Kotlin reflection can expose nullability and constructor types | No shared Java-side recursive representation exists for hand-off |

The previous compatibility fallback at the end of `FieldGeneratorResolver.resolveAndGenerate`
returned `null` for synthetic `Object.class` elements. It was removed in Slice C because it created
collections with null elements and maps with missing entries instead of reporting the original
unsupported signature.

## Required internal model

Introduce one package-private immutable model with these facts:

- original declared `Type` and stable `getTypeName()` signature;
- resolved raw class when one exists;
- recursively resolved type arguments or array component;
- kind: class, parameterized type, generic array, wildcard, or type variable;
- the active type-variable bindings inherited from the concrete root type;
- an explicit unresolved reason when a wildcard or variable is ambiguous.

The model is an implementation detail. It must not add a public API while the supported bounds are
still being proven.

## Resolution rules

1. Plain classes retain their class identity; ordinary arrays recursively retain their component.
2. Parameterized types retain every argument rather than projecting arguments to `Class<?>`.
3. `? extends X` resolves to its single effective upper bound. An unbounded wildcard is ambiguous.
4. `? super X` resolves generated values to its single lower bound.
5. A type variable first uses the concrete declaring-type binding, then one effective bound.
   Multiple unrelated bounds remain unsupported until an explicit generation policy exists.
6. Generic arrays resolve their component recursively and create an array only when the component
   has a concrete runtime class.
7. Raw containers and unresolved `Object` arguments fail through the existing strict/lenient
   policy; they no longer silently manufacture null elements.
8. Paths compose at every argument: `field[]`, `field.key`, `field.value`, and nested object fields.
9. Type-use annotations remain attached to the corresponding node when an `AnnotatedType` is
   available. Declaration annotations continue to apply at the owning field/component.

## Incremental implementation slices

### Slice A — Parse and test the model

- Add model-only tests for classes, nested parameterized types, wildcards, variables, and generic
  arrays.
- Do not change generated values in this slice.

**Complete.** `ResolvedType` retains recursive arguments, components, effective bounds, bindings,
signatures, and explicit unresolved reasons without adding public API.

### Slice B — Containers and optionals

- Replace `typeArg(...)` with recursive child nodes.
- Cover `List<List<String>>`, `Map<String, List<Integer>>`, nested optionals, and records containing
  the same shapes.

**Complete.** Optional, set, list, queue, and map child resolution now passes `ResolvedType` nodes
through the existing resolver. Mutable fields and Java records share the same behavior.

### Slice C — Bounds and inherited variables

- Resolve upper/lower wildcards and concrete superclass/interface bindings.
- Reject ambiguous/unbounded shapes with the complete path and signature.

**Complete.** Upper/lower wildcard bounds feed default container generation. Raw containers and
unbounded wildcard arguments now fail at the parent field with the full container signature;
explicit lenient mode returns `null` for the whole value and emits a sanitized diagnostic. Concrete
root bindings propagate through inherited direct and container fields, including multi-level
parameterized bindings. Custom collection subtypes project their element/key/value variables through
their generic superclass and interface contracts. Parameterized nested classes and records carry
their resolved bindings into the child generator, including when nested inside another container;
raw and unbounded child types fail before recursive construction.

### Slice D — Generic arrays and schema metadata

- Generate concrete generic arrays when their component binding is known.
- Make schema record inference follow the same recursive resolution rules.

**Complete.** Generic arrays retain recursively resolved components for mutable fields and
records, including inherited `T[]` bindings and parameterized components such as `List<String>[]`.
Unresolved array components fail at the parent field in strict mode and discard the whole array with
a contextual diagnostic in explicit lenient mode. Record JSON Schema inference now preserves nested
collections, maps, optionals, parameterized records, generic arrays, wildcard bounds, enums, and
fixed generic collection subtypes. Recursive and unbounded shapes terminate conservatively at `{}`.
Optional record values serialize as their contained value or JSON `null`, matching the inferred
nullable schema.

The object model remains package-private as required by the v2 API policy. Java package boundaries
therefore prevent the schema package from importing it without creating a new public API. A narrow
schema-side `Type` adapter implements the same resolution contract and is locked by parity tests;
cross-module consolidation is deferred to Slice E, where constructor and Kotlin consumers can drive
the final internal/public boundary intentionally.

### Slice E — Consumers

- Route constructor work in Step 2.4 and Kotlin immutable construction in Step 2.5 through the same
  model instead of creating parallel type systems.

**In progress.** Kotlin primary-constructor generation now proves concrete bindings through nested
generic data classes. Object-graph cycle detection and caching distinguish the raw class together
with its resolved generic signature, so `Box<Inner<String>>` cannot reuse a completed
`Box<List<Integer>>` instance merely because both erase to `Box`. A true same-signature recursive
reference retains the existing cycle boundary. Resolved child types now retain their matching
`AnnotatedType` node through optional values, array components, collection elements, and map
keys/values. Type-use constraints therefore apply at every supported container depth and report
the existing composed child path; declaration annotations remain on the owning field/component.

## Completion gate

- No nested parameterized argument is converted to `Object.class` merely because it is not a plain
  class.
- Supported values are assignment-compatible at every container depth.
- Unsupported shapes fail with a sanitized root-relative path and full declared signature.
- Java fields and records behave equivalently.
- Schema metadata represents the same supported generic shapes as object generation.
- The full Java 21 pre-commit gate remains green.
