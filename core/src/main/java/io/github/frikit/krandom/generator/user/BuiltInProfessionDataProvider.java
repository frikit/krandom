/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.user;

import io.github.frikit.krandom.generator.locale.SupportedLocale;

import java.util.Locale;

/**
 * Built-in profession provider backed by classpath profession resources.
 *
 * <p>Profession lists are loaded from {@code krandom/professions/<locale>.txt} (one profession per
 * line, ordered most-common-first). Ranked-generation weights are derived from list position so the
 * dataset can grow without maintaining a parallel weights table: the first (most common) entry is
 * weighted highest and the last entry receives weight {@code 1}.
 */
final class BuiltInProfessionDataProvider implements ProfessionDataProvider {

    private final Locale   locale;
    private final String[] professions;

    BuiltInProfessionDataProvider(SupportedLocale supportedLocale) {
        this.locale = supportedLocale.locale();
        this.professions =
            LocaleTextResourceLoader.load("krandom/professions/" + supportedLocale.resourcePrefix() + ".txt");
    }

    /**
     * Derives descending rank weights from list position: entry {@code i} of {@code count} entries
     * gets weight {@code count - i}, so the most common profession (first) is weighted highest and
     * the least common (last) gets weight {@code 1}. Every weight is positive.
     */
    private static int[] rankWeights(int count) {
        int[] weights = new int[count];
        for (int i = 0; i < count; i++) {
            weights[i] = count - i;
        }
        return weights;
    }

    @Override
    public Locale getLocale() {
        return locale;
    }

    @Override
    public String[] getProfessions() {
        return professions.clone();
    }

    @Override
    public int[] getWeights() {
        return rankWeights(professions.length);
    }
}
