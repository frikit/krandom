# Kotlin + Gradle Example (Test-based)

Uses `io.github.frikit:krandom-core:0.1.0-SNAPSHOT` by default for repo-local verification.

## Run

```bash
./gradlew :core:publishToMavenLocal
cd examples/kotlin-gradle
./gradlew test
```

To use a different artifact version:

```bash
./gradlew -PkrandomVersion=<version> test
```
