#!/bin/bash

curr=${PDW}
cd ../

echo Make gradlew executable
chmod +x gradlew

echo Upgrade gradle wrapper...
version_json=$(curl https://services.gradle.org/versions/current)

version_line=""

while read -r line; do
    if [[ ${line} == *"version"* ]]; then
        version_line=${line}
    fi
done <<< "$version_json"

version_line=${version_line/\"version\" : \"/""}
version_line=${version_line/\",/""}

echo "Version is [${version_line}]"

latest_version=${version_line}
type="ALL"

echo Upgrade gradle version to "${latest_version}"
./gradlew wrapper --gradle-version "${latest_version}" --distribution-type=${type} --warning-mode=ALL

sleep 1

./gradlew

cd "${curr}" || exit
