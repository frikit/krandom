# DataFaker Feature Parity Analysis

## Library Overview

- **Name**: DataFaker
- **Language**: Java
- **Version Analyzed**: Latest (2024+)
- **GitHub**: https://github.com/datafaker-net/datafaker
- **License**: Apache 2.0
- **Key Strength**: 200+ providers, extensive localization, schema-based output

## Executive Summary

DataFaker is the most feature-rich Java faker library with 200+ generator providers, 60+ locales, and advanced features like schema-based output formats (CSV/JSON/YAML/XML), unique value enforcement,
and GraalVM native image support. It's a fork of JavaFaker with significant enhancements.

---

## Feature Categories

### 1. PERSONAL IDENTITY

| Feature                     | DataFaker Support                            | krandom Status | Implementation Priority | Notes                                     |
|-----------------------------|----------------------------------------------|----------------|-------------------------|-------------------------------------------|
| **Name Generation**         |
| Full name                   | ✅ `name()`, `fullName()`, `nameWithMiddle()` | ✅ Partial      | HIGH                    | krandom has basic name, needs middle name |
| First name                  | ✅ `firstName()`                              | ✅ Yes          | ✓ DONE                  |                                           |
| Last name                   | ✅ `lastName()`                               | ✅ Yes          | ✓ DONE                  |                                           |
| Gender-specific first names | ✅ `femaleFirstName()`, `maleFirstName()`     | ❌ No           | HIGH                    | Important for realistic data              |
| Name prefix                 | ✅ `prefix()` (Mr., Mrs., Dr.)                | ❌ No           | MEDIUM                  | Common requirement                        |
| Name suffix                 | ✅ `suffix()` (Jr., Sr., III)                 | ❌ No           | MEDIUM                  |                                           |
| Title                       | ✅ `title()` (professional titles)            | ❌ No           | MEDIUM                  | Job titles available via TitleGenerator   |
| **ID Numbers**              |
| SSN (US)                    | ✅ `ssnValid()`                               | ❌ No           | HIGH                    | Luhn-valid, country-specific              |
| Singapore FIN/UIN           | ✅ `singaporeanFin()`, `singaporeanUin()`     | ❌ No           | LOW                     | Locale-specific                           |
| Poland PESEL                | ✅ `peselNumber()`                            | ❌ No           | LOW                     | Locale-specific                           |
| China SSN                   | ✅ `validZhCNSsn()`                           | ❌ No           | LOW                     | Locale-specific                           |
| Portugal NIF                | ✅ `validPtNif()`                             | ❌ No           | LOW                     | Locale-specific                           |
| Mexico SSN                  | ✅ `validEsMXSsn()`                           | ❌ No           | LOW                     | Locale-specific                           |
| South Africa SSN            | ✅ `validEnZaSsn()`                           | ❌ No           | LOW                     | Locale-specific                           |
| **Gender & Demographics**   |
| Gender types                | ✅ `types()`, `binaryTypes()`                 | ❌ No           | MEDIUM                  | Useful for forms                          |
| Race                        | ✅ `race()`                                   | ❌ No           | LOW                     | Sensitive data                            |
| Education level             | ✅ `educationalAttainment()`                  | ❌ No           | MEDIUM                  | Useful for profiles                       |
| Marital status              | ✅ `maritalStatus()`                          | ❌ No           | MEDIUM                  | Common demographic                        |
| **Relationships**           |
| Direct relationships        | ✅ `direct()` (mother, father)                | ❌ No           | LOW                     | Nice-to-have                              |
| Extended family             | ✅ `extended()`, `inLaw()`                    | ❌ No           | LOW                     |                                           |
| **Other Personal**          |
| Passport number             | ✅ `valid()`                                  | ❌ No           | MEDIUM                  | Travel documents                          |
| Driver's license            | ✅ `drivingLicense()`                         | ❌ No           | MEDIUM                  | ID documents                              |

### 2. ADDRESS & LOCATION

