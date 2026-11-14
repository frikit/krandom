# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

## [Unreleased]

### Added

- `GeneratorConfig.Builder.germanStreetLayout(boolean)` and `GeneratorConfig.isGermanStreetLayout()`, with
  `germanStreetLayout(Boolean)` in the Kotlin DSL's `config { }`, write the street addresses of the locales
  whose language is German (`de`, `de_DE`, `de_AT`, `de_CH` and any other `de_*`) as German does: the street
  name and its type as one word, then the house number. Seeded with 42, `de_DE` gives `"Schlossanger 7389"`
  rather than `"7389 Schloss Anger"`. It is off by default, so seeded output stays the same, and it becomes
  the default in 4.0. `StreetAddressGenerator.generate`, the street of `generateFullAddress()`, and
  `AddressInfo.street()` and `address()` follow it, and so do `Krandom.streetAddress()`, the
  `address.street_address` and `address.address_info` schema columns and the object engine's street fields.
  The same draws are made either way. A recipe records `setting.address.german-street-layout=true` while it
  is on, and a recipe without it replays with it off. Other locales that write the street first, such as
  `nl_NL`, keep the house number first: their street data is shaped differently.
- `Krandom.postalLocality()`, `Generators.ofPostalLocality()`, `ofPostalLocality(Locale)` and
  `ofPostalLocality(GeneratorConfig)`, `Generators.location().postalLocality()` and `postalLocality(Locale)`,
  the `PostalLocalityGenerator` and the `PostalLocality` record give a city, its region and region code, and
  one of its postal codes, all from one place of `Vocabulary.POSTAL_PLACE`, so the postal code serves the
  city. `Krandom.city()` and `postalCode()` draw separately, and their values need not agree.
  `Krandom.seeded("pins", Locale.US).postalLocality()` gives Pasadena, Texas, `TX`, `77505`. The session
  draws it from a stream of its own, so no other value changes. A locale without postal places, such
  as `de_AT` or `ja_JP`, throws `UnsupportedOperationException` unless places are registered for its country.

### Changed

- A seeded `Schema` takes each value of a common built-in `Field` reference from one generator kept for its
  column and thread, reseeded with the seed from the value's named stream, instead of building a generator
  for each value. These references are names, usernames, email addresses, phone numbers, street addresses,
  address records (`address.address_info`), cities, states, postal codes, countries, currencies, money,
  UUIDs, dates, timestamps, words, sentences and paragraphs.
  Every value's stream seed is now folded in steps. A value that still builds its generator copies its
  configuration instead of rebuilding it, unless the configuration has modules.
  This makes no output changes: every value is the one that a generator built for it gives, and the 3.0.1
  pins hold.
  - In `SchemaOutputBenchmark` (two runs each, in the order before, after, after, before), the four
    workloads went from 518, 423, 469 and 462 to 978, 696, 786 and 794 operations a second.
    Allocation went from 12.5, 12.9, 12.5 and 12.5 MB to 4.0, 4.5, 3.9 and 4.0 MB an operation.
  - Against `e6ba573d`, the last commit before 3.0.1 gave each value its own stream, the workloads are
    +6.7%, −6.6%, −8.6% and −0.6% by the median of their fork scores, with 8% to 11% more allocation. All
    of them are within the regression budgets, so the benchmark methodology no longer lists an accepted
    breach.
  - Each value still builds its generator for other built-in references, `registerProvider` references,
    module providers, and every reference of a configuration with modules or its own `DataRegistryContext`.

### Fixed

- `CharGenerator.pool`, the `StringGenerator` built from such a pool, including through the three
  `StringGenerator.pool` factories, and `PasswordPolicy.Builder.require` and `requireSymbols` now draw a
  supplementary character of the caller's pool, one outside the Basic Multilingual Plane, such as the emoji
  U+1F600 that UTF-16 writes as a surrogate pair, as one whole character. They drew each half of the pair as a
  character of its own, so a string or password drawn from the pool `"a😀"` held unpaired surrogates, which no
  Unicode encoding can hold, and `CharGenerator.pool(char...)` split a pair given as its two halves side by side
  the same way. A length, and the count of a requirement, is of characters as code points, so such a character
  counts once and takes two `char`s of the string, and a string or password that holds one is longer in
  `String.length()` than its length. `CharGenerator.generate()` returns a `Character`, which cannot hold such a
  character, so for a pool that holds one it now throws `IllegalStateException`, naming the character, rather
  than return half of it, and `StringGenerator` draws strings from that pool. Seeded output changes only for a
  pool that holds a supplementary character, whose characters are now fewer and whole: seeded with 42, a
  `StringGenerator` of length 4 from `"a😀b🎉"` gave `"\ude00ba\ude00"`, `"a🞉\ude00"` and
  `"🞉\ude00\ude00"`, and now gives `"baba"`, `"😀🎉😀b"` and `"ba🎉😀"`. A pool of characters from the Basic
  Multilingual Plane only, including one that holds an unpaired surrogate, keeps every draw, as tests pinned
  before the change show, and no built-in pool holds a supplementary character.
- `TextGenerator.generate(TextOptions)` now ends a text that fills `maxChars` and whose last character is a
  surrogate pair by replacing the whole pair with its closing dot, rather than only the pair's low half, which
  left the high half on its own, so that every text can be encoded. Such a text is now one `char` shorter than
  `maxChars`, which it holds at most. Only words that the caller supplies through `TextOptions` can end a text
  in a supplementary character, as no built-in vocabulary holds one, so seeded output changes only for a text
  of such words whose cut at `maxChars` ends in a whole pair: the word list `["a😀"]` with a `maxChars` of 3
  gave `"a\ud83d."` with any seed, and now gives `"a."`.
- `CompanyUrlGenerator.generateFromCompanyName(String)` now keeps the first 63 letters and digits of the
  company name as the host label, the most a label may hold, as `CompanyEmailGenerator` and the coherence pass
  do since 3.0.1 and 3.0.2. It kept them all, so a name of more than 63 ASCII letters and digits gave a URL
  whose host `IDN.toASCII` rejects. Output changes only for such a name, which a caller supplies, and the cut
  draws nothing, so no other value changes. The names that `CompanyNameGenerator` draws, which
  `generateFromCompanyName()` uses, are shorter: as 3.0.2 records, the longest label of 5,000,000 seeded names
  in the 50 supported locales has 51 characters.
- `Schema` and `SchemaProjection` now write DEL (U+007F), the C1 controls U+0080 to U+009F, U+2028, U+2029,
  U+FFFE and U+FFFF in YAML as escapes such as `\u0085`, in names and values, as YAML double-quoted strings
  allow, so that a YAML parser reads every document and every string back as written. They wrote them raw, as
  JSON may: SnakeYAML rejected a document with any of them in a name, and with any of them but U+0085, U+2028
  and U+2029 in a value, and read U+0085 in a value back as another character. JSON, JSON Lines, CSV, XML, SQL
  and TOML output is unchanged, as is YAML output of text without these characters. No built-in generator gives
  them from its own data, so seeded YAML output changes only for text that holds one, such as a value or a
  field name that the caller supplies: the value `"a\u0085b"` was written as `"a` U+0085 `b"` and is now
  written as `"a\u0085b"`.
- `Schema.toJsonSchema()` now writes each field name in `properties` and `required` as the data writers write
  it, with each unpaired surrogate, half of a UTF-16 surrogate pair without its other half, as U+FFFD, as every
  writer does since 3.0.2. It kept the name as given, so the JSON Schema of a field named `"a\ud800"` named a
  property `"a\ud800"` that no written record holds, as each holds `"a\uFFFD"`, and could not be encoded as
  UTF-8 itself. The `KrandomJacksonModule` serializer, which writes `toJsonSchema()`, writes the same names.
  Only a field name with an unpaired surrogate, which the caller supplies, is written differently; no
  generated value changes.
- `Schema`, its `SchemaParser.fromJsonSchema` and `fromOpenApi` forms, and `SchemaProjection` now reject two
  field names that differ only in unpaired surrogates, such as `"a\ud800"` and `"a\udc00"`, or `"a\uFFFD"` and
  `"a\ud800"`, with an `IllegalArgumentException` that names both, as every writer writes them as one name,
  `"a\uFFFD"`. Such a schema wrote each record with that key twice, which JSON parsers read as one key or
  reject, and a CSV or SQL header with the same column twice, and its JSON Schema would hold one property for
  both. This follows the rejection of a blank field name at the same point. A schema whose names stay distinct
  as written, including names that differ in a surrogate pair, is built and writes as before, so no seeded
  value changes.

## [3.1.0] - 2026-11-13

### Added

- `Schema.nextRecord()`, `Schema.nextRecords(int)`, and `SchemaProjection.projectRow(T)` return a
  schema's records and a projection's rows as `Map<String, @Nullable Object>`, so callers that read the
  JSpecify annotations, such as NullAway and Kotlin, see that a value is `null` where its provider or
  extractor returns `null`, as a parsed property of type `null` always does and one with OpenAPI's
  `nullable` does half of the time. They return the maps that the methods they replace return, and every
  record method of a schema draws from its one record sequence.

### Changed

- The generators and methods that pass `null` through now say so in their type parameters, so a nullable
  type argument such as `@Nullable Boolean` is legal for callers that read the JSpecify annotations, such
  as NullAway and Kotlin. `ConstantGenerator`, `ContextualGenerator`, `RepeatGenerator` and
  `UniqueGenerator`, the `Generators` factories `ofConstant`, `repeat`, `unique`, `sequence` and
  `threadLocal`, the three `ProviderHub.unique` methods, the contextual field and predicate overloads of
  `GeneratorConfig.Builder.objectOverride`, every overload of `ObjectFaker.ruleFor` and `ruleForContext`,
  `PropertySelector` and `PropertyPath` for the property's type, and the varargs `Krandom.pick` bound
  their type parameter by `@Nullable Object` rather than `Object`, and `PropertyPath.then` continues a path
  past a nullable property with an accessor of its non-null type. So
  `Generators.repeat(Generators.ofNullableBoolean(), 3)`, which NullAway and Kotlin rejected, compiles as a
  `RepeatGenerator<@Nullable Boolean>`, or `RepeatGenerator<Boolean?>` in Kotlin, and a rule can name a
  nullable property through a `PropertySelector<User, @Nullable String>`. Signatures in bytecode and
  runtime behavior are unchanged, as each of these already returned the `null` its source or argument
  gave. This brings the one source change for Kotlin that 3.1.0 makes, an exception to the rule in
  VERSIONING.md that a minor release keeps the public API compatible. Kotlin reads a star projection of
  `ConstantGenerator`, `ContextualGenerator`, `RepeatGenerator`, `UniqueGenerator` or `PropertySelector`
  through the new bound, so code that reads a value from one now reads it as `Any?`, and code that reads it
  as `Any`, such as `val value: Any = generator.generate(context)` for a `ContextualGenerator<*>`, no
  longer compiles. The exception is deliberate: the class bound is what lets nullable values compose, as
  `RepeatGenerator<Boolean?>` cannot be written without it. It fails only at compile time: compiled code
  calls the same methods and gets the same values. NullAway checks the new tests in
  `core/src/nullnessTest/java`, which compile each of these with a nullable type argument.
