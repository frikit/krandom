/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.spring.slice;

import io.github.frikit.krandom.generator.Generator;
import io.github.frikit.krandom.generator.provider.ProviderHub;
import io.github.frikit.krandom.spring.KrandomProviderCustomizer;
import io.github.frikit.krandom.spring.KrandomTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import static org.junit.jupiter.api.Assertions.assertEquals;

@KrandomTest
@DisplayName("@KrandomTest with a provider customizer on a cached context")
class KrandomTestProviderCustomizerTest {

    @TestConfiguration(proxyBeanMethods = false)
    static class Providers {

        @Bean
        KrandomProviderCustomizer ticketNumbers() {
            return hub -> hub.register("test.ticket", config -> (Generator<String>) () -> "TICKET-1");
        }
    }

    @Autowired
    ProviderHub providerHub;

    @Test
    @DisplayName("the first test sees the provider registered once at startup")
    void firstTestUsesProvider() {
        assertEquals("TICKET-1", providerHub.get("test.ticket", Generator.class).generate());
    }

    @Test
    @DisplayName("later tests reuse the cached hub without registering again")
    void secondTestUsesProvider() {
        assertEquals("TICKET-1", providerHub.get("test.ticket", Generator.class).generate());
    }
}
