/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.schema;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.frikit.krandom.generator.GeneratorConfig;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

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
            assertThrows(NullPointerException.class, () -> SchemaParser.fromJsonSchema(null));
        }

        @Test
        @DisplayName("rejects null config")
        void rejectsNullConfig() {
            assertThrows(NullPointerException.class, () -> SchemaParser.fromJsonSchema(Map.of(), null));
        }

        @Test
        @DisplayName("rejects non-object type")
        void rejectsNonObjectType() {
            var ex = assertThrows(
                    IllegalArgumentException.class, () -> SchemaParser.fromJsonSchema(Map.of("type", "string")));
            assertTrue(ex.getMessage().contains("\"type\": \"object\""));
        }

        @Test
        @DisplayName("rejects missing type")
        void rejectsMissingType() {
            assertThrows(
                    IllegalArgumentException.class, () -> SchemaParser.fromJsonSchema(Map.of("properties", Map.of())));
        }

        @Test
        @DisplayName("rejects missing properties")
        void rejectsMissingProperties() {
            assertThrows(IllegalArgumentException.class, () -> SchemaParser.fromJsonSchema(Map.of("type", "object")));
        }

        @Test
        @DisplayName("rejects non-map properties")
        void rejectsNonMapProperties() {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> SchemaParser.fromJsonSchema(Map.of("type", "object", "properties", "not-a-map")));
        }

        @Test
        @DisplayName("rejects empty properties")
        void rejectsEmptyProperties() {
            assertThrows(IllegalArgumentException.class, () -> SchemaParser.fromJsonSchema(objectSchema(Map.of())));
        }

        @Test
        @DisplayName("rejects non-string property key")
        void rejectsNonStringPropertyKey() {
            Map<Object, Object> properties = new HashMap<>();
            properties.put(42, Map.of("type", "string"));
            Map<String, Object> schema = new HashMap<>();
            schema.put("type", "object");
            schema.put("properties", properties);
            assertThrows(IllegalArgumentException.class, () -> SchemaParser.fromJsonSchema(schema));
        }

        @Test
        @DisplayName("rejects non-map property value")
        void rejectsNonMapPropertyValue() {
            Map<String, Object> properties = new HashMap<>();
            properties.put("name", "not-a-schema");
            assertThrows(IllegalArgumentException.class, () -> SchemaParser.fromJsonSchema(objectSchema(properties)));
        }

        @Test
        @DisplayName("rejects a non-string format with its schema path")
        void rejectsNonStringFormat() {
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> SchemaParser.fromJsonSchema(
                            objectSchema(Map.of("value", Map.of("type", "string", "format", 42)))));
            assertTrue(exception.getMessage().contains("$.properties.value"));
        }

        @Test
        @DisplayName("rejects unsupported type names with their schema path")
        void rejectsUnsupportedType() {
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> SchemaParser.fromJsonSchema(objectSchema(Map.of("value", Map.of("type", "binary")))));
            assertTrue(exception.getMessage().contains("$.properties.value"));
        }

        @Test
        @DisplayName("rejects type lists containing multiple concrete types")
        void rejectsMultipleConcreteTypes() {
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> SchemaParser.fromJsonSchema(
                            objectSchema(Map.of("value", Map.of("type", List.of("string", "integer"))))));
            assertTrue(exception.getMessage().contains("$.properties.value"));
        }

        @Test
        @DisplayName("rejects references and composition keywords with their schema path")
        void rejectsUnsupportedReferencesAndCompositions() {
            IllegalArgumentException reference = assertThrows(
                    IllegalArgumentException.class,
                    () -> SchemaParser.fromJsonSchema(
                            objectSchema(Map.of("address", Map.of("$ref", "#/components/schemas/Address")))));
            assertTrue(reference.getMessage().contains("$ref"));
            assertTrue(reference.getMessage().contains("$.properties.address"));

            IllegalArgumentException composition = assertThrows(
                    IllegalArgumentException.class,
                    () -> SchemaParser.fromJsonSchema(objectSchema(
                            Map.of("status", Map.of("allOf", List.of(Map.of("type", "string")))))));
            assertTrue(composition.getMessage().contains("allOf"));
            assertTrue(composition.getMessage().contains("$.properties.status"));
        }

        @Test
        @DisplayName("rejects unsupported array items and cyclic schema maps with their path")
        void rejectsUnsupportedItemsAndCyclicMaps() {
            IllegalArgumentException items = assertThrows(
                    IllegalArgumentException.class,
                    () -> SchemaParser.fromJsonSchema(objectSchema(
                            Map.of("tags", Map.of("type", "array", "items", Map.of("oneOf", List.of()))))));
            assertTrue(items.getMessage().contains("oneOf"));
            assertTrue(items.getMessage().contains("$.properties.tags.items"));

            Map<String, Object> recursive = new HashMap<>();
            Map<String, Object> properties = new HashMap<>();
            recursive.put("type", "object");
            recursive.put("properties", properties);
            properties.put("self", recursive);
            IllegalArgumentException cycle =
                    assertThrows(IllegalArgumentException.class, () -> SchemaParser.fromJsonSchema(recursive));
            assertTrue(cycle.getMessage().contains("Recursive JSON Schema map"));
            assertTrue(cycle.getMessage().contains("$.properties.self"));
        }

        @Test
        @DisplayName("accepts the JSON Schema control keywords emitted by krandom exports")
        void acceptsKrandomExportControlKeywords() {
            assertDoesNotThrow(() -> SchemaParser.fromJsonSchema(Map.of(
                    "$schema",
                    "https://json-schema.org/draft/2020-12/schema",
                    "type",
                    "object",
                    "additionalProperties",
                    false,
                    "required",
                    List.of("name"),
                    "properties",
                    Map.of("name", Map.of("type", "string")))));
        }
    }

    @Nested
    @DisplayName("string types")
    class StringTypes {

        @Test
        @DisplayName("generates plain strings")
        void plainString() {
            Schema schema = SchemaParser.fromJsonSchema(objectSchema(Map.of("data", Map.of("type", "string"))));
            Map<String, Object> record = schema.generate();
            assertInstanceOf(String.class, record.get("data"));
        }

        @Test
        @DisplayName("respects minLength and maxLength")
        void boundedString() {
            Schema schema = SchemaParser.fromJsonSchema(
                    objectSchema(Map.of("code", Map.of("type", "string", "minLength", 5, "maxLength", 5))));
            for (int i = 0; i < 10; i++) {
                String value = (String) schema.generate().get("code");
                assertEquals(5, value.length());
            }
        }

        @Test
        @DisplayName("generates from regex pattern")
        void regexPattern() {
            // Use a non-semantic field name so pattern resolution in resolveString is exercised
            Schema schema = SchemaParser.fromJsonSchema(
                    objectSchema(Map.of("code123", Map.of("type", "string", "pattern", "\\d{5}"))));
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
            Schema schema = SchemaParser.fromJsonSchema(objectSchema(Map.of("xyzField", fieldSchema)));
            String value = (String) schema.generate().get("xyzField");
            assertTrue(value.matches("[A-Z]{3}"), "Expected 3 uppercase, got: " + value);
        }

        @Test
        @DisplayName("format: email")
        void formatEmail() {
            Schema schema = SchemaParser.fromJsonSchema(
                    objectSchema(Map.of("mail", Map.of("type", "string", "format", "email"))));
            String value = (String) schema.generate().get("mail");
            assertTrue(value.contains("@"), "Expected email, got: " + value);
        }

        @Test
        @DisplayName("format: idn-email uses the email generator, whose values are all valid for it")
        void formatIdnEmail() {
            Schema schema = SchemaParser.fromJsonSchema(
                    objectSchema(Map.of("mail", Map.of("type", "string", "format", "idn-email"))));
            String value = (String) schema.generate().get("mail");
            assertTrue(value.contains("@"), "Expected email, got: " + value);
        }

        @Test
        @DisplayName("format: uri")
        void formatUri() {
            Schema schema = SchemaParser.fromJsonSchema(
                    objectSchema(Map.of("link", Map.of("type", "string", "format", "uri"))));
            String value = (String) schema.generate().get("link");
            assertNotNull(value);
            assertTrue(value.contains("://"), "Expected URI, got: " + value);
        }

        @Test
        @DisplayName("format: url alias")
        void formatUrl() {
            Schema schema = SchemaParser.fromJsonSchema(
                    objectSchema(Map.of("link", Map.of("type", "string", "format", "url"))));
            String value = (String) schema.generate().get("link");
            assertTrue(value.contains("://"), "Expected URL, got: " + value);
        }

        @Test
        @DisplayName("format: uuid")
        void formatUuid() {
            Schema schema = SchemaParser.fromJsonSchema(
                    objectSchema(Map.of("identifier", Map.of("type", "string", "format", "uuid"))));
            String value = (String) schema.generate().get("identifier");
            assertTrue(
                    value.matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"),
                    "Expected UUID, got: " + value);
        }

        @Test
        @DisplayName("format: date")
        void formatDate() {
            Schema schema = SchemaParser.fromJsonSchema(
                    objectSchema(Map.of("dob", Map.of("type", "string", "format", "date"))));
            String value = (String) schema.generate().get("dob");
            assertTrue(value.matches("\\d{4}-\\d{2}-\\d{2}"), "Expected date, got: " + value);
        }

        @Test
        @DisplayName("format: date-time")
        void formatDateTime() {
            Schema schema = SchemaParser.fromJsonSchema(
                    objectSchema(Map.of("ts", Map.of("type", "string", "format", "date-time"))));
            String value = (String) schema.generate().get("ts");
            assertNotNull(value);
            assertTrue(value.contains("T"), "Expected date-time, got: " + value);
        }

        @Test
        @DisplayName("format: time")
        void formatTime() {
            Schema schema = SchemaParser.fromJsonSchema(
                    objectSchema(Map.of("clock", Map.of("type", "string", "format", "time"))));
            String value = (String) schema.generate().get("clock");
            assertTrue(value.matches("\\d{2}:\\d{2}(:\\d{2}.*)?"), "Expected time, got: " + value);
        }

        @Test
        @DisplayName("format: ipv4")
        void formatIpv4() {
            Schema schema = SchemaParser.fromJsonSchema(
                    objectSchema(Map.of("addr", Map.of("type", "string", "format", "ipv4"))));
            String value = (String) schema.generate().get("addr");
            assertTrue(value.matches("\\d+\\.\\d+\\.\\d+\\.\\d+"), "Expected IPv4, got: " + value);
        }

        @Test
        @DisplayName("format: ipv6")
        void formatIpv6() {
            Schema schema = SchemaParser.fromJsonSchema(
                    objectSchema(Map.of("addr6", Map.of("type", "string", "format", "ipv6"))));
            String value = (String) schema.generate().get("addr6");
            assertTrue(value.contains(":"), "Expected IPv6, got: " + value);
        }

        @Test
        @DisplayName("format: hostname")
        void formatHostname() {
            Schema schema = SchemaParser.fromJsonSchema(
                    objectSchema(Map.of("host", Map.of("type", "string", "format", "hostname"))));
            String value = (String) schema.generate().get("host");
            assertTrue(value.contains("."), "Expected hostname, got: " + value);
        }

        @Test
        @DisplayName("unknown format falls through to semantic or default string")
        void unknownFormat() {
            Schema schema = SchemaParser.fromJsonSchema(
                    objectSchema(Map.of("data", Map.of("type", "string", "format", "unknown-custom"))));
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
            Schema schema = SchemaParser.fromJsonSchema(objectSchema(Map.of("count", Map.of("type", "integer"))));
            Object value = schema.generate().get("count");
            assertInstanceOf(Number.class, value);
        }

        @Test
        @DisplayName("respects minimum and maximum")
        void boundedInteger() {
            Schema schema = SchemaParser.fromJsonSchema(
                    objectSchema(Map.of("score", Map.of("type", "integer", "minimum", 10, "maximum", 20))));
            for (int i = 0; i < 50; i++) {
                int value = ((Number) schema.generate().get("score")).intValue();
                assertTrue(value >= 10 && value <= 20, "Expected 10-20, got: " + value);
            }
        }

        @Test
        @DisplayName("respects exclusiveMinimum and exclusiveMaximum")
        void exclusiveBounds() {
            Schema schema = SchemaParser.fromJsonSchema(objectSchema(
                    Map.of("val", Map.of("type", "integer", "exclusiveMinimum", 0, "exclusiveMaximum", 3))));
            for (int i = 0; i < 50; i++) {
                int value = ((Number) schema.generate().get("val")).intValue();
                assertTrue(value >= 1 && value <= 2, "Expected 1-2, got: " + value);
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
            assertTrue(
                    longVal >= bigMin && longVal <= bigMax, "Expected " + bigMin + "-" + bigMax + ", got: " + longVal);
        }
    }

    @Nested
    @DisplayName("number types")
    class NumberTypes {

        @Test
        @DisplayName("generates doubles with defaults")
        void defaultNumber() {
            Schema schema = SchemaParser.fromJsonSchema(objectSchema(Map.of("price", Map.of("type", "number"))));
            Object value = schema.generate().get("price");
            assertInstanceOf(Number.class, value);
        }

        @Test
        @DisplayName("respects minimum and maximum")
        void boundedNumber() {
            Schema schema = SchemaParser.fromJsonSchema(
                    objectSchema(Map.of("rate", Map.of("type", "number", "minimum", 0.0, "maximum", 1.0))));
            for (int i = 0; i < 50; i++) {
                double value = ((Number) schema.generate().get("rate")).doubleValue();
                assertTrue(value >= 0.0 && value <= 1.0, "Expected 0.0-1.0, got: " + value);
            }
        }

        @Test
        @DisplayName("respects exclusiveMinimum and exclusiveMaximum")
        void exclusiveBounds() {
            Schema schema = SchemaParser.fromJsonSchema(objectSchema(
                    Map.of("temp", Map.of("type", "number", "exclusiveMinimum", 0.0, "exclusiveMaximum", 100.0))));
            for (int i = 0; i < 20; i++) {
                double value = ((Number) schema.generate().get("temp")).doubleValue();
                assertTrue(value > 0.0 && value < 100.0, "Expected >0 and <100, got: " + value);
            }
        }
    }

    @Nested
    @DisplayName("boolean type")
    class BooleanType {

        @Test
        @DisplayName("generates booleans")
        void generateBoolean() {
            Schema schema = SchemaParser.fromJsonSchema(objectSchema(Map.of("active", Map.of("type", "boolean"))));
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
            Schema schema = SchemaParser.fromJsonSchema(objectSchema(Map.of("nothing", Map.of("type", "null"))));
            assertNull(schema.generate().get("nothing"));
        }
    }

    @Nested
    @DisplayName("enum and const")
    class EnumAndConst {

        @Test
        @DisplayName("const always returns the constant value")
        void constValue() {
            Schema schema = SchemaParser.fromJsonSchema(
                    objectSchema(Map.of("status", Map.of("type", "string", "const", "active"))));
            for (int i = 0; i < 10; i++) {
                assertEquals("active", schema.generate().get("status"));
            }
        }

        @Test
        @DisplayName("const must be one of the enum values")
        void constOutsideEnum() {
            Map<String, Object> fieldSchema = new HashMap<>();
            fieldSchema.put("type", "string");
            fieldSchema.put("const", "fixed");
            fieldSchema.put("enum", List.of("a", "b", "c"));
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> SchemaParser.fromJsonSchema(objectSchema(Map.of("val", fieldSchema))));
            assertTrue(exception.getMessage().contains("$.properties.val"), exception.getMessage());
        }

        @Test
        @DisplayName("enum selects from provided values")
        void enumValues() {
            List<String> options = List.of("red", "green", "blue");
            Map<String, Object> fieldSchema = new HashMap<>();
            fieldSchema.put("type", "string");
            fieldSchema.put("enum", options);
            Schema schema = SchemaParser.fromJsonSchema(objectSchema(Map.of("color", fieldSchema)));
            for (int i = 0; i < 20; i++) {
                Object value = schema.generate().get("color");
                assertTrue(options.contains(value), "Expected one of " + options + ", got: " + value);
            }
        }

        @Test
        @DisplayName("rejects an empty enum with the schema path")
        void emptyEnum() {
            Map<String, Object> fieldSchema = new HashMap<>();
            fieldSchema.put("type", "string");
            fieldSchema.put("enum", List.of());
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> SchemaParser.fromJsonSchema(objectSchema(Map.of("data", fieldSchema))));
            assertTrue(exception.getMessage().contains("$.properties.data"));
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
                    Map.of("scores", Map.of("type", "array", "items", items, "minItems", 3, "maxItems", 3))));
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
            Schema schema = SchemaParser.fromJsonSchema(
                    objectSchema(Map.of("tags", Map.of("type", "array", "minItems", 2, "maxItems", 2))));
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
            Schema schema = SchemaParser.fromJsonSchema(objectSchema(Map.of(
                    "items",
                    Map.of("type", "array", "items", Map.of("type", "string"), "minItems", 1, "maxItems", 5))));
            for (int i = 0; i < 20; i++) {
                @SuppressWarnings("unchecked")
                List<Object> list = (List<Object>) schema.generate().get("items");
                assertTrue(list.size() >= 1 && list.size() <= 5, "Expected 1-5 items, got: " + list.size());
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
            Map<String, Object> address = Map.of("type", "object", "properties", addressProps);

            Schema schema = SchemaParser.fromJsonSchema(objectSchema(Map.of("address", address)));
            @SuppressWarnings("unchecked")
            Map<String, Object> nested = (Map<String, Object>) schema.generate().get("address");
            assertNotNull(nested);
            assertInstanceOf(String.class, nested.get("street"));
            assertTrue(((String) nested.get("zip")).matches("\\d{5}"));
        }

        @Test
        @DisplayName("nested object without properties returns empty map")
        void emptyNestedObject() {
            Schema schema = SchemaParser.fromJsonSchema(objectSchema(Map.of("meta", Map.of("type", "object"))));
            @SuppressWarnings("unchecked")
            Map<String, Object> nested = (Map<String, Object>) schema.generate().get("meta");
            assertNotNull(nested);
            assertTrue(nested.isEmpty());
        }

        @Test
        @DisplayName("nested object with empty properties returns empty map")
        void emptyPropertiesNestedObject() {
            Schema schema = SchemaParser.fromJsonSchema(
                    objectSchema(Map.of("meta", Map.of("type", "object", "properties", Map.of()))));
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
            Schema schema = SchemaParser.fromJsonSchema(objectSchema(Map.of("data", fieldSchema)));
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
            Schema schema = SchemaParser.fromJsonSchema(objectSchema(Map.of("firstName", Map.of("type", "string"))));
            String value = (String) schema.generate().get("firstName");
            assertNotNull(value);
            assertTrue(value.length() > 0);
        }

        @Test
        @DisplayName("resolves email to email address")
        void email() {
            Schema schema = SchemaParser.fromJsonSchema(objectSchema(Map.of("email", Map.of("type", "string"))));
            String value = (String) schema.generate().get("email");
            assertTrue(value.contains("@"), "Expected email, got: " + value);
        }

        @Test
        @DisplayName("resolves city semantically")
        void city() {
            Schema schema = SchemaParser.fromJsonSchema(objectSchema(Map.of("city", Map.of("type", "string"))));
            String value = (String) schema.generate().get("city");
            assertNotNull(value);
            assertTrue(value.length() > 0);
        }

        @Test
        @DisplayName("resolves snake_case field names")
        void snakeCase() {
            Schema schema = SchemaParser.fromJsonSchema(objectSchema(Map.of("last_name", Map.of("type", "string"))));
            String value = (String) schema.generate().get("last_name");
            assertNotNull(value);
            assertTrue(value.length() > 0);
        }

        @Test
        @DisplayName("resolves camelCase field names")
        void camelCase() {
            Schema schema = SchemaParser.fromJsonSchema(objectSchema(Map.of("lastName", Map.of("type", "string"))));
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
                assertNotNull(entry.getValue(), "Field '" + entry.getKey() + "' should not be null");
                assertInstanceOf(String.class, entry.getValue());
                assertTrue(
                        ((String) entry.getValue()).length() > 0, "Field '" + entry.getKey() + "' should not be empty");
            }
        }

        @Test
        @DisplayName("non-semantic field name falls back to generic string")
        void nonSemanticField() {
            Schema schema = SchemaParser.fromJsonSchema(objectSchema(Map.of("xyzAbcQwerty", Map.of("type", "string"))));
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
            Schema schema = SchemaParser.fromJsonSchema(objectSchema(Map.of("val", fieldSchema)));
            Object value = schema.generate().get("val");
            assertInstanceOf(Number.class, value);
        }

        @Test
        @DisplayName("all-null type list produces null")
        void allNullTypeList() {
            Map<String, Object> fieldSchema = new HashMap<>();
            fieldSchema.put("type", List.of("null"));
            Schema schema = SchemaParser.fromJsonSchema(objectSchema(Map.of("val", fieldSchema)));
            assertNull(schema.generate().get("val"));
        }
    }

    @Nested
    @DisplayName("untyped fields")
    class UntypedFields {

        @Test
        @DisplayName("field without type uses semantic resolution or string fallback")
        void noType() {
            Schema schema = SchemaParser.fromJsonSchema(objectSchema(Map.of("xyzUnknown", Map.of())));
            Object value = schema.generate().get("xyzUnknown");
            assertInstanceOf(String.class, value);
        }

        @Test
        @DisplayName("untyped field with semantic name resolves semantically")
        void untypedSemantic() {
            Map<String, Object> emptySchema = new HashMap<>();
            Schema schema = SchemaParser.fromJsonSchema(objectSchema(Map.of("email", emptySchema)));
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
            Schema schema = SchemaParser.fromOpenApi(objectSchema(Map.of("data", Map.of("type", "string"))));
            Object value = schema.generate().get("data");
            assertInstanceOf(String.class, value);
        }

        @Test
        @DisplayName("fromOpenApi with config delegates to fromJsonSchema")
        void fromOpenApiWithConfig() {
            GeneratorConfig config = GeneratorConfig.builder().seed(42L).build();
            Schema schema = SchemaParser.fromOpenApi(objectSchema(Map.of("data", Map.of("type", "string"))), config);
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
                assertTrue(score >= 1 && score <= 100, "Expected 1-100, got: " + score);
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
            Map<String, Object> address = Map.of("type", "object", "properties", addressProps);

            Map<String, Object> tagItems = Map.of("type", "string");

            Map<String, Object> props = new HashMap<>();
            props.put("firstName", Map.of("type", "string"));
            props.put("email", Map.of("type", "string", "format", "email"));
            props.put("age", Map.of("type", "integer", "minimum", 18, "maximum", 65));
            props.put("score", Map.of("type", "number", "minimum", 0.0, "maximum", 100.0));
            props.put("active", Map.of("type", "boolean"));
            props.put("tags", Map.of("type", "array", "items", tagItems, "minItems", 1, "maxItems", 3));
            props.put("address", address);
            props.put("status", Map.of("type", "string", "enum", List.of("active", "inactive", "pending")));
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
            assertTrue(List.of("active", "inactive", "pending").contains(record.get("status")));
            assertEquals("1.0", record.get("version"));
        }
    }

    @Nested
    @DisplayName("default config overload")
    class DefaultConfig {

        @Test
        @DisplayName("single-arg fromJsonSchema uses default config")
        void defaultConfig() {
            Schema schema = SchemaParser.fromJsonSchema(objectSchema(Map.of("val", Map.of("type", "string"))));
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
            Schema schema = SchemaParser.fromJsonSchema(objectSchema(Map.of("phoneNumber", Map.of("type", "string"))));
            assertInstanceOf(String.class, schema.generate().get("phoneNumber"));
        }

        @Test
        @DisplayName("resolves age")
        void age() {
            Schema schema = SchemaParser.fromJsonSchema(objectSchema(Map.of("age", Map.of("type", "string"))));
            assertNotNull(schema.generate().get("age"));
        }

        @Test
        @DisplayName("resolves state")
        void state() {
            Schema schema = SchemaParser.fromJsonSchema(objectSchema(Map.of("state", Map.of("type", "string"))));
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
            Schema schema = SchemaParser.fromJsonSchema(objectSchema(Map.of("website", Map.of("type", "string"))));
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
            Schema schema = SchemaParser.fromJsonSchema(objectSchema(Map.of("domain", Map.of("type", "string"))));
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
            Schema schema = SchemaParser.fromJsonSchema(objectSchema(Map.of("emailAddress", Map.of("type", "string"))));
            String value = (String) schema.generate().get("emailAddress");
            assertTrue(value.contains("@"), "Expected email, got: " + value);
        }

        @Test
        @DisplayName("resolves zipCode")
        void zipCode() {
            Schema schema = SchemaParser.fromJsonSchema(objectSchema(Map.of("zipCode", Map.of("type", "string"))));
            assertNotNull(schema.generate().get("zipCode"));
        }

        @Test
        @DisplayName("resolves id semantically")
        void id() {
            Schema schema = SchemaParser.fromJsonSchema(objectSchema(Map.of("id", Map.of("type", "string"))));
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
            Schema schema = SchemaParser.fromJsonSchema(objectSchema(Map.of("email", fieldSchema)));
            String value = (String) schema.generate().get("email");
            assertTrue(value.contains("@"), "Expected semantic email resolution, got: " + value);
        }

        @Test
        @DisplayName("unknown format with non-semantic name falls to bounded string")
        void unknownFormatNonSemantic() {
            Map<String, Object> fieldSchema = new HashMap<>();
            fieldSchema.put("type", "string");
            fieldSchema.put("format", "custom-unknown");
            fieldSchema.put("minLength", 10);
            fieldSchema.put("maxLength", 10);
            Schema schema = SchemaParser.fromJsonSchema(objectSchema(Map.of("xyzData", fieldSchema)));
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
            Schema schema = SchemaParser.fromJsonSchema(objectSchema(Map.of("huge", fieldSchema)));
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
            Schema schema = SchemaParser.fromJsonSchema(objectSchema(Map.of("deep", fieldSchema)));
            Object value = schema.generate().get("deep");
            assertInstanceOf(Number.class, value);
            long longVal = ((Number) value).longValue();
            assertTrue(
                    longVal >= negMin && longVal <= negMax,
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
            mixedProps.put(42, Map.of("type", "string")); // non-string key
            mixedProps.put("invalid", "not-a-map"); // non-map value
            Map<String, Object> nested = new HashMap<>();
            nested.put("type", "object");
            nested.put("properties", mixedProps);
            Schema schema = SchemaParser.fromJsonSchema(objectSchema(Map.of("obj", nested)));
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
        @DisplayName("rejects an empty type list")
        void emptyTypeList() {
            Map<String, Object> fieldSchema = new HashMap<>();
            fieldSchema.put("type", List.of());
            assertThrows(
                    IllegalArgumentException.class,
                    () -> SchemaParser.fromJsonSchema(objectSchema(Map.of("val", fieldSchema))));
        }

        @Test
        @DisplayName("rejects type lists with non-string entries")
        void nonStringTypeList() {
            Map<String, Object> fieldSchema = new HashMap<>();
            fieldSchema.put("type", List.of(42, "string"));
            assertThrows(
                    IllegalArgumentException.class,
                    () -> SchemaParser.fromJsonSchema(objectSchema(Map.of("val", fieldSchema))));
        }

        @Test
        @DisplayName("rejects a non-string non-list type")
        void typeAsNumber() {
            Map<String, Object> fieldSchema = new HashMap<>();
            fieldSchema.put("type", 42);
            assertThrows(
                    IllegalArgumentException.class,
                    () -> SchemaParser.fromJsonSchema(objectSchema(Map.of("val", fieldSchema))));
        }
    }

    @Nested
    @DisplayName("malformed enum")
    class EnumEdgeCases {

        @Test
        @DisplayName("rejects an enum with a non-list value")
        void nonListEnum() {
            Map<String, Object> fieldSchema = new HashMap<>();
            fieldSchema.put("type", "string");
            fieldSchema.put("enum", "not-a-list");
            assertThrows(
                    IllegalArgumentException.class,
                    () -> SchemaParser.fromJsonSchema(objectSchema(Map.of("data", fieldSchema))));
        }
    }

    @Nested
    @DisplayName("combined constraints")
    class CombinedConstraints {

        private static List<Object> values(Map<String, Object> property) {
            return values(property, GeneratorConfig.builder().seed(7L).build());
        }

        private static List<Object> values(Map<String, Object> property, GeneratorConfig config) {
            List<Object> values = new ArrayList<>();
            for (Map<String, Object> record : SchemaParser.fromJsonSchema(
                            objectSchema(Map.of("value", property)), config)
                    .generateBatch(300)) {
                values.add(record.get("value"));
            }
            return values;
        }

        private static Set<Object> distinct(Map<String, Object> property) {
            return new HashSet<>(values(property));
        }

        private static Set<Long> integers(Map<String, Object> property) {
            Set<Long> integers = new HashSet<>();
            for (Object value : values(property)) {
                integers.add(((Number) value).longValue());
            }
            return integers;
        }

        private static Set<Double> numbers(Map<String, Object> property) {
            Set<Double> numbers = new HashSet<>();
            for (Object value : values(property)) {
                numbers.add(((Number) value).doubleValue());
            }
            return numbers;
        }

        private static void assertRejected(Map<String, Object> property) {
            assertRejectedAt("$.properties.value", Map.of("value", property));
        }

        private static void assertRejectedAt(String path, Map<String, Object> properties) {
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class, () -> SchemaParser.fromJsonSchema(objectSchema(properties)));
            assertTrue(exception.getMessage().contains(path), exception.getMessage());
        }

        private static Map<String, Object> withNull(String keyword, Object value, String otherKeyword, Object other) {
            Map<String, Object> property = new HashMap<>();
            property.put(keyword, value);
            property.put(otherKeyword, other);
            return property;
        }

        private static Set<Object> setOf(Object... values) {
            return new HashSet<>(Arrays.asList(values));
        }

        @Test
        @DisplayName("rejects a pattern whose values never fit the length bounds")
        void rejectsPatternThatCannotFit() {
            assertRejected(Map.of("type", "string", "pattern", "^A{8}$", "maxLength", 3));
            assertRejected(Map.of("type", "string", "pattern", "^A{8}$", "minLength", 9));
        }

        @Test
        @DisplayName("open-ended repetition can reach a declared minimum length")
        void openEndedRepetitionReachesMinLength() {
            Set<Integer> lengths = new HashSet<>();
            for (Object value : values(Map.of("type", "string", "pattern", "^[a-z]+$", "minLength", 20))) {
                assertTrue(((String) value).matches("[a-z]{20,40}"), "bounds violated: " + value);
                lengths.add(((String) value).length());
            }
            assertTrue(lengths.size() > 10, "lengths must vary: " + lengths);

            for (Object value :
                    values(Map.of("type", "string", "pattern", "^[a-z]+$", "minLength", 20, "maxLength", 22))) {
                assertTrue(((String) value).matches("[a-z]{20,22}"), "bounds violated: " + value);
            }
            for (Object value : values(Map.of("type", "string", "pattern", "^[a-z]{25,}$", "minLength", 30))) {
                assertTrue(((String) value).matches("[a-z]{30,60}"), "bounds violated: " + value);
            }
            for (Object value : values(Map.of("type", "string", "pattern", "^[a-z]{20,40}$", "minLength", 20))) {
                assertTrue(((String) value).matches("[a-z]{20,40}"), "bounds violated: " + value);
            }
        }

        @Test
        @DisplayName("open-ended repetition stops at ten unless a longer minimum asks for more")
        void openEndedRepetitionDefaultLimit() {
            Set<Integer> lengths = new HashSet<>();
            for (Object value : values(Map.of("type", "string", "pattern", "^[a-z]+$"))) {
                lengths.add(((String) value).length());
            }
            assertEquals(10, lengths.stream().max(Integer::compare).orElseThrow());

            lengths.clear();
            for (Object value : values(Map.of("type", "string", "pattern", "^[a-z]+$", "maxLength", 50))) {
                lengths.add(((String) value).length());
            }
            assertEquals(10, lengths.stream().max(Integer::compare).orElseThrow());
        }

        @Test
        @DisplayName("a minimum length beyond the repetition cap is rejected")
        void openEndedRepetitionCannotPassTheCap() {
            assertRejected(Map.of("type", "string", "pattern", "^[a-z]+$", "minLength", 10_001));
            assertRejected(Map.of("type", "string", "pattern", "^[a-z]+$", "minLength", Integer.MAX_VALUE));
        }

        @ParameterizedTest
        @ValueSource(
                strings = {
                    "^(?:abc)$",
                    "\\bfoo\\b",
                    "[\\d]{3}",
                    "a|b",
                    "a{20,}",
                    "^[a-z]+$",
                    "(?<word>[a-z]+)-\\d",
                    ".{3}",
                    "[\\w.-]+@example\\.com",
                })
        @DisplayName("every value generated from a pattern matches it")
        void patternValuesMatch(String regex) {
            Pattern pattern = Pattern.compile(regex);
            for (Object value : values(Map.of("type", "string", "pattern", regex))) {
                assertTrue(pattern.matcher((String) value).matches(), regex + " generated " + value);
            }
        }

        @Test
        @DisplayName("a pattern that cannot be generated from is rejected with its path and what is wrong")
        void rejectsPatternThatCannotBeGenerated() {
            Map<String, String> patterns = Map.of(
                    "(?=a)a", "Unsupported lookahead at position 0",
                    "(a)\\1", "Unsupported backreference \\1",
                    "[a", "Unclosed character class",
                    "a{5,2}", "Illegal repetition range");
            patterns.forEach((pattern, problem) -> {
                Map<String, Object> schema =
                        objectSchema(Map.of("value", Map.of("type", "string", "pattern", pattern)));
                IllegalArgumentException exception =
                        assertThrows(IllegalArgumentException.class, () -> SchemaParser.fromJsonSchema(schema));
                assertTrue(exception.getMessage().contains("$.properties.value"), exception.getMessage());
                assertTrue(exception.getMessage().contains(problem), exception.getMessage());
            });
        }

        @Test
        @DisplayName("a const or enum is only checked against a pattern, so it need not be generated from it")
        void explicitValuesAreCheckedAgainstAnyPattern() {
            assertEquals(Set.of("abc"), distinct(Map.of("const", "abc", "pattern", "(?=a)abc")));
            assertEquals(Set.of("abc"), distinct(Map.of("enum", List.of("abc", "xyz"), "pattern", "(?=a)abc")));
        }

        @Test
        @DisplayName("rejects a length range with no values, whatever else is declared")
        void rejectsEmptyLengthRange() {
            assertRejected(Map.of("type", "string", "minLength", 5, "maxLength", 1));
            assertRejected(Map.of("type", "string", "pattern", "a{1,9}", "minLength", 5, "maxLength", 1));
            assertRejected(Map.of("type", "string", "format", "uuid", "minLength", 5, "maxLength", 1));
        }

        @Test
        @DisplayName("draws pattern values until one fits the length bounds")
        void retriesPatternUntilItFits() {
            Set<Integer> lengths = new HashSet<>();
            for (Object value :
                    values(Map.of("type", "string", "pattern", "[a-z]{1,10}", "minLength", 6, "maxLength", 7))) {
                assertTrue(((String) value).matches("[a-z]{6,7}"), "bounds violated: " + value);
                lengths.add(((String) value).length());
            }
            assertEquals(Set.of(6, 7), lengths);
        }

        @Test
        @DisplayName("keeps drawing when only one pattern value in a hundred fits")
        void retriesRareFits() {
            assertEquals(Set.of("a"), distinct(Map.of("type", "string", "pattern", "a{1,100}", "maxLength", 1)));
        }

        @Test
        @DisplayName("a pattern that fits its bounds keeps the values it has without them")
        void fittingPatternKeepsItsValues() {
            Map<String, Object> fits = Map.of("type", "string", "pattern", "^A{8}$", "minLength", 8, "maxLength", 8);
            assertEquals(Set.of("AAAAAAAA"), distinct(fits));
            assertEquals(
                    values(Map.of("type", "string", "pattern", "[A-Z]{4}")),
                    values(Map.of("type", "string", "pattern", "[A-Z]{4}", "maxLength", 4)));
        }

        @Test
        @DisplayName("applies length bounds to a format")
        void formatMeetsLengthBounds() {
            assertRejected(Map.of("type", "string", "format", "uuid", "maxLength", 3));
            for (Object value : values(Map.of("type", "string", "format", "uuid", "minLength", 36, "maxLength", 36))) {
                assertEquals(36, ((String) value).length());
            }
            // Generated emails here run from 17 to 32 characters, so this bound turns many away.
            for (Object value : values(Map.of("type", "string", "format", "email", "maxLength", 20))) {
                assertTrue(((String) value).contains("@"), "not an email: " + value);
                assertTrue(((String) value).length() <= 20, "maxLength violated: " + value);
            }
        }

        @Test
        @DisplayName("applies length bounds to a format in an unseeded schema")
        void formatMeetsLengthBoundsUnseeded() {
            for (Object value :
                    values(Map.of("type", "string", "format", "email", "maxLength", 20), GeneratorConfig.defaults())) {
                assertTrue(((String) value).length() <= 20, "maxLength violated: " + value);
            }
        }

        @Test
        @DisplayName("a length bound of zero is honoured")
        void zeroLengthBounds() {
            for (Object value : values(Map.of("type", "string", "minLength", 0, "maxLength", 3))) {
                assertTrue(((String) value).length() <= 3, "maxLength violated: " + value);
            }
            assertEquals(Set.of(""), distinct(Map.of("type", "string", "maxLength", 0)));
            assertEquals(Set.of(""), distinct(Map.of("type", "string", "minLength", 0, "maxLength", 0)));
            assertEquals(Set.of(""), distinct(Map.of("type", "string", "pattern", "a?", "maxLength", 0)));
        }

        @Test
        @DisplayName("string constraints apply to a schema without a type")
        void untypedStringConstraints() {
            for (Object value : values(Map.of("maxLength", 2))) {
                assertTrue(((String) value).length() <= 2, "maxLength violated: " + value);
            }
            assertEquals(Set.of("a"), distinct(Map.of("pattern", "^a$")));
            for (Object value : values(Map.of("format", "uuid"))) {
                assertEquals(36, ((String) value).length());
            }
            // Numeric bounds say nothing about a string.
            assertInstanceOf(String.class, values(Map.of("minimum", 5)).getFirst());
        }

        @Test
        @DisplayName("enum keeps only the values within the length bounds")
        void enumWithinLength() {
            List<String> options = List.of("a", "bbbb");
            assertEquals(Set.of("a"), distinct(Map.of("type", "string", "enum", options, "maxLength", 1)));
            assertEquals(Set.of("bbbb"), distinct(Map.of("type", "string", "enum", options, "minLength", 2)));
            assertEquals(
                    Set.of("a", "bbbb"),
                    distinct(Map.of("type", "string", "enum", options, "minLength", 1, "maxLength", 4)));
            // JSON Schema counts characters, not UTF-16 units.
            String emoji = "😀";
            assertEquals(Set.of(emoji), distinct(Map.of("enum", List.of(emoji, "ab"), "maxLength", 1)));
        }

        @Test
        @DisplayName("enum keeps only the strings the pattern finds")
        void enumMatchingPattern() {
            List<String> options = List.of("apple", "banana");
            assertEquals(Set.of("apple"), distinct(Map.of("enum", options, "pattern", "^a")));
            // A pattern searches the string; it need not match all of it.
            assertEquals(Set.of("banana"), distinct(Map.of("enum", options, "pattern", "an")));
            // A pattern says nothing about a number.
            assertEquals(Set.of(5), distinct(Map.of("enum", List.of(5, "apple"), "pattern", "^b")));
        }

        @Test
        @DisplayName("rejects what a const or enum cannot be checked against, with the path")
        void rejectsWhatCannotBeChecked() {
            assertRejected(Map.of("enum", List.of("a"), "pattern", "[a"));
            assertRejected(Map.of("const", "a", "pattern", "[a"));
            assertRejected(Map.of("const", Double.NaN));
            assertRejected(Map.of("enum", List.of(1, Double.POSITIVE_INFINITY)));
            assertRejected(Map.of("const", 1, "minimum", Double.NaN));
        }

        @Test
        @DisplayName("enum keeps only the values of the declared type")
        void enumOfDeclaredType() {
            List<Object> options = Arrays.asList(1, "a", 2.5, 3.0, true, null, List.of("x"), Map.of("k", "v"));
            assertEquals(setOf(1, 3.0), distinct(withNull("type", "integer", "enum", options)));
            assertEquals(setOf(1, 2.5, 3.0), distinct(withNull("type", "number", "enum", options)));
            assertEquals(setOf("a"), distinct(withNull("type", "string", "enum", options)));
            assertEquals(setOf(true), distinct(withNull("type", "boolean", "enum", options)));
            assertEquals(setOf((Object) null), distinct(withNull("type", "null", "enum", options)));
            assertEquals(setOf(List.of("x")), distinct(withNull("type", "array", "enum", options)));
            assertEquals(setOf(Map.of("k", "v")), distinct(withNull("type", "object", "enum", options)));
            assertEquals(setOf("a", null), distinct(withNull("type", List.of("string", "null"), "enum", options)));

            Map<String, Object> nullable = withNull("type", "string", "enum", options);
            nullable.put("nullable", true);
            Schema schema = SchemaParser.fromJsonSchema(objectSchema(Map.of("value", nullable)));
            Set<Object> seen = new HashSet<>();
            for (Map<String, Object> record : schema.generateBatch(100)) {
                seen.add(record.get("value"));
            }
            assertEquals(setOf("a", null), seen);
        }

        @Test
        @DisplayName("enum keeps only the numbers within the bounds")
        void enumWithinNumericBounds() {
            List<Integer> options = List.of(1, 5, 10);
            assertEquals(Set.of(5, 10), distinct(Map.of("enum", options, "minimum", 5)));
            assertEquals(Set.of(1, 5), distinct(Map.of("enum", options, "maximum", 5)));
            assertEquals(Set.of(10), distinct(Map.of("enum", options, "exclusiveMinimum", 5)));
            assertEquals(Set.of(1), distinct(Map.of("enum", options, "exclusiveMaximum", 5)));
            assertEquals(Set.of(10), distinct(Map.of("enum", options, "minimum", 5, "exclusiveMinimum", true)));
            assertEquals(Set.of(5, 10), distinct(Map.of("enum", options, "minimum", 5, "exclusiveMinimum", false)));
            assertEquals(Set.of(1), distinct(Map.of("enum", options, "maximum", 5, "exclusiveMaximum", true)));
            assertEquals(Set.of(1, 5), distinct(Map.of("enum", options, "maximum", 5, "exclusiveMaximum", false)));
            // Whatever Java type holds a number, it compares by its value.
            BigDecimal decimal = new BigDecimal("2.5");
            assertEquals(Set.of(2L, decimal), distinct(Map.of("enum", List.of(1.5, 2L, decimal), "minimum", 2)));
        }

        @Test
        @DisplayName("enum keeps only the arrays and objects that meet their own constraints")
        void enumOfStructures() {
            List<List<Integer>> arrays = List.of(List.of(1), List.of(1, 2), List.of(1, 2, 3));
            assertEquals(Set.of(List.of(1)), distinct(Map.of("enum", arrays, "maxItems", 1)));
            assertEquals(Set.of(List.of(1, 2, 3)), distinct(Map.of("enum", arrays, "minItems", 3)));

            List<Object> items = List.of(List.of("a"), List.of(1));
            assertEquals(Set.of(List.of(1)), distinct(Map.of("enum", items, "items", Map.of("type", "integer"))));

            List<Map<String, Object>> objects = List.of(Map.of("a", 1), Map.of("a", "x"), Map.of("b", 2));
            Map<String, Object> aIsInteger = Map.of("a", Map.of("type", "integer"));
            assertEquals(
                    Set.of(Map.of("a", 1), Map.of("b", 2)),
                    distinct(Map.of("type", "object", "enum", objects, "properties", aIsInteger)));
            assertEquals(new HashSet<>(objects), distinct(Map.of("type", "object", "enum", objects)));
        }

        @Test
        @DisplayName("rejects an enum left with no values, with its path")
        void rejectsEmptiedEnum() {
            assertRejected(Map.of("type", "string", "enum", List.of("aa", "bbbb"), "maxLength", 1));
            assertRejected(Map.of("type", "integer", "enum", List.of("a", "b")));
            assertRejected(Map.of("enum", List.of(1, 2), "minimum", 3));
            assertRejected(Map.of("enum", List.of("a"), "pattern", "^b"));
        }

        @Test
        @DisplayName("rejects a const that breaks another constraint, with its path")
        void rejectsConflictingConst() {
            List<Map<String, Object>> conflicts = List.of(
                    Map.of("type", "integer", "const", "x"),
                    Map.of("type", "string", "const", 5),
                    Map.of("type", "integer", "const", 2.5),
                    Map.of("type", "boolean", "const", "true"),
                    Map.of("const", "abc", "maxLength", 2),
                    Map.of("const", "abc", "minLength", 4),
                    Map.of("const", "abc", "pattern", "^x"),
                    Map.of("const", 5, "minimum", 6),
                    Map.of("const", 5, "maximum", 4),
                    Map.of("const", 5, "exclusiveMinimum", 5),
                    Map.of("const", 5, "exclusiveMaximum", 5),
                    Map.of("const", "z", "enum", List.of("a", "b")),
                    Map.of("const", List.of(1, 2), "maxItems", 1),
                    Map.of("const", List.of(1, 2), "minItems", 3),
                    Map.of("const", List.of(1, 2), "items", Map.of("type", "string")),
                    Map.of("const", List.of(2), "items", Map.of("const", 1)),
                    Map.of("const", List.of(2), "items", Map.of("enum", List.of(1))),
                    Map.of("const", Map.of("a", 1), "properties", Map.of("a", Map.of("type", "string"))));
            for (Map<String, Object> conflict : conflicts) {
                assertRejected(conflict);
            }
            assertRejected(withNull("type", "string", "const", null));
        }

        @Test
        @DisplayName("a const that meets the other constraints is generated")
        void acceptsAgreeingConst() {
            assertEquals(Set.of("b"), distinct(Map.of("const", "b", "enum", List.of("a", "b"))));
            // Numbers compare by value, so 1 and 1.0 are the same JSON value.
            assertEquals(Set.of(1), distinct(Map.of("const", 1, "enum", List.of(1.0, 2.0))));
            assertEquals(Set.of(5.0), distinct(Map.of("type", "integer", "const", 5.0)));
            assertEquals(Set.of(5), distinct(Map.of("type", "number", "const", 5)));
            assertEquals(Set.of(5), distinct(Map.of("type", "integer", "const", 5, "minimum", 5, "maximum", 5)));
            assertEquals(Set.of("abc"), distinct(Map.of("const", "abc", "minLength", 3, "maxLength", 3)));
            assertEquals(Set.of("abc"), distinct(Map.of("const", "abc", "pattern", "b")));
            assertEquals(Set.of(List.of(1)), distinct(Map.of("const", List.of(1), "items", Map.of("type", "integer"))));
            assertEquals(Set.of(List.of(1)), distinct(Map.of("const", List.of(1), "items", "not-a-schema")));
            assertEquals(
                    Set.of(Map.of("a", 1)),
                    distinct(Map.of("const", Map.of("a", 1), "properties", Map.of("a", Map.of("type", "integer")))));
            assertEquals(
                    Set.of(Map.of("a", 1)),
                    distinct(Map.of("const", Map.of("a", 1), "properties", Map.of("b", Map.of("type", "string")))));
            assertEquals(
                    Set.of(Map.of("a", 1)), distinct(Map.of("const", Map.of("a", 1), "properties", Map.of("a", "x"))));
            assertEquals(Set.of(Map.of("a", 1)), distinct(Map.of("const", Map.of("a", 1), "properties", "x")));
            // A date is written as a string, so it is one as far as its type goes.
            LocalDate date = LocalDate.of(2020, 1, 2);
            assertEquals(Set.of(date), distinct(Map.of("type", "string", "const", date)));
            assertEquals(setOf((Object) null), distinct(withNull("const", null, "enum", Arrays.asList("a", null))));
            assertEquals(setOf((Object) null), distinct(withNull("const", null, "type", List.of("string", "null"))));
        }

        @Test
        @DisplayName("a keyword constrains only its own JSON type")
        void keywordsConstrainTheirOwnType() {
            assertEquals(Set.of(5), distinct(Map.of("const", 5, "minLength", 9)));
            assertEquals(Set.of("abc"), distinct(Map.of("const", "abc", "minimum", 100)));
            assertEquals(Set.of(true), distinct(Map.of("const", true, "maxLength", 0, "maximum", -1)));
            assertEquals(Set.of("abc"), distinct(Map.of("const", "abc", "maxItems", 0)));
        }

        @Test
        @DisplayName("format does not constrain a const, because it is an annotation")
        void formatIsAnAnnotation() {
            assertEquals(Set.of("not-an-email"), distinct(Map.of("const", "not-an-email", "format", "email")));
            assertEquals(Set.of("x"), distinct(Map.of("enum", List.of("x"), "format", "uuid")));
        }

        @Test
        @DisplayName("integer bounds intersect in both directions")
        void integerBoundsIntersect() {
            assertEquals(
                    Set.of(100L, 101L, 102L),
                    integers(Map.of("type", "integer", "minimum", 100, "exclusiveMinimum", 0, "maximum", 102)));
            assertEquals(
                    Set.of(101L, 102L),
                    integers(Map.of("type", "integer", "exclusiveMinimum", 100, "minimum", 0, "maximum", 102)));
            assertEquals(
                    Set.of(3L, 4L, 5L),
                    integers(Map.of("type", "integer", "maximum", 5, "exclusiveMaximum", 100, "minimum", 3)));
            assertEquals(
                    Set.of(3L, 4L),
                    integers(Map.of("type", "integer", "exclusiveMaximum", 5, "maximum", 100, "minimum", 3)));
            assertEquals(
                    Set.of(5L, 6L),
                    integers(Map.of("type", "integer", "minimum", 5, "exclusiveMinimum", 4, "maximum", 6)));
        }

        @Test
        @DisplayName("number bounds intersect in both directions")
        void numberBoundsIntersect() {
            for (double value :
                    numbers(Map.of("type", "number", "minimum", 100.0, "exclusiveMinimum", 0.0, "maximum", 150.0))) {
                assertTrue(value >= 100.0 && value <= 150.0, "bounds violated: " + value);
            }
            for (double value :
                    numbers(Map.of("type", "number", "maximum", 5.0, "exclusiveMaximum", 100.0, "minimum", 4.0))) {
                assertTrue(value >= 4.0 && value <= 5.0, "bounds violated: " + value);
            }
            // Bounds one double apart leave one value, so a bound that was ignored would show.
            double above = Math.nextUp(1.0);
            double below = Math.nextDown(1.0);
            assertEquals(
                    Set.of(above),
                    numbers(Map.of("type", "number", "exclusiveMinimum", 1.0, "minimum", 0.0, "maximum", above)));
            assertEquals(
                    Set.of(above),
                    numbers(Map.of("type", "number", "minimum", 1.0, "exclusiveMinimum", 1.0, "maximum", above)));
            assertEquals(
                    Set.of(below),
                    numbers(Map.of("type", "number", "exclusiveMaximum", 1.0, "maximum", 2.0, "minimum", below)));
            assertEquals(
                    Set.of(below),
                    numbers(Map.of("type", "number", "maximum", 1.0, "exclusiveMaximum", 1.0, "minimum", below)));
        }

        @Test
        @DisplayName("rejects overlapping bounds with no values between them, with the path")
        void rejectsEmptyNumericIntersection() {
            for (String type : List.of("integer", "number")) {
                assertRejected(Map.of("type", type, "minimum", 5, "maximum", 1));
                assertRejected(Map.of("type", type, "minimum", 5, "exclusiveMinimum", 5, "maximum", 5));
                assertRejected(Map.of("type", type, "maximum", 5, "exclusiveMaximum", 5, "minimum", 5));
                assertRejected(Map.of("type", type, "exclusiveMinimum", 5, "exclusiveMaximum", 5));
                assertRejected(Map.of("type", type, "minimum", 200, "exclusiveMaximum", 150, "maximum", 300));
            }
        }

        @Test
        @DisplayName("an integer rounds fractional bounds inward")
        void integerRoundsFractionalBoundsInward() {
            assertEquals(Set.of(2L, 3L), integers(Map.of("type", "integer", "minimum", 1.5, "maximum", 3.5)));
            assertEquals(Set.of(-3L, -2L), integers(Map.of("type", "integer", "minimum", -3.5, "maximum", -1.5)));
            assertEquals(
                    Set.of(2L, 3L),
                    integers(Map.of("type", "integer", "exclusiveMinimum", 1.5, "exclusiveMaximum", 3.5)));
            assertEquals(
                    Set.of(-1L, 0L),
                    integers(Map.of("type", "integer", "exclusiveMinimum", -1.5, "exclusiveMaximum", 0.5)));
            assertEquals(
                    Set.of(2L), integers(Map.of("type", "integer", "exclusiveMinimum", 1.0, "exclusiveMaximum", 3.0)));
            assertEquals(Set.of(2L, 3L), integers(Map.of("type", "integer", "minimum", 2.0, "maximum", 3.0)));
            assertRejected(Map.of("type", "integer", "minimum", 1.2, "maximum", 1.8));
        }

        @Test
        @DisplayName("an integer stays within the range a long can hold")
        void integerBeyondLong() {
            BigInteger huge = BigInteger.TWO.pow(70);
            assertRejected(Map.of("type", "integer", "minimum", huge));
            assertRejected(Map.of("type", "integer", "exclusiveMaximum", huge.negate()));
            for (long value : integers(Map.of("type", "integer", "minimum", Long.MAX_VALUE - 1, "maximum", huge))) {
                assertTrue(value >= Long.MAX_VALUE - 1, "minimum violated: " + value);
            }
            assertEquals(
                    Set.of(Long.MAX_VALUE),
                    integers(Map.of("type", "integer", "exclusiveMinimum", Long.MAX_VALUE - 1, "maximum", huge)));
            assertEquals(
                    Set.of(Long.MIN_VALUE),
                    integers(Map.of(
                            "type", "integer", "exclusiveMaximum", Long.MIN_VALUE + 1, "minimum", huge.negate())));
        }

        @Test
        @DisplayName("a boolean exclusive bound is OpenAPI 3.0's flag on the inclusive one")
        void openApiExclusiveFlags() {
            assertEquals(
                    Set.of(1L, 2L),
                    integers(Map.of("type", "integer", "minimum", 0, "exclusiveMinimum", true, "maximum", 2)));
            assertEquals(
                    Set.of(0L, 1L, 2L),
                    integers(Map.of("type", "integer", "minimum", 0, "exclusiveMinimum", false, "maximum", 2)));
            assertEquals(
                    Set.of(0L, 1L),
                    integers(Map.of("type", "integer", "minimum", 0, "exclusiveMaximum", true, "maximum", 2)));
            assertEquals(
                    Set.of(0L, 1L, 2L),
                    integers(Map.of("type", "integer", "minimum", 0, "exclusiveMaximum", false, "maximum", 2)));

            for (double value :
                    numbers(Map.of("type", "number", "minimum", 0, "maximum", 1, "exclusiveMaximum", true))) {
                assertTrue(value >= 0.0 && value < 1.0, "bounds violated: " + value);
            }
            assertEquals(
                    Set.of(1.0),
                    numbers(Map.of("type", "number", "minimum", 1, "maximum", 1, "exclusiveMaximum", false)));
            assertEquals(
                    Set.of(1.0),
                    numbers(Map.of("type", "number", "minimum", 1, "maximum", 1, "exclusiveMinimum", false)));
            assertRejected(Map.of("type", "number", "minimum", 1, "maximum", 1, "exclusiveMaximum", true));
            assertRejected(Map.of("type", "number", "minimum", 1, "maximum", 1, "exclusiveMinimum", true));

            // Without the inclusive keyword the flag has nothing to apply to.
            assertInstanceOf(
                    Number.class,
                    values(Map.of("type", "integer", "exclusiveMinimum", true)).getFirst());
            assertInstanceOf(
                    Number.class,
                    values(Map.of("type", "number", "exclusiveMaximum", true)).getFirst());
        }

        @Test
        @DisplayName("a lone bound beyond the default range keeps its own side of the range")
        void loneBoundBeyondTheDefaultRange() {
            for (Object value : values(Map.of("type", "number", "minimum", 2_000_000))) {
                assertTrue(((Number) value).doubleValue() >= 2_000_000, "minimum violated: " + value);
            }
            for (Object value : values(Map.of("type", "number", "maximum", -2_000_000))) {
                assertTrue(((Number) value).doubleValue() <= -2_000_000, "maximum violated: " + value);
            }
            for (Object value : values(Map.of("type", "integer", "minimum", 5_000_000_000L))) {
                assertTrue(((Number) value).longValue() >= 5_000_000_000L, "minimum violated: " + value);
            }
            for (Object value : values(Map.of("type", "integer", "maximum", -5_000_000_000L))) {
                assertTrue(((Number) value).longValue() <= -5_000_000_000L, "maximum violated: " + value);
            }
        }

        @Test
        @DisplayName("a lone bound inside the default range keeps the default on the other side")
        void loneBoundInsideTheDefaultRange() {
            for (Object value : values(Map.of("type", "number", "minimum", 5))) {
                double number = ((Number) value).doubleValue();
                assertTrue(number >= 5 && number <= 1_000_000, "bounds violated: " + value);
            }
            for (Object value : values(Map.of("type", "number", "maximum", 5))) {
                double number = ((Number) value).doubleValue();
                assertTrue(number <= 5 && number >= -1_000_000, "bounds violated: " + value);
            }
            for (Object value : values(Map.of("type", "integer", "exclusiveMinimum", 5))) {
                long number = ((Number) value).longValue();
                assertTrue(number > 5 && number <= Integer.MAX_VALUE, "bounds violated: " + value);
            }
            for (Object value : values(Map.of("type", "integer", "exclusiveMaximum", 5))) {
                long number = ((Number) value).longValue();
                assertTrue(number < 5 && number >= Integer.MIN_VALUE, "bounds violated: " + value);
            }
        }

        @Test
        @DisplayName("array sizes intersect, and an empty range is rejected while parsing")
        void arraySizes() {
            assertRejected(Map.of("type", "array", "minItems", 5, "maxItems", 1));
            for (Object value : values(Map.of("type", "array", "minItems", 50))) {
                assertTrue(((List<?>) value).size() >= 50, "minItems violated: " + value);
            }
            assertEquals(Set.of(List.of()), distinct(Map.of("type", "array", "maxItems", 0)));
            for (Object value : values(Map.of("type", "array", "maxItems", 2))) {
                assertTrue(((List<?>) value).size() <= 2, "maxItems violated: " + value);
            }
        }

        @Test
        @DisplayName("a conflict names the path of the schema that declares it")
        void conflictNamesItsPath() {
            Map<String, Object> conflict = Map.of("type", "string", "pattern", "^A{8}$", "maxLength", 3);
            Map<String, Object> nested = Map.of("type", "object", "properties", Map.of("code", conflict));
            assertRejectedAt("$.properties.parent.properties.code", Map.of("parent", nested));
            assertRejectedAt("$.properties.list.items", Map.of("list", Map.of("type", "array", "items", conflict)));
            assertRejectedAt(
                    "$.properties.list.items.properties.code",
                    Map.of("list", Map.of("type", "array", "items", nested)));
        }

        @Test
        @DisplayName("values keep to the bounds as parsed when the schema map changes later")
        void boundsAreFixedWhileParsing() {
            Map<String, Object> property =
                    new HashMap<>(Map.of("type", "string", "pattern", "a{1,10}", "maxLength", 2));
            Schema schema = SchemaParser.fromJsonSchema(objectSchema(Map.of("value", property)));
            property.put("maxLength", 10);
            for (Map<String, Object> record : schema.generateBatch(100)) {
                assertTrue(((String) record.get("value")).length() <= 2, "maxLength violated: " + record);
            }
        }

        @Test
        @DisplayName("a number keeps within a bound that a double cannot hold")
        void numberBoundBeyondDoublePrecision() {
            long bound = 9_007_199_254_740_993L; // 2^53 + 1, which rounds down to 2^53 as a double
            assertRejected(Map.of("type", "number", "minimum", bound, "maximum", bound));
            for (double value : numbers(Map.of("type", "number", "minimum", bound, "maximum", bound + 3))) {
                assertTrue(
                        new BigDecimal(value).compareTo(BigDecimal.valueOf(bound)) >= 0, "minimum violated: " + value);
            }
            for (double value : numbers(Map.of("type", "number", "maximum", bound, "minimum", bound - 3))) {
                assertTrue(
                        new BigDecimal(value).compareTo(BigDecimal.valueOf(bound)) <= 0, "maximum violated: " + value);
            }
            // 2^53 + 3 rounds up to 2^53 + 4.
            long roundsUp = bound + 2;
            for (double value : numbers(Map.of("type", "number", "maximum", roundsUp, "minimum", roundsUp - 3))) {
                assertTrue(
                        new BigDecimal(value).compareTo(BigDecimal.valueOf(roundsUp)) <= 0,
                        "maximum violated: " + value);
            }
            for (double value :
                    numbers(Map.of("type", "number", "exclusiveMaximum", roundsUp, "minimum", roundsUp - 3))) {
                assertTrue(
                        new BigDecimal(value).compareTo(BigDecimal.valueOf(roundsUp)) < 0, "bound violated: " + value);
            }
            for (double value : numbers(Map.of("type", "number", "exclusiveMinimum", bound, "maximum", bound + 3))) {
                assertTrue(new BigDecimal(value).compareTo(BigDecimal.valueOf(bound)) > 0, "bound violated: " + value);
            }
            // A float bound means the decimal it writes, not the wider binary value it holds.
            assertEquals(Set.of(0.1), numbers(Map.of("type", "number", "minimum", 0.1f, "maximum", 0.1f)));
        }

        @Test
        @DisplayName("a number range can reach the limits of a double, and past them")
        void numberRangeAtTheLimitsOfADouble() {
            double max = Double.MAX_VALUE;
            for (double value : numbers(Map.of("type", "number", "minimum", -max, "maximum", max))) {
                assertTrue(Double.isFinite(value), "not a number JSON can write: " + value);
            }
            for (double value : numbers(Map.of("type", "number", "maximum", max))) {
                assertTrue(Double.isFinite(value) && value >= -1_000_000, "bounds violated: " + value);
            }
            for (double value : numbers(Map.of("type", "number", "minimum", 1e308, "maximum", max))) {
                assertTrue(value >= 1e308 && value <= max, "bounds violated: " + value);
            }
            assertEquals(Set.of(max), numbers(Map.of("type", "number", "minimum", max, "maximum", max)));

            // No double reaches these bounds, so the far side is unconstrained and the near side empty.
            BigDecimal beyond = new BigDecimal("1e400");
            assertRejected(Map.of("type", "number", "minimum", beyond));
            assertRejected(Map.of("type", "number", "exclusiveMinimum", beyond));
            assertRejected(Map.of("type", "number", "maximum", beyond.negate()));
            assertRejected(Map.of("type", "number", "exclusiveMaximum", beyond.negate()));
            for (double value : numbers(Map.of("type", "number", "maximum", beyond))) {
                assertTrue(value >= -1_000_000 && value <= 1_000_000, "default range lost: " + value);
            }
            for (double value : numbers(Map.of("type", "number", "minimum", beyond.negate()))) {
                assertTrue(value >= -1_000_000 && value <= 1_000_000, "default range lost: " + value);
            }
            for (double value :
                    numbers(Map.of("type", "number", "minimum", 0, "maximum", beyond, "exclusiveMaximum", 1))) {
                assertTrue(value >= 0 && value < 1, "bounds violated: " + value);
            }
        }

        @Test
        @DisplayName("a value is judged as it is written")
        void valuesJudgedAsWritten() {
            Optional<String> a = Optional.of("a");
            assertEquals(Set.of(a), distinct(Map.of("type", "string", "const", a, "maxLength", 1)));
            assertRejected(Map.of("type", "string", "const", Optional.of("abc"), "maxLength", 1));
            assertRejected(Map.of("type", "string", "const", Optional.empty()));
            assertEquals(Set.of(Optional.empty()), distinct(Map.of("type", "null", "const", Optional.empty())));
            assertEquals(Set.of(a), distinct(Map.of("const", a, "enum", List.of("a"))));
            assertEquals(Set.of("a"), distinct(Map.of("const", "a", "enum", List.of(a))));

            assertRejected(Map.of("const", Set.of(1, 2), "maxItems", 1));
            assertRejected(Map.of("const", new int[] {1, 2}, "items", Map.of("type", "string")));
            assertInstanceOf(
                    int[].class,
                    values(Map.of("type", "array", "const", new int[] {1, 2}, "items", Map.of("type", "integer")))
                            .getFirst());

            record Point(int x) {}
            Point point = new Point(1);
            assertRejected(Map.of("const", point, "properties", Map.of("x", Map.of("type", "string"))));
            assertEquals(
                    Set.of(point),
                    distinct(Map.of(
                            "type", "object", "const", point, "properties", Map.of("x", Map.of("type", "integer")))));
        }

        @Test
        @DisplayName("nullable adds null only where a const or enum leaves room for it")
        void nullableRespectsConstAndEnum() {
            Map<String, Object> enumWithoutNull = Map.of("type", "string", "nullable", true, "enum", List.of("a"));
            assertEquals(Set.of("a"), distinct(enumWithoutNull));
            Map<String, Object> constant = Map.of("type", "string", "nullable", true, "const", "a");
            assertEquals(Set.of("a"), distinct(constant));
            // The plain case still draws null, so the checks above can fail.
            assertEquals(setOf("a", null), distinct(withNull("nullable", true, "enum", Arrays.asList("a", null))));
            Set<Object> nullable = distinct(Map.of("type", "string", "nullable", true, "minLength", 2, "maxLength", 2));
            assertTrue(nullable.contains(null), "expected a null among " + nullable);
        }

        @Test
        @DisplayName("a semantic value of another type becomes a string only when the type says so")
        void semanticValueMeetsDeclaredType() {
            for (String name : List.of("datetimeTimestamp", "datetime_timestamp")) {
                Map<String, Object> declared = objectSchema(Map.of(name, Map.of("type", "string")));
                Object value = SchemaParser.fromJsonSchema(declared).generate().get(name);
                assertInstanceOf(String.class, value);
                assertTrue(((String) value).matches("\\d+"), "expected the timestamp's digits, got: " + value);
            }
            Map<String, Object> untyped = objectSchema(Map.of("datetimeTimestamp", Map.of()));
            assertInstanceOf(
                    Number.class,
                    SchemaParser.fromJsonSchema(untyped).generate().get("datetimeTimestamp"));

            // A value that is already a string keeps the provider's own metadata for export.
            Map<String, Object> email = objectSchema(Map.of("email", Map.of("type", "string")));
            Map<?, ?> exported = (Map<?, ?>)
                    SchemaParser.fromJsonSchema(email).toJsonSchema().get("properties");
            assertEquals("email", ((Map<?, ?>) exported.get("email")).get("format"));
        }

        @Test
        @DisplayName("a value written as a string is constrained by that text")
        void valuesWrittenAsStrings() {
            LocalDate date = LocalDate.of(2020, 1, 2);
            assertRejected(Map.of("type", "string", "const", date, "maxLength", 9));
            assertRejected(Map.of("type", "string", "const", date, "minLength", 11));
            assertRejected(Map.of("const", date, "pattern", "^1999"));
            assertEquals(Set.of(date), distinct(Map.of("type", "string", "const", date, "maxLength", 10)));
            assertEquals(Set.of(date), distinct(Map.of("const", date, "pattern", "^2020")));
            assertRejected(Map.of("type", "string", "enum", List.of(Thread.State.NEW), "maxLength", 1));
            assertEquals(
                    Set.of(Thread.State.NEW),
                    distinct(Map.of("enum", List.of(Thread.State.NEW, Thread.State.BLOCKED), "maxLength", 3)));
            assertRejected(Map.of("const", 'a', "maxLength", 0));
            assertEquals(Set.of('a'), distinct(Map.of("type", "string", "const", 'a', "maxLength", 1)));
        }

        @Test
        @DisplayName("a combination that rarely fits can fail on a later record, naming its path")
        void rareFitCanFailLater() {
            // Only the empty string fits, one value in 6,561. The search while parsing happens to find it, and
            // a later record runs out of candidates.
            Map<String, Object> property = Map.of("type", "string", "pattern", "a{0,80}a{0,80}", "maxLength", 0);
            Schema schema = SchemaParser.fromJsonSchema(
                    objectSchema(Map.of("value", property)),
                    GeneratorConfig.builder().seed(7L).build());
            SchemaGenerationException exception =
                    assertThrows(SchemaGenerationException.class, () -> schema.generateBatch(100));
            assertInstanceOf(IllegalArgumentException.class, exception.getCause());
            assertTrue(exception.getCause().getMessage().contains("$.properties.value"), exception.getMessage());
        }

        @Test
        @DisplayName("seeded values replay under combined constraints")
        void combinedConstraintsReplay() {
            List<Map<String, Object>> properties = List.of(
                    Map.of("type", "string", "pattern", "[a-z]{1,10}", "minLength", 6, "maxLength", 7),
                    Map.of("type", "string", "format", "email", "maxLength", 20),
                    Map.of("enum", List.of(1, 5, 10), "minimum", 5),
                    Map.of("type", "number", "minimum", 100, "exclusiveMinimum", 0, "maximum", 150),
                    Map.of("type", "integer", "minimum", 100, "exclusiveMinimum", 0, "maximum", 150));
            for (Map<String, Object> property : properties) {
                assertEquals(values(property), values(property));
            }
        }
    }

    @Test
    @DisplayName("seeded format providers reproduce their generated record")
    void seededFormatProvidersReproduce() {
        Map<String, Object> properties = new LinkedHashMap<>();
        for (String format :
                List.of("email", "idn-email", "uri", "uuid", "date", "date-time", "time", "ipv4", "ipv6", "hostname")) {
            properties.put(format, Map.of("type", "string", "format", format));
        }
        GeneratorConfig config = GeneratorConfig.builder().seed(42L).build();

        Map<String, Object> first =
                SchemaParser.fromJsonSchema(objectSchema(properties), config).generate();
        Map<String, Object> second =
                SchemaParser.fromJsonSchema(objectSchema(properties), config).generate();

        assertEquals(first, second);
    }
}
