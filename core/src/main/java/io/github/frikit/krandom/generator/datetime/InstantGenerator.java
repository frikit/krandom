/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.datetime;

import io.github.frikit.krandom.generator.Generator;
import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.Seedable;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Objects;
import java.util.Random;

/**
 * Generates random {@link Instant} values.
 *
 * <p>The generated values fall on a random UTC day in the range [1970-01-01, 2100-12-31], at a random
 * time within that day to the millisecond, the precision of {@link java.util.Date}. This exercises
 * time-of-day logic and improves coverage, but it does not guarantee distinct instants.
 *
 * <p><strong>Basic Usage:</strong>
 * <pre>{@code
 * InstantGenerator gen = new InstantGenerator();
 * Instant instant = gen.generate();  // e.g. 2063-11-04T09:41:27.318Z
 * }</pre>
 *
 * <p><strong>Seeded Generation:</strong>
 * <pre>{@code
 * GeneratorConfig config = GeneratorConfig.builder().seed(42L).build();
 * InstantGenerator gen = new InstantGenerator(config);
 * Instant instant = gen.generate();  // reproducible
 * }</pre>
 *
 * <p><strong>Thread Safety:</strong>
 * This generator holds a single mutable PRNG. Concurrent calls are memory-safe but interleave the
 * random sequence (destroying reproducibility) and contend on the PRNG, so an instance is <em>not</em>
 * safe to share across threads. Confine one instance per thread, or wrap construction with
 * {@code Generators.threadLocal(...)} for deterministic concurrent use.
 *
 * @see DateGenerator
 * @see LocalDateTimeGenerator
 */
public final class InstantGenerator implements Generator<Instant>, Seedable {

    private static final int MIN_YEAR = 1970;
    private static final int MAX_YEAR = 2100;
    private static final long MILLIS_PER_DAY = Duration.ofDays(1).toMillis();

    private final Random random;
    private final @org.jspecify.annotations.Nullable LocalDate rangeMin;
    private final @org.jspecify.annotations.Nullable LocalDate rangeMax;

    /**
     * Creates an instant generator with default configuration.
     */
    public InstantGenerator() {
        this(GeneratorConfig.defaults());
    }

    /**
     * Creates an instant generator with the specified configuration.
     *
     * @param config the generator configuration; must not be {@code null}
     * @throws NullPointerException if {@code config} is {@code null}
     */
    public InstantGenerator(GeneratorConfig config) {
        Objects.requireNonNull(config, "config must not be null");
        this.random = config.createRandom();
        this.rangeMin = null;
        this.rangeMax = null;
    }

    /**
     * Creates an instant generator restricted to the given date range.
     * Intended for use by {@code FieldGeneratorResolver} when a date range is configured.
     *
     * @param min earliest date (inclusive, from the start of that UTC day)
     * @param max latest date (inclusive, through the end of that UTC day)
     */
    public InstantGenerator(LocalDate min, LocalDate max) {
        this.random = new Random();
        this.rangeMin = min;
        this.rangeMax = max;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Generates a random instant, to the millisecond, within a random UTC day of the date range or
     * of [1970-01-01, 2100-12-31].
     *
     * @return a random instant; never {@code null}
     */
    @Override
    public Instant generate() {
        LocalDate date;
        if (rangeMin != null) {
            long lo = rangeMin.toEpochDay();
            long hi = Objects.requireNonNull(rangeMax).toEpochDay();
            date = LocalDate.ofEpochDay(lo + random.nextLong(hi - lo + 1));
        } else {
            int year = MIN_YEAR + random.nextInt(MAX_YEAR - MIN_YEAR + 1);
            int month = 1 + random.nextInt(12);
            int maxDay = LocalDate.of(year, month, 1).lengthOfMonth();
            int day = 1 + random.nextInt(maxDay);
            date = LocalDate.of(year, month, day);
        }
        return date.atStartOfDay().toInstant(ZoneOffset.UTC).plusMillis(random.nextLong(MILLIS_PER_DAY));
    }

    /**
     * Reseeds this generator's owned random source for deterministic replay.
     */
    @Override
    public void reseed(long seed) {
        random.setSeed(seed);
    }
}
