#!/usr/bin/env bash
# Run the focused v2 foundation acceptance suite from clean project outputs.

set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
RUNS="${FOUNDATION_ACCEPTANCE_RUNS:-2}"

if ! [[ "${RUNS}" =~ ^[1-9][0-9]*$ ]]; then
    echo "FOUNDATION_ACCEPTANCE_RUNS must be a positive integer, was '${RUNS}'" >&2
    exit 2
fi

CORE_TESTS=(
    io.github.frikit.krandom.generator.GeneratorConfigTest
    io.github.frikit.krandom.generator.GeneratorTest
    io.github.frikit.krandom.generator.GenerationRecipeGoldenStreamTest
    io.github.frikit.krandom.generator.GenerationRecipeTest
    io.github.frikit.krandom.generator.RandomSourceContractTest
    io.github.frikit.krandom.generator.finance.BankInfoGeneratorTest
    io.github.frikit.krandom.generator.finance.BicIsinGeneratorTest
    io.github.frikit.krandom.generator.finance.CreditCardGeneratorTest
    io.github.frikit.krandom.generator.finance.CreditCardInfoGeneratorTest
    io.github.frikit.krandom.generator.finance.Phase2FinanceGeneratorsTest
    io.github.frikit.krandom.generator.finance.Phase3FinanceGeneratorsTest
    io.github.frikit.krandom.generator.location.PhoneNumberGeneratorTest
    io.github.frikit.krandom.generator.object.BeanValidationConstraintGenerationTest
    io.github.frikit.krandom.generator.object.BeanValidationNormalizationTest
    io.github.frikit.krandom.generator.object.BeanValidationScalarNormalizationTest
    io.github.frikit.krandom.generator.object.BeanValidationSupportMatrixTest
    io.github.frikit.krandom.generator.object.BeanValidationTextNormalizationTest
    io.github.frikit.krandom.generator.object.FieldGeneratorResolverCollectionFallbackTest
    io.github.frikit.krandom.generator.object.ObjectConstructionPolicyTest
    io.github.frikit.krandom.generator.object.ObjectGenerationFailurePolicyTest
    io.github.frikit.krandom.generator.object.ObjectGeneratorCollectionTest
    io.github.frikit.krandom.generator.object.ObjectGeneratorInheritedGenericTest
    io.github.frikit.krandom.generator.object.ObjectGeneratorNestedGenericTest
    io.github.frikit.krandom.generator.object.ObjectGeneratorNestedTypeUseConstraintTest
    io.github.frikit.krandom.generator.object.ObjectGeneratorParameterizedObjectTest
    io.github.frikit.krandom.generator.object.ResolvedTypeTest
    io.github.frikit.krandom.generator.provider.ProviderCatalogTest
    io.github.frikit.krandom.generator.user.DrivingLicenseGeneratorTest
    io.github.frikit.krandom.generator.user.PassportGeneratorTest
    io.github.frikit.krandom.generator.user.nationalid.NationalIdGeneratorTest
    io.github.frikit.krandom.generator.commerce.CnpjGeneratorTest
)

step() {
    echo
    echo "==> $*"
}

run_core_tests() {
    local arguments=(":core:test")
    local test_class
    for test_class in "${CORE_TESTS[@]}"; do
        arguments+=("--tests" "${test_class}")
    done
    "${REPO_ROOT}/gradlew" "${arguments[@]}"
}

for ((run = 1; run <= RUNS; run++)); do
    step "Clean project outputs for foundation acceptance pass ${run}/${RUNS}"
    "${REPO_ROOT}/gradlew" clean

    step "Run focused Java foundation acceptance tests for pass ${run}/${RUNS}"
    run_core_tests

    step "Run focused JUnit replay acceptance tests for pass ${run}/${RUNS}"
    "${REPO_ROOT}/gradlew" :junit:test \
        --tests io.github.frikit.krandom.junit.KrandomExtensionTest \
        --tests io.github.frikit.krandom.junit.KrandomExtensionEngineTest

    step "Run focused Kotest replay acceptance tests for pass ${run}/${RUNS}"
    "${REPO_ROOT}/gradlew" :kotest-extensions:test \
        --tests io.github.frikit.krandom.kotest.KrandomArbTest

    step "Run focused Kotlin acceptance tests for pass ${run}/${RUNS}"
    "${REPO_ROOT}/gradlew" :kotlin-dsl:test \
        --tests io.github.frikit.krandom.dsl.KrandomDslTest \
        --tests io.github.frikit.krandom.dsl.KotlinImmutableObjectGenerationTest
done
