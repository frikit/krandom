# DataFaker Provider Catalog — Live Mapping

Full mapping of **every provider** documented at
<https://www.datafaker.net/documentation/providers/> (snapshot **2026-06-21**)
against krandom's current generators. This is the "what can we take from
DataFaker" inventory that feeds the curated Base-provider backlog in
[`GAP-TRACKER.md`](./GAP-TRACKER.md). The narrative feature matrix lives in
[`datafaker-parity.md`](./datafaker-parity.md); this file is the **provider-by-provider** index.

DataFaker documents **256 providers** across 5 groups:
Base (127) · Entertainment (74) · Food (8) · Healthcare (5) · Sport (9) · Videogame (33).

## Legend

| Tag | Meaning |
|-----|---------|
| ✅ **HAVE** | Implemented in krandom core today |
| 🟡 **PARTIAL** | Partially covered; some sub-features missing |
| 🟢 **BACKLOG** | Core-worthy, realistic, in-scope — **the implementable list** (GAP-TRACKER "curated ~20") |
| 🟣 **NOVELTY** | Pop-culture / fandom / long-tail vocabulary → optional `krandom-novelty` module, **not core** |
| ⛔ **SKIP** | Intentionally out of scope (sensitive data, trivial to compose, or curated reference data) |

**Strategy (locked in GAP-TRACKER):** core only takes ✅/🟡/🟢. Every 🟣 is a
candidate for the opt-in novelty module so headline provider *count* can approach
DataFaker's without bloating core. ⛔ stays out by design.

---

## Group 1 — Base Providers (127)

### 🟢 Core backlog — implementable next (the curated ~20)

| DataFaker provider | krandom | Notes |
|--------------------|---------|-------|
| Blood Type | ✅ DONE | `BloodTypeGenerator` — locale-weighted, slice 1 (2026-06-21) |
| Zodiac | ✅ DONE | `ZodiacGenerator` + `ChineseZodiacGenerator` (user) — localized across all 35 locales (`krandom/zodiac`, `krandom/chinese_zodiac`); `signFor(date)`/`animalFor(year)` |
| Mbti | ✅ DONE | `MbtiGenerator` (user) — 16 types + `withNickname()` |
| Nato Phonetic Alphabet | ✅ DONE | `NatoPhoneticGenerator` (text) — universal ICAO standard |
| Pronouns | ✅ DONE | `PronounGenerator` (user) — `subject/object` sets localized across all 35 locales (`krandom/pronouns`) |
| Measurement | ✅ DONE | `MeasurementGenerator` (measurement) — units/quantities localized across all 35 locales |
| Nation / Nationality / Language Code | ✅ DONE | `NationalityGenerator` (user) — demonyms localized across all 35 locales |
| Vehicle | ✅ DONE | `VehicleGenerator` + `VinGenerator` (vehicle) — make/model + ISO-3779 VIN |
| Weather | ✅ DONE | `WeatherGenerator` (weather) — description/temperature localized across all 35 locales |
| Passport | ✅ DONE | `PassportGenerator` (user) — generic format `[A-Z][0-9]{8}` |
| Driving License | ✅ DONE | `DrivingLicenseGenerator` (user) — generic format `[A-Z]{2}[0-9]{6}` |
| Programming Language | ✅ DONE | `ProgrammingLanguageGenerator` (tech) — universal proper nouns |
| University | 🟢 | institution names — **deprioritized**: needs curated real per-locale institution names (data/research task, not the translation pattern) |
| Restaurant | ✅ DONE | `RestaurantTypeGenerator` (commerce) — cuisine/type localized across all 35 locales |
| Hobby | ✅ DONE | `HobbyGenerator` (user) — activity vocabulary localized across all 35 locales (`krandom/hobbies`) |
| Financial Terms | ✅ DONE | `FinancialTermGenerator` (finance) — finance vocabulary localized across all 35 locales |
| Computer / Device | ✅ DONE | `ComputerGenerator` (tech) — universal OS/platform/deviceType |
| Aws / Azure | ✅ DONE | `AwsGenerator` + `AzureGenerator` (tech) — universal regions/instanceId/s3Bucket, resourceGroup |
| CNPJ (BR) | ✅ DONE | `CnpjGenerator` (commerce) — Brazilian company ID; canonical config fails closed and the deprecated bridge is check-digit valid |
| CPF (BR) | 🟡 Configured-only | `NationalIdGenerator` with explicit `REALISTIC_UNCLASSIFIED` policy; the default fails closed |

