/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.location;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Locale;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Location registry fallback")
class LocationRegistryFallbackTest {

    @ParameterizedTest(name = "{0}")
    @MethodSource("fallbackRegistries")
    @DisplayName("all location registries resolve locale by language fallback")
    void allRegistriesUseLanguageFallback(String name, Function<Locale, ?> resolver,
                                          Predicate<Locale> isRegistered) {
        Locale locale = Locale.of("en", "ZZ");
        assertTrue(isRegistered.test(locale), name + " should report fallback locale as registered");
        assertNotNull(resolver.apply(locale), name + " should return fallback provider");
    }

    private static Stream<org.junit.jupiter.params.provider.Arguments> fallbackRegistries() {
        return Stream.of(
            org.junit.jupiter.params.provider.Arguments.of("CountryDataRegistry",
                                                           (Function<Locale, ?>) CountryDataRegistry::forLocale,
                                                           (Predicate<Locale>) CountryDataRegistry::isRegistered),
            org.junit.jupiter.params.provider.Arguments.of("CityDataRegistry",
                                                           (Function<Locale, ?>) CityDataRegistry::forLocale,
                                                           (Predicate<Locale>) CityDataRegistry::isRegistered),
            org.junit.jupiter.params.provider.Arguments.of("StateDataRegistry",
                                                           (Function<Locale, ?>) StateDataRegistry::forLocale,
                                                           (Predicate<Locale>) StateDataRegistry::isRegistered),
            org.junit.jupiter.params.provider.Arguments.of("StreetAddressDataRegistry",
                                                           (Function<Locale, ?>) StreetAddressDataRegistry::forLocale,
                                                           (Predicate<Locale>) StreetAddressDataRegistry::isRegistered)
        );
    }
}
