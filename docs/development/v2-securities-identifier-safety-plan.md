# V2 Securities-Identifier Safety Plan

**Status:** Complete
**Master plan:** [Step 2.9](v2-master-implementation-plan.md#step-29--add-explicit-financial-and-identity-safety-modes)

## Stage 1: Define the safety boundary

**Goal:** Treat ISIN and CUSIP values as real financial-instrument identifiers rather than safe
fixtures merely because their checksum is valid.

**Success Criteria:** The code and public guidance state that neither an ISIN nor a CUSIP generated
by krandom is assigned, non-routable, or safe to submit to a trading, custody, clearing, or
settlement system.

**Tests:** Characterization tests distinguish legacy compatibility constructors from configured
generation.

**Status:** Complete

## Stage 2: Enforce explicit configuration

**Goal:** Make configured ISIN and CUSIP generation fail closed unless a caller explicitly selects
an isolated-test compatibility policy.

**Success Criteria:** `GeneratorConfig`, `Generators`, and the finance namespace apply the same
policy; the deprecated direct constructors retain historical realistic-but-unclassified output.

**Tests:** Default configured generators and canonical facades throw; explicit compatibility mode
preserves valid length, shape, checksum, locale, and seeded behavior.

**Status:** Complete

## Stage 3: Preserve replay and document migration

**Goal:** Persist the selected policy in portable recipes and give 1.6 consumers an unambiguous
migration path.

**Success Criteria:** New recipes record the policy, old recipes preserve their historic
realistic-unclassified replay behavior, and API/migration documentation matches the implementation.

**Tests:** Recipe round-trip and absent-setting compatibility tests pass; public API checks accept
only the deliberate additive and deprecated symbols.

**Status:** Complete
