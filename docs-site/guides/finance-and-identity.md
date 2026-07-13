---
layout: page
title: Finance and Identity
permalink: /guides/finance-and-identity/
---

# Finance and Identity

## Synthetic data safety

kRandom generates synthetic test data only. Finance and identity generators intentionally produce
values that look realistic enough for validation tests, but they must not be used as real credentials,
payment instruments, or government identifiers.

- Credit card numbers preserve issuer shape and length but deliberately fail Luhn by default. A
  checksum-valid value is an explicit validator-fixture opt-in, not a processor sandbox credential.
- US locale-style phone numbers use NANPA's fictional `555-0100` through `555-0199` range by
  default. Other locales, custom masks, and MSISDN values remain unclassified.
- `PaymentInfo.instrumentReference()` is masked and exposes only a short tail reference.
- National IDs follow locale-specific fake formats where supported; they are not proof of identity.
- If your system can accidentally call real payment, credit, onboarding, or KYC services, keep kRandom
  output behind test-only configuration and never seed production workflows from it.

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

### Payment-card modes

Use the default for format and rejection-path fixtures. It is deliberately non-Luhn-valid. For an
isolated validator test that specifically needs a checksum-valid number, opt in explicitly:

```java
GeneratorConfig validatorConfig = GeneratorConfig.builder()
        .seed(42L)
        .paymentCardSafetyPolicy(PaymentCardSafetyPolicy.CHECKSUM_VALID)
        .build();

String validatorFixture = new CreditCardGenerator(validatorConfig).generateNumber();
assert CreditCardGenerator.isValidLuhn(validatorFixture);
```

`CHECKSUM_VALID` does not create a real account, a Stripe (or other processor) sandbox value, or
permission to call any payment, KYC, identity, or account-creation system. Use the processor's
documented sandbox credentials and test keys for external integration tests.

### Phone-number modes

The default policy uses the NANPA fictional range only when the locale is US. Keep the scope
explicit when older behavior is required:

```java
GeneratorConfig realisticPhoneConfig = GeneratorConfig.builder()
        .locale(Locale.US)
        .phoneNumberSafetyPolicy(PhoneNumberSafetyPolicy.REALISTIC_UNCLASSIFIED)
        .build();

String unclassifiedPhone = new PhoneNumberGenerator(realisticPhoneConfig).generate();
```

This opt-in is realistic-looking but makes no non-routability claim. Custom phone masks and MSISDN
values are also unclassified.

## Identity snippets

```java
FullNameGenerator names = Generators.ofFullName();
String full = names.generate();
String email = Generators.ofEmail().generate();
String username = Generators.ofUsername().generate();
String nationalId = Generators.ofNationalId(Locale.US).generate();
```

National ID support is locale-aware through `Generators.ofNationalId(locale)`. Unsupported locales fail
fast instead of silently generating a misleading identifier.
