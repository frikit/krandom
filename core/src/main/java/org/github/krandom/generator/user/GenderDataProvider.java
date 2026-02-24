/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.user;

import java.util.Locale;

/**
 * Contract for a locale-specific gender-label data source.
 *
 * <p>Implement this interface and register an instance with {@link GenderDataRegistry} to
 * extend or override gender labels for any locale — including locales not built into the library.
 *
 * <pre>{@code
 * GenderDataRegistry.register(new GenderDataProvider() {
 *     public Locale getLocale() { return Locale.of("ko", "KR"); }
 *     public String getMaleLabel()   { return "남성"; }
 *     public String getFemaleLabel() { return "여성"; }
 * });
 * GenderGenerator gen = new GenderGenerator(Locale.of("ko", "KR"));
 * }</pre>
 *
 * <p>The built-in baseline is provided by {@link LocaleGenderData}.
 */
public interface GenderDataProvider {

    /** The locale this provider supplies labels for. */
    Locale getLocale();

    /** Localized label for the male gender (e.g., {@code "Male"}, {@code "Homme"}, {@code "男性"}). */
    String getMaleLabel();

    /** Localized label for the female gender (e.g., {@code "Female"}, {@code "Femme"}, {@code "女性"}). */
    String getFemaleLabel();
}
