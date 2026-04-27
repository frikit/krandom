# Python Faker Feature Parity Analysis

## Library Overview

- **Name**: Faker (Python)
- **Language**: Python
- **Version Analyzed**: 26.x (Python 3.8+)
- **GitHub**: https://github.com/joke2k/faker
- **License**: MIT
- **Key Strength**: 80+ locales, provider architecture, extensive real-world datasets, highest maturity

## Java Execution Plan

- Active plan: `docs/plans/faker-python-java-plan.md`
- Current scope: Java parity only (Kotlin/Scala deferred)
- Delivery model: one parity slice at a time with tests + `./scripts/pre_commit_check.sh`

## Executive Summary

Python Faker is the most mature and widely-adopted fake data library in the Python ecosystem (27k+ GitHub stars, 10+ years), offering 80+ locale support, 20+ core providers, and a flexible plugin
architecture. It originated as a port of PHP Faker and has become the de facto standard for test data generation in Python. Key strengths include comprehensive locale-aware data (names, addresses,
phone numbers), extensive date/time utilities, realistic financial data (IBAN, credit cards), and battle-tested stability from massive real-world usage.

## Phase 0 Audit Baseline (2026-03-01)

This section is the current Java parity baseline for execution planning. Some legacy rows below are stale.

### Already Covered (Java)

- Identity: `full name`, `first/last name`, `title/prefix`, `suffix`, `username`, locale-aware name generation.
- Address/location: `city`, `state`, `state_abbr`, `street_name`, `street_address`, `building_number`, `secondary_address`, `postcode/zipcode`, `country`, `country_code(alpha-2)`, `latitude`,
  `longitude`.
- Internet/network: `email`, `safe_email`, `free_email`, `company_email`, `free_email_domain`, `domain_name`, `hostname`, `tld`, `url`, `uri`, `slug`, `ipv4`, `ipv4_private/public/cidr`, `ipv6`,
  `ipv6 cidr`, `ip(v4|v6)`, `mac_address`, `port_number` (+ system/user/dynamic ranges), `http_method`, `http_status_code`, `uuid4`, `user_agent`.
- Finance/commercial: `credit card number/expiry/cvv/type/full`, `swift/bic`, `currency code/name/symbol/info`, `pricetag` equivalent via `MoneyGenerator`.
- Company/job/text/date: `company`, `company_suffix`, `job title`, `profile/simple_profile`, `word/words`, `sentence/sentences`, `paragraph/paragraphs`, `date/past/future/between`,
  `datetime/past/future/before/after/between`, `iso8601`, `unix_time`, `timezone`, `time`, `duration`.
- Numbers/codes/file/color: `digit`, `number_with_format`, `isbn10/isbn13`, `file_path`, `dir_path`, `file_name`, `file_extension`, `mime_type`, `semver`, `hex/rgb/rgba/hsl/hsla`.

### Open Gaps Tagged for Implementation

| Gap                                                                      | Status              | Priority Tag | Notes                                                              |
|--------------------------------------------------------------------------|---------------------|--------------|--------------------------------------------------------------------|
| `name_female()/name_male()` explicit convenience APIs                    | Implemented         | P0 ✅         | Added full-name convenience wrappers.                              |
| Gender-specific last-name variants                                       | Missing             | P1           | Locale-specific surname morphology not implemented.                |
| `profile()/simple_profile()`                                             | Implemented         | P0 ✅         | Added dedicated profile generators and models.                     |
| `country_code(alpha-3)` and current-country helpers                      | Implemented         | P0 ✅         | Added alpha-3 generation and current-country helper APIs.          |
| `hostname()`, `uri()`                                                    | Implemented         | P0 ✅         | Added dedicated hostname and URI generators.                       |
| `company_email()`                                                        | Implemented         | P0 ✅         | Added dedicated company-email generator and email convenience API. |
| `http_method()`                                                          | Implemented         | P0 ✅         | Added dedicated HTTP method generator.                             |
| Browser-specific user agents (`chrome/firefox/safari/opera/android/ios`) | Missing             | P1           | Generic UA exists.                                                 |
| `iban()`, `aba()`, `bban()`, `bank_country()`                            | Missing             | P1           | Banking set incomplete.                                            |
| `currency()` dict shape                                                  | Implemented         | P1 ✅         | `CurrencyGenerator.generateAsMap()` and `CurrencyInfo` exist.       |
| `bs()`, `catch_phrase()`                                                 | Missing             | P1           | Company language providers missing.                                |
| `text()/texts()` char-limited blocks                                     | Missing             | P1           | Word/sentence/paragraph exist.                                     |
| Custom word lists + uniqueness flags in text providers                   | Missing             | P1           | Could map to options APIs.                                         |
| `iso8601()`, `timezone()`, date component helpers (`month_name`, etc.)   | Implemented/Partial | P0 ✅         | Added iso8601/timezone; month-name helper already existed.         |
| `country_calling_code()`, `msisdn()`                                     | Missing             | P1           | Phone generator exists.                                            |
| `pydecimal()`, `null_boolean()`                                          | Missing/Partial     | P1           | Weighted boolean exists (`withLikelihood`).                        |
| EAN family (`ean8/ean13/ean/localized`)                                  | Missing             | P1           | ISBN exists; barcode family missing.                               |
| Real hash algorithms (`md5/sha1/sha256`)                                 | Partial             | Explicit gap | Random hex hash exists; algorithmic message digesting is deferred. |
| Color names (`color_name`, `safe_color_name`)                            | Missing             | P1           | Color formats exist.                                               |

### Intentional Skip Candidates (Phase 4 Review)

- Passport composite providers (`passport_full`, `passport_owner`, MRZ) unless demanded by product scope.
- Low-ROI locale-niche identity variants with weak reuse in current framework goals.
- Faker-Python Python-specific return-shape compat where Java-native models already exist and are preferable.

---

## Feature Categories

### 1. PERSONAL IDENTITY

