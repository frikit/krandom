/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.spring;

import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.finance.BankingSafetyPolicy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(
    classes = KrandomFullContextTest.FullContextConfiguration.class,
    properties = {
        "krandom.seed=24680",
        "krandom.locale=fr-FR",
        "krandom.object-max-depth=3",
        "krandom.object-null-probability=0.25",
        "krandom.min-string-length=7",
        "krandom.max-string-length=11",
        "krandom.min-collection-size=2",
        "krandom.max-collection-size=4",
        "krandom.clock=2026-01-01T00:00:00Z",
        "krandom.clock-zone=Europe/Paris",
        "krandom.banking-safety-policy=realistic-unclassified"
    })
@DisplayName("full Spring Boot context")
class KrandomFullContextTest {

    @SpringBootConfiguration
    @EnableAutoConfiguration
    static class FullContextConfiguration {
    }

    @Autowired
    GeneratorConfig generatorConfig;

    @Test
    @DisplayName("binds the same GeneratorConfig contract as the krandom test slice")
    void bindsConfiguredGeneratorContract() {
        assertEquals(24680L, generatorConfig.getSeed().getAsLong());
        assertEquals(Locale.FRANCE, generatorConfig.getLocale());
        assertEquals(3, generatorConfig.getObjectMaxDepth());
        assertEquals(0.25, generatorConfig.getObjectNullProbability());
        assertEquals(7, generatorConfig.getMinStringLength());
        assertEquals(11, generatorConfig.getMaxStringLength());
        assertEquals(2, generatorConfig.getMinCollectionSize());
        assertEquals(4, generatorConfig.getMaxCollectionSize());
        assertEquals(Instant.parse("2026-01-01T00:00:00Z"), generatorConfig.getClock().instant());
        assertEquals(ZoneId.of("Europe/Paris"), generatorConfig.getClock().getZone());
        assertEquals(BankingSafetyPolicy.REALISTIC_UNCLASSIFIED, generatorConfig.getBankingSafetyPolicy());
    }
}
