/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator;

import io.github.frikit.krandom.generator.commerce.RestaurantTypeDataProvider;
import io.github.frikit.krandom.generator.commerce.RestaurantTypeDataRegistry;
import io.github.frikit.krandom.generator.commerce.RestaurantTypeGenerator;
import io.github.frikit.krandom.generator.finance.FinancialTermDataProvider;
import io.github.frikit.krandom.generator.finance.FinancialTermDataRegistry;
import io.github.frikit.krandom.generator.finance.FinancialTermGenerator;
import io.github.frikit.krandom.generator.location.CityDataProvider;
import io.github.frikit.krandom.generator.location.CountryDataProvider;
import io.github.frikit.krandom.generator.location.StateDataProvider;
import io.github.frikit.krandom.generator.location.StreetAddressDataProvider;
import io.github.frikit.krandom.generator.measurement.MeasurementDataProvider;
import io.github.frikit.krandom.generator.measurement.MeasurementDataRegistry;
import io.github.frikit.krandom.generator.measurement.MeasurementGenerator;
import io.github.frikit.krandom.generator.user.FirstNameDataProvider;
import io.github.frikit.krandom.generator.user.FirstNameDataRegistry;
import io.github.frikit.krandom.generator.user.GenderDataProvider;
import io.github.frikit.krandom.generator.user.HobbyDataProvider;
import io.github.frikit.krandom.generator.user.HobbyDataRegistry;
import io.github.frikit.krandom.generator.user.HobbyGenerator;
import io.github.frikit.krandom.generator.user.BloodTypeDataProvider;
import io.github.frikit.krandom.generator.user.BloodTypeDataRegistry;
import io.github.frikit.krandom.generator.user.BloodTypeGenerator;
import io.github.frikit.krandom.generator.user.ChineseZodiacDataProvider;
import io.github.frikit.krandom.generator.user.ChineseZodiacDataRegistry;
import io.github.frikit.krandom.generator.user.ChineseZodiacGenerator;
import io.github.frikit.krandom.generator.user.LastNameDataProvider;
import io.github.frikit.krandom.generator.user.NationalityDataProvider;
import io.github.frikit.krandom.generator.user.NationalityDataRegistry;
import io.github.frikit.krandom.generator.user.NationalityGenerator;
import io.github.frikit.krandom.generator.user.ProfessionDataProvider;
import io.github.frikit.krandom.generator.user.PronounDataProvider;
import io.github.frikit.krandom.generator.user.PronounDataRegistry;
import io.github.frikit.krandom.generator.user.PronounGenerator;
import io.github.frikit.krandom.generator.user.SuffixDataProvider;
import io.github.frikit.krandom.generator.user.TitleDataProvider;
import io.github.frikit.krandom.generator.user.ZodiacDataProvider;
import io.github.frikit.krandom.generator.user.ZodiacDataRegistry;
import io.github.frikit.krandom.generator.user.ZodiacGenerator;
import io.github.frikit.krandom.generator.user.nationalid.NationalIdProvider;
import io.github.frikit.krandom.generator.weather.WeatherDataProvider;
import io.github.frikit.krandom.generator.weather.WeatherDataRegistry;
import io.github.frikit.krandom.generator.weather.WeatherGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("DataRegistryContext")
class DataRegistryContextTest {

    @Test
    @DisplayName("globalDefault delegates to static registries")
    void globalDefaultDelegatesToStaticRegistries() {
        DataRegistryContext context = DataRegistryContext.globalDefault();
        Locale locale = Locale.US;

        assertTrue(context.isFirstNameRegistered(locale));
        assertTrue(context.isLastNameRegistered(locale));
        assertTrue(context.isGenderRegistered(locale));
        assertTrue(context.isTitleRegistered(locale));
        assertTrue(context.isSuffixRegistered(locale));
        assertTrue(context.isProfessionRegistered(locale));
        assertTrue(context.isCityRegistered(locale));
        assertTrue(context.isStateRegistered(locale));
        assertTrue(context.isCountryRegistered(locale));
        assertTrue(context.isStreetAddressRegistered(locale));
        assertTrue(context.isNationalIdRegistered(locale));

        assertNotNull(context.firstNameProvider(locale));
        assertNotNull(context.lastNameProvider(locale));
        assertNotNull(context.genderProvider(locale));
        assertNotNull(context.titleProvider(locale));
        assertNotNull(context.suffixProvider(locale));
        assertNotNull(context.professionProvider(locale));
        assertNotNull(context.cityProvider(locale));
        assertNotNull(context.stateProvider(locale));
        assertNotNull(context.countryProvider(locale));
        assertNotNull(context.streetAddressProvider(locale));
        assertNotNull(context.nationalIdProvider(locale));

        assertFalse(context.firstNameRegisteredKeys().isEmpty());
        assertFalse(context.lastNameRegisteredKeys().isEmpty());
        assertFalse(context.genderRegisteredKeys().isEmpty());
        assertFalse(context.titleRegisteredKeys().isEmpty());
        assertFalse(context.suffixRegisteredKeys().isEmpty());
        assertFalse(context.professionRegisteredKeys().isEmpty());
        assertFalse(context.cityRegisteredKeys().isEmpty());
        assertFalse(context.stateRegisteredKeys().isEmpty());
        assertFalse(context.countryRegisteredKeys().isEmpty());
        assertFalse(context.streetAddressRegisteredKeys().isEmpty());
        assertFalse(context.nationalIdRegisteredKeys().isEmpty());
    }

