/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.text;

import io.github.frikit.krandom.generator.GeneratorConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("ParagraphGenerator")
class ParagraphGeneratorTest {

    @Test
    @DisplayName("constructors validate config")
    void constructors() {
        assertThrows(NullPointerException.class, () -> new ParagraphGenerator(null));
        ParagraphGenerator gen = new ParagraphGenerator();
        assertNotNull(gen.generate());
        assertEquals(Locale.US, gen.getLocale());
    }

    @Test
    @DisplayName("generate() produces paragraph with 3-7 sentences")
    void generateDefaultRange() {
        ParagraphGenerator gen = new ParagraphGenerator(GeneratorConfig.builder().seed(3L).build());
        String paragraph = gen.generate();
        int dots = paragraph.length() - paragraph.replace(".", "").length();
        assertTrue(dots >= 3 && dots <= 7);
    }

    @Test
    @DisplayName("generate(sentences) validates input")
    void generateSentencesValidation() {
        ParagraphGenerator gen = new ParagraphGenerator();
        assertThrows(IllegalArgumentException.class, () -> gen.generate(0));
        assertThrows(IllegalArgumentException.class, () -> gen.generate(-1));
    }

    @Test
    @DisplayName("generate(sentences) respects exact count")
    void generateSentencesCount() {
        ParagraphGenerator gen = new ParagraphGenerator(GeneratorConfig.builder().seed(5L).build());
        String paragraph = gen.generate(4);
        int dots = paragraph.length() - paragraph.replace(".", "").length();
        assertEquals(4, dots);
    }

    @Test
    @DisplayName("generateParagraphs(count/range) supports range-style paragraph batches")
    void generateParagraphsRangeStyle() {
        ParagraphGenerator gen = new ParagraphGenerator(GeneratorConfig.builder().seed(27L).build());
        String fixed = gen.generateParagraphs(2);
        assertEquals(2, fixed.split("\\n\\n").length);

        for (int i = 0; i < 20; i++) {
            String ranged = gen.generateParagraphs(2, 4);
            int paragraphs = ranged.split("\\n\\n").length;
            assertTrue(paragraphs >= 2 && paragraphs <= 4);
        }

        assertThrows(IllegalArgumentException.class, () -> gen.generateParagraphs(0));
        assertThrows(IllegalArgumentException.class, () -> gen.generateParagraphs(0, 2));
        assertThrows(IllegalArgumentException.class, () -> gen.generateParagraphs(5, 4));
    }

    @Test
    @DisplayName("generate(options) supports sentence count option")
    void generateOptions() {
        ParagraphGenerator gen = new ParagraphGenerator(GeneratorConfig.builder().seed(9L).build());
        String fixed = gen.generate(ParagraphGenerator.ParagraphOptions.withSentences(2));
        int dots = fixed.length() - fixed.replace(".", "").length();
        assertEquals(2, dots);
        assertNotNull(gen.generate(new ParagraphGenerator.ParagraphOptions(null)));
    }

    @Test
    @DisplayName("generate(options) validates null")
    void generateOptionsNullValidation() {
        ParagraphGenerator gen = new ParagraphGenerator();
        assertThrows(NullPointerException.class, () -> gen.generate((ParagraphGenerator.ParagraphOptions) null));
    }

    @Test
    @DisplayName("ParagraphOptions validates values")
    void optionsValidation() {
        assertThrows(IllegalArgumentException.class, () -> new ParagraphGenerator.ParagraphOptions(0));
    }

    @Test
    @DisplayName("seeded generators are reproducible")
    void seededReproducibility() {
        GeneratorConfig cfg = GeneratorConfig.builder().seed(33L).locale(Locale.of("es", "ES")).build();
        ParagraphGenerator a = new ParagraphGenerator(cfg);
        ParagraphGenerator b = new ParagraphGenerator(cfg);
        for (int i = 0; i < 20; i++) {
            assertEquals(a.generate(), b.generate());
            assertEquals(a.generate(2), b.generate(2));
        }
    }
}
