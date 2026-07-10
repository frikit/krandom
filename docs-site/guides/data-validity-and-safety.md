---
layout: page
title: Data Validity and Safety
permalink: /guides/data-validity-and-safety/
---

# Data Validity and Safety

Generated values are test fixtures, not production identities or credentials. Use these terms precisely when selecting or documenting a generator:

| Term | Meaning |
|:---|:---|
| **Format-valid** | Matches the documented shape, alphabet, length, or regular expression |
| **Checksum-valid** | Also satisfies a published check-digit/checksum algorithm |
| **Semantically plausible** | Related fields and ranges look realistic for their stated domain |
| **Test-safe / non-routable** | Uses an official test range/value or a deliberately non-routable space |

These levels are not interchangeable. A checksum-valid credit-card number, IBAN, or national ID is not automatically test-safe and must not be sent to payment processors, identity systems, KYC services, account-creation endpoints, or other external production systems.

## Payment-card contract

`CreditCardGenerator` and `CreditCardInfoGenerator` use the typed
`PaymentCardSafetyPolicy` in `GeneratorConfig`:

| Policy | Properties | Intended use |
|:---|:---|:---|
| `TEST_SAFE_NON_ROUTABLE` (default) | Issuer-shaped and format-valid; deliberately fails Luhn | Formatting, UI, and validation-rejection fixtures |
| `CHECKSUM_VALID` | Issuer-shaped and Luhn-valid; not a sandbox credential | Isolated validator fixtures only |

The selected policy is included in a portable generation recipe. `CHECKSUM_VALID` never means
that a number is real, processor-approved, or safe to send externally. Use the payment processor's
documented sandbox values and test credentials for integration tests.

## Phone-number contract

`PhoneNumberSafetyPolicy.TEST_SAFE_WHERE_AVAILABLE` is the default for locale-style output. It
uses [NANPA's fictional, non-working `555-0100` through `555-0199` line-number range](https://www.nanpa.com/numbering/555-line-numbers) for US
locales, including the language-only English fallback. Other locales remain realistic but unclassified, as do custom phone-number
templates and generated MSISDNs. `REALISTIC_UNCLASSIFIED` explicitly preserves the prior
realistic-looking behavior without a safety claim.

The NANPA allocation does not make the same range safe in countries outside its numbering plan.
Use a country-specific official test allocation only when the generator documents it.

## Other finance and identity generators

IBAN, bank-account, phone, crypto-address, and national-ID generators remain individually
classified. Do not infer test safety from format or checksum validity. Keep generated
personal-looking values out of logs and failure reports, and use official sandbox credentials for
all external integrations.
