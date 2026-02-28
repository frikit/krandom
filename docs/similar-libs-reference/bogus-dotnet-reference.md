# Bogus (.NET) — Comprehensive Reference

**Repository:** https://github.com/bchavez/Bogus
**Author:** Brian Chavez
**License:** MIT
**Language:** C# (.NET Standard 1.3+, .NET Framework 4.0+)
**Purpose:** reference for deciding which krandom features to implement next.

---

## 1. Purpose and Overview

**Bogus** is a C# port of the JavaScript `faker.js` library. It generates realistic fake data for populating databases, UIs, and test fixtures in .NET applications.

Key design decisions:

- **Fluent API** inspired by FluentValidation — rules declared as `.RuleFor()` chains.
- **DataSet providers** mirror faker.js categories (Address, Finance, Internet, …).
- **Deterministic output** via seeding — critical for stable test assertions.
- **70+ locales** with transparent fallback to English.

Notable users: Bitwarden Passwordless Server, Elasticsearch .NET, FluentValidation, Microsoft Orleans.

---

## 2. Installation

```
dotnet add package Bogus
```

---

## 3. Core API

### 3.1 `Faker<T>` class

```csharp
var userId = 1;
var faker = new Faker<User>()
    .CustomInstantiator(f => new User { Id = userId++ })
    .RuleFor(u => u.FirstName, f => f.Name.FirstName())
    .RuleFor(u => u.LastName,  f => f.Name.LastName())
    .RuleFor(u => u.Email,     (f, u) => f.Internet.Email(u.FirstName, u.LastName));
```

The two-argument `RuleFor` overload receives both the `Faker` facade (`f`) and the partially-built object (`u`), enabling cross-property dependencies.

#### Key `Faker<T>` methods

| Method                            | Description                                                     |
|-----------------------------------|-----------------------------------------------------------------|
| `RuleFor(expr, func)`             | Assign a generation rule to one property                        |
| `RuleForType(type, func)`         | Apply one rule to every property of the given CLR type on `T`   |
| `CustomInstantiator(func)`        | Replace default `new T()` with a factory method                 |
| `FinishWith(action)`              | Execute a post-generation action after all rules run            |
| `Ignore(expr)`                    | Mark a property as intentionally unruled (for StrictMode)       |
| `RuleSet(name, action)`           | Define a named group of rules for selective generation          |
| `StrictMode(bool)`                | Require rules for every property before `Generate()` is allowed |
| `UseSeed(int)`                    | Apply an instance-level seed                                    |
| `UseDateTimeReference(DateTime?)` | Pin the "current time" used by all Date methods                 |
| `Clone()`                         | Deep-copy internal state into a new isolated `Faker<T>`         |
| `Populate(T instance)`            | Hydrate an existing object rather than creating a new one       |
| `AssertConfigurationIsValid()`    | Throw `ValidationException` if misconfigured                    |
| `Validate(ruleSets)`              | Return `bool` validation result without throwing                |

### 3.2 `Generate` / `GenerateBetween`

```csharp
User user            = faker.Generate();                   // single instance
List<User> users     = faker.Generate(100);                // fixed count
List<User> users     = faker.GenerateBetween(5, 20);       // random count in range
IEnumerable<User>    = faker.GenerateLazy(50);             // deferred LINQ
IEnumerable<User>    = faker.GenerateForever();            // infinite stream

// Named rule sets
List<User> premium   = faker.Generate(10, "premium");
List<User> mixed     = faker.Generate(10, "default,premium");
```

### 3.3 Seeding and reproducibility

```csharp
// Global seed — shared by all Faker instances
Randomizer.Seed = new Random(8675309);

// Instance seed — isolated
var faker = new Faker<User>().UseSeed(1234);

// Clock override for Date methods
DataSets.Date.SystemClock = () => new DateTime(2025, 1, 1, 0, 0, 0, DateTimeKind.Utc);
```

Best practice: place new `.RuleFor()` calls at the *end* of existing declarations. Inserting rules in the middle shifts the random sequence, breaking deterministic outputs.

---

## 4. Providers / DataSets

Every provider is exposed as a property on the non-generic `Faker` facade and as a standalone instantiable class in `Bogus.DataSets`.

```csharp
var f = new Faker("en");
string city = f.Address.City();

// or standalone
var address = new DataSets.Address("en");
string city  = address.City();
```

### 4.1 Address

