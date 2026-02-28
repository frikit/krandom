# Chance.js Feature Parity Analysis

## Library Overview

- **Name**: Chance.js
- **Language**: JavaScript / Node.js
- **Version Analyzed**: Latest (2024+)
- **GitHub**: https://github.com/chancejs/chancejs
- **Website**: https://chancejs.com
- **License**: MIT
- **Key Strengths**: Mobile-first support, weighted random, natural language generation, extensive options parameters, minimalist fluent API

## Executive Summary

Chance.js is a minimalist yet powerful random data generator for JavaScript with unique strengths in **weighted random selection**, **normally-distributed values**, **mobile device support**, and *
*rich parameterization**. Unlike object-graph generators, it provides building blocks for manual fixture construction with a clean fluent API. Key differentiators include:

- **Weighted random** - `weighted(['heads', 'tails'], [7, 3])` for biased selection
- **Normal distribution** - Box-Muller transform for realistic statistical data
- **Mobile-first** - UK/US/FR mobile phone formats, device-specific data
- **Rich options** - Every method supports extensive parameter customization
- **Helper methods** - `n()`, `unique()`, `pickset()`, `shuffle()` for collection operations
- **Natural language** - Sentence/paragraph generation with syllable control
- **Reproducible seeding** - Full deterministic test support
- **Likelihood control** - `bool({likelihood: 80})` for probability-based generation

---

## Implementation Status

**Last Updated**: 2026-02-25 (session 5)

### Completed Features ✅

#### Numbers Section (8/8 features - 100% complete)

- ✅ Natural numbers with exclusion - `Generators.ofNaturalNumber().excluding(...)`
- ✅ Prime number generation - `Generators.ofPrime()`
- ✅ Fixed decimal precision - `ofDouble().withPrecision(decimals)`, `ofFloat().withPrecision(decimals)`
- ✅ Normal distribution - `Generators.ofNormal(mean, stdDev)` with Box-Muller transform
- ✅ Random integer - Already existed via `Generators.ofInt()`
- ✅ Random float - Already existed via `Generators.ofFloat()`

**Metrics**:

- Test coverage: 99.7% line, 99.1% branch
- New tests: ~300 comprehensive test cases
- Documentation: Full Javadoc + usage examples
- Pre-commit: All checks passing

#### Booleans Section (2/2 features - 100% complete)

- ✅ Random boolean - `Generators.ofBoolean()`
- ✅ Weighted boolean (likelihood) - `ofBoolean().withLikelihood(percentage)`

**Metrics**:

- Test coverage: 100% for BooleanGenerator
- New tests: 40+ comprehensive test cases covering all likelihood scenarios
- Statistical validation: Empirical probability testing with 5% tolerance
- Pre-commit: All checks passing

#### Characters Section (6/6 features - 100% complete)

- ✅ Random character - `CharGenerator.letters()`, `CharGenerator.digits()`, etc.
- ✅ Custom character pool - `CharGenerator.pool("aeiou")` or `CharGenerator.pool('X', 'Y', 'Z')`
- ✅ Alpha only - `CharGenerator.letters()` or `builder().uppercase().lowercase()`
- ✅ Numeric only - `CharGenerator.digits()` or `builder().digits()`
- ✅ Symbols only - `builder().special()`
- ✅ Case control - `builder().uppercase()` or `builder().lowercase()`

**Metrics**:

- Test coverage: 99.7% line, 99.2% branch
- New tests: 50+ comprehensive test cases
- Support for custom pools (String and varargs)
- Seeded custom pool support
- Pre-commit: All checks passing

#### Strings Section (6/6 features - 100% complete)

- ✅ Random string - `Generators.ofString()` with configurable CharGenerator
- ✅ Variable length - `ofString().minLength(5).maxLength(20)`
- ✅ Fixed length - `ofString().length(10)`
- ✅ Custom pool - `StringGenerator.pool("abc", 5)` generates strings from custom character pool
- ✅ Alpha strings - `ofString(CharGenerator.letters())`
- ✅ Numeric strings - `ofString(CharGenerator.digits())`

**Metrics**:

- Test coverage: 99.8% line, 99.2% branch
- New tests: 40+ comprehensive test cases
- Custom pool support with fixed/variable length
- Seeded generation support (with documented limitations)
- Pre-commit: All checks passing

#### Person Identity Section (4/4 name components complete)

- ✅ Name prefix (title) - `TitleGenerator` with 10 built-in locales (en_US, en_GB, en_AU, fr_FR, de_DE, ja_JP, es_ES, it_IT, pt_BR, zh_CN), extensible via `TitleDataRegistry`
- ✅ Name suffix - `SuffixGenerator` with 10 built-in locales, extensible via `SuffixDataRegistry`
- ✅ First name - `FirstNameGenerator` with 10 locales, gender-aware via `generate(Gender.MALE/FEMALE)`
- ✅ Last name - `LastNameGenerator` with 10 locales, extensible via `LastNameDataRegistry`

**Metrics**:

- Test coverage: 100% branch for `generator.user` package
- Name data loaded from 30 classpath resource files (`krandom/names/`)
- ~75 male + ~75 female first names and ~80–100 last names per locale
- Gender enum (`Gender.MALE`, `Gender.FEMALE`) with gender-neutral `generate()` fallback
- Registry extensibility: custom providers register/override at runtime
- Seeded generation, `generateList()`, and `stream()` all supported

#### Demographics Section (11/11 features complete — 2 krandom extensions beyond Chance.js)

- ✅ Age generation - `AgeGenerator` with `AgeType` enum (CHILD 1-12, TEEN 13-19, ADULT 18-65, SENIOR 65-100)
- ✅ Age ranges - `new AgeGenerator(AgeType.CHILD/TEEN/ADULT/SENIOR)` for type-based bounds
- ✅ Gender labels - `GenderGenerator` with 10 locales (locale-aware: "Male/Female", "Homme/Femme", "Männlich/Weiblich", etc.)
- ✅ Birthday as `LocalDate` - `BirthdayGenerator` with type-based age ranges
- ✅ Birthday as string - `generateAsString()` returns locale-aware format (e.g., `"5/27/1983"` for en_US, `"27.5.1983"` for de_DE, `"1983/5/27"` for ja_JP) — **extends Chance.js** (US-only)
- ✅ American format birthday - `generateAsAmericanString()` returns `MM/dd/yyyy` (e.g., `"05/27/1983"`)
- ✅ Type-based birthday - `new BirthdayGenerator(AgeType.ADULT)` for age-appropriate dates
- ✅ SSN (US) - `new NationalIdGenerator(Locale.US)` via `UsNationalIdProvider`; `AAA-GG-SSSS` (area 666 excluded)
- ✅ SSN format control - `new UsNationalIdProvider().withoutDashes()` / `.lastFourOnly()`
- ✅ **[krandom extension]** Multi-locale national IDs - `NationalIdGenerator` with 10 built-in country providers (see National ID section below)
- ✅ **[krandom extension]** Locale-aware birthday string - `new BirthdayGenerator(Locale.GERMANY).generateAsString()` → `"27.5.1983"`

**Metrics**:

- Test coverage: 100% branch for `generator.user` and `generator.user.nationalid` packages
- `AgeType` enum covers all 4 Chance.js categories with correct ranges
- `GenderGenerator` locale-aware via `GenderDataRegistry` / `LocaleGenderData` / `GenderDataProvider` stack
- `BirthdayGenerator` generates statistically correct birth dates (exact year-window per age)
- `BirthdayGenerator` locale constructors: `new BirthdayGenerator(Locale)`, `(AgeType, Locale)`, `(int, int, Locale)` plus seeded variants; `getLocale()` accessor
- `NationalIdGenerator` replaces `SsnGenerator`; fluent options on `UsNationalIdProvider` preserved
- Seeded generation, `generateList()`, and `stream()` all supported

#### National ID Section (10/10 locales — krandom-unique, no Chance.js equivalent)

- ✅ US SSN - `new NationalIdGenerator(Locale.US)` → `"411-90-0070"` (area 666 excluded per SSA)
- ✅ UK NI number - `new NationalIdGenerator(Locale.UK)` → `"AB 12 34 56 C"` (letter rules + disallowed pairs)
- ✅ AU TFN - `new NationalIdGenerator(Locale.of("en","AU"))` → `"123 456 782"` (mod-11 weighted checksum)
- ✅ FR NIR - `new NationalIdGenerator(Locale.FRANCE)` → 15-digit with control key `97 − (N mod 97)`
- ✅ DE Steuer-ID - `new NationalIdGenerator(Locale.GERMANY)` → 11 digits, ISO 7064 Mod 11,10 check
- ✅ JP My Number - `new NationalIdGenerator(Locale.JAPAN)` → 12 digits, weighted-sum mod-11 check
- ✅ ES DNI - `new NationalIdGenerator(Locale.of("es","ES"))` → `"12345678Z"` (mod-23 letter)
- ✅ IT Codice Fiscale - `new NationalIdGenerator(Locale.ITALY)` → 16-char alphanumeric with check character
- ✅ BR CPF - `new NationalIdGenerator(Locale.of("pt","BR"))` → `"123.456.789-09"` (double mod-11 verifiers)
- ✅ CN Resident ID - `new NationalIdGenerator(Locale.CHINA)` → 18 chars, ISO 7064 Mod 11,2 check

**Metrics**:

- Test coverage: 100% branch for `generator.user.nationalid` package
- Architecture: `NationalIdProvider` interface + `NationalIdRegistry` (ConcurrentHashMap, language-level fallback) + `NationalIdGenerator` facade — mirrors `TitleGenerator` / `TitleDataRegistry`
  pattern exactly
- Registry extensibility: `NationalIdRegistry.register(provider)` adds/overrides any locale at runtime
- All 10 algorithms include verifiable checksums; package-private static helpers expose branch-testable logic
- Seeded generation, `generateList()`, and `stream()` all supported

#### Location Section (6/6 location features complete — country, city, state/province, postal codes, phone numbers, coordinates with locale support)

- ✅ Country names - `CountryGenerator` with 10 locales supporting all 195 UN-recognized countries (193 members + 2 observer states)
    - en_US, en_GB, en_AU: English country names (e.g., "United States", "Germany", "Japan")
    - de_DE: German names (e.g., "Vereinigte Staaten", "Deutschland", "Japan")
    - fr_FR: French names (e.g., "États-Unis", "Allemagne", "Japon")
    - es_ES: Spanish names (e.g., "Estados Unidos", "Alemania", "Japón")
    - it_IT: Italian names (e.g., "Stati Uniti", "Germania", "Giappone")
    - pt_BR: Portuguese names (e.g., "Estados Unidos", "Alemanha", "Japão")
    - ja_JP: Japanese names (e.g., "アメリカ合衆国", "ドイツ", "日本")
    - zh_CN: Simplified Chinese names (e.g., "美国", "德国", "日本")

**Metrics**:

- Test coverage: 100% for `CountryGenerator` and supporting classes
- Architecture: `CountryDataProvider` interface + `CountryDataRegistry` (follows same pattern as other registries) + `CountryGenerator`
- Data: 195 countries per locale loaded from resource files (`krandom/countries/`)
- Official names: Sourced from UN documents and ISO 3166 standards for each language
- Registry extensibility: `CountryDataRegistry.register(provider)` adds/overrides any locale at runtime
- Seeded generation, `generateList()`, and `stream()` all supported

- ✅ City names - `CityGenerator` with 10 locales supporting locale-specific major cities
    - en_US: 247 major US cities (New York, Los Angeles, Chicago, Houston, etc.)
    - en_GB: 101 major UK cities (London, Manchester, Birmingham, Glasgow, etc.)
    - en_AU: 72 major Australian cities (Sydney, Melbourne, Brisbane, Perth, etc.)
    - de_DE: 100 major German cities with proper German spelling (Berlin, München, Köln, etc.)
    - fr_FR: 107 major French cities (Paris, Lyon, Marseille, Toulouse, etc.)
    - es_ES: 84 major Spanish cities (Madrid, Barcelona, Valencia, Sevilla, etc.)
    - it_IT: 99 major Italian cities (Roma, Milano, Napoli, Torino, etc.)
    - pt_BR: 89 major Brazilian cities (São Paulo, Rio de Janeiro, Brasília, etc.)
    - ja_JP: 162 major Japanese cities (東京, 大阪, 京都, 横浜, etc.)
    - zh_CN: 212 major Chinese cities (北京, 上海, 广州, 深圳, etc.)

