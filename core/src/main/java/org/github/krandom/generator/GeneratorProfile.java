/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator;

import java.util.Locale;
import java.util.Objects;

/**
 * Named configuration presets for common generation modes.
 */
public enum GeneratorProfile {

    /**
     * Deterministic and tighter default ranges.
     */
    STRICT {
        @Override
        public GeneratorConfig.Builder applyToBuilder(GeneratorConfig.Builder builder) {
            Objects.requireNonNull(builder, "builder");
            return builder.seed(1L)
                          .stringLength(3, 12)
                          .collectionSize(1, 6);
        }
    },

    /**
     * User-facing defaults with broader ranges and locale-first behavior.
     */
    REALISTIC {
        @Override
        public GeneratorConfig.Builder applyToBuilder(GeneratorConfig.Builder builder) {
            Objects.requireNonNull(builder, "builder");
            return builder.stringLength(5, 24)
                          .collectionSize(1, 12);
        }
    },

    /**
     * Performance-oriented defaults using inexpensive seeded PRNG and small payloads.
     */
    FAST {
        @Override
        public GeneratorConfig.Builder applyToBuilder(GeneratorConfig.Builder builder) {
            Objects.requireNonNull(builder, "builder");
            return builder.seed(0L)
                          .stringLength(3, 10)
                          .collectionSize(0, 5);
        }
    };

    /**
     * Applies this profile template to an existing builder.
     */
    public abstract GeneratorConfig.Builder applyToBuilder(GeneratorConfig.Builder builder);

    /**
     * Creates a new config for this profile with default locale.
     */
    public GeneratorConfig createConfig() {
        return applyToBuilder(GeneratorConfig.builder()).build();
    }

    /**
     * Creates a new config for this profile and locale.
     */
    public GeneratorConfig createConfig(Locale locale) {
        return applyToBuilder(GeneratorConfig.builder().locale(locale)).build();
    }

    /**
     * Derives a config from an existing base config using this profile.
     */
    public GeneratorConfig applyTo(GeneratorConfig baseConfig) {
        Objects.requireNonNull(baseConfig, "baseConfig");
        return applyToBuilder(baseConfig.toBuilder()).build();
    }
}
