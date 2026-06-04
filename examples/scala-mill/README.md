# Scala + Mill Example (Test-based)

Uses `io.github.frikit:krandom-core:1.1.0-SNAPSHOT` by default for repo-local verification.

Use the embedded snapshot default only for repo-local Maven-local checks. For published versions, pass the target version explicitly.

## Run

```bash
./gradlew :core:publishToMavenLocal
cd examples/scala-mill
mill -i app.test
```

To use a different artifact version:

```bash
mill -i -Dkrandom.version=<version> app.test
```