### ✅ Already covered

| DataFaker provider | krandom generator(s) |
|--------------------|----------------------|
| Address | `StreetAddressGenerator`, `AddressInfoGenerator` |
| Barcode | 🟡 `EanGenerator`, `UpcGenerator` (whole codes; taxonomy skipped) |
| Bool | `BooleanGenerator`, `NullableBooleanGenerator` |
| Business | 🟡 `CompanyInfoGenerator` family |
| Code | 🟡 `IsbnGenerator`, `EanGenerator`, `IsinGenerator`, `CusipGenerator`… |
| Coin | `CoinGenerator` (games.coin) |
| Color | `ColorGenerator` |
| Commerce | `CommerceGenerator`, `ProductInfoGenerator`, `OrderInfoGenerator` |
| Company | `CompanyNameGenerator`, `CompanyUrlGenerator`, `CompanyEmailGenerator` |
| Country | `CountryGenerator` |
| Credentials | 🟡 `PasswordGenerator`, `UsernameGenerator` |
| Currency | `CurrencyGenerator`, `CurrencyPairGenerator` |
| Date And Time / Time / Time And Date | `DateGenerator`, `TimeGenerator`, `Instant/LocalDateTime/…` |
| Demographic | 🟡 `GenderGenerator`, `MaritalStatusGenerator`, `EducationalAttainmentGenerator` |
| Domain | `DomainGenerator` |
| Fake Duration | `DurationGenerator` |
| File | `FileNameGenerator`, `FileExtensionGenerator`, `MimeTypeGenerator`, path gens |
| Finance | 🟡 Configured-only banking identifiers via `IbanGenerator`, `BicGenerator`, `AbaRoutingGenerator`, and `BankInfoGenerator`; canonical configuration fails closed |
| Gender | `GenderGenerator` |
| Hashing | `HashGenerator` |
| Id Number | 🟡 `NationalIdGenerator` (US/UK/AU/FR/DE/JP/ES/IT/BR/CN) |
| Image | 🟡 `AvatarUrlGenerator` (avatar URLs; generic image URLs ⛔) |
| Industry Segments | `IndustryGenerator` |
| Internet | `EmailGenerator`, `URLGenerator`, `UriGenerator`, `IPv4/IPv6Generator`, `MacAddressGenerator`, `PortGenerator`, `UserAgentGenerator` |
| Job | `JobFieldGenerator`, `JobTypeGenerator`, `PositionGenerator`, `SeniorityGenerator` |
| Locality / Location | `RandomLocaleGenerator`, location package (`CoordinatesGenerator`, `GeohashGenerator`…) |
| Lorem / Text / Word / Verb | `LoremIpsumGenerator`, `TextGenerator`, `WordGenerator`, `SentenceGenerator`, `ParagraphGenerator` |
| Money | `MoneyGenerator` |
| Name | `FullNameGenerator`, `FirstName`/`LastName`/`MiddleName`, `TitleGenerator`, `SuffixGenerator` |
| Number / Demographic numbers | `NumberGenerator`, `IntGenerator`, `DoubleGenerator`, `BigDecimalGenerator`, … |
| Options | `PickGenerator`, `PickSetGenerator`, `WeightedGenerator`, `EnumGenerator` |
| Phone Number | `PhoneNumberGenerator` |
| Unique | `UniqueGenerator`, `Generators.unique(...)` |

### 🟡 Partial / 🟣 Novelty / ⛔ Skip — remaining Base providers

| DataFaker provider | Status | Rationale |
|--------------------|--------|-----------|
| Crypto Coin | 🟡 PARTIAL | `CryptoAddressGenerator` has addresses; coin-name catalog → novelty |
| Twitter | 🟡 PARTIAL | `SocialHandleGenerator`/`SocialProfileGenerator` cover handles |
| Subscription / Stock | ⛔ SKIP | `PaymentInfoGenerator` done; subscription & ticker catalogs out of scope |
| Compass / Size / Garment Size | ⛔ SKIP | trivial fixed lists; compose from `PickGenerator` |
| Relationship | ⛔ SKIP | niche kinship vocabulary, low fixture value |
| Ancient, Animal, App, Appliance, Artist, Australia, Aviation, Brand, Camera, Cannabis, Cat, Chiquito, Community, Construction, Cosmere, Culture Series, Dc Comics, Drone, Dungeons And Dragons, Educator, Electrical Components, Emergency, Emoji, Famous Last Words, Fingerprint, Funny Name, Hacker, Hipster, Hololive, Horse, House, Kpop, Large Language Model, Marketing, Matz, Medical, Military, Mood, Mountain, Mountaineering, Music, Nigeria, Olympic Sport, Photography, Planet, Robin, Rock Band, Science, Shakespeare, Sip, Slack Emoji, Space, Superhero, Team, Tire, Transport, Yoda | 🟣 NOVELTY | Pop-culture / fandom / long-tail / domain vocabularies → `krandom-novelty` module |

