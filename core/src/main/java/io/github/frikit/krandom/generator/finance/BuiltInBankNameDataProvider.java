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
 * Built-in fictional bank names backed by classpath resources.
 *
 * <p>Entries are loaded from {@code krandom/bank_names/<locale>.txt}, one per line; blank lines and {@code #}
 * comments are ignored.
 */
final class BuiltInBankNameDataProvider implements BankNameDataProvider {

    private final Locale locale;
    private final List<String> bankNames;

    BuiltInBankNameDataProvider(SupportedLocale supportedLocale) {
        this(supportedLocale.locale(), supportedLocale.resourcePrefix());
    }

    BuiltInBankNameDataProvider(Locale locale, String resourcePrefix) {
        this.locale = locale;
        this.bankNames = List.of(LocaleTextResourceLoader.load("krandom/bank_names/" + resourcePrefix + ".txt"));
    }

    @Override
    public Locale getLocale() {
        return locale;
    }

    @Override
    public List<String> getBankNames() {
        return bankNames;
    }
}
