# Fake-rs Feature Parity Analysis

## Library Overview

- **Name**: fake-rs (crate: `fake`)
- **Language**: Rust
- **Version Analyzed**: 2.x (Latest)
- **GitHub**: https://github.com/cksac/fake-rs
- **License**: MIT OR Apache-2.0
- **Last Updated**: 2026-02-28 (Java phased plan moved to `docs/plans/fake-rs-java-plan.md`)
- **Key Strengths**:
    - Trait-based architecture for type safety
    - Procedural macros (`#[derive(Dummy)]`) for automatic fake data generation
    - 28 locale support with locale-specific data
    - Compile-time type safety with zero-cost abstractions
    - Custom RNG support for deterministic testing
    - Modular feature flags for minimal dependencies

## Java Execution Plan

- Active plan: `docs/plans/fake-rs-java-plan.md`
- Current scope: Java parity only (Kotlin/Scala deferred)
- Delivery model: one parity slice at a time with tests + `./scripts/pre_commit_check.sh`

## Executive Summary

Fake-rs is a sophisticated Rust library designed for generating realistic fake data with an emphasis on type safety, compile-time guarantees, and zero-cost abstractions. Unlike imperative fake data
libraries, fake-rs leverages Rust's trait system to provide a declarative, composable API that integrates seamlessly with Rust's type system.

**Unique Differentiators:**

- **Trait-Based Design**: The `Dummy` trait allows any custom type to automatically participate in fake data generation
- **Procedural Macros**: `#[derive(Dummy)]` enables automatic implementation for complex nested structures
- **Locale Architecture**: First-class support for 28 locales with region-appropriate data
- **Type Safety**: All generators are statically typed with compile-time validation
- **RNG Flexibility**: Supports any `rand::Rng` implementation for reproducible or non-deterministic generation
- **Feature Flags**: Modular design with 9 feature flags (chrono, uuid, http, bigdecimal, etc.) for minimal dependencies
- **Zero-Cost Abstractions**: Trait-based design compiles to efficient code with no runtime overhead

While fake-rs has fewer domain-specific providers compared to Java/JavaScript libraries (17 core modules vs 200+ in DataFaker), its strength lies in its **architectural sophistication** and *
*Rust-native design philosophy**. The library excels at generating basic data types with strong type safety and locale support, making it ideal for Rust testing, fuzzing, and fixture generation.

---

## Audited Java Status (2026-02-28)

This section is the current source of truth for Java-core parity status. Some legacy table rows below are stale.

| fake-rs Feature                                     | Java Status | Notes / Differences                                                                    |
|-----------------------------------------------------|-------------|----------------------------------------------------------------------------------------|
| FirstName / LastName                                | ✅ Exists    | Locale-aware generators exist.                                                         |
| Name (full)                                         | ✅ Exists    | `FullNameGenerator` implemented.                                                       |
| NameWithTitle                                       | ✅ Exists    | Via `FullNameGenerator.generate(NameOptions)` with `prefix=true`; API shape differs.   |
| Title / Suffix                                      | ✅ Exists    | `TitleGenerator`, `SuffixGenerator` implemented.                                       |
| CityName / StateName / StateAbbr                    | ✅ Exists    | `CityGenerator`, `StateGenerator.generate(boolean)` implemented.                       |
| StreetName / StreetSuffix / BuildingNumber          | ✅ Exists    | In `StreetAddressGenerator` as dedicated methods.                                      |
| Full street address                                 | ✅ Exists    | `StreetAddressGenerator.generateFullAddress()`.                                        |
| Secondary address                                   | ✅ Exists    | `StreetAddressGenerator.generateSecondaryAddress()`.                                   |
| PostCode                                            | ✅ Exists    | `PostalCodeGenerator` supports locale formats (including US ZIP+4 option).             |
| CountryName                                         | ✅ Exists    | `CountryGenerator`.                                                                    |
| CountryCode                                         | ✅ Exists    | `CountryGenerator.generateCode()` returns ISO alpha-2 codes.                           |
| Latitude / Longitude                                | ✅ Exists    | `CoordinatesGenerator` provides both.                                                  |
| Geohash                                             | ✅ Exists    | `GeohashGenerator` with precision 1-12 plus static encode helpers.                    |
| FreeEmail / SafeEmail / FreeEmailProvider           | ✅ Exists    | `EmailGenerator.generateFreeEmail()`, `generateSafeEmail()`, `getFreeEmailProvider()`. |
| DomainSuffix                                        | ✅ Exists    | `DomainGenerator.getTLD()`.                                                            |
| Slug                                                | ✅ Exists    | `SlugGenerator`.                                                                       |
| DomainName                                          | ✅ Exists    | `DomainGenerator.generateName()` plus full-domain composition methods.                 |
| URL                                                 | ✅ Exists    | `URLGenerator` with options/path/query and encoded file-name segment support.          |
| Username                                            | ✅ Exists    | `UsernameGenerator`.                                                                   |
| Password(range)                                     | ✅ Exists    | `PasswordGenerator.generate(min,max)` and fixed-length overload.                       |
| IPv4 / IPv6                                         | ✅ Exists    | Both implemented.                                                                      |
| IP (v4 or v6)                                       | ✅ Exists    | `IPGenerator` returns either v4 or v6 per call.                                        |
| IPv4 private/public/CIDR                            | ✅ Exists    | `IPv4Generator.generatePrivate/public/cidr`.                                           |
| IPv6 CIDR                                           | ✅ Exists    | `IPv6Generator.generateCidr`.                                                          |
| MACAddress / Port / UserAgent                       | ✅ Exists    | Implemented in `network` package.                                                      |
| HTTP status code                                    | ✅ Exists    | `HttpStatusCodeGenerator`.                                                             |
| UUID                                                | ✅ Exists    | `UUIDGenerator` supports v4/v5/v7 (fake-rs commonly v4 via feature flag).              |
| CurrencyCode / Name / Symbol                        | ✅ Exists    | `CurrencyGenerator` supports all three.                                                |
| Credit card number / expiry / CVV                   | ✅ Exists    | `CreditCardGenerator` + `CardExpirationGenerator`.                                     |
| BIC / ISIN                                          | ✅ Exists    | `BicGenerator` and `IsinGenerator`.                                                    |
| CompanyName / Industry / Profession                 | ✅ Exists    | Implemented; `ProfessionGenerator` is locale-extensible.                               |
| JobField / JobSeniority / JobTitle(Position)        | ✅ Exists    | `JobFieldGenerator`, `SeniorityGenerator`, `PositionGenerator`.                        |
| JobType                                             | ✅ Exists    | `JobTypeGenerator`.                                                                    |
| Word / Sentence / Paragraph                         | ✅ Exists    | Locale-aware text generators implemented.                                              |
| Words/Sentences/Paragraphs with fake-rs `Range` API | ✅ Exists    | Range-style min/max APIs added on word/sentence/paragraph generators.                  |
| DateTime / Date / Time                              | ✅ Exists    | `LocalDateTimeGenerator`, `DateGenerator`, `TimeGenerator`.                            |
| DateTimeBefore / After / Between                    | ✅ Exists    | `LocalDateTimeGenerator.before()`, `.after()`, `.between()`.                           |
| Duration                                            | ✅ Exists    | `DurationGenerator`.                                                                   |
| PhoneNumber / CellNumber                            | ✅ Exists    | `PhoneNumberGenerator` supports formatted + mobile/landline selection.                 |
| NumberWithFormat                                    | ✅ Exists    | `NumberWithFormatGenerator` (`#` placeholder format).                                  |
| Digit                                               | ✅ Exists    | `DigitGenerator`.                                                                      |
| ISBN / ISBN10 / ISBN13                              | ✅ Exists    | `IsbnGenerator` supports both formats.                                                 |
| Hex/RGB color                                       | ✅ Exists    | `ColorGenerator` supports multiple color formats.                                      |
| RGBA/HSL/HSLA strings                               | ✅ Exists    | Added in `ColorFormat` + `ColorGenerator`.                                             |
| License plate                                       | Missing   | Not implemented.                                                                       |
| FileName / FileExtension                            | ✅ Exists    | Implemented in `file` package.                                                         |
| FilePath / DirPath / MimeType / Semver              | ✅ Exists    | `FilePathGenerator`, `DirPathGenerator`, `MimeTypeGenerator`, `SemverGenerator`.       |

