# Scala + sbt Example (Test-based)

Uses `io.github.frikit:krandom-core:0.1.0-SNAPSHOT` by default for repo-local verification.

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
