# Release Runbook

End-to-end process for publishing a kRandom version to Maven Central via
the Central Portal (<https://central.sonatype.com>).

## One-time setup

### 1. Central Portal namespace

`io.github.frikit` must be claimed in the Central Portal under the
publisher's account. Verify at <https://central.sonatype.com> → "Namespaces".

### 2. GitHub repository secrets

The release workflow (`.github/workflows/release-maven-central.yml`) needs
four repository secrets:

| Secret | Source | Notes |
| --- | --- | --- |
| `CENTRAL_PORTAL_USERNAME` | Central Portal → Account → "Generate User Token" — the **username** field of the resulting token | Short alphanumeric, looks like `VfPXaR` |
| `CENTRAL_PORTAL_PASSWORD` | Same token's **password** field | Long alphanumeric |
| `GPG_SIGNING_KEY` | ASCII-armored GPG private key (`gpg --armor --export-secret-key <key-id>`) | Multi-line; paste verbatim |
| `GPG_SIGNING_PASSWORD` | Passphrase for the GPG key above | |

The GPG public key must be uploaded to a public keyserver (for example
`hkps://keys.openpgp.org`) before Sonatype validation will pass.

## Cutting a release

1. **Decide the version.** SemVer; for example `1.0.0`. Confirm the
   `[Unreleased]` section in `CHANGELOG.md` is final.
2. **Land any last commits on `main`.** Run `./scripts/pre_commit_check.sh`
   and `./scripts/verify_examples_local.sh` locally; both must pass.
3. **Cut the CHANGELOG section.** Promote `[Unreleased]` to
   `[<version>] - <YYYY-MM-DD>`, add a fresh empty `[Unreleased]` heading,
   update the diff link at the bottom, and commit.
4. **Trigger the workflow.**
   GitHub UI → "Actions" → "release-maven-central" → "Run workflow":
   - `version`: the SemVer version, no `v` prefix.
   - `publishingType`: `USER_MANAGED` for the first release (gives you a
     manual gate in the Central Portal UI). Switch to `AUTOMATIC` once you
     trust the pipeline.
5. **Watch the workflow.**
   - "Build, test, and check" — full clean + tests + coverage gate and
     validated per-module CycloneDX 1.6 SBOMs
     (`-x :benchmarks:test -x :benchmarks:check`).
   - "Publish to Maven Central (Central Portal)" — runs
     `./gradlew publishAggregationToCentralPortal`. The
     `com.gradleup.nmcp.settings` plugin assembles a signed bundle and
     uploads it via the Central Portal API.
   - "Create GitHub Release" — tags `v<version>`, attaches per-module
     jars and JSON/XML SBOMs, and writes auto-generated release notes.
6. **(USER_MANAGED only) Release in the portal.**
   <https://central.sonatype.com> → "Deployments" → find the upload →
   "Publish". Validation runs first; if it fails, fix and re-upload.
   Releases are immutable once published.

## Post-release

- Verify on Maven Central: `https://repo1.maven.org/maven2/io/github/frikit/krandom-core/<version>/`
  (allow up to ~30 minutes for index propagation).
- Download each `krandom-<module>.cdx.json` release asset and confirm its
  metadata component version matches the release tag.
- Update README install snippets to the new version if you keep concrete
  versions there (currently we use `<version>` placeholders).
- Bump the in-repo version back to a `*-SNAPSHOT` for ongoing development
  if appropriate.

## Troubleshooting

- **"Invalid credentials" on upload** — regenerate the user token in
  Central Portal; it shows only once.
- **"Signature validation failed"** — confirm the GPG public key is on a
  public keyserver and the private key in `GPG_SIGNING_KEY` matches.
- **"namespace not claimed"** — finish namespace verification at
  <https://central.sonatype.com> → "Namespaces" before retrying.
- **POM validation errors** — Central Portal requires `name`,
  `description`, `url`, `licenses`, `developers`, and `scm`; all are wired
  in `build.gradle.kts`. Each module also has a distinct `<description>`.
