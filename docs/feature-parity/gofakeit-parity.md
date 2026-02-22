# GoFakeit Feature Parity Analysis

## Library Overview

- **Name**: GoFakeit
- **Language**: Go
- **Version Analyzed**: v6.28.0 (v7 also available)
- **GitHub**: https://github.com/brianvoe/gofakeit
- **Docs**: https://pkg.go.dev/github.com/brianvoe/gofakeit/v6
- **License**: MIT
- **Key Strengths**: 310+ functions, zero dependencies, struct tagging system, template engine, regex generation, structured output formats

## Executive Summary

GoFakeit is a comprehensive fake data generation library for Go with 310+ functions across 50+ domain categories. Its standout features include:

- **Struct Tag Auto-Population**: Use `fake:` tags to automatically populate struct fields via reflection
- **Go Template Integration**: All 310+ functions available as template actions
- **Regex Pattern Generation**: Generate strings matching any RE2 regular expression
- **Multiple Output Formats**: CSV, JSON, XML, Markdown, SQL, fixed-width data generation
- **Extensible Architecture**: Register custom generators via `AddFuncLookup`
- **Zero Dependencies**: Pure Go standard library only
- **Multiple PRNG Backends**: PCG (default), ChaCha8, JSF, SFC, crypto/rand, deterministic sources
- **Thread-Safe**: Global faker uses mutex-guarded PCG; unlocked variants for single-threaded use

Unlike DataFaker which focuses on locale-based data variety, GoFakeit emphasizes **developer experience** through its struct tagging system, template engine, and pattern-based generation capabilities.
It's designed for Go developers who need quick, type-safe fake data with minimal boilerplate.

---

## Feature Categories

### 1. PERSON & IDENTITY

| Feature                    | GoFakeit Support                                         | krandom Status | Implementation Priority | Notes                                                                           |
|----------------------------|----------------------------------------------------------|----------------|-------------------------|---------------------------------------------------------------------------------|
| **Name Generation**        |
| Full name                  | ✅ `Name()`                                               | ✅ Yes          | ✓ DONE                  | Combines first + last                                                           |
| First name                 | ✅ `FirstName()`                                          | ✅ Yes          | ✓ DONE                  |                                                                                 |
| Middle name                | ✅ `MiddleName()`                                         | ❌ No           | MEDIUM                  | Common requirement                                                              |
| Last name                  | ✅ `LastName()`                                           | ✅ Yes          | ✓ DONE                  |                                                                                 |
| Name prefix                | ✅ `NamePrefix()` (Mr., Dr., Ms.)                         | ❌ No           | MEDIUM                  | Professional titles                                                             |
| Name suffix                | ✅ `NameSuffix()` (Jr., Sr., PhD, III)                    | ❌ No           | MEDIUM                  | Generational/academic                                                           |
| Gender-specific names      | ❌ No                                                     | ❌ No           | LOW                     | Not in GoFakeit                                                                 |
| **Demographics**           |
| Gender                     | ✅ `Gender()` (male/female)                               | ❌ No           | MEDIUM                  | Binary only                                                                     |
| Age                        | ✅ `Age()` (0-100)                                        | ❌ No           | MEDIUM                  | Random age                                                                      |
| SSN                        | ✅ `SSN()`                                                | ✅ Yes          | ✓ DONE                  | US Social Security                                                              |
| Hobby                      | ✅ `Hobby()`                                              | ❌ No           | LOW                     | Personal interests                                                              |
| **Complete Person Object** |
| PersonInfo struct          | ✅ `Person()` returns struct                              | ❌ No           | MEDIUM                  | FirstName, LastName, Gender, Age, SSN, Hobby, Job, Address, Contact, CreditCard |
| **Identifiers**            |
| Username                   | ✅ `Username()`                                           | ✅ Yes          | ✓ DONE                  |                                                                                 |
| Password                   | ✅ `Password(lower, upper, numeric, special, space, num)` | ❌ No           | HIGH                    | Policy-based generation                                                         |
| Passport number            | ❌ No                                                     | ❌ No           | LOW                     | Not in GoFakeit                                                                 |
| Driver's license           | ❌ No                                                     | ❌ No           | LOW                     | Not in GoFakeit                                                                 |

### 2. ADDRESS & LOCATION

| Feature                | GoFakeit Support                  | krandom Status | Implementation Priority | Notes                                                      |
|------------------------|-----------------------------------|----------------|-------------------------|------------------------------------------------------------|
| **Complete Address**   |
| Address struct         | ✅ `Address()` returns AddressInfo | ❌ No           | HIGH                    | Address, Street, Unit, City, State, Zip, Country, Lat, Lon |
| Full address string    | ✅ `Street()`                      | ❌ No           | HIGH                    | "123 Main St Apt 4B"                                       |
| **Street Components**  |
| Street name            | ✅ `StreetName()`                  | ❌ No           | HIGH                    | "Main", "Oak", "First"                                     |
| Street number          | ✅ `StreetNumber()`                | ❌ No           | HIGH                    | "123", "4567"                                              |
| Street prefix          | ✅ `StreetPrefix()`                | ❌ No           | MEDIUM                  | "North", "East"                                            |
| Street suffix          | ✅ `StreetSuffix()`                | ❌ No           | MEDIUM                  | "St", "Ave", "Blvd", "Rd"                                  |
| Unit/Apartment         | ✅ `Unit()`                        | ❌ No           | MEDIUM                  | "Apt 4B", "Suite 200"                                      |
| **City & State**       |
| City                   | ✅ `City()`                        | ❌ No           | HIGH                    | "Portland", "Austin"                                       |
| State                  | ✅ `State()`                       | ❌ No           | HIGH                    | "Oregon", "Texas"                                          |
| State abbreviation     | ✅ `StateAbr()`                    | ❌ No           | HIGH                    | "OR", "TX"                                                 |
| **Postal Codes**       |
| ZIP code               | ✅ `Zip()`                         | ❌ No           | HIGH                    | US 5-digit                                                 |
| Postcode (generic)     | ❌ No (use Zip)                    | ❌ No           | MEDIUM                  | International support                                      |
| **Country**            |
| Country name           | ✅ `Country()`                     | ❌ No           | HIGH                    | "United States", "Canada"                                  |
| Country abbreviation   | ✅ `CountryAbr()`                  | ❌ No           | HIGH                    | "US", "CA" (2-letter)                                      |
| Country code 3         | ❌ No                              | ❌ No           | MEDIUM                  | ISO 3-letter codes                                         |
| **Coordinates**        |
| Latitude               | ✅ `Latitude()`                    | ❌ No           | MEDIUM                  | -90 to 90                                                  |
| Longitude              | ✅ `Longitude()`                   | ❌ No           | MEDIUM                  | -180 to 180                                                |
| Latitude in range      | ✅ `LatitudeInRange(min, max)`     | ❌ No           | MEDIUM                  | Bounded coordinates                                        |
| Longitude in range     | ✅ `LongitudeInRange(min, max)`    | ❌ No           | MEDIUM                  |                                                            |
| **Direction**          |
| Compass direction      | ❌ No                              | ❌ No           | LOW                     | N, NE, NNE not in GoFakeit                                 |
| Time zone              | ✅ `TimeZone()`                    | ❌ No           | MEDIUM                  | Full timezone name                                         |
| Time zone abbreviation | ✅ `TimeZoneAbv()`                 | ❌ No           | MEDIUM                  | PST, EST                                                   |
| Time zone full         | ✅ `TimeZoneFull()`                | ❌ No           | MEDIUM                  | With GMT offset                                            |
| Time zone offset       | ✅ `TimeZoneOffset()`              | ❌ No           | MEDIUM                  | +/-HH:MM                                                   |
| Time zone region       | ✅ `TimeZoneRegion()`              | ❌ No           | MEDIUM                  | Geographic region                                          |

### 3. CONTACT & COMMUNICATION

| Feature             | GoFakeit Support     | krandom Status | Implementation Priority | Notes                     |
|---------------------|----------------------|----------------|-------------------------|---------------------------|
| **Email**           |
| Email address       | ✅ `Email()`          | ✅ Yes          | ✓ DONE                  |                           |
| Safe email          | ❌ No                 | ❌ No           | MEDIUM                  | example.com domain        |
| Email subject       | ❌ No                 | ❌ No           | LOW                     | Not in GoFakeit           |
| Email text          | ✅ `EmailText(opts)`  | ❌ No           | LOW                     | Generated email content   |
| **Contact Info**    |
| ContactInfo struct  | ✅ `Contact()`        | ❌ No           | MEDIUM                  | Phone, Email              |
| **Phone**           |
| Phone number        | ✅ `Phone()`          | ❌ No           | HIGH                    | Random format             |
| Phone formatted     | ✅ `PhoneFormatted()` | ❌ No           | HIGH                    | (555) 123-4567            |
| International phone | ❌ No                 | ❌ No           | MEDIUM                  | +1 format not in GoFakeit |
| Extension           | ❌ No                 | ❌ No           | LOW                     | Not in GoFakeit           |

