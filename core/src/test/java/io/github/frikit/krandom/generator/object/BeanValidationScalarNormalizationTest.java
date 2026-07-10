/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.object;

import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.failure.GenerationFailureCategory;
import io.github.frikit.krandom.generator.failure.GenerationFailureDiagnostic;
import io.github.frikit.krandom.generator.object.exception.ObjectGenerationException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.AssertFalse;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Negative;
import jakarta.validation.constraints.NegativeOrZero;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Bean Validation scalar normalization")
class BeanValidationScalarNormalizationTest {

    private static final Clock CLOCK =
        Clock.fixed(Instant.parse("2026-07-09T12:00:00Z"), ZoneOffset.UTC);
    private static final Validator VALIDATOR =
        Validation.byProvider(HibernateValidator.class)
                  .configure()
                  .clockProvider(() -> CLOCK)
                  .messageInterpolator(new ParameterMessageInterpolator())
                  .buildValidatorFactory()
                  .getValidator();

    @Test
    @DisplayName("compatible numeric and temporal intersections validate over deterministic seeds")
    void compatibleScalarIntersectionsValidate() {
        for (long seed = 0; seed < 32; seed++) {
            GeneratorConfig config = GeneratorConfig.builder().seed(seed).clock(CLOCK).build();

            CompatibleScalarFixture value =
                new ObjectGenerator<>(CompatibleScalarFixture.class, config).generate();

            assertTrue(value.narrowDecimal.compareTo(new BigDecimal("1.100")) > 0);
            assertTrue(value.narrowDecimal.compareTo(new BigDecimal("1.105")) < 0);
            assertTrue(value.exclusiveInteger > 4 && value.exclusiveInteger <= 6);
            assertNotNull(value.future);
            assertTrue(VALIDATOR.validate(value).isEmpty(), () -> VALIDATOR.validate(value).toString());
        }
    }

    @Test
    @DisplayName("inverted numeric intervals fail before generating a value")
    void invertedNumericIntervalFailsContextually() {
        assertConflict(InvertedNumericFixture.class, "InvertedNumericFixture.value", int.class.getTypeName());
    }

    @Test
    @DisplayName("integral target domains participate in interval normalization")
    void emptyIntegralTargetDomainsFailContextually() {
        assertConflict(ByteOverflowFixture.class, "ByteOverflowFixture.value", Byte.class.getTypeName());
        assertConflict(FractionalIntegerFixture.class, "FractionalIntegerFixture.value", Integer.class.getTypeName());
    }

    @Test
    @DisplayName("contradictory sign constraints fail before generation")
    void contradictorySignConstraintsFailContextually() {
        assertConflict(SignConflictFixture.class, "SignConflictFixture.value", BigDecimal.class.getTypeName());
    }

    @Test
    @DisplayName("contradictory boolean assertions fail before generation")
    void contradictoryBooleanAssertionsFailContextually() {
        assertConflict(AssertionConflictFixture.class, "AssertionConflictFixture.value", Boolean.class.getTypeName());
    }

    @Test
    @DisplayName("boolean assertions reject non-boolean targets")
    void assertionOnUnsupportedTargetFailsContextually() {
        assertConflict(UnsupportedAssertionFixture.class,
                       "UnsupportedAssertionFixture.value",
                       String.class.getTypeName());
    }

    @Test
    @DisplayName("future and past-or-present constraints have no intersection")
    void contradictoryTemporalConstraintsFailContextually() {
        assertConflict(TemporalConflictFixture.class,
                       "TemporalConflictFixture.value",
                       Instant.class.getTypeName());
    }

    @Test
    @DisplayName("future-or-present and past-or-present intersect at the configured present")
    void presentTemporalIntersectionUsesConfiguredClock() {
        GeneratorConfig config = GeneratorConfig.builder().clock(CLOCK).build();

        PresentFixture value = new ObjectGenerator<>(PresentFixture.class, config).generate();

        assertEquals(CLOCK.instant(), value.value);
        assertTrue(VALIDATOR.validate(value).isEmpty(), () -> VALIDATOR.validate(value).toString());
    }

