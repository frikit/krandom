/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.object;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.frikit.krandom.generator.Generator;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.lang.reflect.Field;
import java.lang.reflect.RecordComponent;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.MonthDay;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("BeanValidationSupport")
class BeanValidationSupportTest {

    @Test
    @DisplayName("returns generator for sized string")
    void sizedStringConstraint() throws Exception {
        Field field = Sample.class.getDeclaredField("sizedText");
        Generator<?> generator = BeanValidationSupport.constraintGeneratorFor(field, String.class);
        assertNotNull(generator);
    }

    @Test
    @DisplayName("returns generator for partial decimal constraints")
    void partialDecimalConstraint() throws Exception {
        Field field = Sample.class.getDeclaredField("decimalMinOnly");
        Generator<?> generator = BeanValidationSupport.constraintGeneratorFor(field, BigDecimal.class);
        assertNotNull(generator);
        assertTrue(((BigDecimal) generator.generate()).compareTo(new BigDecimal("1.5")) >= 0);
    }

    @Test
    @DisplayName("finds getter annotations when field annotation is absent")
    void getterConstraint() throws Exception {
        Field field = Sample.class.getDeclaredField("getterSizedText");
        Generator<?> generator = BeanValidationSupport.constraintGeneratorFor(field, String.class, new Random(1L));
        assertNotNull(generator);
        assertTrue(((String) generator.generate()).length() >= 3);
    }

    @Test
    @DisplayName("returns null generator for @Null reference type")
    void nullConstraint() throws Exception {
        Field field = Sample.class.getDeclaredField("nullOnly");
        Generator<?> generator = BeanValidationSupport.constraintGeneratorFor(field, String.class);
        assertNotNull(generator);
        assertNull(generator.generate());
    }

    @Test
    @DisplayName("covers native numeric and size branch variants")
    void numericAndSizeVariants() throws Exception {
        assertNull(BeanValidationSupport.constraintGeneratorFor(null, String.class));
        assertEquals(4, BeanValidationSupport.sizeFor(null, new Random(1L), 4, 4));
        assertEquals(
                0,
                BeanValidationSupport.sizeFor(
                        BeanValidationSupport.sizeRangeFor(field("emptyText")), new Random(1L), 1, 10));

        String empty = (String) generatorFor("emptyText", String.class).generate();
        assertEquals("", empty);

        BeanValidationSupport.SizeRange unbounded = BeanValidationSupport.sizeRangeFor(field("unboundedText"));
        assertEquals(new BeanValidationSupport.SizeRange(1, Integer.MAX_VALUE), unbounded);
        int unboundedSize = BeanValidationSupport.sizeFor(unbounded, new Random(1L), 1, 10);
        assertTrue(unboundedSize >= 1 && unboundedSize <= 10);
        assertEquals(
                new BeanValidationSupport.SizeRange(1, 255),
                BeanValidationSupport.constraintModelFor(field("unboundedText"), String.class)
                        .text()
                        .size());
        assertEquals(
                new BeanValidationSupport.SizeRange(300, 300),
                new BeanValidationSupport.SizeRange(300, Integer.MAX_VALUE).forText());
        assertEquals(new BeanValidationSupport.SizeRange(2, 4), new BeanValidationSupport.SizeRange(2, 4).forText());

        String numericText = (String) generatorFor("numericText", String.class).generate();
        assertTrue(Long.parseLong(numericText) >= 5L && Long.parseLong(numericText) <= 7L);

        String decimalText = (String) generatorFor("decimalText", String.class).generate();
        assertEquals(0, new BigDecimal(decimalText).compareTo(new BigDecimal("1.25")));

        String decimalMaxText =
                (String) generatorFor("decimalMaxText", String.class).generate();
        assertTrue(new BigDecimal(decimalMaxText).compareTo(new BigDecimal("9.50")) <= 0);

        BigDecimal exclusiveMin =
                (BigDecimal) generatorFor("exclusiveMin", BigDecimal.class).generate();
        assertTrue(exclusiveMin.compareTo(new BigDecimal("1.00")) > 0);

        BigDecimal exclusiveMax =
                (BigDecimal) generatorFor("exclusiveMax", BigDecimal.class).generate();
        assertTrue(exclusiveMax.compareTo(new BigDecimal("5.00")) < 0);

        BigDecimal decimalMinWithMax =
                (BigDecimal) generatorFor("decimalMinWithMax", BigDecimal.class).generate();
        assertTrue(decimalMinWithMax.compareTo(new BigDecimal("1.00")) >= 0);
        assertTrue(decimalMinWithMax.compareTo(new BigDecimal("2.00")) <= 0);

        BigDecimal decimalMaxWithMin =
                (BigDecimal) generatorFor("decimalMaxWithMin", BigDecimal.class).generate();
        assertTrue(decimalMaxWithMin.compareTo(new BigDecimal("1.00")) >= 0);
        assertTrue(decimalMaxWithMin.compareTo(new BigDecimal("2.00")) <= 0);

        assertThrows(
                BeanValidationSupport.ConstraintConflictException.class,
                () -> generatorFor("invertedDecimal", BigDecimal.class));

        assertEquals(5L, generatorFor("exactLong", long.class).generate());
        assertThrows(
                BeanValidationSupport.ConstraintConflictException.class,
                () -> generatorFor("nullPrimitive", int.class));
        assertTrue((Byte) generatorFor("boxedByte", Byte.class).generate() <= (byte) 10);
        assertTrue((Short) generatorFor("boxedShort", Short.class).generate() >= (short) 3);
        assertTrue((Long) generatorFor("boxedLong", Long.class).generate() >= 5L);
        assertTrue(((Number) generatorFor("boundedNumber", Number.class).generate()).longValue() >= 5L);
        assertEquals(
                BigInteger.valueOf(5L),
                generatorFor("exactBigInteger", BigInteger.class).generate());
        assertThrows(
                BeanValidationSupport.ConstraintConflictException.class,
                () -> generatorFor("invertedBigInteger", BigInteger.class));
        assertTrue(((BigInteger) generatorFor("positiveBigInteger", BigInteger.class)
                                .generate())
                        .signum()
                > 0);
        assertTrue((Float) generatorFor("positivePrimitiveFloat", float.class).generate() > 0.0f);
        assertTrue((Float) generatorFor("positiveFloat", Float.class).generate() > 0.0f);
        assertEquals(2.0d, (Double) generatorFor("exactDouble", Double.class).generate());
        assertTrue((Double) generatorFor("negativeDouble", Double.class).generate() < 0.0d);
        assertThrows(
                BeanValidationSupport.ConstraintConflictException.class,
                () -> generatorFor("numericUnsupported", Object.class));
        assertNull(generatorFor("unconstrained", Object.class));
    }

