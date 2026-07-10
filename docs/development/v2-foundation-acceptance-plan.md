# V2 Foundation Acceptance Gate Plan

**Master-plan link:** Step 2.10 — foundation integration gate

## Stage 1: Map audit criteria to executable contracts

**Goal:** Turn each P0.1, P0.2, and P0.6 acceptance criterion from the v2 readiness audit into
an explicit set of focused tests.

**Success Criteria:** The evidence map identifies the test classes that prove object correctness,
random-source determinism, and safe financial or identity fixtures.

**Tests:** Review [`v2-foundation-acceptance.md`](v2-foundation-acceptance.md) against the audit.

**Status:** Complete

## Stage 2: Add a repeatable clean acceptance command

**Goal:** Provide one local command that runs the focused cross-module acceptance suite twice from
clean project outputs.

**Success Criteria:** The command includes Java object, random-source, recipe, safety-policy, and
Kotlin immutable-object coverage; either pass must fail the command when a selected contract
regresses.

**Tests:** `JAVA_HOME=<JDK 21+> ./scripts/verify_foundation_acceptance.sh`

**Status:** Complete

## Stage 3: Record the evidence without closing unrelated work

**Goal:** Mark only the Step 2.10 test evidence supported by the acceptance command while keeping
the open cross-framework replay and internal type-model boundaries visible.

**Success Criteria:** The master plan links to the command and evidence, and Stage 2 remains in
progress until its outstanding implementation action is complete. (The jqwik replay expectation
recorded here was later ruled out of scope: jqwik is forbidden in this project.)

**Tests:** Run the acceptance command and `./scripts/pre_commit_check.sh`.

**Status:** Complete
