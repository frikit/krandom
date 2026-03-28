/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.schema;

import org.github.krandom.generator.GeneratorConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
}
