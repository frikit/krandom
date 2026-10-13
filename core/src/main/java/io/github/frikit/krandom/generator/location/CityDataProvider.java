/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.location;

import io.github.frikit.krandom.generator.LocaleDataProvider;
import java.util.Locale;

/**
 * Contract for a locale-specific city name data source.
 *
 * <p>Implement this interface and register an instance on a configuration-scoped context with
 * {@link io.github.frikit.krandom.generator.DataRegistryContext.Builder#register(io.github.frikit.krandom.generator.Vocabulary, io.github.frikit.krandom.generator.LocaleDataProvider)}
 * to extend or override city data for any locale — including locales not built into the library.
 *
 * <pre>{@code
 * DataRegistryContext context = DataRegistryContext.builder()
 *     .register(Vocabulary.CITY, new CityDataProvider() {
 *         public Locale getLocale() { return Locale.of("ko", "KR"); }
 *         public String[] getCities() { return new String[]{"서울", "부산", "인천"}; }
 *     })
 *     .build();
 * GeneratorConfig config = GeneratorConfig.builder()
 *     .locale(Locale.of("ko", "KR"))
 *     .registryContext(context)
 *     .build();
 * CityGenerator gen = new CityGenerator(config);
 * }</pre>
 *
 * <p>The built-in baseline is seeded by {@link CityDataRegistry} from
 * {@link io.github.frikit.krandom.generator.locale.SupportedLocale}. Custom registrations take
 * precedence over the built-in data for the same locale key.
 */
public interface CityDataProvider extends LocaleDataProvider {

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
