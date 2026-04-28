---
layout: page
title: Generator Catalog
permalink: /generator-catalog/
---

# Generator Catalog (Java)

Main entrypoint: `io.github.frikit.krandom.generator.Generators`

## Base and numeric

- `ofByte`, `ofShort`, `ofInt`, `ofNaturalNumber`, `ofLong`, `ofFloat`, `ofDouble`
- `ofNormal`, `ofPrime`
- `ofBigDecimal`, `ofBigInteger`
- `ofBoolean`, `ofNullableBoolean`
- `ofDigit`, `ofNumberWithFormat`, `ofPyDecimal`
- `ofChar`, `ofString`, `ofTemplate`, `ofProviderTemplate`
- `constant`, `ofConstant`

## Algorithms and games

- `ofFibonacci`, `ofLuhn`
- `ofCoin`, `ofDice`

## Network and internet

- `ofIPv4`, `ofIPv6`, `ofIP`, `ofPort`, `ofMacAddress`
- `ofDomain`, `ofHostname`, `ofUrl`, `ofUri`, `ofSlug`, `ofUserAgent`
- `ofHttpMethod`, `ofHttpStatusCode`

## Date and time

- `ofLocalDate`, `ofLocalDateTime`, `ofInstant`, `ofZonedDateTime`
- `ofDuration`, `ofTimezone`, `ofCalendar`
- `ofLocale`

## Location

- `ofStreetAddress`, `ofAddressInfo`, `ofCity`, `ofState`, `ofPostalCode`, `ofCountry`, `ofPhoneNumber`, `ofGeohash`

## User and identity

- `ofFullName`, `ofEmail`, `ofContactInfo`, `ofJobInfo`, `ofPersonInfo`, `ofCompanyEmail`, `ofCompanyInfo`
- `ofUsername`, `ofPassword`, `ofAvatarUrl`
- `ofSimpleProfile`, `ofProfile`
- `ofProfession`, `ofJobField`, `ofJobType`, `ofPosition`, `ofIndustry`
- `ofEducationalAttainment`, `ofMaritalStatus`
- `ofNationalId(locale)` and seeded overload

## Company and commerce

- `ofCompanyName`, `ofCompanyUrl`, `ofCompanyBuzzword`, `ofCompanyCatchPhrase`
- `ofCommerce`, `ofProductInfo`, `ofOrderInfo`, `ofShipmentInfo`

## Finance and codes

- `ofCurrency`, `ofMoney`
- `ofCreditCard`, `ofCreditCardInfo`, `ofCardExpiration`, `ofInvoiceInfo`, `ofPaymentInfo`
- `ofBic`, `ofBban`, `ofIban`, `ofAbaRouting`, `ofBankCountry`, `ofBankAccount`, `ofBankInfo`, `ofBankName`, `ofBankType`
- `ofIsin`, `ofCusip`, `ofEin`, `ofCryptoAddress`
- `ofUuid`, `ofHash`, `ofIdentifierMask`, `ofEan`, `ofUpc`, `ofIsbn`

## Files and system

- `ofFileExtension`, `ofFileName`, `ofDirPath`, `ofFilePath`, `ofMimeType`, `ofSemver`
- `ofVersion`, `ofPlatformId`, `ofExceptionPayload`, `ofDatabase`

## Text

- `ofLoremIpsum`, `ofWord`, `ofSyllable`, `ofSentence`, `ofParagraph`, `ofText`
- `ofTemplate`, `ofProviderTemplate`

## Structured generation

- `ofField`, `ofSchema`
- `ofProviderHub`

## Selection helpers

- `pickFrom`, `pickSetFrom`, `shuffleOf`, `weighted`
- `unique`, `uniqueValues`, `repeat`

## Type-based lookup

- `forType(Class<T>)`

Supported built-ins include boxed and primitive Java scalar types, plus `String`, `Calendar`,
`GregorianCalendar`, and `Locale`.
