/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.schema;

import io.github.frikit.krandom.generator.GeneratorConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Seeded JSON Schema generation must replay, and explicit schema constraints must win over
 * field-name semantics.
 */
class SchemaParserReplayTest {

    private static final Map<String, Object> MIXED = Map.of(
        "type", "object",
        "properties", Map.of(
            "count", Map.of("type", "integer", "minimum", 0, "maximum", 1_000_000),
            "big", Map.of("type", "integer", "minimum", 0, "maximum", 9_000_000_000L),
            "huge", Map.of("type", "integer", "minimum", 0, "maximum", Long.MAX_VALUE),
            "anyLong", Map.of("type", "integer", "minimum", Long.MIN_VALUE, "maximum", Long.MAX_VALUE),
            "ratio", Map.of("type", "number", "minimum", 0, "maximum", 1),
            "flag", Map.of("type", "boolean"),
            "code", Map.of("type", "string", "pattern", "[A-Z]{4}"),
            "anything", Map.of(),
            "tags", Map.of("type", "array", "minItems", 2, "maxItems", 2)));

    private static List<Map<String, Object>> records(Map<String, Object> jsonSchema, long seed, int count) {
        return SchemaParser.fromJsonSchema(jsonSchema, GeneratorConfig.builder().seed(seed).build())
                           .generateBatch(count);
    }

    private static Map<String, Object> single(String name, Map<String, Object> property) {
        return Map.of("type", "object", "properties", Map.of(name, property));
    }

    @Test
    @DisplayName("two schemas built from the same seed produce the same records")
    void seededSchemasReplay() {
        List<Map<String, Object>> first = records(MIXED, 42L, 5);
        assertEquals(first, records(MIXED, 42L, 5));
        assertNotEquals(first, records(MIXED, 43L, 5));
    }

    @Test
    @DisplayName("exclusiveMinimum excludes the bound even when it is not zero")
    void exclusiveMinimumIsExclusive() {
        Map<String, Object> schema = single("x", Map.of(
            "type", "number", "exclusiveMinimum", 1.0, "maximum", 1.0000000000000004));
        for (Map<String, Object> record : records(schema, 3L, 500)) {
            assertTrue(((Number) record.get("x")).doubleValue() > 1.0, "value must be above 1.0: " + record);
        }
    }

    @Test
    @DisplayName("a number range with equal bounds yields that value")
    void equalNumberBoundsYieldTheValue() {
        Map<String, Object> schema = single("x", Map.of("type", "number", "minimum", 5, "maximum", 5));
        assertEquals(5.0, ((Number) records(schema, 1L, 1).getFirst().get("x")).doubleValue());
    }

    @Test
    @DisplayName("empty number and integer ranges fail while parsing")
    void emptyRangesFailWhileParsing() {
        GeneratorConfig config = GeneratorConfig.builder().seed(1L).build();
        assertThrows(IllegalArgumentException.class, () -> SchemaParser.fromJsonSchema(
            single("x", Map.of("type", "number", "exclusiveMinimum", 1.0, "maximum", 1.0)), config));
        assertThrows(IllegalArgumentException.class, () -> SchemaParser.fromJsonSchema(
            single("x", Map.of("type", "integer", "minimum", 5, "maximum", 4)), config));
        assertThrows(IllegalArgumentException.class, () -> SchemaParser.fromJsonSchema(
            single("x", Map.of("type", "integer", "exclusiveMinimum", Long.MAX_VALUE)), config));
        assertThrows(IllegalArgumentException.class, () -> SchemaParser.fromJsonSchema(
            single("x", Map.of("type", "integer", "exclusiveMaximum", Long.MIN_VALUE)), config));
    }

    @Test
    @DisplayName("columns with the same format or length bounds draw from their own streams")
    void equalColumnsUseTheirOwnStreams() {
        List<Map<String, Object>> properties = new ArrayList<>();
        for (String format : List.of("email", "uri", "uuid", "date", "date-time", "time", "ipv4", "ipv6", "hostname")) {
            properties.add(Map.of("type", "string", "format", format));
        }
        properties.add(Map.of("type", "string", "minLength", 8, "maxLength", 12));
        for (Map<String, Object> property : properties) {
            Map<String, Object> schema = Map.of(
                "type", "object", "properties", Map.of("left", property, "right", property));
            List<Map<String, Object>> batch = records(schema, 11L, 20);
            long repeated = batch.stream().filter(record -> record.get("left").equals(record.get("right"))).count();
            assertTrue(repeated < 3, "columns repeat each other for " + property + ": " + batch);
        }
    }

    @Test
    @DisplayName("format and length-bounded columns replay without depending on other columns")
    void formatColumnsReplayPerField() {
        Map<String, Object> id = Map.of("type", "string", "format", "uuid");
        Map<String, Object> code = Map.of("type", "string", "minLength", 4, "maxLength", 6);
        Map<String, Object> base = Map.of("type", "object", "properties", Map.of("id", id, "code", code));
        Map<String, Object> wider = Map.of("type", "object", "properties", Map.of(
            "id", id, "code", code, "contact", Map.of("type", "string", "format", "email")));
        List<Map<String, Object>> first = records(base, 21L, 10);
        assertEquals(first, records(base, 21L, 10));
        List<Map<String, Object>> withContact = records(wider, 21L, 10);
        for (int i = 0; i < first.size(); i++) {
            assertEquals(first.get(i).get("id"), withContact.get(i).get("id"));
            assertEquals(first.get(i).get("code"), withContact.get(i).get("code"));
        }
    }

    @Test
    @DisplayName("a pattern wins over a semantic field name")
    void patternWinsOverSemanticName() {
        Map<String, Object> schema = single("email", Map.of("type", "string", "pattern", "[A-Z]{3}"));
        for (Map<String, Object> record : records(schema, 5L, 50)) {
            assertTrue(((String) record.get("email")).matches("[A-Z]{3}"), "pattern violated: " + record);
        }
    }

    @Test
    @DisplayName("length bounds win over a semantic field name")
    void lengthBoundsWinOverSemanticName() {
        Map<String, Object> schema = single("firstName", Map.of("type", "string", "minLength", 2, "maxLength", 3));
        for (Map<String, Object> record : records(schema, 6L, 50)) {
            int length = ((String) record.get("firstName")).length();
            assertTrue(length >= 2 && length <= 3, "length bounds violated: " + record);
        }
        Map<String, Object> maxOnly = single("lastName", Map.of("type", "string", "maxLength", 2));
        for (Map<String, Object> record : records(maxOnly, 6L, 50)) {
            assertTrue(((String) record.get("lastName")).length() <= 2, "maxLength violated: " + record);
        }
    }
}
