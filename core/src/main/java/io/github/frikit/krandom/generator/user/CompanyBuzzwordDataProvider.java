/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.user;

import java.util.List;
import java.util.Locale;

/**
 * Contract for locale-specific company buzzword vocabulary.
 *
 * <p>Built-in providers are loaded from {@code krandom/company_buzzwords/<list>/<locale>.txt} and served by
 * {@link CompanyBuzzwordDataRegistry}; locales without built-in data fall back to the bundled English
 * {@code default.txt} data.
 */
public interface CompanyBuzzwordDataProvider {

    /**
     * The locale this provider supplies company buzzword vocabulary for.
     *
     * @return provider locale
     */
    Locale getLocale();

    /**
     * The localized leading verbs such as {@code "streamline"}; never empty.
     *
     * @return immutable list, in resource-file order
     */
    List<String> getVerbs();

    /**
     * The localized adjectives such as {@code "end-to-end"}; never empty.
     *
     * @return immutable list, in resource-file order
     */
    List<String> getAdjectives();

    /**
     * The localized plural nouns such as {@code "solutions"}; never empty.
     *
     * @return immutable list, in resource-file order
     */
    List<String> getNouns();

    /**
     * The localized phrase format combining {@code {verb}}, {@code {adjective}} and {@code {noun}}.
     *
     * @return format with the documented placeholders
     */
    String getFormat();
}
