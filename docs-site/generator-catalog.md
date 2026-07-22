---
layout: page
title: Generator Catalog
permalink: /generator-catalog/
---

# Generator Catalog (Java)

The main entry point is `io.github.frikit.krandom.generator.Generators`. Most
locale-aware factories accept a `Locale` or `GeneratorConfig`; use a shared
`GeneratorConfig` when a fixture needs common locale, seed, clock, safety, or
registry settings. Factories with a `long` overload provide a locally seeded
generator where shown by the API.

## Base, numeric, and algorithms

- `ofConstant`, `ofBoolean`, `ofNullableBoolean`, `ofChar`, `ofString`, `ofDigit`,
  `ofNumber`, `ofNumberWithFormat`, `ofRegex`, `ofPyDecimal`
- `ofByte`, `ofShort`, `ofInt`, `ofNaturalNumber`, `ofLong`, `ofFloat`, `ofDouble`,
  `ofAtomicInteger`, `ofAtomicLong`, `ofBigDecimal`, `ofBigInteger`
- `ofNormal`, `ofPrime`, `ofFibonacci`, `ofLuhn`, `ofCoin`, `ofDice`

## Network and internet

- `ofIPv4`, `ofIPv6`, `ofIP`, `ofPort`, `ofMacAddress`
- `ofDomain`, `ofHostname`, `ofUrl` (text URL), `ofUri` (text URI), `ofURI`
  (`URI` object), `ofURL` (`URL` object), `ofSlug`, `ofUserAgent`. Lowercase `Url`/`Uri`
  names generate text; uppercase `URL`/`URI` names generate the corresponding JDK object.
- `ofHttpMethod`, `ofHttpStatusCode`

## Date, time, and locale

- `ofLocalDate`, `ofLocalTime`, `ofLocalDateTime`, `ofInstant`, `ofZonedDateTime`,
  `ofOffsetDateTime`, `ofOffsetTime`
- `ofDuration`, `ofPeriod`, `ofYear`, `ofYearMonth`, `ofMonthDay`, `ofZoneId`,
  `ofZoneOffset`, `ofTimeZone`, `ofTimezone`, `ofCalendar`
- `ofUtilDate`, `ofSqlDate`, `ofSqlTime`, `ofSqlTimestamp`, `ofLocale`

## People, location, and profiles

- `ofFullName`, `ofMiddleName`, `ofEmail`, `ofUsername`, `ofPassword`,
  `ofAvatarUrl`, `ofContactInfo`, `ofSimpleProfile`, `ofProfile`
- `ofStreetAddress`, `ofAddressInfo`, `ofCity`, `ofState`, `ofPostalCode`,
  `ofCountry`, `ofPhoneNumber`, `ofGeohash`
- `ofCompanyEmail`, `ofCompanyInfo`, `ofJobInfo`, `ofPersonInfo`, `ofSocialHandle`,
  `ofSocialProfile`
- `ofProfession`, `ofIndustry`, `ofJobField`, `ofJobType`, `ofSeniority`,
  `ofPosition`, `ofEducationalAttainment`, `ofMaritalStatus`, `ofBloodType`,
  `ofPronoun`, `ofNationality`
- `ofZodiac`, `ofChineseZodiac`, `ofNatoPhonetic`, `ofHobby`,
  `ofProgrammingLanguage`, `ofMbti`

## Company, commerce, vehicle, and domain data

- `ofCompanyName`, `ofCompanyUrl`, `ofCompanyBuzzword`, `ofCompanyCatchPhrase`
- `ofCommerce`, `ofProductInfo`, `ofOrderInfo`, `ofShipmentInfo`
- `ofVin`, `ofVehicle`, `ofWeather`, `ofMeasurement`, `ofFinancialTerm`,
  `ofRestaurantType`
- `ofAws`, `ofAzure`, `ofComputer`, `ofColor`

## Finance, identity, and identifiers

- Safe-by-default finance: `ofCurrency`, `ofCurrencyPair`, `ofMoney`,
  `ofCreditCard`, `ofCreditCardInfo`, `ofCardExpiration`, `ofInvoiceInfo`,
  `ofPaymentInfo`
- Banking (explicit policy required before generation): `ofBic`, `ofBban`,
  `ofIban`, `ofAbaRouting`, `ofBankAccount`, `ofBankInfo`; `ofBankCountry`,
  `ofBankName`, and `ofBankType` provide descriptive bank data
- Other fail-closed generators (explicit policy required): `ofIsin`, `ofCusip`,
  `ofEin`, `ofCnpj`, `ofCpf`, `ofCryptoAddress`, `ofNationalId`, `ofPassport`,
  `ofDrivingLicense`
- General identifiers: `ofUuid` (the canonical UUID factory; no redundant `ofUUID` alias),
  `ofHash`, `ofIdentifierMask`, `ofEan`, `ofUpc`,
  `ofIsbn`

`PaymentCardSafetyPolicy.TEST_SAFE_NON_ROUTABLE` is the default for cards.
Banking, securities, business-tax, crypto-address, national-ID, and identity-document output is
disabled by default. Select the corresponding `GeneratorConfig` safety policy only for isolated
fixtures; see [Finance and Identity]({{ '/guides/finance-and-identity/' | relative_url }}) and
[Data Validity and Safety]({{ '/guides/data-validity-and-safety/' | relative_url }}).

## Files, text, templates, and system data

- `ofFileExtension`, `ofFileName`, `ofDirPath`, `ofFilePath`, `ofMimeType`,
  `ofSemver`, `ofVersion`, `ofPlatformId`, `ofDatabase`, `ofExceptionPayload`
- `ofLoremIpsum`, `ofWord`, `ofSyllable`, `ofSentence`, `ofParagraph`, `ofText`
- `ofTemplate`, `ofProviderTemplate`, `ofDataFakerExpression`

## Objects, schemas, and providers

- `ofObject` generates a type; `ofObjectFaker` adds targeted rules and exclusions.
- `ofField` resolves provider keys; `ofSchema` generates batches and exports them.
- `ofProviderHub` exposes named provider lookup and aliases.

## Selection, reuse, and concurrency helpers

- `pick`, `pickSet`, `shuffle`, `weighted`, `unique`, `repeat`
- `threadLocal` supplies one generator instance per calling thread.

The removed 1.x aliases (`constant`, `pickFrom`, `pickSetFrom`, `shuffleOf`, and
`uniqueValues`) are not available in 2.0.0. See the
[1.x-to-2.0.0 migration guide](https://github.com/frikit/krandom/blob/main/docs/migration/v1.6-to-v2.md)
for exact replacements.

## Domain namespaces

For discoverability, the same families are grouped under `person`, `finance`, `location`,
`network`, `text`, `commerce`, `identifier`, and `datetime`. Each namespace accepts an optional
`GeneratorConfig`.

## Type-based lookup

`forType(Class<T>)` supplies built-ins for primitive/wrapper scalars, `String`, `Number`, big
numbers, atomics, UUID, `Locale`, `URI`/`URL`, legacy date/time types, and common `java.time`
types. It throws `IllegalArgumentException` for unsupported classes; use an explicit generator or
an object override for those types.
