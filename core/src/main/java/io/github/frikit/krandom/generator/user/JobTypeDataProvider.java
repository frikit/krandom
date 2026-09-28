/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.user;

import java.util.List;
import java.util.Locale;

/**
 * Contract for locale-specific employment type values.
 *
 * <p>Built-in providers are loaded from {@code krandom/job_types/<locale>.txt} and served by
 * {@link JobTypeDataRegistry}; locales without built-in data fall back to the bundled English
 * {@code default.txt} data.
 */
public interface JobTypeDataProvider {

    /**
     * The locale this provider supplies employment type values for.
     *
     * @return provider locale
     */
    Locale getLocale();

    /**
     * The localized employment types such as {@code "Full-time"}; never empty.
     *
     * @return immutable list, in resource-file order
     */
    List<String> getJobTypes();
}
