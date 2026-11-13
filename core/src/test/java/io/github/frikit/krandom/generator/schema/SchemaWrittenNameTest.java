/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.schema;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * A field name is written with each unpaired surrogate as U+FFFD, in the JSON Schema of {@link Schema} as in
 * the data that every writer writes, so two names that differ only in their unpaired surrogates would be one.
 */
@DisplayName("Field names as written")
class SchemaWrittenNameTest {

    /** U+1F600, a supplementary character, which UTF-16 writes as a surrogate pair. */
    private static final String PAIR = "😀";

    /** The message names each unpaired surrogate as an escape, so that the message itself can be encoded. */
    private static final String MESSAGE = "field names \"a\\ud800\" and \"a\\udc00\" are both written as"
            + " \"a\uFFFD\", as every writer writes an unpaired surrogate as U+FFFD";

    private static Map<String, SchemaValueProvider> fields(String... names) {
        Map<String, SchemaValueProvider> fields = new LinkedHashMap<>();
        for (String name : names) {
            fields.put(name, SchemaValueProvider.withSample(ctx -> 1, 1));
        }
        return fields;
    }

    @Test
    @DisplayName("toJsonSchema writes an unpaired surrogate in a field name as U+FFFD and keeps a pair")
    void toJsonSchemaWritesFieldNamesAsTheDataWritersDo() {
        Schema schema = new Schema(fields("high\uD800", "low\uDC00", "reversed\uDC00\uD800", "pair" + PAIR, "plain"));

        Map<String, Object> jsonSchema = schema.toJsonSchema();

        List<String> written = List.of("high�", "low�", "reversed��", "pair" + PAIR, "plain");
        assertEquals(written, List.copyOf(((Map<?, ?>) jsonSchema.get("properties")).keySet()));
        assertEquals(written, jsonSchema.get("required"));
        assertEquals(Map.of("type", "integer"), ((Map<?, ?>) jsonSchema.get("properties")).get("high�"));
        // The written records hold the same names.
        assertEquals(
                "{\"high�\":1,\"low�\":1,\"reversed��\":1,\"pair" + PAIR + "\":1,\"plain\":1}\n",
                schema.toJsonLines(1));
    }

    @Test
    @DisplayName("toJsonSchema of a parsed root enum writes the column's name as U+FFFD and keeps its values")
    void toJsonSchemaOfARootEnumWritesTheColumnName() {
        Schema schema = SchemaParser.fromJsonSchema(Map.of(
                "type",
                "object",
                "properties",
                Map.of("x\uD800", Map.of("type", "integer")),
                "enum",
                List.of(Map.of("x\uD800", 7))));

        Map<?, ?> properties = (Map<?, ?>) schema.toJsonSchema().get("properties");

        assertEquals(List.of("x�"), List.copyOf(properties.keySet()));
        assertEquals(List.of(7), ((Map<?, ?>) properties.get("x�")).get("enum"));
    }

    @Test
    @DisplayName("two field names that differ only in unpaired surrogates are rejected")
    void namesWrittenAsOneAreRejected() {
        IllegalArgumentException schema =
                assertThrows(IllegalArgumentException.class, () -> new Schema(fields("a\uD800", "a\uDC00")));
        assertEquals(MESSAGE, schema.getMessage());

        Map<String, Function<Object, ?>> projected = new LinkedHashMap<>();
        projected.put("a\uD800", Function.identity());
        projected.put("a\uDC00", Function.identity());
        IllegalArgumentException projection =
                assertThrows(IllegalArgumentException.class, () -> SchemaProjection.of(projected));
        assertEquals(MESSAGE, projection.getMessage());

        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("a\uD800", Map.of("type", "integer"));
        properties.put("a\uDC00", Map.of("type", "integer"));
        IllegalArgumentException parsed = assertThrows(
                IllegalArgumentException.class,
                () -> SchemaParser.fromJsonSchema(Map.of("type", "object", "properties", properties)));
        assertEquals(MESSAGE, parsed.getMessage());

        // A name written as U+FFFD itself collides with one that holds an unpaired surrogate there.
        IllegalArgumentException replacement =
                assertThrows(IllegalArgumentException.class, () -> new Schema(fields("b\uFFFD", "b\uDBFF")));
        assertEquals(
                "field names \"b\uFFFD\" and \"b\\udbff\" are both written as \"b\uFFFD\", as every writer writes"
                        + " an unpaired surrogate as U+FFFD",
                replacement.getMessage());
    }

    @Test
    @DisplayName("names that stay distinct as written are kept, also when they differ in a surrogate pair")
    void namesThatStayDistinctAreKept() {
        Schema schema = new Schema(fields("a\uD800", "a" + PAIR, "a\uD800\uD800", "a"));

        assertEquals("{\"a�\":1,\"a" + PAIR + "\":1,\"a��\":1,\"a\":1}\n", schema.toJsonLines(1));
        assertEquals(4, ((Map<?, ?>) schema.toJsonSchema().get("properties")).size());
    }
}
