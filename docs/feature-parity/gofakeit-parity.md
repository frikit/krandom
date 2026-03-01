# GoFakeit vs kRandom (Java) - Phase 0 Baseline

## Library Overview

- Name: GoFakeit (Go)
- Version context: v6 API surface (reference parity target)
- kRandom target: Java core parity only
- Active plan: `docs/plans/gofakeit-java-plan.md`

## Phase 0 Audit Baseline (2026-03-01)

This baseline replaces the earlier stale matrix and normalizes parity into `Yes`, `Partial`, or `No`.

### Summary by Category

| Category                                                    | Status  | Locale Notes                                  | Comments                                                                                                                                        |
|-------------------------------------------------------------|---------|-----------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------|
| Primitive random APIs                                       | Yes     | Locale not required                           | Numeric/string/boolean/enum/range generation is strong and already broad.                                                                       |
| Identity/person                                             | Partial | Locale-aware in name/profile providers        | First/last/full/middle names, username, password, age, gender, profile exist; GoFakeit-style aliases and person-object shape still differ.      |
| Address/location                                            | Partial | Locale-aware in core address providers        | Street/city/state/postcode/country/coords/phone are implemented; several GoFakeit helper names and composite object forms remain gaps.          |
| Contact/phone                                               | Partial | Locale-aware phone formats exist              | Email and phone generation exist; contact-info composite and exact API shapes differ.                                                           |
| Internet/network                                            | Yes     | Mostly locale-neutral                         | URL/domain/suffix/slug/ip/mac/port/http method/status/user-agent coverage is present with naming differences in some APIs.                      |
| Finance/payments                                            | Partial | Locale used where meaningful                  | Credit-card suite, ACH routing, currency, IBAN/BIC/ISIN, crypto address exist; ACH account/bank-type/EIN/CUSIP/object-shape differences remain. |
| Company/job/commerce                                        | Partial | Commerce has locale-aware vocabulary          | Company/job providers exist; GoFakeit job/composite object and some convenience aliases are still missing.                                      |
| Date/time                                                   | Partial | Locale-aware formatting in relevant providers | Date/time/timezone support is broad; GoFakeit helper naming and some component conveniences are not 1:1.                                        |
| Text/lorem/patterns                                         | Partial | Locale-aware text generators exist            | Word/sentence/paragraph/lorem are implemented; numerify/lexify/bothify/asciify and GoFakeit generate/template compatibility remain incomplete.  |
| Object/struct generation                                    | Partial | Locale not central                            | Reflection object generation exists; GoFakeit-style `fake:` tag semantics and `fakesize` directives are not implemented.                        |
| Structured output (CSV/JSON/XML/SQL)                        | No      | Locale not central                            | Dedicated formatted-output providers are not implemented.                                                                                       |
| System/database                                             | Partial | Locale not central                            | Version/platform/exception payload plus database `column`/`type` subset now exist.                                                              |
| Long-tail novelty datasets (minecraft/emoji/celebrity/etc.) | No      | Locale not central                            | Out of current core scope; defer to intentional skip unless explicitly requested.                                                               |

## Open Gaps Tagged for Execution

| Gap                                                                                     | Current Status | Priority Tag        | Notes                                                                                   |
|-----------------------------------------------------------------------------------------|----------------|---------------------|-----------------------------------------------------------------------------------------|
| Reconcile GoFakeit alias names for core providers (identity/address/network/date/text)  | Partial        | P0                  | Most behavior exists but API names differ; add compatibility aliases where practical.   |
| Pattern APIs (`numerify`, `lexify`, `bothify`, `asciify`)                               | Partial        | P0                  | Can be layered over existing template/format generators.                                |
| Address/contact composite objects (`Address`, `Contact`, `Person`)                      | Partial        | P0                  | Core generators exist; convenience object payloads still missing.                       |
| Finance object/convenience parity (`CreditCardInfo`, ACH account, bank type, EIN/CUSIP) | Partial        | P1                  | Several primitives exist; missing convenience APIs and some identifiers.                |
| Company/job object parity (`Job` object + descriptor/level mapping)                     | Partial        | P1                  | Current generators cover many pieces but not GoFakeit object shape.                     |
| GoFakeit-style struct tags (`fake:`, `fakesize`, regex/list tags)                       | No             | P1                  | Requires tag parser + mapping layer over object generation.                             |
| Template compatibility (`Generate("{function}")` + function lookup registry)            | Partial        | P1                  | Existing template/string generators provide base but not full GoFakeit syntax/registry. |
| Structured output providers (CSV/JSON/XML/SQL/Markdown/fixed-width)                     | No             | P1                  | Not implemented in current Java generators.                                             |
| Locale breadth expansion for text/company/address datasets                              | Partial        | P2                  | Core locales exist; depth and breadth below GoFakeit’s broad catalog.                   |
| Novelty/long-tail families (minecraft/emoji/celebrity/food/media/animals)               | No             | P2 (skip candidate) | Low ROI for core fixture parity unless explicitly requested.                            |

## Alias Candidates Identified in Phase 0

Potential `Yes` via alias/wrapper additions (minimal logic changes):

- `Phone()` / `PhoneFormatted()` -> `PhoneNumberGenerator.generate(false/true)`
- `DomainName()` / `DomainSuffix()` / `URL()` / `UrlSlug()` -> existing domain/url/slug generators
- `HTTPMethod()` / `HTTPStatusCode()` -> existing HTTP generators
- `StreetName()` / `StreetSuffix()` / `StreetNumber()` -> existing street-address component methods
- `StateAbr()` -> `StateGenerator.generate(true)`
- `CurrencyShort()` / `CurrencyLong()` / `Price(min,max)` -> existing currency/money/commerce methods
- `CreditCardNumber()` / `CreditCardExp()` / `CreditCardCvv()` / `CreditCardType()` -> existing credit-card methods

## Locale Support Baseline (Java)

Current locale-sensitive behavior already exists in:

- Names/profile providers
- Address/city/state/country/postcode/phone providers
- Commerce vocabulary generation
- Currency/money/finance formatting where relevant
- Text generators (word/sentence/paragraph families)

Known limitation versus GoFakeit:

- Locale breadth and vocabulary depth are curated and narrower than large multi-domain catalogs.

## Phase 0 Outcome

- Parity baseline is now normalized and actionable.
- Stale `No` statuses caused by outdated rows have been corrected to `Yes`/`Partial` at category level.
- Priority sequencing is now ready for Phase 1 implementation.