| Feature                  | DataFaker Support                                     | krandom Status | Implementation Priority | Notes                      |
|--------------------------|-------------------------------------------------------|----------------|-------------------------|----------------------------|
| **Street Address**       |
| Street name              | ✅ `streetName()`                                      | ❌ No           | HIGH                    | Core address component     |
| Street address           | ✅ `streetAddress()`                                   | ❌ No           | HIGH                    | Full street with number    |
| Street number            | ✅ `streetAddressNumber()`, `buildingNumber()`         | ❌ No           | HIGH                    |                            |
| Secondary address        | ✅ `secondaryAddress()` (Apt, Suite)                   | ❌ No           | MEDIUM                  | Common for apartments      |
| Street suffix/prefix     | ✅ `streetSuffix()`, `streetPrefix()`                  | ❌ No           | MEDIUM                  | St, Ave, Blvd              |
| **City & State**         |
| City name                | ✅ `city()`, `cityName()`                              | ❌ No           | HIGH                    | Essential                  |
| City prefix/suffix       | ✅ `cityPrefix()`, `citySuffix()`                      | ❌ No           | LOW                     | Building blocks            |
| State                    | ✅ `state()`                                           | ❌ No           | HIGH                    | US states                  |
| State abbreviation       | ✅ `stateAbbr()`                                       | ❌ No           | HIGH                    | CA, TX, NY                 |
| **Postal Codes**         |
| ZIP code                 | ✅ `zipCode()`                                         | ❌ No           | HIGH                    | US postal codes            |
| ZIP+4                    | ✅ `zipCodePlus4()`                                    | ❌ No           | MEDIUM                  | Extended ZIP               |
| ZIP by state             | ✅ `zipCodeByState()`                                  | ❌ No           | MEDIUM                  | State-specific             |
| County by ZIP            | ✅ `countyByZipCode()`                                 | ❌ No           | LOW                     | Geographic mapping         |
| Postcode (generic)       | ✅ `postcode()`                                        | ❌ No           | MEDIUM                  | International              |
| Eircode (Ireland)        | ✅ `eircode()`                                         | ❌ No           | LOW                     | Locale-specific            |
| Mailbox                  | ✅ `mailBox()` (PO Box)                                | ❌ No           | LOW                     |                            |
| **Country & Nation**     |
| Country name             | ✅ `country()`                                         | ❌ No           | HIGH                    | Essential                  |
| Country code             | ✅ `countryCode()`, `countryCode2()`, `countryCode3()` | ❌ No           | HIGH                    | ISO codes                  |
| Capital city             | ✅ `capital()`                                         | ❌ No           | MEDIUM                  | Geographic data            |
| Currency                 | ✅ `currency()`, `currencyCode()`                      | ✅ Yes          | ✓ DONE                  | Already in Money generator |
| Flag emoji               | ✅ `flag()`                                            | ❌ No           | LOW                     | Unicode flags              |
| Nationality              | ✅ `nationality()`                                     | ❌ No           | MEDIUM                  | Citizen of...              |
| Language                 | ✅ `language()`, `isoLanguage()`                       | ❌ No           | MEDIUM                  | Spoken languages           |
| **Coordinates**          |
| Latitude                 | ✅ `latitude()`                                        | ❌ No           | MEDIUM                  | Geographic coordinates     |
| Longitude                | ✅ `longitude()`                                       | ❌ No           | MEDIUM                  |                            |
| Lat/Lon pair             | ✅ `latLon()`, `lonLat()`                              | ❌ No           | MEDIUM                  | Combined coordinates       |
| **Direction & Location** |
| Compass direction        | ✅ `word()`, `abbreviation()`, `azimuth()`             | ❌ No           | LOW                     | N, NE, NNE                 |
| Time zone                | ✅ `timeZone()`                                        | ❌ No           | MEDIUM                  | America/New_York           |
| Full address             | ✅ `fullAddress()`                                     | ❌ No           | HIGH                    | Complete formatted address |

### 3. INTERNET & NETWORKING

