/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.location;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.frikit.krandom.generator.DataRegistryContext;
import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.Vocabulary;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Postal place data")
class PostalPlaceDataTest {

    private static final StateResourceLoader.StateData REGIONS =
            new StateResourceLoader.StateData(new String[] {"Michigan", "England"}, new String[] {"MI", ""});

    @Test
    @DisplayName("a postal place copies its postal codes and rejects missing or blank values")
    void postalPlaceValidatesAndCopies() {
        List<String> codes = new ArrayList<>(List.of("49503", "49504"));
        PostalPlace place = new PostalPlace("Grand Rapids", "Michigan", "MI", codes);
        codes.add("00000");

        assertEquals(List.of("49503", "49504"), place.postalCodes());
        assertThrows(
                UnsupportedOperationException.class, () -> place.postalCodes().add("1"));
        assertEquals("", new PostalPlace("Leeds", "England", "", List.of("LS6 2WF")).regionCode());

        assertEquals("city", npe(() -> new PostalPlace(null, "Michigan", "MI", List.of("1"))));
        assertEquals("region", npe(() -> new PostalPlace("Flint", null, "MI", List.of("1"))));
        assertEquals("regionCode", npe(() -> new PostalPlace("Flint", "Michigan", null, List.of("1"))));
        assertEquals("postalCodes", npe(() -> new PostalPlace("Flint", "Michigan", "MI", null)));
        assertEquals("city must not be blank", iae(() -> new PostalPlace(" ", "Michigan", "MI", List.of("1"))));
        assertEquals("region must not be blank", iae(() -> new PostalPlace("Flint", "", "MI", List.of("1"))));
        assertEquals(
                "postalCodes of Flint must not be empty",
                iae(() -> new PostalPlace("Flint", "Michigan", "MI", List.of())));
        assertEquals(
                "postal code at index 1 of Flint must not be blank",
                iae(() -> new PostalPlace("Flint", "Michigan", "MI", List.of("48502", " "))));
        assertEquals(
                "postal code at index 0 of Flint must not be blank",
                iae(() -> new PostalPlace("Flint", "Michigan", "MI", Arrays.asList((String) null))));
    }

    @Test
    @DisplayName("the registry resolves places by exact key, then country, never by another country's language")
    void registryResolvesByCountry() {
        PostalPlaceDataProvider us = PostalPlaceDataRegistry.forLocale(Locale.US);

        assertEquals(Locale.US, us.getLocale());
        assertSame(us, PostalPlaceDataRegistry.forLocale(Locale.of("es", "US")), "same country, other language");
        assertSame(us, PostalPlaceDataRegistry.forLocale(Locale.ENGLISH), "a language-only locale uses its language");
        assertNull(PostalPlaceDataRegistry.forLocale(Locale.CANADA), "same language, country without places");
        assertNull(PostalPlaceDataRegistry.forLocale(Locale.of("en", "IN")));
        assertNull(PostalPlaceDataRegistry.forLocale(Locale.ROOT));
        assertNull(PostalPlaceDataRegistry.forLocale(null));
        assertTrue(PostalPlaceDataRegistry.isRegistered(Locale.US));
        assertFalse(PostalPlaceDataRegistry.isRegistered(Locale.CANADA));
        assertFalse(PostalPlaceDataRegistry.isRegistered(null));
        assertEquals(
                Set.of("en_US", "en", "en_GB", "de_DE", "de", "fr_FR", "fr"), PostalPlaceDataRegistry.registeredKeys());
        assertEquals(252, us.getPlaces().size());
        assertSame(us.getPlaces(), us.getPlaces());
    }

