#!/usr/bin/env bash
# Verifies JPMS module boundaries across every published jar (master plan Step 3.8):
#  - each jar declares a module identity (module-info.class or Automatic-Module-Name);
#  - module names are unique and match the compatibility contract below;
#  - no package is split across two published jars.
# The module list comes from `publishedModules` in gradle.properties (the BOM has no jar).
set -euo pipefail

cd "$(dirname "$0")/.."

gradle_property() {
    awk -F= -v key="$1" '$1 == key { print substr($0, index($0, "=") + 1) }' gradle.properties
}

DEVELOPMENT_VERSION="$(gradle_property developmentVersion)"
PUBLISHED_MODULES="$(gradle_property publishedModules)"
if [[ -z "${DEVELOPMENT_VERSION}" || -z "${PUBLISHED_MODULES}" ]]; then
    echo "FAIL: gradle.properties must define developmentVersion and publishedModules" >&2
    exit 1
fi

MODULES=()
IFS=',' read -r -a published <<< "${PUBLISHED_MODULES}"
for module in "${published[@]}"; do
    [[ "${module}" == "bom" ]] || MODULES+=("${module}")
done

GRADLEW=./gradlew
if [[ -n "${JAVA_HOME:-}" ]]; then
    export JAVA_HOME
fi

echo "Assembling published jars..."
jar_tasks=()
for module in "${MODULES[@]}"; do
    jar_tasks+=(":${module}:jar")
done
"${GRADLEW}" --quiet "${jar_tasks[@]}"

WORKDIR=$(mktemp -d)
trap 'rm -rf "${WORKDIR}"' EXIT

PACKAGES_FILE="${WORKDIR}/packages.txt"
NAMES_FILE="${WORKDIR}/names.txt"
: > "${PACKAGES_FILE}"
: > "${NAMES_FILE}"

fail=0
for module in "${MODULES[@]}"; do
    # Select the jar for the current development version; build/libs can hold stale release jars.
    jar_path="${module}/build/libs/${module}-${DEVELOPMENT_VERSION}.jar"
    if [[ ! -f "${jar_path}" ]]; then
        echo "FAIL: no jar found for ${module} at ${jar_path}" >&2
        fail=1
        continue
    fi

    # Search captured output instead of piping into `grep -q`: under pipefail an early-exiting
    # reader turns the writer's SIGPIPE into a pipeline failure even when the pattern matched.
    listing=$(unzip -l "${jar_path}")

    module_name=""
    if grep -q 'module-info\.class$' <<< "${listing}"; then
        jar_tool="${JAVA_HOME:+${JAVA_HOME}/bin/}jar"
        module_name=$("${jar_tool}" --describe-module --file "${jar_path}" \
            | awk 'NR==1 {print $1}' | cut -d@ -f1)
        identity="module-info"
    else
        manifest=$(unzip -p "${jar_path}" META-INF/MANIFEST.MF)
        module_name=$(printf '%s\n' "${manifest}" | tr -d '\r' \
            | awk -F': ' '/^Automatic-Module-Name/ {print $2}')
        identity="Automatic-Module-Name"
    fi
    if [[ -z "${module_name}" ]]; then
        echo "FAIL: ${module} jar has neither module-info.class nor Automatic-Module-Name" >&2
        fail=1
        continue
    fi
    echo "OK: ${module} -> ${module_name} (${identity})"
    echo "${module_name} ${module}" >> "${NAMES_FILE}"

    # Packages of every class outside META-INF/ (versioned classes included in their base package).
    awk -v module="${module}" '
        $4 ~ /\.class$/ && $4 !~ /(^|\/)module-info\.class$/ && $4 !~ /^META-INF\// {
            package = $4
            sub(/\/[^\/]*\.class$/, "", package)
            print package " " module
        }
    ' <<< "${listing}" | sort -u >> "${PACKAGES_FILE}"
done

EXPECTED_NAMES="io.github.frikit.krandom core
io.github.frikit.krandom.jackson jackson
io.github.frikit.krandom.junit junit
io.github.frikit.krandom.kotlin.dsl kotlin-dsl
io.github.frikit.krandom.kotest kotest-extensions
io.github.frikit.krandom.spring.boot.starter spring-boot-starter"
if ! diff <(printf '%s\n' "${EXPECTED_NAMES}" | sort) <(sort "${NAMES_FILE}") > /dev/null; then
    echo "FAIL: module names changed; they are part of the compatibility contract:" >&2
    diff <(printf '%s\n' "${EXPECTED_NAMES}" | sort) <(sort "${NAMES_FILE}") >&2 || true
    fail=1
fi

duplicate_names=$(awk '{print $1}' "${NAMES_FILE}" | sort | uniq -d)
if [[ -n "${duplicate_names}" ]]; then
    echo "FAIL: duplicate module names:" >&2
    echo "${duplicate_names}" >&2
    fail=1
fi

split_packages=$(sort -u "${PACKAGES_FILE}" | awk '{print $1}' | sort | uniq -d)
if [[ -n "${split_packages}" ]]; then
    echo "FAIL: packages split across published jars:" >&2
    for pkg in ${split_packages}; do
        awk -v package="${pkg}" '$1 == package' "${PACKAGES_FILE}" | sort -u >&2
    done
    fail=1
fi

if [[ ${fail} -ne 0 ]]; then
    echo "Module boundary verification FAILED" >&2
    exit 1
fi
echo "Module boundary verification passed: unique module identities, no split packages."
