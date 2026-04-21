/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.locale;

import org.github.krandom.generator.DataRegistryContext;
import org.github.krandom.generator.GeneratorConfig;
import org.github.krandom.generator.location.CityGenerator;
import org.github.krandom.generator.location.CountryGenerator;
import org.github.krandom.generator.location.StateGenerator;
import org.github.krandom.generator.location.StreetAddressGenerator;
import org.github.krandom.generator.user.FirstNameGenerator;
import org.github.krandom.generator.user.Gender;
import org.github.krandom.generator.user.GenderGenerator;
import org.github.krandom.generator.user.LastNameGenerator;
import org.github.krandom.generator.user.ProfessionGenerator;
import org.github.krandom.generator.user.SuffixGenerator;
import org.github.krandom.generator.user.TitleGenerator;
import org.github.krandom.generator.user.nationalid.NationalIdGenerator;
import org.github.krandom.generator.user.nationalid.NationalIdProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Locale;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("LocaleDataBundle")
class LocaleDataBundleTest {

    @Test
    @DisplayName("bundle applies to isolated registry contexts in one step")
    void appliesToIsolatedContext() {
        Locale locale = Locale.of("es", "MX");
        LocaleDataBundle bundle = LocaleDataBundle.builder(locale)
                                                  .firstNames(new String[] { "Mateo" }, new String[] { "Sofía" })
                                                  .lastNames("Hernández")
                                                  .genderLabels("Hombre", "Mujer")
                                                  .titles("Lic.")
                                                  .suffixes("Jr.")
                                                  .professions("Ingeniero")
                                                  .cities("Ciudad de México")
                                                  .states(new String[] { "Jalisco" }, new String[] { "JAL" })
                                                  .countries("México")
                                                  .streetAddress(new String[] { "Juárez" },
                                                                 new String[] { "Av" },
                                                                 new String[] { "Avenida" })
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
    @DisplayName("global bundle registration makes a new locale available through normal constructors")
    void registersGlobally() {
        Locale locale = Locale.of("cy", "GB");
        LocaleDataBundle bundle = LocaleDataBundle.builder(locale)
                                                  .firstNames(new String[] { "Dafydd" }, new String[] { "Carys" })
                                                  .lastNames("Jones")
                                                  .cities("Caerdydd")
                                                  .states(new String[] { "Cymru" }, new String[] { "CY" })
                                                  .countries("Y Deyrnas Unedig")
                                                  .titles("Dr")
                                                  .suffixes("PhD")
                                                  .professions("Peiriannydd")
                                                  .streetAddress(new String[] { "Ddraig" },
                                                                 new String[] { "Rd" },
                                                                 new String[] { "Road" })
                                                  .nationalIdProvider(new FixedNationalIdProvider(locale, "CY-777"))
                                                  .build();

        bundle.registerGlobal();

        assertTrue(DataRegistryContext.globalDefault().isFirstNameRegistered(locale));
        assertTrue(DataRegistryContext.globalDefault().isNationalIdRegistered(locale));
        assertEquals("Dafydd", new FirstNameGenerator(locale).generate(Gender.MALE));
        assertEquals("Jones", new LastNameGenerator(locale).generate());
        assertEquals("Caerdydd", new CityGenerator(locale).generate());
        assertEquals("Cymru", new StateGenerator(locale).generate());
        assertEquals("Y Deyrnas Unedig", new CountryGenerator(locale).generate());
        assertEquals("Dr", new TitleGenerator(GeneratorConfig.builder().locale(locale).seed(1L).build()).generate());
        assertEquals("PhD", new SuffixGenerator(GeneratorConfig.builder().locale(locale).seed(1L).build()).generate());
        assertEquals("Peiriannydd", new ProfessionGenerator(GeneratorConfig.builder().locale(locale).seed(1L).build()).generate());
        assertEquals("CY-777", new NationalIdGenerator(locale, 1L).generate());

        StreetAddressGenerator streets = new StreetAddressGenerator(GeneratorConfig.builder().locale(locale).seed(1L).build());
        assertEquals("Ddraig", streets.generateStreetName());
        assertEquals("Rd", streets.generateStreetSuffix());
    }

    @Test
    @DisplayName("bundle validation rejects malformed datasets and null bundle registration")
    void validatesBundleInputs() {
        Locale locale = Locale.of("fr", "CA");

        assertThrows(NullPointerException.class, () -> LocaleDataBundle.builder(null));
        assertThrows(IllegalArgumentException.class,
                     () -> LocaleDataBundle.builder(locale).states(new String[] { "Québec" }, new String[] { "QC", "ON" }));
        assertThrows(IllegalArgumentException.class,
                     () -> LocaleDataBundle.builder(locale).titles("Dr", null));
        assertThrows(IllegalArgumentException.class,
                     () -> LocaleDataBundle.builder(locale).titles());
        assertThrows(IllegalArgumentException.class,
                     () -> LocaleDataBundle.builder(locale).genderLabels(null, "Femme"));
        assertThrows(IllegalArgumentException.class,
                     () -> LocaleDataBundle.builder(locale).genderLabels(" ", "Femme"));
        assertThrows(IllegalArgumentException.class,
                     () -> LocaleDataBundle.builder(locale).professions(new String[] { "Ingénieur" }, new int[] { 1, 2 }));
        assertThrows(IllegalArgumentException.class,
                     () -> LocaleDataBundle.builder(locale).professions(new String[] { "Ingénieur" }, new int[] { 0 }));
        assertThrows(IllegalArgumentException.class,
                     () -> LocaleDataBundle.builder(locale).states(new String[] { "Québec" }, new String[] { null }));
        assertThrows(NullPointerException.class,
                     () -> LocaleDataBundle.builder(locale).states(new String[] { "Québec" }, null));
        assertThrows(NullPointerException.class,
                     () -> LocaleDataBundle.builder(locale).streetAddress(new String[] { "Rue" }, null, new String[] { "Rue" }));
        assertThrows(IllegalArgumentException.class,
                     () -> LocaleDataBundle.builder(locale).countries("Canada", " "));
        assertThrows(IllegalArgumentException.class,
                     () -> LocaleDataBundle.builder(locale)
                                           .nationalIdProvider(new FixedNationalIdProvider(Locale.US, "US-1")));
        assertThrows(NullPointerException.class,
                     () -> LocaleDataBundle.builder(locale).nationalIdProvider(null));
        assertThrows(NullPointerException.class,
                     () -> LocaleDataBundle.builder(locale).build().applyTo(null));
        assertThrows(NullPointerException.class,
                     () -> DataRegistryContext.builder().registerLocaleData(null));
    }

    @Test
    @DisplayName("partial bundles only register the provider families they define")
    void partialBundlesOnlyRegisterDefinedFamilies() {
        Locale locale = Locale.of("mi", "NZ");
        LocaleDataBundle bundle = LocaleDataBundle.builder(locale)
                                                  .firstNames(new String[] { "Aroha" }, new String[] { "Moana" })
                                                  .lastNames("Ngata")
                                                  .build();

        DataRegistryContext context = DataRegistryContext.builder()
                                                         .isolated()
                                                         .registerLocaleData(bundle)
                                                         .build();

        assertTrue(context.isFirstNameRegistered(locale));
        assertTrue(context.isLastNameRegistered(locale));
        assertFalse(context.isCityRegistered(locale));
        assertFalse(context.isCountryRegistered(locale));
        assertNotNull(context.firstNameProvider(locale));
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
                                                              .states(new String[] { "Québec" }, new String[0])
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

    @Test
    @DisplayName("partial global registration leaves undefined provider families untouched")
    void partialGlobalRegistrationLeavesMissingFamiliesUnregistered() {
        Locale locale = Locale.of("gd", "GB");
        LocaleDataBundle bundle = LocaleDataBundle.builder(locale)
                                                  .countries("Alba")
                                                  .build();

        bundle.registerGlobal();

        assertTrue(DataRegistryContext.globalDefault().isCountryRegistered(locale));
        assertFalse(DataRegistryContext.globalDefault().isCityRegistered(locale));
        assertEquals("Alba", new CountryGenerator(locale).generate());
    }

    @Test
    @DisplayName("global registration also covers gender-only bundles without country data")
    void globalRegistrationSupportsGenderWithoutCountry() {
        Locale locale = Locale.of("br", "FR");
        LocaleDataBundle bundle = LocaleDataBundle.builder(locale)
                                                  .genderLabels("Paotr", "Plac'h")
                                                  .build();

        bundle.registerGlobal();

        assertTrue(DataRegistryContext.globalDefault().isGenderRegistered(locale));
        assertFalse(DataRegistryContext.globalDefault().isCountryRegistered(locale));
        assertTrue(Set.of("Paotr", "Plac'h").contains(new GenderGenerator(
            GeneratorConfig.builder().locale(locale).seed(2L).build()
        ).generate()));
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
