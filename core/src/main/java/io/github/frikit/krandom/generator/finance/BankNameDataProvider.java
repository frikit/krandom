/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.finance;

import java.util.List;
import java.util.Locale;

/**
 * Contract for locale-specific fictional bank names.
 *
 * <p>Built-in providers are loaded from {@code krandom/bank_names/<locale>.txt} and served by
 * {@link BankNameDataRegistry}; locales without built-in data fall back to the bundled English
 * {@code default.txt} data.
 */
public interface BankNameDataProvider {

    /**
     * The locale this provider supplies fictional bank names for.
     *
     * @return provider locale
     */
    Locale getLocale();

    /**
     * The localized fictional bank names such as {@code "First National Bank"}; never empty.
     *
     * @return immutable list, in resource-file order
     */
    List<String> getBankNames();
}
