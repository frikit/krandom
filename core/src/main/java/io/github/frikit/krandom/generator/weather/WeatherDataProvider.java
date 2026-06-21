/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.weather;

import java.util.List;
import java.util.Locale;

/**
 * Contract for a locale-specific list of weather-condition names.
 *
 * <p>Implement this interface and register an instance with {@link WeatherDataRegistry} to add or
 * override the weather vocabulary for any locale.
 */
public interface WeatherDataProvider {

    /**
     * The locale this provider supplies condition names for.
     */
    Locale getLocale();

    /**
     * The localized weather-condition names; must be non-empty.
     */
    List<String> getConditions();
}
