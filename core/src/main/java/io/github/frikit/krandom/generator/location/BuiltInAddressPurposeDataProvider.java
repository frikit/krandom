/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.location;

import io.github.frikit.krandom.generator.locale.SupportedLocale;
import io.github.frikit.krandom.generator.user.LocaleTextResourceLoader;
import java.util.List;
import java.util.Locale;

/**
 * Built-in address purposes backed by classpath resources.
 *
 * <p>Entries are loaded from {@code krandom/address_purposes/<locale>.txt}, one per line; blank lines and
 * {@code #} comments are ignored.
 */
final class BuiltInAddressPurposeDataProvider implements AddressPurposeDataProvider {

    private final Locale locale;
    private final List<String> addressPurposes;

    BuiltInAddressPurposeDataProvider(SupportedLocale supportedLocale) {
        this(supportedLocale.locale(), supportedLocale.resourcePrefix());
    }

    BuiltInAddressPurposeDataProvider(Locale locale, String resourcePrefix) {
        this.locale = locale;
        this.addressPurposes =
                List.of(LocaleTextResourceLoader.load("krandom/address_purposes/" + resourcePrefix + ".txt"));
    }

    @Override
    public Locale getLocale() {
        return locale;
    }

    @Override
    public List<String> getAddressPurposes() {
        return addressPurposes;
    }
}
