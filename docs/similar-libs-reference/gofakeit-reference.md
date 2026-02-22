# gofakeit Reference

**Repository:** https://github.com/brianvoe/gofakeit
**Docs:** https://pkg.go.dev/github.com/brianvoe/gofakeit/v6
**Current stable:** v6.28.0 (v7 also available)
**Language:** Go

---

## 1. Purpose and Overview

`gofakeit` is a random fake-data generation library written in pure Go with 310+ data-generation functions across ~50 domain categories.

Key characteristics:

- **310+ functions** covering addresses, people, finance, internet, hacker jargon, Minecraft, etc.
- **Zero external dependencies** — pure Go standard library only.
- **Multiple PRNG backends**: PCG (default), ChaCha8, JSF, SFC, `crypto/rand`, and a deterministic "dumb" source. Backend is pluggable via `rand.Source`.
- **Reproducible output**: pass any non-zero `uint64` seed for deterministic sequences.
- **Struct auto-population**: annotate struct fields with `fake:` tags and call `gofakeit.Struct(&v)`.
- **Go template engine**: `gofakeit.Template(tmpl, opts)` exposes all 310+ functions as template actions.
- **Regex generation**: `gofakeit.Regex(pattern)` generates a string matching any RE2 pattern.
- **Structured data generation**: CSV, JSON, XML, Markdown, SQL, fixed-width.
- **Extensible**: register custom generators via `gofakeit.AddFuncLookup`.
- **`Fakeable` interface**: implement `Fake(*Faker) (any, error)` for custom struct field generation.
- **Thread-safe by default**: global faker uses mutex-guarded PCG; unlocked variants available.

---

## 2. Installation

```bash
go get github.com/brianvoe/gofakeit/v7   # latest
go get github.com/brianvoe/gofakeit/v6   # stable, widely deployed (this doc)
```

```go
import "github.com/brianvoe/gofakeit/v6"
```

---

## 3. Core API

### 3.1 Global functions vs `Faker` struct

| Form            | Example           | Notes                                                               |
|-----------------|-------------------|---------------------------------------------------------------------|
| Package-level   | `gofakeit.Name()` | Uses global `GlobalFaker`; mutex-protected, safe for concurrent use |
| Instance method | `f.Name()`        | Thread-safety depends on construction mode                          |

### 3.2 Constructors

```go
func New(seed uint64) *Faker         // seed=0 uses crypto/rand; any other = deterministic
func NewCrypto() *Faker              // backed by crypto/rand
func NewCustom(source rand.Source) *Faker
func NewUnlocked(seed uint64) *Faker // no mutex — fastest for single-goroutine use
func NewFaker(src rand.Source, lock bool) *Faker
```

### 3.3 Global helpers

```go
func Seed(seed int64)                // re-seeds the GlobalFaker (0 = crypto/rand)
func SetGlobalFaker(faker *Faker)    // replace GlobalFaker entirely
```

### 3.4 Usage patterns

```go
// Deterministic
faker := gofakeit.New(12345)
fmt.Println(faker.Name())   // identical on every run with same seed

// Cryptographically secure
faker := gofakeit.NewCrypto()

// Global convenience
gofakeit.Seed(8675309)
fmt.Println(gofakeit.Phone())
```

---

## 4. All Functions by Category

### 4.1 Address

```go
func Address() *AddressInfo
func Street() string
func StreetName() string
func StreetNumber() string
func StreetPrefix() string
func StreetSuffix() string
func Unit() string
func City() string
func State() string
func StateAbr() string
func Country() string
func CountryAbr() string
func Zip() string
func Latitude() float64
func LatitudeInRange(min, max float64) (float64, error)
func Longitude() float64
func LongitudeInRange(min, max float64) (float64, error)
```

`AddressInfo` struct: `Address`, `Street`, `Unit`, `City`, `State`, `Zip`, `Country`, `Latitude`, `Longitude`.

### 4.2 Animal

```go
func Animal() string
func AnimalType() string
func Bird() string
func Cat() string
func Dog() string
func FarmAnimal() string
func PetName() string
```

### 4.3 App

```go
func AppName() string
func AppVersion() string
func AppAuthor() string
```

