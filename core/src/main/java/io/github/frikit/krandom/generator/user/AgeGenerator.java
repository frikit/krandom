/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.user;

import io.github.frikit.krandom.generator.Generator;
import io.github.frikit.krandom.generator.GeneratorConfig;

import java.security.SecureRandom;
import java.util.Objects;
import java.util.OptionalLong;
import java.util.Random;

/**
 * Generates random ages as integers.
 *
 * <p>Supports type-based ranges via {@link AgeType} (child, teen, adult, senior),
 * custom min/max bounds, or the full default range of 1–100.
 *
 * <pre>{@code
 * int age    = new AgeGenerator().generate();                      // 1–100
 * int adult  = new AgeGenerator(AgeType.ADULT).generate();        // 18–65
 * int child  = new AgeGenerator(AgeType.CHILD, 99L).generate();   // 1–12, seeded
 * int custom = new AgeGenerator(21, 30).generate();               // 21–30
 * }</pre>
 */
public final class AgeGenerator implements Generator<Integer> {

    private static final int DEFAULT_MIN = 1;
    private static final int DEFAULT_MAX = 100;

    private final Random random;
    private final int    minAge;
    private final int    maxAge;

    /**
     * Generates ages in the full range [1, 100].
     */
    public AgeGenerator() {
        this(DEFAULT_MIN, DEFAULT_MAX, OptionalLong.empty());
    }

    /**
     * Generates ages in the full range [1, 100] with a fixed seed for reproducible output.
     */
    public AgeGenerator(long seed) {
        this(DEFAULT_MIN, DEFAULT_MAX, OptionalLong.of(seed));
    }

    /**
     * Generates ages in the full range [1, 100] using the provided root config.
     */
    public AgeGenerator(GeneratorConfig config) {
        this(DEFAULT_MIN, DEFAULT_MAX, config);
    }

    /**
     * Generates ages in the range defined by the given {@link AgeType}.
     *
     * @param type the age category; must not be {@code null}
     */
    public AgeGenerator(AgeType type) {
        this(Objects.requireNonNull(type, "type must not be null").getMinAge(),
             type.getMaxAge(),
             OptionalLong.empty());
    }

    /**
     * Generates ages in the range defined by the given {@link AgeType}, with a fixed seed.
     *
     * @param type the age category; must not be {@code null}
     * @param seed PRNG seed for reproducible output
     */
    public AgeGenerator(AgeType type, long seed) {
        this(Objects.requireNonNull(type, "type must not be null").getMinAge(),
             type.getMaxAge(),
             OptionalLong.of(seed));
    }

    /**
     * Generates ages in the range defined by the given {@link AgeType}, using the provided root config.
     *
     * @param type   the age category; must not be {@code null}
     * @param config generator configuration; must not be {@code null}
     */
    public AgeGenerator(AgeType type, GeneratorConfig config) {
        this(Objects.requireNonNull(type, "type must not be null").getMinAge(),
             type.getMaxAge(),
             config);
    }

    /**
     * Generates ages in the given inclusive range.
     *
     * @param minAge minimum age (inclusive, must be ≥ 0)
     * @param maxAge maximum age (inclusive, must be ≥ {@code minAge})
     */
    public AgeGenerator(int minAge, int maxAge) {
        this(minAge, maxAge, OptionalLong.empty());
    }

    /**
     * Generates ages in the given inclusive range using the provided root config.
     *
     * @param minAge minimum age (inclusive, must be ≥ 0)
     * @param maxAge maximum age (inclusive, must be ≥ {@code minAge})
     * @param config generator configuration; must not be {@code null}
     */
    public AgeGenerator(int minAge, int maxAge, GeneratorConfig config) {
        this(minAge, maxAge, Objects.requireNonNull(config, "config must not be null").createRandom());
    }

    private AgeGenerator(int minAge, int maxAge, OptionalLong seed) {
        this(minAge, maxAge, seed.isPresent() ? new Random(seed.getAsLong()) : new SecureRandom());
    }

    private AgeGenerator(int minAge, int maxAge, Random random) {
        if (minAge < 0) {
            throw new IllegalArgumentException("minAge must be >= 0, got: " + minAge);
        }
        if (maxAge < minAge) {
            throw new IllegalArgumentException(
                "maxAge must be >= minAge, got: minAge=" + minAge + ", maxAge=" + maxAge);
        }
        this.minAge = minAge;
        this.maxAge = maxAge;
        this.random = Objects.requireNonNull(random, "random must not be null");
    }

    @Override
    public Integer generate() {
        return minAge + random.nextInt(maxAge - minAge + 1);
    }

    /**
     * Returns the minimum age (inclusive) this generator will produce.
     */
    public int getMinAge() {
        return minAge;
    }

    /**
     * Returns the maximum age (inclusive) this generator will produce.
     */
    public int getMaxAge() {
        return maxAge;
    }
}
