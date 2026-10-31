# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

## [Unreleased]

### Added

- `NetworkSafetyPolicy`, set with `GeneratorConfig.Builder.networkSafetyPolicy` or
  `krandom.network-safety-policy` in the Spring starter.
- `GeneratorConfig.getObjectRequestedUniqueFieldNames()`, the names given to `objectUniqueField` and
  `objectUniqueFields`.
- `config { objectUniqueFields(...) }` in the Kotlin DSL, which had no way to request unique fields. It
  replaces the names that are unique by default.
- `KrandomExtension` now injects `Krandom` parameters. A session is built from the same configuration
  as the `GeneratorConfig` parameter, so `@KrandomSeed`, replay overrides and recipes apply, and a
  failure's seed reproduces its values. Every `Krandom` parameter of one test, across its constructor,
  `@BeforeEach`, test method and `@AfterEach`, is the same session, so streams continue from one method
  to the next. Each test, repetition and parameterized invocation has its own session, and
  `@BeforeAll`, `@AfterAll` and `PER_CLASS` constructors share one for each class.

### Changed

- Generated network values are now reserved fixture destinations by default, under
  `NetworkSafetyPolicy.TEST_SAFE_RESERVED`. They pointed at real targets, such as `218.13.174.12`,
  `http://digital.us`, `https://github.com/vsmith553`, and real avatar hosts. IPv4 addresses and CIDRs
  are now in the RFC 5737 blocks, IPv6 in `2001:db8::/32`, and domains, hostnames, URLs, URIs, company
  URLs, avatar URLs, and social links under `.test`, with their usual labels and shape, as in
  `https://www.digital.test/files?id=525` and `https://github.test/vsmith553`. MAC addresses are locally
  administered and unicast. The promise is a reserved destination, not transport isolation. An explicit
  TLD or destination is used verbatim, while a scheme or subdomain that would end the host is rejected.
  `generatePublic()` and `generatePrivate()` now fail with `IllegalStateException`, and a unique IP fails
  when the 762 reserved IPv4 addresses run out. The object engine, JSON Schema formats, and providers
  follow: a generated `domain`, `url`, or `companyUrl` is reserved, an email beside a generated `domain`
  takes it, and a `companyName` derives a `.test` domain even with no email field. Seeded output changes
  for all of these and for values that depend on them. `REALISTIC_UNCLASSIFIED` restores the previous
  output draw for draw. A recipe records the policy in the new setting `network.safety-policy`, and
  replay requires it, so a recipe without it, such as one from an earlier version, is rejected.
- A recipe now records which unique fields were requested, in the new setting
  `object.requested-unique-fields`, as a replay must treat them the same way. Replay requires it and
  `object.unique-fields`, so a recipe without them, such as one from an earlier version, is rejected.
- Update Spring Boot to 4.1.2 (the `krandom-spring-boot-starter` dependency), the Gradle wrapper to
  9.8.2, Error Prone to 2.51.0, and NullAway to 0.14.3; align the consumer examples with Spring Boot
  4.1.2 and Maven Surefire 3.6.1.
- Generated UTC offsets now follow one rule, `-12:00` to `+14:00` in 15-minute steps, in
  `ZoneOffsetGenerator`, `OffsetTimeGenerator`, `OffsetDateTimeGenerator`, and the `ZoneOffset`,
  `OffsetTime`, and `OffsetDateTime` members of generated objects, including semantic fields such as
  `createdAt`. `ZoneOffsetGenerator` and `OffsetTimeGenerator` drew any second in plus or minus 18:00,
  such as `+07:17:55`. `OffsetDateTimeGenerator` and the object engine's `ZoneOffset` and `OffsetTime`
  drew quarter hours up to plus or minus 18:00. An `OffsetDateTime` member took a random zone's offset,
  such as `-00:44:30`, and now keeps only that local date-time. The range and grid are a fixture
  convention covering the offsets in actual use, not a standard: RFC 3339 excludes seconds from an
  offset but imposes neither, and JSON imposes nothing. `OffsetDateTimeGenerator.between` still bounds
  the instant, and a `ZonedDateTime` or `Calendar` keeps its zone's offset. Seeded output changes for
  these offsets and for values that depend on them, such as an `updatedAt` aligned with an
  `OffsetDateTime` `createdAt`, an `age` read from an `OffsetDateTime` `birthDate`, or a field sharing a
  stream with a unique `OffsetDateTime` field whose retries change.
- `InstantGenerator` and the `Instant` members of generated objects are now generated with a time of
  day, to the millisecond, within the selected UTC day. They were always midnight UTC, so time-of-day
  logic was never exercised and two instants on one day compared equal. This improves coverage and does
  not guarantee distinct instants. The coherence pass can still write midnight, when it aligns an
  `Instant` with a sibling that holds only a date or derives a `birthDate` from a constrained or
  overridden `age`. A date range still includes the whole of its last day, through `23:59:59.999Z`. A
  semantic `createdAt` or `updatedAt` with no configured range can now be later than a fixed clock's
  current time on the clock's date, as a `LocalDateTime`, `Date`, or `Timestamp` field already could, and
  `STRICT` mode can copy such a value over a `@Past` sibling. Seeded output changes for `Instant` values
  and for values that depend on them, such as a `Date` `updatedAt` aligned with an `Instant` `createdAt`.

### Fixed

- The coherence pass no longer moves a `createdAt` or `updatedAt` out of a configured `objectDateRange`. An
  `OffsetDateTime` or `ZonedDateTime` is generated with the range on its local date, but its sibling was
  aligned at UTC, so on a one-day range about one such object in ten had a sibling a day outside it: a
  `createdAt` of `2024-02-29T23:00-12:00` gave an `updatedAt` of `2024-03-01T11:00Z`. Where the UTC date is
  outside the range, an `updatedAt` that holds an offset now reads the same instant at the offset nearest UTC,
  in 15-minute steps, that gives a date in the range, here `2024-02-29T23:45-11:15`. Any other `updatedAt`
  stays while `createdAt` moves to it, to midnight UTC beside a date-only `updatedAt`. `createdAt` is still
  not after `updatedAt`, and this changes seeded output only for objects that were out of the range. A field
  that cannot move, such as an override or a `@Past` value, can still take its sibling out of the range, where
  no date in it keeps the pair in order. A `birthDate` derived from a constrained or overridden `age` still
  follows that age out of the range.
- `ZonedDateTime` and `OffsetDateTime` members and `new ZonedDateTimeGenerator(min, max)` no longer pass the
  last date of a range when a gap in the drawn zone's rules moves them forward. `Pacific/Apia` and
  `Pacific/Fakaofo` skipped `2011-12-30`, so a one-day range on that date gave `2011-12-31` about once in 300
  draws. A `ZonedDateTime` is now the drawn date-time at UTC, and an `OffsetDateTime` keeps it, with its drawn
  offset. This also holds for a semantic field's default window, which ends on the clock's date. Seeded output
  changes only for objects that drew such a value, which may differ after the coherence pass too.
- `@Email` fields no longer get an ordinary domain under the default
  `EmailDomainPolicy.TEST_SAFE_RESERVED_DOMAINS`. They drew a random `.com`, `.net` or `.org` domain,
  such as `fbww@dgeedi.org`. They now use `example.com`, `example.net` or `example.org`, or a `.test`
  domain when `@Size` rules those out. A domain in the annotation's `regexp` or a `@Pattern` is used
  as written, and `REALISTIC_UNCLASSIFIED` keeps the old shape. Seeded output changes for these
  fields.
- Sibling fields no longer give a generated email an ordinary domain under that policy. A
  `companyEmail` beside a `companyName` used to become `hello@acmelabs.com`, and an `email` beside a
  `domain` took that domain's generated value. In an object with an email field, a domain derived
  from a company name now ends in `.test`, and a generated `domain` takes the email's reserved domain.
  A domain or URL set through an override is still used verbatim, and `REALISTIC_UNCLASSIFIED` keeps
  `.com`. Seeded output changes for objects with these fields.
- A unique `domain` field now numbers a subdomain (`n1.acme.test`). It used to append to the
  top-level domain (`acme.test1`), which left the reserved space.
- `AvatarUrlGenerator` no longer writes the size in the digits of the JVM's default locale, which
  gave `size=٢٥٦` on an Arabic JVM.
- `SqlTimeGenerator` now generates a time of day, to the second, so a seeded `java.sql.Time` reads the
  same whichever of the JDK's time zones is the JVM's default. It used to change with the zone.
  Seeded output changes.
- `SchemaParser` now applies a field's constraints together, so a generated value meets all of them,
  and a combination with no value is rejected with its schema path. Seeded output changes for the
  schemas below.
- A `pattern` or `format` no longer ignores `minLength` and `maxLength`. `^A{8}$` with `maxLength` 3
  generated `AAAAAAAA`, and is now rejected. Values are drawn again until one fits, up to 10,000 times
  each. This samples rather than analyses, so a combination that rarely fits can also be rejected,
  while parsing or on a later record. `^[a-z]+$` with `minLength` 20 used to generate values that were
  too short, and now generates 20 to 40 characters, as a pattern's `*`, `+` and `{n,}` can repeat up to
  twice `minLength`, or up to 10,000 times.
- A `const` or `enum` value must now meet the field's `type`, length bounds, `pattern`, numeric bounds,
  `minItems`, `maxItems`, `items` and `properties`. An `enum` keeps the values that do: `[a, bbbb]`
  with `maxLength` 1 now generates only `a`. A `const` that breaks one is rejected, including a `const`
  missing from the `enum` beside it, which used to win. `format` is not checked on these values,
  because it is an annotation in Draft 2020-12.
- Numeric bounds now intersect. `minimum` 100 with `exclusiveMinimum` 0 used to draw as low as 1.
  OpenAPI 3.0's `exclusiveMinimum` and `exclusiveMaximum` flags now work: `maximum` 1 with
  `exclusiveMaximum: true` used to draw up to 1,000,000 on a `number`, and `exclusiveMinimum: false`
  used to exclude the minimum on an `integer`.
- An `integer` now rounds a fractional bound inward. `minimum` 1.5 used to allow 1, and a bound beyond
  the range of a `long` used to wrap around or saturate.
- These are no longer rejected or left to fail while generating: `minLength` 0, `maxLength` 0, which
  generates the empty string, a lone `minimum` or `maximum` beyond the default range, such as `minimum`
  2000000 on a `number`, and a lone `minItems` above the default array size or `maxItems` below it.
  `minItems` above `maxItems` now fails while parsing.
- A schema with no `type` now honours `pattern`, `format` and length bounds. It used to generate any
  text.
- `nullable: true` no longer adds `null` to a `const` or `enum` that leaves it out, and `type: string`
  now holds for a name that matches a provider of another type: `datetimeTimestamp` generated a `Long`
  and now generates its digits as a string.
- A `number` now stays within a bound that a double cannot hold. `minimum` 9007199254740993 used to
  allow 9007199254740992. A range wider than a double holds, such as `minimum` -1.7976931348623157E308
  with `maximum` 1.7976931348623157E308, now generates values, where it used to fail while generating.
- `format: idn-email` now generates emails, which are all valid for it. It used to generate a random
  string.