    @Test
    @DisplayName("covers temporal branch variants")
    void temporalVariants() throws Exception {
        assertNotNull(generatorFor("futureLocalDateTime", LocalDateTime.class).generate());
        assertNotNull(generatorFor("pastLocalDateTime", LocalDateTime.class).generate());
        assertNotNull(generatorFor("futureZonedDateTime", ZonedDateTime.class).generate());
        assertNotNull(generatorFor("futureOffsetDateTime", OffsetDateTime.class).generate());
        assertNotNull(generatorFor("futureSqlDate", java.sql.Date.class).generate());
        assertNotNull(generatorFor("futureTimestamp", Timestamp.class).generate());
        assertNotNull(generatorFor("futureLocalTime", LocalTime.class).generate());
        assertNotNull(generatorFor("pastLocalTime", LocalTime.class).generate());
        assertNotNull(generatorFor("futureOffsetTime", OffsetTime.class).generate());
        assertNotNull(generatorFor("futureYear", Year.class).generate());
        assertNotNull(generatorFor("pastYear", Year.class).generate());
        assertNotNull(generatorFor("futureYearMonth", YearMonth.class).generate());
        assertNotNull(generatorFor("pastYearMonth", YearMonth.class).generate());
        assertNotNull(generatorFor("futureMonthDay", MonthDay.class).generate());
        assertThrows(
                BeanValidationSupport.ConstraintConflictException.class,
                () -> generatorFor("futureUnsupported", Object.class));
    }

