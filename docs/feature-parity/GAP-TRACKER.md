# Competitive Gap Tracker (Master)

Single source of truth for **where krandom stands against the JVM test-data
landscape and what we build next**. Consolidates the per-library parity docs in
this folder into one prioritized backlog. Update this file whenever a backlog
item ships or a competitor adds something material.

**Last updated:** 2026-07-18

## Strategy decisions (locked)

1. **Core-first.** krandom core stays a lean, high-quality *generator powerhouse
   for unit tests*. We do **not** clone novelty/fandom/sport/food catalogs into
   core (jar bloat + per-franchise licensing/maintenance risk).
2. **Optional novelty module.** Pop-culture catalogs (Pokémon, Zelda, Star Wars,
   Beer, sports rosters, …) ship in a **separate opt-in artifact**
   (working name `krandom-novelty`) so the provider *count* can approach
   DataFaker's without bloating core. Nothing in core depends on it.
3. **Migration-doc program.** User-facing "switch from X → krandom" guides with
   honest pros/cons are a first-class adoption lever (see backlog Phase 4).
   Template: [`migration/k-random-to-krandom.md`](./k-random-to-krandom.md).

## Corrected facts (supersede stale per-doc claims)

| Claim | Stale value (in older docs) | Actual (2026-06-20) |
|---|---|---|
| Built-in locales | "10 locales" (`datafaker-parity.md`) | **35 locales** (`core/.../resources/krandom/cities/*_cities.txt`) |
| Per-generator locale coverage | "10 locales" in many cells | names/genders/suffixes/professions/titles expanded to **35** in v1.3–v1.4 |
| Generator count | "~50 in krandom" | grown well past that; see `Generators` public API |

> The per-row "10 locales" notes inside `datafaker-parity.md` predate the
> v1.3–v1.4 resource expansion and should be read as **≥10, now 35**.

## The 263-provider reality

DataFaker currently documents **263** providers across Base, Entertainment,
Food, Healthcare, Sport, and Videogame groups. A large share is
pop-culture/fandom/sport/food vocabulary; kRandom already covers most everyday
fixture domains
(names, address, internet, finance, company, job, text, date/time, phone,
numbers/codes, color, hashing, UUID, configured IBAN/BIC fixtures, cards, templates, unique, schema
export). The reusable engineering gaps—schema transformation/formats, local data packs, HTTP
fixtures, constrained text and sequences, University data, and experimental native-image
readiness—are now shipped. Locale breadth and long-tail curated vocabulary remain deliberate,
demand-led work, not "200 missing providers." Headline-count parity is neither a core goal nor a
release metric.

## Cross-library status

| Competitor | Role vs krandom | Maintained? | Tracking doc | Net gap for krandom |
|---|---|---|---|---|
| **DataFaker** | Realism / breadth leader | ✅ active (v2.7.0, 2026) | `datafaker-parity.md` | locale breadth, YAML/URL data-source compatibility, and long-tail domain catalogs |
| **Instancio** | Object-graph leader, closest rival to `ObjectFaker` | ✅ active (v5.x) | [`instancio-parity.md`](./instancio-parity.md) | JPA metadata, feeds, depth/group selectors, and generic type-token parity |
| **EasyRandom** | Object-graph, legacy | ⚠️ maintenance mode; v6 (records, Java 17) ~2026 | `easy-random-parity.md` ✅ | none major — `ObjectFaker` is a superset; capture migrators |
| **JavaFaker** | Dead predecessor of DataFaker | ❌ unmaintained since 2024 | (covered via DataFaker) | migration target only; nothing to adopt |

**Biggest tracking gap: there is no Instancio parity doc**, yet Instancio is the
competitor most aligned with krandom's `ObjectFaker` value prop. Creating it is
Phase 1 below.

## Prioritized backlog

Checkboxes are the live to-do list. Each shipped item: update this table + the
relevant per-library doc + `./scripts/pre_commit_check.sh`.

### Phase 1 — Fix tracking (current)
- [x] Master gap tracker (this file)
- [ ] Correct stale locale claims in `datafaker-parity.md` (10 → 35)
- [x] [`instancio-parity.md`](./instancio-parity.md) — object-graph feature matrix vs `ObjectFaker`
- [ ] Add a one-line "see GAP-TRACKER.md" pointer to each per-library parity doc

