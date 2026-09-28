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
 * <p>Implement this interface and register an instance on a configuration-scoped context with
 * {@link io.github.frikit.krandom.generator.DataRegistryContext.Builder#registerMeasurementProvider(MeasurementDataProvider)}
 * to add or override the measurement vocabulary for any locale. {@link MeasurementDataRegistry}
 * holds the built-in vocabulary served by
 * {@link io.github.frikit.krandom.generator.DataRegistryContext#globalDefault()}.
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
