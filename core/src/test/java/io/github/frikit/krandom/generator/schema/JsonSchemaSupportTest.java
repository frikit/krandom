/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.schema;

import io.github.frikit.krandom.generator.failure.GenerationFailureCategory;
import io.github.frikit.krandom.generator.failure.GenerationOperation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
    @DisplayName("record schemas retain recursive generic type metadata")
    void recordSchemaRetainsRecursiveGenericMetadata() {
        Map<String, Object> schema = JsonSchemaSupport.record(GenericTypeRecord.class);

        assertEquals("string", nestedSchema(schema, "properties", "nestedLists", "items", "items").get("type"));
        assertEquals(
            "integer",
            nestedSchema(schema, "properties", "mapped", "additionalProperties", "items").get("type"));
        assertEquals(
            List.of("string", "null"),
            nestedSchema(schema, "properties", "optionalDate").get("type"));
        assertEquals("date", nestedSchema(schema, "properties", "optionalDate").get("format"));
        assertEquals(
            List.of("string", "null"),
            nestedSchema(schema, "properties", "optionalState").get("type"));
        assertEquals(
            Arrays.asList("ACTIVE", "PAUSED", null),
            nestedSchema(schema, "properties", "optionalState").get("enum"));
        assertEquals(
            "string",
            nestedSchema(schema, "properties", "payload", "properties", "value").get("type"));
        assertEquals(
            "integer",
            nestedSchema(schema, "properties", "payloads", "items", "properties", "value").get("type"));
        assertEquals(
            "string",
            nestedSchema(schema, "properties", "genericArray", "items", "items").get("type"));
        assertEquals("number", nestedSchema(schema, "properties", "upper", "items").get("type"));
        assertEquals("integer", nestedSchema(schema, "properties", "lower", "items").get("type"));
        assertEquals(Map.of(), nestedSchema(schema, "properties", "unbounded", "items"));
        assertEquals("string", nestedSchema(schema, "properties", "state").get("type"));
        assertEquals(List.of("ACTIVE", "PAUSED"), nestedSchema(schema, "properties", "state").get("enum"));
        assertEquals("null", nestedSchema(schema, "properties", "emptyState").get("type"));
        assertEquals("string", nestedSchema(schema, "properties", "fixedList", "items").get("type"));
        assertEquals(
            "integer",
            nestedSchema(schema, "properties", "fixedMap", "additionalProperties").get("type"));
        assertEquals(Map.of(), nestedSchema(JsonSchemaSupport.record(RecursiveRecord.class),
                                             "properties", "next"));
        assertEquals("number", nestedSchema(JsonSchemaSupport.record(BoundedPayload.class),
                                             "properties", "value").get("type"));
        assertEquals(Map.of(), nestedSchema(JsonSchemaSupport.record(GenericPayload.class),
                                             "properties", "value"));
        assertEquals(Map.of(), nestedSchema(JsonSchemaSupport.record(IntersectionPayload.class),
                                             "properties", "value"));
        Map<String, Object> recursiveBoundSchema = JsonSchemaSupport.record(RecursiveBound.class);
        assertEquals("array", nestedSchema(recursiveBoundSchema, "properties", "value").get("type"));
        assertEquals("array", nestedSchema(recursiveBoundSchema, "properties", "value", "items").get("type"));
        assertEquals(Map.of(), nestedSchema(recursiveBoundSchema, "properties", "value", "items", "items"));

        Map<String, Object> rawSchema = JsonSchemaSupport.record(RawTypeRecord.class);
        assertEquals(Map.of(), nestedSchema(rawSchema, "properties", "optional"));
        assertEquals(Map.of(), nestedSchema(rawSchema, "properties", "list", "items"));
        assertEquals(Map.of(), nestedSchema(rawSchema, "properties", "map", "additionalProperties"));
        assertEquals(Map.of(), nestedSchema(rawSchema, "properties", "comparable"));
    }

    @Test
    @DisplayName("sample inference unwraps optional values")
    void sampleInferenceUnwrapsOptionals() {
        assertEquals("integer", JsonSchemaSupport.infer(Optional.of(7)).get("type"));
        assertEquals("null", JsonSchemaSupport.infer(Optional.empty()).get("type"));
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
        assertEquals(Map.of("type", "null"), JsonSchemaSupport.nullable(JsonSchemaSupport.nullType()));
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
    @DisplayName("infer wraps reflective failures from record accessors")
    void inferWrapsRecordAccessorFailure() {
        SchemaGenerationException ex = assertThrows(
            SchemaGenerationException.class,
            () -> JsonSchemaSupport.infer(new EnvelopeRecord(new ThrowingRecord("payload"))));
        var context = ex.getContext().orElseThrow();
        assertEquals(GenerationFailureCategory.REFLECTION, context.category());
        assertEquals(GenerationOperation.READ, context.operation());
        assertEquals("nested.value", context.path());
        assertEquals(ThrowingRecord.class, context.ownerType());
        assertEquals(String.class.getTypeName(), context.declaredType());
        assertEquals(-1, context.recordIndex());
        assertTrue(ex.getCause() instanceof IllegalStateException);
        assertFalse(ex.getMessage().contains("accessor failure"));
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

        Type unknown = new Type() {
            @Override
            public String getTypeName() {
                return "unknown";
            }
        };
        assertEquals(Map.of(), JsonSchemaSupport.fromType(unknown));
        assertThrows(NullPointerException.class, () -> JsonSchemaSupport.fromType(null));
    }

    private record SampleRecord(String name, int count) {
    }

    private record ThrowingRecord(String value) {
        @Override
        public String value() {
            throw new IllegalStateException("accessor failure");
        }
    }

    private record EnvelopeRecord(ThrowingRecord nested) {
    }

    private record NestedRecord(String value) {
    }

    private record NullableRecord(LocalDate settledOn, String requiredText) {
    }

    private record GenericPayload<T>(T value, List<T> values) {
    }

    private record GenericTypeRecord(
        List<List<String>> nestedLists,
        Map<String, List<Integer>> mapped,
        Optional<LocalDate> optionalDate,
        Optional<State> optionalState,
        GenericPayload<String> payload,
        List<GenericPayload<Integer>> payloads,
        List<String>[] genericArray,
        List<? extends Number> upper,
        List<? super Integer> lower,
        List<?> unbounded,
        State state,
        EmptyState emptyState,
        FixedStringList fixedList,
        FixedStringIntegerMap fixedMap
    ) {
    }

    private record RecursiveRecord(String value, RecursiveRecord next) {
    }

    private enum State {
        ACTIVE,
        PAUSED
    }

    private enum EmptyState {
    }

    private record BoundedPayload<T extends Number>(T value) {
    }

    private record IntersectionPayload<T extends Number & Comparable<T>>(T value) {
    }

    private record RecursiveBound<T extends List<T>>(T value) {
    }

    @SuppressWarnings("rawtypes")
    private record RawTypeRecord(Optional optional, List list, Map map, Comparable<String> comparable) {
    }

    private static final class FixedStringList extends ArrayList<String> {
    }

    private static final class FixedStringIntegerMap extends LinkedHashMap<String, Integer> {
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

    @SuppressWarnings("unchecked")
    private static Map<String, Object> nestedSchema(Map<String, Object> schema, String... path) {
        Object current = schema;
        for (String segment : path) {
            current = ((Map<String, Object>) current).get(segment);
        }
        return (Map<String, Object>) current;
    }
}