| Method                                            | Example output                                      |
|---------------------------------------------------|-----------------------------------------------------|
| `ZipCode(string format = null)`                   | `"94107"`                                           |
| `City()`                                          | `"Bernhardfort"`                                    |
| `CityPrefix()`                                    | `"South"`                                           |
| `CitySuffix()`                                    | `"berg"`                                            |
| `StreetAddress(bool useFullAddress = false)`      | `"60643 Oberbrunner Bypass"`                        |
| `StreetName()`                                    | `"Oberbrunner Bypass"`                              |
| `StreetSuffix()`                                  | `"Bypass"`                                          |
| `BuildingNumber()`                                | `"60643"`                                           |
| `SecondaryAddress()`                              | `"Suite 175"`                                       |
| `County()`                                        | `"Cambridgeshire"`                                  |
| `Country()`                                       | `"Mozambique"`                                      |
| `CountryCode(Iso3166Format format = Alpha2)`      | `"MR"` / `"CMR"`                                    |
| `FullAddress()`                                   | `"60643 Oberbrunner Bypass, Danielchester, Monaco"` |
| `State()`                                         | `"New Mexico"`                                      |
| `StateAbbr()`                                     | `"NM"`                                              |
| `Latitude(double min = -90, double max = 90)`     | `18.634`                                            |
| `Longitude(double min = -180, double max = 180)`  | `-77.001`                                           |
| `Direction(bool useAbbreviation = false)`         | `"Northeast"` / `"NE"`                              |
| `CardinalDirection(bool useAbbreviation = false)` | `"North"` / `"N"`                                   |
| `OrdinalDirection(bool useAbbreviation = false)`  | `"Northwest"` / `"NW"`                              |

### 4.2 Commerce

| Method                                                                             | Example output                    |
|------------------------------------------------------------------------------------|-----------------------------------|
| `Department(int max = 3, bool returnMax = false)`                                  | `"Music, Jewelery, Baby & Books"` |
| `Price(decimal min = 1, decimal max = 1000, int decimals = 2, string symbol = "")` | `"$14.99"`                        |
| `Categories(int num)`                                                              | `["Kids", "Music", "Jewelery"]`   |
| `ProductName()`                                                                    | `"Generic Wooden Bacon"`          |
| `ProductAdjective()`                                                               | `"Generic"`                       |
| `ProductMaterial()`                                                                | `"Rubber"`                        |
| `Product()`                                                                        | `"Soap"`                          |
| `ProductDescription()`                                                             | Long description string           |
| `Color()`                                                                          | `"plum"`                          |
| `Ean8()`                                                                           | `"61860605"`                      |
| `Ean13()`                                                                          | `"6186060643914"`                 |

### 4.3 Company

| Method                                 | Example output                        |
|----------------------------------------|---------------------------------------|
| `CompanyName(int? formatIndex = null)` | `"Smith LLC"`                         |
| `CompanySuffix()`                      | `"Inc"` / `"LLC"`                     |
| `CatchPhrase()`                        | `"Synergized zero-defect matrices"`   |
| `Bs()`                                 | `"engineer bleeding-edge interfaces"` |

### 4.4 Database

| Method        | Example output       |
|---------------|----------------------|
| `Column()`    | `"password"`         |
| `Type()`      | `"real"`             |
| `Collation()` | `"ascii_general_ci"` |
| `Engine()`    | `"CSV"`              |

### 4.5 Date

All `DateTime` methods have a `DateTimeOffset` counterpart (append `Offset`).

| Method                                                        | Notes                           |
|---------------------------------------------------------------|---------------------------------|
| `Past(int yearsToGoBack = 1, DateTime? refDate = null)`       | Random date in the past         |
| `Future(int yearsToGoForward = 1, DateTime? refDate = null)`  | Random date in the future       |
| `Between(DateTime start, DateTime end)`                       | Uniform in range                |
| `Recent(int days = 1, DateTime? refDate = null)`              | Within last N days              |
| `Soon(int days = 1, DateTime? refDate = null)`                | Within next N days              |
| `Timespan(TimeSpan? maxSpan = null)`                          | Random span, default max 7 days |
| `Month(bool abbreviation = false, bool useContext = false)`   | `"October"` / `"Oct"`           |
| `Weekday(bool abbreviation = false, bool useContext = false)` | `"Wednesday"` / `"Wed"`         |
| `TimeZoneString()`                                            | `"America/Los_Angeles"`         |