### 4. INTERNET & NETWORKING

| Feature            | GoFakeit Support            | krandom Status | Implementation Priority | Notes                   |
|--------------------|-----------------------------|----------------|-------------------------|-------------------------|
| **URLs & Domains** |
| URL                | ✅ `URL()`                   | ❌ No           | HIGH                    | Full URL with protocol  |
| Domain name        | ✅ `DomainName()`            | ❌ No           | HIGH                    | example.com             |
| Domain suffix      | ✅ `DomainSuffix()`          | ❌ No           | MEDIUM                  | .com, .org, .io         |
| URL slug           | ✅ `UrlSlug(words)`          | ❌ No           | MEDIUM                  | my-awesome-slug         |
| **IP Addresses**   |
| IPv4               | ✅ `IPv4Address()`           | ✅ Yes          | ✓ DONE                  | RFC 791                 |
| IPv6               | ✅ `IPv6Address()`           | ✅ Yes          | ✓ DONE                  | RFC 4291                |
| Private IPv4       | ❌ No                        | ❌ No           | MEDIUM                  | RFC1918 ranges          |
| Public IPv4        | ❌ No                        | ❌ No           | MEDIUM                  | Non-private             |
| IPv4 CIDR          | ❌ No                        | ❌ No           | MEDIUM                  | Network notation        |
| IPv6 CIDR          | ❌ No                        | ❌ No           | MEDIUM                  |                         |
| **Network**        |
| MAC address        | ✅ `MacAddress()`            | ❌ No           | MEDIUM                  | aa:bb:cc:dd:ee:ff       |
| Port               | ❌ No                        | ❌ No           | MEDIUM                  | 1-65535                 |
| **HTTP**           |
| HTTP method        | ✅ `HTTPMethod()`            | ❌ No           | MEDIUM                  | GET, POST, PUT, DELETE  |
| HTTP status code   | ✅ `HTTPStatusCode()`        | ❌ No           | MEDIUM                  | 100-511 range           |
| HTTP status simple | ✅ `HTTPStatusCodeSimple()`  | ❌ No           | MEDIUM                  | 200, 301, 302, 404, 500 |
| HTTP version       | ✅ `HTTPVersion()`           | ❌ No           | LOW                     | HTTP/1.1, HTTP/2.0      |
| **User Agents**    |
| User agent         | ✅ `UserAgent()`             | ❌ No           | MEDIUM                  | Random browser UA       |
| Chrome UA          | ✅ `ChromeUserAgent()`       | ❌ No           | LOW                     | Specific browser        |
| Firefox UA         | ✅ `FirefoxUserAgent()`      | ❌ No           | LOW                     |                         |
| Safari UA          | ✅ `SafariUserAgent()`       | ❌ No           | LOW                     |                         |
| Opera UA           | ✅ `OperaUserAgent()`        | ❌ No           | LOW                     |                         |
| API UA             | ✅ `APIUserAgent()`          | ❌ No           | LOW                     | Bot/API user agent      |
| **Other**          |
| Input name         | ✅ `InputName()`             | ❌ No           | LOW                     | HTML input field names  |
| Log level          | ✅ `LogLevel(type)`          | ❌ No           | LOW                     | apache, nginx, syslog   |
| Image URL          | ✅ `ImageURL(width, height)` | ❌ No           | LOW                     | picsum.photos           |

### 5. FINANCE & PAYMENT

| Feature               | GoFakeit Support                                | krandom Status | Implementation Priority | Notes                        |
|-----------------------|-------------------------------------------------|----------------|-------------------------|------------------------------|
| **Credit Cards**      |
| CreditCardInfo struct | ✅ `CreditCard()`                                | ❌ No           | HIGH                    | Type, Number, Exp, CVV       |
| Credit card number    | ✅ `CreditCardNumber(opts)`                      | ❌ No           | HIGH                    | Luhn-valid                   |
| Credit card type      | ✅ `CreditCardType()`                            | ❌ No           | MEDIUM                  | Visa, Mastercard, Amex, etc. |
| Credit card expiry    | ✅ `CreditCardExp()`                             | ❌ No           | HIGH                    | MM/YY format                 |
| Credit card CVV       | ✅ `CreditCardCvv()`                             | ❌ No           | HIGH                    | 3-4 digits                   |
| Card types supported  | ✅ Visa, Mastercard, Amex, Discover, Diners, JCB | ❌ No           | HIGH                    | Comprehensive                |
| **Banking**           |
| ACH routing           | ✅ `AchRouting()`                                | ❌ No           | MEDIUM                  | US routing numbers           |
| ACH account           | ✅ `AchAccount()`                                | ❌ No           | MEDIUM                  | US account numbers           |
| Bank name             | ✅ `BankName()`                                  | ❌ No           | LOW                     |                              |
| Bank type             | ✅ `BankType()`                                  | ❌ No           | LOW                     | Checking, Savings            |
| SWIFT/BIC             | ❌ No                                            | ❌ No           | MEDIUM                  | International banking        |
| IBAN                  | ❌ No                                            | ❌ No           | MEDIUM                  | International account        |
| **Cryptocurrency**    |
| Bitcoin address       | ✅ `BitcoinAddress()`                            | ❌ No           | MEDIUM                  | P2PKH format                 |
| Bitcoin private key   | ✅ `BitcoinPrivateKey()`                         | ❌ No           | LOW                     | WIF format                   |
| **Currency**          |
| CurrencyInfo struct   | ✅ `Currency()`                                  | ❌ No           | HIGH                    | Short, Long                  |
| Currency short        | ✅ `CurrencyShort()`                             | ❌ No           | HIGH                    | USD, EUR, GBP                |
| Currency long         | ✅ `CurrencyLong()`                              | ❌ No           | HIGH                    | "United States Dollar"       |
| Currency symbol       | ❌ No                                            | ❌ No           | MEDIUM                  | $, €, £                      |
| Price                 | ✅ `Price(min, max)`                             | ❌ No           | HIGH                    | Random price in range        |
| **Securities**        |
| CUSIP                 | ✅ `Cusip()`                                     | ❌ No           | LOW                     | US securities ID             |
| ISIN                  | ✅ `Isin()`                                      | ❌ No           | LOW                     | International securities     |
| **Tax & Employment**  |
| SSN                   | ✅ `SSN()`                                       | ✅ Yes          | ✓ DONE                  | Social Security Number       |
| EIN                   | ✅ `EIN()`                                       | ❌ No           | MEDIUM                  | Employer ID Number           |

### 6. COMPANY & BUSINESS

| Feature        | GoFakeit Support    | krandom Status | Implementation Priority | Notes                             |
|----------------|---------------------|----------------|-------------------------|-----------------------------------|
| **Company**    |
| Company name   | ✅ `Company()`       | ❌ No           | HIGH                    | Random company                    |
| Company suffix | ✅ `CompanySuffix()` | ❌ No           | MEDIUM                  | Inc, LLC, Ltd                     |
| Industry       | ❌ No                | ❌ No           | MEDIUM                  | Not in GoFakeit                   |
| **Job**        |
| JobInfo struct | ✅ `Job()`           | ❌ No           | MEDIUM                  | Company, Title, Descriptor, Level |
| Job title      | ✅ `JobTitle()`      | ✅ Yes          | ✓ DONE                  |                                   |
| Job descriptor | ✅ `JobDescriptor()` | ❌ No           | MEDIUM                  | Senior, Lead, Chief               |
| Job level      | ✅ `JobLevel()`      | ❌ No           | MEDIUM                  | Entry, Mid, Senior                |
| **Marketing**  |
| BS phrase      | ✅ `BS()`            | ❌ No           | LOW                     | Corporate BS                      |
| Buzzword       | ✅ `BuzzWord()`      | ❌ No           | LOW                     | Synergy, leverage                 |
| Slogan         | ✅ `Slogan()`        | ❌ No           | LOW                     | Company slogans                   |

### 7. DATE & TIME

