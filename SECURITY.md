# Security Policy

## Supported Versions

Security fixes are issued for the latest released minor line. Older minor
versions are best-effort.

| Version | Supported |
| ------- | --------- |
| latest minor (`x.y.*`) | Yes |
| previous minor (`x.(y-1).*`) | Best effort |
| earlier | No |

## Reporting a Vulnerability

Please **do not open a public GitHub issue** for security reports.

1. Open a private report through GitHub's
   [Security Advisories](https://github.com/frikit/krandom/security/advisories/new)
   form (preferred — it scopes the discussion and lets us coordinate a CVE if
   needed).
2. If GitHub Security Advisories are not available to you, email
   <ofrikit94@gmail.com> with the subject prefix `[krandom security]` and a
   clear description of the issue plus steps to reproduce.

You will get an acknowledgement within **5 business days**. Expect updates
on the triage outcome and target fix window within **15 business days** of
acknowledgement.

## Disclosure Process

- We work with the reporter on a fix in a private fork.
- A CVE is requested when impact warrants one.
- The fix is released, the advisory is published, and the reporter is
  credited (unless they prefer to remain anonymous).

## Scope

In scope:

- Code and metadata in this repository's published modules (`krandom-bom`, `krandom-core`,
  `krandom-jackson`, `krandom-junit`, `krandom-spring-boot-starter`,
  `krandom-kotest-extensions`, `krandom-kotlin-dsl`).
- Build and release tooling that ships in the repository
  (`scripts/`, `.github/workflows/`).

Out of scope:

- Third-party dependencies (please report upstream).
- Issues that require attacker-controlled input fed directly into reflective
  generators with a deliberately hostile classloader — kRandom is a
  test/fixture library and is not intended as a sandboxed runtime.
