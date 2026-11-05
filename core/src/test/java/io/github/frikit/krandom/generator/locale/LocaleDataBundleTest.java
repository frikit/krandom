/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.frikit.krandom.generator.DataRegistryContext;
import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.Vocabulary;
import io.github.frikit.krandom.generator.location.AddressInfo;
import io.github.frikit.krandom.generator.location.AddressInfoGenerator;
import io.github.frikit.krandom.generator.location.CityGenerator;
import io.github.frikit.krandom.generator.location.CountryGenerator;
import io.github.frikit.krandom.generator.location.PostalPlace;
import io.github.frikit.krandom.generator.location.PostalPlaceDataProvider;
import io.github.frikit.krandom.generator.location.StateGenerator;
import io.github.frikit.krandom.generator.location.StreetAddressGenerator;
import io.github.frikit.krandom.generator.user.FirstNameGenerator;
import io.github.frikit.krandom.generator.user.Gender;
import io.github.frikit.krandom.generator.user.GenderGenerator;
import io.github.frikit.krandom.generator.user.LastNameGenerator;
import io.github.frikit.krandom.generator.user.ProfessionGenerator;
import io.github.frikit.krandom.generator.user.SuffixGenerator;
import io.github.frikit.krandom.generator.user.TitleGenerator;
import io.github.frikit.krandom.generator.user.nationalid.NationalIdGenerator;
import io.github.frikit.krandom.generator.user.nationalid.NationalIdProvider;
import io.github.frikit.krandom.generator.user.nationalid.NationalIdSafetyPolicy;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("LocaleDataBundle")
class LocaleDataBundleTest {

    @Test
    @DisplayName("bundle applies to isolated registry contexts in one step")
    void appliesToIsolatedContext() {
        Locale locale = Locale.of("es", "MX");
        LocaleDataBundle bundle = LocaleDataBundle.builder(locale)
                .firstNames(new String[] {"Mateo"}, new String[] {"Sofía"})
                .lastNames("Hernández")
                .genderLabels("Hombre", "Mujer")
                .titles("Lic.")
                .suffixes("Jr.")
                .professions("Ingeniero")
                .cities("Ciudad de México")
                .states(new String[] {"Jalisco"}, new String[] {"JAL"})
                .countries("México")
                .streetAddress(new String[] {"Juárez"}, new String[] {"Av"}, new String[] {"Avenida"})
                .nationalIdProvider(new FixedNationalIdProvider(locale, "MX-123"))
                .build();

        DataRegistryContext context = DataRegistryContext.builder()
                .isolated()
                .registerLocaleData(bundle)
                .build();
        GeneratorConfig config = GeneratorConfig.builder()
                .locale(locale)
                .registryContext(context)
                .seed(7L)
                .nationalIdSafetyPolicy(NationalIdSafetyPolicy.REALISTIC_UNCLASSIFIED)
                .build();

        assertEquals("Mateo", new FirstNameGenerator(config).generate(Gender.MALE));
        assertEquals("Sofía", new FirstNameGenerator(config).generate(Gender.FEMALE));
        assertEquals("Hernández", new LastNameGenerator(config).generate());
        assertTrue(Set.of("Hombre", "Mujer").contains(new GenderGenerator(config).generate()));
        assertEquals("Lic.", new TitleGenerator(config).generate());
        assertEquals("Jr.", new SuffixGenerator(config).generate());
        assertEquals("Ingeniero", new ProfessionGenerator(config).generate());
        assertEquals("Ciudad de México", new CityGenerator(config).generate());
        assertEquals("Jalisco", new StateGenerator(config).generate());
        assertEquals("JAL", new StateGenerator(config).generate(true));
        assertEquals("México", new CountryGenerator(config).generate());

        StreetAddressGenerator streets = new StreetAddressGenerator(config);
        assertEquals("Juárez", streets.generateStreetName());
        assertEquals("Av", streets.generateStreetSuffix());
        assertEquals("Avenida", streets.generateStreetSuffix(false));

        assertEquals("MX-123", new NationalIdGenerator(config).generate());
    }

    @Test
    @DisplayName("bundle validation rejects malformed datasets and null bundle registration")
    void validatesBundleInputs() {
        Locale locale = Locale.of("fr", "CA");

        assertThrows(NullPointerException.class, () -> LocaleDataBundle.builder(null));
        assertThrows(
                IllegalArgumentException.class,
                () -> LocaleDataBundle.builder(locale).states(new String[] {"Québec"}, new String[] {"QC", "ON"}));
        assertThrows(
                IllegalArgumentException.class,
                () -> LocaleDataBundle.builder(locale).titles("Dr", null));
        assertThrows(
                IllegalArgumentException.class,
                () -> LocaleDataBundle.builder(locale).titles());
        assertThrows(
                IllegalArgumentException.class,
                () -> LocaleDataBundle.builder(locale).genderLabels(null, "Femme"));
        assertThrows(
                IllegalArgumentException.class,
                () -> LocaleDataBundle.builder(locale).genderLabels(" ", "Femme"));
        assertThrows(
                IllegalArgumentException.class,
                () -> LocaleDataBundle.builder(locale).professions(new String[] {"Ingénieur"}, new int[] {1, 2}));
        assertThrows(
                IllegalArgumentException.class,
                () -> LocaleDataBundle.builder(locale).professions(new String[] {"Ingénieur"}, new int[] {0}));
        assertThrows(
                IllegalArgumentException.class,
                () -> LocaleDataBundle.builder(locale).states(new String[] {"Québec"}, new String[] {null}));
        assertThrows(
                NullPointerException.class,
                () -> LocaleDataBundle.builder(locale).states(new String[] {"Québec"}, null));
        assertThrows(
                NullPointerException.class,
                () -> LocaleDataBundle.builder(locale).streetAddress(new String[] {"Rue"}, null, new String[] {"Rue"}));
        assertThrows(
                IllegalArgumentException.class,
                () -> LocaleDataBundle.builder(locale).countries("Canada", " "));
        assertThrows(
                IllegalArgumentException.class,
                () -> LocaleDataBundle.builder(locale)
                        .nationalIdProvider(new FixedNationalIdProvider(Locale.US, "US-1")));
        assertThrows(
                NullPointerException.class,
                () -> LocaleDataBundle.builder(locale).nationalIdProvider(null));
        assertThrows(
                NullPointerException.class,
                () -> LocaleDataBundle.builder(locale).build().applyTo(null));
        assertThrows(
                NullPointerException.class, () -> DataRegistryContext.builder().registerLocaleData(null));
    }

