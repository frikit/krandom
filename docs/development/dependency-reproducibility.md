# Dependency reproducibility

The main kRandom build treats dependency coordinates and artifact bytes as reviewed source inputs.
Gradle verifies every resolved plugin, build tool, production dependency, and test dependency against
`gradle/verification-metadata.xml` in strict mode. A missing artifact or changed checksum fails the
build.

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

Make one dependency or plugin update at a time. Then run the same broad task graph used to create the
baseline:

```bash
./gradlew --write-verification-metadata sha256 \
  clean build check checkApiContract :core:javadoc \
  --max-workers=1 --no-daemon \
  -x :benchmarks:test -x :benchmarks:check
```

This command adds checksums required by the resolved graph; it is not approval to accept the file
wholesale. Review the XML diff and require all of the following before committing it:

1. Every new component is explained by the intended version change.
2. Every new artifact has a SHA-256 checksum and no broad trusted-artifact exception was added.
3. Direct dependency and plugin versions are checked against their official release pages or Maven
   Central metadata.
4. `./scripts/pre_commit_check.sh` passes without `--write-verification-metadata` so verification is
   exercised in strict mode.

Checksums provide integrity after this reviewed baseline is established; they do not prove publisher
identity. Adding PGP identity verification is a separate hardening step and must not replace SHA-256
checks.

## Dependency-locking decision

Dependency locking is deliberately not enabled at this stage. All direct versions are exact,
non-reproducible selectors are rejected during resolution, and verification metadata freezes both
module metadata and artifact bytes. Lock files would duplicate that graph across this multi-project
library without protecting plugin resolution or `buildSrc`, while adding another update surface.
They would also provide no useful contract to library consumers, who must resolve dependencies in
their own application graph.

Re-evaluate locking if an unavoidable version range enters the resolved graph, if identical source
and verification metadata ever select different versions, or if the project starts publishing
resolved rather than declared dependency versions.
