# DataFaker Feature Parity Analysis

> **2.0.0 API note:** This is a historical parity analysis, not a current API tutorial. The
> canonical selection helpers are `pick`, `pickSet`, `shuffle`, and `unique`; national-ID fixtures
> require an explicit `GeneratorConfig` with `NationalIdSafetyPolicy.REALISTIC_UNCLASSIFIED`.
> Use the [generator catalog](../../docs-site/generator-catalog.md) for current runnable APIs.

Where this analysis uses `nationalIdConfig(locale)`, it denotes a `GeneratorConfig` built with the
given locale and `.nationalIdSafetyPolicy(NationalIdSafetyPolicy.REALISTIC_UNCLASSIFIED)` for an
isolated fixture.

## Library Overview

- **Name**: DataFaker
- **Language**: Java
- **Version Analyzed**: Latest (2024+)
- **GitHub**: https://github.com/datafaker-net/datafaker
- **License**: Apache 2.0
- **Key Strength**: 263 documented providers, extensive localization, schema transformations

**Last Updated**: 2026-07-18 — current implementation work is tracked in
[`../development/v2-datafaker-parity-implementation-plan.md`](../development/v2-datafaker-parity-implementation-plan.md).
DataFaker's current catalog lists **263 providers** and its README lists **70
locale tags**. The older per-row figures in this historical matrix are not a
release contract; use the current generator catalog and migration guide for
supported APIs.

## Java Execution Plan

- Active plan: `docs/plans/datafaker-java-plan.md`
- Current scope: Java parity only (Kotlin/Scala deferred)
- Delivery model: one parity slice at a time with tests + `./scripts/pre_commit_check.sh`

## Executive Summary

DataFaker is the broadest Java faker catalog, with 263 documented providers and
70 advertised locale tags. Its reusable advantages are schema transformations
(including existing-object projection and YAML/TOML), declarative data loading,
HTTP response fixtures, constrained text generation, variable-length sequences,
and experimental GraalVM native-image metadata. It is a fork of JavaFaker with
significant enhancements.

## Java Parity Contract — 2026-04-28

This document now treats parity as **100% of krandom's scoped Java generator-library contract**, not a literal clone of every DataFaker novelty catalog. DataFaker currently documents 263 providers across base, entertainment, food, healthcare, sport, and videogame groups; cloning every long-tail vocabulary would bloat krandom without improving the Java-first generator experience.

Covered in the 100% contract:

- Core realistic data: identity, address, network, finance, commerce, company, job, text, date/time, phone, number/code, and color.
- Java library ergonomics: locale/config overloads, deterministic seeds, schema JSONL/JSON/CSV/XML/SQL/YAML/TOML/JSON Schema, existing-object projection, custom provider registration, object generation, and DataFaker/JavaFaker-style template helpers.
- Explicit non-goals: novelty/entertainment catalogs, sports/team rosters, medical vocabularies, food/drink catalogs, stock ticker catalogs, fingerprint data, live LLM-model catalogs, and return-shape clones that are language-specific to another library. Local configuration-scoped data packs are in scope; runtime URL loading is not.

---

## Feature Categories

### 1. PERSONAL IDENTITY

| Feature                     | DataFaker Support                            | krandom Status | Implementation Priority | Notes                                                  |
|-----------------------------|----------------------------------------------|----------------|-------------------------|--------------------------------------------------------|
| **Name Generation**         |
| Full name                   | ✅ `name()`, `fullName()`, `nameWithMiddle()` | ✅ Yes          | ✓ DONE                  | `FullNameGenerator` plus `NameOptions` covers composite naming |
| First name                  | ✅ `firstName()`                              | ✅ Yes          | ✓ DONE                  |                                                        |
| Last name                   | ✅ `lastName()`                               | ✅ Yes          | ✓ DONE                  |                                                        |
| Gender-specific first names | ✅ `femaleFirstName()`, `maleFirstName()`     | ✅ Yes          | ✓ DONE                  | `gen.generate(Gender.MALE/FEMALE)` — 10 locales        |
| Name prefix                 | ✅ `prefix()` (Mr., Mrs., Dr.)                | ✅ Yes          | ✓ DONE                  | `TitleGenerator` — 10 locales, extensible              |
| Name suffix                 | ✅ `suffix()` (Jr., Sr., III)                 | ✅ Yes          | ✓ DONE                  | `SuffixGenerator` — 10 locales                         |
| Title                       | ✅ `title()` (professional titles)            | ✅ Yes          | ✓ DONE                  | `TitleGenerator` for honorifics                        |
| **ID Numbers**              |
| SSN (US)                    | ✅ `ssnValid()`                               | ✅ Yes          | ✓ DONE                  | `Generators.ofNationalId(nationalIdConfig(Locale.US))` — area 666 excluded   |
| Singapore FIN/UIN           | ✅ `singaporeanFin()`, `singaporeanUin()`     | No (intentional) | SKIP                | Niche locale ID outside 10-locale `NationalIdGenerator` scope; add via community PR if requested |
| Poland PESEL                | ✅ `peselNumber()`                            | No (intentional) | SKIP                | Niche locale ID outside `NationalIdGenerator` scope; add via community PR if requested |
| China SSN                   | ✅ `validZhCNSsn()`                           | ✅ Yes          | ✓ DONE                  | `Generators.ofNationalId(nationalIdConfig(Locale.CHINA))` — 18-char ISO 7064 |
| Portugal NIF                | ✅ `validPtNif()`                             | No (intentional) | SKIP                | Niche locale ID outside `NationalIdGenerator` scope; add via community PR if requested |
| Mexico SSN                  | ✅ `validEsMXSsn()`                           | No (intentional) | SKIP                | Niche locale ID outside `NationalIdGenerator` scope; add via community PR if requested |
| South Africa SSN            | ✅ `validEnZaSsn()`                           | No (intentional) | SKIP                | Niche locale ID outside `NationalIdGenerator` scope; add via community PR if requested |
| **Gender & Demographics**   |
| Gender types                | ✅ `types()`, `binaryTypes()`                 | ✅ Yes          | ✓ DONE                  | `GenderGenerator` — 10 locales, locale-aware labels    |
| Race                        | ✅ `race()`                                   | No (intentional) | SKIP                | Sensitive demographic data — outside scope                          |
| Education level             | ✅ `educationalAttainment()`                  | ✅ Yes          | ✓ DONE                  | `EducationalAttainmentGenerator`                       |
| Marital status              | ✅ `maritalStatus()`                          | ✅ Yes          | ✓ DONE                  | `MaritalStatusGenerator`                               |
| **Relationships**           |
| Direct relationships        | ✅ `direct()` (mother, father)                | No (intentional) | SKIP                | Niche vocabulary — low fixture value                                |
| Extended family             | ✅ `extended()`, `inLaw()`                    | No (intentional) | SKIP                | Niche vocabulary — low fixture value                                |
| **Other Personal**          |
| Passport number             | ✅ `valid()`                                  | No (intentional) | SKIP                | Travel-document datasets deferred until requested      |
| Driver's license            | ✅ `drivingLicense()`                         | No (intentional) | SKIP                | Locale-specific licensing data deferred until requested |

