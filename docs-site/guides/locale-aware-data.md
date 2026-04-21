---
layout: page
title: Locale-Aware Data
permalink: /guides/locale-aware-data/
---

# Locale-Aware Data

Most identity, address, text, and money generators support locale via `GeneratorConfig` or locale constructors.

## Example

```java
GeneratorConfig de = GeneratorConfig.builder()
        .locale(Locale.GERMANY)
        .seed(7L)
        .build();

String fullName = new FullNameGenerator(de).generate();
String city = new CityGenerator(de).generate();
String street = new StreetAddressGenerator(de).generate();
String money = new MoneyGenerator(de).generate();
String sentence = new SentenceGenerator(de).generate();
```

## Recommended pattern

- Build one `GeneratorConfig` per test fixture profile.
- Reuse it across generators to keep locale + seed consistent.

## Locale quality tiers

`SupportedLocale` now exposes built-in dataset quality metadata so callers can tell whether a locale is backed by native data or a fallback.

- `NATIVE_DATASET`: resource-backed identity/address data and profession data are native for that locale.
- `CURATED_FALLBACK_DATASET`: a provider uses a deliberate fallback dataset chosen to stay usable until native data exists.
- `ALIAS_FALLBACK_DATASET`: resource-backed providers reuse another supported locale's packaged dataset directly.

Example:

```java
SupportedLocale locale = SupportedLocale.fromLocale(Locale.of("nl", "NL")).orElseThrow();

LocaleDataQualityTier overall = locale.qualityTier();
LocaleDataQualityTier resourceTier = locale.resourceDataTier();
LocaleDataQualityTier professionTier = locale.professionDataTier();
Optional<SupportedLocale> resourceFallback = locale.resourceFallbackLocale();
```

Current built-in quality split:

- Native today for resource-backed identity/address data plus profession data: `en_US`, `en_GB`, `en_AU`, `fr_FR`, `de_DE`, `ja_JP`, `es_ES`, `it_IT`, `pt_BR`, `zh_CN`, `nl_NL`, `pl_PL`, `cs_CZ`, `ko_KR`, `ru_RU`, `tr_TR`, `sv_SE`, `nb_NO`, `ar_SA`, `hi_IN`
- Fallback-backed today: none in the built-in `SupportedLocale` catalog

Fallback tiers are still productized compatibility behavior, not hidden implementation details. They remain part of the metadata model for future or custom locale expansions even though the current built-in catalog is fully native-backed.

The previous native-upgrade priority list is now exhausted for the current built-in locale catalog.

## Locale contribution quality bar

Native locale additions are expected to ship real dataset coverage, not only registration hooks.

The current built-in quality gates enforce:

- minimum dataset sizes for names, cities, states, streets, titles, suffixes, and professions
- no blank values in validated built-in arrays
- duplicate ratio no worse than `5%`
- script sanity coverage for the locale's expected writing system

Contributor-facing details for resource layout and validation rules are documented in the repository-level `docs/locale-contribution-guide.md`.