    @Test
    @DisplayName("temporal constraints use the configured clock")
    void temporalConstraintsUseClock() throws Exception {
        Clock clock = Clock.fixed(Instant.parse("2026-06-04T12:00:00Z"), ZoneId.of("Asia/Tokyo"));
        ZonedDateTime zoned = (ZonedDateTime) BeanValidationSupport.constraintGeneratorFor(
                        field("futureZonedDateTime"), ZonedDateTime.class, new Random(1L), clock)
                .generate();
        OffsetTime offsetTime = (OffsetTime) BeanValidationSupport.constraintGeneratorFor(
                        field("futureOffsetTime"), OffsetTime.class, new Random(1L), clock)
                .generate();
        Year futureYear = (Year)
                BeanValidationSupport.constraintGeneratorFor(field("futureYear"), Year.class, new Random(1L), clock)
                        .generate();

        assertEquals(clock.getZone(), zoned.getZone());
        assertTrue(zoned.toInstant().isAfter(clock.instant()));
        assertEquals(OffsetDateTime.now(clock).getOffset(), offsetTime.getOffset());
        assertEquals(Year.now(clock).plusYears(1), futureYear);
        assertThrows(
                NullPointerException.class,
                () -> BeanValidationSupport.constraintGeneratorFor(
                        field("futureYear"), Year.class, new Random(1L), null));
    }

    @Test
    @DisplayName("accepts reads each date and time type against the clock as Hibernate Validator does")
    void acceptsReadsEachDateAndTimeTypeAgainstTheClock() throws Exception {
        Clock clock = Clock.fixed(Instant.parse("2026-06-04T12:00:00Z"), ZoneId.of("Asia/Tokyo"));
        Instant now = clock.instant();
        Instant before = now.minusSeconds(1);
        Instant after = now.plusSeconds(1);
        LocalDate today = LocalDate.now(clock);
        LocalDateTime localNow = LocalDateTime.now(clock);
        OffsetDateTime offsetNow = OffsetDateTime.now(clock);
        ZonedDateTime zonedNow = ZonedDateTime.now(clock);
        // A value of each type before, at and after the clock's present, after the type it is checked as.
        List<List<Object>> rows = List.of(
                List.of(Instant.class, before, now, after),
                List.of(LocalDate.class, today.minusDays(1), today, today.plusDays(1)),
                List.of(LocalDateTime.class, localNow.minusSeconds(1), localNow, localNow.plusSeconds(1)),
                List.of(OffsetDateTime.class, offsetNow.minusSeconds(1), offsetNow, offsetNow.plusSeconds(1)),
                List.of(ZonedDateTime.class, zonedNow.minusSeconds(1), zonedNow, zonedNow.plusSeconds(1)),
                List.of(GregorianCalendar.class, calendarAt(before), calendarAt(now), calendarAt(after)),
                List.of(
                        java.util.Date.class,
                        java.util.Date.from(before),
                        java.util.Date.from(now),
                        java.util.Date.from(after)),
                List.of(
                        java.sql.Date.class,
                        new java.sql.Date(before.toEpochMilli()),
                        new java.sql.Date(now.toEpochMilli()),
                        new java.sql.Date(after.toEpochMilli())),
                List.of(Timestamp.class, Timestamp.from(before), Timestamp.from(now), Timestamp.from(after)));
        // Which of before, at and after each bound allows.
        Map<String, List<Boolean>> allowed = Map.of(
                "pastBound", List.of(true, false, false),
                "pastOrPresentBound", List.of(true, true, false),
                "futureBound", List.of(false, false, true),
                "futureOrPresentBound", List.of(false, true, true),
                "presentBound", List.of(false, true, false));

        for (List<Object> row : rows) {
            Class<?> type = (Class<?>) row.get(0);
            for (Map.Entry<String, List<Boolean>> bound : allowed.entrySet()) {
                for (int when = 0; when < 3; when++) {
                    Object value = row.get(when + 1);
                    assertEquals(
                            bound.getValue().get(when),
                            accepts(bound.getKey(), type, value, clock),
                            bound.getKey() + " " + type.getSimpleName() + " " + value);
                }
            }
        }

        // A LocalDate is read against the date in the clock's zone, here a day ahead of UTC.
        Clock tokyoEvening = Clock.fixed(Instant.parse("2026-06-04T20:00:00Z"), ZoneId.of("Asia/Tokyo"));
        assertTrue(accepts("pastBound", LocalDate.class, LocalDate.of(2026, 6, 4), tokyoEvening));
    }

