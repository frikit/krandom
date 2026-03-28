/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.identifier;

import org.github.krandom.generator.GeneratorConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("EanGenerator")
class EanGeneratorTest {

    @Test
    @DisplayName("generates ean8 and ean13 variants")
    void variants() {
        EanGenerator generator = new EanGenerator(GeneratorConfig.builder().seed(3L).build());
        assertTrue(generator.generateEan8().matches("\\d{8}"));
        assertTrue(generator.generateEan13().matches("\\d{13}"));
        boolean saw8 = false;
        boolean saw13 = false;
        for (int i = 0; i < 100 && !(saw8 && saw13); i++) {
            String value = generator.generate();
            saw8 |= value.length() == 8;
            saw13 |= value.length() == 13;
        }
        assertTrue(saw8 && saw13);
    }

    @Test
    @DisplayName("localized generation applies prefix")
    void localized() {
        EanGenerator generator = new EanGenerator();
        assertTrue(generator.generateLocalizedEan13("590").startsWith("590"));
        assertTrue(generator.generateLocalizedEan8("84").startsWith("84"));
        assertTrue(generator.generateLocalizedEan13("59-0").startsWith("590"));
    }

    @Test
    @DisplayName("seeded generation reproducible")
    void seeded() {
        GeneratorConfig cfg = GeneratorConfig.builder().seed(98L).build();
        EanGenerator a = new EanGenerator(cfg);
        EanGenerator b = new EanGenerator(cfg);
        assertEquals(a.generateEan13(), b.generateEan13());
    }

    @Test
    @DisplayName("invalid prefixes are rejected")
    void invalid() {
        EanGenerator generator = new EanGenerator();
        assertThrows(IllegalArgumentException.class, () -> generator.generateLocalizedEan8("12345678"));
        assertThrows(IllegalArgumentException.class, () -> generator.generateLocalizedEan13("1234567890123"));
        assertThrows(NullPointerException.class, () -> new EanGenerator(null));
    }

    @Test
    @DisplayName("internal length guard rejects unsupported EAN lengths")
    void internalLengthGuard() throws Exception {
        EanGenerator generator = new EanGenerator();
        Method method = EanGenerator.class.getDeclaredMethod("generateWithLength", int.class, String.class);
        method.setAccessible(true);

        InvocationTargetException thrown = assertThrows(InvocationTargetException.class,
                                                        () -> method.invoke(generator, 9, null));
        assertInstanceOf(IllegalArgumentException.class, thrown.getCause());
    }
}
