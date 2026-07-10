/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.finance;

import io.github.frikit.krandom.generator.Generator;
import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.BusinessTaxIdentifierSafetyPolicy;

import java.security.SecureRandom;
import java.util.Objects;
import java.util.Random;

/**
 * Generates US EIN (Employer Identification Number) values.
 */
public final class EinGenerator implements Generator<String> {

    private final Random random;
    private final BusinessTaxIdentifierSafetyPolicy safetyPolicy;

    /**
     * Creates a generator with the historical realistic-output behavior.
     *
     * @deprecated since 1.6; use {@link #EinGenerator(GeneratorConfig)} and select an explicit
     * safety policy instead
     */
    @Deprecated(since = "1.6", forRemoval = true)
    public EinGenerator() {
        this(GeneratorConfig.builder()
                            .businessTaxIdentifierSafetyPolicy(
                                BusinessTaxIdentifierSafetyPolicy.REALISTIC_UNCLASSIFIED)
                            .build());
    }

    /**
     * Creates a generator from explicit configuration.
     *
     * @param config the generator configuration; must not be {@code null}
     */
    public EinGenerator(GeneratorConfig config) {
        Objects.requireNonNull(config, "config must not be null");
        this.random = config.createRandom();
        this.safetyPolicy = config.getBusinessTaxIdentifierSafetyPolicy();
    }

    /**
     * Generates formatted EIN value ({@code NN-NNNNNNN}).
     */
    @Override
    public String generate() {
        requireRealisticOutput();
        int prefix = 1 + random.nextInt(99);
        int suffix = random.nextInt(10_000_000);
        return String.format("%02d-%07d", prefix, suffix);
    }

    /**
     * Generates EIN digits without separator.
     */
    public String generateUnformatted() {
        return generate().replace("-", "");
    }

    private void requireRealisticOutput() {
        if (safetyPolicy == BusinessTaxIdentifierSafetyPolicy.DISABLED) {
            throw new IllegalStateException(
                "Business tax-identifier generation is disabled by default; select "
                + "businessTaxIdentifierSafetyPolicy(REALISTIC_UNCLASSIFIED) only for isolated fixtures");
        }
    }
}
