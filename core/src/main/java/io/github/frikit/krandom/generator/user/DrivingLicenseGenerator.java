/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.user;

import io.github.frikit.krandom.generator.Generator;
import io.github.frikit.krandom.generator.GeneratorConfig;

import java.util.Objects;
import java.util.Random;

/**
 * Generates generic driving-license numbers in the form of two uppercase letters followed by six
 * digits, e.g. {@code "AB123456"} (matching {@code [A-Z]{2}[0-9]{6}}).
 *
 * <p>The format is a generic eight-character form; country-specific driving-license formats are a
 * future enhancement.
 *
 * <p>Configured generation is disabled by default. For an isolated compatibility fixture, select
 * {@link IdentityDocumentSafetyPolicy#REALISTIC_UNCLASSIFIED} explicitly.
 */
public final class DrivingLicenseGenerator implements Generator<String> {

    private static final String LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    private final Random random;
    private final IdentityDocumentSafetyPolicy safetyPolicy;

    /**
     * Creates a generator with the historical realistic-output behavior.
     *
     * @deprecated since 1.6; use {@link #DrivingLicenseGenerator(GeneratorConfig)} and select an
     * explicit safety policy instead
     */
    @Deprecated(since = "1.6", forRemoval = true)
    public DrivingLicenseGenerator() {
        this(GeneratorConfig.builder()
                            .identityDocumentSafetyPolicy(IdentityDocumentSafetyPolicy.REALISTIC_UNCLASSIFIED)
                            .build());
    }

    /**
     * Creates a generator from explicit configuration (optional seed).
     *
     * @param config the generator configuration; must not be {@code null}
     */
    public DrivingLicenseGenerator(GeneratorConfig config) {
        Objects.requireNonNull(config, "config must not be null");
        this.random = config.createRandom();
        this.safetyPolicy = config.getIdentityDocumentSafetyPolicy();
    }

    /**
     * Generates a generic driving-license number: two uppercase letters followed by six digits.
     *
     * @return a driving-license number such as {@code "AB123456"}; never {@code null}
     */
    @Override
    public String generate() {
        safetyPolicy.requireRealisticOutput();
        StringBuilder sb = new StringBuilder(8);
        sb.append(LETTERS.charAt(random.nextInt(LETTERS.length())));
        sb.append(LETTERS.charAt(random.nextInt(LETTERS.length())));
        for (int i = 0; i < 6; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }
}
