# Dependency reproducibility

The main kRandom build pins direct dependency versions and rejects non-reproducible dependency
selectors. Gradle wrapper distributions are checksum-verified and GitHub Actions are SHA-pinned.
The repository does not currently track `gradle/verification-metadata.xml`, so Gradle dependency
artifact checksums are not enforced. Exact version selectors alone do not freeze artifact bytes.

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

## Establishing verification metadata

Artifact verification remains a separate hardening task. To establish a baseline, generate SHA-256
metadata for the build, then review all resolved components and artifacts before accepting it:

```bash
./gradlew --write-verification-metadata sha256 \
  clean build check checkApiContract :core:javadoc \
  --max-workers=1 --no-daemon \
  -x :benchmarks:test -x :benchmarks:check
```

This command adds checksums required by the resolved graph; it is not approval to accept the file
wholesale. Review the XML diff and require all of the following before committing it:

1. Every component is explained by the current build; later additions must match an intended update.
2. Every new artifact has a SHA-256 checksum and no broad trusted-artifact exception was added.
3. Direct dependency and plugin versions are checked against their official release pages or Maven
   Central metadata.
4. `./scripts/pre_commit_check.sh` passes without `--write-verification-metadata` so verification is
   exercised in strict mode.

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
