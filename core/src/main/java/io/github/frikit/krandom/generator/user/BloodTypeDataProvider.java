/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.user;

import java.util.List;
import java.util.Locale;

/**
 * Contract for a locale-specific blood-type distribution.
 *
 * <p>ABO/Rh frequencies differ by population, so blood types are weighted per locale. Implement this
 * interface and register an instance with {@link BloodTypeDataRegistry} to add or override the
 * distribution for any locale — including locales not built into the library.
 *
 * <p>{@link #getTypes()} and {@link #getWeights()} are parallel lists: the type at index {@code i}
 * is generated with relative weight {@code getWeights().get(i)}.
 *
 * <pre>{@code
 * BloodTypeDataRegistry.register(new BloodTypeDataProvider() {
 *     public Locale getLocale()        { return Locale.of("ko", "KR"); }
 *     public List<String> getTypes()   { return List.of("O+", "A+", "B+", "AB+"); }
 *     public List<Integer> getWeights(){ return List.of(27, 34, 27, 12); }
 * });
 * }</pre>
 */
public interface BloodTypeDataProvider {

    /**
     * The locale this provider supplies a distribution for.
     */
    Locale getLocale();

    /**
     * Blood-type labels (e.g. {@code "O+"}, {@code "AB-"}); parallel to {@link #getWeights()}.
     */
    List<String> getTypes();

    /**
     * Relative selection weights, parallel to {@link #getTypes()}; each value must be positive.
     */
    List<Integer> getWeights();
}