    @Test
    @DisplayName(
            "accepts gives Hibernate Validator's verdict for each date and time type, under a clock finer than a millisecond")
    void acceptsGivesHibernateValidatorsVerdictUnderAClockFinerThanAMillisecond() throws Exception {
        // Half a millisecond into a second, so that a value at the clock's millisecond is before its instant.
        Clock clock = Clock.fixed(Instant.parse("2026-06-04T12:00:00.000500Z"), ZoneId.of("Asia/Tokyo"));
        Validator hibernate = Validation.byProvider(HibernateValidator.class)
                .configure()
                .clockProvider(() -> clock)
                .messageInterpolator(new ParameterMessageInterpolator())
                .buildValidatorFactory()
                .getValidator();
        Instant now = clock.instant();
        Instant millisecond = now.truncatedTo(ChronoUnit.MILLIS);
        LocalDate today = LocalDate.now(clock);
        LocalDateTime localNow = LocalDateTime.now(clock);
        OffsetDateTime offsetNow = OffsetDateTime.now(clock);
        ZonedDateTime zonedNow = ZonedDateTime.now(clock);
        // The clock's instant at another offset or zone too, which Hibernate Validator compares by local date-time.
        Map<Class<?>, List<Object>> values = Map.of(
                InstantBounds.class,
                List.of(now.minusSeconds(1), millisecond, now, now.plusSeconds(1)),
                LocalDateBounds.class,
                List.of(today.minusDays(1), today, today.plusDays(1)),
                LocalDateTimeBounds.class,
                List.of(
                        localNow.minusSeconds(1),
                        localNow.truncatedTo(ChronoUnit.MILLIS),
                        localNow,
                        localNow.plusSeconds(1)),
                OffsetDateTimeBounds.class,
                List.of(
                        offsetNow.minusSeconds(1),
                        offsetNow.truncatedTo(ChronoUnit.MILLIS),
                        offsetNow,
                        offsetNow.withOffsetSameInstant(ZoneOffset.UTC),
                        offsetNow.plusSeconds(1)),
                ZonedDateTimeBounds.class,
                List.of(
                        zonedNow.minusSeconds(1),
                        zonedNow.truncatedTo(ChronoUnit.MILLIS),
                        zonedNow,
                        zonedNow.withZoneSameInstant(ZoneOffset.UTC),
                        zonedNow.plusSeconds(1)),
                CalendarBounds.class,
                List.of(calendarAt(now.minusSeconds(1)), calendarAt(millisecond), calendarAt(now.plusSeconds(1))),
                DateBounds.class,
                List.of(
                        java.util.Date.from(now.minusSeconds(1)),
                        java.util.Date.from(millisecond),
                        java.util.Date.from(now.plusSeconds(1))),
                SqlDateBounds.class,
                List.of(
                        new java.sql.Date(now.minusSeconds(1).toEpochMilli()),
                        new java.sql.Date(millisecond.toEpochMilli()),
                        new java.sql.Date(now.plusSeconds(1).toEpochMilli())),
                TimestampBounds.class,
                List.of(Timestamp.from(now.minusSeconds(1)), Timestamp.from(now), Timestamp.from(now.plusSeconds(1))));

        for (Map.Entry<Class<?>, List<Object>> type : values.entrySet()) {
            for (Field field : type.getKey().getDeclaredFields()) {
                for (Object value : type.getValue()) {
                    boolean valid = hibernate
                            .validateValue(type.getKey(), field.getName(), value)
                            .isEmpty();
                    assertEquals(
                            valid,
                            BeanValidationSupport.accepts(field, field.getType(), value, clock),
                            type.getKey().getSimpleName() + "." + field.getName() + " " + value);
                }
            }
        }
    }

    @Test
    @DisplayName("an inclusive bound takes the present at the clock's edge, where a strict one has no value")
    void inclusiveBoundsTakeThePresentAtTheClocksEdge() throws Exception {
        // The last nanosecond of a year and its first, where a MonthDay and a time of day reach their ends.
        Clock yearEnd = Clock.fixed(Instant.parse("2026-12-31T23:59:59.999999999Z"), ZoneOffset.UTC);
        Clock yearStart = Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC);
        List<Class<?>> edgeTypes = List.of(MonthDayBounds.class, LocalTimeBounds.class, OffsetTimeBounds.class);

