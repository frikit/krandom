/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.datetime;

import io.github.frikit.krandom.generator.Generator;
import io.github.frikit.krandom.generator.GeneratorConfig;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

/**
 * Generates random {@link ZonedDateTime} values.
 *
 * <p>Combines a random {@link LocalDateTime} in [1970-01-01T00:00:00, 2100-12-31T23:59:59]
 * with a randomly selected {@link ZoneId} from the system's available time-zone set.
 *
 * <p><strong>Basic Usage:</strong>
 * <pre>{@code
 * ZonedDateTimeGenerator gen = new ZonedDateTimeGenerator();
 * ZonedDateTime zdt = gen.generate();  // e.g. 2031-03-15T09:47:22+05:30[Asia/Kolkata]
 * }</pre>
 *
 * <p><strong>Seeded Generation:</strong>
 * <pre>{@code
 * GeneratorConfig config = GeneratorConfig.builder().seed(42L).build();
 * ZonedDateTimeGenerator gen = new ZonedDateTimeGenerator(config);
 * ZonedDateTime zdt = gen.generate();  // reproducible
 * }</pre>
 *
 * <p><strong>Thread Safety:</strong>
 * This generator is thread-safe and can be shared across threads.
 *
 * @see LocalDateTimeGenerator
 * @see InstantGenerator
 */
public final class ZonedDateTimeGenerator implements Generator<ZonedDateTime> {

    private static final int MIN_YEAR = 1970;
    private static final int MAX_YEAR = 2100;

    /**
     * Sorted, stable snapshot of all available zone IDs taken at class-load time.
     */
    private static final List<ZoneId> ZONE_IDS;

    static {
        List<ZoneId> zones = new ArrayList<>();
        for (String id : ZoneId.getAvailableZoneIds()) {
            zones.add(ZoneId.of(id));
        }
        zones.sort(java.util.Comparator.comparing(ZoneId::getId));
        ZONE_IDS = java.util.Collections.unmodifiableList(zones);
    }

    private final Random    random;
    private final LocalDate rangeMin;
    private final LocalDate rangeMax;

    /**
     * Creates a zoned-date-time generator with default configuration.
     */
    public ZonedDateTimeGenerator() {
        this(GeneratorConfig.defaults());
    }

    /**
     * Creates a zoned-date-time generator with the specified configuration.
     *
     * @param config the generator configuration; must not be {@code null}
     * @throws NullPointerException if {@code config} is {@code null}
     */
    public ZonedDateTimeGenerator(GeneratorConfig config) {
        Objects.requireNonNull(config, "config must not be null");
        this.random = config.createRandom();
        this.rangeMin = null;
        this.rangeMax = null;
    }

    /**
     * Creates a zoned-date-time generator restricted to the given date range.
     * Intended for use by {@code FieldGeneratorResolver} when a date range is configured.
     *
     * @param min earliest date (inclusive)
     * @param max latest date (inclusive)
     */
    public ZonedDateTimeGenerator(LocalDate min, LocalDate max) {
        this.random = new SecureRandom();
        this.rangeMin = min;
        this.rangeMax = max;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Generates a random zoned date-time using a random local date-time and a random
     * available time zone.
     *
     * @return a random zoned date-time; never {@code null}
     */
    @Override
    public ZonedDateTime generate() {
        LocalDate date;
        if (rangeMin != null) {
            long lo = rangeMin.toEpochDay();
            long hi = rangeMax.toEpochDay();
            date = LocalDate.ofEpochDay(lo + random.nextLong(hi - lo + 1));
        } else {
            int year = MIN_YEAR + random.nextInt(MAX_YEAR - MIN_YEAR + 1);
            int month = 1 + random.nextInt(12);
            int maxDay = LocalDate.of(year, month, 1).lengthOfMonth();
            int day = 1 + random.nextInt(maxDay);
            date = LocalDate.of(year, month, day);
        }
        int hour = random.nextInt(24);
        int minute = random.nextInt(60);
        int second = random.nextInt(60);
        LocalDateTime ldt = LocalDateTime.of(date, LocalTime.of(hour, minute, second));
        ZoneId zone = ZONE_IDS.get(random.nextInt(ZONE_IDS.size()));
        return ZonedDateTime.of(ldt, zone);
    }
}
