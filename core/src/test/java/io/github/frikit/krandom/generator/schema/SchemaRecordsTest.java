/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.schema;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.frikit.krandom.generator.GeneratorConfig;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The records of a {@link Schema} and the rows of a {@link SchemaProjection}: the methods added in 3.1 return
 * the same maps as the deprecated ones, from the same record sequence, with each {@code null} kept.
 */
@DisplayName("Schema records")
class SchemaRecordsTest {

    record Person(String name, String nickname) {}

    /** A seeded schema whose records hold their index, a drawn word, and a null. */
    private static Schema indexed(long seed) {
        GeneratorConfig config =
                GeneratorConfig.builder().locale(Locale.US).seed(seed).build();
        Map<String, SchemaValueProvider> fields = new LinkedHashMap<>();
        fields.put("index", SchemaContext::recordIndex);
        fields.put("word", new Field(config).bind("text.word"));
        fields.put("missing", context -> null);
        return new Schema(config, fields);
    }

    @Test
    @DisplayName("a record keeps the null a provider returns under its column")
    void recordKeepsNull() {
        Map<String, Object> record = indexed(1L).nextRecord();

        assertEquals(List.of("index", "word", "missing"), List.copyOf(record.keySet()));
        assertEquals(0, record.get("index"));
        assertTrue(record.containsKey("missing"));
        assertNull(record.get("missing"));
    }

    @Test
    @DisplayName("nextRecords rejects a negative count and returns that many records in order")
    void nextRecordsCount() {
        Schema schema = indexed(2L);

        IllegalArgumentException negative = assertThrows(IllegalArgumentException.class, () -> schema.nextRecords(-1));
        assertEquals("count must be >= 0, got: -1", negative.getMessage());
        assertEquals(List.of(), schema.nextRecords(0));
        List<Map<String, Object>> records = schema.nextRecords(3);
        assertEquals(
                List.of(0, 1, 2),
                records.stream().map(record -> record.get("index")).toList());
        assertTrue(records.stream().allMatch(record -> record.get("missing") == null));
    }

    @Test
    @SuppressWarnings("deprecation")
    @DisplayName("generate, generateBatch and loop return the records that nextRecord and nextRecords do")
    void deprecatedMethodsReturnTheSameRecords() {
        Schema legacy = indexed(7L);
        Schema current = indexed(7L);

        assertEquals(current.nextRecord(), legacy.generate());
        assertEquals(current.nextRecords(3), legacy.generateBatch(3));
        assertEquals(current.nextRecords(2), legacy.loop(2));
        assertTrue(legacy.generate().containsKey("missing"));
        assertNull(legacy.generate().get("missing"));
        assertEquals(
                "count must be >= 0, got: -1",
                assertThrows(IllegalArgumentException.class, () -> legacy.generateBatch(-1))
                        .getMessage());
        assertEquals(
                "count must be >= 0, got: -1",
                assertThrows(IllegalArgumentException.class, () -> legacy.loop(-1))
                        .getMessage());
    }

    @Test
    @SuppressWarnings("deprecation")
    @DisplayName("the deprecated and the new methods draw from one record sequence")
    void oneRecordSequence() {
        Schema mixed = indexed(9L);
        List<Map<String, Object>> drawn = new ArrayList<>();

        drawn.add(mixed.generate());
        drawn.add(mixed.nextRecord());
        drawn.addAll(mixed.generateBatch(1));
        drawn.addAll(mixed.nextRecords(1));
        drawn.addAll(mixed.loop(1));
        drawn.add(mixed.generateList(1).getFirst());

        assertEquals(indexed(9L).nextRecords(6), drawn);
    }

    @Test
    @SuppressWarnings("deprecation")
    @DisplayName("projectRow keeps the null an extractor returns, and project returns the same row")
    void projectionKeepsNull() {
        Map<String, Function<Person, ?>> columns = new LinkedHashMap<>();
        columns.put("name", Person::name);
        columns.put("nickname", Person::nickname);
        SchemaProjection<Person> projection = SchemaProjection.of(columns);
        Person ada = new Person("Ada", null);

        Map<String, Object> row = projection.projectRow(ada);

        assertEquals(List.of("name", "nickname"), List.copyOf(row.keySet()));
        assertEquals("Ada", row.get("name"));
        assertNull(row.get("nickname"));
        assertEquals(row, projection.project(ada));
        assertThrows(UnsupportedOperationException.class, () -> row.put("name", "Grace"));
        assertThrows(NullPointerException.class, () -> projection.projectRow(null));
    }
}
