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

## Custom locale bundles

For application-level locales or test-only overrides, you can register one locale pack instead of wiring every provider registry by hand.

```java
Locale locale = Locale.forLanguageTag("es-MX");

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

GeneratorConfig mx = GeneratorConfig.builder()
        .locale(locale)
        .registryContext(context)
        .seed(7L)
        .build();

String firstName = new FirstNameGenerator(mx).generate(Gender.MALE);
String city = new CityGenerator(mx).generate();
String street = new StreetAddressGenerator(mx).generate();
```

Register bundles through `DataRegistryContext.builder().registerLocaleData(...)` on the `GeneratorConfig` that consumes them. 2.0.0 removed process-wide registration (`registerGlobal()` and the per-registry `register(...)` methods): custom vocabulary is always scoped to a configuration and cannot leak between tests.

## Registering a single vocabulary

A bundle covers the person and address vocabularies. Any vocabulary, including the ones a bundle
does not cover, can be registered on its own under its `Vocabulary` key; the provider implements
that vocabulary's data interface:

```java
DataRegistryContext context = DataRegistryContext.builder()
    .register(Vocabulary.WEATHER, new RainyWeather(Locale.UK)) // implements WeatherDataProvider
    .build();
GeneratorConfig config = GeneratorConfig.builder().registryContext(context).build();

WeatherDataProvider weather = context.provider(Vocabulary.WEATHER, Locale.UK);
Set<String> keys = context.registeredKeys(Vocabulary.WEATHER); // "en_GB", "en" and the bundled keys
```

`Vocabulary.all()` lists the 42 built-in vocabularies. Lookups resolve the exact
`language_COUNTRY` key first, then the bundled data for that key, then the language entry (a
regional registration also serves its language) and finally the bundled language data;
`isolated()` contexts skip the bundled data. `Vocabulary.NATIONAL_ID` and `Vocabulary.POSTAL_PLACE`
are the exceptions: for a locale with a country, the third step is the registration for that country
whatever its language, and the bundled data is looked up by country too, because identity-number
formats and postal geography are national; language-only locales use the language entry.

Generators you write yourself can define their own vocabulary with
`Vocabulary.custom("colour", ColourProvider.class)`, where `ColourProvider` extends
`LocaleDataProvider`; keep the key in a constant, because keys compare by identity.

## Coherent addresses

`AddressInfoGenerator`, `StreetAddressGenerator.generateFullAddress()`, and, for a locale with a
country, the object engine's address members (`city`, `state`, `postalCode`) take an address's city,
region, and postal code from one place of `Vocabulary.POSTAL_PLACE`, so the three agree: an `en_US`
address in Grand Rapids is in Michigan, with a Grand Rapids ZIP code. The bundled places:

| Locale | Places | Region | Postal codes | Source and licence |
| --- | --- | --- | --- | --- |
| `en_US` | 252 cities, in all 50 states and DC | the state and its USPS code | up to ten ZIP codes of the city | U.S. Census Bureau, public domain |
| `en_GB` | 111 cities in Great Britain | England, Scotland, or Wales | up to ten postcodes in the city, one per postcode district | Ordnance Survey OS Open Names, Open Government Licence v3.0 |
| `de_DE` | 115 cities, in all 16 Länder | the Land | the postal code of the city's administrative seat | Destatis Gemeindeverzeichnis, reuse with attribution |
| `fr_FR` | 107 communes, in 14 régions | the région | up to ten postal codes of the commune | La Poste and Insee, Licence Ouverte 2.0 |

The header of each table in `krandom/postal_places/` records its sources, licence, retrieval date, and
checksums, and how its rows were chosen. The tables have these limits:

- `en_GB` has no places in Northern Ireland, because OS Open Names covers Great Britain only, so its
  Northern Ireland cities, such as Belfast, appear only in `CityGenerator`. A postcode is one that OS
  Open Names places in the city; OS Open Names does not record Royal Mail's post towns, so a postcode's
  post town can be a neighbouring town.
