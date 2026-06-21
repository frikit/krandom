/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.user;

import io.github.frikit.krandom.generator.Generator;
import io.github.frikit.krandom.generator.GeneratorConfig;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Random;

/**
 * Generates Chinese zodiac animals such as {@code "Dragon"}.
 *
 * <p>{@link #generate()} returns a uniformly random animal. {@link #animalFor(int)} and
 * {@link #animalFor(LocalDate)} map a Gregorian year to its animal using the 12-year cycle.
 *
 * <p><b>Note:</b> the mapping is by Gregorian year, so dates falling between January 1 and the lunar
 * New Year are attributed to the Gregorian year's animal rather than the previous animal. This matches
 * common fixture libraries and keeps the result deterministic without a lunar calendar.
 *
 * <pre>{@code
 *   String any  = new ChineseZodiacGenerator().generate();      // e.g. "Tiger"
 *   String y24  = new ChineseZodiacGenerator().animalFor(2024); // "Dragon"
 * }</pre>
 */
public final class ChineseZodiacGenerator implements Generator<String> {

    /** Animals ordered so that {@code ANIMALS[year mod 12]} yields the year's animal. */
    private static final String[] ANIMALS = {
        "Monkey", "Rooster", "Dog", "Pig", "Rat", "Ox",
        "Tiger", "Rabbit", "Dragon", "Snake", "Horse", "Goat"
    };

    private final Random random;

    /**
     * Creates a generator using the default configuration.
     */
    public ChineseZodiacGenerator() {
        this(GeneratorConfig.defaults());
    }

    /**
     * Creates a generator from explicit configuration (optional seed).
     *
     * @param config the generator configuration; must not be {@code null}
     */
    public ChineseZodiacGenerator(GeneratorConfig config) {
        Objects.requireNonNull(config, "config must not be null");
        this.random = config.createRandom();
    }

    /**
     * Generates a uniformly random Chinese zodiac animal.
     *
     * @return an animal such as {@code "Dragon"}; never {@code null}
     */
    @Override
    public String generate() {
        return ANIMALS[random.nextInt(ANIMALS.length)];
    }

    /**
     * Returns the Chinese zodiac animal for the given Gregorian year.
     *
     * @param year the Gregorian year (negative years are supported)
     * @return the corresponding animal; never {@code null}
     */
    public String animalFor(int year) {
        return ANIMALS[Math.floorMod(year, 12)];
    }

    /**
     * Returns the Chinese zodiac animal for the given date's Gregorian year.
     *
     * @param date the date whose year's animal to resolve; must not be {@code null}
     * @return the corresponding animal; never {@code null}
     */
    public String animalFor(LocalDate date) {
        Objects.requireNonNull(date, "date must not be null");
        return animalFor(date.getYear());
    }
}
