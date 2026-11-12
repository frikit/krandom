# Scala + Mill Example (Test-based)

Uses `io.github.frikit:krandom-core:3.0.3-SNAPSHOT` by default for repo-local verification.

Use the embedded snapshot default only for repo-local Maven-local checks. For published versions, pass the target version explicitly.

## What it shows

`ScalaMillUsageTest` fills a Scala 3 case class from the `Generators` factories in a ScalaTest suite.

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
