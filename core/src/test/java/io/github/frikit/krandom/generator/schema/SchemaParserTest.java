/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.schema;

import io.github.frikit.krandom.generator.GeneratorConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("SchemaParser")
class SchemaParserTest {

    private static Map<String, Object> objectSchema(Map<String, Object> properties) {
        return Map.of("type", "object", "properties", properties);
    }

    @Nested
    @DisplayName("fromJsonSchema validation")
    class Validation {

        @Test
        @DisplayName("rejects null schema")
        void rejectsNullSchema() {
            assertThrows(NullPointerException.class,
                () -> SchemaParser.fromJsonSchema(null));
        }

        @Test
        @DisplayName("rejects null config")
        void rejectsNullConfig() {
            assertThrows(NullPointerException.class,
                () -> SchemaParser.fromJsonSchema(Map.of(), null));
        }

        @Test
        @DisplayName("rejects non-object type")
        void rejectsNonObjectType() {
            var ex = assertThrows(IllegalArgumentException.class,
                () -> SchemaParser.fromJsonSchema(Map.of("type", "string")));
            assertTrue(ex.getMessage().contains("\"type\": \"object\""));
        }

        @Test
        @DisplayName("rejects missing type")
        void rejectsMissingType() {
            assertThrows(IllegalArgumentException.class,
                () -> SchemaParser.fromJsonSchema(Map.of("properties", Map.of())));
        }

        @Test
        @DisplayName("rejects missing properties")
        void rejectsMissingProperties() {
            assertThrows(IllegalArgumentException.class,
                () -> SchemaParser.fromJsonSchema(Map.of("type", "object")));
        }

        @Test
        @DisplayName("rejects non-map properties")
        void rejectsNonMapProperties() {
            assertThrows(IllegalArgumentException.class,
                () -> SchemaParser.fromJsonSchema(
                    Map.of("type", "object", "properties", "not-a-map")));
        }

        @Test
        @DisplayName("rejects empty properties")
        void rejectsEmptyProperties() {
            assertThrows(IllegalArgumentException.class,
                () -> SchemaParser.fromJsonSchema(objectSchema(Map.of())));
        }

        @Test
        @DisplayName("rejects non-string property key")
        void rejectsNonStringPropertyKey() {
            Map<Object, Object> properties = new HashMap<>();
            properties.put(42, Map.of("type", "string"));
            Map<String, Object> schema = new HashMap<>();
            schema.put("type", "object");
            schema.put("properties", properties);
            assertThrows(IllegalArgumentException.class,
                () -> SchemaParser.fromJsonSchema(schema));
        }

        @Test
        @DisplayName("rejects non-map property value")
        void rejectsNonMapPropertyValue() {
            Map<String, Object> properties = new HashMap<>();
            properties.put("name", "not-a-schema");
            assertThrows(IllegalArgumentException.class,
                () -> SchemaParser.fromJsonSchema(objectSchema(properties)));
        }
    }

    @Nested
    @DisplayName("string types")
    class StringTypes {

        @Test
        @DisplayName("generates plain strings")
        void plainString() {
            Schema schema = SchemaParser.fromJsonSchema(objectSchema(
                Map.of("data", Map.of("type", "string"))));
            Map<String, Object> record = schema.generate();
            assertInstanceOf(String.class, record.get("data"));
        }

        @Test
        @DisplayName("respects minLength and maxLength")
        void boundedString() {
            Schema schema = SchemaParser.fromJsonSchema(objectSchema(
                Map.of("code", Map.of("type", "string", "minLength", 5, "maxLength", 5))));
            for (int i = 0; i < 10; i++) {
                String value = (String) schema.generate().get("code");
                assertEquals(5, value.length());
            }
        }

        @Test
        @DisplayName("generates from regex pattern")
        void regexPattern() {
            // Use a non-semantic field name so pattern resolution in resolveString is exercised
            Schema schema = SchemaParser.fromJsonSchema(objectSchema(
                Map.of("code123", Map.of("type", "string", "pattern", "\\d{5}"))));
            for (int i = 0; i < 10; i++) {
                String value = (String) schema.generate().get("code123");
                assertTrue(value.matches("\\d{5}"), "Expected 5 digits, got: " + value);
            }
        }

        @Test
        @DisplayName("generates from regex pattern with format present")
        void regexPatternWithFormat() {
            // Pattern takes priority over format in resolveString
            Map<String, Object> fieldSchema = new HashMap<>();
            fieldSchema.put("type", "string");
            fieldSchema.put("pattern", "[A-Z]{3}");
            fieldSchema.put("format", "email");
            Schema schema = SchemaParser.fromJsonSchema(objectSchema(
                Map.of("xyzField", fieldSchema)));
            String value = (String) schema.generate().get("xyzField");
            assertTrue(value.matches("[A-Z]{3}"), "Expected 3 uppercase, got: " + value);
        }

        @Test
        @DisplayName("format: email")
        void formatEmail() {
            Schema schema = SchemaParser.fromJsonSchema(objectSchema(
                Map.of("mail", Map.of("type", "string", "format", "email"))));
            String value = (String) schema.generate().get("mail");
            assertTrue(value.contains("@"), "Expected email, got: " + value);
        }

