# fake-rs Reference

**Repository:** https://github.com/cksac/fake-rs
**Author:** cksac (Calvin Chu)
**License:** MIT OR Apache-2.0
**Status:** Active
**Current version:** 2.x (crate `fake` on crates.io)
**Docs:** https://docs.rs/fake/latest/fake/

---

## 1. Purpose and Overview

`fake` is a Rust library for generating realistic-looking fake data for testing, seeding databases, and populating fixtures. It covers a broad range of domains — addresses, names, internet data,
finance, dates, barcodes, colours, and more — through a unified trait-based API.

**Design principles:**

- All fake-data generation is generic over any `Rng` (random number generator), making it compatible with both reproducible (seeded) and non-deterministic generators.
- The `Dummy` trait allows any custom type to participate in fake-data generation by implementing how to produce a fake instance.
- A `#[derive(Dummy)]` procedural macro automatically derives `Dummy` for structs and enums whose fields themselves implement `Dummy`.
- Locale support lets many fakers produce region-appropriate data (e.g., Japanese names, French phone numbers).
- The `fake!` convenience macro provides concise single-expression data generation.

**Primary use cases:**

- Generating test fixtures in Rust unit and integration tests
- Populating development/staging databases with realistic-looking data
- Fuzzing and property-based testing helpers
- Prototyping and demo data generation

---

## 2. Installation

```toml
[dependencies]
fake = "2"
```

### Feature flags

| Feature flag   | What it enables                                                          |
|----------------|--------------------------------------------------------------------------|
| `derive`       | Enables `#[derive(Dummy)]` procedural macro                              |
| `chrono`       | Faker types for `chrono` date/time types (`DateTime`, `NaiveDate`, etc.) |
| `http`         | Faker types for HTTP status codes                                        |
| `uuid`         | Faker types producing `uuid::Uuid`                                       |
| `bigdecimal`   | Faker types for `bigdecimal::BigDecimal`                                 |
| `decimal`      | Faker types for `rust_decimal::Decimal`                                  |
| `random_color` | Enables the CSS color-name faker                                         |
| `geo`          | Enables geohash fakers                                                   |
| `semver`       | Enables semantic version fakers                                          |
| `email`        | Enables more elaborate email fakers                                      |

Full example with common features:

```toml
[dependencies]
fake = { version = "2", features = ["derive", "chrono", "http", "uuid"] }
rand = "0.8"
```

---

## 3. Core API

### The `Fake` trait

`Fake` is the primary user-facing trait. Faker module types (unit structs) implement it. So do standard ranges and primitives.

```rust
// Short form — uses thread-local RNG
let name: String = fake::faker::name::en::FirstName().fake();

// Long form — pass your own RNG (e.g. seeded, reproducible)
use rand::SeedableRng;
use rand::rngs::StdRng;
let mut rng = StdRng::seed_from_u64(42);
let name: String = fake::faker::name::en::FirstName().fake_with_rng(&mut rng);
```

### The `Dummy` trait

`Dummy<T>` describes how to generate a fake instance of `Self` given a config of type `T`. When `T` is `Faker`, no configuration is needed; `Faker` is the catch-all.

```rust
pub trait Dummy<T>: Sized {
    fn dummy_with_rng<R: Rng + ?Sized>(config: &T, rng: &mut R) -> Self;
    fn dummy(config: &T) -> Self { ... }   // calls dummy_with_rng with thread_rng
}
```

**Manual implementation:**

```rust
use fake::{Dummy, Fake, Faker};
use rand::Rng;

struct Point { x: f64, y: f64 }

impl Dummy<Faker> for Point {
    fn dummy_with_rng<R: Rng + ?Sized>(_: &Faker, rng: &mut R) -> Self {
        Point {
            x: rng.gen_range(-90.0..=90.0),
            y: rng.gen_range(-180.0..=180.0),
        }
    }
}

let p: Point = Faker.fake();
```

**Custom config type:**

```rust
struct AgeRange { min: u8, max: u8 }
struct Person { age: u8 }

impl Dummy<AgeRange> for Person {
    fn dummy_with_rng<R: Rng + ?Sized>(config: &AgeRange, rng: &mut R) -> Self {
        Person { age: rng.gen_range(config.min..=config.max) }
    }
}

let p: Person = AgeRange { min: 18, max: 65 }.fake();
```

### `#[derive(Dummy)]`

Requires the `derive` feature. Generates `Dummy<Faker>` for structs and enums by recursively calling the faker specified for each field.

```rust
use fake::{Dummy, Fake, Faker};

#[derive(Dummy, Debug)]
struct User {
    #[dummy(faker = "fake::faker::name::en::FirstName()")]
    first_name: String,

    #[dummy(faker = "fake::faker::name::en::LastName()")]
    last_name: String,

    #[dummy(faker = "fake::faker::internet::en::SafeEmail()")]
    email: String,

    #[dummy(faker = "18..65")]
    age: u8,

    #[dummy(default)]
    is_verified: bool,   // always Default::default() == false
}

let user: User = Faker.fake();
println!("{:?}", user);
```

Enums work too — each variant is chosen randomly, variant fields are independently populated:

```rust
#[derive(Dummy, Debug)]
enum Status {
    Active,
    Inactive,
    Pending { since_days: u32 },
}

let s: Status = Faker.fake();
```

#### Field attribute summary

| Attribute                  | Meaning                                                                 |
|----------------------------|-------------------------------------------------------------------------|
| `#[dummy(faker = "expr")]` | Use the given expression (must implement `Fake`) to generate this field |
| `#[dummy(default)]`        | Use `Default::default()` instead of generating                          |
| `#[dummy(fixed = "expr")]` | Evaluate expression once and use it for every instance                  |

### `Faker`

The catch-all faker type. `Faker.fake::<T>()` generates a fake `T` using `T`'s `Dummy<Faker>` implementation.

Built-in `Dummy<Faker>` implementations:

| Rust type                                  | Notes                              |
|--------------------------------------------|------------------------------------|
| `bool`                                     | Equal probability true / false     |
| `i8`, `i16`, `i32`, `i64`, `i128`, `isize` | Full signed range                  |
| `u8`, `u16`, `u32`, `u64`, `u128`, `usize` | Full unsigned range                |
| `f32`, `f64`                               | Full float range                   |
| `char`                                     | Any valid Unicode scalar           |
| `String`                                   | Random characters                  |
| `Option<T>`                                | 50% `None`, 50% `Some(T::dummy())` |
| `Vec<T>`                                   | Random length, random elements     |
| Tuples up to 10-element                    | Each element independently faked   |

```rust
use fake::{Fake, Faker};

let n: i32        = Faker.fake();
let s: String     = Faker.fake();
let b: bool       = Faker.fake();
let v: Vec<i32>   = Faker.fake();
let o: Option<u8> = Faker.fake();
```

### Using `Rng`

All `.fake_with_rng(&mut rng)` calls accept any `rand::Rng` implementation:

```rust
use fake::{Fake, faker::name::en::Name};
use rand::SeedableRng;
use rand::rngs::StdRng;

let mut rng = StdRng::seed_from_u64(1234);

// Deterministic: same seed always produces the same value
let name: String = Name().fake_with_rng(&mut rng);
```

Common RNG types: `rand::thread_rng()` (global, non-deterministic), `StdRng::seed_from_u64(n)` (seeded), `SmallRng::from_entropy()` (fast, non-crypto).

### The `fake!` macro

```rust
use fake::fake;

// Basic
let name: String = fake!(Name in en);

// With locale
let name: String = fake!(Name in zh_tw);

// Generate a Vec
let names: Vec<String> = fake!(Name in en, 5);
```

Syntax: `fake!(<FakerType> in <locale>)` or `fake!(<FakerType> in <locale>, <count>)`.

---

## 4. ALL Faker Modules and Types

All faker types are zero-sized unit structs (or tuple structs where a parameter is needed). They live under `fake::faker::<category>::<locale>`. The canonical path for English is
`fake::faker::<category>::en::<Type>`.

---

### 4.1 Administrative

Module: `fake::faker::administrative`

| Type        | Output   | Description                                |
|-------------|----------|--------------------------------------------|
| `StateAbbr` | `String` | US state abbreviation, e.g. `"CA"`, `"NY"` |
| `StateName` | `String` | US state full name, e.g. `"California"`    |

```rust
use fake::{Fake, faker::administrative::en::*};

let abbr: String = StateAbbr().fake();  // "TX"
let name: String = StateName().fake();  // "Texas"
```

---

### 4.2 Address

Module: `fake::faker::address`

| Type             | Output   | Description                                    |
|------------------|----------|------------------------------------------------|
| `CityName`       | `String` | City name, e.g. `"Springfield"`                |
| `CityPrefix`     | `String` | City name prefix, e.g. `"North"`, `"South"`    |
| `CitySuffix`     | `String` | City name suffix, e.g. `"ville"`, `"ton"`      |
| `StreetName`     | `String` | Full street name, e.g. `"Oak Avenue"`          |
| `StreetSuffix`   | `String` | Street type suffix, e.g. `"Street"`, `"Road"`  |
| `BuildingNumber` | `String` | Building/house number, e.g. `"42"`             |
| `PostCode`       | `String` | Postal / ZIP code, e.g. `"90210"`              |
| `TimeZone`       | `String` | IANA time zone name, e.g. `"America/New_York"` |
| `CountryName`    | `String` | Country full name, e.g. `"Germany"`            |
| `CountryCode`    | `String` | ISO 3166-1 alpha-2 code, e.g. `"DE"`           |
| `Latitude`       | `String` | Latitude string, range -90..90                 |
| `Longitude`      | `String` | Longitude string, range -180..180              |
| `Geohash(u8)`    | `String` | Geohash string of given precision (1–12)       |

```rust
use fake::{Fake, faker::address::en::*};

let city: String    = CityName().fake();
let street: String  = StreetName().fake();
let post: String    = PostCode().fake();
let country: String = CountryName().fake();
let lat: String     = Latitude().fake();
let lon: String     = Longitude().fake();
let geo: String     = Geohash(8).fake();   // 8-char geohash
```

---

### 4.3 Automotive

Module: `fake::faker::automotive`