| Feature                     | faker Support                                                          | krandom Status | Implementation Priority | Notes                                      |
|-----------------------------|------------------------------------------------------------------------|----------------|-------------------------|--------------------------------------------|
| **Name Generation**         |
| Full name                   | ✅ `name()`, `name_female()`, `name_male()`                             | ✅ Yes          | ✓ DONE                  | `FullNameGenerator` with `NameOptions`     |
| First name                  | ✅ `first_name()`                                                       | ✅ Yes          | ✓ DONE                  | Already implemented                        |
| Last name                   | ✅ `last_name()`                                                        | ✅ Yes          | ✓ DONE                  | Already implemented                        |
| Gender-specific first names | ✅ `first_name_female()`, `first_name_male()`, `first_name_nonbinary()` | ✅ Yes          | ✓ DONE                  | `FirstNameGenerator.generate(Gender)`      |
| Gender-specific last names  | ✅ `last_name_female()`, `last_name_male()`, `last_name_nonbinary()`    | ✅ Partial      | Explicit gap            | Last names exist; gendered surname morphology intentionally not modeled yet |
| Name prefix                 | ✅ `prefix()` (Mr., Mrs., Dr., Prof.)                                   | ✅ Yes          | ✓ DONE                  | `TitleGenerator` and full-name prefix option |
| Name suffix                 | ✅ `suffix()` (Jr., Sr., PhD, III, IV)                                  | ✅ Yes          | ✓ DONE                  | `SuffixGenerator`                         |
| Full name by gender         | ✅ `name_female()`, `name_male()`                                       | ✅ Yes          | ✓ DONE                  | `FullNameGenerator.NameOptions.gender(...)` |
| **Titles**                  |
| Title/prefix                | ✅ `prefix()`                                                           | ✅ Yes          | ✓ DONE                  | TitleGenerator implemented                 |
| **SSN & IDs**               |
| SSN (US)                    | ✅ `ssn()`                                                              | ✅ Yes          | ✓ DONE                  | SocialSecurityNumber implemented           |
| Invalid SSN                 | ✅ `invalid_ssn()`                                                      | ❌ No           | LOW                     | Testing edge cases                         |
| EIN (Employer ID)           | ✅ `ein()`                                                              | ✅ Yes          | ✓ DONE                  | `EinGenerator`                             |
| ITIN (Tax ID)               | ✅ `itin()`                                                             | ❌ No           | LOW                     | Individual taxpayer ID                     |
| **Passport**                |
| Passport number             | ✅ `passport_number()`                                                  | ❌ No (intentional) | SKIP                | Travel-document datasets deferred until requested |
| Passport gender             | ✅ `passport_gender()`                                                  | ❌ No           | LOW                     | M/F/X designation                          |
| Passport MRZ                | ✅ `passport_mrz()`                                                     | ❌ No           | LOW                     | Machine-readable zone                      |
| Full passport               | ✅ `passport_full()`                                                    | ❌ No           | LOW                     | Complete document                          |
| Passport owner              | ✅ `passport_owner()`                                                   | ❌ No           | LOW                     | Owner details dict                         |
| **Profile**                 |
| Simple profile              | ✅ `simple_profile()`                                                   | ✅ Yes          | ✓ DONE                  | `SimpleProfileGenerator`                   |
| Full profile                | ✅ `profile()`                                                          | ✅ Yes          | ✓ DONE                  | `ProfileGenerator` / `UserProfile`         |

### 2. ADDRESS & LOCATION

| Feature              | faker Support                              | krandom Status | Implementation Priority | Notes                        |
|----------------------|--------------------------------------------|----------------|-------------------------|------------------------------|
| **Street Address**   |
| Full address         | ✅ `address()`                              | ✅ Yes          | ✓ DONE                  | `StreetAddressGenerator.generateFullAddress()` |
| Street name          | ✅ `street_name()`                          | ✅ Yes          | ✓ DONE                  | `StreetAddressGenerator.generateStreetName()` |
| Street address       | ✅ `street_address()`                       | ✅ Yes          | ✓ DONE                  | `StreetAddressGenerator.generate()` |
| Building number      | ✅ `building_number()`                      | ✅ Yes          | ✓ DONE                  | `generateBuildingNumber()` / `generateStreetAddressNumber()` |
| Secondary address    | ✅ `secondary_address()`                    | ✅ Yes          | ✓ DONE                  | `generateSecondaryAddress()` |
| Street suffix        | ✅ `street_suffix()`                        | ❌ No (intentional) | SKIP                | Internal building block; full street names are generated |
| **City & State**     |
| City name            | ✅ `city()`                                 | ✅ Yes          | ✓ DONE                  | `CityGenerator`              |
| City prefix          | ✅ `city_prefix()`                          | ❌ No           | LOW                     | Lake, North, East            |
| City suffix          | ✅ `city_suffix()`                          | ❌ No           | LOW                     | ville, town, burg            |
| State                | ✅ `state()`                                | ✅ Yes          | ✓ DONE                  | `StateGenerator.generate()`  |
| State abbreviation   | ✅ `state_abbr()`                           | ✅ Yes          | ✓ DONE                  | `StateGenerator.generate(true)` |
| **Postal Codes**     |
| Postcode             | ✅ `postcode()`                             | ✅ Yes          | ✓ DONE                  | `PostalCodeGenerator`        |
| ZIP code             | ✅ `zipcode()` (alias)                      | ✅ Yes          | ✓ DONE                  | US format via `PostalCodeGenerator(Locale.US)` |
| **Country & Nation** |
| Country name         | ✅ `country()`                              | ✅ Yes          | ✓ DONE                  | `CountryGenerator.generate()` |
| Country code         | ✅ `country_code(representation='alpha-2')` | ✅ Yes          | ✓ DONE                  | `CountryGenerator.generateCode(...)` |
| Current country      | ✅ `current_country()`                      | ✅ Yes          | ✓ DONE                  | `CountryGenerator.currentCountry()` |
| Current country code | ✅ `current_country_code()`                 | ✅ Yes          | ✓ DONE                  | `CountryGenerator.currentCountryCode(...)` |
| **Coordinates**      |
| Latitude             | ✅ `latitude()`                             | ✅ Yes          | ✓ DONE                  | `CoordinatesGenerator.generateLatitude()` |
| Longitude            | ✅ `longitude()`                            | ✅ Yes          | ✓ DONE                  | `CoordinatesGenerator.generateLongitude()` |
| Lat/Lng tuple        | ✅ `latlng()`                               | ✅ Yes          | ✓ DONE                  | `CoordinatesGenerator.generate()` |
| Single coordinate    | ✅ `coordinate()`                           | ✅ Partial      | Explicit gap            | Latitude/longitude APIs exist; center/radius option not modeled |
| Local lat/lng        | ✅ `local_latlng()`                         | ✅ Partial      | Explicit gap            | Locale-bounded coordinates exist; real populated-place lookup deferred |
| Location on land     | ✅ `location_on_land()`                     | ❌ No           | LOW                     | Curated land-only dataset    |