### 2. ADDRESS & LOCATION

| Feature                  | DataFaker Support                                     | krandom Status | Implementation Priority | Notes                                                          |
|--------------------------|-------------------------------------------------------|----------------|-------------------------|----------------------------------------------------------------|
| **Street Address**       |
| Street name              | ✅ `streetName()`                                      | ✅ Yes          | ✓ DONE                  | `StreetAddressGenerator.generateStreetName()`                  |
| Street address           | ✅ `streetAddress()`                                   | ✅ Yes          | ✓ DONE                  | `StreetAddressGenerator.generate()`                            |
| Street number            | ✅ `streetAddressNumber()`, `buildingNumber()`         | ✅ Yes          | ✓ DONE                  | `generateStreetAddressNumber()` and `generateBuildingNumber()` |
| Secondary address        | ✅ `secondaryAddress()` (Apt, Suite)                   | ✅ Yes          | ✓ DONE                  | `StreetAddressGenerator.generateSecondaryAddress()`            |
| Street suffix/prefix     | ✅ `streetSuffix()`, `streetPrefix()`                  | No (intentional) | SKIP                | Internal building blocks — `StreetAddressGenerator` already emits full addresses |
| **City & State**         |
| City name                | ✅ `city()`, `cityName()`                              | ✅ Yes          | ✓ DONE                  | `CityGenerator` — 10 locales, locale-specific cities           |
| City prefix/suffix       | ✅ `cityPrefix()`, `citySuffix()`                      | No (intentional) | SKIP                | Internal building blocks — `CityGenerator` already produces full city names |
| State                    | ✅ `state()`                                           | ✅ Yes          | ✓ DONE                  | `StateGenerator.generate()` — 10 locales                       |
| State abbreviation       | ✅ `stateAbbr()`                                       | ✅ Yes          | ✓ DONE                  | `StateGenerator.generate(true)` — CA, TX, NSW, etc.            |
| **Postal Codes**         |
| ZIP code                 | ✅ `zipCode()`                                         | ✅ Yes          | ✓ DONE                  | `PostalCodeGenerator` — 10 locales                             |
| ZIP+4                    | ✅ `zipCodePlus4()`                                    | ✅ Yes          | ✓ DONE                  | `PostalCodeGenerator.generate(true)` → "90210-1234"            |
| ZIP by state             | ✅ `zipCodeByState()`                                  | No (intentional) | SKIP                | State-specific ZIP mapping is curated geo data, not core generation |
| County by ZIP            | ✅ `countyByZipCode()`                                 | No (intentional) | SKIP                | Curated ZIP-to-county mapping is geographic reference data, not core generation |
| Postcode (generic)       | ✅ `postcode()`                                        | ✅ Yes          | ✓ DONE                  | 10 locale-specific formats (JP: "100-0001", DE: "10115")       |
| Eircode (Ireland)        | ✅ `eircode()`                                         | No (intentional) | SKIP                | Niche locale postal format — add via community PR if requested              |
| Mailbox                  | ✅ `mailBox()` (PO Box)                                | No (intentional) | SKIP                | Niche format — low fixture value                                            |
| **Country & Nation**     |
| Country name             | ✅ `country()`                                         | ✅ Yes          | ✓ DONE                  | `CountryGenerator` — 195 countries, 10 locales                 |
| Country code             | ✅ `countryCode()`, `countryCode2()`, `countryCode3()` | ✅ Yes          | ✓ DONE                  | `CountryGenerator.generateCode(...)`                           |
| Capital city             | ✅ `capital()`                                         | No (intentional) | SKIP                | Curated geographic facts deferred until requested              |
| Currency                 | ✅ `currency()`, `currencyCode()`                      | ✅ Yes          | ✓ DONE                  | Already in Money generator                                     |
| Flag emoji               | ✅ `flag()`                                            | No (intentional) | SKIP                | Novelty Unicode rendering — low fixture value                               |
| Nationality              | ✅ `nationality()`                                     | No (intentional) | SKIP                | Demonym vocabulary is long-tail locale data                    |
| Language                 | ✅ `language()`, `isoLanguage()`                       | No (intentional) | SKIP                | Language-name vocabulary deferred until requested              |
| **Coordinates**          |
| Latitude                 | ✅ `latitude()`                                        | ✅ Yes          | ✓ DONE                  | `CoordinatesGenerator.generateLatitude()` — locale-bounded     |
| Longitude                | ✅ `longitude()`                                       | ✅ Yes          | ✓ DONE                  | `CoordinatesGenerator.generateLongitude()` — locale-bounded    |
| Lat/Lon pair             | ✅ `latLon()`, `lonLat()`                              | ✅ Yes          | ✓ DONE                  | `CoordinatesGenerator.generate()` → "35.12,-80.12"             |
| **Direction & Location** |
| Compass direction        | ✅ `word()`, `abbreviation()`, `azimuth()`             | No (intentional) | SKIP                | Niche compass vocabulary is outside the core fixture scope     |
| Time zone                | ✅ `timeZone()`                                        | ✅ Yes          | ✓ DONE                  | `CountryGenerator.generateTimezone()` / `TimezoneGenerator`    |
| Full address             | ✅ `fullAddress()`                                     | ✅ Yes          | ✓ DONE                  | `StreetAddressGenerator.generateFullAddress()`                 |