**Metrics**:

- Test coverage: 100% for `CityGenerator` and supporting classes
- Architecture: `CityDataProvider` interface + `CityDataRegistry` + `CityGenerator` (mirrors CountryGenerator pattern)
- Data: 70-250 cities per locale loaded from resource files (`krandom/cities/`)
- Locale-specific: Each locale returns cities from that country/region (US cities for en_US, German cities for de_DE, etc.)
- Official names: Uses proper local spellings with native characters (München not Munich, 東京 not Tokyo romanization)
- Registry extensibility: `CityDataRegistry.register(provider)` adds/overrides any locale at runtime
- Seeded generation, `generateList()`, and `stream()` all supported

- ✅ State/Province names - `StateGenerator` with 10 locales supporting abbreviations and full names
    - en_US: 51 US states + DC with abbreviations (California/CA, Texas/TX, New York/NY, etc.)
    - en_GB: 4 UK countries (England, Scotland, Wales, Northern Ireland)
    - en_AU: 8 Australian states/territories with abbreviations (New South Wales/NSW, Victoria/VIC, Queensland/QLD, etc.)
    - de_DE: 16 German federal states (Bayern, Nordrhein-Westfalen, Baden-Württemberg, etc.)
    - fr_FR: 18 French regions (Île-de-France, Auvergne-Rhône-Alpes, Nouvelle-Aquitaine, etc.)
    - es_ES: 19 Spanish autonomous communities (Andalucía, Cataluña, Madrid, País Vasco, etc.)
    - it_IT: 20 Italian regions (Toscana, Lombardia, Lazio, Sicilia, etc.)
    - pt_BR: 27 Brazilian states + federal district with abbreviations (São Paulo/SP, Rio de Janeiro/RJ, Minas Gerais/MG, etc.)
    - ja_JP: 47 Japanese prefectures (東京都, 大阪府, 京都府, 北海道, etc.)
    - zh_CN: 34 Chinese province-level divisions (北京市, 上海市, 广东省, 四川省, etc.)

**Metrics**:

- Test coverage: 100% for `StateGenerator` and supporting classes
- Architecture: `StateDataProvider` interface + `StateDataRegistry` + `StateGenerator` (mirrors City/Country pattern)
- Data: 4-51 states/provinces per locale loaded from resource files (`krandom/states/`)
- Dual format support: `generate()` returns full names, `generate(true)` returns abbreviations (when available)
- Locale-specific: Each locale returns states/provinces from that country (US states for en_US, UK countries for en_GB, etc.)
- Official names: Uses proper local spellings (Bayern not Bavaria in de_DE)
- Registry extensibility: `StateDataRegistry.register(provider)` adds/overrides any locale at runtime
- Seeded generation, `generateList()`, and `stream()` all supported

- ✅ Postal codes - `PostalCodeGenerator` with 10 locales generating locale-specific postal code formats
    - en_US: US ZIP codes (5-digit: "90210" or ZIP+4: "90210-1234")
    - en_GB: UK postcodes (realistic formats: "SW1A 2AA", "N1 9GU", "EC1A 1BB")
    - en_AU: Australian postcodes (4 digits: "2000", "3000", "4000")
    - de_DE: German postal codes (5 digits: "10115", "80331", "20095")
    - fr_FR: French postal codes (5 digits: "75001", "69001", "13001")
    - es_ES: Spanish postal codes (5 digits: "28001", "08001", "41001")
    - it_IT: Italian postal codes (5 digits: "00118", "20121", "10121")
    - pt_BR: Brazilian CEP (8 digits with optional hyphen: "01310-100" or "01310100")
    - ja_JP: Japanese postal codes (7 digits with optional hyphen: "100-0001" or "1000001")
    - zh_CN: Chinese postal codes (6 digits: "100000", "200000", "510000")

**Metrics**:

- Test coverage: 100% for `PostalCodeGenerator`
- Architecture: Direct format generation (no registry/provider needed - generates programmatically)
- Format accuracy: Each locale follows official postal code rules and formats
- UK postcodes: Realistic area codes (SW, EC, N, W, etc.) with proper format variations
- Extended formats: `generate(true)` returns ZIP+4 for US, hyphenated formats for Brazil/Japan
- Seeded generation, `generateList()`, and `stream()` all supported

- ✅ Phone numbers - `PhoneNumberGenerator` with 10 locales generating locale-specific phone number formats
    - en_US: US phone numbers (formatted: "(555) 123-4567", unformatted: "5551234567")
    - en_GB: UK landlines ("020 7946 0958") and mobiles ("07700 900123")
    - en_AU: Australian landlines ("02 1234 5678") and mobiles ("0412 345 678")
    - de_DE: German landlines ("030 12345678") and mobiles ("0151 12345678")
    - fr_FR: French landlines ("01 23 45 67 89") and mobiles ("06 12 34 56 78")
    - es_ES: Spanish landlines ("91 123 45 67") and mobiles ("612 34 56 78")
    - it_IT: Italian landlines ("06 1234 5678") and mobiles ("320 123 4567")
    - pt_BR: Brazilian landlines ("(11) 3456-7890") and mobiles ("(11) 98765-4321")
    - ja_JP: Japanese landlines ("03-1234-5678") and mobiles ("090-1234-5678")
    - zh_CN: Chinese landlines ("010-12345678") and mobiles ("138 1234 5678")

**Metrics**:

- Test coverage: 100% for `PhoneNumberGenerator`
- Architecture: Direct format generation (no registry/provider needed - generates programmatically)
- Format accuracy: Each locale follows official phone numbering conventions
- Mobile vs Landline: 8 locales support mobile/landline distinction
- Realistic data: Uses actual area codes and mobile prefixes per country
- Formatted vs Unformatted: `generate(false)` returns digits only, `generate(true)` returns locale-formatted
- Seeded generation, `generateList()`, and `stream()` all supported

- ✅ Coordinates - `CoordinatesGenerator` with 10 locales generating coordinates within geographical bounds
    - en_US: Continental United States (lat: 24.5 to 49.0, lon: -125.0 to -66.0)
    - en_GB: United Kingdom (lat: 49.9 to 60.8, lon: -8.2 to 1.8)
    - en_AU: Australia (lat: -44.0 to -10.0, lon: 113.0 to 154.0)
    - de_DE: Germany (lat: 47.3 to 55.0, lon: 5.9 to 15.0)
    - fr_FR: France (lat: 41.3 to 51.1, lon: -5.2 to 9.6)
    - es_ES: Spain (lat: 36.0 to 43.8, lon: -9.3 to 4.3)
    - it_IT: Italy (lat: 36.6 to 47.1, lon: 6.6 to 18.5)
    - pt_BR: Brazil (lat: -33.7 to 5.3, lon: -74.0 to -34.8)
    - ja_JP: Japan (lat: 24.0 to 45.5, lon: 122.9 to 153.9)
    - zh_CN: China (lat: 18.2 to 53.6, lon: 73.5 to 135.0)

**Metrics**:

- Test coverage: 100% for `CoordinatesGenerator`
- Architecture: Direct generation with geographical bounds per locale
- Precision control: 1-10 decimal places (default: 6 for ~0.1 meter accuracy)
- Geographical accuracy: Coordinates always within country bounding boxes
- Methods: `generateLatitude()`, `generateLongitude()`, `generate()` for coordinate pairs
- Precision variants: `generateLatitude(int precision)`, `generateLongitude(int precision)`
- Seeded generation, `generateList()`, and `stream()` all supported

#### Finance Section (5/5 finance feature groups complete — credit cards, currencies, currency pairs, money amounts & card expiration)

- ✅ **Credit cards** - `CreditCardGenerator` with 6 major card types and Luhn algorithm validation
    - Visa: 16-digit cards (prefix: 4), formatted as "4532 1488 0343 6467"
    - Mastercard: 16-digit cards (prefix: 51-55, 2221-2720), formatted as "5425 2334 3010 9903"
    - American Express: 15-digit cards (prefix: 34, 37), formatted as "3782 822463 10005", 4-digit CVV
    - Discover: 16-digit cards (prefix: 6011, 644-649, 65), formatted as "6011 1111 1111 1117"
    - JCB: 16-digit cards (prefix: 3528-3589), formatted as "3530 1113 3330 0000"
    - Diners Club: 14-digit cards (prefix: 300-305, 36, 38), formatted as "3056 9309 0259 04"

- ✅ **Currency** - `CurrencyGenerator` with ISO 4217 standards and locale awareness
    - 50+ major world currencies with complete ISO 4217 data
    - Primary currencies for 10 supported locales (USD, EUR, GBP, AUD, BRL, JPY, CNY)
    - Full currency information: code, name, symbol, numeric code
    - Locale-specific generation: `generate(locale)` returns primary currency for that locale
    - Methods: `generate()`, `generateWithInfo()`, `getName()`, `getSymbol()`, `getNumericCode()`
    - Examples: USD → "United States Dollar" → "$" → "840"

- ✅ **Card Expiration** - `CardExpirationGenerator` with locale-aware date formatting
    - Future-only mode: Generates dates 1-60 months in the future (default)
    - Flexible mode: Can generate dates up to 60 months in past or future
    - Locale-specific formatting: MM/YY for Western locales, YY/MM for Asian locales
    - Component extraction: `getMonth()` and `getYear()` methods
    - Year formats: 2-digit (default) or 4-digit
    - Methods: `generate()`, `generate(futureOnly)`, `generate(locale)`, `getMonth()`, `getYear()`
    - Examples: "03/26" (US), "26/03" (JP), "07" (month), "2028" (full year)

**Metrics**:

- **Credit Cards**:
    - Test coverage: 100% for `CreditCardGenerator`
    - Architecture: CardType enum + CardInfo record + CreditCardGenerator
    - Luhn validation: All generated cards pass Luhn checksum algorithm
    - CVV generation: 3 digits (most cards), 4 digits (American Express)
    - Expiration dates: MM/YY format, always 1-60 months in the future
    - Card-specific formatting: Amex (4-6-5), Diners (4-6-4), Standard (4-4-4-4)
    - Methods: `generate()`, `generate(boolean formatted)`, `generateWithType()`, `getCvv()`, `getExpirationDate()`
    - Seeded generation, `generateList()`, and `stream()` all supported

- **Currency**:
    - Test coverage: 100% for `CurrencyGenerator`
    - Architecture: Currency enum + CurrencyInfo record + CurrencyGenerator
    - ISO 4217 compliance: All codes, names, symbols, and numeric codes conform to ISO 4217
    - Supported currencies: 50 major world currencies (expandable)
    - Locale support: 10 locales with primary currency mappings
    - Methods: `generate()`, `generate(locale)`, `generateWithInfo()`, `getName()`, `getSymbol()`, `getNumericCode()`
    - Each method has locale-aware variant: `getName(locale)`, `getSymbol(locale)`, etc.
    - Stream support: `stream()`, `streamWithInfo()`, `generateList()`, `generateListWithInfo()`
    - Seeded generation supported for reproducible results

- **Card Expiration**:
    - Test coverage: 100% for `CardExpirationGenerator`
    - Architecture: Standalone generator with YearMonth-based generation
    - Locale support: 10 locales with format variations (MM/YY vs YY/MM)
    - Future-only mode: Configurable at construction time
    - Date range: 1-60 months future (default) or ±60 months (flexible mode)
    - Methods: `generate()`, `generate(futureOnly)`, `generate(locale)`, `generate(locale, futureOnly)`, `getMonth()`, `getYear()`, `getYear(fullYear)`
    - Component methods: All support optional futureOnly parameter
    - Seeded generation supported for reproducible results
    - Stream and list generation inherited from Generator interface

