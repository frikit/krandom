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
String json = schema.toJson(10);
String csv = schema.toCsv(10);
String xml = schema.toXml(10);
String sql = schema.toSqlInserts("public.orders", 10);
String yaml = schema.toYaml(10);
String toml = schema.toToml(10);
```

`Field.bind(...)` covers both scalar tokens and composite provider payloads. Composite records are
kept as nested JSON objects in JSONL, JSON, YAML, TOML, and JSON Schema, and as JSON cell/literal
values in CSV, XML, and SQL exports:

```java
Field field = Generators.ofField(Locale.US);

Map<String, SchemaValueProvider> fields = new LinkedHashMap<>();
fields.put("order", field.bind("commerce.order_info"));
fields.put("company", field.bind("company.info"));
fields.put("payment", field.bind("finance.payment_info"));

Schema schema = Generators.ofSchema(Locale.US, fields);

String jsonl = schema.toJsonLines(1);
String json = schema.toJson(1);
String csv = schema.toCsv(1);
String xml = schema.toXml(1);
String sql = schema.toSqlInserts("public.orders", 1);
String yaml = schema.toYaml(1);
String toml = schema.toToml(1);
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

`SchemaParser.fromJsonSchema(...)` implements a deliberately small JSON Schema/OpenAPI subset. It
is a fixture-schema importer, not a general JSON Schema validator or reference resolver:

| Construct | Supported behavior |
| --- | --- |
| Root | An object with a non-empty `properties` map is required. Every record holds each of those properties, as a column, so a root `required` name or property dependency that names another property, or a `minProperties` or `maxProperties` that those properties do not meet, is rejected at `$`. |
| Types | `string`, `integer`, `number`, `boolean`, `array`, `object`, and `null`; a type list may contain exactly one concrete type plus `null`. |
| Scalars | `const`, a non-empty `enum`, numeric bounds, `multipleOf`, string length bounds, and a `pattern` in the [syntax `RegexGenerator` supports]({{ '/guides/data-validity-and-safety/' | relative_url }}#regular-expression-contract). |
| Strings | `email`/`idn-email`, `uri`/`url`, `uuid`, `date`, `date-time`, `time`, `ipv4`, `ipv6`, and `hostname` receive dedicated generation. An unknown *string* format deliberately uses semantic-name or bounded-string fallback rather than pretending to implement it. A schema with no `type` generates a string under the same rules. |
| Structures | `items` as one schema or as a list of schemas, `additionalItems` beside a list, `minItems`/`maxItems`, and `uniqueItems` for arrays; nested `properties`, `required`, `additionalProperties`, `minProperties`/`maxProperties`, and property `dependencies` for objects. An array without `items` produces strings, and `true` and `false` work as schemas that allow any value or none. |
| OpenAPI controls | OpenAPI 3.0 `nullable`, plus the `$schema` that `toJsonSchema()` emits, which is accepted as a document control rather than a generation constraint. |

Constraints apply together, and a keyword constrains only values of its own JSON type:

- A `const` must meet the other constraints, and an `enum` keeps only the values that do, so `enum`
  `[a, bbbb]` with `maxLength` 1 generates `a`. `format` is not checked on these values, because it is
  an annotation in Draft 2020-12. A `pattern` is checked with `java.util.regex` and searches the
  string, as in JSON Schema.
- Values compare as JSON values, so a `const` of `{"id": 1}` matches the `enum` member `{"id": 1.0}`.
  A number compares by its value at any depth, whatever Java type holds it, an array item by item in
  order, an object member by member under the name it is written with, so the key `1` is the member
  `"1"`, and a value written as a string, such as a `LocalDate`, by its text. `NaN` and the infinities
  are not JSON numbers, and an object with two keys written under one name, such as `1` and `"1"`, is
  not a JSON object, so a `const` or `enum` that holds either, at any depth, is rejected with the schema
  path.
- Numeric bounds intersect, so `minimum` 100 with `exclusiveMinimum` 0 starts at 100. OpenAPI 3.0's
  boolean `exclusiveMinimum` and `exclusiveMaximum` work as flags on the inclusive bound. An `integer`
  rounds a fractional bound inward and stays within the range of a `long`.
- `multipleOf` must be a number above 0, and generates exact multiples within the bounds, as decimals,
  with no binary rounding: `multipleOf` 0.01 gives values such as `12.34`. An `integer` stays an
  `Integer` or `Long` and takes the multiples that are whole, so `multipleOf` 2.5 gives multiples of 5. A
  `number` is a `BigDecimal`. Bounds that hold no multiple, such as `minimum` 1 and `maximum` 4 with
  `multipleOf` 5, are rejected with the schema path, and a `const` or `enum` keeps only multiples. A
  missing bound defaults from the declared one, as for any number, and where that default range holds
  no multiple, the open side reaches the nearest one: `minimum` 0.1 with `multipleOf` 1000000000 gives
  1000000000. An `integer` multiple must fit a `long`.
- `uniqueItems: true` gives arrays whose items are distinct JSON values, so `1` and `1.0` count as one.
  While parsing, a search with a fixed seed looks for `maxItems` distinct items, drawing up to 100 for
  each. Items that cannot reach `minItems`, such as an `enum` of two values with `minItems` 3, are
  rejected with the schema path, and where the search finds fewer than `maxItems`, an array holds at
  most that many. Each array then draws up to 100 items for each item it holds, keeps the distinct ones
  found if they run out, and fails with the schema path below `minItems`.
- A list of `items` is the tuple form of the drafts up to 2019-09: each position follows its own schema,
  and `additionalItems` is the schema of the items after them. The parser reads both forms whatever
  `$schema` says, and rejects `prefixItems`, the Draft 2020-12 replacement. An array holds the whole tuple
  unless `minItems` or `maxItems` moves its size, so `minItems` 0 allows shorter arrays and `maxItems` 4
  allows two items after a tuple of two. Without `additionalItems`, the items after the tuple are
  strings, as for an array without `items`. `additionalItems: false` ends an array with the tuple, and a
  `false` entry ends it before that position, so a `minItems` past either is rejected with the schema
  path, and `items: false` allows only the empty array. `additionalItems` beside a single `items` schema
  is ignored, as those drafts say. With `uniqueItems`, each item is drawn for the position it fills, so
  an array ends early at a position that has no new value, and fails below `minItems`.
- A nested object holds every property it declares unless its other keywords ask otherwise.
  `maxProperties` leaves out optional properties, chosen at random, until the object fits, and
  `minProperties` adds as few additional properties as it needs, named `property1`, `property2`, and so on, past every
  name the schema uses, with values from the `additionalProperties` schema, or strings without one. A
  `required` property that is not in `properties` is generated the same way, and its name chooses a
  realistic value where it can, so `required: ["email"]` gives an email address. Where
  `additionalProperties` is `false`, none of these can be added, so the schema is rejected with its path.
  A property schema `true` generates a string, as `{}` does, and `false` leaves the property out, so a
  `required` property with either `false` or a value that is no schema is rejected.
- A property dependency, such as `"dependencies": {"card": ["billing"]}`, brings the properties it
  names, through every link, beside the property that needs them, so `required` brings them too. A
  property whose dependencies cannot be generated is left out, and rejected if it is required.
  `maxProperties` leaves out a property only after every property that depends on it, and properties
  that depend on each other only together; where `additionalProperties` is `false`, `minProperties` and
  `maxProperties` must then leave a range as wide as each such group. A schema dependency, which asks an
  object to meet a second schema, needs schema composition, so it is rejected with its path, such as
  `$.properties.order.dependencies.card`. `dependencies` is the form of the drafts up to 7, read
  whatever `$schema` says; `dependentRequired` and `dependentSchemas`, the Draft 2019-09 forms, are
  rejected.
- A `pattern` or `format` value is drawn again until it fits `minLength` and `maxLength`, up to 10,000
  times each. Length is counted in characters, not UTF-16 units.
- A value generated from a `pattern` matches the whole pattern, which also satisfies the search. A
  `pattern` that `RegexGenerator` cannot generate from, such as one with a lookahead, is rejected with
  its schema path. A `const` or `enum` beside it is only checked against it, so it can use any Java
  regex.

A combination that leaves no value is rejected with its schema path, such as
`$.properties.parent.properties.code` or `$.properties.pair.items[1]`. That includes a nested `items`,
tuple entry, `additionalItems`, or property schema, even where an empty array or an object without that
property would be valid, because the importer resolves every schema it is given. Pattern and format
lengths are judged by sampling 1,000 values while parsing,
rather than by analysing the pattern. A combination that rarely fits can be rejected too, and one
that passes parsing can still fail on a later record. A pattern's `*`, `+`, and `{n,}` repeat up to
twice `minLength` when that is more than their usual 10 times, up to 10,000, so `^[a-z]+$` with
`minLength` 20 generates 20 to 40 characters.

`$ref`, dynamic/recursive references, `allOf`, `anyOf`, `oneOf`, `not`, conditional schemas (`if`,
`then`, `else`), `contains`, `prefixItems`, `patternProperties`, `propertyNames`, `dependentRequired`,
`dependentSchemas`, schema dependencies, and the `unevaluated` keywords are unsupported. They fail
during parsing with the nested schema path. Malformed `type`, `format`, `enum`, and `items` values, and a malformed `additionalItems`
beside a list of items, also fail during parsing with that path; they never silently become an
unconstrained field. A Java map that recursively contains itself is
rejected for the same reason: the importer has no document-ID or reference-resolution model.

Schema export is metadata-driven and side-effect free. Built-in `Field` providers carry an exact
fragment; a custom lambda with no metadata deliberately exports `{}` to mean **unknown**, not an
invented type. Attach `SchemaValueProvider.withJsonSchema(...)` or `withSample(...)` when a custom
provider can make a narrower claim. `Schema`, `SchemaProjection`, and parser-created generators are
stateful: use one instance per thread (or `Generators.threadLocal(...)`) and share only immutable
configuration and completed provider registrations.

Use the streaming writer methods when you want payload output without materializing the whole batch first:

```java
StringBuilder out = new StringBuilder();
schema.writeJsonLines(out, 1000);
schema.writeJson(out, 1000);
schema.writeCsv(out, 1000);
schema.writeXml(out, 1000);
schema.writeSqlInserts(out, "public.orders", 1000);
schema.writeYaml(out, 1000);
schema.writeToml(out, 1000);
```

For stream-based output, `writeTo(OutputStream, OutputFormat, count)` writes UTF-8 and flushes its adapter writer without closing the caller-owned stream. The `Writer` overload also leaves ownership with the caller.

`JSON` is an array document; `JSONL` stays one object per line. YAML renders an ordinary sequence
and TOML renders an array of `records` tables. TOML has no `null` value, so its writer rejects a
null field instead of silently changing data.

### Projecting existing objects

`SchemaProjection<T>` applies named extractors to objects you already have. It consumes an
`Iterable` once and writes incrementally, so it does not require a duplicate map batch:

```java
record Order(String id, int quantity) {}

Map<String, Function<Order, ?>> columns = new LinkedHashMap<>();
columns.put("id", Order::id);
columns.put("quantity", Order::quantity);

SchemaProjection<Order> projection = SchemaProjection.of(columns);
List<Order> orders = List.of(new Order("ORD-1", 2));
String json = projection.toJson(orders);
String yaml = projection.toYaml(orders);
projection.writeTo(writer, OutputFormat.SQL, orders, "public.orders");
```

Projection supports the same JSONL, JSON, CSV, XML, SQL, YAML, and TOML formats as generated
schemas. `Schema` is still the right API when values must be generated from `Field` providers;
use `SchemaProjection` when the values already exist.

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