### 3. INTERNET & NETWORKING

| Feature           | DataFaker Support                    | krandom Status | Implementation Priority | Notes                                                 |
|-------------------|--------------------------------------|----------------|-------------------------|-------------------------------------------------------|
| **Email**         |
| Email address     | ✅ `emailAddress()`                   | ✅ Yes          | ✓ DONE                  | `EmailGenerator` — 5 formats, 12 domains, 10 locales  |
| Safe email        | ✅ `safeEmailAddress()` (example.com) | ✅ Yes          | ✓ DONE                  | `gen.generate("example.com")` — custom domain support |
| Email subject     | ✅ `emailSubject()`                   | No (intentional) | SKIP                | Niche mail-fixture vocabulary; compose from text generators if needed |
| **Domain & URLs** |
| Domain name       | ✅ `domainName()`                     | ✅ Yes          | ✓ DONE                  | `DomainGenerator` — 12 popular TLDs, 10 locale TLDs   |
| Domain word       | ✅ `domainWord()`                     | ✅ Yes          | ✓ DONE                  | Embedded in `DomainGenerator` (1-2 word combos)       |
| Domain suffix     | ✅ `domainSuffix()` (.com, .org)      | ✅ Yes          | ✓ DONE                  | `DomainGenerator.getTLD()` → "com", "io", "de"        |
| URL               | ✅ `url()`                            | ✅ Yes          | ✓ DONE                  | `URLGenerator` — 5 protocols, path, query params      |
| Web domain        | ✅ `webdomain()`                      | ✅ Yes          | ✓ DONE                  | Covered by `DomainGenerator`                          |
| Slug              | ✅ `slug()`                           | ✅ Yes          | ✓ DONE                  | `SlugGenerator.generate()` / `slugify(...)`           |
| **IP Addresses**  |
| IPv4              | ✅ `ipV4Address()`                    | ✅ Yes          | ✓ DONE                  | Already implemented                                   |
| IPv4 private      | ✅ `privateIpV4Address()`             | ✅ Yes          | ✓ DONE                  | `IPv4Generator.generatePrivate()`                     |
| IPv4 public       | ✅ `publicIpV4Address()`              | ✅ Yes          | ✓ DONE                  | `IPv4Generator.generatePublic()`                      |
| IPv4 CIDR         | ✅ `ipV4Cidr()`                       | ✅ Yes          | ✓ DONE                  | `IPv4Generator.generateCidr()`                        |
| IPv6              | ✅ `ipV6Address()`                    | ✅ Yes          | ✓ DONE                  | Already implemented                                   |
| IPv6 CIDR         | ✅ `ipV6Cidr()`                       | ✅ Yes          | ✓ DONE                  | `IPv6Generator.generateCidr()`                        |
| **Network**       |
| MAC address       | ✅ `macAddress()`                     | ✅ Yes          | ✓ DONE                  | `MacAddressGenerator`                                 |
| Port              | ✅ `port()`                           | ✅ Yes          | ✓ DONE                  | `PortGenerator`                                       |
| HTTP method       | ✅ `httpMethod()` (GET, POST)         | ✅ Yes          | ✓ DONE                  | `HttpMethodGenerator`                                 |
| HTTP fixture      | ✅ coherent request/response fixtures | ✅ Yes          | ✓ DONE                  | `HttpFixtureGenerator` supplies method, version, status, headers, content type, encoding, user agent, and compatible body |
| **Identifiers**   |
| UUID v3           | ✅ `uuidv3()`                         | No (intentional) | SKIP                | MD5 namespace UUIDs are rarely useful; v4, v5, and v7 are supported |
| UUID v4           | ✅ `uuid()`, `uuidv4()`               | ✅ Yes          | ✓ DONE                  | `UUIDGenerator.generateV4()` — RFC 4122 §4.4          |
| UUID v7           | ✅ `uuidv7()`                         | ✅ Yes          | ✓ DONE                  | `UUIDGenerator.generateV7()`                          |
| **User Agents**   |
| User agent        | ✅ `userAgent()`                      | ✅ Yes          | ✓ DONE                  | `UserAgentGenerator.generate()`                       |
| Bot user agent    | ✅ `botUserAgent()`                   | ✅ Yes          | ✓ DONE                  | `UserAgentGenerator.generateBot()`                    |
| **Other**         |
| Image URL         | ✅ `image()`                          | No (intentional) | SKIP                | Placeholder/image data generation is outside core Java faker scope |

### 4. FINANCE & COMMERCE

