/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.user;

import java.util.Locale;

/**
 * Contract for a locale-specific first-name data source.
 *
 * <p>Implement this interface and register an instance with {@link FirstNameDataRegistry} to
 * extend or override first-name data for any locale — including locales not built into the library.
 *
 * <pre>{@code
 * FirstNameDataRegistry.register(new FirstNameDataProvider() {
 *     public Locale getLocale() { return Locale.of("ko", "KR"); }
 *     public String[] getMaleFirstNames()   { return new String[]{"민준", "서준", "예준"}; }
 *     public String[] getFemaleFirstNames() { return new String[]{"서연", "서윤", "지우"}; }
 * });
 * FirstNameGenerator gen = new FirstNameGenerator(Locale.of("ko", "KR"));
 * }</pre>
 *
 * <p>The built-in baseline is provided by {@link LocaleFirstNameData}.
 */
public interface FirstNameDataProvider {

    /** The locale this provider supplies data for. */
    Locale getLocale();

    /** Male first names for this locale. */
    String[] getMaleFirstNames();

    /** Female first names for this locale. */
    String[] getFemaleFirstNames();
}
