/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.frikit.krandom.generator.commerce.RestaurantTypeDataProvider;
import io.github.frikit.krandom.generator.commerce.RestaurantTypeGenerator;
import io.github.frikit.krandom.generator.finance.FinancialTermDataProvider;
import io.github.frikit.krandom.generator.finance.FinancialTermGenerator;
import io.github.frikit.krandom.generator.location.CityDataProvider;
import io.github.frikit.krandom.generator.location.CountryDataProvider;
import io.github.frikit.krandom.generator.location.StateDataProvider;
import io.github.frikit.krandom.generator.location.StreetAddressDataProvider;
import io.github.frikit.krandom.generator.measurement.MeasurementDataProvider;
import io.github.frikit.krandom.generator.measurement.MeasurementGenerator;
import io.github.frikit.krandom.generator.user.BloodTypeDataProvider;
import io.github.frikit.krandom.generator.user.BloodTypeGenerator;
import io.github.frikit.krandom.generator.user.ChineseZodiacDataProvider;
import io.github.frikit.krandom.generator.user.ChineseZodiacGenerator;
import io.github.frikit.krandom.generator.user.FirstNameDataProvider;
import io.github.frikit.krandom.generator.user.FirstNameDataRegistry;
import io.github.frikit.krandom.generator.user.GenderDataProvider;
import io.github.frikit.krandom.generator.user.HobbyDataProvider;
import io.github.frikit.krandom.generator.user.HobbyGenerator;
import io.github.frikit.krandom.generator.user.LastNameDataProvider;
import io.github.frikit.krandom.generator.user.NationalityDataProvider;
import io.github.frikit.krandom.generator.user.NationalityGenerator;
import io.github.frikit.krandom.generator.user.ProfessionDataProvider;
import io.github.frikit.krandom.generator.user.PronounDataProvider;
import io.github.frikit.krandom.generator.user.PronounGenerator;
import io.github.frikit.krandom.generator.user.SuffixDataProvider;
import io.github.frikit.krandom.generator.user.TitleDataProvider;
import io.github.frikit.krandom.generator.user.ZodiacDataProvider;
import io.github.frikit.krandom.generator.user.ZodiacGenerator;
import io.github.frikit.krandom.generator.user.nationalid.NationalIdProvider;
import io.github.frikit.krandom.generator.weather.WeatherDataProvider;
import io.github.frikit.krandom.generator.weather.WeatherGenerator;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("DataRegistryContext")
class DataRegistryContextTest {

    @Test
    @DisplayName("globalDefault delegates to static registries")
    void globalDefaultDelegatesToStaticRegistries() {
        DataRegistryContext context = DataRegistryContext.globalDefault();
        Locale locale = Locale.US;

        assertTrue(context.isRegistered(Vocabulary.FIRST_NAME, locale));
        assertTrue(context.isRegistered(Vocabulary.LAST_NAME, locale));
        assertTrue(context.isRegistered(Vocabulary.GENDER, locale));
        assertTrue(context.isRegistered(Vocabulary.TITLE, locale));
        assertTrue(context.isRegistered(Vocabulary.SUFFIX, locale));
        assertTrue(context.isRegistered(Vocabulary.PROFESSION, locale));
        assertTrue(context.isRegistered(Vocabulary.CITY, locale));
        assertTrue(context.isRegistered(Vocabulary.STATE, locale));
        assertTrue(context.isRegistered(Vocabulary.COUNTRY, locale));
        assertTrue(context.isRegistered(Vocabulary.STREET_ADDRESS, locale));
        assertTrue(context.isRegistered(Vocabulary.NATIONAL_ID, locale));

        assertNotNull(context.provider(Vocabulary.FIRST_NAME, locale));
        assertNotNull(context.provider(Vocabulary.LAST_NAME, locale));
        assertNotNull(context.provider(Vocabulary.GENDER, locale));
        assertNotNull(context.provider(Vocabulary.TITLE, locale));
        assertNotNull(context.provider(Vocabulary.SUFFIX, locale));
        assertNotNull(context.provider(Vocabulary.PROFESSION, locale));
        assertNotNull(context.provider(Vocabulary.CITY, locale));
        assertNotNull(context.provider(Vocabulary.STATE, locale));
        assertNotNull(context.provider(Vocabulary.COUNTRY, locale));
        assertNotNull(context.provider(Vocabulary.STREET_ADDRESS, locale));
        assertNotNull(context.provider(Vocabulary.NATIONAL_ID, locale));

        assertFalse(context.registeredKeys(Vocabulary.FIRST_NAME).isEmpty());
        assertFalse(context.registeredKeys(Vocabulary.LAST_NAME).isEmpty());
        assertFalse(context.registeredKeys(Vocabulary.GENDER).isEmpty());
        assertFalse(context.registeredKeys(Vocabulary.TITLE).isEmpty());
        assertFalse(context.registeredKeys(Vocabulary.SUFFIX).isEmpty());
        assertFalse(context.registeredKeys(Vocabulary.PROFESSION).isEmpty());
        assertFalse(context.registeredKeys(Vocabulary.CITY).isEmpty());
        assertFalse(context.registeredKeys(Vocabulary.STATE).isEmpty());
        assertFalse(context.registeredKeys(Vocabulary.COUNTRY).isEmpty());
        assertFalse(context.registeredKeys(Vocabulary.STREET_ADDRESS).isEmpty());
        assertFalse(context.registeredKeys(Vocabulary.NATIONAL_ID).isEmpty());
    }

    @Test
    @DisplayName("isolated context does not fallback to global registries")
    void isolatedContextWithoutFallback() {
        DataRegistryContext context = DataRegistryContext.builder().isolated().build();
        Locale locale = Locale.US;

        assertFalse(context.isRegistered(Vocabulary.FIRST_NAME, locale));
        assertFalse(context.isRegistered(Vocabulary.LAST_NAME, locale));
        assertFalse(context.isRegistered(Vocabulary.GENDER, locale));
        assertFalse(context.isRegistered(Vocabulary.TITLE, locale));
        assertFalse(context.isRegistered(Vocabulary.SUFFIX, locale));
        assertFalse(context.isRegistered(Vocabulary.PROFESSION, locale));
        assertFalse(context.isRegistered(Vocabulary.CITY, locale));
        assertFalse(context.isRegistered(Vocabulary.STATE, locale));
        assertFalse(context.isRegistered(Vocabulary.COUNTRY, locale));
        assertFalse(context.isRegistered(Vocabulary.STREET_ADDRESS, locale));
        assertFalse(context.isRegistered(Vocabulary.NATIONAL_ID, locale));

        assertNull(context.provider(Vocabulary.FIRST_NAME, locale));
        assertNull(context.provider(Vocabulary.LAST_NAME, locale));
        assertNull(context.provider(Vocabulary.GENDER, locale));
        assertNull(context.provider(Vocabulary.TITLE, locale));
        assertNull(context.provider(Vocabulary.SUFFIX, locale));
        assertNull(context.provider(Vocabulary.PROFESSION, locale));
        assertNull(context.provider(Vocabulary.CITY, locale));
        assertNull(context.provider(Vocabulary.STATE, locale));
        assertNull(context.provider(Vocabulary.COUNTRY, locale));
        assertNull(context.provider(Vocabulary.STREET_ADDRESS, locale));
        assertNull(context.provider(Vocabulary.NATIONAL_ID, locale));
        assertNull(context.provider(Vocabulary.FIRST_NAME, null));

        assertTrue(context.registeredKeys(Vocabulary.FIRST_NAME).isEmpty());
        assertTrue(context.registeredKeys(Vocabulary.LAST_NAME).isEmpty());
        assertTrue(context.registeredKeys(Vocabulary.GENDER).isEmpty());
        assertTrue(context.registeredKeys(Vocabulary.TITLE).isEmpty());
        assertTrue(context.registeredKeys(Vocabulary.SUFFIX).isEmpty());
        assertTrue(context.registeredKeys(Vocabulary.PROFESSION).isEmpty());
        assertTrue(context.registeredKeys(Vocabulary.CITY).isEmpty());
        assertTrue(context.registeredKeys(Vocabulary.STATE).isEmpty());
        assertTrue(context.registeredKeys(Vocabulary.COUNTRY).isEmpty());
        assertTrue(context.registeredKeys(Vocabulary.STREET_ADDRESS).isEmpty());
        assertTrue(context.registeredKeys(Vocabulary.NATIONAL_ID).isEmpty());
    }

