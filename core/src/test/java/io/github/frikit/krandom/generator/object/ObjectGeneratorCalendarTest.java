/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.object;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.frikit.krandom.generator.GeneratorConfig;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.PastOrPresent;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Set;
import java.util.TimeZone;
import java.util.stream.Collectors;
import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("ObjectGenerator — Calendar and GregorianCalendar members")
class ObjectGeneratorCalendarTest {

    private static final int SAMPLES = 100;

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-06-15T12:00:00Z"), ZoneId.of("Europe/Paris"));

    private static final Validator VALIDATOR = Validation.byProvider(HibernateValidator.class)
            .configure()
            .clockProvider(() -> CLOCK)
            .messageInterpolator(new ParameterMessageInterpolator())
            .buildValidatorFactory()
            .getValidator();

    static final class CalendarHolder {
        Calendar calendar;
        GregorianCalendar gregorian;
    }

    record CalendarRecord(Calendar calendar, GregorianCalendar gregorian) {}

    static final class ConstrainedCalendars {
        @Past Calendar past;

        @Future GregorianCalendar future;

        @PastOrPresent GregorianCalendar pastOrPresent;

        @FutureOrPresent Calendar futureOrPresent;

        @PastOrPresent @FutureOrPresent Calendar present;

        @PastOrPresent @FutureOrPresent GregorianCalendar presentGregorian;
    }

    static final class AuditedCalendars {
        Calendar createdAt;
        GregorianCalendar updatedAt;
    }

    static final class MixedAudit {
        Calendar createdAt;
        Instant updatedAt;
    }

    static final class Person {
        GregorianCalendar birthDate;
        int age;
    }

    static final class FixedAgePerson {
        Calendar birthDate;

        @Min(30) @Max(30) int age;
    }

    private static GeneratorConfig config(long seed) {
        return GeneratorConfig.builder().seed(seed).clock(CLOCK).build();
    }

    private static LocalDate localDate(Calendar calendar) {
        return calendar.toInstant().atZone(calendar.getTimeZone().toZoneId()).toLocalDate();
    }

    private static void assertGregorian(Calendar calendar) {
        GregorianCalendar gregorian = assertInstanceOf(GregorianCalendar.class, calendar);
        assertFalse(gregorian.isLenient());
    }

    @Test
    @DisplayName("Calendar and GregorianCalendar members are non-lenient Gregorian calendars in the default range")
    void calendarMembersAreGenerated() {
        for (int i = 0; i < SAMPLES; i++) {
            CalendarHolder holder = new ObjectGenerator<>(CalendarHolder.class, config(i)).generate();
            CalendarRecord record = new ObjectGenerator<>(CalendarRecord.class, config(i)).generate();

            for (Calendar calendar :
                    new Calendar[] {holder.calendar, holder.gregorian, record.calendar(), record.gregorian()}) {
                assertGregorian(calendar);
                LocalDate date = localDate(calendar);
                assertFalse(date.isBefore(LocalDate.of(1970, 1, 1)), date::toString);
                assertFalse(date.isAfter(LocalDate.of(2100, 12, 31)), date::toString);
            }
        }
    }

    @Test
    @DisplayName("a seed reproduces Calendar members")
    void seedReproducesCalendarMembers() {
        CalendarHolder first = new ObjectGenerator<>(CalendarHolder.class, config(42L)).generate();
        CalendarHolder second = new ObjectGenerator<>(CalendarHolder.class, config(42L)).generate();

        assertEquals(first.calendar, second.calendar);
        assertEquals(first.gregorian, second.gregorian);
    }

    @Test
    @DisplayName("objectDateRange bounds the local date of a Calendar member")
    void dateRangeBoundsTheLocalDate() {
        LocalDate day = LocalDate.of(2024, 2, 29);
        for (int i = 0; i < SAMPLES; i++) {
            GeneratorConfig ranged =
                    config(i).toBuilder().objectDateRange(day, day).build();

            CalendarHolder holder = new ObjectGenerator<>(CalendarHolder.class, ranged).generate();

            assertEquals(day, localDate(holder.calendar));
            assertEquals(day, localDate(holder.gregorian));
        }
    }