### Architecture-level Differences (Intentional / Significant)

- fake-rs trait/macro model (`Fake`, `Dummy`, `#[derive(Dummy)]`) does not map 1:1 to Java; Java uses explicit generator classes.
- fake-rs locale selection is compile-time module-path based; Java locale is runtime via `GeneratorConfig`.
- fake-rs feature flags are crate-level compile features; Java currently exposes modules directly without an equivalent flag system.
- fake-rs RNG extensibility accepts any `rand::Rng`; Java supports deterministic seed via `GeneratorConfig` but not pluggable RNG interfaces across all generators.

## Feature Categories

### 1. PERSONAL IDENTITY

| Feature                   | Fake-rs Support              | krandom Status | Implementation Priority | Notes                                 |
|---------------------------|------------------------------|----------------|-------------------------|---------------------------------------|
| **Name Generation**       |
| First name                | ✅ `FirstName()`              | ✅ Yes          | ✓ DONE                  | krandom has basic name support        |
| Last name                 | ✅ `LastName()`               | ✅ Yes          | ✓ DONE                  |                                       |
| Full name                 | ✅ `Name()`                   | ✅ Yes          | ✓ DONE                  | `FullNameGenerator`                   |
| Name with title           | ✅ `NameWithTitle()`          | ✅ Yes          | ✓ DONE                  | `FullNameGenerator.generate(NameOptions)` with prefix |
| Title/Prefix              | ✅ `Title()` (Mr., Mrs., Dr.) | ✅ Yes          | ✓ DONE                  | `TitleGenerator`                      |
| Suffix                    | ✅ `Suffix()` (Jr., Sr., III) | ✅ Yes          | ✓ DONE                  | `SuffixGenerator`                     |
| **Gender & Demographics** |
| Gender-specific names     | No                         | No           | LOW                     | Not in fake-rs design                 |
| Race/Ethnicity            | No                         | No           | LOW                     | Not available                         |
| Education level           | No                         | No           | LOW                     | Not available                         |
| Marital status            | No                         | No           | LOW                     | Not available                         |
| **ID Numbers**            |
| SSN/National ID           | No                         | ✅ Yes          | ✓ DONE                  | `NationalIdGenerator` supports configured locales |
| Passport number           | No                         | No           | LOW                     | Not available                         |
| Driver's license          | No                         | No           | LOW                     | Not available                         |

**Analysis**: Fake-rs focuses on basic name generation with strong locale support (28 locales). It lacks advanced identity features like ID validation, gender-specific names, or demographic data. The
emphasis is on simple, locale-aware name generation rather than comprehensive identity simulation.

### 2. ADDRESS & LOCATION

| Feature              | Fake-rs Support                       | krandom Status | Implementation Priority | Notes                     |
|----------------------|---------------------------------------|----------------|-------------------------|---------------------------|
| **City & State**     |
| City name            | ✅ `CityName()`                        | ✅ Yes          | ✓ DONE                  | `CityGenerator`           |
| City prefix          | ✅ `CityPrefix()`                      | No (intentional) | SKIP                | Internal vocabulary; full city names are generated |
| City suffix          | ✅ `CitySuffix()`                      | No (intentional) | SKIP                | Internal vocabulary; full city names are generated |
| State name           | ✅ `StateName()` (US only)             | ✅ Yes          | ✓ DONE                  | `StateGenerator.generate()` |
| State abbreviation   | ✅ `StateAbbr()` (US only)             | ✅ Yes          | ✓ DONE                  | `StateGenerator.generate(true)` |
| **Street Address**   |
| Street name          | ✅ `StreetName()`                      | ✅ Yes          | ✓ DONE                  | `StreetAddressGenerator.generateStreetName()` |
| Street suffix        | ✅ `StreetSuffix()`                    | No (intentional) | SKIP                | Internal vocabulary; full street names are generated |
| Building number      | ✅ `BuildingNumber()`                  | ✅ Yes          | ✓ DONE                  | `StreetAddressGenerator.generateBuildingNumber()` |
| Full street address  | No (compose manually)               | ✅ Yes          | ✓ DONE                  | `StreetAddressGenerator.generateFullAddress()` |
| Secondary address    | No                                  | ✅ Yes          | ✓ DONE                  | `StreetAddressGenerator.generateSecondaryAddress()` |
| **Postal Codes**     |
| Post code            | ✅ `PostCode()`                        | ✅ Yes          | ✓ DONE                  | `PostalCodeGenerator`     |
| ZIP code             | ✅ `PostCode()` (US)                   | ✅ Yes          | ✓ DONE                  | `PostalCodeGenerator(Locale.US)` |
| ZIP+4                | No                                  | No           | LOW                     | Not available             |
| **Country & Region** |
| Country name         | ✅ `CountryName()`                     | ✅ Yes          | ✓ DONE                  | `CountryGenerator`        |
| Country code         | ✅ `CountryCode()`                     | ✅ Yes          | ✓ DONE                  | `CountryGenerator.generateCode()` |
| Time zone            | ✅ `TimeZone()`                        | ✅ Yes          | ✓ DONE                  | `TimezoneGenerator`       |
| Capital city         | No                                  | No           | LOW                     | Not available             |
| Nationality          | No                                  | No           | LOW                     | Not available             |
| Language             | No                                  | No           | LOW                     | Not available             |
| **Coordinates**      |
| Latitude             | ✅ `Latitude()`                        | ✅ Yes          | ✓ DONE                  | `CoordinatesGenerator.generateLatitude()` |
| Longitude            | ✅ `Longitude()`                       | ✅ Yes          | ✓ DONE                  | `CoordinatesGenerator.generateLongitude()` |
| Geohash              | ✅ `Geohash(u8)` (requires `geo` flag) | ✅ Yes          | ✓ DONE                  | `GeohashGenerator` precision 1-12 |