        @Test
        @DisplayName("format: uri")
        void formatUri() {
            Schema schema = SchemaParser.fromJsonSchema(objectSchema(
                Map.of("link", Map.of("type", "string", "format", "uri"))));
            String value = (String) schema.generate().get("link");
            assertNotNull(value);
            assertTrue(value.contains("://"), "Expected URI, got: " + value);
        }

        @Test
        @DisplayName("format: url alias")
        void formatUrl() {
            Schema schema = SchemaParser.fromJsonSchema(objectSchema(
                Map.of("link", Map.of("type", "string", "format", "url"))));
            String value = (String) schema.generate().get("link");
            assertTrue(value.contains("://"), "Expected URL, got: " + value);
        }

        @Test
        @DisplayName("format: uuid")
        void formatUuid() {
            Schema schema = SchemaParser.fromJsonSchema(objectSchema(
                Map.of("identifier", Map.of("type", "string", "format", "uuid"))));
            String value = (String) schema.generate().get("identifier");
            assertTrue(value.matches(
                "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"),
                "Expected UUID, got: " + value);
        }

        @Test
        @DisplayName("format: date")
        void formatDate() {
            Schema schema = SchemaParser.fromJsonSchema(objectSchema(
                Map.of("dob", Map.of("type", "string", "format", "date"))));
            String value = (String) schema.generate().get("dob");
            assertTrue(value.matches("\\d{4}-\\d{2}-\\d{2}"),
                "Expected date, got: " + value);
        }

        @Test
        @DisplayName("format: date-time")
        void formatDateTime() {
            Schema schema = SchemaParser.fromJsonSchema(objectSchema(
                Map.of("ts", Map.of("type", "string", "format", "date-time"))));
            String value = (String) schema.generate().get("ts");
            assertNotNull(value);
            assertTrue(value.contains("T"), "Expected date-time, got: " + value);
        }

        @Test
        @DisplayName("format: time")
        void formatTime() {
            Schema schema = SchemaParser.fromJsonSchema(objectSchema(
                Map.of("clock", Map.of("type", "string", "format", "time"))));
            String value = (String) schema.generate().get("clock");
            assertTrue(value.matches("\\d{2}:\\d{2}(:\\d{2}.*)?"),
                "Expected time, got: " + value);
        }

        @Test
        @DisplayName("format: ipv4")
        void formatIpv4() {
            Schema schema = SchemaParser.fromJsonSchema(objectSchema(
                Map.of("addr", Map.of("type", "string", "format", "ipv4"))));
            String value = (String) schema.generate().get("addr");
            assertTrue(value.matches("\\d+\\.\\d+\\.\\d+\\.\\d+"),
                "Expected IPv4, got: " + value);
        }

        @Test
        @DisplayName("format: ipv6")
        void formatIpv6() {
            Schema schema = SchemaParser.fromJsonSchema(objectSchema(
                Map.of("addr6", Map.of("type", "string", "format", "ipv6"))));
            String value = (String) schema.generate().get("addr6");
            assertTrue(value.contains(":"), "Expected IPv6, got: " + value);
        }

        @Test
        @DisplayName("format: hostname")
        void formatHostname() {
            Schema schema = SchemaParser.fromJsonSchema(objectSchema(
                Map.of("host", Map.of("type", "string", "format", "hostname"))));
            String value = (String) schema.generate().get("host");
            assertTrue(value.contains("."), "Expected hostname, got: " + value);
        }

        @Test
        @DisplayName("unknown format falls through to semantic or default string")
        void unknownFormat() {
            Schema schema = SchemaParser.fromJsonSchema(objectSchema(
                Map.of("data", Map.of("type", "string", "format", "unknown-custom"))));
            Object value = schema.generate().get("data");
            assertInstanceOf(String.class, value);
        }
    }

    @Nested
    @DisplayName("integer types")
    class IntegerTypes {

        @Test
        @DisplayName("generates integers with defaults")
        void defaultInteger() {
            Schema schema = SchemaParser.fromJsonSchema(objectSchema(
                Map.of("count", Map.of("type", "integer"))));
            Object value = schema.generate().get("count");
            assertInstanceOf(Number.class, value);
        }

        @Test
        @DisplayName("respects minimum and maximum")
        void boundedInteger() {
            Schema schema = SchemaParser.fromJsonSchema(objectSchema(
                Map.of("score", Map.of("type", "integer", "minimum", 10, "maximum", 20))));
            for (int i = 0; i < 50; i++) {
                int value = ((Number) schema.generate().get("score")).intValue();
                assertTrue(value >= 10 && value <= 20,
                    "Expected 10-20, got: " + value);
            }
        }

