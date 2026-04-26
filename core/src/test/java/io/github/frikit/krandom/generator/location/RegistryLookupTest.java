/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.location;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("RegistryLookup")
class RegistryLookupTest {

    @Test
    @DisplayName("putWithLanguageFallback stores exact and language entries")
    void putWithFallbackStoresLanguageEntry() {
        Map<String, String> registry = new HashMap<>();

        RegistryLookup.putWithLanguageFallback(registry, Locale.of("en", "US"), "us");

        assertEquals("us", registry.get("en_US"));
        assertEquals("us", registry.get("en"));
    }

    @Test
    @DisplayName("putWithLanguageFallback replaces language entry for language-only locale")
    void putWithFallbackReplacesLanguageEntryForLanguageOnlyLocale() {
        Map<String, String> registry = new HashMap<>();
        RegistryLookup.putWithLanguageFallback(registry, Locale.of("en", "US"), "us");

        RegistryLookup.putWithLanguageFallback(registry, Locale.of("en"), "en");

        assertEquals("us", registry.get("en_US"));
        assertEquals("en", registry.get("en"));
    }

    @Test
    @DisplayName("findWithFallback resolves exact, fallback and null cases")
    void findWithFallback() {
        Map<String, String> registry = new HashMap<>();
        RegistryLookup.putWithLanguageFallback(registry, Locale.of("en", "US"), "us");

        assertSame("us", RegistryLookup.findWithFallback(registry, Locale.of("en", "US")));
        assertSame("us", RegistryLookup.findWithFallback(registry, Locale.of("en", "CA")));
        assertNull(RegistryLookup.findWithFallback(registry, Locale.of("fr", "FR")));
        assertNull(RegistryLookup.findWithFallback(registry, null));
    }

    @Test
    @DisplayName("containsWithFallback resolves exact, fallback, unknown and null cases")
    void containsWithFallback() {
        Map<String, String> registry = new HashMap<>();
        RegistryLookup.putWithLanguageFallback(registry, Locale.of("en", "US"), "us");

        assertTrue(RegistryLookup.containsWithFallback(registry, Locale.of("en", "US")));
        assertTrue(RegistryLookup.containsWithFallback(registry, Locale.of("en", "CA")));
        assertFalse(RegistryLookup.containsWithFallback(registry, Locale.of("fr", "FR")));
        assertFalse(RegistryLookup.containsWithFallback(registry, null));
    }

    @Test
    @DisplayName("utility constructor throws")
    void utilityConstructorThrows() throws Exception {
        Constructor<RegistryLookup> constructor = RegistryLookup.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        InvocationTargetException thrown = assertThrows(InvocationTargetException.class,
                                                        constructor::newInstance);
        assertTrue(thrown.getCause() instanceof UnsupportedOperationException);
        assertEquals("Utility class", thrown.getCause().getMessage());
    }
}
