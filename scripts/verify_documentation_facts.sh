#!/bin/bash
# Verify public documentation against the release/module/locale facts in gradle.properties.

set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
FACTS_FILE="${REPO_ROOT}/gradle.properties"

fact() {
    local key="$1"
    awk -F= -v key="${key}" '$1 == key { print substr($0, index($0, "=") + 1) }' "${FACTS_FILE}"
}

fail() {
    echo "Documentation facts check failed: $*" >&2
    exit 1
}

DEVELOPMENT_VERSION="$(fact developmentVersion)"
LATEST_GA_VERSION="$(fact latestGaVersion)"
JAVA_MINIMUM_VERSION="$(fact javaMinimumVersion)"
PUBLISHED_MODULES="$(fact publishedModules)"
NATIVE_LOCALE_COUNT="$(fact nativeLocaleCount)"
FALLBACK_LOCALE_COUNT="$(fact fallbackLocaleCount)"
LOCALE_VARIANT_COUNT="$(fact localeVariantCount)"
BEAN_VALIDATION_CONSTRAINTS="$(fact beanValidationConstraints)"
SCHEMA_EXPORT_FORMATS="$(fact schemaExportFormats)"

for value in \
    "${DEVELOPMENT_VERSION}" \
    "${LATEST_GA_VERSION}" \
    "${JAVA_MINIMUM_VERSION}" \
    "${PUBLISHED_MODULES}" \
    "${NATIVE_LOCALE_COUNT}" \
    "${FALLBACK_LOCALE_COUNT}" \
    "${LOCALE_VARIANT_COUNT}" \
    "${BEAN_VALIDATION_CONSTRAINTS}" \
    "${SCHEMA_EXPORT_FORMATS}"; do
    [[ -n "${value}" ]] || fail "a required gradle.properties fact is empty"
done

SUPPORTED_LOCALE_FILE="${REPO_ROOT}/core/src/main/java/io/github/frikit/krandom/generator/locale/SupportedLocale.java"
ACTUAL_TOTAL="$(grep -Ec '^    [A-Z]{2}_[A-Z]{2}\(' "${SUPPORTED_LOCALE_FILE}")"
ACTUAL_FALLBACK="$(grep -Ec '^    [A-Z]{2}_[A-Z]{2}\("[a-z]+", "[A-Z]+",' "${SUPPORTED_LOCALE_FILE}")"
ACTUAL_NATIVE="$(( ACTUAL_TOTAL - ACTUAL_FALLBACK ))"

[[ "${ACTUAL_TOTAL}" == "${LOCALE_VARIANT_COUNT}" ]] || fail "SupportedLocale has ${ACTUAL_TOTAL} variants, expected ${LOCALE_VARIANT_COUNT}"
[[ "${ACTUAL_NATIVE}" == "${NATIVE_LOCALE_COUNT}" ]] || fail "SupportedLocale has ${ACTUAL_NATIVE} native variants, expected ${NATIVE_LOCALE_COUNT}"
[[ "${ACTUAL_FALLBACK}" == "${FALLBACK_LOCALE_COUNT}" ]] || fail "SupportedLocale has ${ACTUAL_FALLBACK} fallback variants, expected ${FALLBACK_LOCALE_COUNT}"

BEAN_VALIDATION_FILE="${REPO_ROOT}/core/src/main/java/io/github/frikit/krandom/generator/object/BeanValidationSupport.java"
BEAN_VALIDATION_GUIDE="${REPO_ROOT}/docs-site/guides/bean-validation.md"
IFS=',' read -r -a constraints <<< "${BEAN_VALIDATION_CONSTRAINTS}"
for constraint in "${constraints[@]}"; do
    grep -Fq "import jakarta.validation.constraints.${constraint};" "${BEAN_VALIDATION_FILE}" || fail "Bean Validation fact ${constraint} is not implemented"
    grep -Fq "| \`@${constraint}\` |" "${BEAN_VALIDATION_GUIDE}" || fail "Bean Validation guide does not document ${constraint}"
done
ACTUAL_CONSTRAINT_COUNT="$(grep -Ec '^import jakarta\.validation\.constraints\.' "${BEAN_VALIDATION_FILE}")"
[[ "${ACTUAL_CONSTRAINT_COUNT}" == "${#constraints[@]}" ]] || fail "BeanValidationSupport imports ${ACTUAL_CONSTRAINT_COUNT} constraints, facts list ${#constraints[@]}"

