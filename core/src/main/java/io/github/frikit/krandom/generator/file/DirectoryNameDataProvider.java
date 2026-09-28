/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.file;

import java.util.List;
import java.util.Locale;

/**
 * Contract for locale-specific directory names.
 *
 * <p>Built-in providers are loaded from {@code krandom/directory_names/<locale>.txt} and served by
 * {@link DirectoryNameDataRegistry}; locales without built-in data fall back to the bundled English
 * {@code default.txt} data.
 */
public interface DirectoryNameDataProvider {

    /**
     * The locale this provider supplies directory names for.
     *
     * @return provider locale
     */
    Locale getLocale();

    /**
     * The localized path-safe directory names such as {@code "projects"}; never empty.
     *
     * @return immutable list, in resource-file order
     */
    List<String> getDirectoryNames();
}
