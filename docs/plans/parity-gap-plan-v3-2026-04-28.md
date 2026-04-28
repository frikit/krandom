# Parity Gap Plan v3 — 2026-04-28 (source-verified)

## Context

Supersedes `parity-gap-plan-v2-2026-04-27.md`, which itself superseded
`unified-parity-gap-plan-2026-04-27.md`. Several parity docs have been
linter-cleaned since v2: `datafaker-parity.md` and `fake-rs-parity.md` now
include audited-status sections that close most stale rows.

This v3 was produced by:

1. Counting open rows per parity doc (`grep -c "❌ No\b"`).
2. Splitting by priority (`HIGH` / `MEDIUM` / `LOW`) and excluding
   `❌ No (intentional)` rows.
3. **Grepping `core/src/main/java`** to confirm each remaining claim is a real
   gap, not stale doc text.

## Historical open-row census (before the 2026-04-28 Java parity sweep)

| Doc                       | Total `❌ No` | `(intentional)` | HIGH genuine | MEDIUM genuine | LOW genuine |
| ------------------------- | ------------- | --------------- | ------------ | -------------- | ----------- |
| `chancejs-parity.md`      | 4             | 0               | 0            | 1              | 0           |
| `datafaker-parity.md`     | 193           | 32              | 0            | 0              | 161         |
| `easy-random-parity.md`   | 51            | 0               | 0            | 13             | 38          |
| `fake-rs-parity.md`       | 94            | 16              | 0            | 0              | (audited section: 2 missing) |
| `faker-python-parity.md`  | 50            | 9               | 0            | 0              | (mostly LOW + 1 deferred design row) |
| `feature-parity-bogus.md` | 0             | 0               | 0            | 0              | 0           |
| `gofakeit-parity.md`      | 0             | 0               | 0            | 0              | 0           |
| `mimesis-parity.md`       | 0             | 0               | 0            | 0              | 0           |

**Bogus / GoFakeit / Mimesis are fully closed.** Chancejs has one MEDIUM row.
Datafaker had zero HIGH/MEDIUM but a 161-row LOW long-tail. Easy-random had
13 MEDIUM rows (all SPI/registry — pre-declined). The 2026-04-28 Java sweep
closed the Java-library ambiguity by marking stale rows as implemented and
collapsing DataFaker long-tail catalogs into explicit intentional-skip
families.

## Source-verified real code gaps (the entire actionable list)

| # | Gap                            | Verified by                                                     | Plans referencing it                          | Effort |
| - | ------------------------------ | --------------------------------------------------------------- | --------------------------------------------- | ------ |
| 1 | `UniqueGenerator.reset()`      | Implemented in `selection/UniqueGenerator.java`                 | faker-python `unique.clear()`                 | Done   |
| 2 | Template registry `{name}` lookup | Implemented as `ProviderTemplateGenerator` backed by `FieldLookup` | gofakeit `Generate("{firstname}")`, faker-python `pystr_format` | Done   |
| 3 | `hexify` provider method       | Implemented in `TextFormatProvider`                             | faker-python `hexify("^^^^")`                 | Done   |
| 4 | `GeohashGenerator`             | Implemented under `generator/location`                          | fake-rs audited section line 63               | Done   |
| 5 | `ConstantGenerator`            | Implemented under `generator/base`                              | easy-random `ConstantRandomizer`              | Done   |
| 6 | `CalendarGenerator`            | Implemented under `generator/datetime`                          | easy-random `java.util.Calendar`              | Done   |
| 7 | `RandomLocaleGenerator`        | Implemented under `generator/locale`                            | easy-random "Locale" MEDIUM row               | Done   |

**Six are S, one is M.** Total ≤ 1 day of focused work.

## Already-decided skips (sweep, no code)

### S1. License plate (cross-doc consistency)

