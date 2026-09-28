# Dependency reproducibility

The main kRandom build pins direct dependency versions and rejects non-reproducible dependency
selectors. Gradle wrapper distributions are checksum-verified and GitHub Actions are SHA-pinned.
The repository tracks `gradle/verification-metadata.xml` with reviewed SHA-256 checksums for every
resolved external artifact. Exact version selectors alone do not freeze artifact bytes; Gradle
verification rejects an artifact whose bytes do not match this reviewed baseline.

## Repository policy

The main build resolves dependencies from Maven Central only. `mavenLocal()` is available solely
when a maintainer explicitly supplies `-PuseLocalMaven`; local artifacts must never be used for a
release. `RepositoriesMode.FAIL_ON_PROJECT_REPOS` prevents a project or plugin from silently adding
another dependency repository. Plugin resolution is limited to the Gradle Plugin Portal and Maven
Central, and `buildSrc` declares Maven Central explicitly.

Every project configuration calls Gradle's `failOnNonReproducibleResolution()`. Dynamic selectors
such as `1.+` and `latest.release`, Maven version ranges, and changing modules are therefore rejected
when the configuration is resolved. The development project version may remain a `-SNAPSHOT`
because project dependencies do not download a changing external module.

The standalone builds under `examples/` intentionally retain only `mavenLocal()` followed by Maven
Central: they are consumer simulations that verify the locally published kRandom snapshot. They do
not participate in a release publication.

## Updating verification metadata

When a reviewed dependency update adds artifacts, regenerate the SHA-256 metadata and review all
resolved components and artifacts before accepting it:

```bash
JAVA_HOME=<JDK 21+> ./scripts/update_verification_metadata.sh
```

Gradle records only the artifacts an invocation resolves, including those resolved while tasks run,
so the script runs every task graph that CI, the release workflow, and `scripts/pre_commit_check.sh`
use with `--write-verification-metadata sha256`:

| Invocation | Graph |
| --- | --- |
| `spotlessCheck build` | compilation, all tests (benchmarks and examples-e2e included), coverage, and Javadoc |
| `verifyReleaseSboms` | CycloneDX, in its own invocation as in CI |
| `:core:pitest` | PIT and its JUnit 5 plugin |
| `nmcpZipAggregation` | publications for Maven Central and Maven local |

Add a new invocation when CI starts resolving a different graph. The consumer builds under
`examples/` are separate Gradle builds without verification metadata.

On the unchanged dependency set the script is a no-op. It fails if an existing artifact gains a
second checksum (`<also-trust>`) or a trust exception appears: an artifact that resolves to different
bytes needs investigation, not acceptance. The script adds checksums; it is not approval to accept
the file wholesale. Review the XML diff and require all of the following before committing it:

1. Every component is explained by the current build; later additions must match an intended update.
2. Every new artifact has a SHA-256 checksum and no broad trusted-artifact exception was added.
3. Direct dependency and plugin versions are checked against their official release pages or Maven
   Central metadata.
4. `./scripts/pre_commit_check.sh` passes without `--write-verification-metadata` so verification is
   exercised in strict mode.

Gradle keeps existing entries, so checksums for dependencies that are no longer used remain until
the file is pruned. To prune, move the tracked file out of the repository, run the script to
bootstrap a new file, and confirm with `git diff` that the regeneration only removes components and
keeps every retained checksum unchanged.

### Dependabot updates

Dependabot cannot run the script, so its Gradle pull requests fail verification whenever they add
artifacts. Minor and patch updates of the main build arrive as one grouped pull request each week;
major updates arrive separately. For each such pull request:

1. Check out its branch and run `./scripts/update_verification_metadata.sh`.
2. Review the metadata diff with the checklist above; the new components should belong to the
   updated dependencies.
3. Commit the metadata to the branch, push, and let CI verify it in strict mode.

The consumer examples under `examples/` receive separate Gradle and Maven pull requests, one per
dependency across all example directories; they need no metadata update.

Checksums provide integrity after this reviewed baseline is established; they do not prove publisher
identity. Adding PGP identity verification is a separate hardening step and must not replace SHA-256
checks.

## Dependency locking

Dependency locking is not enabled. Direct versions are exact and non-reproducible selectors are
rejected during resolution, but there is no committed lock state for the resolved transitive graph.
Locking and artifact verification address different concerns: locks record selected versions, while
verification metadata records accepted artifact bytes. Neither is a dependency-resolution contract
for consumers, who resolve their own application graphs.

Re-evaluate locking if the same source selects different transitive versions, if a dependency needs
a version range, or if the project starts publishing resolved rather than declared versions.