### 4.6 Finance

| Method                                                          | Example output                                |
|-----------------------------------------------------------------|-----------------------------------------------|
| `Account(int length = 8)`                                       | `"61860606"`                                  |
| `AccountName()`                                                 | `"Home Loan Account"`                         |
| `Amount(decimal min = 0, decimal max = 1000, int decimals = 2)` | `603.52m`                                     |
| `TransactionType()`                                             | `"payment"`                                   |
| `Currency(bool includeFundCodes = false)`                       | `{Code="USD", Symbol="$", Description="..."}` |
| `CreditCardNumber(CardType provider = null)`                    | Luhn-valid card number                        |
| `CreditCardCvv()`                                               | `"742"`                                       |
| `BitcoinAddress()`                                              | P2PKH format                                  |
| `EthereumAddress()`                                             | `"0x91da090b74f2b910..."`                     |
| `LitecoinAddress()`                                             | `"L7PE5D8HxpXjM3ig..."`                       |
| `RoutingNumber()`                                               | ABA routing number with valid check digit     |
| `Bic()`                                                         | `"CVQAMUB1"`                                  |
| `Iban(bool formatted = false, string countryCode = null)`       | `"MT78CVQA0491707AV6092536EZ69UM5"`           |

Supported `CardType`: `Visa`, `Mastercard`, `AmericanExpress`, `Discover`. All numbers pass Luhn.

### 4.7 Hacker

| Method           | Example output                                                           |
|------------------|--------------------------------------------------------------------------|
| `Abbreviation()` | `"RAM"`                                                                  |
| `Adjective()`    | `"neural"`                                                               |
| `Noun()`         | `"driver"`                                                               |
| `Verb()`         | `"calculate"`                                                            |
| `IngVerb()`      | `"bypassing"`                                                            |
| `Phrase()`       | `"Use the neural RAM driver, then you can calculate the neural driver!"` |

### 4.8 Images

| Method                                                           | Notes               |
|------------------------------------------------------------------|---------------------|
| `DataUri(int width, int height, string htmlColor = "grey")`      | Inline SVG data URI |
| `PicsumUrl(int width = 640, int height = 480, ...)`              | picsum.photos URL   |
| `PlaceholderUrl(int width, int height, string text = null, ...)` | placehold.co        |
| `LoremFlickrUrl(int width = 320, int height = 240, ...)`         | loremflickr.com     |

### 4.9 Internet

| Method                                                                             | Example output                              |
|------------------------------------------------------------------------------------|---------------------------------------------|
| `Email(string firstName = null, string lastName = null, ...)`                      | `"Bernhard.Schultz@yahoo.com"`              |
| `ExampleEmail(...)`                                                                | `"jane@example.com"`                        |
| `UserName(string firstName = null, string lastName = null)`                        | `"Lee_Brekke3"`                             |
| `DomainName()`                                                                     | `"lee.com"`                                 |
| `DomainWord()`                                                                     | `"lee"`                                     |
| `DomainSuffix()`                                                                   | `"name"`                                    |
| `Ip()`                                                                             | `"154.28.208.165"`                          |
| `IpAddress()`                                                                      | IPv4 `IPAddress` object                     |
| `Ipv6()`                                                                           | `"da23:9c4c:e0c4:2dd7:e3c4:a896:17f2:55b2"` |
| `Ipv6Address()`                                                                    | IPv6 `IPAddress` object                     |
| `Port()`                                                                           | Random port number                          |
| `IpEndPoint()`                                                                     | IPv4 `IPEndPoint`                           |
| `UserAgent()`                                                                      | Full browser UA string                      |
| `Mac(string separator = ":")`                                                      | `"9a:1c:d0:a5:09:9f"`                       |
| `Password(int length = 10, bool memorable = false, ...)`                           | `"YmaMy0eWbv"`                              |
| `Color(byte baseRed = 0, ..., ColorFormat format = Hex)`                           | `"#4d0e68"` / `"rgb(77,14,104)"`            |
| `Protocol()`                                                                       | `"https"`                                   |
| `Url()`                                                                            | `"https://lee.com"`                         |
| `UrlWithPath(string protocol = null, string domain = null, string fileExt = null)` | `"https://bitarmory.com/soft/deposit"`      |
| `UrlRootedPath(string fileExt = null)`                                             | `"/foo/bar"`                                |

### 4.10 Lorem