- `krandom-kotlin-dsl` and `krandom-kotest-extensions` are now compiled for JVM target 17, against the Java 17
  API, rather than for 21, and for Kotlin 2.2 rather than 2.4: with Kotlin language and API version 2.2, and
  with `kotlin-stdlib` 2.2.21 rather than 2.4.21 as their published dependency, as `krandom-kotlin-dsl` has for
  `kotlin-reflect`. So Kotlin code compiled for JVM target 17, as Android modules usually are, can call their
  inline functions: `krandom`, `krandomList`, `krandomGenerator`, `KrandomBuilder.ruleForType` and
  `krandomReplayObjectArb`; and a Kotlin 2.1.0 or later compiler can read them, such as the Kotlin 2.2.10 that
  the Android Gradle plugin 9.5.1 brings. Such code failed to compile with "Cannot inline bytecode built with
  JVM target 21 into bytecode that is being built with JVM target 17", and a Kotlin 2.2 compiler with "Module
  was compiled with an incompatible version of Kotlin. The binary version of its metadata is 2.4.0, expected
  version is 2.2.0", for these modules and for the `kotlin-stdlib` 2.4.21 that they brought. A Kotlin 2.0
  compiler still cannot read them. A consumer on a newer Kotlin keeps its own newer `kotlin-stdlib`, which
  Gradle selects over 2.2.21; it gets `kotlin-reflect` 2.2.21 unless it names a version of its own, and with
  Kotlin 2.4.21 the Kotlin DSL still reads its data classes' parameter names through it. The Gradle module
  metadata of both modules now declares `org.gradle.jvm.version` 17 rather than 21. `krandom-core`, which both
  depend on, runs on Java 17 too from 3.1.0 (see below), so both modules run on Java 17: an Android module
  can use them in its local unit tests, on Java 17 or later, and not in app code or on-device tests. The
  public API is unchanged. The `examples/kotlin-android` example, which CI runs, compiles its unit tests for
  JVM target 17 with the Kotlin that the Android Gradle plugin 9.5.1 brings, and the Android unit tests guide
  describes its setup.
- `krandom-core`, `krandom-jackson`, `krandom-junit` and `krandom-spring-boot-starter` now run on Java 17 or
  later, rather than 21: the build compiles them against the Java 17 API (`--release 17`), their Gradle module
  metadata declares Java 17 (`org.gradle.jvm.version`), so a Gradle build on Java 17 resolves them, and CI runs
  their tests on Java 17. Seeded output on Java 21 and later does not change: the code that used the API or
  language of Java 18 to 21, such as `Locale.of`, `Math.clamp`, `Math.ceilDiv`, `List.getFirst` and
  pattern-matching `switch`, now computes the same values with Java 17's, and every pinned seeded test passes
  unchanged on Java 21. Values drawn from the JDK's own data follow the JDK that runs them, on Java 17 as on
  any other: locale display names and date formats from its CLDR version, time zones from its TZDB, and its
  ISO country list. With the Kotlin modules' JVM 17 target above, every kRandom module runs on Java 17 in
  3.1.0; building kRandom still needs JDK 21 or later.

### Deprecated

- `Schema.generate()`, `Schema.generateBatch(int)`, `Schema.loop(int)`, and `SchemaProjection.project(T)`,
  whose `Map<String, Object>` claims values that are never `null`, although a value is `null` where its
  provider or extractor returns `null`. Use `nextRecord()`, `nextRecords(int)`, and
  `projectRow(T)`, which return the same maps typed `Map<String, @Nullable Object>`; the deprecated
  methods behave as before. A `Schema` stays a `Generator<Map<String, Object>>`, so the `generateList`,
  `stream`, `map`, and `filter` it inherits keep that type, and `schema::nextRecord` is a
  `Generator<Map<String, @Nullable Object>>` of the same records.

## [3.0.3] - 2026-11-12

### Changed

- `StreetAddressGenerator` now reads each locale's secondary units, such as `Apt` and `Suite`, from their
  resource once and shares them between that locale's generators, rather than opening and reading the
  resource each time a generator is built; a locale without such a resource shares its fallback units the
  same way. No generated value changes: the units are the same strings in the same order, as a test checks
  against a fresh read of every locale's resource, and every pinned seeded test passes unchanged. A portable
  seeded `Schema`, whose `Field` values each build their own generators, gains the most:
  `SchemaOutputBenchmark.largeSchemaBatch`, 250 records of 12 references, gave 240 operations a second and
  gives 478, and allocates 12.5 MB an operation rather than 20.7 MB. Before 3.0.1 gave each schema value its
  own stream, it gave 933 operations a second and allocated 3.7 MB, and the benchmark methodology still
  records the difference as an accepted breach of its regression budgets, with the raw runs.

### Fixed

- A resumed release (`resumeGithubRelease=true`) now attaches the signed Maven Central bundle, module
  jars, and SBOMs that the initial run attested and kept, after checking them against their attestations
  and against the files that Maven Central serves, rather than building and signing them again. The rebuilt
  files carried new signatures and SBOMs, so a GitHub release could hold files that differ from the
  deployment on Maven Central and from the ones the initial run attested.

## [3.0.2] - 2026-11-12

### Changed

- `GeneratorConfig.forChildStream(name)`, which a composite generator calls for each child it builds, now
  copies a seeded configuration that installs no module, with the child's numeric seed, rather than
  rebuilding it through a builder, which copied each of the configuration's collections twice. No
  generated value changes: the child equals the rebuilt configuration in every field, and every pinned
  seeded test passes unchanged. A configuration that installs a module is still rebuilt, so the module's
  `configure` still runs for each child. A portable seeded `Schema`, whose `Field` values each build their
  own generators, gains the most: `SchemaOutputBenchmark.largeSchemaBatch`, 250 records of 12 references,
  gave 197 operations a second and gives 264, and allocates 20.6 MB an operation rather than 30.8 MB.
  Before 3.0.1 gave each schema value its own stream, it gave 933 operations a second and allocated
  3.7 MB, and the benchmark methodology still records the difference as an accepted breach of its
  regression budgets, with the raw runs.

### Fixed

- The coherence pass now keeps the first 63 letters and digits of a company name as the label of the domain
  it derives from that name, the most a label may hold, as the data validity guide says every address a
  generator builds passes Hibernate Validator's `@Email`, and as `CompanyEmailGenerator` does since 3.0.1. It
  kept them all, so a `companyName` of more than 63 ASCII letters and digits, whether an override, a rule or
  a kept initializer set it or `@Size(min = 64)` had it generated, gave its `domain`, `companyEmail`,
  `companyUrl` and the other email and URL fields that take that domain a label that `IDN.toASCII` rejects.
  Seeded with 42, of 2,000 objects with an `@Size(min = 64, max = 100)` `companyName` beside a `companyEmail`
  and a `companyUrl`, 2,000 had an email that `@Email` rejects and a URL whose host `IDN.toASCII` rejects,
  and now none has. In `STRICT` mode an `@Email` field beside such a name kept its generated address, as the
  derived one broke `@Email`, and now takes the derived `hello@` address, as it does beside a shorter name:
  with seed 42, 2,000 of 2,000 such values changed. Company names that `CompanyNameGenerator` draws are
  unaffected, as the longest label of 5,000,000 seeded names in the 50 supported locales has 51 characters,
  so seeded output changes only for a company name of more than 63 ASCII letters and digits.
- `Schema` and `SchemaProjection` now write an unpaired surrogate, half of a UTF-16 surrogate pair without
  its other half, as U+FFFD in JSON, JSON Lines, CSV, YAML, SQL and TOML, in values and in keys, CSV headers,
  and table and column names, as their XML output already did, so that each document can be encoded as the
  UTF-8 that the schema guide says `writeTo(OutputStream, ...)` writes. They wrote it as it was, which no
  Unicode encoding can hold: a strict UTF-8 encoder rejected the document, and `writeTo(OutputStream, ...)`
  wrote `?` in its place, so the value `"a\ud800b"` read back as `a?b`. U+FFFD is written rather than an
  escape such as `\ud800`, which TOML cannot write and strict JSON parsers reject. A surrogate pair, every
  other character, and XML output are written as before. No built-in generator gives an unpaired surrogate
  from its own data, so seeded output changes only for text that holds one, such as a value or a column name
  that the caller supplies.

## [3.0.1] - 2026-11-11

### Fixed

- The coherence pass now leaves a field initializer that generation keeps as the class set it, as the
  object-generation guide says initializers are kept unless `objectOverrideDefaultInitialization(true)` is
  set. It overwrote one, so `fullName = "KEEP"` beside `firstName = "Ada"` and `lastName = "Lovelace"` became
  `Ada Lovelace`. Seeded with `42`, of 2,000 objects each, it replaced a kept `fullName` beside generated
  names in 2,000, replaced a kept `email` in 2,000, and moved a kept `updatedAt` of 2001-01-01 in 2,000, and
  now does so in none. A generated sibling follows the kept value instead, so `createdAt` moves back to that
  `updatedAt` and `birthDate` to a kept `age`, and the pass still fills a blank initializer and aligns a field
  that a constructor fills from a generated argument. Seeded output changes only for objects with an initializer that the pass derives or
  aligns.
- A Kotlin class built through its primary constructor now has its other mutable properties, declared in its
  body or inherited, filled through their fields after construction, as a Java class's fields are after its
  constructor, and as the Kotlin DSL guide says rules and generated values replace initializers. They kept
  their initializers whatever the rules: with `data class User(val id: Long) { var name = "old" }`,
  `rule(User::name) { "new" }` gave `"old"`, as did a `GeneratorConfig` field override, and a `lateinit`
  property stayed unset. Seeded with `42`, of 2,000 `User` values from the DSL, 2,000 had `name` `"old"` and
  none now has; the constructor's values are the same. A Kotlin `object` or companion object is still returned
  as it is, and a field in a package that is not open to kRandom, such as one that an exception class
  inherits from `Throwable`, keeps what the constructor set. Seeded output changes for such properties.
- A Kotlin primary-constructor argument now meets the constraints of its parameter and of its property's field
  and getter together, as the Kotlin DSL guide says `@param:` and `@field:` targets are both recognized. Kotlin
  compiles no parameter names unless `-java-parameters` is set, so the field a parameter fills could not be
  found: the adapter passed the parameter where it had a constraint, which dropped the field's, and a getter's
  constraints were always dropped. Seeded with `42`, of 2,000 values each, `@param:NotBlank
  @field:Size(min = 2, max = 2) val code` broke its size in 1,994 and `@get:Size(min = 2, max = 2) val code`
  in 2,000, and now neither does. While a construction adapter generates an argument of the constructed type,
  its parameter is read with the field its member name gives, as a parameter compiled with its name is, and
  only then, so the values of other types do not depend on what was generated before. The field's type may
  differ from the parameter's only by the wildcards that Kotlin's declaration-site variance writes, so the
  getter constraint of a `List<T>` property reaches its `List<? extends T>` parameter. Seeded output changes
  only for such arguments.
- A `GeneratorConfig` field predicate override, `objectOverride(Predicate<Field>, ...)` in either form, now
  replaces a Kotlin default argument and fills a required primary-constructor parameter when it matches the
  property's field, of the parameter's type, as `ObjectConstructionContext` says explicit field overrides stay
  stronger than defaults. `hasExplicitOverride` left both forms out, so the adapter kept `"default"` for
  `data class User(val name: String = "default")`, and the resolver matched a predicate only against a field
  it was given, not a parameter. Seeded with `42`, of 2,000 values each, a predicate on `name` reached neither
  the defaulted nor a required `name`, and now reaches both. Seeded output changes only where such an override
  matches a constructor property.
- An `ObjectFaker` rule on a nested path, a nested context rule and a dependent rule now keep a field whose name
  `objectUniqueField` requested unique, as the object-generation guide and `ObjectGenerator` say such a name
  stays unique however its value is produced. These rules run after generation and were not checked: seeded
  with `42`, `ruleFor("address.city", () -> "Berlin")` with `objectUniqueField("city")` gave 5 of 5 objects
  `Berlin`, as a dependent rule gave 5 of 5 a requested `email` of `ada@example.com`. Their values are now
  checked as a root rule's are: a repeat applies the rule again, a dependent rule to the same object, up to
  `objectUniquenessMaxAttempts` times, and then generation fails, or with `objectIgnoreErrors(true)` the field
  takes its default, which the generation report shows as a fallback. A rule may give an object the value it
  already holds, which repeats nothing, and the value it replaces is given back where generation gave it to
  that member, so a rule over a small domain does not use it up twice, while a value the class set is not.
  Each field still has its own values, and a name that is unique by default but not requested may still
  repeat. `ObjectFaker.strict()` no longer reports such a rule as shadowed. Seeded output changes only for
  fixtures with such a rule on a requested name.