| Feature           | DataFaker Support                    | krandom Status | Implementation Priority | Notes                   |
|-------------------|--------------------------------------|----------------|-------------------------|-------------------------|
| **Email**         |
| Email address     | ✅ `emailAddress()`                   | ❌ No           | HIGH                    | Essential for testing   |
| Safe email        | ✅ `safeEmailAddress()` (example.com) | ❌ No           | HIGH                    | Non-deliverable domains |
| Email subject     | ✅ `emailSubject()`                   | ❌ No           | LOW                     |                         |
| **Domain & URLs** |
| Domain name       | ✅ `domainName()`                     | ❌ No           | HIGH                    | example.com             |
| Domain word       | ✅ `domainWord()`                     | ❌ No           | MEDIUM                  |                         |
| Domain suffix     | ✅ `domainSuffix()` (.com, .org)      | ❌ No           | MEDIUM                  | TLDs                    |
| URL               | ✅ `url()`                            | ❌ No           | HIGH                    | Full URLs               |
| Web domain        | ✅ `webdomain()`                      | ❌ No           | MEDIUM                  |                         |
| Slug              | ✅ `slug()`                           | ❌ No           | MEDIUM                  | URL-friendly strings    |
| **IP Addresses**  |
| IPv4              | ✅ `ipV4Address()`                    | ✅ Yes          | ✓ DONE                  | Already implemented     |
| IPv4 private      | ✅ `privateIpV4Address()`             | ❌ No           | MEDIUM                  | RFC1918 addresses       |
| IPv4 public       | ✅ `publicIpV4Address()`              | ❌ No           | MEDIUM                  | Non-private             |
| IPv4 CIDR         | ✅ `ipV4Cidr()`                       | ❌ No           | MEDIUM                  | Network notation        |
| IPv6              | ✅ `ipV6Address()`                    | ✅ Yes          | ✓ DONE                  | Already implemented     |
| IPv6 CIDR         | ✅ `ipV6Cidr()`                       | ❌ No           | MEDIUM                  | IPv6 networks           |
| **Network**       |
| MAC address       | ✅ `macAddress()`                     | ❌ No           | MEDIUM                  | Hardware addresses      |
| Port              | ✅ `port()`                           | ❌ No           | MEDIUM                  | 1-65535                 |
| HTTP method       | ✅ `httpMethod()` (GET, POST)         | ❌ No           | LOW                     | REST APIs               |
| **Identifiers**   |
| UUID v3           | ✅ `uuidv3()`                         | ❌ No           | MEDIUM                  | Name-based              |
| UUID v4           | ✅ `uuid()`, `uuidv4()`               | ❌ No           | HIGH                    | Random UUID             |
| UUID v7           | ✅ `uuidv7()`                         | ❌ No           | LOW                     | Time-ordered            |
| **User Agents**   |
| User agent        | ✅ `userAgent()`                      | ❌ No           | MEDIUM                  | Browser strings         |
| Bot user agent    | ✅ `botUserAgent()`                   | ❌ No           | LOW                     | Crawler UAs             |
| **Other**         |
| Image URL         | ✅ `image()`                          | ❌ No           | LOW                     | Placeholder images      |

### 4. FINANCE & COMMERCE