        @Test
        @DisplayName("respects exclusiveMinimum and exclusiveMaximum")
        void exclusiveBounds() {
            Schema schema = SchemaParser.fromJsonSchema(objectSchema(
                Map.of("val", Map.of("type", "integer",
                    "exclusiveMinimum", 0, "exclusiveMaximum", 3))));
            for (int i = 0; i < 50; i++) {
                int value = ((Number) schema.generate().get("val")).intValue();
                assertTrue(value >= 1 && value <= 2,
                    "Expected 1-2, got: " + value);
            }
        }

        @Test
        @DisplayName("uses long range when exceeding int bounds")
        void longRange() {
            long bigMin = (long) Integer.MAX_VALUE + 1;
            long bigMax = (long) Integer.MAX_VALUE + 100;
            Map<String, Object> props = new HashMap<>();
            props.put("big", Map.of("type", "integer", "minimum", bigMin, "maximum", bigMax));
            Schema schema = SchemaParser.fromJsonSchema(objectSchema(props));
            Object value = schema.generate().get("big");
            assertInstanceOf(Number.class, value);
            long longVal = ((Number) value).longValue();
            assertTrue(longVal >= bigMin && longVal <= bigMax,
                "Expected " + bigMin + "-" + bigMax + ", got: " + longVal);
        }
    }

    @Nested
    @DisplayName("number types")
    class NumberTypes {

        @Test
        @DisplayName("generates doubles with defaults")
        void defaultNumber() {
            Schema schema = SchemaParser.fromJsonSchema(objectSchema(
                Map.of("price", Map.of("type", "number"))));
            Object value = schema.generate().get("price");
            assertInstanceOf(Number.class, value);
        }

        @Test
        @DisplayName("respects minimum and maximum")
        void boundedNumber() {
            Schema schema = SchemaParser.fromJsonSchema(objectSchema(
                Map.of("rate", Map.of("type", "number", "minimum", 0.0, "maximum", 1.0))));
            for (int i = 0; i < 50; i++) {
                double value = ((Number) schema.generate().get("rate")).doubleValue();
                assertTrue(value >= 0.0 && value <= 1.0,
                    "Expected 0.0-1.0, got: " + value);
            }
        }

        @Test
        @DisplayName("respects exclusiveMinimum and exclusiveMaximum")
        void exclusiveBounds() {
            Schema schema = SchemaParser.fromJsonSchema(objectSchema(
                Map.of("temp", Map.of("type", "number",
                    "exclusiveMinimum", 0.0, "exclusiveMaximum", 100.0))));
            for (int i = 0; i < 20; i++) {
                double value = ((Number) schema.generate().get("temp")).doubleValue();
                assertTrue(value > 0.0 && value < 100.0,
                    "Expected >0 and <100, got: " + value);
            }
        }
    }

    @Nested
    @DisplayName("boolean type")
    class BooleanType {

        @Test
        @DisplayName("generates booleans")
        void generateBoolean() {
            Schema schema = SchemaParser.fromJsonSchema(objectSchema(
                Map.of("active", Map.of("type", "boolean"))));
            Object value = schema.generate().get("active");
            assertInstanceOf(Boolean.class, value);
        }
    }

    @Nested
    @DisplayName("null type")
    class NullType {

        @Test
        @DisplayName("generates null values")
        void generateNull() {
            Schema schema = SchemaParser.fromJsonSchema(objectSchema(
                Map.of("nothing", Map.of("type", "null"))));
            assertNull(schema.generate().get("nothing"));
        }
    }

    @Nested
    @DisplayName("enum and const")
    class EnumAndConst {

        @Test
        @DisplayName("const always returns the constant value")
        void constValue() {
            Schema schema = SchemaParser.fromJsonSchema(objectSchema(
                Map.of("status", Map.of("type", "string", "const", "active"))));
            for (int i = 0; i < 10; i++) {
                assertEquals("active", schema.generate().get("status"));
            }
        }

        @Test
        @DisplayName("const takes priority over enum")
        void constOverEnum() {
            Map<String, Object> fieldSchema = new HashMap<>();
            fieldSchema.put("type", "string");
            fieldSchema.put("const", "fixed");
            fieldSchema.put("enum", List.of("a", "b", "c"));
            Schema schema = SchemaParser.fromJsonSchema(objectSchema(
                Map.of("val", fieldSchema)));
            assertEquals("fixed", schema.generate().get("val"));
        }

        @Test
        @DisplayName("enum selects from provided values")
        void enumValues() {
            List<String> options = List.of("red", "green", "blue");
            Map<String, Object> fieldSchema = new HashMap<>();
            fieldSchema.put("type", "string");
            fieldSchema.put("enum", options);
            Schema schema = SchemaParser.fromJsonSchema(objectSchema(
                Map.of("color", fieldSchema)));
            for (int i = 0; i < 20; i++) {
                Object value = schema.generate().get("color");
                assertTrue(options.contains(value),
                    "Expected one of " + options + ", got: " + value);
            }
        }

        @Test
        @DisplayName("empty enum falls through to type-based generation")
        void emptyEnum() {
            Map<String, Object> fieldSchema = new HashMap<>();
            fieldSchema.put("type", "string");
            fieldSchema.put("enum", List.of());
            Schema schema = SchemaParser.fromJsonSchema(objectSchema(
                Map.of("data", fieldSchema)));
            Object value = schema.generate().get("data");
            assertInstanceOf(String.class, value);
        }
    }

