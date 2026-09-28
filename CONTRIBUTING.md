# Contributing to krandom

Thank you for your interest in contributing to krandom! This document describes the workflow and requirements for contributing.

## Prerequisites

- **Java 21** (Temurin recommended)
- The included Gradle wrapper

Verify your setup:

```bash
java -version   # must report 21+
./gradlew --version
```

## Development workflow

1. **Fork** the repository and clone your fork.
2. **Create a branch** from `main` for your change.
3. **Make your changes** — keep commits focused and atomic.
4. **Run the pre-commit checks** before pushing:

   ```bash
   ./scripts/pre_commit_check.sh
   ```

   The full gate applies formatting and license headers, then verifies Markdown formatting,
   repository and docs-site links, documentation facts, pinned build inputs, compilation, module
   boundaries, release SBOMs, Javadoc, all tests (forced to rerun), critical-path mutation testing,
   and exact core coverage.

   While iterating, `./scripts/pre_commit_check.sh --fast` keeps formatting, documentation checks,
   compilation, module boundaries, tests, and the core coverage gate, but reuses up-to-date test
   results and skips mutation testing and SBOM validation. Run the full gate before pushing.

5. **Open a pull request** against `main`.

## Code quality gates

- **Coverage**: `krandom-core` enforces exact 100% line, branch, instruction, method, class, and
  complexity coverage via JaCoCo; its report is uploaded to Codecov. New core code must be covered
  by tests. The integration modules (`jackson`, `junit`, `spring-boot-starter`, `kotest-extensions`,
  and `kotlin-dsl`) generate JaCoCo reports in `<module>/build/reports/jacoco/test` without a
  coverage gate; the pre-commit summary prints their line and branch coverage. New integration
  code still needs behavior tests.
- **Mutation testing**: deterministic, safety-sensitive, object, and schema paths must retain at
  least an 85% mutation score and 98% mutated-class line coverage. Expand targets incrementally
  around meaningful branching, review survivors by behavior, and do not treat 100% as a goal when
  equivalent or implementation-only mutations remain.
- **Module boundaries**: `scripts/verify_module_boundaries.sh` requires a unique JPMS module name
  for every published jar and rejects packages split across published jars.
- **Release SBOMs**: `./gradlew verifyReleaseSboms` generates and validates the CycloneDX JSON and XML
  SBOM of every published module.
- **Documentation**: the pre-commit gate checks Markdown formatting, repository and docs-site
  links, and the version, module, and locale facts in `gradle.properties`. CI checks the same
  formatting, repository links, and facts; the documentation workflow checks docs-site links.
- **Dependency integrity**: dependencies resolve against reviewed SHA-256 checksums in
  `gradle/verification-metadata.xml`; see [dependency reproducibility](docs/development/dependency-reproducibility.md)
  for updating it, including for Dependabot pull requests.
- **Formatting**: Spotless enforces consistent formatting and MIT license headers. Run `./gradlew spotlessApply` to fix formatting issues.
- **Tests**: all tests must pass. Java modules use JUnit Jupiter; Kotlin modules use Kotest.

CI additionally builds on Java 21 and 25, requires the GraalVM native-image smoke test, and runs every
consumer example against the locally published snapshot.

## Maintenance tools

These scripts are run by hand; none of them is part of the pre-commit gate.

- `scripts/update_verification_metadata.sh` records checksums for every CI and pre-commit task
  graph after a dependency update.
- `scripts/upgrade_latest_gradle.sh` moves the Gradle wrapper to the latest release with its pinned
  distribution checksum; Dependabot normally proposes wrapper updates.
- `scripts/verify_examples_local.sh` publishes the snapshot to Maven local and runs the consumer
  examples; `KRANDOM_REQUIRE_SCALA_TOOLS=true` makes missing sbt or Mill an error.
- `scripts/verify_native_image.sh` builds the native-image smoke fixture. It skips when
  `native-image` is missing unless `KRANDOM_REQUIRE_NATIVE_IMAGE=true`, which CI sets.
- `scripts/run_benchmarks.sh` runs JMH and regenerates the benchmark dashboard.
- `scripts/verify_release_rehearsal.sh` and `scripts/verify_examples_central.sh` are release steps
  described in the [release runbook](docs/release-runbook.md).

## What makes a good contribution

- **Bug fixes** with a reproducing test case.
- **New generators** that follow the existing `Generator<T>` pattern and include locale-aware data where applicable.
- **Locale data expansion** — see [locale contribution guide](docs/locale-contribution-guide.md) for adding or improving locale datasets.
- **Documentation improvements** — especially usage examples and API guides.
- **Performance improvements** backed by JMH benchmark data.
- **Domain data packs and extensions** — long-tail providers are welcome when they have a clear
  fixture use case, source, license, checksum, safety classification, maintainer, and invariant
  tests. Use a configuration-scoped `KRandomModule` for provider/schema contributions or a
  verified `LocalDataPack` for offline datasets; do not add global registration or runtime
  network loading.

## Pull request guidelines

- Keep PRs focused on a single concern.
- Include tests for new functionality.
- Update relevant documentation if the public API changes.
- Describe user-visible changes, including changed or removed API, in the `Unreleased` section of
  [CHANGELOG.md](CHANGELOG.md).
- Ensure the full `./scripts/pre_commit_check.sh` (without `--fast`) passes locally before requesting review.
- Write a clear PR description explaining *what* and *why*.

## Reporting issues

Use [GitHub Issues](https://github.com/frikit/krandom/issues) for bug reports and feature requests. Include:

- Java version and OS
- Minimal reproducing code or test case
- Expected vs. actual behavior

## License

By contributing, you agree that your contributions will be licensed under the [MIT License](LICENSE).
