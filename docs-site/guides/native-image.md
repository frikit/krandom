---
layout: page
title: GraalVM Native Image
permalink: /guides/native-image/
---

# GraalVM Native Image

`krandom-core` ships experimental GraalVM reachability metadata for its bundled text resources
and object-generation internals. Ordinary generators—including primitive, locale, HTTP-fixture,
schema, and local data-pack generators—need no application-specific reflection configuration.

Object generation is different: kRandom intentionally reflects over your model classes at runtime.
GraalVM cannot discover arbitrary application types from the kRandom dependency alone. Add your
model classes to your application's reachability metadata and keep their constructors and fields
available for reflection.

For a record or ordinary model used by `ObjectGenerator`, include its declared constructors,
methods, and fields in the application metadata:

```json
{
  "reflection": [
    {
      "type": "com.example.fixtures.Customer",
      "allDeclaredConstructors": true,
      "allDeclaredMethods": true,
      "allDeclaredFields": true
    }
  ]
}
```

Named-module applications still need the same qualified `opens` relationship as on the JVM:

```java
module com.example.fixtures {
    requires io.github.frikit.krandom;

    opens com.example.fixtures.model to io.github.frikit.krandom;
}
```

If metadata or module access is missing, object generation fails with contextual reflection
diagnostics. Prefer explicit object factories for third-party or intentionally encapsulated model
types; this avoids reflective construction for that value.

## Local verification

Run the optional smoke check under a GraalVM JDK with `native-image` installed:

```bash
./scripts/verify_native_image.sh
```

The script builds a small deterministic core fixture into a native executable. On a regular JDK it
prints a skip message and exits successfully, so normal repository checks remain portable.
