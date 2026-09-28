/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.user;

import java.util.List;
import java.util.Locale;

/**
 * Contract for locale-specific job position titles.
 *
 * <p>Built-in providers are loaded from {@code krandom/positions/<locale>.txt} and served by
 * {@link PositionDataRegistry}; locales without built-in data fall back to the bundled English
 * {@code default.txt} data.
 */
public interface PositionDataProvider {

    /**
     * The locale this provider supplies job position titles for.
     *
     * @return provider locale
     */
    Locale getLocale();

    /**
     * The localized position titles such as {@code "Software Engineer"}; never empty.
     *
     * @return immutable list, in resource-file order
     */
    List<String> getPositions();
}
