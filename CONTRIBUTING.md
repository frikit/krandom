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

   This runs formatting, markdown checks, compilation, tests, Javadoc validation, and coverage verification.

5. **Open a pull request** against `main`.

## Code quality gates

- **Coverage**: the build enforces exact 100% line, branch, instruction, method, class, and complexity coverage via JaCoCo. New code must be covered by tests.
- **Mutation testing**: critical object/schema paths must retain at least an 85% mutation score and
  98% mutated-class line coverage.
- **Formatting**: Spotless enforces consistent formatting and MIT license headers. Run `./gradlew spotlessApply` to fix formatting issues.
- **Tests**: all tests must pass. The test suite uses Kotest (DescribeSpec) for the core module and JUnit 5 for Java modules.

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
- Classify every public API change against the released baseline and update the current v3 plan.
- Ensure `./scripts/pre_commit_check.sh` passes locally before requesting review.
- Write a clear PR description explaining *what* and *why*.

## Reporting issues

Use [GitHub Issues](https://github.com/frikit/krandom/issues) for bug reports and feature requests. Include:

- Java version and OS
- Minimal reproducing code or test case
- Expected vs. actual behavior

## License

By contributing, you agree that your contributions will be licensed under the [MIT License](LICENSE).
