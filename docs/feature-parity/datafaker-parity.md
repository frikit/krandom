# DataFaker Feature Parity Analysis

## Library Overview

- **Name**: DataFaker
- **Language**: Java
- **Version Analyzed**: Latest (2024+)
- **GitHub**: https://github.com/datafaker-net/datafaker
- **License**: Apache 2.0
- **Key Strength**: 200+ providers, extensive localization, schema-based output

**Last Updated**: 2026-02-28 (Java phased plan moved to `docs/plans/datafaker-java-plan.md`)

## Java Execution Plan

- Active plan: `docs/plans/datafaker-java-plan.md`
- Current scope: Java parity only (Kotlin/Scala deferred)
- Delivery model: one parity slice at a time with tests + `./scripts/pre_commit_check.sh`

## Executive Summary

DataFaker is the most feature-rich Java faker library with 200+ generator providers, 60+ locales, and advanced features like schema-based output formats (CSV/JSON/YAML/XML), unique value enforcement,
and GraalVM native image support. It's a fork of JavaFaker with significant enhancements.

---

## Feature Categories

### 1. PERSONAL IDENTITY

| Feature                     | DataFaker Support                            | krandom Status | Implementation Priority | Notes                                                  |
|-----------------------------|----------------------------------------------|----------------|-------------------------|--------------------------------------------------------|
| **Name Generation**         |
| Full name                   | ✅ `name()`, `fullName()`, `nameWithMiddle()` | ✅ Partial      | HIGH                    | krandom has basic name, needs middle name              |
| First name                  | ✅ `firstName()`                              | ✅ Yes          | ✓ DONE                  |                                                        |
| Last name                   | ✅ `lastName()`                               | ✅ Yes          | ✓ DONE                  |                                                        |
| Gender-specific first names | ✅ `femaleFirstName()`, `maleFirstName()`     | ✅ Yes          | ✓ DONE                  | `gen.generate(Gender.MALE/FEMALE)` — 10 locales        |
| Name prefix                 | ✅ `prefix()` (Mr., Mrs., Dr.)                | ✅ Yes          | ✓ DONE                  | `TitleGenerator` — 10 locales, extensible              |
| Name suffix                 | ✅ `suffix()` (Jr., Sr., III)                 | ✅ Yes          | ✓ DONE                  | `SuffixGenerator` — 10 locales                         |
| Title                       | ✅ `title()` (professional titles)            | ✅ Yes          | ✓ DONE                  | `TitleGenerator` for honorifics                        |
| **ID Numbers**              |
| SSN (US)                    | ✅ `ssnValid()`                               | ✅ Yes          | ✓ DONE                  | `NationalIdGenerator(Locale.US)` — area 666 excluded   |
| Singapore FIN/UIN           | ✅ `singaporeanFin()`, `singaporeanUin()`     | ❌ No           | LOW                     | Locale-specific                                        |
| Poland PESEL                | ✅ `peselNumber()`                            | ❌ No           | LOW                     | Locale-specific                                        |
| China SSN                   | ✅ `validZhCNSsn()`                           | ✅ Yes          | ✓ DONE                  | `NationalIdGenerator(Locale.CHINA)` — 18-char ISO 7064 |
| Portugal NIF                | ✅ `validPtNif()`                             | ❌ No           | LOW                     | Locale-specific                                        |
| Mexico SSN                  | ✅ `validEsMXSsn()`                           | ❌ No           | LOW                     | Locale-specific                                        |
| South Africa SSN            | ✅ `validEnZaSsn()`                           | ❌ No           | LOW                     | Locale-specific                                        |
| **Gender & Demographics**   |
| Gender types                | ✅ `types()`, `binaryTypes()`                 | ✅ Yes          | ✓ DONE                  | `GenderGenerator` — 10 locales, locale-aware labels    |
| Race                        | ✅ `race()`                                   | ❌ No           | LOW                     | Sensitive data                                         |
| Education level             | ✅ `educationalAttainment()`                  | ✅ Yes          | ✓ DONE                  | `EducationalAttainmentGenerator`                       |
| Marital status              | ✅ `maritalStatus()`                          | ✅ Yes          | ✓ DONE                  | `MaritalStatusGenerator`                               |
| **Relationships**           |
| Direct relationships        | ✅ `direct()` (mother, father)                | ❌ No           | LOW                     | Nice-to-have                                           |
| Extended family             | ✅ `extended()`, `inLaw()`                    | ❌ No           | LOW                     |                                                        |
| **Other Personal**          |
| Passport number             | ✅ `valid()`                                  | ❌ No           | MEDIUM                  | Travel documents                                       |
| Driver's license            | ✅ `drivingLicense()`                         | ❌ No           | MEDIUM                  | ID documents                                           |

### 2. ADDRESS & LOCATION