- An inclusive temporal constraint on a `MonthDay`, `LocalTime` or `OffsetTime` now gives the present where
  the clock is at the end of its range, as the Bean Validation guide says only a strict one has no value
  there. `@FutureOrPresent MonthDay` failed generation all day on 31 December, `@PastOrPresent MonthDay` all
  day on 1 January, and `@FutureOrPresent` or `@PastOrPresent` on a `LocalTime` or `OffsetTime` at the last or
  first instant of a day; they now give that day or time, which Hibernate Validator accepts under the same
  clock. `@Future` and `@Past` still fail there. Other values are unchanged, so seeded output changes only
  where generation failed.
- `@PastOrPresent` with `@FutureOrPresent` on a `Date`, `java.sql.Date`, `Timestamp` or `Calendar` now fails
  generation as a constraint conflict under a clock finer than a millisecond, as the Bean Validation guide
  says an impossible contract fails. These types hold the clock's instant only to the millisecond, which
  Hibernate Validator compares with the whole instant, so under a clock fixed at `.123456789Z` the generated
  value fell before it and broke `@FutureOrPresent` for all four types, and the millisecond after would
  break `@PastOrPresent`. Under a clock at a whole millisecond the value is still the clock's instant, which
  Hibernate Validator accepts. Seeded output changes only for these members under a finer clock, such as the
  system clock, where generation now fails.
- `CompanyEmailGenerator.generate(String)` now keeps the first 63 letters and digits of a company name as the
  domain label, the most a label may hold, as the data validity guide says every address a generator builds
  passes Hibernate Validator's `@Email`. It kept them all, so with seed 42 `generate("a".repeat(64))` gave
  `carolinetran@` and a label of 64 `a`s under `.test`, which `@Email` rejects; the label now has 63. Generated
  company names are unaffected: the longest label of 800,000 seeded names in eight locales has 52 characters,
  so seeded output changes only for a given company name of more than 63 letters and digits.
- `ProviderHub.register(name, factory)`, which uses `ConflictPolicy.FAIL`, now rejects a name that is already
  an alias, as the provider-hub guide says `FAIL` rejects a duplicate registration. It checked provider names
  only, so `register("first_name", ...)` succeeded and `get("first_name")` then returned the new provider
  instead of the `person.first_name` generator that the built-in alias names. `ConflictPolicy.REPLACE` still
  lets the name resolve to the new provider. Generated values are unchanged.
- `MoneyGenerator` now shows an amount below its maximum, as its `[0, max)` range says. It bounded the drawn
  amount, which the currency format then rounded, so a draw of 0.999 through `generateDollar(1)` showed
  `$1.00`. An amount that would show at the maximum is now rounded down: with seed 42, of 1,000,000 values
  each, `generateDollar(1)` showed `$1.00` 5,186 times and `generate(1)` in `ja_JP` showed `￥1` 500,202
  times, and they now show `$0.99` and `￥0`. With the default maximum of 10,000, 50 of 1,000,000 yen amounts
  showed `￥10,000` and none of 1,000,000 dollar amounts reached `$10,000.00`. Every other value is unchanged.
- `PickSetGenerator` and `Generators.pickSet` now pick distinct values, and `FinitePoolGenerator` and
  `Generators.pool` emit each value at most once a cycle, as they say. Both counted positions, so a list that
  repeats a value gave it twice: `pickSet(List.of("x", "x"), 2)` gave `[x, x]`. They now count values by
  `equals`, so that a list with repeats keeps working wherever the count can be met: a pick takes the first
  distinct values of the same shuffle, a count above the number of distinct values fails at construction,
  and a pool emits each value at its first copy in the same order and drops the others, so `remaining()`
  counts distinct values. With seed 42, of 100,000 picks of two from `a, a, b, b, c`, the 20,135 that held a
  repeat changed, such as `[a, a]` to `[a, b]`, and no other did. A list without repeats gives the same
  values as before.
- `new SqlDateGenerator(min, max)`, and a `java.sql.Date` member under `objectDateRange`, now give only dates
  that read back as days of the range. A `java.sql.Date` holds a day as its start in the default time zone,
  in the Julian calendar before 15 October 1582, so a day before the year 1, after the year 292278994, in
  5 to 14 October 1582, or that the default zone skipped, such as 30 December 2011 in `Pacific/Apia`, read
  back as another: a range of the single day 0000-06-01 gave 0001-06-01, and one from 1582-10-01 to
  1582-10-15 gave days up to 1582-10-24. A drawn day that reads back outside the range now gives the range's
  last day that a `java.sql.Date` holds, and a range that holds no such day, such as one in the year 0, fails:
  `new SqlDateGenerator(min, max)` with `IllegalArgumentException`, and a `java.sql.Date` member as a
  constraint conflict at its path, or under `objectIgnoreErrors(true)` as `null` that the failure listener is
  told of. With seed 42, of 100,000 values from 1582-10-01 to 1582-10-15, the 60,120 past `max` now give
  1582-10-15; every value that read back in its range is unchanged, as is every value of a range whose days a
  `java.sql.Date` all holds.
- `LocalDateTimeGenerator.future(days)` now stays in `[now + 1 second, now + days]` and `past(days)` in
  `[now - days, now - 1 second]`, as they say. Each drew from a span one second longer, which `future` began
  at `now + 1 second` and `past` ended at `now - 1 second`, so `future` could give `now + days + 1 second` and
  `past` `now - days - 1 second`. That one value now gives the end of the range, and every other draw keeps its
  value: with seed 42, of 1,000,000 values each, 13 of `future(1)` and 16 of `past(1)` changed, and none of
  `future()`, which spans 3,650 days.
- A generation failure in a nested object now tells a `GenerationFailureListener` the path that generation
  reached it on, from the root object, as `GenerationFailureContext` says its path is. It named the declaring
  type instead, so under `objectIgnoreErrors(true)` failures at `Top.left.task` and `Top.right.task` both
  reached the listener as `Mid.task`, and a strict failure was told first as `Mid.task` and then, as the
  exception names it, as `Top.left.task`. An unsupported type, a constraint conflict, a collection, map or
  array insertion, a collection that cannot be constructed, a `@Randomizer` and generation's assignment of a
  field now name the member's path, as uniqueness and depth-limit failures already did, so a field that the root inherits
  is named by the root's type rather than by the superclass that declares it. Generated values are unchanged.
- `ObjectFaker.assertConfigurationIsValid()` and `ObjectFaker.strict()` now report a root rule, root ignore or
  include-mode exclusion as ambiguous where it reaches an object that a nested rule creates, as the
  object-generation guide says they report every other path it applies at. A nested rule creates an object
  on its path that generation leaves `null`, past `objectMaxDepth`, at a cycle, where the configuration
  excludes its field, or under `@Null` or a null probability of one, as a root of its own that those decisions
  reach, but the check followed only what generation
  builds. So with `objectMaxDepth(1)`, `ruleFor("id", ...)` beside `ruleFor("middle.purchase.note", ...)` also
  set `middle.purchase.id` without a report, and a rule on a self-referencing type listed one of the three
  paths it reached. Generated values are unchanged.
- A string seed of several lines, which `GeneratorConfig.Builder.seed(String)` accepts as any non-blank text,
  now makes a `GenerationRecipe`, as the recipe's documentation says it carries any seed text.
  `GenerationRecipe.Builder.seedText(...)` rejected a line break, so with `seed("suite\ncase")`,
  `getGenerationRecipe()`, seeded object generation and seeded `Schema` construction all failed with
  `IllegalArgumentException`. The recipe encodes the breaks, as `seed-text=suite%0Acase`, and replays them.
  Every other configuration generates as before.
- `DataFakerExpressionGenerator` and `Generators.ofDataFakerExpression(...)` now reject every `#{...}` token they
  do not support when they are built, as they and the DataFaker migration guide say an unknown token fails
  fast. A token was read only where it held letters, dots and underscores, so `#{Name.noSuchMethod1}`,
  `#{Name.firstName()}` and `#{number.number_between '1','10'}` stayed in the output as literal text.
  Expressions of supported tokens generate as before.
- `TextGenerator.generate(TextOptions)` with `uniqueWords` no longer runs forever on a word list that repeats
  a word. It waited for as many different words as the list had entries, so `new TextOptions(20,
  List.of("one", "one"), true, false)` never returned; it now returns `one.`, and a text holds each distinct
  word of the list at most once. Seeded output is unchanged for every text that was generated before.
- `Schema` and `SchemaProjection` YAML output quotes a key that a YAML parser reads as another type than a
  string: a boolean or null word in any letter case, such as `true`, `No`, `ON`, or `null`; an integer, such
  as `123`, `-1`, `1_000`, `017`, `0x1F`, `0o17`, or `0b101`; a float with an exponent, such as `1e3`; or a
  date, such as `2024-01-05`. It wrote them bare, so `- true: 1` read back as a boolean key. Other keys,
  such as `1st` or `-`, are written as before, and values were already quoted.
- `Schema` and `SchemaProjection` TOML output escapes U+007F (DEL) as `\u007f` in strings and quoted keys.
  It wrote the character raw, as JSON allows, but TOML forbids it, so a parser such as Python's `tomllib`
  rejected the document. Every other character is written as before, and JSON output keeps DEL raw.
- The JSON Schema that `Field.constant(...)`, `SchemaValueProvider.withSample(...)`, and payload templates
  export, and so `Schema.toJsonSchema()`, now accepts their own values. Array items came from the first item
  that is not `null` alone, so `[1, "a"]` exported integer items and `[1, null]` items that reject `null`;
  items of different types now export `{}`, which allows any value, and a `null` item makes the others'
  schema allow `null`: `{"type": ["integer", "null"]}` beside integers, and an `anyOf` with the null type
  beside a schema whose `const` or composition could reject `null`. A map key that is not a string was left out of
  `properties` beside `additionalProperties: false`, so `{1: "x"}` exported a schema that rejects the
  `{"1": "x"}` every writer writes; the key is now the member `"1"`. Generated values are unchanged.
- `SchemaParser.fromJsonSchema(...)` now holds every record to a root `const` or `enum`, which it ignored. A
  root `const` of `{"x": "expected"}` beside a property `x` with the `const` `actual` was accepted, and every
  record broke it; it is now rejected at `$`, as is any root `const` or `enum` that names no record of
  exactly the root's properties that meets their schemas. Beside an `x` of any string, every record is now
  `{"x": "expected"}`, where with seed 42 the first three held `XFnHbdhjiPu`, `yPJYFTHauN`, and `ZTqiGLl`. A
  record that the columns generate and that the root names is kept, so seeded output changes only for
  records that broke the root `const` or `enum`; any other record is replaced by one the root names, drawn
  from the record's own stream where there are several. `toJsonSchema()` then exports each column as an
  `enum` of the values that the named records give it, written as the writers write them, while the column's
  provider in `getFields()` keeps its own metadata.
- In `SchemaParser.fromJsonSchema(...)`, a property without a `type` whose name chooses a semantic value of
  another type, such as the integer Unix timestamp of `datetimeTimestamp` or the company object of
  `companyInfo`, now meets the schema's keywords for that type, which it ignored. With seed 42 and a UTC
  clock, `datetimeTimestamp` with `maximum` -1000000000 gave 798249600, 1438128000, and 3699302400, and now
  gives -1521810362, -2139508635, and -1901379525; `companyInfo` with `maxProperties` 2 gave a company of
  eight members, and now gives `{}`. A semantic value that meets the keywords is kept, so seeded output
  changes only for values that broke them; any other value is drawn from the keywords as if the schema
  declared the type, and keywords that leave that type no value, such as `minimum` 5 with `maximum` 4, are
  rejected with the schema path. The exported schema of such a property names only its type.
