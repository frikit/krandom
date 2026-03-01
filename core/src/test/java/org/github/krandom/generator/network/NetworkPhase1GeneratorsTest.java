/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.network;

import org.github.krandom.generator.GeneratorConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Locale;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Phase 1 network generators")
class NetworkPhase1GeneratorsTest {

    @Test
    @DisplayName("hostname generator produces subdomain plus domain")
    void hostname() {
        String hostname = new HostnameGenerator(Locale.US).generate();
        assertTrue(hostname.matches("[a-z]+\\.[a-z0-9]+\\.[a-z]{2,}"));
    }

    @Test
    @DisplayName("hostname generator validates fixed subdomain")
    void hostnameFixedSubdomain() {
        HostnameGenerator gen = new HostnameGenerator();
        assertTrue(gen.generate("api").startsWith("api."));
        assertThrows(IllegalArgumentException.class, () -> gen.generate(" "));
    }

    @Test
    @DisplayName("URI generator uses URL-form URI output")
    void uri() {
        UriGenerator generator = new UriGenerator(Locale.GERMANY);
        boolean sawQuery = false;
        boolean sawNoQuery = false;
        for (int i = 0; i < 120 && !(sawQuery && sawNoQuery); i++) {
            String uri = generator.generate();
            assertTrue(uri.contains("://"));
            assertTrue(uri.matches("[a-z]+://.+"));
            sawQuery |= uri.contains("?");
            sawNoQuery |= !uri.contains("?");
        }
        assertTrue(sawQuery);
        assertTrue(sawNoQuery);
    }

    @Test
    @DisplayName("http method generator uses known methods")
    void httpMethod() {
        HttpMethodGenerator gen = new HttpMethodGenerator(GeneratorConfig.builder().seed(7L).build());
        Set<String> methods = Set.of("GET", "POST", "PUT", "PATCH", "DELETE", "HEAD", "OPTIONS");
        for (int i = 0; i < 20; i++) {
            assertTrue(methods.contains(gen.generate()));
        }
    }

    @Test
    @DisplayName("seeded hostname generation is reproducible")
    void seededHostname() {
        GeneratorConfig cfg = GeneratorConfig.builder().seed(123L).locale(Locale.US).build();
        HostnameGenerator a = new HostnameGenerator(cfg);
        HostnameGenerator b = new HostnameGenerator(cfg);
        assertEquals(a.generate(), b.generate());
    }

    @Test
    @DisplayName("seeded URI generation is reproducible")
    void seededUri() {
        GeneratorConfig cfg = GeneratorConfig.builder().seed(321L).locale(Locale.US).build();
        UriGenerator a = new UriGenerator(cfg);
        UriGenerator b = new UriGenerator(cfg);
        assertEquals(a.generate(), b.generate());
    }

    @Test
    @DisplayName("constructors reject null config")
    void nullConfig() {
        assertThrows(NullPointerException.class, () -> new HostnameGenerator((GeneratorConfig) null));
        assertThrows(NullPointerException.class, () -> new UriGenerator((GeneratorConfig) null));
        assertThrows(NullPointerException.class, () -> new HttpMethodGenerator(null));
    }
}