### Phase 2 — Close core gaps
- [x] **DataFaker v2 parity plan** — [`../development/v2-datafaker-parity-implementation-plan.md`](../development/v2-datafaker-parity-implementation-plan.md): documentation, fixture ergonomics, schema projections/formats, local data packs + University, native-image readiness
- [ ] **Native locale growth** (only after provenance and native-data quality gates; the current fallback variants are already honest about their tier)
- [ ] **Curated Base providers** (core-worthy, test-fixture value):
  - **Shipped:** Blood Type, Zodiac + Chinese Zodiac, NATO phonetic, Pronouns,
    Vehicle (VIN + make/model + plate), CNPJ, MBTI, Hobby, Programming Language,
    Measurement, Financial Terms, Nationality, Weather, Passport, Driving License,
    AWS, Azure, Computer/Device, Restaurant; CPF requires an explicit national-ID compatibility
    policy.
    University is delivered through the local data-pack work rather than a
    process-wide curated registry.
  - **Locale coverage:** Zodiac, Chinese Zodiac, Pronoun, Hobby, Measurement,
    Financial Terms, Nationality, Restaurant, and Weather are localized across
    **all 35 built-in locales** (per-locale resource files under
    `krandom/{zodiac,chinese_zodiac,pronouns,hobbies,measurement,finance,nationality,restaurant,weather}/<locale>.txt`,
    English fallback). Non-English translations are best-effort and open to
    native-speaker review (edit the `.txt`; no code change). Blood Type is
    locale-weighted (partial: `default`/`en_US`/`ja_JP`). NATO/VIN/Programming
    Language/MBTI-codes/AWS/Azure/Computer/Passport/Driving License stay
    locale-independent by design.
    Full DataFaker mapping: [`datafaker-providers-catalog.md`](./datafaker-providers-catalog.md).
  - [x] Vehicle (VIN, make/model) ✅ · Weather ✅ · Passport ✅ · Driving License ✅
  - [x] Nationality / Language / Nation ✅ · Blood Type ✅ · Zodiac ✅ · MBTI ✅
  - [x] NATO phonetic alphabet ✅ · Measurement/units ✅ · Pronouns ✅
  - [x] Cloud resource names (AWS/Azure) ✅ · Computer/Device/OS ✅ · Programming Language ✅
  - [x] University via verified local data packs · Restaurant ✅ · Hobby ✅ · Financial Terms ✅ · CNPJ ✅ (BR company id)
  - **Design note — locale-frequency providers (blood type, …):** back them with per-locale resource files (`krandom/bloodtypes/<locale>.txt`, `TYPE WEIGHT` lines) + weighted selection, seeded only for locales that have a file (others fall back to `default.txt`), mirroring the Gender data-provider/registry pattern. Do **not** hardcode as enums — distributions differ by population/locale. (slice 1: `default`, `en_US`, `ja_JP` shipped; remaining 32 locales are backlog.)
- [ ] **Ergonomics parity with Instancio** (the real competitive pressure):
  - [ ] Bean-Validation/JPA-aware generation, first-class & documented (krandom has `BeanValidationSupport` — promote it)
  - [ ] Predicate/type **selectors** + reusable **Model/template** concept (beyond `ruleFor`/profiles)
  - [ ] Advertise + lock-in `record`/`sealed`/deep-generics parity with a test matrix
- [x] **GraalVM native-image** reachability metadata and optional smoke verification (experimental; application models still require consumer metadata)

### Phase 3 — Optional novelty module (`krandom-novelty`)
- [ ] New opt-in Gradle module; zero core dependency
- [ ] Port high-demand fandom catalogs first (Star Wars, Pokémon, LOTR, GoT, …)
- [ ] Per-franchise licensing review before each catalog lands

### Phase 4 — Migration docs (adoption lever) ✅ done
- [x] [`../migration/from-javafaker.md`](../migration/from-javafaker.md) (dead → easiest capture)
- [x] [`../migration/from-datafaker.md`](../migration/from-datafaker.md) (uses `ofDataFakerExpression` adapter)
- [x] [`../migration/from-easyrandom.md`](../migration/from-easyrandom.md) (`nextObject` → `ObjectFaker`)
- [x] [`../migration/from-instancio.md`](../migration/from-instancio.md) (selectors/Models → `ruleFor`/profiles; honest gaps)

Each migration doc: **Why switch · What's equivalent (mapping table) · What
krandom does better · What it doesn't have yet (link backlog) · Copy-paste
examples.** Any competitor "pro" with no krandom answer becomes a Phase 2 item.

## Per-library parity docs (detail lives here)
- [`datafaker-parity.md`](./datafaker-parity.md) (narrative matrix) · [`datafaker-providers-catalog.md`](./datafaker-providers-catalog.md) (curated 263-provider capability mapping) · [`easy-random-parity.md`](./easy-random-parity.md)
- `instancio-parity.md` *(to create — Phase 1)*
- Non-JVM references: `faker-python-parity.md`, `chancejs-parity.md`, `fake-rs-parity.md`, `gofakeit-parity.md`, `feature-parity-bogus.md`, `mimesis-parity.md`, `k-random-reference-parity.md`