| Type           | Output   | Description                  |
|----------------|----------|------------------------------|
| `LicencePlate` | `String` | Vehicle licence plate string |

```rust
use fake::{Fake, faker::automotive::en::*};

let plate: String = LicencePlate().fake();  // "ABC-1234"
```

---

### 4.4 Barcode

Module: `fake::faker::barcode`

| Type     | Output   | Description                                   |
|----------|----------|-----------------------------------------------|
| `Isbn`   | `String` | ISBN (10 or 13 digits) with valid check digit |
| `Isbn10` | `String` | 10-digit ISBN with check digit                |
| `Isbn13` | `String` | 13-digit ISBN with check digit                |

```rust
use fake::{Fake, faker::barcode::en::*};

let isbn10: String = Isbn10().fake();
let isbn13: String = Isbn13().fake();
```

---

### 4.5 Color

Module: `fake::faker::color`

| Type        | Output   | Description                                                              |
|-------------|----------|--------------------------------------------------------------------------|
| `HexColor`  | `String` | Hex color string, e.g. `"#a1b2c3"`                                       |
| `RgbColor`  | `String` | RGB color string, e.g. `"rgb(12,34,200)"`                                |
| `RgbaColor` | `String` | RGBA color string, e.g. `"rgba(12,34,200,0.8)"`                          |
| `HslColor`  | `String` | HSL color string, e.g. `"hsl(270,60%,50%)"`                              |
| `HslaColor` | `String` | HSLA color string, e.g. `"hsla(270,60%,50%,0.8)"`                        |
| `Color`     | `String` | CSS color name, e.g. `"rebeccapurple"` (requires `random_color` feature) |

```rust
use fake::{Fake, faker::color::en::*};

let hex: String  = HexColor().fake();
let rgb: String  = RgbColor().fake();
let rgba: String = RgbaColor().fake();
let hsl: String  = HslColor().fake();
let hsla: String = HslaColor().fake();
let name: String = Color().fake();     // requires random_color feature
```

---

### 4.6 Company

Module: `fake::faker::company`

| Type             | Output   | Description                                 |
|------------------|----------|---------------------------------------------|
| `CompanyName`    | `String` | Company name, e.g. `"Acme Corp"`            |
| `CompanySuffix`  | `String` | Legal suffix, e.g. `"LLC"`, `"Inc."`        |
| `Bs`             | `String` | Business strategy phrase (full, three-part) |
| `BsAdj`          | `String` | Business strategy adjective component       |
| `BsNoun`         | `String` | Business strategy noun component            |
| `BsVerb`         | `String` | Business strategy verb component            |
| `Buzzword`       | `String` | Buzzword phrase (full)                      |
| `BuzzwordMiddle` | `String` | Buzzword middle component                   |
| `BuzzwordTail`   | `String` | Buzzword tail component                     |
| `CatchPhase`     | `String` | Company catch-phrase                        |
| `Industry`       | `String` | Industry name, e.g. `"FinTech"`             |
| `Profession`     | `String` | Job profession, e.g. `"Software Engineer"`  |

```rust
use fake::{Fake, faker::company::en::*};

let company: String     = CompanyName().fake();
let suffix: String      = CompanySuffix().fake();
let catchphrase: String = CatchPhase().fake();
let buzzword: String    = Buzzword().fake();
let industry: String    = Industry().fake();
let profession: String  = Profession().fake();
```

---

### 4.7 Currency

Module: `fake::faker::currency`

| Type             | Output   | Description                                   |
|------------------|----------|-----------------------------------------------|
| `CurrencyCode`   | `String` | ISO 4217 currency code, e.g. `"USD"`, `"EUR"` |
| `CurrencyName`   | `String` | Currency full name, e.g. `"US Dollar"`        |
| `CurrencySymbol` | `String` | Currency symbol, e.g. `"$"`, `"€"`            |

```rust
use fake::{Fake, faker::currency::en::*};

let code: String   = CurrencyCode().fake();
let name: String   = CurrencyName().fake();
let symbol: String = CurrencySymbol().fake();
```

---

### 4.8 Date and Time

Module: `fake::faker::chrono` — requires the `chrono` Cargo feature.

| Type                                            | Output type             | Description                                       |
|-------------------------------------------------|-------------------------|---------------------------------------------------|
| `DateTime`                                      | `chrono::DateTime<Utc>` | Random datetime in a wide historical/future range |
| `DateTimeBefore(DateTime<Utc>)`                 | `chrono::DateTime<Utc>` | Random datetime strictly before the given point   |
| `DateTimeAfter(DateTime<Utc>)`                  | `chrono::DateTime<Utc>` | Random datetime strictly after the given point    |
| `DateTimeBetween(DateTime<Utc>, DateTime<Utc>)` | `chrono::DateTime<Utc>` | Random datetime in the given interval             |
| `Date`                                          | `chrono::NaiveDate`     | Random calendar date                              |
| `Time`                                          | `chrono::NaiveTime`     | Random time of day                                |
| `Duration`                                      | `chrono::Duration`      | Random signed duration                            |

