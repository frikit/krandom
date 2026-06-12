---
layout: page
title: Migration from Faker and Chance
permalink: /guides/migration-from-faker-chance/
---

# Migration from Faker and Chance

Use this mapping to move existing Faker/Chance-style code to kRandom.

## API mapping

| Use case | Faker/Chance style | kRandom equivalent |
|---|---|---|
| Full name | `faker.name().fullName()` / `chance.name()` | `Generators.ofFullName().generate()` |
| Email | `faker.internet().emailAddress()` / `chance.email()` | `Generators.ofEmail().generate()` |
| City | `faker.address().city()` / `chance.city()` | `Generators.ofCity().generate()` |
| Country | `faker.address().country()` / `chance.country()` | `Generators.ofCountry().generate()` |
| Regex string | custom regex helper | `new RegexGenerator(pattern).generate()` |
| Object fixture | custom builder | `Generators.ofObject(MyType.class).generate()` |
| Deterministic runs | seeded faker/chance | `GeneratorConfig.builder().seed(...).build()` |

## Determinism pattern

```java
GeneratorConfig seeded = GeneratorConfig.builder()
        .seed(12345L)
        .locale(java.util.Locale.US)
        .build();

String first = new FullNameGenerator(seeded).generate();
String second = new FullNameGenerator(seeded).generate();
```

## Locale migration pattern

```java
GeneratorConfig fr = GeneratorConfig.builder()
        .locale(java.util.Locale.of("fr", "FR"))
        .build();

String city = new CityGenerator(fr).generate();
String country = new CountryGenerator(fr).generate();
```

## Template helpers (numerify / letterify / bothify)

Faker-style string templates are supported natively:

```java
String code = Generators.ofTemplate("ORD-####-??").generate(); // digits + letters
String digits = Generators.ofTemplate("###-##-####").generate();
String mixed = Generators.ofProviderTemplate("{firstname}-##").generate(); // provider token + digits
```

`#` expands to a digit, `?` to a letter; `ofProviderTemplate` additionally
resolves `{token}` placeholders (e.g. `{firstname}`, `{email}`, `{city}`)
through the `ProviderHub`. For one-off transformations,
`TemplateStringGenerator` also exposes `numerify(String)` and
`letterify(String)` directly.

DataFaker users: a dedicated guide exists —
[Migration from DataFaker]({{ '/guides/migration-from-datafaker/' | relative_url }}).

## Notes

- Prefer `Generators.of*()` factories for straightforward migration.
- Use `GeneratorConfig.toBuilder()` when deriving scenario-specific variants.
- Use `DataRegistryContext` when tests require isolated locale/provider registration.
