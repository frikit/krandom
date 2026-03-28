/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.javaapi;

import org.github.krandom.generator.GeneratorConfig;
import org.github.krandom.generator.GeneratorProfile;
import org.github.krandom.generator.provider.ProviderHub;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GeneratorsTest {

    @Test
    void seededIntGenerationIsDeterministic() {
        int a = Generators.ofInt(1, 100, 42L).generate();
        int b = Generators.ofInt(1, 100, 42L).generate();
        assertEquals(a, b);
    }

    @Test
    void commonFacadeGeneratorsWork() {
        String name = Generators.ofFullName().generate();
        String email = Generators.ofEmail().generate();
        String country = Generators.ofCountry().generate();

        assertNotNull(name);
        assertFalse(name.isBlank());
        assertTrue(email.contains("@"));
        assertFalse(country.isBlank());
    }

    @Test
    void profileConfigFactoryAppliesTemplate() {
        GeneratorConfig strict = Generators.config(GeneratorProfile.STRICT, Locale.GERMANY);
        assertEquals(Locale.GERMANY, strict.getLocale());
        assertTrue(strict.getSeed().isPresent());
        assertEquals(1L, strict.getSeed().getAsLong());
        assertEquals(3, strict.getMinStringLength());
        assertEquals(12, strict.getMaxStringLength());
    }

    @Test
    void providerHubProfileFactoryExposesProfileMetadata() {
        ProviderHub hub = Generators.ofProviderHub(GeneratorProfile.FAST);
        assertSame(GeneratorProfile.FAST, hub.getProfile());
    }

    @Test
    void forTypeDelegatesToCoreFactory() {
        Integer value = Generators.forType(Integer.class).generate();
        assertNotNull(value);
    }
}