| Feature               | DataFaker Support                                                                             | krandom Status | Implementation Priority | Notes                 |
|-----------------------|-----------------------------------------------------------------------------------------------|----------------|-------------------------|-----------------------|
| **Credit Cards**      |
| Credit card number    | ✅ 10 types, Luhn-valid                                                                        | ❌ No           | HIGH                    | VISA, MC, AMEX, etc.  |
| Card types            | ✅ VISA, MASTERCARD, DISCOVER, AMEX, DINERS, JCB, DANKORT, FORBRUGSFORENINGEN, LASER, UNIONPAY | ❌ No           | HIGH                    | Comprehensive         |
| Card expiry           | ✅ `creditCardExpiry()`                                                                        | ❌ No           | HIGH                    | MM/YY format          |
| Security code         | ✅ `securityCode()` (CVV)                                                                      | ❌ No           | HIGH                    | 3-4 digits            |
| **Banking**           |
| BIC/SWIFT             | ✅ `bic()`                                                                                     | ❌ No           | MEDIUM                  | Bank identifier       |
| IBAN                  | ✅ `iban()`                                                                                    | ❌ No           | MEDIUM                  | International account |
| US routing number     | ✅ `usRoutingNumber()`                                                                         | ❌ No           | MEDIUM                  | ACH routing           |
| **Money & Currency**  |
| Currency name         | ✅ `currency()`                                                                                | ✅ Yes          | ✓ DONE                  |                       |
| Currency code         | ✅ `currencyCode()` (USD, EUR)                                                                 | ✅ Yes          | ✓ DONE                  |                       |
| Currency symbol       | ✅ `currencySymbol()` ($, €)                                                                   | ❌ No           | MEDIUM                  |                       |
| Currency numeric code | ✅ `currencyNumericCode()` (840)                                                               | ❌ No           | LOW                     | ISO 4217              |
| Price                 | ✅ `price()`                                                                                   | ❌ No           | MEDIUM                  | Formatted prices      |
| **Stock Market**      |
| NASDAQ symbol         | ✅ `nsdqSymbol()`                                                                              | ❌ No           | LOW                     | Stock tickers         |
| NYSE symbol           | ✅ `nyseSymbol()`                                                                              | ❌ No           | LOW                     |                       |
| NSE symbol            | ✅ `nseSymbol()`                                                                               | ❌ No           | LOW                     | India                 |
| LSE symbol            | ✅ `lseSymbol()`                                                                               | ❌ No           | LOW                     | London                |
| Exchange names        | ✅ `exchanges()`                                                                               | ❌ No           | LOW                     |                       |
| **Commerce**          |
| Department            | ✅ `department()`                                                                              | ❌ No           | LOW                     | Store departments     |
| Product name          | ✅ `productName()`                                                                             | ❌ No           | MEDIUM                  | E-commerce            |
| Material              | ✅ `material()`                                                                                | ❌ No           | LOW                     | Product materials     |
| Brand                 | ✅ `brand()`, `sport()`, `car()`, `watch()`                                                    | ❌ No           | MEDIUM                  | Brand names           |
| Vendor                | ✅ `vendor()`                                                                                  | ❌ No           | LOW                     | Suppliers             |
| Promotion code        | ✅ `promotionCode()`                                                                           | ❌ No           | LOW                     | Discount codes        |
| **Subscriptions**     |
| Plans                 | ✅ `plans()`                                                                                   | ❌ No           | LOW                     | Free, Premium, etc.   |
| Statuses              | ✅ `statuses()`                                                                                | ❌ No           | LOW                     | Active, Cancelled     |
| Payment methods       | ✅ `paymentMethods()`                                                                          | ❌ No           | LOW                     | Card, PayPal, etc.    |
| Payment terms         | ✅ `paymentTerms()`                                                                            | ❌ No           | LOW                     | Net 30, etc.          |

### 5. COMPANY & BUSINESS

| Feature        | DataFaker Support            | krandom Status | Implementation Priority | Notes                   |
|----------------|------------------------------|----------------|-------------------------|-------------------------|
| Company name   | ✅ `name()`                   | ❌ No           | HIGH                    | Essential business data |
| Company suffix | ✅ `suffix()` (Inc, LLC, Ltd) | ❌ No           | MEDIUM                  | Legal entities          |
| Industry       | ✅ `industry()`               | ❌ No           | MEDIUM                  | Business sectors        |
| Profession     | ✅ `profession()`             | ❌ No           | MEDIUM                  |                         |
| Buzzword       | ✅ `buzzword()`               | ❌ No           | LOW                     | Marketing speak         |
| Catch phrase   | ✅ `catchPhrase()`            | ❌ No           | LOW                     | Company slogans         |
| BS phrase      | ✅ `bs()`                     | ❌ No           | LOW                     | Corporate BS            |
| Logo URL       | ✅ `logo()`                   | ❌ No           | LOW                     | Company logos           |
| Company URL    | ✅ `url()`                    | ❌ No           | MEDIUM                  | Corporate websites      |

### 6. JOB & CAREER

