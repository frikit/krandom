/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.location;

import io.github.frikit.krandom.generator.LocaleDataProvider;
import java.util.List;
import java.util.Locale;

/**
 * Contract for locale-specific address types by property use.
 *
 * <p>Built-in providers are loaded from {@code krandom/address_types/<locale>.txt} and served by
 * {@link AddressTypeDataRegistry}; locales without built-in data fall back to the bundled English
 * {@code default.txt} data.
 */
public interface AddressTypeDataProvider extends LocaleDataProvider {

    /**
     * The locale this provider supplies address types for.
     *
     * @return provider locale
     */
    Locale getLocale();

    /**
     * The localized address types such as {@code "Residential"}; never empty.
     *
     * @return immutable list, in resource-file order
     */
    List<String> getAddressTypes();
}
