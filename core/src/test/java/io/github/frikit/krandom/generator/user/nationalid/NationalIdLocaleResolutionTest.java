/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.user.nationalid;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.frikit.krandom.generator.DataRegistryContext;
import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.Vocabulary;
import io.github.frikit.krandom.generator.locale.SupportedLocale;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("National-ID locale resolution")
class NationalIdLocaleResolutionTest {

    /** SupportedLocale countries that have no built-in national-ID provider. */
    private static final Set<String> COUNTRIES_WITHOUT_PROVIDER =
            Set.of("CA", "NZ", "IE", "ZA", "BE", "CH", "AT", "MX", "AR", "PT", "TW");

    @Test
    @DisplayName("a regional variant never borrows another country's identifier")
    void regionalVariantsFailFastWithoutTheirCountrysProvider() {
        List<Locale> locales = List.of(
                Locale.CANADA,
                Locale.CANADA_FRENCH,
                Locale.of("pt", "PT"),
                Locale.TAIWAN,
                Locale.of("de", "AT"),
                Locale.of("fr", "BE"),
                Locale.of("nl", "BE"),
                Locale.of("de", "CH"),
                Locale.of("fr", "CH"),
                Locale.of("en", "NZ"),
                Locale.of("en", "IE"),
                Locale.of("en", "ZA"),
                Locale.of("es", "MX"),
                Locale.of("es", "AR"),
                Locale.of("de", "LI"));
        for (Locale locale : locales) {
            assertFalse(NationalIdRegistry.isRegistered(locale), locale.toString());
            assertNull(NationalIdRegistry.forLocale(locale), locale.toString());
            UnsupportedOperationException error =
                    assertThrows(UnsupportedOperationException.class, () -> new NationalIdGenerator(config(locale)));
            assertTrue(error.getMessage().contains(locale.toString()), error.getMessage());
        }
    }

    @Test
    @DisplayName("another language of a supported country uses that country's provider")
    void sameCountryVariantsUseTheirCountrysProvider() {
        NationalIdProvider india = NationalIdRegistry.forLocale(Locale.of("hi", "IN"));
        NationalIdProvider spain = NationalIdRegistry.forLocale(Locale.of("es", "ES"));

        assertSame(india, NationalIdRegistry.forLocale(Locale.of("en", "IN")));
        assertTrue(NationalIdRegistry.isRegistered(Locale.of("en", "IN")));
        assertSame(spain, NationalIdRegistry.forLocale(Locale.of("gl", "ES")));
        assertNotSame(spain, NationalIdRegistry.forLocale(Locale.of("ca", "ES")));
        String indianId = new NationalIdGenerator(config(Locale.of("en", "IN"))).generate();
        assertEquals(india.generate(new Random(5L)), new NationalIdGenerator(config(Locale.of("hi", "IN"))).generate());
        assertTrue(indianId.matches("^[2-9]\\d{11}$"), indianId);
    }

    @Test
    @DisplayName("language-only locales use the language default and unknown locales fail fast")
    void languageOnlyLocalesUseTheLanguageDefault() {
        assertSame(NationalIdRegistry.forLocale(Locale.US), NationalIdRegistry.forLocale(Locale.ENGLISH));
        assertSame(NationalIdRegistry.forLocale(Locale.of("pt", "BR")), NationalIdRegistry.forLocale(Locale.of("pt")));
        assertTrue(NationalIdRegistry.isRegistered(Locale.ENGLISH));
        assertNull(NationalIdRegistry.forLocale(Locale.ROOT));
        assertFalse(NationalIdRegistry.isRegistered(Locale.ROOT));
        assertFalse(NationalIdRegistry.isRegistered(null));
        assertNull(NationalIdRegistry.forLocale(null));
    }

    @Test
    @DisplayName("every SupportedLocale uses its own country's provider or fails fast")
    void everySupportedLocaleUsesItsCountryOrFailsFast() {
        for (SupportedLocale supportedLocale : SupportedLocale.values()) {
            Locale locale = supportedLocale.locale();
            if (COUNTRIES_WITHOUT_PROVIDER.contains(locale.getCountry())) {
                assertThrows(
                        UnsupportedOperationException.class,
                        () -> new NationalIdGenerator(config(locale)),
                        supportedLocale.name());
            } else {
                assertEquals(
                        locale.getCountry(),
                        NationalIdRegistry.forLocale(locale).getLocale().getCountry(),
                        supportedLocale.name());
                assertFalse(new NationalIdGenerator(config(locale)).generate().isBlank(), supportedLocale.name());
            }
        }
    }

