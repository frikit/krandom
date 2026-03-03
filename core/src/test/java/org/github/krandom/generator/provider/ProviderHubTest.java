/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.provider;

import org.github.krandom.generator.GeneratorConfig;
import org.github.krandom.generator.datetime.DateGenerator;
import org.github.krandom.generator.finance.MoneyGenerator;
import org.github.krandom.generator.identifier.UUIDGenerator;
import org.github.krandom.generator.location.StreetAddressGenerator;
import org.github.krandom.generator.network.URLGenerator;
import org.github.krandom.generator.text.WordGenerator;
import org.github.krandom.generator.user.FullNameGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ProviderHub")
class ProviderHubTest {

    @Test
    @DisplayName("default hub exposes built-in providers and aliases")
    void builtInsAndAliasesAreAvailable() {
        ProviderHub hub = new ProviderHub();

        assertTrue(hub.providerNames().contains("person"));
        assertTrue(hub.providerNames().contains("internet"));
        assertEquals("person", hub.aliases().get("full_name"));
        assertEquals("internet", hub.aliases().get("url"));

        assertInstanceOf(FullNameGenerator.class, hub.get("person"));
        assertInstanceOf(FullNameGenerator.class, hub.get("full_name"));
        assertInstanceOf(StreetAddressGenerator.class, hub.get("address"));
        assertInstanceOf(URLGenerator.class, hub.get("internet"));
        assertInstanceOf(URLGenerator.class, hub.get("url"));
        assertInstanceOf(MoneyGenerator.class, hub.get("finance"));
        assertInstanceOf(DateGenerator.class, hub.get("datetime"));
        assertInstanceOf(WordGenerator.class, hub.get("text"));
        assertInstanceOf(UUIDGenerator.class, hub.get("code"));
    }

    @Test
    @DisplayName("locale and seed config is propagated to custom providers")
    void localeAndSeedPropagateToFactory() {
        GeneratorConfig config = GeneratorConfig.builder().locale(Locale.JAPAN).seed(42L).build();
        ProviderHub hub = new ProviderHub(config);
        AtomicReference<GeneratorConfig> observed = new AtomicReference<>();

        hub.register("custom", cfg -> {
            observed.set(cfg);
            return "ok";
        });

        assertEquals("ok", hub.get("custom"));
        assertNotNull(observed.get());
        assertEquals(Locale.JAPAN, observed.get().getLocale());
        assertTrue(observed.get().getSeed().isPresent());
        assertEquals(42L, observed.get().getSeed().getAsLong());
    }

    @Test
    @DisplayName("typed get validates runtime type")
    void typedGetValidatesType() {
        ProviderHub hub = new ProviderHub(Locale.US);

        MoneyGenerator money = hub.get("finance", MoneyGenerator.class);
        assertNotNull(money.generate());

        IllegalArgumentException mismatch = assertThrows(IllegalArgumentException.class,
                () -> hub.get("finance", FullNameGenerator.class));
        assertTrue(mismatch.getMessage().contains("not"));
    }

    @Test
    @DisplayName("registration conflict can fail or replace")
    void registerConflictPolicy() {
        ProviderHub hub = new ProviderHub();

        assertThrows(IllegalArgumentException.class,
                () -> hub.register("person", cfg -> "x"));

        hub.register("person", cfg -> "replacement", ConflictPolicy.REPLACE);
        assertEquals("replacement", hub.get("person"));
    }

    @Test
    @DisplayName("alias registration validates target and conflict policy")
    void aliasRegistrationAndConflicts() {
        ProviderHub hub = new ProviderHub();

        assertThrows(IllegalArgumentException.class,
                () -> hub.registerAlias("mystery", "missing"));

        hub.register("custom", cfg -> 10);
        hub.registerAlias("c", "custom");
        assertEquals(10, hub.get("c"));

        assertThrows(IllegalArgumentException.class,
                () -> hub.registerAlias("c", "custom"));

        hub.registerAlias("c", "custom", ConflictPolicy.REPLACE);
        assertEquals(10, hub.get("c"));

        assertThrows(IllegalArgumentException.class,
                () -> hub.registerAlias("person", "custom", ConflictPolicy.REPLACE));
    }

    @Test
    @DisplayName("has returns true for canonical names and aliases")
    void hasChecksCanonicalAndAlias() {
        ProviderHub hub = new ProviderHub();

        assertTrue(hub.has("person"));
        assertTrue(hub.has("full_name"));
        assertFalse(hub.has("not_exists"));
    }

    @Test
    @DisplayName("unknown provider throws and null or blank names are rejected")
    void unknownAndInvalidNameValidation() {
        ProviderHub hub = new ProviderHub();

        assertThrows(IllegalArgumentException.class, () -> hub.get("missing"));
        assertThrows(NullPointerException.class, () -> hub.get(null));
        assertThrows(IllegalArgumentException.class, () -> hub.get("   "));
        assertThrows(NullPointerException.class, () -> hub.get("person", null));
    }

    @Test
    @DisplayName("constructor and registration validate null arguments")
    void nullArgumentValidation() {
        assertThrows(NullPointerException.class, () -> new ProviderHub((GeneratorConfig) null));
        assertThrows(NullPointerException.class, () -> new ProviderHub((Locale) null));

        ProviderHub hub = new ProviderHub();
        assertThrows(NullPointerException.class, () -> hub.register("x", null));
        assertThrows(NullPointerException.class, () -> hub.register("x", cfg -> "v", null));
        assertThrows(NullPointerException.class, () -> hub.registerAlias("x", "person", null));
    }

    @Test
    @DisplayName("getConfig returns the configured generator config")
    void getConfigReturnsConfiguredValue() {
        GeneratorConfig config = GeneratorConfig.builder().locale(Locale.ITALY).seed(7L).build();
        ProviderHub hub = new ProviderHub(config);

        assertSame(config, hub.getConfig());
    }

    @Test
    @DisplayName("alias equal to target canonical name is accepted")
    void aliasEqualToTargetCanonicalNameAllowed() {
        ProviderHub hub = new ProviderHub();

        hub.registerAlias("person", "person", ConflictPolicy.REPLACE);
        assertEquals("person", hub.aliases().get("person"));
    }
}
