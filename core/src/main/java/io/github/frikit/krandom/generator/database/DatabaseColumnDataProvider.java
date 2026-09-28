/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.database;

import java.util.List;
import java.util.Locale;

/**
 * Contract for locale-specific database column names.
 *
 * <p>Built-in providers are loaded from {@code krandom/database_columns/<locale>.txt} and served by
 * {@link DatabaseColumnDataRegistry}; locales without built-in data fall back to the bundled English
 * {@code default.txt} data.
 */
public interface DatabaseColumnDataProvider {

    /**
     * The locale this provider supplies database column names for.
     *
     * @return provider locale
     */
    Locale getLocale();

    /**
     * The localized snake_case column names such as {@code "created_at"}; never empty.
     *
     * @return immutable list, in resource-file order
     */
    List<String> getColumns();
}
