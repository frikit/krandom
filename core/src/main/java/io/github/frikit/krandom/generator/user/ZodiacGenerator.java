/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.user;

import io.github.frikit.krandom.generator.Generator;
import io.github.frikit.krandom.generator.GeneratorConfig;

import java.time.LocalDate;
import java.time.MonthDay;
import java.util.Objects;
import java.util.Random;

/**
 * Generates Western (tropical) zodiac signs such as {@code "Scorpio"}.
 *
 * <p>{@link #generate()} returns a uniformly random sign. To derive the sign that corresponds to a
 * particular birth date, use {@link #signFor(LocalDate)} or {@link #signFor(MonthDay)} — both use the
 * conventional tropical-zodiac date boundaries.
 *
 * <pre>{@code
 *   String any  = new ZodiacGenerator().generate();                 // e.g. "Leo"
 *   String mine = new ZodiacGenerator().signFor(LocalDate.of(1990, 11, 5)); // "Scorpio"
 * }</pre>
 */
public final class ZodiacGenerator implements Generator<String> {

    /** The twelve signs in zodiac order, used for uniform random selection. */
    private static final String[] SIGNS = {
        "Aries", "Taurus", "Gemini", "Cancer", "Leo", "Virgo",
        "Libra", "Scorpio", "Sagittarius", "Capricorn", "Aquarius", "Pisces"
    };

    /** First day (inclusive) on which each month's later sign begins, indexed by month - 1. */
    private static final int[] CUTOFF = {20, 19, 21, 20, 21, 21, 23, 23, 23, 23, 22, 22};

    /** The sign that begins on {@code CUTOFF[month - 1]} within each month, indexed by month - 1. */
    private static final String[] SIGN_FROM_CUTOFF = {
        "Aquarius", "Pisces", "Aries", "Taurus", "Gemini", "Cancer",
        "Leo", "Virgo", "Libra", "Scorpio", "Sagittarius", "Capricorn"
    };

    private final Random random;

    /**
     * Creates a generator using the default configuration.
     */
    public ZodiacGenerator() {
        this(GeneratorConfig.defaults());
    }

    /**
     * Creates a generator from explicit configuration (optional seed).
     *
     * @param config the generator configuration; must not be {@code null}
     */
    public ZodiacGenerator(GeneratorConfig config) {
        Objects.requireNonNull(config, "config must not be null");
        this.random = config.createRandom();
    }

    /**
     * Generates a uniformly random Western zodiac sign.
     *
     * @return a sign such as {@code "Scorpio"}; never {@code null}
     */
    @Override
    public String generate() {
        return SIGNS[random.nextInt(SIGNS.length)];
    }

    /**
     * Returns the Western zodiac sign for the given date.
     *
     * @param date the date whose sign to resolve; must not be {@code null}
     * @return the corresponding sign; never {@code null}
     */
    public String signFor(LocalDate date) {
        Objects.requireNonNull(date, "date must not be null");
        return signFor(MonthDay.from(date));
    }

    /**
     * Returns the Western zodiac sign for the given month and day.
     *
     * @param monthDay the month-day whose sign to resolve; must not be {@code null}
     * @return the corresponding sign; never {@code null}
     */
    public String signFor(MonthDay monthDay) {
        Objects.requireNonNull(monthDay, "monthDay must not be null");
        int idx = monthDay.getMonthValue() - 1;
        if (monthDay.getDayOfMonth() >= CUTOFF[idx]) {
            return SIGN_FROM_CUTOFF[idx];
        }
        return SIGN_FROM_CUTOFF[(idx + 11) % 12];
    }
}
