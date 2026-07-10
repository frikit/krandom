# V2 Financial and Identity Safety Contract

**Status:** In Progress
**Master plan:** [Step 2.9](v2-master-implementation-plan.md#step-29--add-explicit-financial-and-identity-safety-modes)

## Scope and vocabulary

This contract distinguishes four independent properties:

- **Format-valid:** the value has the expected textual structure.
- **Checksum-valid:** the value passes its published checksum, when one exists.
- **Semantically plausible:** the value has realistic-looking issuer, country, or regional fields.
- **Test-safe/non-routable:** the value is either deliberately invalid for a real network or is an
  official sandbox value used only with that processor's sandbox credentials.

Neither checksum validity nor semantic plausibility means a value is safe to use in production,
KYC, account creation, payment authorization, or live network calls.

## Stage 1: Classification inventory

**Goal:** Classify all sensitive generators by their current behavior before changing defaults.
**Success Criteria:** Every generator below has one documented status and no default is described
as safe merely because it passes a validator.
**Tests:** Classification tests and provider metadata must agree with the table.
**Status:** In Progress

| Family | Current behavior | v2 target |
| --- | --- | --- |
| Credit cards | Issuer-shaped, format-valid numbers that deliberately fail Luhn by default; `CHECKSUM_VALID` is an explicit opt-in | Default remains non-routable; processor-sandbox values require a separately named processor mode |
| IBAN, ABA, BIC, bank account | Format/checksum behavior varies by generator | Do not claim test safety until each scheme has a non-routable or official sandbox contract |
| Payment payloads | Masked references derived from card or bank generators | Carry the selected policy metadata without exposing credential values |
| National IDs, CPF/CNPJ, passport, driving licence | Country-specific shapes and algorithms | Default to non-production fixtures; country-specific valid modes require separate proof |
| Phone numbers | US locale-style output uses NANPA's fictional `555-0100` to `555-0199` range by default; other locales remain realistic but unclassified | Keep the NANPA mode scoped to its documented allocation; add another locale only with authoritative proof |
| Crypto addresses | Syntactically plausible destination strings | Never describe as safe for live transfers; use explicit non-production modes only after network-specific proof |

## Stage 2: Payment-card safety modes

**Goal:** Make the selected card-output mode enforceable through `GeneratorConfig` and visible in
the recipe.
**Success Criteria:** The default card output is non-routable, while a caller must explicitly
choose checksum-valid or a named processor sandbox mode. The card's metadata says which properties
hold.
**Tests:** Default values fail Luhn without losing the selected card format; checksum-valid mode
passes Luhn; each supported sandbox value is fixed, documented, and deterministic under a seed.
**Status:** In Progress

Implemented on the 1.6 bridge:

- `PaymentCardSafetyPolicy.TEST_SAFE_NON_ROUTABLE` is the default. It keeps issuer prefix and
  length rules, then deliberately changes the Luhn check digit so the generated number fails Luhn.
- `PaymentCardSafetyPolicy.CHECKSUM_VALID` is an explicit `GeneratorConfig` opt-in for isolated
  validator fixtures. It is not an official processor sandbox mode or a usable credential.
- Portable recipes persist the selected policy as
  `setting.payment.card-safety-policy`, so replay retains the same card contract.

Recipes created before that setting existed replay their historic checksum-valid behavior. Newly
created recipes always write the setting explicitly and therefore preserve the new default.

Processor-sandbox support remains unimplemented until the library can name the processor, its
documented test values, and the required sandbox credential boundary without implying portability
between processors.

The processor-sandbox mode must name the processor and never imply that its values are accepted by
another processor. Stripe documents sandbox card values and requires test API keys; use a processor
token rather than a raw number in server-side test code when that integration supports tokens.

## Stage 3: Bank, phone, and identity contracts

**Goal:** Add a scheme-specific non-routable or sandbox strategy only when authoritative source
material supports it.
**Success Criteria:** No generator is upgraded from "unclassified" on the strength of format or
checksum alone. A policy that cannot be made safe fails closed or remains unavailable.
**Tests:** Per-scheme validator and non-routability tests, plus property tests over every supported
locale or country provider.
**Status:** In Progress

Implemented phone-number scope:

- `PhoneNumberSafetyPolicy.TEST_SAFE_WHERE_AVAILABLE` is the default. US locale-style output uses
  a real area code plus `555-0100` through `555-0199`.
- `PhoneNumberSafetyPolicy.REALISTIC_UNCLASSIFIED` preserves the prior realistic-looking output
  without claiming that it is non-routable.
- Other locales, custom phone-number templates, and synthetic MSISDN output are unclassified.
- Portable recipes record the selected phone policy. Recipes recorded before the setting was added
  retain their historic unclassified replay behavior.

For North American phone fixtures, NANPA reserves the fictional non-working `555-0100` through
`555-0199` line-number block. This allocation does not establish a safe range for other countries.

## Stage 4: Metadata, schemas, and guidance

**Goal:** Expose safety metadata from the provider catalog and schema projections, and make the
forbidden uses impossible to miss in public documentation.
**Success Criteria:** Provider reference, schema metadata, recipes, and guides name the same
policy and classification. Generated personal-looking or payment-looking values are never offered
as credentials for production systems.
**Tests:** Documentation/reference snapshot tests and catalog-metadata completeness checks.
**Status:** In Progress

Implemented provider-catalog metadata:

- Every `ProviderDescriptor` exposes immutable `ProviderSafetyMetadata` with independent format,
  checksum, semantic-plausibility, and test-safety classifications. `UNCLASSIFIED` is explicit: it
  means krandom makes no claim for that dimension.
- `finance.credit_card` and `finance.credit_card_info` guarantee their documented shape and
  issuer semantics, while checksum validity and test safety are configuration-dependent.
- `address.phone_number` guarantees its documented output shape, has no checksum dimension, and
  makes configuration-dependent semantic and test-safety claims because the fictional range is
  deliberately limited to documented US locale-style output.
- The generated [provider catalog reference](../reference/provider-catalog.md) renders the exact
  catalog metadata, and its snapshot test prevents documentation drift.
- Each classified `FieldLookup` schema projection adds an `x-krandom-safety` extension with those
  claims plus a nested `policy.setting` and the selected `policy.selected` value. `finance.cvv`
  intentionally has no extension: the card-number policy does not change CVV generation.

## Sources

- [Stripe testing guidance and card values](https://docs.stripe.com/testing) — sandbox values only;
  Stripe requires test API keys and cautions against using real card details.
- [NANPA 555 line numbers](https://www.nanpa.com/numbering/555-line-numbers) — reserves
  `555-0100` through `555-0199` for fictional non-working use.
