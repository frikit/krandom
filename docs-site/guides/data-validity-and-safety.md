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
draws from ranges that the numbering authority reserves for fiction and never assigns to a
subscriber, for the two numbering plans whose fiction range kRandom implements:

- North American Numbering Plan locales, which resolve to the United States or Canada (`en_US`,
  `en_CA`, `fr_CA`, and the language-only English fallback), use
  [NANPA's fictional, non-working `555-0100` through `555-0199` line-number range](https://www.nanpa.com/numbering/555-line-numbers).
- United Kingdom locales (`en_GB`, and any other locale whose country is `GB`) use
  [Ofcom's telephone numbers for TV and radio drama](https://www.ofcom.org.uk/phones-and-broadband/phone-numbers/numbers-for-drama):
  mobile numbers `07700 900000` to `07700 900999`, and landline numbers from the 16 geographic
  blocks below. Ofcom sets these numbers aside for drama and does not allocate them to
  communications providers for their customers, and says it will not in the foreseeable future.

| Area | Landline drama range |
|:---|:---|
| Leeds | `0113 496 0000` to `0113 496 0999` |
| Sheffield | `0114 496 0000` to `0114 496 0999` |
| Nottingham | `0115 496 0000` to `0115 496 0999` |
| Leicester | `0116 496 0000` to `0116 496 0999` |
| Bristol | `0117 496 0000` to `0117 496 0999` |
| Reading | `0118 496 0000` to `0118 496 0999` |
| Birmingham | `0121 496 0000` to `0121 496 0999` |
| Edinburgh | `0131 496 0000` to `0131 496 0999` |
| Glasgow | `0141 496 0000` to `0141 496 0999` |
| Liverpool | `0151 496 0000` to `0151 496 0999` |
| Manchester | `0161 496 0000` to `0161 496 0999` |
| London | `020 7946 0000` to `020 7946 0999` |
| Tyneside, Durham and Sunderland | `0191 498 0000` to `0191 498 0999` |
| Northern Ireland | `028 9649 6000` to `028 9649 6999` |
| Cardiff | `029 2018 0000` to `029 2018 0999` |
| No area | `01632 960000` to `01632 960999` |

Every other `SupportedLocale` country uses its own national format; other locales remain realistic
but unclassified, as do custom phone-number templates and generated MSISDNs. A locale whose country
has no built-in format uses the first catalog country of its language (for example `de_LI` uses the
German format), and an unknown language uses `en_US`. `REALISTIC_UNCLASSIFIED` explicitly preserves
the prior realistic-looking behavior, including realistic NANP exchanges and UK area codes and
mobile prefixes, without a safety claim.

Neither allocation makes the same digits safe in another country's numbering plan: NANPA's range
is fictional only within the NANP, and Ofcom's ranges only within the United Kingdom. Use a
country-specific official test allocation only when the generator documents it.

## Email-domain contract

`EmailDomainPolicy.TEST_SAFE_RESERVED_DOMAINS` is the default for every domain a generator chooses
on its own. Personal addresses from `EmailGenerator` (and the contact, person, and profile payloads
built on it) use only the [RFC 2606](https://www.rfc-editor.org/rfc/rfc2606) reserved domains
`example.com`, `example.net`, and `example.org`; company addresses from `CompanyEmailGenerator`
keep the company-derived label under the reserved `.test` top-level domain (for example
`jsmith@acme.test`). None of these can reach a real mailbox. Local parts are always lowercase ASCII:
Latin-script names lose their diacritics (German umlauts expand to `ae`/`oe`/`ue`), Cyrillic and
Greek names are transliterated, and names in other scripts are replaced by romanized names for the
locale's language.

The same rule holds when an object has related fields. A generated email takes the domain of a
sibling such as `domain` or `url` only if that domain is reserved or was set through an override, and
a domain derived from `companyName` ends in `.test` (`hello@acmelabs.test`). An `@Email` field uses
`example.com`, `example.net` or `example.org`, or a `.test` domain when its `@Size` rules those out.
The policy covers only domains a generator chooses: values from your own overrides, rules and
randomizers, and domains in an `@Email` `regexp` or a `@Pattern`, are used as written.

`EmailDomainPolicy.REALISTIC_UNCLASSIFIED` restores the popular mailbox-provider domains
(`gmail.com`, `yahoo.com`, …) and realistic company top-level domains for isolated fixtures that
never send mail. The free-provider methods (`generateFreeEmail`, `getFreeEmailProvider`,
`generateFreeEmailDomain`) require that opt-in and fail closed otherwise. A domain passed
explicitly, such as `generate("corp.test")`, is always used verbatim. The selected policy is
recorded in portable generation recipes as `email.domain-policy`.

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
