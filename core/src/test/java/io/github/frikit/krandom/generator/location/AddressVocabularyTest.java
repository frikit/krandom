/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.location;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.Generators;
import io.github.frikit.krandom.generator.locale.SupportedLocale;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Address types and address purposes")
class AddressVocabularyTest {

    private static final List<String> TYPES =
            List.of("Residential", "Commercial", "Industrial", "Agricultural", "Institutional", "Mixed-use");
    private static final List<String> PURPOSES = List.of(
            "Home address",
            "Work address",
            "Billing address",
            "Shipping address",
            "Mailing address",
            "Temporary address",
            "Previous address");

    private static GeneratorConfig seeded(Locale locale) {
        return GeneratorConfig.builder().seed(11L).locale(locale).build();
    }

    @Test
    @DisplayName("US English is the default and covers every type and purpose")
    void usEnglishIsTheDefault() {
        assertEquals(TYPES, AddressTypeDataRegistry.forLocale(Locale.US).getAddressTypes());
        assertEquals(PURPOSES, AddressPurposeDataRegistry.forLocale(Locale.US).getAddressPurposes());
        assertTrue(TYPES.contains(new AddressTypeGenerator().generate()));
        assertTrue(PURPOSES.contains(new AddressPurposeGenerator().generate()));
        assertEquals(
                new HashSet<>(TYPES), new HashSet<>(new AddressTypeGenerator(seeded(Locale.US)).generateList(200)));
        assertEquals(
                new HashSet<>(PURPOSES),
                new HashSet<>(new AddressPurposeGenerator(seeded(Locale.US)).generateList(200)));
    }

    @Test
    @DisplayName("British and Australian English word delivery and mailing their own way")
    void englishWordingFollowsTheLocale() {
        List<String> british = AddressPurposeDataRegistry.forLocale(Locale.UK).getAddressPurposes();
        List<String> australian =
                AddressPurposeDataRegistry.forLocale(Locale.of("en", "AU")).getAddressPurposes();

        assertEquals(List.of("Delivery address", "Correspondence address"), british.subList(3, 5));
        assertEquals(List.of("Delivery address", "Postal address"), australian.subList(3, 5));
        assertEquals(
                british,
                AddressPurposeDataRegistry.forLocale(Locale.of("en", "IE")).getAddressPurposes());
        assertEquals(
                australian,
                AddressPurposeDataRegistry.forLocale(Locale.of("en", "NZ")).getAddressPurposes());
        assertEquals(
                PURPOSES, AddressPurposeDataRegistry.forLocale(Locale.CANADA).getAddressPurposes());
        assertEquals(TYPES, AddressTypeDataRegistry.forLocale(Locale.UK).getAddressTypes());
    }

    @Test
    @DisplayName("every supported locale lists the six types and seven purposes once each")
    void everySupportedLocaleListsEachConceptOnce() {
        for (SupportedLocale supportedLocale : SupportedLocale.values()) {
            Locale locale = supportedLocale.locale();
            List<String> types = AddressTypeDataRegistry.forLocale(locale).getAddressTypes();
            List<String> purposes = AddressPurposeDataRegistry.forLocale(locale).getAddressPurposes();

            assertEquals(TYPES.size(), types.size(), locale + " " + types);
            assertEquals(types.size(), new HashSet<>(types).size(), locale + " " + types);
            assertEquals(PURPOSES.size(), purposes.size(), locale + " " + purposes);
            assertEquals(purposes.size(), new HashSet<>(purposes).size(), locale + " " + purposes);
            assertEquals(locale, AddressTypeDataRegistry.forLocale(locale).getLocale());
            assertEquals(locale, AddressPurposeDataRegistry.forLocale(locale).getLocale());
        }
    }

