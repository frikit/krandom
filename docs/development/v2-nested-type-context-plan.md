# V2 Nested Type Context Plan

**Master-plan link:** Step 2.2 — recursive type model

## Stage 1: Define nested annotation behaviour

**Goal:** Prove that Bean Validation type-use annotations apply to generated values inside
optionals, collections, maps, and record components.

**Success Criteria:** A focused test covers each container edge and asserts that an incompatible
type-use constraint reports the child path rather than the owning field path.

**Tests:** `ObjectGeneratorNestedTypeUseConstraintTest`

**Status:** Complete

## Stage 2: Preserve annotated type nodes during recursion

**Goal:** Carry the matching `AnnotatedType` node beside each resolved generic child type.

**Success Criteria:** Existing declaration-level field/component behaviour is unchanged, while
child resolution receives its own type-use annotations for optional values, arrays, collection
elements, and map keys/values.

**Tests:** Focused object-generator test suite and the Java 21 pre-commit gate.

**Status:** Complete

## Stage 3: Document and verify the contract

**Goal:** Record the supported annotation/path model and close the corresponding master-plan
item.

**Success Criteria:** The recursive type model, master plan, and changelog describe the behaviour;
the full local check passes.

**Tests:** `./scripts/pre_commit_check.sh`

**Status:** Complete
