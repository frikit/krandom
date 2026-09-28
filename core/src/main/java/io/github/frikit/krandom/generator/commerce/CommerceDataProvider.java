/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.commerce;

import java.util.List;
import java.util.Locale;

/**
 * Contract for locale-specific commerce vocabulary.
 *
 * <p>Built-in providers are loaded from {@code krandom/commerce/<list>/<locale>.txt} and served by
 * {@link CommerceDataRegistry}; locales without built-in data fall back to the bundled English
 * {@code default.txt} data.
 */
public interface CommerceDataProvider {

    /**
     * The locale this provider supplies commerce vocabulary for.
     *
     * @return provider locale
     */
    Locale getLocale();

    /**
     * The localized product adjectives such as {@code "Small"}; never empty.
     *
     * @return immutable list, in resource-file order
     */
    List<String> getAdjectives();

    /**
     * The localized product materials such as {@code "Steel"}; never empty.
     *
     * @return immutable list, in resource-file order
     */
    List<String> getMaterials();

    /**
     * The localized product nouns such as {@code "Chair"}; never empty.
     *
     * @return immutable list, in resource-file order
     */
    List<String> getProducts();

    /**
     * The localized store departments such as {@code "Books"}; never empty.
     *
     * @return immutable list, in resource-file order
     */
    List<String> getDepartments();

    /**
     * The localized product colors such as {@code "red"}; never empty.
     *
     * @return immutable list, in resource-file order
     */
    List<String> getColors();

    /**
     * The localized product-name format combining {@code {adjective}}, {@code {material}} and {@code {product}}.
     *
     * @return format with the documented placeholders
     */
    String getProductNameFormat();

    /**
     * The localized description format combining {@code {adjective}}, {@code {product}} and {@code {color}}.
     *
     * @return format with the documented placeholders
     */
    String getProductDescriptionFormat();
}