    @Test
    @DisplayName("isolated context does not fallback to global registries")
    void isolatedContextWithoutFallback() {
        DataRegistryContext context = DataRegistryContext.builder().isolated().build();
        Locale locale = Locale.US;

        assertFalse(context.isFirstNameRegistered(locale));
        assertFalse(context.isLastNameRegistered(locale));
        assertFalse(context.isGenderRegistered(locale));
        assertFalse(context.isTitleRegistered(locale));
        assertFalse(context.isSuffixRegistered(locale));
        assertFalse(context.isProfessionRegistered(locale));
        assertFalse(context.isCityRegistered(locale));
        assertFalse(context.isStateRegistered(locale));
        assertFalse(context.isCountryRegistered(locale));
        assertFalse(context.isStreetAddressRegistered(locale));
        assertFalse(context.isNationalIdRegistered(locale));

        assertNull(context.firstNameProvider(locale));
        assertNull(context.lastNameProvider(locale));
        assertNull(context.genderProvider(locale));
        assertNull(context.titleProvider(locale));
        assertNull(context.suffixProvider(locale));
        assertNull(context.professionProvider(locale));
        assertNull(context.cityProvider(locale));
        assertNull(context.stateProvider(locale));
        assertNull(context.countryProvider(locale));
        assertNull(context.streetAddressProvider(locale));
        assertNull(context.nationalIdProvider(locale));
        assertNull(context.firstNameProvider(null));

        assertTrue(context.firstNameRegisteredKeys().isEmpty());
        assertTrue(context.lastNameRegisteredKeys().isEmpty());
        assertTrue(context.genderRegisteredKeys().isEmpty());
        assertTrue(context.titleRegisteredKeys().isEmpty());
        assertTrue(context.suffixRegisteredKeys().isEmpty());
        assertTrue(context.professionRegisteredKeys().isEmpty());
        assertTrue(context.cityRegisteredKeys().isEmpty());
        assertTrue(context.stateRegisteredKeys().isEmpty());
        assertTrue(context.countryRegisteredKeys().isEmpty());
        assertTrue(context.streetAddressRegisteredKeys().isEmpty());
        assertTrue(context.nationalIdRegisteredKeys().isEmpty());
    }

    @Test
    @DisplayName("builder registration supports exact and language fallback lookups")
    void builderRegistrationLookupAndKeys() {
        Locale locale = Locale.US;
        Locale fallbackLocale = Locale.CANADA;

        DataRegistryContext context = DataRegistryContext.builder()
            .isolated()
            .registerFirstNameProvider(firstNameProvider(locale))
            .registerLastNameProvider(lastNameProvider(locale))
            .registerGenderProvider(genderProvider(locale))
            .registerTitleProvider(titleProvider(locale))
            .registerSuffixProvider(suffixProvider(locale))
            .registerProfessionProvider(professionProvider(locale))
            .registerCityProvider(cityProvider(locale))
            .registerStateProvider(stateProvider(locale))
            .registerCountryProvider(countryProvider(locale))
            .registerStreetAddressProvider(streetAddressProvider(locale))
            .registerNationalIdProvider(nationalIdProvider(locale))
            .build();

        assertEquals("m1", context.firstNameProvider(fallbackLocale).getMaleFirstNames()[0]);
        assertEquals("l1", context.lastNameProvider(fallbackLocale).getLastNames()[0]);
        assertEquals("male", context.genderProvider(fallbackLocale).getMaleLabel());
        assertEquals("Dr", context.titleProvider(fallbackLocale).getTitles()[0]);
        assertEquals("Jr", context.suffixProvider(fallbackLocale).getSuffixes()[0]);
        assertEquals("Engineer", context.professionProvider(fallbackLocale).getProfessions()[0]);
        assertEquals("CityOne", context.cityProvider(fallbackLocale).getCities()[0]);
        assertEquals("StateOne", context.stateProvider(fallbackLocale).getStates()[0]);
        assertEquals("CountryOne", context.countryProvider(fallbackLocale).getCountries()[0]);
        assertEquals("Main", context.streetAddressProvider(fallbackLocale).getStreetNames()[0]);
        assertEquals("NID", context.nationalIdProvider(fallbackLocale).generate(new Random(1L)));

        assertTrue(context.firstNameRegisteredKeys().contains("en_US"));
        assertTrue(context.firstNameRegisteredKeys().contains("en"));
        assertTrue(context.lastNameRegisteredKeys().contains("en_US"));
        assertTrue(context.genderRegisteredKeys().contains("en_US"));
        assertTrue(context.titleRegisteredKeys().contains("en_US"));
        assertTrue(context.suffixRegisteredKeys().contains("en_US"));
        assertTrue(context.professionRegisteredKeys().contains("en_US"));
        assertTrue(context.cityRegisteredKeys().contains("en_US"));
        assertTrue(context.stateRegisteredKeys().contains("en_US"));
        assertTrue(context.countryRegisteredKeys().contains("en_US"));
        assertTrue(context.streetAddressRegisteredKeys().contains("en_US"));
        assertTrue(context.nationalIdRegisteredKeys().contains("en_US"));
        assertTrue(context.nationalIdRegisteredKeys().contains("en"));
    }