```rust
use fake::{Fake, faker::chrono::en::*};
use chrono::Utc;

let dt: chrono::DateTime<Utc> = DateTime().fake();
let d:  chrono::NaiveDate      = Date().fake();
let t:  chrono::NaiveTime      = Time().fake();
let dur: chrono::Duration       = Duration().fake();

let now = Utc::now();
let before: chrono::DateTime<Utc> = DateTimeBefore(now).fake();
let after:  chrono::DateTime<Utc> = DateTimeAfter(now).fake();
```

---

### 4.9 Finance

Module: `fake::faker::finance`

| Type   | Output   | Description                                             |
|--------|----------|---------------------------------------------------------|
| `Bic`  | `String` | ISO 9362 Bank Identifier Code, e.g. `"DEUTDEFF"`        |
| `Isin` | `String` | ISO 6166 International Securities Identification Number |

```rust
use fake::{Fake, faker::finance::en::*};

let bic: String  = Bic().fake();
let isin: String = Isin().fake();
```

---

### 4.10 Filesystem

Module: `fake::faker::filesystem`

| Type            | Output   | Description                                                               |
|-----------------|----------|---------------------------------------------------------------------------|
| `FilePath`      | `String` | Absolute file path, e.g. `"/home/user/docs/file.txt"`                     |
| `FileName`      | `String` | File name with extension, e.g. `"report.pdf"`                             |
| `FileExtension` | `String` | File extension without dot, e.g. `"png"`                                  |
| `DirPath`       | `String` | Absolute directory path, e.g. `"/var/log/app"`                            |
| `MimeType`      | `String` | MIME type string, e.g. `"image/jpeg"`                                     |
| `Semver`        | `String` | Semantic version string, e.g. `"1.2.3-alpha"` (requires `semver` feature) |
| `SemverStable`  | `String` | Stable semver, no pre-release label, e.g. `"2.0.1"`                       |

```rust
use fake::{Fake, faker::filesystem::en::*};

let path: String = FilePath().fake();
let name: String = FileName().fake();
let ext: String  = FileExtension().fake();
let dir: String  = DirPath().fake();
let mime: String = MimeType().fake();
let ver: String  = Semver().fake();
let sv: String   = SemverStable().fake();
```

---

### 4.11 HTTP

Module: `fake::faker::http` — requires the `http` Cargo feature.

| Type              | Output | Description                              |
|-------------------|--------|------------------------------------------|
| `RfcStatusCode`   | `u16`  | Any valid RFC HTTP status code (100–599) |
| `ValidStatusCode` | `u16`  | A commonly used HTTP status code         |

```rust
use fake::{Fake, faker::http::en::*};

let rfc: u16   = RfcStatusCode().fake();   // e.g. 418
let valid: u16 = ValidStatusCode().fake(); // e.g. 200
```

---

### 4.12 Internet

Module: `fake::faker::internet`

| Type                     | Output   | Description                                               |
|--------------------------|----------|-----------------------------------------------------------|
| `FreeEmail`              | `String` | Email at a free provider, e.g. `"alice@gmail.com"`        |
| `SafeEmail`              | `String` | Email at RFC 2606 safe domain, e.g. `"alice@example.org"` |
| `FreeEmailProvider`      | `String` | Free email provider domain, e.g. `"gmail.com"`            |
| `DomainSuffix`           | `String` | TLD, e.g. `"com"`, `"io"`                                 |
| `Username`               | `String` | Username slug, e.g. `"john_doe42"`                        |
| `Password(Range<usize>)` | `String` | Password whose length falls within the given range        |
| `IPv4`                   | `String` | IPv4 address string, e.g. `"192.168.1.1"`                 |
| `IPv6`                   | `String` | IPv6 address string, e.g. `"2001:db8::1"`                 |
| `IP`                     | `String` | Either IPv4 or IPv6 (random choice)                       |
| `MACAddress`             | `String` | MAC address, e.g. `"00:1A:2B:3C:4D:5E"`                   |
| `Color`                  | `String` | Hex color, e.g. `"#a1b2c3"`                               |
| `UserAgent`              | `String` | Browser User-Agent string                                 |
| `Slug`                   | `String` | URL slug, e.g. `"some-page-title"`                        |

```rust
use fake::{Fake, faker::internet::en::*};

let email: String = FreeEmail().fake();
let safe: String  = SafeEmail().fake();
let user: String  = Username().fake();
let pass: String  = Password(8..=16).fake();
let ipv4: String  = IPv4().fake();
let ipv6: String  = IPv6().fake();
let ip: String    = IP().fake();
let mac: String   = MACAddress().fake();
let ua: String    = UserAgent().fake();
let slug: String  = Slug().fake();
```

---

### 4.13 Job

Module: `fake::faker::job`

| Type           | Output   | Description                                       |
|----------------|----------|---------------------------------------------------|
| `JobSeniority` | `String` | Seniority level, e.g. `"Senior"`, `"Junior"`      |
| `JobTitle`     | `String` | Full job title, e.g. `"Senior Software Engineer"` |
| `JobField`     | `String` | Job field / domain, e.g. `"Engineering"`          |
| `JobType`      | `String` | Employment type, e.g. `"Full-time"`               |

