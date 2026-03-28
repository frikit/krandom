/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.network;

import org.github.krandom.generator.GeneratorConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("UserAgentGenerator variants")
class UserAgentVariantsTest {

    @Test
    @DisplayName("browser-specific user-agent methods produce expected tokens")
    void variants() {
        UserAgentGenerator generator = new UserAgentGenerator();
        assertTrue(generator.generateChrome().contains("Chrome/"));
        assertTrue(generator.generateFirefox().contains("Firefox/"));
        assertTrue(generator.generateSafari().contains("Safari/"));
        assertTrue(generator.generateOpera().contains("OPR/"));
        assertTrue(generator.generateAndroid().contains("Android"));
        assertTrue(generator.generateIos().contains("iPhone"));
    }

    @Test
    @DisplayName("seeded generation is reproducible")
    void seeded() {
        GeneratorConfig config = GeneratorConfig.builder().seed(11L).build();
        UserAgentGenerator a = new UserAgentGenerator(config);
        UserAgentGenerator b = new UserAgentGenerator(config);
        assertEquals(a.generate(), b.generate());
    }
}
