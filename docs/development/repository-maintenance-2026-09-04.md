# Repository maintenance — 2026-09-04

## Stage 1: Review open pull requests
**Goal**: Review PRs #102, #103, and #104 against main and test their combined changes.
**Success Criteria**: Scoped diffs, passing PR checks, and passing local pre-commit checks.
**Tests**: JDK 21 scripts/pre_commit_check.sh, including mutation and coverage gates.
**Status**: Complete

All three PRs have passing Java 21/25, native-image, mutation, consumer-example, and
Snyk checks. The changes update Easy Random to 6.0.1, Spotless to 8.10.1, and the
SHA-pinned setup-java action to 6.0.0. GitHub reports no open Dependabot alerts.

## Stage 2: Merge reviewed pull requests
**Goal**: Merge the reviewed PR heads and verify main CI.
**Success Criteria**: All three PRs merged; final main workflow succeeds.
**Tests**: GitHub tests + coverage workflow on the final merged commit.
**Status**: Complete

## Stage 3: Refresh dependencies and consumer examples
**Goal**: Apply remaining stable dependency updates and align consumer example versions.
**Success Criteria**: Build, API, SBOM, tests, coverage, mutation, and examples pass.
**Tests**: scripts/pre_commit_check.sh and scripts/verify_examples_local.sh on JDK 21.
**Status**: Complete

Maven Central metadata identifies SLF4J 2.0.19, japicmp 0.26.2, NMCP 1.6.2,
SnakeYAML 2.7, and benchmark-only Instancio 6.0.0. Review Instancio's major-version
changes against actual benchmark usage. Align examples with Kotlin 2.4.10,
JUnit 6.1.3, and Spring Boot 4.1.1. Gradle 9.7.1, CycloneDX 3.4.1,
and the PIT Gradle plugin 1.19.0 are current stable releases.

The existing dependency-reproducibility document incorrectly claims strict artifact
verification: no gradle/verification-metadata.xml is tracked. Correct the document
to distinguish implemented version pinning from proposed artifact verification.

## Stage 4: Deliver maintenance changes
**Goal**: Leave a reviewed, verified maintenance diff ready for commit approval.
**Success Criteria**: Record test results and any limitations; preserve v3 preparation work.
**Tests**: git diff --check and final diff review.
**Status**: Complete

## Review and verification results

- PRs [#102](https://github.com/frikit/krandom/pull/102),
  [#103](https://github.com/frikit/krandom/pull/103), and
  [#104](https://github.com/frikit/krandom/pull/104) merged after the combined local
  pre-commit check passed. Final main commit: `06724023f4f0dd5320497891d3922a6ff41d45a4`.
- Additional dependency updates passed the full JDK 21 pre-commit script: formatting,
  documentation, immutable build-input checks, compilation, API compatibility and evolution,
  release SBOM validation, Javadoc, tests, mutation testing, and coverage.
- Test XML reports total 15,110 tests, zero failures/errors, and 17 skipped tests.
  Coverage reports 100% lines and branches. PIT reports 1,325 mutations, 1,217 detected
  (92%), including one timeout; this passes the existing mutation threshold.
- Instancio 6.0.0 passed actual object and bulk JMH smoke runs, including batch sizes
  100 and 1,000. These short runs check execution only and are not performance evidence.
- Updated SHA pins for GraalVM setup 1.6.6, Pages deployment 5.0.1, and GitHub release
  action 3.0.3 after reading upstream release notes. All 20 action references remain SHA-pinned.
  Publishing and deployment actions were not executed locally.
- The user's original `codex/v3-preparation` checkout remains unchanged.

## Dependency sources

Versions were checked against Maven Central metadata, the Gradle Plugin Portal, and
upstream GitHub release/tag APIs on 2026-09-04.

| Dependency | Previous | Updated | Source |
| --- | --- | --- | --- |
| SLF4J | 2.0.18 | 2.0.19 | [Maven Central](https://repo.maven.apache.org/maven2/org/slf4j/slf4j-api/maven-metadata.xml) |
| japicmp | 0.26.1 | 0.26.2 | [Maven Central](https://repo.maven.apache.org/maven2/com/github/siom79/japicmp/japicmp/maven-metadata.xml) |
| NMCP | 1.6.1 | 1.6.2 | [Release](https://github.com/GradleUp/nmcp/releases/tag/v1.6.2) |
| SnakeYAML | 2.6 | 2.7 | [Maven Central](https://repo.maven.apache.org/maven2/org/yaml/snakeyaml/maven-metadata.xml) |
| Instancio (benchmarks) | 5.6.0 | 6.0.0 | [Release](https://github.com/instancio/instancio/releases/tag/instancio-parent-6.0.0) |
| GraalVM setup | 1.6.4 | 1.6.6 | [Release](https://github.com/graalvm/setup-graalvm/releases/tag/v1.6.6) |
| Pages deployment | 5.0.0 | 5.0.1 | [Release](https://github.com/actions/deploy-pages/releases/tag/v5.0.1) |
| GitHub release action | 3.0.2 | 3.0.3 | [Release](https://github.com/softprops/action-gh-release/releases/tag/v3.0.3) |

Consumer examples now use the main build's Kotlin 2.4.10, JUnit 6.1.3, and Spring Boot 4.1.1.

All consumer examples passed with `KRANDOM_REQUIRE_SCALA_TOOLS=true`: Java and Kotlin
Gradle/Maven examples, integration-module examples, JPMS, Scala/sbt, and Scala/Mill.
Markdown formatting, docs-site links, documentation facts, and `git diff --check` passed.
The repository-wide Markdown-link checker exists only on the separate v3 preparation
branch; it was not available on main. No checks or tests were disabled for this maintenance.

The maintenance changes are incorporated into the authorized
[2.2.0 release plan](release-2.2.0-plan.md) on `codex/release-2.2.0`.
The earlier review and verification results above describe the initial main-based maintenance pass.

Final merged-main [CI run](https://github.com/frikit/krandom/actions/runs/33885281274)
passed all five jobs: Java 21, Java 25, GraalVM native-image smoke, mutation, and examples.