| Feature               | DataFaker Support                                                                             | krandom Status | Implementation Priority | Notes                                                        |
|-----------------------|-----------------------------------------------------------------------------------------------|----------------|-------------------------|--------------------------------------------------------------|
| **Credit Cards**      |
| Credit card number    | ✅ 10 types, Luhn-valid                                                                        | ✅ Yes          | ✓ DONE                  | `CreditCardGenerator` — Luhn-valid, 6 card types             |
| Card types            | ✅ VISA, MASTERCARD, DISCOVER, AMEX, DINERS, JCB, DANKORT, FORBRUGSFORENINGEN, LASER, UNIONPAY | ✅ Yes          | ✓ DONE                  | Visa/MC/Amex/Discover/JCB/Diners supported                   |
| Card expiry           | ✅ `creditCardExpiry()`                                                                        | ✅ Yes          | ✓ DONE                  | `CardExpirationGenerator` — MM/YY, future-only, locale-aware |
| Security code         | ✅ `securityCode()` (CVV)                                                                      | ✅ Yes          | ✓ DONE                  | `CreditCardGenerator.getCvv()` — 3 or 4 digits               |
| **Banking**           |
| BIC/SWIFT             | ✅ `bic()`                                                                                     | 🟡 Configured-only | ✓ DONE               | `BicGenerator` with explicit banking compatibility policy    |
| IBAN                  | ✅ `iban()`                                                                                    | 🟡 Configured-only | ✓ DONE               | `IbanGenerator` with explicit banking compatibility policy   |
| US routing number     | ✅ `usRoutingNumber()`                                                                         | 🟡 Configured-only | ✓ DONE               | `AbaRoutingGenerator` with explicit banking compatibility policy |
| **Money & Currency**  |
| Currency name         | ✅ `currency()`                                                                                | ✅ Yes          | ✓ DONE                  |                                                              |
| Currency code         | ✅ `currencyCode()` (USD, EUR)                                                                 | ✅ Yes          | ✓ DONE                  |                                                              |
| Currency symbol       | ✅ `currencySymbol()` ($, €)                                                                   | ✅ Yes          | ✓ DONE                  | `CurrencyGenerator.getSymbol()` / `getSymbol(locale)`        |
| Currency numeric code | ✅ `currencyNumericCode()` (840)                                                               | ✅ Yes          | ✓ DONE                  | `CurrencyGenerator.getNumericCode()` — ISO 4217              |
| Price                 | ✅ `price()`                                                                                   | ✅ Yes          | ✓ DONE                  | `MoneyGenerator` — locale-aware, dollar/euro helpers         |
| **Stock Market**      |
| NASDAQ symbol         | ✅ `nsdqSymbol()`                                                                              | No (intentional) | SKIP                | Stock ticker catalogs are long-tail reference data                   |
| NYSE symbol           | ✅ `nyseSymbol()`                                                                              | No (intentional) | SKIP                | Stock ticker catalogs are long-tail reference data                   |
| NSE symbol            | ✅ `nseSymbol()`                                                                               | No (intentional) | SKIP                | Market-specific ticker catalogs are out of core scope                |
| LSE symbol            | ✅ `lseSymbol()`                                                                               | No (intentional) | SKIP                | Market-specific ticker catalogs are out of core scope                |
| Exchange names        | ✅ `exchanges()`                                                                               | No (intentional) | SKIP                | Market reference catalogs are out of core scope                      |
| **Commerce**          |
| Department            | ✅ `department()`                                                                              | ✅ Yes          | ✓ DONE                  | `CommerceGenerator.generateDepartment()` / `generateCategory()` |
| Product name          | ✅ `productName()`                                                                             | ✅ Yes          | ✓ DONE                  | `CommerceGenerator.productName()` / `ProductInfoGenerator`   |
| Material              | ✅ `material()`                                                                                | ✅ Yes          | ✓ DONE                  | `CommerceGenerator.generateMaterial()` / `ProductInfo.material()` |
| Brand                 | ✅ `brand()`, `sport()`, `car()`, `watch()`                                                    | No (intentional) | SKIP                | Generic brand/novelty datasets deferred until requested      |
| Vendor                | ✅ `vendor()`                                                                                  | No (intentional) | SKIP                | Supplier catalogs are long-tail commerce vocabulary                  |
| Promotion code        | ✅ `promotionCode()`                                                                           | No (intentional) | SKIP                | Discount-code shape is trivial with `TemplateStringGenerator`        |
| **Subscriptions**     |
| Plans                 | ✅ `plans()`                                                                                   | No (intentional) | SKIP                | Subscription catalogs are product-specific vocabulary                 |
| Statuses              | ✅ `statuses()`                                                                                | No (intentional) | SKIP                | Subscription catalogs are product-specific vocabulary                 |
| Payment methods       | ✅ `paymentMethods()`                                                                          | ✅ Yes          | ✓ DONE                  | `PaymentInfoGenerator` emits structured payment details              |
| Payment terms         | ✅ `paymentTerms()`                                                                            | No (intentional) | SKIP                | Business terms are domain-specific vocabulary                         |

### 5. COMPANY & BUSINESS

| Feature        | DataFaker Support            | krandom Status | Implementation Priority | Notes                                 |
|----------------|------------------------------|----------------|-------------------------|---------------------------------------|
| Company name   | ✅ `name()`                   | ✅ Yes          | ✓ DONE                  | `CompanyNameGenerator.generate()`     |
| Company suffix | ✅ `suffix()` (Inc, LLC, Ltd) | ✅ Yes          | ✓ DONE                  | `CompanyNameGenerator.generate(true)` |
| Industry       | ✅ `industry()`               | ✅ Yes          | ✓ DONE                  | `IndustryGenerator.generate()`        |
| Profession     | ✅ `profession()`             | ✅ Yes          | ✓ DONE                  | `ProfessionGenerator.generate()`      |
| Buzzword       | ✅ `buzzword()`               | ✅ Yes          | ✓ DONE                  | `CompanyBuzzwordGenerator`           |
| Catch phrase   | ✅ `catchPhrase()`            | ✅ Yes          | ✓ DONE                  | `CompanyCatchPhraseGenerator`        |
| BS phrase      | ✅ `bs()`                     | ✅ Yes          | ✓ DONE                  | `CompanyInfoGenerator` combines business phrase data |
| Logo URL       | ✅ `logo()`                   | No (intentional) | SKIP                | External logo/image URL data is outside core scope |
| Company URL    | ✅ `url()`                    | ✅ Yes          | ✓ DONE                  | `CompanyUrlGenerator.generate()`      |

