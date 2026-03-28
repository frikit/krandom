/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.user;

import java.util.Locale;

/**
 * Contract for a locale-specific last-name data source.
 *
 * <p>Implement this interface and register an instance with {@link LastNameDataRegistry} to
 * extend or override last-name data for any locale — including locales not built into the library.
 *
 * <pre>{@code
 * LastNameDataRegistry.register(new LastNameDataProvider() {
 *     public Locale getLocale() { return Locale.of("ko", "KR"); }
 *     public String[] getLastNames() { return new String[]{"김", "이", "박", "최", "정"}; }
 * });
 * LastNameGenerator gen = new LastNameGenerator(Locale.of("ko", "KR"));
 * }</pre>
 *
 * <p>The built-in baseline is seeded by {@link LastNameDataRegistry} from
 * {@link org.github.krandom.generator.locale.SupportedLocale}.
 */
public interface LastNameDataProvider {

    /** The locale this provider supplies data for. */
    Locale getLocale();

    /** Last names (family names / surnames) for this locale. */
    String[] getLastNames();
}