    @Test
    @DisplayName("the configured present is projected to every supported temporal target")
    void presentIntersectionSupportsEveryTemporalTarget() {
        GeneratorConfig config = GeneratorConfig.builder().clock(CLOCK).build();

        PresentTargetFixture value = new ObjectGenerator<>(PresentTargetFixture.class, config).generate();

        assertEquals(CLOCK.instant(), value.instant);
        assertEquals(LocalDate.now(CLOCK), value.localDate);
        assertEquals(LocalDateTime.now(CLOCK), value.localDateTime);
        assertEquals(ZonedDateTime.now(CLOCK), value.zonedDateTime);
        assertEquals(OffsetDateTime.now(CLOCK), value.offsetDateTime);
        assertEquals(Date.from(CLOCK.instant()), value.date);
        assertEquals(new java.sql.Date(CLOCK.millis()), value.sqlDate);
        assertEquals(Timestamp.from(CLOCK.instant()), value.timestamp);
        assertEquals(CLOCK.instant(), value.calendar.toInstant());
        assertEquals(LocalTime.now(CLOCK), value.localTime);
        assertEquals(OffsetTime.now(CLOCK), value.offsetTime);
        assertEquals(Year.now(CLOCK), value.year);
        assertEquals(YearMonth.now(CLOCK), value.yearMonth);
        assertEquals(MonthDay.now(CLOCK), value.monthDay);
        assertTrue(VALIDATOR.validate(value).isEmpty(), () -> VALIDATOR.validate(value).toString());
    }

    @Test
    @DisplayName("strict temporal constraints fail at target-domain clock boundaries")
    void strictTemporalClockBoundariesFailContextually() {
        assertConflict(
            FutureLocalTimeBoundaryFixture.class,
            configAt("2026-07-09T23:59:59.999999999Z"),
            "FutureLocalTimeBoundaryFixture.value",
            LocalTime.class.getTypeName());
        assertConflict(
            PastLocalTimeBoundaryFixture.class,
            configAt("2026-07-09T00:00:00Z"),
            "PastLocalTimeBoundaryFixture.value",
            LocalTime.class.getTypeName());
        assertConflict(
            FutureMonthDayBoundaryFixture.class,
            configAt("2026-12-31T12:00:00Z"),
            "FutureMonthDayBoundaryFixture.value",
            MonthDay.class.getTypeName());
        assertConflict(
            PastMonthDayBoundaryFixture.class,
            configAt("2026-01-01T12:00:00Z"),
            "PastMonthDayBoundaryFixture.value",
            MonthDay.class.getTypeName());
    }

    @Test
    @DisplayName("lenient temporal boundary failures return the type default and emit context")
    void lenientTemporalBoundaryReturnsDefaultAndEmitsContext() {
        AtomicReference<GenerationFailureDiagnostic> observed = new AtomicReference<>();
        GeneratorConfig config = GeneratorConfig.builder()
                                                .clock(Clock.fixed(
                                                    Instant.parse("2026-07-09T23:59:59.999999999Z"),
                                                    ZoneOffset.UTC))
                                                .objectIgnoreErrors(true)
                                                .generationFailureListener(observed::set)
                                                .build();

        FutureLocalTimeBoundaryFixture value =
            new ObjectGenerator<>(FutureLocalTimeBoundaryFixture.class, config).generate();

        assertNull(value.value);
        assertEquals(GenerationFailureCategory.UNSUPPORTED_TYPE, observed.get().context().category());
        assertEquals("FutureLocalTimeBoundaryFixture.value", observed.get().context().path());
    }

    @Test
    @DisplayName("bound ordering and one-sided domains retain the strongest compatible facts")
    void boundOrderingAndOneSidedDomainsAreNormalized() {
        BoundOrderingFixture value = new ObjectGenerator<>(BoundOrderingFixture.class).generate();

        assertTrue(value.strongerLower.compareTo(new BigDecimal("2")) >= 0);
        assertTrue(value.weakerLower.compareTo(new BigDecimal("2")) >= 0);
        assertTrue(value.exclusiveEqualLower.compareTo(new BigDecimal("2")) > 0);
        assertTrue(value.strongerUpper.compareTo(new BigDecimal("1")) <= 0);
        assertTrue(value.weakerUpper.compareTo(new BigDecimal("1")) <= 0);
        assertTrue(value.exclusiveEqualUpper.compareTo(new BigDecimal("2")) < 0);
        assertEquals(0, value.zero);
        assertTrue(value.upperOnlyBigInteger.compareTo(BigInteger.valueOf(5)) <= 0);
        assertTrue(value.fractionalExclusiveInteger >= 5 && value.fractionalExclusiveInteger <= 6);
    }

