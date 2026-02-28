/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.file;

import org.github.krandom.generator.GeneratorConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FileExtensionGeneratorTest {

    @Test
    void generateDefault() {
        assertNotNull(new FileExtensionGenerator().generate());
    }

    @Test
    void seededReproducibility() {
        FileExtensionGenerator a = new FileExtensionGenerator(GeneratorConfig.builder().seed(42L).build());
        FileExtensionGenerator b = new FileExtensionGenerator(GeneratorConfig.builder().seed(42L).build());
        for (int i = 0; i < 20; i++) {
            assertEquals(a.generate(), b.generate());
        }
    }

    @Test
    void generateFromNormalizesLeadingDot() {
        FileExtensionGenerator generator = new FileExtensionGenerator(GeneratorConfig.builder().seed(1L).build());
        String ext = generator.generateFrom(".json");
        assertEquals("json", ext);
    }

    @Test
    void generateFromValidation() {
        FileExtensionGenerator generator = new FileExtensionGenerator();
        assertThrows(NullPointerException.class, () -> generator.generateFrom((String[]) null));
        assertThrows(IllegalArgumentException.class, () -> generator.generateFrom());
        assertThrows(IllegalArgumentException.class, () -> generator.generateFrom(" "));
    }

    @Test
    void defaultExtensionsExposed() {
        String[] exts = FileExtensionGenerator.defaultExtensions();
        assertTrue(exts.length > 0);
    }
}