**Analysis**: Fake-rs provides solid address fundamentals with locale support but requires manual composition for full addresses. The geohash feature is unique and useful for geographic testing.
Missing secondary addresses and advanced geographic features.

### 3. INTERNET & NETWORKING

| Feature             | Fake-rs Support                                                 | krandom Status | Implementation Priority | Notes                      |
|---------------------|-----------------------------------------------------------------|----------------|-------------------------|----------------------------|
| **Email**           |
| Free email          | ✅ `FreeEmail()`                                                 | ✅ Yes          | ✓ DONE                  | `EmailGenerator.generateFreeEmail()` |
| Safe email          | ✅ `SafeEmail()`                                                 | ✅ Yes          | ✓ DONE                  | `EmailGenerator.generateSafeEmail()` |
| Free email provider | ✅ `FreeEmailProvider()`                                         | ✅ Yes          | ✓ DONE                  | `EmailGenerator.getFreeEmailProvider()` |
| Email subject       | No                                                            | No           | LOW                     | Not available              |
| **Domain & URLs**   |
| Domain suffix       | ✅ `DomainSuffix()`                                              | ✅ Yes          | ✓ DONE                  | `DomainGenerator.getTLD()` |
| Username            | ✅ `Username()`                                                  | ✅ Yes          | ✓ DONE                  | `UsernameGenerator`        |
| Password            | ✅ `Password(Range)`                                             | ✅ Yes          | ✓ DONE                  | `PasswordGenerator.generate(min,max)` |
| Slug                | ✅ `Slug()` (requires `email` flag)                              | ✅ Yes          | ✓ DONE                  | `SlugGenerator`            |
| Domain name         | No (compose manually)                                         | ✅ Yes          | ✓ DONE                  | `DomainGenerator`          |
| URL                 | No                                                            | ✅ Yes          | ✓ DONE                  | `URLGenerator`             |
| **IP Addresses**    |
| IPv4                | ✅ `IPv4()`                                                      | ✅ Yes          | ✓ DONE                  | Already implemented        |
| IPv6                | ✅ `IPv6()`                                                      | ✅ Yes          | ✓ DONE                  | Already implemented        |
| IP (v4 or v6)       | ✅ `IP()`                                                        | ✅ Yes          | ✓ DONE                  | `IPGenerator`              |
| IPv4 private        | No                                                            | No           | LOW                     | RFC1918 addresses          |
| IPv4 CIDR           | No                                                            | No           | LOW                     | Not available              |
| IPv6 CIDR           | No                                                            | No           | LOW                     | Not available              |
| **Network**         |
| MAC address         | ✅ `MACAddress()`                                                | ✅ Yes          | ✓ DONE                  | `MacAddressGenerator`      |
| Port                | No                                                            | No           | LOW                     | Not available              |
| HTTP method         | No                                                            | No           | LOW                     | Not available              |
| HTTP status code    | ✅ `RfcStatusCode()`, `ValidStatusCode()` (requires `http` flag) | ✅ Yes          | ✓ DONE                  | `HttpStatusCodeGenerator`  |
| **Identifiers**     |
| UUID                | ✅ `uuid::Uuid` support (requires `uuid` flag)                   | ✅ Yes          | ✓ DONE                  | `UUIDGenerator`            |
| User agent          | ✅ `UserAgent()`                                                 | ✅ Yes          | ✓ DONE                  | `UserAgentGenerator`       |
| Color (hex)         | ✅ `Color()`                                                     | No           | LOW                     | Hex color codes            |

**Analysis**: Fake-rs has strong fundamentals for internet data with good feature flag integration (http, uuid). The Password generator is configurable by length. Missing URL composition and CIDR
notation. The Color generator is in the internet module, which is unusual.

### 4. FINANCE & COMMERCE

| Feature               | Fake-rs Support      | krandom Status | Implementation Priority | Notes                       |
|-----------------------|----------------------|----------------|-------------------------|-----------------------------|
| **Banking & Finance** |
| BIC/SWIFT             | ✅ `Bic()`            | ✅ Yes          | ✓ DONE                  | `BicGenerator`              |
| ISIN                  | ✅ `Isin()`           | No           | LOW                     | International Securities ID |
| **Currency**          |
| Currency code         | ✅ `CurrencyCode()`   | ✅ Yes          | ✓ DONE                  | ISO 4217 codes              |
| Currency name         | ✅ `CurrencyName()`   | ✅ Yes          | ✓ DONE                  | Full currency names         |
| Currency symbol       | ✅ `CurrencySymbol()` | ✅ Yes          | ✓ DONE                  | `CurrencyGenerator.generateCurrencySymbol()` |
| **Credit Cards**      |
| Credit card number    | No                 | ✅ Yes          | ✓ DONE                  | `CreditCardGenerator`       |
| Card expiry           | No                 | ✅ Yes          | ✓ DONE                  | `CardExpirationGenerator`   |
| CVV/Security code     | No                 | ✅ Yes          | ✓ DONE                  | `CreditCardGenerator.generateSecurityCode()` |
| **Commerce**          |
| Product name          | No                 | No           | LOW                     | Not available               |
| Price                 | No                 | No           | LOW                     | Not available               |
| Department            | No                 | No           | LOW                     | Not available               |
| Brand                 | No                 | No           | LOW                     | Not available               |
| **Stock Market**      |
| Stock symbols         | No                 | No           | LOW                     | Not available               |

**Analysis**: Fake-rs provides minimal finance features focused on identifiers (BIC, ISIN) and currency data. Notably missing credit card generation, which is common in testing libraries. The finance
module is very lightweight compared to other libraries.

### 5. COMPANY & BUSINESS

| Feature         | Fake-rs Support                                      | krandom Status | Implementation Priority | Notes                    |
|-----------------|------------------------------------------------------|----------------|-------------------------|--------------------------|
| Company name    | ✅ `CompanyName()`                                    | ✅ Yes          | ✓ DONE                  | `CompanyNameGenerator`   |
| Company suffix  | ✅ `CompanySuffix()`                                  | ✅ Yes          | ✓ DONE                  | `CompanyNameGenerator.generate(true)` |
| Business jargon | ✅ `Bs()`, `BsAdj()`, `BsNoun()`, `BsVerb()`          | No           | LOW                     | Corporate BS phrases     |
| Buzzword        | ✅ `Buzzword()`, `BuzzwordMiddle()`, `BuzzwordTail()` | No           | LOW                     | Marketing buzzwords      |
| Catch phrase    | ✅ `CatchPhase()` (sic)                               | No           | LOW                     | Company slogans          |
| Industry        | ✅ `Industry()`                                       | ✅ Yes          | ✓ DONE                  | `IndustryGenerator`      |
| Profession      | ✅ `Profession()`                                     | ✅ Yes          | ✓ DONE                  | `ProfessionGenerator`    |
| Logo URL        | No                                                 | No           | LOW                     | Not available            |

