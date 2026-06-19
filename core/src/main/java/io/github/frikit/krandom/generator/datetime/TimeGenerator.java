/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.datetime;

import io.github.frikit.krandom.generator.Generator;
import io.github.frikit.krandom.generator.GeneratorConfig;

import java.security.SecureRandom;
import java.time.LocalTime;
import java.util.Objects;
import java.util.Random;

/**
 * Generates random times and time components.
 *
 * <p>This generator produces times in various formats and provides methods
 * to generate individual time components like hour, minute, second, etc.
 *
 * <p><strong>Basic Usage:</strong>
 * <pre>{@code
 * TimeGenerator gen = new TimeGenerator();
 * LocalTime time = gen.generate();  // Random time
 * String timeStr = gen.generateString();  // "14:23:45"
 * }</pre>
 *
 * <p><strong>Time Components:</strong>
 * <pre>{@code
 * TimeGenerator gen = new TimeGenerator();
 * int hour12 = gen.generateHour();      // 1-12
 * int hour24 = gen.generateHour24();    // 0-23
 * int minute = gen.generateMinute();    // 0-59
 * int second = gen.generateSecond();    // 0-59
 * int millis = gen.generateMillisecond();  // 0-999
 * String ampm = gen.generateAmPm();     // "am" or "pm"
 * }</pre>
 *
 * <p><strong>Seeded Generation:</strong>
 * <pre>{@code
 * GeneratorConfig config = GeneratorConfig.builder().seed(12345L).build();
 * TimeGenerator gen = new TimeGenerator(config);
 * LocalTime time = gen.generate();  // Reproducible output
 * }</pre>
 *
 * <p><strong>Thread Safety:</strong>
 * This generator holds a single mutable PRNG. Concurrent calls are memory-safe but interleave the
 * random sequence (destroying reproducibility) and contend on the PRNG, so an instance is <em>not</em>
 * safe to share across threads. Confine one instance per thread, or wrap construction with
 * {@code Generators.threadLocal(...)} for deterministic concurrent use.
 */
public final class TimeGenerator implements Generator<LocalTime> {

    private final GeneratorConfig config;
    private final Random          random;

    /**
     * Creates a time generator with default configuration.
     */
    public TimeGenerator() {
        this(GeneratorConfig.defaults());
    }

    /**
     * Creates a time generator with the specified configuration.
     *
     * @param config the generator configuration; must not be {@code null}
     * @throws NullPointerException if {@code config} is {@code null}
     */
    public TimeGenerator(GeneratorConfig config) {
        this.config = Objects.requireNonNull(config, "config must not be null");
        this.random = config.createRandom();
    }

    /**
     * {@inheritDoc}
     *
     * <p>Generates a random time.
     *
     * @return a random time; never {@code null}
     */
    @Override
    public LocalTime generate() {
        int hour = generateHour24();
        int minute = generateMinute();
        int second = generateSecond();
        int nano = generateMillisecond() * 1_000_000; // Convert milliseconds to nanoseconds
        return LocalTime.of(hour, minute, second, nano);
    }

    /**
     * Generates a time as a string in ISO format (HH:MM:SS).
     *
     * @return a time string; never {@code null}
     */
    public String generateString() {
        LocalTime time = generate();
        return String.format("%02d:%02d:%02d",
                             time.getHour(), time.getMinute(), time.getSecond());
    }

    /**
     * Generates a random hour in 12-hour format (1-12).
     *
     * @return an hour
     */
    public int generateHour() {
        return 1 + random.nextInt(12);
    }

    /**
     * Generates a random hour in 24-hour format (0-23).
     *
     * @return an hour
     */
    public int generateHour24() {
        return random.nextInt(24);
    }

    /**
     * Generates a random minute (0-59).
     *
     * @return a minute
     */
    public int generateMinute() {
        return random.nextInt(60);
    }

    /**
     * Generates a random second (0-59).
     *
     * @return a second
     */
    public int generateSecond() {
        return random.nextInt(60);
    }

    /**
     * Generates a random millisecond (0-999).
     *
     * @return a millisecond
     */
    public int generateMillisecond() {
        return random.nextInt(1000);
    }

    /**
     * Generates a random AM/PM indicator.
     *
     * @return "am" or "pm"
     */
    public String generateAmPm() {
        return random.nextBoolean() ? "am" : "pm";
    }
}