| Feature                  | DataFaker Support                                     | krandom Status | Implementation Priority | Notes                                                          |
|--------------------------|-------------------------------------------------------|----------------|-------------------------|----------------------------------------------------------------|
| **Street Address**       |
| Street name              | ✅ `streetName()`                                      | ✅ Yes          | ✓ DONE                  | `StreetAddressGenerator.generateStreetName()`                  |
| Street address           | ✅ `streetAddress()`                                   | ✅ Yes          | ✓ DONE                  | `StreetAddressGenerator.generate()`                            |
| Street number            | ✅ `streetAddressNumber()`, `buildingNumber()`         | ✅ Yes          | ✓ DONE                  | `generateStreetAddressNumber()` and `generateBuildingNumber()` |
| Secondary address        | ✅ `secondaryAddress()` (Apt, Suite)                   | ✅ Yes          | ✓ DONE                  | `StreetAddressGenerator.generateSecondaryAddress()`            |
| Street suffix/prefix     | ✅ `streetSuffix()`, `streetPrefix()`                  | ❌ No           | MEDIUM                  | St, Ave, Blvd                                                  |
| **City & State**         |
| City name                | ✅ `city()`, `cityName()`                              | ✅ Yes          | ✓ DONE                  | `CityGenerator` — 10 locales, locale-specific cities           |
| City prefix/suffix       | ✅ `cityPrefix()`, `citySuffix()`                      | ❌ No           | LOW                     | Building blocks                                                |
| State                    | ✅ `state()`                                           | ✅ Yes          | ✓ DONE                  | `StateGenerator.generate()` — 10 locales                       |
| State abbreviation       | ✅ `stateAbbr()`                                       | ✅ Yes          | ✓ DONE                  | `StateGenerator.generate(true)` — CA, TX, NSW, etc.            |
| **Postal Codes**         |
| ZIP code                 | ✅ `zipCode()`                                         | ✅ Yes          | ✓ DONE                  | `PostalCodeGenerator` — 10 locales                             |
| ZIP+4                    | ✅ `zipCodePlus4()`                                    | ✅ Yes          | ✓ DONE                  | `PostalCodeGenerator.generate(true)` → "90210-1234"            |
| ZIP by state             | ✅ `zipCodeByState()`                                  | ❌ No           | MEDIUM                  | State-specific mapping not implemented                         |
| County by ZIP            | ✅ `countyByZipCode()`                                 | ❌ No           | LOW                     | Geographic mapping                                             |
| Postcode (generic)       | ✅ `postcode()`                                        | ✅ Yes          | ✓ DONE                  | 10 locale-specific formats (JP: "100-0001", DE: "10115")       |
| Eircode (Ireland)        | ✅ `eircode()`                                         | ❌ No           | LOW                     | Locale-specific                                                |
| Mailbox                  | ✅ `mailBox()` (PO Box)                                | ❌ No           | LOW                     |                                                                |
| **Country & Nation**     |
| Country name             | ✅ `country()`                                         | ✅ Yes          | ✓ DONE                  | `CountryGenerator` — 195 countries, 10 locales                 |
| Country code             | ✅ `countryCode()`, `countryCode2()`, `countryCode3()` | ❌ No           | MEDIUM                  | CountryGenerator returns names, not ISO codes                  |
| Capital city             | ✅ `capital()`                                         | ❌ No           | MEDIUM                  | Geographic data                                                |
| Currency                 | ✅ `currency()`, `currencyCode()`                      | ✅ Yes          | ✓ DONE                  | Already in Money generator                                     |
| Flag emoji               | ✅ `flag()`                                            | ❌ No           | LOW                     | Unicode flags                                                  |
| Nationality              | ✅ `nationality()`                                     | ❌ No           | MEDIUM                  | Citizen of...                                                  |
| Language                 | ✅ `language()`, `isoLanguage()`                       | ❌ No           | MEDIUM                  | Spoken languages                                               |
| **Coordinates**          |
| Latitude                 | ✅ `latitude()`                                        | ✅ Yes          | ✓ DONE                  | `CoordinatesGenerator.generateLatitude()` — locale-bounded     |
| Longitude                | ✅ `longitude()`                                       | ✅ Yes          | ✓ DONE                  | `CoordinatesGenerator.generateLongitude()` — locale-bounded    |
| Lat/Lon pair             | ✅ `latLon()`, `lonLat()`                              | ✅ Yes          | ✓ DONE                  | `CoordinatesGenerator.generate()` → "35.12,-80.12"             |
| **Direction & Location** |
| Compass direction        | ✅ `word()`, `abbreviation()`, `azimuth()`             | ❌ No           | LOW                     | N, NE, NNE                                                     |
| Time zone                | ✅ `timeZone()`                                        | ❌ No           | MEDIUM                  | America/New_York                                               |
| Full address             | ✅ `fullAddress()`                                     | ✅ Yes          | ✓ DONE                  | `StreetAddressGenerator.generateFullAddress()`                 |

### 3. INTERNET & NETWORKING

| Feature           | DataFaker Support                    | krandom Status | Implementation Priority | Notes                                                 |
|-------------------|--------------------------------------|----------------|-------------------------|-------------------------------------------------------|
| **Email**         |
| Email address     | ✅ `emailAddress()`                   | ✅ Yes          | ✓ DONE                  | `EmailGenerator` — 5 formats, 12 domains, 10 locales  |
| Safe email        | ✅ `safeEmailAddress()` (example.com) | ✅ Yes          | ✓ DONE                  | `gen.generate("example.com")` — custom domain support |
| Email subject     | ✅ `emailSubject()`                   | ❌ No           | LOW                     |                                                       |
| **Domain & URLs** |
| Domain name       | ✅ `domainName()`                     | ✅ Yes          | ✓ DONE                  | `DomainGenerator` — 12 popular TLDs, 10 locale TLDs   |
| Domain word       | ✅ `domainWord()`                     | ✅ Partial      | ✓ DONE                  | Embedded in `DomainGenerator` (1-2 word combos)       |
| Domain suffix     | ✅ `domainSuffix()` (.com, .org)      | ✅ Yes          | ✓ DONE                  | `DomainGenerator.getTLD()` → "com", "io", "de"        |
| URL               | ✅ `url()`                            | ✅ Yes          | ✓ DONE                  | `URLGenerator` — 5 protocols, path, query params      |
| Web domain        | ✅ `webdomain()`                      | ✅ Partial      | ✓ DONE                  | Covered by `DomainGenerator`                          |
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
| HTTP method       | ✅ `httpMethod()` (GET, POST)         | ❌ No           | LOW                     | REST APIs                                             |
| **Identifiers**   |
| UUID v3           | ✅ `uuidv3()`                         | ❌ No           | LOW                     | Not implemented (v5 SHA-1 is similar)                 |
| UUID v4           | ✅ `uuid()`, `uuidv4()`               | ✅ Yes          | ✓ DONE                  | `UUIDGenerator.generateV4()` — RFC 4122 §4.4          |
| UUID v7           | ✅ `uuidv7()`                         | ✅ Yes          | ✓ DONE                  | `UUIDGenerator.generateV7()`                          |
| **User Agents**   |
| User agent        | ✅ `userAgent()`                      | ✅ Yes          | ✓ DONE                  | `UserAgentGenerator.generate()`                       |
| Bot user agent    | ✅ `botUserAgent()`                   | ✅ Yes          | ✓ DONE                  | `UserAgentGenerator.generateBot()`                    |
| **Other**         |
| Image URL         | ✅ `image()`                          | ❌ No           | LOW                     | Placeholder images                                    |