| Method                                                          | Notes                                      |
|-----------------------------------------------------------------|--------------------------------------------|
| `Word()`                                                        | Single lorem word                          |
| `Words(int num = 3)`                                            | Array of lorem words                       |
| `Letter(int num = 1)`                                           | N characters                               |
| `Sentence(int? wordCount = null, int? range = 0)`               | Default 3–10 words                         |
| `Sentences(int? sentenceCount = null, string separator = "\n")` | Multiple sentences                         |
| `Paragraph(int min = 3)`                                        | `[min, min+3]` sentences                   |
| `Paragraphs(int count = 3, string separator = "\n\n")`          | Fixed count                                |
| `Paragraphs(int min, int max, string separator = "\n\n")`       | Random count in range                      |
| `Text()`                                                        | Output from a randomly chosen lorem method |
| `Lines(int? lineCount = null, string separator = "\n")`         | 1–5 lines by default                       |
| `Slug(int wordcount = 3)`                                       | `"lorem-ipsum-dolor"`                      |

### 4.11 Music

| Method    | Example output |
|-----------|----------------|
| `Genre()` | `"Hip Hop"`    |

### 4.12 Name

| Method                                                                                                                           | Notes                                           |
|----------------------------------------------------------------------------------------------------------------------------------|-------------------------------------------------|
| `FirstName(Gender? gender = null)`                                                                                               | Locale-aware; gender-specific where data exists |
| `LastName(Gender? gender = null)`                                                                                                |                                                 |
| `FullName(Gender? gender = null)`                                                                                                | Concatenates first + last                       |
| `Prefix(Gender? gender = null)`                                                                                                  | `"Mr."` / `"Ms."`                               |
| `Suffix()`                                                                                                                       | `"Jr."` / `"PhD"`                               |
| `FindName(string firstName = "", string lastName = "", bool? withPrefix = null, bool? withSuffix = null, Gender? gender = null)` | Full composed name                              |
| `JobTitle()`                                                                                                                     | `"Investor Research Assistant"`                 |
| `JobDescriptor()`                                                                                                                | `"Investor"`                                    |
| `JobArea()`                                                                                                                      | `"Communications"`                              |
| `JobType()`                                                                                                                      | `"Orchestrator"`                                |

**`Gender` enum:** `Male`, `Female`.

### 4.13 Phone

| Method                                              | Notes                      |
|-----------------------------------------------------|----------------------------|
| `PhoneNumber(string format = null)`                 | Replace `#` with digits    |
| `PhoneNumberFormat(int phoneFormatsArrayIndex = 0)` | Use locale's formats array |

```csharp
phone.PhoneNumber("(###) ###-####"); // "(555) 123-4567"
```

### 4.14 Rant

| Method                                               | Example output            |
|------------------------------------------------------|---------------------------|
| `Review(string product = "product")`                 | Humorous user review text |
| `Reviews(string product = "product", int lines = 2)` | Array of reviews          |

### 4.15 System

| Method                              | Example output                    |
|-------------------------------------|-----------------------------------|
| `FileName(string ext = null)`       | `"soft_deposit.gif"`              |
| `CommonFileName(string ext = null)` | Filename with common extension    |
| `DirectoryPath()`                   | `"/sys"`                          |
| `FilePath()`                        | `"/sys/bluetooth.js"`             |
| `MimeType()`                        | `"audio/x-aiff"`                  |
| `CommonFileType()`                  | `"video"`                         |
| `CommonFileExt()`                   | `"pdf"`                           |
| `FileType()`                        | Any MIME-registered type string   |
| `FileExt(string mimeType = null)`   | `"jpg"` for `"image/jpeg"`        |
| `Semver()`                          | `"3.7.1"`                         |
| `Version()`                         | `System.Version` object           |
| `Exception()`                       | `Exception` with fake stack trace |
| `AndroidId()`                       | GCM registration ID               |
| `ApplePushToken()`                  | 64-char hex token                 |

### 4.16 Vehicle

| Method                     | Example output                          |
|----------------------------|-----------------------------------------|
| `Vin(bool strict = false)` | `"L3TN1M1OHAY675714"` (always 17 chars) |
| `Manufacturer()`           | `"Maserati"`                            |
| `Model()`                  | `"Prius"`                               |
| `Type()`                   | `"Minivan"`                             |
| `Fuel()`                   | `"Gasoline"`                            |

`strict = true` restricts VIN characters to alphanumeric uppercase excluding I, O, Q.