    @Test
    @DisplayName("builder registration supports exact and language fallback lookups")
    void builderRegistrationLookupAndKeys() {
        Locale locale = Locale.US;
        Locale fallbackLocale = Locale.CANADA;

        DataRegistryContext context = DataRegistryContext.builder()
                .isolated()
                .register(Vocabulary.FIRST_NAME, firstNameProvider(locale))
                .register(Vocabulary.LAST_NAME, lastNameProvider(locale))
                .register(Vocabulary.GENDER, genderProvider(locale))
                .register(Vocabulary.TITLE, titleProvider(locale))
                .register(Vocabulary.SUFFIX, suffixProvider(locale))
                .register(Vocabulary.PROFESSION, professionProvider(locale))
                .register(Vocabulary.CITY, cityProvider(locale))
                .register(Vocabulary.STATE, stateProvider(locale))
                .register(Vocabulary.COUNTRY, countryProvider(locale))
                .register(Vocabulary.STREET_ADDRESS, streetAddressProvider(locale))
                .register(Vocabulary.NATIONAL_ID, nationalIdProvider(locale))
                .build();

        assertEquals(
                "m1", context.provider(Vocabulary.FIRST_NAME, fallbackLocale).getMaleFirstNames()[0]);
        assertEquals(
                "l1", context.provider(Vocabulary.LAST_NAME, fallbackLocale).getLastNames()[0]);
        assertEquals("male", context.provider(Vocabulary.GENDER, fallbackLocale).getMaleLabel());
        assertEquals("Dr", context.provider(Vocabulary.TITLE, fallbackLocale).getTitles()[0]);
        assertEquals("Jr", context.provider(Vocabulary.SUFFIX, fallbackLocale).getSuffixes()[0]);
        assertEquals(
                "Engineer",
                context.provider(Vocabulary.PROFESSION, fallbackLocale).getProfessions()[0]);
        assertEquals(
                "CityOne", context.provider(Vocabulary.CITY, fallbackLocale).getCities()[0]);
        assertEquals(
                "StateOne", context.provider(Vocabulary.STATE, fallbackLocale).getStates()[0]);
        assertEquals(
                "CountryOne",
                context.provider(Vocabulary.COUNTRY, fallbackLocale).getCountries()[0]);
        assertEquals(
                "Main",
                context.provider(Vocabulary.STREET_ADDRESS, fallbackLocale).getStreetNames()[0]);
        // National identifiers are country-specific: another country never borrows them by language.
        assertNull(context.provider(Vocabulary.NATIONAL_ID, fallbackLocale));
        assertEquals(
                "NID", context.provider(Vocabulary.NATIONAL_ID, Locale.ENGLISH).generate(new Random(1L)));

        assertTrue(context.registeredKeys(Vocabulary.FIRST_NAME).contains("en_US"));
        assertTrue(context.registeredKeys(Vocabulary.FIRST_NAME).contains("en"));
        assertTrue(context.registeredKeys(Vocabulary.LAST_NAME).contains("en_US"));
        assertTrue(context.registeredKeys(Vocabulary.GENDER).contains("en_US"));
        assertTrue(context.registeredKeys(Vocabulary.TITLE).contains("en_US"));
        assertTrue(context.registeredKeys(Vocabulary.SUFFIX).contains("en_US"));
        assertTrue(context.registeredKeys(Vocabulary.PROFESSION).contains("en_US"));
        assertTrue(context.registeredKeys(Vocabulary.CITY).contains("en_US"));
        assertTrue(context.registeredKeys(Vocabulary.STATE).contains("en_US"));
        assertTrue(context.registeredKeys(Vocabulary.COUNTRY).contains("en_US"));
        assertTrue(context.registeredKeys(Vocabulary.STREET_ADDRESS).contains("en_US"));
        assertTrue(context.registeredKeys(Vocabulary.NATIONAL_ID).contains("en_US"));
        assertTrue(context.registeredKeys(Vocabulary.NATIONAL_ID).contains("en"));
    }

    @Test
    @DisplayName("language-only locale registration stores language key")
    void languageOnlyRegistration() {
        DataRegistryContext context = DataRegistryContext.builder()
                .isolated()
                .register(Vocabulary.COUNTRY, countryProvider(Locale.of("en")))
                .build();
        assertTrue(context.registeredKeys(Vocabulary.COUNTRY).contains("en"));
        assertEquals(
                "CountryOne",
                context.provider(Vocabulary.COUNTRY, Locale.of("en", "GB")).getCountries()[0]);
    }

    @Test
    @DisplayName("weather providers remain isolated between contexts")
    void weatherProvidersRemainIsolatedBetweenContexts() {
        DataRegistryContext first = DataRegistryContext.builder()
                .isolated()
                .register(Vocabulary.WEATHER, weatherProvider(Locale.US, "Sun"))
                .build();
        DataRegistryContext second = DataRegistryContext.builder()
                .isolated()
                .register(Vocabulary.WEATHER, weatherProvider(Locale.US, "Rain"))
                .build();

        assertEquals(
                "Sun",
                new WeatherGenerator(
                                GeneratorConfig.builder().registryContext(first).build())
                        .generate());
        assertEquals(
                "Rain",
                new WeatherGenerator(GeneratorConfig.builder()
                                .registryContext(second)
                                .build())
                        .generate());
        assertEquals(Set.of("en", "en_US"), first.registeredKeys(Vocabulary.WEATHER));

        DataRegistryContext empty = DataRegistryContext.builder().isolated().build();
        assertNull(empty.provider(Vocabulary.WEATHER, Locale.US));
        assertFalse(empty.isRegistered(Vocabulary.WEATHER, Locale.US));
        assertTrue(DataRegistryContext.globalDefault().isRegistered(Vocabulary.WEATHER, Locale.of("ru", "RU")));
        assertFalse(DataRegistryContext.globalDefault()
                .registeredKeys(Vocabulary.WEATHER)
                .isEmpty());
    }

