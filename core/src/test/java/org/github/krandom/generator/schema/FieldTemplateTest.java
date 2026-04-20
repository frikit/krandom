/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.schema;

import org.github.krandom.generator.GeneratorConfig;
import org.github.krandom.generator.text.WordGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Field templates")
class FieldTemplateTest {

    @Test
    @DisplayName("string templates resolve registry tokens and literal placeholders")
    void stringTemplatesResolveRegistryTokensAndLiteralPlaceholders() {
        Field field = baseField();
        SchemaValueProvider provider = field.template("ORD-## for {{custom.customer.alias}} ref {{custom.order_id}} code ??");

        String value = (String) provider.generate(context(7));

        assertTrue(value.matches("ORD-\\d{2} for Ada ref 1007 code [a-z]{2}"));
    }

    @Test
    @DisplayName("payload templates keep exact-placeholder values typed")
    void payloadTemplatesKeepExactPlaceholderValuesTyped() {
        Field field = baseField();
        Map<String, Object> shell = new LinkedHashMap<>();
        shell.put("id", "{{ custom.order_id }}");
        shell.put("customer", "{{custom.customer}}");
        shell.put("meta", "{{custom.meta}}");
        shell.put("label", "ORD-##");
        shell.put("items", List.of("{{custom.customer}}", "{{custom.order_id}}", Map.of("word", "{{text.word.provider}}")));
        shell.put("codes", new Object[]{ "{{custom.order_id}}", "SKU-##" });

        Object generated = field.template(shell).generate(context(4));

        assertInstanceOf(Map.class, generated);
        @SuppressWarnings("unchecked")
        Map<String, Object> payload = (Map<String, Object>) generated;
        assertEquals(1004, payload.get("id"));
        assertEquals("Ada", payload.get("customer"));
        assertEquals(Map.of("tier", "gold"), payload.get("meta"));
        assertTrue(((String) payload.get("label")).matches("ORD-\\d{2}"));

        @SuppressWarnings("unchecked")
        List<Object> items = (List<Object>) payload.get("items");
        assertEquals("Ada", items.get(0));
        assertEquals(1004, items.get(1));
        assertInstanceOf(Map.class, items.get(2));

        Object[] codes = (Object[]) payload.get("codes");
        assertEquals(1004, codes[0]);
        assertTrue(((String) codes[1]).matches("SKU-\\d{2}"));
    }

    @Test
    @DisplayName("payload templates preserve nested nulls and scalar literals")
    void payloadTemplatesPreserveNestedNullsAndScalarLiterals() {
        Field field = baseField();
        Map<String, Object> shell = new LinkedHashMap<>();
        shell.put("plainNumber", 7);
        shell.put("plainBoolean", true);
        shell.put("missing", null);
        shell.put("items", new java.util.ArrayList<>(java.util.Arrays.asList(null, 3, "{{custom.order_id}}")));

        @SuppressWarnings("unchecked")
        Map<String, Object> generated = (Map<String, Object>) field.template(shell).generate(context(2));

        assertEquals(7, generated.get("plainNumber"));
        assertEquals(true, generated.get("plainBoolean"));
        assertEquals(null, generated.get("missing"));
        assertEquals(java.util.Arrays.asList(null, 3, 1002), generated.get("items"));
    }

    @Test
    @DisplayName("provider-backed tokens can be interpolated through templates")
    void providerBackedTokensCanBeInterpolatedThroughTemplates() {
        Field field = baseField();

        String a = (String) field.template("{{text.word.provider}}").generate(context(0));
        String b = (String) field.template("{{text.word.provider}}").generate(context(1));

        assertTrue(a.matches("[a-z]+"));
        assertTrue(b.matches("[a-z]+"));
        assertEquals(a, b);
    }

    @Test
    @DisplayName("template resolution fails for unknown tokens")
    void templateResolutionFailsForUnknownTokens() {
        Field field = baseField();

        IllegalArgumentException stringEx = assertThrows(IllegalArgumentException.class,
                                                         () -> field.template("{{missing.token}}").generate(context(0)));
        assertTrue(stringEx.getMessage().contains("Unknown field reference"));

        IllegalArgumentException payloadEx = assertThrows(IllegalArgumentException.class,
                                                          () -> field.template(Map.of("id", "{{missing.token}}"))
                                                                      .generate(context(0)));
        assertTrue(payloadEx.getMessage().contains("Unknown field reference"));
    }

    @Test
    @DisplayName("template overloads validate null inputs")
    void templateOverloadsValidateNullInputs() {
        Field field = baseField();

        assertThrows(NullPointerException.class, () -> field.template((String) null));
        assertThrows(NullPointerException.class, () -> field.template((Object) null));
    }

    private static Field baseField() {
        return new Field(GeneratorConfig.builder().locale(Locale.US).seed(42L).build())
            .register("custom.customer", ctx -> "Ada")
            .register("custom.order_id", ctx -> 1000 + ctx.recordIndex())
            .register("custom.meta", ctx -> Map.of("tier", "gold"))
            .registerAlias("custom.customer.alias", "custom.customer")
            .registerProvider("text.word.provider", WordGenerator::new, WordGenerator.class, WordGenerator::generateWord);
    }

    private static SchemaContext context(int recordIndex) {
        return new SchemaContext(Locale.US, new Random(123L), recordIndex);
    }
}