---

## Groups 2–5 — wholesale novelty (→ module, not core)

These groups are **entirely** pop-culture / lifestyle / domain catalogs. None
enter core; high-demand ones get ported to `krandom-novelty` after a
per-catalog licensing review (GAP-TRACKER Phase 3).

- **Entertainment (74):** Avatar, Back To The Future, Big Bang Theory, Bluey, Book, Breaking Bad, Buffy, Chuck Norris, Death Note, Doctor Who, Dune, Family Guy, Friends, Futurama, Game Of Thrones, Ghostbusters, Harry Potter, Hitchhikers Guide, Hobbit, How I Met Your Mother, Lord Of The Rings, Movie, Naruto, One Piece, Pokemon, Princess Bride, Rick And Morty, Seinfeld, Simpsons, South Park, Spongebob, Star Trek, Star Wars, Stranger Things, Studio Ghibli, Witcher, … (74 total) → 🟣 module
- **Food (8):** Apple, Beer, Cheese, Coffee, Dessert, Food, Ice Cream, Tea → 🟣 module
- **Healthcare (5):** Care Provider, Disease, Medical Procedure, Medication, Observation → 🟣 module (needs domain ownership; see `healthcare-*` skills)
- **Sport (9):** Baseball, Basketball, Chess, Cricket, England Foot Ball, Football, Formula1, Martial Art, Volleyball → 🟣 module
- **Videogame (33):** Clash Of Clans, Dark Souls, Elden Ring, Elder Scrolls, Fallout, Final Fantasy XIV, Half Life, Hearthstone, League Of Legends, Mass Effect, Minecraft, Overwatch, Sonic, Street Fighter, Super Mario, Zelda, … (33 total) → 🟣 module

---

## Scoreboard

| Bucket | Count (approx) | Disposition |
|--------|----------------|-------------|
| ✅ HAVE / 🟡 PARTIAL | ~65 Base | core, shipped (incl. the now-cleared curated backlog) |
| 🟢 BACKLOG (curated) | 1 Base (University) | **deprioritized — needs curated per-locale institution data** |
| 🟣 NOVELTY | ~60 Base + 129 (groups 2–5) | optional `krandom-novelty` module |
| ⛔ SKIP | ~6 | intentionally out of scope |

**Net core engineering work from DataFaker = essentially done.** The curated 🟢
Base backlog is cleared except **University** (deprioritized: needs curated real
per-locale institution names, a data/research task rather than the translation
pattern). Remaining headroom is locale expansion (35 → 60+). Everything else is
either shipped, a module concern, or a deliberate skip — consistent with the
locked GAP-TRACKER strategy.

---

## Build order (proposed)

Slice cadence = one provider per PR, full source + provider/registry +
resources (where locale-varying) + tests + `./scripts/pre_commit_check.sh`,
mirroring the Blood Type slice.

1. **Blood Type** ✅ shipped
2. **Person-attribute pair: Zodiac + MBTI** ✅ shipped (+ Chinese Zodiac, Nationality, Pronouns, Hobby)
3. NATO Phonetic Alphabet · Pronouns ✅ shipped
4. Vehicle (VIN check-digit) · Passport · Driving License ✅ shipped
5. Nation / Nationality / Language Code ✅ shipped
6. Programming Language · Computer/Device · Aws/Azure ✅ shipped
7. Restaurant · Hobby · Financial Terms · Measurement · Weather ✅ shipped — **University** still open (deprioritized: needs curated per-locale institution data)
8. CNPJ ✅ shipped; CPF is available only through an explicit national-ID compatibility policy

**Curated Base backlog is now essentially cleared — only University remains.**
