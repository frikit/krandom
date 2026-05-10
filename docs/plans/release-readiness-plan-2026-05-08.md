# Release-Readiness Plan — 2026-05-08

## Context

Pre-release review (2026-05-08) surfaced one set of release blockers, one set
of "should land before 1.0.0" items, and a small hygiene tail. This plan
sequences the work so low-risk fixes ship first and the steps that need
external decisions (publishing approach, version number) are flagged
explicitly.

The sequence is intentional: doc/hygiene first to clean up the diff surface,
then community files, then the publishing rewrite (which needs an end-to-end
test against a throwaway version), then the actual `1.0.0` cut and
verification.

## Phase 1 — Documentation drift and hygiene (no consumer-visible behavior change)

- [ ] **1.1 Fix Spring Boot version drift in public docs.** `gradle/libs.versions.toml:15`
  pins `spring-boot = "4.0.6"`, but `README.md:14` and
  `docs-site/guides/spring-boot-starter.md:9` still say "Spring Boot 3.x".
  Replace those with Spring Boot 4.x and add a one-liner that the starter
  requires Spring Boot 4.x (the `api` scope on `spring-boot-autoconfigure`
  forces this on consumers).
- [ ] **1.2 Add `resume.txt` to `.gitignore`.** Untracked Claude/Copilot
  session-resume scratchpad sitting at repo root. Move the rule to the
  "Local-only scratch" group of `.gitignore` (next to `sonatype.user`).
- [ ] **1.3 Remove empty `states/` directory.** Leftover from earlier work,
  not tracked by git, just clutters the repo root.
- [ ] **1.4 Differentiate POM descriptions per module.** `build.gradle.kts:77`
  emits `description.set("kRandom ${project.name} module")`. Central Portal
  validation can flag generic descriptions; replace with per-module sentences
  (for example "Jackson serialization integration for kRandom generators",
  "Spring Boot 4.x auto-configuration for kRandom").

## Phase 2 — Community files (Roadmap 1.4)

- [ ] **2.1 Add `SECURITY.md`** with GitHub Security Advisories disclosure
  flow and a contact email.
- [ ] ~~**2.2 Add `CODE_OF_CONDUCT.md`** using Contributor Covenant 2.1.~~ — skipped per user decision (2026-05-08).
- [ ] **2.3 Add `.github/ISSUE_TEMPLATE/bug_report.md`** with Java version,
  OS, krandom version, minimal reproduction, expected vs actual.
- [ ] **2.4 Add `.github/ISSUE_TEMPLATE/feature_request.md`** with
  use-case-first framing.
- [ ] **2.5 Add `.github/PULL_REQUEST_TEMPLATE.md`** capturing summary,
  test plan, and a checkbox confirming `./scripts/pre_commit_check.sh`
  passed locally.

## Phase 3 — Verify last open code-quality findings

- [ ] **3.1 Confirm Finding #1 from `codex-fresh-review-plan-2026-04-27.md`
  is closed.** `FullNameGenerator` should pass a derived `GeneratorConfig`
  (not just a `Locale`) to `TitleGenerator`, `SuffixGenerator`, and
  `MiddleNameGenerator` when a `NameOptions.nationality` is selected. Verify
  the regression tests called for in that plan exist for the
  `generate(NameOptions)` path; if not, add them.
- [ ] **3.2 Defer codex round-2 polish items to `1.0.x`.** `FieldGeneratorResolver`
  collection-subtype fallback, registry validation consistency, and location
  fallback policy unification (see `docs/reviews/project-review-codex.md`).
  Document this decision in CHANGELOG so consumers know they're tracked but
  not blocking `1.0.0`.

## Phase 4 — Publishing rewrite (DECISION REQUIRED)

