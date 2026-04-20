# Scala + Mill Example (Test-based)

Uses `io.github.frikit:krandom-core:0.1.0-SNAPSHOT` by default for repo-local verification.

Use the embedded snapshot default only for repo-local Maven-local checks. For published versions, pass the target version explicitly.

## Run

```bash
./gradlew :core:publishToMavenLocal
cd examples/scala-mill
mill -i app.test
```

To use a different artifact version:

```bash
mill -Dkrandom.version=<version> -i app.test
```
