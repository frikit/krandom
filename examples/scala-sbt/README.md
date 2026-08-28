# Scala + sbt Example (Test-based)

Uses `io.github.frikit:krandom-core:3.0.0-SNAPSHOT` by default for repo-local verification.

Use the embedded snapshot default only for repo-local Maven-local checks. For published versions, pass the target version explicitly.

## Run

```bash
./gradlew :core:publishToMavenLocal
cd examples/scala-sbt
sbt test
```

To use a different artifact version:

```bash
sbt -Dkrandom.version=<version> test
```