    @Test
    @DisplayName("language-only locale registration stores language key")
    void languageOnlyRegistration() {
        DataRegistryContext context = DataRegistryContext.builder()
                                                         .isolated()
                                                         .registerCountryProvider(countryProvider(Locale.of("en")))
                                                         .build();
        assertTrue(context.countryRegisteredKeys().contains("en"));
        assertEquals("CountryOne", context.countryProvider(Locale.of("en", "GB")).getCountries()[0]);
    }

    @Test
    @DisplayName("weather providers remain isolated between contexts")
    void weatherProvidersRemainIsolatedBetweenContexts() {
        DataRegistryContext first = DataRegistryContext.builder()
                                                        .isolated()
                                                        .registerWeatherProvider(weatherProvider(Locale.US, "Sun"))
                                                        .build();
        DataRegistryContext second = DataRegistryContext.builder()
                                                         .isolated()
                                                         .registerWeatherProvider(weatherProvider(Locale.US, "Rain"))
                                                         .build();

        assertEquals("Sun", new WeatherGenerator(GeneratorConfig.builder().registryContext(first).build()).generate());
        assertEquals("Rain", new WeatherGenerator(GeneratorConfig.builder().registryContext(second).build()).generate());
        assertEquals(Set.of("en", "en_US"), first.weatherRegisteredKeys());

        DataRegistryContext empty = DataRegistryContext.builder().isolated().build();
        assertNull(empty.weatherProvider(Locale.US));
        assertFalse(empty.isWeatherRegistered(Locale.US));
        assertTrue(DataRegistryContext.globalDefault().isWeatherRegistered(Locale.of("ru", "RU")));
        assertFalse(DataRegistryContext.globalDefault().weatherRegisteredKeys().isEmpty());
    }

    @Test
    @DisplayName("measurement providers remain isolated between contexts")
    void measurementProvidersRemainIsolatedBetweenContexts() {
        DataRegistryContext first = DataRegistryContext.builder()
                                                        .isolated()
                                                        .registerMeasurementProvider(measurementProvider(Locale.US, "Meter"))
                                                        .build();
        DataRegistryContext second = DataRegistryContext.builder()
                                                         .isolated()
                                                         .registerMeasurementProvider(measurementProvider(Locale.US, "Yard"))
                                                         .build();

        assertEquals("Meter", new MeasurementGenerator(GeneratorConfig.builder().registryContext(first).build()).generate());
        assertEquals("Yard", new MeasurementGenerator(GeneratorConfig.builder().registryContext(second).build()).generate());
        assertEquals(Set.of("en", "en_US"), first.measurementRegisteredKeys());
        DataRegistryContext empty = DataRegistryContext.builder().isolated().build();
        assertTrue(first.isMeasurementRegistered(Locale.US));
        assertNull(empty.measurementProvider(Locale.US));
        assertFalse(empty.isMeasurementRegistered(Locale.US));
        assertFalse(DataRegistryContext.globalDefault().measurementRegisteredKeys().isEmpty());
    }

    @Test
    @DisplayName("financial-term providers remain isolated between contexts")
    void financialTermProvidersRemainIsolatedBetweenContexts() {
        DataRegistryContext first = DataRegistryContext.builder()
                                                        .isolated()
                                                        .registerFinancialTermProvider(financialTermProvider(Locale.US, "Asset"))
                                                        .build();
        DataRegistryContext second = DataRegistryContext.builder()
                                                         .isolated()
                                                         .registerFinancialTermProvider(financialTermProvider(Locale.US, "Dividend"))
                                                         .build();

        assertEquals("Asset", new FinancialTermGenerator(GeneratorConfig.builder().registryContext(first).build()).generate());
        assertEquals("Dividend", new FinancialTermGenerator(GeneratorConfig.builder().registryContext(second).build()).generate());
        assertEquals(Set.of("en", "en_US"), first.financialTermRegisteredKeys());
        DataRegistryContext empty = DataRegistryContext.builder().isolated().build();
        assertTrue(first.isFinancialTermRegistered(Locale.US));
        assertNull(empty.financialTermProvider(Locale.US));
        assertFalse(empty.isFinancialTermRegistered(Locale.US));
        assertFalse(DataRegistryContext.globalDefault().financialTermRegisteredKeys().isEmpty());
    }

