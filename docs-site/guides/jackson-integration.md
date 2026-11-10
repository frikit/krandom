---
layout: page
title: Jackson Integration
permalink: /guides/jackson-integration/
---

# Jackson Integration

Use `krandom-jackson` when you want Jackson to understand kRandom schema definitions.

## Jackson 3

`krandom-jackson` is built for **Jackson 3** (`tools.jackson.core:jackson-databind`, packages
`tools.jackson.*`) and brings `jackson-databind` 3 transitively. Jackson 3 has no annotations module
of its own: it uses the 2.x `com.fasterxml.jackson.core:jackson-annotations` (package
`com.fasterxml.jackson.annotation`), and `jackson-databind` brings the matching version, `2.22` for
Jackson 3.2. Leave that version to Jackson. The artifact's `3.0-rc` versions are pre-releases that
Jackson 3 does not use, although Maven Central lists `3.0-rc5` as its latest release.

`KrandomJacksonModule` is a Jackson 3 module. It cannot be registered with a Jackson 2
(`com.fasterxml.jackson.databind`) `ObjectMapper`: kRandom 2.10.0 is the last release built for
Jackson 2. On Jackson 2, either stay on that release or register a serializer of your own that writes
`schema.toJsonSchema()`.

When Spring Boot 4 auto-configures Jackson, it builds a Jackson 3 `JsonMapper` and registers every
`JacksonModule` bean with it, so a `KrandomJacksonModule` bean is enough there; see
[Spring Boot Starter]({{ '/guides/spring-boot-starter/' | relative_url }}#jackson-version).

## Dependency

Use Maven Central:

```kotlin
repositories {
    mavenCentral()
}

dependencies {
    implementation("io.github.frikit:krandom-core:2.12.0")
    implementation("io.github.frikit:krandom-jackson:2.12.0")
}
```

## Configure A JsonMapper

Jackson 3 mappers are immutable, so add the module while the mapper is built:

```java
import io.github.frikit.krandom.jackson.KrandomJacksonModule;
import tools.jackson.databind.json.JsonMapper;

JsonMapper mapper = JsonMapper.builder()
        .addModule(new KrandomJacksonModule())
        .build();
```

`KrandomJackson.configure(builder)` adds the module to any Jackson 3 mapper builder and returns the
same builder, and `KrandomJackson.newJsonMapper()` is the shortest setup path:

```java
import io.github.frikit.krandom.jackson.KrandomJackson;

JsonMapper configured = KrandomJackson.configure(JsonMapper.builder()).build();
JsonMapper mapper = KrandomJackson.newJsonMapper();
```

Jackson 3 methods throw the unchecked `tools.jackson.core.JacksonException`, so serializing needs no
`try`/`catch`.

## Serialize A Schema As JSON Schema

```java
import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.schema.Field;
import io.github.frikit.krandom.generator.schema.Schema;
import io.github.frikit.krandom.generator.schema.SchemaValueProvider;
import io.github.frikit.krandom.jackson.KrandomJackson;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

GeneratorConfig cfg = GeneratorConfig.builder()
        .locale(Locale.US)
        .seed(42L)
        .build();

Field field = new Field(cfg);
Map<String, SchemaValueProvider> fields = new LinkedHashMap<>();
fields.put("order", field.bind("commerce.order_info"));
fields.put("company", field.bind("company.info"));
fields.put("payment", field.bind("finance.payment_info"));

Schema schema = new Schema(cfg, fields);
JsonMapper mapper = KrandomJackson.newJsonMapper();

JsonNode jsonSchema = mapper.valueToTree(schema);
String type = jsonSchema.path("properties").path("order").path("type").asString(); // "object"
```

Serializing a `Schema` is side-effect free: it reads provider JSON Schema metadata and does not
advance seeded row generation.

## Serialize Generated Rows

Generated rows are ordinary `Map<String, Object>` values. Composite provider outputs are Java
records, so Jackson serializes them as nested objects:

```java
Map<String, Object> row = schema.generate();

JsonNode rowJson = mapper.valueToTree(row);
String orderNumber = rowJson.path("order").path("orderNumber").asString();
String companyName = rowJson.path("company").path("name").asString();
String orderedOn = rowJson.path("order").path("orderedOn").asString(); // ISO-8601 date text
```

By default, Jackson 3 serializes `java.time` values, such as an order's `orderedOn` date, as ISO-8601
text without an extra module, and writes a `java.util.Date` as ISO-8601 text rather than epoch
milliseconds. It lists a class's properties in alphabetical order, while a record keeps the order of
its components.

For non-Jackson exports, `Schema` also preserves composite records as structured data:

```java
String jsonl = schema.toJsonLines(10);
String csv = schema.toCsv(10);
String xml = schema.toXml(10);
String sql = schema.toSqlInserts("public.orders", 10);
Map<String, Object> jsonSchemaMap = schema.toJsonSchema();
```