    @Test
    @DisplayName("measurement providers remain isolated between contexts")
    void measurementProvidersRemainIsolatedBetweenContexts() {
        DataRegistryContext first = DataRegistryContext.builder()
                .isolated()
                .register(Vocabulary.MEASUREMENT, measurementProvider(Locale.US, "Meter"))
                .build();
        DataRegistryContext second = DataRegistryContext.builder()
                .isolated()
                .register(Vocabulary.MEASUREMENT, measurementProvider(Locale.US, "Yard"))
                .build();

        assertEquals(
                "Meter",
                new MeasurementGenerator(
                                GeneratorConfig.builder().registryContext(first).build())
                        .generate());
        assertEquals(
                "Yard",
                new MeasurementGenerator(GeneratorConfig.builder()
                                .registryContext(second)
                                .build())
                        .generate());
        assertEquals(Set.of("en", "en_US"), first.registeredKeys(Vocabulary.MEASUREMENT));
        DataRegistryContext empty = DataRegistryContext.builder().isolated().build();
        assertTrue(first.isRegistered(Vocabulary.MEASUREMENT, Locale.US));
        assertNull(empty.provider(Vocabulary.MEASUREMENT, Locale.US));
        assertFalse(empty.isRegistered(Vocabulary.MEASUREMENT, Locale.US));
        assertFalse(DataRegistryContext.globalDefault()
                .registeredKeys(Vocabulary.MEASUREMENT)
                .isEmpty());
    }

    @Test
    @DisplayName("financial-term providers remain isolated between contexts")
    void financialTermProvidersRemainIsolatedBetweenContexts() {
        DataRegistryContext first = DataRegistryContext.builder()
                .isolated()
                .register(Vocabulary.FINANCIAL_TERM, financialTermProvider(Locale.US, "Asset"))
                .build();
        DataRegistryContext second = DataRegistryContext.builder()
                .isolated()
                .register(Vocabulary.FINANCIAL_TERM, financialTermProvider(Locale.US, "Dividend"))
                .build();

        assertEquals(
                "Asset",
                new FinancialTermGenerator(
                                GeneratorConfig.builder().registryContext(first).build())
                        .generate());
        assertEquals(
                "Dividend",
                new FinancialTermGenerator(GeneratorConfig.builder()
                                .registryContext(second)
                                .build())
                        .generate());
        assertEquals(Set.of("en", "en_US"), first.registeredKeys(Vocabulary.FINANCIAL_TERM));
        DataRegistryContext empty = DataRegistryContext.builder().isolated().build();
        assertTrue(first.isRegistered(Vocabulary.FINANCIAL_TERM, Locale.US));
        assertNull(empty.provider(Vocabulary.FINANCIAL_TERM, Locale.US));
        assertFalse(empty.isRegistered(Vocabulary.FINANCIAL_TERM, Locale.US));
        assertFalse(DataRegistryContext.globalDefault()
                .registeredKeys(Vocabulary.FINANCIAL_TERM)
                .isEmpty());
    }

    @Test
    @DisplayName("restaurant-type providers remain isolated between contexts")
    void restaurantTypeProvidersRemainIsolatedBetweenContexts() {
        DataRegistryContext first = DataRegistryContext.builder()
                .isolated()
                .register(Vocabulary.RESTAURANT_TYPE, restaurantTypeProvider(Locale.US, "Italian"))
                .build();
        DataRegistryContext second = DataRegistryContext.builder()
                .isolated()
                .register(Vocabulary.RESTAURANT_TYPE, restaurantTypeProvider(Locale.US, "Steakhouse"))
                .build();

        assertEquals(
                "Italian",
                new RestaurantTypeGenerator(
                                GeneratorConfig.builder().registryContext(first).build())
                        .generate());
        assertEquals(
                "Steakhouse",
                new RestaurantTypeGenerator(GeneratorConfig.builder()
                                .registryContext(second)
                                .build())
                        .generate());
        assertEquals(Set.of("en", "en_US"), first.registeredKeys(Vocabulary.RESTAURANT_TYPE));
        DataRegistryContext empty = DataRegistryContext.builder().isolated().build();
        assertTrue(first.isRegistered(Vocabulary.RESTAURANT_TYPE, Locale.US));
        assertNull(empty.provider(Vocabulary.RESTAURANT_TYPE, Locale.US));
        assertFalse(empty.isRegistered(Vocabulary.RESTAURANT_TYPE, Locale.US));
        assertFalse(DataRegistryContext.globalDefault()
                .registeredKeys(Vocabulary.RESTAURANT_TYPE)
                .isEmpty());
    }

    @Test
    @DisplayName("hobby providers remain isolated between contexts")
    void hobbyProvidersRemainIsolatedBetweenContexts() {
        DataRegistryContext first = DataRegistryContext.builder()
                .isolated()
                .register(Vocabulary.HOBBY, hobbyProvider(Locale.US, "Chess"))
                .build();
        DataRegistryContext second = DataRegistryContext.builder()
                .isolated()
                .register(Vocabulary.HOBBY, hobbyProvider(Locale.US, "Running"))
                .build();

        assertEquals(
                "Chess",
                new HobbyGenerator(
                                GeneratorConfig.builder().registryContext(first).build())
                        .generate());
        assertEquals(
                "Running",
                new HobbyGenerator(GeneratorConfig.builder()
                                .registryContext(second)
                                .build())
                        .generate());
        assertEquals(Set.of("en", "en_US"), first.registeredKeys(Vocabulary.HOBBY));
        DataRegistryContext empty = DataRegistryContext.builder().isolated().build();
        assertTrue(first.isRegistered(Vocabulary.HOBBY, Locale.US));
        assertNull(empty.provider(Vocabulary.HOBBY, Locale.US));
        assertFalse(empty.isRegistered(Vocabulary.HOBBY, Locale.US));
        assertFalse(DataRegistryContext.globalDefault()
                .registeredKeys(Vocabulary.HOBBY)
                .isEmpty());
    }

    @Test
    @DisplayName("nationality providers remain isolated between contexts")
    void nationalityProvidersRemainIsolatedBetweenContexts() {
        DataRegistryContext first = DataRegistryContext.builder()
                .isolated()
                .register(Vocabulary.NATIONALITY, nationalityProvider(Locale.US, "Canadian"))
                .build();
        DataRegistryContext second = DataRegistryContext.builder()
                .isolated()
                .register(Vocabulary.NATIONALITY, nationalityProvider(Locale.US, "Brazilian"))
                .build();

        assertEquals(
                "Canadian",
                new NationalityGenerator(
                                GeneratorConfig.builder().registryContext(first).build())
                        .generate());
        assertEquals(
                "Brazilian",
                new NationalityGenerator(GeneratorConfig.builder()
                                .registryContext(second)
                                .build())
                        .generate());
        assertEquals(Set.of("en", "en_US"), first.registeredKeys(Vocabulary.NATIONALITY));
        DataRegistryContext empty = DataRegistryContext.builder().isolated().build();
        assertTrue(first.isRegistered(Vocabulary.NATIONALITY, Locale.US));
        assertNull(empty.provider(Vocabulary.NATIONALITY, Locale.US));
        assertFalse(empty.isRegistered(Vocabulary.NATIONALITY, Locale.US));
        assertFalse(DataRegistryContext.globalDefault()
                .registeredKeys(Vocabulary.NATIONALITY)
                .isEmpty());
    }