| Feature             | GoFakeit Support          | krandom Status | Implementation Priority | Notes                 |
|---------------------|---------------------------|----------------|-------------------------|-----------------------|
| **Date Generation** |
| Random date         | ✅ `Date()`                | ❌ No           | HIGH                    | Random time.Time      |
| Date range          | ✅ `DateRange(start, end)` | ❌ No           | HIGH                    | Between two dates     |
| Future date         | ✅ `FutureDate()`          | ❌ No           | HIGH                    | After now             |
| Past date           | ✅ `PastDate()`            | ❌ No           | HIGH                    | Before now            |
| Birthday            | ❌ No                      | ❌ No           | MEDIUM                  | Age-appropriate       |
| **Date Components** |
| Day                 | ✅ `Day()`                 | ❌ No           | MEDIUM                  | 1-31                  |
| Month               | ✅ `Month()`               | ❌ No           | MEDIUM                  | 1-12                  |
| Month string        | ✅ `MonthString()`         | ❌ No           | MEDIUM                  | "January", "February" |
| Year                | ✅ `Year()`                | ❌ No           | MEDIUM                  | Random year           |
| Weekday             | ✅ `WeekDay()`             | ❌ No           | MEDIUM                  | "Monday", "Tuesday"   |
| **Time Components** |
| Hour                | ✅ `Hour()`                | ❌ No           | LOW                     | 0-23                  |
| Minute              | ✅ `Minute()`              | ❌ No           | LOW                     | 0-59                  |
| Second              | ✅ `Second()`              | ❌ No           | LOW                     | 0-59                  |
| Nanosecond          | ✅ `NanoSecond()`          | ❌ No           | LOW                     | 0-999999999           |
| **Duration**        |
| Duration            | ❌ No                      | ❌ No           | MEDIUM                  | Time spans            |
| Period              | ❌ No                      | ❌ No           | MEDIUM                  | Date periods          |

### 8. TEXT & LOREM

| Feature              | GoFakeit Support                                     | krandom Status | Implementation Priority | Notes                 |
|----------------------|------------------------------------------------------|----------------|-------------------------|-----------------------|
| **Lorem Ipsum**      |
| Lorem word           | ✅ `LoremIpsumWord()`                                 | ❌ No           | HIGH                    | Single word           |
| Lorem sentence       | ✅ `LoremIpsumSentence(wordCount)`                    | ❌ No           | HIGH                    | N words               |
| Lorem paragraph      | ✅ `LoremIpsumParagraph(pCount, sCount, wCount, sep)` | ❌ No           | HIGH                    | Structured paragraphs |
| **General Text**     |
| Word                 | ✅ `Word()`                                           | ❌ No           | HIGH                    | Random word           |
| Sentence             | ✅ `Sentence(wordCount)`                              | ❌ No           | HIGH                    | N words sentence      |
| Sentence simple      | ✅ `SentenceSimple()`                                 | ❌ No           | MEDIUM                  | Default word count    |
| Paragraph            | ✅ `Paragraph(pCount, sCount, wCount, sep)`           | ❌ No           | HIGH                    | Full paragraphs       |
| Quote                | ✅ `Quote()`                                          | ❌ No           | LOW                     | Famous quotes         |
| Question             | ✅ `Question()`                                       | ❌ No           | MEDIUM                  | Sentence ending in ?  |
| **Phrases**          |
| Phrase               | ✅ `Phrase()`                                         | ❌ No           | MEDIUM                  | Random phrase         |
| Phrase noun          | ✅ `PhraseNoun()`                                     | ❌ No           | LOW                     | Noun-based phrase     |
| Phrase verb          | ✅ `PhraseVerb()`                                     | ❌ No           | LOW                     | Verb-based phrase     |
| Comment              | ✅ `Comment()`                                        | ❌ No           | LOW                     | Code comment text     |
| **Specialized Text** |
| Hipster word         | ✅ `HipsterWord()`                                    | ❌ No           | LOW                     | Trendy words          |
| Hipster sentence     | ✅ `HipsterSentence(wordCount)`                       | ❌ No           | LOW                     |                       |
| Hipster paragraph    | ✅ `HipsterParagraph(pCount, sCount, wCount, sep)`    | ❌ No           | LOW                     |                       |

### 9. NUMBERS & PRIMITIVES

| Feature             | GoFakeit Support           | krandom Status | Implementation Priority | Notes              |
|---------------------|----------------------------|----------------|-------------------------|--------------------|
| **Integer Types**   |
| Int                 | ✅ `Int()`                  | ✅ Yes          | ✓ DONE                  |                    |
| Int8                | ✅ `Int8()`                 | ✅ Yes          | ✓ DONE                  |                    |
| Int16               | ✅ `Int16()`                | ✅ Yes          | ✓ DONE                  |                    |
| Int32               | ✅ `Int32()`                | ✅ Yes          | ✓ DONE                  |                    |
| Int64               | ✅ `Int64()`                | ✅ Yes          | ✓ DONE                  |                    |
| Uint                | ✅ `Uint()`                 | ❌ No           | MEDIUM                  | Unsigned int       |
| Uint8               | ✅ `Uint8()`                | ❌ No           | MEDIUM                  |                    |
| Uint16              | ✅ `Uint16()`               | ❌ No           | MEDIUM                  |                    |
| Uint32              | ✅ `Uint32()`               | ❌ No           | MEDIUM                  |                    |
| Uint64              | ✅ `Uint64()`               | ❌ No           | MEDIUM                  |                    |
| **Float Types**     |
| Float32             | ✅ `Float32()`              | ✅ Yes          | ✓ DONE                  |                    |
| Float64             | ✅ `Float64()`              | ✅ Yes          | ✓ DONE                  |                    |
| Float32 range       | ✅ `Float32Range(min, max)` | ✅ Yes          | ✓ DONE                  |                    |
| Float64 range       | ✅ `Float64Range(min, max)` | ✅ Yes          | ✓ DONE                  |                    |
| **Bounded Numbers** |
| Number              | ✅ `Number(min, max)`       | ✅ Yes          | ✓ DONE                  | Int range          |
| IntN                | ✅ `IntN(n)`                | ✅ Yes          | ✓ DONE                  | 0 to n-1           |
| IntRange            | ✅ `IntRange(min, max)`     | ✅ Yes          | ✓ DONE                  |                    |
| **Digits**          |
| Digit               | ✅ `Digit()`                | ✅ Yes          | ✓ DONE                  | "0"-"9"            |
| DigitN              | ✅ `DigitN(n)`              | ❌ No           | MEDIUM                  | N digits as string |
| Digit not zero      | ❌ No                       | ❌ No           | LOW                     | 1-9                |
| **Boolean**         |
| Bool                | ✅ `Bool()`                 | ✅ Yes          | ✓ DONE                  |                    |
| **UUID**            |
| UUID                | ✅ `UUID()`                 | ❌ No           | HIGH                    | v4 UUID            |
| UUID v3             | ❌ No                       | ❌ No           | LOW                     | Name-based         |
| UUID v7             | ❌ No                       | ❌ No           | LOW                     | Time-ordered       |

### 10. STRING PATTERNS & TEMPLATES

| Feature             | GoFakeit Support         | krandom Status | Implementation Priority | Notes                        |
|---------------------|--------------------------|----------------|-------------------------|------------------------------|
| **Pattern-Based**   |
| Numerify            | ✅ `Numerify(str)`        | ❌ No           | HIGH                    | '#' → random digit           |
| Lexify              | ✅ `Lexify(str)`          | ❌ No           | HIGH                    | '?' → random letter          |
| Bothify             | ✅ `Bothify(str)`         | ❌ No           | HIGH                    | Combine numerify + lexify    |
| Asciify             | ✅ `Asciify(str)`         | ❌ No           | MEDIUM                  | '*' → random ASCII printable |
| **Template System** |
| Generate            | ✅ `Generate(dataVal)`    | ❌ No           | HIGH                    | {function} placeholders      |
| Template            | ✅ `Template(tmpl, opts)` | ❌ No           | HIGH                    | Full Go template engine      |
| **Regex**           |
| Regex generation    | ✅ `Regex(pattern)`       | ❌ No           | MEDIUM                  | Generate from RE2 pattern    |
| **Map Generation**  |
| Random map          | ✅ `Map()`                | ❌ No           | LOW                     | Heterogeneous map            |

### 11. STRUCTURED DATA OUTPUT

| Feature               | GoFakeit Support         | krandom Status | Implementation Priority | Notes               |
|-----------------------|--------------------------|----------------|-------------------------|---------------------|
| **Format Generation** |
| CSV                   | ✅ `CSV(opts)`            | ❌ No           | MEDIUM                  | Table data output   |
| JSON                  | ✅ `JSON(opts)`           | ❌ No           | MEDIUM                  | JSON object/array   |
| XML                   | ✅ `XML(opts)`            | ❌ No           | MEDIUM                  | XML documents       |
| SQL                   | ✅ `SQL(opts)`            | ❌ No           | MEDIUM                  | INSERT statements   |
| Markdown              | ✅ `Markdown(opts)`       | ❌ No           | LOW                     | Markdown tables     |
| Fixed-width           | ✅ `FixedWidth(opts)`     | ❌ No           | LOW                     | Fixed-width columns |
| **Options Structure** |
| Row counts            | ✅ All formats support    | ❌ No           | MEDIUM                  | Configurable rows   |
| Field definitions     | ✅ Name, Function, Params | ❌ No           | MEDIUM                  | Custom fields       |

### 12. OBJECT & STRUCT GENERATION