### 3. INTERNET & NETWORKING

| Feature           | faker Support                    | krandom Status | Implementation Priority | Notes                       |
|-------------------|----------------------------------|----------------|-------------------------|-----------------------------|
| **Email**         |
| Email address     | ✅ `email()`                      | ✅ Yes          | ✓ DONE                  | Email generator exists      |
| Safe email        | ✅ `safe_email()`                 | ✅ Yes          | ✓ DONE                  | `EmailGenerator.generateSafeEmail()` |
| Free email        | ✅ `free_email()`                 | ✅ Yes          | ✓ DONE                  | `EmailGenerator.generateFreeEmail()` |
| Company email     | ✅ `company_email()`              | ✅ Yes          | ✓ DONE                  | `CompanyEmailGenerator` / `EmailGenerator.generateCompanyEmail()` |
| Free email domain | ✅ `free_email_domain()`          | ❌ No           | LOW                     | gmail.com, yahoo.com        |
| **Domain & URLs** |
| Domain name       | ✅ `domain_name()`                | ✅ Yes          | ✓ DONE                  | `DomainGenerator`           |
| Hostname          | ✅ `hostname()`                   | ✅ Yes          | ✓ DONE                  | `HostnameGenerator`         |
| TLD               | ✅ `tld()`                        | ✅ Yes          | ✓ DONE                  | `DomainGenerator.getTLD()`  |
| URL               | ✅ `url()`                        | ✅ Yes          | ✓ DONE                  | `URLGenerator`              |
| URI               | ✅ `uri()`                        | ✅ Yes          | ✓ DONE                  | `UriGenerator`              |
| Slug              | ✅ `slug()`                       | ✅ Yes          | ✓ DONE                  | `SlugGenerator`             |
| **IP Addresses**  |
| IPv4              | ✅ `ipv4()`                       | ✅ Yes          | ✓ DONE                  | Already implemented         |
| IPv4 private      | ✅ `ipv4_private()`               | ✅ Yes          | ✓ DONE                  | `IPv4Generator.generatePrivate()` |
| IPv4 public       | ✅ `ipv4_public()`                | ✅ Yes          | ✓ DONE                  | `IPv4Generator.generatePublic()` |
| IPv4 by class     | ✅ `ipv4(address_class='a/b/c')`  | ❌ No           | LOW                     | Classful addressing         |
| IPv4 network      | ✅ `ipv4(network=True)`           | ✅ Yes          | ✓ DONE                  | `IPv4Generator.generateCidr()` |
| IPv6              | ✅ `ipv6()`                       | ✅ Yes          | ✓ DONE                  | Already implemented         |
| IPv6 network      | ✅ `ipv6(network=True)`           | ✅ Yes          | ✓ DONE                  | `IPv6Generator.generateCidr()` |
| **Network**       |
| MAC address       | ✅ `mac_address()`                | ✅ Yes          | ✓ DONE                  | `MacAddressGenerator`       |
| MAC multicast     | ✅ `mac_address(multicast=True)`  | ❌ No           | LOW                     | Multicast MACs              |
| Port number       | ✅ `port_number()`                | ✅ Yes          | ✓ DONE                  | `PortGenerator`             |
| System port       | ✅ `port_number(is_system=True)`  | ❌ No           | LOW                     | 0-1023                      |
| User port         | ✅ `port_number(is_user=True)`    | ❌ No           | LOW                     | 1024-49151                  |
| Dynamic port      | ✅ `port_number(is_dynamic=True)` | ❌ No           | LOW                     | 49152-65535                 |
| HTTP method       | ✅ `http_method()`                | ✅ Yes          | ✓ DONE                  | `HttpMethodGenerator`       |
| HTTP status code  | ✅ `http_status_code()`           | ✅ Yes          | ✓ DONE                  | `HttpStatusCodeGenerator`   |
| **Identifiers**   |
| UUID v4           | ✅ `uuid4()`                      | ✅ Yes          | ✓ DONE                  | `UUIDGenerator.generateV4()` |
| **User Identity** |
| Username          | ✅ `user_name()`                  | ✅ Yes          | ✓ DONE                  | Username generator exists   |
| **User Agents**   |
| User agent        | ✅ `user_agent()`                 | ✅ Yes          | ✓ DONE                  | `UserAgentGenerator`        |
| Chrome            | ✅ `chrome()`                     | ❌ No           | LOW                     | Chrome UA                   |
| Firefox           | ✅ `firefox()`                    | ❌ No           | LOW                     | Firefox UA                  |
| Safari            | ✅ `safari()`                     | ❌ No           | LOW                     | Safari UA                   |
| Opera             | ✅ `opera()`                      | ❌ No           | LOW                     | Opera UA                    |
| Android           | ✅ `android()`                    | ❌ No           | LOW                     | Android UA                  |
| iOS               | ✅ `ios()`                        | ❌ No           | LOW                     | iOS UA                      |

### 4. FINANCE & COMMERCE