- ✅ **Currency Pairs** - `CurrencyPairGenerator` — random FX currency pairs with locale-aware base
    - Random pair: `generate()` → `"EUR/USD"`, `"JPY/GBP"` (both currencies always differ)
    - Locale-aware base: `generate(Locale.US)` → `"USD/..."`, `generate(Locale.JAPAN)` → `"JPY/..."`
    - Rich pair object: `generateWithInfo()` / `generateWithInfo(Locale)` → `CurrencyPair` record
    - `CurrencyPair` record: `base()`, `quote()` (both `CurrencyInfo`), `toPairString()` → `"EUR/USD"`
    - Validates base ≠ quote in compact constructor; null/unknown locale falls back to random pair
    - Constructors: `CurrencyPairGenerator()`, `CurrencyPairGenerator(config)` (seeded)

- ✅ **Money Amounts** - `MoneyGenerator` — locale-aware formatted monetary amounts
    - Default range `[0, 10 000)` (matches Chance.js `dollar()` default)
    - Locale-aware: `generate()` uses instance locale (default `en_US` → `"$4,231.87"`)
    - Locale override: `generate(Locale.GERMANY)` → `"4.231,87 €"`, `generate(Locale.JAPAN)` → `"￥4,232"`
    - Max control: `generate(100.0)`, `generateDollar(50.0)`, `generateEuro(500.0)`
    - Dollar helper: `generateDollar()` → `"$4,231.87"` (always USD regardless of instance locale)
    - Euro helper: `generateEuro()` → `"4 231,87 €"` (always EUR/France formatting)
    - Constructors: `MoneyGenerator()`, `MoneyGenerator(Locale)`, `MoneyGenerator(config)` (seeded)
    - Unrecognized locales fall back to USD formatting

**Metrics (Currency Pairs + Money)**:

- Test coverage: 100% line and branch for both generators (`generator.finance` package)
- Finance package now at 100% line and branch coverage
- `MoneyGenerator` uses `NumberFormat.getCurrencyInstance(locale)` for locale-correct symbols, thousand/decimal separators, and decimal-place counts
- `CurrencyPairGenerator` retry loop covered via 2000-iteration probabilistic test
- Seeded generation, `generateList()`, and `stream()` all supported

#### User Section (1/1 user feature complete — email generation with locale-aware names)

- ✅ **Email** - `EmailGenerator` with locale-aware names and multiple formats
    - 5 email formats: firstname.lastname, firstnamelastname, jsmith, firstname_lastname, lastname.firstname
    - 12 popular domains: gmail.com, yahoo.com, outlook.com, hotmail.com, icloud.com, protonmail.com, mail.com, aol.com, zoho.com, gmx.com, yandex.com, qq.com
    - Custom domain support: `generate("example.com")`
    - Format control: `generate(EmailFormat.FIRSTNAME_DOT_LASTNAME)`
    - Locale-aware name generation: Uses FirstNameGenerator and LastNameGenerator
    - Supports all 10 locales (en_US, en_GB, en_AU, de_DE, fr_FR, es_ES, it_IT, pt_BR, ja_JP, zh_CN)
    - Examples: "john.smith@gmail.com", "jsmith@yahoo.com", "mueller.hans@gmail.com"

**Metrics**:

- **Email**:
    - Test coverage: 100% for `EmailGenerator`
    - Architecture: EmailFormat enum + EmailGenerator + Integration with name generators
    - Supported formats: 5 common email formats
    - Popular domains: 12 major email providers
    - Locale support: 10 locales with appropriate names for each
    - Methods: `generate()`, `generate(domain)`, `generate(format)`, `generate(format, domain)`
    - Name integration: Integrates with FirstNameGenerator and LastNameGenerator
    - Format variations: Supports dots, underscores, initial-only, and name order variations
    - Seeded generation supported for reproducible results
    - Stream and list generation inherited from Generator interface

#### Network Section (4/4 network features complete — domain, URL, IPv4 & IPv6 generators with seeding support)

- ✅ **Domain** - `DomainGenerator` with popular and locale-specific TLDs
    - 12 popular TLDs: com, net, org, io, co, dev, app, tech, online, site, xyz, pro
    - 10 locale-specific country-code TLDs: us, uk, au, de, fr, es, it, br, jp, cn
    - Random TLD selection: Mix of popular and locale-specific based on configuration
    - Custom TLD support: `generate("io")` → "techcloud.io"
    - TLD-only generation: `getTLD()` → "com"
    - Popular TLD access: `getPopularTLD()` → Always from popular set
    - Locale TLD access: `getLocaleTLD()` → Locale-specific or null
    - Domain name generation: 1-2 word combinations from 33-word dictionary
    - Examples: "techcloud.com", "datahub.io", "secureprime.de"

- ✅ **URL** - `URLGenerator` with protocols, paths, and query parameters
    - 5 protocols: http, https, ftp, ws, wss
    - Domain integration: Uses DomainGenerator for realistic domains
    - Path generation: 1-3 segments from 20-word path dictionary
    - Query parameter generation: 1-3 parameters from 12-param dictionary
    - Protocol control: `generate("https")` → "https://..."
    - Path URLs: `generateWithPath()` → "https://example.com/api/users"
    - Full URLs with query: `generateWithPathAndQuery()` → "https://example.com/api/users?id=123&page=1"
    - Component access: `getProtocol()`, `getPath()`, `getQueryString()`
    - Locale-aware: Domain TLD influenced by locale configuration
    - Examples: "https://techcloud.com", "ftp://datahub.io/files", "wss://api.example.de/v1?sort=asc"

- ✅ **IPv4** - `IPv4Generator` with RFC 791 compliance and seeding support
    - RFC 791 compliant dotted-decimal notation
    - First octet restricted to [0, 223] - excludes multicast (224-239) and reserved (240-255)
    - Octets 2-4 in full [0, 255] range
    - Seeded generation for reproducible results
    - Constructors: `IPv4Generator()`, `IPv4Generator(config)`
    - Examples: "192.168.1.1", "10.0.0.5", "172.16.254.1"

- ✅ **IPv6** - `IPv6Generator` with RFC 4291/5952 compliance and seeding support
    - RFC 4291 §2.2 - 128-bit address as eight 16-bit groups
    - RFC 5952 §4.1 - Leading zeros suppressed within groups
    - RFC 5952 §4.3 - Lowercase hexadecimal digits
    - Intentionally omits :: compression (random addresses rarely have consecutive zeros)
    - Seeded generation for reproducible results
    - Constructors: `IPv6Generator()`, `IPv6Generator(config)`
    - Examples: "2001:db8:85a3:0:0:8a2e:370:7334", "fe80:0:0:0:204:61ff:fe9d:f156"

**Metrics**:

- **Domain**:
    - Test coverage: 97.0% branch coverage for `DomainGenerator`
    - Total tests: 50 comprehensive tests
    - Popular TLDs: 12 common top-level domains
    - Locale TLDs: 10 country-code domains matching supported locales
    - Methods: `generate()`, `generate(tld)`, `getTLD()`, `getPopularTLD()`, `getLocaleTLD()`
    - Domain words: 33-word dictionary for realistic domain names
    - Single/double word generation: Random 1-2 word combinations
    - Seeded generation supported for reproducible results

- **URL**:
    - Test coverage: 93.9% branch coverage for `URLGenerator`
    - Total tests: 68 comprehensive tests
    - Protocols: 5 standard protocols (http, https, ftp, ws, wss)
    - Path segments: 20-word dictionary for paths
    - Query parameters: 12-param dictionary for realistic URLs
    - Methods: `generate()`, `generate(protocol)`, `generateWithPath()`, `generateWithPathAndQuery()`, `getProtocol()`, `getPath()`, `getQueryString()`
    - Path variation: 1-3 segments per path
    - Query variation: 1-3 parameters per query string
    - Integration: Uses DomainGenerator for realistic domain generation
    - Seeded generation supported for reproducible results

- **IPv4**:
    - Test coverage: 100% branch coverage for `IPv4Generator`
    - Total tests: 70+ comprehensive tests (including format validation, range checking, seeded generation)
    - RFC compliance: RFC 791 dotted-decimal notation
    - Address validation: All generated addresses pass Apache Commons InetAddressValidator
    - Octet ranges: First [0-223], others [0-255]
    - Methods: `generate()`
    - Seeded generation supported for reproducible results
    - Supports GeneratorConfig for seeding

- **IPv6**:
    - Test coverage: 100% branch coverage for `IPv6Generator`
    - Total tests: 70+ comprehensive tests (including format validation, RFC compliance, seeded generation)
    - RFC compliance: RFC 4291 §2.2, RFC 5952 §4.1 & §4.3
    - Address validation: All generated addresses pass Apache Commons InetAddressValidator
    - Format: Eight 16-bit groups in lowercase hexadecimal
    - Methods: `generate()`
    - Seeded generation supported for reproducible results
    - Supports GeneratorConfig for seeding

#### Color Section (6/6 color features complete — color generation with multiple formats)

