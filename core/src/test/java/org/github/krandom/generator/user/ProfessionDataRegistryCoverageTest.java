/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("ProfessionDataRegistry coverage")
class ProfessionDataRegistryCoverageTest {

    private static ProfessionDataProvider provider(Locale locale, String[] professions, int[] weights) {
        return new ProfessionDataProvider() {

            @Override
            public Locale getLocale() {
                return locale;
            }

            @Override
            public String[] getProfessions() {
                return professions;
            }

            @Override
            public int[] getWeights() {
                return weights;
            }
        };
    }

    @Test
    @DisplayName("append merges for exact locale")
    void appendMergesForExactLocale() {
        Locale locale = Locale.of("qa", "QA");
        ProfessionDataRegistry.register(provider(locale, new String[] { "A" }, new int[] { 1 }));

        ProfessionDataRegistry.append(locale, new String[] { "B" }, new int[] { 2 });
        ProfessionDataProvider provider = ProfessionDataRegistry.forLocale(locale);

        assertEquals(2, provider.getProfessions().length);
    }

    @Test
    @DisplayName("append creates new exact provider when only language fallback exists")
    void appendCreatesExactOnFallback() {
        Locale languageOnly = Locale.of("qb");
        Locale exact = Locale.of("qb", "QB");
        ProfessionDataRegistry.register(provider(languageOnly, new String[] { "LangOnly" }, new int[] { 1 }));

        ProfessionDataRegistry.append(exact, new String[] { "ExactOnly" }, new int[] { 1 });
        ProfessionDataProvider provider = ProfessionDataRegistry.forLocale(exact);

        assertEquals(exact, provider.getLocale());
        assertEquals(1, provider.getProfessions().length);
        assertEquals("ExactOnly", provider.getProfessions()[0]);
    }

    @Test
    @DisplayName("localeMatches handles both mismatch and full match")
    void localeMatchesCoverage() throws Exception {
        Method localeMatches = ProfessionDataRegistry.class.getDeclaredMethod("localeMatches", Locale.class, Locale.class);
        localeMatches.setAccessible(true);

        assertFalse((Boolean) localeMatches.invoke(null, Locale.US, Locale.GERMANY));
        assertTrue((Boolean) localeMatches.invoke(null, Locale.US, Locale.US));
    }
}