### 6. JOB & CAREER

| Feature    | DataFaker Support | krandom Status | Implementation Priority | Notes                           |
|------------|-------------------|----------------|-------------------------|---------------------------------|
| Job field  | ✅ `field()`       | ✅ Yes          | ✓ DONE                  | `JobFieldGenerator.generate()`  |
| Seniority  | ✅ `seniority()`   | ✅ Yes          | ✓ DONE                  | `SeniorityGenerator.generate()` |
| Position   | ✅ `position()`    | ✅ Yes          | ✓ DONE                  | `PositionGenerator.generate()`  |
| Job title  | ✅ `title()`       | ✅ Yes          | ✓ DONE                  | Combined title                  |
| Key skills | ✅ `keySkills()`   | No (intentional) | SKIP                | Job-skill catalogs are domain-specific vocabulary |

### 7. TEXT & LOREM

| Feature              | DataFaker Support                                     | krandom Status | Implementation Priority | Notes               |
|----------------------|-------------------------------------------------------|----------------|-------------------------|---------------------|
| **Lorem Ipsum**      |
| Character            | ✅ `character()`                                       | ✅ Yes          | ✓ DONE                  | `CharGenerator`      |
| Characters           | ✅ `characters(n)`                                     | ✅ Yes          | ✓ DONE                  | `StringGenerator` / `CharGenerator.generateList(n)` |
| Word                 | ✅ `word()`                                            | ✅ Yes          | ✓ DONE                  | `WordGenerator`     |
| Words                | ✅ `words(n)`                                          | ✅ Yes          | ✓ DONE                  | `WordGenerator.generateList(n)` |
| Sentence             | ✅ `sentence()`                                        | ✅ Yes          | ✓ DONE                  | `SentenceGenerator` |
| Sentences            | ✅ `sentences(n)`                                      | ✅ Yes          | ✓ DONE                  | `SentenceGenerator.generateList(n)` |
| Paragraph            | ✅ `paragraph()`                                       | ✅ Yes          | ✓ DONE                  | `ParagraphGenerator` |
| Paragraphs           | ✅ `paragraphs(n)`                                     | ✅ Yes          | ✓ DONE                  | `ParagraphGenerator.generateList(n)` |
| Fixed string         | ✅ `fixedString(n)`                                    | ✅ Yes          | ✓ DONE                  | `StringGenerator.builder().minLength(n).maxLength(n)` |
| Max length sentence  | ✅ `maxLengthSentence(n)`                              | ✅ Yes          | ✓ DONE                  | `TextGenerator` handles char-limited text blocks |
| **Specialized Text** |
| Hacker speak         | ✅ `abbreviation()`, `adjective()`, `noun()`, `verb()` | No (intentional) | SKIP                | Novelty vocabulary is outside core scope |
| Hipster words        | ✅ `word()`                                            | No (intentional) | SKIP                | Novelty vocabulary is outside core scope |
| Shakespeare quotes   | ✅ Multiple plays                                      | No (intentional) | SKIP                | Copyright/quote-style catalogs are outside core scope |
| Yoda quotes          | ✅ `quote()`                                           | No (intentional) | SKIP                | Pop-culture catalogs are outside core scope |
| Chuck Norris facts   | ✅ Available                                           | No (intentional) | SKIP                | Joke catalogs are outside core scope |

### 8. DATE & TIME

| Feature      | DataFaker Support                     | krandom Status | Implementation Priority | Notes                                                        |
|--------------|---------------------------------------|----------------|-------------------------|--------------------------------------------------------------|
| Future date  | ✅ `future()`                          | ✅ Yes          | ✓ DONE                  | `DateGenerator.future()` / `future(int)`                     |
| Past date    | ✅ `past()`                            | ✅ Yes          | ✓ DONE                  | `DateGenerator.past()` / `past(int)`                         |
| Date between | ✅ `between()`                         | ✅ Yes          | ✓ DONE                  | `DateGenerator.between(LocalDate, LocalDate)`                |
| Birthday     | ✅ `birthday()`, `birthdayLocalDate()` | ✅ Yes          | ✓ DONE                  | `BirthdayGenerator` — type-based, locale-aware string format |
| Duration     | ✅ `duration()`                        | ✅ Yes          | ✓ DONE                  | `DurationGenerator`                                          |
| Period       | ✅ `period()`                          | No (intentional) | SKIP                | Java callers use explicit date/time ranges; a dedicated period object is not core faker scope |

### 9. PHONE NUMBERS

| Feature              | DataFaker Support              | krandom Status | Implementation Priority | Notes                                                      |
|----------------------|--------------------------------|----------------|-------------------------|------------------------------------------------------------|
| Phone number         | ✅ `phoneNumber()`              | ✅ Yes          | ✓ DONE                  | `PhoneNumberGenerator` — 10 locales, formatted/unformatted |
| National format      | ✅ `phoneNumberNational()`      | ✅ Yes          | ✓ DONE                  | `generate(true)` → "(555) 123-4567", "020 7946 0958"       |
| International format | ✅ `phoneNumberInternational()` | ✅ Yes          | ✓ DONE                  | Country calling codes and MSISDN values are available       |
| Cell phone           | ✅ `cellPhone()`                | ✅ Yes          | ✓ DONE                  | `generate(true, true)` → mobile numbers per locale         |
| Cell international   | ✅ `cellPhoneInternational()`   | ✅ Yes          | ✓ DONE                  | Mobile numbers and calling codes exist via `PhoneNumberGenerator` |
| Extension            | ✅ `extension()`                | No (intentional) | SKIP                | Extension strings are trivial template output              |
| Subscriber number    | ✅ `subscriberNumber()`         | No (intentional) | SKIP                | Not exposed as a separate public concept                   |

