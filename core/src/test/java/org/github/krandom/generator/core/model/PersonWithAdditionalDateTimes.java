/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.core.model;

import java.time.Duration;
import java.time.MonthDay;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.Period;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZoneOffset;

/**
 * POJO with additional JSR-310 fields used to verify object-generation support.
 */
public class PersonWithAdditionalDateTimes {

    private OffsetDateTime offsetDateTime;
    private OffsetTime offsetTime;
    private Year year;
    private YearMonth yearMonth;
    private MonthDay monthDay;
    private Duration duration;
    private Period period;
    private ZoneId zoneId;
    private ZoneOffset zoneOffset;

    public PersonWithAdditionalDateTimes() { }

    public OffsetDateTime getOffsetDateTime() { return offsetDateTime; }
    public OffsetTime getOffsetTime() { return offsetTime; }
    public Year getYear() { return year; }
    public YearMonth getYearMonth() { return yearMonth; }
    public MonthDay getMonthDay() { return monthDay; }
    public Duration getDuration() { return duration; }
    public Period getPeriod() { return period; }
    public ZoneId getZoneId() { return zoneId; }
    public ZoneOffset getZoneOffset() { return zoneOffset; }
}
