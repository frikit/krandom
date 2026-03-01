# GoFakeit Java Parity Execution Plan

## Scope

- Target: **Java parity only**.
- Out of scope for this plan: Kotlin/Scala implementations.
- Source of truth for gaps: `docs/feature-parity/gofakeit-parity.md`.
- Scope guard: prioritize high-value fixture APIs first; defer low-ROI novelty datasets until core parity is stable.

## Delivery Rules

- Implement in small vertical slices (one provider family at a time).
- For each slice:
    1. Add/update tests first (behavior + edge cases).
    2. Implement Java core changes.
    3. Run `./scripts/pre_commit_check.sh`.
    4. Update parity rows + this checklist.
- Keep package structure domain-oriented and reusable (no utility dumping).
- Use locale-aware behavior where provider semantics depend on locale.
- Prefer file-backed datasets for large vocabularies.

## Definition of Done (per feature)

- GoFakeit-equivalent behavior covered by tests.
- Seeded determinism preserved where applicable.
- Locale-aware behavior implemented where relevant.
- Pre-commit passes and coverage does not regress.
- Parity row updated to `Yes`, `Partial`, `No`, or `Intentional skip`.

## Phase Plan

### Phase 0: Baseline Re-Audit & Prioritization (P0) ✅

Goal: normalize stale parity rows before implementation.

- [x] Re-audit `gofakeit-parity.md` against current Java code (many rows are likely stale).
- [x] Reclassify rows to `Yes/Partial/No` with API-shape notes.
- [x] Retag missing rows as `P0`, `P1`, `P2`, or `Intentional skip candidate`.
- [x] Identify features already implemented under different names (alias candidates).

Acceptance:

- Parity doc is reliable and implementation-ready.

### Phase 1: Core P0 Fixture Surface (P0)

Goal: close high-frequency APIs used in most test data workflows.

- [x] Identity/contact:
    - [x] name-prefix/suffix alignment, gender/age aliases, password-policy parity.
    - [x] phone aliases (`phone`, `phoneFormatted`) and contact-info composite.
- [x] Address/location:
    - [x] street component helpers (number/prefix/suffix/unit).
    - [x] city/state/state-abbr/zip/country/country-abbr aliases.
    - [x] address composite struct/value object parity.
- [x] Internet/network:
    - [x] URL/domain/domain-suffix/url-slug aliases and API normalization.
    - [x] MAC/port/http method/http status/http status simple/user-agent aliases.
- [x] Date/time/text:
    - [x] date/date-range/future/past helpers and component aliases.
    - [x] word/sentence/paragraph/lorem helpers with option parity.
- [x] Pattern generation:
    - [x] numerify/lexify/bothify/asciify API surface using existing template primitives.

Acceptance:

- Highest-usage GoFakeit rows move to `Yes` or justified `Partial`.

### Phase 2: Finance + Company + Commerce (P1)

Goal: close business-heavy parity used in integration and seeding.

- [ ] Finance:
    - [ ] credit-card object and convenience aliases (number/type/exp/cvv).
    - [ ] ACH account/routing and bank-name/type helpers.
    - [ ] currency object + short/long/symbol + price(min,max).
    - [ ] bitcoin/ein/cusip/isin parity decisions and implementations.
- [ ] Company/job:
    - [ ] company/companySuffix aliases and job object parity.
    - [ ] job descriptor/level helpers and mappings to current generators.
- [ ] Product/commerce:
    - [ ] product name/description/category/material/UPC/ISBN helpers.

Acceptance:

- Finance/company/commerce rows move to `Yes/Partial` with explicit mapping notes.

### Phase 3: Object Tagging + Templates + Structured Output (P1)

Goal: implement GoFakeit’s main architectural differentiators.

- [ ] Struct-tag parity layer over object generation:
    - [ ] `fake:` tag mappings to generator lookups.
    - [ ] `skip`/`-` support.
    - [ ] size directives (`fakesize`) for collections.
    - [ ] regex/random-string tag patterns.
- [ ] Template/generate parity:
    - [ ] `{function}` style string generation compatibility helpers.
    - [ ] function registry hooks for custom lookup registration.
- [ ] Structured output:
    - [ ] CSV/JSON/XML/SQL emitters with field definitions and row-count options.

Acceptance:

- Core GoFakeit “DX” features (tags/templates/structured output) are available in Java form.

### Phase 4: Locale/Data Depth + Utilities (P2)

Goal: expand breadth where it improves real fixture quality.

- [ ] Locale breadth expansion for person/address/company/text providers.
- [ ] File-backed dataset normalization for scalable locale additions.
- [ ] Utility parity where useful:
    - [ ] random-int/string helpers, weighted picks, shuffle helpers.
    - [ ] language/timezone/color option refinements.
- [ ] Add deterministic multi-locale seeded tests for new providers.

Acceptance:

- Locale-sensitive providers are consistent and maintainable.

### Phase 5: Intentional Skip Decisions + Final Pass (P2)

Goal: close parity tracking with explicit scope decisions.

- [ ] Mark low-ROI provider families as intentional skips with rationale:
    - [ ] Minecraft/celebrity/emoji/animal/food/entertainment long-tail datasets.
    - [ ] image byte generators and other heavy non-core fixtures (unless explicitly requested).
- [ ] Document non-1:1 API mappings where Java design intentionally differs.
- [ ] Publish final implemented-vs-skipped summary in `gofakeit-parity.md`.

Acceptance:

- Remaining gaps are explicitly classified as backlog or intentional skip.

## Recommended Execution Order

1. Execute **Phase 0 / Baseline Re-Audit**.
2. Execute **Phase 1 / Core P0 Fixture Surface**.
3. Execute **Phase 2 / Finance + Company + Commerce**.
4. Execute **Phase 3 / Object Tagging + Templates + Structured Output**.
5. Execute **Phase 4 / Locale/Data Depth + Utilities**.
6. Finalize **Phase 5 / Intentional Scope Decisions + Final Pass**.

## Task Tracker

- Current step: **Phase 2 pending**
- Status: **In progress**
- Next deliverable:
    - Implement Phase 2 finance/company/commerce parity slice.
    - Run `./scripts/pre_commit_check.sh` after each vertical slice.