**Analysis**: Fake-rs has a well-rounded company module with corporate jargon generators. The BS (business speak) generators are useful for generating realistic-sounding corporate content. Profession
overlaps with the job module.

### 6. JOB & CAREER

| Feature       | Fake-rs Support    | krandom Status | Implementation Priority | Notes                     |
|---------------|--------------------|----------------|-------------------------|---------------------------|
| Job field     | ✅ `JobField()`     | ✅ Yes          | ✓ DONE                  | `JobFieldGenerator`       |
| Job seniority | ✅ `JobSeniority()` | ✅ Yes          | ✓ DONE                  | `SeniorityGenerator`      |
| Job title     | ✅ `JobTitle()`     | ✅ Yes          | ✓ DONE                  | Combined job titles       |
| Job type      | ✅ `JobType()`      | ✅ Yes          | ✓ DONE                  | `JobTypeGenerator`        |
| Position      | No               | No           | LOW                     | Overlaps with title       |
| Key skills    | No               | No           | LOW                     | Not available             |

**Analysis**: Fake-rs has a focused job module with seniority, field, type, and title. Good for generating employment data. krandom already has job title support.

### 7. TEXT & LOREM

| Feature         | Fake-rs Support        | krandom Status | Implementation Priority | Notes                           |
|-----------------|------------------------|----------------|-------------------------|---------------------------------|
| **Lorem Ipsum** |
| Word            | ✅ `Word()`             | ✅ Yes          | ✓ DONE                  | `WordGenerator`                 |
| Words           | ✅ `Words(Range)`       | ✅ Yes          | ✓ DONE                  | `WordGenerator.generateList(n)` and range APIs |
| Sentence        | ✅ `Sentence(Range)`    | ✅ Yes          | ✓ DONE                  | `SentenceGenerator`             |
| Sentences       | ✅ `Sentences(Range)`   | ✅ Yes          | ✓ DONE                  | `SentenceGenerator.generateList(n)` |
| Paragraph       | ✅ `Paragraph(Range)`   | ✅ Yes          | ✓ DONE                  | `ParagraphGenerator`            |
| Paragraphs      | ✅ `Paragraphs(Range)`  | ✅ Yes          | ✓ DONE                  | `ParagraphGenerator.generateList(n)` |
| Character       | No (use `char` type) | No           | LOW                     | Use `Faker.fake::<char>()`      |
| Characters      | No                   | No           | LOW                     | Not a dedicated method          |

**Analysis**: Fake-rs has an excellent lorem module with Range-configurable generators. The ability to specify exact ranges (e.g., `Words(3..10)`) provides fine-grained control. This is more flexible
than fixed-count generators.

### 8. DATE & TIME

| Feature                                           | Fake-rs Support                 | krandom Status | Implementation Priority | Notes                    |
|---------------------------------------------------|---------------------------------|----------------|-------------------------|--------------------------|
| **Chrono Types** (requires `chrono` feature flag) |
| DateTime                                          | ✅ `DateTime()`                  | ✅ Yes          | ✓ DONE                  | `LocalDateTimeGenerator` |
| DateTime before                                   | ✅ `DateTimeBefore(DateTime)`    | ✅ Yes          | ✓ DONE                  | `LocalDateTimeGenerator.before(...)` |
| DateTime after                                    | ✅ `DateTimeAfter(DateTime)`     | ✅ Yes          | ✓ DONE                  | `LocalDateTimeGenerator.after(...)` |
| DateTime between                                  | ✅ `DateTimeBetween(start, end)` | ✅ Yes          | ✓ DONE                  | `LocalDateTimeGenerator.between(...)` |
| Date                                              | ✅ `Date()`                      | ✅ Yes          | ✓ DONE                  | `DateGenerator`          |
| Time                                              | ✅ `Time()`                      | ✅ Yes          | ✓ DONE                  | `TimeGenerator`          |
| Duration                                          | ✅ `Duration()`                  | ✅ Yes          | ✓ DONE                  | `DurationGenerator`      |
| **Date Formatting**                               |
| Formatted dates                                   | No (use chrono format)        | No           | LOW                     | Not pre-formatted        |
| ISO 8601                                          | No (use chrono format)        | No           | LOW                     | Use chrono's to_rfc3339  |
| Unix timestamp                                    | No (use chrono convert)       | No           | LOW                     | Use chrono's timestamp   |

**Analysis**: Fake-rs integrates with the chrono crate for type-safe date/time generation. The before/after/between variants are powerful for generating temporal sequences. No pre-formatted string
output; relies on chrono's formatting capabilities.

### 9. PHONE NUMBERS

| Feature      | Fake-rs Support   | krandom Status | Implementation Priority | Notes                      |
|--------------|-------------------|----------------|-------------------------|----------------------------|
| Phone number | ✅ `PhoneNumber()` | ✅ Yes          | ✓ DONE                  | `PhoneNumberGenerator`     |
| Cell number  | ✅ `CellNumber()`  | ✅ Yes          | ✓ DONE                  | `PhoneNumberGenerator.generate(..., true)` |
| E.164 format | No              | No           | LOW                     | International format       |
| Country code | No              | No           | LOW                     | Not separate               |

**Analysis**: Fake-rs provides basic phone number generation with locale support across 28 locales. No explicit international formatting (E.164), but locale support handles regional formats.

### 10. NUMBERS & CODES

| Feature               | Fake-rs Support                           | krandom Status | Implementation Priority | Notes                  |
|-----------------------|-------------------------------------------|----------------|-------------------------|------------------------|
| **Primitive Numbers** |
| Integer (i8-i128)     | ✅ `Faker.fake::<i32>()`                   | ✅ Yes          | ✓ DONE                  | Via Dummy<Faker>       |
| Unsigned (u8-u128)    | ✅ `Faker.fake::<u32>()`                   | ✅ Yes          | ✓ DONE                  | Via Dummy<Faker>       |
| Float (f32, f64)      | ✅ `Faker.fake::<f64>()`                   | ✅ Yes          | ✓ DONE                  | Via Dummy<Faker>       |
| Boolean               | ✅ `Faker.fake::<bool>()`                  | ✅ Yes          | ✓ DONE                  | Via Dummy<Faker>       |
| **Formatted Numbers** |
| Digit string          | ✅ `Digit()`                               | No           | LOW                     | Single digit as string |
| Number with format    | ✅ `NumberWithFormat(&str)`                | ✅ Yes          | ✓ DONE                  | `NumberWithFormatGenerator` |
| **Barcodes**          |
| ISBN                  | ✅ `Isbn()`                                | ✅ Yes          | ✓ DONE                  | `IsbnGenerator`        |
| ISBN-10               | ✅ `Isbn10()`                              | ✅ Yes          | ✓ DONE                  | `IsbnGenerator`        |
| ISBN-13               | ✅ `Isbn13()`                              | ✅ Yes          | ✓ DONE                  | `IsbnGenerator`        |
| EAN                   | No                                      | No           | LOW                     | Not available          |
| UPC                   | No                                      | No           | LOW                     | Not available          |
| **Other Numbers**     |
| Decimal               | ✅ Dummy impl (requires `decimal` flag)    | No           | LOW                     | rust_decimal::Decimal  |
| BigDecimal            | ✅ Dummy impl (requires `bigdecimal` flag) | No           | LOW                     | bigdecimal::BigDecimal |