| Feature            | faker Support                                                           | krandom Status | Implementation Priority | Notes                         |
|--------------------|-------------------------------------------------------------------------|----------------|-------------------------|-------------------------------|
| **Credit Cards**   |
| Credit card number | ✅ Luhn-valid, 9 types                                                   | ✅ Yes          | ✓ DONE                  | `CreditCardGenerator` with Luhn-valid card numbers |
| Card types         | ✅ visa, mastercard, amex, diners, discover, jcb, visa13, visa16, visa19 | ✅ Partial      | Explicit gap            | `CardType` covers major networks; some Faker-specific variants omitted |
| Card expiry        | ✅ `credit_card_expire()`                                                | ✅ Yes          | ✓ DONE                  | `CardExpirationGenerator` / `CreditCardGenerator.generateExpiry()` |
| Security code      | ✅ `credit_card_security_code()`                                         | ✅ Yes          | ✓ DONE                  | `CreditCardGenerator.generateSecurityCode()` |
| Card provider      | ✅ `credit_card_provider()`                                              | ✅ Yes          | ✓ DONE                  | `CreditCardGenerator.generateProvider()` |
| Full card          | ✅ `credit_card_full()`                                                  | ✅ Yes          | ✓ DONE                  | `CreditCardGenerator.generateFull()` / `CreditCardInfoGenerator` |
| **Banking**        |
| IBAN               | ✅ `iban()`                                                              | ✅ Yes          | ✓ DONE                  | `IbanGenerator`               |
| SWIFT/BIC          | ✅ `swift()`, `swift8()`, `swift11()`                                    | ✅ Yes          | ✓ DONE                  | `BicGenerator`                |
| ABA routing        | ✅ `aba()`                                                               | ✅ Yes          | ✓ DONE                  | `AbaRoutingGenerator`         |
| BBAN               | ✅ `bban()`                                                              | ❌ No           | LOW                     | Basic bank account            |
| Bank country       | ✅ `bank_country()`                                                      | ❌ No           | LOW                     | ISO country code              |
| **Currency**       |
| Currency dict      | ✅ `currency()`                                                          | ✅ Yes          | ✓ DONE                  | `CurrencyGenerator.generateAsMap()` / `CurrencyInfo` |
| Currency code      | ✅ `currency_code()`                                                     | ✅ Yes          | ✓ DONE                  | `CurrencyGenerator.generateCurrencyCode()` |
| Currency name      | ✅ `currency_name()`                                                     | ✅ Yes          | ✓ DONE                  | `CurrencyGenerator.generateCurrencyName()` |
| Currency symbol    | ✅ `currency_symbol()`                                                   | ✅ Yes          | ✓ DONE                  | `CurrencyGenerator.generateCurrencySymbol()` |
| Price tag          | ✅ `pricetag()`                                                          | ✅ Yes          | ✓ DONE                  | `CurrencyGenerator.generatePriceTag()` / `MoneyGenerator` |

### 5. COMPANY & BUSINESS

| Feature         | faker Support        | krandom Status | Implementation Priority | Notes               |
|-----------------|----------------------|----------------|-------------------------|---------------------|
| Company name    | ✅ `company()`        | ✅ Yes          | ✓ DONE                  | `CompanyNameGenerator` |
| Company suffix  | ✅ `company_suffix()` | ✅ Yes          | ✓ DONE                  | `CompanyNameGenerator.generate(true)` |
| Buzzword phrase | ✅ `bs()`             | ❌ No           | LOW                     | Business-speak      |
| Catch phrase    | ✅ `catch_phrase()`   | ❌ No           | LOW                     | Corporate slogans   |

### 6. JOB & CAREER

| Feature   | faker Support | krandom Status | Implementation Priority | Notes               |
|-----------|---------------|----------------|-------------------------|---------------------|
| Job title | ✅ `job()`     | ✅ Yes          | ✓ DONE                  | `PositionGenerator` / `JobInfoGenerator` |

### 7. TEXT & LOREM

| Feature          | faker Support                                  | krandom Status | Implementation Priority | Notes                  |
|------------------|------------------------------------------------|----------------|-------------------------|------------------------|
| **Lorem Ipsum**  |
| Word             | ✅ `word()`                                     | ✅ Yes          | ✓ DONE                  | `WordGenerator`        |
| Words            | ✅ `words(nb=6)`                                | ✅ Yes          | ✓ DONE                  | `WordGenerator.generateList(n)` |
| Sentence         | ✅ `sentence()`                                 | ✅ Yes          | ✓ DONE                  | `SentenceGenerator`    |
| Sentences        | ✅ `sentences(nb=3)`                            | ✅ Yes          | ✓ DONE                  | `SentenceGenerator.generateList(n)` |
| Paragraph        | ✅ `paragraph()`                                | ✅ Yes          | ✓ DONE                  | `ParagraphGenerator`   |
| Paragraphs       | ✅ `paragraphs(nb=3)`                           | ✅ Yes          | ✓ DONE                  | `ParagraphGenerator.generateList(n)` |
| Text             | ✅ `text(max_nb_chars=200)`                     | ✅ Yes          | ✓ DONE                  | `TextGenerator`        |
| Texts            | ✅ `texts(nb_texts=3)`                          | ✅ Yes          | ✓ DONE                  | `TextGenerator.generateList(n)` |
| Custom word list | ✅ `ext_word_list` parameter                    | ❌ No           | LOW                     | Custom vocabulary      |
| Variable length  | ✅ `variable_nb_words`, `variable_nb_sentences` | ✅ Yes          | ✓ DONE                  | Range-style APIs on text generators |
| Unique words     | ✅ `words(unique=True)`                         | ✅ Partial      | Explicit gap            | `UniqueGenerator` can wrap `WordGenerator`; no Faker-style flag |

### 8. DATE & TIME

