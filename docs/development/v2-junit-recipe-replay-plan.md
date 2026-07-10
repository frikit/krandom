# V2 JUnit Recipe Replay Plan

**Master-plan link:** Step 3.6 — JUnit replay integration

## Stage 1: Define the JUnit replay boundary

**Goal:** Specify a source-edit-free JUnit replay override for a complete generation recipe or a
numeric seed, including precedence, privacy, and configuration-error behavior.

**Success Criteria:** System properties are deterministic and take precedence over annotation
seeds; a recipe retains its textual-seed metadata when injected, while diagnostics do not reveal
that text.

**Tests:** Engine-test fixtures cover recipe override, numeric seed override, malformed input, and
failure reporting.

**Status:** Complete

## Stage 2: Implement portable JUnit replay overrides

**Goal:** Make `KrandomExtension` build its injected `GeneratorConfig` from a validated replay
recipe or seed without requiring a change to the test source.

**Success Criteria:** A CI runner can set one documented JVM property and get the same config for
all injected parameters in the test; unsupported or conflicting overrides fail before the test
body runs.

**Tests:** `KrandomExtensionEngineTest`.

**Status:** Complete

## Stage 3: Document and verify the release slice

**Goal:** Document copyable replay options, preserve the existing safe diagnostic behavior, and
run the focused plus full release gates.

**Success Criteria:** The guide names the accepted formats and precedence; jqwik bridging is
permanently out of scope because jqwik is forbidden in this project.

**Tests:** `:junit:test`, `./scripts/pre_commit_check.sh`, and local consumer verification.

**Status:** Complete
