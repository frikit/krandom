---
layout: page
title: Java API Module
permalink: /java-api/
---

# Java API Module

## Current contract

`java-api` is currently a thin dependency wrapper around `core`.

In this phase:

- Java behavior and feature parity live in `core`.
- `java-api` exists to provide a stable Java-facing artifact boundary.
- Releasing `java-api` is equivalent to releasing `core` functionality.

## Why this shape

- Avoids duplicating implementation while Java feature work is moving quickly.
- Keeps package and import paths stable for Java users.
- Leaves room for future module-specific API facades without breaking existing usage.

## Dependency behavior

`java-api/build.gradle.kts`:

```kotlin
dependencies {
    implementation(project(":core"))
}
```

So importing `java-api` gives the same generator surface as importing `core` directly.