    @Test
    @DisplayName("restaurant-type providers remain isolated between contexts")
    void restaurantTypeProvidersRemainIsolatedBetweenContexts() {
        DataRegistryContext first = DataRegistryContext.builder()
                                                        .isolated()
                                                        .registerRestaurantTypeProvider(restaurantTypeProvider(Locale.US, "Italian"))
                                                        .build();
        DataRegistryContext second = DataRegistryContext.builder()
                                                         .isolated()
                                                         .registerRestaurantTypeProvider(restaurantTypeProvider(Locale.US, "Steakhouse"))
                                                         .build();

        assertEquals("Italian", new RestaurantTypeGenerator(GeneratorConfig.builder().registryContext(first).build()).generate());
        assertEquals("Steakhouse", new RestaurantTypeGenerator(GeneratorConfig.builder().registryContext(second).build()).generate());
        assertEquals(Set.of("en", "en_US"), first.restaurantTypeRegisteredKeys());
        DataRegistryContext empty = DataRegistryContext.builder().isolated().build();
        assertTrue(first.isRestaurantTypeRegistered(Locale.US));
        assertNull(empty.restaurantTypeProvider(Locale.US));
        assertFalse(empty.isRestaurantTypeRegistered(Locale.US));
        assertFalse(DataRegistryContext.globalDefault().restaurantTypeRegisteredKeys().isEmpty());
    }

    @Test
    @DisplayName("hobby providers remain isolated between contexts")
    void hobbyProvidersRemainIsolatedBetweenContexts() {
        DataRegistryContext first = DataRegistryContext.builder()
                                                        .isolated()
                                                        .registerHobbyProvider(hobbyProvider(Locale.US, "Chess"))
                                                        .build();
        DataRegistryContext second = DataRegistryContext.builder()
                                                         .isolated()
                                                         .registerHobbyProvider(hobbyProvider(Locale.US, "Running"))
                                                         .build();

        assertEquals("Chess", new HobbyGenerator(GeneratorConfig.builder().registryContext(first).build()).generate());
        assertEquals("Running", new HobbyGenerator(GeneratorConfig.builder().registryContext(second).build()).generate());
        assertEquals(Set.of("en", "en_US"), first.hobbyRegisteredKeys());
        DataRegistryContext empty = DataRegistryContext.builder().isolated().build();
        assertTrue(first.isHobbyRegistered(Locale.US));
        assertNull(empty.hobbyProvider(Locale.US));
        assertFalse(empty.isHobbyRegistered(Locale.US));
        assertFalse(DataRegistryContext.globalDefault().hobbyRegisteredKeys().isEmpty());
    }

    @Test
    @DisplayName("nationality providers remain isolated between contexts")
    void nationalityProvidersRemainIsolatedBetweenContexts() {
        DataRegistryContext first = DataRegistryContext.builder()
                                                        .isolated()
                                                        .registerNationalityProvider(nationalityProvider(Locale.US, "Canadian"))
                                                        .build();
        DataRegistryContext second = DataRegistryContext.builder()
                                                         .isolated()
                                                         .registerNationalityProvider(nationalityProvider(Locale.US, "Brazilian"))
                                                         .build();

        assertEquals("Canadian", new NationalityGenerator(GeneratorConfig.builder().registryContext(first).build()).generate());
        assertEquals("Brazilian", new NationalityGenerator(GeneratorConfig.builder().registryContext(second).build()).generate());
        assertEquals(Set.of("en", "en_US"), first.nationalityRegisteredKeys());
        DataRegistryContext empty = DataRegistryContext.builder().isolated().build();
        assertTrue(first.isNationalityRegistered(Locale.US));
        assertNull(empty.nationalityProvider(Locale.US));
        assertFalse(empty.isNationalityRegistered(Locale.US));
        assertFalse(DataRegistryContext.globalDefault().nationalityRegisteredKeys().isEmpty());
    }

    @Test
    @DisplayName("pronoun providers remain isolated between contexts")
    void pronounProvidersRemainIsolatedBetweenContexts() {
        DataRegistryContext first = DataRegistryContext.builder()
                                                        .isolated()
                                                        .registerPronounProvider(pronounProvider(Locale.US, "she/her"))
                                                        .build();
        DataRegistryContext second = DataRegistryContext.builder()
                                                         .isolated()
                                                         .registerPronounProvider(pronounProvider(Locale.US, "they/them"))
                                                         .build();

        assertEquals("she/her", new PronounGenerator(GeneratorConfig.builder().registryContext(first).build()).generate());
        assertEquals("they/them", new PronounGenerator(GeneratorConfig.builder().registryContext(second).build()).generate());
        assertEquals(Set.of("en", "en_US"), first.pronounRegisteredKeys());
        DataRegistryContext empty = DataRegistryContext.builder().isolated().build();
        assertTrue(first.isPronounRegistered(Locale.US));
        assertNull(empty.pronounProvider(Locale.US));
        assertFalse(empty.isPronounRegistered(Locale.US));
        assertFalse(DataRegistryContext.globalDefault().pronounRegisteredKeys().isEmpty());
    }