| Feature               | GoFakeit Support                         | krandom Status | Implementation Priority | Notes                           |
|-----------------------|------------------------------------------|----------------|-------------------------|---------------------------------|
| **Struct Population** |
| Struct tagging        | ✅ `fake:` tags                           | ❌ No           | HIGH                    | `fake:"{function}"`             |
| Struct reflection     | ✅ `Struct(&v)`                           | ✅ Yes          | ✓ DONE                  | ObjectGenerator uses reflection |
| Skip fields           | ✅ `fake:"skip"` or `fake:"-"`            | ❌ No           | MEDIUM                  | Exclude fields                  |
| Regex tags            | ✅ `fake:"{regex:[pattern]}"`             | ❌ No           | MEDIUM                  | Pattern-based fields            |
| Random string tags    | ✅ `fake:"{randomstring:[a,b,c]}"`        | ❌ No           | MEDIUM                  | Choice from list                |
| Size control          | ✅ `fakesize:"n"` or `fakesize:"min,max"` | ❌ No           | MEDIUM                  | Slice/map/array size            |
| Time format           | ✅ `format:"layout"`                      | ❌ No           | MEDIUM                  | time.Time parsing               |
| Fakeable interface    | ✅ `Fake(*Faker) (any, error)`            | ❌ No           | MEDIUM                  | Custom field generation         |
| **Slice Generation**  |
| Slice                 | ✅ `Slice(v)`                             | ❌ No           | MEDIUM                  | Populate any slice              |

### 13. COLLECTIONS & UTILITIES

| Feature           | GoFakeit Support               | krandom Status | Implementation Priority | Notes             |
|-------------------|--------------------------------|----------------|-------------------------|-------------------|
| **Selection**     |
| Weighted choice   | ✅ `Weighted(options, weights)` | ❌ No           | MEDIUM                  | Probability-based |
| Random string     | ✅ `RandomString([]string)`     | ❌ No           | MEDIUM                  | Pick from array   |
| Random int        | ✅ `RandomInt([]int)`           | ❌ No           | MEDIUM                  | Pick from array   |
| **Shuffling**     |
| Shuffle strings   | ✅ `ShuffleStrings([]string)`   | ❌ No           | LOW                     | In-place shuffle  |
| Shuffle ints      | ✅ `ShuffleInts([]int)`         | ❌ No           | LOW                     |                   |
| Shuffle any slice | ✅ `ShuffleAnySlice(any)`       | ❌ No           | LOW                     | Generic shuffle   |

### 14. GAMES & ENTERTAINMENT

| Feature                 | GoFakeit Support            | krandom Status | Implementation Priority | Notes                 |
|-------------------------|-----------------------------|----------------|-------------------------|-----------------------|
| **Games**               |
| Dice                    | ✅ `Dice(numDice, sides)`    | ✅ Yes          | ✓ DONE                  | D4/D6/D8/D10/D12/D20  |
| Flip coin               | ✅ `FlipACoin()`             | ✅ Yes          | ✓ DONE                  | Heads/Tails           |
| Gamertag                | ✅ `Gamertag()`              | ❌ No           | LOW                     | Gaming usernames      |
| **Minecraft**           |
| Minecraft animal        | ✅ `MinecraftAnimal()`       | ❌ No           | LOW                     | Cow, Pig, Chicken     |
| Minecraft biome         | ✅ `MinecraftBiome()`        | ❌ No           | LOW                     | Plains, Forest        |
| Minecraft dye           | ✅ `MinecraftDye()`          | ❌ No           | LOW                     | Red, Blue             |
| Minecraft food          | ✅ `MinecraftFood()`         | ❌ No           | LOW                     | Bread, Apple          |
| Minecraft mob boss      | ✅ `MinecraftMobBoss()`      | ❌ No           | LOW                     | Ender Dragon          |
| Minecraft mob hostile   | ✅ `MinecraftMobHostile()`   | ❌ No           | LOW                     | Zombie, Creeper       |
| Minecraft ore           | ✅ `MinecraftOre()`          | ❌ No           | LOW                     | Diamond, Gold         |
| Minecraft tool          | ✅ `MinecraftTool()`         | ❌ No           | LOW                     | Pickaxe, Shovel       |
| Minecraft weapon        | ✅ `MinecraftWeapon()`       | ❌ No           | LOW                     | Sword, Bow            |
| Minecraft wood          | ✅ `MinecraftWood()`         | ❌ No           | LOW                     | Oak, Birch            |
| Minecraft armor tier    | ✅ `MinecraftArmorTier()`    | ❌ No           | LOW                     | Leather, Iron         |
| Minecraft armor part    | ✅ `MinecraftArmorPart()`    | ❌ No           | LOW                     | Helmet, Chestplate    |
| Minecraft enchantment   | ✅ `MinecraftEnchantment()`  | ❌ No           | LOW                     | Sharpness, Protection |
| Minecraft item          | ✅ `MinecraftItem()`         | ❌ No           | LOW                     | Random item           |
| Minecraft village job   | ✅ `MinecraftVillageJob()`   | ❌ No           | LOW                     | Farmer, Blacksmith    |
| Minecraft village level | ✅ `MinecraftVillageLevel()` | ❌ No           | LOW                     | Novice, Expert        |
| Minecraft weather       | ✅ `MinecraftWeather()`      | ❌ No           | LOW                     | Clear, Rain           |

### 15. COLORS

| Feature        | GoFakeit Support | krandom Status | Implementation Priority | Notes                          |
|----------------|------------------|----------------|-------------------------|--------------------------------|
| Color name     | ✅ `Color()`      | ❌ No           | MEDIUM                  | "MediumOrchid"                 |
| Hex color      | ✅ `HexColor()`   | ❌ No           | MEDIUM                  | "#a45fb2"                      |
| RGB color      | ✅ `RGBColor()`   | ❌ No           | MEDIUM                  | "rgb(123,45,67)"               |
| Safe color     | ✅ `SafeColor()`  | ❌ No           | LOW                     | Web-safe colors                |
| Nice colors    | ✅ `NiceColors()` | ❌ No           | LOW                     | Aesthetically pleasing palette |
| **Hex Values** |
| Hex uint8      | ✅ `HexUint8()`   | ❌ No           | LOW                     | 2 hex digits                   |
| Hex uint16     | ✅ `HexUint16()`  | ❌ No           | LOW                     | 4 hex digits                   |
| Hex uint32     | ✅ `HexUint32()`  | ❌ No           | LOW                     | 8 hex digits                   |
| Hex uint64     | ✅ `HexUint64()`  | ❌ No           | LOW                     | 16 hex digits                  |
| Hex uint128    | ✅ `HexUint128()` | ❌ No           | LOW                     | 32 hex digits                  |
| Hex uint256    | ✅ `HexUint256()` | ❌ No           | LOW                     | 64 hex digits                  |

### 16. FOOD & DRINK

| Feature          | GoFakeit Support  | krandom Status | Implementation Priority | Notes            |
|------------------|-------------------|----------------|-------------------------|------------------|
| **General Food** |
| Fruit            | ✅ `Fruit()`       | ❌ No           | LOW                     | Apple, Banana    |
| Vegetable        | ✅ `Vegetable()`   | ❌ No           | LOW                     | Carrot, Broccoli |
| Breakfast        | ✅ `Breakfast()`   | ❌ No           | LOW                     | Pancakes, Eggs   |
| Lunch            | ✅ `Lunch()`       | ❌ No           | LOW                     | Sandwich, Salad  |
| Dinner           | ✅ `Dinner()`      | ❌ No           | LOW                     | Steak, Pasta     |
| Snack            | ✅ `Snack()`       | ❌ No           | LOW                     | Chips, Nuts      |
| Dessert          | ✅ `Dessert()`     | ❌ No           | LOW                     | Cake, Ice cream  |
| Drink            | ✅ `Drink()`       | ❌ No           | LOW                     | Water, Soda      |
| **Beer**         |
| Beer name        | ✅ `BeerName()`    | ❌ No           | LOW                     | "Stone IPA"      |
| Beer style       | ✅ `BeerStyle()`   | ❌ No           | LOW                     | IPA, Stout       |
| Beer hop         | ✅ `BeerHop()`     | ❌ No           | LOW                     | Cascade, Citra   |
| Beer yeast       | ✅ `BeerYeast()`   | ❌ No           | LOW                     | Ale, Lager       |
| Beer malt        | ✅ `BeerMalt()`    | ❌ No           | LOW                     | Pale, Caramel    |
| Beer IBU         | ✅ `BeerIbu()`     | ❌ No           | LOW                     | Bitterness units |
| Beer alcohol     | ✅ `BeerAlcohol()` | ❌ No           | LOW                     | ABV %            |
| Beer BLG         | ✅ `BeerBlg()`     | ❌ No           | LOW                     | Gravity          |

### 17. ANIMALS