### 4.17 Randomizer (primitives)

Exposed as `f.Random` on the `Faker` facade.

#### Numeric

| Method                                                  | Notes                       |
|---------------------------------------------------------|-----------------------------|
| `Number(int min = 0, int max = 1)`                      | Inclusive bounds            |
| `Int / Long / Short / UShort / UInt / ULong`            | Typed variants with min/max |
| `Double / Decimal / Float`                              | Floating-point variants     |
| `Byte / SByte`                                          | Byte variants               |
| `Even(int min, int max)`                                | Even numbers only           |
| `Odd(int min, int max)`                                 | Odd numbers only            |
| `Digits(int count, int minDigit = 0, int maxDigit = 9)` | Array of digit integers     |
| `Char(char min, char max)`                              | Single character            |
| `Chars(char min, char max, int count = 5)`              | Character array             |

#### Boolean

| Method               | Notes                                      |
|----------------------|--------------------------------------------|
| `Bool()`             | 50/50                                      |
| `Bool(float weight)` | `weight` = probability of `true` (0.0–1.0) |

#### String and bytes

| Method                                                              | Notes                                   |
|---------------------------------------------------------------------|-----------------------------------------|
| `String(int? length, char minChar, char maxChar)`                   | Fixed-length from char range            |
| `String2(int length, string chars)`                                 | Draw from explicit pool                 |
| `AlphaNumeric(int length)`                                          | `[0-9a-z]` characters                   |
| `Hash(int length = 40, bool upperCase = false)`                     | Hex hash string                         |
| `Hexadecimal(int length = 1, string prefix = "0x")`                 | Hex string with prefix                  |
| `Utf16String(int minLength, int maxLength, bool excludeSurrogates)` | Valid UTF-16 string                     |
| `Bytes(int count)`                                                  | `byte[]`                                |
| `Replace(string format)`                                            | `#` → digit, `?` → letter, `*` → either |

#### Collections

| Method                                          | Notes                 |
|-------------------------------------------------|-----------------------|
| `ArrayElement<T>(T[] array)`                    | Single random element |
| `ArrayElements<T>(T[] array, int? count)`       | Random subset         |
| `ListItem<T>(IList<T> list)`                    | Single random item    |
| `Shuffle<T>(IEnumerable<T> source)`             | Shuffled sequence     |
| `WeightedRandom<T>(T[] items, float[] weights)` | Weighted selection    |

#### Enum

| Method                                          | Notes                              |
|-------------------------------------------------|------------------------------------|
| `Enum<T>(params T[] exclude)`                   | Random enum value, with exclusions |
| `EnumValues<T>(int? count, params T[] exclude)` | Random subset of enum values       |

#### Identifiers

| Method              | Notes                      |
|---------------------|----------------------------|
| `Guid()` / `Uuid()` | Random GUID                |
| `Word()`            | Single English word/phrase |
| `Words(int? count)` | Multiple words             |
| `RandomLocale()`    | Random locale string       |

### 4.18 Person (composite card)

```csharp
var person = new Person("en", seed: 1337, refDate: DateTime.Now);
```

Properties: `FirstName`, `LastName`, `FullName`, `Gender`, `DateOfBirth`, `Email`, `UserName`, `Avatar`, `Phone`, `Website`, `Address` (Street, Suite, City, State, ZipCode, Geo), `Company` (Name,
CatchPhrase, Bs).

Country-specific extensions:

```csharp
person.Ssn()  // US — "771-62-9016"
company.Ein() // US — "61-8606060"
person.Cpf()  // Brazil
person.Sin()  // Canada
```

---

## 5. Locale Support

70+ locales supported. Specify at construction time:

```csharp
var faker   = new Faker("ko");
var name    = new DataSets.Name("ru");
var address = new DataSets.Address("de");
```

Missing locale data falls back to `"en"` transparently.

**Selected locales:** `ar`, `az`, `cz`, `da`, `de`, `de_AT`, `de_CH`, `el`, `en`, `en_AU`, `en_CA`, `en_GB`, `en_IE`, `en_IND`, `en_NG`, `en_ZA`, `es`, `es_MX`, `fa`, `fi`, `fr`, `fr_BE`, `fr_CA`,
`fr_CH`, `hr`, `hu`, `hy`, `id_ID`, `it`, `ja`, `ko`, `lv`, `nb_NO`, `ne`, `nl`, `nl_BE`, `pl`, `pt_BR`, `pt_PT`, `ro`, `ru`, `sk`, `sl`, `sr_Cyrl_RS`, `sr_Latn_RS`, `sv`, `tr`, `uk`, `vi`, `zh_CN`,
`zh_TW`, `ge`, `ur`.