**Analysis**: Fake-rs leverages Rust's type system for numeric generation via the `Dummy<Faker>` trait. The NumberWithFormat is powerful for custom formats. ISBN generators are useful for book/product
testing. Feature flags for decimal types show modular design.

### 11. COLOR

| Feature    | Fake-rs Support                            | krandom Status | Implementation Priority | Notes                   |
|------------|--------------------------------------------|----------------|-------------------------|-------------------------|
| Hex color  | ✅ `HexColor()`                             | ✅ Yes          | ✓ DONE                  | `ColorGenerator`        |
| RGB color  | ✅ `RgbColor()`                             | ✅ Yes          | ✓ DONE                  | `ColorGenerator`        |
| RGBA color | ✅ `RgbaColor()`                            | ✅ Yes          | ✓ DONE                  | `ColorGenerator`        |
| HSL color  | ✅ `HslColor()`                             | ✅ Yes          | ✓ DONE                  | `ColorGenerator`        |
| HSLA color | ✅ `HslaColor()`                            | ✅ Yes          | ✓ DONE                  | `ColorGenerator`        |
| Color name | ✅ `Color()` (requires `random_color` flag) | No           | LOW                     | CSS color names         |
| Safe color | No                                       | No           | LOW                     | Not available           |

**Analysis**: Fake-rs has comprehensive color generation with multiple format support (hex, RGB, HSL). The color name feature requires the `random_color` flag. Good coverage for UI testing and design
work.

### 12. AUTOMOTIVE

| Feature       | Fake-rs Support                       | krandom Status | Implementation Priority | Notes                  |
|---------------|---------------------------------------|----------------|-------------------------|------------------------|
| License plate | ✅ `LicencePlate()` (British spelling) | No           | LOW                     | Vehicle plates         |
| VIN           | No                                  | No           | LOW                     | Vehicle identification |
| Make/Model    | No                                  | No           | LOW                     | Not available          |

**Analysis**: Minimal automotive support with just license plates. Much lighter than comprehensive auto libraries.

### 13. FILESYSTEM

| Feature        | Fake-rs Support                                         | krandom Status | Implementation Priority | Notes                |
|----------------|---------------------------------------------------------|----------------|-------------------------|----------------------|
| File path      | ✅ `FilePath()`                                          | ✅ Yes          | ✓ DONE                  | `FilePathGenerator`   |
| File name      | ✅ `FileName()`                                          | ✅ Yes          | ✓ DONE                  | `FileNameGenerator`   |
| File extension | ✅ `FileExtension()`                                     | ✅ Yes          | ✓ DONE                  | `FileExtensionGenerator` |
| Directory path | ✅ `DirPath()`                                           | ✅ Yes          | ✓ DONE                  | `DirPathGenerator`    |
| MIME type      | ✅ `MimeType()`                                          | ✅ Yes          | ✓ DONE                  | `MimeTypeGenerator`   |
| Semver         | ✅ `Semver()`, `SemverStable()` (requires `semver` flag) | ✅ Yes          | ✓ DONE                  | `SemverGenerator`     |

**Analysis**: Fake-rs has a unique filesystem module useful for file I/O testing. The semantic version generators are valuable for package/release testing. MIME type generation is handy for HTTP/API
testing.

### 14. SCIENCE & EDUCATION

| Feature          | Fake-rs Support | krandom Status | Implementation Priority | Notes         |
|------------------|-----------------|----------------|-------------------------|---------------|
| Chemical element | No            | No           | LOW                     | Not available |
| Unit             | No            | No           | LOW                     | Not available |
| Scientific term  | No            | No           | LOW                     | Not available |

**Analysis**: No science/education domain support in fake-rs.

### 15. ANIMALS

| Feature        | Fake-rs Support | krandom Status | Implementation Priority | Notes         |
|----------------|-----------------|----------------|-------------------------|---------------|
| Animal name    | No            | No           | LOW                     | Not available |
| Dog/Cat breeds | No            | No           | LOW                     | Not available |

**Analysis**: No animal domain support in fake-rs.

### 16. FOOD & DRINK

| Feature    | Fake-rs Support | krandom Status | Implementation Priority | Notes         |
|------------|-----------------|----------------|-------------------------|---------------|
| Food/Dish  | No            | No           | LOW                     | Not available |
| Ingredient | No            | No           | LOW                     | Not available |

**Analysis**: No food domain support in fake-rs.

### 17. SPORTS

| Feature    | Fake-rs Support | krandom Status | Implementation Priority | Notes         |
|------------|-----------------|----------------|-------------------------|---------------|
| Sport name | No            | No           | LOW                     | Not available |
| Team name  | No            | No           | LOW                     | Not available |

**Analysis**: No sports domain support in fake-rs.

### 18. ENTERTAINMENT

| Feature         | Fake-rs Support | krandom Status | Implementation Priority | Notes         |
|-----------------|-----------------|----------------|-------------------------|---------------|
| Movie/TV data   | No            | No           | LOW                     | Not available |
| Video game data | No            | No           | LOW                     | Not available |
| Anime/Manga     | No            | No           | LOW                     | Not available |

**Analysis**: No entertainment domain support in fake-rs.

### 19. HEALTHCARE

| Feature       | Fake-rs Support | krandom Status | Implementation Priority | Notes         |
|---------------|-----------------|----------------|-------------------------|---------------|
| Medical terms | No            | No           | LOW                     | Not available |
| Blood type    | No            | No           | LOW                     | Not available |

**Analysis**: No healthcare domain support in fake-rs.

### 20. MILITARY

| Feature       | Fake-rs Support | krandom Status | Implementation Priority | Notes         |
|---------------|-----------------|----------------|-------------------------|---------------|
| Military rank | No            | No           | LOW                     | Not available |
| Equipment     | No            | No           | LOW                     | Not available |

**Analysis**: No military domain support in fake-rs.

---

## ADVANCED FEATURES

### Trait-Based Architecture