### 4.4 Beer

```go
func BeerName() string
func BeerStyle() string
func BeerHop() string
func BeerYeast() string
func BeerMalt() string
func BeerIbu() string
func BeerAlcohol() string
func BeerBlg() string
```

### 4.5 Book

```go
func Book() *BookInfo     // BookInfo: Title, Author, Genre
func BookTitle() string
func BookAuthor() string
func BookGenre() string
```

### 4.6 Car

```go
func Car() *CarInfo       // CarInfo: Type, Fuel, Transmission, Brand, Model, Year
func CarMaker() string
func CarModel() string
func CarType() string
func CarFuelType() string
func CarTransmissionType() string
```

### 4.7 Celebrity

```go
func CelebrityActor() string
func CelebrityBusiness() string
func CelebritySport() string
```

### 4.8 Color

```go
func Color() string       // "MediumOrchid"
func HexColor() string    // "#a45fb2"
func RGBColor() string    // "rgb(123,45,67)"
func SafeColor() string   // web-safe color name
func NiceColors() []string
```

### 4.9 Company / Job

```go
func Company() string
func CompanySuffix() string
func JobTitle() string
func JobDescriptor() string
func JobLevel() string
func Job() string
func BS() string
func BuzzWord() string
func Slogan() string
```

`JobInfo` struct: `Company`, `Title`, `Descriptor`, `Level`.

### 4.10 Contact

```go
func Contact() *ContactInfo  // ContactInfo: Phone, Email
func Email() string
func Phone() string
func PhoneFormatted() string
func Username() string
```

### 4.11 Credit Card / Payment / Finance

```go
func CreditCard() *CreditCardInfo        // Type, Number, Exp, Cvv
func CreditCardNumber(cco *CreditCardOptions) string
func CreditCardType() string
func CreditCardExp() string
func CreditCardCvv() string
func AchAccount() string
func AchRouting() string
func BankName() string
func BankType() string
func BitcoinAddress() string
func BitcoinPrivateKey() string
func Currency() *CurrencyInfo            // Short ("USD"), Long ("United States Dollar")
func CurrencyShort() string
func CurrencyLong() string
func Price(min, max float64) float64
func SSN() string
func Cusip() string
func Isin() string
func EIN() string
```

### 4.12 Date / Time

```go
func Date() time.Time
func DateRange(start, end time.Time) time.Time
func FutureDate() time.Time
func PastDate() time.Time
func Day() int
func Month() int
func MonthString() string
func Year() int
func WeekDay() string
func Hour() int
func Minute() int
func Second() int
func NanoSecond() int
func TimeZone() string
func TimeZoneAbv() string
func TimeZoneFull() string
func TimeZoneOffset() string
func TimeZoneRegion() string
```

### 4.13 Emoji

```go
func Emoji() string
func EmojiDescription() string
func EmojiCategory() string
func EmojiAlias() string
func EmojiTag() string
func EmojiFlag() string
func EmojiAnimal() string
func EmojiFood() string
// ... (25+ emoji functions total)
```

### 4.14 Error

```go
func Error() error
func ErrorDatabase() error
func ErrorGRPC() error
func ErrorHTTP() error
func ErrorHTTPClient() error
func ErrorHTTPServer() error
func ErrorRuntime() error
func ErrorValidation() error
func ErrorObject() error
```

### 4.15 File

```go
func FileExtension() string   // "png", "go", "sql"
func FileMimeType() string    // "image/png", "text/html"
```

### 4.16 Food / Drink

```go
func Fruit() string
func Vegetable() string
func Breakfast() string
func Lunch() string
func Dinner() string
func Snack() string
func Dessert() string
func Drink() string
```

### 4.17 Games / Gambling

```go
func Dice(numDice, sides int) []int  // roll numDice dice with sides faces each
func FlipACoin() string              // "Heads" or "Tails"
func Gamertag() string
func Hobby() string
```

### 4.18 Generate / Pattern

```go
func Generate(dataVal string) string   // {function} placeholders; '#'=digit, '?'=letter
func Regex(regexStr string) string     // string satisfying RE2 pattern
func Map() map[string]interface{}      // random heterogeneous map
```