- A `@NotNull` nested object member is no longer left `null` at `objectMaxDepth`. With
  `objectMaxDepth(1)`, `record Top(Mid mid)` and `record Mid(@NotNull Leaf leaf)` used to give
  `Top[mid=Mid[leaf=null]]`. Generation now fails with an `ObjectGenerationException` naming
  `Top.mid.leaf`, whether `@NotNull` is on a field, getter, record component or constructor parameter.
  Raise `objectMaxDepth` to generate the member, or give it an override. `objectIgnoreErrors(true)`
  keeps the `null` and reports to `generationFailureListener`. A member with no required constraint,
  or only `@Size`, is still left `null`, and so is an element of `List<@NotNull Leaf>`. Below the limit,
  a cycle is still cut with `null`, even on a `@NotNull` member. A nested `ObjectFaker` rule runs after
  generation, so it needs a larger `objectMaxDepth` too.
- A field requested with `objectUniqueField` or `objectUniqueFields` now stays unique however its value
  is produced. Only semantic, built-in and enum values were checked, so `objectUniqueField("id")` with
  `@Min(1) @Max(1000) int id` repeated values with no error. Overrides, `@Randomizer`, `@Fake`,
  `@FakeRange` and Bean Validation constraints are now checked too, and a repeat draws again. So a
  constant override on a requested field now fails on the next object from the same generator. A
  requested name is matched as given: requesting `id` does not do this for `customerId`. Dependent and
  nested `ObjectFaker` rules run after generation and are not checked. A repeat uses up a draw, so seeded
  output changes for objects where one happens. The names unique by default, such as `email` and `id`, are
  not requested: an override, annotation or constraint on one of them may still repeat its values.
- Each unique field now has its own values. All the aliases of a name, and the same name on different
  types, used to share one set. `orderId` and `customerId` can now be equal, and so can `Order.id` and a
  nested `Customer.id`. A field inherited from a superclass is one field, whichever subclass reaches it,
  and a constructor argument shares the values of the field with its name. Seeded output changes for
  objects where two such fields would have drawn the same value.
- A unique field whose values run out now fails through the object engine's failure handling. It used
  to throw an error without a path, and without telling `generationFailureListener`, whatever
  `objectIgnoreErrors` said. In a nested object the reason was lost as well. The error now names the
  path, such as `Outer.mid.id`. `objectIgnoreErrors(true)` gives the field the default for its type,
  or `null` for a derived value such as an email, and tells the listener.
- The coherence pass no longer overwrites a requested `createdAt`, `updatedAt`, `birthDate`, `age`,
  `price`, `amount`, `balance`, `active` or `status` with a derived value, which could repeat. The field
  keeps its generated value, and the other field of the pair adapts where it can. So two requested
  fields can be left out of line with each other: uniqueness wins.
- A derived value that a unique field already holds is no longer numbered. A generated unique `domain`
  with no email beside it was always `n1.`-prefixed, and with `objectUniquenessMaxAttempts(1)` it failed
  on the first object. Seeded output changes for such a field. A field initializer stays as the class
  set it, where a unique `domain` initializer was numbered from the second object on.
- `objectUniqueFields()` with no names no longer breaks a seeded configuration. Generating with it, or
  asking it for a recipe, failed with `setting value must be a non-blank single-line value`. A recipe now
  writes an empty set of names as `-`.