### 4. FINANCE & COMMERCE

| Feature               | DataFaker Support                                                                             | krandom Status | Implementation Priority | Notes                                                        |
|-----------------------|-----------------------------------------------------------------------------------------------|----------------|-------------------------|--------------------------------------------------------------|
| **Credit Cards**      |
| Credit card number    | ✅ 10 types, Luhn-valid                                                                        | ✅ Yes          | ✓ DONE                  | `CreditCardGenerator` — Luhn-valid, 6 card types             |
| Card types            | ✅ VISA, MASTERCARD, DISCOVER, AMEX, DINERS, JCB, DANKORT, FORBRUGSFORENINGEN, LASER, UNIONPAY | ✅ Yes          | ✓ DONE                  | Visa/MC/Amex/Discover/JCB/Diners supported                   |
| Card expiry           | ✅ `creditCardExpiry()`                                                                        | ✅ Yes          | ✓ DONE                  | `CardExpirationGenerator` — MM/YY, future-only, locale-aware |
| Security code         | ✅ `securityCode()` (CVV)                                                                      | ✅ Yes          | ✓ DONE                  | `CreditCardGenerator.getCvv()` — 3 or 4 digits               |
| **Banking**           |
| BIC/SWIFT             | ✅ `bic()`                                                                                     | ❌ No           | MEDIUM                  | Bank identifier                                              |
| IBAN                  | ✅ `iban()`                                                                                    | ❌ No           | MEDIUM                  | International account                                        |
| US routing number     | ✅ `usRoutingNumber()`                                                                         | ❌ No           | MEDIUM                  | ACH routing                                                  |
| **Money & Currency**  |
| Currency name         | ✅ `currency()`                                                                                | ✅ Yes          | ✓ DONE                  |                                                              |
| Currency code         | ✅ `currencyCode()` (USD, EUR)                                                                 | ✅ Yes          | ✓ DONE                  |                                                              |
| Currency symbol       | ✅ `currencySymbol()` ($, €)                                                                   | ✅ Yes          | ✓ DONE                  | `CurrencyGenerator.getSymbol()` / `getSymbol(locale)`        |
| Currency numeric code | ✅ `currencyNumericCode()` (840)                                                               | ✅ Yes          | ✓ DONE                  | `CurrencyGenerator.getNumericCode()` — ISO 4217              |
| Price                 | ✅ `price()`                                                                                   | ✅ Yes          | ✓ DONE                  | `MoneyGenerator` — locale-aware, dollar/euro helpers         |
| **Stock Market**      |
| NASDAQ symbol         | ✅ `nsdqSymbol()`                                                                              | ❌ No           | LOW                     | Stock tickers                                                |
| NYSE symbol           | ✅ `nyseSymbol()`                                                                              | ❌ No           | LOW                     |                                                              |
| NSE symbol            | ✅ `nseSymbol()`                                                                               | ❌ No           | LOW                     | India                                                        |
| LSE symbol            | ✅ `lseSymbol()`                                                                               | ❌ No           | LOW                     | London                                                       |
| Exchange names        | ✅ `exchanges()`                                                                               | ❌ No           | LOW                     |                                                              |
| **Commerce**          |
| Department            | ✅ `department()`                                                                              | ❌ No           | LOW                     | Store departments                                            |
| Product name          | ✅ `productName()`                                                                             | ❌ No           | MEDIUM                  | E-commerce                                                   |
| Material              | ✅ `material()`                                                                                | ❌ No           | LOW                     | Product materials                                            |
| Brand                 | ✅ `brand()`, `sport()`, `car()`, `watch()`                                                    | ❌ No           | MEDIUM                  | Brand names                                                  |
| Vendor                | ✅ `vendor()`                                                                                  | ❌ No           | LOW                     | Suppliers                                                    |
| Promotion code        | ✅ `promotionCode()`                                                                           | ❌ No           | LOW                     | Discount codes                                               |
| **Subscriptions**     |
| Plans                 | ✅ `plans()`                                                                                   | ❌ No           | LOW                     | Free, Premium, etc.                                          |
| Statuses              | ✅ `statuses()`                                                                                | ❌ No           | LOW                     | Active, Cancelled                                            |
| Payment methods       | ✅ `paymentMethods()`                                                                          | ❌ No           | LOW                     | Card, PayPal, etc.                                           |
| Payment terms         | ✅ `paymentTerms()`                                                                            | ❌ No           | LOW                     | Net 30, etc.                                                 |

### 5. COMPANY & BUSINESS

| Feature        | DataFaker Support            | krandom Status | Implementation Priority | Notes                                 |
|----------------|------------------------------|----------------|-------------------------|---------------------------------------|
| Company name   | ✅ `name()`                   | ✅ Yes          | ✓ DONE                  | `CompanyNameGenerator.generate()`     |
| Company suffix | ✅ `suffix()` (Inc, LLC, Ltd) | ✅ Yes          | ✓ DONE                  | `CompanyNameGenerator.generate(true)` |
| Industry       | ✅ `industry()`               | ✅ Yes          | ✓ DONE                  | `IndustryGenerator.generate()`        |
| Profession     | ✅ `profession()`             | ✅ Yes          | ✓ DONE                  | `ProfessionGenerator.generate()`      |
| Buzzword       | ✅ `buzzword()`               | ❌ No           | LOW                     | Marketing speak                       |
| Catch phrase   | ✅ `catchPhrase()`            | ❌ No           | LOW                     | Company slogans                       |
| BS phrase      | ✅ `bs()`                     | ❌ No           | LOW                     | Corporate BS                          |
| Logo URL       | ✅ `logo()`                   | ❌ No           | LOW                     | Company logos                         |
| Company URL    | ✅ `url()`                    | ✅ Yes          | ✓ DONE                  | `CompanyUrlGenerator.generate()`      |

