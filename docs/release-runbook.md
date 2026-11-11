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

1. **Decide the version.** SemVer, as [VERSIONING.md](../VERSIONING.md) defines it: a major release
   for removed or incompatibly changed public API, a minor release for compatible additions, and a
   patch release for fixes only; for example `3.1.0`. Confirm the `[Unreleased]` section in
   `CHANGELOG.md` is final. A minor or patch release needs a passing
   `./gradlew checkApiCompatibility`; see [Public API compatibility](#public-api-compatibility).
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

## Public API compatibility

[VERSIONING.md](../VERSIONING.md) defines the public API, and a minor or patch release keeps all of
it. `./gradlew checkApiCompatibility --continue` compares every published jar with the release that
`apiBaselineVersion` in `gradle.properties` names. `check`, the pre-commit gate, the CI job
**Public API Compatibility**, the release rehearsal, and the release workflow run it.

- **Java modules** (`core`, `jackson`, `junit`, `spring-boot-starter`): japicmp compares each jar with
  the released jar from Maven Central and fails on any binary- or source-incompatible change to public
  or protected API in the public-API packages: the packages that the released `krandom-core` module
  descriptor exports, and `io.github.frikit.krandom.jackson`, `.junit`, and `.spring`. Packages that
  `krandom-core` does not export are not checked. Each of these packages must stay exported, also if
  an integration module, now an automatic module, gains a module descriptor, and each module keeps its
  JPMS name. The `krandom.*` properties bind through the setters of
  `KrandomProperties`, so a removed or retyped property fails too. The full reports are in
  `build/reports/api-compatibility/<module>/`.
- **Kotlin modules** (`kotlin-dsl`, `kotest-extensions`): the Kotlin Gradle plugin dumps the ABI of
  the current code, leaving out `internal` declarations that are public in bytecode, and every class
  and member of the committed baseline `<module>/api/<module>-<apiBaselineVersion>.api` must still be
  there. A member may move to a supertype, become more visible, or lose `final` or `abstract`, and a
  class may gain supertypes; a type that users can implement, extend, or apply, such as an
  annotation, must not gain an abstract member. The plugin's `checkKotlinAbi`, which would reject
  additions, is turned off, and
  `updateKotlinAbi` only writes the current dump to `<module>/build/api-compatibility/current/`.

A failure names each broken declaration on its own line:

```text
krandom-core breaks the 3.0.0 public API (2 incompatible changes):
  io.github.frikit.krandom.generator.color.ColorGenerator#generateColorName(): METHOD_RETURN_TYPE_CHANGED (breaks binary and source)
  io.github.frikit.krandom.generator.color.ColorGenerator#generateSafeColorName(): METHOD_REMOVED (breaks binary and source)

krandom-kotlin-dsl breaks the 3.0.0 public API (1 incompatible change):
  io.github.frikit.krandom.dsl.ConfigScope: removed `public final fun seed (J)V`
```

What to do:

- **An addition** passes; list it under Added in the changelog. A method added to an interface or
  abstract class that users implement needs a default implementation, or the check reports it.
- **A deprecation** is how a minor release retires API. Keep the declaration, mark it `@Deprecated`
  with a `@deprecated` Javadoc tag that names the replacement (in Kotlin,
  `@Deprecated("…", ReplaceWith("…"))` at the default `WARNING` level), add the replacement, and list
  the deprecation under Deprecated in the changelog. The check passes, because deprecating changes no
  signature. `DeprecationLevel.ERROR` and `HIDDEN` stop existing callers from compiling, so they wait
  for a major release like the removal itself; the check reports `HIDDEN`, but not `ERROR`.
- **A removal or an incompatible change** fails; restore the baseline declaration. Only a major
  release removes or changes public API, and the owner decides how the baseline moves when a major
  release line opens.
- **A compatible change that the Kotlin check rejects**, such as an annotation element with a default
  value or an abstract member added to a sealed type, which no user can implement: record it with
  `./gradlew updateKotlinApiBaseline` in the same pull request and explain it there, so that review
  sees the baseline diff. japicmp also rejects an element added to a Java annotation even when it has
  a default value, and the Java check has no such escape; give the new setting another form, such as
  a new annotation.

The check does not cover:

- Kotlin `inline` functions with `reified` type parameters: they compile to synthetic methods that
  the ABI dump leaves out (`krandom`, `krandomList`, `krandomGenerator`, the reified
  `KrandomBuilder.ruleForType`, and `krandomReplayObjectArb`). The `@PublishedApi` members they call
  are checked, so compiled callers keep working, but a change to their own signatures shows only in
  the module tests that call them; review such changes by hand.
- Kotlin source details that a JVM signature does not carry: parameter names, nullability, default
  values, `vararg` versus an array, and type arguments. The ABI dump records erased JVM signatures, so
  a change to one of these passes; review it by hand. For the Java modules, japicmp reports changed
  type arguments and a parameter that stops being varargs.
- Behavior: seeded output, the `krandom-recipe/v1` format, and the `fnv1a64-v1` derivation stay with
  the tests, such as `GenerationRecipeTest`.

After each release the baseline moves to it; see [Post-release](#post-release).

## Release rehearsal and recovery

Before dispatching a release, rehearse the exact non-publishing quality and artifact checks from a
clean version decision:

```bash
JAVA_HOME=<JDK 21+> ./scripts/verify_release_rehearsal.sh 2.4.0
```

The rehearsal validates SemVer, requires that `v2.4.0` does not already exist locally, verifies the
runbook recovery markers, and runs the release-facts, documentation-facts, build, and release-SBOM
checks with `-PreleaseVersion=2.4.0`. It never reads publication credentials, tags a commit, uploads artifacts,
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
- Land a follow-up version-facts commit on `main`: set `developmentVersion` to
  the next `*-SNAPSHOT`, retain `latestGaVersion` at the release just
  published, and move the public API baseline to it. Set
  `apiBaselineVersion=<version>`, which the check resolves from Maven Central,
  and run `./gradlew updateKotlinApiBaseline`, which replaces the dumps in
  `kotlin-dsl/api/` and `kotest-extensions/api/` with those of the current code.
  Run it before any other change lands on `main`, or else on a checkout of the
  `v<version>` tag with `-PapiBaselineVersion=<version>` and copy the two dumps
  over. The new baseline jars need checksums before strict verification
  accepts them: run `./scripts/update_verification_metadata.sh` under JDK 21
  and JDK 25, as [dependency reproducibility](development/dependency-reproducibility.md)
  describes, check that it added only the `krandom-*` `<version>` jars and
  module files, and confirm each jar with
  `gh attestation verify --repo frikit/krandom <jar>`. Then run
  `./scripts/verify_documentation_facts.sh` and
  `./gradlew checkApiCompatibility`; the published install guidance stays
  unchanged.
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