SCHEMA_FILE="${REPO_ROOT}/core/src/main/java/io/github/frikit/krandom/generator/schema/Schema.java"
IFS=',' read -r -a schema_formats <<< "${SCHEMA_EXPORT_FORMATS}"
for format in "${schema_formats[@]}"; do
    case "${format}" in
        CSV) method="toCsv" ;;
        JSONL) method="toJsonLines" ;;
        XML) method="toXml" ;;
        SQL) method="toSqlInserts" ;;
        *) fail "unknown schema export format fact: ${format}" ;;
    esac
    grep -Fq " ${method}(" "${SCHEMA_FILE}" || fail "Schema export fact ${format} has no ${method} method"
done

IFS=',' read -r -a modules <<< "${PUBLISHED_MODULES}"
for module in "${modules[@]}"; do
    grep -Fq "krandom-${module}" "${REPO_ROOT}/README.md" || fail "README does not list krandom-${module}"
done

CORE_MODULE_NAME="io.github.frikit.krandom"
CORE_MODULE_DESCRIPTOR="${REPO_ROOT}/core/src/main/java/module-info.java"
OBJECT_GENERATION_GUIDE="${REPO_ROOT}/docs-site/guides/object-generation.md"
grep -Fq "module ${CORE_MODULE_NAME} {" "${CORE_MODULE_DESCRIPTOR}" || fail "core module descriptor name changed"
grep -Fq "opens com.example.fixtures.model to ${CORE_MODULE_NAME};" "${OBJECT_GENERATION_GUIDE}" || fail "object guide is missing the qualified opens contract"
grep -Fq "opens io.github.frikit.krandom.examples.jpms.openconsumer to ${CORE_MODULE_NAME};" \
    "${REPO_ROOT}/examples/java-jpms/open-consumer/src/main/java/module-info.java" || fail "JPMS open consumer does not exercise the documented core module name"

grep -Fq "The latest released version is \`${LATEST_GA_VERSION}\`" "${REPO_ROOT}/README.md" || fail "README latest GA version is stale"
grep -Fq "The current version is \`${LATEST_GA_VERSION}\`" "${REPO_ROOT}/docs-site/getting-started.md" || fail "getting-started latest GA version is stale"
grep -Fq "${NATIVE_LOCALE_COUNT} native locale datasets" "${REPO_ROOT}/docs-site/guides/locale-aware-data.md" || fail "native locale documentation is stale"
grep -Fq "${FALLBACK_LOCALE_COUNT} curated fallback variants" "${REPO_ROOT}/docs-site/guides/locale-aware-data.md" || fail "fallback locale documentation is stale"
grep -Fq "${LOCALE_VARIANT_COUNT} supported locale variants" "${REPO_ROOT}/docs-site/guides/locale-aware-data.md" || fail "total locale documentation is stale"
grep -Fq 'krandom-junit` (from `1.2.0`)' "${REPO_ROOT}/docs-site/getting-started.md" || fail "JUnit module introduction version is stale"
for term in 'Format-valid' 'Checksum-valid' 'Semantically plausible' 'Test-safe / non-routable'; do
    grep -Fq "${term}" "${REPO_ROOT}/docs-site/guides/data-validity-and-safety.md" || fail "data validity term is missing: ${term}"
done
if rg -n 'SecureRandom' "${REPO_ROOT}/spring-boot-starter/src/main/java/io/github/frikit/krandom/spring/KrandomProperties.java"; then
    fail "Spring properties still claim SecureRandom is the unseeded default"
fi

if rg -n 'io\.github\.frikit:krandom-[^`" ]*:1\.0\.0|<version>1\.0\.0</version>' \
    "${REPO_ROOT}/README.md" \
    "${REPO_ROOT}/docs-site" \
    "${REPO_ROOT}/docs/migration"; then
    fail "current installation/migration documentation still contains 1.0.0 coordinates"
fi

if rg -n '<locale>_(first_male|first_female|last|street_names|street_types_short|street_types_long|secondary_units)\.txt' \
    "${REPO_ROOT}/docs/locale-contribution-guide.md"; then
    fail "locale contribution guide still contains the obsolete flat resource layout"
fi

echo "Documentation facts verified: ${LATEST_GA_VERSION} GA, ${DEVELOPMENT_VERSION} development, ${#modules[@]} modules, ${ACTUAL_TOTAL} locale variants."