### 10. NUMBERS & CODES

| Feature           | DataFaker Support                                | krandom Status | Implementation Priority | Notes          |
|-------------------|--------------------------------------------------|----------------|-------------------------|----------------|
| **Basic Numbers** |
| Random digit      | ✅ `randomDigit()`                                | ✅ Yes          | ✓ DONE                  | 0-9            |
| Digit not zero    | ✅ `randomDigitNotZero()`                         | ✅ Yes          | ✓ DONE                  | Numeric range generators cover 1-9 |
| Positive number   | ✅ `positive()`                                   | ✅ Yes          | ✓ DONE                  | Positive ranges via numeric generators |
| Negative number   | ✅ `negative()`                                   | ✅ Yes          | ✓ DONE                  | Negative ranges via numeric generators |
| Number between    | ✅ `numberBetween()`                              | ✅ Yes          | ✓ DONE                  | Range          |
| Random double     | ✅ `randomDouble()`                               | ✅ Yes          | ✓ DONE                  |                |
| **Book Codes**    |
| ISBN-10           | ✅ `isbn10()`                                     | ✅ Yes          | ✓ DONE                  | `IsbnGenerator` |
| ISBN-13           | ✅ `isbn13()`                                     | ✅ Yes          | ✓ DONE                  | `IsbnGenerator` |
| ISBN components   | ✅ `isbnGs1()`, `isbnGroup()`, `isbnRegistrant()` | No (intentional) | SKIP                | krandom exposes valid whole ISBNs, not intermediate parts |
| **Product Codes** |
| ASIN              | ✅ `asin()`                                       | No (intentional) | SKIP                | Marketplace-specific identifiers are out of core scope |
| IMEI              | ✅ `imei()`                                       | No (intentional) | SKIP                | Device identifiers are out of core scope |
| EAN-8/13          | ✅ `ean8()`, `ean13()`                            | ✅ Yes          | ✓ DONE                  | `EanGenerator` |
| GTIN              | ✅ `gtin8/12/13/14()`                             | ✅ Yes          | ✓ DONE                  | EAN/UPC cover product-code fixture needs; GTIN-14 is intentionally not separate |
| Barcode type      | ✅ `type()`                                       | No (intentional) | SKIP                | Whole EAN/UPC values are supported; barcode taxonomy is not |
| **Hashing**       |
| MD2/MD5           | ✅ `md2()`, `md5()`                               | ✅ Yes          | ✓ DONE                  | `HashGenerator.generateMd5()`; MD2 intentionally not exposed |
| SHA family        | ✅ `sha1/256/384/512()`                           | ✅ Yes          | ✓ DONE                  | `HashGenerator.generateSha1()` / `generateSha256()` |

### 11. COLOR

| Feature    | DataFaker Support | krandom Status | Implementation Priority | Notes                                           |
|------------|-------------------|----------------|-------------------------|-------------------------------------------------|
| Color name | ✅ `name()`        | ✅ Yes          | ✓ DONE                  | `ColorGenerator.generateColorName()`            |
| Hex color  | ✅ `hex()`         | ✅ Yes          | ✓ DONE                  | `ColorGenerator` — HEX/SHORT_HEX/RGB/0x formats |

### 12-25. LONG-TAIL PROVIDER FAMILIES

DataFaker's largest surface area is curated vocabulary catalogs. These are explicitly outside krandom's 100% Java parity contract unless a real product use case promotes one to the roadmap.

| Family | DataFaker examples | krandom Status | Implementation Priority | Notes |
|--------|--------------------|----------------|-------------------------|-------|
| Animals and taxonomy | Animal, Cat, Dog, Horse | No (intentional) | SKIP | Long-tail vocabularies; add via community PR if maintained |
| Science, education, mythology | Science, Educator, Ancient/Mythology | No (intentional) | SKIP | Curated reference data, not core generation |
| Music, arts, books | Music, Book, Artist | No (intentional) | SKIP | Novelty/reference catalogs are outside core scope |
| Transport and aviation | Vehicle, Aviation, Airport, Flight | No (intentional) | SKIP | Automotive/aviation identifiers and facts are domain-specific |
| Healthcare and military | Disease, Medical, Military | No (intentional) | SKIP | Clinical and military vocabularies need domain ownership |
| Sports | Formula 1, basketball, baseball, football, cricket, chess | No (intentional) | SKIP | Large sports catalogs bloat the jar and change quickly |
| Food and drink | Food, Beer, Coffee, Tea, Dessert | No (intentional) | SKIP | Long-tail lifestyle vocabulary |
| Home, weather, device, app, programming | House, Weather, Device, App, ProgrammingLanguage | No (intentional) | SKIP | Either product-specific or simple enough to compose from base generators |
| Entertainment, anime, games | TV/film franchises, Pokemon, Minecraft, Zelda | No (intentional) | SKIP | Pop-culture catalogs are novelty data and may have licensing/maintenance risk |
| Miscellaneous novelty | Emoji, Space, Superhero, Team | No (intentional) | SKIP | Outside core Java faker scope |
| Boolean | `bool()` | ✅ Yes | ✓ DONE | `BooleanGenerator` |

---

## ADVANCED FEATURES

### Configuration & Customization