| Feature                   | Fake-rs Support          | krandom Status | Implementation Priority | Notes                         |
|---------------------------|--------------------------|----------------|-------------------------|-------------------------------|
| **Core Traits**           |
| `Fake` trait              | ✅ Primary API            | No (intentional) | SKIP                | Rust trait — Java equivalent is `Generator<T>` (`generate()` method)  |
| `Dummy` trait             | ✅ Extensibility API      | No (intentional) | SKIP                | Rust trait — Java equivalent is `ObjectFaker.ruleFor` + `@Fake`       |
| Generic over `Rng`        | ✅ All methods            | ✅ Yes              | ✓ DONE              | `GeneratorConfig.Builder.randomFactory(Supplier<? extends Random>)`   |
| **Procedural Macros**     |
| `#[derive(Dummy)]`        | ✅ Auto-implementation    | No (intentional) | SKIP                | Rust proc-macro — Java reflection-based `ObjectGenerator` is the equivalent |
| `#[dummy(faker = "...")]` | ✅ Field-level config     | No (intentional) | SKIP                | Rust attribute — Java equivalent is `@Fake("name")` annotation        |
| `#[dummy(default)]`       | ✅ Use Default::default() | No (intentional) | SKIP                | Rust attribute — Java equivalent is `ObjectFaker.ignore`              |
| `#[dummy(fixed = "...")]` | ✅ Fixed value            | No (intentional) | SKIP                | Rust attribute — Java equivalent is `ObjectFaker.ruleFor(name, () -> value)` |
| **Macros**                |
| `fake!` macro             | ✅ Concise syntax         | No (intentional) | SKIP                | Rust macro — no Java equivalent; use `Generators.ofX()` factories     |
| **Type Safety**           |
| Compile-time checking     | ✅ All types              | ✅ Partial     | Open item            | Java keeps runtime reflection for object graphs; generator APIs remain statically typed |
| Zero-cost abstractions    | ✅ Trait monomorphization | N/A            | N/A                     | Rust-specific                 |

**Analysis**: Fake-rs's trait-based architecture is its crown jewel. The `#[derive(Dummy)]` macro enables declarative fake data generation for complex nested structures without boilerplate. This is a
paradigm shift from imperative faker libraries—types "opt into" fake generation via trait implementation, enabling compiler-verified fake data generation.

**Example:**

```rust
#[derive(Dummy, Debug)]
struct User {
    #[dummy(faker = "fake::faker::name::en::FirstName()")]
    first_name: String,

    #[dummy(faker = "18..65")]
    age: u8,

    #[dummy(default)]
    is_verified: bool,  // always false
}

let user: User = Faker.fake();
```

### Locale Support

| Feature                 | Fake-rs Support                                                                                           | krandom Status | Implementation Priority | Notes                       |
|-------------------------|-----------------------------------------------------------------------------------------------------------|----------------|-------------------------|-----------------------------|
| **Locale Count**        | 28 locales                                                                                                | Unknown        | N/A                     | vs 60+ in DataFaker         |
| **Supported Locales**   |
| English variants        | ✅ en, en_US, en_GB                                                                                        | Unknown        | HIGH                    | Multiple English locales    |
| European                | ✅ fr_FR, de_DE, es_ES, pt_BR, pt_PT, it_IT, pl_PL, nl_NL, da_DK, fi_FI, hu_HU, nb_NO, ro_RO, sk_SK, sv_SE | Unknown        | MEDIUM                  | 15 European locales         |
| Asian                   | ✅ zh_CN, zh_TW, ja_JP, ko_KR, vi_VN, id_ID                                                                | Unknown        | MEDIUM                  | 6 Asian locales             |
| Middle Eastern          | ✅ ar_SA, he_IL, fa_IR                                                                                     | Unknown        | LOW                     | 3 Middle Eastern locales    |
| Eastern European        | ✅ ru_RU, uk_UA, tr_TR                                                                                     | Unknown        | LOW                     | 3 Eastern European locales  |
| **Locale Architecture** |
| Path-based              | ✅ `fake::faker::name::<locale>`                                                                           | No (intentional) | SKIP                | Rust module-path locale API does not map to Java runtime config |
| Compile-time selection  | ✅ Type system                                                                                             | No (intentional) | SKIP                | Java intentionally uses runtime `Locale` / `GeneratorConfig` |
| Per-faker locale        | ✅ Yes                                                                                                     | ✅ Yes          | ✓ DONE                  | Each generator can take `Locale` / `GeneratorConfig` |

**Analysis**: Fake-rs's locale architecture is unique—locales are compile-time module paths, not runtime configuration. This provides type safety and eliminates runtime locale lookup overhead.
However, it makes dynamic locale selection impossible without runtime dispatch.

**Example:**

```rust
use fake::{Fake, faker::name};

let en_name: String = name::en::Name().fake();
let ja_name: String = name::ja_jp::Name().fake();
let fr_name: String = name::fr_fr::Name().fake();
```

### Feature Flags & Modularity

| Feature Flag   | Purpose                   | krandom Equivalent | Implementation Priority | Notes              |
|----------------|---------------------------|--------------------|-------------------------|--------------------|
| `derive`       | Enable `#[derive(Dummy)]` | No (intentional) | SKIP                    | Rust proc-macro feature flag — Java equivalent is `ObjectFaker.ruleFor` and `@Fake` annotation |
| `chrono`       | Date/time types           | ✅ Yes              | ✓ DONE                  | Java time generators cover date/time types |
| `uuid`         | UUID generation           | ✅ Yes              | ✓ DONE                  | `UUIDGenerator` |
| `http`         | HTTP status codes         | ✅ Yes              | ✓ DONE                  | `HttpStatusCodeGenerator` |
| `bigdecimal`   | BigDecimal support        | No               | LOW                     | bigdecimal crate   |
| `decimal`      | Decimal support           | No               | LOW                     | rust_decimal crate |
| `random_color` | CSS color names           | No               | LOW                     | color_name crate   |
| `geo`          | Geohash support           | ✅ Yes              | ✓ DONE                  | `GeohashGenerator` |
| `semver`       | Semantic versions         | ✅ Yes              | ✓ DONE                  | `SemverGenerator` |

**Analysis**: Fake-rs's modular feature flags minimize dependencies and compilation time. Each flag enables integration with a specific ecosystem crate, following Rust's "pay for what you use"
philosophy. This is a key differentiator from monolithic faker libraries.

### Custom RNG Integration

| Feature            | Fake-rs Support              | krandom Status | Implementation Priority | Notes                   |
|--------------------|------------------------------|----------------|-------------------------|-------------------------|
| Thread-local RNG   | ✅ `.fake()`                  | No (intentional) | SKIP                | Java generators are explicit instances, not global thread-local faker calls |
| Seeded RNG         | ✅ `.fake_with_rng(&mut rng)` | ✅ Yes          | ✓ DONE                  | `GeneratorConfig.seed(...)` |
| Reproducible tests | ✅ Via StdRng::seed_from_u64  | ✅ Yes          | ✓ DONE                  | Seeded configs are deterministic |
| Custom Rng impl    | ✅ Any rand::Rng              | ✅ Yes          | ✓ DONE                  | `GeneratorConfig.Builder.randomFactory(Supplier<? extends Random>)` |

**Analysis**: All fake-rs generators accept any `rand::Rng` implementation, enabling deterministic testing with seeded RNGs. This is critical for reproducible tests and property-based testing
integration.

**Example:**

```rust
use rand::SeedableRng;
use rand::rngs::StdRng;

let mut rng = StdRng::seed_from_u64(42);
let name: String = Name().fake_with_rng(&mut rng);  // Deterministic
```

### Extensibility via Traits

