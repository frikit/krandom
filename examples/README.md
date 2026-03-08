# kRandom Examples Monorepo

This folder contains test-based examples for each supported language/build-tool combination.

Version used in all examples: `0.1.0`

## Matrix

- Java + Gradle: `examples/java-gradle`
- Java + Maven: `examples/java-maven`
- Kotlin + Gradle: `examples/kotlin-gradle`
- Kotlin + Maven: `examples/kotlin-maven`
- Scala + sbt: `examples/scala-sbt`
- Scala + Mill: `examples/scala-mill`

## Run commands

```bash
# Java
cd examples/java-gradle && ./gradlew test
cd examples/java-maven && mvn -q test

# Kotlin
cd examples/kotlin-gradle && ./gradlew test
cd examples/kotlin-maven && mvn -q test

# Scala
cd examples/scala-sbt && sbt test
cd examples/scala-mill && mill -i app.test
```