    @Test
    @DisplayName("lookups fall back from language_COUNTRY to the language, and miss unknown or null locales")
    void registryLookups() {
        Locale japaneseInBrazil = Locale.of("ja", "BR");

        assertTrue(AddressTypeDataRegistry.isRegistered(Locale.GERMANY));
        assertTrue(AddressPurposeDataRegistry.isRegistered(Locale.GERMANY));
        assertTrue(AddressTypeDataRegistry.isRegistered(japaneseInBrazil));
        assertTrue(AddressPurposeDataRegistry.isRegistered(japaneseInBrazil));
        assertTrue(AddressTypeDataRegistry.isRegistered(Locale.JAPANESE));
        assertTrue(AddressPurposeDataRegistry.isRegistered(Locale.JAPANESE));
        assertEquals(
                Locale.GERMANY,
                AddressTypeDataRegistry.forLocale(Locale.GERMANY).getLocale());
        assertEquals(
                Locale.GERMANY,
                AddressPurposeDataRegistry.forLocale(Locale.GERMANY).getLocale());
        assertEquals(
                Locale.JAPAN,
                AddressTypeDataRegistry.forLocale(japaneseInBrazil).getLocale());
        assertEquals(
                Locale.JAPAN,
                AddressPurposeDataRegistry.forLocale(japaneseInBrazil).getLocale());
        assertEquals(
                Locale.JAPAN, AddressTypeDataRegistry.forLocale(Locale.JAPANESE).getLocale());
        assertEquals(
                Locale.JAPAN,
                AddressPurposeDataRegistry.forLocale(Locale.JAPANESE).getLocale());
        for (Locale missing : new Locale[] {null, Locale.of("is"), Locale.of("is", "IS")}) {
            assertFalse(AddressTypeDataRegistry.isRegistered(missing));
            assertFalse(AddressPurposeDataRegistry.isRegistered(missing));
            assertNull(AddressTypeDataRegistry.forLocale(missing));
            assertNull(AddressPurposeDataRegistry.forLocale(missing));
        }
        assertTrue(AddressTypeDataRegistry.registeredKeys().containsAll(List.of("ja_JP", "ja", "en_GB", "en")));
        assertTrue(AddressPurposeDataRegistry.registeredKeys().containsAll(List.of("de_DE", "de", "en_AU", "en")));
    }

    @Test
    @DisplayName("an unsupported locale falls back to the English lists")
    void unsupportedLocaleFallsBackToEnglish() {
        Locale icelandic = Locale.of("is", "IS");

        assertTrue(TYPES.containsAll(new AddressTypeGenerator(icelandic).generateList(20)));
        assertTrue(PURPOSES.containsAll(new AddressPurposeGenerator(icelandic).generateList(20)));
    }

    @Test
    @DisplayName("a generator draws only from its locale's list, the same way for the same seed")
    void generatorsDrawFromTheLocaleList() {
        List<String> germanTypes =
                AddressTypeDataRegistry.forLocale(Locale.GERMANY).getAddressTypes();
        List<String> germanPurposes =
                AddressPurposeDataRegistry.forLocale(Locale.GERMANY).getAddressPurposes();

        assertTrue(
                germanTypes.containsAll(Generators.ofAddressType(Locale.GERMANY).generateList(30)));
        assertTrue(germanPurposes.containsAll(
                Generators.ofAddressPurpose(Locale.GERMANY).generateList(30)));
        assertEquals(
                new AddressTypeGenerator(seeded(Locale.GERMANY)).generateList(20),
                Generators.ofAddressType(seeded(Locale.GERMANY)).generateList(20));
        assertEquals(
                new AddressPurposeGenerator(seeded(Locale.GERMANY)).generateList(20),
                Generators.ofAddressPurpose(seeded(Locale.GERMANY)).generateList(20));
        assertTrue(TYPES.contains(Generators.ofAddressType().generate()));
        assertTrue(PURPOSES.contains(Generators.ofAddressPurpose().generate()));
    }

    @Test
    @DisplayName("constructors reject a null locale or configuration")
    void constructorsRejectNull() {
        assertThrows(NullPointerException.class, () -> new AddressTypeGenerator((Locale) null));
        assertThrows(NullPointerException.class, () -> new AddressTypeGenerator((GeneratorConfig) null));
        assertThrows(NullPointerException.class, () -> new AddressPurposeGenerator((Locale) null));
        assertThrows(NullPointerException.class, () -> new AddressPurposeGenerator((GeneratorConfig) null));
    }
}
