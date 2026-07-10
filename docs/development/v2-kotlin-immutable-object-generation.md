# V2 Kotlin Immutable Object Generation

**Status:** Complete

## Investigation log

The initial adapter compile uncovered three distinct integration facts: Kotlin reflection is a new
verified runtime dependency; `KParameter` does not expose a stable Java-parameter bridge; and Kotlin
tests must keep multiline expected values outside infix matcher expressions. The implementation now
uses the primary constructor's stable Java parameter index, falls back to a backing field for
annotations, and marks resolver output nullable at the Java boundary so Kotlin enforces non-null
parameters before constructor invocation.

**Scope:** Immutable Kotlin object construction through `krandom-kotlin-dsl`; Java core remains
free of Kotlin runtime dependencies.

## Goal

Immutable Kotlin values must be constructed through their primary constructors with Kotlin's
nullability and default-parameter semantics. Core must reject such values clearly when the Kotlin
integration is unavailable; it must never allocate a partially initialized Kotlin object.

## Stage 1: Core hand-off boundary

**Goal:** Define a small construction-adapter boundary in core without adding Kotlin dependencies.
**Success Criteria:** Core discovers an installed adapter for root and nested values, gives adapters
the existing field-resolution pipeline, and names the Kotlin DSL dependency when an immutable
Kotlin type has no adapter.
**Tests:** Core remains Java-only; root/nested adapter ordering and unsupported Kotlin diagnostics
are covered from compiled Kotlin consumers.
**Status:** Complete

## Stage 2: Primary constructor adapter

**Goal:** Implement Kotlin reflection in `krandom-kotlin-dsl`.
**Success Criteria:** Data classes and immutable primary-constructor classes preserve non-null
properties, use generic parameter types and validation annotations, preserve optional defaults
unless an explicit override applies, and recurse through nested Kotlin values.
**Tests:** `val`, nullable/non-null, defaults, nested generics, field/type rules, constructor
constraints, value classes, sealed classes, objects, and cycles have explicit outcomes.
**Status:** Complete

## Stage 3: Published-consumer contract and documentation

**Goal:** Verify the boundary from locally published artifacts and document supported shapes.
**Success Criteria:** Kotlin Gradle and Maven consumers prove the supported/unsupported split;
KDoc, migration notes, and the master plan state the exact dependency and limitations.
**Tests:** Full Java 21 pre-commit gate plus local consumer matrix.
**Status:** Complete
