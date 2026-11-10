# Java + Maven Example (Test-based)

Uses `io.github.frikit:krandom-core:3.1.0-SNAPSHOT` by default for repo-local verification.

Use the embedded snapshot default only for repo-local Maven-local checks. For published versions, pass the target version explicitly.

## What it shows

- `JavaMavenUsageTest`: the `Generators` factories, a typed `ObjectModel` with `strict()`, and
  object-generation options: independent field streams, a snapshot clock that the replay recipe keeps,
  and a failed `ObjectFaker` profile that leaves no rule behind.
- `FixtureDiagnosticsExampleTest`: `ObjectFaker.strict()` rejects a shadowed rule with a
  `FixtureRuleException` before anything is generated, and its report names the rule and its path,
  never a value.
- `GenerationReportExampleTest`: a `GenerationReportListener` set on the `GeneratorConfig` receives an
  entry for each generated member, and the test asserts on an entry's path, step, generator and matched
  rule, never on a value.
- `SealedTypeExampleTest`: a sealed interface, as a member and as the root, is generated as one of its
  permitted subclasses, with no subtype mapping.

## Run

```bash
./gradlew :bom:publishToMavenLocal :core:publishToMavenLocal
cd examples/java-maven
mvn -q test
```

To use a different artifact version:

```bash
mvn -q -Dkrandom.version=<version> test
```
