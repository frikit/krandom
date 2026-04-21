# Locale Contribution Guide

This guide defines the minimum bar for adding or upgrading a built-in locale in `krandom`.

## Required resource families

A native built-in locale is expected to provide:

- names:
  - `<locale>_first_male.txt`
  - `<locale>_first_female.txt`
  - `<locale>_last.txt`
- cities:
  - `<locale>_cities.txt`
- states / regions:
  - `<locale>_states.txt`
- streets:
  - `<locale>_street_names.txt`
  - `<locale>_street_types_short.txt`
  - `<locale>_street_types_long.txt`
  - `<locale>_secondary_units.txt`
- profession coverage in `BuiltInProfessionDataProvider`
- localized titles / suffixes where the locale is supported

Country data can come from built-in resource files or from JDK-localized country names, depending on the locale strategy already used in the codebase.

## Dataset minimums

The current locale coverage tests enforce these minimums:

- male first names: `>= 40`
- female first names: `>= 40`
- last names: `>= 40`
- professions: `>= 25`
- titles: `>= 4`
- suffixes: `>= 3`
- cities: `>= 70`
- states / regions: `>= 4`
- countries: `>= 150`
- street names: `>= 20`
- street types short: `>= 10`
- street types long: `>= 10`

## Data quality rules

Current tests also enforce:

- no blank entries in built-in datasets
- duplicate ratio no worse than `5%` for validated arrays
- script sanity coverage of at least `90%` for the locale's expected writing system across the main identity and address datasets

Expected script families are currently:

- Latin-script locales: `LATIN`
- `ru_RU`: `CYRILLIC`
- `ar_SA`: `ARABIC`
- `hi_IN`: `DEVANAGARI`
- `ko_KR`: `HANGUL`
- `ja_JP`: `HAN` / `HIRAGANA` / `KATAKANA`
- `zh_CN`: `HAN`

## Formatting rules

- Use UTF-8 text files.
- One logical value per line.
- Do not include blank lines.
- Keep values human-readable and production-plausible, not placeholder text.
- Prefer native-language spellings over transliterated fallbacks for native locales.
- Keep street/resource files aligned to the same locale prefix used in `SupportedLocale`.

## Code changes required for a new locale

1. Add or update the `SupportedLocale` entry.
2. Add matching resource files under `core/src/main/resources/krandom/`.
3. Update `BuiltInProfessionDataProvider` if the locale needs native profession data.
4. Update any locale-specific country-name handling if the locale should use JDK-localized country names.
5. Update locale docs in `docs-site/guides/locale-aware-data.md`.
6. Run `./scripts/pre_commit_check.sh`.

## Fallback policy

- Native datasets are preferred for built-in locales.
- Fallback tiers are still allowed in the metadata model for future or custom expansions.
- If a locale is intentionally fallback-backed, document the fallback source explicitly and keep the tier honest in `SupportedLocale`.
