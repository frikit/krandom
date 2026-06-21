/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.measurement;

import java.util.List;
import java.util.Locale;

/**
 * Contract for a locale-specific list of measurement-unit names.
 *
 * <p>Implement this interface and register an instance with {@link MeasurementDataRegistry} to add
 * or override the measurement vocabulary for any locale.
 */
public interface MeasurementDataProvider {

    /**
     * The locale this provider supplies unit names for.
     */
    Locale getLocale();

    /**
     * The localized measurement-unit names; must be non-empty.
     */
    List<String> getUnits();
}