    @Test
    @DisplayName("pronoun providers remain isolated between contexts")
    void pronounProvidersRemainIsolatedBetweenContexts() {
        DataRegistryContext first = DataRegistryContext.builder()
                .isolated()
                .register(Vocabulary.PRONOUN, pronounProvider(Locale.US, "she/her"))
                .build();
        DataRegistryContext second = DataRegistryContext.builder()
                .isolated()
                .register(Vocabulary.PRONOUN, pronounProvider(Locale.US, "they/them"))
                .build();

        assertEquals(
                "she/her",
                new PronounGenerator(
                                GeneratorConfig.builder().registryContext(first).build())
                        .generate());
        assertEquals(
                "they/them",
                new PronounGenerator(GeneratorConfig.builder()
                                .registryContext(second)
                                .build())
                        .generate());
        assertEquals(Set.of("en", "en_US"), first.registeredKeys(Vocabulary.PRONOUN));
        DataRegistryContext empty = DataRegistryContext.builder().isolated().build();
        assertTrue(first.isRegistered(Vocabulary.PRONOUN, Locale.US));
        assertNull(empty.provider(Vocabulary.PRONOUN, Locale.US));
        assertFalse(empty.isRegistered(Vocabulary.PRONOUN, Locale.US));
        assertFalse(DataRegistryContext.globalDefault()
                .registeredKeys(Vocabulary.PRONOUN)
                .isEmpty());
    }

    @Test
    @DisplayName("pronoun providers reject values without one subject/object separator")
    void pronounProvidersRejectMalformedSets() {
        assertThrows(
                IllegalArgumentException.class,
                () -> DataRegistryContext.builder().register(Vocabulary.PRONOUN, pronounProvider(Locale.US, "/them")));
        assertThrows(
                IllegalArgumentException.class,
                () -> DataRegistryContext.builder().register(Vocabulary.PRONOUN, pronounProvider(Locale.US, "they/")));
        assertThrows(
                IllegalArgumentException.class,
                () -> DataRegistryContext.builder()
                        .register(Vocabulary.PRONOUN, pronounProvider(Locale.US, "they/them/us")));
    }

    @Test
    @DisplayName("blood-type providers remain isolated between contexts")
    void bloodTypeProvidersRemainIsolatedBetweenContexts() {
        DataRegistryContext first = DataRegistryContext.builder()
                .isolated()
                .register(
                        Vocabulary.BLOOD_TYPE,
                        bloodTypeProvider(Locale.US, java.util.List.of("O+"), java.util.List.of(1)))
                .build();
        DataRegistryContext second = DataRegistryContext.builder()
                .isolated()
                .register(
                        Vocabulary.BLOOD_TYPE,
                        bloodTypeProvider(Locale.US, java.util.List.of("AB-"), java.util.List.of(1)))
                .build();

        assertEquals(
                "O+",
                new BloodTypeGenerator(
                                GeneratorConfig.builder().registryContext(first).build())
                        .generate());
        assertEquals(
                "AB-",
                new BloodTypeGenerator(GeneratorConfig.builder()
                                .registryContext(second)
                                .build())
                        .generate());
        assertEquals(Set.of("en", "en_US"), first.registeredKeys(Vocabulary.BLOOD_TYPE));
        DataRegistryContext empty = DataRegistryContext.builder().isolated().build();
        assertTrue(first.isRegistered(Vocabulary.BLOOD_TYPE, Locale.US));
        assertNull(empty.provider(Vocabulary.BLOOD_TYPE, Locale.US));
        assertFalse(empty.isRegistered(Vocabulary.BLOOD_TYPE, Locale.US));
        assertFalse(DataRegistryContext.globalDefault()
                .registeredKeys(Vocabulary.BLOOD_TYPE)
                .isEmpty());
    }

    @Test
    @DisplayName("blood-type providers require parallel positive weights")
    void bloodTypeProvidersRejectInvalidDistributions() {
        assertThrows(
                IllegalArgumentException.class,
                () -> DataRegistryContext.builder()
                        .register(
                                Vocabulary.BLOOD_TYPE,
                                bloodTypeProvider(Locale.US, java.util.List.of("O+"), java.util.List.of(1, 2))));
        assertThrows(
                IllegalArgumentException.class,
                () -> DataRegistryContext.builder()
                        .register(
                                Vocabulary.BLOOD_TYPE,
                                bloodTypeProvider(Locale.US, java.util.List.of("O+"), java.util.List.of(0))));
        assertThrows(
                IllegalArgumentException.class,
                () -> DataRegistryContext.builder()
                        .register(
                                Vocabulary.BLOOD_TYPE,
                                bloodTypeProvider(
                                        Locale.US,
                                        java.util.List.of("O+"),
                                        java.util.Collections.singletonList(null))));
        assertThrows(
                NullPointerException.class,
                () -> DataRegistryContext.builder()
                        .register(Vocabulary.BLOOD_TYPE, bloodTypeProvider(Locale.US, java.util.List.of("O+"), null)));
    }

    @Test
    @DisplayName("Chinese-zodiac providers remain isolated between contexts")
    void chineseZodiacProvidersRemainIsolatedBetweenContexts() {
        DataRegistryContext first = DataRegistryContext.builder()
                .isolated()
                .register(Vocabulary.CHINESE_ZODIAC, chineseZodiacProvider(Locale.US, "First Dragon"))
                .build();
        DataRegistryContext second = DataRegistryContext.builder()
                .isolated()
                .register(Vocabulary.CHINESE_ZODIAC, chineseZodiacProvider(Locale.US, "Second Dragon"))
                .build();

        assertEquals(
                "First Dragon",
                new ChineseZodiacGenerator(
                                GeneratorConfig.builder().registryContext(first).build())
                        .animalFor(2024));
        assertEquals(
                "Second Dragon",
                new ChineseZodiacGenerator(GeneratorConfig.builder()
                                .registryContext(second)
                                .build())
                        .animalFor(2024));
        assertEquals(Set.of("en", "en_US"), first.registeredKeys(Vocabulary.CHINESE_ZODIAC));
        DataRegistryContext empty = DataRegistryContext.builder().isolated().build();
        assertTrue(first.isRegistered(Vocabulary.CHINESE_ZODIAC, Locale.US));
        assertNull(empty.provider(Vocabulary.CHINESE_ZODIAC, Locale.US));
        assertFalse(empty.isRegistered(Vocabulary.CHINESE_ZODIAC, Locale.US));
        assertFalse(DataRegistryContext.globalDefault()
                .registeredKeys(Vocabulary.CHINESE_ZODIAC)
                .isEmpty());
    }

    @Test
    @DisplayName("Chinese-zodiac providers require the complete ordered cycle")
    void chineseZodiacProvidersRejectIncompleteCycles() {
        assertThrows(
                IllegalArgumentException.class,
                () -> DataRegistryContext.builder()
                        .register(Vocabulary.CHINESE_ZODIAC, chineseZodiacProvider(Locale.US, "only", 11)));
    }