```rust
use fake::{Fake, faker::job::en::*};

let seniority: String = JobSeniority().fake();
let title: String     = JobTitle().fake();
let field: String     = JobField().fake();
let jtype: String     = JobType().fake();
```

---

### 4.14 Lorem

Module: `fake::faker::lorem`

| Type                       | Output        | Description                                  |
|----------------------------|---------------|----------------------------------------------|
| `Word`                     | `String`      | A single lorem ipsum word                    |
| `Words(Range<usize>)`      | `Vec<String>` | Words with count in the given range          |
| `Sentence(Range<usize>)`   | `String`      | A sentence with word count in the range      |
| `Sentences(Range<usize>)`  | `Vec<String>` | Multiple sentences                           |
| `Paragraph(Range<usize>)`  | `String`      | A paragraph with sentence count in the range |
| `Paragraphs(Range<usize>)` | `Vec<String>` | Multiple paragraphs                          |

```rust
use fake::{Fake, faker::lorem::en::*};

let word: String           = Word().fake();
let words: Vec<String>     = Words(3..8).fake();
let sentence: String       = Sentence(5..15).fake();
let sentences: Vec<String> = Sentences(2..5).fake();
let para: String           = Paragraph(3..6).fake();
let paras: Vec<String>     = Paragraphs(2..4).fake();
```

---

### 4.15 Name

Module: `fake::faker::name`

| Type            | Output   | Description                                        |
|-----------------|----------|----------------------------------------------------|
| `FirstName`     | `String` | Given name, e.g. `"Alice"`                         |
| `LastName`      | `String` | Family name, e.g. `"Smith"`                        |
| `Name`          | `String` | Full name (first + last), e.g. `"Alice Smith"`     |
| `NameWithTitle` | `String` | Full name with honorific, e.g. `"Dr. Alice Smith"` |
| `Title`         | `String` | Honorific only, e.g. `"Mr."`, `"Dr."`              |
| `Suffix`        | `String` | Name suffix, e.g. `"Jr."`, `"III"`                 |

```rust
use fake::{Fake, faker::name::en::*};

let first: String  = FirstName().fake();
let last: String   = LastName().fake();
let name: String   = Name().fake();
let titled: String = NameWithTitle().fake();
let title: String  = Title().fake();
let suffix: String = Suffix().fake();
```

---

### 4.16 Number

Module: `fake::faker::number`

| Type                             | Output   | Description                                                     |
|----------------------------------|----------|-----------------------------------------------------------------|
| `Digit`                          | `String` | A single digit character, `"0"` through `"9"`                   |
| `NumberWithFormat(&'static str)` | `String` | Number formatted by a pattern, using `#` as a digit placeholder |

```rust
use fake::{Fake, faker::number::en::*};

let d: String = Digit().fake();                          // "7"
let n: String = NumberWithFormat("###-##-####").fake();  // "412-53-9821"
let n: String = NumberWithFormat("0800-######").fake();  // "0800-394821"
```

---

### 4.17 Phone

Module: `fake::faker::phone_number`

| Type          | Output   | Description                              |
|---------------|----------|------------------------------------------|
| `PhoneNumber` | `String` | Phone number (locale-appropriate format) |
| `CellNumber`  | `String` | Mobile/cell phone number                 |

```rust
use fake::{Fake, faker::phone_number::en::*};

let phone: String = PhoneNumber().fake();  // "(555) 867-5309"
let cell: String  = CellNumber().fake();   // "555-123-4567"
```

---

## 5. Locale Support

Most faker modules are split into locale sub-modules. The path pattern is:

```
fake::faker::<category>::<locale>::<Type>
```

**Supported locales** (coverage varies by module; `en` is the most complete):

| Locale  | Language / Region                |
|---------|----------------------------------|
| `en`    | English (most complete coverage) |
| `en_US` | English – United States          |
| `en_GB` | English – United Kingdom         |
| `fr_FR` | French – France                  |
| `de_DE` | German – Germany                 |
| `es_ES` | Spanish – Spain                  |
| `pt_BR` | Portuguese – Brazil              |
| `zh_CN` | Chinese (Simplified)             |
| `zh_TW` | Chinese (Traditional)            |
| `ja_JP` | Japanese                         |
| `ko_KR` | Korean                           |
| `ar_SA` | Arabic – Saudi Arabia            |
| `ru_RU` | Russian                          |
| `it_IT` | Italian                          |
| `pl_PL` | Polish                           |
| `tr_TR` | Turkish                          |
| `uk_UA` | Ukrainian                        |
| `vi_VN` | Vietnamese                       |
| `he_IL` | Hebrew – Israel                  |
| `fa_IR` | Persian – Iran                   |
| `id_ID` | Indonesian                       |
| `pt_PT` | Portuguese – Portugal            |
| `da_DK` | Danish                           |
| `fi_FI` | Finnish                          |
| `hu_HU` | Hungarian                        |
| `nb_NO` | Norwegian Bokmål                 |
| `nl_NL` | Dutch                            |
| `ro_RO` | Romanian                         |
| `sk_SK` | Slovak                           |
| `sv_SE` | Swedish                          |

