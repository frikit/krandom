/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.frikit.krandom.generator.user.nationalid.NationalIdProvider;
import java.lang.reflect.Proxy;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Vocabulary keys")
class VocabularyTest {

    /** A consumer-defined vocabulary: colour names per locale. */
    interface ColorProvider extends LocaleDataProvider {

        List<String> colors();
    }

    private record Colors(Locale getLocale, List<String> colors) implements ColorProvider {}

    private static final Vocabulary<ColorProvider> COLOR = Vocabulary.custom("color", ColorProvider.class);

    @Test
    @DisplayName("the built-in vocabularies are listed once each with distinct names")
    void builtInVocabulariesAreListedOnce() {
        List<Vocabulary<?>> all = Vocabulary.all();

        assertEquals(41, all.size());
        Set<String> names = new HashSet<>();
        for (Vocabulary<?> vocabulary : all) {
            assertTrue(names.add(vocabulary.name()), "duplicate vocabulary name " + vocabulary.name());
            assertEquals(vocabulary.name(), vocabulary.toString());
            assertTrue(LocaleDataProvider.class.isAssignableFrom(vocabulary.providerType()), vocabulary.name());
        }
        assertSame(Vocabulary.FIRST_NAME, all.get(0));
        assertEquals("firstName", Vocabulary.FIRST_NAME.name());
        assertEquals("bankAccount", all.get(all.size() - 1).name());
        assertThrows(UnsupportedOperationException.class, () -> all.remove(0));
    }

    @Test
    @DisplayName("every built-in vocabulary but university has bundled data")
    void bundledDataBacksEveryBuiltInVocabularyButUniversity() {
        DataRegistryContext global = DataRegistryContext.globalDefault();

        for (Vocabulary<?> vocabulary : Vocabulary.all()) {
            boolean expected = vocabulary != Vocabulary.UNIVERSITY;
            Set<String> keys = global.registeredKeys(vocabulary);
            assertEquals(expected, vocabulary.hasBuiltInData(), vocabulary.name());
            assertEquals(expected, !keys.isEmpty(), vocabulary.name());
            for (String key : keys) {
                Locale locale = Locale.forLanguageTag(key.replace('_', '-'));
                assertTrue(global.isRegistered(vocabulary, locale), vocabulary.name() + " " + key);
            }
        }
        assertNull(global.provider(Vocabulary.UNIVERSITY, Locale.US));
        assertNotNull(global.provider(Vocabulary.FIRST_NAME, Locale.US));
    }

    @Test
    @DisplayName("a custom vocabulary resolves only what was registered in the context")
    void customVocabularyResolvesScopedProvidersOnly() {
        Colors british = new Colors(Locale.UK, List.of("grey"));
        DataRegistryContext context =
                DataRegistryContext.builder().register(COLOR, british).build();

        assertFalse(COLOR.hasBuiltInData());
        assertEquals("color", COLOR.toString());
        assertSame(british, context.provider(COLOR, Locale.UK));
        assertSame(
                british, context.provider(COLOR, Locale.ENGLISH), "a regional registration is its language's fallback");
        assertSame(british, context.provider(COLOR, Locale.US), "no exact or global match falls back to the language");
        assertNull(context.provider(COLOR, Locale.GERMANY));
        assertNull(context.provider(COLOR, null));
        assertTrue(context.isRegistered(COLOR, Locale.UK));
        assertEquals(Set.of("en_GB", "en"), context.registeredKeys(COLOR));
        assertNull(DataRegistryContext.globalDefault().provider(COLOR, Locale.UK));
        assertTrue(DataRegistryContext.globalDefault().registeredKeys(COLOR).isEmpty());
        assertFalse(Vocabulary.all().contains(COLOR));
    }

    @Test
    @DisplayName("custom vocabularies compare by identity and need a usable name")
    void customVocabulariesCompareByIdentity() {
        Vocabulary<ColorProvider> sameName = Vocabulary.custom("color", ColorProvider.class);
        DataRegistryContext context = DataRegistryContext.builder()
                .register(COLOR, new Colors(Locale.UK, List.of("grey")))
                .build();

        assertNotSame(COLOR, sameName);
        assertNotNull(context.provider(COLOR, Locale.UK));
        assertNull(context.provider(sameName, Locale.UK), "a key with the same name is a different vocabulary");
        assertFalse(context.isRegistered(sameName, Locale.UK));
        assertTrue(context.registeredKeys(sameName).isEmpty());
        assertThrows(NullPointerException.class, () -> Vocabulary.custom(null, ColorProvider.class));
        assertThrows(NullPointerException.class, () -> Vocabulary.custom("color", null));
        assertThrows(IllegalArgumentException.class, () -> Vocabulary.custom(" ", ColorProvider.class));
    }

