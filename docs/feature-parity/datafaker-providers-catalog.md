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
| Zodiac | 🟢 | Western (12) + Chinese (12) signs; derivable from a birthdate. Deterministic, no licensing risk |
| Mbti | 🟢 | 16 personality types; uniform or weighted pick |
| Nato Phonetic Alphabet | 🟢 | Alpha…Zulu; trivial fixed list, high fixture value |
| Pronouns | 🟢 | he/him, she/her, they/them, neopronouns; locale-aware |
| Measurement | 🟢 | units (metric/imperial), quantities |
| Nation / Nationality / Language Code | 🟢 | demonyms + ISO language names/codes; locale-aware |
| Vehicle | 🟢 | VIN (check-digit valid), make/model, plate |
| Weather | 🟢 | description, temperature (locale unit) |
| Passport | 🟢 | locale-formatted passport numbers |
| Driving License | 🟢 | locale-formatted license numbers |
| Programming Language | 🟢 | language names + versions |
| University | 🟢 | institution names |
| Restaurant | 🟢 | names, types |
| Hobby | 🟢 | activity vocabulary |
| Financial Terms | 🟢 | finance vocabulary |
| Computer / Device | 🟢 | OS, platform, device names |
| Aws / Azure | 🟢 | cloud resource-name shapes (ARNs, regions, service ids) |
| CNPJ (BR) | 🟢 | Brazilian company id, check-digit valid (national-id family) |
| CPF (BR) | 🟢 | Brazilian person id, check-digit valid (national-id family) |

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
| Finance | `IbanGenerator`, `BicGenerator`, `AbaRoutingGenerator`, `BankInfoGenerator` |
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
| ✅ HAVE / 🟡 PARTIAL | ~45 Base | core, shipped |
| 🟢 BACKLOG (curated) | ~21 Base | **build into core, one slice at a time** |
| 🟣 NOVELTY | ~60 Base + 129 (groups 2–5) | optional `krandom-novelty` module |
| ⛔ SKIP | ~6 | intentionally out of scope |

**Net core engineering work from DataFaker = the ~21 🟢 backlog providers + locale
expansion (35 → 60+).** Everything else is either done, a module concern, or a
deliberate skip — consistent with the locked GAP-TRACKER strategy.

---

## Build order (proposed)

Slice cadence = one provider per PR, full source + provider/registry +
resources (where locale-varying) + tests + `./scripts/pre_commit_check.sh`,
mirroring the Blood Type slice.

1. **Blood Type** ✅ shipped
2. **Person-attribute pair: Zodiac + MBTI** — natural follow-on to Blood Type (deterministic, no curated catalog, immediate fixture value)
3. NATO Phonetic Alphabet · Pronouns (tiny, high-value fixed lists)
4. Vehicle (VIN check-digit) · Passport · Driving License (validated-id family)
5. Nation / Nationality / Language Code (locale vocabulary)
6. Programming Language · Computer/Device · Aws/Azure (engineering fixtures)
7. University · Restaurant · Hobby · Financial Terms · Measurement · Weather
8. CNPJ / CPF (extend `NationalIdGenerator` family)