```go
gofakeit.Generate("{firstname}")               // "Alice"
gofakeit.Generate("{sentence:3}")              // "Record river mind"
gofakeit.Generate("{number:1,100}")            // "42"
gofakeit.Generate("###-???")                  // "481-fda"
gofakeit.Regex("[a-z]{5}[0-9]{3}")            // "fkqwj812"
```

### 4.19 Hacker

```go
func HackerPhrase() string
func HackerAbbreviation() string   // "HTTP", "RAM", "GPU"
func HackerAdjective() string      // "neural", "redundant"
func HackerNoun() string           // "feed", "bandwidth"
func HackerVerb() string
func HackeringVerb() string        // gerund form
```

### 4.20 Hex

```go
func HexColor() string
func HexUint8() string
func HexUint16() string
func HexUint32() string
func HexUint64() string
func HexUint128() string
func HexUint256() string
```

### 4.21 Hipster

```go
func HipsterWord() string
func HipsterSentence(wordCount int) string
func HipsterParagraph(paragraphCount, sentenceCount, wordCount int, separator string) string
```

### 4.22 HTTP

```go
func HTTPMethod() string
func HTTPStatusCode() int
func HTTPStatusCodeSimple() int   // only 200, 301, 302, 404, 500
func HTTPVersion() string          // "HTTP/1.1", "HTTP/2.0"
```

### 4.23 Image

```go
func Image(width, height int) []byte
func ImageJpeg(width, height int) []byte
func ImagePng(width, height int) []byte
func ImageURL(width, height int) string   // picsum.photos URL
func Svg(options *SvgOptions) string
```

### 4.24 Internet / Network

```go
func URL() string
func DomainName() string
func DomainSuffix() string            // "com", "org", "io"
func UrlSlug(words int) string        // "my-awesome-slug"
func IPv4Address() string
func IPv6Address() string
func MacAddress() string
func UserAgent() string
func ChromeUserAgent() string
func FirefoxUserAgent() string
func SafariUserAgent() string
func OperaUserAgent() string
func APIUserAgent() string
func LogLevel(logType string) string   // "apache", "nginx", "syslog", "nfs"
func Password(lower, upper, numeric, special, space bool, num int) string
func InputName() string               // HTML input field name value
```

### 4.25 Language

```go
func Language() string               // "English", "Japanese"
func LanguageAbbreviation() string   // "en", "ja"
func LanguageBCP() string            // "en-US"
func ProgrammingLanguage() string    // "Go", "Python"
func ProgrammingLanguageBest() string // always returns "Go"
```

### 4.26 Lorem Ipsum / Text

```go
func LoremIpsumWord() string
func LoremIpsumSentence(wordCount int) string
func LoremIpsumParagraph(paragraphCount, sentenceCount, wordCount int, separator string) string
func Word() string
func Sentence(wordCount int) string
func SentenceSimple() string
func Paragraph(paragraphCount, sentenceCount, wordCount int, separator string) string
func Quote() string
func Question() string
func Phrase() string
func PhraseNoun() string
func PhraseVerb() string
func Comment() string
```

### 4.27 Math / Number

```go
func Number(min, max int) int
func Int() int
func Int8() int8
func Int16() int16
func Int32() int32
func Int64() int64
func IntN(n int) int
func IntRange(min, max int) int
func Uint() uint
func Uint8() uint8
func Uint16() uint16
func Uint32() uint32
func Uint64() uint64
func Float32() float32
func Float32Range(min, max float32) float32
func Float64() float64
func Float64Range(min, max float64) float64
func Digit() string
func DigitN(n int) string
func Numerify(str string) string    // '#' -> random digit
func Lexify(str string) string      // '?' -> random letter
func Bothify(str string) string     // '#' digit, '?' letter
func Asciify(str string) string     // '*' -> random ASCII printable
```

### 4.28 Minecraft

```go
func MinecraftAnimal() string
func MinecraftBiome() string
func MinecraftDye() string
func MinecraftFood() string
func MinecraftMobBoss() string
func MinecraftMobHostile() string
func MinecraftOre() string
func MinecraftTool() string
func MinecraftWeapon() string
func MinecraftWood() string
// ... (17 functions total)
```

