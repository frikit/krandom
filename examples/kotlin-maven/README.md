# Kotlin + Maven Example (Test-based)

Uses `io.github.frikit:krandom-core:0.1.0-SNAPSHOT` by default for repo-local verification.

## Run

```bash
./gradlew :core:publishToMavenLocal
cd examples/kotlin-maven
mvn -q test
```

To use a different artifact version:

```bash
mvn -q -Dkrandom.version=<version> test
```
