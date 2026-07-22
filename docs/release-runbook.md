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

1. **Decide the version.** SemVer; for example `2.1.0`. Confirm the
   `[Unreleased]` section in `CHANGELOG.md` is final.
2. **Land any last commits on `main`.** Run `./scripts/pre_commit_check.sh`
   and `./scripts/verify_examples_local.sh` locally; both must pass.
3. **Cut the release documentation.** Promote `[Unreleased]` to
   `[<version>] - <YYYY-MM-DD>`, add a fresh empty `[Unreleased]` heading,
   update the diff link at the bottom, set `latestGaVersion=<version>`, and
   update concrete public coordinates in `README.md` and `docs-site/`. Run
   `./scripts/verify_documentation_facts.sh` and commit this release-ready
   documentation before triggering the workflow.
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
   - "Assemble signed Maven Central bundle" and "Attest release build
     provenance" — create the exact upload ZIP and record signed GitHub/Sigstore
     provenance for it, all module jars, and all SBOM assets before publication.
   - "Publish to Maven Central (Central Portal)" — runs
     `./gradlew publishAggregationToCentralPortal`. The
     `com.gradleup.nmcp.aggregation` plugin uploads the already attested signed
     bundle via the Central Portal API.
   - "Create GitHub Release" — tags `v<version>`, attaches per-module
     jars and JSON/XML SBOMs, and writes auto-generated release notes.
6. **(USER_MANAGED only) Release in the portal.**
   <https://central.sonatype.com> → "Deployments" → find the upload →
   "Publish". Validation runs first; if it fails, fix and re-upload.
   Releases are immutable once published.

## Release rehearsal and recovery

Before dispatching a release, rehearse the exact non-publishing quality and artifact checks from a
clean version decision:

```bash
JAVA_HOME=<JDK 21+> ./scripts/verify_release_rehearsal.sh 2.1.0
```

The rehearsal validates SemVer, requires that `v2.1.0` does not already exist locally, verifies the
runbook recovery markers, and runs API compatibility plus release-SBOM checks with
`-PreleaseVersion=2.1.0`. It never reads publication credentials, tags a commit, uploads artifacts,
or contacts Maven Central.

If a release workflow fails, first identify the last completed step and check Central Portal before
retrying:

| Last completed step | Safe recovery |
| --- | --- |
| Before **Publish to Maven Central** | Fix the failure and dispatch the normal workflow again. No immutable artifact has been uploaded. |
| **Publish to Maven Central** completed, but the deployment is pending | Do not re-upload. Complete validation and publish (for `USER_MANAGED`) in Central Portal; then continue with the GitHub release only if needed. |
| **Publish to Maven Central** completed, but **Create GitHub Release** failed | Verify the exact version and target commit in Central Portal, then re-dispatch the workflow with the same version and `resumeGithubRelease=true`. It rebuilds, signs, and attests the release assets but deliberately skips Central upload. |
| Maven Central has already published the version | Never rerun the Central upload. Published coordinates are immutable. Repair only the missing GitHub release/announcement and retain the original version. |

`resumeGithubRelease=true` is a recovery control, not a shortcut: it must only be used after
confirming that the Central deployment for that exact version and commit exists. The workflow still
refuses an existing Git tag, so it cannot overwrite a completed GitHub release.

## Post-release

- Verify on Maven Central: `https://repo1.maven.org/maven2/io/github/frikit/krandom-core/<version>/`
  (allow up to ~30 minutes for index propagation).
- Download each `krandom-<module>.cdx.json` release asset and confirm its
  metadata component version matches the release tag.
- Verify a downloaded jar, SBOM, or `aggregation.zip` with
  `gh attestation verify --repo frikit/krandom <path>`.
- Run the Central-only consumer gate after the coordinates are visible:
  `KRANDOM_VERSION=<version> ./scripts/verify_examples_central.sh`. It exercises a plain-Java
  Maven/Gradle consumer and a Kotlin/Spring Maven consumer without Maven-local resolution.
- Land a follow-up version-facts commit on `main`. Update `apiBaselineVersion`
  to the released version, set `developmentVersion` to the next `*-SNAPSHOT`,
  and retain `latestGaVersion` at the release just published. Then run
  `./scripts/verify_documentation_facts.sh`; this moves the next
  API-compatibility baseline forward without changing the published install
  guidance.
- Confirm the GitHub Pages deployment includes that follow-up documentation
  commit before announcing the release broadly.

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
