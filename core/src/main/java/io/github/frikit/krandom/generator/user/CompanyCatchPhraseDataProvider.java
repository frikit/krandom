/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.user;

import java.util.List;
import java.util.Locale;

/**
 * Contract for locale-specific company catch-phrase vocabulary.
 *
 * <p>Built-in providers are loaded from {@code krandom/company_catch_phrases/<list>/<locale>.txt} and served by
 * {@link CompanyCatchPhraseDataRegistry}; locales without built-in data fall back to the bundled English
 * {@code default.txt} data.
 */
public interface CompanyCatchPhraseDataProvider {

    /**
     * The locale this provider supplies company catch-phrase vocabulary for.
     *
     * @return provider locale
     */
    Locale getLocale();

    /**
     * The localized adjectives such as {@code "Adaptive"}; never empty.
     *
     * @return immutable list, in resource-file order
     */
    List<String> getAdjectives();

    /**
     * The localized nouns such as {@code "Platform"}; never empty.
     *
     * @return immutable list, in resource-file order
     */
    List<String> getNouns();

    /**
     * The localized taglines such as {@code "for modern teams"}; never empty.
     *
     * @return immutable list, in resource-file order
     */
    List<String> getTaglines();

    /**
     * The localized phrase format combining {@code {adjective}}, {@code {noun}} and {@code {tagline}}.
     *
     * @return format with the documented placeholders
     */
    String getFormat();
}
