/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.finance;

import java.util.List;
import java.util.Locale;

/**
 * Contract for locale-specific bank type labels.
 *
 * <p>Built-in providers are loaded from {@code krandom/bank_types/<locale>.txt} and served by
 * {@link BankTypeDataRegistry}; locales without built-in data fall back to the bundled English
 * {@code default.txt} data.
 */
public interface BankTypeDataProvider {

    /**
     * The locale this provider supplies bank type labels for.
     *
     * @return provider locale
     */
    Locale getLocale();

    /**
     * The localized bank type labels such as {@code "Retail Bank"}; never empty.
     *
     * @return immutable list, in resource-file order
     */
    List<String> getBankTypes();
}
