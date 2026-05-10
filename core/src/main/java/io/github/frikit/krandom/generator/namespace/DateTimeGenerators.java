/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.namespace;

import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.datetime.*;

/**
 * Fluent namespace for date/time-related generators.
 *
 * <p>Usage: {@code Generators.datetime().localDate().generate()}
 */
public final class DateTimeGenerators {

    private final GeneratorConfig config;

    public DateTimeGenerators() {
        this(GeneratorConfig.builder().build());
    }

    public DateTimeGenerators(GeneratorConfig config) {
        this.config = config;
    }

    public DateGenerator localDate() { return new DateGenerator(config); }

    public UtilDateGenerator utilDate() { return new UtilDateGenerator(config); }

    public SqlDateGenerator sqlDate() { return new SqlDateGenerator(config); }

    public SqlTimeGenerator sqlTime() { return new SqlTimeGenerator(config); }

    public SqlTimestampGenerator sqlTimestamp() { return new SqlTimestampGenerator(config); }

    public TimeGenerator localTime() { return new TimeGenerator(config); }

    public LocalDateTimeGenerator localDateTime() { return new LocalDateTimeGenerator(config); }

    public InstantGenerator instant() { return new InstantGenerator(config); }

    public OffsetTimeGenerator offsetTime() { return new OffsetTimeGenerator(config); }

    public ZonedDateTimeGenerator zonedDateTime() { return new ZonedDateTimeGenerator(config); }

    public OffsetDateTimeGenerator offsetDateTime() { return new OffsetDateTimeGenerator(config); }

    public YearGenerator year() { return new YearGenerator(config); }

    public YearMonthGenerator yearMonth() { return new YearMonthGenerator(config); }

    public MonthDayGenerator monthDay() { return new MonthDayGenerator(config); }

    public DurationGenerator duration() { return new DurationGenerator(config); }

    public PeriodGenerator period() { return new PeriodGenerator(config); }

    public ZoneIdGenerator zoneId() { return new ZoneIdGenerator(config); }

    public ZoneOffsetGenerator zoneOffset() { return new ZoneOffsetGenerator(config); }

    public TimezoneGenerator timezone() { return new TimezoneGenerator(config); }

    public LegacyTimeZoneGenerator timeZone() { return new LegacyTimeZoneGenerator(config); }

    public CalendarGenerator calendar() { return new CalendarGenerator(config); }
}
