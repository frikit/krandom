# Competitive Gap Tracker (Master)

Single source of truth for **where krandom stands against the JVM test-data
landscape and what we build next**. Consolidates the per-library parity docs in
this folder into one prioritized backlog. Update this file whenever a backlog
item ships or a competitor adds something material.

**Last updated:** 2026-06-20

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

## The "264 providers" reality

DataFaker documents **258** providers (Base / Entertainment / Food / Sport /
Videogame). **~150 are pop-culture/fandom/sport/food novelty**; only **~100 are
"Base" everyday data**, and krandom already covers most of the core Base set
(names, address, internet, finance, company, job, text, date/time, phone,
numbers/codes, color, hashing, UUID, IBAN/BIC, cards, templates, unique, schema
export). So the engineering gap is **locales + a curated ~20 Base providers +
ergonomics**, not "200 missing providers." Headline-count parity is handled by
the optional novelty module, not core.

## Cross-library status

| Competitor | Role vs krandom | Maintained? | Tracking doc | Net gap for krandom |
|---|---|---|---|---|
| **DataFaker** | Realism / breadth leader | ✅ active (v2.5.4, 2026) | `datafaker-parity.md` ✅ | locales 35→60+, ~20 Base providers, GraalVM native metadata, novelty (→ module) |
| **Instancio** | Object-graph leader, closest rival to `ObjectFaker` | ✅ active (v5.x) | ❌ **MISSING — create it** | Bean-Validation/JPA-aware generation, selector/Model ergonomics, sealed/generics parity advertising |
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
- [ ] **Locales 35 → 60+** (roadmap doc + per-locale data PRs; prioritize by user demand: add e.g. `pt_PT`, `en_CA`, `en_IN`, `zh_TW`, `es_MX`, `fr_CA`, `de_AT`, `de_CH`, plus broader EU/APAC)
- [ ] **Curated ~20 Base providers** (core-worthy, test-fixture value):
  - **Shipped:** Blood Type, Zodiac + Chinese Zodiac, NATO phonetic, Pronouns,
    Vehicle (VIN + make/model + plate), CNPJ, MBTI, Hobby, Programming Language;
    CPF exposed via `ofCpf()`.
  - **Locale coverage:** Zodiac, Chinese Zodiac, Pronoun, and Hobby are localized
    across **all 35 built-in locales** (per-locale resource files under
    `krandom/{zodiac,chinese_zodiac,pronouns,hobbies}/<locale>.txt`, English fallback).
    Non-English translations are best-effort and open to native-speaker review (edit
    the `.txt`; no code change). NATO/VIN/Programming Language/CNPJ/MBTI-codes stay
    locale-independent by design.
    Full DataFaker mapping: [`datafaker-providers-catalog.md`](./datafaker-providers-catalog.md).
  - [ ] Vehicle (VIN, make/model) ✅ · Weather · Passport · Driving License
  - [ ] Nationality / Language / Nation · Blood Type ✅ · Zodiac ✅ · MBTI ✅
  - [ ] NATO phonetic alphabet ✅ · Measurement/units · Pronouns ✅
  - [ ] Cloud resource names (AWS/Azure) · Computer/Device/OS · Programming Language ✅
  - [ ] University · Restaurant · Hobby ✅ · Financial Terms · CNPJ ✅ (BR company id)
  - **Design note — locale-frequency providers (blood type, …):** back them with per-locale resource files (`krandom/bloodtypes/<locale>.txt`, `TYPE WEIGHT` lines) + weighted selection, seeded only for locales that have a file (others fall back to `default.txt`), mirroring the Gender data-provider/registry pattern. Do **not** hardcode as enums — distributions differ by population/locale. (slice 1: `default`, `en_US`, `ja_JP` shipped; remaining 32 locales are backlog.)
- [ ] **Ergonomics parity with Instancio** (the real competitive pressure):
  - [ ] Bean-Validation/JPA-aware generation, first-class & documented (krandom has `BeanValidationSupport` — promote it)
  - [ ] Predicate/type **selectors** + reusable **Model/template** concept (beyond `ruleFor`/profiles)
  - [ ] Advertise + lock-in `record`/`sealed`/deep-generics parity with a test matrix
- [ ] **GraalVM native-image** reachability metadata (Quarkus/Micronaut test users)

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
- [`datafaker-parity.md`](./datafaker-parity.md) (narrative matrix) · [`datafaker-providers-catalog.md`](./datafaker-providers-catalog.md) (full 256-provider live mapping) · [`easy-random-parity.md`](./easy-random-parity.md)
- `instancio-parity.md` *(to create — Phase 1)*
- Non-JVM references: `faker-python-parity.md`, `chancejs-parity.md`, `fake-rs-parity.md`, `gofakeit-parity.md`, `feature-parity-bogus.md`, `mimesis-parity.md`, `k-random-reference-parity.md`
