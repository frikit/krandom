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

String jsonl = schema.toJsonLines(10);
String csv = schema.toCsv(10);
String xml = schema.toXml(10);
String sql = schema.toSqlInserts("public.orders", 10);
```

Use the streaming writer methods when you want payload output without materializing the whole batch first:

```java
StringBuilder out = new StringBuilder();
schema.writeJsonLines(out, 1000);
schema.writeCsv(out, 1000);
schema.writeXml(out, 1000);
schema.writeSqlInserts(out, "public.orders", 1000);
```

`FieldLookup` is now an extensible token registry rather than a fixed table, and `Field` exposes the same registration flow:

```java
Field field = Generators.ofField(GeneratorConfig.builder().locale(Locale.US).seed(42L).build())
        .register("custom.order_id", ctx -> "ORD-" + ctx.recordIndex())
        .registerAlias("custom.order", "custom.order_id")
        .registerProvider("text.word.provider",
                WordGenerator::new,
                WordGenerator.class,
                WordGenerator::generateWord);

SchemaValueProvider orderId = field.bind("custom.order");
SchemaValueProvider extraWord = field.bind("text.word.provider");
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