    @Test
    @DisplayName("Western-zodiac providers remain isolated between contexts")
    void zodiacProvidersRemainIsolatedBetweenContexts() {
        DataRegistryContext first = DataRegistryContext.builder()
                .isolated()
                .register(Vocabulary.ZODIAC, zodiacProvider(Locale.US, "First Aries"))
                .build();
        DataRegistryContext second = DataRegistryContext.builder()
                .isolated()
                .register(Vocabulary.ZODIAC, zodiacProvider(Locale.US, "Second Aries"))
                .build();

        assertEquals(
                "First Aries",
                new ZodiacGenerator(
                                GeneratorConfig.builder().registryContext(first).build())
                        .signFor(java.time.LocalDate.of(2024, 3, 21)));
        assertEquals(
                "Second Aries",
                new ZodiacGenerator(GeneratorConfig.builder()
                                .registryContext(second)
                                .build())
                        .signFor(java.time.LocalDate.of(2024, 3, 21)));
        assertEquals(Set.of("en", "en_US"), first.registeredKeys(Vocabulary.ZODIAC));
        DataRegistryContext empty = DataRegistryContext.builder().isolated().build();
        assertTrue(first.isRegistered(Vocabulary.ZODIAC, Locale.US));
        assertNull(empty.provider(Vocabulary.ZODIAC, Locale.US));
        assertFalse(empty.isRegistered(Vocabulary.ZODIAC, Locale.US));
        assertFalse(DataRegistryContext.globalDefault()
                .registeredKeys(Vocabulary.ZODIAC)
                .isEmpty());
    }

    @Test
    @DisplayName("Western-zodiac providers require the complete ordered cycle")
    void zodiacProvidersRejectIncompleteCycles() {
        assertThrows(
                IllegalArgumentException.class,
                () -> DataRegistryContext.builder().register(Vocabulary.ZODIAC, zodiacProvider(Locale.US, "only", 11)));
    }

    @Test
    @DisplayName("locale provider families prefer exact values then language fallbacks")
    void localeProviderFamiliesPreferExactValuesThenLanguageFallbacks() {
        FirstNameDataProvider languageFirstNames = firstNameProvider(Locale.ENGLISH);
        FirstNameDataProvider usFirstNames = firstNameProvider(Locale.US);
        WeatherDataProvider languageWeather = weatherProvider(Locale.ENGLISH, "English weather");
        WeatherDataProvider usWeather = weatherProvider(Locale.US, "US weather");
        BloodTypeDataProvider languageBloodTypes =
                bloodTypeProvider(Locale.ENGLISH, List.of("English blood"), List.of(1));
        BloodTypeDataProvider usBloodTypes = bloodTypeProvider(Locale.US, List.of("US blood"), List.of(1));
        ZodiacDataProvider languageZodiac = zodiacProvider(Locale.ENGLISH, "English aries");
        ZodiacDataProvider usZodiac = zodiacProvider(Locale.US, "US aries");

        DataRegistryContext context = DataRegistryContext.builder()
                .isolated()
                .register(Vocabulary.FIRST_NAME, languageFirstNames)
                .register(Vocabulary.FIRST_NAME, usFirstNames)
                .register(Vocabulary.WEATHER, languageWeather)
                .register(Vocabulary.WEATHER, usWeather)
                .register(Vocabulary.BLOOD_TYPE, languageBloodTypes)
                .register(Vocabulary.BLOOD_TYPE, usBloodTypes)
                .register(Vocabulary.ZODIAC, languageZodiac)
                .register(Vocabulary.ZODIAC, usZodiac)
                .build();

        assertSame(usFirstNames, context.provider(Vocabulary.FIRST_NAME, Locale.US));
        assertSame(languageFirstNames, context.provider(Vocabulary.FIRST_NAME, Locale.CANADA));
        assertSame(usWeather, context.provider(Vocabulary.WEATHER, Locale.US));
        assertSame(languageWeather, context.provider(Vocabulary.WEATHER, Locale.CANADA));
        assertSame(usBloodTypes, context.provider(Vocabulary.BLOOD_TYPE, Locale.US));
        assertSame(languageBloodTypes, context.provider(Vocabulary.BLOOD_TYPE, Locale.CANADA));
        assertSame(usZodiac, context.provider(Vocabulary.ZODIAC, Locale.US));
        assertSame(languageZodiac, context.provider(Vocabulary.ZODIAC, Locale.CANADA));
    }

    @Test
    @DisplayName("lookups prefer scoped exact, then global exact, then scoped language, then global language")
    void scopedRegionalDataNeverOverridesGlobalExactMatches() {
        FirstNameDataProvider indianEnglish = firstNameProvider(Locale.of("en", "IN"));
        FirstNameDataProvider germanOnly = firstNameProvider(Locale.GERMAN);
        DataRegistryContext context = DataRegistryContext.builder()
                .register(Vocabulary.FIRST_NAME, indianEnglish)
                .register(Vocabulary.FIRST_NAME, germanOnly)
                .build();

        assertSame(indianEnglish, context.provider(Vocabulary.FIRST_NAME, Locale.of("en", "IN")));
        assertSame(FirstNameDataRegistry.forLocale(Locale.US), context.provider(Vocabulary.FIRST_NAME, Locale.US));
        assertSame(FirstNameDataRegistry.forLocale(Locale.UK), context.provider(Vocabulary.FIRST_NAME, Locale.UK));
        assertSame(
                FirstNameDataRegistry.forLocale(Locale.GERMANY),
                context.provider(Vocabulary.FIRST_NAME, Locale.GERMANY));
        assertSame(indianEnglish, context.provider(Vocabulary.FIRST_NAME, Locale.of("en", "SG")));
        assertSame(germanOnly, context.provider(Vocabulary.FIRST_NAME, Locale.of("de", "LI")));
        assertSame(indianEnglish, context.provider(Vocabulary.FIRST_NAME, Locale.ENGLISH));
        assertSame(
                FirstNameDataRegistry.forLocale(Locale.of("fr", "LU")),
                context.provider(Vocabulary.FIRST_NAME, Locale.of("fr", "LU")));
        assertNull(context.provider(Vocabulary.FIRST_NAME, null));
    }

    @Test
    @DisplayName("a scoped regional bundle does not replace default-locale names")
    void scopedRegionalBundleDoesNotReplaceDefaultLocaleNames() {
        DataRegistryContext context = DataRegistryContext.builder()
                .registerLocaleData(
                        io.github.frikit.krandom.generator.locale.LocaleDataBundle.builder(Locale.of("en", "IN"))
                                .firstNames(new String[] {"Arjun"}, new String[] {"Priya"})
                                .lastNames("Sharma")
                                .build())
                .build();
        GeneratorConfig usConfig =
                GeneratorConfig.builder().seed(7L).registryContext(context).build();
        GeneratorConfig indiaConfig = GeneratorConfig.builder()
                .seed(7L)
                .locale(Locale.of("en", "IN"))
                .registryContext(context)
                .build();

        List<String> usLastNames =
                new io.github.frikit.krandom.generator.user.LastNameGenerator(usConfig).generateList(50);

        assertFalse(usLastNames.contains("Sharma"), usLastNames.toString());
        assertEquals(
                new io.github.frikit.krandom.generator.user.LastNameGenerator(
                                GeneratorConfig.builder().seed(7L).build())
                        .generateList(50),
                usLastNames);
        assertEquals(
                List.of("Sharma", "Sharma"),
                new io.github.frikit.krandom.generator.user.LastNameGenerator(indiaConfig).generateList(2));
    }

