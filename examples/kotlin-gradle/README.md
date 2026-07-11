# Kotlin + Gradle Example (Test-based)

Uses `io.github.frikit:krandom-core:2.0.0-SNAPSHOT` by default for repo-local verification.

Use the embedded snapshot default only for repo-local Maven-local checks. For published versions, pass the target version explicitly.

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
