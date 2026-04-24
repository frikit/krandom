/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.schema;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("JsonSchemaSupport")
class JsonSchemaSupportTest {

    @Test
    @DisplayName("infers scalar and structured samples")
    void inferScalarAndStructuredSamples() {
        assertEquals("string", JsonSchemaSupport.infer('A').get("type"));
        assertEquals("boolean", JsonSchemaSupport.infer(false).get("type"));
        assertEquals("integer", JsonSchemaSupport.infer(BigInteger.TEN).get("type"));
        assertEquals("number", JsonSchemaSupport.infer(BigDecimal.TEN).get("type"));
        assertEquals("array", JsonSchemaSupport.infer(new int[] { 1, 2 }).get("type"));
        assertEquals("object", JsonSchemaSupport.infer(new SampleRecord("Ada", 7)).get("type"));
        assertEquals("object", JsonSchemaSupport.infer(Map.of("record", new SampleRecord("Ada", 7))).get("type"));
        assertEquals("string", JsonSchemaSupport.infer(new PlainValue()).get("type"));
    }

    @Test
    @DisplayName("record schemas cover Java scalar, temporal, array, nested, and fallback types")
    void recordSchemaCoversTypeBranches() {
        @SuppressWarnings("unchecked")
        Map<String, Object> properties = (Map<String, Object>) JsonSchemaSupport.record(TypeRecord.class)
                                                                                .get("properties");

        assertEquals("string", ((Map<?, ?>) properties.get("text")).get("type"));
        assertEquals("string", ((Map<?, ?>) properties.get("boxedCharacter")).get("type"));
        assertEquals("string", ((Map<?, ?>) properties.get("primitiveCharacter")).get("type"));
        assertEquals("boolean", ((Map<?, ?>) properties.get("primitiveBoolean")).get("type"));
        assertEquals("boolean", ((Map<?, ?>) properties.get("boxedBoolean")).get("type"));
        assertEquals("integer", ((Map<?, ?>) properties.get("primitiveByte")).get("type"));
        assertEquals("integer", ((Map<?, ?>) properties.get("primitiveShort")).get("type"));
        assertEquals("integer", ((Map<?, ?>) properties.get("primitiveInteger")).get("type"));
        assertEquals("integer", ((Map<?, ?>) properties.get("primitiveLong")).get("type"));
        assertEquals("integer", ((Map<?, ?>) properties.get("boxedByte")).get("type"));
        assertEquals("integer", ((Map<?, ?>) properties.get("boxedShort")).get("type"));
        assertEquals("integer", ((Map<?, ?>) properties.get("boxedInteger")).get("type"));
        assertEquals("integer", ((Map<?, ?>) properties.get("boxedLong")).get("type"));
        assertEquals("integer", ((Map<?, ?>) properties.get("bigInteger")).get("type"));
        assertEquals("number", ((Map<?, ?>) properties.get("primitiveFloat")).get("type"));
        assertEquals("number", ((Map<?, ?>) properties.get("primitiveDouble")).get("type"));
        assertEquals("number", ((Map<?, ?>) properties.get("boxedFloat")).get("type"));
        assertEquals("number", ((Map<?, ?>) properties.get("boxedDouble")).get("type"));
        assertEquals("number", ((Map<?, ?>) properties.get("decimal")).get("type"));
        assertEquals("date", ((Map<?, ?>) properties.get("date")).get("format"));
        assertEquals("time", ((Map<?, ?>) properties.get("time")).get("format"));
        assertEquals("date-time", ((Map<?, ?>) properties.get("localDateTime")).get("format"));
        assertEquals("date-time", ((Map<?, ?>) properties.get("offsetDateTime")).get("format"));
        assertEquals("date-time", ((Map<?, ?>) properties.get("zonedDateTime")).get("format"));
        assertEquals("array", ((Map<?, ?>) properties.get("tags")).get("type"));
        assertEquals("object", ((Map<?, ?>) properties.get("nested")).get("type"));
        assertEquals(Map.of(), properties.get("unknown"));
    }

    @Test
    @DisplayName("nullable schemas preserve metadata and record required properties")
    void nullableSchemasPreserveMetadataAndRequiredProperties() {
        Map<String, Object> nullableString = JsonSchemaSupport.nullable(JsonSchemaSupport.stringFormat("date"));
        assertEquals(List.of("string", "null"), nullableString.get("type"));
        assertEquals("date", nullableString.get("format"));

        Map<String, Object> alreadyNullable = JsonSchemaSupport.nullable(Map.of("type", List.of("string", "null")));
        assertEquals(List.of("string", "null"), alreadyNullable.get("type"));

        Map<String, Object> composite = JsonSchemaSupport.nullable(Map.of("oneOf", List.of(Map.of("type", "string"))));
        assertEquals("null", ((Map<?, ?>) ((List<?>) composite.get("oneOf")).get(1)).get("type"));

        assertEquals(Map.of(), JsonSchemaSupport.nullable(Map.of()));
        assertThrows(NullPointerException.class, () -> JsonSchemaSupport.record(NullableRecord.class, null));

        @SuppressWarnings("unchecked")
        Map<String, Object> properties = (Map<String, Object>) JsonSchemaSupport.record(NullableRecord.class,
                                                                                        Set.of("settledOn"))
                                                                                .get("properties");
        assertEquals(List.of("string", "null"), ((Map<?, ?>) properties.get("settledOn")).get("type"));
        assertEquals("date", ((Map<?, ?>) properties.get("settledOn")).get("format"));
        assertEquals("string", ((Map<?, ?>) properties.get("requiredText")).get("type"));
    }

    @Test
    @DisplayName("copy and record validation reject invalid schema inputs")
    void validation() {
        assertThrows(IllegalArgumentException.class, () -> JsonSchemaSupport.record(String.class));

        Map<String, Object> invalidNestedKey = new LinkedHashMap<>();
        invalidNestedKey.put("properties", Map.of(1, Map.of("type", "string")));

        assertThrows(IllegalArgumentException.class, () -> JsonSchemaSupport.copyJsonSchema(invalidNestedKey));
        assertEquals(List.of("required"), JsonSchemaSupport.copyJsonSchema(Map.of("required", List.of("required")))
                                                         .get("required"));
    }

    private record SampleRecord(String name, int count) {
    }

    private record NestedRecord(String value) {
    }

    private record NullableRecord(LocalDate settledOn, String requiredText) {
    }

    private record TypeRecord(
        String text,
        Character boxedCharacter,
        char primitiveCharacter,
        boolean primitiveBoolean,
        Boolean boxedBoolean,
        byte primitiveByte,
        short primitiveShort,
        int primitiveInteger,
        long primitiveLong,
        Byte boxedByte,
        Short boxedShort,
        Integer boxedInteger,
        Long boxedLong,
        BigInteger bigInteger,
        float primitiveFloat,
        double primitiveDouble,
        Float boxedFloat,
        Double boxedDouble,
        BigDecimal decimal,
        LocalDate date,
        LocalTime time,
        LocalDateTime localDateTime,
        OffsetDateTime offsetDateTime,
        ZonedDateTime zonedDateTime,
        String[] tags,
        NestedRecord nested,
        PlainValue unknown
    ) {
    }

    private static final class PlainValue {
    }
}
