/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.user;

import java.util.Locale;

/**
 * Contract for a locale-specific profession data source.
 *
 * <p>Implement and register via {@link io.github.frikit.krandom.generator.DataRegistryContext.Builder} to
 * override built-in profession data for a locale or add support for new locales.
 */
public interface ProfessionDataProvider {

    /**
     * @return non-null locale this provider serves.
     */
    Locale getLocale();

    /**
     * @return non-null, non-empty profession list.
     */
    String[] getProfessions();

    /**
     * Relative weights aligned with {@link #getProfessions()} for ranked generation.
     *
     * @return non-null array with same length as professions; every weight must be positive
     */
    int[] getWeights();
}
