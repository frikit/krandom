# Bogus (.NET) vs kRandom (Java) - Phase 0 Baseline

## Library Overview

- Name: Bogus (.NET)
- Version analyzed: latest public API surface (as represented in existing parity notes)
- kRandom target: Java core parity only
- Active plan: `docs/plans/bogus-java-plan.md`

## Phase 0 Audit Baseline (2026-03-01)

This baseline replaces older stale status claims and normalizes parity into `Yes`, `Partial`, or `No`.

### Summary by Category

| Category               | Status  | Locale Notes                                   | Comments                                                                                                                                           |
|------------------------|---------|------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------|
| Primitive random APIs  | Yes     | Locale not required                            | Numeric/string/enum/random/hash coverage is strong.                                                                                                |
| Object generation core | Partial | Locale not central                             | Reflection/object generation is strong, but no Bogus-style fluent `Faker<T>` rule engine yet.                                                      |
| Person/identity        | Partial | Locale-aware in multiple providers             | Names/titles/gender/profiles are covered; some Bogus composites and convenience APIs remain different.                                             |
| Network/internet       | Partial | Locale usually not central                     | IPv4/IPv6/MAC/port/domain/url/uri/user-agent are present; Bogus-specific .NET object forms are not relevant in Java.                               |
| Finance                | Partial | Locale used where meaningful                   | Credit-card suite, IBAN/BIC/ABA, currency, money, EAN exist; account/transaction/crypto provider set is incomplete.                                |
| Address/location       | Partial | Locale-aware datasets implemented              | Street/city/state/postcode/country/coordinates/phone exist; some Bogus-only fields (for example county/direction variants) are missing or not 1:1. |
| Date/time              | Partial | Locale-aware formatting in relevant generators | Broad date/time coverage exists; Bogus-specific helpers (`recent`, `soon`, some offset-specific ergonomics) are not complete 1:1.                  |
| Lorem/text             | Partial | Locale-aware word datasets present             | Word/sentence/paragraph/text family exists; a few Bogus-specific variants (for example dedicated lines helper) are not yet explicit.               |
| Company/business       | Yes     | Locale-aware phrase datasets present           | Company name/suffix/bs/catchphrase present.                                                                                                        |
| Files/system           | Partial | Locale not required                            | file/path/ext/mime/semver present; Bogus platform-specific providers are not all implemented.                                                      |
| Phone                  | Partial | Locale-aware formats implemented               | Phone generation is locale-aware; Bogus-specific format-template API shape is not fully mirrored.                                                  |
| Database               | No      | Locale not required                            | Bogus database provider family not implemented.                                                                                                    |
| Vehicle                | No      | Locale not required                            | Vehicle provider family not implemented.                                                                                                           |
| Hacker                 | No      | Locale not required                            | Hacker phrase family not implemented.                                                                                                              |
| Images                 | No      | Locale not required                            | Image URL/data-uri families not implemented.                                                                                                       |
| Music/entertainment    | No      | Locale not required                            | Music/rant provider families not implemented.                                                                                                      |
| Architecture helpers   | Partial | Locale not required                            | Seeding, streams, weighted selection exist; rule sets, fluent fake profiles, and some Bogus-specific decorators are missing.                       |
| Locale breadth         | Partial | Core locales supported                         | kRandom provides locale-aware behavior for core generators, but not Bogus-scale 70+ locale breadth.                                                |

## Open Gaps Tagged for Execution

| Gap                                                 | Current Status | Priority Tag | Notes                                                               |
|-----------------------------------------------------|----------------|--------------|---------------------------------------------------------------------|
| Fluent `Faker<T>` + `ruleFor` model                 | No             | P0           | Main architectural parity gap versus Bogus UX.                      |
| Strict-mode/config validation for rule-based fakers | No             | P0           | Needed once fluent faker is introduced.                             |
| Populate existing instance / finish hook parity     | No             | P1           | Useful for migration ergonomics.                                    |
| Address/phone Bogus-style format helpers            | Partial        | P1           | Core data exists; API shape differs.                                |
| Finance account/transaction/crypto provider set     | Partial        | P1           | Core banking/card/currency covered; advanced finance still missing. |
| Database provider family                            | No             | P2           | Low direct usage in current framework scope.                        |
| Vehicle provider family                             | No             | P2           | Optional domain scope.                                              |
| Hacker/images/music families                        | No             | P2           | Optional domain scope.                                              |
| Locale breadth expansion beyond core locales        | Partial        | P1           | Quality/depth objective, not only API count.                        |

## Locale Support Baseline (Java)

Current Java generators already using locale-sensitive behavior include:

- User/identity: first/last/full/middle names, titles, suffixes, profiles, email/provider variants.
- Address/location: street/city/state/country/postcode, phone formats, country/calling-code behavior.
- Finance: currency and money formatting, bank-country/IBAN/BBAN behavior where applicable.
- Text/company: locale-aware text vocabularies and company buzz/catch phrase datasets.
- Date/time: locale-aware month name/format behaviors in relevant date generators.

Known locale limitation versus Bogus:

- Breadth is a curated core-locale set, not Bogus-scale locale catalog.

## Phase 0 Outcome

- Old status table was stale and not suitable as implementation truth.
- This baseline is now the source for Phase 1 implementation sequencing.
- Next execution step is Phase 1 from `docs/plans/bogus-java-plan.md`.
