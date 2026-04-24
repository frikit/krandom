---
layout: page
title: Jackson Integration
permalink: /guides/jackson-integration/
---

# Jackson Integration

Use `krandom-jackson` when you want Jackson to understand kRandom schema definitions.

## Dependency

The current release channel is GitHub Packages:

```kotlin
repositories {
    mavenCentral()
    maven {
        url = uri("https://maven.pkg.github.com/frikit/krandom")
        credentials {
            username = providers.gradleProperty("gpr.user")
                .orElse(providers.environmentVariable("GITHUB_ACTOR"))
                .orNull
            password = providers.gradleProperty("gpr.key")
                .orElse(providers.environmentVariable("GITHUB_TOKEN"))
                .orNull
        }
    }
}

dependencies {
    implementation("io.github.frikit:krandom-core:<version>")
    implementation("io.github.frikit:krandom-jackson:<version>")
}
```

## Configure An ObjectMapper

```java
import com.fasterxml.jackson.databind.ObjectMapper;
import org.github.krandom.jackson.KrandomJackson;

ObjectMapper mapper = KrandomJackson.configure(new ObjectMapper());
```

`KrandomJackson.newObjectMapper()` is the shortest setup path:

```java
ObjectMapper mapper = KrandomJackson.newObjectMapper();
```

## Serialize A Schema As JSON Schema

```java
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.github.krandom.generator.GeneratorConfig;
import org.github.krandom.generator.schema.Field;
import org.github.krandom.generator.schema.Schema;
import org.github.krandom.generator.schema.SchemaValueProvider;
import org.github.krandom.jackson.KrandomJackson;

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
ObjectMapper mapper = KrandomJackson.newObjectMapper();

JsonNode jsonSchema = mapper.valueToTree(schema);
String type = jsonSchema.path("properties").path("order").path("type").asText(); // "object"
```

## Serialize Generated Rows

Generated rows are ordinary `Map<String, Object>` values. Composite provider outputs are Java
records, so Jackson serializes them as nested objects:

```java
Map<String, Object> row = schema.generate();

JsonNode rowJson = mapper.valueToTree(row);
String orderNumber = rowJson.path("order").path("orderNumber").asText();
String companyName = rowJson.path("company").path("name").asText();
```

For non-Jackson exports, `Schema` also preserves composite records as structured data:

```java
String jsonl = schema.toJsonLines(10);
String csv = schema.toCsv(10);
String xml = schema.toXml(10);
String sql = schema.toSqlInserts("public.orders", 10);
Map<String, Object> jsonSchemaMap = schema.toJsonSchema();
```
