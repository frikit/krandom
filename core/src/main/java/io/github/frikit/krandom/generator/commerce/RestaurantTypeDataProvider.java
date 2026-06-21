/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.commerce;

import java.util.List;
import java.util.Locale;

/**
 * Contract for a locale-specific list of restaurant cuisine/type names.
 *
 * <p>Implement this interface and register an instance with {@link RestaurantTypeDataRegistry} to add
 * or override the restaurant-type vocabulary for any locale.
 */
public interface RestaurantTypeDataProvider {

    /**
     * The locale this provider supplies restaurant types for.
     */
    Locale getLocale();

    /**
     * The localized restaurant cuisine/type names; must be non-empty.
     */
    List<String> getTypes();
}
