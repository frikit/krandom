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

The same token registry now powers declarative templates:

```java
Field field = Generators.ofField(Locale.US)
        .register("custom.order_id", ctx -> 1000 + ctx.recordIndex())
        .registerAlias("custom.order", "custom.order_id");

SchemaValueProvider subject = field.template("Order {{custom.order}} for {{person.full_name}} (??-##)");

SchemaValueProvider payload = field.template(Map.of(
        "orderId", "{{custom.order}}",
        "customer", "{{person.full_name}}",
        "email", "{{person.email}}",
        "reference", "ORD-##",
        "tags", List.of("{{text.word}}", "{{text.word}}")
));
```

String templates always render to strings. Payload templates recurse through maps, lists, and arrays; strings that contain only a single `{{token}}` resolve to the raw generated value, so numbers and nested objects stay typed inside the generated payload.

## Provider hub

`ProviderHub` is a generic registry with aliases, locale propagation, runtime extensibility, and a broader dotted taxonomy.

```java
ProviderHub hub = Generators.ofProviderHub(Locale.US);

FullNameGenerator person = hub.get("person.full_name", FullNameGenerator.class);
EmailGenerator email = hub.get("person.email", EmailGenerator.class);
CityGenerator city = hub.get("address.city", CityGenerator.class);
URLGenerator internet = hub.get("url", URLGenerator.class); // alias for internet.url
TextFormatProvider format = hub.get("text.format", TextFormatProvider.class);

hub.register("custom-product-code", cfg -> new IdentifierMaskGenerator(cfg));
hub.registerAlias("sku", "custom-product-code");

String sku = format.template("SKU-??-####");
String coupon = format.lexify("promo-????");
String token = format.asciify("***-***");
String reference = format.regexify("[A-Z]{3}\\d{4}");
```

The old coarse names like `person`, `address`, `internet`, `finance`, `datetime`, `text`, and `code` still work, but the preferred naming style is dotted and explicit:

- `person.full_name`, `person.first_name`, `person.last_name`, `person.email`, `person.username`
- `address.street_address`, `address.city`, `address.state`, `address.postal_code`, `address.country`, `address.phone_number`
- `internet.url`, `internet.domain`, `internet.hostname`
- `finance.money`, `finance.currency`
- `datetime.date`, `datetime.time`
- `text.word`, `text.sentence`, `text.paragraph`, `text.format`
- `code.uuid`

`ProviderHub` also exposes uniqueness helpers directly, so provider-based generation can stay on the same surface:

```java
UniqueGenerator<UUID> uniqueIds = hub.unique(new UUIDGenerator());
UUID first = uniqueIds.generate();
UUID second = uniqueIds.generate();
```

## Conflict policies

- `ConflictPolicy.FAIL` (default): reject duplicate registration.
- `ConflictPolicy.REPLACE`: override an existing provider or alias.