    @Test
    @DisplayName("pronoun providers reject values without one subject/object separator")
    void pronounProvidersRejectMalformedSets() {
        assertThrows(IllegalArgumentException.class, () -> DataRegistryContext.builder()
                                                                             .registerPronounProvider(pronounProvider(Locale.US, "/them")));
        assertThrows(IllegalArgumentException.class, () -> DataRegistryContext.builder()
                                                                             .registerPronounProvider(pronounProvider(Locale.US, "they/")));
        assertThrows(IllegalArgumentException.class, () -> DataRegistryContext.builder()
                                                                             .registerPronounProvider(pronounProvider(Locale.US, "they/them/us")));
    }

    @Test
    @DisplayName("blood-type providers remain isolated between contexts")
    void bloodTypeProvidersRemainIsolatedBetweenContexts() {
        DataRegistryContext first = DataRegistryContext.builder()
                                                        .isolated()
                                                        .registerBloodTypeProvider(bloodTypeProvider(Locale.US, java.util.List.of("O+"), java.util.List.of(1)))
                                                        .build();
        DataRegistryContext second = DataRegistryContext.builder()
                                                         .isolated()
                                                         .registerBloodTypeProvider(bloodTypeProvider(Locale.US, java.util.List.of("AB-"), java.util.List.of(1)))
                                                         .build();

        assertEquals("O+", new BloodTypeGenerator(GeneratorConfig.builder().registryContext(first).build()).generate());
        assertEquals("AB-", new BloodTypeGenerator(GeneratorConfig.builder().registryContext(second).build()).generate());
        assertEquals(Set.of("en", "en_US"), first.bloodTypeRegisteredKeys());
        DataRegistryContext empty = DataRegistryContext.builder().isolated().build();
        assertTrue(first.isBloodTypeRegistered(Locale.US));
        assertNull(empty.bloodTypeProvider(Locale.US));
        assertFalse(empty.isBloodTypeRegistered(Locale.US));
        assertFalse(DataRegistryContext.globalDefault().bloodTypeRegisteredKeys().isEmpty());
    }

    @Test
    @DisplayName("blood-type providers require parallel positive weights")
    void bloodTypeProvidersRejectInvalidDistributions() {
        assertThrows(IllegalArgumentException.class, () -> DataRegistryContext.builder()
                                                                             .registerBloodTypeProvider(bloodTypeProvider(Locale.US, java.util.List.of("O+"), java.util.List.of(1, 2))));
        assertThrows(IllegalArgumentException.class, () -> DataRegistryContext.builder()
                                                                             .registerBloodTypeProvider(bloodTypeProvider(Locale.US, java.util.List.of("O+"), java.util.List.of(0))));
        assertThrows(IllegalArgumentException.class, () -> DataRegistryContext.builder()
                                                                             .registerBloodTypeProvider(bloodTypeProvider(Locale.US, java.util.List.of("O+"), java.util.Collections.singletonList(null))));
        assertThrows(NullPointerException.class, () -> DataRegistryContext.builder()
                                                                             .registerBloodTypeProvider(bloodTypeProvider(Locale.US, java.util.List.of("O+"), null)));
    }

    @Test
    @DisplayName("Chinese-zodiac providers remain isolated between contexts")
    void chineseZodiacProvidersRemainIsolatedBetweenContexts() {
        DataRegistryContext first = DataRegistryContext.builder()
                                                        .isolated()
                                                        .registerChineseZodiacProvider(chineseZodiacProvider(Locale.US, "First Dragon"))
                                                        .build();
        DataRegistryContext second = DataRegistryContext.builder()
                                                         .isolated()
                                                         .registerChineseZodiacProvider(chineseZodiacProvider(Locale.US, "Second Dragon"))
                                                         .build();

        assertEquals("First Dragon", new ChineseZodiacGenerator(GeneratorConfig.builder().registryContext(first).build()).animalFor(2024));
        assertEquals("Second Dragon", new ChineseZodiacGenerator(GeneratorConfig.builder().registryContext(second).build()).animalFor(2024));
        assertEquals(Set.of("en", "en_US"), first.chineseZodiacRegisteredKeys());
        DataRegistryContext empty = DataRegistryContext.builder().isolated().build();
        assertTrue(first.isChineseZodiacRegistered(Locale.US));
        assertNull(empty.chineseZodiacProvider(Locale.US));
        assertFalse(empty.isChineseZodiacRegistered(Locale.US));
        assertFalse(DataRegistryContext.globalDefault().chineseZodiacRegisteredKeys().isEmpty());
    }