    @Test
    @DisplayName("concurrent isolated contexts do not leak provider data")
    void concurrentIsolatedContextsDoNotLeakProviderData() throws Exception {
        DataRegistryContext first = scopedVocabularyContext("first");
        DataRegistryContext second = scopedVocabularyContext("second");

        try (ExecutorService executor = Executors.newFixedThreadPool(2)) {
            Future<List<String>> firstOutput = executor.submit(() -> generateScopedVocabulary(first));
            Future<List<String>> secondOutput = executor.submit(() -> generateScopedVocabulary(second));

            assertEquals(Collections.nCopies(200, expectedScopedVocabulary("first")), firstOutput.get());
            assertEquals(Collections.nCopies(200, expectedScopedVocabulary("second")), secondOutput.get());
        }
    }

    @Test
    @DisplayName("builder validates provider inputs")
    void builderValidatesInput() {
        Locale locale = Locale.US;
        DataRegistryContext.Builder builder = DataRegistryContext.builder().isolated();

        assertThrows(NullPointerException.class, () -> builder.register(Vocabulary.FIRST_NAME, null));
        assertThrows(
                IllegalArgumentException.class,
                () -> builder.register(Vocabulary.FIRST_NAME, new FirstNameDataProvider() {
                    @Override
                    public Locale getLocale() {
                        return locale;
                    }

                    @Override
                    public String[] getMaleFirstNames() {
                        return new String[0];
                    }

                    @Override
                    public String[] getFemaleFirstNames() {
                        return new String[] {"f1"};
                    }
                }));

        assertThrows(
                IllegalArgumentException.class,
                () -> builder.register(Vocabulary.LAST_NAME, new LastNameDataProvider() {
                    @Override
                    public Locale getLocale() {
                        return locale;
                    }

                    @Override
                    public String[] getLastNames() {
                        return new String[0];
                    }
                }));

        assertThrows(
                IllegalArgumentException.class,
                () -> builder.register(Vocabulary.GENDER, new GenderDataProvider() {
                    @Override
                    public Locale getLocale() {
                        return locale;
                    }

                    @Override
                    public String getMaleLabel() {
                        return " ";
                    }

                    @Override
                    public String getFemaleLabel() {
                        return "female";
                    }
                }));
        assertThrows(
                IllegalArgumentException.class,
                () -> builder.register(Vocabulary.GENDER, new GenderDataProvider() {
                    @Override
                    public Locale getLocale() {
                        return locale;
                    }

                    @Override
                    public String getMaleLabel() {
                        return null;
                    }

                    @Override
                    public String getFemaleLabel() {
                        return "female";
                    }
                }));

        assertThrows(
                IllegalArgumentException.class,
                () -> builder.register(Vocabulary.TITLE, new TitleDataProvider() {
                    @Override
                    public Locale getLocale() {
                        return locale;
                    }

                    @Override
                    public String[] getTitles() {
                        return new String[0];
                    }
                }));

        assertThrows(
                IllegalArgumentException.class,
                () -> builder.register(Vocabulary.SUFFIX, new SuffixDataProvider() {
                    @Override
                    public Locale getLocale() {
                        return locale;
                    }

                    @Override
                    public String[] getSuffixes() {
                        return new String[0];
                    }
                }));

        assertThrows(
                IllegalArgumentException.class,
                () -> builder.register(Vocabulary.PROFESSION, new ProfessionDataProvider() {
                    @Override
                    public Locale getLocale() {
                        return locale;
                    }

                    @Override
                    public String[] getProfessions() {
                        return new String[] {"Engineer"};
                    }

                    @Override
                    public int[] getWeights() {
                        return new int[0];
                    }
                }));
        assertThrows(
                IllegalArgumentException.class,
                () -> builder.register(Vocabulary.PROFESSION, new ProfessionDataProvider() {
                    @Override
                    public Locale getLocale() {
                        return locale;
                    }

                    @Override
                    public String[] getProfessions() {
                        return new String[0];
                    }

                    @Override
                    public int[] getWeights() {
                        return new int[0];
                    }
                }));
        assertThrows(
                IllegalArgumentException.class,
                () -> builder.register(Vocabulary.PROFESSION, new ProfessionDataProvider() {
                    @Override
                    public Locale getLocale() {
                        return locale;
                    }

                    @Override
                    public String[] getProfessions() {
                        return new String[] {"  "};
                    }

                    @Override
                    public int[] getWeights() {
                        return new int[] {1};
                    }
                }));
        assertThrows(
                IllegalArgumentException.class,
                () -> builder.register(Vocabulary.PROFESSION, new ProfessionDataProvider() {
                    @Override
                    public Locale getLocale() {
                        return locale;
                    }

                    @Override
                    public String[] getProfessions() {
                        return new String[] {null};
                    }

                    @Override
                    public int[] getWeights() {
                        return new int[] {1};
                    }
                }));
        assertThrows(
                IllegalArgumentException.class,
                () -> builder.register(Vocabulary.PROFESSION, new ProfessionDataProvider() {
                    @Override
                    public Locale getLocale() {
                        return locale;
                    }

                    @Override
                    public String[] getProfessions() {
                        return new String[] {"Engineer"};
                    }

                    @Override
                    public int[] getWeights() {
                        return new int[] {0};
                    }
                }));

        assertThrows(
                NullPointerException.class,
                () -> builder.register(Vocabulary.CITY, new CityDataProvider() {
                    @Override
                    public Locale getLocale() {
                        return null;
                    }

                    @Override
                    public String[] getCities() {
                        return new String[] {"CityOne"};
                    }
                }));
        assertThrows(
                IllegalArgumentException.class,
                () -> builder.register(Vocabulary.CITY, new CityDataProvider() {
                    @Override
                    public Locale getLocale() {
                        return locale;
                    }

                    @Override
                    public String[] getCities() {
                        return new String[] {" "};
                    }
                }));
        assertThrows(
                IllegalArgumentException.class,
                () -> builder.register(Vocabulary.CITY, new CityDataProvider() {
                    @Override
                    public Locale getLocale() {
                        return locale;
                    }

                    @Override
                    public String[] getCities() {
                        return new String[] {null};
                    }
                }));

        assertThrows(
                NullPointerException.class,
                () -> builder.register(Vocabulary.STATE, new StateDataProvider() {
                    @Override
                    public Locale getLocale() {
                        return null;
                    }

                    @Override
                    public String[] getStates() {
                        return new String[] {"StateOne"};
                    }

                    @Override
                    public String[] getAbbreviations() {
                        return new String[] {"S1"};
                    }
                }));
        assertThrows(
                IllegalArgumentException.class,
                () -> builder.register(Vocabulary.STATE, new StateDataProvider() {
                    @Override
                    public Locale getLocale() {
                        return locale;
                    }

                    @Override
                    public String[] getStates() {
                        return new String[] {""};
                    }

                    @Override
                    public String[] getAbbreviations() {
                        return new String[] {"S1"};
                    }
                }));
        assertThrows(
                IllegalArgumentException.class,
                () -> builder.register(Vocabulary.STATE, new StateDataProvider() {
                    @Override
                    public Locale getLocale() {
                        return locale;
                    }

                    @Override
                    public String[] getStates() {
                        return new String[] {null};
                    }

                    @Override
                    public String[] getAbbreviations() {
                        return new String[] {"S1"};
                    }
                }));
        assertThrows(
                IllegalArgumentException.class,
                () -> builder.register(Vocabulary.STATE, new StateDataProvider() {
                    @Override
                    public Locale getLocale() {
                        return locale;
                    }

                    @Override
                    public String[] getStates() {
                        return new String[] {"StateOne"};
                    }

                    @Override
                    public String[] getAbbreviations() {
                        return new String[] {null};
                    }
                }));

        assertThrows(
                NullPointerException.class,
                () -> builder.register(Vocabulary.COUNTRY, new CountryDataProvider() {
                    @Override
                    public Locale getLocale() {
                        return null;
                    }

                    @Override
                    public String[] getCountries() {
                        return new String[] {"CountryOne"};
                    }
                }));
        assertThrows(
                IllegalArgumentException.class,
                () -> builder.register(Vocabulary.COUNTRY, new CountryDataProvider() {
                    @Override
                    public Locale getLocale() {
                        return locale;
                    }

                    @Override
                    public String[] getCountries() {
                        return new String[] {" "};
                    }
                }));
        assertThrows(
                IllegalArgumentException.class,
                () -> builder.register(Vocabulary.COUNTRY, new CountryDataProvider() {
                    @Override
                    public Locale getLocale() {
                        return locale;
                    }

                    @Override
                    public String[] getCountries() {
                        return new String[] {null};
                    }
                }));

        assertThrows(
                IllegalArgumentException.class,
                () -> builder.register(Vocabulary.STREET_ADDRESS, new StreetAddressDataProvider() {
                    @Override
                    public Locale getLocale() {
                        return locale;
                    }

                    @Override
                    public String[] getStreetNames() {
                        return new String[] {"Main"};
                    }

                    @Override
                    public String[] getStreetTypesShort() {
                        return new String[0];
                    }

                    @Override
                    public String[] getStreetTypesLong() {
                        return new String[] {"Street"};
                    }
                }));

        assertThrows(
                NullPointerException.class,
                () -> builder.register(Vocabulary.NATIONAL_ID, new NationalIdProvider() {
                    @Override
                    public Locale getLocale() {
                        return null;
                    }

                    @Override
                    public String generate(Random random) {
                        return "x";
                    }
                }));

        assertThrows(
                NullPointerException.class,
                () -> DataRegistryContext.builder()
                        .useGlobalFallback(true)
                        .register(Vocabulary.FIRST_NAME, new FirstNameDataProvider() {
                            @Override
                            public Locale getLocale() {
                                return null;
                            }

                            @Override
                            public String[] getMaleFirstNames() {
                                return new String[] {"m1"};
                            }

                            @Override
                            public String[] getFemaleFirstNames() {
                                return new String[] {"f1"};
                            }
                        }));

        assertThrows(NullPointerException.class, () -> builder.register(Vocabulary.WEATHER, null));
        assertThrows(
                IllegalArgumentException.class,
                () -> builder.register(Vocabulary.WEATHER, weatherProvider(locale, java.util.List.of())));
        assertThrows(
                IllegalArgumentException.class,
                () -> builder.register(Vocabulary.WEATHER, weatherProvider(locale, java.util.List.of(" "))));
        assertThrows(
                IllegalArgumentException.class,
                () -> builder.register(
                        Vocabulary.WEATHER, weatherProvider(locale, java.util.Collections.singletonList(null))));
    }