Dynamic locale switching:

```csharp
var name = new DataSets.Name("ru");
name["en"].LastName(); // English last name from a Russian-locale instance
```

---

## 6. StrictMode and AssertConfigurationIsValid

```csharp
var faker = new Faker<Order>()
    .StrictMode(true)
    .RuleFor(o => o.OrderId,  f => f.Random.Int())
    .RuleFor(o => o.Item,     f => f.Lorem.Sentence())
    .Ignore(o => o.LotNumber)
    .RuleFor(o => o.Quantity, f => f.Random.Number(1, 10));

faker.Generate();                    // succeeds — all non-ignored props have rules
faker.AssertConfigurationIsValid();  // throws ValidationException if misconfigured
bool ok = faker.Validate();          // non-throwing boolean form
```

---

## 7. Custom Datasets

```csharp
public class StarWars : DataSet
{
    public string Character() => GetRandomArrayItem("characters");
    public string Planet()    => GetRandomArrayItem("planets");
}
```

Locale data in `data_extend/` as JSON:

```json
{
  "star_wars": {
    "characters": [
      "Luke Skywalker",
      "Leia Organa",
      "Han Solo"
    ],
    "planets": [
      "Tatooine",
      "Alderaan",
      "Hoth"
    ]
  }
}
```

---

## 8. Extension Points and Utility Methods

```csharp
// 50% chance of null
.RuleFor(u => u.MiddleName, f => f.Name.FirstName().OrNull(f))
.RuleFor(o => o.LotNumber,  f => f.Random.Int(0, 100).OrNull(f, 0.8f))

// Pick from array
.RuleFor(o => o.Item, f => f.PickRandom(new[] { "apple", "banana", "cherry" }))

// Weighted selection
f.Random.WeightedRandom(items, weights)

// Auto-incrementing index
.RuleFor(u => u.Id,  f => f.IndexGlobal)  // global across all instances
.RuleFor(u => u.Seq, f => f.IndexFaker)   // scoped to this Faker<T>
```

---

## 9. Testing Framework Integration

```csharp
// xUnit
public class OrderTests
{
    private static readonly Faker<Order> OrderFaker = new Faker<Order>()
        .RuleFor(o => o.OrderId, f => f.Random.Int(1, 1000))
        .RuleFor(o => o.Item,    f => f.Commerce.ProductName())
        .RuleFor(o => o.Amount,  f => f.Finance.Amount());

    [Fact]
    public void order_amount_is_positive()
    {
        var order = OrderFaker.Generate();
        Assert.True(order.Amount > 0);
    }
}
```

---

## 10. Mapping to krandom

### Primitive / base types

| krandom class      | Bogus equivalent                  |
|--------------------|-----------------------------------|
| `IntGenerator`     | `f.Random.Int(min, max)`          |
| `LongGenerator`    | `f.Random.Long(min, max)`         |
| `ShortGenerator`   | `f.Random.Short(min, max)`        |
| `FloatGenerator`   | `f.Random.Float(min, max)`        |
| `DoubleGenerator`  | `f.Random.Double(min, max)`       |
| `ByteGenerator`    | `f.Random.Byte(min, max)`         |
| `BooleanGenerator` | `f.Random.Bool()`                 |
| `CharGenerator`    | `f.Random.Char(min, max)`         |
| `StringGenerator`  | `f.Random.String2(length, chars)` |
| `EnumGenerator`    | `f.Random.Enum<TEnum>()`          |

### Algorithm generators

| krandom class                              | Bogus equivalent                                       |
|--------------------------------------------|--------------------------------------------------------|
| `FibonacciGenerator`                       | No equivalent                                          |
| `LuhnGenerator`                            | `f.Finance.CreditCardNumber()` (Luhn-valid internally) |
| `NaturalNumberGenerator` (prime/composite) | No equivalent                                          |

### Network generators

| krandom class   | Bogus equivalent    |
|-----------------|---------------------|
| `IPv4Generator` | `f.Internet.Ip()`   |
| `IPv6Generator` | `f.Internet.Ipv6()` |

### Game generators