| Feature    | DataFaker Support | krandom Status | Implementation Priority | Notes                  |
|------------|-------------------|----------------|-------------------------|------------------------|
| Job field  | ✅ `field()`       | ❌ No           | MEDIUM                  | Engineering, Marketing |
| Seniority  | ✅ `seniority()`   | ❌ No           | MEDIUM                  | Junior, Senior, Lead   |
| Position   | ✅ `position()`    | ❌ No           | MEDIUM                  | Developer, Manager     |
| Job title  | ✅ `title()`       | ✅ Yes          | ✓ DONE                  | Combined title         |
| Key skills | ✅ `keySkills()`   | ❌ No           | LOW                     | Job requirements       |

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

| Feature      | DataFaker Support                     | krandom Status | Implementation Priority | Notes              |
|--------------|---------------------------------------|----------------|-------------------------|--------------------|
| Future date  | ✅ `future()`                          | ❌ No           | HIGH                    | Configurable range |
| Past date    | ✅ `past()`                            | ❌ No           | HIGH                    | Configurable range |
| Date between | ✅ `between()`                         | ❌ No           | HIGH                    | Range generation   |
| Birthday     | ✅ `birthday()`, `birthdayLocalDate()` | ❌ No           | MEDIUM                  | Age-appropriate    |
| Duration     | ✅ `duration()`                        | ❌ No           | MEDIUM                  | Time spans         |
| Period       | ✅ `period()`                          | ❌ No           | MEDIUM                  | Date periods       |

### 9. PHONE NUMBERS

| Feature              | DataFaker Support              | krandom Status | Implementation Priority | Notes           |
|----------------------|--------------------------------|----------------|-------------------------|-----------------|
| Phone number         | ✅ `phoneNumber()`              | ❌ No           | HIGH                    | General format  |
| National format      | ✅ `phoneNumberNational()`      | ❌ No           | HIGH                    | (555) 123-4567  |
| International format | ✅ `phoneNumberInternational()` | ❌ No           | HIGH                    | +1 555 123 4567 |
| Cell phone           | ✅ `cellPhone()`                | ❌ No           | MEDIUM                  | Mobile numbers  |
| Cell international   | ✅ `cellPhoneInternational()`   | ❌ No           | MEDIUM                  |                 |
| Extension            | ✅ `extension()`                | ❌ No           | LOW                     | x1234           |
| Subscriber number    | ✅ `subscriberNumber()`         | ❌ No           | LOW                     |                 |

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

| Feature    | DataFaker Support | krandom Status | Implementation Priority | Notes         |
|------------|-------------------|----------------|-------------------------|---------------|
| Color name | ✅ `name()`        | ❌ No           | MEDIUM                  | "Red", "Blue" |
| Hex color  | ✅ `hex()`         | ❌ No           | MEDIUM                  | #FF5733       |

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

