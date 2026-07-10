/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.finance;

import java.util.List;
import java.util.Locale;

/**
 * Contract for a locale-specific list of financial-term names.
 *
 * <p>Implement this interface and register an instance with
 * {@link io.github.frikit.krandom.generator.DataRegistryContext.Builder#registerFinancialTermProvider(FinancialTermDataProvider)}
 * to add or override financial-term vocabulary for one configuration. The global
 * {@link FinancialTermDataRegistry} remains a compatibility bridge.
 */
public interface FinancialTermDataProvider {

    /**
     * The locale this provider supplies financial terms for.
     */
    Locale getLocale();

    /**
     * The localized financial-term names; must be non-empty.
     */
    List<String> getTerms();
}