| Feature             | GoFakeit Support | krandom Status | Implementation Priority | Notes              |
|---------------------|------------------|----------------|-------------------------|--------------------|
| **General Animals** |
| Animal              | ✅ `Animal()`     | ❌ No           | LOW                     | Random animal      |
| Animal type         | ✅ `AnimalType()` | ❌ No           | LOW                     | Mammal, Bird, Fish |
| Bird                | ✅ `Bird()`       | ❌ No           | LOW                     | Eagle, Sparrow     |
| Cat                 | ✅ `Cat()`        | ❌ No           | LOW                     | Persian, Siamese   |
| Dog                 | ✅ `Dog()`        | ❌ No           | LOW                     | Labrador, Beagle   |
| Farm animal         | ✅ `FarmAnimal()` | ❌ No           | LOW                     | Cow, Chicken       |
| Pet name            | ✅ `PetName()`    | ❌ No           | LOW                     | Fluffy, Max        |

### 18. MEDIA & ENTERTAINMENT

| Feature      | GoFakeit Support  | krandom Status | Implementation Priority | Notes                |
|--------------|-------------------|----------------|-------------------------|----------------------|
| **Books**    |
| Book info    | ✅ `Book()`        | ❌ No           | LOW                     | Title, Author, Genre |
| Book title   | ✅ `BookTitle()`   | ❌ No           | LOW                     |                      |
| Book author  | ✅ `BookAuthor()`  | ❌ No           | LOW                     |                      |
| Book genre   | ✅ `BookGenre()`   | ❌ No           | LOW                     | Fiction, Mystery     |
| **Movies**   |
| Movie info   | ✅ `Movie()`       | ❌ No           | LOW                     | Name, Genre          |
| Movie name   | ✅ `MovieName()`   | ❌ No           | LOW                     |                      |
| Movie genre  | ✅ `MovieGenre()`  | ❌ No           | LOW                     | Action, Drama        |
| **Music**    |
| Music genre  | ✅ `MusicGenre()`  | ❌ No           | LOW                     | Rock, Jazz           |
| Music name   | ✅ `MusicName()`   | ❌ No           | LOW                     | Song/album name      |
| Music artist | ✅ `MusicArtist()` | ❌ No           | LOW                     | Artist/band name     |

### 19. VEHICLES

| Feature          | GoFakeit Support          | krandom Status | Implementation Priority | Notes                                        |
|------------------|---------------------------|----------------|-------------------------|----------------------------------------------|
| **Cars**         |
| Car info         | ✅ `Car()`                 | ❌ No           | LOW                     | Type, Fuel, Transmission, Brand, Model, Year |
| Car maker        | ✅ `CarMaker()`            | ❌ No           | LOW                     | Toyota, Ford                                 |
| Car model        | ✅ `CarModel()`            | ❌ No           | LOW                     | Camry, F-150                                 |
| Car type         | ✅ `CarType()`             | ❌ No           | LOW                     | Sedan, SUV                                   |
| Car fuel type    | ✅ `CarFuelType()`         | ❌ No           | LOW                     | Gasoline, Diesel                             |
| Car transmission | ✅ `CarTransmissionType()` | ❌ No           | LOW                     | Automatic, Manual                            |

### 20. CELEBRITY

| Feature            | GoFakeit Support        | krandom Status | Implementation Priority | Notes         |
|--------------------|-------------------------|----------------|-------------------------|---------------|
| Celebrity actor    | ✅ `CelebrityActor()`    | ❌ No           | LOW                     | Famous actors |
| Celebrity business | ✅ `CelebrityBusiness()` | ❌ No           | LOW                     | Entrepreneurs |
| Celebrity sport    | ✅ `CelebritySport()`    | ❌ No           | LOW                     | Athletes      |

### 21. APP & SOFTWARE

| Feature     | GoFakeit Support | krandom Status | Implementation Priority | Notes            |
|-------------|------------------|----------------|-------------------------|------------------|
| App name    | ✅ `AppName()`    | ❌ No           | LOW                     | Random app       |
| App version | ✅ `AppVersion()` | ❌ No           | LOW                     | Semantic version |
| App author  | ✅ `AppAuthor()`  | ❌ No           | LOW                     | Developer name   |

### 22. PRODUCT & COMMERCE

| Feature             | GoFakeit Support         | krandom Status | Implementation Priority | Notes                      |
|---------------------|--------------------------|----------------|-------------------------|----------------------------|
| Product info        | ✅ `Product()`            | ❌ No           | MEDIUM                  | Comprehensive product data |
| Product name        | ✅ `ProductName()`        | ❌ No           | MEDIUM                  | E-commerce                 |
| Product description | ✅ `ProductDescription()` | ❌ No           | MEDIUM                  |                            |
| Product category    | ✅ `ProductCategory()`    | ❌ No           | MEDIUM                  | Electronics, Clothing      |
| Product feature     | ✅ `ProductFeature()`     | ❌ No           | LOW                     | Key features               |
| Product material    | ✅ `ProductMaterial()`    | ❌ No           | LOW                     | Cotton, Steel              |
| Product UPC         | ✅ `ProductUPC()`         | ❌ No           | MEDIUM                  | Barcode                    |
| Product audience    | ✅ `ProductAudience()`    | ❌ No           | LOW                     | Target demographic         |
| Product dimension   | ✅ `ProductDimension()`   | ❌ No           | LOW                     | Size specs                 |
| Product use case    | ✅ `ProductUseCase()`     | ❌ No           | LOW                     |                            |
| Product benefit     | ✅ `ProductBenefit()`     | ❌ No           | LOW                     |                            |
| Product suffix      | ✅ `ProductSuffix()`      | ❌ No           | LOW                     |                            |
| Product ISBN        | ✅ `ProductISBN()`        | ❌ No           | MEDIUM                  | Book identifier            |

### 23. FILE & MIME TYPES

| Feature        | GoFakeit Support    | krandom Status | Implementation Priority | Notes                |
|----------------|---------------------|----------------|-------------------------|----------------------|
| File extension | ✅ `FileExtension()` | ❌ No           | MEDIUM                  | .png, .pdf, .txt     |
| MIME type      | ✅ `FileMimeType()`  | ❌ No           | MEDIUM                  | image/png, text/html |

### 24. IMAGES

| Feature     | GoFakeit Support             | krandom Status | Implementation Priority | Notes             |
|-------------|------------------------------|----------------|-------------------------|-------------------|
| Image bytes | ✅ `Image(width, height)`     | ❌ No           | LOW                     | Random image data |
| Image JPEG  | ✅ `ImageJpeg(width, height)` | ❌ No           | LOW                     | JPEG format       |
| Image PNG   | ✅ `ImagePng(width, height)`  | ❌ No           | LOW                     | PNG format        |
| SVG         | ✅ `Svg(options)`             | ❌ No           | LOW                     | Vector graphics   |

### 25. ERROR GENERATION

| Feature           | GoFakeit Support      | krandom Status | Implementation Priority | Notes               |
|-------------------|-----------------------|----------------|-------------------------|---------------------|
| Generic error     | ✅ `Error()`           | ❌ No           | LOW                     | Random error        |
| Database error    | ✅ `ErrorDatabase()`   | ❌ No           | LOW                     | DB-specific         |
| gRPC error        | ✅ `ErrorGRPC()`       | ❌ No           | LOW                     | gRPC codes          |
| HTTP error        | ✅ `ErrorHTTP()`       | ❌ No           | LOW                     | HTTP errors         |
| HTTP client error | ✅ `ErrorHTTPClient()` | ❌ No           | LOW                     | 4xx errors          |
| HTTP server error | ✅ `ErrorHTTPServer()` | ❌ No           | LOW                     | 5xx errors          |
| Runtime error     | ✅ `ErrorRuntime()`    | ❌ No           | LOW                     | Runtime exceptions  |
| Validation error  | ✅ `ErrorValidation()` | ❌ No           | LOW                     | Validation failures |
| Error object      | ✅ `ErrorObject()`     | ❌ No           | LOW                     | Structured error    |

### 26. EMOJI

| Feature           | GoFakeit Support       | krandom Status | Implementation Priority | Notes            |
|-------------------|------------------------|----------------|-------------------------|------------------|
| Emoji             | ✅ `Emoji()`            | ❌ No           | LOW                     | Random emoji     |
| Emoji description | ✅ `EmojiDescription()` | ❌ No           | LOW                     | Text description |
| Emoji category    | ✅ `EmojiCategory()`    | ❌ No           | LOW                     | Smileys, Animals |
| Emoji alias       | ✅ `EmojiAlias()`       | ❌ No           | LOW                     | :smile:          |
| Emoji tag         | ✅ `EmojiTag()`         | ❌ No           | LOW                     | Keywords         |
| Emoji flag        | ✅ `EmojiFlag()`        | ❌ No           | LOW                     | Country flags    |
| Emoji animal      | ✅ `EmojiAnimal()`      | ❌ No           | LOW                     | Animal emojis    |
| Emoji food        | ✅ `EmojiFood()`        | ❌ No           | LOW                     | Food emojis      |

### 27. HACKER & TECH

