/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.schema;

import org.github.krandom.generator.GeneratorConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Schema")
class SchemaTest {

    @Test
    @DisplayName("generate and batch generation are deterministic when seeded")
    void deterministicBatch() {
        Field fieldA = new Field(GeneratorConfig.builder().locale(Locale.US).seed(42L).build());
        Field fieldB = new Field(GeneratorConfig.builder().locale(Locale.US).seed(42L).build());
        Map<String, SchemaValueProvider> fieldsA = new LinkedHashMap<>();
        fieldsA.put("name", fieldA.bind("person.full_name"));
        fieldsA.put("email", fieldA.bind("person.email"));
        fieldsA.put("price", fieldA.bind("finance.price"));
        fieldsA.put("tags", fieldA.list("text.word", 2, 4));
        Map<String, SchemaValueProvider> fieldsB = new LinkedHashMap<>();
        fieldsB.put("name", fieldB.bind("person.full_name"));
        fieldsB.put("email", fieldB.bind("person.email"));
        fieldsB.put("price", fieldB.bind("finance.price"));
        fieldsB.put("tags", fieldB.list("text.word", 2, 4));

        Schema a = new Schema(GeneratorConfig.builder().locale(Locale.US).seed(500L).build(), fieldsA);
        Schema b = new Schema(GeneratorConfig.builder().locale(Locale.US).seed(500L).build(), fieldsB);

        assertEquals(a.generate(), b.generate());
        assertEquals(a.generateBatch(5), b.generateBatch(5));
    }

    @Test
    @DisplayName("record index increments across generate and loop")
    void recordIndex() {
        Map<String, SchemaValueProvider> fields = Map.of(
            "index", ctx -> ctx.recordIndex()
        );
        Schema schema = new Schema(fields);
        assertEquals(0, schema.generate().get("index"));
        assertEquals(1, schema.generate().get("index"));
        List<Map<String, Object>> loop = schema.loop(2);
        assertEquals(2, loop.get(0).get("index"));
        assertEquals(3, loop.get(1).get("index"));
    }

    @Test
    @DisplayName("nested fields can be generated through Field helper")
    void nested() {
        Field field = new Field(Locale.GERMANY);
        Map<String, SchemaValueProvider> address = new LinkedHashMap<>();
        address.put("city", field.bind("address.city"));
        address.put("country", field.bind("address.country"));

        Map<String, SchemaValueProvider> root = new LinkedHashMap<>();
        root.put("name", field.bind("person.full_name"));
        root.put("address", field.nested(address));
        root.put("codes", field.list("code.ean13", 1, 2));

        Map<String, Object> record = new Schema(Locale.GERMANY, root).generate();
        assertTrue(record.get("address") instanceof Map<?, ?>);
        assertTrue(record.get("codes") instanceof List<?>);
    }

    @Test
    @DisplayName("schema wraps provider failures with field context")
    void wrapsErrors() {
        Map<String, SchemaValueProvider> fields = new LinkedHashMap<>();
        fields.put("ok", ctx -> "v");
        fields.put("bad", ctx -> {
            throw new IllegalStateException("boom");
        });
        Schema schema = new Schema(fields);
        SchemaGenerationException ex = assertThrows(SchemaGenerationException.class, schema::generate);
        assertTrue(ex.getMessage().contains("bad"));
        assertTrue(ex.getCause() instanceof IllegalStateException);
    }

    @Test
    @DisplayName("constructor and input validation")
    void validation() {
        Map<String, SchemaValueProvider> valid = Map.of("x", ctx -> 1);
        assertThrows(NullPointerException.class, () -> new Schema((GeneratorConfig) null, valid));
        assertThrows(NullPointerException.class, () -> new Schema((Map<String, SchemaValueProvider>) null));
        assertThrows(IllegalArgumentException.class, () -> new Schema(Map.of()));
        assertThrows(IllegalArgumentException.class, () -> new Schema(Map.of(" ", ctx -> 1)));
        Map<String, SchemaValueProvider> nullKey = new LinkedHashMap<>();
        nullKey.put(null, ctx -> 1);
        assertThrows(IllegalArgumentException.class, () -> new Schema(nullKey));
        Map<String, SchemaValueProvider> nullProvider = new LinkedHashMap<>();
        nullProvider.put("x", null);
        assertThrows(IllegalArgumentException.class, () -> new Schema(nullProvider));

        Schema schema = new Schema(valid);
        assertEquals(Locale.US, schema.getLocale());
        assertEquals(1, schema.getFields().size());
        assertThrows(UnsupportedOperationException.class, () -> schema.getFields().put("y", ctx -> 2));
        assertThrows(IllegalArgumentException.class, () -> schema.generateBatch(-1));
        assertEquals(List.of(), schema.generateBatch(0));
    }