### 6. JOB & CAREER

| Feature    | DataFaker Support | krandom Status | Implementation Priority | Notes                           |
|------------|-------------------|----------------|-------------------------|---------------------------------|
| Job field  | ✅ `field()`       | ✅ Yes          | ✓ DONE                  | `JobFieldGenerator.generate()`  |
| Seniority  | ✅ `seniority()`   | ✅ Yes          | ✓ DONE                  | `SeniorityGenerator.generate()` |
| Position   | ✅ `position()`    | ✅ Yes          | ✓ DONE                  | `PositionGenerator.generate()`  |
| Job title  | ✅ `title()`       | ✅ Yes          | ✓ DONE                  | Combined title                  |
| Key skills | ✅ `keySkills()`   | ❌ No           | LOW                     | Job requirements                |

### 7. TEXT & LOREM

| Feature              | DataFaker Support                                     | krandom Status | Implementation Priority | Notes               |
|----------------------|-------------------------------------------------------|----------------|-------------------------|---------------------|
| **Lorem Ipsum**      |
| Character            | ✅ `character()`                                       | ❌ No           | MEDIUM                  | Single char         |
| Characters           | ✅ `characters(n)`                                     | ❌ No           | MEDIUM                  | N characters        |
| Word                 | ✅ `word()`                                            | ❌ No           | HIGH                    | Single word         |
| Words                | ✅ `words(n)`                                          | ❌ No           | HIGH                    | Multiple words      |
| Sentence             | ✅ `sentence()`                                        | ❌ No           | HIGH                    | Full sentence       |
| Sentences            | ✅ `sentences(n)`                                      | ❌ No           | HIGH                    | Multiple sentences  |
| Paragraph            | ✅ `paragraph()`                                       | ❌ No           | HIGH                    | Full paragraph      |
| Paragraphs           | ✅ `paragraphs(n)`                                     | ❌ No           | HIGH                    | Multiple paragraphs |
| Fixed string         | ✅ `fixedString(n)`                                    | ❌ No           | MEDIUM                  | Exact length        |
| Max length sentence  | ✅ `maxLengthSentence(n)`                              | ❌ No           | MEDIUM                  | Length-limited      |
| **Specialized Text** |
| Hacker speak         | ✅ `abbreviation()`, `adjective()`, `noun()`, `verb()` | ❌ No           | LOW                     | Tech jargon         |
| Hipster words        | ✅ `word()`                                            | ❌ No           | LOW                     | Trendy vocabulary   |
| Shakespeare quotes   | ✅ Multiple plays                                      | ❌ No           | LOW                     | Literary quotes     |
| Yoda quotes          | ✅ `quote()`                                           | ❌ No           | LOW                     | Star Wars           |
| Chuck Norris facts   | ✅ Available                                           | ❌ No           | LOW                     | Jokes               |

### 8. DATE & TIME

| Feature      | DataFaker Support                     | krandom Status | Implementation Priority | Notes                                                        |
|--------------|---------------------------------------|----------------|-------------------------|--------------------------------------------------------------|
| Future date  | ✅ `future()`                          | ✅ Yes          | ✓ DONE                  | `DateGenerator.future()` / `future(int)`                     |
| Past date    | ✅ `past()`                            | ✅ Yes          | ✓ DONE                  | `DateGenerator.past()` / `past(int)`                         |
| Date between | ✅ `between()`                         | ✅ Yes          | ✓ DONE                  | `DateGenerator.between(LocalDate, LocalDate)`                |
| Birthday     | ✅ `birthday()`, `birthdayLocalDate()` | ✅ Yes          | ✓ DONE                  | `BirthdayGenerator` — type-based, locale-aware string format |
| Duration     | ✅ `duration()`                        | ❌ No           | MEDIUM                  | Time spans                                                   |
| Period       | ✅ `period()`                          | ❌ No           | MEDIUM                  | Date periods                                                 |

### 9. PHONE NUMBERS

| Feature              | DataFaker Support              | krandom Status | Implementation Priority | Notes                                                      |
|----------------------|--------------------------------|----------------|-------------------------|------------------------------------------------------------|
| Phone number         | ✅ `phoneNumber()`              | ✅ Yes          | ✓ DONE                  | `PhoneNumberGenerator` — 10 locales, formatted/unformatted |
| National format      | ✅ `phoneNumberNational()`      | ✅ Yes          | ✓ DONE                  | `generate(true)` → "(555) 123-4567", "020 7946 0958"       |
| International format | ✅ `phoneNumberInternational()` | ❌ No           | LOW                     | No +country prefix implemented yet                         |
| Cell phone           | ✅ `cellPhone()`                | ✅ Yes          | ✓ DONE                  | `generate(true, true)` → mobile numbers per locale         |
| Cell international   | ✅ `cellPhoneInternational()`   | ❌ No           | MEDIUM                  |                                                            |
| Extension            | ✅ `extension()`                | ❌ No           | LOW                     | x1234                                                      |
| Subscriber number    | ✅ `subscriberNumber()`         | ❌ No           | LOW                     |                                                            |

### 10. NUMBERS & CODES

