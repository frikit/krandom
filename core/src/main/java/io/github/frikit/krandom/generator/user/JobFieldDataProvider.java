/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.user;

import java.util.List;
import java.util.Locale;

/**
 * Contract for locale-specific job field names.
 *
 * <p>Built-in providers are loaded from {@code krandom/job_fields/<locale>.txt} and served by
 * {@link JobFieldDataRegistry}; locales without built-in data fall back to the bundled English
 * {@code default.txt} data.
 */
public interface JobFieldDataProvider {

    /**
     * The locale this provider supplies job field names for.
     *
     * @return provider locale
     */
    Locale getLocale();

    /**
     * The localized job field (department) names such as {@code "Engineering"}; never empty.
     *
     * @return immutable list, in resource-file order
     */
    List<String> getJobFields();
}
