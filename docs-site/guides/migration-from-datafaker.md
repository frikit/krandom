---
layout: page
title: Migration from DataFaker
permalink: /guides/migration-from-datafaker/
---

# Migration from DataFaker

Use this mapping to move DataFaker (or JavaFaker) code to kRandom. This is
feature-parity guidance, not source-compatible import replacement. JavaFaker
users: the same mappings apply — JavaFaker is unmaintained and DataFaker is
its successor, so both columns read the same.

## Install

```kotlin
dependencies {
    implementation("io.github.frikit:krandom-core:2.1.0")
}
```

## API mapping

| DataFaker | kRandom equivalent |
|---|---|
| `new Faker()` | no instance needed — `Generators.of*()` statics or domain namespaces |
| `new Faker(Locale.GERMANY)` | `Generators.ofCity(Locale.GERMANY)` per call, or `GeneratorConfig.builder().locale(...)` shared |
| `new Faker(new Random(42))` | `GeneratorConfig.builder().seed(42L).build()` |
| `faker.name().fullName()` | `Generators.ofFullName().generate()` |
| `faker.name().firstName()` | `Generators.person().firstName().generate()` |
| `faker.name().lastName()` | `Generators.person().lastName().generate()` |
| `faker.internet().emailAddress()` | `Generators.ofEmail().generate()` |
| `faker.internet().url()` | `Generators.ofUrl().generate()` |
| `faker.internet().ipV4Address()` | `Generators.ofIPv4().generate()` |
| `faker.internet().username()` | `Generators.ofUsername().generate()` |
| `faker.address().city()` | `Generators.ofCity().generate()` |
| `faker.address().country()` | `Generators.ofCountry().generate()` |
| `faker.address().streetAddress()` | `Generators.ofStreetAddress().generate()` |
| `faker.address().zipCode()` | `Generators.ofPostalCode().generate()` |
| `faker.phoneNumber().phoneNumber()` | `Generators.ofPhoneNumber().generate()` |
| `faker.company().name()` | `Generators.ofCompanyName().generate()` |
| `faker.finance().iban()` | `new IbanGenerator(bankingConfig).generate()` |
| `faker.finance().creditCard()` | `Generators.ofCreditCard().generate()` |
| `faker.number().numberBetween(1, 100)` | `Generators.ofInt(1, 100).generate()` |
| `faker.regexify("[a-z]{8}")` | `new RegexGenerator("[a-z]{8}").generate()` |
| `faker.university().name()` | load a local data pack, then `Generators.ofUniversity(config).name()` |

Domain namespaces (`Generators.person()`, `location()`, `finance()`,
`network()`, `text()`, `commerce()`, `identifier()`, `datetime()`) mirror
DataFaker's provider-object style if you prefer fluent discovery over flat
statics. Each accepts an optional `GeneratorConfig`.

In 2.0.0, banking identifiers are fail-closed. Define `bankingConfig` only for an isolated
compatibility fixture; it does not make an IBAN safe to submit anywhere:

```java
GeneratorConfig bankingConfig = GeneratorConfig.builder()
        .bankingSafetyPolicy(BankingSafetyPolicy.REALISTIC_UNCLASSIFIED)
        .build();
```

## Template helpers

DataFaker's string templates map directly:

| DataFaker | kRandom equivalent |
|---|---|
| `faker.numerify("###-##")` | `Generators.ofTemplate("###-##").generate()` |
| `faker.letterify("????")` | `Generators.ofTemplate("????").generate()` |
| `faker.bothify("##??")` | `Generators.ofTemplate("##??").generate()` |
| `faker.expression("#{Name.firstName}")` | `Generators.ofDataFakerExpression("#{Name.firstName}").generate()` (same syntax) or `Generators.ofProviderTemplate("{firstname}").generate()` (native syntax) |

`ofProviderTemplate` resolves `{token}` placeholders through the
`ProviderHub` (tokens like `{firstname}`, `{email}`, `{city}`) and expands
`#`/`?` placeholders in the same string — see the
[Schema and Provider Hub]({{ '/guides/schema-and-provider-hub/' | relative_url }})
guide for the full token list.

## Bulk and structured output

Both libraries have schema-based bulk output. kRandom's v2 schema API streams generated rows to
JSONL, JSON arrays, CSV, XML, SQL, YAML, and TOML, and exports JSON Schema. `SchemaProjection<T>`
also transforms existing object sequences through those same formats without materializing a map
batch. For generated row-style data use `Field` + `Schema`; for existing objects use
`SchemaProjection<T>` — see
[Schema and Provider Hub]({{ '/guides/schema-and-provider-hub/' | relative_url }}).

## Seeding differences

DataFaker shares one `Random` across the whole faker instance. kRandom seeds
per generator or per shared `GeneratorConfig`; string seeds are supported via
a stable derivation (`GeneratorConfig.deriveSeed`, `fnv1a64-v1`). Tests
should assert on shape, not exact values — see
[VERSIONING.md](https://github.com/frikit/krandom/blob/main/VERSIONING.md)
for the seed-stability policy.

## Honest gaps

- **Long-tail novelty providers** (sports teams, movies, food, animals,
  pop-culture catalogs): not shipped. If your tests depend on these, keep
  DataFaker for those calls or open an issue — domain packs are
  community-contributable.
- **Locales**: kRandom supports 50 locale variants (35 native datasets and 15 curated fallbacks);
  DataFaker's current README lists 70 locale tags, including generic and novelty variants.
  Check yours against the [Locale-Aware Data]({{ '/guides/locale-aware-data/' | relative_url }}) guide.
- **Local data packs**: DataFaker accepts YAML files and URLs. kRandom accepts a versioned,
  SHA-256-verified local University CSV pack with declared source and license; runtime network
  loading is intentionally excluded. See [Local Data Packs]({{ '/guides/local-data-packs/' | relative_url }}).
- **`#{...}` expression syntax**: the common tokens work directly via
  `Generators.ofDataFakerExpression(...)` (case-insensitive, camelCase or
  snake_case; unknown tokens fail fast with the supported list). Exotic
  provider tokens beyond that list still need manual mapping.