### 4.29 Misc / Utility

```go
func Bool() bool
func UUID() string
func FlipACoin() string
func Dice(numDice, sides int) []int
func Weighted(options []string, weights []float64) string
func RandomString(a []string) string
func RandomInt(i []int) int
func ShuffleStrings(a []string)
func ShuffleInts(a []int)
func ShuffleAnySlice(v any)
func Slice(v interface{}) interface{}
func Struct(v interface{})
```

### 4.30 Movie / Music

```go
func Movie() *MovieInfo       // Name, Genre
func MovieName() string
func MovieGenre() string
func MusicGenre() string
func MusicName() string
func MusicArtist() string
```

### 4.31 Person

```go
func Person() *PersonInfo     // FirstName, LastName, Gender, Age, SSN, Hobby, Job, Address, Contact, CreditCard
func Name() string
func FirstName() string
func MiddleName() string
func LastName() string
func NamePrefix() string      // "Mr.", "Dr."
func NameSuffix() string      // "Jr.", "PhD"
func Gender() string          // "male" or "female"
func Age() int                // 0-100
func SSN() string
func Hobby() string
```

### 4.32 Product

```go
func Product() *ProductInfo
func ProductName() string
func ProductDescription() string
func ProductCategory() string
func ProductFeature() string
func ProductMaterial() string
func ProductUPC() string
func ProductAudience() string
func ProductDimension() string
func ProductUseCase() string
func ProductBenefit() string
func ProductSuffix() string
func ProductISBN() string
```

### 4.33 Grammar (Parts of Speech)

**Adjectives:** `Adjective`, `AdjectiveDemonstrative`, `AdjectiveDescriptive`, `AdjectiveIndefinite`, `AdjectiveInterrogative`, `AdjectivePossessive`, `AdjectiveProper`, `AdjectiveQuantitative`

**Adverbs:** `Adverb`, `AdverbDegree`, `AdverbFrequencyDefinite`, `AdverbFrequencyIndefinite`, `AdverbManner`, `AdverbPlace`, `AdverbTimeDefinite`, `AdverbTimeIndefinite`

**Nouns:** `Noun`, `NounAbstract`, `NounCollectiveAnimal`, `NounCollectivePeople`, `NounCollectiveThing`, `NounCommon`, `NounConcrete`, `NounCountable`, `NounProper`, `NounUncountable`

**Verbs:** `Verb`, `VerbAction`, `VerbHelping`, `VerbIntransitive`, `VerbLinking`, `VerbTransitive`

**Pronouns:** `Pronoun`, `PronounDemonstrative`, `PronounPersonal`, `PronounPossessive`, `PronounReflective`, `PronounRelative`

**Connectives / Prepositions:** `Connective`, `ConnectiveCasual`, `Preposition`, `PrepositionCompound`, `PrepositionSimple`, `Interjection`

### 4.34 Structured Data Format Generation

```go
func CSV(co *CSVOptions) string
func JSON(jo *JSONOptions) string
func XML(xo *XMLOptions) string
func FixedWidth(co *FixedWidthOptions) string
func SQL(so *SQLOptions) string
func Markdown(co *MarkdownOptions) string
func EmailText(co *EmailOptions) string
```

All options structs accept row counts, field definitions (`{Name, Function, Params}`), and format parameters.

---

## 5. Struct Tag System

`gofakeit.Struct(&v)` uses reflection to walk exported fields and populate them.

### 5.1 Tag reference

| Tag                             | Effect                     | Example                               |
|---------------------------------|----------------------------|---------------------------------------|
| `fake:"{function}"`             | Call named function        | `fake:"{firstname}"`                  |
| `fake:"{function:arg1,arg2}"`   | Function with parameters   | `fake:"{number:1,100}"`               |
| `fake:"{regex:[pattern]}"`      | Generate from RE2 regex    | `fake:"{regex:[a-z]{5}}"`             |
| `fake:"{randomstring:[a,b,c]}"` | Pick from list             | `fake:"{randomstring:[foo,bar,baz]}"` |
| `fake:"skip"` or `fake:"-"`     | Skip this field            |                                       |
| `fakesize:"n"`                  | Exact slice/map/array size | `fakesize:"5"`                        |
| `fakesize:"min,max"`            | Random size in range       | `fakesize:"2,8"`                      |
| `format:"layout"`               | `time.Time` parse layout   | `format:"2006-01-02"`                 |

