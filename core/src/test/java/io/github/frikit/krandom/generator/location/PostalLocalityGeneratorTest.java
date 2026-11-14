/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.location;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.frikit.krandom.generator.DataRegistryContext;
import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.Generators;
import io.github.frikit.krandom.generator.Krandom;
import io.github.frikit.krandom.generator.Vocabulary;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("a postal locality takes its city, region and postal code from one postal place")
class PostalLocalityGeneratorTest {

    private static GeneratorConfig seeded(Locale locale) {
        return GeneratorConfig.builder().locale(locale).seed(42L).build();
    }

    @ParameterizedTest
    @ValueSource(strings = {"en-US", "es-US", "en-GB", "de-DE", "fr-FR", "en", "de", "fr"})
    @DisplayName("each value's postal code serves its city, in its region")
    void eachValueComesFromOnePlace(String tag) {
        GeneratorConfig config = seeded(Locale.forLanguageTag(tag));
        List<PostalPlace> places = Objects.requireNonNull(
                        config.getRegistryContext().provider(Vocabulary.POSTAL_PLACE, config.getLocale()))
                .getPlaces();
        PostalLocalityGenerator generator = new PostalLocalityGenerator(config);
        Set<String> cities = new HashSet<>();
        Set<String> codes = new HashSet<>();
        for (int i = 0; i < 300; i++) {
            PostalLocality value = generator.generate();
            assertTrue(
                    places.stream()
                            .anyMatch(place -> place.city().equals(value.city())
                                    && place.region().equals(value.region())
                                    && place.regionCode().equals(value.regionCode())
                                    && place.postalCodes().contains(value.postalCode())),
                    tag + " " + value);
            cities.add(value.city());
            codes.add(value.postalCode());
        }
        assertTrue(cities.size() > 50, tag + " " + cities.size());
        assertTrue(codes.size() >= cities.size(), tag + " " + codes.size());
    }

    @Test
    @DisplayName("the session's postal locality is seeded from a stream of its own")
    void sessionValuesAreSeeded() {
        Krandom fake = Krandom.seeded("pins", Locale.US);

        assertEquals(
                List.of(
                        new PostalLocality("Pasadena", "Texas", "TX", "77505"),
                        new PostalLocality("Lincoln", "Nebraska", "NE", "68502"),
                        new PostalLocality("Weiden in der Oberpfalz", "Bayern", "", "92637")),
                List.of(
                        fake.postalLocality(),
                        fake.postalLocality(),
                        Krandom.seeded("pins", Locale.GERMANY).postalLocality()));

        PostalLocalityGenerator own =
                new PostalLocalityGenerator(Krandom.seeded("pins", Locale.US).streamConfig("krandom.postalLocality"));
        assertEquals(new PostalLocality("Pasadena", "Texas", "TX", "77505"), own.generate());
    }

    @Test
    @DisplayName("the session's city and postal code keep their values when it draws postal localities")
    void sessionCityAndPostalCodeKeepTheirValues() {
        Krandom plain = Krandom.seeded("pins", Locale.UK);
        Krandom mixed = Krandom.seeded("pins", Locale.UK);
        for (int i = 0; i < 5; i++) {
            String city = plain.city();
            String postalCode = plain.postalCode();
            mixed.postalLocality();
            assertEquals(city, mixed.city());
            mixed.postalLocality();
            assertEquals(postalCode, mixed.postalCode());
        }
    }

    @Test
    @DisplayName("the factories and the namespace build the same generator")
    void factoriesBuildTheSameGenerator() {
        GeneratorConfig config = seeded(Locale.FRANCE);
        PostalLocality expected = new PostalLocalityGenerator(config).generate();

        assertEquals(expected, Generators.ofPostalLocality(config).generate());
        assertEquals(expected, Generators.location(config).postalLocality().generate());
        assertEquals(
                expected,
                Generators.location(seeded(Locale.US))
                        .postalLocality(Locale.FRANCE)
                        .generate());
        assertEquals(Locale.US, Generators.ofPostalLocality().getLocale());
        assertEquals(Locale.US, new PostalLocalityGenerator().getLocale());
        assertEquals(Locale.UK, Generators.ofPostalLocality(Locale.UK).getLocale());
        assertTrue(
                new PostalLocalityGenerator(Locale.UK).generate().postalCode().matches("[A-Z0-9 ]+"));
        assertThrows(NullPointerException.class, () -> new PostalLocalityGenerator((Locale) null));
        assertThrows(NullPointerException.class, () -> new PostalLocalityGenerator((GeneratorConfig) null));
    }

    @ParameterizedTest
    @ValueSource(strings = {"de-AT", "de-CH", "en-CA", "ja-JP", "es"})
    @DisplayName("a locale without postal places has none to give")
    void localeWithoutPlacesIsUnsupported(String tag) {
        Locale locale = Locale.forLanguageTag(tag);

        UnsupportedOperationException error =
                assertThrows(UnsupportedOperationException.class, () -> new PostalLocalityGenerator(locale));
        assertTrue(error.getMessage().startsWith("Locale " + locale + " has no postal places."), error.getMessage());
        assertTrue(error.getMessage().contains("en_US"), error.getMessage());
        Krandom fake = Krandom.seeded("pins", locale);
        assertThrows(UnsupportedOperationException.class, fake::postalLocality);
    }

    @Test
    @DisplayName("registered places serve their country")
    void registeredPlacesServeTheirCountry() {
        Locale mexico = Locale.forLanguageTag("es-MX");
        PostalPlaceDataProvider guadalajara = new PostalPlaceDataProvider() {
            @Override
            public Locale getLocale() {
                return mexico;
            }

            @Override
            public List<PostalPlace> getPlaces() {
                return List.of(new PostalPlace("Guadalajara", "Jalisco", "JAL", List.of("44100", "44160")));
            }
        };
        GeneratorConfig config = GeneratorConfig.builder()
                .locale(mexico)
                .registryContext(DataRegistryContext.builder()
                        .register(Vocabulary.POSTAL_PLACE, guadalajara)
                        .build())
                .seed(3L)
                .build();
        PostalLocalityGenerator generator = new PostalLocalityGenerator(config);

        Set<String> codes = new HashSet<>();
        for (int i = 0; i < 40; i++) {
            PostalLocality value = generator.generate();
            assertEquals("Guadalajara", value.city());
            assertEquals("Jalisco", value.region());
            assertEquals("JAL", value.regionCode());
            codes.add(value.postalCode());
        }
        assertEquals(Set.of("44100", "44160"), codes);
    }
}