    @Test
    @DisplayName("@Past, @Future and their OrPresent forms hold against the configured clock")
    void temporalConstraintsHold() {
        for (int i = 0; i < SAMPLES; i++) {
            ConstrainedCalendars calendars = new ObjectGenerator<>(ConstrainedCalendars.class, config(i)).generate();

            assertGregorian(calendars.past);
            assertGregorian(calendars.future);
            assertGregorian(calendars.pastOrPresent);
            assertGregorian(calendars.futureOrPresent);
            assertEquals(TimeZone.getTimeZone(CLOCK.getZone()), calendars.past.getTimeZone());
            assertTrue(calendars.past.toInstant().isBefore(CLOCK.instant()));
            assertTrue(calendars.future.toInstant().isAfter(CLOCK.instant()));
            assertFalse(calendars.pastOrPresent.toInstant().isAfter(CLOCK.instant()));
            assertFalse(calendars.futureOrPresent.toInstant().isBefore(CLOCK.instant()));
            assertEquals(CLOCK.instant(), calendars.present.toInstant());
            assertEquals(CLOCK.instant(), calendars.presentGregorian.toInstant());
            assertGregorian(calendars.present);
            assertGregorian(calendars.presentGregorian);
            assertValid(calendars);
        }
    }

    @Test
    @DisplayName("semantic createdAt and updatedAt calendars fall in the last ten years, in order")
    void semanticCalendarsAreRecentAndOrdered() {
        LocalDate today = LocalDate.now(CLOCK);
        for (int i = 0; i < SAMPLES; i++) {
            AuditedCalendars audited = new ObjectGenerator<>(AuditedCalendars.class, config(i)).generate();

            assertGregorian(audited.createdAt);
            assertGregorian(audited.updatedAt);
            for (Calendar calendar : new Calendar[] {audited.createdAt, audited.updatedAt}) {
                LocalDate date = localDate(calendar);
                assertFalse(date.isBefore(today.minusYears(10)), date::toString);
                assertFalse(date.isAfter(today), date::toString);
            }
            assertFalse(audited.createdAt.after(audited.updatedAt));
        }
    }

    @Test
    @DisplayName("aligning createdAt and updatedAt keeps calendars in a one-day objectDateRange")
    void coherenceKeepsCalendarsInTheRange() {
        LocalDate day = LocalDate.of(2024, 2, 29);
        for (int i = 0; i < SAMPLES; i++) {
            GeneratorConfig ranged =
                    config(i).toBuilder().objectDateRange(day, day).build();

            AuditedCalendars audited = new ObjectGenerator<>(AuditedCalendars.class, ranged).generate();
            MixedAudit mixed = new ObjectGenerator<>(MixedAudit.class, ranged).generate();

            assertEquals(day, localDate(audited.createdAt));
            assertEquals(day, localDate(audited.updatedAt));
            assertFalse(audited.createdAt.after(audited.updatedAt));
            assertFalse(mixed.createdAt.toInstant().isAfter(mixed.updatedAt));
        }
    }

    @Test
    @DisplayName("a Calendar birthDate and age agree, whichever of the two is fixed")
    void birthDateAndAgeAgree() {
        LocalDate today = LocalDate.now(CLOCK);
        for (int i = 0; i < SAMPLES; i++) {
            Person person = new ObjectGenerator<>(Person.class, config(i)).generate();
            FixedAgePerson fixed = new ObjectGenerator<>(FixedAgePerson.class, config(i)).generate();

            LocalDate birthDate =
                    person.birthDate.toInstant().atOffset(ZoneOffset.UTC).toLocalDate();
            assertEquals(ChronoUnit.YEARS.between(birthDate, today), person.age);
            assertGregorian(fixed.birthDate);
            assertEquals(
                    today.minusYears(30),
                    fixed.birthDate.toInstant().atOffset(ZoneOffset.UTC).toLocalDate());
        }
    }

    private static void assertValid(Object value) {
        Set<ConstraintViolation<Object>> violations = VALIDATOR.validate(value);
        assertTrue(
                violations.isEmpty(),
                () -> violations.stream()
                        .map(violation -> violation.getPropertyPath() + "=" + violation.getInvalidValue())
                        .collect(Collectors.joining(", ")));
    }
}
