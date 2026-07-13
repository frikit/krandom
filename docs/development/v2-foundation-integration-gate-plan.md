# V2 Foundation Integration Gate Plan

**Master-plan link:** Step 2.10 — foundation integration gate

## Stage 1: Make the release gate reproducible

**Goal:** Provide one local command that verifies the correctness gate and published consumer
artifacts in their supported Java, Kotlin, JPMS, and Scala environments.

**Success Criteria:** The command delegates to the existing full project gate and local-artifact
consumer verification without publishing to a remote repository.

**Tests:** `scripts/verify_foundation_integration_gate.sh`

**Status:** Complete

## Stage 2: Record Stage 2 evidence and limitations

**Goal:** Tie P0.1, P0.2, and P0.6 acceptance evidence, API evolution, exceptions, and migration
guidance to the repeatable command; explicitly retain unresolved architectural work.

**Success Criteria:** A maintainer can see the checked evidence and why Stage 2 remains open.

**Tests:** Documentation links and `./scripts/pre_commit_check.sh`

**Status:** Complete

## Stage 3: Verify the combined gate

**Goal:** Run the new command from a clean local worktree.

**Success Criteria:** All delegated gates pass, including public API classification and local
consumer examples.

**Tests:** `./scripts/verify_foundation_integration_gate.sh`

**Status:** Complete
