# kRandom bundled locale resources

This directory holds the curated, locale-aware datasets that back kRandom's
fake-data generators (names, cities, addresses, countries, …). Everything here
is loaded from the classpath at runtime by the `*ResourceLoader` classes in
`io.github.frikit.krandom.generator.*`.

## File conventions

All datasets follow the same rules, enforced per folder by their loader:

- **Encoding:** UTF-8. Entries are written in the locale's own language and
  script (native script for Arabic, Hebrew, Greek, Cyrillic, Thai, Hangul,
  Devanagari, CJK, …; correct diacritics for Latin scripts).
- **One entry per line.** Blank lines and lines beginning with `#` (header
  comments) are ignored by the loaders.
- **No duplicates.** Within any single file every entry must be byte-exact
  unique.
- **Naming:** `<language>_<COUNTRY>_<dataset>.txt`, e.g. `de_DE_cities.txt`,
  using the BCP-47 `xx_YY` locale form that maps to `java.util.Locale`.

New locales and entries are **curated from real data** — never bulk-generated.
See [`docs/locale-contribution-guide.md`](../../../../../docs/locale-contribution-guide.md)
and `CONTRIBUTING.md` for the contribution workflow and quality bar.

## Locale coverage and goal

`java.util.Locale.getAvailableLocales()` on **JDK 21** returns **1,069**
locales — 811 distinct language+country combinations, across 226 languages and
252 regions. kRandom does not aim to mechanically cover all of them; it curates
the **most widely used** locales with real, verified data.

**Goal:** grow to at least the **100 most popular locales** (covering ~90% of
real-world use cases), and hundreds over time. Current coverage by folder:

| Folder | Datasets per locale | Files | Locales supported | Per-entry rule | Status |
|:---|:---|---:|---:|:---|:---|
| `cities/` | 1 | 35 | 35 | ≥ 100 unique real cities | all ≥ 100 (107–247) |
| `names/` | 3 subfolders | 105 | 35 | ≥ 100 unique per file | all ≥ 100 |
| `states/` | 1 | 35 | 35 | all first-level subdivisions + codes | ok |
| `streets/` | 4 | 140 | 35 | complete, paired, unique (see below) | ok |
| `countries/` | 1 | 35 | 35 | full world set (195), localized | all 35 at 195 |
| `text/` | n/a | 1 set | non-locale | complete standard set | ok |

All five locale-keyed folders (cities, names, states, streets, countries) now
cover the same 35-locale core set. The next step is growing that core toward
the 100-most-popular-locales goal.

## Folders

### `cities/`

One file per locale, `<locale>_cities.txt`, one city name per line.

- **Content:** real cities and towns of the locale's country, in the local
  language/script.
- **Rule:** **at least 100 unique** entries per locale. (Today every file is
  ≥ 100; the smallest is `fr_FR` at 107, the largest `en_US` at 247.)
- **Loader:** `CityResourceLoader` / `BuiltInCityDataProvider`.

### `names/`

Split into three subfolders by name type, each holding one file per locale
named `<locale>.txt` (one name per line):

- `names/first_male/<locale>.txt` — common male given names
- `names/first_female/<locale>.txt` — common female given names
- `names/last/<locale>.txt` — common family names

- **Content:** popular given names (by gender) and family names for the locale.
- **Rule:** **target ≥ 100 unique** entries per file, drawn from genuinely
  popular names. *Current files hold 39–74 entries and are below target — this
  folder is the priority for the next curation pass.*
- **Loader:** the name registries under `generator.user` (e.g.
  `FirstNameDataRegistry`, `LastNameDataRegistry`).

### `states/`

One file per locale, `<locale>_states.txt`.

- **Content / format:** the country's first-level administrative subdivisions
  (states, provinces, regions, …) as **alternating lines** — the subdivision
  name followed by its official short code, e.g.

  ```text
  Alabama
  AL
  Alaska
  AK
  ```

- **Rule:** cover **all** first-level subdivisions of the country, each paired
  with its official abbreviation/code; names and codes both unique.
- **Loader:** `StateDataRegistry`.

### `streets/`

Four files per locale, one per address component:

| File | Content | Rule |
|:---|:---|:---|
| `<locale>_street_types_long.txt` | full street-type words (`Street`, `Avenue`, `Boulevard`) | the **complete** real set for the locale, unique |
| `<locale>_street_types_short.txt` | their abbreviations (`St`, `Ave`, `Blvd`) | **paired 1:1** with the long list (same count/order), unique |
| `<locale>_street_names.txt` | representative real street base-names | unique, representative sample |
| `<locale>_secondary_units.txt` | sub-address designators (apartment, suite, floor, …) | all common designators for the locale, unique |

- **Rule (street types):** the long and short type lists must be **exhaustive**
  for the locale and **correspond one-to-one** (every long form has its short
  form at the same position), with no duplicates in either list.
- **Loader:** `StreetAddressDataRegistry`.

### `countries/`

One file per locale, `<locale>_countries.txt`, one country name per line.

- **Content:** the names of the world's countries, localized into that
  language (e.g. `de_DE` → `Ägypten`, `Albanien`).
- **Rule:** the **full set of world countries** (currently 195 per file),
  localized and unique.
- **Coverage:** all 35 core locales (195 countries each).

### `text/`

Non-locale-keyed auxiliary text data. Currently a `phonetics/` subfolder
holding phonetic-alphabet data (used by text/spelling generators). Unlike the
folders above, files here are not named per `xx_YY` locale.

- **Rule:** each dataset is the **complete standard set** for what it
  represents (e.g. the full phonetic alphabet), unique.

## Adding or expanding a locale

1. Use the existing `xx_YY_<dataset>.txt` naming and UTF-8, one-entry-per-line
   format; keep the optional leading `#` header comment.
2. Use **real, verifiable** data in the locale's native language/script; do not
   invent or machine-translate entries.
3. Satisfy the folder's rule above (e.g. ≥ 100 unique cities; exhaustive,
   paired street types).
4. Ensure no duplicate lines (byte-exact), then run
   `./scripts/pre_commit_check.sh` so the registry coverage checks pass.
