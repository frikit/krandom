# Kotlin + Maven Example (Test-based)

Uses `io.github.frikit:krandom-core:2.3.0-SNAPSHOT` by default for repo-local verification.

Use the embedded snapshot default only for repo-local Maven-local checks. For published versions, pass the target version explicitly.

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
