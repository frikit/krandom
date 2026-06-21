/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.vehicle;

import io.github.frikit.krandom.generator.Generator;
import io.github.frikit.krandom.generator.GeneratorConfig;

import java.util.Objects;
import java.util.Random;

/**
 * Generates vehicle makes, models, and US-style license plates.
 *
 * <p>{@link #generate()} returns a {@code "Make Model"} pair; {@link #make()} and {@link #model()}
 * return the parts individually, and {@link #licensePlate()} returns a plate such as
 * {@code "ABC 1234"}.
 *
 * <pre>{@code
 *   String v     = new VehicleGenerator().generate();     // e.g. "Toyota Corolla"
 *   String make  = new VehicleGenerator().make();         // e.g. "Honda"
 *   String plate = new VehicleGenerator().licensePlate(); // e.g. "ABC 1234"
 * }</pre>
 */
public final class VehicleGenerator implements Generator<String> {

    /** Each row is {@code {make, model, model, …}}. */
    private static final String[][] MAKE_MODELS = {
        {"Toyota", "Camry", "Corolla", "RAV4", "Highlander"},
        {"Honda", "Civic", "Accord", "CR-V", "Pilot"},
        {"Ford", "F-150", "Mustang", "Focus", "Explorer"},
        {"Chevrolet", "Silverado", "Malibu", "Equinox", "Tahoe"},
        {"Volkswagen", "Golf", "Passat", "Tiguan", "Jetta"},
        {"BMW", "3 Series", "5 Series", "X3", "X5"},
        {"Mercedes-Benz", "C-Class", "E-Class", "GLC", "S-Class"},
        {"Nissan", "Altima", "Sentra", "Rogue", "Leaf"},
        {"Hyundai", "Elantra", "Tucson", "Santa Fe", "Sonata"},
        {"Tesla", "Model 3", "Model S", "Model X", "Model Y"}
    };

    private final Random random;

    /**
     * Creates a generator using the default configuration.
     */
    public VehicleGenerator() {
        this(GeneratorConfig.defaults());
    }

    /**
     * Creates a generator from explicit configuration (optional seed).
     *
     * @param config the generator configuration; must not be {@code null}
     */
    public VehicleGenerator(GeneratorConfig config) {
        Objects.requireNonNull(config, "config must not be null");
        this.random = config.createRandom();
    }

    /**
     * Generates a {@code "Make Model"} pair (e.g. {@code "Toyota Corolla"}).
     *
     * @return a make-and-model string; never {@code null}
     */
    @Override
    public String generate() {
        String[] row = MAKE_MODELS[random.nextInt(MAKE_MODELS.length)];
        return row[0] + ' ' + row[1 + random.nextInt(row.length - 1)];
    }

    /**
     * Returns a random vehicle make (e.g. {@code "Honda"}).
     *
     * @return a make; never {@code null}
     */
    public String make() {
        return MAKE_MODELS[random.nextInt(MAKE_MODELS.length)][0];
    }

    /**
     * Returns a random model from a randomly chosen make (e.g. {@code "Civic"}).
     *
     * @return a model; never {@code null}
     */
    public String model() {
        String[] row = MAKE_MODELS[random.nextInt(MAKE_MODELS.length)];
        return row[1 + random.nextInt(row.length - 1)];
    }

    /**
     * Returns a US-style license plate: three uppercase letters, a space, then four digits
     * (e.g. {@code "ABC 1234"}).
     *
     * @return a license plate; never {@code null}
     */
    public String licensePlate() {
        StringBuilder sb = new StringBuilder(8);
        for (int i = 0; i < 3; i++) {
            sb.append((char) ('A' + random.nextInt(26)));
        }
        sb.append(' ');
        for (int i = 0; i < 4; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }
}
