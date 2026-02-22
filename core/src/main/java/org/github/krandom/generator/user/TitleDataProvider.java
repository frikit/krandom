/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.user;

import java.util.Locale;

/**
 * Contract for a locale-specific title data source.
 *
 * <p>Implement this interface and register an instance with {@link TitleDataRegistry} to extend or
 * override title data for any locale — including locales not built into the library.
 *
 * <pre>{@code
 * TitleDataRegistry.register(new TitleDataProvider() {
 *     public Locale getLocale() { return new Locale("ko", "KR"); }
 *     public String[] getTitles() { return new String[]{"씨", "님", "박사", "교수"}; }
 * });
 * TitleGenerator gen = new TitleGenerator(new Locale("ko", "KR"));
 * }</pre>
 *
 * <p>The built-in baseline is provided by {@link LocaleTitleData}, which implements this interface
 * for every locale it covers. Custom registrations take precedence over the built-in data for the
 * same locale key.
 */
public interface TitleDataProvider {

    /**
     * The locale this provider supplies data for.
     *
     * @return non-null locale
     */
    Locale getLocale();

    /**
     * Returns the title strings for this locale.
     *
     * @return non-null, non-empty array of title strings
     */
    String[] getTitles();
}
