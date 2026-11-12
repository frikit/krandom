/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.nullness;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.Generators;
import io.github.frikit.krandom.generator.schema.Schema;
import io.github.frikit.krandom.generator.schema.SchemaParser;
import io.github.frikit.krandom.generator.schema.SchemaProjection;
import io.github.frikit.krandom.generator.schema.SchemaValueProvider;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Null-marked consumer code over schema records and projected rows, whose values are {@code null} where a
 * provider or extractor returns {@code null}. NullAway checks this source, so it compiles only while the API
 * types those values as nullable, and each test asserts that the {@code null} reaches the caller.
 */
@NullMarked
@DisplayName("Schema records and projected rows have nullable values")
class NullableSchemaRecordsTest {

    record Profile(String name, @Nullable String nickname) {}

    private static Schema profiles() {
        Map<String, SchemaValueProvider> fields = new LinkedHashMap<>();
        fields.put("name", context -> "Ada");
        fields.put("nickname", context -> null);
        return Generators.ofSchema(GeneratorConfig.builder().seed(42L).build(), fields);
    }

    @Test
    @DisplayName("generateRecord and generateRecords keep the null a provider returns")
    void generatedRecordsKeepNull() {
        Schema schema = profiles();
        Map<String, @Nullable Object> record = schema.generateRecord();
        List<Map<String, @Nullable Object>> records = schema.generateRecords(2);

        assertEquals("Ada", record.get("name"));
        assertTrue(record.containsKey("nickname"));
        assertNull(record.get("nickname"));
        assertEquals(2, records.size());
        for (Map<String, @Nullable Object> each : records) {
            assertTrue(each.containsKey("nickname"));
            assertNull(each.get("nickname"));
        }
    }

    @Test
    @DisplayName("a parsed schema's null property is a null value of its record")
    void parsedNullPropertyIsNull() {
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("name", Map.of("const", "Ada"));
        properties.put("gone", Map.of("type", "null"));
        Map<String, Object> jsonSchema = new LinkedHashMap<>();
        jsonSchema.put("type", "object");
        jsonSchema.put("properties", properties);

        Map<String, @Nullable Object> record =
                SchemaParser.fromJsonSchema(jsonSchema).generateRecord();

        assertEquals("Ada", record.get("name"));
        assertTrue(record.containsKey("gone"));
        assertNull(record.get("gone"));
    }

    @Test
    @DisplayName("projectRecord keeps the null an extractor returns")
    void projectedRowKeepsNull() {
        Map<String, Function<Profile, @Nullable Object>> columns = new LinkedHashMap<>();
        columns.put("name", Profile::name);
        columns.put("nickname", Profile::nickname);
        SchemaProjection<Profile> projection = SchemaProjection.of(columns);

        Map<String, @Nullable Object> row = projection.projectRecord(new Profile("Ada", null));

        assertEquals("Ada", row.get("name"));
        assertTrue(row.containsKey("nickname"));
        assertNull(row.get("nickname"));
    }
}
