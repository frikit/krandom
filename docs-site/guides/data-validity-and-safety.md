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
| `STRIPE_SANDBOX` | Stripe's published interactive test-card number for the selected supported card type | Stripe sandbox tests with Stripe test API keys only |
| `CHECKSUM_VALID` | Issuer-shaped and Luhn-valid; not a sandbox credential | Isolated validator fixtures only |

The selected policy is included in a portable generation recipe. `CHECKSUM_VALID` never means
that a number is real, processor-approved, or safe to send externally. `STRIPE_SANDBOX` is not
portable to another processor and must never be used with live Stripe keys. Use the payment
processor's documented sandbox values and test credentials for integration tests.

## Phone-number contract

`PhoneNumberSafetyPolicy.TEST_SAFE_WHERE_AVAILABLE` is the default for locale-style output. It
uses [NANPA's fictional, non-working `555-0100` through `555-0199` line-number range](https://www.nanpa.com/numbering/555-line-numbers) for US
locales, including the language-only English fallback. Other locales remain realistic but unclassified, as do custom phone-number
templates and generated MSISDNs. `REALISTIC_UNCLASSIFIED` explicitly preserves the prior
realistic-looking behavior without a safety claim.

The NANPA allocation does not make the same range safe in countries outside its numbering plan.
Use a country-specific official test allocation only when the generator documents it.

## Fail-closed finance and identity contracts

The following output families are `DISABLED` by default. Calling `generate()` without the matching
explicit policy fails fast instead of silently producing a plausible external identifier.

| Family | `GeneratorConfig` policy | Available explicit mode |
|:---|:---|:---|
| Banking: ABA routing, account, BBAN, BIC, IBAN, and bank payloads | `bankingSafetyPolicy` | `REALISTIC_UNCLASSIFIED` |
| National IDs and CPF | `nationalIdSafetyPolicy` | `REALISTIC_UNCLASSIFIED` |
| Passport and driving license | `identityDocumentSafetyPolicy` | `REALISTIC_UNCLASSIFIED` |
| CNPJ and EIN | `businessTaxIdentifierSafetyPolicy` | `REALISTIC_UNCLASSIFIED` |
| Cryptocurrency destination addresses | `cryptoAddressSafetyPolicy` | `REALISTIC_UNCLASSIFIED` |
| ISIN and CUSIP | `securitiesIdentifierSafetyPolicy` | `REALISTIC_UNCLASSIFIED` |

`REALISTIC_UNCLASSIFIED` supports isolated compatibility fixtures only. It is not a claim of
non-routability, unassigned status, or safety for a production system. Phone numbers remain
separately classified by `PhoneNumberSafetyPolicy`; the default is test-safe only where the
generator documents an official allocation.

Keep generated personal-looking values out of logs and failure reports, and use the external
provider's official sandbox credentials, test instruments, or testnet for every integration test.