- `RegexGenerator` no longer generates values that do not match its pattern. For any pattern, every value
  now satisfies `Pattern.compile(pattern).matcher(value).matches()`, or the constructor throws
  `IllegalArgumentException`. `^(?:abc)$` generated `?:abc`, `\bfoo\b` generated `bfoob`, `[\d]` drew `\`
  and `d`, and `a+?` generated `aaa?`. This also covers `Generators.ofRegex`, `regexify`, `@Pattern`,
  `@Email(regexp = ...)` and a JSON Schema `pattern`. Patterns it already got right keep their seeded
  values, apart from the two changes noted below. Values do change for a pattern that matched only by
  accident, such as `\S*$+`, which wrote the `+` into the value, or `[\D]`, which drew the letter `D`.
- `RegexGenerator` now supports non-capturing and named groups, `|` at the top level, escapes inside a
  character class, such as `[\w.-]`, a `]` first in a class, lazy quantifiers, `\f`, `\a`, `\e`, `\xhh`,
  `\x{h...h}`, `\uhhhh`, and a repeated supplementary character. `^`, `$`, `\A`, `\Z`, `\z` and `\b` are
  now checked at construction: they are accepted where they hold on every value, such as `\b(foo|bar)\b`,
  and rejected elsewhere, such as `a^b`, which generated `ab`.
- `.` now draws from printable ASCII. It always generated a literal dot, so `.{5}` gave `.....`. Seeded
  output changes for a pattern with a `.`.
- `RegexGenerator` now rejects at construction what it cannot honour, with a message naming the construct
  and its position: lookaround, atomic groups, backreferences, inline flags, possessive quantifiers, a
  quantifier with nothing to repeat, `\B`, `\G`, `\b{g}`, `\R`, `\X`, `\h`, `\H`, `\v`, `\V`, `\p`, `\P`,
  `\N`, `\c`, octal `\0`, `\Q`, class intersection and nesting, a supplementary character in a class, and
  surrogates. A malformed pattern now fails with `java.util.regex`'s own error. `[abc`, `\d{3` and `\q`
  used to be accepted, and `a{5,2}` failed only while generating.
- `a{20,}` now generates 20 `a`s, where it failed while generating. `*`, `+` and `{n,}` repeat at most 10
  times, or `n` times when that is more. A `GeneratorConfig` whose minimum string length is above 5 now
  raises 10 to twice that length, up to 10,000, so that a value can reach it, and a JSON Schema `minLength`
  or a `@Size` minimum does the same. `[0-9]+` with `@Size(min = 20)` used to fail, and now works. Seeded
  output changes for a pattern with an open-ended repetition under such a minimum.
- A pattern `RegexGenerator` cannot generate from no longer fails a `@Pattern` or `@Email` field that
  another source can meet, as it is still enforced on every candidate. `\p{L}+` with
  `@Size(min = 2, max = 5)` used to fail, and now works. Where nothing can meet it, the failure names the
  unsupported construct. That includes a pattern the wrong text used to satisfy by luck: `^(?!admin).*$`
  alone generated `?!admin`, and now fails. Seeded output can change for a field whose pattern is no
  longer a source.
- A JSON Schema `pattern` that `RegexGenerator` cannot generate from is now rejected with its schema path.

## [2.9.0] - 2026-10-19

### Added

- `Krandom.maritalStatus()`, and `firstName(Gender)` and `middleName(Gender)` for names that match
  a gender. The gendered overloads draw from the same stream as `firstName()` and `middleName()`.

### Changed

- Marital statuses add `Annulled`, `Dissolved Partnership` and `Surviving Partner` in all 35
  locales, so each locale lists nine statuses in the same order. Where a country has a legal term,
  the entry uses it, for example `Ehe aufgehoben`, `Aufgehobene Lebenspartnerschaft` and
  `Durch Tod aufgelöste Lebenspartnerschaft` in German. Seeded marital-status output changes.
- United Kingdom (`en_GB`) phone numbers now come from Ofcom's ranges for TV and radio drama under
  the default `PhoneNumberSafetyPolicy.TEST_SAFE_WHERE_AVAILABLE`: mobile numbers from
  `07700 900000` to `07700 900999`, and landline numbers from 16 geographic blocks of 1,000, such
  as `020 7946 0000` to `020 7946 0999` and `0161 496 0000` to `0161 496 0999`. Ofcom sets these
  numbers aside for drama and does not allocate them to communications providers for their
  customers, and says it will not in the foreseeable future. `REALISTIC_UNCLASSIFIED` keeps the
  realistic area codes, mobile prefixes and subscriber numbers, with the same seeded output as
  before. Seeded `en_GB` phone output changes under the default policy.

## [2.8.0] - 2026-10-19

### Added

- `CountryGenerator.generateCodes()` and `currentCountryCodes()` return a `CountryCodes` record
  with the alpha-2, alpha-3 and numeric ISO 3166-1 codes of one country, so the three always
  belong together. The `Krandom` session adds `countryCode()`, `countryCodeAlpha3()` and
  `countryCodeNumeric()`, each drawn independently from its own stream, and `countryCodes()` for
  the three codes of one country.
- Address types and address purposes in all 35 locales. `Generators.ofAddressType()` gives the
  kind of property at an address (residential, commercial, industrial, agricultural, institutional
  or mixed-use; `Wohnen` in German), and `Generators.ofAddressPurpose()` what an address is used
  for (home, work, billing, shipping, mailing, temporary or previous; `Rechnungsadresse` in
  German). British and Australian English use their own wording ("Delivery address",
  "Correspondence address", "Postal address"). Both are also on `Generators.location()` and the
  `Krandom` session (`addressType()`, `addressPurpose()`), and custom locales register through
  `Vocabulary.ADDRESS_TYPE` and `Vocabulary.ADDRESS_PURPOSE`.

### Changed

- Country codes come from an ISO 3166-1 table bundled with kRandom
  (`krandom/country_codes/iso_3166_1.txt`, all 249 assigned countries) instead of the JDK's
  country list, so seeded codes no longer depend on the Java version. Alpha-2 and alpha-3 output
  is unchanged, because the table keeps the JDK's order. `generateCodeNumeric()` now draws from
  all 249 countries instead of 20, which changes its seeded output, and
  `currentCountryCodeNumeric()` works for every ISO 3166-1 country instead of 45.

## [2.7.0] - 2026-10-19

### Added

- `Krandom`, one seeded source of everyday fixture values that returns plain values. Create one
  session and reuse it (`Krandom fake = Krandom.seeded("orders", Locale.UK)`), then call
  `fake.firstName()`, `birthday(25, 80)`, `dateBetween(from, to)`, `postalCode()`,
  `nationalIdCompact()`, `pick(...)`, and more. Each method draws from its own stream (only
  `nationalIdCompact()` shares `nationalId()`'s, and the two `pick` overloads share one), derived
  from the seed and the method's name whatever the child-stream policy, so a method's values are
  unaffected by calls of other methods, and the same seed repeats the same data (dates too, with a
  fixed clock). `streamConfig(name)` gives any other generator a stream of its own. Kotlin and
  Scala callers get plain values with no generator or stream wiring.

### Changed

- Update Palantir Java Format to 2.102.0, which keeps a switch expression assigned to a variable on
  its declaration line; nine source files were reformatted.
- Update Jackson to 2.22.4 (the `krandom-jackson` dependency), Kotest to 6.2.6, Hibernate
  Validator to 9.1.5.Final, and PIT to 1.30.1; refresh the GitHub Action pins (setup-java 6.1.0).
- `ObjectFaker.ruleFor` and `ruleForContext` replace an earlier rule of any kind for the identical
  field path instead of failing with "already has a registered rule"; a replacement of the same
  kind keeps its place in the evaluation order. Defaults from a profile, a shared fixture, or an
  earlier `ObjectModel` can now be refined per test (`defaults.and(perTest)`). `ignore` on a field
  that has a rule still fails. A rule for an ignored nested path such as `address.city`, and a rule
  or `include` below an ignored nested path, now fail too instead of being dropped; `ignore` below
  an ignored nested path now fails as well.

### Fixed

- An override that targets a field now replaces a non-default field initializer. An `ObjectFaker`
  root rule, or an object override registered by owner type and field name or by field predicate,
  was skipped for an initialized field such as `List<Item> items = new ArrayList<>()` unless
  `objectOverrideDefaultInitialization(true)` was set. Type-level overrides still keep
  initializers, and `populate(existing)` now overwrites a field that has such an override. A field
  whose constructor argument was already produced by its by-name override is not generated a second
  time.
- An override of an inherited field is no longer rewritten by semantic coherence: an `ObjectFaker`
  rule on an inherited `email`, `domain`, `url`, or other harmonized field could be replaced by a
  derived value.
- A plain or predicate field override now takes precedence over a contextual type override, as
  documented; the contextual type override was resolved first.

### Documentation

- Explain that generators built from one seeded configuration share its random stream and how to
  give each its own with `forChildStream` under `ChildStreamPolicy.INDEPENDENT`, state that
  `krandom-junit` requires JUnit 6, and note the Scala 3 SAM-conversion pitfall for helpers
  overloaded on `Generator` and function types.

## [2.6.0] - 2026-10-14

### Added

- JUnit failure output prints copyable Gradle (`KRANDOM_JUNIT_RECIPE=base64:... ./gradlew test
  --tests '<class>' --rerun`) and Maven (`mvn test -Dtest='<class>' -Dkrandom.junit.recipe=...`)
  replay commands and states whether the recipe clock was read at failure time, captured before
  injection, or fixed by a replay recipe. `krandom.junit.recipe`, `krandom.junit.seed`, and
  `krandom.junit.snapshot-clock` are also read as JUnit configuration parameters
  (`junit-platform.properties`, launcher parameters), with `KRANDOM_JUNIT_SNAPSHOT_CLOCK` as a new
  environment fallback.
- `krandom.junit.class-recipe` (`KRANDOM_JUNIT_CLASS_RECIPE`) replays the class-scoped configurations
  used by `@BeforeAll`/`@AfterAll` and `PER_CLASS` constructors, one `<test class>=base64:<recipe>`
  entry per class. The printed replay commands include it whenever the failing test's enclosing
  classes had class-scoped configurations, so class-level fixtures replay with the test's own data.
- Kotest `checkAllWithRecipe(config, arbFactory, propTestConfig)` snapshots the clock once and uses
  that snapshot for sampling and the failure recipe.
- Spring `KrandomObjectFakerFactory.generator(type, streamName)` and `faker(type, streamName)` derive
  reproducible named child streams; `KrandomProviderCustomizer` beans register providers once when
  the auto-configured `ProviderHub` is created; the effective recipe is logged at startup.
- `CsvFormulaPolicy.NEUTRALIZE` for `Schema` and `SchemaProjection` CSV export prefixes cells that
  spreadsheets would evaluate as formulas; the default output is unchanged.
- `ObjectConstructionContext.isExcluded(String)` lets construction adapters honour field exclusions.
- `ChildStreamPolicy` with `GeneratorConfig.Builder#childStreamPolicy` and
  `GeneratorConfig#forChildStream`: the opt-in `INDEPENDENT` policy gives every child of a seeded
  composite generator (`PaymentInfoGenerator`, `OrderInfoGenerator`, `PersonInfoGenerator`, and the
  other built-in composites) its own named stream, so sibling values such as the payment, invoice,
  and order identifiers of one payment are no longer correlated. It requires a seed and is recorded
  in recipes as `child-stream-policy`; the default `LEGACY` keeps seeded output unchanged.
- `EmailDomainPolicy` with `GeneratorConfig.Builder#emailDomainPolicy`, recorded in recipes as
  `email.domain-policy` and exposed as `ProviderSafetyPolicy.EMAIL_DOMAIN` safety metadata on the
  `person.email` and `company.email` providers.
- Locale-aware vocabulary for industries, job fields, seniorities, positions, educational
  attainment, marital status, employment types, company names, company buzzwords and catch phrases,
  text words, commerce product vocabulary, database column names, directory names, bank names, bank
  types and bank-account vocabulary. Each concept follows the resource-backed pattern
  (`XDataProvider`, read-only `XDataRegistry`, `krandom/<concept>/<locale>.txt`) with an English
  `default.txt` fallback; all 32 non-English native locales ship real translations of every
  display vocabulary (database column and directory names keep their English identifiers outside
  the previously localized languages). Like hobbies or zodiac signs, each concept resolves through
  the configuration's `DataRegistryContext` under its own `Vocabulary` key, so scoped registrations
  apply and isolated contexts never read the global registries.
- `Vocabulary<P>` keys for the 39 built-in vocabularies (`Vocabulary.all()`),
  `Vocabulary.custom(name, providerType)` for consumer-defined vocabularies that live only in the
  contexts they are registered in, and the `LocaleDataProvider` contract that every vocabulary
  provider interface now extends.
- `Locale` constructors for `IndustryGenerator`, `JobFieldGenerator`, `SeniorityGenerator`,
  `PositionGenerator`, `EducationalAttainmentGenerator`, `MaritalStatusGenerator` and
  `CompanyNameGenerator`, plus `Generators.ofX(Locale)` overloads for these and for job types,
  buzzwords, catch phrases, text, commerce, database, directory paths, bank names and bank types.
- `GeneratorConfig` constructors for `CoinGenerator`, `DiceGenerator`, `LuhnGenerator`,
  `FibonacciGenerator`, and for every generator whose only seeded form took a raw `long` seed
  (numeric, big-number, boolean, regex, normal-distribution, date/time, locale, geohash, selection,
  template, age and birthday generators), including `DoubleGenerator` and `FloatGenerator`, whose
  `withPrecision` keeps the configuration's seed or caller-owned source, plus
  `CharGenerator.letters(GeneratorConfig)`,
  `StringGenerator.letters(GeneratorConfig)` and `StringGenerator.Builder#config`.
- `Generators.ofX(GeneratorConfig)` overloads for every no-argument factory (including `ofIban`,
  `ofUuid`, `ofCoin`, `ofDouble`, `ofFloat` and `ofDice(DiceType, GeneratorConfig)`), and
  configuration-aware `pick`, `pickSet`, `pool`, `shuffle` and `weighted`.
- Facade factories `ofFirstName`, `ofLastName`, `ofGender`, `ofAge`, `ofBirthday` and
  `ofCoordinates` (with `Locale`/`GeneratorConfig` forms where the generator supports them),
  keeping the facade in step with the fluent namespaces.
- `Generators.ofUrlObject`, `ofUriObject` and `ofTimeZoneObject` (and
  `DateTimeGenerators#timeZoneObject`) as the unambiguous object-returning factories.

### Changed

- Expand critical-path mutation testing to deterministic and safety-sensitive generators, with
  focused tests for UUID bit layout, crypto-address fail-closed behavior, and next-word boundaries,
  and to the object engine's `FieldGeneratorResolver` and `SemanticCoherenceAdjuster` plus
  `GeneratorConfig` and `GenerationRecipe`. Coherence tests call the adjuster's helpers directly
  instead of through reflection.
- JUnit constructor parameters under the default lifecycle now share the test method's
  configuration; `@BeforeAll`/`@AfterAll` and `PER_CLASS` constructor parameters receive a separate
  class-scoped configuration.
- Kotest adapters honour the configuration seed: it parents every sample seed and is recorded by
  `krandomKotestRecipe`, so seeded configurations produce different samples than 2.5.0 for the same
  Kotest seed (unseeded configurations are unchanged).
- Kotlin DSL `exclude(...)` targets the generated type's field instead of every field with that
  name; unknown exclusions and rules or exclusions on computed or delegated properties fail when the
  generator is built.
- Spring `krandom.recipe` now rejects every other generation property it already defines (clock,
  bounds, object settings, and policies, not only seed and locale); `@KrandomTest` excludes
  component-scanned application beans like Spring Boot slices (opt in with `@Import`).
- `CompetitorObjectBenchmark` reuses a prebuilt Instancio `Model`, and `FieldStreamsBenchmark` also
  measures the default `RELAXED` semantic mode.
- `@Randomizer` generators keep one instance per annotated field for the lifetime of a top-level
  generator, so stateful randomizers keep their state; `Seedable` randomizers are reseeded for every
  object from its seeded stream and replay from the seed and recipe.
- With `ObjectFieldStreamPolicy.INDEPENDENT`, an unrelated semantic provider (for example from an
  installed module) no longer shifts member streams. Seeded `INDEPENDENT` output in `RELAXED` and
  `STRICT` modes therefore differs from 2.5.0; `LEGACY` output is unchanged.
- Sorted containers (`SortedSet`, `NavigableSet`, `TreeSet`, `PriorityQueue`, `SortedMap`,
  `NavigableMap`, `TreeMap`) of comparable elements use natural ordering instead of text ordering.
- JSON Schema `pattern`, `minLength`, and `maxLength` now win over semantic field names.
- Object generation builds semantic, temporal, and provider generators on first use and looks up
  recipe presence and construction adapters once per generator. A portable seeded six-field object
  now allocates about 0.25 MB instead of 2.2 MB, with byte-identical seeded output.
- Document that unique fields stay unique for a generator's lifetime and how to reset or disable
  that tracking.
- Read the published-module list from `gradle.properties`.
- Run module-boundary and Markdown-link checks in CI; validate the Gradle wrapper; add read-only
  permissions, superseded-run cancellation, and job timeouts to the CI workflow; require the
  native-image smoke test in CI.
- Group Dependabot minor/patch Gradle and GitHub Actions updates, cover the consumer examples, and
  add `scripts/update_verification_metadata.sh` for regenerating dependency checksums.
- Compile `krandom-core` main sources with NullAway (JSpecify mode) through Error Prone, so the
  `@NullMarked` and `@Nullable` contracts are verified instead of documented; no other Error Prone
  check is enabled and test sources are not checked.
- Add `scripts/fix_dependabot_pr.sh`, which records the verification metadata for a Dependabot
  pull request in a temporary worktree under every JDK in `KRANDOM_METADATA_JDKS` and commits it
  on the branch, leaving the diff review and the push to the maintainer.
- Add `pre_commit_check.sh --fast`, module-boundary checks in the pre-commit gate, and JaCoCo
  reports for the integration modules; only `krandom-core` has the exact coverage gate and is
  uploaded to Codecov.
- `DataRegistryContext` is addressed by `Vocabulary` key: `provider(Vocabulary, Locale)`,
  `isRegistered(Vocabulary, Locale)`, `registeredKeys(Vocabulary)` and
  `Builder#register(Vocabulary, provider)` replace the per-vocabulary methods (`cityProvider`,
  `isCityRegistered`, `cityRegisteredKeys`, `Builder#registerCityProvider` and their siblings for
  the other 38 vocabularies), which are deleted. Resolution order, validation rules and error
  messages are unchanged, and `registerLocaleData` and `registerDataPack` remain.
- Format Java with Palantir Java Format and Kotlin sources and Gradle scripts with ktlint through
  Spotless, with a shared `.editorconfig`; `spotlessCheck` runs as part of `check`. The reformat
  spreads statements over more lines, so the `spring-boot-starter` JaCoCo line floor moves from
  0.96 to 0.95; the tested code is unchanged. The one-time reformat commit is listed in
  `.git-blame-ignore-revs`.
- The Jakarta Validation API is now a compile-only dependency of `krandom-core` (`requires static`
  in the module descriptor) instead of a transitive runtime dependency. Consumers that rely on
  constraint-aware generation add `jakarta.validation:jakarta.validation-api` themselves; without
  it, constraints are ignored and plain values are generated.
- Enforce per-module JaCoCo line and branch floors for the integration modules (`jackson`,
  `junit`, `spring-boot-starter`, `kotest-extensions`, `kotlin-dsl`) at the values measured when
  the floors were introduced, so their coverage cannot regress silently.
- Email output defaults to `EmailDomainPolicy.TEST_SAFE_RESERVED_DOMAINS`: `EmailGenerator` and the
  contact, person, and profile payloads built on it use only `example.com`, `example.net`, and
  `example.org`, and `CompanyEmailGenerator` uses the reserved `.test` top-level domain, so staging
  tests can no longer mail real people. `REALISTIC_UNCLASSIFIED` restores mailbox-provider domains;
  `generateFreeEmail`, `getFreeEmailProvider`, and `generateFreeEmailDomain` require it and fail
  closed otherwise. Default seeded email output changes.
- Update Jackson Databind to 2.22.3, which fixes two denial-of-service issues (CVE-2026-91776,
  CVE-2026-91777), Kotlin to 2.4.20, Kotest to 6.2.5, SLF4J to 2.0.20, and the Gradle wrapper to
  9.8.0. Refresh test and build tooling: Logback 1.6.4, Hibernate Validator 9.1.4.Final, Spotless
  8.10.3, and PIT 1.30.0, which is now pinned in the version catalog.
- Update the Instancio benchmark competitor to 6.1.0 and align the consumer examples with Kotlin
  2.4.20, Maven Surefire 3.6.0, sbt 1.13.0, and Mill 1.1.10. Refresh the GitHub Action pins
  (setup-java 6.0.1, Codecov 7.1.1, Coursier 3.0.3) and label every pin with its exact release.
- Update the Gradle wrapper to 9.8.1, which fixes three high-severity daemon and repository
  vulnerabilities (GHSA-mvvg-497x-hmj8, GHSA-xwqc-3h47-hg64, GHSA-j5m7-59rp-24f5), Kotlin to
  2.4.21, Logback to 1.6.5, Spotless to 8.10.4, and the CycloneDX plugin to 3.5.1; align the
  consumer examples with Kotlin 2.4.21 and refresh the GitHub Action pins (wrapper-validation
  6.4.0, setup-graalvm 1.6.7, Coursier 3.0.4).
- Seeded `@FakeRange` doubles and floats with `min >= max` fail with the JDK's
  `bound must be greater than origin` message, like unseeded ones.

### Removed

- Raw `long seed` constructors and the matching `Generators` factories (for example
  `IntGenerator(int, int, long)`, `DoubleGenerator(double, double, long)`,
  `Generators.ofInt(int, int, long)`, `ofFloat(float, float, long)`, `ofRegex(String, long)`,
  `ofCalendar(long)`, `ofNationalId(Locale, long)`) and `TextGenerators#template(String, long)`.
  Pass `GeneratorConfig.builder().seed(seed).build()` to the `GeneratorConfig` form instead; it
  produces the same values. Schema regex columns now build such a configuration per value, which
  costs about 0.3 µs more per value.
- `Generators.ofURL`, `ofURI` and `ofTimeZone` and `DateTimeGenerators#timeZone`, which differed
  from the text factories `ofUrl`, `ofUri`, `ofTimezone` only by letter case while returning a
  different type. Use `ofUrlObject`, `ofUriObject`, `ofTimeZoneObject` and `timeZoneObject`.
- The protected `AbstractBoundedGenerator(min, max, Long)` and `(GeneratorConfig, min, max)`
  constructors: subclasses use `(min, max)` or `(min, max, GeneratorConfig)`.
  `NormalDistributionGenerator(GeneratorConfig, double, double)` is now
  `NormalDistributionGenerator(double, double, GeneratorConfig)`.
- The single-argument Spring `KrandomAutoConfiguration.providerHub(GeneratorConfig)` and the
  six-argument `FullNameGenerator.NameOptions` constructor.
- Replay of recipes without the safety-policy and email-domain settings, which only releases before
  those policies wrote: `GenerationRecipe#toGeneratorConfig()` now fails with
  `Recipe requires setting: <key>`. Field overrides are looked up by fully-qualified owner name
  only.

- Compatibility obligations toward earlier releases. Only the current release is maintained, and a
  release no longer promises API, behavior, or generated-output compatibility with earlier ones
  (see `VERSIONING.md`).
- The japicmp public API gate (`checkApiContract`, `checkApiCompatibility`, `checkApiEvolution`,
  `config/api-compatibility-excludes.txt`, `config/api-evolution-allowlist.txt`, and the
  `apiBaselineVersion` fact), the `generatePublicApiInventory` report, the check that ran a 2.2.0
  consumer and extension on the candidate (`verifyV2ConsumerCompatibility`), and the CI job that
  tested the Kotest adapters against the previous Kotest minor line (`-PkotestVersion`).
- The 1.x-to-2.0.0 migration guides and the deferred major-version plan. The release-facts check
  now finds the previous release in the changelog.

### Fixed

- `EnumSet` and `EnumMap` members are generated as real `EnumSet` and `EnumMap` instances. Before,
  an `EnumMap` field was silently left `null` in every mode without notifying the failure listener,
  and an `EnumSet` record component failed with a raw `IllegalArgumentException`.
- A concrete collection subtype without a usable no-argument constructor is reported through the
  failure policy: strict mode throws a contextual `CONSTRUCTION` failure, lenient mode returns
  `null` and notifies the listener. Previously the member was silently `null` in both modes.
- A value that a record's canonical constructor or a class's single declared constructor cannot
  accept (for example a by-name override of the wrong type) is a contextual `CONSTRUCTION` failure
  instead of a raw `IllegalArgumentException`.
- Constructor parameters of a class built through its single declared constructor are resolved by
  parameter name when the class was compiled with `-parameters`: by-name overrides, exclusions
  (`objectExcludeField` by parameter name, whether or not a field backs it, or `@Exclude` on the
  backing field), and semantic providers such as `firstName` or `email` now apply, and
  `ObjectFieldStreamPolicy.INDEPENDENT` keys the parameter stream by name. Without compiled
  parameter names the positional `constructorArg<index>` keys remain and also become the
  `INDEPENDENT` stream identity, which was previously the bare index. Seeded output for
  constructor-built classes therefore changes in both cases. `GeneratorConfig` gains
  `isObjectFieldNameExcluded(String)`.
- A JUnit configuration created for `@BeforeAll` or constructor injection no longer leaks into test
  methods, so a method-level `@KrandomSeed` applies and the reported seed reproduces the failure.
- Kotlin DSL rules on fields inherited from a superclass now apply instead of being ignored.
- Kotlin DSL and other construction-adapter exclusions apply to immutable Kotlin data classes: an
  excluded optional parameter keeps its default, a nullable one receives `null`, and a required one
  fails clearly.
- `@FakeRange` on `int`, `short`, and `byte` fields no longer overflows with the default `max`: a
  range reaching past the field type's largest value includes it (`@FakeRange(min = 127)` on a
  `byte` yields `127`), and ranges with no values for the field type fail clearly instead of
  wrapping.
- The semantic coherence pass no longer fails objects with `java.sql.Date`/`java.sql.Time`
  timestamps, `String` birth dates, or other age and timestamp types it cannot represent; `byte`,
  `float`, and `double` ages follow the birth date, and `RELAXED` mode keeps `@FakeRange` values,
  whose age then decides the birth date for every supported age type. A `byte` or `short` field
  keeps its generated value instead of wrapping an age it cannot hold.
- A `Set` or `Map` that cannot reach its declared `@Size` minimum fails (or honours ignored errors)
  instead of silently returning fewer elements.
- Sorted containers of records and other classes no longer fail as unsupported; element types that
  would only sort by identity hash code fail with context instead of varying between runs. A sorted
  set counts its elements by its own ordering, so elements that order as equal are replaced before
  the `@Size` minimum is checked instead of silently shrinking the set.
- Seeded JSON Schema generation replays: integer, number, boolean, pattern, and untyped values use
  the per-record field stream. `exclusiveMinimum` excludes non-zero bounds, `maxLength` alone no
  longer conflicts with the configured minimum length, and empty ranges fail while parsing.
- Seeded JSON Schema `format` (email, uri, uuid, date, date-time, time, ipv4, ipv6, hostname) and
  length-bounded string values also come from the per-record field stream. Two columns with the
  same format or bounds previously repeated each other in every record; their seeded values
  change. Unseeded, secure, and caller-owned random sources keep one format generator per column.
- XML export stays well-formed for any text (characters XML 1.0 cannot represent become U+FFFD),
  SQL export quotes a column name containing dots as one identifier, and TOML export quotes keys
  outside the ASCII bare-key alphabet.
- Make documentation-fact guards use portable `grep`; they previously passed silently on CI
  runners without `rg`. Script search errors and a missing `git` now fail instead of passing, and
  SIGPIPE-prone pipelines were removed.
- Keep the Gradle distribution checksum when `upgrade_latest_gradle.sh` updates the wrapper.
- Email local parts are always lowercase ASCII, matching the documented `yuki.tanaka@…` examples:
  diacritics are removed (German umlauts expand to `ae`/`oe`/`ue`), Cyrillic and Greek names are
  transliterated, and names in other scripts use romanized names for the locale's language instead
  of native-script local parts such as `翔.佐藤@…`. Seeded email output changes.
- `CountryGenerator.generateCallingCode`, `generateContinent`, and `generateTimezone` indexed
  `Map.ofEntries` views whose iteration order is randomized per JVM, so seeded output differed on
  every run; they now select from declaration order. Their seeded values change.
- Make `IbanGenerator` and `BbanGenerator` follow the ISO 13616 IBAN registry. Both resolve one
  country and build its registry BBAN (length, letter and digit positions, and national check
  digits for BE, CZ, ES, FI, FR, HR, HU, IT, NO, PL, PT, and SK); IBANs carry ISO 7064 MOD 97-10
  check digits as ASCII digits under any JVM default locale. The locale's country is used when it
  has an IBAN format (AT, BE, BG, BR, CH, CZ, DE, DK, ES, FI, FR, GB, GR, HR, HU, IE, IL, IT, NL,
  NO, PL, PT, RO, RU, SA, SE, SK, TR, UA); every other locale, including the default `en_US`, now
  produces German (`DE`) values instead of invalid `US`, `JP`, `CN`, or `AU` IBANs. Seeded IBAN and
  BBAN output changes; `BankCountryGenerator` output is unchanged.
- Make `CreditCardGenerator.isValidLuhn` return `false` for input without digits or with any
  character other than ASCII digits and spaces; it previously stripped every non-digit, so inputs
  such as `"abc"` or dash-separated numbers passed.
- Name the exact opt-in in the disabled banking-generation error:
  `GeneratorConfig.builder().bankingSafetyPolicy(BankingSafetyPolicy.REALISTIC_UNCLASSIFIED).build()`.
- Resolve `PhoneNumberGenerator` and `PostalCodeGenerator` formats from the locale's country
  instead of exact locale keys, so supported regional locales such as `de_AT` or `fr_CA` no longer
  silently produce US numbers and ZIP codes. A locale whose country has no built-in format, or
  that has no country, uses the first catalog country of its language (`en` and `en_SG` → US,
  `de_LI` → DE, `pt` → BR, `ca` → ES, `no` → NO); an unknown language uses `en_US`.
  `generateCountryCallingCode()` and `generateMsisdn()` use the resolved country instead of `+1`.
- Add built-in national phone formats (landline and mobile, formatted and unformatted), calling
  codes, and postal-code formats for every `SupportedLocale` country: DK, FI, HU, RO, SK, UA, BG,
  HR, GR, TH, VN, ID, MY, IL, CA, NZ, IE, ZA, BE, CH, AT, MX, AR, PT, and TW; `ca_ES` and `en_IN`
  now use the Spanish and Indian formats. Seeded phone and postal output changes for these
  previously US-fallback locales and for other locales whose country or language now resolves
  differently (for example `en_DE`); seeded output of the existing formats is unchanged.
- Canadian locales (`en_CA`, `fr_CA`) use Canadian area codes and, under the default
  `PhoneNumberSafetyPolicy.TEST_SAFE_WHERE_AVAILABLE`, NANPA's fictional `555-0100` through
  `555-0199` range like US locales, so they no longer produce potentially real numbers.
- National IDs documented as checksum-valid now validate against their published rules and
  samples: Chinese resident IDs use the GB 11643-1999 check character (`11010519491231002X`
  validates), Australian TFNs keep only weighted sums divisible by 11 (`123 456 782` validates),
  German Steuer-IDs follow the digit-repetition rule and map check digit 10 to 0, and Italian
  codici fiscali never encode an impossible day such as 30 February. Seeded output changes for
  `zh_CN`, `en_AU`, and `de_DE` IDs and for the Italian codes that encoded an impossible date.
- Danish CPR fixtures (`da_DK`) now carry a century code that matches their 1950-1999 birth date
  and never the unassigned sequence `0000`; Finnish identity codes (`fi_FI`) always use the 1900s
  century sign `-`; Slovak birth numbers (`sk_SK`) no longer put 1950-1953 birth dates in the
  ten-digit form, where they denote births in 2050-2053. Seeded output changes only for the values
  that violated these rules.
- `DoubleGenerator.withPrecision` and `FloatGenerator.withPrecision` keep the generator's seed
  (the constructor seed or the latest `reseed`) instead of dropping it, so identically seeded
  precision generators replay; rounded values stay inside `[min, max)` (for example
  `new DoubleGenerator(0, 1).withPrecision(0)` can no longer emit `1`), and ranges without a value
  at the requested precision are rejected with `IllegalArgumentException`.
- Range sampling no longer overflows: `DurationGenerator.betweenSeconds(0, Long.MAX_VALUE)`,
  `DateGenerator.generateYear` over ranges wider than `Integer.MAX_VALUE` (reversed bounds now fail
  with a clear message), `AgeGenerator(0, Integer.MAX_VALUE)`, and `UtilDateGenerator` /
  `SqlTimestampGenerator` ranges wider than `Long.MAX_VALUE` milliseconds. Ranges that fit keep
  their seeded values.
- `GeneratorConfig.Builder#collectionSize` and `#stringLength` reject `Integer.MAX_VALUE` as the
  maximum while building the configuration; drawing a size up to it overflowed during generation.
- `SimpleProfileGenerator` and `ProfileGenerator` birthdays follow the configured clock (including
  `snapshotClock()`) and random source instead of the system clock, so seeded profiles with a fixed
  clock replay on any day. Output with the default clock is unchanged.
- National IDs never borrow another country's identifier: `NationalIdGenerator` resolves the
  locale's country and fails fast with `UnsupportedOperationException` when that country has no
  provider, so `en_CA` no longer yields a US SSN, `pt_PT` a Brazilian CPF, `zh_TW` a mainland
  Chinese ID, or `de_AT` a German tax ID. Another language of a supported country uses that
  country's provider (`en_IN` → Aadhaar); a language-only locale uses its first catalog country.
- The default object unique-field names are built in declaration order instead of from a `Set.of`
  whose iteration order is randomized per JVM, so the default recipe serializes
  `object.unique-fields` identically on every run.
- `DataRegistryContext` lookups resolve scoped exact, then global exact, then scoped language, then
  global language matches. A scoped regional bundle such as `en_IN` no longer replaces another
  region's built-in data, for example `en_US` names under the default locale.
- National IDs, postal codes, tax identifiers, and formatted dates and times use ASCII digits under
  any JVM default locale; under a locale such as `ar-SA` they previously contained Arabic-Indic
  digits. Output under ASCII-digit default locales is unchanged.
- `Generators.ofCompanyName(Locale)` and the other vocabulary generators now honour the configured
  locale instead of always returning English. Seeded English output is unchanged; seeded output for
  non-English locales changes (also in composites that use these generators).
- `TextGenerator` Japanese and Chinese vocabularies were English words; they are now real Japanese
  and Chinese words (seeded `ja`/`zh` text output changes).
- Commerce product descriptions for `de`, `fr`, `es` and `it` used the English sentence template
  ("A … in …."); they are now localized (seeded description output changes for those languages;
  product names are unchanged).
- `FullNameGenerator` nationality token `"uk"`/`"UK"` now selects British names; it previously
  resolved to Ukrainian. Ukrainian names remain available through `uk_UA`, `uk-UA`, `UA` and `UK_UA`.
- `CoinGenerator`, `DiceGenerator`, `LuhnGenerator` and `FibonacciGenerator` ignored configuration
  and could not be seeded; they now draw from the configuration's random source.

### Documentation

- Link the Javadoc API reference on javadoc.io from the README and the docs site, and align
  `SECURITY.md` with the single-maintained-release policy in `VERSIONING.md`.
- State that `krandom-jackson` targets Jackson 2.x while Spring Boot 4 defaults to Jackson 3, and
  document replay commands, clock semantics, class-scoped JUnit configurations, and `@KrandomTest`
  bean inclusion.
- The README no longer claims seeded output is stable across JDK builds: zone IDs follow the JDK's
  tzdata, and ISO country lists and locale display data also follow the running JDK.
- State in the Javadoc which parts of the Bulgarian, Croatian, Greek, Hungarian, Indonesian,
  Israeli, Romanian, Thai, Ukrainian, and Vietnamese national-ID fixtures are random, including
  that no official check digit is computed, and correct the French NIR example key. Their output is
  unchanged. The migration guides no longer advertise checksum national IDs for every locale.
- Document the NANP fictional phone range for Canadian locales, the email-domain policy, the
  country-specific national-ID resolution, and the composite child-stream policy in the guides.
- Describe `krandom-junit` as a JUnit Jupiter extension: it builds on JUnit Jupiter 6, not JUnit 5.
- Data provider and registry Javadoc shows configuration-scoped registration through a
  `DataRegistryContext` instead of the global registry API that 2.0.0 removed.

## [2.5.0] - 2026-09-24

### Fixed

- Make UUIDv7 generation honor the configured clock so fixed-clock recipes reproduce timestamps.
- Make machine identifiers, schema field normalization, names, and CSS color output independent of
  the JVM default locale; restore semantic `id` fields to UUID generation.

## [2.4.0] - 2026-09-05

### Changed

- Isolate object-field stream planning behind a focused internal policy while preserving the
  public API, legacy seeded output, and explicit independent-stream behavior.
- Expand native-image qualification to cover locale-backed resources, record construction, and a
  configuration-scoped extension provider.
- Add the stream planner to critical-path mutation testing.

### Security

- Enforce reviewed SHA-256 verification metadata for resolved Gradle dependencies. Existing exact
  version, wrapper checksum, and immutable GitHub Action requirements remain in place.

### Documentation

- Clarify the supported Java `ObjectFaker` and Kotlin DSL capability boundaries and record the
  compatible 2.x fixture contract.

## [2.3.0] - 2026-09-04

### Added

- Explicit `GeneratorConfig.snapshotClock()` for sharing a captured instant between generation
  and replay diagnostics; existing configurations retain their live clocks.
- Opt-in `ObjectFieldStreamPolicy.INDEPENDENT` for seed-owned object generation, preserving
  unrelated structural field streams when rules, exclusions, or modules are installed. The
  default `LEGACY` policy and existing v1 recipe interpretation remain unchanged. Recipes record
  explicit policies; custom callbacks still do not claim portable replay.
- Kotlin DSL stream-policy configuration and JUnit's `krandom.junit.snapshot-clock=true` option.

### Fixed

- Failed profile application restores library-owned faker configuration, including nested profiles,
  rules, validation settings, and cached generator references. Callback side effects and consumed
  randomness are outside the rollback guarantee.

### Documentation

- Replace superseded research and completed plans with a maintained documentation map and product
  roadmap; defer v3 in favor of compatible 2.x delivery.
- Validate repository-local Markdown links and version-policy facts, and document snapshot and
  stream-policy usage across Java, Kotlin, JUnit, and Kotest.

## [2.2.0] - 2026-09-04

### Changed

- Update SLF4J to 2.0.19 and refresh build tooling: Spotless 8.10.1, japicmp 0.26.2,
  and NMCP 1.6.2.
- Update benchmark dependencies to Easy Random 6.0.1, Instancio 6.0.0, and SnakeYAML 2.7.
- Align consumer examples with Kotlin 2.4.10, JUnit 6.1.3, and Spring Boot 4.1.1.
- Refresh immutable GitHub Action pins for Java setup, GraalVM, Pages, and release publication.
- Verify the unchanged public API against the released 2.1.0 baseline without historical exclusions.

### Documentation

- Correct dependency-reproducibility guidance to distinguish version pinning from artifact
  checksum verification, which is not yet enabled for resolved dependencies.
- Refresh public installation coordinates and repository-local examples for 2.2.0.

## [2.1.0] - 2026-08-26

### Added

- Explicit, configuration-scoped `KRandomModule` extensions. A metadata-complete
  `ProviderDescriptor` can now contribute provider aliases, schema projections, safety claims,
  and semantic object-field generation without classpath scanning or global registration;
  conflicts fail during configuration construction.
- Rich contextual generation metadata: `GenerationContext` now exposes the full root-to-field
  path, reflected declared type and declaration, and active `GeneratorConfig`, while retaining
  its existing constructor and field/owner/depth accessors.
- Type-safe Java object rules through `PropertySelector` and composable `PropertyPath`, including
  getter and record-accessor references for root and nested `ObjectFaker` rules.
- Immutable, composable `ObjectModel` fixture configurations and opt-in `ObjectFaker.strict()`
  validation. Existing dependent rules now provide type-safe correlated assignments.
- Critical object-engine mutation testing with measured 85% mutation and 98% mutated-class
  coverage thresholds, plus Gradle and Maven consumer coverage for the new public fixture API.
- `HttpFixtureGenerator`, coherent static HTTP request/response fixture records, and a namespaced
  `network().httpFixture()` entry point. They generate compatible response-body shapes without
  making network calls.
- `PasswordPolicy`, bounded `SequenceGenerator`, and `FinitePoolGenerator` for common test-fixture
  constraints: required password character sets, deterministic nullable sequences, and
  resettable without-replacement pools.
- `SchemaProjection`, which projects existing objects into JSONL, JSON, CSV, XML, SQL, YAML, or
  TOML without materializing a batch. `Schema` now also writes JSON arrays, YAML, and TOML.
- Verified `LocalDataPack` loading and configuration-scoped University fixtures. Packs are local
  directories with a versioned manifest, required provenance/license declarations, bounded
  SHA-256-verified CSV data, and no network-loading path.
- Experimental GraalVM native-image reachability metadata, a conditional local smoke verifier,
  and guidance for registering application-owned reflective types.

## [2.0.0] - 2026-07-17

### Added

- Versioned, human-readable `GenerationRecipe` replay metadata for portable seeded configurations.
- `checkAllWithRecipe` and `krandomKotestRecipe`: Kotest property failures can now carry the
  portable kRandom configuration recipe alongside Kotest's seed report, and the adapter suite is
  verified against the current and previous Kotest minor via `-PkotestVersion`.
- `krandomConfig { }`, an idiomatic Kotlin builder for standalone `GeneratorConfig` values with
  textual seeds and explicit construction policies; the DSL's one intentional default difference
  (`objectOverrideDefaultInitialization`) is now documented and covered by a Java/Kotlin
  equivalence test.
- Spring starter properties for replay and safety: `krandom.recipe` (serialized replay recipe),
  `krandom.clock`/`krandom.clock-zone` (fixed generation clock), the eight `*-safety-policy`
  properties, and `krandom.object-construction-policy`. Invalid combinations fail at context
  startup with actionable messages.
- `@KrandomTest` is now a self-contained Spring test slice: it bootstraps the TestContext
  framework itself, disables full application auto-configuration, and starts only the documented
  kRandom beans; `krandom.*` properties bind as in the full context.
- Type-safe Kotlin DSL rules: `rule(Type::property)` and `exclude(Type::property)` accept
  `KProperty1` references, duplicate field/type rules fail at registration, and unknown field
  rules fail before generation with the known field names.
- `krandomIntArb`, `krandomLongArb`, `krandomDoubleArb`, and `krandomPickArb`, shrinking-aware
  Kotest adapters for bounded primitives and selections with in-range edge cases and
  range-bounded shrink candidates.
- Kotest replay-safe factory and object `Arb` adapters now derive a fresh kRandom configuration
  from each host random-source draw.
- `PaymentCardSafetyPolicy.STRIPE_SANDBOX`, an explicit Stripe-only sandbox mode that maps each
  card type supported by kRandom to Stripe's published interactive test-card number. It requires
  Stripe sandbox/test API keys and is not portable to another processor; server-side Stripe tests
  should use named `PaymentMethod` values instead of raw card numbers.
- `PaymentCardSafetyPolicy`, an enforceable configuration contract for generated card numbers.
- `PhoneNumberSafetyPolicy`, an enforceable contract for locale-style phone-number fixtures.
- `NationalIdSafetyPolicy`, an enforceable fail-closed configuration contract for national-ID
  generation.
- `BankingSafetyPolicy`, an enforceable fail-closed configuration contract for banking identifiers
  and account values.
- `IdentityDocumentSafetyPolicy`, an enforceable fail-closed configuration contract for generic
  passport and driving-license identifiers.
- `BusinessTaxIdentifierSafetyPolicy`, an enforceable fail-closed configuration contract for CNPJ
  and EIN values.
- `CnpjGenerator.withAlphanumericFormat()`, an explicit unclassified compatibility mode for the
  Brazilian 14-character alphanumeric CNPJ shape and its official check-digit algorithm.
- `CryptoAddressSafetyPolicy`, an enforceable fail-closed configuration contract for generated
  cryptocurrency destination-address shapes.
- `SecuritiesIdentifierSafetyPolicy`, an enforceable fail-closed configuration contract for ISIN
  and CUSIP values.
- `ProviderSafetyMetadata`, `ProviderValidity`, and `ProviderTestSafety`, describing conservative
  format, checksum, semantic-plausibility, and test-safety claims from the provider catalog.
- `ProviderSafetyPolicy` and an `x-krandom-safety` JSON Schema extension that carries the selected
  payment-card or phone-number safety policy for classified schema references.
- `ProviderCatalog` definitions now centralize built-in provider keys, aliases, factories,
  schema extractors/metadata, and object-field semantic mappings for `ProviderHub` and
  `FieldLookup`.
- Structured, value-sanitized generation failure context shared by object and schema exceptions, with stable category, operation, path, type, depth, and record-index fields.
- Immutable Kotlin primary-constructor generation in `krandom-kotlin-dsl`, including nullable/non-null parameters, defaults, nested generics, field/type rules, and Jakarta Validation. Java-only core remains free of Kotlin runtime dependencies and reports an actionable integration requirement instead of allocating an invalid Kotlin value.
- An explicit `io.github.frikit.krandom` core module descriptor plus open/closed Java 21 named-module consumers. Missing reflective access now reports the exact qualified `opens` directive.
- Optional `GenerationFailureListener` diagnostics configured through `GeneratorConfig`, exposing only structured context, cause class name, and an optional replay identity—never generated values or throwables.
- `krandom-bom`, a Maven/Gradle platform that keeps all published kRandom modules on one version. Consumer examples now import the BOM and omit individual kRandom module versions.
- Japicmp compatibility and evolution gates check every published jar module against the configured latest-GA baseline (`1.5.0`) in local pre-commit checks, CI, and releases. Binary/source breaks fail independently, while additions or other public changes require exact reviewed classification.
- A generated HTML/XML public API inventory for every published jar module, plus a checked-in
  2.0.0 disposition document covering facade aliases, registries, object generation, and
  integrations.
- Machine-readable release/module/locale/constraint/schema facts with a documentation gate that rejects stale versions, support counts, resource paths, and default-random claims.
- Immutable GitHub Action revisions plus checksum verification for the Gradle wrapper distribution and downloaded Mill launcher, enforced locally and in release CI.
- Strict Gradle dependency verification with reviewed SHA-256 metadata, centralized repositories, and rejection of dynamic or changing dependency selectors.
- Validated CycloneDX 1.6 JSON/XML SBOMs for every published module, attached automatically to GitHub releases.
- Pinned GitHub/Sigstore build-provenance attestations for the signed Maven Central bundle, jars, and SBOMs before publication.

### Changed
- Object generation caches Bean Validation constraint/accessor metadata, reuses sorted time-zone
  metadata, and resolves immutable built-in provider descriptors without rebuilding a mutable
  provider hub for every object.
- Competitor benchmarks separate structural object generation from semantic fixture construction;
  full publishable runs use three forks and report GC/allocation metrics against documented
  regression budgets. Dashboard parsing now handles parameterized and competitor-only JMH tables
  and fails if the generated methodology link is missing.
- Semantic coherence now deterministically derives a birth date from an explicitly overridden age,
  while preserving both values when the user explicitly overrides both fields.
- `PhoneNumberGenerator` now uses NANPA's fictional `555-0100` through `555-0199` range by
  default for US locale-style output. Other locales, custom templates, and MSISDN
  output remain explicitly unclassified; `REALISTIC_UNCLASSIFIED` restores the prior behavior and
  legacy recipes preserve that behavior when their phone-policy setting is absent.
- `CreditCardGenerator` and `CreditCardInfoGenerator` now produce issuer-shaped numbers that
  deliberately fail Luhn by default. `CHECKSUM_VALID` is an explicit validator-fixture opt-in and
  is not a real or processor-sandbox credential; the selected policy is replayed in portable
  generation recipes. Recipes recorded before the policy setting retain their historic
  checksum-valid replay behavior.
- `GeneratorConfig` and `Generators.ofNationalId(...)` now disable national-ID generation by
  default. `REALISTIC_UNCLASSIFIED` is an explicit compatibility opt-in; the locale and seeded
  `NationalIdGenerator` constructors are removed, and portable recipes persist the selected
  policy.
- `GeneratorConfig` and canonical banking facades now disable account numbers, ABA routing
  numbers, BBANs, IBANs, BICs, and structured bank payloads by default.
  `REALISTIC_UNCLASSIFIED` is an explicit compatibility opt-in; the affected direct constructors
  are removed. Payment payloads use an opaque `ACCT-TEST-####` reference for bank methods while
  banking output is disabled, and portable recipes persist the selected policy.
- `GeneratorConfig`, `Generators.ofPassport()`, and `Generators.ofDrivingLicense()` now disable
  generic document identifiers by default. `REALISTIC_UNCLASSIFIED` is an explicit compatibility
  opt-in; the direct `PassportGenerator` and `DrivingLicenseGenerator` constructors are removed,
  and portable recipes persist the selected policy.
- `GeneratorConfig`, `Generators.ofCnpj()`, and `Generators.ofEin()` now disable corporate tax-ID
  generation by default. `REALISTIC_UNCLASSIFIED` is an explicit compatibility opt-in; direct
  `CnpjGenerator` and `EinGenerator` constructors are removed, and portable recipes persist the
  selected policy.
- `GeneratorConfig` and canonical crypto-address facades now disable plausible destination-address
  output by default. `REALISTIC_UNCLASSIFIED` is an explicit compatibility opt-in; the direct
  `CryptoAddressGenerator` constructor is removed, and portable recipes persist the selected
  policy.
- `GeneratorConfig` and canonical ISIN/CUSIP facades now disable securities-identifier output by
  default. `REALISTIC_UNCLASSIFIED` is an explicit compatibility opt-in; direct `IsinGenerator`
  and `CusipGenerator` constructors are removed, and portable recipes persist the selected
  policy.
- Recursive object generation now distinguishes resolved generic signatures when detecting cycles
  and reusing completed objects, so nested Kotlin generic data classes retain their concrete type
  bindings instead of receiving a value from a different erased generic instantiation.
- Data-registry customisation is now configuration-scoped. Use the matching
  `DataRegistryContext.Builder` registration method to isolate custom locale data to one
  `GeneratorConfig`.
- The Java + Gradle consumer example now demonstrates a custom weather provider scoped to one
  `GeneratorConfig`, rather than process-wide static registry mutation.
- `WeatherGenerator` now resolves vocabulary through `DataRegistryContext`, so independent
  `GeneratorConfig` instances can safely use different validated weather providers for the same
  locale. The global weather registry remains a compatibility fallback.
- `MeasurementGenerator` now resolves unit vocabulary through `DataRegistryContext`, so a custom
  provider is isolated to its owning `GeneratorConfig`; the global registry remains a fallback.
- `FinancialTermGenerator` now resolves vocabulary through `DataRegistryContext`, so a custom
  provider is isolated to its owning `GeneratorConfig`; the global registry remains a fallback.
- `RestaurantTypeGenerator` now resolves vocabulary through `DataRegistryContext`, so a custom
  provider is isolated to its owning `GeneratorConfig`; the global registry remains a fallback.
- `HobbyGenerator` now resolves vocabulary through `DataRegistryContext`, so a custom provider is
  isolated to its owning `GeneratorConfig`; the global registry remains a fallback.
- `NationalityGenerator` now resolves vocabulary through `DataRegistryContext`, so a custom
  provider is isolated to its owning `GeneratorConfig`; the global registry remains a fallback.
- `PronounGenerator` now resolves vocabulary through `DataRegistryContext`, so a custom provider
  is isolated to its owning `GeneratorConfig`; context registration now rejects malformed
  `subject/object` values; the global registry remains a fallback.
- `BloodTypeGenerator` now resolves distributions through `DataRegistryContext`, so custom
  parallel positive-weight distributions are isolated to their owning `GeneratorConfig`; the global
  registry remains a fallback.
- `ChineseZodiacGenerator` now resolves its validated, ordered twelve-animal cycle through
  `DataRegistryContext`, so per-configuration vocabulary preserves year-to-animal mapping; the
  global registry remains a fallback.
- `ZodiacGenerator` now resolves its validated, ordered twelve-sign cycle through
  `DataRegistryContext`, so per-configuration vocabulary preserves date-to-sign mapping; the
  global registry remains a fallback.
- `krandom-core` is now a Java-only build and no longer publishes an unused `kotlin-stdlib` runtime dependency. Kotlin remains confined to the Kotlin DSL and Kotest integration modules.
- Maven Central publication now uses NMCP's explicit aggregation plugin and an exact seven-module graph, removing the convenience settings plugin's Gradle 10 deprecation.

### Changed (breaking)
- Bounded generators enforce strict bound semantics: `min >= max` now throws
  `IllegalArgumentException` with one consistent message across int, long, double, float, short,
  byte, prime, number, and atomic generators. Reversed bounds were previously swapped silently;
  2.0.0 never swaps a caller mistake. The protected `lo`/`hi` helpers on
  `AbstractBoundedGenerator` are removed.

### Removed
- The legacy 1.x `Generators` facade aliases `constant`, `pickFrom`, `pickset`,
  `pickSetFrom`, `shuffleOf`, and `uniqueValues`. Use the canonical `ofConstant`, `pick`,
  `pickSet`, `shuffle`, and `unique`; the migration table is in `docs/migration/v1.6-to-v2.md`.
- `Generator.reseed(long)` and `Generator.reseed(String)`, including the reflection-based
  fallback that mutated discovered `Random` fields. Reseeding is now exclusively the typed
  `Seedable` contract; see the "Typed reseeding" migration section.
- The mutable Kotest bridges `Generator.toArb()`, the no-argument `krandomArb { ... }` factory,
  and `krandomObjectArb`. Use `krandomArb(config) { ... }`, `krandomReplayObjectArb`, and the
  shrinking-aware bounded arbs, which derive fresh generators from Kotest's `RandomSource`.
- All process-wide registry mutation: the 23 legacy static `register(...)`/`append(...)`
  methods on the data registries and `LocaleDataBundle.registerGlobal()`. Register custom
  vocabulary on the consuming configuration via `DataRegistryContext.builder()` instead; built-in
  locale data still loads automatically and read-only lookups are unchanged.
- The 21 legacy no-argument and `Locale`-based constructors on the finance and identity
  generators (ABA routing, bank account/info, BBAN, BIC, IBAN, ISIN, CUSIP, EIN, CNPJ, crypto
  address, passport, driving license, national ID). Construct them with a `GeneratorConfig`
  carrying an explicit safety policy; the removed bridges' exact replacement expressions are in
  `docs/migration/v1.6-to-v2.md`.

### Fixed
- Recursive object generation now retains type-use annotations for optional values, array
  components, collection elements, and map keys/values. Nested Bean Validation constraints apply
  at the corresponding type node and failures retain the composed child path.
- `Generator.map(...)` and `Generator.filter(...)` now preserve deterministic reseeding when their source implements `Seedable`; non-seedable sources remain honest and do not claim that capability.
- Object generation no longer swallows custom-map insertion failures and returns a partial map: strict mode reports sanitized indexed context, while explicit lenient mode discards the whole map.
- Concrete list, set, and queue insertion failures no longer return partial or unexplained values: strict mode reports sanitized field context, while explicit lenient mode discards the whole collection.
- Concrete collection and map constructors that throw now produce sanitized construction context in strict mode and `null` in explicit lenient mode; types without a no-arg constructor retain the compatibility fallback.
- Primitive-array element assignment now fails with sanitized indexed context in strict mode; explicit lenient mode retains the documented JVM default element.
- Direct fields with unsupported interface, abstract, `Object`, or JDK types now fail with sanitized structured context by default; explicit lenient mode retains the type-default fallback.
- `@Randomizer` construction and execution failures now report the sanitized owner field path, operation, and generator type while preserving original or already-structured causes.
- Strict nested-object failures now compose one root-relative path across every object boundary while retaining the child failure category, operation, owner type, depth, and original cause.
- Top-level class and record constructor failures now expose sanitized `CONSTRUCTION` context and preserve the original target exception without copying its message.
- Semantic-coherence reflection failures now expose structured read/alignment context; explicit lenient mode returns `null` for failed reads or retains the prior value for failed writes and emits value-sanitized diagnostics.
- Object-generation assignment, container, nested, unsupported-type, and semantic-reflection failures now share one strict/lenient policy with standardized value-sanitized diagnostics and unchanged documented fallbacks.
- `ObjectFaker` nested rule, include, and ignore reflection failures now report sanitized root-relative target paths and preserve the original cause.
- Schema value-provider and metadata-export failures now have distinct operations and messages; metadata export no longer reports a synthetic record index.
- Schema semantic resolution now probes reference membership explicitly instead of swallowing arbitrary runtime failures during binding.
- Schema record conversion now reports sanitized nested component paths and types while preserving the unwrapped accessor failure.
- Object generation now preserves nested parameterized types through optionals, collections, maps, mutable fields, and Java record components.
- Raw containers and unbounded generic arguments now fail contextually by default instead of producing null-filled values; upper/lower wildcard bounds generate their effective type.
- Concrete generic superclass/interface bindings now propagate into inherited direct fields and container elements, including custom collection subtypes.
- Generic arrays now preserve bound and parameterized component types for mutable fields and records; unresolved components fail at the parent field instead of producing null-filled arrays.
- Parameterized nested classes and records now retain their type-variable bindings through direct fields, containers, and inherited declarations; raw or unbounded child types fail at the parent boundary.
- Record JSON Schema inference now retains nested collections, maps, optionals, generic records, generic arrays, wildcard bounds, enums, and fixed generic collection subtypes; Optional values serialize as their contained value or `null`.
- Bean Validation nullability and size rules now normalize `@Null`, `@NotNull`, `@NotEmpty`, `@NotBlank`, and `@Size`; required constraints override null/empty probabilities, while impossible intersections and unsupported targets fail contextually.
- Numeric, sign, assertion, and temporal Bean Validation constraints now intersect before generation; empty real or target-type domains, contradictory assertions/directions, malformed bounds, and unsupported scalar targets fail through the structured generation policy.
- String Bean Validation now composes email, repeatable pattern, numeric, size, and blankness rules through a bounded candidate search; incompatible combinations, malformed or unsupported regex syntax, and invalid targets fail contextually.
- The published Bean Validation guide now derives its 21-constraint matrix from repository facts, with deterministic Hibernate Validator coverage across fields, getters, records, and interface accessors.
- Object generation now defaults to `SAFE_CONSTRUCTORS`: it invokes no-argument or one unambiguous declared constructor with generated arguments; legacy Objenesis allocation requires explicit `UNSAFE_CONSTRUCTOR_BYPASS` configuration.
- Safe construction now rejects abstract, interface, array, primitive, enum, annotation, local, anonymous, and non-static inner roots before allocation; constructor parameters use the same type and Bean Validation resolver as fields.
- Type and contextual object overrides now act as validated root factories before reflection, including for interfaces; null, wrong-type, and throwing factories use structured strict/lenient custom-generator handling, and field-access preflight runs before constructors.
- Core locale and provider datasets now load through their owning classes so resources remain available when `krandom-core` runs as a strongly encapsulated named module.

## [1.5.0] - 2026-06-22

### Added
- **21 new DataFaker-parity generators**, each exposed on the `Generators` facade with 100% line/branch coverage. The locale-aware vocabulary generators ship per-locale resource files for all 35 built-in locales (`krandom/<concept>/<locale>.txt`) with English fallback.
  - **Person attributes:** `BloodTypeGenerator` (`ofBloodType`, locale-weighted ABO/Rh), `ZodiacGenerator` (`ofZodiac`, Western signs + `signFor(date)`; localized across 35 locales), `ChineseZodiacGenerator` (`ofChineseZodiac`, animals + `animalFor(year)`; 35 locales), `PronounGenerator` (`ofPronoun`, subject/object sets; 35 locales), `MbtiGenerator` (`ofMbti`, 16 types + `withNickname()`), `NationalityGenerator` (`ofNationality`, demonyms; 35 locales), `HobbyGenerator` (`ofHobby`; 35 locales).
  - **Localized vocabulary (35 locales):** `MeasurementGenerator` (`ofMeasurement`, units), `FinancialTermGenerator` (`ofFinancialTerm`), `RestaurantTypeGenerator` (`ofRestaurantType`, cuisine/type), `WeatherGenerator` (`ofWeather`, conditions).
  - **Identifiers & formats:** `VinGenerator` (`ofVin`, ISO-3779 check digit) + `VehicleGenerator` (`ofVehicle`, make/model/plate); `CnpjGenerator` (`ofCnpj`) and `ofCpf()` for Brazilian company/person tax ids (check-digit valid); `PassportGenerator` (`ofPassport`); `DrivingLicenseGenerator` (`ofDrivingLicense`).
  - **Technical / universal:** `NatoPhoneticGenerator` (`ofNatoPhonetic`, ICAO; `wordFor`/`spell`), `ProgrammingLanguageGenerator` (`ofProgrammingLanguage`), `AwsGenerator` (`ofAws`; region/instanceId/s3Bucket), `AzureGenerator` (`ofAzure`; region/resourceGroup), `ComputerGenerator` (`ofComputer`; OS/platform/deviceType).
- `LocaleTextResourceLoader` is now `public` so locale-aware generators in any package can reuse the shared classpath resource loader.
- A source-audited DataFaker provider mapping, competitive gap tracker, Instancio parity matrix,
  and migration guides from JavaFaker, DataFaker, Easy Random, and Instancio. The dated research
  snapshots remain available in the `v2.1.0` tag; the migration guides remain maintained.

### Changed
- Dependency bumps (verified no regression across tests, 100% coverage gate, consumer examples, and JMH benchmarks): `net.datafaker` 2.5.4→2.6.0, `com.diffplug.spotless` 8.6.0→8.7.0, `io.kotest` 6.1.11→6.2.1, `com.gradleup.nmcp.settings` 1.5.0→1.6.0; CI actions `actions/checkout` 6→7 and `softprops/action-gh-release` 3.0.0→3.0.1.
- Benchmark dashboard refreshed to a full-run JMH baseline; `run_benchmarks.sh` now redacts machine-local paths from captured result files.
- CHANGELOG pre-1.0.0 history condensed into a single summarized section.

## [1.4.0] - 2026-06-20

### Changed
- Gender labels, name suffixes, professions, and titles are now curated per-locale classpath resource files (`krandom/genders/<locale>.txt`, `krandom/suffixes/<locale>.txt`, `krandom/professions/<locale>.txt`, `krandom/titles/<locale>.txt`), loaded the same way as names — the last hardcoded language-string switches in the built-in user-data providers are gone, so adding or expanding a locale is a data edit, not a code change. **Relevant to consumers who load these classpath resources directly.** Gender and suffix data is migrated verbatim (generator behaviour unchanged); profession and title coverage is expanded (35 locales, 40 professions each, plus broader honorific/title sets). Profession ranked weights are now derived from list position, so the files can be any length.
- Build: Gradle wrapper upgraded 9.5.1 → 9.6.0.

### Fixed
- `CoordinatesGenerator` now renders coordinates as plain decimals via `BigDecimal.toPlainString()`: a near-zero value such as longitude `0.00044` previously serialized as `4.4E-4` and failed coordinate-format validation. Output never uses scientific notation and always uses `.` as the decimal separator regardless of locale.

## [1.3.0] - 2026-06-20

### Added
- Localized country datasets for all 35 supported locales (195 countries each). Previously only 10 locales had curated country files; the other 25 fell back to JDK-localized names.

### Changed
- Expanded every built-in locale dataset to richer, fully-unique coverage: cities ≥100 per locale, first (male/female) and last names ≥100 per locale, street names >25 per locale, plus comprehensive secondary-unit designators and paired long/short street types per locale.
- **Resource layout:** `names/` and `streets/` datasets are reorganized into per-type subfolders — `names/{first_male,first_female,last}/<locale>.txt` and `streets/{street_names,street_types_long,street_types_short,secondary_units}/<locale>.txt` (previously flat `<locale>_<type>.txt`). Relevant to consumers who load these classpath resources directly.
- Tightened locale coverage tests to lock in the new dataset floors (cities ≥100, names ≥100, countries ≥195, street names ≥26, street types ≥15) and to assert the long/short street-type lists stay paired (equal length) for every locale.

## [1.2.0] - 2026-06-19

### Added
- `GeneratorConfig.objectSubtype(declaredType, implementationType)` and `ObjectGeneratorConfig.subtype(...)`: explicit mapping of abstract/interface field types to concrete implementations during object generation (kRandom's answer to classpath scanning in other libraries).
- `Generators.ofDataFakerExpression(...)`: migration adapter resolving DataFaker-style `#{Provider.method}` expressions (case-insensitive, camelCase or snake_case) through the `ProviderHub`.
- **JUnit 5 extension module** (`krandom-junit`): `KrandomExtension` fixes the seed per test, injects seeded `GeneratorConfig`/`GeneratorConfig.Builder` parameters, and reports the seed on failure (JUnit report entry + `System.err` reproduction hint); `@KrandomSeed(value|text)` pins seeds at method or class level.

### Changed
- Core logging now uses the SLF4J facade instead of `java.util.logging`, so kRandom diagnostics flow through the consuming application's configured logging backend without a JUL-to-SLF4J bridge. The `ignoreErrors` swallowed-failure diagnostics are now logged at SLF4J `DEBUG` (previously `java.util.logging` FINE).

### Fixed
- `krandom-core` no longer ships a logging backend or configuration to consumers: removed the packaged `logback.xml` (which set the root logger to DEBUG and wrote `logs/krandom.log`) and dropped `logback-classic` from the runtime classpath, where it could hijack or conflict with the consuming application's logging.
- `LuhnGenerator` payload digits now span the full `[0, 9]` range; previously they were drawn from `[1, 9]`, so `0` never appeared in the first nine positions.
- `BigDecimalGenerator` now fails fast with `IllegalArgumentException` when a bound overflows `long` after scaling, instead of silently narrowing via `longValue()` and emitting out-of-range or invalid values.
- `RegexGenerator` rejects explicit `{n}` / `{n,m}` repetition counts above 10 000 at construction time, preventing unbounded output expansion (e.g. `a{2000000000}`) from exhausting memory.
- Object-graph generation sorts settable fields by name before allocating per-field seeds, so seeded output is reproducible across JDK builds, vendors, and instrumentation agents rather than depending on unspecified `Class.getDeclaredFields()` ordering.
- Corrected the inaccurate "thread-safe and can be shared across threads" Javadoc on 17 scalar generators (UUID, email, date/time, network, currency, …) to match the `Generator` contract: instances hold a single mutable PRNG and are not safe to share across threads. Added a "Concurrency and determinism" section to the README documenting the one-instance-per-thread pattern and the determinism/sharing trade-off.

## [1.1.0] - 2026-05-10

### Added
- Configurable random source on `GeneratorConfig`: `builder().random(Random)` for client-owned PRNG instances and `builder().secureRandom()` for `SecureRandom`-backed generation.
- Deterministic temporal generation: `GeneratorConfig` now carries a `Clock`, making date/time, card-expiration, and birthday generators reproducible under a fixed clock and seed.
- `Schema.writeTo(OutputStream | Writer, OutputFormat, count[, tableName])` streaming export overloads.

### Changed
- `ProviderHub` is now thread-safe: registries are backed by concurrent maps and single registrations are atomic under `ConflictPolicy.FAIL`; the thread-safety contract is documented in the class Javadoc.
- `objectIgnoreErrors=true` / `ignoreErrors=true` no longer swallows population failures fully silently: each ignored failure is logged (at `java.util.logging` FINE in 1.1.0; migrated to SLF4J `DEBUG` in 1.2.0) for diagnosability.
- Hardened object generation internals: `FieldGeneratorResolver`, `SemanticCoherenceAdjuster`, and `SemanticFieldRegistry` refactored for stricter semantic-alias resolution and edge-case coverage.
- Consumer API polish across `Generator` and `Schema`; module metadata refreshed for published artifacts.
- Kotlin updated to 2.4.0; Gradle wrapper downloads made resilient (longer timeout, retries).

### Removed
- jqwik integration module (`jqwik-extensions`); property-based testing support continues via `kotest-extensions`.

## [1.0.0] - 2026-05-08

First public release on Maven Central under `io.github.frikit`.

### Added
- **Spring Boot starter module** (`spring-boot-starter`) with auto-configuration, externalized `krandom.*` properties, and `KrandomObjectFakerFactory` bean.
- **Comparative JMH benchmarks** against DataFaker, JavaFaker, EasyRandom, and Instancio for scalar, object, and bulk generation.
- Monthly benchmark report convention under `docs/benchmarks/`.
- Schema export formats: CSV, JSONL, XML, SQL via `Schema.toCsv()`, `toJsonl()`, `toXml()`, `toSqlInserts()`.
- Schema export is now side-effect free (no internal state mutation).
- Locale facade overloads for schema providers.
- Nationality and identity edge-case handling in schema exports.
- `SECURITY.md` (vulnerability reporting via GitHub Security Advisories), `.github/ISSUE_TEMPLATE/` (bug + feature), and `.github/PULL_REQUEST_TEMPLATE.md`.
- Release runbook at `docs/release-runbook.md`.

### Changed
- Spring Boot starter uses `java-library` plugin with `api` scope for transitive dependency exposure.
- Spring Boot starter is now built against Spring Boot 4.x (`spring-boot-autoconfigure 4.0.6`); consumer applications must be on Spring Boot 4.x.
- Benchmark README restructured to lead with competitor comparison results.
- README updated with performance section, Spring Boot starter in modules table, and published artifact list.
- POM `<description>` is now distinct per published module (Central Portal validation friendliness).
- Maven Central publishing migrated from legacy OSSRH (`s01.oss.sonatype.org`) to the Central Portal (`central.sonatype.com`) via the `com.gradleup.nmcp.settings` plugin. The release workflow now invokes `./gradlew publishAggregationToCentralPortal`.

### Deferred to a future patch
- `FieldGeneratorResolver` falls back to `ArrayList`/`HashMap` for unknown concrete `List`/`Queue`/`Map` subtypes; the declared concrete type's no-arg constructor should be tried first.
- Registry input validation is inconsistent across user data registries (`FirstName`/`LastName`/`Gender`/`Title`/`Suffix` accept content that downstream generators reject; `Profession`/`StreetAddress` validate at registration). Standardize on register-time validation.
- Locale fallback policy differs by registry: `CountryDataRegistry` does language fallback; `CityDataRegistry`/`StateDataRegistry`/`StreetAddressDataRegistry` are exact-match only. Pick one policy and codify it.

The original review context remains available in Git history and the `v1.0.0` tag.

---

## [Pre-1.0.0] - 2026

Condensed summary of all pre-1.0.0 development (≈Feb–Apr 2026), originally tracked as ten dated `0.x` entries. See the git history for the full per-iteration breakdown.

### Added
- **Primitives & math**: int, long, float, double, boolean, char, string, `BigDecimal`/`BigInteger`, number ranges; Fibonacci, Luhn, hashing, UUID, dice, coin flip.
- **Identity & locale data**: first/middle/last/full names, age, birthday, gender, suffixes, professions, titles; country/city/state/postal code/phone/coordinates with locale-aware datasets and quality tiers; National ID generators for 10 countries (US SSN, GB NI, AU TFN, FR NIR, DE Steuer-ID, JP My Number, ES DNI, IT Codice Fiscale, BR CPF, CN Resident ID).
- **Internet & finance**: email, domain, URL, IPv4/IPv6, MAC address, username/password, social profile/handle, `AvatarUrlGenerator`; credit cards, currency, card expiration, IBAN/SWIFT-BIC, ISIN, bank account/name/type.
- **Text & codes**: word/syllable/sentence/paragraph/Lorem Ipsum, company name, street address, ISBN.
- **Selection combinators**: `PickGenerator`, `PickSetGenerator`, `ShuffleGenerator`, `UniqueGenerator`, `RepeatGenerator`, `WeightedGenerator` (Chance.js parity).
- **Object generation**: reflection-based population; `ObjectGenerator` (Optional, atomic, JSR-310, semantic mode, nullability, uniqueness, same-name overrides); `ObjectFaker` fluent API (`ruleFor()`, nested paths, include/ignore, named profiles); composite payloads (Address/Contact/Person) and structured payloads (job/bank/card/product/order/invoice/shipment/payment); `GeneratorConfig` as the single source of truth.
- **Schema & providers**: Schema DSL (`Field`/`Schema`/`FieldLookup`) with JSONL/CSV output and extensible tokens; `ProviderHub` (Mimesis-style alias lookup with conflict policy); custom locale bundles; `Randomizer`/`RandomizerArgument` annotations; `reseed()`.
- **Tooling & distribution**: multi-module Gradle (Java 21 toolchain), Spotless + MIT license headers, JaCoCo near-100% coverage gates (99.9% line/branch/instruction/method/class/complexity), `pre_commit_check.sh`, JMH benchmarks module; consumer examples (Java/Kotlin/Scala), GitHub Pages docs site, and a GitHub Actions release workflow.

### Changed
- Relicensed Apache 2.0 → MIT; moved to the `io.github.frikit.krandom` package and `io.github.frikit` group/artifact IDs.
- Unified random sourcing on `config.createRandom()` and object-generation defaults under `GeneratorConfig`; streamlined provider registration/validation; data providers seed from `SupportedLocale`.
- Promoted all built-in fallback locales to native datasets; simplified locale handling; migrated test stack Spek2 → Kotest; replaced log4j 1.x with Logback.

### Removed
- Kotlin and Scala API modules (implementation surface kept focused); deprecated APIs and deprecated Java locale usage.

[Unreleased]: https://github.com/frikit/krandom/compare/v2.9.0...HEAD
[2.9.0]: https://github.com/frikit/krandom/compare/v2.8.0...v2.9.0
[2.8.0]: https://github.com/frikit/krandom/compare/v2.7.0...v2.8.0
[2.7.0]: https://github.com/frikit/krandom/compare/v2.6.0...v2.7.0
[2.6.0]: https://github.com/frikit/krandom/compare/v2.5.0...v2.6.0
[2.5.0]: https://github.com/frikit/krandom/compare/v2.4.0...v2.5.0
[2.4.0]: https://github.com/frikit/krandom/compare/v2.3.0...v2.4.0
[2.3.0]: https://github.com/frikit/krandom/compare/v2.2.0...v2.3.0
[2.2.0]: https://github.com/frikit/krandom/compare/v2.1.0...v2.2.0
[2.1.0]: https://github.com/frikit/krandom/compare/v2.0.0...v2.1.0
[2.0.0]: https://github.com/frikit/krandom/compare/v1.5.0...v2.0.0
[1.5.0]: https://github.com/frikit/krandom/compare/v1.4.0...v1.5.0
[1.4.0]: https://github.com/frikit/krandom/compare/v1.3.0...v1.4.0
[1.3.0]: https://github.com/frikit/krandom/compare/v1.2.0...v1.3.0
[1.2.0]: https://github.com/frikit/krandom/compare/v1.1.0...v1.2.0
[1.1.0]: https://github.com/frikit/krandom/compare/v1.0.0...v1.1.0
[1.0.0]: https://github.com/frikit/krandom/releases/tag/v1.0.0