| Feature                  | DataFaker                          | krandom | Priority | Implementation Notes                                                   |
|--------------------------|------------------------------------|---------|----------|------------------------------------------------------------------------|
| **Locale Support**       |
| Multiple locales         | ✅ 70 advertised locale tags        | ✅ Yes   | ✓ DONE   | **50 supported variants** (35 native + 15 documented fallbacks); native growth remains tracked in GAP-TRACKER.md |
| Locale-aware data        | ✅ Names, addresses, phones         | ✅ Yes   | ✓ DONE   | Names, cities, states, postcodes, phones, coordinates all locale-aware |
| Runtime locale switching | ✅ Yes                              | ✅ Yes   | ✓ DONE   | Pass different `Locale` to constructor per call                        |
| **Seeding**              |
| Reproducible output      | ✅ Constructor with seed            | ✅ Yes   | ✓ DONE   | Most generators support                                                |
| **String Utilities**     |
| Numerify                 | ✅ `numerify("###-####")`           | ✅ Yes   | ✓ DONE   | `TemplateStringGenerator.numerify(...)`                                |
| Letterify                | ✅ `letterify("???-???")`           | ✅ Yes   | ✓ DONE   | `TemplateStringGenerator.letterify(...)`                               |
| Bothify                  | ✅ `bothify("???-###")`             | ✅ Yes   | ✓ DONE   | `TemplateStringGenerator.bothify(...)`                                 |
| Regexify                 | ✅ `regexify("[A-Z]{3}\\d{4}")`     | ✅ Yes   | ✓ DONE   | `TextFormatProvider.regexify(...)` / `RegexGenerator`                  |
| Examplify                | ✅ `examplify("ABC-1234")`          | ✅ Yes   | ✓ DONE   | `TextFormatProvider.examplify(...)`                                    |
| Templatify               | ✅ Custom templates                 | ✅ Yes | ✓ DONE | `TemplateStringGenerator` handles `#`/`?`; `ProviderTemplateGenerator` resolves provider tokens |
| **Data Sources**         |
| Custom YAML              | ✅ `addPath()`, `addUrl()`          | No (intentional) | SKIP | Runtime YAML data-source loading is deferred; Java registries are code-first |
| YAML key resolution      | ✅ `resolve(key)`                   | No (intentional) | SKIP | Runtime YAML source compatibility is not mirrored; use `ProviderTemplateGenerator` |
| **Collections**          |
| Generate lists           | ✅ `collection().len(n).generate()` | ✅ Yes   | ✓ DONE   | `gen.generateList(n)` on every generator                               |
| Variable length          | ✅ `minLen()`, `maxLen()`           | ✅ Yes   | ✓ DONE   | `generateList(n)` with any n                                           |
| Nullable values          | ✅ `nullRate(0.1)`                  | ✅ Yes | ✓ DONE | Nullable booleans plus object-level null probability cover core null-rate use cases |
| Stream API               | ✅ `stream().limit(n)`              | ✅ Yes   | ✓ DONE   | `gen.stream().limit(n)` on every generator                             |
| **Unique Values**        |
| Unique enforcement       | ✅ `faker.unique()`                 | ✅ Yes   | ✓ DONE   | `Generators.unique(...)` and `Generators.unique(...)`            |
| **Output Formats**       |
| CSV generation           | ✅ Schema-based                     | ✅ Yes   | ✓ DONE   | `Schema.toCsv()`                                                       |
| JSON generation          | ✅ Schema-based                     | ✅ Yes   | ✓ DONE   | `Schema.toJsonLines()`                                                 |
| YAML generation          | ✅ Schema-based                     | ✅ Yes | ✓ DONE | `Schema.toYaml(...)` and `OutputFormat.YAML` |
| TOML generation          | ✅ Schema-based                     | ✅ Yes | ✓ DONE | `Schema.toToml(...)` and `OutputFormat.TOML`; null values fail fast because TOML has no null literal |
| Existing-object projection | ✅ Schema transformation            | ✅ Yes | ✓ DONE | `SchemaProjection<T>` writes an object sequence incrementally in every schema output format |
| XML generation           | ✅ Schema-based                     | ✅ Yes | ✓ DONE | `Schema.toXml(...)` and `OutputFormat.XML` |
| **Expressions**          |
| YAML expressions         | ✅ `#{Provider.method}`             | No (intentional) | SKIP | Java-native `Field.template(...)` and `ProviderTemplateGenerator` are supported; DataFaker YAML expression syntax is not mirrored |
| **Custom Providers**     |
| Extend with custom       | ✅ `AbstractProvider<T>`            | ✅ Yes | ✓ DONE | `FieldLookup.registerProvider(...)` / `ProviderHub.register(...)` |
| **Object Population**    |
| POJO population          | ✅ `@Fake` annotation               | ✅ Yes | ✓ DONE | `ObjectGenerator`, `ObjectFaker`, and `@Fake` support object population |

---

## IMPLEMENTATION RECOMMENDATIONS

### Phase 1: CRITICAL GAPS (Must Have)

1. ~~**Email Generation**~~ ✅ DONE — `EmailGenerator` (5 formats, 12 domains, 10 locales)
2. ~~**UUID Generation**~~ ✅ DONE — `UUIDGenerator` (v4 + v5, RFC 4122)
3. ~~**Boolean Generator**~~ ✅ DONE — `BooleanGenerator` with `withLikelihood()`
4. ~~**Lorem Text**~~ ✅ DONE
    - `WordGenerator`, `SentenceGenerator`, `ParagraphGenerator` available
5. ~~**Locale Support Infrastructure**~~ ✅ DONE — 10 locales, all major generators locale-aware
6. ~~**String Templates**~~ ✅ DONE
    - `TemplateStringGenerator` supports `numerify()`, `letterify()`, `bothify()`
