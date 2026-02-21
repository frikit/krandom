/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Objects;
import java.util.OptionalLong;

/**
 * Immutable configuration shared across generators.
 *
 * <p>Obtain an instance via the fluent {@link Builder}:
 * <pre>{@code
 *   GeneratorConfig config = GeneratorConfig.builder()
 *       .seed(42L)
 *       .charset(StandardCharsets.UTF_8)
 *       .stringLength(8, 32)
 *       .locale(Locale.GERMANY)
 *       .build();
 * }</pre>
 *
 * or use {@link #defaults()} for a zero-configuration instance.
 */
public final class GeneratorConfig {

    private final OptionalLong seed;
    private final Charset charset;
    private final int minStringLength;
    private final int maxStringLength;
    private final int minCollectionSize;
    private final int maxCollectionSize;
    private final Locale locale;

    private GeneratorConfig(Builder b) {
        this.seed             = b.seed;
        this.charset          = b.charset;
        this.minStringLength  = b.minStringLength;
        this.maxStringLength  = b.maxStringLength;
        this.minCollectionSize = b.minCollectionSize;
        this.maxCollectionSize = b.maxCollectionSize;
        this.locale           = b.locale;
    }

    /** Config with all defaults applied. */
    public static GeneratorConfig defaults() {
        return builder().build();
    }

    public static Builder builder() {
        return new Builder();
    }

    // ── Accessors ─────────────────────────────────────────────────────────────

    /** Seed for deterministic generation; empty means {@link java.security.SecureRandom}. */
    public OptionalLong getSeed() { return seed; }

    public Charset getCharset() { return charset; }

    public int getMinStringLength() { return minStringLength; }
    public int getMaxStringLength() { return maxStringLength; }

    public int getMinCollectionSize() { return minCollectionSize; }
    public int getMaxCollectionSize() { return maxCollectionSize; }

    /** Locale for locale-aware generators (names, addresses, etc.). Default: {@link Locale#US}. */
    public Locale getLocale() { return locale; }

    // ── Builder ───────────────────────────────────────────────────────────────

    public static final class Builder {

        private OptionalLong seed         = OptionalLong.empty();
        private Charset      charset      = StandardCharsets.US_ASCII;
        private int minStringLength       = 5;
        private int maxStringLength       = 20;
        private int minCollectionSize     = 1;
        private int maxCollectionSize     = 10;
        private Locale locale             = Locale.US;

        /** Fix the PRNG seed for reproducible output. */
        public Builder seed(long seed) {
            this.seed = OptionalLong.of(seed);
            return this;
        }

        /** Character set used by string / char generators. */
        public Builder charset(Charset charset) {
            this.charset = Objects.requireNonNull(charset, "charset");
            return this;
        }

        /** Length range (inclusive on both ends) for generated strings. */
        public Builder stringLength(int min, int max) {
            if (min < 1)   throw new IllegalArgumentException("min string length must be >= 1");
            if (max < min) throw new IllegalArgumentException("max string length must be >= min");
            this.minStringLength = min;
            this.maxStringLength = max;
            return this;
        }

        /** Size range (inclusive on both ends) for generated collections / arrays. */
        public Builder collectionSize(int min, int max) {
            if (min < 0)   throw new IllegalArgumentException("min collection size must be >= 0");
            if (max < min) throw new IllegalArgumentException("max collection size must be >= min");
            this.minCollectionSize = min;
            this.maxCollectionSize = max;
            return this;
        }

        /** Locale for locale-aware generators (names, addresses, phone numbers, etc.). */
        public Builder locale(Locale locale) {
            this.locale = Objects.requireNonNull(locale, "locale");
            return this;
        }

        public GeneratorConfig build() {
            return new GeneratorConfig(this);
        }
    }
}
