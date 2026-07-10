# Java JPMS consumer

This Gradle composite verifies Java 21 named-module behavior against the locally published
`krandom-core` artifact.

- `open-consumer` proves safe constructor and mutable-field access with a qualified `opens` clause.
- `closed-consumer` proves that a missing `opens` clause fails with the exact actionable directive.

Run it through `../../scripts/verify_examples_local.sh`, or after publishing the requested kRandom
version to Maven local:

```bash
../../gradlew -PkrandomVersion=1.6.0-SNAPSHOT verifyJpms
```
