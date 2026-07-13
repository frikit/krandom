# V2 Stripe Sandbox Card Plan

**Master-plan link:** Step 2.9 — financial and identity safety modes

## Stage 1: Establish the processor-specific contract

**Goal:** Define one explicit Stripe sandbox policy using only values documented by Stripe for
interactive test payments.

**Success Criteria:** The policy is opt-in, keeps the non-routable default unchanged, and states
that it requires Stripe sandbox/test API keys; server-side code is directed to Stripe
`PaymentMethod` values instead of raw card numbers.

**Tests:** Red tests cover one fixed official test number for every card type currently supported
by `CreditCardGenerator`.

**Status:** Complete

## Stage 2: Implement deterministic card mapping

**Goal:** Route each supported card type to its corresponding Stripe sandbox number under the
explicit policy.

**Success Criteria:** Formatted and unformatted generation, structured card information, and
seeded recipes retain a coherent card type and the official number; other policies retain their
current behaviour.

**Tests:** `CreditCardGeneratorTest`, `CreditCardInfoGeneratorTest`, and recipe/configuration
tests.

**Status:** Complete

## Stage 3: Publish and verify the safety boundary

**Goal:** Update safety metadata, migration guidance, and the Stage 2 checklist without
overstating portability or production safety.

**Success Criteria:** The provider catalog and generated documentation identify the selected
policy, the financial-safety contract links Stripe's official guidance, and all local checks pass.

**Tests:** `./scripts/pre_commit_check.sh` and local consumer examples.

**Status:** Complete
