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

`Field.bind(...)` covers both scalar tokens and composite provider payloads. Composite records are
kept as nested JSON objects in JSONL and JSON Schema, and as JSON cell/literal values in CSV, XML,
and SQL exports:

```java
Field field = Generators.ofField(Locale.US);

Map<String, SchemaValueProvider> fields = new LinkedHashMap<>();
fields.put("order", field.bind("commerce.order_info"));
fields.put("company", field.bind("company.info"));
fields.put("payment", field.bind("finance.payment_info"));

Schema schema = Generators.ofSchema(Locale.US, fields);

String jsonl = schema.toJsonLines(1);
String csv = schema.toCsv(1);
String xml = schema.toXml(1);
String sql = schema.toSqlInserts("public.orders", 1);
Map<String, Object> jsonSchema = schema.toJsonSchema();
```

`Schema.toJsonSchema()` is metadata-driven and does not call providers. This means exporting a
JSON Schema will not advance seeded generator sequences. Built-in `Field.bind(...)`, `Field.list(...)`,
`Field.nested(...)`, constants, and templates carry JSON Schema metadata automatically. For custom
lambda providers, the default JSON Schema fragment is unconstrained (`{}`) unless you attach metadata:

```java
SchemaValueProvider typedAmount = SchemaValueProvider.withSample(
        ctx -> 1000 + ctx.recordIndex(),
        1000
);

SchemaValueProvider typedPayload = SchemaValueProvider.withJsonSchema(
        ctx -> Map.of("status", "READY"),
        Map.of(
                "type", "object",
                "additionalProperties", false,
                "properties", Map.of("status", Map.of("type", "string")),
                "required", List.of("status")
        )
);
```

### JSON Schema import subset

`SchemaParser.fromJsonSchema(...)` accepts object schemas using scalar `type` values, nullable type
lists, `format`, `enum`, `const`, numeric/string/array bounds, `pattern`, `items`, and nested
`properties`. It also accepts `$schema`, `required`, and `additionalProperties`, so a schema emitted
by `toJsonSchema()` can be imported again. References and composition are intentionally unsupported:
`$ref`, `allOf`, `anyOf`, `oneOf`, `not`, conditional schemas, and pattern/dependent-property
composition fail immediately with the nested schema path. Unknown string formats use the normal
semantic-or-string fallback; they do not claim an unimplemented format contract.

Use the streaming writer methods when you want payload output without materializing the whole batch first:

```java
StringBuilder out = new StringBuilder();
schema.writeJsonLines(out, 1000);
schema.writeCsv(out, 1000);
schema.writeXml(out, 1000);
schema.writeSqlInserts(out, "public.orders", 1000);
```

For stream-based output, `writeTo(OutputStream, OutputFormat, count)` writes UTF-8 and flushes its adapter writer without closing the caller-owned stream. The `Writer` overload also leaves ownership with the caller.

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
ContactInfoGenerator contacts = hub.get("person.contact_info", ContactInfoGenerator.class);
ProductInfoGenerator products = hub.get("commerce.product_info", ProductInfoGenerator.class);
OrderInfoGenerator orders = hub.get("commerce.order_info", OrderInfoGenerator.class);
ShipmentInfoGenerator shipments = hub.get("commerce.shipment_info", ShipmentInfoGenerator.class);
BankInfoGenerator banks = hub.get("finance.bank_info", BankInfoGenerator.class);
InvoiceInfoGenerator invoices = hub.get("finance.invoice_info", InvoiceInfoGenerator.class);
PaymentInfoGenerator payments = hub.get("finance.payment_info", PaymentInfoGenerator.class);
CompanyInfoGenerator companies = hub.get("company.info", CompanyInfoGenerator.class);
CityGenerator city = hub.get("address.city", CityGenerator.class);
URLGenerator internet = hub.get("url", URLGenerator.class); // alias for internet.url
TextFormatProvider format = hub.get("text.format", TextFormatProvider.class);

hub.register("custom-product-code", cfg -> new IdentifierMaskGenerator(cfg));
hub.registerAlias("sku", "custom-product-code");

String sku = format.template("SKU-??-####");
String coupon = format.lexify("promo-????");
String token = format.asciify("***-***");
String reference = format.regexify("[A-Z]{3}\\d{4}");
String invoice = format.examplify("INV-2026-AB12");
```

The old coarse names like `person`, `address`, `internet`, `finance`, `datetime`, `text`, and `code` still work, but the preferred naming style is dotted and explicit:

- `person.full_name`, `person.first_name`, `person.last_name`, `person.email`, `person.username`
- `person.contact_info`, `person.person_info`, `person.job_info`
- `company.name`, `company.email`, `company.url`, `company.buzzword`, `company.catch_phrase`, `company.industry`, `company.info`
- `address.address_info`
- `commerce.product_info`
- `address.street_address`, `address.city`, `address.state`, `address.postal_code`, `address.country`, `address.phone_number`
- `internet.url`, `internet.domain`, `internet.hostname`
- `finance.money`, `finance.currency`, `finance.bank_info`, `finance.credit_card_info`
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
