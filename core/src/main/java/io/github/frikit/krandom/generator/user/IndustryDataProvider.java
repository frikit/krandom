/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.user;

import java.util.List;
import java.util.Locale;

/**
 * Contract for locale-specific industry names.
 *
 * <p>Built-in providers are loaded from {@code krandom/industries/<locale>.txt} and served by
 * {@link IndustryDataRegistry}; locales without built-in data fall back to the bundled English
 * {@code default.txt} data.
 */
public interface IndustryDataProvider {

    /**
     * The locale this provider supplies industry names for.
     *
     * @return provider locale
     */
    Locale getLocale();

    /**
     * The localized industry names such as {@code "Technology"}; never empty.
     *
     * @return immutable list, in resource-file order
     */
    List<String> getIndustries();
}
