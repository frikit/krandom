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
 * Generates everyday hobby names such as {@code "Photography"}.
 *
 * <pre>{@code
 *   String hobby = new HobbyGenerator().generate(); // e.g. "Gardening"
 * }</pre>
 */
public final class HobbyGenerator implements Generator<String> {

    private static final String[] HOBBIES = {
        "Photography", "Gardening", "Hiking", "Painting", "Cooking", "Reading", "Cycling",
        "Running", "Yoga", "Chess", "Knitting", "Woodworking", "Pottery", "Fishing",
        "Birdwatching", "Calligraphy", "Gaming", "Baking", "Surfing", "Climbing",
        "Dancing", "Sketching", "Astronomy", "Origami", "Skiing", "Brewing"
    };

    private final Random random;

    /**
     * Creates a generator using the default configuration.
     */
    public HobbyGenerator() {
        this(GeneratorConfig.defaults());
    }

    /**
     * Creates a generator from explicit configuration (optional seed).
     *
     * @param config the generator configuration; must not be {@code null}
     */
    public HobbyGenerator(GeneratorConfig config) {
        Objects.requireNonNull(config, "config must not be null");
        this.random = config.createRandom();
    }

    /**
     * Generates a uniformly random hobby name.
     *
     * @return a hobby such as {@code "Photography"}; never {@code null}
     */
    @Override
    public String generate() {
        return HOBBIES[random.nextInt(HOBBIES.length)];
    }
}