| krandom class   | Bogus equivalent                      |
|-----------------|---------------------------------------|
| `CoinGenerator` | `f.Random.Bool()` (no HEAD/TAIL enum) |
| `DiceGenerator` | `f.Random.Number(1, sides)`           |

### User / person data

| krandom class             | Bogus equivalent                      |
|---------------------------|---------------------------------------|
| `FirstName.kt`            | `f.Name.FirstName()`                  |
| `SurName.kt`              | `f.Name.LastName()`                   |
| `Email.kt`                | `f.Internet.Email()`                  |
| `Username.kt`             | `f.Internet.UserName()`               |
| `Gender.kt`               | `f.Name.FirstName(gender)` (implicit) |
| `Age.kt`                  | `f.Random.Int(0, 99)`                 |
| `BirthDay.kt`             | `f.Date.Past(80)`                     |
| `Title.kt`                | `f.Name.Prefix()`                     |
| `SocialSecurityNumber.kt` | `person.Ssn()` (US extension)         |

### Object generation

| krandom class        | Bogus equivalent                        |
|----------------------|-----------------------------------------|
| `ObjectGenerator<T>` | `Faker<T>` with `RuleFor` / `AutoBogus` |
| `HexHashGenerator`   | `f.Random.Hash(length)`                 |

---

## 11. Feature Gap Analysis

### High priority (broad applicability)

| Bogus feature                    | Category  | Suggested krandom location                        |
|----------------------------------|-----------|---------------------------------------------------|
| `f.Name.FullName()`              | Name      | Compose `FirstName` + `SurName` into `FullName`   |
| `f.Name.JobTitle()`              | Name      | `JobTitleGenerator` in `user` package             |
| `f.Lorem.*`                      | Text      | `LoremGenerator` in new `text` package            |
| `f.Address.*`                    | Address   | `AddressGenerator` group in new `address` package |
| `f.Date.Past / Future / Between` | Date/Time | `DateGenerator` in new `datetime` package         |
| `f.Finance.Amount()`             | Finance   | `AmountGenerator` (`BigDecimal`)                  |
| `f.Finance.CreditCardNumber()`   | Finance   | Wrap `LuhnGenerator` as `CreditCardGenerator`     |
| `f.Commerce.ProductName()`       | Commerce  | `ProductNameGenerator`                            |
| `f.Commerce.Price()`             | Commerce  | `PriceGenerator`                                  |

### Medium priority (domain-specific)

| Bogus feature                | Category | Suggested krandom location                 |
|------------------------------|----------|--------------------------------------------|
| `f.Internet.UserAgent()`     | Internet | `UserAgentGenerator` in `generator.network` package  |
| `f.Internet.Password()`      | Internet | `PasswordGenerator` in `user` package      |
| `f.Internet.Mac()`           | Network  | `MacAddressGenerator` in `generator.network` package |
| `f.Internet.Color()`         | Color    | `ColorGenerator`                           |
| `f.Finance.Iban()`           | Finance  | `IbanGenerator`                            |
| `f.Finance.BitcoinAddress()` | Finance  | `CryptoAddressGenerator`                   |
| `f.System.Semver()`          | System   | `SemverGenerator`                          |
| `f.System.MimeType()`        | System   | `MimeTypeGenerator`                        |
| `f.Vehicle.Vin()`            | Vehicle  | `VinGenerator` in new `vehicle` package    |

### Architectural gaps

| Gap                   | Bogus approach                                         | Recommendation                                                                |
|-----------------------|--------------------------------------------------------|-------------------------------------------------------------------------------|
| Fluent object builder | `Faker<T>` with `RuleFor`, `StrictMode`, `Generate(n)` | Add fluent `Faker<T>`-style builder in `java-api` / `kotlin-api` stub modules |
| Named rule sets       | `RuleSet(name, action)` + `Generate(n, "ruleset")`     | Useful for valid vs. invalid test fixtures                                    |
| Probabilistic nulls   | `OrNull(f, weight)` extension                          | Add as generator decorators                                                   |
| Infinite stream       | `GenerateForever()` returning `IEnumerable<T>`         | Add `generateStream()` returning `Stream<T>`                                  |
| Weighted selection    | `f.Random.WeightedRandom(items, weights)`              | Add to `Generators` utility                                                   |
| Locale-aware datasets | 70+ locale data files                                  | Phase 2: locale data files for Name, Address, Lorem                           |
| Composite Person card | `Person` class with related fields                     | Add `PersonCard` aggregate                                                    |