### 5.2 `Fakeable` interface

```go
type Fakeable interface {
    Fake(f *Faker) (any, error)
}
```

### 5.3 Struct example

```go
type Order struct {
    ID         string    `fake:"{uuid}"`
    CustomerID int       `fake:"{number:1000,9999}"`
    FirstName  string    `fake:"{firstname}"`
    LastName   string    `fake:"{lastname}"`
    Email      string    `fake:"{email}"`
    Tags       []string  `fakesize:"3"`
    Ref        string    `fake:"{regex:[A-Z]{3}-[0-9]{6}}"`
    PlacedAt   time.Time `fake:"{year}-{month}-{day}" format:"2006-01-02"`
    TotalPrice float64   `fake:"{price:5.00,500.00}"`
    Internal   *string   `fake:"skip"`
}

var o Order
gofakeit.Struct(&o)
```

---

## 6. Template Generation

```go
func Template(template string, opts *TemplateOptions) string
```

All 310+ functions available as Go template actions.

```go
tmpl := `Subject: {{RandomString (SliceString "Hello" "Greetings" "Hi")}}

Dear {{LastName}},

{{Paragraph 1 3 8 "\n"}}

Best regards,
{{FirstName}} {{LastName}}
`

result, err := gofakeit.Template(tmpl, &gofakeit.TemplateOptions{})
```

---

## 7. Custom Functions

```go
gofakeit.AddFuncLookup("teamname", gofakeit.Info{
    Category:    "custom",
    Description: "Random sports team name",
    Example:     "Blue Hawks",
    Output:      "string",
    Generate: func(f *gofakeit.Faker, m *gofakeit.MapParams, info *gofakeit.Info) (any, error) {
        colors  := []string{"Blue", "Red", "Green", "Gold", "Silver"}
        animals := []string{"Hawks", "Lions", "Bears", "Wolves", "Eagles"}
        return f.RandomString(colors) + " " + f.RandomString(animals), nil
    },
})

gofakeit.Generate("{teamname}")    // "Gold Bears"
gofakeit.RemoveFuncLookup("teamname")
```

---

## 8. Mapping to krandom

### 8.1 Implemented in krandom

| gofakeit function                   | krandom class                       | Notes                  |
|-------------------------------------|-------------------------------------|------------------------|
| `Number(min, max)`                  | `IntGenerator`                      | Bounded                |
| `Int64()` / `Int32()` etc           | `LongGenerator`, `IntGenerator`     |                        |
| `Float64Range()` / `Float32Range()` | `FloatGenerator`, `DoubleGenerator` |                        |
| `Bool()`                            | `BooleanGenerator`                  |                        |
| `FirstName()`                       | `FirstName.kt`                      | EN only                |
| `LastName()`                        | `SurName.kt`                        | EN only                |
| `Email()`                           | `Email.kt`                          |                        |
| `Username()`                        | `Username.kt`                       |                        |
| `SSN()`                             | `SocialSecurityNumber.kt`           |                        |
| `IPv4Address()`                     | `IPv4Generator.java`                | RFC 791 unicast        |
| `IPv6Address()`                     | `IPv6Generator.java`                | RFC 4291 / 5952        |
| `Dice(n, sides)`                    | `DiceGenerator.java`                | D4/D6/D8/D10/D12/D20   |
| `FlipACoin()`                       | `CoinGenerator.java`                |                        |
| `Struct(&v)` (reflection fill)      | `ObjectGenerator.java`              | Reflection-based       |
| Luhn-valid number                   | `LuhnGenerator.java`                | 10-digit               |
| Fibonacci                           | `FibonacciGenerator.java`           | No gofakeit equivalent |
| Natural/prime/composite numbers     | `NaturalNumberGenerator.kt`         | No gofakeit equivalent |

