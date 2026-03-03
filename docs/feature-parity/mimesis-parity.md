# Mimesis vs kRandom (Java) - Phase 0 Baseline

## Library Overview

- Name: Mimesis (Python)
- Version context: 18.x API surface (reference parity target)
- kRandom target: Java core parity only
- Active plan: `docs/plans/mimesis-java-plan.md`

## Phase 0 Audit Baseline (2026-03-03)

This baseline replaces stale row-level statuses and normalizes parity into `Yes`, `Partial`, or `No` at category level with implementation notes.

### Summary by Category

| Category                                       | Status  | Locale Notes                                  | Comments |
|------------------------------------------------|---------|-----------------------------------------------|----------|
| Primitive/base generators                      | Yes     | Locale not required                           | Numeric, boolean, string, enum, format, regex, distribution generators are mature and complete. |
| Identity/person                                | Partial | Locale-aware name/profile/gender providers    | First/last/full/middle names, username, password, title/suffix, age, birthday, profile records exist; Mimesis-specific API shape/options still differ. |
| Address/location                               | Partial | Locale-aware city/state/country/postal/phone  | Core generators exist and are locale-aware; Mimesis-style helper naming/options (country-code format enums, continent/calling-code aliases) are incomplete. |
| Internet/network                               | Partial | Mostly locale-neutral                         | URL/domain/hostname/uri/slug/ip/mac/port/http/user-agent coverage exists; unique-email and some option-rich API forms are still gaps. |
| Date/time                                      | Partial | Locale-aware formatting where relevant         | Date/time/instant/localdatetime/zoneddatetime/timezone/duration generators exist; Mimesis-specific formatting and alias contracts are incomplete. |
| Finance/payments                               | Partial | Locale-aware where meaningful                  | Credit-card suite, ACH/account/routing, currency, money, IBAN/BBAN/BIC/ISIN, EIN/CUSIP, crypto-address exist; some Mimesis object/option contracts remain. |
| Commerce/company/job                           | Partial | Locale-aware in commerce and profession data   | Company/industry/job/profession and commerce product primitives are present; Mimesis provider-specific naming and object contracts are not fully aligned. |
| Text/lorem                                     | Partial | Locale-aware text providers                    | Word/sentence/paragraph/lorem/text generators exist; Mimesis vocabulary/answer/quote options are only partially mapped. |
| Identifiers/codes                              | Yes     | Locale not central                            | UUID, hash, EAN, ISBN, UPC, national-id families and Luhn-style support are robust. |
| Structured/bulk generation (Schema/Field DSL) | No      | Locale propagation not yet applicable          | Mimesis’ core Schema+Field bulk-generation model is not implemented in Java form yet. |
| Generic provider hub/extensibility             | Partial | Locale propagation to providers exists today   | Domain generators are extensible in several areas, but no unified Mimesis-style generic provider hub/registry contract yet. |
| Binary/hardware/science niche providers        | No      | Locale not central                            | Out of current core scope; candidate intentional skip unless explicitly requested. |

## Open Gaps Tagged for Execution

| Gap                                                                 | Current Status | Priority Tag | Notes |
|---------------------------------------------------------------------|----------------|--------------|-------|
| Reconcile core Mimesis API shape (identity/address/internet/date)  | Partial        | P0           | Behavior largely exists; naming/options differ. |
| Telephone mask + identifier mask contracts                          | Partial        | P0           | Existing phone/format primitives can be wrapped for parity-style APIs. |
| Unique email and richer domain/url option contracts                 | Partial        | P0           | Existing email/domain/url generators cover most primitives. |
| Mimesis credit-card/network/object contract alignment               | Partial        | P1           | Core card generators exist; object/alias contract alignment remains. |
| Company/job/commerce provider naming and object-shape parity        | Partial        | P1           | Domain generators exist; provider-level compatibility layer is incomplete. |
| Schema/Field bulk generation DSL                                    | No             | P1           | Major missing differentiator and high-value target. |
| Generic provider hub and runtime provider registration              | Partial        | P1           | Extensibility exists in places; unified hub contract missing. |
| Locale breadth expansion (~Mimesis scale)                           | Partial        | P2           | Existing locale support is strong but narrower in breadth/depth. |
| Binary/hardware/science provider families                           | No             | P2 (skip candidate) | Low ROI for core fixture workflows unless requested. |

## Alias Candidates Identified in Phase 0

Potential `Yes` via alias/wrapper additions with minimal logic changes:

- `full_name(reverse=...)` -> `FullNameGenerator` option bag extension.
- `telephone(mask=...)` -> existing phone generator + template/format substitution.
- `identifier(mask=...)` -> template-string primitives (`numerify`/`bothify`) and/or number format generator.
- `country_code(fmt=A2/A3/NUMERIC)` -> `CountryGenerator.generateCode()` / `generateCodeAlpha3()` + numeric mapping layer.
- `currency_iso_code` / `currency_symbol` / `price` -> existing currency/money/commerce APIs.
- `credit_card_number` / `cvv` / `expiration` / network aliases -> existing credit-card methods.
- `uuid(version=4)` -> existing UUID generator alias mapping.

## Locale Support Baseline (Java)

Current locale-sensitive behavior already exists in:

- Names/profile providers (including gender-aware naming support where modeled).
- Address providers (city/state/country/postal/phone/coordinates).
- Commerce vocabulary and profession/job datasets.
- Currency/money formatting and locale-sensitive finance helpers.
- Text generators (word/sentence/paragraph/lorem families).

Known limitation vs Mimesis:

- Locale breadth/depth is curated and narrower than Mimesis’ larger locale catalog.

## Phase 0 Outcome

- Mimesis parity baseline is now normalized and actionable.
- Stale row-level `No` statuses have been reclassified at category level into `Yes/Partial/No` based on current Java implementation.
- Priority sequencing is now ready for Phase 1 implementation.
