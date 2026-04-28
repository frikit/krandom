/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.datetime;

import io.github.frikit.krandom.generator.Generator;
import io.github.frikit.krandom.generator.GeneratorConfig;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Objects;
import java.util.Random;

/**
 * Generates legacy {@link Calendar} values using the same default temporal range as modern
 * date/time generators.
 */
public final class CalendarGenerator implements Generator<Calendar> {

    private static final int MIN_YEAR = 1970;
    private static final int MAX_YEAR = 2100;
    private static final List<ZoneId> ZONE_IDS;

    static {
        List<ZoneId> zones = new ArrayList<>();
        for (String id : ZoneId.getAvailableZoneIds()) {
            zones.add(ZoneId.of(id));
        }
        zones.sort(Comparator.comparing(ZoneId::getId));
        ZONE_IDS = Collections.unmodifiableList(zones);
    }

    private final Random    random;
    private final LocalDate rangeMin;
    private final LocalDate rangeMax;

    /**
     * Creates a calendar generator with default configuration.
     */
    public CalendarGenerator() {
        this(GeneratorConfig.defaults());
    }

    /**
     * Creates a deterministic calendar generator using the supplied seed.
     *
     * @param seed deterministic seed
     */
    public CalendarGenerator(long seed) {
        this(GeneratorConfig.builder().seed(seed).build());
    }

    /**
     * Creates a calendar generator with explicit configuration.
     *
     * @param config generator configuration
     */
    public CalendarGenerator(GeneratorConfig config) {
        this(config, null, null);
    }

    /**
     * Creates an unseeded calendar generator restricted to the given date range.
     *
     * @param min earliest date, inclusive
     * @param max latest date, inclusive
     */
    public CalendarGenerator(LocalDate min, LocalDate max) {
        this(GeneratorConfig.defaults(), min, max);
    }

    /**
     * Creates a calendar generator restricted to the given date range.
     *
     * @param min    earliest date, inclusive
     * @param max    latest date, inclusive
     * @param config generator configuration
     */
    public CalendarGenerator(LocalDate min, LocalDate max, GeneratorConfig config) {
        this(config, min, max);
    }

    private CalendarGenerator(GeneratorConfig config, LocalDate min, LocalDate max) {
        Objects.requireNonNull(config, "config must not be null");
        if ((min == null) != (max == null)) {
            throw new IllegalArgumentException("min and max must both be provided or both be null");
        }
        if (min != null && min.isAfter(max)) {
            throw new IllegalArgumentException("min must be on or before max");
        }
        this.random = config.createRandom();
        this.rangeMin = min;
        this.rangeMax = max;
    }

    /**
     * Generates a non-lenient {@link GregorianCalendar}.
     *
     * @return generated calendar
     */
    @Override
    public Calendar generate() {
        GregorianCalendar calendar = GregorianCalendar.from(generateZonedDateTime());
        calendar.setLenient(false);
        return calendar;
    }

    private ZonedDateTime generateZonedDateTime() {
        LocalDate date = rangeMin == null ? generateDefaultDate() : generateRangeDate();
        LocalTime time = LocalTime.of(random.nextInt(24), random.nextInt(60), random.nextInt(60));
        ZoneId zone = ZONE_IDS.get(random.nextInt(ZONE_IDS.size()));
        return ZonedDateTime.of(LocalDateTime.of(date, time), zone);
    }

    private LocalDate generateDefaultDate() {
        int year = MIN_YEAR + random.nextInt(MAX_YEAR - MIN_YEAR + 1);
        int month = 1 + random.nextInt(12);
        int maxDay = LocalDate.of(year, month, 1).lengthOfMonth();
        return LocalDate.of(year, month, 1 + random.nextInt(maxDay));
    }

    private LocalDate generateRangeDate() {
        long lo = rangeMin.toEpochDay();
        long hi = rangeMax.toEpochDay();
        return LocalDate.ofEpochDay(lo + random.nextLong(hi - lo + 1));
    }
}
