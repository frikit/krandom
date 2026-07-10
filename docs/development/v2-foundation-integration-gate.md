# V2 Foundation Integration Gate

**Status:** Repeatable gate complete; Stage 2 remains in progress
**Master plan:** [Step 2.10](v2-master-implementation-plan.md#step-210--run-the-foundation-integration-gate)

## Run locally

```bash
JAVA_HOME=<JDK 21+> ./scripts/verify_foundation_integration_gate.sh
```

The command runs the full pre-commit gate first, then publishes the current artifacts to Maven
Local and verifies the Java Gradle/Maven, integration-module, Java 21 JPMS, Kotlin Gradle/Maven,
and available Scala consumers. It never publishes to a remote repository.

## Evidence covered by the command

| Contract | Evidence | Gate coverage |
| --- | --- | --- |
| P0.1 — object values are valid or fail contextually | Safe construction, recursive types, Bean Validation, Kotlin immutable-object, and structured failure tests | Full test suite and coverage gate |
| P0.2 — deterministic source ownership is explicit | `GeneratorConfig` source-combination, recipe, and named-child-stream tests | Full test suite and coverage gate |
| P0.6 — sensitive fixtures are safe by default | Payment-card, phone, banking, national-ID, identity-document, crypto, business-tax, and securities safety-policy tests | Full test suite and coverage gate |
| Public API evolution is reviewed | Japicmp binary/source compatibility and exact evolution allowlist | `checkApiContract` inside pre-commit |
| Published artifacts work for consumers | Java, Kotlin, JPMS, and available Scala examples resolve the current Maven Local artifacts | `verify_examples_local.sh` |

## Reviewed error and migration surfaces

- [`v2-generation-failure-inventory.md`](v2-generation-failure-inventory.md) remains the source
  of truth for strict versus lenient generation boundaries. Its public context types and
  `ObjectGenerationException`/`SchemaGenerationException` compatibility path are classified in
  [`v2-public-api-inventory.md`](v2-public-api-inventory.md).
- [`v1.6-to-v2.md`](../migration/v1.6-to-v2.md) records every intentional 1.6 bridge and its v2
  replacement. The public API gate rejects unreviewed changes rather than treating this document
  as a waiver.

## Intentional remaining limitation

Step 2.2 still has one open action: schema inference has a narrow package-local `Type` adapter
that mirrors the package-private object-generation `ResolvedType` model. Sharing that exact class
across the `object` and `schema` packages would require widening it into the public API or making a
larger package-boundary change. The v1.6 bridge keeps the model internal until a v2 API boundary is
chosen deliberately. This gate therefore demonstrates current correctness and consumer
compatibility; it does not claim that Stage 2 is complete.
