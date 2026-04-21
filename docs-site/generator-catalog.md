---
layout: page
title: Generator Catalog
permalink: /generator-catalog/
---

# Generator Catalog (Java)

Main entrypoint: `org.github.krandom.generator.Generators`

## Base and numeric

- `ofByte`, `ofShort`, `ofInt`, `ofNaturalNumber`, `ofLong`, `ofFloat`, `ofDouble`
- `ofNormal`, `ofPrime`
- `ofBigDecimal`, `ofBigInteger`
- `ofBoolean`, `ofNullableBoolean`
- `ofDigit`, `ofNumberWithFormat`, `ofPyDecimal`
- `ofChar`, `ofString`, `ofTemplate`

## Algorithms and games

- `ofFibonacci`, `ofLuhn`
- `ofCoin`, `ofDice`

## Network and internet

- `ofIPv4`, `ofIPv6`, `ofIP`, `ofPort`, `ofMacAddress`
- `ofDomain`, `ofHostname`, `ofUrl`, `ofUri`, `ofSlug`, `ofUserAgent`
- `ofHttpMethod`, `ofHttpStatusCode`

## Date and time

- `ofLocalDate`, `ofLocalDateTime`, `ofInstant`, `ofZonedDateTime`
- `ofDuration`, `ofTimezone`

## Location

- `ofStreetAddress`, `ofAddressInfo`, `ofCity`, `ofState`, `ofPostalCode`, `ofCountry`, `ofPhoneNumber`

## User and identity

- `ofFullName`, `ofEmail`, `ofContactInfo`, `ofPersonInfo`, `ofCompanyEmail`
- `ofUsername`, `ofPassword`, `ofAvatarUrl`
- `ofSimpleProfile`, `ofProfile`
- `ofProfession`, `ofJobField`, `ofJobType`, `ofPosition`, `ofIndustry`
- `ofEducationalAttainment`, `ofMaritalStatus`
- `ofNationalId(locale)` and seeded overload

## Company and commerce

- `ofCompanyName`, `ofCompanyUrl`, `ofCompanyBuzzword`, `ofCompanyCatchPhrase`
- `ofCommerce`

## Finance and codes

- `ofCurrency`, `ofMoney`
- `ofCreditCard`, `ofCardExpiration`
- `ofBic`, `ofBban`, `ofIban`, `ofAbaRouting`, `ofBankCountry`, `ofBankAccount`, `ofBankName`, `ofBankType`
- `ofIsin`, `ofCusip`, `ofEin`, `ofCryptoAddress`
- `ofUuid`, `ofHash`, `ofIdentifierMask`, `ofEan`, `ofUpc`, `ofIsbn`

## Files and system

- `ofFileExtension`, `ofFileName`, `ofDirPath`, `ofFilePath`, `ofMimeType`, `ofSemver`
- `ofVersion`, `ofPlatformId`, `ofExceptionPayload`, `ofDatabase`

## Text

- `ofLoremIpsum`, `ofWord`, `ofSyllable`, `ofSentence`, `ofParagraph`, `ofText`

## Structured generation

- `ofField`, `ofSchema`
- `ofProviderHub`

## Selection helpers

- `pickFrom`, `pickSetFrom`, `shuffleOf`, `weighted`
- `unique`, `uniqueValues`, `repeat`

## Type-based lookup

- `forType(Class<T>)`

Supported built-ins include boxed and primitive Java scalar types, plus `String`.
