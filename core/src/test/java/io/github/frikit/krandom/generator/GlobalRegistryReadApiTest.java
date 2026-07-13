/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator;

import io.github.frikit.krandom.generator.commerce.RestaurantTypeDataRegistry;
import io.github.frikit.krandom.generator.location.CityDataRegistry;
import io.github.frikit.krandom.generator.location.CountryDataRegistry;
import io.github.frikit.krandom.generator.location.StateDataRegistry;
import io.github.frikit.krandom.generator.location.StreetAddressDataRegistry;
import io.github.frikit.krandom.generator.finance.FinancialTermDataRegistry;
import io.github.frikit.krandom.generator.measurement.MeasurementDataRegistry;
import io.github.frikit.krandom.generator.user.BloodTypeDataRegistry;
import io.github.frikit.krandom.generator.user.ChineseZodiacDataRegistry;
import io.github.frikit.krandom.generator.user.HobbyDataRegistry;
import io.github.frikit.krandom.generator.user.NationalityDataRegistry;
import io.github.frikit.krandom.generator.user.PronounDataRegistry;
import io.github.frikit.krandom.generator.user.TitleDataRegistry;
import io.github.frikit.krandom.generator.weather.WeatherDataRegistry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Locale;
import java.util.Set;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Read-only contract of the global registries after the v2 removal of process-wide mutation:
 * built-ins are pre-seeded at class load, lookups fall back from exact locale to language, and
 * unknown or null locales resolve to nothing. Assertions derive from each registry's own seeded
 * keys because the built-in locale sets differ per data family.
 */
@DisplayName("Global registry read APIs")
class GlobalRegistryReadApiTest {

    private record ReadApi(String name,
                           Function<Locale, Boolean> isRegistered,
                           Function<Locale, Object> forLocale,
                           Set<String> registeredKeys) {
    }

    private static java.util.List<ReadApi> readApis() {
        return java.util.List.of(
            new ReadApi("weather", WeatherDataRegistry::isRegistered,
                WeatherDataRegistry::forLocale, WeatherDataRegistry.registeredKeys()),
            new ReadApi("restaurantType", RestaurantTypeDataRegistry::isRegistered,
                RestaurantTypeDataRegistry::forLocale, RestaurantTypeDataRegistry.registeredKeys()),
            new ReadApi("financialTerm", FinancialTermDataRegistry::isRegistered,
                FinancialTermDataRegistry::forLocale, FinancialTermDataRegistry.registeredKeys()),
            new ReadApi("measurement", MeasurementDataRegistry::isRegistered,
                MeasurementDataRegistry::forLocale, MeasurementDataRegistry.registeredKeys()),
            new ReadApi("pronoun", PronounDataRegistry::isRegistered,
                PronounDataRegistry::forLocale, PronounDataRegistry.registeredKeys()),
            new ReadApi("hobby", HobbyDataRegistry::isRegistered,
                HobbyDataRegistry::forLocale, HobbyDataRegistry.registeredKeys()),
            new ReadApi("nationality", NationalityDataRegistry::isRegistered,
                NationalityDataRegistry::forLocale, NationalityDataRegistry.registeredKeys()),
            new ReadApi("chineseZodiac", ChineseZodiacDataRegistry::isRegistered,
                ChineseZodiacDataRegistry::forLocale, ChineseZodiacDataRegistry.registeredKeys()),
            new ReadApi("title", TitleDataRegistry::isRegistered,
                TitleDataRegistry::forLocale, TitleDataRegistry.registeredKeys()),
            new ReadApi("bloodType", BloodTypeDataRegistry::isRegistered,
                BloodTypeDataRegistry::forLocale, BloodTypeDataRegistry.registeredKeys()),
            new ReadApi("country", CountryDataRegistry::isRegistered,
                CountryDataRegistry::forLocale, CountryDataRegistry.registeredKeys()),
            new ReadApi("city", CityDataRegistry::isRegistered,
                CityDataRegistry::forLocale, CityDataRegistry.registeredKeys()),
            new ReadApi("state", StateDataRegistry::isRegistered,
                StateDataRegistry::forLocale, StateDataRegistry.registeredKeys()),
            new ReadApi("streetAddress", StreetAddressDataRegistry::isRegistered,
                StreetAddressDataRegistry::forLocale, StreetAddressDataRegistry.registeredKeys()));
    }

    private static Locale seededCountryLocale(ReadApi api) {
        String key = api.registeredKeys().stream()
            .filter(k -> k.contains("_"))
            .sorted()
            .findFirst()
            .orElseThrow(() -> new AssertionError(api.name() + " seeded no country-level key"));
        String[] parts = key.split("_", 2);
        return Locale.of(parts[0], parts[1]);
    }

    @Test
    @DisplayName("seeded locales resolve by exact key and by language fallback")
    void builtInsResolveWithFallback() {
        for (ReadApi api : readApis()) {
            Locale seeded = seededCountryLocale(api);

            assertTrue(api.isRegistered().apply(seeded), api.name());
            assertNotNull(api.forLocale().apply(seeded), api.name());

            Locale unknownRegion = Locale.of(seeded.getLanguage(), "ZZ");
            assertTrue(api.isRegistered().apply(unknownRegion), api.name());
            assertNotNull(api.forLocale().apply(unknownRegion), api.name());

            Locale languageOnly = Locale.of(seeded.getLanguage());
            assertTrue(api.isRegistered().apply(languageOnly), api.name());
            assertNotNull(api.forLocale().apply(languageOnly), api.name());
        }
    }

    @Test
    @DisplayName("null and unknown locales resolve to nothing")
    void nullAndUnknownLocalesResolveToNothing() {
        for (ReadApi api : readApis()) {
            assertFalse(api.isRegistered().apply(null), api.name());
            assertNull(api.forLocale().apply(null), api.name());

            Locale unknown = Locale.of("xx", "YY");
            assertFalse(api.isRegistered().apply(unknown), api.name());
            assertNull(api.forLocale().apply(unknown), api.name());
        }
    }

    @Test
    @DisplayName("key snapshots pair every seeded country key with its language fallback")
    void registeredKeySnapshotsContainLanguageFallbacks() {
        for (ReadApi api : readApis()) {
            Locale seeded = seededCountryLocale(api);
            assertTrue(api.registeredKeys().contains(seeded.getLanguage()),
                () -> api.name() + " must seed the language fallback for " + seeded);
        }
    }
}
