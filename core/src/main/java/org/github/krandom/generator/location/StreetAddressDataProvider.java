/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.location;

import java.util.Locale;

/**
 * Contract for locale-specific street-address components.
 */
public interface StreetAddressDataProvider {

    /**
     * @return non-null locale this provider serves.
     */
    Locale getLocale();

    /**
     * @return non-null, non-empty street names.
     */
    String[] getStreetNames();

    /**
     * @return non-null, non-empty short street suffixes (e.g., St, Ave).
     */
    String[] getStreetTypesShort();

    /**
     * @return non-null, non-empty long street suffixes (e.g., Street, Avenue).
     */
    String[] getStreetTypesLong();
}

