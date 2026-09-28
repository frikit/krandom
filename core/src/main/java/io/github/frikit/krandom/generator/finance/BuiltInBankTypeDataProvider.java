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
 * Built-in bank type labels backed by classpath resources.
 *
 * <p>Entries are loaded from {@code krandom/bank_types/<locale>.txt}, one per line; blank lines and {@code #}
 * comments are ignored.
 */
final class BuiltInBankTypeDataProvider implements BankTypeDataProvider {

    private final Locale locale;
    private final List<String> bankTypes;

    BuiltInBankTypeDataProvider(SupportedLocale supportedLocale) {
        this(supportedLocale.locale(), supportedLocale.resourcePrefix());
    }

    BuiltInBankTypeDataProvider(Locale locale, String resourcePrefix) {
        this.locale = locale;
        this.bankTypes = List.of(LocaleTextResourceLoader.load("krandom/bank_types/" + resourcePrefix + ".txt"));
    }

    @Override
    public Locale getLocale() {
        return locale;
    }

    @Override
    public List<String> getBankTypes() {
        return bankTypes;
    }
}
