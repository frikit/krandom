---
layout: page
title: Finance and Identity
permalink: /guides/finance-and-identity/
---

# Finance and Identity

## Finance snippets

```java
CreditCardGenerator cards = Generators.ofCreditCard();
String number = cards.generateNumber();
String cvv = cards.generateCvv();
String exp = Generators.ofCardExpiration().generate();

String iban = Generators.ofIban().generate();
String bic = Generators.ofBic().generate();
String isin = Generators.ofIsin().generate();
String cusip = Generators.ofCusip().generate();
```

## Identity snippets

```java
FullNameGenerator names = Generators.ofFullName();
String full = names.generate();
String email = Generators.ofEmail().generate();
String username = Generators.ofUsername().generate();
String nationalId = Generators.ofNationalId(Locale.US).generate();
```