    @Test
    @DisplayName("floating-point bounds are projected without crossing their decimal contracts")
    void floatingPointBoundsRespectRepresentability() {
        FloatingPointFixture value = new ObjectGenerator<>(FloatingPointFixture.class).generate();

        assertTrue(BigDecimal.valueOf(value.floatExclusiveLower).compareTo(new BigDecimal("0.1")) > 0);
        assertTrue(BigDecimal.valueOf(value.doubleExclusiveLower).compareTo(new BigDecimal("0.1")) > 0);
        assertTrue(BigDecimal.valueOf(value.floatExclusiveUpper).compareTo(new BigDecimal("0.1")) < 0);
        assertTrue(BigDecimal.valueOf(value.doubleExclusiveUpper).compareTo(new BigDecimal("0.1")) < 0);
        assertTrue(BigDecimal.valueOf(value.roundedFloatLower).compareTo(new BigDecimal("0.100000001")) >= 0);
        assertTrue(BigDecimal.valueOf(value.roundedDoubleLower).compareTo(new BigDecimal("0.10000000000000001")) >= 0);
        assertTrue(BigDecimal.valueOf(value.roundedFloatUpper).compareTo(new BigDecimal("0.099999999")) <= 0);
        assertTrue(BigDecimal.valueOf(value.roundedDoubleUpper).compareTo(new BigDecimal("0.09999999999999999")) <= 0);
        assertTrue(BigDecimal.valueOf(value.inclusiveFloatLower).compareTo(new BigDecimal("0.1")) >= 0);
        assertTrue(BigDecimal.valueOf(value.inclusiveDoubleUpper).compareTo(new BigDecimal("0.1")) <= 0);
        assertTrue(BigDecimal.valueOf(value.exclusiveRoundedAboveLower).compareTo(new BigDecimal("0.099999999")) > 0);
        assertTrue(BigDecimal.valueOf(value.exclusiveRoundedBelowUpper).compareTo(new BigDecimal("0.100000001")) < 0);
    }

    @Test
    @DisplayName("unrepresentable floating-point and malformed decimal ranges fail contextually")
    void unrepresentableFloatingPointRangesFailContextually() {
        assertConflict(FloatLowerOverflowFixture.class,
                       "FloatLowerOverflowFixture.value",
                       Float.class.getTypeName());
        assertConflict(FloatUpperOverflowFixture.class,
                       "FloatUpperOverflowFixture.value",
                       Float.class.getTypeName());
        assertConflict(FloatingGapFixture.class,
                       "FloatingGapFixture.value",
                       Double.class.getTypeName());
        assertConflict(InvalidDecimalFixture.class,
                       "InvalidDecimalFixture.value",
                       BigDecimal.class.getTypeName());
        assertConflict(UpperExclusivePointFixture.class,
                       "UpperExclusivePointFixture.value",
                       BigDecimal.class.getTypeName());
    }

    private static GeneratorConfig configAt(String instant) {
        return GeneratorConfig.builder()
                              .clock(Clock.fixed(Instant.parse(instant), ZoneOffset.UTC))
                              .build();
    }

    private static void assertConflict(Class<?> fixtureType, String path, String declaredType) {
        assertConflict(fixtureType, GeneratorConfig.defaults(), path, declaredType);
    }

    private static void assertConflict(Class<?> fixtureType,
                                       GeneratorConfig config,
                                       String path,
                                       String declaredType) {
        ObjectGenerationException ex = assertThrows(
            ObjectGenerationException.class,
            () -> new ObjectGenerator<>(fixtureType, config).generate());

        var context = ex.getContext().orElseThrow();
        assertEquals(GenerationFailureCategory.UNSUPPORTED_TYPE, context.category());
        assertEquals(path, context.path());
        assertEquals(declaredType, context.declaredType());
        assertNotNull(ex.getCause());
    }

    static final class CompatibleScalarFixture {

        @DecimalMin(value = "1.100", inclusive = false)
        @DecimalMax(value = "1.105", inclusive = false)
        BigDecimal narrowDecimal;

        @DecimalMin(value = "4", inclusive = false)
        @DecimalMax("6")
        int exclusiveInteger;

        @AssertTrue
        Boolean enabled;

        @Future
        Instant future;

        @PastOrPresent
        LocalDate pastOrPresent;
    }

    static final class InvertedNumericFixture {

        @Min(10)
        @Max(5)
        int value;
    }

    static final class ByteOverflowFixture {

        @Min(128)
        Byte value;
    }

    static final class FractionalIntegerFixture {

        @DecimalMin("5.1")
        @DecimalMax("5.9")
        Integer value;
    }

    static final class SignConflictFixture {

        @Positive
        @Negative
        BigDecimal value;
    }

    static final class AssertionConflictFixture {

        @AssertTrue
        @AssertFalse
        Boolean value;
    }

    static final class UnsupportedAssertionFixture {

        @AssertTrue
        String value;
    }

    static final class TemporalConflictFixture {

        @Future
        @PastOrPresent
        Instant value;
    }

    static final class PresentFixture {

        @FutureOrPresent
        @PastOrPresent
        Instant value;
    }

