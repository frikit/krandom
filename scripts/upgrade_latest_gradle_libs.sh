#!/bin/sh

curr=${PDW}
cd ../

latest="latest"

echo Update jacoco lib
url="https://mvnrepository.com/artifact/org.jacoco/org.jacoco.agent/"

echo Follow redirects to find version...
redirectUrl=$(curl -Lis -o /dev/null -w %{url_effective} ${url}${latest})

echo "Find url [$redirectUrl]"
versionLib=${redirectUrl/$url/""}
echo "Find version [$versionLib]"

sed -i.back "s/\(^jacoco_version=\).*/\1$versionLib/g" gradle.properties

echo Update kotlin lib
url="https://mvnrepository.com/artifact/org.jetbrains.kotlin/kotlin-stdlib/"

echo Follow redirects to find version...
redirectUrl=$(curl -Lis -o /dev/null -w %{url_effective} ${url}${latest})

echo "Find url [$redirectUrl]"
versionLib=${redirectUrl/$url/""}
echo "Find version [$versionLib]"

sed -i.back "s/\(^kotlin_version=\).*/\1$versionLib/g" gradle.properties

echo Copy upgrade gradle prop into existing one...
cat gradle.properties > gradle.properties.back

echo Update current gradle.properties...
mv -vf gradle.properties.back gradle.properties

echo Output gradle.properties...
cat gradle.properties

cd ${curr}
