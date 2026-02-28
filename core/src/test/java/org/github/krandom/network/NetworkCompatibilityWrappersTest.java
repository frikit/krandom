/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.network;

import org.github.krandom.generator.GeneratorConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Legacy network package compatibility wrappers")
class NetworkCompatibilityWrappersTest {

    @Test
    @DisplayName("IPv4 wrapper delegates generation")
    void ipv4WrapperDelegates() {
        assertNotNull(new IPv4Generator().generate());
        assertNotNull(new IPv4Generator(GeneratorConfig.defaults()).generate());
    }

    @Test
    @DisplayName("IPv6 wrapper delegates generation")
    void ipv6WrapperDelegates() {
        assertNotNull(new IPv6Generator().generate());
        assertNotNull(new IPv6Generator(GeneratorConfig.defaults()).generate());
    }

    @Test
    @DisplayName("MAC wrapper delegates all generation variants")
    void macWrapperDelegates() {
        MacAddressGenerator generator = new MacAddressGenerator();
        assertNotNull(generator.generate());
        assertNotNull(new MacAddressGenerator(GeneratorConfig.defaults()).generate());
        assertTrue(generator.generate('-').contains("-"));
        String lower = generator.generateLowercase();
        assertTrue(lower.equals(lower.toLowerCase()));
        assertTrue(generator.generateLowercase('-').contains("-"));
    }
}