| Feature                   | faker Support           | krandom Status | Implementation Priority | Notes                          |
|---------------------------|-------------------------|----------------|-------------------------|--------------------------------|
| **Date Generation**       |
| Date                      | ✅ `date()`              | ✅ Yes          | ✓ DONE                  | `DateGenerator`                |
| Date between              | ✅ `date_between()`      | ✅ Yes          | ✓ DONE                  | `DateGenerator.between(...)`   |
| Date of birth             | ✅ `date_of_birth()`     | ✅ Yes          | ✓ DONE                  | `BirthdayGenerator`            |
| Future date               | ✅ `future_date()`       | ✅ Yes          | ✓ DONE                  | `DateGenerator.future(...)`    |
| Past date                 | ✅ `past_date()`         | ✅ Yes          | ✓ DONE                  | `DateGenerator.past(...)`      |
| **DateTime Generation**   |
| DateTime                  | ✅ `date_time()`         | ✅ Yes          | ✓ DONE                  | `LocalDateTimeGenerator`       |
| DateTime between          | ✅ `date_time_between()` | ✅ Yes          | ✓ DONE                  | `LocalDateTimeGenerator.between(...)` |
| Future datetime           | ✅ `future_datetime()`   | ✅ Yes          | ✓ DONE                  | `LocalDateTimeGenerator.future(...)` |
| Past datetime             | ✅ `past_datetime()`     | ✅ Yes          | ✓ DONE                  | `LocalDateTimeGenerator.past(...)` |
| ISO 8601                  | ✅ `iso8601()`           | ✅ Yes          | ✓ DONE                  | Java time values serialize/format as ISO-8601 |
| Unix time                 | ✅ `unix_time()`         | ✅ Yes          | ✓ DONE                  | `DateGenerator.generateTimestamp()` |
| Time                      | ✅ `time()`              | ✅ Yes          | ✓ DONE                  | `TimeGenerator`                 |
| Time delta                | ✅ `time_delta()`        | ✅ Yes          | ✓ DONE                  | `DurationGenerator`             |
| Time series               | ✅ `time_series()`       | ❌ No           | LOW                     | Lazy generator                 |
| **Date Components**       |
| Day of month              | ✅ `day_of_month()`      | ✅ Partial      | Explicit gap            | `DateGenerator.generateWithDay(...)` exists; no direct formatted-string helper |
| Day of week               | ✅ `day_of_week()`       | ✅ Partial      | Explicit gap            | Java `LocalDate` exposes day-of-week; no dedicated string generator |
| Month                     | ✅ `month()`             | ✅ Partial      | Explicit gap            | `DateGenerator.generateWithMonth(...)` exists; no direct formatted-string helper |
| Month name                | ✅ `month_name()`        | ✅ Partial      | Explicit gap            | Java `LocalDate` can format month names; no dedicated string generator |
| Year                      | ✅ `year()`              | ✅ Partial      | Explicit gap            | `DateGenerator.generateWithYear(...)` exists; no direct formatted-string helper |
| Century                   | ✅ `century()`           | ❌ No           | LOW                     | Roman numerals                 |
| AM/PM                     | ✅ `am_pm()`             | ❌ No           | LOW                     | AM or PM                       |
| **Timezone**              |
| Timezone                  | ✅ `timezone()`          | ✅ Yes          | ✓ DONE                  | `TimezoneGenerator`             |
| **Date Offset Shorthand** |
| Support for '+10d', '-2y' | ✅ Yes                   | ❌ No (intentional) | SKIP                | Python-style relative-date parser deferred; Java range APIs are explicit |

### 9. PHONE NUMBERS

| Feature              | faker Support              | krandom Status | Implementation Priority | Notes                  |
|----------------------|----------------------------|----------------|-------------------------|------------------------|
| Phone number         | ✅ `phone_number()`         | ✅ Yes          | ✓ DONE                  | `PhoneNumberGenerator` |
| Country calling code | ✅ `country_calling_code()` | ✅ Yes          | ✓ DONE                  | `PhoneNumberGenerator.generateCountryCallingCode()` |
| MSISDN               | ✅ `msisdn()`               | ✅ Yes          | ✓ DONE                  | `PhoneNumberGenerator.generateMsisdn()` |

### 10. NUMBERS & CODES

| Feature           | faker Support                                                | krandom Status | Implementation Priority | Notes                       |
|-------------------|--------------------------------------------------------------|----------------|-------------------------|-----------------------------|
| **Basic Numbers** |
| Random int        | ✅ `pyint()`                                                  | ✅ Yes          | ✓ DONE                  | IntGenerator                |
| Random float      | ✅ `pyfloat()`                                                | ✅ Yes          | ✓ DONE                  | FloatGenerator              |
| Random decimal    | ✅ `pydecimal()`                                              | ✅ Yes          | ✓ DONE                  | `PyDecimalGenerator`             |
| Boolean           | ✅ `pybool()` / `boolean()`                                   | ✅ Yes          | ✓ DONE                  | BooleanGenerator            |
| Weighted boolean  | ✅ `boolean(chance_of_getting_true=50)`                       | ✅ Yes          | ✓ DONE                  | `BooleanGenerator.withLikelihood(...)` |
| Null boolean      | ✅ `null_boolean()`                                           | ❌ No           | LOW                     | True/False/None             |
| **Book Codes**    |
| ISBN-10           | ✅ `isbn10()`                                                 | ✅ Yes          | ✓ DONE                  | `IsbnGenerator`             |
| ISBN-13           | ✅ `isbn13()`                                                 | ✅ Yes          | ✓ DONE                  | `IsbnGenerator`             |
| **Product Codes** |
| EAN-8             | ✅ `ean8()`                                                   | ✅ Yes          | ✓ DONE                  | `EanGenerator`              |
| EAN-13            | ✅ `ean13()`                                                  | ✅ Yes          | ✓ DONE                  | `EanGenerator`              |
| EAN (generic)     | ✅ `ean()`                                                    | ✅ Yes          | ✓ DONE                  | `EanGenerator`              |
| Localized EAN     | ✅ `localized_ean()`, `localized_ean8()`, `localized_ean13()` | ❌ No           | LOW                     | With country prefix         |
| **Hashing**       |
| MD5               | ✅ `md5()`                                                    | ✅ Partial      | Explicit gap            | `HashGenerator` emits digest-shaped hex; not a real message digest |
| SHA1              | ✅ `sha1()`                                                   | ✅ Partial      | Explicit gap            | `HashGenerator` supports SHA-length output; not algorithmic digesting |
| SHA256            | ✅ `sha256()`                                                 | ✅ Partial      | Explicit gap            | `HashGenerator` supports SHA-length output; not algorithmic digesting |
| Binary            | ✅ `binary()`                                                 | ❌ No           | LOW                     | Random bytes                |

### 11. COLOR

