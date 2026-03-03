---
layout: page
title: Schema and Provider Hub
permalink: /guides/schema-and-provider-hub/
---

# Schema and Provider Hub

## Schema DSL (`Field` + `Schema`)

```java
Field f = Generators.ofField(Locale.US);

Map<String, SchemaValueProvider> fields = new LinkedHashMap<>();
fields.put("name", f.bind("person.full_name"));
fields.put("email", f.bind("person.email"));
fields.put("tags", f.list(f.bind("text.word"), 2, 4));
fields.put("address", f.nested(Map.of(
        "city", f.bind("address.city"),
        "country", f.bind("address.country")
)));

Schema schema = Generators.ofSchema(Locale.US, fields);
List<Map<String, Object>> batch = schema.generateBatch(10);
```

## Provider hub

`ProviderHub` is a generic registry with aliases, locale propagation, and runtime extensibility.

```java
ProviderHub hub = Generators.ofProviderHub(Locale.US);

FullNameGenerator person = hub.get("person", FullNameGenerator.class);
URLGenerator internet = hub.get("url", URLGenerator.class); // alias

hub.register("custom-product-code", cfg -> new IdentifierMaskGenerator(cfg));
hub.registerAlias("sku", "custom-product-code");
```

## Conflict policies

- `ConflictPolicy.FAIL` (default): reject duplicate registration.
- `ConflictPolicy.REPLACE`: override an existing provider or alias.