    @Test
    @DisplayName("toJsonSchema infers primitive, nested and array field types")
    void toJsonSchema() {
        Map<String, SchemaValueProvider> fields = new LinkedHashMap<>();
        fields.put("name", ctx -> "alice");
        fields.put("age", ctx -> 42);
        fields.put("active", ctx -> true);
        fields.put("tags", ctx -> List.of("a", "b"));
        fields.put("meta", ctx -> Map.of("score", 9.5, "vip", false));
        fields.put("missing", ctx -> null);
        fields.put("byteValue", ctx -> (byte) 7);
        fields.put("shortValue", ctx -> (short) 8);
        fields.put("longValue", ctx -> 9L);
        fields.put("bigIntValue", ctx -> BigInteger.valueOf(10));
        fields.put("nullItems", ctx -> Arrays.asList(null, null));
        fields.put("mapWithNonStringKey", ctx -> Map.of(1, "value"));
        fields.put("customObject", ctx -> new Object());

        Schema schema = new Schema(fields);
        Map<String, Object> jsonSchema = schema.toJsonSchema();
        assertEquals("https://json-schema.org/draft/2020-12/schema", jsonSchema.get("$schema"));
        assertEquals("object", jsonSchema.get("type"));
        assertEquals(false, jsonSchema.get("additionalProperties"));

        @SuppressWarnings("unchecked")
        Map<String, Object> properties = (Map<String, Object>) jsonSchema.get("properties");

        @SuppressWarnings("unchecked")
        Map<String, Object> nameType = (Map<String, Object>) properties.get("name");
        assertEquals("string", nameType.get("type"));

        @SuppressWarnings("unchecked")
        Map<String, Object> ageType = (Map<String, Object>) properties.get("age");
        assertEquals("integer", ageType.get("type"));

        @SuppressWarnings("unchecked")
        Map<String, Object> activeType = (Map<String, Object>) properties.get("active");
        assertEquals("boolean", activeType.get("type"));

        @SuppressWarnings("unchecked")
        Map<String, Object> tagsType = (Map<String, Object>) properties.get("tags");
        assertEquals("array", tagsType.get("type"));
        @SuppressWarnings("unchecked")
        Map<String, Object> tagItems = (Map<String, Object>) tagsType.get("items");
        assertEquals("string", tagItems.get("type"));

        @SuppressWarnings("unchecked")
        Map<String, Object> metaType = (Map<String, Object>) properties.get("meta");
        assertEquals("object", metaType.get("type"));
        assertEquals(false, metaType.get("additionalProperties"));

        @SuppressWarnings("unchecked")
        Map<String, Object> missingType = (Map<String, Object>) properties.get("missing");
        assertEquals("null", missingType.get("type"));

        @SuppressWarnings("unchecked")
        Map<String, Object> byteType = (Map<String, Object>) properties.get("byteValue");
        assertEquals("integer", byteType.get("type"));

        @SuppressWarnings("unchecked")
        Map<String, Object> shortType = (Map<String, Object>) properties.get("shortValue");
        assertEquals("integer", shortType.get("type"));

        @SuppressWarnings("unchecked")
        Map<String, Object> longType = (Map<String, Object>) properties.get("longValue");
        assertEquals("integer", longType.get("type"));

        @SuppressWarnings("unchecked")
        Map<String, Object> bigIntType = (Map<String, Object>) properties.get("bigIntValue");
        assertEquals("integer", bigIntType.get("type"));

        @SuppressWarnings("unchecked")
        Map<String, Object> nullItemsType = (Map<String, Object>) properties.get("nullItems");
        @SuppressWarnings("unchecked")
        Map<String, Object> nullItemsSchema = (Map<String, Object>) nullItemsType.get("items");
        assertEquals("null", nullItemsSchema.get("type"));

        @SuppressWarnings("unchecked")
        Map<String, Object> mapWithNonStringKeyType = (Map<String, Object>) properties.get("mapWithNonStringKey");
        assertEquals("object", mapWithNonStringKeyType.get("type"));
        @SuppressWarnings("unchecked")
        Map<String, Object> nonStringProperties = (Map<String, Object>) mapWithNonStringKeyType.get("properties");
        assertTrue(nonStringProperties.isEmpty());

        @SuppressWarnings("unchecked")
        Map<String, Object> customObjectType = (Map<String, Object>) properties.get("customObject");
        assertEquals("string", customObjectType.get("type"));
    }

