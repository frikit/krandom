/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.user;

import java.util.List;
import java.util.Locale;

/**
 * Contract for a locale-specific list of hobby names.
 *
 * <p>Implement this interface and register an instance with {@link HobbyDataRegistry} to add or
 * override the hobby vocabulary for any locale.
 */
public interface HobbyDataProvider {

    /**
     * The locale this provider supplies hobby names for.
     */
    Locale getLocale();

    /**
     * The localized hobby names; must be non-empty.
     */
    List<String> getHobbies();
}