| Feature         | faker Support         | krandom Status | Implementation Priority | Notes              |
|-----------------|-----------------------|----------------|-------------------------|--------------------|
| Color name      | ✅ `color_name()`      | ✅ Yes          | ✓ DONE                  | `ColorGenerator.generateColorName()` |
| Safe color name | ✅ `safe_color_name()` | ✅ Yes          | ✓ DONE                  | `ColorGenerator.generateSafeColorName()` |
| Hex color       | ✅ `hex_color()`       | ✅ Yes          | ✓ DONE                  | `ColorGenerator`     |
| Safe hex color  | ✅ `safe_hex_color()`  | ❌ No           | LOW                     | Short hex #ff0     |
| RGB color       | ✅ `rgb_color()`       | ✅ Yes          | ✓ DONE                  | `ColorGenerator`     |
| RGB CSS         | ✅ `rgb_css_color()`   | ✅ Yes          | ✓ DONE                  | `ColorGenerator`     |
| HSL color       | ✅ `hsl_color()`       | ❌ No           | LOW                     | hsl(120, 50%, 50%) |
| RGB tuple       | ✅ `color_rgb()`       | ✅ Partial      | Explicit gap            | RGB values exist as formatted strings; tuple return shape is Python-specific |
| RGB float tuple | ✅ `color_rgb_float()` | ❌ No           | LOW                     | (r, g, b) 0..1     |
| HSL tuple       | ✅ `color_hsl()`       | ❌ No           | LOW                     | (H, S%, L%)        |

### 12. AUTOMOTIVE

| Feature       | faker Support       | krandom Status | Implementation Priority | Notes              |
|---------------|---------------------|----------------|-------------------------|--------------------|
| VIN           | ✅ `vin()`           | ❌ No (intentional) | SKIP                | Automotive identifiers are long-tail domain data |
| License plate | ✅ `license_plate()` | ❌ No (intentional) | SKIP                | Automotive identifiers are long-tail domain data |

### 13. FILE & UNIX

| Feature        | faker Support                       | krandom Status | Implementation Priority | Notes              |
|----------------|-------------------------------------|----------------|-------------------------|--------------------|
| File extension | ✅ `file_extension()`                | ✅ Yes          | ✓ DONE                  | `FileExtensionGenerator` |
| File name      | ✅ `file_name()`                     | ✅ Yes          | ✓ DONE                  | `FileNameGenerator` |
| File path      | ✅ `file_path()`                     | ✅ Yes          | ✓ DONE                  | `FilePathGenerator` |
| MIME type      | ✅ `mime_type()`                     | ✅ Yes          | ✓ DONE                  | `MimeTypeGenerator` |
| Unix device    | ✅ `unix_device()`                   | ❌ No           | LOW                     | /dev/sda           |
| Unix partition | ✅ `unix_partition()`                | ❌ No           | LOW                     | /dev/sda1          |
| File category  | ✅ audio, image, office, text, video | ❌ No           | LOW                     | Category filtering |

### 14. PYTHON NATIVE TYPES

| Feature      | faker Support      | krandom Status | Implementation Priority | Notes            |
|--------------|--------------------|----------------|-------------------------|------------------|
| pyint        | ✅ `pyint()`        | ✅ Yes          | ✓ DONE                  | IntGenerator     |
| pyfloat      | ✅ `pyfloat()`      | ✅ Yes          | ✓ DONE                  | FloatGenerator   |
| pydecimal    | ✅ `pydecimal()`    | ✅ Yes          | ✓ DONE                  | `PyDecimalGenerator` |
| pybool       | ✅ `pybool()`       | ✅ Yes          | ✓ DONE                  | BooleanGenerator |
| pystr        | ✅ `pystr()`        | ✅ Yes          | ✓ DONE                  | StringGenerator  |
| pystr_format | ✅ `pystr_format()` | ✅ Partial      | Explicit gap            | `TemplateStringGenerator` handles `#`/`?`; `{provider}` registry remains open |
| pylist       | ✅ `pylist()`       | ❌ No (intentional) | SKIP                | Python-native return type; Java callers use `Generator.generateList(n)` |
| pydict       | ✅ `pydict()`       | ❌ No (intentional) | SKIP                | Python-native return type; Java callers use schema/map generation |
| pytuple      | ✅ `pytuple()`      | ❌ No (intentional) | SKIP                | Python-native return type; no Java tuple equivalent |

### 15. STRUCTURED OUTPUT

| Feature         | faker Support | krandom Status | Implementation Priority | Notes             |
|-----------------|---------------|----------------|-------------------------|-------------------|
| CSV generation  | ✅ `csv()`     | ✅ Yes          | ✓ DONE                  | `Schema.toCsv()` |
| JSON generation | ✅ `json()`    | ✅ Yes          | ✓ DONE                  | `Schema.toJsonLines()` |

### 16. SECURITY

| Feature             | faker Support                                   | krandom Status | Implementation Priority | Notes             |
|---------------------|-------------------------------------------------|----------------|-------------------------|-------------------|
| Password            | ✅ `password()`                                  | ✅ Yes          | ✓ DONE                  | `PasswordGenerator` |
| Password length     | ✅ `length` param                                | ✅ Yes          | ✓ DONE                  | `PasswordGenerator.generate(min,max)` |
| Password complexity | ✅ special_chars, digits, upper_case, lower_case | ✅ Yes          | ✓ DONE                  | Policy options on `PasswordGenerator` |

---

## ADVANCED FEATURES

### Configuration & Customization