### 8.2 Gaps — Tier 1 (high value, simple)

| gofakeit function                                      | Category  | Priority                                      |
|--------------------------------------------------------|-----------|-----------------------------------------------|
| `UUID()`                                               | Misc      | Wraps `java.util.UUID.randomUUID()` — trivial |
| `Date()`, `DateRange()`, `FutureDate()`, `PastDate()`  | Date/Time | Uses `java.time.*` already on classpath       |
| `Month()`, `Year()`, `WeekDay()`                       | Date/Time | Scalar date parts                             |
| `Password(...)`                                        | Internet  | Extend `StringGenerator` with policy params   |
| `Phone()` / `PhoneFormatted()`                         | Contact   | Format-string `Numerify` approach             |
| `City()`, `State()`, `Country()`, `Zip()`, `Street*()` | Address   | CSV-backed, mirrors `FirstName` pattern       |
| `CurrencyShort()` / `CurrencyLong()` / `Price()`       | Finance   | ISO 4217 lookup table                         |
| `MacAddress()`                                         | Network   | 6 hex octets — trivial                        |
| `HTTPMethod()` / `HTTPStatusCode()`                    | HTTP      | Static list / range                           |
| `UrlSlug(n)`                                           | Internet  | Compose lorem words with hyphens              |

### 8.3 Gaps — Tier 2 (medium value)

| gofakeit function                                                                 | Category | Notes                                    |
|-----------------------------------------------------------------------------------|----------|------------------------------------------|
| `URL()` / `DomainName()` / `DomainSuffix()`                                       | Internet | String composition                       |
| `UserAgent()`                                                                     | Internet | Static list of real UA strings           |
| `CreditCardNumber()` / `CreditCardType()` / `CreditCardExp()` / `CreditCardCvv()` | Finance  | Extend `LuhnGenerator` with BIN prefixes |
| `Company()` / `CompanySuffix()`                                                   | Company  | CSV-backed                               |
| `JobTitle()` / `JobLevel()`                                                       | Job      | CSV-backed                               |
| `Color()` / `HexColor()` / `RGBColor()`                                           | Color    | CSS named colors + hex                   |
| `LoremIpsumWord/Sentence/Paragraph()`                                             | Text     | Standard lorem word list                 |
| `FileExtension()` / `FileMimeType()`                                              | File     | Static lookup table                      |
| `AchRouting()` / `AchAccount()`                                                   | Finance  | US bank routing numbers                  |
| `BitcoinAddress()`                                                                | Finance  | P2PKH format                             |

### 8.4 Gaps — Tier 3 (niche / lower priority)

| gofakeit function                           | Category | Notes                           |
|---------------------------------------------|----------|---------------------------------|
| `Weighted(options, weights)`                | Utility  | `WeightedGenerator<T>` wrapper  |
| `Regex(pattern)`                            | Pattern  | RE2 regex string generation     |
| `Generate("{func}")` template               | Pattern  | Pattern-based string generation |
| Structured data (CSV, JSON, XML, SQL)       | Format   | Format-generation API           |
| Beer / Food / Animal sub-categories         | Domain   | Low practical demand            |
| Minecraft items                             | Domain   | Highly niche                    |
| Grammar sub-types (Adjective*, Noun*, etc.) | Grammar  | NLP / content generation        |
| `HackerPhrase()` / `Hipster*()`             | Fun      | Demo/placeholder data           |

### 8.5 Architectural comparison

| Aspect                   | gofakeit                                        | krandom                                                           |
|--------------------------|-------------------------------------------------|-------------------------------------------------------------------|
| **Reproducibility**      | Single seed on `Faker`; all calls deterministic | Each generator creates its own `SecureRandom`; no shared seed     |
| **PRNG**                 | PCG default; crypto optional                    | `SecureRandom` everywhere (crypto-strength by default)            |
| **Object graph filling** | `fake:` struct tags + `Struct(&v)`              | `ObjectGenerator<T>` via reflection; no annotation needed         |
| **Template / pattern**   | Full `text/template`; `Generate("{func:arg}")`  | Partial: `Numerify`/`Lexify` in `StringGenerator`; no full engine |
| **Weighted selection**   | `Weighted(options, weights)` built in           | Not available                                                     |
| **Regex generation**     | `Regex(pattern)` built in                       | Not available                                                     |
| **Structured output**    | CSV, JSON, XML, SQL, Markdown                   | Not available                                                     |