| Feature                  | DataFaker                          | krandom | Priority | Implementation Notes        |
|--------------------------|------------------------------------|---------|----------|-----------------------------|
| **Locale Support**       |
| Multiple locales         | ✅ 60+ locales                      | ❌ No    | HIGH     | Essential for i18n          |
| Locale-aware data        | ✅ Names, addresses, phones         | ❌ No    | HIGH     | Currently no locale support |
| Runtime locale switching | ✅ Yes                              | ❌ No    | MEDIUM   |                             |
| **Seeding**              |
| Reproducible output      | ✅ Constructor with seed            | ✅ Yes   | ✓ DONE   | Most generators support     |
| **String Utilities**     |
| Numerify                 | ✅ `numerify("###-####")`           | ❌ No    | HIGH     | Template-based generation   |
| Letterify                | ✅ `letterify("???-???")`           | ❌ No    | HIGH     |                             |
| Bothify                  | ✅ `bothify("???-###")`             | ❌ No    | HIGH     | Combined                    |
| Regexify                 | ✅ `regexify("[A-Z]{3}\\d{4}")`     | ❌ No    | HIGH     | Regex-based generation      |
| Examplify                | ✅ `examplify("ABC-1234")`          | ❌ No    | MEDIUM   | Match pattern               |
| Templatify               | ✅ Custom templates                 | ❌ No    | MEDIUM   |                             |
| **Data Sources**         |
| Custom YAML              | ✅ `addPath()`, `addUrl()`          | ❌ No    | MEDIUM   | Extensibility               |
| YAML key resolution      | ✅ `resolve(key)`                   | ❌ No    | LOW      |                             |
| **Collections**          |
| Generate lists           | ✅ `collection().len(n).generate()` | ❌ No    | HIGH     | Bulk generation             |
| Variable length          | ✅ `minLen()`, `maxLen()`           | ❌ No    | MEDIUM   |                             |
| Nullable values          | ✅ `nullRate(0.1)`                  | ❌ No    | MEDIUM   | Realistic nulls             |
| Stream API               | ✅ `stream().limit(n)`              | ❌ No    | HIGH     | Java 8 streams              |
| **Unique Values**        |
| Unique enforcement       | ✅ `faker.unique()`                 | ❌ No    | HIGH     | No duplicates               |
| **Output Formats**       |
| CSV generation           | ✅ Schema-based                     | ❌ No    | MEDIUM   | Structured output           |
| JSON generation          | ✅ Schema-based                     | ❌ No    | MEDIUM   |                             |
| YAML generation          | ✅ Schema-based                     | ❌ No    | LOW      |                             |
| XML generation           | ✅ Schema-based                     | ❌ No    | LOW      |                             |
| **Expressions**          |
| YAML expressions         | ✅ `#{Provider.method}`             | ❌ No    | MEDIUM   | Composable generators       |
| **Custom Providers**     |
| Extend with custom       | ✅ `AbstractProvider<T>`            | ❌ No    | LOW      | Plugin system               |
| **Object Population**    |
| POJO population          | ✅ `@Fake` annotation               | ❌ No    | LOW      | Auto-fill objects           |

---

## IMPLEMENTATION RECOMMENDATIONS

### Phase 1: CRITICAL GAPS (Must Have)

1. **Email Generation** - Essential for testing
    - `emailAddress()`, `safeEmailAddress()`
2. **UUID Generation** - Common identifier need
    - `uuid()` (v4 minimum)
3. **Boolean Generator** - Basic data type missing
    - `bool()`
4. **Lorem Text** - Critical for UI/content testing
    - `word()`, `words()`, `sentence()`, `paragraph()`
5. **Locale Support Infrastructure** - Foundation for i18n
    - Basic locale framework
6. **String Templates** - Powerful generation tool
    - `numerify()`, `letterify()`, `bothify()`
7. **Collection Generation** - Bulk data needs
    - `collection().len(n).generate()`
8. **Unique Value Enforcement** - Prevent duplicates
    - `unique()` wrapper

### Phase 2: HIGH VALUE (Should Have)

1. **Address Components**
    - Street, city, state, ZIP, country
2. **Phone Numbers**
    - National and international formats
3. **Credit Cards**
    - Luhn-valid cards with multiple types
4. **Names Enhancement**
    - Gender-specific first names, prefixes, suffixes
5. **Date Generators**
    - Past, future, birthday, between
6. **Company Data**
    - Company names, industries, buzzwords
7. **URL/Domain Generation**
    - Full URLs, domain names

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

1. **200+ providers** vs ~15 in krandom
2. **60+ locales** with locale-aware data
3. **Schema-based output** (CSV/JSON/YAML/XML)
4. **Template-based generation** (numerify, regexify)
5. **Unique value enforcement** built-in
6. **Expression language** for composable generators
7. **GraalVM native image** support
8. **Collections/Stream API** integration
9. **POJO auto-population** via annotations
10. **Comprehensive entertainment data** (40+ franchises)

### krandom Strengths (vs DataFaker)

1. **Simpler, focused API** - easier to learn
2. **Kotlin-first** design
3. **Type-safe builders** for complex generators
4. **Custom algorithms** (Fibonacci, Luhn)
5. **Cleaner project structure**
6. **Better test coverage** (99%+)

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
