# V2 Foundation Acceptance Evidence

**Status:** Focused evidence complete — verified with two clean passes on 2026-07-10
**Master-plan link:** [Step 2.10](v2-master-implementation-plan.md#step-210--run-the-foundation-integration-gate)

## Run locally

```bash
JAVA_HOME=<JDK 21+> ./scripts/verify_foundation_acceptance.sh
```

The command starts every pass with `clean`, then runs the focused core, JUnit, Kotest, and Kotlin
acceptance tests. It runs two clean passes by default so reproducibility claims do not depend on
stale project outputs. `FOUNDATION_ACCEPTANCE_RUNS=1` is available only for local diagnosis;
release evidence always uses the default two-pass run.

## Evidence map

| Audit acceptance criterion | Executable evidence |
| --- | --- |
| P0.1: immutable Kotlin values are constructed or rejected before escape | `KotlinImmutableObjectGenerationTest` and `ObjectConstructionPolicyTest` |
| P0.1: nested lists, maps, wildcards, records, and Kotlin bindings retain their declared types | `ObjectGeneratorNestedGenericTest`, `ObjectGeneratorInheritedGenericTest`, `ObjectGeneratorParameterizedObjectTest`, `ObjectGeneratorCollectionTest`, `ResolvedTypeTest`, and `KotlinImmutableObjectGenerationTest` |
| P0.1: Bean Validation supports positive, nullability, boundary, and contradiction cases against Hibernate Validator | `BeanValidationConstraintGenerationTest`, `BeanValidationNormalizationTest`, `BeanValidationScalarNormalizationTest`, `BeanValidationTextNormalizationTest`, and `BeanValidationSupportMatrixTest` |
| P0.1: default generation does not return after assignment or insertion failure | `ObjectGenerationFailurePolicyTest` and `FieldGeneratorResolverCollectionFallbackTest` |
| P0.2: every random-source combination is defined and rejected when ambiguous | `GeneratorConfigTest` and `RandomSourceContractTest` |
| P0.2: composed generators and Java object/schema paths replay deterministically | `GeneratorTest`, `GenerationRecipeTest`, `GenerationRecipeGoldenStreamTest`, `KrandomExtensionTest`, `KrandomExtensionEngineTest`, `KrandomArbTest`, and `KrandomDslTest` |
| P0.6: safe defaults and explicit opt-in policies cover payment, bank, securities, identity, phone, crypto, and tax fixtures | `CreditCardGeneratorTest`, `CreditCardInfoGeneratorTest`, `BankInfoGeneratorTest`, `BicIsinGeneratorTest`, `Phase2FinanceGeneratorsTest`, `Phase3FinanceGeneratorsTest`, `NationalIdGeneratorTest`, `DrivingLicenseGeneratorTest`, `PassportGeneratorTest`, `PhoneNumberGeneratorTest`, and `CnpjGeneratorTest` |
| P0.6: sensitive provider metadata declares the validity and safety tier | `ProviderCatalogTest` and `GenerationRecipeTest` |

The command is intentionally focused on the Stage 2 exit criteria. The broader
[`verify_foundation_integration_gate.sh`](../../scripts/verify_foundation_integration_gate.sh)
continues to run the whole project suite, API compatibility check, and locally published consumer
examples.

## Scope boundary

This evidence closes the repeated-clean-run and default-error-handling assertions in Step 2.10,
and — together with the JUnit replay overrides and Kotest host-seeded adapters — the combined
P0.1/P0.2/P0.6 assertion for the supported frameworks. The audit's jqwik replay expectation is out
of scope by owner decision (2026-07-10): jqwik is forbidden in this project and its integration
module was removed in 1.1.0. JUnit replay work continues in [Step
3.6](v2-master-implementation-plan.md#step-36--complete-junit-replay-integration).

It does not close Stage 2: the remaining internal schema/object type-model boundary is
documented in
[`v2-foundation-integration-gate.md`](v2-foundation-integration-gate.md#intentional-remaining-limitation).
