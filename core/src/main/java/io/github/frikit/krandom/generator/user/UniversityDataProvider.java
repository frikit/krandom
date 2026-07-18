/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.user;

import java.util.Locale;

/**
 * Contract for a locale-specific university fixture data source.
 *
 * <p>Implementations are normally supplied by a verified
 * {@link io.github.frikit.krandom.generator.datapack.LocalDataPack} and registered on a
 * configuration-scoped registry context.
 */
public interface UniversityDataProvider {

    /**
     * Returns the locale supplied by this provider.
     *
     * @return provider locale
     */
    Locale getLocale();

    /**
     * Returns coherent university fixtures for the locale.
     *
     * @return non-empty fixture array
     */
    UniversityData[] getUniversities();
}