    @Test
    @DisplayName("registration rejects null keys, providers and locales")
    void registrationRejectsNulls() {
        DataRegistryContext.Builder builder = DataRegistryContext.builder();

        assertEquals(
                "vocabulary",
                assertThrows(NullPointerException.class, () -> builder.register(null, new Colors(Locale.UK, List.of())))
                        .getMessage());
        assertEquals(
                "provider",
                assertThrows(NullPointerException.class, () -> builder.register(COLOR, null))
                        .getMessage());
        assertEquals(
                "provider.getLocale()",
                assertThrows(NullPointerException.class, () -> builder.register(COLOR, new Colors(null, List.of())))
                        .getMessage());
        assertThrows(NullPointerException.class, () -> builder.build().provider(null, Locale.UK));
        assertThrows(NullPointerException.class, () -> builder.build().registeredKeys(null));
    }

    @Test
    @DisplayName("a language-only registration serves every region of the language")
    void languageOnlyRegistrationServesEveryRegion() {
        Colors english = new Colors(Locale.ENGLISH, List.of("gray"));
        Colors british = new Colors(Locale.UK, List.of("grey"));
        DataRegistryContext context = DataRegistryContext.builder()
                .isolated()
                .register(COLOR, english)
                .register(COLOR, british)
                .build();

        assertSame(english, context.provider(COLOR, Locale.US));
        assertSame(british, context.provider(COLOR, Locale.UK));
        assertSame(english, context.provider(COLOR, Locale.ENGLISH), "the first language entry is kept");
        assertEquals(Set.of("en", "en_GB"), context.registeredKeys(COLOR));
    }

    @Test
    @DisplayName("national identity providers fall back by country, not by language")
    void nationalIdFallsBackByCountry() {
        NationalIdProvider canadian = nationalId(Locale.CANADA_FRENCH);
        DataRegistryContext isolated = DataRegistryContext.builder()
                .isolated()
                .register(Vocabulary.NATIONAL_ID, canadian)
                .build();

        assertSame(canadian, isolated.provider(Vocabulary.NATIONAL_ID, Locale.CANADA_FRENCH));
        assertSame(canadian, isolated.provider(Vocabulary.NATIONAL_ID, Locale.CANADA), "same country, other language");
        assertSame(
                canadian,
                isolated.provider(Vocabulary.NATIONAL_ID, Locale.FRENCH),
                "language-only lookups use the language entry");
        assertNull(isolated.provider(Vocabulary.NATIONAL_ID, Locale.FRANCE), "same language, other country");
        assertEquals(
                Set.of("fr_CA", "fr"),
                isolated.registeredKeys(Vocabulary.NATIONAL_ID),
                "the country index is not a key");

        DataRegistryContext withGlobal = DataRegistryContext.builder()
                .register(Vocabulary.NATIONAL_ID, canadian)
                .build();
        assertSame(canadian, withGlobal.provider(Vocabulary.NATIONAL_ID, Locale.CANADA_FRENCH));
        assertNotNull(withGlobal.provider(Vocabulary.NATIONAL_ID, Locale.FRANCE));
        assertNotSame(
                canadian,
                withGlobal.provider(Vocabulary.NATIONAL_ID, Locale.FRANCE),
                "bundled data for an exact key wins over the country index");
    }

    private static NationalIdProvider nationalId(Locale locale) {
        Object proxy = Proxy.newProxyInstance(
                NationalIdProvider.class.getClassLoader(),
                new Class<?>[] {NationalIdProvider.class},
                (p, method, args) -> {
                    if (method.getName().equals("getLocale")) {
                        return locale;
                    }
                    return switch (method.getName()) {
                        case "hashCode" -> System.identityHashCode(p);
                        case "equals" -> p == args[0];
                        case "toString" -> "NationalIdProvider(" + locale + ")";
                        default -> null;
                    };
                });
        return (NationalIdProvider) proxy;
    }
}
