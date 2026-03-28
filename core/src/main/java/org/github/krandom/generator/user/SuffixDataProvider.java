/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.user;

import java.util.Locale;

/**
 * Contract for a locale-specific name-suffix data source.
 *
 * <p>Implement this interface and register an instance with {@link SuffixDataRegistry} to extend
 * or override suffix data for any locale — including locales not built into the library.
 *
 * <pre>{@code
 * SuffixDataRegistry.register(new SuffixDataProvider() {
 *     public Locale getLocale() { return Locale.of("ko", "KR"); }
 *     public String[] getSuffixes() { return new String[]{"박사", "학사"}; }
 * });
 * SuffixGenerator gen = new SuffixGenerator(Locale.of("ko", "KR"));
 * }</pre>
 *
 * <p>The built-in baseline is seeded by {@link SuffixDataRegistry} from
 * {@link org.github.krandom.generator.locale.SupportedLocale}. Custom registrations take
 * precedence over the built-in data for the same locale key.
 */
public interface SuffixDataProvider {

    /**
     * The locale this provider supplies data for.
     *
     * @return non-null locale
     */
    Locale getLocale();

    /**
     * Returns the name-suffix strings for this locale (e.g. "Jr.", "Sr.", "PhD").
     *
     * @return non-null array of suffix strings
     */
    String[] getSuffixes();
}