    @Test
    @DisplayName("records serialize as structured objects across schema exporters")
    void recordsSerializeAsStructuredObjects() {
        Map<String, SchemaValueProvider> fields = new LinkedHashMap<>();
        fields.put("customer", ctx -> new CustomerRecord("Ada", new AddressRecord("London", 7)));
        Schema schema = new Schema(fields);

        String jsonl = schema.toJsonLines(1);
        assertTrue(jsonl.contains("\"customer\":{\"name\":\"Ada\",\"address\":{\"city\":\"London\",\"houseNumber\":7}}"));

        String csv = schema.toCsv(1);
        assertTrue(csv.contains("\"{"));
        assertTrue(csv.contains("\"\"houseNumber\"\":7"));

        String xml = schema.toXml(1);
        assertTrue(xml.contains("\"houseNumber\":7"));

        String sql = schema.toSqlInserts("public.customers", 1);
        assertTrue(sql.contains("'{\"name\":\"Ada\",\"address\":{\"city\":\"London\",\"houseNumber\":7}}'"));

        Map<String, Object> jsonSchema = schema.toJsonSchema();
        @SuppressWarnings("unchecked")
        Map<String, Object> properties = (Map<String, Object>) jsonSchema.get("properties");
        @SuppressWarnings("unchecked")
        Map<String, Object> customerType = (Map<String, Object>) properties.get("customer");
        assertEquals("object", customerType.get("type"));
        @SuppressWarnings("unchecked")
        Map<String, Object> customerProperties = (Map<String, Object>) customerType.get("properties");
        @SuppressWarnings("unchecked")
        Map<String, Object> addressType = (Map<String, Object>) customerProperties.get("address");
        assertEquals("object", addressType.get("type"));
    }

    @Test
    @DisplayName("composite provider field bindings export structured schema rows")
    void compositeProviderFieldBindingsExportStructuredRows() {
        GeneratorConfig config = GeneratorConfig.builder().locale(Locale.US).seed(123L).build();
        Field field = new Field(config);
        Map<String, SchemaValueProvider> fields = new LinkedHashMap<>();
        fields.put("order", field.bind("commerce.order_info"));
        fields.put("company", field.bind("company.info"));
        fields.put("payment", field.bind("finance.payment_info"));
        Schema schema = new Schema(config, fields);

        String jsonl = schema.toJsonLines(1);
        assertTrue(jsonl.contains("\"order\":{\"orderNumber\""));
        assertTrue(jsonl.contains("\"company\":{\"name\""));
        assertTrue(jsonl.contains("\"payment\":{\"paymentNumber\""));

        assertTrue(schema.toCsv(1).contains("\"\"paymentNumber\"\""));
        assertTrue(schema.toXml(1).contains("\"orderNumber\""));
        assertTrue(schema.toSqlInserts("fixtures.orders", 1).contains("'{\"orderNumber\""));

        Map<String, Object> jsonSchema = schema.toJsonSchema();
        @SuppressWarnings("unchecked")
        Map<String, Object> properties = (Map<String, Object>) jsonSchema.get("properties");
        assertEquals("object", ((Map<?, ?>) properties.get("order")).get("type"));
        assertEquals("object", ((Map<?, ?>) properties.get("company")).get("type"));
        assertEquals("object", ((Map<?, ?>) properties.get("payment")).get("type"));
    }

    @Test
    @DisplayName("toJsonSchema wraps provider failure with field context")
    void toJsonSchemaWrapsFailures() {
        Map<String, SchemaValueProvider> fields = new LinkedHashMap<>();
        fields.put("ok", ctx -> "v");
        fields.put("boom", ctx -> {
            throw new IllegalStateException("explode");
        });
        Schema schema = new Schema(fields);
        SchemaGenerationException exception = assertThrows(SchemaGenerationException.class, schema::toJsonSchema);
        assertTrue(exception.getMessage().contains("boom"));
        assertFalse(exception.getMessage().isBlank());
    }

    private record CustomerRecord(String name, AddressRecord address) {

    }

    private record AddressRecord(String city, int houseNumber) {

    }
}
