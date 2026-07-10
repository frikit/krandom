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
 * <p>Implement this interface and register an instance with
 * {@link io.github.frikit.krandom.generator.DataRegistryContext.Builder#registerHobbyProvider(HobbyDataProvider)}
 * to add or override hobby vocabulary for one configuration. The global {@link HobbyDataRegistry}
 * remains a compatibility bridge.
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
