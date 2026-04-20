/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.locale;

/**
 * Declares the quality tier of a built-in locale dataset.
 */
public enum LocaleDataQualityTier {

    NATIVE_DATASET(0),
    CURATED_FALLBACK_DATASET(1),
    ALIAS_FALLBACK_DATASET(2);

    private final int severity;

    LocaleDataQualityTier(int severity) {
        this.severity = severity;
    }

    /**
     * Returns {@code true} when this tier relies on a fallback dataset instead of native data.
     */
    public boolean isFallback() {
        return this != NATIVE_DATASET;
    }

    /**
     * Returns the more conservative of the two tiers.
     */
    public static LocaleDataQualityTier max(LocaleDataQualityTier left, LocaleDataQualityTier right) {
        return left.severity >= right.severity ? left : right;
    }
}
