# Faker-Python Java Parity Execution Plan

## Scope

- Target: **Java parity only**.
- Out of scope for this plan: Kotlin/Scala implementations.
- Source of truth for gaps: `docs/feature-parity/faker-python-parity.md`.
- Scope guard: prioritize high-value Faker-Python providers first; keep locale-aware behavior where appropriate.

## Delivery Rules

- Implement in small vertical slices (one parity capability at a time).
- For each slice:
    1. Add/update tests first (behavior + edge cases).
    2. Implement Java core changes.
    3. Run `./scripts/pre_commit_check.sh`.
    4. Update parity doc rows + this checklist.
- Keep package structure domain-oriented and reusable.

## Definition of Done (per feature)

- Faker-Python-equivalent behavior covered by tests.
- Seeded determinism preserved where applicable.
- Locale-aware behavior implemented where provider semantics depend on locale.
- Pre-commit passes and coverage does not regress.
- Parity row updated to `Yes`, `Partial`, or explicit `Intentional skip`.

## Phase Plan

### Phase 0: Baseline Alignment (P0) ✅

Goal: establish a reliable starting point before implementation.

- [x] Re-audit `faker-python-parity.md` against current Java code.
- [x] Mark rows as `Yes/Partial/No` with explicit API/behavior differences.
- [x] Tag each missing row as `P0`, `P1`, or `Intentional skip candidate`.

Acceptance:

- Parity doc reflects current real status and phase priorities.

### Phase 1: Core P0 Coverage (Identity/Address/Internet/Date) (P0) ✅

Goal: close high-frequency fixture generators used across most test suites.

- [x] Identity:
    - [x] full-name and gender variants (`name`, `name_female`, `name_male`)
    - [x] `prefix`, `suffix` parity refinements
    - [x] `profile` / `simple_profile`
- [x] Address/location:
    - [x] `address`, `street_name`, `street_address`, `building_number`, `secondary_address`
    - [x] `city`, `state`, `state_abbr`, `postcode/zipcode`
    - [x] `country_code` variants (`alpha-2`, `alpha-3`) + current-country helpers
- [x] Internet/network:
    - [x] `safe_email`, `free_email`, `company_email`, `free_email_domain`
    - [x] `domain_name`, `hostname`, `tld`, `url`, `uri`
    - [x] `http_method`, `http_status_code`
    - [x] `uuid4`
- [x] Date/time:
    - [x] `date`, `date_between`, `past_date`, `future_date`
    - [x] `date_time`, `date_time_between`, `past_datetime`, `future_datetime`
    - [x] `iso8601`, `unix_time`, `timezone`

Acceptance:

- Highest-priority cross-domain rows move to `Yes` or well-justified `Partial`.

### Phase 2: P1 Domain Expansion (Finance/Company/Text/Phone/Codes) (P1)

Goal: close medium-priority parity for business and content fixtures.

- [x] Finance:
    - [x] credit-card suite (`number`, `expiry`, `security_code`, `provider`, `full`)
    - [x] banking (`iban`, `swift/bic`, `aba`)
    - [x] currency suite (`currency`, `currency_code`, `currency_name`, `currency_symbol`, `pricetag`)
- [x] Company/job:
    - [x] `company`, `company_suffix`, `bs`, `catch_phrase`
    - [x] `job`
- [x] Text/lorem:
    - [x] `word/words`, `sentence/sentences`, `paragraph/paragraphs`
    - [x] `text/texts`, uniqueness and variable-length options
- [x] Phone and numeric/code providers:
    - [x] `phone_number`, `country_calling_code`, `msisdn`
    - [x] `pydecimal`, weighted/nullable booleans
    - [x] EAN family, hash suite (`sha1`, `sha256`, etc.)

Acceptance:

- Medium-priority rows move to `Yes/Partial` with clear behavior notes.

### Phase 3: Locale Depth and Provider Options (P1)

Goal: raise parity quality by matching Faker-Python option-rich APIs and locale behavior.

- [ ] Add locale-aware variants for providers that should differ by language/region.
- [ ] Add option-bag APIs where Faker-Python supports configurable output patterns.
- [ ] Add deterministic-seed tests across multiple locales for new providers.
- [ ] Normalize resource/file-backed datasets for maintainable locale expansion.

Acceptance:

- Locale-sensitive rows show explicit multi-locale support and deterministic tests.

### Phase 4: Intentional Scope Decisions + Final Parity Pass (P2)

Goal: explicitly close low-ROI gaps and publish final parity state.

- [ ] Mark low-value or non-core Faker-Python providers as intentional skips with rationale.
- [ ] Document non-1:1 API mappings where Java design intentionally differs.
- [ ] Publish final implemented-vs-skipped parity summary in `faker-python-parity.md`.

Acceptance:

- Remaining `No` rows are either active backlog or intentional skip with rationale.

## Execution Order (next steps)

1. Execute **Phase 0 / Baseline Alignment**.
2. Execute **Phase 1 / Core P0 Coverage**.
3. Execute **Phase 2 / P1 Domain Expansion**.
4. Execute **Phase 3 / Locale Depth and Provider Options**.
5. Finalize **Phase 4 / Intentional Scope Decisions + Final Parity Pass**.

## Task Tracker

- Current step: **Phase 2 complete**
- Status: **In progress (Phase 3+ remaining)**
- Next deliverable:
    - Execute Phase 3 locale-depth improvements (option bags + broader locale determinism tests).
    - Run `./scripts/pre_commit_check.sh` after each feature slice.
