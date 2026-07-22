# v2.1 Mutation Pilot Baseline

## Scope

The v2.1 pilot is intentionally narrow. It mutates the schema import/export contract and the
locale lookup primitive rather than using a nominal project-wide percentage:

- `SchemaParser`: numeric bounds, OpenAPI nullability, seeded parser formats, and rejected schema
  shapes.
- `Schema` and `SchemaValueProvider`: JSONL/JSON/CSV/XML/SQL/YAML/TOML output and escaping.
- `RegistryLookup`: exact and language-level locale fallback.

The matching tests are the schema package plus `RegistryLookupTest`; they include deterministic
seed tests, nullable-field tests, numeric-bound tests, and escaped-output tests. Run the pilot with:

```bash
JAVA_HOME=<JDK 21+> ./gradlew :core:pitest --max-workers=1 --no-daemon
```

PIT writes HTML and XML reports under `core/build/reports/pitest/`. The task is separate from the
fast coverage gate while this is a measured pilot rather than a release-blocking threshold.

## Baseline recorded 2026-07-22

| Measure | Result |
| --- | ---: |
| Mutated classes | 4 |
| Tests examined | 87 |
| Mutations generated | 486 |
| Mutations killed | 455 |
| Test strength | 94% |
| Mutated-class line coverage | 99% (860/865) |
| Mutations without coverage | 0 |
| Timeouts | 1 |
| Wall time | 21 seconds |

The one timeout is retained as an observation, not hidden or treated as killed. No mutation
threshold is enforced yet: the next release-planning decision should review surviving and timed-out
mutants, remove equivalent mutants where justified, then choose a threshold against this explicit
baseline. The standard JaCoCo gate remains unchanged.