    @Test
    @DisplayName("Chinese-zodiac providers require the complete ordered cycle")
    void chineseZodiacProvidersRejectIncompleteCycles() {
        assertThrows(IllegalArgumentException.class, () -> DataRegistryContext.builder()
                                                                             .registerChineseZodiacProvider(chineseZodiacProvider(Locale.US, "only", 11)));
    }

    @Test
    @DisplayName("Western-zodiac providers remain isolated between contexts")
    void zodiacProvidersRemainIsolatedBetweenContexts() {
        DataRegistryContext first = DataRegistryContext.builder()
                                                        .isolated()
                                                        .registerZodiacProvider(zodiacProvider(Locale.US, "First Aries"))
                                                        .build();
        DataRegistryContext second = DataRegistryContext.builder()
                                                         .isolated()
                                                         .registerZodiacProvider(zodiacProvider(Locale.US, "Second Aries"))
                                                         .build();

        assertEquals("First Aries", new ZodiacGenerator(GeneratorConfig.builder().registryContext(first).build()).signFor(java.time.LocalDate.of(2024, 3, 21)));
        assertEquals("Second Aries", new ZodiacGenerator(GeneratorConfig.builder().registryContext(second).build()).signFor(java.time.LocalDate.of(2024, 3, 21)));
        assertEquals(Set.of("en", "en_US"), first.zodiacRegisteredKeys());
        DataRegistryContext empty = DataRegistryContext.builder().isolated().build();
        assertTrue(first.isZodiacRegistered(Locale.US));
        assertNull(empty.zodiacProvider(Locale.US));
        assertFalse(empty.isZodiacRegistered(Locale.US));
        assertFalse(DataRegistryContext.globalDefault().zodiacRegisteredKeys().isEmpty());
    }

    @Test
    @DisplayName("Western-zodiac providers require the complete ordered cycle")
    void zodiacProvidersRejectIncompleteCycles() {
        assertThrows(IllegalArgumentException.class, () -> DataRegistryContext.builder()
                                                                             .registerZodiacProvider(zodiacProvider(Locale.US, "only", 11)));
    }

    @Test
    @DisplayName("locale provider families prefer exact values then language fallbacks")
    void localeProviderFamiliesPreferExactValuesThenLanguageFallbacks() {
        FirstNameDataProvider languageFirstNames = firstNameProvider(Locale.ENGLISH);
        FirstNameDataProvider usFirstNames = firstNameProvider(Locale.US);
        WeatherDataProvider languageWeather = weatherProvider(Locale.ENGLISH, "English weather");
        WeatherDataProvider usWeather = weatherProvider(Locale.US, "US weather");
        BloodTypeDataProvider languageBloodTypes = bloodTypeProvider(Locale.ENGLISH, List.of("English blood"), List.of(1));
        BloodTypeDataProvider usBloodTypes = bloodTypeProvider(Locale.US, List.of("US blood"), List.of(1));
        ZodiacDataProvider languageZodiac = zodiacProvider(Locale.ENGLISH, "English aries");
        ZodiacDataProvider usZodiac = zodiacProvider(Locale.US, "US aries");

        DataRegistryContext context = DataRegistryContext.builder()
                                                         .isolated()
                                                         .registerFirstNameProvider(languageFirstNames)
                                                         .registerFirstNameProvider(usFirstNames)
                                                         .registerWeatherProvider(languageWeather)
                                                         .registerWeatherProvider(usWeather)
                                                         .registerBloodTypeProvider(languageBloodTypes)
                                                         .registerBloodTypeProvider(usBloodTypes)
                                                         .registerZodiacProvider(languageZodiac)
                                                         .registerZodiacProvider(usZodiac)
                                                         .build();

        assertSame(usFirstNames, context.firstNameProvider(Locale.US));
        assertSame(languageFirstNames, context.firstNameProvider(Locale.CANADA));
        assertSame(usWeather, context.weatherProvider(Locale.US));
        assertSame(languageWeather, context.weatherProvider(Locale.CANADA));
        assertSame(usBloodTypes, context.bloodTypeProvider(Locale.US));
        assertSame(languageBloodTypes, context.bloodTypeProvider(Locale.CANADA));
        assertSame(usZodiac, context.zodiacProvider(Locale.US));
        assertSame(languageZodiac, context.zodiacProvider(Locale.CANADA));
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

        assertThrows(NullPointerException.class, () -> builder.registerFirstNameProvider(null));
        assertThrows(IllegalArgumentException.class, () -> builder.registerFirstNameProvider(new FirstNameDataProvider() {
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
                return new String[] { "f1" };
            }
        }));

