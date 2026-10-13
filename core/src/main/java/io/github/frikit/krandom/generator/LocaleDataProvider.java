/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator;

import java.util.Locale;

/**
 * Common contract of every vocabulary data provider: the locale whose data it supplies.
 *
 * <p>Each {@link Vocabulary} names a provider interface that extends this one and adds the getters
 * its generators read. {@link DataRegistryContext.Builder#register} stores a provider under its
 * locale, so the locale decides which lookups the provider answers.
 */
public interface LocaleDataProvider {

    /**
     * Returns the locale this provider's data describes.
     *
     * @return provider locale, never {@code null}
     */
    Locale getLocale();
}
