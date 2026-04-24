/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.schema;

import org.github.krandom.generator.GeneratorConfig;
import org.github.krandom.generator.provider.ConflictPolicy;
import org.github.krandom.generator.text.WordGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Field")
class FieldTest {

    @Test
    @DisplayName("constructors set locale through config")
    void constructors() {
        assertEquals(Locale.US, new Field().getConfig().getLocale());
        assertEquals(Locale.GERMANY, new Field(Locale.GERMANY).getConfig().getLocale());
        GeneratorConfig config = GeneratorConfig.builder().locale(Locale.FRANCE).seed(5L).build();
        assertEquals(Locale.FRANCE, new Field(config).getConfig().getLocale());
        FieldLookup lookup = new FieldLookup(GeneratorConfig.builder().locale(Locale.CANADA).build());
        assertEquals(Locale.CANADA, new Field(lookup).getConfig().getLocale());
    }

    @Test
    @DisplayName("bind and call resolve references")
    void bindAndCall() {
        Field field = new Field(GeneratorConfig.builder().seed(10L).locale(Locale.US).build());
        SchemaContext ctx = new SchemaContext(Locale.US, new Random(2L), 0);
        assertNotNull(field.bind("person.full_name").generate(ctx));
        assertNotNull(field.call("internet.url").generate(ctx));
        assertTrue(field.supportedReferences().contains("text.word"));
    }

    @Test
    @DisplayName("constant returns the provided value")
    void constant() {
        Field field = new Field();
        SchemaValueProvider provider = field.constant("fixed");
        Object value = provider.generate(new SchemaContext(Locale.US, new Random(1L), 0));
        assertEquals("fixed", value);
        assertEquals("string", provider.jsonSchema().get("type"));
    }

    @Test
    @DisplayName("custom registration and provider-backed references stay fluent")
    void customRegistrationIsFluent() {
        Field field = new Field();
        field.register("custom.order_id", ctx -> "ORD-" + ctx.recordIndex())
            .registerAlias("custom.order", "custom.order_id")
            .registerProvider("text.word.provider", WordGenerator::new, WordGenerator.class, WordGenerator::generateWord)
            .registerProvider("custom.counter",
                              CountingProvider::new,
                              CountingProvider.class,
                              CountingProvider::next,
                              Map.of("type", "string"));

        SchemaContext ctx = new SchemaContext(Locale.US, new Random(1L), 9);
        assertEquals("ORD-9", field.bind("custom.order").generate(ctx));
        assertTrue(field.bind("text.word.provider").generate(ctx) instanceof String);
        assertEquals("string", field.bind("custom.counter").jsonSchema().get("type"));
    }

    @Test
    @DisplayName("explicit conflict-policy overloads stay fluent")
    void explicitConflictPolicyOverloadsStayFluent() {
        Field field = new Field();
        field.register("custom.code", ctx -> "A")
            .register("custom.code", ctx -> "B", ConflictPolicy.REPLACE)
            .registerProvider("text.word.policy", WordGenerator::new, WordGenerator.class, WordGenerator::generateWord,
                              ConflictPolicy.REPLACE)
            .registerProvider("custom.counter.policy",
                              CountingProvider::new,
                              CountingProvider.class,
                              CountingProvider::next,
                              Map.of("type", "string"),
                              ConflictPolicy.REPLACE)
            .registerAlias("custom.code.alias", "custom.code", ConflictPolicy.REPLACE);

        SchemaContext ctx = new SchemaContext(Locale.US, new Random(1L), 0);
        assertEquals("B", field.bind("custom.code.alias").generate(ctx));
        assertTrue(field.bind("text.word.policy").generate(ctx) instanceof String);
        assertEquals("value-0", field.bind("custom.counter.policy").generate(ctx));
    }

    @Test
    @DisplayName("list provider supports fixed and ranged sizes")
    void listProvider() {
        Field field = new Field();
        SchemaContext ctx = new SchemaContext(Locale.US, new Random(11L), 0);

        @SuppressWarnings("unchecked")
        List<Object> fixed = (List<Object>) field.list("text.word", 2, 2).generate(ctx);
        assertEquals(2, fixed.size());
        assertEquals("array", field.list("text.word", 2, 2).jsonSchema().get("type"));

        boolean sawMin = false;
        boolean sawMax = false;
        for (int i = 0; i < 80; i++) {
            @SuppressWarnings("unchecked")
            List<Object> ranged = (List<Object>) field.list(field.constant("x"), 1, 3).generate(ctx);
            sawMin |= ranged.size() == 1;
            sawMax |= ranged.size() == 3;
        }
        assertTrue(sawMin);
        assertTrue(sawMax);
    }

    @Test
    @DisplayName("nested provider builds nested map")
    void nestedProvider() {
        Field field = new Field();
        Map<String, SchemaValueProvider> nested = new LinkedHashMap<>();
        nested.put("city", field.bind("address.city"));
        nested.put("zip", field.bind("address.postal_code"));
        Object value = field.nested(nested).generate(new SchemaContext(Locale.US, new Random(1L), 0));
        assertTrue(value instanceof Map<?, ?>);
        @SuppressWarnings("unchecked")
        Map<String, Object> nestedMap = (Map<String, Object>) value;
        assertTrue(nestedMap.containsKey("city"));
        assertTrue(nestedMap.containsKey("zip"));
        assertEquals("object", field.nested(nested).jsonSchema().get("type"));
    }

    @Test
    @DisplayName("list and nested validation branches")
    void validation() {
        Field field = new Field();
        assertThrows(NullPointerException.class, () -> new Field((GeneratorConfig) null));
        assertThrows(NullPointerException.class, () -> new Field((FieldLookup) null));
        assertThrows(NullPointerException.class, () -> field.list((SchemaValueProvider) null, 1, 1));
        assertThrows(IllegalArgumentException.class, () -> field.list("text.word", -1, 2));
        assertThrows(IllegalArgumentException.class, () -> field.list("text.word", 3, 2));
        assertThrows(NullPointerException.class, () -> field.nested(null));
        assertThrows(IllegalArgumentException.class, () -> field.nested(Map.of()));

        Map<String, SchemaValueProvider> blankName = new LinkedHashMap<>();
        blankName.put(" ", field.constant("x"));
        assertThrows(IllegalArgumentException.class, () -> field.nested(blankName));

        Map<String, SchemaValueProvider> nullProvider = new LinkedHashMap<>();
        nullProvider.put("x", null);
        assertThrows(NullPointerException.class, () -> field.nested(nullProvider));

        assertThrows(IllegalArgumentException.class, () -> field.register("person.full_name", ctx -> "x"));
        assertThrows(IllegalArgumentException.class,
                     () -> field.registerAlias("text.word", "person.full_name", ConflictPolicy.REPLACE));
    }

    private static final class CountingProvider {

        private int next;

        private CountingProvider(GeneratorConfig config) {
            assertNotNull(config);
        }

        private String next() {
            return "value-" + next++;
        }
    }
}