The current `release-maven-central.yml` uploads to legacy OSSRH
(`https://s01.oss.sonatype.org/.../staging/deploy/maven2/`) and stops after
upload. Sonatype completed the OSSRH → Central Portal migration in 2025; new
namespaces must use the Central Portal API at
`https://central.sonatype.com/api/v1/publisher/upload`. Even on accounts
where OSSRH still works, the workflow never closes/releases the staging
repo.

**Decision needed:** is `io.github.frikit` registered through OSSRH (legacy)
or Central Portal? `sonatype.user` carries a short alphanumeric token which
matches the Central Portal user-token format, suggesting Central Portal.
Confirm by signing into <https://central.sonatype.com> and checking
"Namespaces".

- [ ] **4.1 Confirm OSSRH vs Central Portal account type.** Output drives
  4.2.
- [ ] **4.2 Choose publishing tool.** Pick one of:
  - `org.jreleaser` (CLI/plugin) — handles bundle build + Central Portal
    upload + close/release in one step. Recommended if Central Portal.
  - `com.gradleup.nmcp` Gradle plugin — Central Portal-aware, simpler.
  - `io.github.gradle-nexus.publish-plugin` — only if account is still
    OSSRH-authoritative.
- [ ] **4.3 Rewrite the publish task wiring in `build.gradle.kts`.** Replace
  the current `OSSRH` `maven {}` repository block + manual `signing` block
  with the chosen plugin's configuration. Keep POM, signing, and module
  list logic equivalent.
- [ ] **4.4 Rewrite `.github/workflows/release-maven-central.yml`.** New
  workflow runs `./gradlew jreleaserDeploy` (or equivalent), which performs
  upload + close + release atomically. Remove the manual
  `publishMavenJavaPublicationToOSSRHRepository` step.
- [ ] **4.5 Document required CI secrets.** Update workflow + a release
  runbook with the secrets the new flow needs (Central Portal token, GPG
  key/password, etc.).
- [ ] **4.6 End-to-end smoke release.** Cut a throwaway version (for
  example `0.9.0-rc1`), run the new workflow, confirm the artifacts land in
  Maven Central, then yank the tag/version.

## Phase 5 — Version + CHANGELOG cut (DECISION REQUIRED)

**Decision needed:** first public release version. The roadmap and
release-workflow default both lean toward `1.0.0`; default to that unless
you want a `0.x` first.

- [ ] **5.1 Pick the first public version.**
- [ ] **5.2 Cut `CHANGELOG.md` `[Unreleased]` → `[<version>] - 2026-mm-dd`.**
  Drop a fresh empty `[Unreleased]` header above it. Also update the
  diff link at the bottom.
- [ ] **5.3 Update version-bearing prose.**
  - `README.md:31` — remove "Maven Central release automation is being
    prepared".
  - `README.md:159` — replace the "Releases" section with a real install
    snippet using the published version.
  - `docs-site/getting-started.md:18` and similar — replace `<version>`
    placeholders with the published version, or keep `<version>` and add a
    pointer to the latest GitHub release.

## Phase 6 — Pre-tag verification

- [ ] **6.1 Run `./scripts/pre_commit_check.sh`.** Confirm spotless,
  compile, tests, javadoc, and the 99.9% coverage gate all stay green on
  current HEAD. Local `core/build/` is several days stale, so the run must
  be from clean.
- [ ] **6.2 Run `./scripts/verify_examples_local.sh`.** Exercises
  `publishToMavenLocal` for every published module and runs the consumer
  examples (Java/Kotlin Gradle + Maven, Scala sbt + Mill).
- [ ] **6.3 Tag the release and trigger the release workflow.** Use the
  workflow_dispatch input on `release-maven-central.yml` (post-rewrite).

## Out of scope for `1.0.0`

- Full Java-library parity with DataFaker/JavaFaker/Easy Random/Instancio
  (`docs/plans/codex-full-java-library-parity-plan-2026-04-28.md`). Project
  ships scoped parity by design. Track as `2.x` roadmap.
- Codex round-2 collection-subtype/registry-validation/location-fallback
  cleanup. Track as `1.0.x` polish.