7. ~~**Collection Generation**~~ ✅ DONE — `gen.generateList(n)` and `gen.stream()` on every generator
8. ~~**Unique Value Enforcement**~~ ✅ DONE
    - `Generators.unique(...)` and `Generators.unique(...)`

### Phase 2: HIGH VALUE (Should Have)

1. ~~**Address Components**~~ ✅ DONE — city/state/ZIP/country/street/full-address covered
2. ~~**Phone Numbers**~~ ✅ DONE — `PhoneNumberGenerator` (10 locales, mobile/landline, formatted/unformatted)
3. ~~**Credit Cards**~~ ✅ DONE — `CreditCardGenerator` (6 types, Luhn-valid, CVV, expiry)
4. ~~**Names Enhancement**~~ ✅ DONE — gender-specific names, prefix/suffix/title all done
5. ~~**Date Generators**~~ ✅ DONE — `BirthdayGenerator` + `DateGenerator.future/past/between`
6. ~~**Company Data**~~ ✅ DONE — company name/suffix, industry, company URL, profession
7. ~~**URL/Domain Generation**~~ ✅ DONE — `URLGenerator`, `DomainGenerator`

### Phase 3: NICE TO HAVE (Could Have)

1. **Vehicle Data** - VIN, make/model
2. **Banking** - IBAN, BIC
3. **Product Codes** - ISBN, EAN, GTIN
4. **Color Generators**
5. **Weather Data**
6. **Aviation Data**

### Phase 4: LOW PRIORITY (Entertainment)

1. **Pop Culture Data** - Movies, TV shows, games
2. **Sports Data** - Teams, players
3. **Food & Drink** - Recipes, ingredients
4. **Animals** - Breeds, species

---

## KEY DIFFERENTIATORS

### DataFaker Strengths (vs krandom)

1. **200+ providers** vs ~50 in krandom (krandom has grown significantly)
2. **70 advertised locale tags** vs **50 supported variants** in kRandom — a real breadth gap, while kRandom distinguishes 35 native datasets from 15 documented fallbacks
3. **Declarative YAML/URL data sources** — kRandom intentionally provides verified local CSV packs rather than runtime URL loading
4. **Template-based generation breadth** (`numerify`, `letterify`, `bothify`, `regexify`) — partial in krandom (`numerify/letterify/bothify` implemented)
5. **Unique value enforcement ergonomics** (`faker.unique()` fluent provider chaining) — partial in krandom (`Generators.unique(...)`)
6. **Expression language** for composable generators (`#{Name.firstName}`)
7. **GraalVM native image** support — now experimental in kRandom too; application model types still need consumer reachability metadata
8. **POJO auto-population** via `@Fake` annotations
9. **Comprehensive domain data** (sports, healthcare, entertainment, 40+ franchises)
10. **Banking codes** (IBAN, BIC/SWIFT, routing numbers)

### krandom Strengths (vs DataFaker)

1. **Kotlin-first** design with type-safe API
2. **ObjectGenerator** — generate complex object graphs (DataFaker requires manual assembly)
3. **Multi-locale National IDs** — 10 countries with verified checksums (US/UK/AU/FR/DE/JP/ES/IT/BR/CN) — DataFaker has only a few
4. **Locale-aware money formatting** — `MoneyGenerator(Locale)` uses `NumberFormat` for correct symbols/separators/decimal places
5. **Locale-aware birthday strings** — `BirthdayGenerator(Locale).generateAsString()` formats per locale convention
6. **Statistical generators** — `NormalDistributionGenerator`, `BooleanGenerator.withLikelihood()`, `PrimeGenerator`
7. **99%+ test coverage** — DataFaker has much lower coverage guarantees
8. **Fibonacci generator** — not available in DataFaker
9. **FX currency pairs** — `CurrencyPairGenerator` with locale-aware base currency

---

## COMPATIBILITY ASSESSMENT

### Direct Port Feasibility

- ✅ **Easy**: Basic generators (names, numbers, booleans)
- ✅ **Moderate**: Locale support, collections API
- ⚠️ **Hard**: YAML-based data sources, expression engine
- ❌ **Not Applicable**: JVM-specific features (@Fake annotations)

### Recommended Approach

1. **Don't copy everything** - DataFaker is bloated with niche data
2. **Focus on core use cases** - 80/20 rule applies
3. **Prioritize business value** - Email, names, addresses, dates
4. **Keep krandom's identity** - Don't become a DataFaker clone
5. **Add locale support** - This is the biggest gap
6. **Implement templates** - Numerify/letterify are powerful
7. **Skip entertainment** - Low ROI for most users

---

## ESTIMATED EFFORT

### High Priority Features

- Locale infrastructure: **5 days**
- String templates (numerify, letterify, bothify): **2 days**
- Email generation: **1 day**
- UUID generation: **1 day**
- Boolean generator: **0.5 days**
- Lorem text: **2 days**
- Collections API: **2 days**
- Unique value enforcement: **2 days**
- **TOTAL: ~15 days**

### Medium Priority Features

- Address components: **3 days**
- Phone numbers: **2 days**
- Credit cards: **2 days**
- Enhanced names: **2 days**
- Date generators: **2 days**
- Company data: **2 days**
- URL/domain: **2 days**
- **TOTAL: ~15 days**

### Total Phase 1 + 2: **~30 days** (1 sprint)

---

## CONCLUSION

DataFaker is feature-complete but bloated. krandom should focus on:

1. **Core business data generators** (80% use cases)
2. **Locale support** (biggest competitive gap)
3. **Template-based generation** (high leverage feature)
4. **Collections/streams** (developer experience)
5. **Unique values** (common requirement)

**Skip**: Entertainment data, niche sports, most specialized providers.

**Maintain**: krandom's clean architecture, type safety, Kotlin-first approach.

**Target**: Match DataFaker on core features (20%), exceed on developer experience (100%).