| Feature               | Fake-rs Support         | krandom Status | Implementation Priority | Notes                    |
|-----------------------|-------------------------|----------------|-------------------------|--------------------------|
| Custom `Dummy` impl   | ✅ Manual implementation | No (intentional) | SKIP                | Rust trait — Java equivalent is `ObjectFaker.ruleFor(field, Generator)` |
| Custom config types   | ✅ `Dummy<T>` for any T  | No (intentional) | SKIP                | Rust trait — Java equivalent is per-field generator wiring             |
| Enum support          | ✅ `#[derive(Dummy)]`    | ✅ Yes              | ✓ DONE              | `EnumGenerator` selects random enum constants                          |
| Nested struct support | ✅ Recursive derivation  | ✅ Yes              | ✓ DONE              | `ObjectGenerator` recursively populates nested types                   |

**Analysis**: The `Dummy` trait allows custom types to integrate seamlessly with fake-rs. This is more powerful than callback-based customization in other libraries—types themselves define how to be
faked, enabling composition and reuse.

**Example:**

```rust
struct Point { x: f64, y: f64 }

impl Dummy<Faker> for Point {
    fn dummy_with_rng<R: Rng + ?Sized>(_: &Faker, rng: &mut R) -> Self {
        Point {
            x: rng.gen_range(-90.0..=90.0),
            y: rng.gen_range(-180.0..=180.0),
        }
    }
}

let point: Point = Faker.fake();
```

### Built-in Dummy Implementations

| Type                          | Fake-rs Support      | krandom Status | Notes                 |
|-------------------------------|----------------------|----------------|-----------------------|
| Primitives (i8-i128, u8-u128) | ✅ Full range         | ✅ Yes          | Via Dummy<Faker>      |
| Floats (f32, f64)             | ✅ Full range         | ✅ Yes          |                       |
| bool                          | ✅ 50/50 probability  | ✅ Yes          |                       |
| char                          | ✅ Any valid Unicode  | ✅ Partial      |                       |
| String                        | ✅ Random chars       | ✅ Partial      |                       |
| Option\<T\>                   | ✅ 50% None           | No           | Automatic             |
| Vec\<T\>                      | ✅ Random length      | No           | Automatic             |
| Tuples (up to 10)             | ✅ Each element faked | No           | Automatic composition |

**Analysis**: Fake-rs provides `Dummy<Faker>` for Rust's standard types, enabling generic fake generation. The Option and Vec support is particularly powerful for complex data structures.

---

## IMPLEMENTATION RECOMMENDATIONS

### Phase 1: CRITICAL GAPS (Must Have)

**1.1 Core Trait System (Estimated: 5-7 days)**

- [ ] Design Kotlin equivalent of `Dummy` trait (interface + extension functions)
- [ ] Implement `Fake` trait equivalent for generator types
- [ ] Create RNG abstraction layer compatible with `kotlin.random.Random`
- [ ] Implement basic `Faker` catch-all generator

**Justification**: The trait-based design is fake-rs's core architecture. Adapting this to Kotlin's type system (interfaces, extension functions, reified generics) is foundational.

**1.2 Address & Location (Estimated: 4-5 days)**

- [ ] CityName, CityPrefix, CitySuffix generators
- [ ] StreetName, StreetSuffix, BuildingNumber generators
- [ ] PostCode generator with locale support
- [ ] CountryName, CountryCode generators
- [ ] StateName, StateAbbr generators (US)
- [ ] Latitude, Longitude generators

**Justification**: Address data is fundamental for testing and missing in krandom. Fake-rs provides locale-aware implementations.

**1.3 Internet Fundamentals (Estimated: 3-4 days)**

- [ ] SafeEmail, FreeEmail generators
- [ ] DomainSuffix generator
- [ ] Username, Password(range) generators
- [ ] MACAddress generator

**Justification**: Email and networking data is essential for modern application testing.

**1.4 Lorem Text (Estimated: 2-3 days)**

- [ ] Word, Words(range) generators
- [ ] Sentence(range), Sentences(range) generators
- [ ] Paragraph(range), Paragraphs(range) generators

**Justification**: Lorem ipsum is a universal testing need. The range-based API is more flexible than fixed counts.

**1.5 Date/Time Integration (Estimated: 3-4 days)**

- [ ] DateTime, Date, Time generators (java.time or kotlinx-datetime)
- [ ] DateTimeBefore, DateTimeAfter, DateTimeBetween generators

**Justification**: Temporal data generation is critical for testing time-based logic.

**Phase 1 Total: ~18-23 days**

### Phase 2: HIGH VALUE (Should Have)

**2.1 Advanced Name Features (Estimated: 2-3 days)**

- [ ] NameWithTitle generator
- [ ] Title/Prefix, Suffix generators

**2.2 Company & Business (Estimated: 2-3 days)**

- [ ] CompanyName, CompanySuffix generators
- [ ] Industry, Profession generators
- [ ] BS phrase generators (BsAdj, BsNoun, BsVerb)

**2.3 Job Enhancements (Estimated: 2 days)**

- [ ] JobField, JobSeniority, JobType generators
- [ ] Enhance existing JobTitle with composition

**2.4 Color Generators (Estimated: 2 days)**

- [ ] HexColor, RgbColor, RgbaColor generators
- [ ] HslColor, HslaColor generators

**2.5 Finance Basics (Estimated: 2-3 days)**

- [ ] BIC, ISIN generators
- [ ] CurrencySymbol generator

**2.6 Filesystem (Estimated: 2-3 days)**

- [ ] FilePath, FileName, FileExtension generators
- [ ] DirPath generator
- [ ] MimeType generator

**2.7 Barcode (Estimated: 1-2 days)**

- [ ] ISBN, ISBN10, ISBN13 generators

**Phase 2 Total: ~13-18 days**

### Phase 3: NICE TO HAVE (Could Have)

**3.1 HTTP Integration (Estimated: 1-2 days)**

- [ ] HTTP status code generators
- [ ] UserAgent generator

**3.2 Phone Numbers (Estimated: 2-3 days)**

- [ ] PhoneNumber, CellNumber generators with locale support

**3.3 Advanced Color (Estimated: 1 day)**

- [ ] Color name generator (CSS color names)

**3.4 Geohash (Completed)**

- [x] Geohash generator with precision control

**3.5 Semver (Estimated: 1-2 days)**

- [ ] Semantic version generators

**Phase 3 Total: ~6-10 days**

### Phase 4: ARCHITECTURAL FEATURES (Advanced)

**4.1 Kotlin Annotation Processor / Compiler Plugin (Estimated: 10-15 days)**

- [ ] Design `@Dummy` annotation equivalent to `#[derive(Dummy)]`
- [ ] Implement code generation for annotated data classes
- [ ] Support field-level configuration (`@DummyFaker`, `@DummyDefault`, `@DummyFixed`)

**Justification**: This is the most valuable architectural feature from fake-rs. Kotlin's annotation processing or compiler plugins could achieve similar declarative generation.

**Example target API:**

```kotlin
@Dummy
data class User(
    @DummyFaker("FirstName") val firstName: String,
    @DummyFaker("LastName") val lastName: String,
    @DummyFaker("18..65") val age: Int,
    @DummyDefault val isVerified: Boolean = false
)

val user = Faker.fake<User>()
```