| Feature                  | faker                                     | krandom   | Priority | Implementation Notes          |
|--------------------------|-------------------------------------------|-----------|----------|-------------------------------|
| **Locale Support**       |
| Multiple locales         | ✅ 80+ locales                             | ✅ Partial | Explicit gap | `SupportedLocale` covers the project-supported locale set, not Faker's full 80+ |
| Locale-aware data        | ✅ Names, addresses, phones                | ✅ Yes     | ✓ DONE   | Names, addresses, phones, IDs, and text are locale-aware |
| Runtime locale switching | ✅ Constructor param                       | ✅ Yes     | ✓ DONE   | Pass `Locale` or `GeneratorConfig` per generator |
| Multi-locale mixing      | ✅ `Faker(['en_US', 'de_DE'])`             | ❌ No / Deferred | DESIGN | Single-locale `GeneratorConfig` is intentional until a concrete use case defines the contract |
| Weighted locale          | ✅ `weights=[0.7, 0.3]`                    | ❌ No      | LOW      | Probability distribution      |
| **Seeding**              |
| Class-level seed         | ✅ `Faker.seed(n)`                         | ❌ No (intentional) | SKIP | Global mutable seed conflicts with instance-scoped `GeneratorConfig.seed(...)` |
| Instance-level seed      | ✅ `faker.seed_instance(n)`                | ✅ Yes     | ✓ DONE   | Most generators support       |
| Custom random            | ✅ `generator` param                       | ✅ Yes     | ✓ DONE   | `GeneratorConfig.Builder.randomFactory(Supplier<? extends Random>)` |
| **String Utilities**     |
| Numerify                 | ✅ `numerify("###-####")`                  | ✅ Yes     | ✓ DONE   | `TextFormatProvider.numerify` |
| Letterify                | ✅ `lexify("???-???")`                     | ✅ Yes     | ✓ DONE   | `TextFormatProvider.lexify`   |
| Bothify                  | ✅ `bothify("???-###")`                    | ✅ Yes     | ✓ DONE   | `TextFormatProvider.bothify`  |
| Hexify                   | ✅ `hexify("^^^^")`                        | ✅ Yes     | ✓ DONE   | `TextFormatProvider.hexify(...)` |
| **Uniqueness**           |
| Unique values            | ✅ `faker.unique.method()`                 | ✅ Yes     | ✓ DONE   | `Generators.unique(source)`   |
| Uniqueness clear         | ✅ `faker.unique.clear()`                  | ✅ Yes     | ✓ DONE   | `UniqueGenerator.reset()` clears remembered values |
| Uniqueness exception     | ✅ `UniquenessException`                   | ✅ Yes     | ✓ DONE   | Throws `IllegalStateException` after `maxAttempts` |
| **Custom Providers**     |
| Add provider             | ✅ `add_provider(MyProvider)`              | ✅ Yes     | ✓ DONE   | `ProviderHub.register(...)` |
| BaseProvider utilities   | ✅ `random_element`, `random_elements`     | ✅ Yes     | ✓ DONE   | `PickGenerator`, `PickSetGenerator`, `WeightedGenerator` |
| **Collections**          |
| Random elements          | ✅ `random_elements(list, length, unique)` | ✅ Yes     | ✓ DONE   | `PickSetGenerator` / `Generators.unique(...)` |
| Random element           | ✅ `random_element(list)`                  | ✅ Yes     | ✓ DONE   | `PickGenerator`               |
| **Profile Generation**   |
| Simple profile           | ✅ `simple_profile()`                      | ✅ Yes     | ✓ DONE   | `SimpleProfileGenerator`      |
| Full profile             | ✅ `profile()`                             | ✅ Yes     | ✓ DONE   | `ProfileGenerator`            |

---

## IMPLEMENTATION RECOMMENDATIONS

### Phase 1: CRITICAL GAPS (Must Have) - 20 days

1. **Email Enhancement** - 1 day
    - `safe_email()` - non-deliverable domains
    - `free_email()` - gmail, yahoo domains

2. **UUID Generation** - 1 day
    - `uuid4()` - proper UUID support

3. **Lorem Text** - 3 days
    - `word()`, `words()`, `sentence()`, `paragraph()`
    - Variable length support
    - Custom word lists

4. **String Templates** - 3 days
    - `numerify()`, `lexify()`, `bothify()`, `hexify()`
    - Powerful template-based generation

5. **Unique Value Enforcement** - 2 days
    - `unique()` proxy wrapper
    - UniquenessException
    - Clear mechanism

6. **Password Generator** - 2 days
    - Policy-based generation
    - Character class requirements

7. **Date/Time Generators** - 4 days
    - `date_between()`, `date_of_birth()` with age bounds
    - `future_date()`, `past_date()`
    - `iso8601()`, `unix_time()`

8. **Boolean Enhancement** - 1 day
    - Weighted boolean generation

9. **Hash Generators** - 1 day
    - SHA1, SHA256 support

10. **Profile Composite** - 2 days
    - `simple_profile()`, `profile()`
    - Combine existing generators

### Phase 2: HIGH VALUE (Should Have) - 25 days

1. **Address Components** - 5 days
    - Street, city, state, ZIP, country
    - Building numbers, secondary addresses

2. **Phone Numbers** - 3 days
    - Locale-formatted phone numbers
    - Country calling codes

3. **Credit Cards Enhancement** - 3 days
    - BIN prefixes for major card types
    - Card expiry, CVV
    - Full card composite

4. **Banking** - 4 days
    - IBAN with check digits
    - SWIFT/BIC codes
    - ABA routing numbers

5. **Names Enhancement** - 3 days
    - Full name composite
    - Gender-specific names
    - Name suffixes

6. **Company Data** - 2 days
    - Company names, suffixes
    - Buzzwords, catch phrases

7. **URL/Domain Generation** - 2 days
    - Full URLs, domain names
    - Slugs, TLDs

8. **Currency** - 2 days
    - Currency codes, names, symbols
    - Price formatting

9. **Color Generators** - 1 day
    - Hex, RGB, HSL, named colors

### Phase 3: NICE TO HAVE (Could Have) - 15 days

1. **MAC Addresses** - 1 day
2. **HTTP Utilities** - 1 day
    - Methods, status codes, ports
3. **Product Codes** - 3 days
    - ISBN-10/13, EAN-8/13 with check digits
4. **Automotive** - 2 days
    - VIN, license plates
5. **File Generators** - 2 days
    - File names, paths, MIME types
6. **User Agents** - 2 days
    - Browser UA strings
7. **Passport Data** - 2 days
    - Passport numbers, MRZ
8. **Coordinates** - 1 day
    - Lat/lng generation
9. **Python Native Types** - 1 day
    - pylist, pydict, pytuple

