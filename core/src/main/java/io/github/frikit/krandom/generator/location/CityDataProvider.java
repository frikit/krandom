/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.location;

import java.util.Locale;

/**
 * Contract for a locale-specific city name data source.
 *
 * <p>Implement this interface and register an instance with {@link CityDataRegistry} to extend
 * or override city data for any locale — including locales not built into the library.
 *
 * <pre>{@code
 * CityDataRegistry.register(new CityDataProvider() {
 *     public Locale getLocale() { return Locale.of("ko", "KR"); }
 *     public String[] getCities() { return new String[]{"서울", "부산", "인천"}; }
 * });
 * CityGenerator gen = new CityGenerator(Locale.of("ko", "KR"));
 * }</pre>
 *
 * <p>The built-in baseline is seeded by {@link CityDataRegistry} from
 * {@link io.github.frikit.krandom.generator.locale.SupportedLocale}. Custom registrations take
 * precedence over the built-in data for the same locale key.
 */
public interface CityDataProvider {

    /**
     * The locale this provider supplies city names for.
     *
     * @return non-null locale
     */
    Locale getLocale();

    /**
     * Returns the city name strings for this locale.
     *
     * @return non-null, non-empty array of city name strings
     */
    String[] getCities();
}