| Feature             | GoFakeit Support         | krandom Status | Implementation Priority | Notes                   |
|---------------------|--------------------------|----------------|-------------------------|-------------------------|
| Hacker phrase       | ✅ `HackerPhrase()`       | ❌ No           | LOW                     | "Parse the neural bus!" |
| Hacker abbreviation | ✅ `HackerAbbreviation()` | ❌ No           | LOW                     | HTTP, RAM, GPU          |
| Hacker adjective    | ✅ `HackerAdjective()`    | ❌ No           | LOW                     | neural, redundant       |
| Hacker noun         | ✅ `HackerNoun()`         | ❌ No           | LOW                     | feed, bandwidth         |
| Hacker verb         | ✅ `HackerVerb()`         | ❌ No           | LOW                     | parse, bypass           |
| Hacker gerund       | ✅ `HackeringVerb()`      | ❌ No           | LOW                     | parsing, bypassing      |

### 28. LANGUAGE

| Feature               | GoFakeit Support              | krandom Status | Implementation Priority | Notes                 |
|-----------------------|-------------------------------|----------------|-------------------------|-----------------------|
| Language              | ✅ `Language()`                | ❌ No           | MEDIUM                  | "English", "Japanese" |
| Language abbreviation | ✅ `LanguageAbbreviation()`    | ❌ No           | MEDIUM                  | "en", "ja"            |
| Language BCP          | ✅ `LanguageBCP()`             | ❌ No           | MEDIUM                  | "en-US"               |
| Programming language  | ✅ `ProgrammingLanguage()`     | ❌ No           | LOW                     | Go, Python, Java      |
| Best language         | ✅ `ProgrammingLanguageBest()` | ❌ No           | LOW                     | Always "Go"           |

### 29. GRAMMAR (Parts of Speech)

| Feature              | GoFakeit Support                | krandom Status | Implementation Priority | Notes               |
|----------------------|---------------------------------|----------------|-------------------------|---------------------|
| **Adjectives**       |
| Adjective            | ✅ `Adjective()`                 | ❌ No           | LOW                     | Random adjective    |
| Demonstrative        | ✅ `AdjectiveDemonstrative()`    | ❌ No           | LOW                     | this, that          |
| Descriptive          | ✅ `AdjectiveDescriptive()`      | ❌ No           | LOW                     | beautiful, ugly     |
| Indefinite           | ✅ `AdjectiveIndefinite()`       | ❌ No           | LOW                     | some, any           |
| Interrogative        | ✅ `AdjectiveInterrogative()`    | ❌ No           | LOW                     | which, what         |
| Possessive           | ✅ `AdjectivePossessive()`       | ❌ No           | LOW                     | my, your            |
| Proper               | ✅ `AdjectiveProper()`           | ❌ No           | LOW                     | American, Victorian |
| Quantitative         | ✅ `AdjectiveQuantitative()`     | ❌ No           | LOW                     | many, few           |
| **Adverbs**          |
| Adverb               | ✅ `Adverb()`                    | ❌ No           | LOW                     | Random adverb       |
| Degree               | ✅ `AdverbDegree()`              | ❌ No           | LOW                     | very, quite         |
| Frequency definite   | ✅ `AdverbFrequencyDefinite()`   | ❌ No           | LOW                     | daily, weekly       |
| Frequency indefinite | ✅ `AdverbFrequencyIndefinite()` | ❌ No           | LOW                     | often, sometimes    |
| Manner               | ✅ `AdverbManner()`              | ❌ No           | LOW                     | quickly, slowly     |
| Place                | ✅ `AdverbPlace()`               | ❌ No           | LOW                     | here, there         |
| Time definite        | ✅ `AdverbTimeDefinite()`        | ❌ No           | LOW                     | now, then           |
| Time indefinite      | ✅ `AdverbTimeIndefinite()`      | ❌ No           | LOW                     | soon, later         |
| **Nouns**            |
| Noun                 | ✅ `Noun()`                      | ❌ No           | LOW                     | Random noun         |
| Abstract             | ✅ `NounAbstract()`              | ❌ No           | LOW                     | love, freedom       |
| Collective animal    | ✅ `NounCollectiveAnimal()`      | ❌ No           | LOW                     | herd, flock         |
| Collective people    | ✅ `NounCollectivePeople()`      | ❌ No           | LOW                     | team, crowd         |
| Collective thing     | ✅ `NounCollectiveThing()`       | ❌ No           | LOW                     | bunch, set          |
| Common               | ✅ `NounCommon()`                | ❌ No           | LOW                     | dog, city           |
| Concrete             | ✅ `NounConcrete()`              | ❌ No           | LOW                     | table, car          |
| Countable            | ✅ `NounCountable()`             | ❌ No           | LOW                     | apple, book         |
| Proper               | ✅ `NounProper()`                | ❌ No           | LOW                     | London, John        |
| Uncountable          | ✅ `NounUncountable()`           | ❌ No           | LOW                     | water, rice         |
| **Verbs**            |
| Verb                 | ✅ `Verb()`                      | ❌ No           | LOW                     | Random verb         |
| Action               | ✅ `VerbAction()`                | ❌ No           | LOW                     | run, jump           |
| Helping              | ✅ `VerbHelping()`               | ❌ No           | LOW                     | can, will           |
| Intransitive         | ✅ `VerbIntransitive()`          | ❌ No           | LOW                     | sleep, arrive       |
| Linking              | ✅ `VerbLinking()`               | ❌ No           | LOW                     | is, seems           |
| Transitive           | ✅ `VerbTransitive()`            | ❌ No           | LOW                     | eat, write          |
| **Pronouns**         |
| Pronoun              | ✅ `Pronoun()`                   | ❌ No           | LOW                     | Random pronoun      |
| Demonstrative        | ✅ `PronounDemonstrative()`      | ❌ No           | LOW                     | this, those         |
| Personal             | ✅ `PronounPersonal()`           | ❌ No           | LOW                     | I, you, he          |
| Possessive           | ✅ `PronounPossessive()`         | ❌ No           | LOW                     | mine, yours         |
| Reflective           | ✅ `PronounReflective()`         | ❌ No           | LOW                     | myself, yourself    |
| Relative             | ✅ `PronounRelative()`           | ❌ No           | LOW                     | who, which          |
| **Other**            |
| Connective           | ✅ `Connective()`                | ❌ No           | LOW                     | and, but            |
| Casual connective    | ✅ `ConnectiveCasual()`          | ❌ No           | LOW                     | because, since      |
| Preposition          | ✅ `Preposition()`               | ❌ No           | LOW                     | on, in              |
| Simple preposition   | ✅ `PrepositionSimple()`         | ❌ No           | LOW                     | at, by              |
| Compound preposition | ✅ `PrepositionCompound()`       | ❌ No           | LOW                     | in front of         |
| Interjection         | ✅ `Interjection()`              | ❌ No           | LOW                     | wow, ouch           |

---

## ADVANCED FEATURES COMPARISON

### Struct Tagging System

**GoFakeit's `fake:` Tag System:**

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

**krandom Status:**

- ✅ **Has**: `ObjectGenerator` with reflection-based population
- ❌ **Missing**: Annotation/tag-based configuration
- ❌ **Missing**: Field-level pattern specifications
- ❌ **Missing**: Size control for collections
- ❌ **Missing**: Skip field markers

**Implementation Priority:** HIGH - This is GoFakeit's killer feature

---

### Template System

**GoFakeit's Template Engine:**

```go
tmpl := `Subject: {{RandomString (SliceString "Hello" "Greetings" "Hi")}}

Dear {{LastName}},

{{Paragraph 1 3 8 "\n"}}

Best regards,
{{FirstName}} {{LastName}}
`

result, err := gofakeit.Template(tmpl, &gofakeit.TemplateOptions{})
```

**krandom Status:**

- ❌ **Not implemented**
- Could be valuable for test data generation scenarios

**Implementation Priority:** MEDIUM - Useful for complex scenarios

---

### Pattern-Based Generation

| Feature                                  | GoFakeit | krandom | Priority |
|------------------------------------------|----------|---------|----------|
| `Numerify("#-###")` → "4-821"            | ✅ Yes    | ❌ No    | HIGH     |
| `Lexify("???")` → "fda"                  | ✅ Yes    | ❌ No    | HIGH     |
| `Bothify("??-##")` → "ab-12"             | ✅ Yes    | ❌ No    | HIGH     |
| `Regex("[a-z]{5}[0-9]{3}")` → "fkqwj812" | ✅ Yes    | ❌ No    | MEDIUM   |
| `Generate("{firstname}-{number:1,100}")` | ✅ Yes    | ❌ No    | HIGH     |

**Implementation Priority:** HIGH - Very flexible and powerful

---

### Structured Data Output

GoFakeit can generate complete data files:

```go
// CSV with 100 rows
csv := gofakeit.CSV(&gofakeit.CSVOptions{
RowCount: 100,
Fields: []gofakeit.Field{
{Name: "id", Function: "number", Params: map[string][]string{"min": {"1"}, "max": {"1000"}}},
{Name: "first_name", Function: "firstname"},
{Name: "email", Function: "email"},
},
})

// JSON array
json := gofakeit.JSON(&gofakeit.JSONOptions{
Type:     "array",
RowCount: 50,
Fields:   /* ... */,
})

// SQL INSERT statements
sql := gofakeit.SQL(&gofakeit.SQLOptions{
Table:    "users",
RowCount: 25,
Fields:   /* ... */,
})
```

**krandom Status:** ❌ Not implemented

**Implementation Priority:** MEDIUM - Niche but powerful for test data generation

---

### Extensibility

**GoFakeit Custom Functions:**

```go
gofakeit.AddFuncLookup("teamname", gofakeit.Info{
Category:    "custom",
Description: "Random sports team name",
Example:     "Blue Hawks",
Output:      "string",
Generate: func (f *gofakeit.Faker, m *gofakeit.MapParams, info *gofakeit.Info) (any, error) {
colors := []string{"Blue", "Red", "Green", "Gold", "Silver"}
animals := []string{"Hawks", "Lions", "Bears", "Wolves", "Eagles"}
return f.RandomString(colors) + " " + f.RandomString(animals), nil
},
})
```

**krandom Status:**

- ✅ Has extensibility through creating new `Generator` implementations
- ❌ No runtime registration system
- ❌ No function lookup mechanism

**Implementation Priority:** LOW - Design difference, not a gap

---

### PRNG Backends

**GoFakeit:**

- PCG (default) - fast, high quality
- ChaCha8 - cryptographically secure
- JSF, SFC - alternative fast PRNGs
- crypto/rand - OS entropy
- Deterministic "dumb" source
- Custom `rand.Source` support

**krandom:**

