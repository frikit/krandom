# fake-rs Java Parity Execution Plan

## Scope

- Target: **Java parity only**.
- Out of scope for this plan: Kotlin/Scala implementations.
- Source of truth for gap status: `docs/feature-parity/fake-rs-parity.md`.
- Scope guard: prioritize practical generator parity, not Rust-specific trait/macro internals.

## Delivery Rules

- Implement in small vertical slices (one parity capability at a time).
- For each slice:
    1. Add/update tests first (behavior + edge cases).
    2. Implement Java core changes.
    3. Run `./scripts/pre_commit_check.sh`.
    4. Update parity doc rows + this checklist.
- Keep domain-oriented package structure and locale/file patterns consistent with current codebase.

## Definition of Done (per feature)

- fake-rs-equivalent behavior covered by tests.
- Seeded determinism preserved where applicable.
- Locale-aware behavior matches existing resource + registry patterns.
- Pre-commit passes with no coverage regression.
- Parity row updated to `Yes`, `Partial`, or explicit `Intentional skip`.

## Phase Plan

### Phase 0: Baseline Freeze (completed)

- [x] Freeze Java-only parity scope.
- [x] Use `fake-rs-parity.md` as canonical gap source.
- [x] Separate Rust-only architectural features from Java parity work.

### Phase 1: Core Data Foundations (P0) ✅

Goal: close high-value generator gaps used in most fixture workflows.

- [x] Identity/contact core:
    - [x] `NameWithTitle` (exists via `FullNameGenerator` options)
    - [x] `FreeEmail`, `SafeEmail`, `FreeEmailProvider`
    - [x] `Username`
    - [x] `Password` with range/length control
- [x] Address/location core:
    - [x] `CityName`, `StateName`, `StateAbbr`
    - [x] `StreetName`, `StreetSuffix`, `BuildingNumber`
    - [x] `PostCode`, `CountryName`, `CountryCode`
- [x] Text core:
    - [x] `Word`, `Words(range)`
    - [x] `Sentence(range)`, `Sentences(range)`
    - [x] `Paragraph(range)`, `Paragraphs(range)`

Acceptance:

- Core identity/address/text high-priority rows move to `Yes`.

### Phase 2: Network + Date-Time + Business Core (P0) ✅

Goal: close practical integration-test generators.

- [x] Networking:
    - [x] `IPv4`
    - [x] `IPv6`
    - [x] `IPv4 private/public/CIDR`
    - [x] `IPv6 CIDR`
    - [x] `IP` (single API that randomly returns v4 or v6)
    - [x] `MACAddress`
    - [x] `UserAgent`
    - [x] `DomainSuffix`, `DomainName` composition helper
- [x] Date/time:
    - [x] `DateTime`, `Date`, `Time`
    - [x] `DateTimeBefore`, `DateTimeAfter`, `DateTimeBetween`
    - [x] `Duration`
- [x] Business:
    - [x] `CompanyName`, `CompanySuffix` (suffix via boolean option)
    - [x] `Industry`, `Profession`
    - [x] `JobField`, `JobSeniority`, `JobType`

Acceptance:

- Phase-2 rows move to `Yes` or well-justified `Partial`.

### Phase 3: Finance/Code/Color/File Utilities (P1) ✅

Goal: complete medium-value fake-rs provider coverage.

- [x] Finance/codes:
    - [x] `Bic`, `Isin`
    - [x] `CurrencySymbol`
    - [x] `NumberWithFormat`, `Digit`
    - [x] `Isbn`, `Isbn10`, `Isbn13` parity check
- [x] Internet/utilities:
    - [x] `Slug`
    - [x] `HTTP status code` generator
    - [x] `UUID` parity validation (`v4` + v5/v7 present)
- [x] Color/file:
    - [x] `Hex/RGB/RGBA/HSL/HSLA` color output parity
    - [x] `FilePath`, `DirPath`, `FileName`, `FileExtension`, `MimeType`
    - [x] `Semver` support

Acceptance:

- Medium-priority generator rows move to `Yes`/`Partial`.

### Phase 4: Intentional Deviation Documentation (P1)

Goal: explicitly close Rust-specific parity gaps not meaningful for Java runtime.

- [ ] Mark Rust architecture features as intentional Java deviations:
    - [ ] `Fake` trait / `Dummy` trait
    - [ ] `#[derive(Dummy)]`, `fake!` macro
    - [ ] feature-flag-specific crate integrations as direct Rust concepts
- [ ] Provide Java-equivalent mapping notes (interfaces, builders, generator composition).
- [ ] Publish final implemented-vs-skipped parity summary.

Acceptance:

- Remaining `No` rows are either active backlog or intentional deviations with rationale.

## Execution Order (next steps)

1. Implement **Phase 1 / Core Data Foundations**.
2. Implement **Phase 2 / Network + Date-Time + Business Core**.
3. Implement **Phase 3 / Finance/Code/Color/File Utilities**.
4. Finalize **Phase 4 / Intentional Deviation Documentation**.

## Task Tracker

- Current step: **Phase 3 complete**
- Status: **In progress (Phase 4 remaining)**
- Next deliverable:
    - Finalize Phase 4 intentional-deviation documentation and close remaining non-Java-meaningful rows.
    - Run `./scripts/pre_commit_check.sh`.
    - Update parity rows and this checklist.