    @Nested
    @DisplayName("array type")
    class ArrayType {

        @Test
        @DisplayName("generates arrays with typed items")
        void typedItems() {
            Map<String, Object> items = Map.of("type", "integer", "minimum", 1, "maximum", 100);
            Schema schema = SchemaParser.fromJsonSchema(objectSchema(
                Map.of("scores", Map.of("type", "array", "items", items,
                    "minItems", 3, "maxItems", 3))));
            @SuppressWarnings("unchecked")
            List<Object> list = (List<Object>) schema.generate().get("scores");
            assertEquals(3, list.size());
            for (Object item : list) {
                assertInstanceOf(Number.class, item);
            }
        }

        @Test
        @DisplayName("defaults to string items when items not specified")
        void defaultItems() {
            Schema schema = SchemaParser.fromJsonSchema(objectSchema(
                Map.of("tags", Map.of("type", "array", "minItems", 2, "maxItems", 2))));
            @SuppressWarnings("unchecked")
            List<Object> list = (List<Object>) schema.generate().get("tags");
            assertEquals(2, list.size());
            for (Object item : list) {
                assertInstanceOf(String.class, item);
            }
        }

        @Test
        @DisplayName("respects minItems and maxItems range")
        void itemCountRange() {
            Schema schema = SchemaParser.fromJsonSchema(objectSchema(
                Map.of("items", Map.of("type", "array",
                    "items", Map.of("type", "string"),
                    "minItems", 1, "maxItems", 5))));
            for (int i = 0; i < 20; i++) {
                @SuppressWarnings("unchecked")
                List<Object> list = (List<Object>) schema.generate().get("items");
                assertTrue(list.size() >= 1 && list.size() <= 5,
                    "Expected 1-5 items, got: " + list.size());
            }
        }
    }

    @Nested
    @DisplayName("nested object type")
    class NestedObjectType {

        @Test
        @DisplayName("generates nested objects")
        void nestedObject() {
            Map<String, Object> addressProps = Map.of(
                "street", Map.of("type", "string"),
                "zip", Map.of("type", "string", "pattern", "\\d{5}"));
            Map<String, Object> address = Map.of(
                "type", "object", "properties", addressProps);

            Schema schema = SchemaParser.fromJsonSchema(objectSchema(
                Map.of("address", address)));
            @SuppressWarnings("unchecked")
            Map<String, Object> nested = (Map<String, Object>) schema.generate().get("address");
            assertNotNull(nested);
            assertInstanceOf(String.class, nested.get("street"));
            assertTrue(((String) nested.get("zip")).matches("\\d{5}"));
        }

        @Test
        @DisplayName("nested object without properties returns empty map")
        void emptyNestedObject() {
            Schema schema = SchemaParser.fromJsonSchema(objectSchema(
                Map.of("meta", Map.of("type", "object"))));
            @SuppressWarnings("unchecked")
            Map<String, Object> nested = (Map<String, Object>) schema.generate().get("meta");
            assertNotNull(nested);
            assertTrue(nested.isEmpty());
        }

        @Test
        @DisplayName("nested object with empty properties returns empty map")
        void emptyPropertiesNestedObject() {
            Schema schema = SchemaParser.fromJsonSchema(objectSchema(
                Map.of("meta", Map.of("type", "object", "properties", Map.of()))));
            @SuppressWarnings("unchecked")
            Map<String, Object> nested = (Map<String, Object>) schema.generate().get("meta");
            assertNotNull(nested);
            assertTrue(nested.isEmpty());
        }
    }

    @Nested
    @DisplayName("nullable fields")
    class NullableFields {

        @Test
        @DisplayName("nullable field can produce null with seeded random")
        void nullableProducesNull() {
            Map<String, Object> fieldSchema = new HashMap<>();
            fieldSchema.put("type", "string");
            fieldSchema.put("nullable", true);
            // Use a single Schema and call generate() many times;
            // over 200 calls at ~50% probability we expect both null and non-null
            Schema schema = SchemaParser.fromJsonSchema(objectSchema(
                Map.of("data", fieldSchema)));
            boolean gotNull = false;
            boolean gotNonNull = false;
            for (int i = 0; i < 200 && !(gotNull && gotNonNull); i++) {
                Object value = schema.generate().get("data");
                if (value == null) {
                    gotNull = true;
                } else {
                    gotNonNull = true;
                }
            }
            assertTrue(gotNull, "Expected at least one null from nullable field");
            assertTrue(gotNonNull, "Expected at least one non-null from nullable field");
        }
    }

    @Nested
    @DisplayName("semantic field name resolution")
    class SemanticResolution {

        @Test
        @DisplayName("resolves firstName to person name")
        void firstName() {
            Schema schema = SchemaParser.fromJsonSchema(objectSchema(
                Map.of("firstName", Map.of("type", "string"))));
            String value = (String) schema.generate().get("firstName");
            assertNotNull(value);
            assertTrue(value.length() > 0);
        }