faker-python already marks `license_plate()` intentional skip ("Automotive
identifiers are long-tail domain data"). fake-rs audit row 95 used to be an
active gap. → Flip fake-rs row to `❌ No (intentional)` with the same
rationale.

### S2. Datafaker LOW long-tail (161 rows)

The unified-plan Step 7 sweep covered Singapore FIN, race, family, postal
niches, flag emoji. Remaining LOW rows fall into already-defined skip
families:

| Family                            | Rationale (from existing skips)                            |
| --------------------------------- | ---------------------------------------------------------- |
| Sports (60+: F1, NBA, NFL, etc.)  | Long-tail vocabulary, low enterprise fixture value         |
| Vehicle (VIN, makes, models)      | Long-tail automotive identifiers                           |
| Medical (ICD-10, diseases)        | Long-tail clinical vocabulary                              |
| Anime / Pop culture / Games       | Novelty data, bloats jar size                              |
| Food / Drink / Animals            | Long-tail vocabulary                                       |
| Stock tickers (NASDAQ/NYSE/etc.)  | Long-tail finance vocabulary                               |
| Mythology / Quotes / Yoda / Chuck | Novelty data                                               |
| Compass direction / Emoji / Image | Niche / novelty                                            |
| UUID v3                           | SHA-1 v5 already covered; v3 MD5-based is rarely needed    |

A few LOW rows are actually **stale** (already implemented):

- Buzzword → `CompanyBuzzwordGenerator` exists
- Catch phrase → `CompanyCatchPhraseGenerator` exists
- HTTP method → `HttpMethodGenerator` exists

Sweep these to `✅ Yes` with code pointers; sweep the rest to
`❌ No (intentional)` with the relevant family rationale.

### S3. Easy-random SPI/registry (13 MEDIUM rows)

Already declined in `easy-random-java-plan.md` Phase 5/6 (startup cost,
ambiguous resolution, over-engineering for krandom scale). Sweep
`ObjectFactory`, `ExclusionPolicy`, `RandomizerRegistry`, `RandomizerProvider`,
`Registry priority`, `Built-in registries`, duplicate `RandomizerRegistry`,
duplicate `ExclusionPolicy`, duplicate `ObjectFactory` to
`❌ No (intentional)` with the existing rationale. **Easy-random `Password`
row is also stale — `PasswordGenerator` exists; flip to `✅ Yes`.**

### S4. faker-python LOW long-tail (~30 rows)

Same family-rationale sweep: passport gender/MRZ/owner (under "passport
intentional skip"), browser-specific UAs (chrome/firefox/safari/etc. —
`UserAgentGenerator` already returns realistic UA strings), city
prefix/suffix (already done elsewhere), localized EAN, century, AM/PM.

## Recommended execution order

| Step | Tier | Effort | Code? | Closes                                                 |
| ---- | ---- | ------ | ----- | ------------------------------------------------------ |
| 1    | A    | S      | No    | Done — S1 license-plate consistency + stale buzzword/catchphrase/http-method/Password rows |
| 2    | B1   | S      | Yes   | `UniqueGenerator.reset()`                              |
| 3    | B3   | S      | Yes   | `hexify` provider method                               |
| 4    | B4   | S      | Yes   | `GeohashGenerator`                                     |
| 5    | B5   | S      | Yes   | `ConstantGenerator`                                    |
| 6    | B6   | S      | Yes   | `CalendarGenerator`                                    |
| 7    | B7   | S      | Yes   | `RandomLocaleGenerator`                                |
| 8    | B2   | M      | Yes   | Template registry with `{name}` lookup                 |
| 9    | S2   | S      | No    | Done — Datafaker LOW intentional-skip sweep            |
| 10   | S3   | S      | No    | Done — Easy-random SPI/registry intentional-skip sweep |
| 11   | S4   | S      | No    | Done — faker-python LOW intentional-skip sweep         |
| —    | C1   | —      | —     | Multi-locale mixing — already deferred-with-rationale  |
| —    | D1   | —      | —     | Locale breadth — continuous, no milestone              |

**Steps 2–8 are the only code-touching work. Steps 1 + 9–11 are doc-only.**

## Deferred with documented rationale (no work)

### C1. Multi-locale mixing (`Faker(['en_US', 'de_DE'])`)

Already documented in `faker-python-parity.md` line 381 as
`❌ No / Deferred — DESIGN — Single-locale GeneratorConfig is intentional
until a concrete use case defines the contract`. No further action.

### Chancejs "Option combinations" (1 MEDIUM)

Composability is achieved per-generator via `GeneratorConfig` and builder
chains. Document this row as `✅ Yes (via builder pattern)` with code
pointer to one representative builder.

## Continuous community work (no milestone)

- **D1.** Locale breadth 20 → 50+ via `locale-contribution-guide.md`
- **D2.** Niche locale national IDs (Singapore FIN, Poland PESEL, Portugal NIF,
  Mexico SSN, ZA SSN) — accept community PRs against `NationalIdProvider` SPI
- **D3.** Long-tail domain vocabularies — accept community PRs only if
  scoped + maintained by submitter

## Verification

After each Tier B step:

- `./scripts/pre_commit_check.sh` must pass (per-package coverage ≥ 99.9%).
- Add a row pointing to the new generator in the relevant parity doc(s).
- Mark the step done in this file.

## Status tracker

- [x] Step 1 — Tier A: license-plate consistency + flip stale rows (buzzword/catchphrase/http-method/easy-random Password)
- [x] Step 2 — `UniqueGenerator.reset()`
- [x] Step 3 — `hexify` provider method
- [x] Step 4 — `GeohashGenerator`
- [x] Step 5 — `ConstantGenerator`
- [x] Step 6 — `CalendarGenerator`
- [x] Step 7 — `RandomLocaleGenerator`
- [x] Step 8 — Template registry with `{name}` lookup
- [x] Step 9 — Datafaker LOW intentional-skip sweep
- [x] Step 10 — Easy-random SPI/registry intentional-skip sweep
- [x] Step 11 — faker-python LOW intentional-skip sweep
- [—] C1 — multi-locale mixing: already deferred
- [—] D1/D2/D3 — continuous community work, no milestone

## Why v3 is shorter than v2

v2's first pass treated parity-doc rows as ground truth and produced a 6-item
"real gap" list. Source verification reduced that to 3, then this v3 audit
expanded it to **7 confirmed real gaps** by combining the v2 verification
list with the audited sections that recently landed in `fake-rs-parity.md`
and `datafaker-parity.md`. The lesson stands: parity-doc rows are a starting
point, not ground truth — every claim must be grepped against
`core/src/main/java` before landing in a plan.