### Phase 4: LOCALE INFRASTRUCTURE - 10 days

1. **Locale Support Framework** - 5 days
    - Locale-aware resource loading
    - Runtime locale switching
    - Multi-locale support

2. **Locale Data Files** - 5 days
    - Names, addresses, phones for top 10 locales
    - US, UK, DE, FR, ES, IT, PT, ZH, JA, RU

---

## EFFORT ESTIMATES

### High Priority Features (Phase 1 + 2)

- Phase 1 (Critical): **20 days**
- Phase 2 (High Value): **25 days**
- **TOTAL: ~45 days** (2 sprints)

### Medium Priority Features (Phase 3)

- Phase 3 (Nice to Have): **15 days**

### Locale Infrastructure (Phase 4)

- Locale Framework: **10 days**

### **Grand Total: ~70 days** (3.5 months)

---

## KEY DIFFERENTIATORS

### faker (Python) Strengths (vs krandom)

1. **80+ locales** with comprehensive locale-aware data
2. **Mature ecosystem** - 10+ years, 27k+ stars
3. **Provider architecture** - easy plugin system
4. **Template utilities** (numerify, lexify, bothify, hexify)
5. **Unique value enforcement** built-in
6. **Extensive date/time** utilities with shortcuts
7. **Real-world datasets** for cities, countries, companies
8. **Profile composites** - complete user/passport data
9. **Multi-locale mixing** with weighted selection
10. **Python-native types** (pylist, pydict, pytuple)
11. **Structured output** (CSV, JSON generation)
12. **Comprehensive financial** data (IBAN, credit cards)

### krandom Strengths (vs faker)

1. **Type-safe builders** for complex generators
2. **Fluent API** - more composable
3. **Game generators** - Dice, Coin (unique to krandom)
4. **Algorithm generators** - Fibonacci, Luhn
5. **Object graph generation** - reflection-based
6. **Better test coverage** (99%+)
7. **Immutable results** - thread-safe by default
8. **Stream API** - infinite streams support
9. **Functional composition** - map/filter on generators
10. **Cleaner architecture** - less magic

---

## COMPATIBILITY ASSESSMENT

### Direct Port Feasibility

- ✅ **Easy**: Basic generators (lorem, colors, hashes, UUIDs)
- ✅ **Moderate**: Address, phone, company, job data
- ⚠️ **Hard**: Locale infrastructure, template utilities
- ⚠️ **Complex**: IBAN/SWIFT with check digits, VIN validation
- ❌ **Not Applicable**: Python-specific (pylist, pydict, pytuple)

### Recommended Approach

1. **Prioritize business value** - Email, addresses, dates, phones first
2. **Implement template utilities** - High leverage (numerify, bothify)
3. **Build locale foundation** - Start with 5-10 locales (US, UK, DE, FR, ES)
4. **Add unique enforcement** - Common requirement, reusable
5. **Focus on core financial** - Credit cards, IBAN (skip exotic codes)
6. **Skip Python-specific** - pylist/pydict not relevant for Kotlin/Java
7. **Maintain krandom identity** - Keep type-safety, fluent API, immutability
8. **Learn from maturity** - faker's 10-year evolution shows what users need

---

## LOCALE PRIORITY RANKING

Based on usage, market size, and developer demographics:

### Tier 1 (Essential) - Implement First

1. **en_US** - English (United States) - Default, largest market
2. **en_GB** - English (United Kingdom) - Major market, different formats
3. **de_DE** - German - Largest EU economy
4. **fr_FR** - French (France) - Major EU market
5. **es_ES** - Spanish (Spain) - Major EU market

### Tier 2 (High Value)

6. **zh_CN** - Chinese (Simplified) - Massive market
7. **ja_JP** - Japanese - Large tech market
8. **pt_BR** - Portuguese (Brazil) - Largest Latin America
9. **ru_RU** - Russian - Large market
10. **it_IT** - Italian - Major EU economy

### Tier 3 (Good to Have)

11. **es_MX** - Spanish (Mexico) - North America
12. **ko_KR** - Korean - Tech market
13. **nl_NL** - Dutch - EU market
14. **pl_PL** - Polish - Growing EU market
15. **tr_TR** - Turkish - Growing market

---

## CONCLUSION

Python Faker represents the gold standard for fake data libraries with unmatched maturity, locale coverage, and real-world validation. krandom should focus on:

### Focus Areas (Ranked by Impact)

1. ✅ **Template Utilities** (numerify, bothify) - High leverage, enables many use cases
2. ✅ **Lorem Text Generation** - Critical for UI/content testing (currently missing)
3. ✅ **Address Components** - Essential business data (street, city, state, ZIP, country)
4. ✅ **Date/Time Enhancement** - Add age-bounded DOB, past/future dates, ISO8601
5. ✅ **Email Enhancement** - Safe emails, free email providers
6. ✅ **Unique Value Enforcement** - Prevent duplicates across all generators
7. ✅ **Locale Infrastructure** - Start with Tier 1 locales (US, UK, DE, FR, ES)
8. ✅ **Financial Data** - Credit cards with BINs, IBAN, currency
9. ✅ **Password Generator** - Policy-based, common testing need
10. ✅ **Profile Composites** - Combine generators into complete user profiles

### Skip (Low ROI)

- Python-specific types (pylist, pydict, pytuple)
- Exotic locales beyond Tier 3
- Entertainment/niche data
- Unix device paths
- Passport MRZ (complex, low usage)

### Maintain krandom Advantages

- Type-safe builders over dynamic proxies
- Immutable, thread-safe results
- Functional composition (map/filter)
- Game and algorithm generators (unique selling point)
- Cleaner architecture without magic

### Success Metrics

- **Core coverage**: Match faker on top 20 most-used methods
- **Locale support**: 5-10 locales (vs faker's 80)
- **Template power**: numerify/bothify equivalents
- **Uniqueness**: Built-in unique() wrapper
- **Quality**: Maintain 99%+ test coverage
- **Identity**: Keep Kotlin-first, type-safe approach

**Target**: Match faker's core business value (80%) with 30% of the implementation effort by focusing on high-impact features and skipping long-tail, niche providers.
