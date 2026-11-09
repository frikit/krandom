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
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.frikit.krandom.generator.GeneratorConfig;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.IDN;
import java.net.URI;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
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
                    "(?i)^[a-z]{3}-\\d$",
                    "\\p{Lu}\\p{Ll}{2,5}",
                    "\\Qa.b\\E\\p{Nd}",
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
        @DisplayName("a pattern too large once minLength raises its repetition is rejected with its path")
        void rejectsPatternTooLargeForItsMinimumLength() {
            // (a+)+ is at most 100 characters as a+ repeats by default, and 10^8 when minLength 5000 raises
            // that to 10 000 times.
            for (Object value : values(Map.of("type", "string", "pattern", "(a+)+", "minLength", 1))) {
                assertTrue(((String) value).matches("a{1,100}"), String.valueOf(value));
            }
            Map<String, Object> schema =
                    objectSchema(Map.of("value", Map.of("type", "string", "pattern", "(a+)+", "minLength", 5_000)));
            IllegalArgumentException exception =
                    assertThrows(IllegalArgumentException.class, () -> SchemaParser.fromJsonSchema(schema));
            assertEquals(
                    "JSON Schema pattern cannot generate values at $.properties.value: Generated values could be longer"
                            + " than the limit of 1000000 characters, with *, + and {n,} repeating up to 10000 times,"
                            + " in pattern: (a+)+",
                    exception.getMessage());
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
                    Map.of("type", "integer", "minimum", 100, "exclusiveMinimum", 0, "maximum", 150),
                    Map.of("type", "number", "multipleOf", 0.01, "minimum", 0, "maximum", 10),
                    Map.of(
                            "type",
                            "array",
                            "uniqueItems",
                            true,
                            "minItems",
                            3,
                            "maxItems",
                            5,
                            "items",
                            Map.of("type", "integer", "minimum", 1, "maximum", 10)));
            for (Map<String, Object> property : properties) {
                assertEquals(values(property), values(property));
            }
        }
    }

    @Nested
    @DisplayName("JSON value equality")
    class JsonValueEquality {

        /** A const beside an enum holding one member is generated only if the two are the same JSON value. */
        private static void assertSameJson(Object constant, Object member) {
            assertEquals(
                    Set.of(constant),
                    CombinedConstraints.distinct(Map.of("const", constant, "enum", List.of(member))),
                    () -> constant + " and " + member);
        }

        private static void assertDifferentJson(Object constant, Object member) {
            CombinedConstraints.assertRejected(Map.of("const", constant, "enum", List.of(member)));
        }

        private static Map<String, Object> linked(String key, Object value, String otherKey, Object other) {
            Map<String, Object> object = new LinkedHashMap<>();
            object.put(key, value);
            object.put(otherKey, other);
            return object;
        }

        @Test
        @DisplayName("an object const matches an enum member that writes its number differently, as in 2.9.0")
        void objectConstMatchesEnumMemberWithNumberWrittenDifferently() {
            assertSameJson(Map.of("id", 1), Map.of("id", 1.0));
            assertSameJson(Map.of("id", 1.0), Map.of("id", 1));
        }

        @Test
        @DisplayName("numbers compare by value at every depth of arrays and objects")
        void numbersCompareByValueAtEveryDepth() {
            assertSameJson(
                    Map.of("a", List.of(1, Map.of("b", List.of(2.0, 3)), "c"), "d", Map.of("e", 4L)),
                    Map.of(
                            "a",
                            List.of(1.0, Map.of("b", List.of(2, new BigDecimal("3.00"))), "c"),
                            "d",
                            Map.of("e", new BigInteger("4"))));
            assertSameJson(List.of(List.of(1), List.of(List.of(2))), List.of(List.of(1.0), List.of(List.of(2.0f))));
            // An array is written as a list.
            assertSameJson(Map.of("ids", new long[] {1L, 2L}), Map.of("ids", List.of(1.0, 2.0)));
            // So is the content of a nested schema's const or enum.
            List<Object> arrays = List.of(List.of(Map.of("id", 1.0)));
            assertEquals(
                    Set.copyOf(arrays),
                    CombinedConstraints.distinct(Map.of("enum", arrays, "items", Map.of("const", Map.of("id", 1)))));
            List<Object> objects = List.of(Map.of("o", Map.of("id", 1.0)));
            assertEquals(
                    Set.copyOf(objects),
                    CombinedConstraints.distinct(Map.of(
                            "enum", objects, "properties", Map.of("o", Map.of("enum", List.of(Map.of("id", 1)))))));
        }

        @Test
        @DisplayName("large integers compare exactly, so a double near one is another number")
        void largeIntegersCompareExactly() {
            assertSameJson(Map.of("n", Long.MAX_VALUE), Map.of("n", BigInteger.valueOf(Long.MAX_VALUE)));
            assertSameJson(
                    List.of(new BigInteger("123456789012345678901234567890")),
                    List.of(new BigDecimal("1.23456789012345678901234567890E+29")));
            // 2^53 + 1 has no double: the nearest, 2^53, is written as another number.
            assertDifferentJson(Map.of("n", 9_007_199_254_740_993L), Map.of("n", 9_007_199_254_740_992.0));
            assertDifferentJson(List.of(Long.MAX_VALUE), List.of((double) Long.MAX_VALUE));
        }

        @Test
        @DisplayName("decimals, negative zero and exponent forms compare by value")
        void decimalsNegativeZeroAndExponentsCompareByValue() {
            assertSameJson(Map.of("p", 0.1), Map.of("p", new BigDecimal("0.10")));
            assertDifferentJson(Map.of("p", 0.1 + 0.2), Map.of("p", 0.3));
            assertSameJson(Map.of("z", -0.0), Map.of("z", 0));
            assertSameJson(List.of(-0.0f), List.of(new BigDecimal("0E-10")));
            assertSameJson(Map.of("e", 1e2), Map.of("e", 100));
            assertSameJson(Map.of("e", new BigDecimal("1E+2")), Map.of("e", 100L));
            assertSameJson(List.of(new BigDecimal("1.5E-3")), List.of(0.0015));
            assertSameJson(List.of(1.0E300), List.of(new BigDecimal("1E300")));
            // A float is compared as it is written, and 0.1f is written 0.1.
            assertSameJson(List.of(0.1f), List.of(0.1));
        }

        @Test
        @DisplayName("arrays compare in order, and objects member by member in any order")
        void arraysCompareInOrderAndObjectsMemberByMember() {
            assertDifferentJson(List.of(1, 2), List.of(2, 1));
            assertDifferentJson(List.of(1), List.of(1, 1));
            assertDifferentJson(List.of(1, 1), List.of(1));
            assertDifferentJson(Map.of("id", 1), Map.of("id", 1, "x", 2));
            assertDifferentJson(Map.of("id", 1, "x", 2), Map.of("id", 1));
            assertDifferentJson(Map.of("a", 1), Map.of("b", 1));
            assertDifferentJson(Collections.singletonMap("a", null), Collections.singletonMap("b", null));
            assertSameJson(linked("a", 1, "b", 2), linked("b", 2.0, "a", 1));
        }

        @Test
        @DisplayName("an object member is known by the name it is written under")
        void objectMemberIsKnownByItsWrittenName() {
            assertSameJson(Map.of(1, "x"), Map.of("1", "x"));
            assertSameJson(Map.of("1", "x"), Map.of(1, "x"));
            assertSameJson(Map.of('k', 2), Map.of("k", 2.0));
            assertDifferentJson(Map.of(1, "x"), Map.of("2", "x"));
            // A property's schema applies to the member written under its name.
            assertEquals(
                    Set.of(Map.of(1, 5)),
                    CombinedConstraints.distinct(
                            Map.of("const", Map.of(1, 5), "properties", Map.of("1", Map.of("type", "integer")))));
            CombinedConstraints.assertRejected(
                    Map.of("const", Map.of(1, "x"), "properties", Map.of("1", Map.of("type", "integer"))));
        }

        @Test
        @DisplayName("rejects an object with two members written under one name, with the path")
        void rejectsTwoMembersWrittenUnderOneName() {
            Map<Object, Object> hiddenNaN = new LinkedHashMap<>();
            hiddenNaN.put(1, Double.NaN);
            hiddenNaN.put("1", 0);
            CombinedConstraints.assertRejected(Map.of("const", hiddenNaN));
            Map<Object, Object> twice = new LinkedHashMap<>();
            twice.put('a', 1);
            twice.put("a", 1);
            CombinedConstraints.assertRejected(Map.of("const", twice));
            CombinedConstraints.assertRejected(Map.of("enum", List.of(Map.of("a", 1), twice)));
            CombinedConstraints.assertRejected(Map.of("enum", List.of(List.of(twice))));
        }

        @Test
        @DisplayName("values of different JSON types differ")
        void valuesOfDifferentJsonTypesDiffer() {
            assertDifferentJson(Map.of("id", "1"), Map.of("id", 1));
            assertDifferentJson(Map.of("id", 1), Map.of("id", "1"));
            assertDifferentJson(Collections.singletonMap("id", null), Map.of("id", 0));
            assertDifferentJson(Map.of("id", 0), Collections.singletonMap("id", null));
            assertDifferentJson(Map.of("id", true), Map.of("id", 1));
            assertDifferentJson(Map.of("id", 1), Map.of("id", true));
            assertDifferentJson(Map.of("id", true), Map.of("id", "true"));
            assertDifferentJson(Map.of("id", "true"), Map.of("id", true));
            assertDifferentJson(Map.of("id", true), Map.of("id", false));
            assertDifferentJson(List.of(List.of()), List.of(Map.of()));
            assertDifferentJson(List.of(Map.of()), List.of(List.of()));
            // Text that reads like an array or object is still a string.
            assertDifferentJson(List.of("[a]"), List.of(List.of("a")));
            assertDifferentJson(List.of(List.of("a")), List.of("[a]"));
            assertDifferentJson(List.of("{a=1}"), List.of(Map.of("a", 1)));
            assertDifferentJson(List.of(Map.of("a", 1)), List.of("{a=1}"));
            assertSameJson(Map.of("ok", true), Map.of("ok", true));
            assertSameJson(Collections.singletonMap("v", null), Collections.singletonMap("v", null));
        }

        @Test
        @DisplayName("a value written as a string matches its text")
        void valueWrittenAsStringMatchesItsText() {
            assertSameJson(Map.of("c", 'x'), Map.of("c", "x"));
            assertSameJson(List.of(new StringBuilder("ab")), List.of("ab"));
            LocalDate date = LocalDate.of(2020, 1, 2);
            assertSameJson(Map.of("on", date), Map.of("on", "2020-01-02"));
            assertSameJson("2020-01-02", date);
            assertDifferentJson(Map.of("on", date), Map.of("on", "2020-01-03"));
        }

        @Test
        @DisplayName("rejects a const or enum with a non-finite number at any depth, with the path")
        void rejectsNestedNonFiniteNumbers() {
            CombinedConstraints.assertRejected(Map.of("const", List.of(Double.NaN)));
            CombinedConstraints.assertRejected(Map.of("const", Map.of("v", Double.POSITIVE_INFINITY)));
            CombinedConstraints.assertRejected(Map.of("enum", List.of(Map.of("v", Float.NaN), Map.of("v", 1))));
            // Every enum member is read, in any order and whatever the declared type leaves out.
            CombinedConstraints.assertRejected(Map.of("const", "x", "enum", List.of(Double.NaN, "x")));
            CombinedConstraints.assertRejected(Map.of("const", "x", "enum", List.of("x", Double.NaN)));
            CombinedConstraints.assertRejected(Map.of("type", "string", "enum", List.of("a", Double.NaN)));
            // nullable is judged only after the const or enum, so the failure keeps its path.
            CombinedConstraints.assertRejected(Map.of("nullable", true, "const", Double.NaN));
            CombinedConstraints.assertRejected(Map.of("nullable", true, "enum", List.of(1, Double.NaN)));
        }
    }

    @Nested
    @DisplayName("multipleOf")
    class MultipleOf {

        private static Set<BigDecimal> decimals(String... values) {
            Set<BigDecimal> decimals = new HashSet<>();
            for (String value : values) {
                decimals.add(new BigDecimal(value));
            }
            return decimals;
        }

        private static void assertMultiplesWithin(Map<String, Object> property, String low, String high) {
            for (Object value : CombinedConstraints.values(property)) {
                BigDecimal number = assertInstanceOf(BigDecimal.class, value);
                assertEquals(0, number.remainder(new BigDecimal("0.5")).signum(), number::toString);
                assertTrue(
                        number.compareTo(new BigDecimal(low)) >= 0 && number.compareTo(new BigDecimal(high)) <= 0,
                        number::toString);
            }
        }

        @Test
        @DisplayName("an integer is a multiple within its bounds")
        void integerIsMultipleWithinBounds() {
            assertEquals(
                    Set.of(5, 10, 15, 20, 25, 30),
                    CombinedConstraints.distinct(
                            Map.of("type", "integer", "multipleOf", 5, "minimum", 1, "maximum", 30)));
            // The whole multiples of 2.5 are the multiples of 5.
            assertEquals(
                    Set.of(0, 5, 10, 15, 20),
                    CombinedConstraints.distinct(
                            Map.of("type", "integer", "multipleOf", 2.5, "minimum", 0, "maximum", 20)));
            assertEquals(
                    Set.of(6, 9),
                    CombinedConstraints.distinct(
                            Map.of("type", "integer", "multipleOf", 3, "exclusiveMinimum", 3, "exclusiveMaximum", 12)));
            assertEquals(
                    Set.of(6, 9),
                    CombinedConstraints.distinct(Map.of(
                            "type",
                            "integer",
                            "multipleOf",
                            3,
                            "minimum",
                            3,
                            "exclusiveMinimum",
                            true,
                            "maximum",
                            12,
                            "exclusiveMaximum",
                            true)));
        }

        @Test
        @DisplayName("an integer without bounds keeps to the int range, and beyond it to a long")
        void integerMultipleKeepsToTheIntRangeOrALong() {
            for (Object value : CombinedConstraints.values(Map.of("type", "integer", "multipleOf", 7))) {
                assertEquals(0, assertInstanceOf(Integer.class, value) % 7);
            }
            // The multiples of 1 reach both ends of the int range, and are still Integer.
            for (Object value : CombinedConstraints.values(Map.of("type", "integer", "multipleOf", 1))) {
                assertInstanceOf(Integer.class, value);
            }
            for (Object value :
                    CombinedConstraints.values(Map.of("type", "integer", "multipleOf", 7, "minimum", 3_000_000_000L))) {
                long number = assertInstanceOf(Long.class, value);
                assertTrue(number >= 3_000_000_000L && number % 7 == 0, String.valueOf(number));
            }
            for (Object value : CombinedConstraints.values(
                    Map.of("type", "integer", "multipleOf", 7, "maximum", -3_000_000_000L))) {
                long number = assertInstanceOf(Long.class, value);
                assertTrue(number <= -3_000_000_000L && number % 7 == 0, String.valueOf(number));
            }
        }

        @Test
        @DisplayName("an open side reaches the nearest multiple where its default range holds none")
        void openSideReachesTheNearestMultiple() {
            assertEquals(
                    Set.of(3_000_000_000L),
                    CombinedConstraints.distinct(
                            Map.of("type", "integer", "multipleOf", 3_000_000_000L, "minimum", 1)));
            assertEquals(
                    Set.of(-3_000_000_000L),
                    CombinedConstraints.distinct(
                            Map.of("type", "integer", "multipleOf", 3_000_000_000L, "maximum", -1)));
            assertEquals(
                    decimals("1000000000"),
                    CombinedConstraints.distinct(Map.of("type", "number", "multipleOf", 1e9, "minimum", 0.1)));
            assertEquals(
                    decimals("-1000000000"),
                    CombinedConstraints.distinct(Map.of("type", "number", "multipleOf", 1e9, "maximum", -0.1)));
            // The default comes from the declared bound, as for any number: 0.1 keeps the default maximum,
            // so only the nearest multiple of 1,500,000 is in reach.
            assertEquals(
                    decimals("1500000"),
                    CombinedConstraints.distinct(Map.of("type", "number", "multipleOf", 1_500_000, "minimum", 0.1)));
            // An integer stays within a long, which holds no multiple of 10^19 above 0.
            CombinedConstraints.assertRejected(
                    Map.of("type", "integer", "multipleOf", new BigDecimal("1E19"), "minimum", 1));
            CombinedConstraints.assertRejected(
                    Map.of("type", "integer", "multipleOf", new BigDecimal("1E19"), "maximum", -1));
        }

        @Test
        @DisplayName("a number is an exact decimal multiple within its bounds")
        void numberIsExactDecimalMultipleWithinBounds() {
            assertEquals(
                    decimals("0.25", "0.5", "0.75", "1"),
                    CombinedConstraints.distinct(
                            Map.of("type", "number", "multipleOf", 0.25, "minimum", 0.1, "maximum", 1.0)));
            assertEquals(
                    decimals("1.5", "2", "2.5"),
                    CombinedConstraints.distinct(
                            Map.of("type", "number", "multipleOf", 0.5, "exclusiveMinimum", 1, "exclusiveMaximum", 3)));
            assertEquals(
                    decimals("2"),
                    CombinedConstraints.distinct(Map.of(
                            "type",
                            "number",
                            "multipleOf",
                            1,
                            "minimum",
                            1,
                            "exclusiveMinimum",
                            true,
                            "maximum",
                            3,
                            "exclusiveMaximum",
                            true)));
            // A step of 100 is written without an exponent.
            assertEquals(
                    decimals("100", "200", "300"),
                    CombinedConstraints.distinct(
                            Map.of("type", "number", "multipleOf", 100, "minimum", 50, "maximum", 300)));
        }

        @Test
        @DisplayName("a step of 0.01 gives exact hundredths, with no binary drift")
        void hundredthsHaveNoBinaryDrift() {
            for (Object value : CombinedConstraints.values(
                    Map.of("type", "number", "multipleOf", 0.01, "minimum", 0, "maximum", 1000))) {
                BigDecimal number = assertInstanceOf(BigDecimal.class, value);
                assertEquals(0, number.remainder(new BigDecimal("0.01")).signum(), number::toString);
                assertTrue(number.scale() <= 2, number::toString);
            }
        }

        @Test
        @DisplayName("a number without a bound keeps to the default range, or within a lone bound beyond it")
        void numberMultipleKeepsToTheDefaultRange() {
            assertMultiplesWithin(Map.of("type", "number", "multipleOf", 0.5), "-1000000", "1000000");
            assertMultiplesWithin(Map.of("type", "number", "multipleOf", 0.5, "minimum", 10), "10", "1000000");
            assertMultiplesWithin(Map.of("type", "number", "multipleOf", 0.5, "maximum", -10), "-1000000", "-10");
            assertMultiplesWithin(
                    Map.of("type", "number", "multipleOf", 0.5, "minimum", 2_000_000), "2000000", "4000000");
            assertMultiplesWithin(
                    Map.of("type", "number", "multipleOf", 0.5, "maximum", -2_000_000), "-4000000", "-2000000");
            // A lone bound at the default limit is beyond it too, so the other side moves past it.
            Map<String, Object> atUpperLimit = Map.of("type", "number", "multipleOf", 0.5, "minimum", 1_000_000);
            assertMultiplesWithin(atUpperLimit, "1000000", "3000000");
            assertTrue(CombinedConstraints.values(atUpperLimit).stream()
                    .anyMatch(value -> ((BigDecimal) value).compareTo(new BigDecimal("1000000")) > 0));
            Map<String, Object> atLowerLimit = Map.of("type", "number", "multipleOf", 0.5, "maximum", -1_000_000);
            assertMultiplesWithin(atLowerLimit, "-3000000", "-1000000");
            assertTrue(CombinedConstraints.values(atLowerLimit).stream()
                    .anyMatch(value -> ((BigDecimal) value).compareTo(new BigDecimal("-1000000")) < 0));
        }

        @Test
        @DisplayName("rejects bounds that hold no multiple, and a step that is not a number above 0, with the path")
        void rejectsBoundsWithoutMultipleAndInvalidSteps() {
            CombinedConstraints.assertRejected(Map.of("type", "integer", "multipleOf", 5, "minimum", 1, "maximum", 4));
            CombinedConstraints.assertRejected(Map.of("type", "number", "multipleOf", 5, "minimum", 1, "maximum", 4));
            CombinedConstraints.assertRejected(
                    Map.of("type", "number", "multipleOf", 0.5, "exclusiveMinimum", 1, "exclusiveMaximum", 1.5));
            for (Object step : List.of(0, -2, 0.0, Double.NaN, Double.POSITIVE_INFINITY, "5")) {
                CombinedConstraints.assertRejected(Map.of("type", "integer", "multipleOf", step));
            }
            // The keyword is checked wherever it appears.
            CombinedConstraints.assertRejected(Map.of("type", "string", "multipleOf", 0));
        }

        @Test
        @DisplayName("a const or enum keeps only multiples, at any depth")
        void constAndEnumKeepOnlyMultiples() {
            assertEquals(
                    Set.of(5, 10), CombinedConstraints.distinct(Map.of("enum", List.of(5, 7, 10), "multipleOf", 5)));
            CombinedConstraints.assertRejected(Map.of("const", 7, "multipleOf", 5));
            // 0.3 is a multiple of 0.1 as written, though not in binary.
            assertEquals(Set.of(0.3), CombinedConstraints.distinct(Map.of("const", 0.3, "multipleOf", 0.1)));
            assertEquals(
                    Set.of(List.of(2, 4)),
                    CombinedConstraints.distinct(Map.of("const", List.of(2, 4), "items", Map.of("multipleOf", 2))));
            CombinedConstraints.assertRejected(Map.of("const", List.of(2, 3), "items", Map.of("multipleOf", 2)));
            // multipleOf constrains only numbers.
            assertEquals(Set.of("abc"), CombinedConstraints.distinct(Map.of("const", "abc", "multipleOf", 2)));
        }
    }

    @Nested
    @DisplayName("uniqueItems")
    class UniqueItems {

        private static Map<String, Object> uniqueArray(int minItems, int maxItems, Map<String, Object> items) {
            return Map.of(
                    "type", "array", "uniqueItems", true, "minItems", minItems, "maxItems", maxItems, "items", items);
        }

        @Test
        @DisplayName("items are distinct as JSON values")
        void itemsAreDistinctAsJsonValues() {
            // 1 and 1.0 are one JSON value, so every pair holds 2 and one of them.
            for (Object value : CombinedConstraints.values(uniqueArray(2, 2, Map.of("enum", List.of(1, 1.0, 2))))) {
                List<?> items = assertInstanceOf(List.class, value);
                assertEquals(2, items.size(), items::toString);
                assertTrue(items.contains(2), items::toString);
            }
            // Without uniqueItems, items repeat.
            Map<String, Object> repeating = new HashMap<>(uniqueArray(2, 2, Map.of("enum", List.of(1))));
            repeating.put("uniqueItems", false);
            assertEquals(Set.of(List.of(1, 1)), CombinedConstraints.distinct(repeating));
            repeating.remove("uniqueItems");
            assertEquals(Set.of(List.of(1, 1)), CombinedConstraints.distinct(repeating));
        }

        @Test
        @DisplayName("items that have just enough distinct values fill the array")
        void itemsWithJustEnoughDistinctValuesFillTheArray() {
            Map<String, Object> property = uniqueArray(50, 50, Map.of("type", "integer", "minimum", 1, "maximum", 50));
            for (long seed : List.of(1L, 2L, 3L)) {
                for (Object value : CombinedConstraints.values(
                        property, GeneratorConfig.builder().seed(seed).build())) {
                    assertEquals(50, new HashSet<>(assertInstanceOf(List.class, value)).size());
                }
            }
        }

        @Test
        @DisplayName("nested arrays of items with few distinct values stay cheap at every level")
        void nestedArraysOfFewDistinctValuesStayCheap() {
            Map<String, Object> items = Map.of("const", 1);
            for (int depth = 0; depth < 8; depth++) {
                items = uniqueArray(1, 2, items);
            }
            Map<String, Object> nested = items;
            List<Object> values =
                    assertTimeoutPreemptively(Duration.ofSeconds(20), () -> CombinedConstraints.values(nested));
            Object only = 1;
            for (int depth = 0; depth < 8; depth++) {
                only = List.of(only);
            }
            assertEquals(Set.of(only), new HashSet<>(values));
        }

        @Test
        @DisplayName("items with fewer distinct values than maxItems give arrays of at most that many")
        void itemsWithFewerDistinctValuesThanMaxItemsGiveSmallerArrays() {
            Set<Integer> sizes = new HashSet<>();
            for (Object value : CombinedConstraints.values(uniqueArray(1, 5, Map.of("enum", List.of(1, 2))))) {
                List<?> items = assertInstanceOf(List.class, value);
                assertEquals(items.size(), new HashSet<>(items).size(), items::toString);
                sizes.add(items.size());
            }
            assertEquals(Set.of(1, 2), sizes);
        }

        @Test
        @DisplayName("rejects items with too few distinct values for minItems, with the path")
        void rejectsItemsWithTooFewDistinctValues() {
            CombinedConstraints.assertRejected(uniqueArray(3, 3, Map.of("enum", List.of("a", "b"))));
            CombinedConstraints.assertRejected(uniqueArray(2, 4, Map.of("enum", List.of(1, 1.0))));
            CombinedConstraints.assertRejected(uniqueArray(2, 2, Map.of("type", "boolean", "const", true)));
            CombinedConstraints.assertRejected(uniqueArray(3, 3, Map.of("type", "boolean")));
            // Two items from two values make only two arrays, one for each order.
            CombinedConstraints.assertRejected(uniqueArray(3, 3, uniqueArray(2, 2, Map.of("enum", List.of(1, 2)))));
            // A nested array is rejected at its own path.
            CombinedConstraints.assertRejectedAt(
                    "$.properties.value.items",
                    Map.of("value", Map.of("type", "array", "items", uniqueArray(2, 2, Map.of("const", 1)))));
            // uniqueItems must be a boolean.
            CombinedConstraints.assertRejected(Map.of("type", "array", "uniqueItems", "yes"));
        }

        @Test
        @DisplayName("a const or enum array must hold distinct items")
        void constAndEnumArraysMustHoldDistinctItems() {
            CombinedConstraints.assertRejected(Map.of("const", List.of(1, 1.0), "uniqueItems", true));
            assertEquals(
                    Set.of(List.of(1, 2)),
                    CombinedConstraints.distinct(
                            Map.of("enum", List.of(List.of(1, 2), List.of(2, 2.0)), "uniqueItems", true)));
            assertEquals(
                    Set.of(List.of(1, 1)),
                    CombinedConstraints.distinct(Map.of("const", List.of(1, 1), "uniqueItems", false)));
            // uniqueItems constrains only arrays.
            assertEquals(Set.of("aa"), CombinedConstraints.distinct(Map.of("const", "aa", "uniqueItems", true)));
        }
    }

    @Nested
    @DisplayName("a list of items and additionalItems")
    class TupleItems {

        private static final List<Object> ONE_TWO = List.of(Map.of("const", 1), Map.of("const", 2));

        private static Map<String, Object> array(Object... keywordsAndValues) {
            Map<String, Object> property = new LinkedHashMap<>();
            property.put("type", "array");
            for (int i = 0; i < keywordsAndValues.length; i += 2) {
                property.put((String) keywordsAndValues[i], keywordsAndValues[i + 1]);
            }
            return property;
        }

        private static Set<Integer> sizes(Map<String, Object> property) {
            Set<Integer> sizes = new HashSet<>();
            for (Object value : CombinedConstraints.values(property)) {
                sizes.add(assertInstanceOf(List.class, value).size());
            }
            return sizes;
        }

        @Test
        @DisplayName("each position follows its own schema, and an array holds the whole tuple by default")
        void positionsFollowTheirOwnSchemas() {
            assertEquals(Set.of(List.of(1, 2)), CombinedConstraints.distinct(array("items", ONE_TWO)));
            for (Object value : CombinedConstraints.values(array(
                    "items",
                    List.of(Map.of("type", "integer", "minimum", 1, "maximum", 9), Map.of("type", "boolean"))))) {
                List<?> items = assertInstanceOf(List.class, value);
                assertEquals(2, items.size(), items::toString);
                int first = assertInstanceOf(Integer.class, items.get(0));
                assertTrue(first >= 1 && first <= 9, items::toString);
                assertInstanceOf(Boolean.class, items.get(1));
            }
        }

        @Test
        @DisplayName("minItems and maxItems move the size away from the tuple's length")
        void boundsMoveTheSize() {
            assertEquals(Set.of(List.of(1)), CombinedConstraints.distinct(array("items", ONE_TWO, "maxItems", 1)));
            assertEquals(
                    Set.of(List.of(), List.of(1), List.of(1, 2)),
                    CombinedConstraints.distinct(array("items", ONE_TWO, "minItems", 0)));
            // Without additionalItems, the items after the tuple are anything: strings, as for an array
            // without items.
            for (Object value :
                    CombinedConstraints.values(array("items", List.of(Map.of("const", 1)), "minItems", 3))) {
                List<?> items = assertInstanceOf(List.class, value);
                assertEquals(3, items.size(), items::toString);
                assertEquals(1, items.get(0));
                assertInstanceOf(String.class, items.get(1));
                assertInstanceOf(String.class, items.get(2));
            }
        }

        @Test
        @DisplayName("additionalItems false ends an array with the tuple")
        void additionalItemsFalse() {
            assertEquals(
                    Set.of(List.of(), List.of(1), List.of(1, 2)),
                    CombinedConstraints.distinct(
                            array("items", ONE_TWO, "additionalItems", false, "minItems", 0, "maxItems", 9)));
            CombinedConstraints.assertRejected(array("items", ONE_TWO, "additionalItems", false, "minItems", 3));
        }

        @Test
        @DisplayName("an additionalItems schema gives the items after the tuple, up to maxItems")
        void additionalItemsSchema() {
            Map<String, Object> five = Map.of("const", 5);
            assertEquals(
                    Set.of(List.of(1, 2, 5)),
                    CombinedConstraints.distinct(array("items", ONE_TWO, "additionalItems", five, "minItems", 3)));
            assertEquals(Set.of(2, 3, 4), sizes(array("items", ONE_TWO, "additionalItems", five, "maxItems", 4)));
            for (Object value :
                    CombinedConstraints.values(array("items", ONE_TWO, "additionalItems", five, "maxItems", 4))) {
                List<?> items = assertInstanceOf(List.class, value);
                assertEquals(List.of(1, 2), items.subList(0, 2));
                assertTrue(items.subList(2, items.size()).stream().allMatch(item -> item.equals(5)), items::toString);
            }
        }

        @Test
        @DisplayName("additionalItems is ignored beside a single items schema, as the drafts that define it say")
        void additionalItemsIgnoredBesideOneSchema() {
            Map<String, Object> one = Map.of("const", 1);
            assertEquals(
                    Set.of(List.of(1, 1)),
                    CombinedConstraints.distinct(
                            array("items", one, "additionalItems", false, "minItems", 2, "maxItems", 2)));
            assertEquals(
                    Set.of(List.of(1)),
                    CombinedConstraints.distinct(array(
                            "items", one, "additionalItems", Map.of("$ref", "#/x"), "minItems", 1, "maxItems", 1)));
            assertEquals(
                    Set.of(List.of(1)),
                    CombinedConstraints.distinct(
                            array("items", one, "additionalItems", "not a schema", "minItems", 1, "maxItems", 1)));
        }

        @Test
        @DisplayName("items false allows only the empty array, and a false entry ends the array before it")
        void falseSchemas() {
            assertEquals(Set.of(List.of()), CombinedConstraints.distinct(array("items", false)));
            assertEquals(Set.of(List.of()), CombinedConstraints.distinct(array("items", false, "minItems", 0)));
            CombinedConstraints.assertRejected(array("items", false, "minItems", 1));
            List<Object> stops = List.of(Map.of("const", 1), false, Map.of("const", 3));
            assertEquals(Set.of(List.of(1)), CombinedConstraints.distinct(array("items", stops, "maxItems", 5)));
            assertEquals(
                    Set.of(List.of(), List.of(1)),
                    CombinedConstraints.distinct(array("items", stops, "minItems", 0, "maxItems", 5)));
            CombinedConstraints.assertRejected(array("items", stops, "minItems", 2));
            // A false first entry allows only the empty array.
            assertEquals(Set.of(List.of()), CombinedConstraints.distinct(array("items", List.of(false))));
        }

        @Test
        @DisplayName("true allows any item, like an empty schema: a string")
        void trueSchemas() {
            for (Object value : CombinedConstraints.values(array("items", List.of(true, Map.of("const", 2))))) {
                List<?> items = assertInstanceOf(List.class, value);
                assertInstanceOf(String.class, items.get(0));
                assertEquals(2, items.get(1));
            }
            for (Object value : CombinedConstraints.values(
                    array("items", List.of(Map.of("const", 1)), "additionalItems", true, "minItems", 2))) {
                assertInstanceOf(
                        String.class, assertInstanceOf(List.class, value).get(1));
            }
            for (Object value : CombinedConstraints.values(array("items", true, "minItems", 2, "maxItems", 2))) {
                List<?> items = assertInstanceOf(List.class, value);
                assertEquals(2, items.size());
                assertInstanceOf(String.class, items.get(0));
            }
            // An empty list of items makes every item an additional one.
            assertEquals(Set.of(List.of()), CombinedConstraints.distinct(array("items", List.of())));
            assertEquals(
                    Set.of(List.of(7, 7)),
                    CombinedConstraints.distinct(
                            array("items", List.of(), "additionalItems", Map.of("const", 7), "minItems", 2)));
        }

        @Test
        @DisplayName("uniqueItems keeps the items distinct across the tuple and after it")
        void uniqueTuples() {
            assertEquals(
                    Set.of(List.of(1, 2)),
                    CombinedConstraints.distinct(array(
                            "uniqueItems", true, "items", List.of(Map.of("const", 1), Map.of("enum", List.of(1, 2))))));
            // 1 and 1.0 are one JSON value, so a tuple of both cannot be unique.
            CombinedConstraints.assertRejected(
                    array("uniqueItems", true, "items", List.of(Map.of("const", 1), Map.of("const", 1.0))));
            // Where minItems leaves room, the array ends at the position that runs out of distinct values.
            assertEquals(
                    Set.of(List.of(1)),
                    CombinedConstraints.distinct(array(
                            "uniqueItems",
                            true,
                            "minItems",
                            1,
                            "items",
                            List.of(Map.of("const", 1), Map.of("const", 1), Map.of("const", 2)))));
            for (Object value : CombinedConstraints.values(array(
                    "uniqueItems",
                    true,
                    "items",
                    List.of(Map.of("const", 1)),
                    "additionalItems",
                    Map.of("enum", List.of(1, 2, 3)),
                    "minItems",
                    3,
                    "maxItems",
                    3))) {
                List<?> items = assertInstanceOf(List.class, value);
                assertEquals(1, items.getFirst(), items::toString);
                assertEquals(Set.of(2, 3), new HashSet<>(items.subList(1, 3)), items::toString);
            }
        }

        @Test
        @DisplayName("a const or enum array meets the tuple, additionalItems and false items")
        void constAndEnumMeetTheTuple() {
            List<Object> tuple = List.of(Map.of("type", "integer"), Map.of("type", "string"));
            assertEquals(
                    Set.of(List.of(1, "a")),
                    CombinedConstraints.distinct(Map.of("const", List.of(1, "a"), "items", tuple)));
            CombinedConstraints.assertRejected(Map.of("const", List.of("a", 1), "items", tuple));
            assertEquals(
                    Set.of(List.of(1, "a", true)),
                    CombinedConstraints.distinct(Map.of("const", List.of(1, "a", true), "items", tuple)));
            CombinedConstraints.assertRejected(
                    Map.of("const", List.of(1, "a", true), "items", tuple, "additionalItems", false));
            CombinedConstraints.assertRejected(Map.of(
                    "const", List.of(1, "a", true), "items", tuple, "additionalItems", Map.of("type", "string")));
            assertEquals(
                    Set.of(List.of(1, "a", "b")),
                    CombinedConstraints.distinct(Map.of(
                            "const",
                            List.of(1, "a", "b"),
                            "items",
                            tuple,
                            "additionalItems",
                            Map.of("type", "string"))));
            assertEquals(
                    Set.of(List.of(1, "a")),
                    CombinedConstraints.distinct(
                            Map.of("enum", List.of(List.of(1, "a"), List.of("a", 1)), "items", tuple)));
            CombinedConstraints.assertRejected(Map.of("const", List.of(1), "items", false));
            assertEquals(Set.of(List.of()), CombinedConstraints.distinct(Map.of("const", List.of(), "items", false)));
            CombinedConstraints.assertRejected(Map.of("const", List.of(1, 2), "items", List.of(true, false)));
            // additionalItems is ignored beside a single items schema here too.
            assertEquals(
                    Set.of(List.of(1, 2)),
                    CombinedConstraints.distinct(Map.of(
                            "const", List.of(1, 2), "items", Map.of("type", "integer"), "additionalItems", false)));
        }

        @Test
        @DisplayName("tuple entries and additionalItems are checked and resolved at their own paths")
        void pathsAndShapes() {
            Map<String, Object> conflict = Map.of("type", "string", "pattern", "^A{8}$", "maxLength", 3);
            CombinedConstraints.assertRejectedAt(
                    "$.properties.value.items[1]", Map.of("value", array("items", List.of(Map.of(), conflict))));
            CombinedConstraints.assertRejectedAt(
                    "$.properties.value.additionalItems",
                    Map.of("value", array("items", List.of(Map.of()), "additionalItems", conflict)));
            CombinedConstraints.assertRejectedAt(
                    "$.properties.value.items[0]", Map.of("value", array("items", List.of(Map.of("$ref", "#/x")))));
            CombinedConstraints.assertRejectedAt(
                    "$.properties.value.additionalItems",
                    Map.of("value", array("items", List.of(), "additionalItems", Map.of("oneOf", List.of()))));
            for (Object items : List.of("string", 5, List.of(1), List.of(Map.of(), "x"))) {
                CombinedConstraints.assertRejected(array("items", items));
            }
            Map<String, Object> nullItems = array();
            nullItems.put("items", null);
            CombinedConstraints.assertRejected(nullItems);
            CombinedConstraints.assertRejected(array("items", List.of(), "additionalItems", "no"));
            // The shape is checked wherever the keyword appears, beside a const too.
            CombinedConstraints.assertRejected(Map.of("type", "string", "items", 5));
            CombinedConstraints.assertRejected(Map.of("const", List.of(1), "items", "not-a-schema"));
        }

        @Test
        @DisplayName("seeded tuples replay")
        void tuplesReplay() {
            List<Map<String, Object>> properties = List.of(
                    array("items", List.of(Map.of("type", "integer"), Map.of("type", "string")), "maxItems", 5),
                    array(
                            "uniqueItems",
                            true,
                            "items",
                            List.of(Map.of("type", "integer", "minimum", 1, "maximum", 5)),
                            "additionalItems",
                            Map.of("type", "integer", "minimum", 1, "maximum", 5),
                            "maxItems",
                            5));
            for (Map<String, Object> property : properties) {
                assertEquals(CombinedConstraints.values(property), CombinedConstraints.values(property));
            }
        }
    }

    @Nested
    @DisplayName("object members: required, additionalProperties, minProperties, maxProperties, dependencies")
    class ObjectMembers {

        private static Map<String, Object> object(Object... keywordsAndValues) {
            Map<String, Object> property = new LinkedHashMap<>();
            property.put("type", "object");
            for (int i = 0; i < keywordsAndValues.length; i += 2) {
                property.put((String) keywordsAndValues[i], keywordsAndValues[i + 1]);
            }
            return property;
        }

        /** Properties that each hold their own name, in order. */
        private static Map<String, Object> named(String... names) {
            Map<String, Object> properties = new LinkedHashMap<>();
            for (String name : names) {
                properties.put(name, Map.of("const", name));
            }
            return properties;
        }

        private static Set<List<String>> memberLists(Map<String, Object> property) {
            Set<List<String>> lists = new HashSet<>();
            for (Object value : CombinedConstraints.values(property)) {
                List<String> names = new ArrayList<>();
                for (Object name : assertInstanceOf(Map.class, value).keySet()) {
                    names.add((String) name);
                }
                lists.add(names);
            }
            return lists;
        }

        private static Map<String, Object> root(Object... keywordsAndValues) {
            Map<String, Object> root = object(keywordsAndValues);
            root.putIfAbsent("properties", named("a", "b"));
            return root;
        }

        private static void assertRootRejected(Map<String, Object> root) {
            IllegalArgumentException exception =
                    assertThrows(IllegalArgumentException.class, () -> SchemaParser.fromJsonSchema(root));
            assertTrue(exception.getMessage().endsWith(" at $"), exception.getMessage());
        }

        @Test
        @DisplayName("maxProperties leaves out optional properties, and required ones stay")
        void maxPropertiesLeavesOutOptionalProperties() {
            assertEquals(
                    Set.of(List.of("a"), List.of("b")),
                    memberLists(object("properties", named("a", "b"), "maxProperties", 1)));
            assertEquals(
                    Set.of(List.of("a", "b"), List.of("a", "c")),
                    memberLists(
                            object("properties", named("a", "b", "c"), "required", List.of("a"), "maxProperties", 2)));
            assertEquals(Set.of(List.of()), memberLists(object("properties", named("a", "b"), "maxProperties", 0)));
            // The values are the properties' own.
            for (Object value : CombinedConstraints.values(object("properties", named("a", "b"), "maxProperties", 1))) {
                Map<?, ?> members = assertInstanceOf(Map.class, value);
                members.forEach((name, member) -> assertEquals(name, member));
            }
            // Within the bounds, every property is generated, as without them.
            assertEquals(
                    Set.of(List.of("a", "b")),
                    memberLists(object("properties", named("a", "b"), "minProperties", 2, "maxProperties", 2)));
            assertEquals(Set.of(List.of("a", "b")), memberLists(object("properties", named("a", "b"))));
        }

        @Test
        @DisplayName("minProperties adds additional properties where additionalProperties allows them")
        void minPropertiesAddsAdditionalProperties() {
            for (Object value : CombinedConstraints.values(object("properties", named("a"), "minProperties", 3))) {
                Map<?, ?> members = assertInstanceOf(Map.class, value);
                assertEquals(List.of("a", "property1", "property2"), List.copyOf(members.keySet()));
                assertInstanceOf(String.class, members.get("property1"));
                assertInstanceOf(String.class, members.get("property2"));
            }
            assertEquals(
                    Set.of(Map.of("property1", 7, "property2", 7)),
                    CombinedConstraints.distinct(
                            object("additionalProperties", Map.of("const", 7), "minProperties", 2)));
            // Their names stay clear of every name the schema gives.
            assertEquals(
                    Set.of(List.of("property1", "property3")),
                    memberLists(object(
                            "properties",
                            named("property1"),
                            "dependencies",
                            Map.of("x", List.of("property2")),
                            "minProperties",
                            2)));
            CombinedConstraints.assertRejected(
                    object("properties", named("a"), "additionalProperties", false, "minProperties", 2));
            assertEquals(
                    Set.of(List.of("a", "b")),
                    memberLists(
                            object("properties", named("a", "b"), "additionalProperties", false, "minProperties", 2)));
        }

        @Test
        @DisplayName("a required property outside properties is generated through additionalProperties")
        void requiredPropertiesOutsideProperties() {
            // Without additionalProperties, it is a string, which its name can make realistic.
            for (Object value :
                    CombinedConstraints.values(object("properties", named("a"), "required", List.of("a", "email")))) {
                Map<?, ?> members = assertInstanceOf(Map.class, value);
                assertEquals(List.of("a", "email"), List.copyOf(members.keySet()));
                assertTrue(((String) members.get("email")).contains("@"), members::toString);
            }
            for (Object value : CombinedConstraints.values(object("required", List.of("code")))) {
                Map<?, ?> members = assertInstanceOf(Map.class, value);
                assertEquals(List.of("code"), List.copyOf(members.keySet()));
                assertInstanceOf(String.class, members.get("code"));
            }
            assertEquals(
                    Set.of(Map.of("a", "a", "id", 9)),
                    CombinedConstraints.distinct(object(
                            "properties",
                            named("a"),
                            "required",
                            List.of("id"),
                            "additionalProperties",
                            Map.of("const", 9))));
            CombinedConstraints.assertRejected(
                    object("properties", named("a"), "required", List.of("ghost"), "additionalProperties", false));
            CombinedConstraints.assertRejected(object("properties", Map.of("a", false), "required", List.of("a")));
            CombinedConstraints.assertRejected(object("properties", Map.of("a", "x"), "required", List.of("a")));
            CombinedConstraints.assertRejected(
                    object("properties", named("a", "b"), "required", List.of("a", "b"), "maxProperties", 1));
            CombinedConstraints.assertRejected(
                    object("properties", named("a"), "minProperties", 2, "maxProperties", 1));
        }

        @Test
        @DisplayName("a true property schema allows any value, a string, and false leaves the property out")
        void booleanPropertySchemas() {
            Map<String, Object> properties = new LinkedHashMap<>();
            properties.put("a", true);
            properties.put("b", false);
            properties.put("c", Map.of("const", "c"));
            for (Object value : CombinedConstraints.values(object("properties", properties))) {
                Map<?, ?> members = assertInstanceOf(Map.class, value);
                assertEquals(List.of("a", "c"), List.copyOf(members.keySet()));
                assertInstanceOf(String.class, members.get("a"));
            }
        }

        @Test
        @DisplayName("a property dependency brings its properties, and a property that cannot have them is left out")
        void propertyDependencies() {
            Map<String, Object> aNeedsB = Map.of("a", List.of("b"));
            assertEquals(
                    Set.of(List.of("a", "b"), List.of("b", "c")),
                    memberLists(
                            object("properties", named("a", "b", "c"), "dependencies", aNeedsB, "maxProperties", 2)));
            // Without bounds, dependencies between declared properties change nothing.
            assertEquals(
                    Set.of(List.of("a", "b")),
                    memberLists(object("properties", named("a", "b"), "dependencies", aNeedsB)));
            // A dependency outside properties is generated through additionalProperties.
            for (Object value : CombinedConstraints.values(
                    object("properties", named("a"), "dependencies", Map.of("a", List.of("zip"))))) {
                Map<?, ?> members = assertInstanceOf(Map.class, value);
                assertEquals(List.of("a", "zip"), List.copyOf(members.keySet()));
                assertInstanceOf(String.class, members.get("zip"));
            }
            // Where additionalProperties is false, the property that needs it is left out, and is rejected
            // if it is required.
            Map<String, Object> aNeedsZip = Map.of("a", List.of("zip"));
            assertEquals(
                    Set.of(List.of("b")),
                    memberLists(object(
                            "properties", named("a", "b"), "dependencies", aNeedsZip, "additionalProperties", false)));
            CombinedConstraints.assertRejected(object(
                    "properties",
                    named("a", "b"),
                    "required",
                    List.of("a"),
                    "dependencies",
                    aNeedsZip,
                    "additionalProperties",
                    false));
            // The dependencies of a required property are required too, through every link.
            assertEquals(
                    Set.of(List.of("a", "b", "c")),
                    memberLists(object(
                            "properties",
                            named("a", "b", "c", "d"),
                            "required",
                            List.of("a"),
                            "dependencies",
                            Map.of("a", List.of("b"), "b", List.of("c")),
                            "maxProperties",
                            3)));
            CombinedConstraints.assertRejected(object(
                    "properties",
                    named("a", "b"),
                    "required",
                    List.of("a"),
                    "dependencies",
                    aNeedsB,
                    "maxProperties",
                    1));
            // A property that is required and also a dependency of a required one counts once.
            assertEquals(
                    Set.of(List.of("a", "b")),
                    memberLists(object(
                            "properties",
                            named("a", "b", "c"),
                            "required",
                            List.of("a", "b"),
                            "dependencies",
                            aNeedsB,
                            "maxProperties",
                            2)));
        }

        @Test
        @DisplayName("a rejection says which property cannot be generated, and why")
        void rejectionsSayWhy() {
            String path = "$.properties.value";
            assertEquals(
                    "JSON Schema required property 'a' cannot be generated at " + path + ": its schema is false",
                    rejection(object("properties", Map.of("a", false), "required", List.of("a"))));
            assertEquals(
                    "JSON Schema required property 'a' cannot be generated at " + path
                            + ": its schema is no schema object",
                    rejection(object("properties", Map.of("a", "x"), "required", List.of("a"))));
            assertEquals(
                    "JSON Schema property 'zip', which dependencies require beside required property 'a', cannot"
                            + " be generated at " + path
                            + ": it is not in properties, and additionalProperties is false",
                    rejection(object(
                            "properties",
                            named("a"),
                            "required",
                            List.of("a"),
                            "dependencies",
                            Map.of("a", List.of("zip")),
                            "additionalProperties",
                            false)));
        }

        private static String rejection(Map<String, Object> property) {
            return assertThrows(
                            IllegalArgumentException.class,
                            () -> SchemaParser.fromJsonSchema(objectSchema(Map.of("value", property))))
                    .getMessage();
        }

        @Test
        @DisplayName("properties that depend on each other are left out together")
        void dependencyCycles() {
            Map<String, Object> bothOrNeither = Map.of("a", List.of("b"), "b", List.of("a"));
            assertEquals(
                    Set.of(List.of("a", "b"), List.of("c")),
                    memberLists(object(
                            "properties", named("a", "b", "c"), "dependencies", bothOrNeither, "maxProperties", 2)));
            // Where that leaves too few, additional properties make up the difference.
            assertEquals(
                    Set.of(List.of("a", "b"), List.of("c", "property1")),
                    memberLists(object(
                            "properties",
                            named("a", "b", "c"),
                            "dependencies",
                            bothOrNeither,
                            "minProperties",
                            2,
                            "maxProperties",
                            2)));
            // Without them, the range must be as wide as the properties left out together.
            CombinedConstraints.assertRejected(object(
                    "properties",
                    named("a", "b", "c"),
                    "dependencies",
                    bothOrNeither,
                    "additionalProperties",
                    false,
                    "minProperties",
                    2,
                    "maxProperties",
                    2));
            assertEquals(
                    Set.of(List.of("a", "b"), List.of("c")),
                    memberLists(object(
                            "properties",
                            named("a", "b", "c"),
                            "dependencies",
                            bothOrNeither,
                            "additionalProperties",
                            false,
                            "minProperties",
                            1,
                            "maxProperties",
                            2)));
            // Where nothing has to be left out, the range can be narrower than a group.
            assertEquals(
                    Set.of(List.of("a", "b")),
                    memberLists(object(
                            "properties",
                            named("a", "b"),
                            "dependencies",
                            bothOrNeither,
                            "additionalProperties",
                            false,
                            "minProperties",
                            2,
                            "maxProperties",
                            2)));
        }

        @Test
        @DisplayName("a schema dependency is rejected with its path, as it needs schema composition")
        void schemaDependenciesAreRejected() {
            CombinedConstraints.assertRejectedAt(
                    "$.properties.value.dependencies.a",
                    Map.of(
                            "value",
                            object(
                                    "properties",
                                    named("a"),
                                    "dependencies",
                                    Map.of("a", Map.of("required", List.of("b"))))));
            CombinedConstraints.assertRejectedAt(
                    "$.properties.value.dependencies.a", Map.of("value", object("dependencies", Map.of("a", true))));
            for (Object dependencies : List.of(List.of("a"), Map.of("a", List.of(1)), Map.of("a", "b"))) {
                CombinedConstraints.assertRejected(object("dependencies", dependencies));
            }
            Map<Object, Object> numberKey = new HashMap<>();
            numberKey.put(1, List.of("a"));
            CombinedConstraints.assertRejected(object("dependencies", numberKey));
            // The keyword is checked wherever it appears.
            CombinedConstraints.assertRejected(Map.of("type", "string", "dependencies", Map.of("a", Map.of())));
        }

        @Test
        @DisplayName("malformed counts, required lists and additionalProperties are rejected with the path")
        void shapes() {
            for (Object count : List.of(-1, 1.5, "2", Double.NaN)) {
                CombinedConstraints.assertRejected(object("minProperties", count));
                CombinedConstraints.assertRejected(object("maxProperties", count));
            }
            // A whole number written as a decimal, or one beyond an int, is a count.
            assertEquals(
                    Set.of(List.of("a"), List.of("b")),
                    memberLists(object("properties", named("a", "b"), "maxProperties", 1.0)));
            assertEquals(
                    Set.of(List.of("a", "b")),
                    memberLists(object("properties", named("a", "b"), "maxProperties", new BigDecimal("1e30"))));
            CombinedConstraints.assertRejected(object("required", "a"));
            CombinedConstraints.assertRejected(object("required", List.of(1)));
            CombinedConstraints.assertRejected(object("additionalProperties", "no"));
            // additionalProperties is checked and resolved like any other subschema.
            CombinedConstraints.assertRejectedAt(
                    "$.properties.value.additionalProperties",
                    Map.of("value", object("additionalProperties", Map.of("$ref", "#/x"))));
            CombinedConstraints.assertRejectedAt(
                    "$.properties.value.additionalProperties",
                    Map.of(
                            "value",
                            object(
                                    "additionalProperties",
                                    Map.of("type", "string", "pattern", "^A{8}$", "maxLength", 3))));
        }

        @Test
        @DisplayName("a const or enum object meets the member keywords")
        void constAndEnumObjects() {
            Map<String, Object> one = Map.of("a", 1);
            assertEquals(Set.of(one), CombinedConstraints.distinct(Map.of("const", one, "required", List.of("a"))));
            CombinedConstraints.assertRejected(Map.of("const", one, "required", List.of("b")));
            CombinedConstraints.assertRejected(Map.of("const", Map.of("a", 1, "b", 2), "maxProperties", 1));
            CombinedConstraints.assertRejected(Map.of("const", one, "minProperties", 2));
            assertEquals(
                    Set.of(one),
                    CombinedConstraints.distinct(Map.of("const", one, "minProperties", 1, "maxProperties", 1)));
            CombinedConstraints.assertRejected(Map.of(
                    "const", Map.of("x", 1), "properties", Map.of("a", Map.of()), "additionalProperties", false));
            CombinedConstraints.assertRejected(
                    Map.of("const", Map.of("x", 1), "additionalProperties", Map.of("type", "string")));
            assertEquals(
                    Set.of(Map.of("x", "s")),
                    CombinedConstraints.distinct(
                            Map.of("const", Map.of("x", "s"), "additionalProperties", Map.of("type", "string"))));
            CombinedConstraints.assertRejected(Map.of("const", one, "dependencies", Map.of("a", List.of("b"))));
            assertEquals(
                    Set.of(Map.of("a", 1, "b", 2)),
                    CombinedConstraints.distinct(
                            Map.of("const", Map.of("a", 1, "b", 2), "dependencies", Map.of("a", List.of("b")))));
            assertEquals(
                    Set.of(Map.of("b", 1)),
                    CombinedConstraints.distinct(
                            Map.of("const", Map.of("b", 1), "dependencies", Map.of("a", List.of("b")))));
            CombinedConstraints.assertRejected(Map.of("const", one, "properties", Map.of("a", false)));
            assertEquals(
                    Set.of(one),
                    CombinedConstraints.distinct(
                            Map.of("enum", List.of(one, Map.of("b", 1)), "required", List.of("a"))));
        }

        @Test
        @DisplayName("the root generates every property as a column, so its member keywords must allow that")
        void rootObject() {
            assertRootRejected(root("maxProperties", 1));
            assertRootRejected(root("minProperties", 3));
            assertRootRejected(root("required", List.of("a", "ghost")));
            assertRootRejected(root("dependencies", Map.of("a", List.of("ghost"))));
            // The root's additionalProperties generates nothing, but is resolved like every schema.
            IllegalArgumentException additional = assertThrows(
                    IllegalArgumentException.class,
                    () -> SchemaParser.fromJsonSchema(root(
                            "additionalProperties", Map.of("type", "string", "pattern", "^A{8}$", "maxLength", 3))));
            assertTrue(additional.getMessage().contains("$.additionalProperties"), additional.getMessage());
            assertEquals(
                    Map.of("a", "a", "b", "b"),
                    SchemaParser.fromJsonSchema(root("additionalProperties", Map.of("type", "integer")))
                            .generate());
            // A dependency of a property the root does not have needs nothing.
            Schema schema = SchemaParser.fromJsonSchema(root(
                    "required",
                    List.of("a", "b"),
                    "minProperties",
                    2,
                    "maxProperties",
                    2,
                    "dependencies",
                    Map.of("a", List.of("b"), "ghost", List.of("other")),
                    "additionalProperties",
                    false));
            assertEquals(Map.of("a", "a", "b", "b"), schema.generate());
        }

        @Test
        @DisplayName("seeded objects replay when members are left out or added")
        void objectsReplay() {
            List<Map<String, Object>> properties = List.of(
                    object(
                            "properties",
                            Map.of("a", Map.of("type", "integer"), "b", Map.of("type", "string")),
                            "maxProperties",
                            1),
                    object("additionalProperties", Map.of("type", "integer"), "minProperties", 3));
            for (Map<String, Object> property : properties) {
                assertEquals(CombinedConstraints.values(property), CombinedConstraints.values(property));
            }
        }
    }

    @Nested
    @DisplayName("formats")
    class Formats {

        private static final String SECONDS_AND_OFFSET = ":\\d{2}(\\.\\d{1,9})?(Z|[+-]\\d{2}:\\d{2})";

        /** RFC 3339 Appendix A, the duration grammar that Draft 2020-12 names. */
        private static final Pattern DURATION =
                Pattern.compile("P(?:(?:\\d+D|\\d+M(?:\\d+D)?|\\d+Y(?:\\d+M(?:\\d+D)?)?)"
                        + "(?:T(?:\\d+H(?:\\d+M(?:\\d+S)?)?|\\d+M(?:\\d+S)?|\\d+S))?"
                        + "|T(?:\\d+H(?:\\d+M(?:\\d+S)?)?|\\d+M(?:\\d+S)?|\\d+S)|\\d+W)");

        /** RFC 6901: slash-led segments, which write ~ and / as ~0 and ~1. */
        private static final String POINTER = "(?:/(?:[^/~]|~[01])*)*";

        private static List<String> strings(String format) {
            List<String> strings = new ArrayList<>();
            for (Object value : CombinedConstraints.values(Map.of("type", "string", "format", format))) {
                strings.add(assertInstanceOf(String.class, value));
            }
            return strings;
        }

        @Test
        @DisplayName("date-time and time are RFC 3339, with seconds and an offset")
        void dateTimeAndTimeAreRfc3339() {
            for (String value : strings("date-time")) {
                assertTrue(value.matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}" + SECONDS_AND_OFFSET), value);
                assertDoesNotThrow(() -> OffsetDateTime.parse(value), value);
            }
            for (String value : strings("time")) {
                assertTrue(value.matches("\\d{2}:\\d{2}" + SECONDS_AND_OFFSET), value);
                assertDoesNotThrow(() -> OffsetTime.parse(value), value);
            }
        }

        @Test
        @DisplayName("date-time-local and time-local are RFC 3339 without the offset, in the ranges of the others")
        void localDateTimeAndTime() {
            for (String value : strings("date-time-local")) {
                assertTrue(value.matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}"), value);
                int year = LocalDateTime.parse(value).getYear();
                assertTrue(year >= 1970 && year <= 2100, value);
            }
            for (String value : strings("time-local")) {
                assertTrue(value.matches("\\d{2}:\\d{2}:\\d{2}(\\.\\d{1,9})?"), value);
                assertDoesNotThrow(() -> LocalTime.parse(value), value);
            }
        }

        /** Members whose types a schema exported from them must keep apart. */
        record Moments(LocalDateTime localDateTime, LocalTime localTime, OffsetDateTime offsetDateTime) {}

        @Test
        @DisplayName("a schema exported from local and offset date-times parses back to values each type holds")
        void exportedDateTimesRoundTrip() {
            Map<String, Object> exported = JsonSchemaSupport.record(Moments.class);
            for (Map<String, Object> record : SchemaParser.fromJsonSchema(
                            exported, GeneratorConfig.builder().seed(7L).build())
                    .generateBatch(300)) {
                assertDoesNotThrow(() -> LocalDateTime.parse((String) record.get("localDateTime")), record::toString);
                assertDoesNotThrow(() -> LocalTime.parse((String) record.get("localTime")), record::toString);
                assertDoesNotThrow(() -> OffsetDateTime.parse((String) record.get("offsetDateTime")), record::toString);
            }
        }

        @Test
        @DisplayName(
                "a URI is also a URI reference, an IRI, an IRI reference and, with no quote or brace, a URI template")
        void uriFamily() {
            for (String format : List.of("uri-reference", "iri", "iri-reference", "uri-template")) {
                for (String value : strings(format)) {
                    URI uri = assertDoesNotThrow(() -> new URI(value), value);
                    assertNotNull(uri.getScheme(), value);
                    assertTrue(value.chars().noneMatch(ch -> " \"'<>\\^`{|}".indexOf(ch) >= 0), value);
                }
            }
        }

        @Test
        @DisplayName("a hostname is also an internationalized one")
        void idnHostname() {
            for (String value : strings("idn-hostname")) {
                assertDoesNotThrow(() -> IDN.toASCII(value, IDN.USE_STD3_ASCII_RULES), value);
                assertTrue(value.matches("[a-z0-9]([a-z0-9-]*[a-z0-9])?(\\.[a-z0-9]([a-z0-9-]*[a-z0-9])?)+"), value);
            }
        }

        @Test
        @DisplayName("duration, json-pointer, relative-json-pointer and regex follow their grammars")
        void grammars() {
            Set<String> durations = new HashSet<>(strings("duration"));
            for (String value : durations) {
                assertTrue(DURATION.matcher(value).matches(), value);
            }
            assertTrue(durations.size() > 100, "durations should vary: " + durations);
            // One to three segments of one to eight lowercase letters, which need no escaping.
            String segments = "(?:/[a-z]{1,8}){1,3}";
            for (String value : strings("json-pointer")) {
                assertTrue(value.matches(POINTER) && value.matches(segments), value);
            }
            for (String value : strings("relative-json-pointer")) {
                assertTrue(
                        value.matches("(?:0|[1-9]\\d*)(?:#|" + POINTER + ")") && value.matches("\\d" + segments),
                        value);
            }
            for (String value : strings("regex")) {
                assertTrue(Pattern.compile(value).matcher(value).matches(), value);
            }
        }

        @Test
        @DisplayName("OpenAPI byte is base64, and binary, password and unknown formats are strings")
        void openApiStringFormats() {
            Set<String> bytes = new HashSet<>(strings("byte"));
            for (String value : bytes) {
                assertTrue(value.matches("[A-Za-z0-9+/]+={0,2}") && value.length() % 4 == 0, value);
                assertTrue(Base64.getDecoder().decode(value).length > 0, value);
            }
            assertTrue(bytes.size() > 100, "bytes should vary: " + bytes);
            for (String format : List.of("binary", "password", "custom-unknown")) {
                assertTrue(strings(format).stream().noneMatch(String::isEmpty), format);
            }
        }

        @Test
        @DisplayName("a value of these formats is drawn again until it fits the length bounds")
        void newFormatsMeetLengthBounds() {
            for (String format : List.of("duration", "json-pointer", "relative-json-pointer", "regex", "byte")) {
                for (Object value : CombinedConstraints.values(
                        Map.of("type", "string", "format", format, "minLength", 11, "maxLength", 12))) {
                    int length = ((String) value).length();
                    assertTrue(length >= 11 && length <= 12, format + " gave " + value);
                }
            }
        }

        @Test
        @DisplayName("OpenAPI int32 keeps an integer within an int, and int64 within a long")
        void integerFormats() {
            for (Object value : CombinedConstraints.values(Map.of(
                    "type", "integer", "format", "int32", "minimum", 2_000_000_000L, "maximum", 10_000_000_000L))) {
                long number = ((Number) value).longValue();
                assertTrue(number >= 2_000_000_000L && number <= Integer.MAX_VALUE, String.valueOf(number));
            }
            for (Object value : CombinedConstraints.values(Map.of(
                    "type",
                    "integer",
                    "format",
                    "int32",
                    "multipleOf",
                    7,
                    "minimum",
                    -10_000_000_000L,
                    "maximum",
                    10_000_000_000L))) {
                long number = ((Number) value).longValue();
                assertTrue(
                        number >= Integer.MIN_VALUE && number <= Integer.MAX_VALUE && number % 7 == 0,
                        String.valueOf(number));
            }
            CombinedConstraints.assertRejected(Map.of("type", "integer", "format", "int32", "minimum", 3_000_000_000L));
            CombinedConstraints.assertRejected(
                    Map.of("type", "integer", "format", "int32", "maximum", -3_000_000_000L));
            for (Object value : CombinedConstraints.values(
                    Map.of("type", "integer", "format", "int64", "minimum", 3_000_000_000L))) {
                assertTrue(((Number) value).longValue() >= 3_000_000_000L, value::toString);
            }
            // Both name integers, which a number with either must be too.
            for (Object value : CombinedConstraints.values(Map.of("type", "number", "format", "int32"))) {
                assertTrue(value instanceof Integer || value instanceof Long, value::toString);
                long number = ((Number) value).longValue();
                assertTrue(number >= Integer.MIN_VALUE && number <= Integer.MAX_VALUE, String.valueOf(number));
            }
            CombinedConstraints.assertRejected(
                    Map.of("type", "number", "format", "int32", "minimum", 3_000_000_000L, "maximum", 3_000_000_000L));
            assertEquals(
                    Set.of(1, 2, 3),
                    CombinedConstraints.distinct(
                            Map.of("type", "number", "format", "int64", "minimum", 0.5, "maximum", 3.5)));
        }

        @Test
        @DisplayName("OpenAPI float and double keep a number within their range")
        void numberFormats() {
            for (Object value : CombinedConstraints.values(
                    Map.of("type", "number", "format", "float", "minimum", -1e300, "maximum", -1e38))) {
                double number = ((Number) value).doubleValue();
                assertTrue(number >= -Float.MAX_VALUE && number <= -1e38, String.valueOf(number));
            }
            for (Object value : CombinedConstraints.values(
                    Map.of("type", "number", "format", "float", "minimum", 1e38, "maximum", 1e300))) {
                double number = ((Number) value).doubleValue();
                assertTrue(number >= 1e38 && number <= Float.MAX_VALUE, String.valueOf(number));
            }
            for (Object value : CombinedConstraints.values(Map.of(
                    "type", "number", "format", "float", "multipleOf", 1e37, "minimum", -1e39, "maximum", 1e39))) {
                BigDecimal number = assertInstanceOf(BigDecimal.class, value);
                assertTrue(number.abs().compareTo(new BigDecimal(Float.MAX_VALUE)) <= 0, number::toString);
            }
            CombinedConstraints.assertRejected(Map.of("type", "number", "format", "float", "minimum", 1e39));
            CombinedConstraints.assertRejected(Map.of("type", "number", "format", "float", "maximum", -1e39));
            BigDecimal beyondDoubles = new BigDecimal("1e400");
            for (Object value : CombinedConstraints.values(Map.of(
                    "type",
                    "number",
                    "format",
                    "double",
                    "multipleOf",
                    1e307,
                    "minimum",
                    beyondDoubles.negate(),
                    "maximum",
                    beyondDoubles))) {
                BigDecimal number = assertInstanceOf(BigDecimal.class, value);
                assertTrue(number.abs().compareTo(new BigDecimal(Double.MAX_VALUE)) <= 0, number::toString);
            }
            // Without bounds, a float keeps the default range.
            for (Object value : CombinedConstraints.values(Map.of("type", "number", "format", "float"))) {
                double number = ((Number) value).doubleValue();
                assertTrue(Math.abs(number) <= 1_000_000, String.valueOf(number));
            }
        }
    }

    @Nested
    @DisplayName("count keywords")
    class Counts {

        private static final long BEYOND_AN_INT = 3_000_000_000L;

        private static Object generated(Map<String, Object> property) {
            return SchemaParser.fromJsonSchema(
                            objectSchema(Map.of("value", property)),
                            GeneratorConfig.builder().seed(7L).build())
                    .generate()
                    .get("value");
        }

        private static String rejection(Map<String, Object> property) {
            return assertThrows(
                            IllegalArgumentException.class,
                            () -> SchemaParser.fromJsonSchema(objectSchema(Map.of("value", property))))
                    .getMessage();
        }

        @Test
        @DisplayName("a maximum beyond an int parses and generates, drawing no more than 1,000,000")
        void maximumBeyondAnInt() {
            int length = assertInstanceOf(String.class, generated(Map.of("type", "string", "maxLength", BEYOND_AN_INT)))
                    .length();
            assertTrue(length >= 1 && length <= 1_000_000, String.valueOf(length));
            int size = assertInstanceOf(
                            List.class,
                            generated(Map.of("type", "array", "items", Map.of("const", 1), "maxItems", BEYOND_AN_INT)))
                    .size();
            assertTrue(size >= 1 && size <= 1_000_000, String.valueOf(size));
            assertEquals(
                    Map.of("a", 1),
                    generated(Map.of(
                            "type",
                            "object",
                            "properties",
                            Map.of("a", Map.of("const", 1)),
                            "maxProperties",
                            BEYOND_AN_INT)));
            // A const or enum is measured against it exactly.
            assertEquals(
                    Set.of("abc"), CombinedConstraints.distinct(Map.of("const", "abc", "maxLength", BEYOND_AN_INT)));
            assertEquals(
                    Set.of(List.of(1)),
                    CombinedConstraints.distinct(Map.of("const", List.of(1), "maxItems", BEYOND_AN_INT)));
            assertEquals(
                    Set.of(Map.of("a", 1)),
                    CombinedConstraints.distinct(Map.of("const", Map.of("a", 1), "maxProperties", BEYOND_AN_INT)));
        }

        @Test
        @DisplayName("a minimum no generated value can hold is rejected with the keyword, the value and the path")
        void minimumBeyondTheLimit() {
            String path = " at $.properties.value is more than the 1000000 ";
            assertEquals(
                    "JSON Schema minLength 3000000000" + path + "characters a generated string can hold",
                    rejection(Map.of("type", "string", "minLength", BEYOND_AN_INT)));
            assertEquals(
                    "JSON Schema minItems 3000000000" + path + "items a generated array can hold",
                    rejection(Map.of("type", "array", "minItems", BEYOND_AN_INT)));
            assertEquals(
                    "JSON Schema minProperties 3000000000" + path + "members a generated object can hold",
                    rejection(Map.of("type", "object", "minProperties", BEYOND_AN_INT)));
            // The limit itself parses, and one more does not, whatever generates the string.
            for (Map<String, Object> string : List.<Map<String, Object>>of(
                    Map.of("type", "string"),
                    Map.of("type", "string", "pattern", "^a+$"),
                    Map.of("type", "string", "format", "email"))) {
                Map<String, Object> over = new HashMap<>(string);
                over.put("minLength", 1_000_001);
                assertEquals(
                        "JSON Schema minLength 1000001" + path + "characters a generated string can hold",
                        rejection(over));
            }
            assertDoesNotThrow(() -> SchemaParser.fromJsonSchema(
                    objectSchema(Map.of("value", Map.of("type", "string", "minLength", 1_000_000)))));
            assertDoesNotThrow(() -> SchemaParser.fromJsonSchema(
                    objectSchema(Map.of("value", Map.of("type", "array", "minItems", 1_000_000)))));
            assertDoesNotThrow(() -> SchemaParser.fromJsonSchema(
                    objectSchema(Map.of("value", Map.of("type", "object", "minProperties", 1_000_000)))));
            assertTrue(rejection(Map.of("type", "array", "minItems", 1_000_001)).contains("minItems 1000001"));
            assertTrue(rejection(Map.of("type", "object", "minProperties", 1_000_001))
                    .contains("minProperties 1000001"));
            // A const or enum cannot meet it either.
            CombinedConstraints.assertRejected(Map.of("const", List.of(1), "minItems", BEYOND_AN_INT));
            // The root names the count it cannot meet, as written.
            Map<String, Object> root = new HashMap<>(objectSchema(Map.of("a", Map.of("const", 1))));
            root.put("minProperties", BEYOND_AN_INT);
            String reason = "JSON Schema root object generates exactly its properties, as columns, so its property"
                    + " count, 1, is ";
            assertEquals(
                    reason + "below minProperties 3000000000 at $",
                    assertThrows(IllegalArgumentException.class, () -> SchemaParser.fromJsonSchema(root))
                            .getMessage());
            root.remove("minProperties");
            root.put("maxProperties", 0);
            assertEquals(
                    reason + "above maxProperties 0 at $",
                    assertThrows(IllegalArgumentException.class, () -> SchemaParser.fromJsonSchema(root))
                            .getMessage());
        }

        @Test
        @DisplayName("configured string lengths and collection sizes are held to the same limit")
        void configuredSizesKeepToTheLimit() {
            GeneratorConfig longStrings = GeneratorConfig.builder()
                    .seed(7L)
                    .stringLength(1_000_001, 1_000_001)
                    .build();
            GeneratorConfig bigCollections = GeneratorConfig.builder()
                    .seed(7L)
                    .collectionSize(1_000_001, 1_000_001)
                    .build();
            assertEquals(1_000_000, ((String) generatedWith(Map.of("type", "string"), longStrings)).length());
            assertEquals(
                    1_000_000,
                    ((List<?>) generatedWith(Map.of("type", "array", "items", Map.of("const", 1)), bigCollections))
                            .size());
            // Items without a schema, and the regex format, draw text of the configured length.
            List<?> items = (List<?>) generatedWith(Map.of("type", "array", "minItems", 1, "maxItems", 1), longStrings);
            assertEquals(1_000_000, ((String) items.getFirst()).length());
            assertEquals(
                    1_000_000,
                    ((String) generatedWith(Map.of("type", "string", "format", "regex"), longStrings)).length());
        }

        @Test
        @DisplayName("an object that would hold more than 1,000,000 members is rejected with its path")
        void objectsKeepToTheLimit() {
            List<String> names = new ArrayList<>(1_000_001);
            for (int i = 0; i <= 1_000_000; i++) {
                names.add("p" + i);
            }
            assertEquals(
                    "JSON Schema object at $.properties.value would hold 1000001 properties, more than the 1000000"
                            + " members a generated object can hold",
                    assertTimeoutPreemptively(
                            Duration.ofSeconds(30), () -> rejection(Map.of("type", "object", "required", names))));
            Map<String, Object> columns = new HashMap<>();
            Map<String, Object> column = Map.of("type", "integer");
            for (String name : names) {
                columns.put(name, column);
            }
            assertEquals(
                    "JSON Schema root object generates exactly its properties, as columns, so its property count,"
                            + " 1000001, is above the 1000000 members a generated object can hold at $",
                    assertTimeoutPreemptively(
                                    Duration.ofSeconds(30),
                                    () -> assertThrows(
                                            IllegalArgumentException.class,
                                            () -> SchemaParser.fromJsonSchema(objectSchema(columns))))
                            .getMessage());
        }

        @Test
        @DisplayName("a count with a huge exponent is judged and named without writing out its digits")
        void hugeExponents() {
            BigDecimal huge = new BigDecimal("1E+1000000000");
            assertEquals(
                    "JSON Schema minLength 1E+1000000000 at $.properties.value is more than the 1000000 characters a"
                            + " generated string can hold",
                    assertTimeoutPreemptively(
                            Duration.ofSeconds(10), () -> rejection(Map.of("type", "string", "minLength", huge))));
            assertTimeoutPreemptively(Duration.ofSeconds(10), () -> {
                int length = ((String) generated(Map.of("type", "string", "maxLength", huge))).length();
                assertTrue(length >= 1 && length <= 1_000_000, String.valueOf(length));
                assertEquals(Set.of("abc"), CombinedConstraints.distinct(Map.of("const", "abc", "maxLength", huge)));
            });
            Map<String, Object> root = new HashMap<>(objectSchema(Map.of("a", Map.of("const", 1))));
            root.put("minProperties", huge);
            assertEquals(
                    "JSON Schema root object generates exactly its properties, as columns, so its property count, 1,"
                            + " is below minProperties 1E+1000000000 at $",
                    assertTimeoutPreemptively(
                                    Duration.ofSeconds(10),
                                    () -> assertThrows(
                                            IllegalArgumentException.class, () -> SchemaParser.fromJsonSchema(root)))
                            .getMessage());
        }

        private static Object generatedWith(Map<String, Object> property, GeneratorConfig config) {
            return SchemaParser.fromJsonSchema(objectSchema(Map.of("value", property)), config)
                    .generate()
                    .get("value");
        }

        @Test
        @DisplayName("a count that is negative, fractional or no number is rejected with the path")
        void malformedCounts() {
            for (String keyword : List.of("minLength", "maxLength", "minItems", "maxItems")) {
                for (Object count : List.of(-1, 1.5, "2", Double.NaN)) {
                    CombinedConstraints.assertRejected(Map.of(keyword, count));
                }
            }
            // A whole number written as a decimal is a count.
            for (Object value : CombinedConstraints.values(Map.of("type", "string", "maxLength", 3.0))) {
                assertTrue(((String) value).length() <= 3, String.valueOf(value));
            }
        }
    }

    @Test
    @DisplayName("seeded format providers reproduce their generated record")
    void seededFormatProvidersReproduce() {
        Map<String, Object> properties = new LinkedHashMap<>();
        for (String format : List.of(
                "email",
                "idn-email",
                "uri",
                "uri-reference",
                "iri",
                "iri-reference",
                "uri-template",
                "uuid",
                "date",
                "date-time",
                "time",
                "date-time-local",
                "time-local",
                "duration",
                "ipv4",
                "ipv6",
                "hostname",
                "idn-hostname",
                "json-pointer",
                "relative-json-pointer",
                "regex",
                "byte")) {
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