```rust
use fake::Fake;

let en_name: String = fake::faker::name::en::Name().fake();     // "John Smith"
let jp_name: String = fake::faker::name::ja_JP::Name().fake();  // "田中 太郎"
let fr_name: String = fake::faker::name::fr_FR::Name().fake();  // "Jean Dupont"
```

---

## 6. Comparison with krandom

### Name / Person

| fake-rs type                     | krandom equivalent                                       | Status          |
|----------------------------------|----------------------------------------------------------|-----------------|
| `faker::name::en::FirstName`     | `org.github.krandom.user.FirstName` (Kotlin)             | Has it          |
| `faker::name::en::LastName`      | `org.github.krandom.user.SurName` (Kotlin)               | Has it          |
| `faker::name::en::Name`          | `GenericUserGenerator.fullName()` (Kotlin)               | Has it          |
| `faker::name::en::NameWithTitle` | `Title`/`TitleResult` exists but no combined single call | Partial         |
| `faker::name::en::Title`         | `org.github.krandom.user.Title` / `TitleResult` enum     | Has it          |
| `faker::name::en::Suffix`        | —                                                        | Gap             |
| Age                              | `org.github.krandom.user.Age` + `AgeGroup` enum          | Has it (unique) |
| Gender                           | `org.github.krandom.user.Gender`                         | Has it (unique) |
| SSN                              | `org.github.krandom.user.SocialSecurityNumber`           | Has it (unique) |
| Birthday                         | `org.github.krandom.user.BirthDay`                       | Has it (unique) |
| Username                         | `org.github.krandom.user.Username`                       | Has it (unique) |

### Internet / Network

| fake-rs type                      | krandom equivalent                            | Status  |
|-----------------------------------|-----------------------------------------------|---------|
| `faker::internet::en::FreeEmail`  | `org.github.krandom.user.Email`               | Has it  |
| `faker::internet::en::SafeEmail`  | —                                             | Gap     |
| `faker::internet::en::Username`   | `org.github.krandom.user.Username`            | Has it  |
| `faker::internet::en::Password`   | `StringGenerator` (no dedicated password API) | Partial |
| `faker::internet::en::IPv4`       | `org.github.krandom.generator.network.IPv4Generator` | Has it  |
| `faker::internet::en::IPv6`       | `org.github.krandom.generator.network.IPv6Generator` | Has it  |
| `faker::internet::en::IP`         | — (no "either v4 or v6" selector)             | Gap     |
| `faker::internet::en::MACAddress` | —                                             | Gap     |
| `faker::internet::en::UserAgent`  | —                                             | Gap     |
| `faker::internet::en::Slug`       | —                                             | Gap     |

### Address / Location

| fake-rs type                      | krandom equivalent | Status |
|-----------------------------------|--------------------|--------|
| `faker::address::en::CityName`    | —                  | Gap    |
| `faker::address::en::StreetName`  | —                  | Gap    |
| `faker::address::en::PostCode`    | —                  | Gap    |
| `faker::address::en::CountryName` | —                  | Gap    |
| `faker::address::en::CountryCode` | —                  | Gap    |
| `faker::address::en::Latitude`    | —                  | Gap    |
| `faker::address::en::Longitude`   | —                  | Gap    |
| `faker::address::en::TimeZone`    | —                  | Gap    |

### Company / Job

| fake-rs type                      | krandom equivalent | Status |
|-----------------------------------|--------------------|--------|
| `faker::company::en::CompanyName` | —                  | Gap    |
| `faker::company::en::Industry`    | —                  | Gap    |
| `faker::company::en::Profession`  | —                  | Gap    |
| `faker::job::en::JobTitle`        | —                  | Gap    |
| `faker::job::en::JobField`        | —                  | Gap    |

### Finance / Barcode

| fake-rs type                        | krandom equivalent                                      | Status          |
|-------------------------------------|---------------------------------------------------------|-----------------|
| `faker::finance::en::Bic`           | —                                                       | Gap             |
| `faker::finance::en::Isin`          | —                                                       | Gap             |
| `faker::barcode::en::Isbn10`        | —                                                       | Gap             |
| `faker::barcode::en::Isbn13`        | —                                                       | Gap             |
| `faker::currency::en::CurrencyCode` | —                                                       | Gap             |
| Luhn                                | `org.github.krandom.generator.algorithms.LuhnGenerator` | Has it (unique) |

### Date / Time

| fake-rs type                  | krandom equivalent                                    | Status  |
|-------------------------------|-------------------------------------------------------|---------|
| `faker::chrono::en::Date`     | `org.github.krandom.user.BirthDay` (birth dates only) | Partial |
| `faker::chrono::en::DateTime` | —                                                     | Gap     |
| `faker::chrono::en::Time`     | —                                                     | Gap     |
| `faker::chrono::en::Duration` | —                                                     | Gap     |

### Lorem / Text

| fake-rs type                  | krandom equivalent | Status |
|-------------------------------|--------------------|--------|
| `faker::lorem::en::Word`      | —                  | Gap    |
| `faker::lorem::en::Sentence`  | —                  | Gap    |
| `faker::lorem::en::Paragraph` | —                  | Gap    |

### krandom Unique (no fake-rs equivalent)

