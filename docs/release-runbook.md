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
   and `./scripts/verify_examples_local.sh` locally; both must pass. The pre-commit report must show
   zero missed instructions, lines, branches, complexity points, methods, and classes under the
   exact 100% JaCoCo gate; a rounded `100.0%` display is not sufficient by itself.
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
   - "Build, test, and check" — full clean + tests + exact 100% coverage gate and
     validated per-module CycloneDX 1.6 SBOMs
     (`-x :benchmarks:test -x :benchmarks:check`).
   - "Assemble signed Maven Central bundle" and "Attest release build
     provenance" — create the exact upload ZIP and record signed GitHub/Sigstore
     provenance for it, all module jars, and all SBOM assets before publication.
   - "Publish to Maven Central (Central Portal)" — runs
     `./gradlew publishAggregationToCentralPortal`. The
     `com.gradleup.nmcp.aggregation` plugin uploads the already attested signed
     bundle via the Central Portal API.
   - With `AUTOMATIC`, "Create GitHub Release" tags `v<version>`, attaches per-module
     jars and JSON/XML SBOMs, and writes auto-generated release notes in the same run.
   - With `USER_MANAGED`, the first run stops after the signed Central upload and does not create
     a public tag or GitHub release for artifacts that are not available yet. Record the exact
     release commit printed by the workflow.
6. **(USER_MANAGED only) Release in the portal and finish GitHub publication.**
   <https://central.sonatype.com> → "Deployments" → find the upload →
   "Publish". Validation runs first; if it fails, fix and re-upload. Once Maven Central shows the
   version as published, rerun the workflow before `main` moves, with the same version,
   `resumeGithubRelease=true`, and `releaseCommit=<SHA printed by the initial run>`. The workflow
   verifies the commit and public Central POM, skips Central upload, and creates the tag and GitHub
   release.
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
| **Publish to Maven Central** completed, but the deployment is pending | Do not re-upload. Complete validation and publish in Central Portal; then rerun before `main` moves with `resumeGithubRelease=true` and the recorded `releaseCommit`. |
| **Publish to Maven Central** completed, but **Create GitHub Release** failed | Verify the exact version and target commit in Central Portal, then re-dispatch with the same version, `resumeGithubRelease=true`, and the recorded `releaseCommit`. An existing tag is accepted only when it already points to that commit. |
| `main` moved after a USER_MANAGED upload | Do not tag the newer commit or re-upload immutable coordinates. Create or repair the GitHub release at the recorded release commit, then verify its assets and attestations manually. |
| Maven Central has already published the version | Never rerun the Central upload. Published coordinates are immutable. Repair only the missing GitHub release/announcement and retain the original version. |

`resumeGithubRelease=true` is a recovery control, not a shortcut: it must only be used after
confirming that the Central deployment for that exact version and commit exists. The workflow
requires the recorded commit, verifies the public Maven Central POM, and refuses an existing tag
unless it resolves to that same commit.

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