        @Test
        @DisplayName("resolves email to email address")
        void email() {
            Schema schema = SchemaParser.fromJsonSchema(objectSchema(
                Map.of("email", Map.of("type", "string"))));
            String value = (String) schema.generate().get("email");
            assertTrue(value.contains("@"), "Expected email, got: " + value);
        }

        @Test
        @DisplayName("resolves city semantically")
        void city() {
            Schema schema = SchemaParser.fromJsonSchema(objectSchema(
                Map.of("city", Map.of("type", "string"))));
            String value = (String) schema.generate().get("city");
            assertNotNull(value);
            assertTrue(value.length() > 0);
        }

        @Test
        @DisplayName("resolves snake_case field names")
        void snakeCase() {
            Schema schema = SchemaParser.fromJsonSchema(objectSchema(
                Map.of("last_name", Map.of("type", "string"))));
            String value = (String) schema.generate().get("last_name");
            assertNotNull(value);
            assertTrue(value.length() > 0);
        }

        @Test
        @DisplayName("resolves camelCase field names")
        void camelCase() {
            Schema schema = SchemaParser.fromJsonSchema(objectSchema(
                Map.of("lastName", Map.of("type", "string"))));
            String value = (String) schema.generate().get("lastName");
            assertNotNull(value);
            assertTrue(value.length() > 0);
        }

        @Test
        @DisplayName("resolves common semantic field names")
        void commonSemanticFields() {
            Map<String, Object> props = new HashMap<>();
            props.put("name", Map.of("type", "string"));
            props.put("phone", Map.of("type", "string"));
            props.put("country", Map.of("type", "string"));
            props.put("url", Map.of("type", "string"));
            props.put("username", Map.of("type", "string"));
            Schema schema = SchemaParser.fromJsonSchema(objectSchema(props));
            Map<String, Object> record = schema.generate();
            for (Map.Entry<String, Object> entry : record.entrySet()) {
                assertNotNull(entry.getValue(),
                    "Field '" + entry.getKey() + "' should not be null");
                assertInstanceOf(String.class, entry.getValue());
                assertTrue(((String) entry.getValue()).length() > 0,
                    "Field '" + entry.getKey() + "' should not be empty");
            }
        }

        @Test
        @DisplayName("non-semantic field name falls back to generic string")
        void nonSemanticField() {
            Schema schema = SchemaParser.fromJsonSchema(objectSchema(
                Map.of("xyzAbcQwerty", Map.of("type", "string"))));
            Object value = schema.generate().get("xyzAbcQwerty");
            assertInstanceOf(String.class, value);
        }
    }

    @Nested
    @DisplayName("type list handling")
    class TypeList {

        @Test
        @DisplayName("picks first non-null type from type array")
        void firstNonNullType() {
            Map<String, Object> fieldSchema = new HashMap<>();
            fieldSchema.put("type", List.of("null", "integer"));
            Schema schema = SchemaParser.fromJsonSchema(objectSchema(
                Map.of("val", fieldSchema)));
            Object value = schema.generate().get("val");
            assertInstanceOf(Number.class, value);
        }

        @Test
        @DisplayName("all-null type list produces null")
        void allNullTypeList() {
            Map<String, Object> fieldSchema = new HashMap<>();
            fieldSchema.put("type", List.of("null"));
            Schema schema = SchemaParser.fromJsonSchema(objectSchema(
                Map.of("val", fieldSchema)));
            assertNull(schema.generate().get("val"));
        }
    }

    @Nested
    @DisplayName("untyped fields")
    class UntypedFields {

        @Test
        @DisplayName("field without type uses semantic resolution or string fallback")
        void noType() {
            Schema schema = SchemaParser.fromJsonSchema(objectSchema(
                Map.of("xyzUnknown", Map.of())));
            Object value = schema.generate().get("xyzUnknown");
            assertInstanceOf(String.class, value);
        }

        @Test
        @DisplayName("untyped field with semantic name resolves semantically")
        void untypedSemantic() {
            Map<String, Object> emptySchema = new HashMap<>();
            Schema schema = SchemaParser.fromJsonSchema(objectSchema(
                Map.of("email", emptySchema)));
            String value = (String) schema.generate().get("email");
            assertTrue(value.contains("@"), "Expected email, got: " + value);
        }
    }

    @Nested
    @DisplayName("OpenAPI aliases")
    class OpenApiAliases {

        @Test
        @DisplayName("fromOpenApi delegates to fromJsonSchema")
        void fromOpenApi() {
            Schema schema = SchemaParser.fromOpenApi(objectSchema(
                Map.of("data", Map.of("type", "string"))));
            Object value = schema.generate().get("data");
            assertInstanceOf(String.class, value);
        }

        @Test
        @DisplayName("fromOpenApi with config delegates to fromJsonSchema")
        void fromOpenApiWithConfig() {
            GeneratorConfig config = GeneratorConfig.builder().seed(42L).build();
            Schema schema = SchemaParser.fromOpenApi(objectSchema(
                Map.of("data", Map.of("type", "string"))), config);
            Object value = schema.generate().get("data");
            assertInstanceOf(String.class, value);
        }
    }