| Feature           | DataFaker Support                                | krandom Status | Implementation Priority | Notes          |
|-------------------|--------------------------------------------------|----------------|-------------------------|----------------|
| **Basic Numbers** |
| Random digit      | ✅ `randomDigit()`                                | ✅ Yes          | ✓ DONE                  | 0-9            |
| Digit not zero    | ✅ `randomDigitNotZero()`                         | ❌ No           | LOW                     | 1-9            |
| Positive number   | ✅ `positive()`                                   | ❌ No           | MEDIUM                  | > 0            |
| Negative number   | ✅ `negative()`                                   | ❌ No           | MEDIUM                  | < 0            |
| Number between    | ✅ `numberBetween()`                              | ✅ Yes          | ✓ DONE                  | Range          |
| Random double     | ✅ `randomDouble()`                               | ✅ Yes          | ✓ DONE                  |                |
| **Book Codes**    |
| ISBN-10           | ✅ `isbn10()`                                     | ❌ No           | MEDIUM                  | With checksum  |
| ISBN-13           | ✅ `isbn13()`                                     | ❌ No           | MEDIUM                  | With checksum  |
| ISBN components   | ✅ `isbnGs1()`, `isbnGroup()`, `isbnRegistrant()` | ❌ No           | LOW                     | Parts          |
| **Product Codes** |
| ASIN              | ✅ `asin()`                                       | ❌ No           | LOW                     | Amazon IDs     |
| IMEI              | ✅ `imei()`                                       | ❌ No           | LOW                     | Mobile devices |
| EAN-8/13          | ✅ `ean8()`, `ean13()`                            | ❌ No           | MEDIUM                  | Barcodes       |
| GTIN              | ✅ `gtin8/12/13/14()`                             | ❌ No           | MEDIUM                  | Product IDs    |
| Barcode type      | ✅ `type()`                                       | ❌ No           | LOW                     |                |
| **Hashing**       |
| MD2/MD5           | ✅ `md2()`, `md5()`                               | ❌ No           | LOW                     | Crypto hashes  |
| SHA family        | ✅ `sha1/256/384/512()`                           | ❌ No           | LOW                     |                |

### 11. COLOR

| Feature    | DataFaker Support | krandom Status | Implementation Priority | Notes                                           |
|------------|-------------------|----------------|-------------------------|-------------------------------------------------|
| Color name | ✅ `name()`        | ❌ No           | MEDIUM                  | "Red", "Blue"                                   |
| Hex color  | ✅ `hex()`         | ✅ Yes          | ✓ DONE                  | `ColorGenerator` — HEX/SHORT_HEX/RGB/0x formats |

### 12. ANIMALS

| Feature             | DataFaker Support                               | krandom Status | Implementation Priority | Notes          |
|---------------------|-------------------------------------------------|----------------|-------------------------|----------------|
| **General Animals** |
| Animal name         | ✅ `name()`                                      | ❌ No           | LOW                     | Common animals |
| Scientific name     | ✅ `scientificName()`                            | ❌ No           | LOW                     | Latin names    |
| Genus/species       | ✅ `genus()`, `species()`                        | ❌ No           | LOW                     | Taxonomy       |
| **Cats**            |
| Cat name            | ✅ `name()`                                      | ❌ No           | LOW                     |                |
| Cat breed           | ✅ `breed()`                                     | ❌ No           | LOW                     |                |
| Cat registry        | ✅ `registry()`                                  | ❌ No           | LOW                     |                |
| **Dogs**            |
| Dog name            | ✅ `name()`                                      | ❌ No           | LOW                     |                |
| Dog breed           | ✅ `breed()`                                     | ❌ No           | LOW                     |                |
| Dog sound           | ✅ `sound()`                                     | ❌ No           | LOW                     | Bark, woof     |
| Dog meme phrase     | ✅ `memePhrase()`                                | ❌ No           | LOW                     | Internet dogs  |
| Dog metadata        | ✅ `age()`, `coatLength()`, `gender()`, `size()` | ❌ No           | LOW                     |                |
| **Horses**          |
| Horse name          | ✅ `name()`                                      | ❌ No           | LOW                     |                |
| Horse breed         | ✅ `breed()`                                     | ❌ No           | LOW                     |                |

### 13. SCIENCE & EDUCATION

| Feature               | DataFaker Support                              | krandom Status | Implementation Priority | Notes             |
|-----------------------|------------------------------------------------|----------------|-------------------------|-------------------|
| **Science**           |
| Chemical element      | ✅ `element()`, `elementSymbol()`               | ❌ No           | LOW                     | H, He, Li         |
| Scientific unit       | ✅ `unit()`                                     | ❌ No           | LOW                     | Meters, kg        |
| Scientist name        | ✅ `scientist()`                                | ❌ No           | LOW                     | Famous scientists |
| Science tool          | ✅ `tool()`                                     | ❌ No           | LOW                     | Microscope, etc.  |
| Particles             | ✅ `quark()`, `leptons()`, `bosons()`           | ❌ No           | LOW                     | Physics           |
| **Education**         |
| University            | ✅ `university()`, `name()`                     | ❌ No           | MEDIUM                  | School names      |
| Course                | ✅ `course()`, `subjectWithNumber()`            | ❌ No           | LOW                     | CS 101            |
| Degree                | ✅ `degree()`                                   | ❌ No           | MEDIUM                  | BS, MS, PhD       |
| Secondary school      | ✅ `secondarySchool()`                          | ❌ No           | LOW                     | High schools      |
| Campus                | ✅ `campus()`                                   | ❌ No           | LOW                     |                   |
| **Ancient/Mythology** |
| Greek gods            | ✅ `god()`, `primordial()`, `titan()`, `hero()` | ❌ No           | LOW                     | Mythology         |

### 14. MUSIC & ARTS