        assertGeneratedValuesValid(
                yearEnd,
                edgeTypes,
                Map.of(
                        MonthDayBounds.class,
                        "future",
                        LocalTimeBounds.class,
                        "future",
                        OffsetTimeBounds.class,
                        "future"));
        assertGeneratedValuesValid(
                yearStart,
                edgeTypes,
                Map.of(MonthDayBounds.class, "past", LocalTimeBounds.class, "past", OffsetTimeBounds.class, "past"));
        assertEquals(MonthDay.of(12, 31), generated(MonthDayBounds.class, "futureOrPresent", yearEnd));
        assertEquals(MonthDay.of(1, 1), generated(MonthDayBounds.class, "pastOrPresent", yearStart));
        assertEquals(LocalTime.MAX, generated(LocalTimeBounds.class, "futureOrPresent", yearEnd));
        assertEquals(LocalTime.MIN, generated(LocalTimeBounds.class, "pastOrPresent", yearStart));
    }

    /**
     * Generates every member of the bounds classes under the clock, and checks that Hibernate Validator, with the
     * same clock, accepts each value, except for the members named in {@code noValue}, whose generation must fail
     * as a constraint conflict, because no value of the type meets their bounds at that clock.
     */
    private static void assertGeneratedValuesValid(Clock clock, List<Class<?>> types, Map<Class<?>, String> noValue)
            throws Exception {
        Validator hibernate = Validation.byProvider(HibernateValidator.class)
                .configure()
                .clockProvider(() -> clock)
                .messageInterpolator(new ParameterMessageInterpolator())
                .buildValidatorFactory()
                .getValidator();
        for (Class<?> type : types) {
            for (Field field : type.getDeclaredFields()) {
                String member = type.getSimpleName() + "." + field.getName() + " at " + clock.instant();
                if (field.getName().equals(noValue.get(type))) {
                    assertThrows(
                            BeanValidationSupport.ConstraintConflictException.class,
                            () -> generated(type, field.getName(), clock),
                            member);
                    continue;
                }
                Object value = generated(type, field.getName(), clock);
                assertTrue(hibernate.validateValue(type, field.getName(), value).isEmpty(), member + " gave " + value);
            }
        }
    }

    private static Object generated(Class<?> type, String fieldName, Clock clock) throws NoSuchFieldException {
        Field field = type.getDeclaredField(fieldName);
        return BeanValidationSupport.constraintGeneratorFor(field, field.getType(), new Random(1L), clock)
                .generate();
    }

    @Test
    @DisplayName("accepts gives Hibernate Validator's verdict for a float or a double against each kind of bound")
    void acceptsGivesHibernateValidatorsVerdictForAFloatOrADouble() throws Exception {
        Validator hibernate = Validation.byProvider(HibernateValidator.class)
                .configure()
                .messageInterpolator(new ParameterMessageInterpolator())
                .buildValidatorFactory()
                .getValidator();
        Clock clock = Clock.systemUTC();
        // 0.1f is 0.10000000149011612 as a double, which @DecimalMax("0.1") rejects although it prints as 0.1.
        // @Min and @Max convert their bound to the value's type, so 16777219 is 16777220f and 2^53 + 1 is 2^53.
        // An infinity lies beyond every bound, and -0.0 is negative.
        Map<Class<?>, List<Object>> values = Map.of(
                FloatBounds.class,
                List.of(
                        0.1f,
                        0.09f,
                        0.11f,
                        0.0f,
                        -0.0f,
                        -0.5f,
                        1.0f,
                        0.99999994f,
                        1.0000001f,
                        2.5f,
                        Float.POSITIVE_INFINITY,
                        Float.NEGATIVE_INFINITY),
                DoubleBounds.class,
                List.of(
                        0.1,
                        0.09,
                        0.11,
                        0.0,
                        -0.0,
                        -0.5,
                        1.0,
                        0.9999999999999999,
                        1.0000000000000002,
                        2.5,
                        Double.POSITIVE_INFINITY,
                        Double.NEGATIVE_INFINITY),
                LargeFloatBounds.class,
                List.of(16_777_216f, 16_777_218f, 16_777_220f, 16_777_222f),
                LargeDoubleBounds.class,
                List.of(9_007_199_254_740_992.0, 9_007_199_254_740_994.0));

        for (Map.Entry<Class<?>, List<Object>> type : values.entrySet()) {
            for (Field field : type.getKey().getDeclaredFields()) {
                for (Object value : type.getValue()) {
                    boolean valid = hibernate
                            .validateValue(type.getKey(), field.getName(), value)
                            .isEmpty();
                    assertEquals(
                            valid,
                            BeanValidationSupport.accepts(field, field.getType(), value, clock),
                            type.getKey().getSimpleName() + "." + field.getName() + " " + value);
                }
            }
        }
    }

    @Test
    @DisplayName("accepts keeps a number within its bounds and a string to its text constraints, and @Null to none")
    void acceptsKeepsANumberWithinItsBoundsAndAStringToItsTextConstraints() throws Exception {
        Clock clock = Clock.systemUTC();
        assertTrue(accepts("exactLong", long.class, 5L, clock));
        assertFalse(accepts("exactLong", long.class, 4L, clock));
        assertFalse(accepts("exactLong", long.class, 6L, clock));
        assertTrue(accepts("boxedByte", Byte.class, (byte) 10, clock));
        assertFalse(accepts("boxedByte", Byte.class, (byte) 11, clock));
        assertTrue(accepts("boxedShort", Short.class, (short) 3, clock));
        assertFalse(accepts("boxedShort", Short.class, (short) 2, clock));
        assertTrue(accepts("exactDouble", Double.class, 2.0, clock));
        assertFalse(accepts("exactDouble", Double.class, 2.5, clock));
        assertTrue(accepts("positiveFloat", Float.class, 0.5f, clock));
        assertFalse(accepts("positiveFloat", Float.class, 0.0f, clock));
        assertFalse(accepts("exclusiveMin", BigDecimal.class, new BigDecimal("1.00"), clock));
        assertTrue(accepts("exclusiveMin", BigDecimal.class, new BigDecimal("1.01"), clock));

        // A string meets its text constraints, numeric ones among them.
        assertTrue(accepts("numericText", String.class, "7", clock));
        assertFalse(accepts("numericText", String.class, "8", clock));
        assertFalse(accepts("numericText", String.class, "seven", clock));
        assertTrue(accepts("sizedText", String.class, "abcd", clock));
        assertFalse(accepts("sizedText", String.class, "abcde", clock));

        // @Null takes no value, and a field with no constraint takes any.
        assertFalse(accepts("nullOnly", String.class, "x", clock));
        assertTrue(accepts("unconstrained", Object.class, "x", clock));
    }

    @Test
    @DisplayName("finds record component and interface accessor annotations")
    void recordAndInterfaceAccessorAnnotations() throws Exception {
        RecordComponent component = ComponentSample.class.getRecordComponents()[0];
        Generator<?> recordGenerator =
                BeanValidationSupport.constraintGeneratorFor(component, String.class, new Random(1L));
        assertNotNull(recordGenerator);
        assertEquals(2, ((String) recordGenerator.generate()).length());

        Generator<?> interfaceGetterGenerator = generatorFor("interfaceSizedText", String.class);
        assertNotNull(interfaceGetterGenerator);
        assertEquals(4, ((String) interfaceGetterGenerator.generate()).length());

        assertNull(generatorFor("parameterizedAccessorText", String.class));

        assertNull(BeanValidationSupport.constraintGeneratorFor(
                Sample.class.getDeclaredMethod("plainMethod"), String.class, new Random(1L)));
    }

    private static Field field(String name) throws NoSuchFieldException {
        return Sample.class.getDeclaredField(name);
    }

    private static Generator<?> generatorFor(String fieldName, Class<?> rawType) throws NoSuchFieldException {
        return BeanValidationSupport.constraintGeneratorFor(field(fieldName), rawType, new Random(1L));
    }

    private static boolean accepts(String fieldName, Class<?> rawType, Object value, Clock clock)
            throws NoSuchFieldException {
        return BeanValidationSupport.accepts(field(fieldName), rawType, value, clock);
    }

    private static GregorianCalendar calendarAt(Instant instant) {
        return GregorianCalendar.from(instant.atZone(ZoneId.of("Asia/Tokyo")));
    }

    interface InterfaceSizedText {

        @Size(min = 4, max = 4) String getInterfaceSizedText();

        @Size(min = 8, max = 8) String getParameterizedAccessorText(String prefix);
    }

    record ComponentSample(@Size(min = 2, max = 2) String code) {}

    static final class FloatBounds {

        @jakarta.validation.constraints.DecimalMax("0.1") Float decimalMax;

        @DecimalMin("0.1") Float decimalMin;

        @jakarta.validation.constraints.DecimalMax(value = "0.1", inclusive = false) Float exclusiveMax;

        @DecimalMin(value = "0.1", inclusive = false) Float exclusiveMin;

        @Max(1) Float max;

        @Min(1) Float min;

        @Positive Float positive;

        @jakarta.validation.constraints.PositiveOrZero Float positiveOrZero;

        @jakarta.validation.constraints.Negative Float negative;

        @jakarta.validation.constraints.NegativeOrZero Float negativeOrZero;
    }

    static final class DoubleBounds {

        @jakarta.validation.constraints.DecimalMax("0.1") Double decimalMax;

        @DecimalMin("0.1") Double decimalMin;

        @jakarta.validation.constraints.DecimalMax(value = "0.1", inclusive = false) Double exclusiveMax;

        @DecimalMin(value = "0.1", inclusive = false) Double exclusiveMin;

        @Max(1) Double max;

        @Min(1) Double min;

        @Positive Double positive;

        @jakarta.validation.constraints.PositiveOrZero Double positiveOrZero;

        @jakarta.validation.constraints.Negative Double negative;

        @jakarta.validation.constraints.NegativeOrZero Double negativeOrZero;
    }

    static final class LargeFloatBounds {

        @Max(16_777_219) Float max;

        @Min(16_777_219) Float min;
    }

    static final class LargeDoubleBounds {

        @Max(9_007_199_254_740_993L) Double max;

        @Min(9_007_199_254_740_993L) Double min;
    }

    static final class InstantBounds {

        @Past Instant past;

        @PastOrPresent Instant pastOrPresent;

        @Future Instant future;

        @FutureOrPresent Instant futureOrPresent;

        @PastOrPresent @FutureOrPresent Instant present;
    }

    static final class LocalDateBounds {

        @Past LocalDate past;

        @PastOrPresent LocalDate pastOrPresent;

        @Future LocalDate future;

        @FutureOrPresent LocalDate futureOrPresent;

        @PastOrPresent @FutureOrPresent LocalDate present;
    }

    static final class LocalDateTimeBounds {

        @Past LocalDateTime past;

        @PastOrPresent LocalDateTime pastOrPresent;

        @Future LocalDateTime future;

        @FutureOrPresent LocalDateTime futureOrPresent;

        @PastOrPresent @FutureOrPresent LocalDateTime present;
    }

    static final class OffsetDateTimeBounds {

        @Past OffsetDateTime past;

        @PastOrPresent OffsetDateTime pastOrPresent;

        @Future OffsetDateTime future;

        @FutureOrPresent OffsetDateTime futureOrPresent;

        @PastOrPresent @FutureOrPresent OffsetDateTime present;
    }

    static final class ZonedDateTimeBounds {

        @Past ZonedDateTime past;

        @PastOrPresent ZonedDateTime pastOrPresent;

        @Future ZonedDateTime future;

        @FutureOrPresent ZonedDateTime futureOrPresent;

        @PastOrPresent @FutureOrPresent ZonedDateTime present;
    }

    static final class CalendarBounds {

        @Past Calendar past;

        @PastOrPresent Calendar pastOrPresent;

        @Future Calendar future;

        @FutureOrPresent Calendar futureOrPresent;

        @PastOrPresent @FutureOrPresent Calendar present;
    }

    static final class DateBounds {

        @Past java.util.Date past;

        @PastOrPresent java.util.Date pastOrPresent;

        @Future java.util.Date future;

        @FutureOrPresent java.util.Date futureOrPresent;

        @PastOrPresent @FutureOrPresent java.util.Date present;
    }

    static final class SqlDateBounds {

        @Past java.sql.Date past;

        @PastOrPresent java.sql.Date pastOrPresent;

        @Future java.sql.Date future;

        @FutureOrPresent java.sql.Date futureOrPresent;

        @PastOrPresent @FutureOrPresent java.sql.Date present;
    }

    static final class TimestampBounds {

        @Past Timestamp past;

        @PastOrPresent Timestamp pastOrPresent;

        @Future Timestamp future;

        @FutureOrPresent Timestamp futureOrPresent;

        @PastOrPresent @FutureOrPresent Timestamp present;
    }

    static final class MonthDayBounds {

        @Past MonthDay past;

        @PastOrPresent MonthDay pastOrPresent;

        @Future MonthDay future;

        @FutureOrPresent MonthDay futureOrPresent;

        @PastOrPresent @FutureOrPresent MonthDay present;
    }

    static final class LocalTimeBounds {

        @Past LocalTime past;

        @PastOrPresent LocalTime pastOrPresent;

        @Future LocalTime future;

        @FutureOrPresent LocalTime futureOrPresent;

        @PastOrPresent @FutureOrPresent LocalTime present;
    }

    static final class OffsetTimeBounds {

        @Past OffsetTime past;

        @PastOrPresent OffsetTime pastOrPresent;

        @Future OffsetTime future;

        @FutureOrPresent OffsetTime futureOrPresent;

        @PastOrPresent @FutureOrPresent OffsetTime present;
    }

    static final class Sample implements InterfaceSizedText {

        @Size(min = 2, max = 4) String sizedText;

        @DecimalMin("1.5") BigDecimal decimalMinOnly;

        String getterSizedText;

        @Null String nullOnly;

        @Size(min = 0, max = 0) String emptyText;

        @Size(min = 1) String unboundedText;

        @Min(5) @Max(7) String numericText;

        @DecimalMin("1.25") @jakarta.validation.constraints.DecimalMax("1.25") String decimalText;

        @jakarta.validation.constraints.DecimalMax("9.50") String decimalMaxText;

        @DecimalMin(value = "1.00", inclusive = false) BigDecimal exclusiveMin;

        @jakarta.validation.constraints.DecimalMax(value = "5.00", inclusive = false) BigDecimal exclusiveMax;

        @DecimalMin("1.00") @Max(2) BigDecimal decimalMinWithMax;

        @Min(1) @jakarta.validation.constraints.DecimalMax("2.00") BigDecimal decimalMaxWithMin;

        @DecimalMin("4.00") @jakarta.validation.constraints.DecimalMax("3.00") BigDecimal invertedDecimal;

        @Min(5) @Max(5) long exactLong;

        @Null int nullPrimitive;

        @Min(5) @Max(5) BigInteger exactBigInteger;

        @Min(4) @Max(3) BigInteger invertedBigInteger;

        @Max(10) Byte boxedByte;

        @Min(3) Short boxedShort;

        @Min(5) Long boxedLong;

        @Min(5) Number boundedNumber;

        @Positive BigInteger positiveBigInteger;

        @Positive float positivePrimitiveFloat;

        @Positive Float positiveFloat;

        @Min(2) @Max(2) Double exactDouble;

        @jakarta.validation.constraints.Negative Double negativeDouble;

        @Min(1) Object numericUnsupported;

        Object unconstrained;

        @Future LocalDateTime futureLocalDateTime;

        @Past LocalDateTime pastLocalDateTime;

        @Future ZonedDateTime futureZonedDateTime;

        @Future OffsetDateTime futureOffsetDateTime;

        @Future java.sql.Date futureSqlDate;

        @Future Timestamp futureTimestamp;

        @Future LocalTime futureLocalTime;

        @Past LocalTime pastLocalTime;

        @Future OffsetTime futureOffsetTime;

        @Future Year futureYear;

        @Past Year pastYear;

        @Future YearMonth futureYearMonth;

        @Past YearMonth pastYearMonth;

        @Future MonthDay futureMonthDay;

        @Future Object futureUnsupported;

        @Past Instant pastBound;

        @PastOrPresent Instant pastOrPresentBound;

        @Future Instant futureBound;

        @FutureOrPresent Instant futureOrPresentBound;

        @PastOrPresent @FutureOrPresent Instant presentBound;

        String interfaceSizedText;

        String parameterizedAccessorText;

        @Size(min = 3, max = 5) String getGetterSizedText() {
            return getterSizedText;
        }

        @Override
        public String getInterfaceSizedText() {
            return interfaceSizedText;
        }

        @Override
        public String getParameterizedAccessorText(String prefix) {
            return prefix + parameterizedAccessorText;
        }

        String plainMethod() {
            return "";
        }
    }
}