    @Test
    @DisplayName("scoped contexts apply the same country rule")
    void scopedContextsApplyTheSameCountryRule() {
        DataRegistryContext isolated = DataRegistryContext.builder()
                .isolated()
                .register(Vocabulary.NATIONAL_ID, fixed(Locale.US, "US-ID"))
                .build();
        assertNull(isolated.provider(Vocabulary.NATIONAL_ID, Locale.CANADA));
        assertFalse(isolated.isRegistered(Vocabulary.NATIONAL_ID, Locale.CANADA));
        assertEquals(
                "US-ID", isolated.provider(Vocabulary.NATIONAL_ID, Locale.US).generate(new Random(1L)));
        assertEquals(
                "US-ID",
                isolated.provider(Vocabulary.NATIONAL_ID, Locale.ENGLISH).generate(new Random(1L)));
        assertEquals(
                "US-ID",
                isolated.provider(Vocabulary.NATIONAL_ID, Locale.of("es", "US")).generate(new Random(1L)));
        assertNull(isolated.provider(Vocabulary.NATIONAL_ID, Locale.GERMAN));
        assertNull(isolated.provider(Vocabulary.NATIONAL_ID, null));

        DataRegistryContext scoped = DataRegistryContext.builder()
                .register(Vocabulary.NATIONAL_ID, fixed(Locale.CANADA_FRENCH, "CA-ID"))
                .register(Vocabulary.NATIONAL_ID, fixed(Locale.of("es", "US"), "US-ES-ID"))
                .build();
        assertEquals(
                "CA-ID",
                scoped.provider(Vocabulary.NATIONAL_ID, Locale.CANADA_FRENCH).generate(new Random(1L)));
        assertEquals(
                "CA-ID", scoped.provider(Vocabulary.NATIONAL_ID, Locale.CANADA).generate(new Random(1L)));
        assertSame(NationalIdRegistry.forLocale(Locale.US), scoped.provider(Vocabulary.NATIONAL_ID, Locale.US));
        assertEquals(
                "US-ES-ID",
                scoped.provider(Vocabulary.NATIONAL_ID, Locale.of("es", "US")).generate(new Random(1L)));
        assertSame(
                NationalIdRegistry.forLocale(Locale.of("hi", "IN")),
                scoped.provider(Vocabulary.NATIONAL_ID, Locale.of("en", "IN")));
        assertNull(scoped.provider(Vocabulary.NATIONAL_ID, Locale.of("de", "AT")));
        assertEquals(
                "CA-ID", scoped.provider(Vocabulary.NATIONAL_ID, Locale.FRENCH).generate(new Random(1L)));
        assertSame(
                NationalIdRegistry.forLocale(Locale.ENGLISH), scoped.provider(Vocabulary.NATIONAL_ID, Locale.ENGLISH));

        DataRegistryContext languageOnly = DataRegistryContext.builder()
                .isolated()
                .register(Vocabulary.NATIONAL_ID, fixed(Locale.GERMAN, "DE-LANG"))
                .build();
        assertEquals(
                "DE-LANG",
                languageOnly.provider(Vocabulary.NATIONAL_ID, Locale.GERMAN).generate(new Random(1L)));
        assertNull(languageOnly.provider(Vocabulary.NATIONAL_ID, Locale.GERMANY));
    }

    private static GeneratorConfig config(Locale locale) {
        return GeneratorConfig.builder()
                .locale(locale)
                .seed(5L)
                .nationalIdSafetyPolicy(NationalIdSafetyPolicy.REALISTIC_UNCLASSIFIED)
                .build();
    }

    private static NationalIdProvider fixed(Locale locale, String value) {
        return new NationalIdProvider() {
            @Override
            public Locale getLocale() {
                return locale;
            }

            @Override
            public String generate(Random random) {
                return value;
            }
        };
    }
}