| krandom generator                     | Notes                       |
|---------------------------------------|-----------------------------|
| `DiceGenerator` + `DiceType` (D4–D20) | Game utility                |
| `CoinGenerator` + `CoinResult`        | Game utility                |
| `FibonacciGenerator`                  | Mathematical sequence       |
| `NaturalNumberGenerator`              | Prime / composite / natural |
| `HexHashGenerator`                    | Configurable hex hash       |
| `ObjectGenerator`                     | Object-graph population     |

---

## 7. Potential Additions for krandom Inspired by fake-rs

### Tier 1 — High value, simple to implement

| Feature                        | fake-rs type(s)                                  | Cross-library presence    |
|--------------------------------|--------------------------------------------------|---------------------------|
| Phone number                   | `PhoneNumber`, `CellNumber`                      | fake-rs, Chance.js, lorem |
| City name                      | `CityName`                                       | fake-rs, Chance.js, lorem |
| State abbreviation / full name | `StateAbbr`, `StateName`                         | fake-rs, Chance.js, lorem |
| Country name / ISO code        | `CountryName`, `CountryCode`                     | fake-rs, Chance.js, lorem |
| Post code / ZIP                | `PostCode`                                       | fake-rs, Chance.js, lorem |
| Company name / suffix          | `CompanyName`, `CompanySuffix`                   | fake-rs, Chance.js        |
| Currency code / name / symbol  | `CurrencyCode`, `CurrencyName`, `CurrencySymbol` | fake-rs, Chance.js        |

### Tier 2 — Medium value

| Feature                                    | fake-rs type(s)                                   | Cross-library presence |
|--------------------------------------------|---------------------------------------------------|------------------------|
| Lorem ipsum words / sentences / paragraphs | `Word`, `Sentence`, `Paragraph`                   | fake-rs, lorem         |
| Job data                                   | `JobTitle`, `JobField`, `JobSeniority`, `JobType` | fake-rs                |
| General Date / DateTime                    | `Date`, `DateTime`, `Time`, `Duration`            | fake-rs, Chance.js     |
| Street / building address                  | `StreetName`, `BuildingNumber`                    | fake-rs                |
| Safe email                                 | `SafeEmail`                                       | fake-rs, lorem         |
| MAC address                                | `MACAddress`                                      | fake-rs, Chance.js     |
| Password (length-bounded)                  | `Password(min..=max)`                             | fake-rs                |

### Tier 3 — Lower priority

| Feature                       | fake-rs type(s)                    | Notes                      |
|-------------------------------|------------------------------------|----------------------------|
| Colour formats                | `HexColor`, `RgbColor`, `HslColor` | CSS / UI data              |
| ISBN barcodes                 | `Isbn10`, `Isbn13`                 | ISBN check-digit algorithm |
| HTTP status codes             | `RfcStatusCode`, `ValidStatusCode` | API test helpers           |
| Filesystem paths / MIME types | `FilePath`, `MimeType`             | File system tests          |
| Semantic version              | `Semver`, `SemverStable`           | Dependency tooling         |
| Latitude / Longitude          | `Latitude`, `Longitude`            | Geo coordinates            |
| User-Agent string             | `UserAgent`                        | HTTP client testing        |

---

## 8. Quick-Reference Table