    @Test
    @DisplayName("the vocabulary validates registered places and falls back by country")
    void vocabularyValidatesAndFallsBackByCountry() {
        PostalPlaceDataProvider mexican = provider(
                Locale.of("es", "MX"), List.of(new PostalPlace("Guadalajara", "Jalisco", "JAL", List.of("44100"))));
        DataRegistryContext context = DataRegistryContext.builder()
                .isolated()
                .register(Vocabulary.POSTAL_PLACE, mexican)
                .build();

        assertSame(mexican, context.provider(Vocabulary.POSTAL_PLACE, Locale.of("en", "MX")));
        assertSame(mexican, context.provider(Vocabulary.POSTAL_PLACE, Locale.of("es")));
        assertNull(context.provider(Vocabulary.POSTAL_PLACE, Locale.of("es", "ES")), "same language, other country");

        DataRegistryContext.Builder builder = DataRegistryContext.builder();
        assertEquals(
                "places must not be empty",
                assertThrows(
                                IllegalArgumentException.class,
                                () -> builder.register(Vocabulary.POSTAL_PLACE, provider(Locale.US, List.of())))
                        .getMessage());
        assertEquals(
                "place at index 0 must not be null",
                assertThrows(
                                IllegalArgumentException.class,
                                () -> builder.register(
                                        Vocabulary.POSTAL_PLACE,
                                        provider(Locale.US, Arrays.asList((PostalPlace) null))))
                        .getMessage());
        assertEquals(
                "places",
                assertThrows(
                                NullPointerException.class,
                                () -> builder.register(Vocabulary.POSTAL_PLACE, provider(Locale.US, null)))
                        .getMessage());
    }

    @Test
    @DisplayName("registered places drive the address, and an empty provider fails at construction")
    void registeredPlacesDriveTheAddress() {
        PostalPlace guadalajara = new PostalPlace("Guadalajara", "Jalisco", "JAL", List.of("44100", "44160"));
        DataRegistryContext context = DataRegistryContext.builder()
                .register(Vocabulary.POSTAL_PLACE, provider(Locale.of("es", "MX"), List.of(guadalajara)))
                .build();
        AddressInfo info = new AddressInfoGenerator(GeneratorConfig.builder()
                        .locale(Locale.of("es", "MX"))
                        .registryContext(context)
                        .seed(3L)
                        .build())
                .generate();

        assertEquals("Guadalajara", info.city());
        assertEquals("Jalisco", info.state());
        assertEquals("JAL", info.stateAbbr());
        assertTrue(guadalajara.postalCodes().contains(info.zip()), info.zip());
        assertEquals("MX", info.countryAbbr());

        List<PostalPlace> shrinking = new ArrayList<>(List.of(guadalajara));
        PostalPlaceDataProvider emptied = provider(Locale.of("es", "MX"), shrinking);
        DataRegistryContext emptiedContext = DataRegistryContext.builder()
                .register(Vocabulary.POSTAL_PLACE, emptied)
                .build();
        shrinking.clear();
        GeneratorConfig emptiedConfig = GeneratorConfig.builder()
                .locale(Locale.of("es", "MX"))
                .registryContext(emptiedContext)
                .build();
        assertEquals(
                "The postal places for es_MX that locale es_MX resolves are empty",
                assertThrows(IllegalStateException.class, () -> new AddressInfoGenerator(emptiedConfig))
                        .getMessage());
    }

    @Test
    @DisplayName("a language-only provider leaves a language-only locale its random country")
    void languageOnlyProviderKeepsRandomCountry() {
        PostalPlace place = new PostalPlace("Ciudad", "Región", "", List.of("10000"));
        DataRegistryContext context = DataRegistryContext.builder()
                .register(Vocabulary.POSTAL_PLACE, provider(Locale.of("es"), List.of(place)))
                .build();
        Set<String> countries = new HashSet<>();
        for (long seed = 0; seed < 50; seed++) {
            AddressInfo info = new AddressInfoGenerator(GeneratorConfig.builder()
                            .locale(Locale.of("es"))
                            .registryContext(context)
                            .seed(seed)
                            .build())
                    .generate();
            assertEquals("Ciudad", info.city());
            assertEquals("Región", info.state());
            assertEquals("10000", info.zip());
            assertTrue(info.address().contains(", Ciudad, Región 10000, "), info.address());
            countries.add(info.countryAbbr());
        }
        assertTrue(countries.size() > 1, "the country stays random: " + countries);
    }