    static final class PresentTargetFixture {

        @FutureOrPresent
        @PastOrPresent
        Instant instant;

        @FutureOrPresent
        @PastOrPresent
        LocalDate localDate;

        @FutureOrPresent
        @PastOrPresent
        LocalDateTime localDateTime;

        @FutureOrPresent
        @PastOrPresent
        ZonedDateTime zonedDateTime;

        @FutureOrPresent
        @PastOrPresent
        OffsetDateTime offsetDateTime;

        @FutureOrPresent
        @PastOrPresent
        Date date;

        @FutureOrPresent
        @PastOrPresent
        java.sql.Date sqlDate;

        @FutureOrPresent
        @PastOrPresent
        Timestamp timestamp;

        @FutureOrPresent
        @PastOrPresent
        Calendar calendar;

        @FutureOrPresent
        @PastOrPresent
        LocalTime localTime;

        @FutureOrPresent
        @PastOrPresent
        OffsetTime offsetTime;

        @FutureOrPresent
        @PastOrPresent
        Year year;

        @FutureOrPresent
        @PastOrPresent
        YearMonth yearMonth;

        @FutureOrPresent
        @PastOrPresent
        MonthDay monthDay;
    }

    static final class FutureLocalTimeBoundaryFixture {

        @Future
        LocalTime value;
    }

    static final class PastLocalTimeBoundaryFixture {

        @jakarta.validation.constraints.Past
        LocalTime value;
    }

    static final class FutureMonthDayBoundaryFixture {

        @Future
        MonthDay value;
    }

    static final class PastMonthDayBoundaryFixture {

        @jakarta.validation.constraints.Past
        MonthDay value;
    }

    static final class BoundOrderingFixture {

        @Min(1)
        @DecimalMin("2")
        BigDecimal strongerLower;

        @Min(2)
        @DecimalMin("1")
        BigDecimal weakerLower;

        @Min(2)
        @DecimalMin(value = "2", inclusive = false)
        BigDecimal exclusiveEqualLower;

        @Max(2)
        @DecimalMax("1")
        BigDecimal strongerUpper;

        @Max(1)
        @DecimalMax("2")
        BigDecimal weakerUpper;

        @Max(2)
        @DecimalMax(value = "2", inclusive = false)
        BigDecimal exclusiveEqualUpper;

        @PositiveOrZero
        @NegativeOrZero
        int zero;

        @Max(5)
        BigInteger upperOnlyBigInteger;

        @DecimalMin(value = "4.1", inclusive = false)
        @DecimalMax(value = "6.9", inclusive = false)
        int fractionalExclusiveInteger;
    }

    static final class FloatingPointFixture {

        @DecimalMin("-1E1000")
        Float lowerOutsideFloatDomain;

        @DecimalMax("1E1000")
        Double upperOutsideDoubleDomain;

        @DecimalMin(value = "0.1", inclusive = false)
        Float floatExclusiveLower;

        @DecimalMin(value = "0.1", inclusive = false)
        Double doubleExclusiveLower;

        @DecimalMax(value = "0.1", inclusive = false)
        Float floatExclusiveUpper;

        @DecimalMax(value = "0.1", inclusive = false)
        Double doubleExclusiveUpper;

        @DecimalMin("0.100000001")
        Float roundedFloatLower;

        @DecimalMin("0.10000000000000001")
        Double roundedDoubleLower;

        @DecimalMax("0.099999999")
        Float roundedFloatUpper;

        @DecimalMax("0.09999999999999999")
        Double roundedDoubleUpper;

        @DecimalMin("0.1")
        Float inclusiveFloatLower;

        @DecimalMax("0.1")
        Double inclusiveDoubleUpper;

        @DecimalMin(value = "0.099999999", inclusive = false)
        Float exclusiveRoundedAboveLower;

        @DecimalMax(value = "0.100000001", inclusive = false)
        Float exclusiveRoundedBelowUpper;
    }

    static final class FloatLowerOverflowFixture {

        @DecimalMin("1E1000")
        Float value;
    }

    static final class FloatUpperOverflowFixture {

        @DecimalMax("-1E1000")
        Float value;
    }

    static final class FloatingGapFixture {

        @DecimalMin(value = "0", inclusive = false)
        @DecimalMax(value = "4.9E-324", inclusive = false)
        Double value;
    }

    static final class InvalidDecimalFixture {

        @DecimalMin("not-a-number")
        BigDecimal value;
    }

    static final class UpperExclusivePointFixture {

        @DecimalMin("0")
        @DecimalMax(value = "0", inclusive = false)
        BigDecimal value;
    }
}