    private static FirstNameDataProvider firstNameProvider(Locale locale) {
        return new FirstNameDataProvider() {
            @Override
            public Locale getLocale() {
                return locale;
            }

            @Override
            public String[] getMaleFirstNames() {
                return new String[] {"m1"};
            }

            @Override
            public String[] getFemaleFirstNames() {
                return new String[] {"f1"};
            }
        };
    }

    private static LastNameDataProvider lastNameProvider(Locale locale) {
        return new LastNameDataProvider() {
            @Override
            public Locale getLocale() {
                return locale;
            }

            @Override
            public String[] getLastNames() {
                return new String[] {"l1"};
            }
        };
    }

    private static GenderDataProvider genderProvider(Locale locale) {
        return new GenderDataProvider() {
            @Override
            public Locale getLocale() {
                return locale;
            }

            @Override
            public String getMaleLabel() {
                return "male";
            }

            @Override
            public String getFemaleLabel() {
                return "female";
            }
        };
    }

    private static TitleDataProvider titleProvider(Locale locale) {
        return new TitleDataProvider() {
            @Override
            public Locale getLocale() {
                return locale;
            }

            @Override
            public String[] getTitles() {
                return new String[] {"Dr"};
            }
        };
    }

    private static SuffixDataProvider suffixProvider(Locale locale) {
        return new SuffixDataProvider() {
            @Override
            public Locale getLocale() {
                return locale;
            }

            @Override
            public String[] getSuffixes() {
                return new String[] {"Jr"};
            }
        };
    }

    private static ProfessionDataProvider professionProvider(Locale locale) {
        return new ProfessionDataProvider() {
            @Override
            public Locale getLocale() {
                return locale;
            }

            @Override
            public String[] getProfessions() {
                return new String[] {"Engineer"};
            }

            @Override
            public int[] getWeights() {
                return new int[] {1};
            }
        };
    }

    private static CityDataProvider cityProvider(Locale locale) {
        return new CityDataProvider() {
            @Override
            public Locale getLocale() {
                return locale;
            }

            @Override
            public String[] getCities() {
                return new String[] {"CityOne"};
            }
        };
    }

    private static StateDataProvider stateProvider(Locale locale) {
        return new StateDataProvider() {
            @Override
            public Locale getLocale() {
                return locale;
            }

            @Override
            public String[] getStates() {
                return new String[] {"StateOne"};
            }

            @Override
            public String[] getAbbreviations() {
                return new String[] {"S1"};
            }
        };
    }

    private static CountryDataProvider countryProvider(Locale locale) {
        return new CountryDataProvider() {
            @Override
            public Locale getLocale() {
                return locale;
            }

            @Override
            public String[] getCountries() {
                return new String[] {"CountryOne"};
            }
        };
    }

    private static StreetAddressDataProvider streetAddressProvider(Locale locale) {
        return new StreetAddressDataProvider() {
            @Override
            public Locale getLocale() {
                return locale;
            }

            @Override
            public String[] getStreetNames() {
                return new String[] {"Main"};
            }

            @Override
            public String[] getStreetTypesShort() {
                return new String[] {"St"};
            }

            @Override
            public String[] getStreetTypesLong() {
                return new String[] {"Street"};
            }
        };
    }

