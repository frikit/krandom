/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.user;

import java.util.List;
import java.util.Locale;

/**
 * Contract for locale-specific job seniority labels.
 *
 * <p>Built-in providers are loaded from {@code krandom/seniorities/<locale>.txt} and served by
 * {@link SeniorityDataRegistry}; locales without built-in data fall back to the bundled English
 * {@code default.txt} data.
 */
public interface SeniorityDataProvider {

    /**
     * The locale this provider supplies job seniority labels for.
     *
     * @return provider locale
     */
    Locale getLocale();

    /**
     * The localized seniority labels such as {@code "Senior"}; never empty.
     *
     * @return immutable list, in resource-file order
     */
    List<String> getSeniorities();
}