- `SecureRandom` (Java's crypto PRNG)
- No seed-based deterministic mode
- No PRNG backend selection

**Implementation Priority:** MEDIUM - Deterministic testing is valuable

---

## IMPLEMENTATION RECOMMENDATIONS

### Phase 1: Core Foundations (HIGH Priority - 2-3 weeks)

**1.1 Pattern-Based String Generation (3 days)**

- Implement `Numerify(pattern)` - replace '#' with random digits
- Implement `Lexify(pattern)` - replace '?' with random letters
- Implement `Bothify(pattern)` - combine both
- Add to `StringGenerator` or create new `PatternGenerator`

**1.2 UUID Generation (1 day)**

- Implement `UUIDGenerator` wrapper around `java.util.UUID`
- Support UUID v4 (random)
- Consider UUID v7 (time-ordered) for future

**1.3 Password Generator (2 days)**

- Configurable character sets (lower, upper, numeric, special, space)
- Minimum/maximum length
- Policy enforcement (at least N of each type)
- Extend `StringGenerator` with policies

**1.4 Date/Time Generators (3 days)**

- `DateGenerator` with configurable range
- `FutureDate()`, `PastDate()`, `DateBetween(start, end)`
- Date component generators: `Month()`, `Year()`, `WeekDay()`
- Use `java.time.*` (LocalDate, LocalDateTime, Instant)

**1.5 Basic Text Generation (3 days)**

- `WordGenerator` - single random word
- `SentenceGenerator` - N words
- `ParagraphGenerator` - structured text
- Lorem ipsum variants
- Use word lists (CSV or embedded resources)

**Estimated Total: 12 days**

---

### Phase 2: Essential Business Data (HIGH Priority - 2-3 weeks)

**2.1 Address Components (4 days)**

- `CityGenerator`, `StateGenerator`, `CountryGenerator`
- `StreetNameGenerator`, `StreetNumberGenerator`, `StreetSuffixGenerator`
- `ZipCodeGenerator`
- `AddressGenerator` - complete address object
- CSV-backed data (cities, states, countries)
- Support for state abbreviations, country codes

**2.2 Phone Numbers (2 days)**

- `PhoneNumberGenerator` with format patterns
- Use `Numerify` for format strings like "(###) ###-####"
- National and international formats
- Area code validation (optional)

**2.3 Credit Cards (3 days)**

- Extend `LuhnGenerator` with BIN prefixes
- Support Visa, Mastercard, Amex, Discover
- `CreditCardExpiryGenerator` (MM/YY)
- `CVVGenerator` (3-4 digits based on type)
- `CreditCardGenerator` - complete card object

**2.4 Currency & Finance (2 days)**

- `CurrencyGenerator` - ISO 4217 codes
- `CurrencyNameGenerator`
- `PriceGenerator` with decimal formatting
- Banking: `AchRoutingGenerator`, `AchAccountGenerator`

**2.5 Company & Job (3 days)**

- `CompanyNameGenerator`
- `CompanySuffixGenerator` (Inc, LLC, Ltd)
- `JobDescriptorGenerator` (Senior, Lead, Chief)
- `JobLevelGenerator` (Entry, Mid, Senior)
- CSV-backed data sources

**Estimated Total: 14 days**

---

### Phase 3: Advanced Features (MEDIUM Priority - 2-3 weeks)

**3.1 Enhanced Struct Tagging (5 days)**

- Design annotation system for JVM/Kotlin
- Consider Kotlin annotations: `@Fake(pattern = "{firstname}")`
- Support pattern strings: `@Fake("{number:1,100}")`
- Support regex patterns: `@Fake("{regex:[A-Z]{3}-[0-9]{6}}")`
- Collection size control: `@FakeSize(min = 2, max = 5)`
- Skip field markers: `@FakeSkip`
- Integrate with existing `ObjectGenerator`

**3.2 Template System (4 days)**

- Evaluate template engines for JVM (Velocity, Freemarker, kotlinx.html DSL)
- Create function registry for template access
- Support all existing generators as template functions
- `TemplateGenerator` class with DSL
- Example: `"Hello {firstname} {lastname}".generate()`

**3.3 Internet & Networking (3 days)**

- `URLGenerator`, `DomainNameGenerator`, `DomainSuffixGenerator`
- `SlugGenerator` (URL-friendly strings)
- `MACAddressGenerator`
- `UserAgentGenerator` (browser strings from static list)
- `HTTPMethodGenerator`, `HTTPStatusCodeGenerator`

**3.4 Color Generators (2 days)**

- `ColorNameGenerator` (CSS named colors)
- `HexColorGenerator` (#RRGGBB)
- `RGBColorGenerator` (rgb(r,g,b))
- Safe/web-safe color variants

**Estimated Total: 14 days**

---

### Phase 4: Specialized & Nice-to-Have (LOW Priority - 1-2 weeks)

**4.1 Structured Output (4 days)**

- `CSVDataGenerator` - generate CSV files
- `JSONDataGenerator` - generate JSON arrays/objects
- `SQLDataGenerator` - generate INSERT statements
- Field definition DSL
- Row count configuration

**4.2 Regex Pattern Generation (3 days)**

- Integrate or implement RE2-compatible regex generator
- Java: Use `dk.brics.automaton` or similar
- `RegexGenerator(pattern)` → matching string

**4.3 Weighted & Collection Utilities (2 days)**

- `WeightedGenerator<T>` - probability-based selection
- `ShuffleGenerator` - randomize collections
- `SampleGenerator<T>` - pick N random items

**4.4 Product & Commerce (2 days)**

- `ProductNameGenerator`
- `ProductCategoryGenerator`
- `BarcodeGenerator` (UPC, EAN)
- File types: `FileExtensionGenerator`, `MimeTypeGenerator`

**Estimated Total: 11 days**

---

### Phase 5: Entertainment & Niche (OPTIONAL - 1 week)

**5.1 Games & Fun (3 days)**

- Already have Dice and Coin ✅
- `GametagGenerator`
- Minecraft generators (17 functions) - LOW ROI

**5.2 Domain-Specific (2 days)**

- Beer, food, animals - static list generators
- Books, movies, music
- Very low practical value

**Estimated Total: 5 days**

---

## EFFORT ESTIMATES SUMMARY

| Phase     | Focus             | Days        | Priority | ROI   |
|-----------|-------------------|-------------|----------|-------|
| Phase 1   | Core foundations  | 12          | HIGH     | ⭐⭐⭐⭐⭐ |
| Phase 2   | Business data     | 14          | HIGH     | ⭐⭐⭐⭐⭐ |
| Phase 3   | Advanced features | 14          | MEDIUM   | ⭐⭐⭐⭐  |
| Phase 4   | Specialized       | 11          | LOW      | ⭐⭐⭐   |
| Phase 5   | Entertainment     | 5           | OPTIONAL | ⭐     |
| **TOTAL** |                   | **56 days** |          |       |

**Recommended MVP (Phases 1-2):** 26 days (~5 weeks)

**Full competitive parity (Phases 1-3):** 40 days (~8 weeks)

---

## KEY DIFFERENTIATORS: GoFakeit vs krandom

### GoFakeit's Unique Strengths

1. **Struct Tagging System** ⭐⭐⭐⭐⭐
    - Declarative data generation
    - Zero boilerplate for complex objects
    - Type-safe at compile time
    - **Gap in krandom**: No annotation-based configuration

2. **Template Engine** ⭐⭐⭐⭐
    - Full Go template integration
    - All 310+ functions as template actions
    - Powerful for generating complex strings
    - **Gap in krandom**: No template system

3. **Pattern-Based Generation** ⭐⭐⭐⭐⭐
    - `Numerify`, `Lexify`, `Bothify`
    - `Generate("{function:params}")`
    - `Regex(pattern)` generation
    - **Gap in krandom**: Limited to basic generators

4. **Structured Output Formats** ⭐⭐⭐
    - CSV, JSON, XML, SQL, Markdown, Fixed-width
    - Configurable rows and fields
    - **Gap in krandom**: No file format generation

5. **Zero Dependencies** ⭐⭐⭐⭐
    - Pure Go standard library
    - Minimal attack surface
    - Easy integration
    - **krandom**: Depends on JVM ecosystem

6. **PRNG Flexibility** ⭐⭐⭐⭐
    - Multiple backends (PCG, ChaCha8, crypto/rand)
    - Deterministic seeding for reproducible tests
    - Custom source support
    - **Gap in krandom**: Only SecureRandom, no seeding

7. **Comprehensive Function Library** ⭐⭐⭐⭐
    - 310+ functions across 50+ categories
    - Covers niche domains (Minecraft, Beer, Hacker)
    - Grammar/NLP functions (parts of speech)
    - **krandom**: ~30-40 generators currently

### krandom's Unique Strengths

1. **Type-Safe Builders** ⭐⭐⭐⭐⭐
    - Kotlin DSL for configuration
    - Compile-time type checking
    - Fluent API design
    - **GoFakeit**: Function-based, less type-safe

2. **JVM Ecosystem Integration** ⭐⭐⭐⭐
    - Native Java/Kotlin/Scala interop
    - Existing JVM tooling
    - Enterprise Java compatibility
    - **GoFakeit**: Go-only

3. **SecureRandom by Default** ⭐⭐⭐⭐
    - Cryptographically secure out of the box
    - No accidental weak randomness
    - **GoFakeit**: Default PCG is not crypto-secure

4. **Custom Algorithms** ⭐⭐⭐
    - Fibonacci, Luhn, Prime numbers
    - Mathematical generators
    - **GoFakeit**: Basic number generation only

5. **Clean Architecture** ⭐⭐⭐⭐
    - Well-structured modules
    - Clear separation of concerns
    - Extensible design
    - **GoFakeit**: Flat function library

6. **Multi-Language API** ⭐⭐⭐⭐
    - Java, Kotlin, Scala wrappers
    - Idiomatic APIs for each language
    - **GoFakeit**: Go-only

---

## COMPATIBILITY ASSESSMENT

### Direct Port Feasibility

| Feature Category       | Difficulty | Notes                                       |
|------------------------|------------|---------------------------------------------|
| **Basic Generators**   | ✅ EASY     | Numbers, booleans, enums - already done     |
| **Pattern Generation** | ✅ EASY     | Numerify/Lexify - string manipulation       |
| **Address/Person**     | ✅ EASY     | CSV-backed like existing TitleGenerator     |
| **Date/Time**          | ✅ EASY     | java.time API is comprehensive              |
| **Credit Cards**       | ✅ MODERATE | Extend existing LuhnGenerator               |
| **Phone Numbers**      | ✅ EASY     | Format patterns with Numerify               |
| **Struct Tagging**     | ⚠️ HARD    | Need annotation processor or reflection DSL |
| **Template System**    | ⚠️ HARD    | Requires template engine integration        |
| **Regex Generation**   | ⚠️ HARD    | Complex algorithm, consider library         |
| **Structured Output**  | ✅ MODERATE | File format generation logic                |
| **PRNG Backends**      | ✅ MODERATE | Java supports multiple RNG algorithms       |
| **Grammar/NLP**        | ✅ EASY     | Static word lists                           |

### Recommended Approach

1. **Don't Port Everything**
    - GoFakeit has 310+ functions, many are low-value niche data
    - Focus on 80/20 rule - 20% of features = 80% of use cases
    - Skip: Minecraft, Celebrity, Beer details, most entertainment

2. **Prioritize Core Business Data**
    - Addresses, phones, emails, dates, numbers
    - Credit cards, banking, currency
    - Company, job, product data
    - Text generation (lorem, sentences)

3. **Implement Pattern System First**
    - Numerify/Lexify/Bothify are foundational
    - Enable many other generators
    - High leverage feature

4. **Add Struct Configuration**
    - Kotlin annotations are more idiomatic than Go tags
    - Consider builder DSL as alternative
    - Example: `Person { firstName = fake.firstName(); email = fake.email() }`

5. **Template System - Later**
    - Nice to have but not essential
    - Can use Kotlin string templates initially
    - Consider if demand exists

6. **Skip Structured Output Initially**
    - Can generate lists/sequences of objects
    - Users can serialize with Jackson/Gson/kotlinx.serialization
    - Add if requested

7. **Consider Seeded Random**
    - Deterministic testing is valuable
    - Java's `Random(seed)` is sufficient
    - Allow injecting `Random` instance into generators

8. **Keep krandom's Identity**
    - Don't become a GoFakeit clone
    - Maintain Kotlin-first, type-safe approach
    - Better builder DSL than function soup
    - Superior architecture

---

## LOCALE SUPPORT COMPARISON

**GoFakeit Locale Support:**

- ❌ **NO locale support** - all data is US English
- Names, addresses, cities are US-centric
- No i18n mechanism
- Single language focus

**krandom Locale Support:**

- ⚠️ **Partial** - some generators support locales (TitleGenerator)
- Infrastructure exists but not comprehensive
- Opportunity to **exceed GoFakeit** here

**Recommendation:**

- This is an area where krandom can BEAT GoFakeit
- Implement proper i18n from the start
- Use locale-aware data sources
- Support multiple cultures for names, addresses, phones

---

## CONCLUSION

### What to Build

**HIGH Priority (Must Have - Phases 1-2):**

1. ✅ Pattern-based generation (Numerify, Lexify, Bothify, Generate)
2. ✅ UUID generator
3. ✅ Password generator with policies
4. ✅ Date/Time generators (past, future, range, components)
5. ✅ Address components (city, state, country, street, zip)
6. ✅ Phone number formatting
7. ✅ Credit card generation (Luhn-valid, multiple types)
8. ✅ Currency and pricing
9. ✅ Company and job data
10. ✅ Text generation (word, sentence, paragraph, lorem)

**MEDIUM Priority (Should Have - Phase 3):**

1. Enhanced struct tagging/annotation system
2. Template system for complex string generation
3. Internet generators (URL, domain, MAC, user agent, HTTP)
4. Color generators (name, hex, RGB)
5. Regex pattern generation
6. Weighted selection utilities

**LOW Priority (Nice to Have - Phases 4-5):**

1. Structured output (CSV, JSON, SQL)
2. Product and commerce data
3. File extensions and MIME types
4. Entertainment data (books, movies, music)
5. Niche domains (beer, food, animals)
6. Grammar/NLP functions

### What to Skip

- ❌ Minecraft generators (17 functions) - highly niche
- ❌ Celebrity names - low practical value
- ❌ Beer/food details - not business-critical
- ❌ Most entertainment providers
- ❌ Overly specific niche data

### What to Do Better

**1. Locale Support** - Exceed GoFakeit by supporting multiple languages/regions
**2. Type Safety** - Leverage Kotlin's type system for better APIs
**3. Builder DSL** - More ergonomic than function calls
**4. Module Organization** - Keep clean architecture
**5. Documentation** - Comprehensive examples and guides

### Target Positioning

> **krandom**: The type-safe, locale-aware fake data library for the JVM
>
> Match GoFakeit on **core features** (business data, patterns, dates)
> Exceed on **developer experience** (DSL, type safety, IDE support)
> Lead on **internationalization** (proper locale support)
> Maintain **clean architecture** (modular, extensible, testable)

### Success Metrics

- **Feature Coverage**: 60-70% of GoFakeit's practical functions
- **Developer Experience**: Superior Kotlin DSL and type safety
- **Locale Support**: 10+ locales (vs GoFakeit's 1)
- **Documentation**: Comprehensive with examples
- **Performance**: Comparable or better
- **Adoption**: Preferred choice for JVM fake data generation

**Estimated Timeline:**

- MVP (Phases 1-2): **5-6 weeks**
- Competitive (Phases 1-3): **8-10 weeks**
- Feature-complete (All phases): **12-14 weeks**

**Resource Requirements:**

- 1-2 developers
- Part-time or full-time
- Iterative releases recommended
