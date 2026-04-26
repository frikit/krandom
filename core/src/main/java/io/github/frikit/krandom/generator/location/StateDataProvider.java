/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.location;

import java.util.Locale;

/**
 * Contract for a locale-specific state/province name data source.
 *
 * <p>Implement this interface and register an instance with {@link StateDataRegistry} to extend
 * or override state data for any locale — including locales not built into the library.
 *
 * <pre>{@code
 * StateDataRegistry.register(new StateDataProvider() {
 *     public Locale getLocale() { return Locale.of("en", "IN"); }
 *     public String[] getStates() { return new String[]{"Maharashtra", "Karnataka", "Tamil Nadu"}; }
 *     public String[] getAbbreviations() { return new String[]{"MH", "KA", "TN"}; }
 * });
 * StateGenerator gen = new StateGenerator(Locale.of("en", "IN"));
 * }</pre>
 *
 * <p>The built-in baseline is seeded by {@link StateDataRegistry} from
 * {@link io.github.frikit.krandom.generator.locale.SupportedLocale}. Custom registrations take
 * precedence over the built-in data for the same locale key.
 */
public interface StateDataProvider {

    /**
     * The locale this provider supplies state names for.
     *
     * @return non-null locale
     */
    Locale getLocale();

    /**
     * Returns the state name strings for this locale.
     *
     * @return non-null, non-empty array of state name strings
     */
    String[] getStates();

    /**
     * Returns the state abbreviation strings for this locale.
     *
     * <p>If abbreviations are not applicable or not available for this locale,
     * this method should return an empty array (not {@code null}).
     *
     * <p>If returned, the length must match {@link #getStates()}, with each
     * abbreviation corresponding to the state at the same index.
     *
     * @return non-null array of abbreviation strings (may be empty)
     */
    String[] getAbbreviations();
}
