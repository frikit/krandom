/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.finance;

import io.github.frikit.krandom.generator.locale.SupportedLocale;
import io.github.frikit.krandom.generator.user.LocaleTextResourceLoader;

import java.util.List;
import java.util.Locale;

/**
 * Built-in bank-account vocabulary backed by classpath resources.
 *
 * <p>Entries are loaded from {@code krandom/bank_accounts/<list>/<locale>.txt}, one per line; blank lines and {@code #}
 * comments are ignored.
 */
final class BuiltInBankAccountDataProvider implements BankAccountDataProvider {

    private final Locale locale;
    private final List<String> accountNames;
    private final List<String> transactionTypes;

    BuiltInBankAccountDataProvider(SupportedLocale supportedLocale) {
        this(supportedLocale.locale(), supportedLocale.resourcePrefix());
    }

    BuiltInBankAccountDataProvider(Locale locale, String resourcePrefix) {
        this.locale = locale;
        this.accountNames = List.of(LocaleTextResourceLoader.load("krandom/bank_accounts/account_names/" + resourcePrefix + ".txt"));
        this.transactionTypes = List.of(LocaleTextResourceLoader.load("krandom/bank_accounts/transaction_types/" + resourcePrefix + ".txt"));
    }

    @Override
    public Locale getLocale() {
        return locale;
    }

    @Override
    public List<String> getAccountNames() {
        return accountNames;
    }

    @Override
    public List<String> getTransactionTypes() {
        return transactionTypes;
    }
}
