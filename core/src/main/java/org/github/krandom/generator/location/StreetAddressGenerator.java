/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.location;

import org.github.krandom.generator.Generator;
import org.github.krandom.generator.GeneratorConfig;

import java.security.SecureRandom;
import java.util.Objects;
import java.util.Random;

/**
 * Generates random US-style street addresses (e.g. {@code "742 Evergreen Terrace"}).
 *
 * <p>The house number is drawn from [1, 9999]; the street name and type are chosen
 * from built-in word lists, giving 200 possible combinations before number variation.
 *
 * <pre>{@code
 * StreetAddressGenerator gen = new StreetAddressGenerator();
 * String address = gen.generate();  // "123 Oak Ave"
 * }</pre>
 *
 * <p><strong>Seeded Generation:</strong>
 * <pre>{@code
 * GeneratorConfig config = GeneratorConfig.builder().seed(42L).build();
 * StreetAddressGenerator gen = new StreetAddressGenerator(config);
 * String address = gen.generate();  // Reproducible output
 * }</pre>
 */
public final class StreetAddressGenerator implements Generator<String> {

    private static final String[] STREET_NAMES = {
        "Main", "Oak", "Maple", "Cedar", "Pine", "Elm", "Washington", "Park",
        "Lake", "Hill", "Ridge", "Forest", "Meadow", "River", "Sunset",
        "Spring", "Valley", "Highland", "Willow", "Lincoln"
    };

    private static final String[] STREET_TYPES = {
        "St", "Ave", "Blvd", "Dr", "Rd", "Ln", "Ct", "Pl", "Way", "Cir"
    };

    private final GeneratorConfig config;
    private final Random random;

    /** Creates a street-address generator with default configuration. */
    public StreetAddressGenerator() {
        this(GeneratorConfig.defaults());
    }

    /**
     * Creates a street-address generator with the specified configuration.
     *
     * @param config the generator configuration; must not be {@code null}
     * @throws NullPointerException if {@code config} is {@code null}
     */
    public StreetAddressGenerator(GeneratorConfig config) {
        this.config = Objects.requireNonNull(config, "config must not be null");
        this.random = config.getSeed().isPresent()
                ? new Random(config.getSeed().getAsLong())
                : new SecureRandom();
    }

    /**
     * {@inheritDoc}
     *
     * <p>Generates a random street address of the form {@code "<number> <streetName> <streetType>"}.
     *
     * @return a street-address string; never {@code null}
     */
    @Override
    public String generate() {
        int number  = random.nextInt(1, 10000);
        String name = STREET_NAMES[random.nextInt(STREET_NAMES.length)];
        String type = STREET_TYPES[random.nextInt(STREET_TYPES.length)];
        return number + " " + name + " " + type;
    }
}
