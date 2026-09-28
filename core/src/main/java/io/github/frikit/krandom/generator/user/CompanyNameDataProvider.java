/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.user;

import java.util.List;
import java.util.Locale;

/**
 * Contract for locale-specific company-name vocabulary.
 *
 * <p>Built-in providers are loaded from {@code krandom/company_names/<list>/<locale>.txt} and served by
 * {@link CompanyNameDataRegistry}; locales without built-in data fall back to the bundled English
 * {@code default.txt} data.
 */
public interface CompanyNameDataProvider {

    /**
     * The locale this provider supplies company-name vocabulary for.
     *
     * @return provider locale
     */
    Locale getLocale();

    /**
     * The localized descriptor words such as {@code "Global"}; never empty.
     *
     * @return immutable list, in resource-file order
     */
    List<String> getPrefixes();

    /**
     * The localized business nouns such as {@code "Solutions"}; never empty.
     *
     * @return immutable list, in resource-file order
     */
    List<String> getNouns();

    /**
     * The localized legal-form suffixes such as {@code "Inc."}; never empty.
     *
     * @return immutable list, in resource-file order
     */
    List<String> getSuffixes();

    /**
     * The localized bare-name format combining {@code {prefix}} and {@code {noun}}, e.g. {@code "{prefix} {noun}"}.
     *
     * @return format with the documented placeholders
     */
    String getNameFormat();

    /**
     * The localized legal-name format combining {@code {name}} and {@code {suffix}}, e.g. {@code "{name} {suffix}"}.
     *
     * @return format with the documented placeholders
     */
    String getLegalNameFormat();
}