        assertThrows(IllegalArgumentException.class, () -> builder.registerLastNameProvider(new LastNameDataProvider() {
            @Override
            public Locale getLocale() {
                return locale;
            }

            @Override
            public String[] getLastNames() {
                return new String[0];
            }
        }));

        assertThrows(IllegalArgumentException.class, () -> builder.registerGenderProvider(new GenderDataProvider() {
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
        assertThrows(IllegalArgumentException.class, () -> builder.registerGenderProvider(new GenderDataProvider() {
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

        assertThrows(IllegalArgumentException.class, () -> builder.registerTitleProvider(new TitleDataProvider() {
            @Override
            public Locale getLocale() {
                return locale;
            }

            @Override
            public String[] getTitles() {
                return new String[0];
            }
        }));

        assertThrows(IllegalArgumentException.class, () -> builder.registerSuffixProvider(new SuffixDataProvider() {
            @Override
            public Locale getLocale() {
                return locale;
            }

            @Override
            public String[] getSuffixes() {
                return new String[0];
            }
        }));

        assertThrows(IllegalArgumentException.class, () -> builder.registerProfessionProvider(new ProfessionDataProvider() {
            @Override
            public Locale getLocale() {
                return locale;
            }

            @Override
            public String[] getProfessions() {
                return new String[] { "Engineer" };
            }

            @Override
            public int[] getWeights() {
                return new int[0];
            }
        }));
        assertThrows(IllegalArgumentException.class, () -> builder.registerProfessionProvider(new ProfessionDataProvider() {
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
        assertThrows(IllegalArgumentException.class, () -> builder.registerProfessionProvider(new ProfessionDataProvider() {
            @Override
            public Locale getLocale() {
                return locale;
            }

            @Override
            public String[] getProfessions() {
                return new String[] { "  " };
            }

            @Override
            public int[] getWeights() {
                return new int[] { 1 };
            }
        }));
        assertThrows(IllegalArgumentException.class, () -> builder.registerProfessionProvider(new ProfessionDataProvider() {
            @Override
            public Locale getLocale() {
                return locale;
            }

            @Override
            public String[] getProfessions() {
                return new String[] { null };
            }

            @Override
            public int[] getWeights() {
                return new int[] { 1 };
            }
        }));
        assertThrows(IllegalArgumentException.class, () -> builder.registerProfessionProvider(new ProfessionDataProvider() {
            @Override
            public Locale getLocale() {
                return locale;
            }

            @Override
            public String[] getProfessions() {
                return new String[] { "Engineer" };
            }

            @Override
            public int[] getWeights() {
                return new int[] { 0 };
            }
        }));

        assertThrows(NullPointerException.class, () -> builder.registerCityProvider(new CityDataProvider() {
            @Override
            public Locale getLocale() {
                return null;
            }

            @Override
            public String[] getCities() {
                return new String[] { "CityOne" };
            }
        }));
        assertThrows(IllegalArgumentException.class, () -> builder.registerCityProvider(new CityDataProvider() {
            @Override
            public Locale getLocale() {
                return locale;
            }

            @Override
            public String[] getCities() {
                return new String[] { " " };
            }
        }));
        assertThrows(IllegalArgumentException.class, () -> builder.registerCityProvider(new CityDataProvider() {
            @Override
            public Locale getLocale() {
                return locale;
            }

            @Override
            public String[] getCities() {
                return new String[] { null };
            }
        }));

        assertThrows(NullPointerException.class, () -> builder.registerStateProvider(new StateDataProvider() {
            @Override
            public Locale getLocale() {
                return null;
            }

            @Override
            public String[] getStates() {
                return new String[] { "StateOne" };
            }

            @Override
            public String[] getAbbreviations() {
                return new String[] { "S1" };
            }
        }));
        assertThrows(IllegalArgumentException.class, () -> builder.registerStateProvider(new StateDataProvider() {
            @Override
            public Locale getLocale() {
                return locale;
            }

            @Override
            public String[] getStates() {
                return new String[] { "" };
            }

            @Override
            public String[] getAbbreviations() {
                return new String[] { "S1" };
            }
        }));
        assertThrows(IllegalArgumentException.class, () -> builder.registerStateProvider(new StateDataProvider() {
            @Override
            public Locale getLocale() {
                return locale;
            }

            @Override
            public String[] getStates() {
                return new String[] { null };
            }

            @Override
            public String[] getAbbreviations() {
                return new String[] { "S1" };
            }
        }));
        assertThrows(IllegalArgumentException.class, () -> builder.registerStateProvider(new StateDataProvider() {
            @Override
            public Locale getLocale() {
                return locale;
            }

            @Override
            public String[] getStates() {
                return new String[] { "StateOne" };
            }

            @Override
            public String[] getAbbreviations() {
                return new String[] { null };
            }
        }));

        assertThrows(NullPointerException.class, () -> builder.registerCountryProvider(new CountryDataProvider() {
            @Override
            public Locale getLocale() {
                return null;
            }

            @Override
            public String[] getCountries() {
                return new String[] { "CountryOne" };
            }
        }));
        assertThrows(IllegalArgumentException.class, () -> builder.registerCountryProvider(new CountryDataProvider() {
            @Override
            public Locale getLocale() {
                return locale;
            }

            @Override
            public String[] getCountries() {
                return new String[] { " " };
            }
        }));
        assertThrows(IllegalArgumentException.class, () -> builder.registerCountryProvider(new CountryDataProvider() {
            @Override
            public Locale getLocale() {
                return locale;
            }

            @Override
            public String[] getCountries() {
                return new String[] { null };
            }
        }));

        assertThrows(IllegalArgumentException.class, () -> builder.registerStreetAddressProvider(new StreetAddressDataProvider() {
            @Override
            public Locale getLocale() {
                return locale;
            }

            @Override
            public String[] getStreetNames() {
                return new String[] { "Main" };
            }

            @Override
            public String[] getStreetTypesShort() {
                return new String[0];
            }

            @Override
            public String[] getStreetTypesLong() {
                return new String[] { "Street" };
            }
        }));

        assertThrows(NullPointerException.class, () -> builder.registerNationalIdProvider(new NationalIdProvider() {
            @Override
            public Locale getLocale() {
                return null;
            }

            @Override
            public String generate(Random random) {
                return "x";
            }
        }));

        assertThrows(NullPointerException.class, () -> DataRegistryContext.builder().useGlobalFallback(true)
                                                                           .registerFirstNameProvider(new FirstNameDataProvider() {
                                                                               @Override
                                                                               public Locale getLocale() {
                                                                                   return null;
                                                                               }

                                                                               @Override
                                                                               public String[] getMaleFirstNames() {
                                                                                   return new String[] { "m1" };
                                                                               }

                                                                               @Override
                                                                               public String[] getFemaleFirstNames() {
                                                                                   return new String[] { "f1" };
                                                                               }
                                                                           }));

        assertThrows(NullPointerException.class, () -> builder.registerWeatherProvider(null));
        assertThrows(IllegalArgumentException.class,
                     () -> builder.registerWeatherProvider(weatherProvider(locale, java.util.List.of())));
        assertThrows(IllegalArgumentException.class,
                     () -> builder.registerWeatherProvider(weatherProvider(locale, java.util.List.of(" "))));
        assertThrows(IllegalArgumentException.class,
                     () -> builder.registerWeatherProvider(
                         weatherProvider(locale, java.util.Collections.singletonList(null))));
    }

    private static FirstNameDataProvider firstNameProvider(Locale locale) {
        return new FirstNameDataProvider() {
            @Override
            public Locale getLocale() {
                return locale;
            }

            @Override
            public String[] getMaleFirstNames() {
                return new String[] { "m1" };
            }

            @Override
            public String[] getFemaleFirstNames() {
                return new String[] { "f1" };
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
                return new String[] { "l1" };
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
                return new String[] { "Dr" };
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
                return new String[] { "Jr" };
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
                return new String[] { "Engineer" };
            }

            @Override
            public int[] getWeights() {
                return new int[] { 1 };
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
                return new String[] { "CityOne" };
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
                return new String[] { "StateOne" };
            }

            @Override
            public String[] getAbbreviations() {
                return new String[] { "S1" };
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
                return new String[] { "CountryOne" };
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
                return new String[] { "Main" };
            }

            @Override
            public String[] getStreetTypesShort() {
                return new String[] { "St" };
            }

            @Override
            public String[] getStreetTypesLong() {
                return new String[] { "Street" };
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
                                  .registerWeatherProvider(weatherProvider(Locale.US, marker + " weather"))
                                  .registerMeasurementProvider(measurementProvider(Locale.US, marker + " unit"))
                                  .registerFinancialTermProvider(financialTermProvider(Locale.US, marker + " term"))
                                  .registerRestaurantTypeProvider(restaurantTypeProvider(Locale.US, marker + " restaurant"))
                                  .registerHobbyProvider(hobbyProvider(Locale.US, marker + " hobby"))
                                  .registerNationalityProvider(nationalityProvider(Locale.US, marker + " nationality"))
                                  .registerPronounProvider(pronounProvider(Locale.US, marker + "/pronoun"))
                                  .registerBloodTypeProvider(bloodTypeProvider(Locale.US, List.of(marker + " blood"), List.of(1)))
                                  .registerChineseZodiacProvider(chineseZodiacProvider(Locale.US, marker + " dragon"))
                                  .registerZodiacProvider(zodiacProvider(Locale.US, marker + " aries"))
                                  .build();
    }

    private static List<String> generateScopedVocabulary(DataRegistryContext context) {
        GeneratorConfig config = GeneratorConfig.builder().locale(Locale.US).registryContext(context).build();
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
            output.add(String.join("|",
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
        return String.join("|",
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
