/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.file;

import org.github.krandom.generator.GeneratorConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FileNameGeneratorTest {

    @Test
    void generateDefault() {
        String name = new FileNameGenerator().generate();
        assertNotNull(name);
        assertFalse(name.isBlank());
    }

    @Test
    void seededReproducibility() {
        FileNameGenerator a = new FileNameGenerator(GeneratorConfig.builder().seed(5L).build());
        FileNameGenerator b = new FileNameGenerator(GeneratorConfig.builder().seed(5L).build());
        for (int i = 0; i < 20; i++) {
            assertEquals(a.generate(), b.generate());
        }
    }

    @Test
    void generateWithExtension() {
        String file = new FileNameGenerator(GeneratorConfig.builder().seed(1L).build()).generateWithExtension("pdf");
        assertTrue(file.endsWith(".pdf"));
    }

    @Test
    void generateWithExtensionNormalizesLeadingDot() {
        String file = new FileNameGenerator(GeneratorConfig.builder().seed(1L).build()).generateWithExtension(".png");
        assertTrue(file.endsWith(".png"));
    }

    @Test
    void generateWithExtensionValidation() {
        FileNameGenerator generator = new FileNameGenerator();
        assertThrows(IllegalArgumentException.class, () -> generator.generateWithExtension(null));
        assertThrows(IllegalArgumentException.class, () -> generator.generateWithExtension(" "));
    }
}
