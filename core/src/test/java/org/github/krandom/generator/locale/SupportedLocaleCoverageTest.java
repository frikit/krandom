/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.locale;

import org.github.krandom.generator.location.CityDataRegistry;
import org.github.krandom.generator.location.CountryDataRegistry;
import org.github.krandom.generator.location.StateDataRegistry;
import org.github.krandom.generator.location.StreetAddressDataRegistry;
import org.github.krandom.generator.user.FirstNameDataRegistry;
import org.github.krandom.generator.user.GenderDataRegistry;
import org.github.krandom.generator.user.LastNameDataRegistry;
import org.github.krandom.generator.user.ProfessionDataRegistry;
import org.github.krandom.generator.user.SuffixDataRegistry;
import org.github.krandom.generator.user.TitleDataRegistry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Supported locale coverage across providers and registries")
class SupportedLocaleCoverageTest {

    @Test
    @DisplayName("user registries are seeded with exact providers for every supported locale")
    void userRegistriesCoverAllSupportedLocales() {
        for (Locale locale : SupportedLocale.locales()) {
            assertTrue(FirstNameDataRegistry.isRegistered(locale), "FirstNameDataRegistry missing " + locale);
            assertTrue(LastNameDataRegistry.isRegistered(locale), "LastNameDataRegistry missing " + locale);
            assertTrue(GenderDataRegistry.isRegistered(locale), "GenderDataRegistry missing " + locale);
            assertTrue(ProfessionDataRegistry.isRegistered(locale), "ProfessionDataRegistry missing " + locale);
            assertTrue(TitleDataRegistry.isRegistered(locale), "TitleDataRegistry missing " + locale);
            assertTrue(SuffixDataRegistry.isRegistered(locale), "SuffixDataRegistry missing " + locale);

            assertTrue(FirstNameDataRegistry.forLocale(locale).getLocale().equals(locale));
            assertTrue(LastNameDataRegistry.forLocale(locale).getLocale().equals(locale));
            assertTrue(GenderDataRegistry.forLocale(locale).getLocale().equals(locale));
            assertTrue(ProfessionDataRegistry.forLocale(locale).getLocale().equals(locale));
            assertTrue(TitleDataRegistry.forLocale(locale).getLocale().equals(locale));
            assertTrue(SuffixDataRegistry.forLocale(locale).getLocale().equals(locale));

            assertTrue(FirstNameDataRegistry.forLocale(locale).getMaleFirstNames().length > 0);
            assertTrue(FirstNameDataRegistry.forLocale(locale).getFemaleFirstNames().length > 0);
            assertTrue(LastNameDataRegistry.forLocale(locale).getLastNames().length > 0);
            assertTrue(!GenderDataRegistry.forLocale(locale).getMaleLabel().isBlank());
            assertTrue(!GenderDataRegistry.forLocale(locale).getFemaleLabel().isBlank());
            assertTrue(ProfessionDataRegistry.forLocale(locale).getProfessions().length > 0);
            assertTrue(TitleDataRegistry.forLocale(locale).getTitles().length > 0);
            assertTrue(SuffixDataRegistry.forLocale(locale).getSuffixes().length > 0);
        }
    }

    @Test
    @DisplayName("location registries are seeded with exact providers for every supported locale")
    void locationRegistriesCoverAllSupportedLocales() {
        for (Locale locale : SupportedLocale.locales()) {
            assertTrue(CityDataRegistry.isRegistered(locale), "CityDataRegistry missing " + locale);
            assertTrue(StateDataRegistry.isRegistered(locale), "StateDataRegistry missing " + locale);
            assertTrue(CountryDataRegistry.isRegistered(locale), "CountryDataRegistry missing " + locale);
            assertTrue(StreetAddressDataRegistry.isRegistered(locale), "StreetAddressDataRegistry missing " + locale);

            assertTrue(CityDataRegistry.forLocale(locale).getLocale().equals(locale));
            assertTrue(StateDataRegistry.forLocale(locale).getLocale().equals(locale));
            assertTrue(CountryDataRegistry.forLocale(locale).getLocale().equals(locale));
            assertTrue(StreetAddressDataRegistry.forLocale(locale).getLocale().equals(locale));

            assertTrue(CityDataRegistry.forLocale(locale).getCities().length > 0);
            assertTrue(StateDataRegistry.forLocale(locale).getStates().length > 0);
            assertTrue(CountryDataRegistry.forLocale(locale).getCountries().length > 0);
            assertTrue(StreetAddressDataRegistry.forLocale(locale).getStreetNames().length > 0);
            assertTrue(StreetAddressDataRegistry.forLocale(locale).getStreetTypesShort().length > 0);
            assertTrue(StreetAddressDataRegistry.forLocale(locale).getStreetTypesLong().length > 0);
        }
    }
}