- `de_DE` gives every address in one city the same postal code, that of the city's administrative seat,
  because the Gemeindeverzeichnis has one for each municipality. For some cities it is the city
  administration's own large-recipient code rather than a delivery district's, such as `80313` for
  München.

Postal geography is national, so the other languages of a country with places use them: `es_US`
addresses are in the United States, with the US state names, and `en_DE` addresses are in Germany. A
locale without a country uses the places of its language and names their country: `Locale.ENGLISH`
gives United States addresses, `Locale.GERMAN` German ones, and `Locale.FRENCH` French ones.

Every other locale composes an address from independent draws: a city from its city data, a region from
its state data, and a postal code in its country's format, which need not agree. This covers the other 31
native locales, for which no table of cities and postal codes from an openly licensed source is bundled
yet, and the 15 fallback variants. A fallback variant's cities and regions come from another
country's data, while its postal codes and country are its own: an `en_CA` address has a US city and state
and a Canadian postal code. `StreetAddressGenerator.generateFullAddress()` names the locale's country in
every locale. `Krandom.city()` and `Krandom.postalCode()` draw separately, and so do separate
`address.city`, `address.state`, and `address.postal_code` schema columns; use `Generators.ofAddressInfo`
or the `address.address_info` provider for an address whose parts belong together.

For a city and a postal code that belong together without the rest of an address, `Krandom.postalLocality()`
and `Generators.ofPostalLocality(...)` (since 3.2.0) give a `PostalLocality`: a city, its region and region
code, and one of its postal codes, all from one place.

```java
Krandom fake = Krandom.seeded("pins", Locale.US);
PostalLocality place = fake.postalLocality();  // Pasadena, Texas, TX, 77505
String line = place.city() + ", " + place.regionCode() + " " + place.postalCode();
```

It draws from a stream of its own, so `city()` and `postalCode()` keep their values. It needs postal places,
so a locale without them, such as `de_AT` or `ja_JP`, throws `UnsupportedOperationException` unless you
register places for its country, as below.

Places you register take precedence over the bundled ones for the same locale key, and serve every
language of their country:

```java
DataRegistryContext context = DataRegistryContext.builder()
        .register(Vocabulary.POSTAL_PLACE, new PostalPlaceDataProvider() {
            @Override
            public Locale getLocale() {
                return Locale.forLanguageTag("es-MX");
            }

            @Override
            public List<PostalPlace> getPlaces() {
                return List.of(new PostalPlace("Guadalajara", "Jalisco", "JAL", List.of("44100", "44160")));
            }
        })
        .build();
```

`LocaleDataBundle.Builder.postalPlaces(...)` registers places with a bundle. Cities or states that you
register without places do not replace the bundled places of a country that has them: a context with its
own `en_US` cities still gives `AddressInfoGenerator` the bundled US places, while `CityGenerator` gives
your cities. Register places for the locale as well to use your own.

## German street layout

Street addresses put the house number first in every locale by default, so a German street reads
`7389 Schloss Anger`. German writes the street first, with its type as part of the name:
`Schlossanger 7389`. Since 3.2.0, `GeneratorConfig.Builder.germanStreetLayout(true)` opts in to that
layout. It is off by default in 3.x, so seeded output does not change unless you ask for it, and it
becomes the default in 4.0.

```java
GeneratorConfig de = GeneratorConfig.builder()
        .locale(Locale.GERMANY)
        .seed(42L)
        .germanStreetLayout(true)
        .build();

String street = new StreetAddressGenerator(de).generate();  // "Schlossanger 7389"
```

- **Locales.** It covers every locale whose language is German: `de`, `de_DE`, `de_AT`, `de_CH`, and any
  other `de_*`. Their street data, the built-in data and data you register for them alike, holds the
  first part of a street name (`Haupt`, `Rosen`) and the street types (`Strasse`, `Weg`, `Str.`) to join
  to it. Every other locale keeps the house number first, with the option on or off.
