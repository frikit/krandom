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
| Identity/person                                | Yes     | Locale-aware name/profile/gender providers    | Added `full_name(reverse)` options parity, telephone/identifier mask helpers, and maintained Java-native demographics/profile mapping. |
| Address/location                               | Yes     | Locale-aware city/state/country/postal/phone  | Added country-code format options (A2/A3/numeric) plus calling-code/continent/timezone helper aliases. |
| Internet/network                               | Yes     | Mostly locale-neutral                         | Added URL/URI/TLD/query alias coverage and unique/configurable email-domain generation; HTTP status message/header helpers included. |
| Date/time                                      | Yes     | Locale-aware formatting where relevant         | Added datetime/timezone alias conveniences on top of existing date/time generators. |
| Finance/payments                               | Yes     | Locale-aware where meaningful                  | Added credit-card payload/map contracts, CVV/expiration/network aliases, and currency/price API-shape aliases with locale-aware formatting. |
| Commerce/company/job                           | Yes     | Locale-aware in commerce and profession data   | Added category/material/product payload parity plus product code helpers (UPC/EAN/ISBN) and structured product payload generation. |
| Text/lorem                                     | Yes     | Locale-aware text providers                    | Added word/sentence/text alias normalization while preserving locale-aware vocabulary behavior. |
| Identifiers/codes                              | Yes     | Locale not central                            | UUID, hash, EAN, ISBN, UPC, national-id families and Luhn-style support are robust. |
| Structured/bulk generation (Schema/Field DSL) | Yes     | Locale propagated through schema field config  | Added Java `Field` + `Schema` DSL with binding lookup, seeded deterministic batches, nested/list field composition, and contextual validation errors. |
| Generic provider hub/extensibility             | Yes     | Locale propagated through provider hub config  | Added a unified `ProviderHub` with canonical providers, alias lookup, runtime provider registration hooks, and conflict-policy controls. |
| Binary/hardware/science niche providers        | No      | Locale not central                            | Out of current core scope; candidate intentional skip unless explicitly requested. |

## Open Gaps Tagged for Execution

| Gap                                                                 | Current Status | Priority Tag | Notes |
|---------------------------------------------------------------------|----------------|--------------|-------|
| Reconcile core Mimesis API shape (identity/address/internet/date)  | Implemented    | P0 ✅        | Core API-shape aliases/options completed in Java generators. |
| Telephone mask + identifier mask contracts                          | Implemented    | P0 ✅        | Added phone mask aliases and a dedicated identifier-mask generator. |
| Unique email and richer domain/url option contracts                 | Implemented    | P0 ✅        | Added unique-email generation and configurable domain-set APIs. |
| Mimesis credit-card/network/object contract alignment               | Implemented    | P1 ✅        | Added card payload record/map helpers and network/cvv/expiration aliases. |
| Company/job/commerce provider naming and object-shape parity        | Implemented    | P1 ✅        | Added commerce category/material/code helpers and product payload object contract. |
| Schema/Field bulk generation DSL                                    | Implemented    | P1 ✅        | Added `schema` package (`FieldLookup`, `Field`, `Schema`, context + exception) with list/nested composition and seeded batch generation. |
| Generic provider hub and runtime provider registration              | Implemented    | P1 ✅        | Added `provider` package hub (`ProviderHub`, `ProviderFactory`, `ConflictPolicy`) with alias compatibility and runtime registration/override semantics. |
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