    @Test
    @DisplayName("a full address names a region without a code by its name, and the locale's country")
    void fullAddressNamesRegionWithoutCode() {
        PostalPlace place = new PostalPlace("Guadalajara", "Jalisco", "", List.of("44100"));
        DataRegistryContext context = DataRegistryContext.builder()
                .register(Vocabulary.POSTAL_PLACE, provider(Locale.of("es", "MX"), List.of(place)))
                .build();
        StreetAddressGenerator generator = new StreetAddressGenerator(GeneratorConfig.builder()
                .locale(Locale.of("es", "MX"))
                .registryContext(context)
                .seed(5L)
                .build());

        String full = generator.generateFullAddress();

        assertTrue(full.endsWith(", Guadalajara, Jalisco 44100, México"), full);
    }

    @Test
    @DisplayName("the loader reads rows, skips comments and blanks, and resolves region codes")
    void loaderReadsRows() {
        List<PostalPlace> places = PostalPlaceResourceLoader.load(
                stream("# header\n\nGrand Rapids\tMichigan\t49503,49504\nLeeds\tEngland\tLS6 2WF\n"), "t.txt", REGIONS);

        assertEquals(
                List.of(
                        new PostalPlace("Grand Rapids", "Michigan", "MI", List.of("49503", "49504")),
                        new PostalPlace("Leeds", "England", "", List.of("LS6 2WF"))),
                places);
        assertThrows(UnsupportedOperationException.class, () -> places.add(places.get(0)));
    }

    @Test
    @DisplayName("the loader names the file and line of a malformed row")
    void loaderRejectsMalformedRows() {
        assertEquals(
                "t.txt:2: expected a city, a region and postal codes separated by tabs: Flint\tMichigan",
                ise(() -> PostalPlaceResourceLoader.load(stream("# h\nFlint\tMichigan\n"), "t.txt", REGIONS)));
        assertEquals(
                "t.txt:1: region 'Ohio' is not in the states file",
                ise(() -> PostalPlaceResourceLoader.load(stream("Akron\tOhio\t44301\n"), "t.txt", REGIONS)));
        assertEquals(
                "t.txt:1: postal code at index 1 of Flint must not be blank",
                ise(() -> PostalPlaceResourceLoader.load(stream("Flint\tMichigan\t48502,\n"), "t.txt", REGIONS)));
        assertEquals(
                "t.txt:1: city must not be blank",
                ise(() -> PostalPlaceResourceLoader.load(stream("\tMichigan\t48502\n"), "t.txt", REGIONS)));
        assertEquals(
                "Postal place resource has no places: t.txt",
                ise(() -> PostalPlaceResourceLoader.load(stream("# only a header\n\n"), "t.txt", REGIONS)));
        assertEquals(
                "Postal place resource not found: krandom/postal_places/xx_XX.txt",
                ise(() -> PostalPlaceResourceLoader.load("krandom/postal_places/xx_XX.txt", REGIONS)));
        IllegalStateException unreadable = assertThrows(
                IllegalStateException.class,
                () -> PostalPlaceResourceLoader.load(new FailingStream(), "t.txt", REGIONS));
        assertEquals("Failed to read postal place resource: t.txt", unreadable.getMessage());
        assertTrue(unreadable.getCause() instanceof IOException);
    }

    private static PostalPlaceDataProvider provider(Locale locale, List<PostalPlace> places) {
        return new PostalPlaceDataProvider() {
            @Override
            public Locale getLocale() {
                return locale;
            }

            @Override
            public List<PostalPlace> getPlaces() {
                return places;
            }
        };
    }

    private static InputStream stream(String text) {
        return new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8));
    }

    private static String npe(org.junit.jupiter.api.function.Executable executable) {
        return assertThrows(NullPointerException.class, executable).getMessage();
    }

    private static String iae(org.junit.jupiter.api.function.Executable executable) {
        return assertThrows(IllegalArgumentException.class, executable).getMessage();
    }

    private static String ise(org.junit.jupiter.api.function.Executable executable) {
        return assertThrows(IllegalStateException.class, executable).getMessage();
    }

    private static final class FailingStream extends InputStream {

        @Override
        public int read() throws IOException {
            throw new IOException("unreadable");
        }
    }
}