    @Test
    @DisplayName("partial bundles only register the provider families they define")
    void partialBundlesOnlyRegisterDefinedFamilies() {
        Locale locale = Locale.of("mi", "NZ");
        LocaleDataBundle bundle = LocaleDataBundle.builder(locale)
                .firstNames(new String[] {"Aroha"}, new String[] {"Moana"})
                .lastNames("Ngata")
                .build();

        DataRegistryContext context = DataRegistryContext.builder()
                .isolated()
                .registerLocaleData(bundle)
                .build();

        assertTrue(context.isRegistered(Vocabulary.FIRST_NAME, locale));
        assertTrue(context.isRegistered(Vocabulary.LAST_NAME, locale));
        assertFalse(context.isRegistered(Vocabulary.CITY, locale));
        assertFalse(context.isRegistered(Vocabulary.COUNTRY, locale));
        assertNotNull(context.provider(Vocabulary.FIRST_NAME, locale));
    }

    @Test
    @DisplayName("bundled postal places give the bundle's addresses their city, region and postal code")
    void postalPlacesDriveBundleAddresses() {
        Locale locale = Locale.of("es", "MX");
        PostalPlace guadalajara = new PostalPlace("Guadalajara", "Jalisco", "JAL", List.of("44100", "44160"));
        LocaleDataBundle bundle =
                LocaleDataBundle.builder(locale).postalPlaces(guadalajara).build();
        DataRegistryContext context =
                DataRegistryContext.builder().registerLocaleData(bundle).build();
        GeneratorConfig config = GeneratorConfig.builder()
                .locale(locale)
                .registryContext(context)
                .seed(11L)
                .build();

        PostalPlaceDataProvider provider = context.provider(Vocabulary.POSTAL_PLACE, locale);
        assertEquals(locale, provider.getLocale());
        assertEquals(List.of(guadalajara), provider.getPlaces());
        AddressInfo info = new AddressInfoGenerator(config).generate();
        assertEquals("Guadalajara", info.city());
        assertEquals("JAL", info.stateAbbr());
        assertTrue(guadalajara.postalCodes().contains(info.zip()), info.zip());
        assertFalse(DataRegistryContext.builder()
                .isolated()
                .registerLocaleData(
                        LocaleDataBundle.builder(locale).cities("Puebla").build())
                .build()
                .isRegistered(Vocabulary.POSTAL_PLACE, locale));

        LocaleDataBundle.Builder builder = LocaleDataBundle.builder(locale);
        assertEquals(
                "postalPlaces must not be null",
                assertThrows(NullPointerException.class, () -> builder.postalPlaces((PostalPlace[]) null))
                        .getMessage());
        assertEquals(
                "postalPlaces must not be empty",
                assertThrows(IllegalArgumentException.class, () -> builder.postalPlaces())
                        .getMessage());
        assertEquals(
                "postalPlaces at index 1 must not be null",
                assertThrows(IllegalArgumentException.class, () -> builder.postalPlaces(guadalajara, null))
                        .getMessage());
    }

    @Test
    @DisplayName("states without abbreviations and uniform profession weights still work")
    void supportsStateFallbackAndUniformProfessionWeights() {
        Locale locale = Locale.of("fr", "CA");
        LocaleDataBundle bundle = LocaleDataBundle.builder(locale)
                .states("Québec")
                .professions("Architecte")
                .build();
        LocaleDataBundle alignedStateBundle = LocaleDataBundle.builder(locale)
                .states(new String[] {"Québec"}, new String[0])
                .build();

        DataRegistryContext.Builder builder = DataRegistryContext.builder().isolated();
        assertSame(builder, bundle.applyTo(builder));
        alignedStateBundle.applyTo(DataRegistryContext.builder().isolated());

        DataRegistryContext context = builder.build();
        GeneratorConfig config = GeneratorConfig.builder()
                .locale(locale)
                .registryContext(context)
                .seed(3L)
                .build();

        assertEquals(locale, bundle.locale());
        assertEquals("Québec", new StateGenerator(config).generate());
        assertEquals("Québec", new StateGenerator(config).generate(true));
        assertEquals("Architecte", new ProfessionGenerator(config).generate());
    }

    private static final class FixedNationalIdProvider implements NationalIdProvider {

        private final Locale locale;
        private final String value;

        private FixedNationalIdProvider(Locale locale, String value) {
            this.locale = locale;
            this.value = value;
        }

        @Override
        public Locale getLocale() {
            return locale;
        }

        @Override
        public String generate(Random random) {
            return value;
        }
    }
}
