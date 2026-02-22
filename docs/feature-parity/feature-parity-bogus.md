# kRandom vs Bogus (.NET) — Feature Parity Matrix

**Last Updated:** 2026-02-21
**Bogus Version:** Latest (C# .NET)
**kRandom Version:** Current development state

---

## Executive Summary

| Metric | kRandom | Bogus |
|--------|---------|-------|
| **Total Feature Categories** | 9 | 18 |
| **Primitive Generators** | ✅ Complete | ✅ Complete |
| **User/Person Data** | ✅ Strong | ✅ Comprehensive |
| **Network Generators** | ✅ IPv4/IPv6 | ✅ IPv4/IPv6 + MAC |
| **Object Population** | ✅ Reflection-based | ✅ Fluent `Faker<T>` |
| **Algorithm Generators** | ✅ Fibonacci, Luhn, Primes | ⚠️ Luhn only (internal) |
| **Game Utilities** | ✅ Dice (D4-D20), Coin | ❌ None |
| **Locale Support** | ❌ None | ✅ 70+ locales |
| **Commerce/Finance** | ❌ None | ✅ Comprehensive |
| **Address Data** | ❌ None | ✅ Comprehensive |
| **Date/Time** | ❌ None | ✅ Comprehensive |
| **Lorem/Text** | ❌ None | ✅ Comprehensive |

---

## Feature Comparison Table

### 1. Primitive Generators

| Feature | kRandom | Bogus | Notes |
|---------|---------|-------|-------|
| **Int** | ✅ `IntGenerator(min, max)` | ✅ `f.Random.Int(min, max)` | Both support ranges |
| **Long** | ✅ `LongGenerator(min, max)` | ✅ `f.Random.Long(min, max)` | |
| **Short** | ✅ `ShortGenerator(min, max)` | ✅ `f.Random.Short(min, max)` | |
| **Byte** | ✅ `ByteGenerator(min, max)` | ✅ `f.Random.Byte(min, max)` | |
| **Float** | ✅ `FloatGenerator(min, max)` | ✅ `f.Random.Float(min, max)` | |
| **Double** | ✅ `DoubleGenerator(min, max)` | ✅ `f.Random.Double(min, max)` | |
| **Boolean** | ✅ `BooleanGenerator()` | ✅ `f.Random.Bool(weight?)` | Bogus supports weighted probability |
| **Char** | ✅ `CharGenerator(min, max)` | ✅ `f.Random.Char(min, max)` | |
| **String** | ✅ `StringGenerator()` | ✅ `f.Random.String2(len, chars)` | |
| **Enum** | ✅ `EnumGenerator<T>()` | ✅ `f.Random.Enum<T>(exclude?)` | Bogus supports exclusions |
| **Even/Odd** | ❌ | ✅ `f.Random.Even/Odd(min, max)` | Bogus has specialized methods |
| **AlphaNumeric** | ❌ | ✅ `f.Random.AlphaNumeric(len)` | |
| **Hex Hash** | ✅ `HexHashGenerator(len)` | ✅ `f.Random.Hash(len)` | Both generate hex strings |
| **GUID/UUID** | ❌ | ✅ `f.Random.Guid()` | |
| **Byte Array** | ❌ | ✅ `f.Random.Bytes(count)` | |
| **UTF-16 String** | ❌ | ✅ `f.Random.Utf16String()` | |

### 2. Object Population

| Feature | kRandom | Bogus | Notes |
|---------|---------|-------|-------|
| **Auto-populate POJO/Record** | ✅ `ObjectGenerator<T>()` | ✅ `Faker<T>()` | Both use reflection |
| **Fluent API** | ❌ | ✅ `RuleFor(prop, func)` | Bogus has declarative builder |
| **Custom instantiator** | ❌ | ✅ `CustomInstantiator(func)` | |
| **Property rules** | ⚠️ Type/field overrides | ✅ `RuleFor` per property | kRandom has basic overrides |
| **Cross-property rules** | ❌ | ✅ `RuleFor((f, u) => ...)` | Bogus allows dependencies |
| **Strict mode** | ❌ | ✅ `StrictMode(true)` | Bogus requires all props ruled |
| **Named rule sets** | ❌ | ✅ `RuleSet(name, action)` | Bogus supports multi-profiles |
| **Populate existing instance** | ❌ | ✅ `Populate(instance)` | |
| **Post-generation hook** | ❌ | ✅ `FinishWith(action)` | |
| **Ignore property** | ❌ | ✅ `Ignore(expr)` | |
| **Configuration validation** | ❌ | ✅ `AssertConfigurationIsValid()` | |
| **Cloning** | ❌ | ✅ `Clone()` | |
| **Max depth control** | ✅ | ❌ | kRandom prevents infinite recursion |

### 3. User / Person Data

| Feature | kRandom | Bogus | Notes |
|---------|---------|-------|-------|
| **First Name** | ✅ `FirstName()` | ✅ `f.Name.FirstName()` | Both use real name datasets |
| **Last Name** | ✅ `SurName()` | ✅ `f.Name.LastName()` | |
| **Full Name** | ❌ | ✅ `f.Name.FullName()` | Bogus composes first+last |
| **Name with prefix/suffix** | ❌ | ✅ `f.Name.FindName()` | Bogus supports "Dr. John Doe Jr." |
| **Gender** | ✅ `Gender()` | ✅ Implicit in `FirstName(gender)` | kRandom has enum, Bogus integrates |
| **Title/Prefix** | ✅ `Title()` (Mr/Ms/Dr) | ✅ `f.Name.Prefix()` | |
| **Suffix** | ❌ | ✅ `f.Name.Suffix()` (Jr/Sr/PhD) | |
| **Age** | ✅ `Age()` | ⚠️ `f.Random.Int(0, 99)` | kRandom dedicated, Bogus manual |
| **Birthday/DOB** | ✅ `BirthDay()` | ✅ `f.Date.Past(80)` | Different approaches |
| **Email** | ✅ `Email()` | ✅ `f.Internet.Email()` | Bogus supports name-based emails |
| **Username** | ✅ `Username()` | ✅ `f.Internet.UserName()` | |
| **SSN** | ✅ `SocialSecurityNumber()` | ✅ `person.Ssn()` (US) | Bogus has locale extensions |
| **Password** | ❌ | ✅ `f.Internet.Password(len)` | |
| **Avatar URL** | ❌ | ✅ `person.Avatar` | |
| **Job Title** | ❌ | ✅ `f.Name.JobTitle()` | |
| **Job Descriptor** | ❌ | ✅ `f.Name.JobDescriptor()` | |
| **Job Area** | ❌ | ✅ `f.Name.JobArea()` | |
| **Job Type** | ❌ | ✅ `f.Name.JobType()` | |
| **Person Card** | ❌ | ✅ `new Person()` | Bogus composite object |

### 4. Network Generators

| Feature | kRandom | Bogus | Notes |
|---------|---------|-------|-------|
| **IPv4** | ✅ `IPv4Generator()` | ✅ `f.Internet.Ip()` | Both RFC 791 compliant |
| **IPv6** | ✅ `IPv6Generator()` | ✅ `f.Internet.Ipv6()` | Both RFC 4291 compliant |
| **IPAddress Object** | ❌ | ✅ `f.Internet.IpAddress()` | Bogus returns .NET `IPAddress` |
| **IPEndPoint** | ❌ | ✅ `f.Internet.IpEndPoint()` | |
| **MAC Address** | ❌ | ✅ `f.Internet.Mac()` | |
| **Port Number** | ❌ | ✅ `f.Internet.Port()` | |
| **Domain Name** | ❌ | ✅ `f.Internet.DomainName()` | |
| **Domain Word** | ❌ | ✅ `f.Internet.DomainWord()` | |
| **Domain Suffix** | ❌ | ✅ `f.Internet.DomainSuffix()` | |
| **URL** | ❌ | ✅ `f.Internet.Url()` | |
| **URL with Path** | ❌ | ✅ `f.Internet.UrlWithPath()` | |
| **Protocol** | ❌ | ✅ `f.Internet.Protocol()` | |
| **User Agent** | ❌ | ✅ `f.Internet.UserAgent()` | |
| **Color (hex/rgb)** | ❌ | ✅ `f.Internet.Color()` | |

### 5. Algorithm / Mathematical Generators

| Feature | kRandom | Bogus | Notes |
|---------|---------|-------|-------|
| **Fibonacci Sequence** | ✅ `FibonacciGenerator()` | ❌ | kRandom unique feature |
| **Luhn Valid Numbers** | ✅ `LuhnGenerator()` | ⚠️ Internal only | Bogus uses for credit cards |
| **Prime Numbers** | ✅ `PrimeNumberGenerator()` | ❌ | kRandom unique feature |
| **Composite Numbers** | ✅ `CompositeNumberGenerator()` | ❌ | kRandom unique feature |
| **Natural Numbers** | ✅ `NaturalNumberGenerator()` | ❌ | kRandom unique feature |
| **Even Numbers** | ❌ | ✅ `f.Random.Even(min, max)` | |
| **Odd Numbers** | ❌ | ✅ `f.Random.Odd(min, max)` | |

### 6. Game Utilities

| Feature | kRandom | Bogus | Notes |
|---------|---------|-------|-------|
| **Dice Rolls** | ✅ D4/D6/D8/D10/D12/D20 | ❌ | kRandom unique feature |
| **Coin Flip** | ✅ `CoinGenerator()` | ⚠️ `f.Random.Bool()` | kRandom has HEAD/TAIL enum |
| **Fair distribution** | ✅ Guaranteed | N/A | kRandom explicitly ensures fairness |

### 7. Finance & Commerce

| Feature | kRandom | Bogus | Notes |
|---------|---------|-------|-------|
| **Credit Card Number** | ⚠️ Via `LuhnGenerator` | ✅ `f.Finance.CreditCardNumber()` | Bogus has typed card providers |
| **Credit Card CVV** | ❌ | ✅ `f.Finance.CreditCardCvv()` | |
| **Amount/Money** | ❌ | ✅ `f.Finance.Amount()` | |
| **Account Number** | ❌ | ✅ `f.Finance.Account()` | |
| **Account Name** | ❌ | ✅ `f.Finance.AccountName()` | |
| **Transaction Type** | ❌ | ✅ `f.Finance.TransactionType()` | |
| **Currency** | ❌ | ✅ `f.Finance.Currency()` | |
| **Bitcoin Address** | ❌ | ✅ `f.Finance.BitcoinAddress()` | |
| **Ethereum Address** | ❌ | ✅ `f.Finance.EthereumAddress()` | |
| **Litecoin Address** | ❌ | ✅ `f.Finance.LitecoinAddress()` | |
| **IBAN** | ❌ | ✅ `f.Finance.Iban()` | |
| **BIC** | ❌ | ✅ `f.Finance.Bic()` | |
| **Routing Number** | ❌ | ✅ `f.Finance.RoutingNumber()` | |
| **EAN-8** | ❌ | ✅ `f.Commerce.Ean8()` | |
| **EAN-13** | ❌ | ✅ `f.Commerce.Ean13()` | |
| **Product Name** | ❌ | ✅ `f.Commerce.ProductName()` | |
| **Product Description** | ❌ | ✅ `f.Commerce.ProductDescription()` | |
| **Price** | ❌ | ✅ `f.Commerce.Price()` | |
| **Department** | ❌ | ✅ `f.Commerce.Department()` | |
| **Product Adjective** | ❌ | ✅ `f.Commerce.ProductAdjective()` | |
| **Product Material** | ❌ | ✅ `f.Commerce.ProductMaterial()` | |
| **Color** | ❌ | ✅ `f.Commerce.Color()` | |

### 8. Address & Location

| Feature | kRandom | Bogus | Notes |
|---------|---------|-------|-------|
| **Street Address** | ❌ | ✅ `f.Address.StreetAddress()` | |
| **Street Name** | ❌ | ✅ `f.Address.StreetName()` | |
| **Street Suffix** | ❌ | ✅ `f.Address.StreetSuffix()` | |
| **Building Number** | ❌ | ✅ `f.Address.BuildingNumber()` | |
| **Secondary Address** | ❌ | ✅ `f.Address.SecondaryAddress()` | |
| **City** | ❌ | ✅ `f.Address.City()` | |
| **City Prefix/Suffix** | ❌ | ✅ `f.Address.CityPrefix()` | |
| **State** | ❌ | ✅ `f.Address.State()` | |
| **State Abbreviation** | ❌ | ✅ `f.Address.StateAbbr()` | |
| **ZIP Code** | ❌ | ✅ `f.Address.ZipCode()` | |
| **County** | ❌ | ✅ `f.Address.County()` | |
| **Country** | ❌ | ✅ `f.Address.Country()` | |
| **Country Code** | ❌ | ✅ `f.Address.CountryCode()` | |
| **Full Address** | ❌ | ✅ `f.Address.FullAddress()` | |
| **Latitude** | ❌ | ✅ `f.Address.Latitude()` | |
| **Longitude** | ❌ | ✅ `f.Address.Longitude()` | |
| **Direction** | ❌ | ✅ `f.Address.Direction()` | |
| **Cardinal Direction** | ❌ | ✅ `f.Address.CardinalDirection()` | |

### 9. Date & Time

| Feature | kRandom | Bogus | Notes |
|---------|---------|-------|-------|
| **Past Date** | ❌ | ✅ `f.Date.Past()` | |
| **Future Date** | ❌ | ✅ `f.Date.Future()` | |
| **Between Dates** | ❌ | ✅ `f.Date.Between(start, end)` | |
| **Recent Date** | ❌ | ✅ `f.Date.Recent(days)` | |
| **Soon Date** | ❌ | ✅ `f.Date.Soon(days)` | |
| **Timespan** | ❌ | ✅ `f.Date.Timespan()` | |
| **Month Name** | ❌ | ✅ `f.Date.Month()` | |
| **Weekday Name** | ❌ | ✅ `f.Date.Weekday()` | |
| **Timezone String** | ❌ | ✅ `f.Date.TimeZoneString()` | |
| **DateTimeOffset** | ❌ | ✅ All methods support offset | |
| **Clock Override** | ❌ | ✅ `UseDateTimeReference()` | |

### 10. Lorem / Text Generation

| Feature | kRandom | Bogus | Notes |
|---------|---------|-------|-------|
| **Word** | ❌ | ✅ `f.Lorem.Word()` | |
| **Words** | ❌ | ✅ `f.Lorem.Words(n)` | |
| **Letter** | ❌ | ✅ `f.Lorem.Letter(n)` | |
| **Sentence** | ❌ | ✅ `f.Lorem.Sentence()` | |
| **Sentences** | ❌ | ✅ `f.Lorem.Sentences(n)` | |
| **Paragraph** | ❌ | ✅ `f.Lorem.Paragraph()` | |
| **Paragraphs** | ❌ | ✅ `f.Lorem.Paragraphs(n)` | |
| **Text** | ❌ | ✅ `f.Lorem.Text()` | |
| **Lines** | ❌ | ✅ `f.Lorem.Lines(n)` | |
| **Slug** | ❌ | ✅ `f.Lorem.Slug()` | |

### 11. Company & Business

| Feature | kRandom | Bogus | Notes |
|---------|---------|-------|-------|
| **Company Name** | ❌ | ✅ `f.Company.CompanyName()` | |
| **Company Suffix** | ❌ | ✅ `f.Company.CompanySuffix()` | |
| **Catchphrase** | ❌ | ✅ `f.Company.CatchPhrase()` | |
| **BS (buzzword)** | ❌ | ✅ `f.Company.Bs()` | |

### 12. System & Files

| Feature | kRandom | Bogus | Notes |
|---------|---------|-------|-------|
| **Filename** | ❌ | ✅ `f.System.FileName()` | |
| **Common Filename** | ❌ | ✅ `f.System.CommonFileName()` | |
| **Directory Path** | ❌ | ✅ `f.System.DirectoryPath()` | |
| **File Path** | ❌ | ✅ `f.System.FilePath()` | |
| **MIME Type** | ❌ | ✅ `f.System.MimeType()` | |
| **File Extension** | ❌ | ✅ `f.System.FileExt()` | |
| **Semver** | ❌ | ✅ `f.System.Semver()` | |
| **Version** | ❌ | ✅ `f.System.Version()` | |
| **Exception** | ❌ | ✅ `f.System.Exception()` | |
| **Android ID** | ❌ | ✅ `f.System.AndroidId()` | |
| **Apple Push Token** | ❌ | ✅ `f.System.ApplePushToken()` | |

### 13. Vehicle

| Feature | kRandom | Bogus | Notes |
|---------|---------|-------|-------|
| **VIN** | ❌ | ✅ `f.Vehicle.Vin()` | |
| **Manufacturer** | ❌ | ✅ `f.Vehicle.Manufacturer()` | |
| **Model** | ❌ | ✅ `f.Vehicle.Model()` | |
| **Type** | ❌ | ✅ `f.Vehicle.Type()` | |
| **Fuel** | ❌ | ✅ `f.Vehicle.Fuel()` | |

### 14. Phone

| Feature | kRandom | Bogus | Notes |
|---------|---------|-------|-------|
| **Phone Number** | ❌ | ✅ `f.Phone.PhoneNumber()` | |
| **Phone Format** | ❌ | ✅ `f.Phone.PhoneNumberFormat()` | |

### 15. Database

| Feature | kRandom | Bogus | Notes |
|---------|---------|-------|-------|
| **Column Name** | ❌ | ✅ `f.Database.Column()` | |
| **Type** | ❌ | ✅ `f.Database.Type()` | |
| **Collation** | ❌ | ✅ `f.Database.Collation()` | |
| **Engine** | ❌ | ✅ `f.Database.Engine()` | |

### 16. Hacker / Tech

| Feature | kRandom | Bogus | Notes |
|---------|---------|-------|-------|
| **Abbreviation** | ❌ | ✅ `f.Hacker.Abbreviation()` | |
| **Adjective** | ❌ | ✅ `f.Hacker.Adjective()` | |
| **Noun** | ❌ | ✅ `f.Hacker.Noun()` | |
| **Verb** | ❌ | ✅ `f.Hacker.Verb()` | |
| **Phrase** | ❌ | ✅ `f.Hacker.Phrase()` | |

### 17. Images

| Feature | kRandom | Bogus | Notes |
|---------|---------|-------|-------|
| **Data URI** | ❌ | ✅ `f.Images.DataUri()` | |
| **Picsum URL** | ❌ | ✅ `f.Images.PicsumUrl()` | |
| **Placeholder URL** | ❌ | ✅ `f.Images.PlaceholderUrl()` | |
| **LoremFlickr URL** | ❌ | ✅ `f.Images.LoremFlickrUrl()` | |

### 18. Music & Entertainment

| Feature | kRandom | Bogus | Notes |
|---------|---------|-------|-------|
| **Music Genre** | ❌ | ✅ `f.Music.Genre()` | |
| **Review Text** | ❌ | ✅ `f.Rant.Review()` | |

### 19. Architectural Features

| Feature | kRandom | Bogus | Notes |
|---------|---------|-------|-------|
| **Seeding (global)** | ✅ `Random` constructor | ✅ `Randomizer.Seed` | |
| **Seeding (instance)** | ✅ Per generator | ✅ `UseSeed(int)` | |
| **Infinite Stream** | ✅ `stream()` | ✅ `GenerateForever()` | |
| **Generate List** | ✅ `generateList(n)` | ✅ `Generate(n)` | |
| **Generate Between** | ❌ | ✅ `GenerateBetween(min, max)` | |
| **Lazy Generation** | ⚠️ Via `stream()` | ✅ `GenerateLazy(n)` | |
| **Functional Transform** | ✅ `map()`, `filter()` | ⚠️ Via LINQ | kRandom built-in, Bogus via .NET |
| **Locale Support** | ❌ | ✅ 70+ locales | |
| **Locale Fallback** | N/A | ✅ Auto to English | |
| **Array Element Pick** | ❌ | ✅ `f.PickRandom(array)` | |
| **Weighted Random** | ❌ | ✅ `f.Random.WeightedRandom()` | |
| **OrNull Extension** | ❌ | ✅ `.OrNull(f, weight)` | |
| **Replace Pattern** | ❌ | ✅ `f.Random.Replace("###-???")` | |
| **Auto-increment Index** | ❌ | ✅ `f.IndexGlobal`, `f.IndexFaker` | |
| **Shuffle Collection** | ❌ | ✅ `f.Random.Shuffle(list)` | |

---

## Coverage Summary by Category

| Category | kRandom Coverage | Bogus Coverage | Gap Priority |
|----------|-----------------|----------------|--------------|
| **Primitives** | 90% | 100% | 🟡 Low — Add Even/Odd, GUID |
| **Object Population** | 60% | 100% | 🟠 Medium — Add fluent API |
| **User/Person** | 70% | 90% | 🟡 Low — Add job titles, full name |
| **Network** | 50% | 100% | 🟠 Medium — Add MAC, domain, URL |
| **Algorithms/Math** | 100% (unique) | 20% | 🟢 kRandom leads |
| **Games** | 100% (unique) | 0% | 🟢 kRandom leads |
| **Finance** | 10% | 100% | 🔴 High — Critical gap |
| **Commerce** | 0% | 100% | 🔴 High — Critical gap |
| **Address** | 0% | 100% | 🔴 High — Critical gap |
| **Date/Time** | 10% (birthday) | 100% | 🔴 High — Critical gap |
| **Lorem/Text** | 0% | 100% | 🔴 High — Critical gap |
| **Company** | 0% | 100% | 🟠 Medium |
| **System/Files** | 0% | 100% | 🟡 Low |
| **Vehicle** | 0% | 100% | 🟡 Low |
| **Phone** | 0% | 100% | 🟠 Medium |
| **Architecture** | 70% | 100% | 🟠 Medium — Add fluent API |

---

## Recommended Roadmap for Feature Parity

### Phase 1: Critical Gaps (High ROI)
1. **Lorem/Text Generators** — Broad applicability for test data
2. **Date/Time Generators** — Essential for realistic data
3. **Address Generators** — Common test requirement
4. **Finance/Commerce** — E-commerce testing critical

### Phase 2: Medium Priority
1. **Fluent `Faker<T>` API** — Improve object population UX
2. **Full Name & Job Titles** — Complete user data
3. **Network Extensions** — MAC, domains, URLs
4. **Phone Numbers** — Locale-aware formats

### Phase 3: Nice-to-Have
1. **Locale Support** — Multi-language data
2. **Company/Business** — Enterprise testing
3. **System/Files** — File path/MIME testing
4. **Vehicle/Entertainment** — Niche domains

### Phase 4: Architectural Enhancements
1. **Weighted Random Selection**
2. **OrNull Probabilistic Decorators**
3. **Named Rule Sets**
4. **Auto-increment Helpers**

---

## kRandom Unique Strengths

Features where kRandom **leads** or has **no Bogus equivalent**:

1. **Fibonacci Sequence Generation** — Mathematical sequences
2. **Prime/Composite Number Generation** — Sieve of Eratosthenes
3. **Explicit Dice Generators** — D4 through D20 with fairness guarantees
4. **Natural Number Generation** — Mathematical utilities
5. **Dedicated Luhn Generator** — Exposed vs. internal in Bogus
6. **Dual Java/Kotlin API** — Multi-language JVM support (vs. .NET only)
7. **Max Depth Control** — Prevents infinite recursion in object population

---

## Conclusion

**Current State:**
- kRandom excels in **mathematical/algorithmic** generators and **gaming utilities**
- Bogus dominates in **realistic data** (addresses, commerce, lorem, finance)
- Both are strong in **primitives** and **user data**

**Strategic Recommendation:**
Focus Phase 1 development on **Lorem, Date/Time, Address, and Finance** to close the 80% use-case gap while maintaining kRandom's unique mathematical strengths.
