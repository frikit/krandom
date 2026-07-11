# Locale Contribution Guide

This guide defines the minimum bar for adding or upgrading a built-in locale in `krandom`.

## Application-level custom locale bundles

Not every locale addition should become a built-in dataset. For test-only or application-specific locales, prefer `LocaleDataBundle` with a config-scoped `DataRegistryContext`:

```java
Locale locale = Locale.of("es", "MX");

LocaleDataBundle bundle = LocaleDataBundle.builder(locale)
        .firstNames(new String[]{"Mateo"}, new String[]{"Sofía"})
        .lastNames("Hernández", "Ramírez")
        .genderLabels("Hombre", "Mujer")
        .cities("Ciudad de México", "Guadalajara")
        .states(new String[]{"Jalisco", "Ciudad de México"}, new String[]{"JAL", "CDMX"})
        .countries("México")
        .streetAddress(new String[]{"Juárez"}, new String[]{"Av"}, new String[]{"Avenida"})
        .build();

DataRegistryContext context = DataRegistryContext.builder()
        .isolated()
        .registerLocaleData(bundle)
        .build();
```

That path keeps overrides local to one `GeneratorConfig` / runtime. v2 removed process-wide registration (`registerGlobal()` and the per-registry `register(...)` methods), so configuration-scoped registration is the only way to supply custom locale data.

## Required resource families

A native built-in locale is expected to provide:

- names:
  - `names/first_male/<locale>.txt`
  - `names/first_female/<locale>.txt`
  - `names/last/<locale>.txt`
- cities:
  - `<locale>_cities.txt`
- states / regions:
  - `<locale>_states.txt`
- streets:
  - `streets/street_names/<locale>.txt`
  - `streets/street_types_short/<locale>.txt`
  - `streets/street_types_long/<locale>.txt`
  - `streets/secondary_units/<locale>.txt`
- professions:
  - `professions/<locale>.txt` (one profession per line, ordered most-common-first)
- titles:
  - `titles/<locale>.txt` (one honorific title per line)
- localized suffixes where the locale is supported

Country data can come from built-in resource files or from JDK-localized country names, depending on the locale strategy already used in the codebase.

Profession lists are ordered most-common-first: ranked generation derives its weights from list position (the first entry is weighted highest, the last gets weight `1`), so no separate weights table is maintained.

## Dataset minimums

The current locale coverage tests enforce these minimums:

- male first names: `>= 100`
- female first names: `>= 100`
- last names: `>= 100`
- professions: `>= 40`
- titles: `>= 4`
- suffixes: `>= 3`
- cities: `>= 100`
- states / regions: `>= 4`
- countries: `>= 195`
- street names: `>= 26`
- street types short: `>= 15`
- street types long: `>= 15` (and the long/short lists must have the same length — paired by line index)

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
3. Add `professions/<locale>.txt` and `titles/<locale>.txt` resource files for the locale.
4. Update any locale-specific country-name handling if the locale should use JDK-localized country names.
5. Update locale docs in `docs-site/guides/locale-aware-data.md`.
6. Run `./scripts/pre_commit_check.sh`.

## Fallback policy

- Native datasets are preferred for built-in locales.
- Fallback tiers are still allowed in the metadata model for future or custom expansions.
- If a locale is intentionally fallback-backed, document the fallback source explicitly and keep the tier honest in `SupportedLocale`.
