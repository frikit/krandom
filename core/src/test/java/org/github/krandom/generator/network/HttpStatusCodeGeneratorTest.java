/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.network;

import org.github.krandom.generator.GeneratorConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("HttpStatusCodeGenerator")
class HttpStatusCodeGeneratorTest {

    @Test
    void generate() {
        HttpStatusCodeGenerator gen = new HttpStatusCodeGenerator();
        for (int i = 0; i < 100; i++) {
            int code = gen.generate();
            assertTrue(code >= 100 && code <= 599);
        }
    }

    @Test
    void categories() {
        HttpStatusCodeGenerator gen = new HttpStatusCodeGenerator(GeneratorConfig.builder().seed(9L).build());
        assertTrue(gen.generateByCategory(1) / 100 == 1);
        assertTrue(gen.generateByCategory(2) / 100 == 2);
        assertTrue(gen.generateByCategory(3) / 100 == 3);
        assertTrue(gen.generateByCategory(4) / 100 == 4);
        assertTrue(gen.generateByCategory(5) / 100 == 5);
        assertThrows(IllegalArgumentException.class, () -> gen.generateByCategory(0));
        assertThrows(IllegalArgumentException.class, () -> gen.generateByCategory(6));
        assertThrows(NullPointerException.class, () -> new HttpStatusCodeGenerator(null));
    }

    @Test
    void reasonPhrasesAndHeaderHelpers() {
        HttpStatusCodeGenerator generator = new HttpStatusCodeGenerator(GeneratorConfig.builder().seed(42L).build());

        assertEquals("OK", generator.reasonPhrase(200));
        assertEquals("Unknown Status", generator.reasonPhrase(777));

        String phrase = generator.generateReasonPhrase();
        assertNotNull(phrase);
        assertFalse(phrase.isBlank());

        String statusLine = generator.generateStatusLine();
        assertTrue(statusLine.startsWith("HTTP/1.1 "));
        assertTrue(statusLine.matches("HTTP/1\\.1 \\d{3} .+"));

        String headerName = generator.generateHeaderName();
        assertNotNull(headerName);
        assertFalse(headerName.isBlank());
    }
}