- ✅ **Color** - `ColorGenerator` with multiple output formats
    - 4 color formats: HEX (#79c157), SHORT_HEX (#60f), RGB (rgb(110,52,164)), HEX_0X (0x79c157)
    - Grayscale support: All RGB components equal for shades of gray
    - Case control: Uppercase and lowercase hex letters
    - Format control: `generate(ColorFormat.HEX)` → "#79c157"
    - Grayscale generation: `generateGrayscale()` → "#e2e2e2"
    - Uppercase generation: `generateUppercase()` → "#79C157"
    - Constructors: `ColorGenerator()`, `ColorGenerator(config)`
    - Examples: "#79c157", "#60f", "rgb(110,52,164)", "0x79c157", "#e2e2e2"

**Metrics**:

- **Color**:
    - Test coverage: 100% branch coverage for `ColorGenerator`
    - Total tests: 35 comprehensive tests
    - Color formats: 4 output formats (HEX, SHORT_HEX, RGB, HEX_0X)
    - Methods: `generate()`, `generate(format)`, `generateGrayscale()`, `generateGrayscale(format)`, `generateUppercase()`, `generateUppercase(format)`
    - RGB component range: All components [0-255]
    - Hex validation: Proper # prefix for HEX/SHORT_HEX, 0x prefix for HEX_0X
    - Grayscale: R=G=B for all grayscale colors
    - Case control: Uppercase converts hex letters (a-f) to (A-F) while keeping 0x prefix lowercase
    - Seeded generation supported for reproducible results

#### DateTime Section (18/18 date/time features complete — comprehensive date and time generation)

- ✅ **DateGenerator** - Date generation and date components
    - Date generation: Random dates between 1970-2100
    - Multiple formats: ISO (YYYY-MM-DD), American (MM/DD/YYYY), European (DD/MM/YYYY)
    - Constrained dates: Generate with specific year, month, or day
    - Date components: Year, month (1-12), month names, day
    - Unix timestamps: Generate Unix timestamps in seconds (midnight)
    - Methods: `generate()`, `generateString()`, `generateAmerican()`, `generateEuropean()`
    - Constraint methods: `generateWithYear()`, `generateWithMonth()`, `generateWithDay()`
    - Component methods: `generateYear()`, `generateMonth()`, `generateMonthName()`, `generateTimestamp()`
    - Leap year handling: Properly handles February in leap/non-leap years
    - Constructors: `DateGenerator()`, `DateGenerator(config)`
    - Examples: "2078-05-27", "05/27/2078", "27/05/2078", "October", 1482975167
    - Returns: LocalDate objects for date manipulation

- ✅ **TimeGenerator** - Time generation and time components
    - Time generation: Random times with hour, minute, second, millisecond precision
    - Time string format: ISO format (HH:MM:SS)
    - Time components: Hour (12/24), minute, second, millisecond, AM/PM
    - Methods: `generate()`, `generateString()`
    - Component methods: `generateHour()`, `generateHour24()`, `generateMinute()`, `generateSecond()`, `generateMillisecond()`, `generateAmPm()`
    - Hour formats: 12-hour (1-12) and 24-hour (0-23)
    - Constructors: `TimeGenerator()`, `TimeGenerator(config)`
    - Examples: LocalTime objects, "14:23:45", hour=14, minute=23, second=45, millisecond=123, "am"/"pm"
    - Returns: LocalTime objects for time manipulation

**Metrics**:

- **DateGenerator**:
    - Test coverage: 100% branch coverage for `DateGenerator`
    - Total tests: 33 comprehensive tests
    - Date range: 1970-2100 (131 years)
    - Date formats: 3 formats (ISO, American, European)
    - Methods: 11 generation methods
    - Month names: 12 full month names (January-December)
    - Leap year support: Proper handling of February 29th
    - Validation: All dates are valid (no invalid combinations like Feb 30)
    - Seeded generation supported for reproducible results

- **TimeGenerator**:
    - Test coverage: 100% branch coverage for `TimeGenerator`
    - Total tests: 25 comprehensive tests
    - Time components: Hour (12/24), minute (0-59), second (0-59), millisecond (0-999)
    - Methods: 8 generation methods
    - Hour formats: Both 12-hour (1-12) and 24-hour (0-23) formats
    - Precision: Millisecond precision via LocalTime nanoseconds
    - AM/PM: Random "am" or "pm" generation
    - Time strings: ISO format (HH:MM:SS)
    - Seeded generation supported for reproducible results

#### UUID Section (3/3 UUID features complete — RFC 4122 compliant UUID generation)

- ✅ **UUIDGenerator** - UUID version 4 and version 5 generation
    - UUID v4: Random UUIDs using RFC 4122 §4.4 specification
    - UUID v5: Name-based UUIDs using SHA-1 hashing per RFC 4122 §4.3
    - Standard namespaces: DNS and URL namespaces from RFC 4122
    - Version control: Explicit methods for v4 and v5 generation
    - String generation: `generateString()`, `generateV4String()`, `generateV5String()`
    - UUID generation: `generate()` → UUIDv4, `generateV4()`, `generateV5(name)`
    - Custom namespaces: `generateV5(namespace, name)` with any UUID namespace
    - Namespace access: `getDnsNamespace()`, `getUrlNamespace()`
    - Constructors: `UUIDGenerator()`, `UUIDGenerator(config)`
    - Returns: Java UUID objects for direct use
    - Examples: "f47ac10b-58cc-4372-a567-0e02b2c3d479" (v4), deterministic v5 from names

**Metrics**:

- **UUIDGenerator**:
    - Test coverage: 100% branch coverage for `UUIDGenerator`
    - Total tests: 36 comprehensive tests
    - UUID versions: 2 versions (v4 random, v5 name-based)
    - Methods: 11 generation methods
    - RFC compliance: RFC 4122 §4.3 (v5) and §4.4 (v4)
    - Namespaces: 2 standard namespaces (DNS, URL)
    - Format validation: Proper version bits (4 or 5) and variant bits (10x)
    - Determinism: V5 UUIDs are deterministic for same namespace+name
    - Seeded generation: V4 supports seeded reproducible generation
    - Thread safety: Safe for concurrent use

#### Hash Section (4/4 hash features complete — hexadecimal hash string generation)

- ✅ **HashGenerator** - Random hexadecimal hash string generation
    - Default length: 40 characters (SHA-1 compatible)
    - Custom lengths: Any positive length via `generate(length)`
    - Case control: Lowercase (default) or uppercase
    - Hash generation: `generate()` → "d96eb2e7e85c3f4f4a7f0e51f5f3b9c6d89c3f4f"
    - Custom length: `generate(16)` → "d96eb2e7e85c3f4f"
    - Uppercase: `generateUppercase()` → "D96EB2E7E85C3F4F4A7F0E51F5F3B9C6D89C3F4F"
    - Uppercase custom: `generateUppercase(32)` → "D96EB2E7E85C3F4F4A7F0E51F5F3B9C6"
    - Hash formats: Compatible with SHA-1 (40), MD5 (32), SHA-256 (64)
    - Constructors: `HashGenerator()`, `HashGenerator(config)`
    - Returns: Hexadecimal strings (0-9, a-f or A-F)
    - Examples: "a3f9c2d5...", "F9A3C2D5..." (uppercase)

**Metrics**:

- **HashGenerator**:
    - Test coverage: 100% branch coverage for `HashGenerator`
    - Total tests: 31 comprehensive tests
    - Hash lengths: 1 to 1000+ characters supported
    - Case formats: 2 formats (lowercase, uppercase)
    - Methods: 4 generation methods
    - Hex validation: All characters are valid hexadecimal (0-9, a-f or A-F)
    - Uniqueness: All generated hashes are statistically unique
    - Common formats: SHA-1 (40), MD5 (32), SHA-256 (64) compatible
    - Seeded generation: Reproducible hashes via GeneratorConfig
    - Thread safety: Safe for concurrent use

### In Progress

_None - awaiting next feature selection_

### Planned

- Natural language (words, sentences, paragraphs)
- Street address generation
- Helper methods (n, unique, pick, pickset, shuffle, weighted)

---

## Feature Categories

### 1. NUMBERS

| Feature                 | Chance.js Support                | krandom Status | Implementation Priority | Notes                               |
|-------------------------|----------------------------------|----------------|-------------------------|-------------------------------------|
| **Integer Generation**  |
| Random integer          | ✅ `integer({min, max})`          | ✅ Yes          | ✓ DONE                  | `Generators.ofInt()`                |
| Natural numbers (≥0)    | ✅ `natural({min, max, exclude})` | ✅ Yes          | ✓ DONE                  | `Generators.ofNaturalNumber()`      |
| Prime numbers           | ✅ `prime({min, max})`            | ✅ Yes          | ✓ DONE                  | `Generators.ofPrime()`              |
| **Floating Point**      |
| Random float            | ✅ `floating({min, max, fixed})`  | ✅ Yes          | ✓ DONE                  | `Generators.ofFloat()`              |
| Fixed decimal places    | ✅ `fixed` parameter              | ✅ Yes          | ✓ DONE                  | `withPrecision(decimals)`           |
| **Statistical**         |
| Normal distribution     | ✅ `normal({mean, dev})`          | ✅ Yes          | ✓ DONE                  | `Generators.ofNormal(mean, stdDev)` |
| Standard deviation      | ✅ `dev` parameter                | ✅ Yes          | ✓ DONE                  | Second parameter in ofNormal()      |
| **Exclusion**           |
| Exclude specific values | ✅ `natural({exclude: [1,2,3]})`  | ✅ Yes          | ✓ DONE                  | `ofNaturalNumber().excluding(...)`  |

### 2. BOOLEANS & BASIC TYPES

| Feature               | Chance.js Support                                           | krandom Status | Implementation Priority | Notes                                    |
|-----------------------|-------------------------------------------------------------|----------------|-------------------------|------------------------------------------|
| **Boolean**           |
| Random boolean        | ✅ `bool()`                                                  | ✅ Yes          | ✓ DONE                  | `Generators.ofBoolean()`                 |
| Weighted boolean      | ✅ `bool({likelihood: 80})`                                  | ✅ Yes          | ✓ DONE                  | `ofBoolean().withLikelihood(%)` - UNIQUE |
| **Characters**        |
| Random character      | ✅ `character({pool, alpha, numeric, symbols, casing})`      | ✅ Yes          | ✓ DONE                  | `CharGenerator` with builder/factories   |
| Custom character pool | ✅ `character({pool: 'aeiou'})`                              | ✅ Yes          | ✓ DONE                  | `CharGenerator.pool("aeiou")`            |
| Alpha only            | ✅ `character({alpha: true})`                                | ✅ Yes          | ✓ DONE                  | `CharGenerator.letters()`                |
| Numeric only          | ✅ `character({numeric: true})`                              | ✅ Yes          | ✓ DONE                  | `CharGenerator.digits()`                 |
| Symbols only          | ✅ `character({symbols: true})`                              | ✅ Yes          | ✓ DONE                  | `builder().special()`                    |
| Case control          | ✅ `character({casing: 'upper'/'lower'})`                    | ✅ Yes          | ✓ DONE                  | `builder().uppercase()`/`lowercase()`    |
| **Strings**           |
| Random string         | ✅ `string({length, pool, casing, alpha, numeric, symbols})` | ✅ Yes          | ✓ DONE                  | `Generators.ofString()`                  |
| Variable length       | ✅ Default 5-20 random                                       | ✅ Yes          | ✓ DONE                  | `ofString().minLength().maxLength()`     |
| Fixed length          | ✅ `string({length: 10})`                                    | ✅ Yes          | ✓ DONE                  | `ofString().length(10)`                  |
| Custom pool           | ✅ `string({pool: 'abc', length: 5})`                        | ✅ Yes          | ✓ DONE                  | `StringGenerator.pool("abc", 5)`         |
| Alpha strings         | ✅ `string({alpha: true})`                                   | ✅ Yes          | ✓ DONE                  | `ofString(CharGenerator.letters())`      |
| Numeric strings       | ✅ `string({numeric: true})`                                 | ✅ Yes          | ✓ DONE                  | `ofString(CharGenerator.digits())`       |

### 3. PERSON IDENTITY

| Feature                                                        | Chance.js Support                                                       | krandom Status | Implementation Priority | Notes                                                                               |
|----------------------------------------------------------------|-------------------------------------------------------------------------|----------------|-------------------------|-------------------------------------------------------------------------------------|
| **Names**                                                      |                                                                         |                |                         |                                                                                     |
| Full name                                                      | ✅ `name({middle, middle_initial, prefix, suffix, gender, nationality})` | ✅ Partial      | MEDIUM                  | Middle + middle-initial supported; nationality remains locale-based                 |
| First name                                                     | ✅ `first({gender, nationality})`                                        | ✅ Yes          | ✓ DONE                  | `FirstNameGenerator` (10 locales, gender-aware)                                     |
| Last name                                                      | ✅ `last()`                                                              | ✅ Yes          | ✓ DONE                  | `LastNameGenerator` (10 locales)                                                    |
| Middle name                                                    | ✅ `name({middle: true})`                                                | ✅ Yes          | ✓ DONE                  | `MiddleNameGenerator` + `FullNameGenerator.generateWithMiddleName(...)`             |
| Middle initial                                                 | ✅ `name({middle_initial: true})`                                        | ✅ Yes          | ✓ DONE                  | `MiddleNameGenerator.generateInitial(...)` + full-name middle initial support       |
| Name prefix                                                    | ✅ `prefix({gender})`                                                    | ✅ Yes          | ✓ DONE                  | `TitleGenerator` (10 locales, no gender filter)                                     |
| Name suffix                                                    | ✅ `suffix()`                                                            | ✅ Yes          | ✓ DONE                  | `SuffixGenerator` (10 locales)                                                      |
| Gender-specific names                                          | ✅ `first({gender: 'male'/'female'})`                                    | ✅ Yes          | ✓ DONE                  | `gen.generate(Gender.MALE/FEMALE)`                                                  |
| Nationality support                                            | ✅ `name({nationality: 'en'/'it'})`                                      | ✅ Partial      | MEDIUM                  | Via locale (10 built-in), not a string param                                        |
| US nationality                                                 | ✅ `first({nationality: 'us'})`                                          | ✅ Yes          | ✓ DONE                  | `new FirstNameGenerator(Locale.US)`                                                 |
| Italian nationality                                            | ✅ `first({nationality: 'it'})`                                          | ✅ Yes          | ✓ DONE                  | `new FirstNameGenerator(Locale.of("it","IT"))`                                      |
| **Demographics**                                               |                                                                         |                |                         |                                                                                     |
| Age                                                            | ✅ `age({type: 'child'/'teen'/'adult'/'senior'})`                        | ✅ Yes          | ✓ DONE                  | `AgeGenerator` with `AgeType` enum (CHILD/TEEN/ADULT/SENIOR)                        |
| Age ranges                                                     | ✅ child(1-12), teen(13-19), adult(18-65), senior(65-100)                | ✅ Yes          | ✓ DONE                  | `new AgeGenerator(AgeType.CHILD)`                                                   |
| Gender                                                         | ✅ `gender()`                                                            | ✅ Yes          | ✓ DONE                  | `GenderGenerator` (10 locales, locale-aware)                                        |
| Birthday                                                       | ✅ `birthday({type, string, american})`                                  | ✅ Yes          | ✓ DONE                  | `BirthdayGenerator` returns `LocalDate`                                             |
| Birthday as string                                             | ✅ `birthday({string: true})`                                            | ✅ Yes          | ✓ DONE                  | `generateAsString()` → '5/27/1983'                                                  |
| American format                                                | ✅ `birthday({american: true})`                                          | ✅ Yes          | ✓ DONE                  | `generateAsAmericanString()` → '05/27/1983'                                         |
| Type-based birthday                                            | ✅ `birthday({type: 'adult'})`                                           | ✅ Yes          | ✓ DONE                  | `new BirthdayGenerator(AgeType.ADULT)`                                              |
| **ID Numbers**                                                 |                                                                         |                |                         |                                                                                     |
| SSN (US)                                                       | ✅ `ssn({ssnFour, dashes})`                                              | ✅ Yes          | ✓ DONE                  | `new NationalIdGenerator(Locale.US)` via `UsNationalIdProvider` (area 666 excluded) |
| Last 4 SSN                                                     | ✅ `ssn({ssnFour: true})`                                                | ✅ Yes          | ✓ DONE                  | `new UsNationalIdProvider().lastFourOnly().generate(random)`                        |
| SSN format control                                             | ✅ `ssn({dashes: false})`                                                | ✅ Yes          | ✓ DONE                  | `new UsNationalIdProvider().withoutDashes().generate(random)`                       |
| **National IDs (krandom extension — no Chance.js equivalent)** |                                                                         |                |                         |                                                                                     |
| UK NI number                                                   | ❌ Not in Chance.js                                                      | ✅ Yes          | ✓ DONE                  | `new NationalIdGenerator(Locale.UK)` → `"AB 12 34 56 C"`                            |
| AU TFN                                                         | ❌ Not in Chance.js                                                      | ✅ Yes          | ✓ DONE                  | `new NationalIdGenerator(Locale.of("en","AU"))` → `"123 456 782"`                   |
| FR NIR                                                         | ❌ Not in Chance.js                                                      | ✅ Yes          | ✓ DONE                  | `new NationalIdGenerator(Locale.FRANCE)` → 15-digit                                 |
| DE Steuer-ID                                                   | ❌ Not in Chance.js                                                      | ✅ Yes          | ✓ DONE                  | `new NationalIdGenerator(Locale.GERMANY)` → 11 digits                               |
| JP My Number                                                   | ❌ Not in Chance.js                                                      | ✅ Yes          | ✓ DONE                  | `new NationalIdGenerator(Locale.JAPAN)` → 12 digits                                 |
| ES DNI                                                         | ❌ Not in Chance.js                                                      | ✅ Yes          | ✓ DONE                  | `new NationalIdGenerator(Locale.of("es","ES"))` → `"12345678Z"`                     |
| IT Codice Fiscale                                              | ❌ Not in Chance.js                                                      | ✅ Yes          | ✓ DONE                  | `new NationalIdGenerator(Locale.ITALY)` → 16 chars                                  |
| BR CPF                                                         | ❌ Not in Chance.js                                                      | ✅ Yes          | ✓ DONE                  | `new NationalIdGenerator(Locale.of("pt","BR"))` → `"123.456.789-09"`                |
| CN Resident ID                                                 | ❌ Not in Chance.js                                                      | ✅ Yes          | ✓ DONE                  | `new NationalIdGenerator(Locale.CHINA)` → 18 chars                                  |
| Custom locale ID                                               | ❌ Not in Chance.js                                                      | ✅ Yes          | ✓ DONE                  | `NationalIdRegistry.register(provider)` at runtime                                  |

### 4. LOCATION & ADDRESS

| Feature            | Chance.js Support                       | krandom Status | Implementation Priority | Notes                    |
|--------------------|-----------------------------------------|----------------|-------------------------|--------------------------|
| **Street Address** |
| Full address       | ✅ `address({short_suffix})`             | ✅ Yes          | ✓ DONE                  | Locale-aware `StreetAddressGenerator` (10 locales) |
| Short suffix       | ✅ `address({short_suffix: true})`       | ✅ Yes          | ✓ DONE                  | `generate(true)` short suffix, `generate(false)` long suffix |
| **City**           |
| City name          | ✅ `city()`                              | ✅ Yes          | HIGH                    | Random city names        |
| **State/Province** |
| State abbreviation | ✅ `state()`                             | ✅ Yes          | HIGH                    | 'AK', 'CA', 'TX'         |
| State full name    | ✅ `state({full: true})`                 | ✅ Yes          | HIGH                    | 'Florida', 'Alaska'      |
| Locale support     | ✅ `state({country: 'us'/'ca'/'au'})`    | ✅ Yes          | MEDIUM                  | US/CA/AU/DE/MX/IT states |
| **Postal Codes**   |
| Postal code        | ✅ `zip()`/`postal()`/`postcode()`       | ✅ Yes          | HIGH                    | Locale-specific formats  |
| Extended formats   | ✅ `generate(true)` for ZIP+4/hyphenated | ✅ Yes          | MEDIUM                  | '90210-1234', '100-0001' |
| **Country**        |
| Country code       | ✅ `country()`                           | ✅ Yes          | HIGH                    | 'DE', 'FR', 'US'         |
| Country full name  | ✅ `country({full: true})`               | ✅ Yes          | HIGH                    | 'Germany'                |
| **Phone Numbers**  |
| Phone number       | ✅ `phone({formatted, country, mobile})` | ✅ Yes          | HIGH                    | Multi-country support    |
| Formatted          | ✅ `generate()`                          | ✅ Yes          | HIGH                    | '(555) 123-4567'         |
| Unformatted        | ✅ `generate(false)`                     | ✅ Yes          | MEDIUM                  | '5551234567'             |
| Mobile vs Landline | ✅ `generate(true, true)` for mobile     | ✅ Yes          | MEDIUM                  | Locale-specific          |
| **Coordinates**    |
| Latitude           | ✅ `generateLatitude()`                  | ✅ Yes          | MEDIUM                  | Locale-bounded           |
| Longitude          | ✅ `generateLongitude()`                 | ✅ Yes          | MEDIUM                  | Locale-bounded           |
| Decimal precision  | ✅ `generateLatitude(7)`                 | ✅ Yes          | MEDIUM                  | 1-10 decimal places      |
| Locale bounds      | ✅ Based on locale                       | ✅ Yes          | MEDIUM                  | Within country bounds    |
| Coordinates pair   | ✅ `generate()`                          | ✅ Yes          | MEDIUM                  | '35.12423,-80.12345'     |

### 5. FINANCE

| Feature             | Chance.js Support                                 | krandom Status | Implementation Priority | Notes                                              |
|---------------------|---------------------------------------------------|----------------|-------------------------|----------------------------------------------------|
| **Credit Cards**    |
| Credit card number  | ✅ `cc({type})`                                    | ✅ Yes          | HIGH                    | Luhn-valid                                         |
| Visa                | ✅ `generate(CardType.VISA)`                       | ✅ Yes          | HIGH                    | 16 digits                                          |
| Mastercard          | ✅ `generate(CardType.MASTERCARD)`                 | ✅ Yes          | HIGH                    | 16 digits                                          |
| American Express    | ✅ `generate(CardType.AMEX)`                       | ✅ Yes          | HIGH                    | 15 digits, 4-digit CVV                             |
| All major cards     | ✅ 6 card types (Visa/MC/Amex/Discover/JCB/Diners) | ✅ Yes          | HIGH                    | Proper formatting                                  |
| CVV/CVC             | ✅ `getCvv()`                                      | ✅ Yes          | MEDIUM                  | 3/4 digits based on type                           |
| Expiration date     | ✅ `getExpirationDate()`                           | ✅ Yes          | HIGH                    | MM/YY format, future                               |
| Card info object    | ✅ `generateWithType()`                            | ✅ Yes          | MEDIUM                  | CardInfo with all details                          |
| **Currency**        |
| Currency object     | ✅ `generateWithInfo()`                            | ✅ Yes          | MEDIUM                  | CurrencyInfo with code/name/symbol                 |
| Currency code       | ✅ `generate()`                                    | ✅ Yes          | HIGH                    | ISO 4217 codes: USD, EUR, GBP                      |
| Currency name       | ✅ `getName()`                                     | ✅ Yes          | MEDIUM                  | "United States Dollar", "Euro"                     |
| Currency symbol     | ✅ `getSymbol()`                                   | ✅ Yes          | MEDIUM                  | $, €, £, ¥                                         |
| Numeric code        | ✅ `getNumericCode()`                              | ✅ Yes          | MEDIUM                  | ISO 4217 numeric: 840, 978                         |
| Locale support      | ✅ `generate(locale)`                              | ✅ Yes          | HIGH                    | 10 locales with primary currencies                 |
| Currency pair       | ✅ `currency_pair()`                               | ✅ Yes          | ✓ DONE                  | `CurrencyPairGenerator` — `"EUR/USD"`, locale base |
| Dollar amount       | ✅ `dollar({max})`                                 | ✅ Yes          | ✓ DONE                  | `MoneyGenerator.generateDollar()` → `"$4,231.87"`  |
| Euro amount         | ✅ `euro({max})`                                   | ✅ Yes          | ✓ DONE                  | `MoneyGenerator.generateEuro()` → `"4 231,87 €"`   |
| Max amount control  | ✅ `dollar({max: 20})`                             | ✅ Yes          | ✓ DONE                  | `generateDollar(50.0)`, `generate(Locale, max)`    |
| **Card Expiration** |
| Expiration date     | ✅ `generate()`                                    | ✅ Yes          | HIGH                    | MM/YY format (03/26)                               |
| Future expiration   | ✅ `generate(true)`                                | ✅ Yes          | HIGH                    | Guaranteed future dates                            |
| Expiration month    | ✅ `getMonth()`                                    | ✅ Yes          | MEDIUM                  | Zero-padded (01-12)                                |
| Expiration year     | ✅ `getYear()`                                     | ✅ Yes          | MEDIUM                  | 2-digit (26) or 4-digit (2026)                     |
| Locale formatting   | ✅ `generate(locale)`                              | ✅ Yes          | MEDIUM                  | MM/YY (West) or YY/MM (Asia)                       |

### 6. WEB & INTERNET

| Feature           | Chance.js Support                                            | krandom Status | Implementation Priority | Notes                           |
|-------------------|--------------------------------------------------------------|----------------|-------------------------|---------------------------------|
| **Email**         |
| Email address     | ✅ `generate()`                                               | ✅ Yes          | HIGH                    | 'john.smith@gmail.com'          |
| Custom domain     | ✅ `generate("example.com")`                                  | ✅ Yes          | HIGH                    | 'john.smith@example.com'        |
| Email formats     | ✅ `generate(EmailFormat)`                                    | ✅ Yes          | MEDIUM                  | 5 formats supported             |
| Locale-aware      | ✅ `EmailGenerator(Locale)`                                   | ✅ Yes          | HIGH                    | Uses locale-appropriate names   |
| **Domain & URL**  |
| Domain name       | ✅ `domain({tld})`                                            | ✅ Yes          | HIGH                    | DomainGenerator 'techcloud.com' |
| Custom TLD        | ✅ `domain({tld: 'ie'})`                                      | ✅ Yes          | MEDIUM                  | generate("ie") → 'datahub.ie'   |
| TLD only          | ✅ `tld()`                                                    | ✅ Yes          | MEDIUM                  | getTLD() → 'com', 'org', 'net'  |
| Full URL          | ✅ `url({protocol, domain, domain_prefix, path, extensions})` | ✅ Partial      | HIGH                    | URLGenerator with path/query    |
| Custom protocol   | ✅ `url({protocol: 'ftp'})`                                   | ✅ Yes          | MEDIUM                  | generate("ftp") → 'ftp://...'   |
| Fixed domain      | ✅ `url({domain: 'example.com'})`                             | ❌ No           | MEDIUM                  | Control domain - not yet        |
| Domain prefix     | ✅ `url({domain_prefix: 'api'})`                              | ❌ No           | MEDIUM                  | Subdomain control - not yet     |
| Fixed path        | ✅ `url({path: '/api/v1'})`                                   | ❌ No           | MEDIUM                  | Control path - not yet          |
| File extensions   | ✅ `url({extensions: ['gif','jpg']})`                         | ❌ No           | MEDIUM                  | Random file type - not yet      |
| **IP Addresses**  |
| IPv4              | ✅ `ip()`                                                     | ✅ Yes          | ✓ DONE                  | IPv4Generator with seeding      |
| IPv6              | ✅ `ipv6()`                                                   | ✅ Yes          | ✓ DONE                  | IPv6Generator with seeding      |
| **Colors**        |
| Color hex         | ✅ `color({format: 'hex'})`                                   | ✅ Yes          | ✓ DONE                  | ColorGenerator HEX format       |
| Short hex         | ✅ `color({format: 'shorthex'})`                              | ✅ Yes          | ✓ DONE                  | ColorGenerator SHORT_HEX        |
| RGB format        | ✅ `color({format: 'rgb'})`                                   | ✅ Yes          | ✓ DONE                  | ColorGenerator RGB format       |
| 0x format         | ✅ `color({format: '0x'})`                                    | ✅ Yes          | ✓ DONE                  | ColorGenerator HEX_0X format    |
| Grayscale         | ✅ `color({grayscale: true})`                                 | ✅ Yes          | ✓ DONE                  | generateGrayscale()             |
| Case control      | ✅ `color({casing: 'upper'})`                                 | ✅ Yes          | ✓ DONE                  | generateUppercase()             |
| **Social**        |
| Twitter handle    | ✅ `twitter()`                                                | ❌ No           | LOW                     | '@dafivatemin'                  |
| Avatar URL        | ✅ `avatar({type, fileExtension, protocol, email})`           | ❌ No           | LOW                     | Gravatar URLs                   |
| **Business**      |
| Company name      | ✅ `company()`                                                | ❌ No           | MEDIUM                  | 'Jombo LLC'                     |
| Profession        | ✅ `profession({ranked})`                                     | ✅ Yes          | ✓ DONE                  | `ProfessionGenerator.generate()` |
| Ranked profession | ✅ `profession({ranked: true})`                               | ✅ Yes          | ✓ DONE                  | `ProfessionGenerator.generate(true)` / `generateRanked()` |

### 7. TIME & DATES

| Feature             | Chance.js Support                              | krandom Status | Implementation Priority | Notes                       |
|---------------------|------------------------------------------------|----------------|-------------------------|-----------------------------|
| **Date Objects**    |
| Random date         | ✅ `date({string, american, year, month, day})` | ✅ Yes          | ✓ DONE                  | DateGenerator generate()    |
| Date as string      | ✅ `date({string: true})`                       | ✅ Yes          | ✓ DONE                  | generateString()            |
| American format     | ✅ `date({american: true})`                     | ✅ Yes          | ✓ DONE                  | generateAmerican()          |
| European format     | ✅ `date({american: false})`                    | ✅ Yes          | ✓ DONE                  | generateEuropean()          |
| Fixed year          | ✅ `date({year: 1990})`                         | ✅ Yes          | ✓ DONE                  | generateWithYear(1990)      |
| Fixed month         | ✅ `date({month: 5})`                           | ✅ Yes          | ✓ DONE                  | generateWithMonth(5)        |
| Fixed day           | ✅ `date({day: 15})`                            | ✅ Yes          | ✓ DONE                  | generateWithDay(15)         |
| **Date Components** |
| Year                | ✅ `year({min, max})`                           | ✅ Yes          | ✓ DONE                  | generateYear() / (min,max)  |
| Month name          | ✅ `month()`                                    | ✅ Yes          | ✓ DONE                  | generateMonthName()         |
| Month object        | ✅ `month({raw: true})`                         | ✅ Partial      | MEDIUM                  | generateMonth() returns int |
| Hour (12-hour)      | ✅ `hour()`                                     | ✅ Yes          | ✓ DONE                  | generateHour()              |
| Hour (24-hour)      | ✅ `hour({twentyfour: true})`                   | ✅ Yes          | ✓ DONE                  | generateHour24()            |
| Minute              | ✅ `minute()`                                   | ✅ Yes          | ✓ DONE                  | generateMinute()            |
| Second              | ✅ `second()`                                   | ✅ Yes          | ✓ DONE                  | generateSecond()            |
| Millisecond         | ✅ `millisecond()`                              | ✅ Yes          | ✓ DONE                  | generateMillisecond()       |
| AM/PM               | ✅ `ampm()`                                     | ✅ Yes          | ✓ DONE                  | generateAmPm()              |
| Unix timestamp      | ✅ `timestamp()`                                | ✅ Yes          | ✓ DONE                  | generateTimestamp()         |

### 8. TEXT & NATURAL LANGUAGE

| Feature          | Chance.js Support             | krandom Status | Implementation Priority | Notes                   |
|------------------|-------------------------------|----------------|-------------------------|-------------------------|
| **Words**        |
| Random word      | ✅ `word({syllables, length})` | ❌ No           | HIGH                    | Natural-looking words   |
| Syllable control | ✅ `word({syllables: 4})`      | ❌ No           | HIGH                    | 'pugilefe' - UNIQUE     |
| Length control   | ✅ `word({length: 10})`        | ❌ No           | HIGH                    | Exact character count   |
| Syllable         | ✅ `syllable({length})`        | ❌ No           | MEDIUM                  | Single syllable 'ko'    |
| **Sentences**    |
| Random sentence  | ✅ `sentence({words})`         | ❌ No           | HIGH                    | Capitalized, punctuated |
| Word count       | ✅ `sentence({words: 5})`      | ❌ No           | HIGH                    | Exact word count        |
| Default range    | ✅ 12-18 words                 | ❌ No           | MEDIUM                  | Variable by default     |
| **Paragraphs**   |
| Random paragraph | ✅ `paragraph({sentences})`    | ❌ No           | HIGH                    | Multiple sentences      |
| Sentence count   | ✅ `paragraph({sentences: 3})` | ❌ No           | MEDIUM                  | Exact sentence count    |
| Default range    | ✅ 3-7 sentences               | ❌ No           | MEDIUM                  | Variable by default     |

### 9. IDENTIFIERS & HASHES

| Feature          | Chance.js Support           | krandom Status | Implementation Priority | Notes                 |
|------------------|-----------------------------|----------------|-------------------------|-----------------------|
| **UUIDs/GUIDs**  |
| GUID v5          | ✅ `guid()` default          | ✅ Yes          | ✓ DONE                  | Name-based SHA-1      |
| GUID v4          | ✅ `guid({version: 4})`      | ✅ Yes          | ✓ DONE                  | Random UUID           |
| Version control  | ✅ `guid({version: 4/5})`    | ✅ Yes          | ✓ DONE                  | UUIDGenerator         |
| **Hashes**       |
| Hash string      | ✅ `hash({length, casing})`  | ✅ Yes          | ✓ DONE                  | `HashGenerator`       |
| Default 40 chars | ✅ Git commit length         | ✅ Yes          | ✓ DONE                  | SHA-1 compatible      |
| Custom length    | ✅ `hash({length: 15})`      | ✅ Yes          | ✓ DONE                  | `generate(length)`    |
| Case control     | ✅ `hash({casing: 'upper'})` | ✅ Yes          | ✓ DONE                  | `generateUppercase()` |

### 10. MISCELLANEOUS GENERATORS

| Feature           | Chance.js Support            | krandom Status | Implementation Priority | Notes                         |
|-------------------|------------------------------|----------------|-------------------------|-------------------------------|
| **Coin Flip**     |
| Coin flip         | ✅ `coin()`                   | ✅ Yes          | ✓ DONE                  | 'heads'/'tails' vs enum       |
| **Dice & RPG**    |
| Dice notation     | ✅ `rpg('3d10')`              | ✅ Yes          | ✓ DONE                  | NdS pattern                   |
| Dice array        | ✅ Returns `[1, 6, 9]`        | ✅ Yes          | ✓ DONE                  | Individual rolls              |
| Dice sum          | ✅ `rpg('3d10', {sum: true})` | ✅ Yes          | ✓ DONE                  | `rollSum(3)` → sum of 3 rolls |
| Flexible notation | ✅ '5d6', '3d10', etc.        | ✅ Yes          | ✓ DONE                  | Standard RPG                  |

### 11. HELPER METHODS (UNIQUE TO CHANCE.JS)

| Feature                | Chance.js Support                           | krandom Status | Implementation Priority | Notes                        |
|------------------------|---------------------------------------------|----------------|-------------------------|------------------------------|
| **Repeat Generation**  |
| n() method             | ✅ `n(fn, count, options)`                   | ✅ Yes          | ✓ DONE                  | `RepeatGenerator<T>` via `Generators.repeat(source, count)` |
| Example                | ✅ `n(chance.integer, 5, {min: 1, max: 10})` | ✅ Yes          | ✓ DONE                  | `Generators.repeat(Generators.ofInt(1, 10), 5)`             |
| **Unique Values**      |
| unique() method        | ✅ `unique(fn, count, options)`              | ✅ Yes          | ✓ DONE                  | `UniqueGenerator<T>` via `Generators.unique(source)`         |
| Example                | ✅ `unique(chance.state, 5)`                 | ✅ Yes          | ✓ DONE                  | `Generators.repeat(Generators.unique(stateGen), 5)`          |
| RangeError             | ✅ Throws if pool too small                  | ✅ Yes          | ✓ DONE                  | Throws `IllegalStateException` when unique pool is exhausted |
| Custom comparator      | ✅ `{comparator: (arr, val) => ...}`         | ✅ Yes          | ✓ DONE                  | `Generators.unique(source, comparator)`                      |
| **Collection Helpers** |
| pick()                 | ✅ `pick(['a','b','c'])`                     | ✅ Yes          | ✓ DONE                  | `PickGenerator<T>` via `Generators.pickFrom(source)`         |
| pickset()              | ✅ `pickset(['a','b','c'], 2)`               | ✅ Yes          | ✓ DONE                  | `PickSetGenerator<T>` via `Generators.pickSetFrom(source, n)`|
| shuffle()              | ✅ `shuffle([1,2,3,4])`                      | ✅ Yes          | ✓ DONE                  | `ShuffleGenerator<T>` via `Generators.shuffleOf(source)`     |
| **Weighted Random**    |
| weighted()             | ✅ `weighted(values, weights)`               | ✅ Yes          | ✓ DONE                  | `WeightedGenerator<T>` via `Generators.weighted(values, weights)` |
| Example                | ✅ `weighted(['heads','tails'], [7,3])`      | ✅ Yes          | ✓ DONE                  | Weighted selection verified with statistical tests            |

---

## ADVANCED FEATURES

### Seeding & Reproducibility

| Feature              | Chance.js                         | krandom | Priority | Implementation Notes    |
|----------------------|-----------------------------------|---------|----------|-------------------------|
| **Seeding**          |
| Constructor seed     | ✅ `new Chance(42)`                | ✅ Yes   | ✓ DONE   | Most generators support |
| String seed          | ✅ `new Chance('my-seed')`         | ❌ No    | MEDIUM   | String-based seeding    |
| Custom RNG           | ✅ `new Chance(Math.random)`       | ❌ No    | LOW      | Function as seed        |
| Deterministic output | ✅ Same seed = same sequence       | ✅ Yes   | ✓ DONE   | Full reproducibility    |
| Re-seed              | ✅ `chance.seed(42)`               | ❌ No    | MEDIUM   | Reset to specific seed  |
| Unseeded mode        | ✅ `new Chance()` uses Math.random | ✅ Yes   | ✓ DONE   | Default behavior        |

### Options & Parameterization

| Feature                | Chance.js                        | krandom | Priority | Implementation Notes                                   |
|------------------------|----------------------------------|---------|----------|--------------------------------------------------------|
| **Rich Options**       |
| Extensive parameters   | ✅ Every method has options       | ❌ No    | HIGH     | name({middle, prefix, suffix, gender, nationality})    |
| Default values         | ✅ Sensible defaults              | ✅ Yes   | ✓ DONE   | Works without options                                  |
| Option combinations    | ✅ Multiple options work together | ❌ No    | MEDIUM   | Composable parameters                                  |
| **Likelihood Control** |
| Boolean likelihood     | ✅ `bool({likelihood: 80})`       | ✅ Yes   | ✓ DONE   | `ofBoolean().withLikelihood(80)` — UNIQUE              |
| **Format Control**     |
| String format          | ✅ `date({string: true})`         | ✅ Yes   | ✓ DONE   | `generateString()`, `generateAsString()` per generator |
| Number format          | ✅ `floating({fixed: 2})`         | ✅ Yes   | ✓ DONE   | `withPrecision(2)` on DoubleGenerator/FloatGenerator   |
| Formatted output       | ✅ `phone({formatted: false})`    | ✅ Yes   | ✓ DONE   | `PhoneNumberGenerator.generate(false)` → digits only   |

### Mobile & Device Support

| Feature           | Chance.js                                | krandom | Priority | Implementation Notes                                       |
|-------------------|------------------------------------------|---------|----------|------------------------------------------------------------|
| **Mobile Phones** |
| UK mobile         | ✅ `phone({country: 'uk', mobile: true})` | ✅ Yes   | ✓ DONE   | `PhoneNumberGenerator(Locale.UK).generate(true, true)`     |
| US mobile         | ✅ Standard format                        | ✅ Yes   | ✓ DONE   | `PhoneNumberGenerator(Locale.US).generate(true, true)`     |
| French mobile     | ✅ `phone({country: 'fr'})`               | ✅ Yes   | ✓ DONE   | `PhoneNumberGenerator(Locale.FRANCE).generate(true, true)` |
| **Multi-Country** |
| US support        | ✅ Default                                | ✅ Yes   | ✓ DONE   | `PhoneNumberGenerator(Locale.US)` + `PostalCodeGenerator`  |
| UK support        | ✅ Full support                           | ✅ Yes   | ✓ DONE   | `PhoneNumberGenerator(Locale.UK)` + `PostalCodeGenerator`  |
| French support    | ✅ Phone numbers                          | ✅ Yes   | ✓ DONE   | `PhoneNumberGenerator(Locale.FRANCE)`                      |
| Italian support   | ✅ Names, states                          | ✅ Yes   | ✓ DONE   | All generators support `Locale.ITALY`                      |

### Statistical Features

| Feature                | Chance.js                        | krandom | Priority | Implementation Notes                           |
|------------------------|----------------------------------|---------|----------|------------------------------------------------|
| **Distributions**      |
| Normal distribution    | ✅ `normal({mean, dev})`          | ✅ Yes   | ✓ DONE   | `Generators.ofNormal(mean, stdDev)` Box-Muller |
| Mean control           | ✅ `mean` parameter               | ✅ Yes   | ✓ DONE   | First param of `ofNormal(mean, stdDev)`        |
| Std deviation          | ✅ `dev` parameter                | ✅ Yes   | ✓ DONE   | Second param of `ofNormal(mean, stdDev)`       |
| IQ-like data           | ✅ `normal({mean: 100, dev: 15})` | ✅ Yes   | ✓ DONE   | `Generators.ofNormal(100.0, 15.0)`             |
| **Weighted Selection** |
| Weighted arrays        | ✅ `weighted(['a','b'], [7,3])`   | ✅ Yes   | ✓ DONE   | `Generators.weighted(values, weights)`         |
| Integer weights        | ✅ Any positive integers          | ❌ No    | HIGH     | Flexible weighting — not yet                   |

### Natural Language Features

| Feature                | Chance.js                | krandom | Priority | Implementation Notes          |
|------------------------|--------------------------|---------|----------|-------------------------------|
| **Pronounceable Text** |
| Syllable generation    | ✅ `syllable()`           | ❌ No    | MEDIUM   | Natural-sounding - UNIQUE     |
| Word generation        | ✅ `word({syllables: 4})` | ❌ No    | HIGH     | Syllable-based words - UNIQUE |
| Sentence structure     | ✅ Capital + period       | ❌ No    | HIGH     | Proper formatting             |
| Paragraph structure    | ✅ Multiple sentences     | ❌ No    | HIGH     | Natural paragraphs            |
| Variable length        | ✅ Default ranges         | ❌ No    | MEDIUM   | 12-18 words, 3-7 sentences    |

---

## IMPLEMENTATION RECOMMENDATIONS

### Phase 1: CORE GAPS (Must Have) - 10 days

1. ~~**Email Generation** (1 day)~~ ✅ DONE — `EmailGenerator` with 5 formats, 12 domains, 10 locales
2. ~~**UUID/GUID Generation** (1 day)~~ ✅ DONE — `UUIDGenerator` (v4 + v5), `HashGenerator`
3. ~~**Boolean Likelihood** (1 day)~~ ✅ DONE — `ofBoolean().withLikelihood(80)`
4. **Natural Language** (3 days) - High-value feature
    - `word({syllables, length})` with syllable control
    - `sentence({words})` with capitalization
    - `paragraph({sentences})` for text blocks
5. **Helper Methods** (2 days) - Core utility
    - `n(fn, count, options)` for repeated generation
    - `unique(fn, count, options)` for distinct values
    - `pick()`, `pickset()`, `shuffle()` for collections
6. **Weighted Random** (1 day) - Unique differentiator
    - `weighted(values, weights)` for biased selection
7. ~~**Location - City support** (1 day)~~ ✅ COMPLETED
    - ~~`city()` for city names~~ - Now supports 10 locales with locale-specific major cities
7. **Normal Distribution** (1 day) - Statistical feature
    - `normal({mean, dev})` with Box-Muller transform

### Phase 2: LOCATION & WEB (Must Have) - 8 days

1. **Address Components** (2 days)
    - `address({short_suffix})` for street addresses
    - `city()` for city names
    - `state({full, territories, country})` with rich options
    - `zip({plusfour})`, `postal()`, `postcode()` for postal codes
2. **Country Support** ~~(1 day)~~ ✅ **DONE**
    - ✅ `country({full})` for country codes/names - **CountryGenerator** with 195 countries across 10 locales
3. **Phone Numbers** (2 days)
    - `phone({formatted, country, mobile})` with multi-country
    - `areacode()` for US area codes
    - UK/US/FR support with mobile detection
4. **Coordinates** (1 day)
    - `latitude({fixed, min, max})`, `longitude()`, `coordinates()`
    - `altitude()`, `depth()`, `geohash()` for advanced geo
5. **URL/Domain** (2 days)
    - `domain({tld})`, `tld()`, `url({protocol, domain, path, extensions})`
    - Rich URL parameterization

### Phase 3: FINANCE & TEXT (Should Have) - 7 days

1. **Credit Cards** (2 days)
    - `cc({type})` with Visa/MC/Amex support
    - `cc_type()` for card metadata
    - `exp({future})`, `exp_month()`, `exp_year()` for expiration
2. ~~**Currency** (1 day)~~ ✅ **DONE**
    - ✅ `currency()` — `CurrencyGenerator` with ISO 4217, 50+ currencies, 10 locales
    - ✅ `currency_pair()` — `CurrencyPairGenerator` (random FX pairs, locale-aware base)
    - ✅ `dollar({max})` — `MoneyGenerator.generateDollar(max)` → `"$4,231.87"`
    - ✅ `euro({max})` — `MoneyGenerator.generateEuro(max)` → `"4 231,87 €"`
3. **Enhanced Names** (1 day — prefix/suffix done)
    - `name({middle, middle_initial, prefix, suffix, gender, nationality})`
    - ~~`prefix({gender})`~~ ✅ `TitleGenerator`, ~~`suffix()`~~ ✅ `SuffixGenerator`
    - Gender-specific and nationality support (remaining)
4. **Date Components** (2 days)
    - `date({string, american, year, month, day})` with rich options
    - `year({min, max})`, `month({raw})`, `hour({twentyfour})`
    - `timestamp()` for Unix time

### Phase 4: ENHANCEMENTS (Nice to Have) - 5 days

1. **Character Generators** (1 day)
    - `character({pool, alpha, numeric, symbols, casing})`
2. **String Enhancements** (1 day)
    - Custom pool support for `string({pool: 'abc'})`
3. **Color Generators** (1 day)
    - `color({format, grayscale, casing})` with multiple formats
4. **Business Data** (1 day)
    - `company()`, `profession({ranked})` with ranking
5. **Advanced Geo** (1 day)
    - Range-restricted coordinates, geohash support

---

## KEY DIFFERENTIATORS

### Chance.js Unique Strengths (vs krandom)

1. ~~**Weighted Random** - `weighted(['a','b'], [7,3])` for biased selection~~ (~~NO EQUIVALENT~~ ✅ DONE — `Generators.weighted(values, weights)`)
2. **Normal Distribution** - Box-Muller transform for realistic statistical data (~~NO EQUIVALENT~~ ✅ DONE)
3. **Likelihood Control** - `bool({likelihood: 80})` for probability-based booleans (~~NO EQUIVALENT~~ ✅ DONE)
4. ~~**Helper Methods** - `n()`, `unique()`, `pick()`, `pickset()`, `shuffle()`~~ (~~NO EQUIVALENT~~ ✅ DONE — selection package parity complete)
5. **Syllable-Based Words** - `word({syllables: 4})` for natural-looking text (NO EQUIVALENT)
6. **Rich Options** - Extensive parameterization on every method (PARTIAL)
7. ~~**Mobile Detection** - `phone({mobile: true})` for mobile-specific formats~~ (~~NO EQUIVALENT~~ ✅ DONE — `PhoneNumberGenerator.generate(true, true)`)
8. ~~**Currency Pairs** - `currency_pair()` for FX simulation (NO EQUIVALENT)~~ ✅ DONE — `CurrencyPairGenerator`
9. ~~**Ranked Professions** - `profession({ranked: true})` for biased selection~~ (~~NO EQUIVALENT~~ ✅ DONE — `ProfessionGenerator.generateRanked()`)
10. **Exclude Arrays** - `natural({exclude: [1,2,3]})` to skip specific values (~~NO EQUIVALENT~~ ✅ DONE)
11. **Natural Language** - Sentence/paragraph with proper capitalization and punctuation (NO EQUIVALENT)
12. ~~**Format Flexibility** - `date({string: true})`, `ssn({dashes: false})` for output control~~ (~~PARTIAL~~ ✅ DONE — separate `generateString()`, `withoutDashes()` methods)

### krandom Unique Strengths (vs Chance.js)

1. **Kotlin-First** - Type-safe, idiomatic Kotlin API
2. **ObjectGenerator** - Generate complex object graphs (Chance.js is manual only)
3. **Multi-Locale National IDs** - 10 countries with verified checksums via `NationalIdGenerator`; extensible registry (NO CHANCE.JS EQUIVALENT)
4. **Locale-Aware Birthday Strings** - `BirthdayGenerator(Locale)` formats `generateAsString()` per locale convention (de_DE: `d.M.yyyy`, ja_JP: `yyyy/M/d`, zh_CN: `yyyy年M月d日`)
5. **Locale-Aware Money Formatting** - `MoneyGenerator(Locale)` uses `NumberFormat.getCurrencyInstance()` for correct symbol placement, thousand/decimal separators, and decimal places per locale (
   e.g., JPY has 0 decimal places)
6. **Fibonacci** - Dedicated Fibonacci number generator
7. **Better Test Coverage** - 99%+ coverage
8. **Cleaner Architecture** - More maintainable codebase
9. **JVM Interop** - Works with Java/Scala/Kotlin

---

## EFFORT ESTIMATES

### Phase 1: Core Gaps - 10 days

- Email generation: **1 day**
- UUID/GUID with versions: **1 day**
- Boolean likelihood: **1 day**
- Natural language (word/sentence/paragraph): **3 days**
- Helper methods (n/unique/pick/pickset/shuffle): **2 days**
- Weighted random: **1 day**
- Normal distribution: **1 day**

### Phase 2: Location & Web - 8 days

- Address components: **2 days**
- Country support: ~~**1 day**~~ ✅ **DONE** - CountryGenerator with 195 countries in 10 locales
- Phone numbers (multi-country, mobile): **2 days**
- Coordinates (lat/lon/geo): **1 day**
- URL/domain with rich options: **2 days**

### Phase 3: Finance & Text - 7 days

- Credit cards (types, expiration): ~~**2 days**~~ ✅ **DONE**
- Currency (objects, pairs, formatted): ~~**1 day**~~ ✅ **DONE**
- Enhanced names (options): **2 days**
- Date components: **2 days**

### Phase 4: Enhancements - 5 days

- Character generators: **1 day**
- String enhancements: **1 day**
- Color generators: **1 day**
- Business data: **1 day**
- Advanced geo: **1 day**

### **TOTAL: ~30 days** (6 weeks)

---

## COMPATIBILITY ASSESSMENT

### Direct Port Feasibility

- ✅ **Easy**: Basic generators (numbers, booleans, strings) - **DONE** (Numbers section complete)
- ✅ **Moderate**: Email, UUID, addresses, phones, dates
- ⚠️ **Moderate-Hard**: ~~Normal distribution (need Box-Muller)~~ **DONE**, syllable generation
- ⚠️ **Hard**: Helper methods (n, unique) need reflection or lambda support
- ⚠️ **Complex**: Weighted random needs algorithm implementation

### Kotlin-Specific Opportunities

1. **Extension Functions** - `List.pick()`, `List.pickset()`, `List.shuffle()`
2. **Inline Reified** - Type-safe `unique<T>()` with generics
3. **Sealed Classes** - Better type safety for card types, currencies
4. **Data Classes** - Clean currency/card type objects
5. **DSL Builders** - Fluent API for complex generators
6. **Coroutines** - Async generation for large datasets
7. **Operator Overloading** - Natural syntax for weighted selection

### Recommended Approach

1. **Port core concepts** - Weighted random, normal distribution, helpers
2. **Enhance with Kotlin** - Extension functions, sealed classes, DSLs
3. **Keep simplicity** - Don't over-engineer
4. **Focus on API** - Great developer experience
5. **Add type safety** - Leverage Kotlin's type system
6. **Skip legacy** - No need for JavaScript compatibility
7. **Document well** - Examples for all features

---

## CONCLUSION

Chance.js offers **unique features** that krandom lacks, particularly in:

### Completed Implementations ✅

**Numbers Section (100% Complete)**:

- ✅ Natural numbers with exclusion - `Generators.ofNaturalNumber().excluding(...)`
- ✅ Prime number generation - `Generators.ofPrime()`
- ✅ Fixed decimal precision - `ofDouble().withPrecision(decimals)`
- ✅ Normal distribution - `Generators.ofNormal(mean, stdDev)` with Box-Muller transform
- ✅ Exclusion support for natural numbers

**Person Names Section (100% Complete)**:

- ✅ First name - `FirstNameGenerator` — 10 locales, gender-aware (`generate(Gender.MALE/FEMALE)`)
- ✅ Last name - `LastNameGenerator` — 10 locales, extensible registry
- ✅ Name prefix/title - `TitleGenerator` — 10 locales
- ✅ Name suffix - `SuffixGenerator` — 10 locales
- Architecture: file-based locale data (`krandom/names/*.txt`), `NameResourceLoader`, per-type registry+provider+enum pattern

**National ID Section (100% Complete — krandom extension)**:

- ✅ US SSN — `NationalIdGenerator(Locale.US)` via `UsNationalIdProvider` (`AAA-GG-SSSS`, area 666 excluded)
- ✅ UK NI number — `NationalIdGenerator(Locale.UK)` (`AB 12 34 56 C`, letter validity + disallowed-pair rejection)
- ✅ AU TFN — `NationalIdGenerator(Locale.of("en","AU"))` (mod-11 weighted checksum)
- ✅ FR NIR — `NationalIdGenerator(Locale.FRANCE)` (control key = `97 − N mod 97`)
- ✅ DE Steuer-ID — `NationalIdGenerator(Locale.GERMANY)` (ISO 7064 Mod 11,10)
- ✅ JP My Number — `NationalIdGenerator(Locale.JAPAN)` (weighted-sum mod-11, 12 digits)
- ✅ ES DNI — `NationalIdGenerator(Locale.of("es","ES"))` (mod-23 check letter)
- ✅ IT Codice Fiscale — `NationalIdGenerator(Locale.ITALY)` (16-char odd/even position check)
- ✅ BR CPF — `NationalIdGenerator(Locale.of("pt","BR"))` (double mod-11 verifier digits)
- ✅ CN Resident ID — `NationalIdGenerator(Locale.CHINA)` (ISO 7064 Mod 11,2, 18 chars)
- Architecture: `NationalIdProvider` interface + `NationalIdRegistry` (ConcurrentHashMap, language-level fallback) + `NationalIdGenerator` facade; mirrors `TitleGenerator` stack exactly; extensible
  via `NationalIdRegistry.register()`

**Locale-Aware Birthday Strings (100% Complete — krandom extension)**:

- ✅ `BirthdayGenerator(Locale)` — locale-aware `generateAsString()` with 3-level fallback (exact key → language key → default)
- Supported patterns: en_US `M/d/yyyy`, en_GB/en_AU/fr_FR/es_ES/it_IT/pt_BR `d/M/yyyy`, de_DE `d.M.yyyy`, ja_JP `yyyy/M/d`, zh_CN `yyyy年M月d日`
- 6 new constructors (3 unseeded + 3 seeded with locale); `getLocale()` accessor; `generateAsAmericanString()` unchanged

**Finance Section (100% Complete)**:

- ✅ Credit cards — `CreditCardGenerator` — 6 card types (Visa/MC/Amex/Discover/JCB/Diners), Luhn-valid, CVV, formatted
- ✅ Currency — `CurrencyGenerator` — ISO 4217, 50+ currencies, code/name/symbol/numericCode, 10 locales
- ✅ Card expiration — `CardExpirationGenerator` — MM/YY, future-only mode, locale formatting (MM/YY vs YY/MM)
- ✅ Currency pairs — `CurrencyPairGenerator` — `generate()` → `"EUR/USD"`, `generate(Locale)` → locale base, `generateWithInfo()` → `CurrencyPair`
- ✅ Money amounts — `MoneyGenerator` — `generateDollar()` → `"$4,231.87"`, `generateEuro()` → `"4 231,87 €"`, `generate(Locale)`, `generate(max)`
- Architecture: `Currency` enum + `CurrencyInfo` record + `CurrencyPair` record + separate generators; `Currency.forLocale()` maps 10 locales

**Helper Methods Section (100% Complete)**:

- ✅ Repeat generation (`n` equivalent) — `RepeatGenerator<T>` via `Generators.repeat(source, count)`
- ✅ Unique values — `UniqueGenerator<T>` via `Generators.unique(source)` / `Generators.unique(source, comparator)`
- ✅ Random element pick — `PickGenerator<T>` via `Generators.pickFrom(source)`
- ✅ Random subset pick — `PickSetGenerator<T>` via `Generators.pickSetFrom(source, count)`
- ✅ Collection shuffle — `ShuffleGenerator<T>` via `Generators.shuffleOf(source)`
- ✅ Weighted random selection — `WeightedGenerator<T>` via `Generators.weighted(values, weights)`
- ✅ Coverage quality: `org.github.krandom.generator.selection` at 100% line + 100% branch in pre-commit coverage report

**Implementation Details**:

- Overall test coverage: 99%+ line and branch (all gates passing)
- `generator.finance` package: 100% line and branch coverage
- ~400 new test cases across names, booleans, chars, strings, numbers, national IDs, locale birthdays, finance
- Statistical validation (68-95-99.7 empirical rule for normal distribution)
- Efficient algorithms (Sieve of Eratosthenes, Box-Muller, ISO 7064 Mod 11,2 and Mod 11,10)
- Full Javadoc documentation

### Top Priority Focus Areas (Remaining)

1. ~~**Weighted Random & Normal Distribution**~~ - ✅ COMPLETE (normal distribution + weighted boolean DONE)
2. ~~**Helper Methods**~~ - ✅ COMPLETE (`repeat`, `unique`, `pick`, `pickset`, `shuffle`, `weighted`)
3. **Natural Language Generation** - Syllable-based words, sentences, paragraphs
4. **Rich Options Parameterization** - Extensive parameter support on all methods
5. ~~**Email & UUID**~~ - ✅ COMPLETE
6. ~~**Location Data**~~ - ✅ COMPLETE (address still missing)
7. ~~**Finance Enhancement**~~ - ✅ COMPLETE — credit cards, currency pairs, formatted amounts all done

### Skip/Low Priority

1. **Multi-nationality** - Low ROI, complex to maintain
2. **Italian/French** - Focus on English first
3. **RPG dice sum** - Minor enhancement to existing Dice
4. **Social/Entertainment** - Twitter handles, avatars (low value)

### Strategic Recommendation

**Phase 1 Complete** ✅ - Implemented 52 features from Chance.js (+ 12 krandom extensions):

- Numbers: Natural numbers, primes, fixed precision, normal distribution, exclusion support (8/8 - 100%)
- Booleans: Random boolean, weighted boolean with likelihood (2/2 - 100%)
- Characters: Custom pools, alpha/numeric/symbols, case control (6/6 - 100%)
- Strings: Custom pools, variable/fixed length, alpha/numeric strings (6/6 - 100%)
- Person names: First name (gender-aware, 10 locales), last name (10 locales), title/prefix, suffix (8/8 core name features - 100%)
- Demographics: Age with type ranges, gender labels (10 locales), birthday (type/string/american), National ID/SSN (full/no-dashes/last-4) (11/11 - 100%)
- **[krandom extension]** Multi-locale National IDs: UK NI, AU TFN, FR NIR, DE Steuer-ID, JP My Number, ES DNI, IT Codice Fiscale, BR CPF, CN Resident ID (10/10 locales - 100%)
- **[krandom extension]** Locale-aware birthday string: 10 locale patterns, 3-level fallback, 6 new constructors

**Next Phase** - Implement remaining high-value features:

- Natural language (word, sentence, paragraph) - HIGH DEMAND
- Street address generation - HIGH USAGE
- Rich parameterization - BETTER UX

**Maintain krandom advantages**:

- ✅ Kotlin-first design with type safety
- ✅ ObjectGenerator for complex graphs
- ✅ Clean architecture and test coverage (99.8%+)
- ✅ JVM ecosystem integration
- ✅ Statistical capabilities (normal distribution via Box-Muller, weighted boolean)
- ✅ Flexible character/string generation (custom pools, builder pattern)
- ✅ Complete finance suite: locale-aware money formatting, FX pairs, 6 card types, 10 currency locales

**Target outcome**: krandom becomes the **most developer-friendly** random data generator for JVM with **unique statistical capabilities** and **flexible character/string generation** not found in
other JVM libraries.

**Progress**: parity materially improved with helper/selection coverage complete. Remaining gaps are concentrated in natural language syllable behavior, richer options parameterization, URL option
richness, and low-priority social/avatar generators.
