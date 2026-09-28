/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.location;

import java.util.Locale;

/**
 * Contract for a locale-specific country name data source.
 *
 * <p>Implement this interface and register an instance on a configuration-scoped context with
 * {@link io.github.frikit.krandom.generator.DataRegistryContext.Builder#registerCountryProvider(CountryDataProvider)}
 * to extend or override country data for any locale — including locales not built into the library.
 *
 * <pre>{@code
 * DataRegistryContext context = DataRegistryContext.builder()
 *     .registerCountryProvider(new CountryDataProvider() {
 *         public Locale getLocale() { return Locale.of("ko", "KR"); }
 *         public String[] getCountries() { return new String[]{"미국", "독일", "프랑스"}; }
 *     })
 *     .build();
 * GeneratorConfig config = GeneratorConfig.builder()
 *     .locale(Locale.of("ko", "KR"))
 *     .registryContext(context)
 *     .build();
 * CountryGenerator gen = new CountryGenerator(config);
 * }</pre>
 *
 * <p>The built-in baseline is seeded by {@link CountryDataRegistry} from
 * {@link io.github.frikit.krandom.generator.locale.SupportedLocale}. Custom registrations take
 * precedence over the built-in data for the same locale key.
 */
public interface CountryDataProvider {

    /**
     * The locale this provider supplies country names for.
     *
     * @return non-null locale
     */
    Locale getLocale();

    /**
     * Returns the country name strings for this locale.
     *
     * @return non-null, non-empty array of country name strings
     */
    String[] getCountries();
}