| Feature     | DataFaker Support | krandom Status | Implementation Priority | Notes            |
|-------------|-------------------|----------------|-------------------------|------------------|
| **Music**   |
| Instrument  | ✅ `instrument()`  | ❌ No           | LOW                     | Guitar, Piano    |
| Musical key | ✅ `key()`         | ❌ No           | LOW                     | C major, A minor |
| Chord       | ✅ `chord()`       | ❌ No           | LOW                     | C7, Am           |
| Genre       | ✅ `genre()`       | ❌ No           | LOW                     | Rock, Jazz       |
| **Books**   |
| Book author | ✅ `author()`      | ❌ No           | LOW                     | Famous authors   |
| Book title  | ✅ `title()`       | ❌ No           | LOW                     |                  |
| Publisher   | ✅ `publisher()`   | ❌ No           | LOW                     |                  |
| Book genre  | ✅ `genre()`       | ❌ No           | LOW                     | Fiction, etc.    |

### 15. TRANSPORT & TRAVEL

| Feature        | DataFaker Support                                     | krandom Status | Implementation Priority | Notes             |
|----------------|-------------------------------------------------------|----------------|-------------------------|-------------------|
| **Vehicles**   |
| VIN            | ✅ `vin()`                                             | ❌ No           | MEDIUM                  | Vehicle ID number |
| Manufacturer   | ✅ `manufacturer()`                                    | ❌ No           | MEDIUM                  | Ford, Toyota      |
| Make           | ✅ `make()`                                            | ❌ No           | MEDIUM                  |                   |
| Model          | ✅ `model()`                                           | ❌ No           | MEDIUM                  | Civic, Camry      |
| Make and model | ✅ `makeAndModel()`                                    | ❌ No           | MEDIUM                  | Combined          |
| Style          | ✅ `style()`                                           | ❌ No           | LOW                     | Sedan, SUV        |
| Color          | ✅ `color()`                                           | ❌ No           | MEDIUM                  | Car colors        |
| Upholstery     | ✅ `upholsteryColor()`, `upholsteryFabric()`           | ❌ No           | LOW                     | Interior          |
| Transmission   | ✅ `transmission()`                                    | ❌ No           | LOW                     | Manual, Auto      |
| Drive type     | ✅ `driveType()`                                       | ❌ No           | LOW                     | FWD, RWD, AWD     |
| Fuel type      | ✅ `fuelType()`                                        | ❌ No           | LOW                     | Gas, Diesel       |
| Car type       | ✅ `carType()`                                         | ❌ No           | LOW                     | Passenger, etc.   |
| Engine         | ✅ `engine()`                                          | ❌ No           | LOW                     | V6, I4            |
| Options        | ✅ `carOptions()`                                      | ❌ No           | LOW                     | Features          |
| Specs          | ✅ `standardSpecs()`                                   | ❌ No           | LOW                     |                   |
| Doors          | ✅ `doors()`                                           | ❌ No           | LOW                     | 2, 4              |
| License plate  | ✅ `licensePlate()`                                    | ❌ No           | MEDIUM                  | Plate numbers     |
| **Aviation**   |
| Aircraft       | ✅ `aircraft()`, `airplane()`, `warplane()`, `cargo()` | ❌ No           | LOW                     | Plane types       |
| Helicopter     | ✅ `armyHelicopter()`, `civilHelicopter()`             | ❌ No           | LOW                     |                   |
| Airport        | ✅ `airport()`, `airportName()`                        | ❌ No           | MEDIUM                  | Airports          |
| METAR          | ✅ `METAR()`                                           | ❌ No           | LOW                     | Weather report    |
| Flight         | ✅ `flight()`, `flightStatus()`, `gate()`              | ❌ No           | LOW                     | Flight info       |
| Airline        | ✅ `airline()`                                         | ❌ No           | MEDIUM                  | Carriers          |

### 16. HEALTHCARE

| Feature                   | DataFaker Support                          | krandom Status | Implementation Priority | Notes            |
|---------------------------|--------------------------------------------|----------------|-------------------------|------------------|
| **Diseases**              |
| ICD-10 code               | ✅ `icd10()`                                | ❌ No           | LOW                     | Medical codes    |
| Disease name              | ✅ `anyDisease()`                           | ❌ No           | LOW                     |                  |
| Specialty diseases        | ✅ `internalDisease()`, `neurology()`, etc. | ❌ No           | LOW                     | By medical field |
| **Medical**               |
| Medicine name             | ✅ Available                                | ❌ No           | LOW                     | Drug names       |
| Hospital name             | ✅ Available                                | ❌ No           | LOW                     |                  |
| Symptoms                  | ✅ Available                                | ❌ No           | LOW                     |                  |
| Medical procedure         | ✅ Available                                | ❌ No           | LOW                     |                  |
| Diagnosis/procedure codes | ✅ Available                                | ❌ No           | LOW                     |                  |
| Medical profession        | ✅ Available                                | ❌ No           | LOW                     |                  |

### 17. MILITARY

| Feature        | DataFaker Support  | krandom Status | Implementation Priority | Notes          |
|----------------|--------------------|----------------|-------------------------|----------------|
| Army rank      | ✅ `armyRank()`     | ❌ No           | LOW                     | Military ranks |
| Marines rank   | ✅ `marinesRank()`  | ❌ No           | LOW                     |                |
| Navy rank      | ✅ `navyRank()`     | ❌ No           | LOW                     |                |
| Air Force rank | ✅ `airForceRank()` | ❌ No           | LOW                     |                |
| DoD paygrade   | ✅ `dodPaygrade()`  | ❌ No           | LOW                     | E-1, O-5, etc. |

### 18. SPORTS (70+ providers)

| Feature      | DataFaker Support                       | krandom Status | Implementation Priority | Notes          |
|--------------|-----------------------------------------|----------------|-------------------------|----------------|
| Formula 1    | ✅ Drivers, teams, circuits, grands prix | ❌ No           | LOW                     | Racing data    |
| Basketball   | ✅ Teams, coaches, positions, players    | ❌ No           | LOW                     | NBA            |
| Baseball     | ✅ Available                             | ❌ No           | LOW                     | MLB            |
| Football     | ✅ Available                             | ❌ No           | LOW                     | NFL            |
| Soccer       | ✅ England Football                      | ❌ No           | LOW                     | Premier League |
| Cricket      | ✅ Available                             | ❌ No           | LOW                     |                |
| Chess        | ✅ Available                             | ❌ No           | LOW                     |                |
| Martial arts | ✅ Available                             | ❌ No           | LOW                     |                |
| Volleyball   | ✅ Available                             | ❌ No           | LOW                     |                |
| (60+ more)   | ✅ Comprehensive                         | ❌ No           | LOW                     | Very extensive |