- In a `Schema` with a portable seeded configuration, one that has a `GenerationRecipe`, a `Field` reference,
  a reference that `registerProvider(...)` adds, the `#` and `?` of a template, and a column that
  `SchemaParser` resolves by its name now draw from each value's own stream, named by its record and column,
  as the `Schema` documentation says every value does. They drew from one generator that every column of
  the `Field` shared, so a column changed the values of the columns after it: with seed 42, a
  `template("##")` column `x` was `03` alone and `84` after another such column, and is now `37` either way.
  Each value now makes its generator from a seed drawn from its stream, which changes every such value of a
  portable seeded schema, and makes it depend on its column's name, as the stream's does: with seed 42, a
  `person.first_name` column named `name` went from `Victor` and `Kenneth` to `Grace` and `Ivy` in its first
  two records, and parsed columns `firstName` and `email` from `Victor` and `caroline.tran@example.com` to
  `Stella` and `landonhill@example.net`. Seeded schemas without a recipe,
  unseeded schemas, a `Field` whose configuration uses a caller-owned, factory-made, or secure random source,
  a `SchemaContext` that the caller makes, and `ProviderTemplateGenerator` keep their values. Making a
  generator for each value takes longer, even though a generator now reads each bundled registry's locale
  keys once rather than copying them on every lookup, which halved the cost of building one, and a value's
  configuration reuses one builder: `SchemaOutputBenchmark.largeSchemaBatch`, 250 records of 12 references,
  gave 933 operations a second and gives 194, and allocates 30.5 MB an operation rather than 3.7 MB. The
  benchmark methodology records this as an accepted breach of its regression budgets, with the raw runs.

### Documentation

- The schema guide no longer says that a schema with no `type` always generates a string, or that a
  property schema `true` does: a property name that chooses a semantic value of another type, such as
  `datetimeTimestamp` or `companyInfo`, keeps that type, and the schema's keywords for that type apply.
- The versioning policy, `Seedable`, `NormalDistributionGenerator` and `GenerationRecipe` now say what a seed
  does not fix: values that follow the Java runtime's TZDB and CLDR data, such as zone IDs, zone offsets and
  formatted money amounts, whose versions a recipe does not record; Gaussian values, whose `Math.log`,
  `Math.sin` and `Math.cos` may differ in the last bit between platforms; and a generator backed by a
  `SecureRandom`, whose `reseed(long)` adds to its state rather than restarting a sequence.

## [3.0.0] - 2026-11-10

### Changed

- kRandom follows Semantic Versioning from 3.0.0. Only a major release removes public API, changes it
  incompatibly, or raises the minimum Java version or the Spring Boot major version, and public API is
  deprecated in a minor release first, with the deprecation naming its replacement. Minor and patch
  releases keep seeded output the same, except where a fix restores a documented contract, which the
  changelog lists under Fixed, and a 3.x release replays recipes that earlier 3.x releases wrote.
  `VERSIONING.md` defines the public API. Releases before 3.0.0 made no compatibility promise. The
  3.0.0 code, API, behavior, and seeded output are those of 2.12.0.

## [2.12.0] - 2026-11-10

### Added