- **Form.** The name and the type become one word, the type starting in lower case:
  `Rosenstrasse 9180`, `Hauptstr. 12`. The built-in data spells `Strasse` and `Muehl` without `ß` or
  umlauts, as before.
- **What follows it.** `StreetAddressGenerator.generate()` and `generate(boolean)`, the street of
  `generateFullAddress()`, and `AddressInfo.street()` and `address()`, and so `Krandom.streetAddress()`,
  the `address.street_address` and `address.address_info` schema columns, and the object engine's
  street fields. `AddressInfo`'s `streetNumber`, `streetName` and `streetSuffix` keep their values, and
  the rest of a full address keeps its order (`…, München, Bayern 80313, Deutschland`).
- **Draws.** The same draws are made in the same order with the option on or off, so it changes how the
  parts are laid out and nothing else.
- **Recipes.** A recipe records `setting.address.german-street-layout=true` while it is on, so it replays
  the same streets; a recipe without the setting replays with it off.

Other countries write the street first too, among them the Netherlands, Belgium, the Nordic countries,
Poland, the Czech Republic, Italy and Spain. The option does not cover them, because their street data is
shaped differently: whole street names, such as the Dutch `Dorpsstraat` and the Swedish `Storgatan`, or a
street type that goes before the name, such as the Italian `Via` of `Via Roma 12`. Each needs a layout of
its own.

## Locale quality tiers

`SupportedLocale` now exposes built-in dataset quality metadata so callers can tell whether a locale is backed by native data or a fallback.

- `NATIVE_DATASET`: resource-backed identity/address data and profession data are native for that locale.
- `CURATED_FALLBACK_DATASET`: a provider uses a deliberate fallback dataset chosen to stay usable until native data exists.
- `ALIAS_FALLBACK_DATASET`: resource-backed providers reuse another supported locale's packaged dataset directly.

Example:

```java
SupportedLocale locale = SupportedLocale.fromLocale(Locale.forLanguageTag("nl-NL")).orElseThrow();

LocaleDataQualityTier overall = locale.qualityTier();
LocaleDataQualityTier resourceTier = locale.resourceDataTier();
LocaleDataQualityTier professionTier = locale.professionDataTier();
Optional<SupportedLocale> resourceFallback = locale.resourceFallbackLocale();
```

Current built-in quality split:

- 35 native locale datasets: the original 20 plus `da_DK`, `fi_FI`, `hu_HU`, `ro_RO`, `sk_SK`, `uk_UA`, `bg_BG`, `hr_HR`, `el_GR`, `th_TH`, `vi_VN`, `id_ID`, `ms_MY`, `he_IL`, and `ca_ES`
- 15 curated fallback variants: `en_CA`, `en_NZ`, `en_IE`, `en_IN`, `en_ZA`, `fr_CA`, `fr_BE`, `fr_CH`, `de_AT`, `de_CH`, `es_MX`, `es_AR`, `pt_PT`, `nl_BE`, and `zh_TW`
- 50 supported locale variants in total

Fallback tiers are productized compatibility behavior, not hidden implementation details. Each fallback variant exposes its canonical resource locale through `resourceFallbackLocale()`.

The 35 native datasets satisfy the current resource coverage gates. The 15 regional variants remain explicitly fallback-backed until equivalent native datasets are contributed.

## Locale contribution quality bar

Native locale additions are expected to ship real dataset coverage, not only registration hooks.

The current built-in quality gates enforce:

- minimum dataset sizes for names, cities, states, streets, titles, suffixes, and professions
- no blank values in validated built-in arrays
- duplicate ratio no worse than `5%`
- script sanity coverage for the locale's expected writing system

Contributor-facing details for built-in resource layout, validation rules, and application-level custom locale bundles are documented in the repository-level `docs/locale-contribution-guide.md`.
