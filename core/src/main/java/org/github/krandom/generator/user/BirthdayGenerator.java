/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.user;

import org.github.krandom.generator.Generator;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.OptionalLong;
import java.util.Random;

/**
 * Generates random birth dates as {@link LocalDate} values.
 *
 * <p>Supports type-based age ranges via {@link AgeType}, custom min/max age bounds,
 * or the default full range of 1–100 years ago.
 *
 * <pre>{@code
 * LocalDate birthday = new BirthdayGenerator().generate();
 * LocalDate adultBirthday = new BirthdayGenerator(AgeType.ADULT).generate();
 *
 * // Formatted output
 * String formatted = new BirthdayGenerator().generateAsString();         // "5/27/1983"
 * String american  = new BirthdayGenerator().generateAsAmericanString(); // "05/27/1983"
 * }</pre>
 */
public final class BirthdayGenerator implements Generator<LocalDate> {

    private static final int DEFAULT_MIN = 1;
    private static final int DEFAULT_MAX = 100;

    private static final DateTimeFormatter STRING_FORMAT   = DateTimeFormatter.ofPattern("M/d/yyyy");
    private static final DateTimeFormatter AMERICAN_FORMAT = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    private final Random random;
    private final int minAge;
    private final int maxAge;

    /** Generates birthdays for ages in the full range [1, 100]. */
    public BirthdayGenerator() {
        this(DEFAULT_MIN, DEFAULT_MAX, OptionalLong.empty());
    }

    /** Generates birthdays for ages [1, 100] with a fixed seed for reproducible output. */
    public BirthdayGenerator(long seed) {
        this(DEFAULT_MIN, DEFAULT_MAX, OptionalLong.of(seed));
    }

    /**
     * Generates birthdays appropriate for the given {@link AgeType}.
     *
     * @param type the age category; must not be {@code null}
     */
    public BirthdayGenerator(AgeType type) {
        this(Objects.requireNonNull(type, "type must not be null").getMinAge(),
                type.getMaxAge(),
                OptionalLong.empty());
    }

    /**
     * Generates birthdays appropriate for the given {@link AgeType}, with a fixed seed.
     *
     * @param type the age category; must not be {@code null}
     * @param seed PRNG seed for reproducible output
     */
    public BirthdayGenerator(AgeType type, long seed) {
        this(Objects.requireNonNull(type, "type must not be null").getMinAge(),
                type.getMaxAge(),
                OptionalLong.of(seed));
    }

    /**
     * Generates birthdays for the given inclusive age range.
     *
     * @param minAge minimum age in years (inclusive, must be ≥ 0)
     * @param maxAge maximum age in years (inclusive, must be ≥ {@code minAge})
     */
    public BirthdayGenerator(int minAge, int maxAge) {
        this(minAge, maxAge, OptionalLong.empty());
    }

    private BirthdayGenerator(int minAge, int maxAge, OptionalLong seed) {
        if (minAge < 0) {
            throw new IllegalArgumentException("minAge must be >= 0, got: " + minAge);
        }
        if (maxAge < minAge) {
            throw new IllegalArgumentException(
                    "maxAge must be >= minAge, got: minAge=" + minAge + ", maxAge=" + maxAge);
        }
        this.minAge = minAge;
        this.maxAge = maxAge;
        this.random = seed.isPresent() ? new Random(seed.getAsLong()) : new SecureRandom();
    }

    /**
     * Generates a random birth date consistent with a person of the configured age range.
     *
     * <p>The returned date is in the past; the person's current age (in completed years)
     * will fall within [{@code minAge}, {@code maxAge}].
     */
    @Override
    public LocalDate generate() {
        LocalDate today   = LocalDate.now();
        int age           = minAge + random.nextInt(maxAge - minAge + 1);
        LocalDate latest  = today.minusYears(age);
        LocalDate earliest = today.minusYears((long) age + 1).plusDays(1);
        long daysInRange  = ChronoUnit.DAYS.between(earliest, latest) + 1;
        return earliest.plusDays(random.nextLong(daysInRange));
    }

    /**
     * Generates a birthday and formats it as {@code M/d/yyyy} (e.g., {@code "5/27/1983"}).
     *
     * <p>Month and day are not zero-padded, matching the Chance.js {@code birthday({string:true})}
     * output.
     */
    public String generateAsString() {
        return generate().format(STRING_FORMAT);
    }

    /**
     * Generates a birthday and formats it as {@code MM/dd/yyyy} (e.g., {@code "05/27/1983"}).
     *
     * <p>Month and day are zero-padded, matching the Chance.js {@code birthday({american:true})}
     * output.
     */
    public String generateAsAmericanString() {
        return generate().format(AMERICAN_FORMAT);
    }

    /** Returns the minimum age (inclusive) used for birthday generation. */
    public int getMinAge() {
        return minAge;
    }

    /** Returns the maximum age (inclusive) used for birthday generation. */
    public int getMaxAge() {
        return maxAge;
    }
}
