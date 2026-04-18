#!/bin/bash
# Verify that the active Java runtime is JDK 21 or newer.

set -euo pipefail

if ! command -v java >/dev/null 2>&1; then
    echo "Java is not available on PATH."
    echo "Install JDK 21+ and set JAVA_HOME before running kRandom build commands."
    exit 1
fi

JAVA_SPEC_VERSION="$(java -XshowSettings:properties -version 2>&1 | sed -n 's/^[[:space:]]*java.specification.version = //p' | head -n 1)"

if [ -z "${JAVA_SPEC_VERSION}" ]; then
    JAVA_SPEC_VERSION="$(java -version 2>&1 | sed -n '1s/.*version "\(.*\)".*/\1/p')"
fi

JAVA_MAJOR="${JAVA_SPEC_VERSION%%.*}"
if [ "${JAVA_MAJOR}" = "1" ]; then
    JAVA_MAJOR="$(printf '%s' "${JAVA_SPEC_VERSION}" | cut -d. -f2)"
fi

if ! [[ "${JAVA_MAJOR}" =~ ^[0-9]+$ ]]; then
    echo "Unable to determine Java major version from: ${JAVA_SPEC_VERSION}"
    echo "Set JAVA_HOME to a JDK 21 installation before running kRandom build commands."
    exit 1
fi

if [ "${JAVA_MAJOR}" -lt 21 ]; then
    echo "kRandom requires Java 21+ for local development."
    echo "Detected Java: ${JAVA_SPEC_VERSION}"
    if [ -n "${JAVA_HOME:-}" ]; then
        echo "JAVA_HOME is currently: ${JAVA_HOME}"
    else
        echo "JAVA_HOME is not set."
    fi
    echo "Set JAVA_HOME to a JDK 21 installation and ensure 'java -version' reports 21+."
    exit 1
fi
