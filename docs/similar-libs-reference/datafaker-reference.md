# DataFaker Library — Comprehensive Reference Document

## Table of Contents

1. [Purpose and Overview](#1-purpose-and-overview)
2. [Installation](#2-installation)
3. [Core API](#3-core-api)
4. [Providers by Category](#4-providers-by-category)
5. [Sequence Generators (Collections and Streams)](#5-sequence-generators-collections-and-streams)
6. [Faker Expressions (YAML-based)](#6-faker-expressions-yaml-based)
7. [Custom Providers](#7-custom-providers)
8. [Comparison: krandom vs DataFaker](#8-comparison-krandom-vs-datafaker)

---

## 1. Purpose and Overview

DataFaker is the **official successor to JavaFaker**. It is a JVM library (Java, Kotlin, Groovy) for generating realistic fake data across hundreds of domains. It was created as a modernised fork of
`java-faker` with updated dependencies, a current 263-provider catalog,
experimental GraalVM native-image support, and a substantially improved API.

| Property              | Value                                           |
|-----------------------|-------------------------------------------------|
| GitHub                | https://github.com/datafaker-net/datafaker      |
| Latest stable version | 2.7.0                                           |
| Java requirement      | Java 17+ (v2.x); Java 8 supported in v1.x (EOL) |
| License               | Apache License 2.0                              |
| Locale support        | 70 advertised locale tags                       |
| Predecessor           | java-faker (GitHub: DiUS/java-faker)            |

### Key improvements over JavaFaker

- Kotlin and Groovy first-class support.
- 200+ providers (JavaFaker had ~100).
- `faker.collection()` and `faker.stream()` for bulk generation.
- Structured output: CSV, JSON, YAML, XML via `Schema` API.
- Unique value enforcement via `faker.unique()`.
- GraalVM native-image metadata (experimental, since 2.4.1).
- Object auto-population via `@Fake` annotations.
- Custom YAML data sources via `addPath()` / `addUrl()`.

---

## 2. Installation

### Maven

```xml

<dependency>
    <groupId>net.datafaker</groupId>
    <artifactId>datafaker</artifactId>
        <version>2.7.0</version>
</dependency>
```

### Gradle (Groovy DSL)

```groovy
dependencies {
    implementation 'net.datafaker:datafaker:2.7.0'
}
```

### Gradle (Kotlin DSL)

```kotlin
dependencies {
    implementation("net.datafaker:datafaker:2.7.0")
}
```

### Snapshot builds

Use the documented snapshot repository only when deliberately testing DataFaker's next snapshot; the current upstream README advertises `2.8.0-SNAPSHOT`.

```xml

<repository>
    <id>sonatype-snapshots</id>
    <url>https://oss.sonatype.org/content/repositories/snapshots</url>
    <snapshots>
        <enabled>true</enabled>
    </snapshots>
</repository>
```

---

## 3. Core API

### 3.1 `Faker` Class

`net.datafaker.Faker` is the main entry point. It extends `BaseFaker` and aggregates all provider accessor methods. Every provider is accessed as a method on `Faker`.

```java
// Default locale (en-US), SecureRandom
Faker faker = new Faker();

// Specific locale
Faker faker = new Faker(Locale.of("de"));
Faker faker = new Faker(Locale.of("en", "US"));

// Seeded (reproducible output)
Faker faker = new Faker(new Random(42));
Faker faker = new Faker(Locale.of("en"), new Random(12345));
```

```kotlin
val faker = Faker()
val faker = Faker(Locale("pt", "BR"))
val faker = Faker(Random(42))
```

### 3.2 Locale Support

DataFaker's current README lists 70 locale tags. Pass a `java.util.Locale` to the constructor:

```java
new Faker(Locale.of("fr"))        // French
    new

Faker(Locale.of("zh", "CN"))  // Chinese (Simplified)
    new

Faker(Locale.of("ja"))        // Japanese
    new

Faker(Locale.of("ko"))        // Korean
    new

Faker(Locale.of("ru"))        // Russian
    new

Faker(Locale.of("es", "MX"))  // Spanish (Mexico)
    new

Faker(Locale.of("pt", "BR"))  // Portuguese (Brazil)
```

Use `faker.locality().allSupportedLocales()` to list all supported locale strings at runtime.

### 3.3 Seeding (Reproducible Output)

```java
Faker faker = new Faker(new Random(12345L));
// Every call to faker.name().fullName() returns the same sequence
```

Seeding is critical for deterministic test data. The seed is passed directly to `java.util.Random` or any `RandomGenerator` implementation.

### 3.4 String Transformation Utilities

These are methods on `BaseFaker` / `Faker` itself (not on providers):

| Method                                      | Pattern  | Description                                                 |
|---------------------------------------------|----------|-------------------------------------------------------------|
| `faker.numerify(String)`                    | `#`      | Replaces `#` with a random digit 0–9                        |
| `faker.letterify(String)`                   | `?`      | Replaces `?` with a random letter a–z                       |
| `faker.bothify(String)`                     | `#`, `?` | Applies both numerify and letterify                         |
| `faker.regexify(String)`                    | regex    | Generates a string matching the regex                       |
| `faker.examplify(String)`                   | example  | Generates a string matching the type pattern of the example |
| `faker.templatify(String, char, String...)` | template | Template-based generation with custom placeholder           |

```java
faker.numerify("###-###")      // "492-371"
faker.

letterify("???-???")     // "kxm-pty"
faker.

bothify("##??##")        // "27kq91"
faker.

regexify("[A-Z]{3}\\d{4}") // "KXP7214"
```

### 3.5 `faker.expression()`

Evaluates a native DataFaker expression string using the YAML key format:

```java
faker.expression("#{Name.fullName}")          // "John Doe"
faker.

expression("#{Address.city}")           // "Los Angeles"
faker.

expression("#{numerify '###-####'}")    // "492-3718"
faker.

expression("#{letterify '???'}")        // "kxm"
faker.

expression("#{regexify '[A-Z0-9]{8}'}")// "K3PQ8WR1"
faker.

expression("#{options.option 'a','b','c'}")  // "b"
faker.

expression("#{date.birthday 'yy','MM','dd'}") // "85,03,21"
```

The expression format follows `#{ProviderClass.method}` or `#{ProviderClass.method 'arg1','arg2'}`.

### 3.6 `faker.resolve(String key)`

Directly resolves a YAML key from the data files:

```java
faker.resolve("name.first_name")  // picks a random first name from YAML
faker.

resolve("address.city")
```

### 3.7 Data Source Customisation

```java
// Add a local YAML file for a locale
faker.addPath(Locale.ENGLISH, Path.of("/my/custom/en.yml"));

    // Add a remote YAML data source
    faker.

addUrl(Locale.ENGLISH, new URL("https://example.com/en.yml"));
```

### 3.8 Object Population (`@Fake` Annotation)

DataFaker can auto-populate Java classes using annotations:

```java
public class Person {

    @Fake("#{Name.fullName}")
    private String name;

    @Fake("#{Address.streetAddress}")
    private String address;

    @Fake("#{Internet.emailAddress}")
    private String email;
}


Person person = faker.populate(Person.class);
```

---

## 4. Providers by Category

### 4.1 Personal Identity

#### `faker.name()`

| Method              | Description                                    |
|---------------------|------------------------------------------------|
| `name()`            | Full name (optional prefix + first + last)     |
| `fullName()`        | Alias for `name()`                             |
| `nameWithMiddle()`  | Full name including middle name                |
| `firstName()`       | Random given name                              |
| `femaleFirstName()` | Female given name                              |
| `maleFirstName()`   | Male given name                                |
| `lastName()`        | Family name                                    |
| `prefix()`          | Title prefix: Mr., Mrs., Dr., etc.             |
| `suffix()`          | Name suffix: Jr., Sr., MD, PhD, etc.           |
| `title()`           | Three-part job title                           |
| `username()`        | Deprecated — use `faker.internet().username()` |

#### `faker.idNumber()`

| Method                                  | Description                     |
|-----------------------------------------|---------------------------------|
| `valid()`                               | Valid locale-specific ID number |
| `invalid()`                             | Invalid ID number               |
| `valid(IdNumberRequest)`                | ID with age/gender parameters   |
| `ssnValid()`                            | Valid US Social Security Number |
| `singaporeanFin()` / `singaporeanUin()` | Singaporean IDs                 |
| `peselNumber(LocalDate, Gender)`        | Polish PESEL                    |
| `validZhCNSsn()`                        | Chinese ID (deprecated)         |
| `validPtNif()`                          | Portuguese NIF (deprecated)     |
| `validEsMXSsn()`                        | Mexican CURP (deprecated)       |
| `validEnZaSsn()`                        | South African SSN (deprecated)  |

#### `faker.gender()`

| Method               | Description                |
|----------------------|----------------------------|
| `types()`            | All gender types           |
| `binaryTypes()`      | Binary types (Male/Female) |
| `shortBinaryTypes()` | Short form (M/F)           |

#### `faker.demographic()`

| Method                    | Description                |
|---------------------------|----------------------------|
| `race()`                  | Random race                |
| `educationalAttainment()` | Education level            |
| `demonym()`               | Demonym (e.g., "American") |
| `sex()`                   | Sex                        |
| `maritalStatus()`         | Marital status             |

#### `faker.relationship()`

| Method       | Description                        |
|--------------|------------------------------------|
| `direct()`   | Direct family relationship         |
| `extended()` | Extended family                    |
| `inLaw()`    | In-law relationships               |
| `spouse()`   | Spouse                             |
| `parent()`   | Parent                             |
| `sibling()`  | Sibling                            |
| `any()`      | Random relationship from all types |

#### `faker.funnyName()`

| Method   | Description   |
|----------|---------------|
| `name()` | Humorous name |

---

### 4.2 Address and Location

#### `faker.address()`

| Method                    | Description                     |
|---------------------------|---------------------------------|
| `streetName()`            | Street name                     |
| `streetAddress()`         | Full street address             |
| `streetAddress(boolean)`  | With optional secondary address |
| `streetAddressNumber()`   | Building number 0–999           |
| `secondaryAddress()`      | Apt, Suite, etc.                |
| `buildingNumber()`        | Building number                 |
| `zipCode()`               | 5-digit ZIP code                |
| `zipCodePlus4()`          | ZIP+4 code                      |
| `zipCodeByState(String)`  | ZIP by US state abbreviation    |
| `postcode()`              | Locale-specific postal code     |
| `eircode()`               | Irish Eircode                   |
| `countyByZipCode(String)` | County from ZIP code            |
| `streetSuffix()`          | St, Ave, Blvd, etc.             |
| `streetPrefix()`          | Street prefix                   |
| `cityPrefix()`            | City prefix                     |
| `citySuffix()`            | City suffix                     |
| `city()`                  | City name                       |
| `cityName()`              | Alias for city()                |
| `state()`                 | State name                      |
| `stateAbbr()`             | State abbreviation              |
| `country()`               | Country name                    |
| `countryCode()`           | Country code                    |
| `latitude()`              | Latitude (-90 to 90)            |
| `longitude()`             | Longitude (-180 to 180)         |
| `latLon()`                | Lat,Lon formatted string        |
| `latLon(String)`          | With custom delimiter           |
| `lonLat()`                | Lon,Lat formatted string        |
| `lonLat(String)`          | With custom delimiter           |
| `timeZone()`              | Timezone string                 |
| `fullAddress()`           | Complete address                |
| `mailBox()`               | Mailbox designation             |

#### `faker.country()`

| Method           | Description               |
|------------------|---------------------------|
| `name()`         | Country name              |
| `countryCode2()` | ISO 2-letter country code |
| `countryCode3()` | ISO 3-letter country code |
| `capital()`      | Capital city              |
| `currency()`     | Currency display name     |
| `currencyCode()` | ISO 4217 currency code    |
| `flag()`         | URL to country flag image |

#### `faker.nation()`

| Method          | Description        |
|-----------------|--------------------|
| `nationality()` | Nationality string |
| `language()`    | Language name      |
| `capitalCity()` | Capital city       |
| `flag()`        | Flag emoji         |
| `isoLanguage()` | ISO language code  |
| `isoCountry()`  | ISO country code   |

#### `faker.compass()`

| Method                       | Description             |
|------------------------------|-------------------------|
| `compassPoint(CompassPoint)` | Set direction point     |
| `word()`                     | Full direction name     |
| `abbreviation()`             | Direction abbreviation  |
| `azimuth()`                  | Direction azimuth value |

#### `faker.locality()`

| Method                             | Description                          |
|------------------------------------|--------------------------------------|
| `allSupportedLocales()`            | List of all supported locale strings |
| `displayName()`                    | Random locale display name           |
| `localeString()`                   | Random locale string (e.g., "es-MX") |
| `localeStringWithoutReplacement()` | Without replacement                  |

---

### 4.3 Internet and Networking

#### `faker.internet()`

| Method                                          | Description                     |
|-------------------------------------------------|---------------------------------|
| `emailAddress()`                                | Random email                    |
| `emailAddress(String name)`                     | Email from given name           |
| `safeEmailAddress()`                            | Safe email (example.com domain) |
| `safeEmailAddress(String name)`                 | Safe email from name            |
| `emailSubject()`                                | Random email subject line       |
| `domainName()`                                  | Full domain name                |
| `domainWord()`                                  | Domain word portion             |
| `domainSuffix()`                                | TLD (.com, .net, etc.)          |
| `url()`                                         | Random web URL                  |
| `url(bool,bool,bool,bool,bool,bool)`            | Customisable URL                |
| `webdomain()`                                   | www.example.com format          |
| `image()`                                       | Image URL                       |
| `image(int w, int h)`                           | Image URL with dimensions       |
| `macAddress()`                                  | MAC address                     |
| `macAddress(String prefix)`                     | MAC with prefix                 |
| `ipV4Address()`                                 | IPv4 address string             |
| `getIpV4Address()`                              | IPv4 as InetAddress             |
| `privateIpV4Address()`                          | Private IPv4                    |
| `publicIpV4Address()`                           | Public IPv4                     |
| `ipV4Cidr()`                                    | IPv4 CIDR notation              |
| `ipV6Address()`                                 | IPv6 address string             |
| `getIpV6Address()`                              | IPv6 as InetAddress             |
| `ipV6Cidr()`                                    | IPv6 CIDR                       |
| `slug()`                                        | URL-friendly slug               |
| `slug(List<String>, String)`                    | Custom slug                     |
| `uuid()` / `uuidv3()` / `uuidv4()` / `uuidv7()` | UUID generation                 |
| `httpMethod()`                                  | GET, POST, PUT, etc.            |
| `port()`                                        | Random port 0–65535             |
| `port(PortRange)`                               | Port in range                   |
| `port(int from, int to)`                        | Custom port range               |
| `userAgent()`                                   | Random browser UA string        |
| `userAgent(UserAgent)`                          | Specific browser UA             |
| `botUserAgent(BotUserAgent)`                    | Bot user agent                  |
| `botUserAgentAny()`                             | Random bot UA                   |

---

### 4.4 Finance and Commerce

#### `faker.finance()`

| Method                       | Description                             |
|------------------------------|-----------------------------------------|
| `creditCard(CreditCardType)` | Credit card number (Luhn-valid)         |
| `creditCard()`               | Random credit card of any type          |
| `bic()`                      | Business Identifier Code                |
| `iban()`                     | Random IBAN                             |
| `iban(String countryCode)`   | IBAN for specific country               |
| `ibanSupportedCountries()`   | Set of supported country codes          |
| `usRoutingNumber()`          | US bank routing number (checksum-valid) |

Supported credit card types: `VISA`, `MASTERCARD`, `DISCOVER`, `AMERICAN_EXPRESS`, `DINERS_CLUB`, `JCB`, `DANKORT`, `FORBRUGSFORENINGEN`, `LASER`, `UNIONPAY`.

#### `faker.money()`

| Method                  | Description                                          |
|-------------------------|------------------------------------------------------|
| `currency()`            | Currency display name (e.g., "United States dollar") |
| `currencyCode()`        | ISO 4217 code (e.g., "USD")                          |
| `currencyNumericCode()` | 3-digit ISO numeric code                             |
| `currencySymbol()`      | Currency symbol (e.g., "$")                          |

#### `faker.stock()`

| Method         | Description          |
|----------------|----------------------|
| `nsdqSymbol()` | NASDAQ ticker symbol |
| `nyseSymbol()` | NYSE ticker symbol   |
| `nseSymbol()`  | NSE symbol           |
| `lseSymbol()`  | LSE symbol           |
| `exchanges()`  | Exchange name        |

#### `faker.business()`

| Method               | Description                  |
|----------------------|------------------------------|
| `creditCardNumber()` | Formatted credit card number |
| `creditCardType()`   | Credit card type name        |
| `creditCardExpiry()` | Future expiry date           |
| `securityCode()`     | CVV/security code            |

#### `faker.commerce()`

| Method                          | Description               |
|---------------------------------|---------------------------|
| `department()`                  | Store department name     |
| `productName()`                 | Product name              |
| `material()`                    | Material description      |
| `brand()`                       | Brand name                |
| `vendor()`                      | Vendor name               |
| `price()`                       | Price between 0.00–100.00 |
| `price(double min, double max)` | Price in custom range     |
| `promotionCode()`               | Promo code with 6 digits  |
| `promotionCode(int digits)`     | Promo code with N digits  |

#### `faker.subscription()`

| Method                | Description            |
|-----------------------|------------------------|
| `plans()`             | Subscription plan name |
| `statuses()`          | Subscription status    |
| `paymentMethods()`    | Payment method         |
| `subscriptionTerms()` | Subscription term      |
| `paymentTerms()`      | Payment term           |

---

### 4.5 Company and Job

#### `faker.company()`

| Method          | Description                      |
|-----------------|----------------------------------|
| `name()`        | Company name                     |
| `suffix()`      | Company suffix (Inc., LLC, etc.) |
| `industry()`    | Industry classification          |
| `profession()`  | Profession string                |
| `buzzword()`    | Business buzzword                |
| `catchPhrase()` | Marketing catch phrase           |
| `bs()`          | Business-speak phrase            |
| `logo()`        | URL to company logo image        |
| `url()`         | Company website URL              |

#### `faker.job()`

| Method        | Description     |
|---------------|-----------------|
| `field()`     | Job field       |
| `seniority()` | Seniority level |
| `position()`  | Job position    |
| `keySkills()` | Key skills      |
| `title()`     | Job title       |

---

### 4.6 Text and Language

#### `faker.lorem()`

| Method                                                                            | Description                   |
|-----------------------------------------------------------------------------------|-------------------------------|
| `character()`                                                                     | Single random character       |
| `characters()`                                                                    | 255 random characters         |
| `characters(int min, int max)`                                                    | Characters in length range    |
| `characters(int min, int max, boolean uppercase, boolean special, boolean digit)` | Full control                  |
| `word()`                                                                          | Single word                   |
| `words()`                                                                         | 3 random words                |
| `words(int n)`                                                                    | N words                       |
| `sentence()`                                                                      | Sentence with 3–8 words       |
| `sentence(int wordCount)`                                                         | Sentence with N words         |
| `sentences(int n)`                                                                | N sentences                   |
| `paragraph()`                                                                     | Paragraph (3+ sentences)      |
| `paragraph(int n)`                                                                | Paragraph with N sentences    |
| `paragraphs(int n)`                                                               | N paragraphs                  |
| `fixedString(int n)`                                                              | Fixed-length letter string    |
| `maxLengthSentence(int n)`                                                        | Sentence truncated to N chars |

#### `faker.hacker()`

| Method           | Description                        |
|------------------|------------------------------------|
| `abbreviation()` | Hacker abbreviation (e.g., "HTTP") |
| `adjective()`    | Hacker adjective                   |
| `noun()`         | Hacker noun                        |
| `verb()`         | Hacker verb                        |
| `ingverb()`      | Hacker gerund verb                 |

#### `faker.hipster()`

| Method   | Description  |
|----------|--------------|
| `word()` | Hipster word |

#### `faker.verb()`

| Method             | Description     |
|--------------------|-----------------|
| `base()`           | Base verb form  |
| `past()`           | Past tense      |
| `pastParticiple()` | Past participle |
| `simplePresent()`  | Simple present  |
| `ingForm()`        | -ing form       |

#### `faker.programmingLanguage()`

| Method      | Description               |
|-------------|---------------------------|
| `name()`    | Programming language name |
| `creator()` | Language creator name     |

#### `faker.shakespeare()`

| Method                  | Description            |
|-------------------------|------------------------|
| `hamletQuote()`         | Hamlet quote           |
| `asYouLikeItQuote()`    | As You Like It quote   |
| `kingRichardIIIQuote()` | King Richard III quote |
| `romeoAndJulietQuote()` | Romeo and Juliet quote |

#### `faker.yoda()`

| Method    | Description |
|-----------|-------------|
| `quote()` | Yoda quote  |

---

### 4.7 Numbers and Identifiers

#### `faker.number()`

| Method                                         | Description             |
|------------------------------------------------|-------------------------|
| `randomDigit()`                                | Single digit 0–9        |
| `randomDigits(int length)`                     | Array of random digits  |
| `randomDigitNotZero()`                         | Digit 1–9               |
| `positive()`                                   | Positive number         |
| `negative()`                                   | Negative number         |
| `numberBetween(int min, int max)`              | Random int in range     |
| `numberBetween(long min, long max)`            | Random long in range    |
| `numberBetween(double min, double max)`        | Random double in range  |
| `randomNumber()`                               | Random long             |
| `randomNumber(int digits)`                     | N-digit number          |
| `randomDouble(int decimals, int min, int max)` | Random double           |
| `digits(int count)`                            | String of random digits |
| `digit()`                                      | Single digit string     |

#### `faker.code()`

| Method                                   | Description             |
|------------------------------------------|-------------------------|
| `isbn10()` / `isbn10(boolean separator)` | Valid ISBN-10           |
| `isbn13()` / `isbn13(boolean separator)` | Valid ISBN-13           |
| `isbnGs1()`                              | GS1 prefix (978 or 979) |
| `isbnGroup()`                            | ISBN group number       |
| `isbnRegistrant()`                       | ISBN registrant element |
| `asin()`                                 | Amazon ASIN             |
| `imei()`                                 | Valid IMEI number       |
| `ean8()` / `gtin8()`                     | 8-digit EAN/GTIN        |
| `ean13()` / `gtin13()`                   | 13-digit EAN/GTIN       |

#### `faker.barcode()`

| Method     | Description          |
|------------|----------------------|
| `ean8()`   | 8-digit EAN barcode  |
| `ean13()`  | 13-digit EAN barcode |
| `gtin8()`  | 8-digit GTIN         |
| `gtin12()` | 12-digit GTIN        |
| `gtin13()` | 13-digit GTIN        |
| `gtin14()` | 14-digit GTIN        |
| `type()`   | Barcode type name    |

#### `faker.hashing()`

| Method     | Description         |
|------------|---------------------|
| `md2()`    | MD2 hash string     |
| `md5()`    | MD5 hash string     |
| `sha1()`   | SHA-1 hash string   |
| `sha256()` | SHA-256 hash string |
| `sha384()` | SHA-384 hash string |
| `sha512()` | SHA-512 hash string |

---

### 4.8 Date and Time

#### `faker.date()` (deprecated in favour of `faker.timeAndDate()`)

| Method                                     | Description                     |
|--------------------------------------------|---------------------------------|
| `future(int, TimeUnit)`                    | Future date from now            |
| `future(int, TimeUnit, String pattern)`    | Future date as formatted string |
| `future(int, int, TimeUnit)`               | Future date with minimum gap    |
| `past(int, TimeUnit)`                      | Past date from now              |
| `past(int, TimeUnit, String pattern)`      | Past date as formatted string   |
| `between(T from, T to)`                    | Date in range                   |
| `between(T from, T to, String pattern)`    | Formatted date in range         |
| `birthday()`                               | Birthday (18–65 years ago)      |
| `birthdayLocalDate()`                      | Birthday as `LocalDate`         |
| `birthday(int minAge, int maxAge)`         | Birthday in age range           |
| `birthday(int, int, String pattern)`       | Formatted birthday              |
| `duration(long max, ChronoUnit)`           | Random `Duration`               |
| `duration(long min, long max, ChronoUnit)` | Duration in range               |
| `period(Period min, Period max)`           | Random `Period`                 |

---

### 4.9 Communication

#### `faker.phoneNumber()`

| Method                         | Description                     |
|--------------------------------|---------------------------------|
| `phoneNumber()`                | National format phone number    |
| `phoneNumberNational()`        | Fixed-line national format      |
| `phoneNumberInternational()`   | Fixed-line international format |
| `cellPhone()`                  | Mobile national format          |
| `cellPhoneInternational()`     | Mobile international format     |
| `extension()`                  | Subscriber number alias         |
| `subscriberNumber()`           | 4-digit subscriber number       |
| `subscriberNumber(int length)` | N-digit subscriber number       |

#### `faker.passport()`

| Method    | Description           |
|-----------|-----------------------|
| `valid()` | Valid passport number |

#### `faker.drivingLicense()`

| Method                                     | Description                 |
|--------------------------------------------|-----------------------------|
| `drivingLicense(String stateAbbreviation)` | US driving licence by state |

---

### 4.10 Colour

#### `faker.color()`

| Method                         | Description                    |
|--------------------------------|--------------------------------|
| `name()`                       | Colour name                    |
| `hex()`                        | Hex colour with `#` prefix     |
| `hex(boolean includeHashSign)` | Hex colour optionally with `#` |

---

### 4.11 Animals and Creatures

#### `faker.animal()`

| Method             | Description                       |
|--------------------|-----------------------------------|
| `name()`           | Animal name                       |
| `scientificName()` | Scientific name (genus + species) |
| `genus()`          | Genus                             |
| `species()`        | Species                           |

#### `faker.cat()`

| Method       | Description  |
|--------------|--------------|
| `name()`     | Cat name     |
| `breed()`    | Cat breed    |
| `registry()` | Cat registry |

#### `faker.dog()`

| Method         | Description     |
|----------------|-----------------|
| `name()`       | Dog name        |
| `breed()`      | Dog breed       |
| `sound()`      | Dog sound       |
| `memePhrase()` | Dog meme phrase |
| `age()`        | Dog age         |
| `coatLength()` | Coat length     |
| `gender()`     | Dog gender      |
| `size()`       | Dog size        |

#### `faker.horse()`

| Method    | Description |
|-----------|-------------|
| `name()`  | Horse name  |
| `breed()` | Horse breed |

---

### 4.12 Science and Education

#### `faker.science()`

| Method            | Description      |
|-------------------|------------------|
| `element()`       | Chemical element |
| `elementSymbol()` | Element symbol   |
| `unit()`          | Scientific unit  |
| `scientist()`     | Scientist's name |
| `tool()`          | Scientific tool  |
| `quark()`         | Quark type       |
| `leptons()`       | Lepton type      |
| `bosons()`        | Boson type       |

#### `faker.educator()`

| Method                | Description                |
|-----------------------|----------------------------|
| `university()`        | University name            |
| `course()`            | Course description         |
| `subjectWithNumber()` | Subject with course number |
| `secondarySchool()`   | Secondary school name      |
| `campus()`            | Campus name                |

#### `faker.university()`

| Method     | Description         |
|------------|---------------------|
| `name()`   | University name     |
| `degree()` | Degree type         |
| `prefix()` | University prefix   |
| `suffix()` | University suffix   |
| `place()`  | University location |

#### `faker.ancient()`

| Method         | Description       |
|----------------|-------------------|
| `god()`        | Ancient god name  |
| `primordial()` | Primordial deity  |
| `titan()`      | Titan name        |
| `hero()`       | Ancient hero name |

---

### 4.13 Music and Arts

#### `faker.music()`

| Method         | Description                   |
|----------------|-------------------------------|
| `instrument()` | Musical instrument            |
| `key()`        | Musical key (C, D#, Eb, etc.) |
| `chord()`      | Chord (key + chord type)      |
| `genre()`      | Music genre                   |

#### `faker.book()`

| Method        | Description    |
|---------------|----------------|
| `author()`    | Author name    |
| `title()`     | Book title     |
| `publisher()` | Publisher name |
| `genre()`     | Book genre     |

---

### 4.14 Transport and Travel

#### `faker.vehicle()`

| Method                         | Description                   |
|--------------------------------|-------------------------------|
| `vin()`                        | Vehicle Identification Number |
| `manufacturer()`               | Vehicle manufacturer          |
| `make()`                       | Vehicle make                  |
| `model()`                      | Vehicle model                 |
| `model(String make)`           | Model for specific make       |
| `makeAndModel()`               | Make + model combined         |
| `style()`                      | Vehicle style                 |
| `color()`                      | Vehicle colour                |
| `upholsteryColor()`            | Upholstery colour             |
| `upholsteryFabric()`           | Upholstery fabric             |
| `upholstery()`                 | Upholstery description        |
| `transmission()`               | Transmission type             |
| `driveType()`                  | Drive type (AWD, FWD, etc.)   |
| `fuelType()`                   | Fuel type                     |
| `carType()`                    | Car category                  |
| `engine()`                     | Engine description            |
| `carOptions()`                 | List of options (5–10)        |
| `carOptions(int min, int max)` | List of options in range      |
| `standardSpecs()`              | Standard specs list           |
| `doors()`                      | Number of doors               |
| `licensePlate()`               | Generic licence plate         |
| `licensePlate(String state)`   | State-specific US plate       |

#### `faker.aviation()`

| Method                      | Description                   |
|-----------------------------|-------------------------------|
| `aircraft()`                | Aircraft type                 |
| `airplane()`                | Airplane designation          |
| `warplane()`                | Warplane designation          |
| `cargo()`                   | Cargo aircraft                |
| `armyHelicopter()`          | Army helicopter               |
| `civilHelicopter()`         | Civil helicopter              |
| `airport()`                 | Airport ICAO code             |
| `airport(AviationCodeType)` | IATA or ICAO code             |
| `airportName()`             | Airport name                  |
| `METAR()`                   | METAR weather report          |
| `manufacturer()`            | Aviation manufacturer         |
| `engineType()`              | Engine type                   |
| `flight()`                  | Flight number (IATA format)   |
| `flight(AviationCodeType)`  | Flight in IATA or ICAO format |
| `flightStatus()`            | Flight status                 |
| `gate()`                    | Airport gate identifier       |
| `airline()`                 | Airline name                  |

---

### 4.15 Healthcare

#### `faker.disease()` (package: `healthcare`)

| Method                                  | Description                 |
|-----------------------------------------|-----------------------------|
| `icd10()`                               | ICD-10 code                 |
| `anyDisease()`                          | Random disease              |
| `internalDisease()`                     | Internal medicine condition |
| `neurology()`                           | Neurological condition      |
| `surgery()`                             | Surgical disease            |
| `paediatrics()`                         | Pediatric disease           |
| `gynecologyAndObstetrics()`             | OB/GYN condition            |
| `ophthalmologyAndOtorhinolaryngology()` | Eye/ENT disease             |
| `dermatology()`                         | Skin disease                |

#### `faker.medication()` (package: `healthcare`)

Generates medication names and related data.

#### `faker.medicalProcedure()` (package: `healthcare`)

Generates medical procedure names.

#### `faker.observation()` (package: `healthcare`)

Generates clinical observation data.

#### `faker.careProvider()` (package: `healthcare`)

Generates care provider information.

#### `faker.medical()` (base, deprecated since 2.3.0)

| Method                | Description           |
|-----------------------|-----------------------|
| `medicineName()`      | Medicine name         |
| `diseaseName()`       | Disease name          |
| `hospitalName()`      | Hospital name         |
| `symptoms()`          | Symptom description   |
| `diagnosisCode()`     | ICD-10 diagnosis code |
| `procedureCode()`     | ICD-10 procedure code |
| `medicalProfession()` | Medical profession    |

---

### 4.16 Military

#### `faker.military()`

| Method           | Description    |
|------------------|----------------|
| `armyRank()`     | Army rank      |
| `marinesRank()`  | Marines rank   |
| `navyRank()`     | Navy rank      |
| `airForceRank()` | Air Force rank |
| `dodPaygrade()`  | DoD pay grade  |

---

### 4.17 Sports

#### `faker.formula1()` (package: `sport`)

| Method        | Description           |
|---------------|-----------------------|
| `driver()`    | F1 driver name        |
| `team()`      | F1 team name          |
| `circuit()`   | F1 circuit name       |
| `grandPrix()` | Grand Prix event name |

#### `faker.basketball()` (package: `sport`)

| Method        | Description      |
|---------------|------------------|
| `teams()`     | Basketball team  |
| `coaches()`   | Basketball coach |
| `positions()` | Player position  |
| `players()`   | Player name      |

#### `faker.baseball()` (package: `sport`)

Baseball-related data.

#### `faker.football()` (package: `sport`)

Association football (soccer) data.

#### `faker.englandFootBall()` (package: `sport`)

English football-specific data.

#### `faker.cricket()` (package: `sport`)

Cricket player and team data.

#### `faker.chess()` (package: `sport`)

Chess terms, openings, and player data.

#### `faker.martialArt()` (package: `sport`)

Martial arts style and practitioner data.

#### `faker.volleyball()` (package: `sport`)

Volleyball team and player data.

---

### 4.18 Food and Drink

#### `faker.food()` (package: `food`)

| Method          | Description               |
|-----------------|---------------------------|
| `ingredient()`  | Food ingredient           |
| `allergen()`    | Allergen                  |
| `spice()`       | Spice name                |
| `dish()`        | Dish name                 |
| `fruit()`       | Fruit name                |
| `vegetable()`   | Vegetable name            |
| `sushi()`       | Sushi type                |
| `measurement()` | Measurement (size + unit) |

#### `faker.beer()` (package: `food`)

| Method    | Description |
|-----------|-------------|
| `brand()` | Beer brand  |
| `name()`  | Beer name   |
| `style()` | Beer style  |
| `hop()`   | Hop variety |
| `yeast()` | Yeast type  |
| `malt()`  | Malt type   |

#### `faker.dessert()` (package: `food`)

| Method      | Description     |
|-------------|-----------------|
| `variety()` | Dessert variety |
| `topping()` | Dessert topping |
| `flavor()`  | Dessert flavour |

#### `faker.coffee()`, `faker.iceCream()`, `faker.tea()`, `faker.apple()`

Specialised food providers with relevant attributes.

---

### 4.19 Home and Environment

#### `faker.house()`

| Method        | Description    |
|---------------|----------------|
| `furniture()` | Furniture item |
| `room()`      | Room name      |

#### `faker.weather()`

| Method                                    | Description                              |
|-------------------------------------------|------------------------------------------|
| `description()`                           | Short weather description                |
| `temperatureCelsius()`                    | Temperature in Celsius (-30 to 38°C)     |
| `temperatureFahrenheit()`                 | Temperature in Fahrenheit (-22 to 100°F) |
| `temperatureCelsius(int min, int max)`    | Custom Celsius range                     |
| `temperatureFahrenheit(int min, int max)` | Custom Fahrenheit range                  |

---

### 4.20 Technology

#### `faker.hashing()`

See section 4.7 — SHA and MD hash generators.

#### `faker.device()`

| Method           | Description         |
|------------------|---------------------|
| `modelName()`    | Device model name   |
| `platform()`     | Operating platform  |
| `manufacturer()` | Device manufacturer |
| `serial()`       | Serial number       |

#### `faker.app()`

| Method      | Description      |
|-------------|------------------|
| `name()`    | Application name |
| `version()` | Version string   |
| `author()`  | Author name      |

---

### 4.21 Entertainment — TV and Film

All entertainment providers are in the `net.datafaker.providers.entertainment` package.

#### `faker.harryPotter()`

`character()`, `location()`, `quote()`, `book()`, `house()`, `spell()`

#### `faker.starTrek()`

`character()`, `location()`, `species()`, `villain()`, `klingon()`, `starship()`

#### `faker.gameOfThrones()`

`character()`, `house()`, `city()`, `dragon()`, `quote()`

#### `faker.rickAndMorty()`

`character()`, `location()`, `quote()`

#### `faker.hitchhikersGuideToTheGalaxy()`

`character()`, `location()`, `marvinQuote()`, `planet()`, `quote()`, `species()`, `starship()`

#### `faker.friends()`

`character()`, `location()`, `quote()`

#### `faker.chuckNorris()`

`fact()`

#### Other TV/Film providers

| Provider                         | Key methods                                        |
|----------------------------------|----------------------------------------------------|
| `faker.avatar()`                 | Characters, quotes from Avatar: The Last Airbender |
| `faker.backToTheFuture()`        | Characters, dates, quotes                          |
| `faker.bigBangTheory()`          | Characters, quotes                                 |
| `faker.breakingBad()`            | Characters, episodes                               |
| `faker.brooklynNineNine()`       | Characters, quotes                                 |
| `faker.buffy()`                  | Characters, quotes, villains                       |
| `faker.cowboyBebop()`            | Characters, episodes, songs                        |
| `faker.doctorWho()`              | Characters, quotes, species                        |
| `faker.dumbAndDumber()`          | Actors, characters, quotes                         |
| `faker.dune()`                   | Characters, planets, quotes                        |
| `faker.familyGuy()`              | Characters, locations, quotes                      |
| `faker.futurama()`               | Characters, hermes, locations, quotes              |
| `faker.ghostbusters()`           | Characters, quotes                                 |
| `faker.gravityFalls()`           | Characters, quotes                                 |
| `faker.howIMetYourMother()`      | Characters, catch phrases                          |
| `faker.kaamelott()`              | Characters, quotes                                 |
| `faker.lebowski()`               | Actors, characters, quotes                         |
| `faker.lordOfTheRings()`         | Characters, locations, quotes                      |
| `faker.moneyHeist()`             | Characters, heists, quotes                         |
| `faker.newGirl()`                | Characters, quotes                                 |
| `faker.oscarMovie()`             | Movies, actors                                     |
| `faker.princessBride()`          | Characters, quotes                                 |
| `faker.residentEvil()`           | Characters, locations                              |
| `faker.seinfeld()`               | Characters, quotes                                 |
| `faker.severance()`              | Characters, departments                            |
| `faker.siliconValley()`          | Characters, companies, quotes                      |
| `faker.simpsons()`               | Characters, locations, quotes                      |
| `faker.southPark()`              | Characters, episodes                               |
| `faker.spongebob()`              | Characters, episodes, quotes                       |
| `faker.stargate()`               | Characters, planets, teams                         |
| `faker.strangerThings()`         | Characters, quotes                                 |
| `faker.suits()`                  | Characters, quotes                                 |
| `faker.supernatural()`           | Characters, monsters                               |
| `faker.theExpanse()`             | Characters, locations                              |
| `faker.theItCrowd()`             | Characters, quotes                                 |
| `faker.theKingkillerChronicle()` | Characters, locations, quotes                      |
| `faker.tronLegacy()`             | Characters, quotes                                 |
| `faker.twinPeaks()`              | Characters, locations, quotes                      |
| `faker.witcher()`                | Characters, locations, monsters, quotes            |
| `faker.bluey()`                  | Characters, locations                              |
| `faker.babylon5()`               | Characters, quotes                                 |
| `faker.finalSpace()`             | Characters, quotes                                 |
| `faker.freshPrinceOfBelAir()`    | Characters, quotes                                 |
| `faker.heyArnold()`              | Characters, quotes                                 |
| `faker.hobbit()`                 | Characters, locations, quotes                      |
| `faker.howToTrainYourDragon()`   | Characters, dragons                                |
| `faker.starWars()`               | Characters, planets, quotes, species               |

---

### 4.22 Entertainment — Anime and Manga

| Provider                     | Key methods                                |
|------------------------------|--------------------------------------------|
| `faker.pokemon()`            | `name()`, `location()`, `move()`, `type()` |
| `faker.dragonBall()`         | Characters                                 |
| `faker.naruto()`             | Characters, villages                       |
| `faker.onePiece()`           | Characters, islands, quotes                |
| `faker.fullmetalAlchemist()` | Characters, locations                      |
| `faker.swordArtOnline()`     | Characters, locations                      |
| `faker.doraemon()`           | Characters                                 |
| `faker.detectiveConan()`     | Characters                                 |

---

### 4.23 Entertainment — Video Games

All video game providers are in `net.datafaker.providers.videogame`.

| Provider                     | Key methods                                                                                   |
|------------------------------|-----------------------------------------------------------------------------------------------|
| `faker.zelda()`              | `game()`, `character()`                                                                       |
| `faker.minecraft()`          | `itemName()`, `tileName()`, `entityName()`, `monsterName()`, `animalName()`, `tileItemName()` |
| `faker.leagueOfLegends()`    | Champions, items, locations                                                                   |
| `faker.overwatch()`          | Heroes, locations, quotes                                                                     |
| `faker.hearthstone()`        | Cards, heroes                                                                                 |
| `faker.elderScrolls()`       | Cities, creatures, dragons, factions                                                          |
| `faker.fallout()`            | Characters, factions                                                                          |
| `faker.massEffect()`         | Characters, locations, species                                                                |
| `faker.worldOfWarcraft()`    | Heroes, races                                                                                 |
| `faker.finalFantasyXIV()`    | Characters, jobs, data centres                                                                |
| `faker.starCraft()`          | Units, planets                                                                                |
| `faker.streetFighter()`      | Characters, stages                                                                            |
| `faker.superMario()`         | Characters, games, locations                                                                  |
| `faker.superSmashBros()`     | Characters, stages                                                                            |
| `faker.dota2()`              | Heroes, items, teams                                                                          |
| `faker.eldenRing()`          | Characters, locations                                                                         |
| `faker.darkSouls()`          | Characters, shields, covenants                                                                |
| `faker.redDeadRedemption2()` | Characters, locations                                                                         |
| `faker.halfLife()`           | Characters                                                                                    |
| `faker.heroesOfTheStorm()`   | Heroes                                                                                        |
| `faker.warhammerFantasy()`   | Creatures, guilds, heroes                                                                     |
| `faker.minecraft()`          | Items, entities, mobs                                                                         |
| `faker.clashOfClans()`       | Troops, heroes                                                                                |
| `faker.myst()`               | Ages, characters                                                                              |
| `faker.sonicTheHedgehog()`   | Characters, zones                                                                             |
| `faker.touhou()`             | Characters                                                                                    |
| `faker.control()`            | Characters, locations                                                                         |
| `faker.esports()`            | Games, players, teams                                                                         |
| `faker.marvelSnap()`         | Cards                                                                                         |
| `faker.soulKnight()`         | Characters                                                                                    |
| `faker.battlefield1()`       | Ranks, weapons                                                                                |

---

### 4.24 Miscellaneous Base Providers

#### `faker.bool()`

| Method   | Description      |
|----------|------------------|
| `bool()` | Random `boolean` |

#### `faker.options()`

| Method                           | Description                 |
|----------------------------------|-----------------------------|
| `option(T... options)`           | Random element from varargs |
| `option(Class<E> enum)`          | Random enum constant        |
| `option(int[] options)`          | Random int from array       |
| `option(long[] options)`         | Random long from array      |
| `option(double[] options)`       | Random double from array    |
| `option(String... options)`      | Random string from varargs  |
| `subset(int size, T... options)` | Unique subset of size N     |
| `nextElement(E[] array)`         | Random element from array   |
| `nextElement(List<E> list)`      | Random element from list    |

#### `faker.text()`

Custom text generation with `TextSymbolsBuilder`:

```java
String password = faker.text().text(
    Text.TextSymbolsBuilder.builder()
                           .len(12)
                           .with(Text.EN_LOWERCASE, 2)
                           .with(Text.EN_UPPERCASE, 2)
                           .with(Text.DIGITS, 2)
                           .with(Text.DEFAULT_SPECIAL, 1)
                           .build()
);
```

#### `faker.emoji()`

| Method      | Description   |
|-------------|---------------|
| `smiley()`  | Smiley emoji  |
| `cat()`     | Cat emoji     |
| `vehicle()` | Vehicle emoji |

#### `faker.space()`

| Method                  | Description               |
|-------------------------|---------------------------|
| `planet()`              | Planet name               |
| `moon()`                | Moon name                 |
| `galaxy()`              | Galaxy name               |
| `nebula()`              | Nebula name               |
| `starCluster()`         | Star cluster name         |
| `constellation()`       | Constellation name        |
| `star()`                | Star name                 |
| `agency()`              | Space agency name         |
| `agencyAbbreviation()`  | Space agency abbreviation |
| `nasaSpaceCraft()`      | NASA spacecraft name      |
| `company()`             | Space-related company     |
| `distanceMeasurement()` | Distance with unit        |
| `meteorite()`           | Meteorite name            |

#### `faker.superhero()`

| Method         | Description          |
|----------------|----------------------|
| `name()`       | Superhero name       |
| `prefix()`     | Superhero prefix     |
| `suffix()`     | Superhero suffix     |
| `power()`      | Superpower           |
| `descriptor()` | Superhero descriptor |

#### `faker.brand()`

| Method    | Description  |
|-----------|--------------|
| `sport()` | Sports brand |
| `car()`   | Car brand    |
| `watch()` | Watch brand  |

#### `faker.team()`

| Method       | Description |
|--------------|-------------|
| `name()`     | Team name   |
| `creature()` | Team mascot |
| `state()`    | State       |
| `sport()`    | Sport       |

---

## 5. Sequence Generators (Collections and Streams)

### 5.1 Collections

```java
// Generate a list of 5 unique names
List<String> names = faker.collection(
                              () -> faker.name().fullName())
                          .len(5)
                          .generate();

// Generate between 3 and 10 items
List<String> cities = faker.collection(
                               () -> faker.address().city())
                           .minLen(3)
                           .maxLen(10)
                           .generate();

// Unique values only
List<String> uniqueNames = faker.collection(
                                    () -> faker.name().firstName())
                                .len(10)
                                .nullRate(0.1)       // 10% nulls
                                .generate();
```

### 5.2 Streams

```java
// Infinite stream — take what you need
faker.stream(() ->faker.

name().

fullName())
    .

limit(100)
    .

generate()
    .

forEach(System.out::println);

// Bounded stream
Stream<String> emailStream = faker.stream(
                                      () -> faker.internet().emailAddress())
                                  .minLen(5)
                                  .maxLen(20)
                                  .generate();
```

### 5.3 Structured Output

DataFaker supports generating structured data using a `Schema` with `Supplier` lambdas:

```java
// CSV output
Schema<Object, String> schema = Schema.of(
        field("id", () -> faker.number().digits(4)),
        field("name", () -> faker.name().fullName()),
        field("email", () -> faker.internet().emailAddress())
    );
String csv = Format.toCsv(schema)
                   .headers()
                   .separator(",")
                   .rows(10)
                   .generate();

// JSON output
String json = Format.toJson(schema).rows(5).generate();

// YAML output
String yaml = Format.toYaml(schema).rows(5).generate();

// XML output
String xml = Format.toXml(schema).rows(5).generate();
```

### 5.4 Unique Value Generation

```java
// Values will never repeat across calls
faker.unique().

fetchFromYaml("name.first_name",Locale.ENGLISH);

// Or scoped to a specific faker
Faker uniqueFaker = new Faker();
String unique1 = uniqueFaker.unique().fetchFromYaml("color.name", Locale.ENGLISH);
String unique2 = uniqueFaker.unique().fetchFromYaml("color.name", Locale.ENGLISH);
// unique1 != unique2
```

---

## 6. Faker Expressions (YAML-based)

DataFaker uses YAML configuration files to store data. The `expression()` method and YAML templates allow composing complex values.

### 6.1 Expression Format

```
#{ProviderClass.method}
#{ProviderClass.method 'arg1'}
#{ProviderClass.method 'arg1','arg2'}
```

### 6.2 Examples

```java
faker.expression("#{Name.fullName}")
// "John Smith"

faker.

expression("#{Address.streetAddress}")
// "123 Main St"

faker.

expression("#{numerify '###-####'}")
// "492-3718"

faker.

expression("#{letterify '???-???'}")
// "kxm-pty"

faker.

expression("#{regexify '[A-Z]{2}[0-9]{4}'}")
// "KX2914"

faker.

expression("#{bothify '##??##'}")
// "27kq91"

faker.

expression("#{options.option 'red','green','blue'}")
// "green"

faker.

expression("#{Internet.emailAddress 'john.doe'}")
// "john.doe@yahoo.com"

faker.

expression("#{date.birthday 'yy','MM','dd'}")
// "85,03,21"
```

### 6.3 Custom YAML Data Files

Create a YAML file following DataFaker's format:

```yaml
en:
  faker:
    my_domain:
      product_names:
        - "Widget Pro"
        - "Gadget Elite"
        - "Doohickey Plus"
```

Register and use:

```java
faker.addPath(Locale.ENGLISH, Path.of("/path/to/my-data.yml"));
String product = faker.expression("#{my_domain.product_names}");
```

---

## 7. Custom Providers

### 7.1 Creating a Custom Provider

```java
// 1. Create a provider class
public class MyCompanyProvider extends AbstractProvider<BaseProviders> {

    protected MyCompanyProvider(BaseProviders faker) {
        super(faker);
    }

    public String productCode() {
        return faker.regexify("[A-Z]{3}-[0-9]{6}");
    }

    public String internalId() {
        return "ID-" + faker.number().digits(8);
    }
}
```

### 7.2 Creating a Custom Faker

```java
// 2. Create a custom Faker aggregating your providers
public class MyFaker extends BaseFaker {

    public MyCompanyProvider myCompany() {
        return getProvider(MyCompanyProvider.class, () -> new MyCompanyProvider(this));
    }
}


// 3. Use it
MyFaker myFaker = new MyFaker();
String code = myFaker.myCompany().productCode();  // "XYZ-491827"
String id = myFaker.myCompany().internalId();   // "ID-83726491"
```

### 7.3 Custom Faker with YAML

Add a YAML file and access via `resolve()` in your provider:

```java
public String productCategory() {
    return resolve("my_company.categories"); // reads from custom YAML
}
```

---

## 8. Comparison: krandom vs DataFaker

### 8.1 What krandom Has

| krandom Feature                                                                                      | Class / Location                                 | Notes                                    |
|------------------------------------------------------------------------------------------------------|--------------------------------------------------|------------------------------------------|
| Primitive generators: `byte`, `short`, `int`, `long`, `float`, `double`, `char`, `boolean`, `String` | `Generators.of*()` in Java; `KRandom*` in Kotlin | Bounded variants with min/max/seed       |
| Enum generator                                                                                       | `EnumGenerator`                                  | Random enum constant                     |
| IPv4 generator                                                                                       | `IPv4Generator`                                  | RFC 791, unicast only                    |
| IPv6 generator                                                                                       | `IPv6Generator`                                  | RFC 4291/5952                            |
| Coin flip                                                                                            | `CoinGenerator`                                  | HEAD / TAIL                              |
| Dice rolls (D4, D6, D8, D10, D12, D20, D100)                                                         | `DiceGenerator` + `DiceType`                     | Full polyhedral set                      |
| Fibonacci number generation                                                                          | `FibonacciGenerator`                             | Random Fibonacci numbers                 |
| Luhn-valid 10-digit numbers                                                                          | `LuhnGenerator`                                  | Credit card payload                      |
| Natural number generator                                                                             | `NaturalNumberGenerator`                         | Prime, composite, natural                |
| Hex hash generator                                                                                   | `HexHashGenerator`                               | Arbitrary-length hex strings             |
| User data: first name, surname, gender, title, age, birthday, email, SSN, username                   | `user/` package                                  | US-centric; CSV-backed                   |
| Object population                                                                                    | `ObjectGenerator`                                | Fills POJO fields with random primitives |
| Properties / config                                                                                  | `Properties.kt`                                  | Runtime config                           |
| Validators: IPv4, IPv6, email, SSN                                                                   | `validators/`                                    | Validation-only classes                  |

### 8.2 What DataFaker Has That krandom Lacks

The table below highlights high-value DataFaker capabilities absent from krandom, grouped by priority for potential implementation.

#### High Priority — Commonly Needed

| DataFaker Feature             | Provider      | Method Examples                                                                                 |
|-------------------------------|---------------|-------------------------------------------------------------------------------------------------|
| Full address generation       | `Address`     | `streetAddress()`, `city()`, `state()`, `zipCode()`, `country()`, `latitude()`, `longitude()`   |
| Internet / URLs               | `Internet`    | `emailAddress()`, `url()`, `domainName()`, `uuid()`, `uuidv7()`, `macAddress()`, `httpMethod()` |
| Company names                 | `Company`     | `name()`, `industry()`, `catchPhrase()`, `bs()`, `url()`                                        |
| Lorem Ipsum                   | `Lorem`       | `word()`, `sentence()`, `paragraph()`, `paragraphs(n)`                                          |
| Random numbers with control   | `Number`      | `numberBetween()`, `randomDouble()`, `digits()`, `randomDigitNotZero()`                         |
| Date and time                 | `DateAndTime` | `future()`, `past()`, `between()`, `birthday()`, `duration()`                                   |
| Phone numbers                 | `PhoneNumber` | `phoneNumber()`, `cellPhone()`, `phoneNumberInternational()`                                    |
| Colour                        | `Color`       | `name()`, `hex()`                                                                               |
| Commerce                      | `Commerce`    | `productName()`, `department()`, `price()`, `brand()`                                           |
| Job                           | `Job`         | `title()`, `field()`, `seniority()`, `position()`                                               |
| Credit card (Luhn-valid full) | `Finance`     | `creditCard()`, `iban()`, `bic()`, `usRoutingNumber()`                                          |
| Options / selection           | `Options`     | `option(T... values)`, `option(Class<E> enum)`, `subset()`                                      |
| Bool                          | `Bool`        | `bool()`                                                                                        |

#### Medium Priority — Useful for Richer Test Data

| DataFaker Feature    | Provider                | Method Examples                                                           |
|----------------------|-------------------------|---------------------------------------------------------------------------|
| Name extensions      | `Name`                  | `prefix()`, `suffix()`, `title()`, `femaleFirstName()`, `maleFirstName()` |
| Country              | `Country`               | `countryCode2()`, `countryCode3()`, `capital()`                           |
| Nation               | `Nation`                | `nationality()`, `language()`, `isoLanguage()`, `isoCountry()`            |
| Currency / Money     | `Money`                 | `currency()`, `currencyCode()`, `currencySymbol()`                        |
| Barcode              | `Barcode`               | `ean8()`, `ean13()`, `gtin14()`                                           |
| Code / ISBN / IMEI   | `Code`                  | `isbn10()`, `isbn13()`, `imei()`, `asin()`, `ean8()`, `ean13()`           |
| Hashing (SHA, MD5)   | `Hashing`               | `md5()`, `sha256()`, `sha512()`                                           |
| Weather              | `Weather`               | `description()`, `temperatureCelsius()`, `temperatureFahrenheit()`        |
| Music                | `Music`                 | `instrument()`, `genre()`, `key()`, `chord()`                             |
| Book                 | `Book`                  | `author()`, `title()`, `publisher()`, `genre()`                           |
| Animal               | `Animal`                | `name()`, `scientificName()`                                              |
| Vehicle              | `Vehicle`               | `vin()`, `make()`, `model()`, `licensePlate()`                            |
| Space                | `Space`                 | `planet()`, `star()`, `galaxy()`, `agency()`                              |
| Science              | `Science`               | `element()`, `elementSymbol()`, `scientist()`                             |
| University           | `University`            | `name()`, `degree()`                                                      |
| Military             | `Military`              | `armyRank()`, `navyRank()`, `airForceRank()`                              |
| Medical (healthcare) | `Disease`, `Medication` | ICD-10 codes, disease names, medication names                             |

#### Lower Priority — Domain-Specific / Niche

| DataFaker Feature                                            | Notes                                                  |
|--------------------------------------------------------------|--------------------------------------------------------|
| Entertainment providers (Harry Potter, Star Trek, GoT, etc.) | Character/quote/location generation for 70+ franchises |
| Sports providers (Formula 1, Basketball, Football, etc.)     | Team/player/driver data                                |
| Food providers (Beer, Food, Dessert, Coffee, Tea)            | Ingredient/dish/drink data                             |
| Video game providers (Zelda, Minecraft, LoL, etc.)           | Character/item/map data for 30+ games                  |
| Superhero                                                    | Name/power/descriptor                                  |
| Avatar / Emoji                                               | Emoji categories                                       |
| Hipster / FunnyName                                          | Novelty generators                                     |
| Chuck Norris facts                                           | `ChuckNorris.fact()`                                   |
| Demographic                                                  | Race, marital status, education level                  |
| Relationship                                                 | Family relationship types                              |
| House                                                        | Room and furniture names                               |
| Brand                                                        | Sport, car, watch brands                               |
| App                                                          | Application name, version, author                      |
| Device                                                       | Model, platform, manufacturer, serial                  |
| Subscription                                                 | Plans, statuses, payment methods                       |
| Passport / DrivingLicense                                    | Document number generation                             |
| Stock                                                        | Ticker symbols for NASDAQ/NYSE/NSE/LSE                 |
| Programming language                                         | Language name and creator                              |
| Team                                                         | Sports team name, mascot, state                        |
| Compass                                                      | Direction, abbreviation, azimuth                       |
| Gender (extended)                                            | Types, binary types, short binary types                |
| Verb                                                         | Base, past, pastParticiple, ingForm                    |
| Ancient mythology                                            | Gods, titans, heroes                                   |
| Aviation                                                     | Aircraft, airports, flights, airlines, METAR           |
| Pet providers (Cat, Dog, Horse)                              | Breed, name, sound                                     |
| Locale utilities                                             | `Locality` provider for locale listing                 |

### 8.3 Architectural Differences

| Dimension              | krandom                                           | DataFaker                                                |
|------------------------|---------------------------------------------------|----------------------------------------------------------|
| Entry point            | `Generators` (static factory); `KRandom*` objects | `new Faker()` instance                                   |
| Provider access        | Direct instantiation of generator classes         | `faker.provider()` accessor methods                      |
| Data source            | CSV files and hardcoded logic                     | YAML files (100+ locale files)                           |
| Locale support         | US-centric (names, SSN)                           | 70 advertised locale tags, locale-aware interpolation    |
| Seeding                | Per-generator seed parameter                      | Single seed on `Faker` constructor                       |
| Bulk generation        | `generator.generateList(n)`                       | `faker.collection()`, `faker.stream()`                   |
| Output formats         | Raw values only                                   | CSV, JSON, YAML, XML via `Schema` API                    |
| Custom providers       | Subclass pattern                                  | `AbstractProvider` + custom `BaseFaker` subclass         |
| Expression engine      | None                                              | `faker.expression("#{Provider.method}")`                 |
| Object population      | `ObjectGenerator` (fills primitive fields)        | `faker.populate(MyClass.class)` with `@Fake` annotations |
| Number theory          | Prime, composite, Fibonacci, Luhn, natural        | `Number`, `Luhn` (via Finance) only                      |
| Uniqueness enforcement | Not built-in                                      | `faker.unique()`                                         |

### 8.4 Recommended Next Implementations for krandom

Based on the gap analysis, the following are the highest-value additions for krandom (in priority order):

1. **Address generator** — street, city, state, country, postcode, lat/lon
2. **Lorem Ipsum** — words, sentences, paragraphs
3. **Internet / URL** — email (improvements), URL, UUID (v3/v7), MAC address, domain
4. **Commerce** — product name, price, department, brand
5. **Company** — name, industry, buzzword, catch phrase
6. **Date/Time** — past, future, between, birthday as structured LocalDate
7. **Number utilities** — `numberBetween` for double/long, `randomDigitNotZero`, `digits(n)`
8. **Colour** — hex colour code generator
9. **Locale-aware name generation** — female/male first name, prefix, suffix
10. **Options selector** — `option(T... values)`, `option(Class<Enum>)`, subset
11. **Finance** — full IBAN, BIC, routing number; credit card types beyond Luhn payload
12. **Currency / Money** — ISO 4217 name, code, symbol
13. **Hashing** — named hash algorithms (MD5, SHA-256, SHA-512) as structured output
14. **Barcode / Code** — ISBN-10, ISBN-13, IMEI, EAN-8, EAN-13
15. **Faker expression engine** — YAML-driven `expression(String)` API
