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
 * Built-in address types backed by classpath resources.
 *
 * <p>Entries are loaded from {@code krandom/address_types/<locale>.txt}, one per line; blank lines and {@code #}
 * comments are ignored.
 */
final class BuiltInAddressTypeDataProvider implements AddressTypeDataProvider {

    private final Locale locale;
    private final List<String> addressTypes;

    BuiltInAddressTypeDataProvider(SupportedLocale supportedLocale) {
        this(supportedLocale.locale(), supportedLocale.resourcePrefix());
    }

    BuiltInAddressTypeDataProvider(Locale locale, String resourcePrefix) {
        this.locale = locale;
        this.addressTypes = List.of(LocaleTextResourceLoader.load("krandom/address_types/" + resourcePrefix + ".txt"));
    }

    @Override
    public Locale getLocale() {
        return locale;
    }

    @Override
    public List<String> getAddressTypes() {
        return addressTypes;
    }
}
