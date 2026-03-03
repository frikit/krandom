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