---

## 9. Quick-Reference Cheat Sheet

```go
// Person
gofakeit.Name()              // "Alice Smith"
gofakeit.FirstName()         // "Alice"
gofakeit.LastName()          // "Smith"
gofakeit.NamePrefix()        // "Dr."
gofakeit.NameSuffix()        // "Jr."
gofakeit.Email()             // "alice.smith@example.com"
gofakeit.Phone()             // "(570)245-7485"
gofakeit.PhoneFormatted()    // "570-245-7485"
gofakeit.Username()          // "alice_s"
gofakeit.SSN()               // "123-45-6789"
gofakeit.Gender()            // "female"
gofakeit.Age()               // 34

// Address
gofakeit.City()              // "Portland"
gofakeit.State()             // "Oregon"
gofakeit.StateAbr()          // "OR"
gofakeit.Country()           // "United States"
gofakeit.Zip()               // "97201"
gofakeit.Street()            // "123 Main St Apt 4B"
gofakeit.Latitude()          // 45.523452
gofakeit.Longitude()         // -122.676207

// Numbers
gofakeit.Number(1, 100)                          // 42
gofakeit.Float64Range(0.5, 9.99)                // 3.14
gofakeit.Bool()                                  // true
gofakeit.UUID()                                  // "590c1440-9888-45b0-bd51-a817ee07c3f2"
gofakeit.Digit()                                 // "7"
gofakeit.DigitN(6)                               // "481293"

// Strings / patterns
gofakeit.Numerify("###-###")                    // "481-293"
gofakeit.Lexify("???")                          // "fda"
gofakeit.Regex("[A-Z]{2}[0-9]{4}")             // "XW8421"
gofakeit.Password(true,true,true,true,false,16) // "aB3$kL9!mN2@xQ7#"

// Internet
gofakeit.URL()                                   // "https://example.com/path"
gofakeit.DomainName()                            // "example.com"
gofakeit.IPv4Address()                           // "192.168.1.1"
gofakeit.IPv6Address()                           // "2001:db8::1"
gofakeit.MacAddress()                            // "aa:bb:cc:dd:ee:ff"
gofakeit.UserAgent()                             // full UA string
gofakeit.HTTPMethod()                            // "GET"
gofakeit.HTTPStatusCode()                        // 404

// Finance
gofakeit.CreditCardNumber(nil)                   // "4287271570245748"
gofakeit.CreditCardType()                        // "Visa"
gofakeit.CreditCardExp()                         // "03/26"
gofakeit.CreditCardCvv()                         // "123"
gofakeit.CurrencyShort()                         // "USD"
gofakeit.Price(1, 100)                           // 42.99
gofakeit.AchRouting()                            // "021000021"
gofakeit.BitcoinAddress()                        // "1A1zP1..."

// Date / Time
gofakeit.Date()                                  // time.Time
gofakeit.FutureDate()                            // time.Time (after now)
gofakeit.PastDate()                              // time.Time (before now)
gofakeit.Month()                                 // 3
gofakeit.MonthString()                           // "March"
gofakeit.WeekDay()                               // "Monday"
gofakeit.Year()                                  // 2023
gofakeit.TimeZone()                              // "America/Los_Angeles"

// Color
gofakeit.Color()                                 // "MediumOrchid"
gofakeit.HexColor()                              // "#a45fb2"
gofakeit.RGBColor()                              // "rgb(164,95,178)"

// Games
gofakeit.FlipACoin()                             // "Heads"
gofakeit.Dice(2, 6)                              // []int{3, 5}

// Hacker
gofakeit.HackerPhrase()                          // "If we parse the bus, we can override the TCP feed!"

// Struct fill
var p gofakeit.PersonInfo
gofakeit.Struct(&p)
// p.FirstName, p.LastName, p.Job.Title, p.Address.City, etc. all populated
```