### 19. FOOD & DRINK

| Feature         | DataFaker Support                      | krandom Status | Implementation Priority | Notes               |
|-----------------|----------------------------------------|----------------|-------------------------|---------------------|
| **Food**        |
| Ingredient      | ✅ `ingredient()`                       | ❌ No           | LOW                     | Cooking ingredients |
| Allergen        | ✅ `allergen()`                         | ❌ No           | LOW                     | Peanuts, dairy      |
| Spice           | ✅ `spice()`                            | ❌ No           | LOW                     | Seasoning           |
| Dish            | ✅ `dish()`                             | ❌ No           | LOW                     | Food names          |
| Fruit           | ✅ `fruit()`                            | ❌ No           | LOW                     |                     |
| Vegetable       | ✅ `vegetable()`                        | ❌ No           | LOW                     |                     |
| Sushi           | ✅ `sushi()`                            | ❌ No           | LOW                     | Japanese food       |
| Measurement     | ✅ `measurement()`                      | ❌ No           | LOW                     | Cup, tbsp           |
| **Drinks**      |
| Beer            | ✅ Brand, name, style, hop, yeast, malt | ❌ No           | LOW                     | Beer details        |
| Coffee          | ✅ Available                            | ❌ No           | LOW                     |                     |
| Tea             | ✅ Available                            | ❌ No           | LOW                     |                     |
| **Desserts**    |
| Dessert variety | ✅ `variety()`                          | ❌ No           | LOW                     | Cake, pie           |
| Topping         | ✅ `topping()`                          | ❌ No           | LOW                     | Frosting, etc.      |
| Flavor          | ✅ `flavor()`                           | ❌ No           | LOW                     | Vanilla, chocolate  |
| Ice cream       | ✅ Available                            | ❌ No           | LOW                     |                     |

### 20. HOME & ENVIRONMENT

| Feature         | DataFaker Support           | krandom Status | Implementation Priority | Notes            |
|-----------------|-----------------------------|----------------|-------------------------|------------------|
| **House**       |
| Furniture       | ✅ `furniture()`             | ❌ No           | LOW                     | Chair, table     |
| Room            | ✅ `room()`                  | ❌ No           | LOW                     | Kitchen, bedroom |
| **Weather**     |
| Description     | ✅ `description()`           | ❌ No           | LOW                     | Sunny, rainy     |
| Temperature (C) | ✅ `temperatureCelsius()`    | ❌ No           | LOW                     | Metric           |
| Temperature (F) | ✅ `temperatureFahrenheit()` | ❌ No           | LOW                     | Imperial         |

### 21. TECHNOLOGY

| Feature         | DataFaker Support  | krandom Status | Implementation Priority | Notes             |
|-----------------|--------------------|----------------|-------------------------|-------------------|
| **Device**      |
| Model name      | ✅ `modelName()`    | ❌ No           | LOW                     | iPhone 12         |
| Platform        | ✅ `platform()`     | ❌ No           | LOW                     | iOS, Android      |
| Manufacturer    | ✅ `manufacturer()` | ❌ No           | LOW                     | Apple, Samsung    |
| Serial number   | ✅ `serial()`       | ❌ No           | LOW                     | Device serials    |
| **App**         |
| App name        | ✅ `name()`         | ❌ No           | LOW                     | Application names |
| Version         | ✅ `version()`      | ❌ No           | LOW                     | 1.2.3             |
| Author          | ✅ `author()`       | ❌ No           | LOW                     | Developer         |
| **Programming** |
| Language name   | ✅ `name()`         | ❌ No           | LOW                     | Java, Python      |
| Creator         | ✅ `creator()`      | ❌ No           | LOW                     | Language authors  |

### 22. ENTERTAINMENT - TV & FILM (40+ franchises)

| Feature           | DataFaker Support                                              | krandom Status | Implementation Priority | Notes         |
|-------------------|----------------------------------------------------------------|----------------|-------------------------|---------------|
| Harry Potter      | ✅ Characters, locations, quotes, books, houses, spells         | ❌ No           | LOW                     | Complete data |
| Star Trek         | ✅ Characters, locations, species, villains, Klingon, starships | ❌ No           | LOW                     |               |
| Star Wars         | ✅ Available                                                    | ❌ No           | LOW                     |               |
| Game of Thrones   | ✅ Characters, houses, cities, dragons, quotes                  | ❌ No           | LOW                     |               |
| Lord of the Rings | ✅ Available                                                    | ❌ No           | LOW                     |               |
| (35+ more shows)  | ✅ Friends, Rick and Morty, Breaking Bad, etc.                  | ❌ No           | LOW                     | Pop culture   |

### 23. ENTERTAINMENT - ANIME & MANGA

| Feature     | DataFaker Support            | krandom Status | Implementation Priority | Notes            |
|-------------|------------------------------|----------------|-------------------------|------------------|
| Pokemon     | ✅ Name, location, move, type | ❌ No           | LOW                     | Complete Pokemon |
| Dragon Ball | ✅ Available                  | ❌ No           | LOW                     |                  |
| Naruto      | ✅ Available                  | ❌ No           | LOW                     |                  |
| One Piece   | ✅ Available                  | ❌ No           | LOW                     |                  |
| (5+ more)   | ✅ Various anime              | ❌ No           | LOW                     |                  |

### 24. ENTERTAINMENT - VIDEO GAMES (30+ games)

