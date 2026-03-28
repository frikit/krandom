/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.text;

import org.github.krandom.generator.GeneratorConfig;
import org.github.krandom.generator.Generators;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("TemplateStringGenerator")
class TemplateStringGeneratorTest {

    @Test
    @DisplayName("generate applies bothify to configured template")
    void generateAppliesBothify() {
        TemplateStringGenerator gen = new TemplateStringGenerator("???-###", 42L);
        String value = gen.generate();
        assertTrue(value.matches("[a-z]{3}-\\d{3}"));
    }

    @Test
    @DisplayName("numerify replaces only hash placeholders")
    void numerifyReplacesHashesOnly() {
        TemplateStringGenerator gen = new TemplateStringGenerator("ABC", 1L);
        String value = gen.numerify("##-??-X");
        assertTrue(value.matches("\\d{2}-\\?\\?-X"));
    }

    @Test
    @DisplayName("letterify replaces only question placeholders")
    void letterifyReplacesQuestionsOnly() {
        TemplateStringGenerator gen = new TemplateStringGenerator("ABC", 1L);
        String value = gen.letterify("##-??-X");
        assertTrue(value.matches("##-[a-z]{2}-X"));
    }

    @Test
    @DisplayName("letterify uppercase mode emits uppercase letters")
    void letterifyUppercaseMode() {
        TemplateStringGenerator gen = new TemplateStringGenerator("ABC", 2L);
        String value = gen.letterify("??", true);
        assertTrue(value.matches("[A-Z]{2}"));
    }

    @Test
    @DisplayName("bothify replaces both placeholder types")
    void bothifyReplacesBoth() {
        TemplateStringGenerator gen = new TemplateStringGenerator("ABC", 3L);
        String value = gen.bothify("??-##");
        assertTrue(value.matches("[a-z]{2}-\\d{2}"));
    }

    @Test
    @DisplayName("seeded generation is deterministic")
    void seededDeterminism() {
        TemplateStringGenerator a = new TemplateStringGenerator("??##", 99L);
        TemplateStringGenerator b = new TemplateStringGenerator("??##", 99L);
        assertEquals(a.generate(), b.generate());
        assertEquals(a.generate(), b.generate());
    }

    @Test
    @DisplayName("config seed constructor is deterministic")
    void configSeedDeterminism() {
        GeneratorConfig cfg = GeneratorConfig.builder().seed(77L).build();
        TemplateStringGenerator a = new TemplateStringGenerator("?#?#", cfg);
        TemplateStringGenerator b = new TemplateStringGenerator("?#?#", cfg);
        assertEquals(a.generate(), b.generate());
    }

    @Test
    @DisplayName("null input validation")
    void nullValidation() {
        assertThrows(NullPointerException.class, () -> new TemplateStringGenerator(null));
        assertThrows(NullPointerException.class, () -> new TemplateStringGenerator("##", (GeneratorConfig) null));

        TemplateStringGenerator gen = new TemplateStringGenerator("##");
        assertThrows(NullPointerException.class, () -> gen.numerify(null));
        assertThrows(NullPointerException.class, () -> gen.letterify(null));
        assertThrows(NullPointerException.class, () -> gen.bothify(null));
    }

    @Test
    @DisplayName("Generators factory methods expose template generator")
    void generatorsFactoryMethods() {
        assertNotNull(Generators.ofTemplate("??##").generate());
        assertNotNull(Generators.ofTemplate("??##", 10L).generate());
    }
}