    private static NationalIdProvider nationalIdProvider(Locale locale) {
        return new NationalIdProvider() {
            @Override
            public Locale getLocale() {
                return locale;
            }

            @Override
            public String generate(Random random) {
                return "NID";
            }
        };
    }

    private static DataRegistryContext scopedVocabularyContext(String marker) {
        return DataRegistryContext.builder()
                .isolated()
                .register(Vocabulary.WEATHER, weatherProvider(Locale.US, marker + " weather"))
                .register(Vocabulary.MEASUREMENT, measurementProvider(Locale.US, marker + " unit"))
                .register(Vocabulary.FINANCIAL_TERM, financialTermProvider(Locale.US, marker + " term"))
                .register(Vocabulary.RESTAURANT_TYPE, restaurantTypeProvider(Locale.US, marker + " restaurant"))
                .register(Vocabulary.HOBBY, hobbyProvider(Locale.US, marker + " hobby"))
                .register(Vocabulary.NATIONALITY, nationalityProvider(Locale.US, marker + " nationality"))
                .register(Vocabulary.PRONOUN, pronounProvider(Locale.US, marker + "/pronoun"))
                .register(Vocabulary.BLOOD_TYPE, bloodTypeProvider(Locale.US, List.of(marker + " blood"), List.of(1)))
                .register(Vocabulary.CHINESE_ZODIAC, chineseZodiacProvider(Locale.US, marker + " dragon"))
                .register(Vocabulary.ZODIAC, zodiacProvider(Locale.US, marker + " aries"))
                .build();
    }

    private static List<String> generateScopedVocabulary(DataRegistryContext context) {
        GeneratorConfig config = GeneratorConfig.builder()
                .locale(Locale.US)
                .registryContext(context)
                .build();
        WeatherGenerator weather = new WeatherGenerator(config);
        MeasurementGenerator measurement = new MeasurementGenerator(config);
        FinancialTermGenerator financialTerm = new FinancialTermGenerator(config);
        RestaurantTypeGenerator restaurantType = new RestaurantTypeGenerator(config);
        HobbyGenerator hobby = new HobbyGenerator(config);
        NationalityGenerator nationality = new NationalityGenerator(config);
        PronounGenerator pronoun = new PronounGenerator(config);
        BloodTypeGenerator bloodType = new BloodTypeGenerator(config);
        ChineseZodiacGenerator chineseZodiac = new ChineseZodiacGenerator(config);
        ZodiacGenerator zodiac = new ZodiacGenerator(config);
        List<String> output = new ArrayList<>(200);

        for (int index = 0; index < 200; index++) {
            output.add(String.join(
                    "|",
                    weather.generate(),
                    measurement.generate(),
                    financialTerm.generate(),
                    restaurantType.generate(),
                    hobby.generate(),
                    nationality.generate(),
                    pronoun.generate(),
                    bloodType.generate(),
                    chineseZodiac.animalFor(2024),
                    zodiac.signFor(LocalDate.of(2024, 3, 21))));
        }
        return output;
    }

    private static String expectedScopedVocabulary(String marker) {
        return String.join(
                "|",
                marker + " weather",
                marker + " unit",
                marker + " term",
                marker + " restaurant",
                marker + " hobby",
                marker + " nationality",
                marker + "/pronoun",
                marker + " blood",
                marker + " dragon",
                marker + " aries");
    }

    private static WeatherDataProvider weatherProvider(Locale locale, String condition) {
        return weatherProvider(locale, java.util.List.of(condition));
    }

    private static WeatherDataProvider weatherProvider(Locale locale, java.util.List<String> conditions) {
        return new WeatherDataProvider() {
            @Override
            public Locale getLocale() {
                return locale;
            }

            @Override
            public java.util.List<String> getConditions() {
                return conditions;
            }
        };
    }

    private static MeasurementDataProvider measurementProvider(Locale locale, String unit) {
        return new MeasurementDataProvider() {
            @Override
            public Locale getLocale() {
                return locale;
            }

            @Override
            public java.util.List<String> getUnits() {
                return java.util.List.of(unit);
            }
        };
    }

    private static FinancialTermDataProvider financialTermProvider(Locale locale, String term) {
        return new FinancialTermDataProvider() {
            @Override
            public Locale getLocale() {
                return locale;
            }

            @Override
            public java.util.List<String> getTerms() {
                return java.util.List.of(term);
            }
        };
    }

    private static RestaurantTypeDataProvider restaurantTypeProvider(Locale locale, String type) {
        return new RestaurantTypeDataProvider() {
            @Override
            public Locale getLocale() {
                return locale;
            }

            @Override
            public java.util.List<String> getTypes() {
                return java.util.List.of(type);
            }
        };
    }

    private static HobbyDataProvider hobbyProvider(Locale locale, String hobby) {
        return new HobbyDataProvider() {
            @Override
            public Locale getLocale() {
                return locale;
            }

            @Override
            public java.util.List<String> getHobbies() {
                return java.util.List.of(hobby);
            }
        };
    }

    private static NationalityDataProvider nationalityProvider(Locale locale, String nationality) {
        return new NationalityDataProvider() {
            @Override
            public Locale getLocale() {
                return locale;
            }

            @Override
            public java.util.List<String> getNationalities() {
                return java.util.List.of(nationality);
            }
        };
    }

    private static PronounDataProvider pronounProvider(Locale locale, String pronounSet) {
        return new PronounDataProvider() {
            @Override
            public Locale getLocale() {
                return locale;
            }

            @Override
            public java.util.List<String> getPronounSets() {
                return java.util.List.of(pronounSet);
            }
        };
    }

    private static BloodTypeDataProvider bloodTypeProvider(
            Locale locale, java.util.List<String> types, java.util.List<Integer> weights) {
        return new BloodTypeDataProvider() {
            @Override
            public Locale getLocale() {
                return locale;
            }

            @Override
            public java.util.List<String> getTypes() {
                return types;
            }

            @Override
            public java.util.List<Integer> getWeights() {
                return weights;
            }
        };
    }

    private static ChineseZodiacDataProvider chineseZodiacProvider(Locale locale, String dragon) {
        return chineseZodiacProvider(locale, dragon, 12);
    }

    private static ChineseZodiacDataProvider chineseZodiacProvider(Locale locale, String dragon, int size) {
        java.util.List<String> animals = new java.util.ArrayList<>();
        for (int index = 0; index < size; index++) {
            animals.add(index == 8 ? dragon : "animal-" + index);
        }
        return new ChineseZodiacDataProvider() {
            @Override
            public Locale getLocale() {
                return locale;
            }

            @Override
            public java.util.List<String> getAnimals() {
                return animals;
            }
        };
    }

    private static ZodiacDataProvider zodiacProvider(Locale locale, String aries) {
        return zodiacProvider(locale, aries, 12);
    }

    private static ZodiacDataProvider zodiacProvider(Locale locale, String aries, int size) {
        java.util.List<String> signs = new java.util.ArrayList<>();
        for (int index = 0; index < size; index++) {
            signs.add(index == 0 ? aries : "sign-" + index);
        }
        return new ZodiacDataProvider() {
            @Override
            public Locale getLocale() {
                return locale;
            }

            @Override
            public java.util.List<String> getSigns() {
                return signs;
            }
        };
    }
}