    @Nested
    @DisplayName("deterministic with seed")
    class Deterministic {

        @Test
        @DisplayName("parsed schema generates non-null values consistently")
        void parsedSchemaGeneratesValues() {
            Map<String, Object> schemaMap = objectSchema(Map.of(
                "label", Map.of("type", "string"),
                "score", Map.of("type", "integer", "minimum", 1, "maximum", 100)));

            Schema schema = SchemaParser.fromJsonSchema(schemaMap);
            for (int i = 0; i < 10; i++) {
                Map<String, Object> record = schema.generate();
                assertNotNull(record.get("label"));
                assertInstanceOf(String.class, record.get("label"));
                int score = ((Number) record.get("score")).intValue();
                assertTrue(score >= 1 && score <= 100,
                    "Expected 1-100, got: " + score);
            }
        }
    }

    @Nested
    @DisplayName("complex schema")
    class ComplexSchema {

        @Test
        @DisplayName("parses a full user schema with all types")
        void fullUserSchema() {
            Map<String, Object> addressProps = Map.of(
                "street", Map.of("type", "string"),
                "city", Map.of("type", "string"),
                "zip", Map.of("type", "string", "pattern", "\\d{5}"));
            Map<String, Object> address = Map.of(
                "type", "object", "properties", addressProps);

            Map<String, Object> tagItems = Map.of("type", "string");

            Map<String, Object> props = new HashMap<>();
            props.put("firstName", Map.of("type", "string"));
            props.put("email", Map.of("type", "string", "format", "email"));
            props.put("age", Map.of("type", "integer", "minimum", 18, "maximum", 65));
            props.put("score", Map.of("type", "number", "minimum", 0.0, "maximum", 100.0));
            props.put("active", Map.of("type", "boolean"));
            props.put("tags", Map.of("type", "array", "items", tagItems,
                "minItems", 1, "maxItems", 3));
            props.put("address", address);
            props.put("status", Map.of("type", "string", "enum",
                List.of("active", "inactive", "pending")));
            props.put("version", Map.of("type", "string", "const", "1.0"));

            Schema schema = SchemaParser.fromJsonSchema(objectSchema(props));
            Map<String, Object> record = schema.generate();

            assertInstanceOf(String.class, record.get("firstName"));
            assertTrue(((String) record.get("email")).contains("@"));
            int age = ((Number) record.get("age")).intValue();
            assertTrue(age >= 18 && age <= 65);
            double score = ((Number) record.get("score")).doubleValue();
            assertTrue(score >= 0.0 && score <= 100.0);
            assertInstanceOf(Boolean.class, record.get("active"));
            @SuppressWarnings("unchecked")
            List<Object> tags = (List<Object>) record.get("tags");
            assertTrue(tags.size() >= 1 && tags.size() <= 3);
            @SuppressWarnings("unchecked")
            Map<String, Object> addr = (Map<String, Object>) record.get("address");
            assertNotNull(addr.get("street"));
            assertTrue(((String) addr.get("zip")).matches("\\d{5}"));
            assertTrue(List.of("active", "inactive", "pending")
                .contains(record.get("status")));
            assertEquals("1.0", record.get("version"));
        }
    }

    @Nested
    @DisplayName("default config overload")
    class DefaultConfig {

        @Test
        @DisplayName("single-arg fromJsonSchema uses default config")
        void defaultConfig() {
            Schema schema = SchemaParser.fromJsonSchema(objectSchema(
                Map.of("val", Map.of("type", "string"))));
            assertDoesNotThrow(schema::generate);
        }
    }

    @Nested
    @DisplayName("semantic reference coverage")
    class SemanticReferenceCoverage {

        @Test
        @DisplayName("resolves fullname and name")
        void fullNameAndName() {
            Map<String, Object> props = new HashMap<>();
            props.put("fullName", Map.of("type", "string"));
            Schema schema = SchemaParser.fromJsonSchema(objectSchema(props));
            assertInstanceOf(String.class, schema.generate().get("fullName"));
        }

        @Test
        @DisplayName("resolves phone and phoneNumber")
        void phoneNumber() {
            Schema schema = SchemaParser.fromJsonSchema(objectSchema(
                Map.of("phoneNumber", Map.of("type", "string"))));
            assertInstanceOf(String.class, schema.generate().get("phoneNumber"));
        }

        @Test
        @DisplayName("resolves age")
        void age() {
            Schema schema = SchemaParser.fromJsonSchema(objectSchema(
                Map.of("age", Map.of("type", "string"))));
            assertNotNull(schema.generate().get("age"));
        }

        @Test
        @DisplayName("resolves state")
        void state() {
            Schema schema = SchemaParser.fromJsonSchema(objectSchema(
                Map.of("state", Map.of("type", "string"))));
            assertNotNull(schema.generate().get("state"));
        }

