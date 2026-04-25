/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.spring;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Externalized configuration for the krandom auto-configuration.
 *
 * <p>All properties are optional. When omitted, krandom uses its built-in defaults
 * (non-deterministic {@code SecureRandom}, US locale, depth 5, etc.).
 *
 * <pre>{@code
 *   krandom.seed=42
 *   krandom.locale=de_DE
 *   krandom.object-max-depth=3
 * }</pre>
 */
@ConfigurationProperties(prefix = "krandom")
public class KrandomProperties {

    /**
     * Numeric PRNG seed for reproducible output. When unset, a non-deterministic
     * {@code SecureRandom} is used.
     */
    private Long seed;

    /**
     * Locale tag (e.g. {@code en_US}, {@code de_DE}). Defaults to {@code en_US}.
     */
    private String locale;

    /**
     * Maximum nesting depth for reflection-based object generation. Defaults to 5.
     */
    private Integer objectMaxDepth;

    /**
     * Probability (0.0 – 1.0) that a nullable reference field is set to null
     * during object generation. Defaults to 0.0.
     */
    private Double objectNullProbability;

    /**
     * Minimum length of generated strings. Defaults to the krandom built-in (1).
     */
    private Integer minStringLength;

    /**
     * Maximum length of generated strings. Defaults to the krandom built-in (20).
     */
    private Integer maxStringLength;

    /**
     * Minimum size of generated collections and arrays. Defaults to the krandom built-in (1).
     */
    private Integer minCollectionSize;

    /**
     * Maximum size of generated collections and arrays. Defaults to the krandom built-in (5).
     */
    private Integer maxCollectionSize;

    public Long getSeed() {
        return seed;
    }

    public void setSeed(Long seed) {
        this.seed = seed;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public Integer getObjectMaxDepth() {
        return objectMaxDepth;
    }

    public void setObjectMaxDepth(Integer objectMaxDepth) {
        this.objectMaxDepth = objectMaxDepth;
    }

    public Double getObjectNullProbability() {
        return objectNullProbability;
    }

    public void setObjectNullProbability(Double objectNullProbability) {
        this.objectNullProbability = objectNullProbability;
    }

    public Integer getMinStringLength() {
        return minStringLength;
    }

    public void setMinStringLength(Integer minStringLength) {
        this.minStringLength = minStringLength;
    }

    public Integer getMaxStringLength() {
        return maxStringLength;
    }

    public void setMaxStringLength(Integer maxStringLength) {
        this.maxStringLength = maxStringLength;
    }

    public Integer getMinCollectionSize() {
        return minCollectionSize;
    }

    public void setMinCollectionSize(Integer minCollectionSize) {
        this.minCollectionSize = minCollectionSize;
    }

    public Integer getMaxCollectionSize() {
        return maxCollectionSize;
    }

    public void setMaxCollectionSize(Integer maxCollectionSize) {
        this.maxCollectionSize = maxCollectionSize;
    }
}