| Category       | Type                     | Module path (en locale)     | Output type             |
|----------------|--------------------------|-----------------------------|-------------------------|
| Administrative | `StateAbbr`              | `faker::administrative::en` | `String`                |
| Administrative | `StateName`              | `faker::administrative::en` | `String`                |
| Address        | `BuildingNumber`         | `faker::address::en`        | `String`                |
| Address        | `CityName`               | `faker::address::en`        | `String`                |
| Address        | `CityPrefix`             | `faker::address::en`        | `String`                |
| Address        | `CitySuffix`             | `faker::address::en`        | `String`                |
| Address        | `CountryCode`            | `faker::address::en`        | `String`                |
| Address        | `CountryName`            | `faker::address::en`        | `String`                |
| Address        | `Geohash(u8)`            | `faker::address::en`        | `String`                |
| Address        | `Latitude`               | `faker::address::en`        | `String`                |
| Address        | `Longitude`              | `faker::address::en`        | `String`                |
| Address        | `PostCode`               | `faker::address::en`        | `String`                |
| Address        | `StreetName`             | `faker::address::en`        | `String`                |
| Address        | `StreetSuffix`           | `faker::address::en`        | `String`                |
| Address        | `TimeZone`               | `faker::address::en`        | `String`                |
| Automotive     | `LicencePlate`           | `faker::automotive::en`     | `String`                |
| Barcode        | `Isbn`                   | `faker::barcode::en`        | `String`                |
| Barcode        | `Isbn10`                 | `faker::barcode::en`        | `String`                |
| Barcode        | `Isbn13`                 | `faker::barcode::en`        | `String`                |
| Color          | `HexColor`               | `faker::color::en`          | `String`                |
| Color          | `HslColor`               | `faker::color::en`          | `String`                |
| Color          | `RgbColor`               | `faker::color::en`          | `String`                |
| Color          | `RgbaColor`              | `faker::color::en`          | `String`                |
| Company        | `Bs`                     | `faker::company::en`        | `String`                |
| Company        | `Buzzword`               | `faker::company::en`        | `String`                |
| Company        | `CatchPhase`             | `faker::company::en`        | `String`                |
| Company        | `CompanyName`            | `faker::company::en`        | `String`                |
| Company        | `CompanySuffix`          | `faker::company::en`        | `String`                |
| Company        | `Industry`               | `faker::company::en`        | `String`                |
| Company        | `Profession`             | `faker::company::en`        | `String`                |
| Currency       | `CurrencyCode`           | `faker::currency::en`       | `String`                |
| Currency       | `CurrencyName`           | `faker::currency::en`       | `String`                |
| Currency       | `CurrencySymbol`         | `faker::currency::en`       | `String`                |
| Date/Time      | `Date`                   | `faker::chrono::en`         | `chrono::NaiveDate`     |
| Date/Time      | `DateTime`               | `faker::chrono::en`         | `chrono::DateTime<Utc>` |
| Date/Time      | `DateTimeAfter(dt)`      | `faker::chrono::en`         | `chrono::DateTime<Utc>` |
| Date/Time      | `DateTimeBefore(dt)`     | `faker::chrono::en`         | `chrono::DateTime<Utc>` |
| Date/Time      | `DateTimeBetween(a,b)`   | `faker::chrono::en`         | `chrono::DateTime<Utc>` |
| Date/Time      | `Duration`               | `faker::chrono::en`         | `chrono::Duration`      |
| Date/Time      | `Time`                   | `faker::chrono::en`         | `chrono::NaiveTime`     |
| Finance        | `Bic`                    | `faker::finance::en`        | `String`                |
| Finance        | `Isin`                   | `faker::finance::en`        | `String`                |
| Filesystem     | `DirPath`                | `faker::filesystem::en`     | `String`                |
| Filesystem     | `FileExtension`          | `faker::filesystem::en`     | `String`                |
| Filesystem     | `FileName`               | `faker::filesystem::en`     | `String`                |
| Filesystem     | `FilePath`               | `faker::filesystem::en`     | `String`                |
| Filesystem     | `MimeType`               | `faker::filesystem::en`     | `String`                |
| Filesystem     | `Semver`                 | `faker::filesystem::en`     | `String`                |
| Filesystem     | `SemverStable`           | `faker::filesystem::en`     | `String`                |
| HTTP           | `RfcStatusCode`          | `faker::http::en`           | `u16`                   |
| HTTP           | `ValidStatusCode`        | `faker::http::en`           | `u16`                   |
| Internet       | `DomainSuffix`           | `faker::internet::en`       | `String`                |
| Internet       | `FreeEmail`              | `faker::internet::en`       | `String`                |
| Internet       | `FreeEmailProvider`      | `faker::internet::en`       | `String`                |
| Internet       | `IP`                     | `faker::internet::en`       | `String`                |
| Internet       | `IPv4`                   | `faker::internet::en`       | `String`                |
| Internet       | `IPv6`                   | `faker::internet::en`       | `String`                |
| Internet       | `MACAddress`             | `faker::internet::en`       | `String`                |
| Internet       | `Password(range)`        | `faker::internet::en`       | `String`                |
| Internet       | `SafeEmail`              | `faker::internet::en`       | `String`                |
| Internet       | `Slug`                   | `faker::internet::en`       | `String`                |
| Internet       | `UserAgent`              | `faker::internet::en`       | `String`                |
| Internet       | `Username`               | `faker::internet::en`       | `String`                |
| Job            | `JobField`               | `faker::job::en`            | `String`                |
| Job            | `JobSeniority`           | `faker::job::en`            | `String`                |
| Job            | `JobTitle`               | `faker::job::en`            | `String`                |
| Job            | `JobType`                | `faker::job::en`            | `String`                |
| Lorem          | `Paragraph(range)`       | `faker::lorem::en`          | `String`                |
| Lorem          | `Paragraphs(range)`      | `faker::lorem::en`          | `Vec<String>`           |
| Lorem          | `Sentence(range)`        | `faker::lorem::en`          | `String`                |
| Lorem          | `Sentences(range)`       | `faker::lorem::en`          | `Vec<String>`           |
| Lorem          | `Word`                   | `faker::lorem::en`          | `String`                |
| Lorem          | `Words(range)`           | `faker::lorem::en`          | `Vec<String>`           |
| Name           | `FirstName`              | `faker::name::en`           | `String`                |
| Name           | `LastName`               | `faker::name::en`           | `String`                |
| Name           | `Name`                   | `faker::name::en`           | `String`                |
| Name           | `NameWithTitle`          | `faker::name::en`           | `String`                |
| Name           | `Suffix`                 | `faker::name::en`           | `String`                |
| Name           | `Title`                  | `faker::name::en`           | `String`                |
| Number         | `Digit`                  | `faker::number::en`         | `String`                |
| Number         | `NumberWithFormat(&str)` | `faker::number::en`         | `String`                |
| Phone          | `CellNumber`             | `faker::phone_number::en`   | `String`                |
| Phone          | `PhoneNumber`            | `faker::phone_number::en`   | `String`                |
