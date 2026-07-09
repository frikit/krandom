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

## Current 1.x rule

- Treat generated finance and identity values as local test data only.
- Check the individual generator's Javadoc/tests before relying on checksum validity.
- Do not claim a value is test-safe unless its generator explicitly documents an official test/non-routable range.
- Keep generated personal-looking values out of logs and failure reports.

The v2 plan introduces explicit validity/safety metadata and safer defaults. Until that work lands, isolate these fixtures inside tests and use official sandbox credentials for external integrations.
