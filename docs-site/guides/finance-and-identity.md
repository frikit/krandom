---
layout: page
title: Finance and Identity
permalink: /guides/finance-and-identity/
---

# Finance and Identity

## Synthetic-data safety

kRandom produces test fixtures, never real credentials, payment instruments, or government
identifiers. Format or checksum validity is not a claim that a value is safe for an external
system. Keep generated values inside test-only boundaries and use the external system's documented
sandbox credentials for payment, onboarding, KYC, account-creation, trading, or blockchain tests.

- Card numbers are issuer-shaped but deliberately fail Luhn by default.
- US locale-style phone numbers use NANPA's fictional `555-0100` through `555-0199` range by
  default. Other locales, custom masks, and MSISDN values are unclassified.
- Banking, national-ID, identity-document, business-tax, crypto-address, and securities-identifier
  generators fail closed by default. They require an explicit compatibility policy before they can
  generate realistic-looking values.

## Safe-by-default finance fixtures

The following generators work with the default configuration:

```java
CreditCardGenerator cards = Generators.ofCreditCard();
String number = cards.generateNumber();       // issuer-shaped; deliberately not Luhn-valid
String cvv = cards.generateCvv();
String exp = Generators.ofCardExpiration().generate();
String money = Generators.ofMoney().generate();
String currency = Generators.ofCurrency().generate();
```

`PaymentInfoGenerator` also works by default. When its payment method is bank-based, the
instrument reference is an opaque `ACCT-TEST-####` value rather than an account identifier.

## Payment-card modes

Use the default for formatting and rejection-path tests. Select `CHECKSUM_VALID` only for an
isolated validator fixture:

```java
GeneratorConfig validatorConfig = GeneratorConfig.builder()
        .seed(42L)
        .paymentCardSafetyPolicy(PaymentCardSafetyPolicy.CHECKSUM_VALID)
        .build();

String validatorFixture = new CreditCardGenerator(validatorConfig).generateNumber();
assert CreditCardGenerator.isValidLuhn(validatorFixture);
```

For a Stripe integration test, use the dedicated Stripe policy with Stripe *test* API keys only:

```java
GeneratorConfig stripeConfig = GeneratorConfig.builder()
        .paymentCardSafetyPolicy(PaymentCardSafetyPolicy.STRIPE_SANDBOX)
        .build();

String stripeTestNumber = new CreditCardGenerator(stripeConfig).generateNumber();
```

`STRIPE_SANDBOX` maps kRandom card types to Stripe's published interactive test-card values. It is
not portable to another processor. For server-side Stripe tests, prefer Stripe's named
`PaymentMethod` values where their documentation recommends them. Neither `CHECKSUM_VALID` nor
`STRIPE_SANDBOX` is safe for a live payment API.

## Banking and other fail-closed identifiers

Select the matching policy explicitly only when an isolated fixture needs realistic-looking output:

```java
GeneratorConfig bankingConfig = GeneratorConfig.builder()
        .locale(Locale.US)
        .seed(42L)
        .bankingSafetyPolicy(BankingSafetyPolicy.REALISTIC_UNCLASSIFIED)
        .build();

String iban = new IbanGenerator(bankingConfig).generate();
String bic = new BicGenerator(bankingConfig).generate();
```

`REALISTIC_UNCLASSIFIED` makes no non-routability or real-world safety claim. The same opt-in
pattern applies to `businessTaxIdentifierSafetyPolicy`, `cryptoAddressSafetyPolicy`,
`securitiesIdentifierSafetyPolicy`, and `identityDocumentSafetyPolicy`. Use a provider's own
sandbox/testnet values when a test crosses a system boundary.

## Identity fixtures

Names, emails, and usernames are ordinary fixture data:

```java
FullNameGenerator names = Generators.ofFullName();
String full = names.generate();
String email = Generators.ofEmail().generate();
String username = Generators.ofUsername().generate();
```

National IDs require their own explicit policy:

```java
GeneratorConfig nationalIdConfig = GeneratorConfig.builder()
        .locale(Locale.US)
        .seed(42L)
        .nationalIdSafetyPolicy(NationalIdSafetyPolicy.REALISTIC_UNCLASSIFIED)
        .build();

String nationalId = Generators.ofNationalId(nationalIdConfig).generate();
```

National-ID support is locale-aware; unsupported locales fail fast. A realistic-looking national
ID must not be used for identity verification, KYC, account creation, or any production workflow.
For the complete policy table, see
[Data Validity and Safety]({{ '/guides/data-validity-and-safety/' | relative_url }}).