**4.2 Locale Architecture (Estimated: 5-7 days)**

- [ ] Design compile-time or runtime locale system
- [ ] Implement locale data for top 5-10 locales (en, fr, de, es, ja)
- [ ] Locale-aware name, address, phone generators

**Phase 4 Total: ~15-22 days**

---

## ESTIMATED EFFORT

### High Priority Features (Phase 1)

- Core Trait System: **5-7 days**
- Address & Location: **4-5 days**
- Internet Fundamentals: **3-4 days**
- Lorem Text: **2-3 days**
- Date/Time: **3-4 days**
- **Phase 1 Total: 18-23 days**

### Medium Priority Features (Phase 2)

- Advanced Names: **2-3 days**
- Company & Business: **2-3 days**
- Job Enhancements: **2 days**
- Color: **2 days**
- Finance: **2-3 days**
- Filesystem: **2-3 days**
- Barcode: **1-2 days**
- **Phase 2 Total: 13-18 days**

### Low Priority Features (Phase 3)

- HTTP, Phone, Geohash, Semver: **6-10 days**

### Architectural Features (Phase 4)

- Annotation Processing: **10-15 days**
- Locale Architecture: **5-7 days**
- **Phase 4 Total: 15-22 days**

### Total Comprehensive Implementation: **52-73 days** (10-15 weeks)

### Recommended Approach: **Phase 1 + 2 = 31-41 days** (~2 months)

This covers 80% of practical value without entertainment/niche domains fake-rs lacks.

---

## KEY DIFFERENTIATORS

### Fake-rs Strengths (vs krandom)

1. **Trait-Based Architecture**
    - Types opt into fake generation via `Dummy` trait
    - Compile-time verification of fake-able types
    - Zero-cost abstractions through monomorphization

2. **Procedural Macros**
    - `#[derive(Dummy)]` eliminates boilerplate for complex types
    - Field-level configuration (`#[dummy(faker = "...")]`)
    - Automatic composition of nested structures

3. **Locale as Type System**
    - 28 locales as compile-time module paths
    - Type-safe locale selection (no runtime errors)
    - Zero runtime overhead for locale selection

4. **RNG Flexibility**
    - All generators generic over `rand::Rng`
    - Enables deterministic testing with seeded RNGs
    - Critical for reproducible tests and property-based testing

5. **Feature Flags**
    - Pay-for-what-you-use dependency model
    - Minimal compilation footprint by default
    - Clean integration with ecosystem crates (chrono, uuid, http)

6. **Type Safety**
    - Compile-time checking of all fake generation
    - Impossible to generate wrong types (vs reflection-based libraries)
    - Rust's ownership system prevents dangling fake data

7. **Range-Based API**
    - `Words(3..10)` more flexible than fixed counts
    - Rust's `Range` type for natural API
    - Configurable generation without builder pattern

### krandom Strengths (vs Fake-rs)

1. **JVM Ecosystem**
    - Deep integration with Java/Kotlin testing frameworks
    - Mature tooling and IDE support
    - Larger user base and community

2. **Existing Features**
    - Job titles already implemented
    - Currency support already exists
    - IPv4/IPv6 generation complete

3. **Runtime Flexibility**
    - Can dynamically select locales (if implemented)
    - Reflection-based customization
    - More flexible for dynamic use cases

4. **Kotlin-Native**
    - Idiomatic Kotlin API
    - DSL potential for configuration
    - Coroutines integration possible

---

## COMPATIBILITY ASSESSMENT

### Direct Port Feasibility

**High Compatibility:**

- Name, Address, Company, Job modules are straightforward ports
- Internet generators (email, username, password) map directly
- Lorem ipsum text generation is universal
- Color generators are format-based (easy to port)
- Barcode generators (ISBN) are algorithmic

**Medium Compatibility:**

- Trait system requires Kotlin interface/extension function adaptation
- Procedural macros require annotation processing or compiler plugin
- Locale architecture: compile-time vs runtime tradeoff
- RNG abstraction: `rand::Rng` vs `kotlin.random.Random`

**Low Compatibility:**

- Feature flags don't map to Kotlin/JVM (use Gradle dependencies instead)
- Chrono integration needs Java 8 Time or kotlinx-datetime
- Zero-cost abstractions are Rust-specific (JVM has different tradeoffs)

### Recommended Approach

1. **Port Data Generators (Phase 1-2)**
    - Implement address, internet, lorem, company, job, color, filesystem modules
    - Use krandom's existing patterns for generators
    - This provides immediate value without architectural changes

2. **Adapt Trait System (Phase 4)**
    - Design Kotlin-idiomatic trait equivalent using interfaces and extension functions
    - Use reified generics where possible: `inline fun <reified T> Faker.fake(): T`
    - Consider Kotlin's `sealed class` for faker types

3. **Annotation Processing for Code Generation (Phase 4)**
    - Implement `@Dummy` annotation for data classes
    - Generate extension functions for fake generation
    - Support field-level annotations for configuration

4. **Locale System (Phase 4)**
    - Start with runtime locale selection (more Kotlin-idiomatic)
    - Use sealed class hierarchy for locale types
    - Consider compile-time optimization later if needed

**Example Kotlin API:**

```kotlin
// Phase 1-2: Direct generators
val email = SafeEmail().fake()
val name = Name(locale = Locale.EN).fake()
val lorem = Words(3..10).fake()

// Phase 4: Trait-based API
@Dummy
data class User(
    @DummyFaker("FirstName") val firstName: String,
    @DummyFaker("SafeEmail") val email: String,
    @DummyRange(18, 65) val age: Int
)

val user = Faker.fake<User>()
```

---

## CONCLUSION

Fake-rs represents a **fundamentally different approach** to fake data generation compared to imperative libraries like DataFaker or JavaScript faker. Its strengths lie in:

1. **Architectural Sophistication**: Trait-based design, procedural macros, and zero-cost abstractions
2. **Type Safety**: Compile-time verification of all fake generation
3. **Rust Ecosystem Integration**: Feature flags, RNG abstraction, ecosystem crate support
4. **Locale Support**: 28 locales with compile-time selection

While fake-rs has fewer domain-specific generators (17 core modules) compared to DataFaker's 200+ providers, it compensates with **architectural elegance and type safety**.

### Focus Areas for krandom

**Immediate Value (Phase 1-2: 31-41 days):**

- Address & location generators (high testing utility)
- Internet fundamentals (email, username, password)
- Lorem ipsum text generation
- Date/time generators
- Company, job, and color generators
- Filesystem and barcode generators

**Long-Term Architecture (Phase 4: 15-22 days):**

- Annotation processing for `@Dummy` equivalent
- Trait-like system using Kotlin interfaces and extension functions
- Locale system with runtime selection
- RNG abstraction for deterministic testing

**Total Recommended Implementation: Phase 1 + 2 = ~2 months**

Fake-rs proves that fake data generation can be **type-safe, composable, and performant**. Adapting these principles to Kotlin would significantly elevate krandom's architectural quality while
maintaining Kotlin's idiomatic style and JVM ecosystem advantages.
