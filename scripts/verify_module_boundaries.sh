#!/usr/bin/env bash
# Verifies JPMS module boundaries across every published jar (master plan Step 3.8):
#  - each jar declares a module identity (module-info.class or Automatic-Module-Name);
#  - module names are unique;
#  - no package is split across two published jars.
set -euo pipefail

cd "$(dirname "$0")/.."

MODULES=(core jackson junit kotlin-dsl kotest-extensions spring-boot-starter)

GRADLEW=./gradlew
if [[ -n "${JAVA_HOME:-}" ]]; then
    export JAVA_HOME
fi

echo "Assembling published jars..."
"${GRADLEW}" --quiet $(printf ':%s:jar ' "${MODULES[@]}")

WORKDIR=$(mktemp -d)
trap 'rm -rf "${WORKDIR}"' EXIT

PACKAGES_FILE="${WORKDIR}/packages.txt"
NAMES_FILE="${WORKDIR}/names.txt"
: > "${PACKAGES_FILE}"
: > "${NAMES_FILE}"

fail=0
for module in "${MODULES[@]}"; do
    jar_path=$(find "${module}/build/libs" -maxdepth 1 -name '*.jar' \
        ! -name '*-sources.jar' ! -name '*-javadoc.jar' | head -1)
    if [[ -z "${jar_path}" ]]; then
        echo "FAIL: no jar found for ${module}" >&2
        fail=1
        continue
    fi

    listing=$(unzip -l "${jar_path}")

    module_name=""
    # grep on a captured listing: `unzip -l | grep -q` under pipefail reports unzip's SIGPIPE
    # as a pipeline failure even when the pattern matched.
    if printf '%s\n' "${listing}" | grep 'module-info.class' > /dev/null; then
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

    printf '%s\n' "${listing}" \
        | awk '{print $4}' \
        | grep '\.class$' \
        | grep -v 'module-info.class' \
        | grep -v 'META-INF/' \
        | sed 's|/[^/]*\.class$||' \
        | sort -u \
        | while read -r pkg; do
            echo "${pkg} ${module}" >> "${PACKAGES_FILE}"
        done
done

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
        grep "^${pkg} " "${PACKAGES_FILE}" | sort -u >&2
    done
    fail=1
fi

if [[ ${fail} -ne 0 ]]; then
    echo "Module boundary verification FAILED" >&2
    exit 1
fi
echo "Module boundary verification passed: unique module identities, no split packages."