        @Test
        @DisplayName("resolves zip and postalCode")
        void zipAndPostalCode() {
            Map<String, Object> props = new HashMap<>();
            props.put("zip", Map.of("type", "string"));
            props.put("postalCode", Map.of("type", "string"));
            Schema schema = SchemaParser.fromJsonSchema(objectSchema(props));
            Map<String, Object> record = schema.generate();
            assertNotNull(record.get("zip"));
            assertNotNull(record.get("postalCode"));
        }

        @Test
        @DisplayName("resolves street and streetAddress")
        void streetAddress() {
            Map<String, Object> props = new HashMap<>();
            props.put("street", Map.of("type", "string"));
            props.put("streetAddress", Map.of("type", "string"));
            Schema schema = SchemaParser.fromJsonSchema(objectSchema(props));
            Map<String, Object> record = schema.generate();
            assertNotNull(record.get("street"));
            assertNotNull(record.get("streetAddress"));
        }

        @Test
        @DisplayName("resolves latitude, longitude, lat, lng, lon")
        void coordinates() {
            Map<String, Object> props = new HashMap<>();
            props.put("latitude", Map.of("type", "string"));
            props.put("longitude", Map.of("type", "string"));
            props.put("lat", Map.of("type", "string"));
            props.put("lng", Map.of("type", "string"));
            props.put("lon", Map.of("type", "string"));
            Schema schema = SchemaParser.fromJsonSchema(objectSchema(props));
            Map<String, Object> record = schema.generate();
            for (String key : List.of("latitude", "longitude", "lat", "lng", "lon")) {
                assertNotNull(record.get(key), key + " should not be null");
            }
        }

        @Test
        @DisplayName("resolves company and companyName")
        void company() {
            Map<String, Object> props = new HashMap<>();
            props.put("company", Map.of("type", "string"));
            props.put("companyName", Map.of("type", "string"));
            Schema schema = SchemaParser.fromJsonSchema(objectSchema(props));
            Map<String, Object> record = schema.generate();
            assertNotNull(record.get("company"));
            assertNotNull(record.get("companyName"));
        }

        @Test
        @DisplayName("resolves website")
        void website() {
            Schema schema = SchemaParser.fromJsonSchema(objectSchema(
                Map.of("website", Map.of("type", "string"))));
            assertNotNull(schema.generate().get("website"));
        }

        @Test
        @DisplayName("resolves ip and ipAddress")
        void ipAddress() {
            Map<String, Object> props = new HashMap<>();
            props.put("ip", Map.of("type", "string"));
            props.put("ipAddress", Map.of("type", "string"));
            Schema schema = SchemaParser.fromJsonSchema(objectSchema(props));
            Map<String, Object> record = schema.generate();
            assertNotNull(record.get("ip"));
            assertNotNull(record.get("ipAddress"));
        }

        @Test
        @DisplayName("resolves domain")
        void domain() {
            Schema schema = SchemaParser.fromJsonSchema(objectSchema(
                Map.of("domain", Map.of("type", "string"))));
            assertNotNull(schema.generate().get("domain"));
        }

        @Test
        @DisplayName("resolves uuid and id")
        void uuidAndId() {
            Map<String, Object> props = new HashMap<>();
            props.put("uuid", Map.of("type", "string"));
            props.put("id", Map.of("type", "string"));
            Schema schema = SchemaParser.fromJsonSchema(objectSchema(props));
            Map<String, Object> record = schema.generate();
            assertNotNull(record.get("uuid"));
            assertNotNull(record.get("id"));
        }

        @Test
        @DisplayName("resolves emailAddress")
        void emailAddress() {
            Schema schema = SchemaParser.fromJsonSchema(objectSchema(
                Map.of("emailAddress", Map.of("type", "string"))));
            String value = (String) schema.generate().get("emailAddress");
            assertTrue(value.contains("@"), "Expected email, got: " + value);
        }

        @Test
        @DisplayName("resolves zipCode")
        void zipCode() {
            Schema schema = SchemaParser.fromJsonSchema(objectSchema(
                Map.of("zipCode", Map.of("type", "string"))));
            assertNotNull(schema.generate().get("zipCode"));
        }

        @Test
        @DisplayName("resolves id semantically")
        void id() {
            Schema schema = SchemaParser.fromJsonSchema(objectSchema(
                Map.of("id", Map.of("type", "string"))));
            assertNotNull(schema.generate().get("id"));
        }
    }

    @Nested
    @DisplayName("string with unknown format and semantic name")
    class StringFormatFallback {

        @Test
        @DisplayName("unknown format with semantic field name resolves semantically")
        void unknownFormatSemanticName() {
            Map<String, Object> fieldSchema = new HashMap<>();
            fieldSchema.put("type", "string");
            fieldSchema.put("format", "custom-unknown");
            Schema schema = SchemaParser.fromJsonSchema(objectSchema(
                Map.of("email", fieldSchema)));
            String value = (String) schema.generate().get("email");
            assertTrue(value.contains("@"),
                "Expected semantic email resolution, got: " + value);
        }

