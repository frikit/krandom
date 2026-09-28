/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.finance;

import java.util.List;
import java.util.Locale;

/**
 * Contract for locale-specific bank-account vocabulary.
 *
 * <p>Built-in providers are loaded from {@code krandom/bank_accounts/<list>/<locale>.txt} and served by
 * {@link BankAccountDataRegistry}; locales without built-in data fall back to the bundled English
 * {@code default.txt} data.
 */
public interface BankAccountDataProvider {

    /**
     * The locale this provider supplies bank-account vocabulary for.
     *
     * @return provider locale
     */
    Locale getLocale();

    /**
     * The localized account names such as {@code "Savings Account"}; never empty.
     *
     * @return immutable list, in resource-file order
     */
    List<String> getAccountNames();

    /**
     * The localized transaction types such as {@code "deposit"}; never empty.
     *
     * @return immutable list, in resource-file order
     */
    List<String> getTransactionTypes();
}
