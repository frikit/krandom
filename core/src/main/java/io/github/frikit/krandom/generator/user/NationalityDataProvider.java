/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.user;

import java.util.List;
import java.util.Locale;

/**
 * Contract for a locale-specific list of nationality demonyms.
 *
 * <p>Implement this interface and register an instance with {@link NationalityDataRegistry} to add
 * or override the nationality vocabulary for any locale.
 */
public interface NationalityDataProvider {

    /**
     * The locale this provider supplies nationalities for.
     */
    Locale getLocale();

    /**
     * The localized nationality demonyms; must be non-empty.
     */
    List<String> getNationalities();
}