        @Test
        @DisplayName("unknown format with non-semantic name falls to bounded string")
        void unknownFormatNonSemantic() {
            Map<String, Object> fieldSchema = new HashMap<>();
            fieldSchema.put("type", "string");
            fieldSchema.put("format", "custom-unknown");
            fieldSchema.put("minLength", 10);
            fieldSchema.put("maxLength", 10);
            Schema schema = SchemaParser.fromJsonSchema(objectSchema(
                Map.of("xyzData", fieldSchema)));
            String value = (String) schema.generate().get("xyzData");
            assertEquals(10, value.length());
        }
    }

    @Nested
    @DisplayName("integer edge cases")
    class IntegerEdgeCases {

        @Test
        @DisplayName("Long.MAX_VALUE as maximum uses safe long range")
        void longMaxValue() {
            Map<String, Object> fieldSchema = new HashMap<>();
            fieldSchema.put("type", "integer");
            fieldSchema.put("minimum", Long.MAX_VALUE - 10);
            fieldSchema.put("maximum", Long.MAX_VALUE);
            Schema schema = SchemaParser.fromJsonSchema(objectSchema(
                Map.of("huge", fieldSchema)));
            Object value = schema.generate().get("huge");
            assertInstanceOf(Number.class, value);
            long longVal = ((Number) value).longValue();
            assertTrue(longVal >= Long.MAX_VALUE - 10 && longVal <= Long.MAX_VALUE);
        }

        @Test
        @DisplayName("min below Integer.MIN_VALUE uses long range")
        void minBelowIntMin() {
            long negMin = (long) Integer.MIN_VALUE - 10;
            long negMax = (long) Integer.MIN_VALUE - 1;
            Map<String, Object> fieldSchema = new HashMap<>();
            fieldSchema.put("type", "integer");
            fieldSchema.put("minimum", negMin);
            fieldSchema.put("maximum", negMax);
            Schema schema = SchemaParser.fromJsonSchema(objectSchema(
                Map.of("deep", fieldSchema)));
            Object value = schema.generate().get("deep");
            assertInstanceOf(Number.class, value);
            long longVal = ((Number) value).longValue();
            assertTrue(longVal >= negMin && longVal <= negMax,
                "Expected " + negMin + " to " + negMax + ", got: " + longVal);
        }
    }

    @Nested
    @DisplayName("nested object edge cases")
    class NestedObjectEdgeCases {

        @Test
        @DisplayName("nested object skips non-conforming entries")
        void skipsNonConforming() {
            Map<Object, Object> mixedProps = new HashMap<>();
            mixedProps.put("valid", Map.of("type", "string"));
            mixedProps.put(42, Map.of("type", "string"));  // non-string key
            mixedProps.put("invalid", "not-a-map");          // non-map value
            Map<String, Object> nested = new HashMap<>();
            nested.put("type", "object");
            nested.put("properties", mixedProps);
            Schema schema = SchemaParser.fromJsonSchema(objectSchema(
                Map.of("obj", nested)));
            @SuppressWarnings("unchecked")
            Map<String, Object> result = (Map<String, Object>) schema.generate().get("obj");
            assertNotNull(result);
            assertTrue(result.containsKey("valid"));
            assertInstanceOf(String.class, result.get("valid"));
        }
    }

    @Nested
    @DisplayName("type list edge cases")
    class TypeListEdgeCases {

        @Test
        @DisplayName("empty type list falls to untyped")
        void emptyTypeList() {
            Map<String, Object> fieldSchema = new HashMap<>();
            fieldSchema.put("type", List.of());
            Schema schema = SchemaParser.fromJsonSchema(objectSchema(
                Map.of("val", fieldSchema)));
            Object value = schema.generate().get("val");
            assertInstanceOf(String.class, value);
        }

        @Test
        @DisplayName("type list with non-string entries is handled")
        void nonStringTypeList() {
            Map<String, Object> fieldSchema = new HashMap<>();
            fieldSchema.put("type", List.of(42, "string"));
            Schema schema = SchemaParser.fromJsonSchema(objectSchema(
                Map.of("val", fieldSchema)));
            Object value = schema.generate().get("val");
            assertInstanceOf(String.class, value);
        }

        @Test
        @DisplayName("type as non-string non-list returns empty string type")
        void typeAsNumber() {
            Map<String, Object> fieldSchema = new HashMap<>();
            fieldSchema.put("type", 42);
            Schema schema = SchemaParser.fromJsonSchema(objectSchema(
                Map.of("val", fieldSchema)));
            // falls through to resolveUntyped -> string
            Object value = schema.generate().get("val");
            assertInstanceOf(String.class, value);
        }
    }

    @Nested
    @DisplayName("enum with non-list value")
    class EnumEdgeCases {

        @Test
        @DisplayName("enum with non-list value falls through to type-based generation")
        void nonListEnum() {
            Map<String, Object> fieldSchema = new HashMap<>();
            fieldSchema.put("type", "string");
            fieldSchema.put("enum", "not-a-list");
            Schema schema = SchemaParser.fromJsonSchema(objectSchema(
                Map.of("data", fieldSchema)));
            Object value = schema.generate().get("data");
            assertInstanceOf(String.class, value);
        }
    }
}