- A value-free generation report. `GeneratorConfig.Builder.generationReportListener(...)` sets a
  `GenerationReportListener`, which receives a `GenerationReportEntry` for each value that object generation
  produces, with an `ObjectGenerator`, an `ObjectFaker`, an `ObjectModel` or the Kotlin DSL: its path, its
  `SeedSource` (the configuration's seed, a stream derived from it by name, a replay recipe, or none), the
  recipe format `krandom-recipe/v1` when the configuration has a portable recipe and the names of the recipe
  settings that shaped the value, the `GenerationStep` and generator that produced it, the `FixtureRule`
  that matched it, whether an `ObjectFaker` rule with its registration number or a `GeneratorConfig`
  override, its Bean Validation constraints, the safety policies that bound it, such as
  `network.safety-policy`, and its `GenerationDecision`s: a cut at `objectMaxDepth` or at a cycle, a fallback
  for an unsupported type, unmet constraints or another failure under `objectIgnoreErrors(true)`, a drawn `null` or empty
  `Optional`, a semantic provider deferred to a constraint or annotation, and a coherence move, or a member
  that `RELAXED` mode keeps. An entry never holds a generated value. Reporting draws nothing from any random
  stream, so seeded output is the same with or without a listener, and without one nothing is reported or
  built. A listener that throws is logged and ignored. `GeneratorConfig.isRestoredFromRecipe()` says whether
  a configuration replays a recipe, and the Kotlin DSL sets the listener with
  `config { generationReportListener { entry -> ... } }`.
- `ObjectFaker.strict()`, which has nothing to do with `ObjectGenerationSemanticMode.STRICT`, now reports
  unused, ambiguous and shadowed fixture rules, together with the root properties that have no rule,
  include or ignore, in one `FixtureRuleException`, before generating or populating. `assertConfigurationIsValid()`, which `ObjectFaker.strict()` runs, reports the same on demand.
  Each problem is a `FixtureRuleProblem` with its category, its `FixtureRule`, given by `FixtureRuleKind`,
  path and registration number, and a reason that names paths and rules, never a value. The rules,
  includes and ignores of an `ObjectFaker` are numbered in registration order, across profiles and composed
  `ObjectModel` values; a `GeneratorConfig` entry has the number -1 and the path where it takes effect.
  - Shadowed: a rule that a later rule for the same path replaces, whatever their kinds; a nested rule that
    a rule on an ancestor path overwrites because it runs later, such as `address.city` under a dependent
    rule on `address`; a root rule that a `GeneratorConfig` contextual field override or contextual
    predicate override for the same member comes before, so that it never runs; a `GeneratorConfig` field
    override for a member that a root rule replaces or comes before, or that a nested or dependent rule
    overwrites; and a name requested with `objectUniqueField`, whose values a nested or dependent rule sets
    without the uniqueness check that a root rule gets.
  - Unused: a rule below a path that a nested ignore registered after it clears; a root rule on a member
    that the `GeneratorConfig` excludes, by `objectExclude`, `objectExcludeField`, `objectExcludeType` or
    `@Exclude`; a root rule or root ignore when a `GeneratorConfig` type override builds the root, which
    `generate()` does and `populate(existing)` does not; and a nested ignore of a member that `generate()`
    never gives a value, because `objectMaxDepth`, an exclusion or include mode leaves an object on its path
    `null`, which an instance given to `populate(existing)` may still hold. A
    nested rule past `objectMaxDepth` is used, because it creates the objects on its path, and a path into
    a member that an `objectSubtype` implementation or a sealed subclass lacks already fails at
    registration, because paths are resolved against the declared types.
  - Ambiguous: a root rule, root ignore or include-mode exclusion that also applies at another path. Each
    applies by its member's declaring class and name, so it also reaches that member in a nested object of
    the root's type, as in `manager.name` for `ruleFor("name", ...)` on an `Employee` with an `Employee
    manager`, or of another subclass of the class that declares an inherited member, as in `customer.id`
    for `ruleFor("id", ...)` on an `Order` whose `Customer` shares its `BaseEntity`. The check follows the
    nested objects that generation reaches, through collections, maps, arrays, `Optional`, constructor
    arguments, `objectSubtype` mappings and sealed types, within `objectMaxDepth` and with the cycle guard's
    keys, and leaves out members that an override, an exclusion, a `@Randomizer`, a kept initializer or
    `@Null` decides, including an element's `@Null` as in `List<@Null Purchase>`, the elements of a
    collection whose configured size within its `@Size` allows none, of a sorted collection or map whose
    elements or keys have no order, and of one whose type arguments do not resolve, and members that an
    `objectNullProbability` or `objectOptionalEmptyProbability` of 1 always leaves absent. A type override
    for the root or for an `objectSubtype` implementation ends the walk, and a path through the root's
    constructor holds for `generate()` only, because `populate(existing)` builds no root. In `generate()`, a
    root rule that fills a constructor argument of its name is not shadowed by a contextual predicate
    override, which reaches fields only; in `populate(existing)`, which fills the field, it is. A dependent
    rule applies at its own path only. A string path that matches two fields still fails at registration.

  `ObjectFaker.strict()` checks once for each configuration, and again after a new rule, include or ignore.
  Without `ObjectFaker.strict()`, generation is unchanged. A replacement of a rule for the same path, the
  documented way to refine a profile or a shared `ObjectModel` per test, is reported as shadowed, so
  `ObjectFaker.strict()` rejects such a refinement.

### Changed

- `ObjectFaker.strict()` and `assertConfigurationIsValid()` throw `FixtureRuleException`, which extends
  `IllegalStateException`, with every problem on a line of its own, where they threw an
  `IllegalStateException` reading `ObjectFaker for <type> has no rules for: [...]`. The undecided root
  properties are now `getUndecidedProperties()` and the line `UNDECIDED root properties [...]`. To see which
  members keep an initializer, `assertConfigurationIsValid()`, and so `ObjectFaker.strict()`, now creates
  one object of each class it follows with the class's no-argument constructor, as generation does.
- The `ObjectFaker` documentation now gives the order in which its rules run and their scope, which
  `ObjectFaker.strict()` checks: a root rule, a root ignore and include mode apply by the member's declaring
  class and name, as they always have, and the other rules at their own paths; against the `GeneratorConfig` overrides for the same member, a root
  context rule comes first, and a root rule after a contextual field override and a contextual predicate
  override and before the others.

### Fixed

- An `ObjectFaker` path through a member declared as an interface, such as `shape.side` where `shape` is a
  `Shape` interface, now fails at registration with `Unknown field 'side' on` and the interface's name, as a
  path into any type without that field does. It failed with a raw `NullPointerException`. An `ObjectFaker`
  for an interface, which a type override builds, no longer fails the same way in
  `assertConfigurationIsValid()` and so in `ObjectFaker.strict()`.
- `Schema` writes a date or time in the RFC 3339 form of the format its type exports, in every output:
  JSON, JSON Lines, YAML, TOML, CSV, XML, and SQL. It wrote `toString()`, which leaves out `:00`
  seconds, so a `LocalDateTime` at 09:05 was written `2024-01-05T09:05`; adds a `ZonedDateTime`'s zone
  ID, as in `2024-01-05T09:05+01:00[Europe/Paris]`; writes a `java.util.Date` in the default time zone,
  as `Fri Jan 05 09:05:00 GMT 2024`; puts a space for the `T` in a `Timestamp`; and lists a `Calendar`'s
  fields. A `LocalDate` is now a full-date, a `LocalTime` a partial-time, a `LocalDateTime` a date-time
  without an offset, an `OffsetTime` a time with its offset, and an `OffsetDateTime`, a `ZonedDateTime`,
  with its offset and no zone ID, and an `Instant`, with `Z`, a date-time. Seconds are always written,
  and a fraction only where it is not zero, without trailing zeros, so an `Instant` at half a second
  ends `.5Z` where it ended `.500Z`. A `java.util.Date` or `Timestamp` is written as its instant, a
  `Calendar` at the offset its own time zone gives that instant, whatever the zone's ID names and even
  beyond the 18:00 a `ZoneOffset` holds, and a `java.sql.Date` or `Time` as its date or time of day,
  where a `java.sql.Date` keeps its era, so 1 BCE is the year 0000. A year outside 0000 to 9999, an
  offset with seconds or beyond 23:59, and a Julian day that ISO does not have, such as 29 February 1500
  in a `java.sql.Date`, which RFC 3339 cannot write, are refused with an `IllegalArgumentException` in
  every output, as JSON refuses a number it cannot write. `SchemaParser` measures and compares a date or
  time `const` or `enum` by the same text, so a `const` of `LocalTime` 09:05 with `maxLength` 5 is now rejected, as
  `09:05:00` is too long, and that `LocalTime` and the string `"09:05:00"` are one JSON value. JSON
  Schema export now names `time` for an `OffsetTime` member and `date-time` for an `Instant` member,
  where it named no format. Written output changes for every value of these types; generated values do
  not.

## [2.11.0] - 2026-11-10

### Added

- `RegexGenerator` now supports `(?i)` and `(?-i)`, which hold to the end of the enclosing group, and
  `(?i:X)` and `(?-i:X)`, which hold inside it. As in `java.util.regex` without `UNICODE_CASE`, only
  ASCII letters match their other case, so a generated ASCII letter can take either case and every
  other character keeps the one written: `(?i)ab` generates `ab`, `aB`, `Ab`, and `AB`, and `(?i)é` only
  `é`. A negated class under `(?i)` leaves out both cases of each letter it names. Other inline flags
  are still rejected, and the message now names them, as in `Unsupported inline flag s`.
- `RegexGenerator` now supports `\p{Name}` and `\P{Name}`, inside and outside a class, for `Lower`,
  `Upper`, `ASCII`, `Alpha`, `Digit`, `Alnum`, `Punct`, `Graph`, `Print`, `Blank`, `Space`, `XDigit`,
  `L`, `Lu`, `Ll`, `N`, `Nd`, `IsAlphabetic`, `IsDigit`, and `IsLatin`, and the one-letter `\pL`, `\pN`,
  `\PL`, and `\PN`. The ASCII ones draw from what they match, and the Unicode ones from a fixed sample of
  ASCII, Latin-1, Greek, and Cyrillic letters and ASCII, Arabic-Indic, and Devanagari digits, listed in
  the data-validity guide. `\P{Name}` draws from the printable ASCII characters outside the property.
  Any other property, or another spelling of these such as `\p{IsLower}`, is rejected, and the message
  now names it.
- `RegexGenerator` now supports quoting with `\Q...\E`, read as `java.util.regex` reads it, so a
  quantifier after `\E` repeats the last quoted character: `\Qab\E+` generates `ab`, `abb`, and so on.
- The object engine now generates `java.util.Calendar` and `GregorianCalendar` members. They failed as
  unsupported types, although `Generators.ofCalendar()` exists, and a `GregorianCalendar` with `@Past` or
  another temporal constraint failed too. A member is a non-lenient `GregorianCalendar` with ISO week rules,
  as from `ofCalendar()`, drawn as a `ZonedDateTime` member is: in a random zone, with `objectDateRange` on
  its local date, and with the semantic windows of `createdAt`, `updatedAt`, and `birthDate`. The coherence
  pass aligns it with its sibling and with `age`, as it does a `ZonedDateTime`. A calendar is drawn from the
  `ZonedDateTime` generator and draws no seed of its own, so seeded output does not change for objects
  without one.
- `SchemaParser` supports `multipleOf` and `uniqueItems`, which it used to accept and ignore, so a
  generated value could break them. Seeded output changes for every schema that uses either.
  - `multipleOf` must be a number above 0. It generates exact multiples within the bounds, as decimals
    with no binary rounding, so `multipleOf` 0.01 gives exact hundredths. An `integer` stays an
    `Integer` or `Long` and takes the whole multiples, so `multipleOf` 2.5 gives multiples of 5. A
    `number` with `multipleOf` now generates a `BigDecimal` rather than a `Double`. Bounds that hold no
    multiple, such as `minimum` 1 and `maximum` 4 with `multipleOf` 5, are rejected with the schema
    path, and a `const` or `enum` keeps only multiples. A missing bound defaults from the declared one,
    as for any number, and where that default range holds no multiple, the open side reaches the
    nearest one, so `minimum` 0.1 with `multipleOf` 1000000000 gives 1000000000. An `integer` multiple
    must fit a `long`.
  - `uniqueItems: true` gives arrays whose items are distinct JSON values, so `1` and `1.0` count as
    one. While parsing, a search with a fixed seed looks for `maxItems` distinct items, drawing up to
    100 for each. Items that cannot reach `minItems`, such as an `enum` of two values with `minItems` 3,
    are rejected with the schema path, and where the search finds fewer than `maxItems`, an array holds
    at most that many, so nested arrays of few values stay cheap. Each array then draws up to 100 items
    for each item it holds, keeps the distinct ones found if they run out, and fails with the schema
    path below `minItems`. A `const` or `enum` array with repeated items is rejected or dropped.
  - A `multipleOf` that is not a number above 0, or a `uniqueItems` that is not a boolean, is rejected
    with the schema path.
- `Vocabulary.POSTAL_PLACE` holds a country's postal places: `PostalPlace` records of a city, the region
  it lies in, the region's code, and postal codes that serve the city, from a `PostalPlaceDataProvider`.
  `PostalPlaceDataRegistry` holds the bundled places, curated from openly licensed national data: for
  `en_US`, 252 cities in all 50 states and DC with up to ten ZIP codes each, from U.S. Census Bureau
  files (public domain); for `en_GB`, 111 cities in Great Britain with up to ten postcodes each, from OS
  Open Names (Open Government Licence v3.0); for `de_DE`, 115 cities with the postal code of each city's
  administrative seat, from the Destatis Gemeindeverzeichnis; and for `fr_FR`, 107 communes with up to
  ten postal codes each, from La Poste and Insee (Licence Ouverte 2.0). The header of each table in
  `krandom/postal_places/` records its sources, licence, retrieval date, and checksums, and how the rows
  were chosen. As for `Vocabulary.NATIONAL_ID`, a registration serves every locale of its
  country, and a locale whose country has no places resolves none rather than borrowing another
  country's through its language. `LocaleDataBundle.Builder.postalPlaces(...)` registers places with a
  bundle. `Vocabulary.all()` lists 42 vocabularies.
- The object engine now generates a sealed interface or abstract sealed class, as a root or as a member, as
  one of the concrete classes of its hierarchy, where it failed as an unsupported type. Each class is equally
  likely, and the choice is drawn from the seeded stream in binary-name order rather than the order the type
  permits them. The classes are each permitted subclass that is concrete, a record, or an enum, which gives
  one of its constants, and the classes of each abstract sealed subclass, at any depth. A class already being
  generated on the member's path is left out while another remains, so a recursive hierarchy such as an
  expression tree ends in a leaf, and the chosen class counts as being generated as its own type, so a member
  of that type inside it is cut as a cycle. A type override or `objectSubtype` mapping for the sealed type
  wins, one for an abstract subclass makes that subclass a choice, and an abstract subclass with neither fails
  generation as an unsupported type that names it, whatever the seed. A sealed root is generated from its seed
  as a root of the chosen class would be, except that the chosen class counts as being generated, so its
  members leave it out of their choices while another class remains. A concrete sealed class is still
  generated as itself. A construction adapter can declare a type sealed and list its subclasses through the
  new default method `ObjectConstructionAdapter.sealedSubclasses`. The choice draws only for sealed members,
  so seeded output does not change for objects without one.
- The Kotlin DSL generates Kotlin sealed classes and interfaces in the same way, reading their subclasses
  from the Kotlin metadata whatever JVM target compiled them, and an `object` subclass gives its instance.
- The Kotlin DSL generates `@JvmInline` value classes, where they failed with `Kotlin value classes are not
  supported for object generation; register a type override`. A value class is generated as the value it
  wraps and boxed through its primary constructor, so its `init` block runs. For a constructor parameter,
  the wrapped value is generated under the parameter's name and with its constraints, or else with those
  declared inside the value class, so `val email: EmailAddress` holds an email address. A value class over
  a string, a number, a nullable value, another value class, or its type parameter, alone or inside a type
  such as `List<T>`, with an argument that may be the owner's own type parameter, works as a constructor
  parameter, nullable or not, as Kotlin's unsigned types do. A value class also works
  as a collection element and, unless it is generic, as the generated type and as a `var` property set
  after construction. A rule or
  `ruleForType` for the value class supplies it, a rule may give the wrapped value instead, which is boxed,
  and a `ruleForType<String>` reaches a `String` inside a value class. A value that the `init` block rejects
  fails with the value class and the parameter named. A class whose primary constructor takes a value
  class is built through that constructor, where the private and synthetic JVM constructors that Kotlin
  compiles it to failed as ambiguous, unless it has a no-argument constructor, which still builds it before
  its fields are filled.
- `SchemaParser` generates `items` written as a list of schemas, one for each position, and
  `additionalItems` beside it, which it used to accept and ignore, so such an array held strings. These
  are the tuple forms of the drafts up to 2019-09, read whatever `$schema` says, and `prefixItems`, their
  Draft 2020-12 replacement, is still rejected. Seeded output changes for every schema with a list of
  `items` or with `items: false`.
  - Each position follows its own schema, and the items after the tuple follow `additionalItems`, or
    are strings without it. An array holds the whole tuple unless `minItems` or `maxItems` moves its
    size, so `minItems` 0 allows shorter arrays.
  - `additionalItems: false` ends an array with the tuple, a `false` entry ends it before that position,
    and `items: false`, which used to give strings, allows only the empty array. A `minItems` past any of
    them is rejected with the schema path, such as `$.properties.pair`, and a tuple entry is checked at
    its own, such as `$.properties.pair.items[1]`.
  - With `uniqueItems`, each item is drawn for the position it fills, so an array ends early at a
    position that has no new value, and fails below `minItems`.
  - A `const` or `enum` array must meet the tuple, `additionalItems`, and `items: false`.
    `additionalItems` beside a single `items` schema is ignored, as those drafts say.
  - An `items` that is not a schema, a boolean, or a list of them, such as `"string"`, and an
    `additionalItems` beside a list that is not a schema or a boolean, are now rejected with the schema
    path. They used to leave the items unconstrained.
- `SchemaParser` supports `minProperties`, `maxProperties`, and property `dependencies`, which it used to
  accept and ignore, and honours `required` and `additionalProperties`, which it read only as document
  controls, so a generated object could break any of them. Seeded output changes only for a nested
  object that these keywords now change, and for one with a property schema `true`.
  - A nested object still holds every property it declares unless a keyword asks otherwise.
    `maxProperties` leaves out optional properties, chosen at random, until the object fits, and
    `minProperties` adds as few additional properties as it needs, named `property1`, `property2`, and
    so on, past every name the schema uses, with values from the `additionalProperties` schema, or
    strings without one. A `required` property that is not in `properties` is generated the same way,
    and its name chooses a realistic value where it can. Where `additionalProperties` is `false`, none
    can be added, so such a schema is rejected with its path.
  - A property schema `true` now generates a string, as `{}` does, where the property used to be left
    out, and `false` leaves the property out. A `required` property whose schema is `false`, or is no
    schema, is rejected with the schema path.
  - A property dependency, such as `"dependencies": {"card": ["billing"]}`, brings the properties it
    names, through every link, beside the property that needs them. A property whose dependencies cannot
    be generated, because they are not in `properties` and `additionalProperties` is `false`, is left
    out, and rejected if it is required. `maxProperties` leaves out a property only after every property
    that depends on it, and properties that depend on each other only together; where
    `additionalProperties` is `false`, `minProperties` and `maxProperties` must then leave a range as wide
    as each such group, or the schema is rejected.
  - A schema dependency, which asks an object that holds one property to meet a second schema too,
    needs schema composition, which the parser does not have, as for `allOf`. It is now rejected with its
    schema path, such as `$.properties.order.dependencies.card`, where it used to be ignored.
    `dependencies` is the form of the drafts up to 7, read whatever `$schema` says, and
    `dependentRequired` and `dependentSchemas`, the Draft 2019-09 forms, are still rejected.
  - The root generates every property as a column, so a root `required` name or property dependency that
    names another property, or a `minProperties` or `maxProperties` that its properties do not meet, is
    now rejected at `$`.
  - A `const` or `enum` object must meet `required`, `additionalProperties`, `minProperties`,
    `maxProperties`, and the property dependencies.
  - A `minProperties` or `maxProperties` that is not a whole number of at least 0, a `required` that is
    not a list of names, an `additionalProperties` that is not a schema or a boolean, and a
    `dependencies` that does not map names to lists of names, are rejected with the schema path. An
    `additionalProperties` schema is checked and resolved like any other, so a `$ref` inside it, or a
    combination that leaves it no value, is now rejected.
- `SchemaParser` generates every format Draft 2020-12 defines, and OpenAPI's `byte`, `int32`, `int64`,
  `float`, and `double`. It used to fill the other string formats with semantic or random strings,
  which a validator that checks formats rejects for some of them, such as `duration` and
  `json-pointer`, and to ignore a format on a number. Seeded output changes for every schema that uses
  one of them.
  - `uri-reference`, `iri`, `iri-reference`, and `uri-template` come from the URL generator, as a URI
    with no quote or brace is each of them, and `idn-hostname` from the hostname generator, as a
    hostname is one.
  - `duration` is RFC 3339 days, hours, minutes, and seconds, such as `P3DT4H5M6S`; `json-pointer` is
    one to three segments of lowercase letters, such as `/abc/de`; `relative-json-pointer` puts a level
    count before one; and `regex` is letters, which match themselves. They meet `minLength` and
    `maxLength` as the other formats do.
  - `byte` is base64. `int32` and `int64` name integers, so a `number` with either is now generated as
    an `integer`, where it used to get fractions. `int32` keeps it within an `int`, so bounds that hold
    no `int` are rejected with the schema path, and `float` and `double` keep a `number` within their
    range, applied after the default range, so a `float` without bounds keeps it.
  - `binary` and `password` allow any string, and no standard validator checks any other format, so
    they keep the semantic-name or bounded-string fallback.

### Changed

- Composite generators now give each child generator its own stream by default, under
  `ChildStreamPolicy.INDEPENDENT`, which was an opt-in since 2.6.0. With a numeric or textual seed, a
  child's seed is derived from the parent seed and the child's name, so siblings no longer start in the
  same state: the payment, invoice, and order numbers of a `PaymentInfo` seeded with `42` were
  `PAY-359D41BAF78A`, `INV-359D41BAF78A`, and `ORD-359D41BAF78A`, and are now `PAY-7DC1F9C37F29`,
  `INV-F852387C7D39`, and `ORD-B9D4F25FCB19`. Seeded output changes for every generator with children,
  such as `FullNameGenerator`, `MiddleNameGenerator`, `EmailGenerator`, `UsernameGenerator`,
  `WordGenerator`, `SentenceGenerator`, `URLGenerator`, `HostnameGenerator`, `OffsetDateTimeGenerator`,
  `PersonInfoGenerator`, and `PaymentInfoGenerator`, and for the values built on them, such as
  `Krandom.fullName()`, `middleName()`, and `email()`, object members such as `fullName`, `email`,
  `username`, and `streetAddress`, and schema columns such as `person.full_name` and `person.email`. The
  Spring starter, the JUnit and Kotest extensions, and the Kotlin DSL take the new default.
  `ChildStreamPolicy.LEGACY` restores the earlier output draw for draw, so the network values of
  releases before 2.10.0 now take it together with `NetworkSafetyPolicy.REALISTIC_UNCLASSIFIED`.
  `INDEPENDENT` no longer requires a seed: an unseeded configuration has nothing to derive from, so
  `forChildStream` returns it unchanged and each child draws from its own fresh `Random` or
  `SecureRandom`, as before. A caller-owned `Random` is still shared by every child, and a random
  factory is still called once for each child. Under `INDEPENDENT`, `forChildStream` rejects a blank
  or multi-line stream name with or without a seed, which the earlier default accepted.
- A recipe now records the child-stream policy in the setting `child-stream-policy`, whichever policy it
  is, and replay requires it, so a recipe without it, such as one written under the earlier `LEGACY`
  default, is rejected rather than replayed with independent streams.
- `krandom-jackson` now requires Jackson 3 (`tools.jackson`), the Jackson that Spring Boot 4
  auto-configures by default, instead of Jackson 2 (`com.fasterxml.jackson`): it depends on
  `tools.jackson.core:jackson-databind` 3.2.4, which brings the 2.x `jackson-annotations` that
  Jackson 3 uses. `KrandomJacksonModule` is a Jackson 3 module and cannot be registered with a
  Jackson 2 `ObjectMapper`, so Jackson 2 users stay on 2.10.0 or register a serializer of their own
  that writes `Schema.toJsonSchema()`. Jackson 3 mappers are immutable, so
  `KrandomJackson.configure(ObjectMapper)` is replaced by `configure(builder)`, which adds the module
  to a mapper builder such as `JsonMapper.builder()` and returns that builder, and `newObjectMapper()`
  by `newJsonMapper()`, which returns a `JsonMapper`. In Spring Boot 4, a `KrandomJacksonModule` bean
  is registered with the auto-configured `JsonMapper`.
- `KrandomJackson.newJsonMapper()` keeps Jackson 3's defaults, so serialized rows differ from those
  of `newObjectMapper()`. `java.time` values, such as the `orderedOn` date of `commerce.order_info`,
  are written as ISO-8601 text, where Jackson 2 rejected them and failed the whole row. A
  `java.util.Date` is ISO-8601 text instead of epoch milliseconds, and a class's properties are in
  alphabetical order, while a record keeps the order of its components.
- `RegexGenerator` now rejects at construction a pattern whose values could be longer than 1,000,000
  characters, or take more than 10,000,000 steps to generate, naming the pattern and the limit.
  Nested repetition such as `((a{10000}){10000}){10000}` used to run out of memory while generating,
  and `(((){10000}){10000}){10000}` to loop for hours. The limits apply to the repetition a
  configuration gives `*`, `+`, and `{n,}`, so under a minimum string length of 4,000, `(a*)*` and
  `(\w+ )+`, which generated values of millions of characters, are now rejected. The limits are fixed,
  with no setting to raise them. A JSON Schema `pattern` over them is rejected with its schema path, and
  a `@Pattern` over them is no longer a source. Each value is now built in a single buffer, and seeded
  output does not change for any pattern that was supported and stays within the limits.
- A `@Pattern` or `@Email(regexp = ...)` that uses `(?i)`, `\p{...}`, or `\Q...\E` is now a source of
  values. It used to be only checked against the values of another source, such as a `@Size` string,
  or to fail where there was none. Seeded output changes for such fields. A JSON Schema `pattern` with
  these constructs used to be rejected and is now accepted.
- `CountryGenerator` now has a calling code, a continent, and a time zone for each of the 249 ISO 3166-1
  countries, not 45. They come from three tables bundled beside the ISO 3166-1 table,
  `krandom/country_codes/calling_codes.txt`, `continents.txt`, and `time_zones.txt`, whose headers
  record the source, licence, retrieval date, and checksum, and the rules for countries a source does
  not cover: E.164 calling codes from libphonenumber 9.0.42, continents from the UN M49 regions as
  Unicode CLDR 49 publishes them, with the Americas divided into North America and South America, and
  time zones from `zone.tab` in the 2026f tz database, taking the zone of the seat of government for a
  country with several.
  `currentCallingCode()`, `currentContinent()`, and `currentTimezone()` now answer for any locale whose
  country is an ISO 3166-1 country, such as `en_NG` (`+234`, `Africa`, `Africa/Lagos`) or `de_LI`
  (`+423`, `Europe`, `Europe/Vaduz`), where they threw `UnsupportedOperationException`.
- `CountryGenerator.generateCallingCode()`, `generateContinent()`, and `generateTimezone()` draw from
  all 249 countries, picking the country that `generateCode()` picks for the same seed, so their seeded
  output changes: with seed `42` the first calling codes were `+64`, `+49`, and `+966` and are now
  `+378`, `+690`, and `+672`. A calling code that several countries share, such as `+1`, now comes up
  more often, and `Antarctica` is a new continent. The values of the 45 earlier countries are
  unchanged except Argentina's time zone, which is now `America/Argentina/Buenos_Aires`, the name
  `zone.tab` lists, instead of `America/Buenos_Aires`, an older name that the tz database keeps only
  as a link; `es_AR` and other `AR` locales get the new name. `PhoneNumberGenerator` takes its calling
  codes from the same table, and its output is unchanged.
- `KrandomExtension` now gives a `@MethodSource` factory the class-scoped configuration of its test
  class, the one `@BeforeAll` receives: from the class-level `@KrandomSeed`, a replay override, a class
  recipe, or a random seed. A method-level `@KrandomSeed` on a `@ParameterizedTest` pinned its factory
  too and now pins only its invocations, so pin the class to fix the generated arguments. A `Krandom`
  factory parameter is a session of the parameterized test's own, built from that configuration, so it
  starts the class-level streams again. The `@BeforeParameterizedClassInvocation` and
  `@AfterParameterizedClassInvocation` methods of a `@ParameterizedClass` invocation also take the
  class-scoped configuration, with a session of the invocation's own, where an unpinned invocation had
  a random seed of its own. Seeded output changes for factories under a method-level pin.
- A collection, array, or map with `@Size` or `@NotEmpty` now takes its size from `collectionSize` within
  the declared bounds, or from the declared bound nearest it where the two do not overlap. The declared
  bounds alone used to set the size, so `@NotEmpty List<String>` had 1 to 255 elements whatever
  `collectionSize` said, and `@Size(max = 100)` 0 to 100. Under the default `collectionSize(1, 10)` they
  now have 1 to 10, `@Size(min = 20)` has 20, and `@Size(max = 2)` with `collectionSize(4, 6)` has 2. With
  no declared maximum, the size is no longer cut at 255, so `@NotEmpty` with `collectionSize(300, 400)` has
  300 to 400 elements. A string's length still follows its declared size alone. Seeded output changes for
  these members.
- A `Calendar` generated for `@Past`, `@Future`, or another temporal constraint is now a non-lenient
  `GregorianCalendar` with ISO week rules in the configured clock's zone, as other `Calendar` members are. It
  came from `Calendar.getInstance` in that zone, so it was lenient and its week rules, and its calendar system,
  came from the JVM's default locale: a `BuddhistCalendar` under `th_TH`, for example. Its instant is the same.
- `Generators.ofUniversity`, `UniversityGenerator`, and `Generators.person(config).university()` now name the
  data-pack route when they refuse a configuration. kRandom still ships no university data, because no
  university dataset it could bundle has an acceptable source and licence, so they refuse in every locale
  until a pack is registered. The message said only that the locale had no local university data pack, and
  now reads `Locale en_US has no university data, and kRandom ships none. Load a local data pack with
  LocalDataPack.load(directory), register it with DataRegistryContext.builder().registerDataPack(pack), and
  pass that context to GeneratorConfig.Builder.registryContext(context). Registered locales: []`.
- `AddressInfoGenerator`, `StreetAddressGenerator.generateFullAddress()`, and the object engine's `city`,
  `state`, and `postalCode` members take an address's city, state, and postal code from one postal place
  where the locale's country has places, so the three agree. They were drawn independently: with seed
  `42`, `AddressInfoGenerator` for `Locale.US` gave `8680 Lakeview Holw, Grand Rapids, AL 02649, United
  States` and now gives `8680 Lakeview Holw, Fontana, CA 92335, United States`; for `Locale.GERMANY`,
  `Oldenburg, Rheinland-Pfalz 52881` became `Mönchengladbach, Nordrhein-Westfalen 41061`. Seeded output
  changes for the locales of the United States, the United Kingdom, Germany, and France, such as `en_US`,
  `es_US`, `en_GB`, `de_DE`, `en_DE`, and `fr_FR`, and for `Locale.ENGLISH`, `Locale.GERMAN`, and
  `Locale.FRENCH`, and for the payloads built on these addresses: `PersonInfo`, `CompanyInfo`,
  `OrderInfo`, `ShipmentInfo`, `InvoiceInfo`, `PaymentInfo`, `SimpleProfile`, and `UserProfile`. In
  every other locale `AddressInfo` keeps its independent draws and its seeded output, apart from the
  `en_US` city that became `Gary` (see Fixed); the locale-aware data guide lists which locales have
  places, and the limits of each table.
  `ChildStreamPolicy.LEGACY` does not bring the earlier addresses back. Bundled places take precedence
  over cities or states registered for the same locale without places, which still serve
  `CityGenerator` and `StateGenerator`.
- A locale without a country gives `AddressInfo` the country of the postal places its language resolves
  instead of a random country name and an unrelated random code: `Locale.ENGLISH` gives `United States`
  and `US`, where with seed `42` it gave `United Kingdom` and `GL`, and `Locale.GERMAN` gives
  `Deutschland` and `DE` rather than `Uruguay` and `GL`.
- `StreetAddressGenerator.generateFullAddress()` ends with the locale's country rather than a random
  one, in every locale: with seed `42`, `en_US` gave `7389 River Park, Kansas City, NY 02649, United
  Kingdom` and now gives `7389 River Park, Renton, WA 98056, United States`, and `en_CA` now ends with
  `Canada` instead of `United Kingdom`. Its places come from a child stream named `postalPlace`. In a
  locale without places the rest of a full address keeps its seeded output, but the country no longer
  takes a draw, so with a caller-owned `Random`, which every child shares, the values drawn after a
  full address change too. The addresses of `SimpleProfile` and `UserProfile` follow.
- Update the build and test tooling, with no change for consumers: Error Prone to 2.52.0 and its Gradle
  plugin to 5.2.0, Spotless to 8.11.0, PIT to 1.30.2, Hibernate Validator to 9.1.6.Final, the Instancio
  benchmark competitor to 6.2.1, and in the CI and release workflows `actions/setup-java` to 6.2.0,
  `coursier/setup-action` to 3.1.0, and `softprops/action-gh-release` to 3.0.4.
- An `objectSubtype` mapping now applies to an interface or abstract root as well as to a member:
  `new ObjectGenerator<>(PaymentMethod.class, config)` generates the mapped implementation, where it failed
  as an unsupported root type. A concrete root is still generated as itself.
- In the Kotlin DSL, an abstract class or interface that is not sealed now fails as `Kotlin abstract type
  <name> requires a concrete type override`, and a sealed one with no subclass as `Sealed type <name> has
  no subclass to generate; register a type override for it`. Both failed as `Kotlin sealed and abstract
  types require a concrete type override`.
- JSON Schema export writes a `LocalDateTime` record member as `format: date-time-local`, and a
  `LocalTime` member and the `datetime.time` provider as `format: time-local`, where it wrote `date-time`
  and `time`. RFC 3339's `date-time` and `time` need an offset, which these values do not have, so a
  schema exported from such a type and parsed back, which now generates RFC 3339 values with an offset,
  gave values the type cannot hold. Both formats come from the
  [OpenAPI Format Registry](https://spec.openapis.org/registry/format/), which describes them as
  RFC 3339 date-time and time without the timezone component. `SchemaParser` now generates both in ISO
  local form, with the seconds, from the ranges of `date-time` and `time`. An `OffsetDateTime` or
  `ZonedDateTime` member is still `date-time`.

### Removed

- Breaking: `krandom-kotlin-dsl` and `krandom-kotest-extensions` now compile in Kotlin's explicit API mode,
  and the declarations that were public only by Kotlin's default, without being meant as API, are no longer
  public. `KotlinPrimaryConstructorAdapter`, the construction adapter that `ServiceLoader` finds, is
  `internal`. The constructors of `KrandomBuilder` and `ConfigScope` are internal, so a builder comes only
  from `krandom`, `krandomList`, and `krandomGenerator`, and a `ConfigScope` only from `config { }` and
  `krandomConfig`. `KrandomBuilder`'s empty companion object is private. Every other public declaration of
  both modules keeps its signature, now with its visibility stated.

### Fixed

- In `ObjectGenerationSemanticMode.RELAXED` and `ObjectGenerationSemanticMode.STRICT`, a `price` above an `amount`
  that the coherence pass lowered to a `balance` now follows the amount down, where its own constraints allow
  that. With a `price` of 80, an `amount` of 90, and a `balance` of 60 that `@Max(60)` or an override holds, the
  amount came down to 60 and the price stayed at 80, above it. A price that cannot follow, such as one with
  `@Min(70)`, keeps its value. Seeded output changes only for objects in which the price ended above the amount.
- In `ObjectGenerationSemanticMode.RELAXED` and `ObjectGenerationSemanticMode.STRICT`, the coherence pass no
  longer breaks `price <= amount <= balance` when it rounds a value into a field. A value that moves down to a
  sibling is now rounded down, and one that moves up is rounded up: to whole units in an integer field, where half
  up had turned an `int` price that followed an amount of 59.60 into 60, and to cents in any other, where a price
  that followed a `@Max(100)` amount of 59.6064 had become 59.61. Money is read exactly, and a `float` or a
  `double` as it prints, stepped to the next value down or up where the nearest one prints on the wrong side. A
  field whose type cannot hold the value keeps its own: a `byte` beside a price of 300 no longer wraps to 44, and
  a `float` beside 1E40 no longer turns infinite. A money string keeps every digit of its own amount when it
  takes the currency code. A `float` or a `double` money value that is infinite or NaN, as an override can give,
  now counts as no amount rather than fail generation with a `NumberFormatException`. Seeded with `42`, of 2,000 objects each, an `int` price beside an amount and a
  `@PositiveOrZero @Max(60)` balance broke the order in 1,466, an `int` price beside a `@PositiveOrZero @Max(100)`
  amount in 985 in `RELAXED` and 982 in `STRICT`, and a `BigDecimal` price beside that amount in 1,015 and 1,010,
  and now none does. Seeded output changes for objects with an integer money field or an amount of more than two
  decimals.
- In `ObjectGenerationSemanticMode.RELAXED`, the coherence pass now leaves a `@Fake` field alone, as it does a
  `@Randomizer` or `@FakeRange` field, and as the object-generation guide and the `@Fake` documentation say:
  `@Fake` takes precedence over field-name semantics, as it already did when the value was generated. Seeded
  with `42`, a `@Fake("fullName")` `fullName` beside `firstName` and `lastName` was overwritten with those
  names in 2,000 of 2,000 objects, and now keeps the name its provider gives. Seeded output changes for such
  objects. In `ObjectGenerationSemanticMode.STRICT` the pass still overwrites a `@Fake` field, as semantics
  win there.
- In `ObjectGenerationSemanticMode.STRICT`, the coherence pass no longer writes a derived string, amount,
  status, or active flag that breaks the field's own Bean Validation constraints, as it already did not for
  dates and ages. Seeded with `42` under a clock fixed at `2026-06-04T12:00Z`, of 2,000 objects each, it broke
  `@Size(max = 12)` on an `email` in 2,000, `@Size(min = 3, max = 3)` on a `fullName` in 2,000, `@Size(max = 10)`
  on a `url` in 2,000, `@Size(max = 5)` on a `city` in 1,849, `@Size(max = 6)` on a `status` in 280,
  `@PositiveOrZero @Max(100)` on an `amount` beside a `price` in 1,985, and `@AssertTrue` on an `active` flag
  beside a `status` held at `SUSPENDED` in 2,000, and now breaks none. Where a derived value would break a
  constraint, the field keeps its generated value, which meets it, and a sibling that depends on it follows it
  where it can: a `price` moves down to a bounded `amount`, an `amount` down to a capped `balance`, and `active`
  follows a `status` that cannot take `INACTIVE`. A unique field's numbered value must meet the constraints too,
  and one they reject is given back, so that it uses up no attempt. A `float` or a `double` is compared with each
  bound as Hibernate Validator compares it: with `@Min` and `@Max` in its own type, so that `16777220f` meets
  `@Max(16777219)`, and with `@DecimalMin` and `@DecimalMax` as the double it is, so that `0.1f` breaks
  `@DecimalMax("0.1")`. A derived value within the constraints is still written, so seeded output changes only
  for objects that broke one.
- `KRANDOM_JUNIT_CLASS_RECIPE` now replays what a `@MethodSource` factory generated. JUnit calls the
  factory with the context of the parameterized test as a whole, which `KrandomExtension` reported as a
  second class-scoped configuration under the test class's name. Beside a `@BeforeAll` entry, the
  printed replay command then failed with a configuration error for the duplicate entry; otherwise the
  factory took the test's recipe on replay, so the arguments of the failing invocation changed. The
  callbacks of a `@ParameterizedClass` invocation had the same fault. Each test class now reports one
  class-scoped configuration, and the replay commands restore it.
- In `ObjectGenerationSemanticMode.STRICT`, the coherence pass no longer moves a `createdAt`, `updatedAt`,
  `birthDate`, or `age` out of its own Bean Validation constraints, which `RELAXED` mode leaves alone. Seeded with `42` under a clock fixed at
  `2026-06-04T12:00Z`, it gave a `@Past` `updatedAt` a `createdAt` past the clock in 974 of 2,000 objects on a
  one-day `objectDateRange`, gave it the value of a `@Future` `createdAt` in all 2,000, and overwrote
  `@Min(30) @Max(40) age` with the age its `birthDate` gives in 1,709, and now does so in none. A field that a
  move would take out of its constraints cannot move, as an overridden one cannot: its sibling follows it, even
  out of `objectDateRange`, so `createdAt` moves back to a `@Past` `updatedAt`, and `birthDate` to a bounded
  `age`. Where the sibling's own constraints stop it too, both keep their values, as in `RELAXED` mode, so a
  `@Future` `createdAt` stays after a `@Past` `updatedAt`. A date or time is read against the configured clock
  as Hibernate Validator reads it. A move within the constraints still happens, so seeded output changes only for
  objects that broke one. The `STRICT` documentation now says that Bean Validation constraints take precedence
  when a value is generated, as they always have.
- An `age` that no `birthDate` of its type can hold, such as an overridden or constrained age of 2,147,483,647,
  or one of 500,000,000 beside a `Calendar`, `Date`, `java.sql.Date`, or `Timestamp`, which count milliseconds,
  now leaves `birthDate` as it is. Deriving the birth date failed generation with a `DateTimeException` or an
  `IllegalArgumentException`, or gave a `java.sql.Date` and, on JDK 21, a `Timestamp` a value that had wrapped
  round to a year millions of years ahead, which a `@Future` one then took in `STRICT` mode. A `java.sql.Date`
  also keeps its value where the age gives a year before 1, whose era `valueOf` drops, or a day that the JVM's
  zone or the 1582 change of calendar skipped, which `valueOf` moves: it took a date that read back as another,
  or, for `0000-02-29`, as none.
- A `birthDate` derived from a negative `age` under a clock on 29 February now gives that age. A year ahead it
  was 28 February, which gives one year less, and it is now 1 March. A positive age is unchanged.
- `age` now counts the years since the local date of an `OffsetDateTime`, `ZonedDateTime` or `Calendar`
  `birthDate`, in its own offset or zone, which is the date its generator draws and `objectDateRange` bounds.
  It counted from the UTC date, which is a day off near midnight far from UTC, so around a birthday the age was
  a year off: `2000-06-05T00:30+14:00` gave 26 on `2026-06-04`, the day before the 26th birthday, and now gives
  25. Seeded with `42` under a clock fixed at `2026-11-05T12:00Z`, 142 `OffsetDateTime`, 115 `ZonedDateTime` and
  95 `Calendar` birth dates in 200,000 of each had such an age, and none do now. Seeded output changes only for
  those objects.
- `new CalendarGenerator(min, max)` and `Generators.ofCalendar(min, max)`, with or without a configuration, no
  longer pass the last date of the range when a gap in the drawn zone's rules moves a value forward, as
  `new ZonedDateTimeGenerator(min, max)` and `Calendar` members already did not. `Pacific/Apia` and
  `Pacific/Fakaofo` skipped `2011-12-30`, so a one-day range on that date gave `2011-12-31` about once in 300
  draws, and a daylight saving gap in `America/Nuuk` moved 23:30 on `2024-03-30` to 00:30 on `2024-03-31`. Such
  a value is now the drawn date-time at UTC. Seeded output changes only for values that drew such a date-time.
- `@NotEmpty` and `@Size` on an array of a type they cannot constrain no longer fail generation at every
  depth, as `@NotEmpty Leaf[]`, `@Size(max = 4) byte[]` and `@NotEmpty int[]` did. Java places an
  annotation written before an array type on the array's component type as well as on the declaration,
  and the engine applied it to each element too. Hibernate Validator reads such a constraint for the
  array alone, and now so does the engine: an array's component type adds no constraints to its
  elements. So `@Size(min = 6, max = 6) String[] codes` holds six strings of the configured length,
  where each used to have six characters. A constraint on a type argument inside the component, as in
  `List<@Email String>[]`, still applies. Seeded output changes for such arrays.
- A `Queue<Leaf>` cut at `objectMaxDepth` no longer fails with a raw `NullPointerException`, reported at
  its parent's path as `Could not generate nested value at 'Top.mid'` with `REFLECTION` context. A queue
  cannot hold the `null` left for a cut nested object, so it now fails as any collection that rejects an
  element does: strict mode reports `COLLECTION_INSERTION` context at the queue's own path, such as
  `Top.mid.queue`, and `objectIgnoreErrors(true)` discards the whole queue and tells
  `generationFailureListener`. The same holds for a `Deque`, a `PriorityQueue`, and a `TreeSet`,
  `SortedSet` or `NavigableSet` of `Comparable` elements, and for an element cut by a cycle. A concrete
  queue, such as `ArrayDeque<Leaf>`, used to drop the `null` elements and could come back empty, and now
  fails the same way. A `LinkedList` is a list, and still holds them.
- A Bean Validation constraint on a field now applies to the constructor argument that fills it. An
  argument read only the constraints on its parameter, so `@Size(min = 3, max = 3) private final String
  code` filled through an unannotated `Account(String code)` got any string, and `@NotNull` on such a final
  field did not fail at `objectMaxDepth`. An argument now meets the constraints of the field it
  initializes, the nearest field with the parameter's name and declared type, and of that field's getter,
  wherever the parameter declares none of the same type, and the field's container-element constraints,
  such as `List<@Email String>`, wherever the parameter's type arguments declare none. As for by-name
  overrides, this needs parameter names, from `-parameters`. A mutable field that keeps the value its
  constructor set, as it does by default, now keeps a constrained one too. Record components already read
  their constraints. Seeded output changes for such arguments, and for the members after them where members
  share one stream.
- `SchemaParser` reads `minLength`, `maxLength`, `minItems`, `maxItems`, `minProperties`, and
  `maxProperties` exactly. It read the first four through `intValue`, so a count beyond an `int`
  wrapped: `maxItems` 3000000000 parsed and then failed every record with `Illegal Capacity:
  -1294967296`, a `minLength` that large was ignored, and a `const` was measured against the wrapped
  value. A generated string, array, or object now holds at most 1,000,000 characters, items, or
  members, the limit `RegexGenerator` keeps for a string: a minimum above it is rejected while parsing,
  naming the keyword, its value, and the schema path, and a larger maximum, even one beyond an `int`,
  draws no more than it, nor does a larger configured `stringLength` or `collectionSize` where the
  schema declares no maximum. An object that would hold more properties, declared or required, is
  rejected with its path, the root included. A count that is negative, fractional, or no number, which
  used to be truncated or ignored, is now rejected with the schema path, and a message writes a count
  beyond a `long`, such as `1E+1000000000`, in scientific notation. Seeded output changes only where a
  count or a configured size is above 1,000,000.
- `SchemaParser` writes `format: date-time` and `format: time` as RFC 3339 requires, with the seconds and
  an offset, such as `2041-07-03T05:12:44+05:30` and `05:12:44.123456789Z`. They came from
  `LocalDateTime` and `LocalTime`, whose text has no offset and can leave out `:00` seconds, so every
  value failed a validator that checks formats. Seeded output changes for every schema with either
  format.
- `SchemaParser` compares `const` and `enum` values as JSON values at every depth: a number by its
  value, whatever Java type holds it, an array item by item in order, and an object member by member
  under the name it is written with, which `properties` now also use, so the key `1` is the member
  `"1"`. Numbers inside objects and arrays were compared by their Java type, so a `const` of `{"id": 1}`
  beside the `enum` `[{"id": 1.0}]` was rejected as missing from it. It is accepted again, as in 2.9.0,
  and generates `{"id": 1}`. A value written as a string, such as a `LocalDate`, now matches its text.
  A `const` or `enum` that holds a `NaN` or infinity at any depth, or an object with two keys written
  under one name, such as `1` and `"1"`, is now rejected while parsing with the schema path. An `enum`
  member that the declared type left out was dropped unchecked, so `type: string` with the `enum`
  `["a", NaN]` was accepted. Seeded output changes only for an `enum` that now keeps a member it used to
  drop.
- An `@Email` field now takes a value from its `regexp` or a `@Pattern` only in the shape Hibernate
  Validator's `@Email` accepts for a dot-atom address: a local part of at most 64 characters with a dot
  only between two others, and a domain whose labels have a dash only inside them, at most 63
  characters a label and 255 in all once converted to ASCII. `a..b@example.com`, `a@example-.com` and
  `a@example.com.` used to be generated and failed validation. Such a candidate is now drawn again, and
  the field fails as any exhausted search does if none fits. As in Hibernate Validator, the domain no
  longer needs a dot, so `user@localhost` from a `@Pattern` is accepted. A quoted local part or an
  address literal such as `a@[192.0.2.1]`, which Hibernate Validator also accepts, is now refused.
  Seeded output is unchanged for the built-in `@Email` sources and changes only for a `@Pattern` or
  `@Email(regexp)` whose candidates the two shapes judge differently. Every address the generators
  build on their own already passed Hibernate Validator's `@Email`, in every supported locale and under
  each email-domain and network-safety policy, `.test` addresses included; a sweep of every route to
  one now checks it, and generated email output is unchanged.
- The `en_US` cities listed Gateshead, a town in England. Gary, Indiana, takes its place in the list, so
  a city drawn from that list as `Gateshead` for a seed is now `Gary`, and no other city moves. The list
  also serves `en_CA`, `en_IN`, and other English locales that fall back to it, such as `en_SG`, so
  their cities, and the addresses and payloads built on them, change in the same way: with seed `158`,
  an `en_CA` `AddressInfo` city was `Gateshead` and is now `Gary`.
- The implementation of an `objectSubtype` mapping for a generic member now takes the member's type
  arguments. With `objectSubtype(Box.class, ListBox.class)` and `record ListBox<T>(List<T> items) implements
  Box<T>`, a `Box<Integer>` member holds integers, where the unbound `T` failed as an unsupported type. The
  arguments reach the implementation through any generic supertypes in between, and are passed as declared,
  not checked. A type variable that has a bound took the bound instead, so a member whose bound the engine
  could generate changes too: with `record DateBox<D extends Date>(D value) implements Box<D>`, a
  `Box<Timestamp>` member now holds a `Timestamp`, where it held a plain `Date`, and its seeded output
  changes.
- A generic nested object reached through two or more inherited type variables now takes its type arguments.
  With `class Middle<V> extends Fixture<V>` and `class Root extends Middle<Box<String>>`, a `T value` field
  of `Fixture` is a `Box<String>` whose `C content` holds a `String`, where the engine read the arguments
  from `V` alone and the unbound `C` failed as an unsupported type. The same holds for a mapped or sealed
  subclass reached that way, and for a collection or map: with `class ListRoot extends Middle<List<String>>`,
  the same field is a `List<String>`, where it failed as an unsupported type. As for a mapping above, a bounded type variable took its bound, so such an
  object whose bound the engine could generate changes too. Such a member now also shares the cycle key of a
  member declared with its type, so a cycle through it is cut where one through that member is, and a
  recursive object reached that way can change.
- A recursive generic object, such as `class Node<T> { T value; Node<T> next; }` reached as `Node<String>`,
  no longer fails as an unsupported type. The cycle guard read `next` as `Node<T>`, another type than
  `Node<String>`, so the engine generated `next` and lost `T` inside it. The guard now reads what a type
  resolves to, as `ObjectPool` documents, so `next` is cut where it repeats `Node<String>`, as the cycle of a
  type without arguments is, and a recursive generic sealed type ends in a leaf as other sealed types do. With
  `objectIgnoreErrors(true)`, such an object kept a generated `next` whose members were `null`, and its `next`
  is now `null` itself.
- A recursive generic member that names its object's type variables, such as `Swap<B, A> swapped` inside a
  `Swap<A, B>` generated as `Swap<String, Integer>`, now keeps their arguments: its `first` is an `Integer`
  and its `second` a `String`. The engine read the member's arguments in the nested object, where `A` and `B`
  named each other, and failed as an unsupported type, or left them `null` with `objectIgnoreErrors(true)`. An
  argument that names the object's own type variable is now read as the object binds it.
- The Kotlin DSL now calls a private primary constructor, as the object engine calls a private Java
  constructor, where `kotlin.reflect.full.IllegalCallableAccessException` escaped without the object's path.
  A factory function that validates outside the constructor's `init` block is not called, so register a
  rule for such a type.
- A Bean Validation constraint on the getter of a Kotlin property of a value class type now applies when
  the engine fills the property's field. Kotlin mangles the name of such a getter, as in `getId-OLSiqns`,
  and the engine looked only for `getId`. Seeded output changes for such properties.

## [2.10.0] - 2026-11-10

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
  follow: a generated `domain`, `url`, or `companyUrl` is reserved, an email built from name fields takes
  a generated `domain` beside it, and a `companyName` derives a `.test` domain even with no email field.
  Seeded output changes for all of these and for values that depend on them. `REALISTIC_UNCLASSIFIED` restores the previous
  output draw for draw. A recipe records the policy in the new setting `network.safety-policy`, and
  replay requires it, so a recipe without it, such as one from an earlier version, is rejected.
- A recipe now records which unique fields were requested, in the new setting
  `object.requested-unique-fields`, as a replay must treat them the same way. Replay requires it and
  `object.unique-fields`, so a recipe without them, such as one from an earlier version, is rejected.
- Update Spring Boot to 4.1.2 (the `krandom-spring-boot-starter` dependency), the Gradle wrapper to
  9.8.2, Error Prone to 2.51.0, and NullAway to 0.14.3; align the consumer examples with Spring Boot
  4.1.2 and Maven Surefire 3.6.1.
- Update the build and test tooling, with no change for consumers: JaCoCo to 0.8.16, Palantir Java
  Format to 2.103.0, Logback to 1.6.6, and in CI `gradle/actions` to 6.4.1 and `graalvm/setup-graalvm`
  to 1.6.8.
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
  `domain` took that domain's generated value. A domain derived from a company name now ends in `.test`,
  and a generated email takes a sibling's domain only if it is reserved or set through an override, which
  is used verbatim. Getting `.com` back takes `REALISTIC_UNCLASSIFIED` for both the email and the network
  policy. Seeded output changes for objects with these fields.
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

[Unreleased]: https://github.com/frikit/krandom/compare/v3.1.0...HEAD
[3.1.0]: https://github.com/frikit/krandom/compare/v3.0.3...v3.1.0
[3.0.3]: https://github.com/frikit/krandom/compare/v3.0.2...v3.0.3
[3.0.2]: https://github.com/frikit/krandom/compare/v3.0.1...v3.0.2
[3.0.1]: https://github.com/frikit/krandom/compare/v3.0.0...v3.0.1
[3.0.0]: https://github.com/frikit/krandom/compare/v2.12.0...v3.0.0
[2.12.0]: https://github.com/frikit/krandom/compare/v2.11.0...v2.12.0
[2.11.0]: https://github.com/frikit/krandom/compare/v2.10.0...v2.11.0
[2.10.0]: https://github.com/frikit/krandom/compare/v2.9.0...v2.10.0
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