| Feature           | DataFaker Support                           | krandom Status | Implementation Priority | Notes            |
|-------------------|---------------------------------------------|----------------|-------------------------|------------------|
| Minecraft         | ✅ Items, tiles, entities, monsters, animals | ❌ No           | LOW                     | Complete MC data |
| Zelda             | ✅ Games, characters                         | ❌ No           | LOW                     |                  |
| League of Legends | ✅ Available                                 | ❌ No           | LOW                     |                  |
| Overwatch         | ✅ Available                                 | ❌ No           | LOW                     |                  |
| (25+ more games)  | ✅ Various franchises                        | ❌ No           | LOW                     | Gaming data      |

### 25. MISCELLANEOUS

| Feature   | DataFaker Support                                       | krandom Status | Implementation Priority | Notes          |
|-----------|---------------------------------------------------------|----------------|-------------------------|----------------|
| Boolean   | ✅ `bool()`                                              | ❌ No           | MEDIUM                  | true/false     |
| Emoji     | ✅ Smiley, cat, vehicle                                  | ❌ No           | LOW                     | Unicode emoji  |
| Space     | ✅ Planets, moons, galaxies, stars, agencies, spacecraft | ❌ No           | LOW                     | Astronomy      |
| Superhero | ✅ Name, prefix, suffix, power, descriptor               | ❌ No           | LOW                     | Comic data     |
| Team      | ✅ Name, creature, state, sport                          | ❌ No           | LOW                     | Team generator |

---

## ADVANCED FEATURES

### Configuration & Customization

| Feature                  | DataFaker                          | krandom | Priority | Implementation Notes                                                   |
|--------------------------|------------------------------------|---------|----------|------------------------------------------------------------------------|
| **Locale Support**       |
| Multiple locales         | ✅ 60+ locales                      | ✅ Yes   | ✓ DONE   | 10 built-in locales (en_US/GB/AU, de/fr/es/it, pt_BR, ja, zh_CN)       |
| Locale-aware data        | ✅ Names, addresses, phones         | ✅ Yes   | ✓ DONE   | Names, cities, states, postcodes, phones, coordinates all locale-aware |
| Runtime locale switching | ✅ Yes                              | ✅ Yes   | ✓ DONE   | Pass different `Locale` to constructor per call                        |
| **Seeding**              |
| Reproducible output      | ✅ Constructor with seed            | ✅ Yes   | ✓ DONE   | Most generators support                                                |
| **String Utilities**     |
| Numerify                 | ✅ `numerify("###-####")`           | ✅ Yes   | ✓ DONE   | `TemplateStringGenerator.numerify(...)`                                |
| Letterify                | ✅ `letterify("???-???")`           | ✅ Yes   | ✓ DONE   | `TemplateStringGenerator.letterify(...)`                               |
| Bothify                  | ✅ `bothify("???-###")`             | ✅ Yes   | ✓ DONE   | `TemplateStringGenerator.bothify(...)`                                 |
| Regexify                 | ✅ `regexify("[A-Z]{3}\\d{4}")`     | ❌ No    | HIGH     | Regex-based generation                                                 |
| Examplify                | ✅ `examplify("ABC-1234")`          | ❌ No    | MEDIUM   | Match pattern                                                          |
| Templatify               | ✅ Custom templates                 | ❌ No    | MEDIUM   |                                                                        |
| **Data Sources**         |
| Custom YAML              | ✅ `addPath()`, `addUrl()`          | ❌ No    | MEDIUM   | Extensibility                                                          |
| YAML key resolution      | ✅ `resolve(key)`                   | ❌ No    | LOW      |                                                                        |
| **Collections**          |
| Generate lists           | ✅ `collection().len(n).generate()` | ✅ Yes   | ✓ DONE   | `gen.generateList(n)` on every generator                               |
| Variable length          | ✅ `minLen()`, `maxLen()`           | ✅ Yes   | ✓ DONE   | `generateList(n)` with any n                                           |
| Nullable values          | ✅ `nullRate(0.1)`                  | ❌ No    | MEDIUM   | Realistic nulls                                                        |
| Stream API               | ✅ `stream().limit(n)`              | ✅ Yes   | ✓ DONE   | `gen.stream().limit(n)` on every generator                             |
| **Unique Values**        |
| Unique enforcement       | ✅ `faker.unique()`                 | ✅ Yes   | ✓ DONE   | `Generators.unique(...)` and `Generators.uniqueValues(...)`            |
| **Output Formats**       |
| CSV generation           | ✅ Schema-based                     | ❌ No    | MEDIUM   | Structured output                                                      |
| JSON generation          | ✅ Schema-based                     | ❌ No    | MEDIUM   |                                                                        |
| YAML generation          | ✅ Schema-based                     | ❌ No    | LOW      |                                                                        |
| XML generation           | ✅ Schema-based                     | ❌ No    | LOW      |                                                                        |
| **Expressions**          |
| YAML expressions         | ✅ `#{Provider.method}`             | ❌ No    | MEDIUM   | Composable generators                                                  |
| **Custom Providers**     |
| Extend with custom       | ✅ `AbstractProvider<T>`            | ❌ No    | LOW      | Plugin system                                                          |
| **Object Population**    |
| POJO population          | ✅ `@Fake` annotation               | ❌ No    | LOW      | Auto-fill objects                                                      |

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
    - `Generators.unique(...)` and `Generators.uniqueValues(...)`

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
2. **60+ locales** vs 10 in krandom — DataFaker has broader locale coverage
3. **Schema-based output** (CSV/JSON/YAML/XML) — no equivalent in krandom
4. **Template-based generation breadth** (`numerify`, `letterify`, `bothify`, `regexify`) — partial in krandom (`numerify/letterify/bothify` implemented)
5. **Unique value enforcement ergonomics** (`faker.unique()` fluent provider chaining) — partial in krandom (`Generators.unique(...)`)
6. **Expression language** for composable generators (`#{Name.firstName}`)
7. **GraalVM native image** support
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
