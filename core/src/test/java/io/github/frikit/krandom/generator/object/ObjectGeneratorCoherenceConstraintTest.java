/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.object;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.frikit.krandom.generator.GeneratorConfig;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/**
 * The coherence pass never moves a {@code createdAt}, {@code updatedAt}, {@code birthDate} or {@code age} outside
 * its own Bean Validation constraints, in either semantic mode, as Hibernate Validator reads them against the
 * configured clock.
 */
@DisplayName("ObjectGenerator — date coherence within Bean Validation constraints")
class ObjectGeneratorCoherenceConstraintTest {

    private static final int SAMPLES = 100;
    private static final Instant NOW = Instant.parse("2026-06-04T12:00:00Z");
    private static final Clock CLOCK = Clock.fixed(NOW, ZoneOffset.UTC);
    private static final LocalDate TODAY = LocalDate.now(CLOCK);
    private static final Instant A_DAY_AGO = NOW.minus(Duration.ofDays(1));

    private static final Validator VALIDATOR = Validation.byProvider(HibernateValidator.class)
            .configure()
            .clockProvider(() -> CLOCK)
            .messageInterpolator(new ParameterMessageInterpolator())
            .buildValidatorFactory()
            .getValidator();

    record PastOrPresentOffset(
            ZonedDateTime createdAt, @PastOrPresent OffsetDateTime updatedAt) {}

    record PastCalendar(LocalDateTime createdAt, @Past GregorianCalendar updatedAt) {}

    record PastOrPresentDate(
            OffsetDateTime createdAt, @PastOrPresent java.util.Date updatedAt) {}

    record PastTimestamp(Instant createdAt, @Past java.sql.Timestamp updatedAt) {}

    record PastLocalDate(Instant createdAt, @Past LocalDate updatedAt) {}

    record FutureInstant(@Future Instant createdAt, Instant updatedAt) {}

    record FutureOrPresentZoned(@FutureOrPresent ZonedDateTime createdAt, OffsetDateTime updatedAt) {}

    record FutureCalendar(@Future Calendar createdAt, Instant updatedAt) {}

    record FutureOrPresentLocalDate(@FutureOrPresent LocalDate createdAt, LocalDate updatedAt) {}

    record Apart(@Future OffsetDateTime createdAt, @Past OffsetDateTime updatedAt) {}

    record PresentUpdatedAt(
            Instant createdAt,
            @PastOrPresent @FutureOrPresent Instant updatedAt) {}

    record RangedAge(LocalDate birthDate, @Min(30) @Max(40) int age) {}

    record PositiveAge(
            OffsetDateTime birthDate, @Positive @Max(40) Integer age) {}

    record PatternedAge(
            ZonedDateTime birthDate,
            @Pattern(regexp = "3[0-9]") String age) {}

    record FutureBirthDate(@Future LocalDate birthDate, int age) {}

    record PastBirthDate(@Past LocalDate birthDate, int age) {}

    record NullBirthDate(@Null LocalDate birthDate, int age) {}

    record NullAge(LocalDate birthDate, @Null Integer age) {}

    @ParameterizedTest(name = "{0}")
    @EnumSource(
            value = ObjectGenerationSemanticMode.class,
            names = {"RELAXED", "STRICT"})
    @DisplayName("an updatedAt that must not pass the clock keeps its value, and createdAt follows it")
    void updatedAtThatMustNotPassTheClockKeepsItsValue(ObjectGenerationSemanticMode mode) {
        // The range is the clock's date, so about half the semantic createdAt draws are past the clock.
        GeneratorConfig config = GeneratorConfig.builder()
                .seed(42L)
                .clock(CLOCK)
                .objectDateRange(TODAY, TODAY)
                .objectSemanticMode(mode)
                .build();

        assertValidAndInOrder(
                PastOrPresentOffset.class, config, PastOrPresentOffset::createdAt, PastOrPresentOffset::updatedAt);
        assertValidAndInOrder(PastCalendar.class, config, PastCalendar::createdAt, PastCalendar::updatedAt);
        assertValidAndInOrder(
                PastOrPresentDate.class, config, PastOrPresentDate::createdAt, PastOrPresentDate::updatedAt);
        assertValidAndInOrder(PastTimestamp.class, config, PastTimestamp::createdAt, PastTimestamp::updatedAt);
        assertValidAndInOrder(PastLocalDate.class, config, PastLocalDate::createdAt, PastLocalDate::updatedAt);
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(
            value = ObjectGenerationSemanticMode.class,
            names = {"RELAXED", "STRICT"})
    @DisplayName("a createdAt that must stay in the future keeps its value beside an updatedAt that cannot move")
    void createdAtThatMustStayInTheFutureKeepsItsValue(ObjectGenerationSemanticMode mode) {
        assertValidAndOutOfOrder(FutureInstant.class, mode, A_DAY_AGO, FutureInstant::createdAt);
        assertValidAndOutOfOrder(
                FutureOrPresentZoned.class,
                mode,
                A_DAY_AGO.atOffset(ZoneOffset.UTC),
                value -> value.createdAt().toInstant());
        assertValidAndOutOfOrder(
                FutureCalendar.class,
                mode,
                A_DAY_AGO,
                value -> value.createdAt().toInstant());
        assertValidAndOutOfOrder(
                FutureOrPresentLocalDate.class,
                mode,
                TODAY.minusDays(1),
                value -> value.createdAt().atStartOfDay().toInstant(ZoneOffset.UTC));
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(
            value = ObjectGenerationSemanticMode.class,
            names = {"RELAXED", "STRICT"})
    @DisplayName("a future createdAt and a past updatedAt both keep their values, out of order")
    void futureCreatedAtAndPastUpdatedAtBothKeepTheirValues(ObjectGenerationSemanticMode mode) {
        ObjectGenerator<Apart> generator = new ObjectGenerator<>(Apart.class, config(mode));
        for (int i = 0; i < SAMPLES; i++) {
            Apart value = generator.generate();
            assertValid(value);
            assertTrue(value.createdAt().isAfter(value.updatedAt()), value.toString());
        }
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(
            value = ObjectGenerationSemanticMode.class,
            names = {"RELAXED", "STRICT"})
    @DisplayName("an updatedAt held at the clock's instant keeps it, and createdAt follows it")
    void updatedAtHeldAtTheClocksInstantKeepsIt(ObjectGenerationSemanticMode mode) {
        GeneratorConfig config = GeneratorConfig.builder()
                .seed(42L)
                .clock(CLOCK)
                .objectDateRange(TODAY, TODAY)
                .objectSemanticMode(mode)
                .build();
        ObjectGenerator<PresentUpdatedAt> generator = new ObjectGenerator<>(PresentUpdatedAt.class, config);
        for (int i = 0; i < SAMPLES; i++) {
            PresentUpdatedAt value = generator.generate();
            assertValid(value);
            assertEquals(NOW, value.updatedAt());
            assertFalse(value.createdAt().isAfter(NOW), value.toString());
        }
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(
            value = ObjectGenerationSemanticMode.class,
            names = {"RELAXED", "STRICT"})
    @DisplayName("an age that must stay in its bounds keeps its value, and birthDate follows it")
    void ageThatMustStayInItsBoundsKeepsItsValue(ObjectGenerationSemanticMode mode) {
        ObjectGenerator<RangedAge> ranged = new ObjectGenerator<>(RangedAge.class, config(mode));
        ObjectGenerator<PositiveAge> positive = new ObjectGenerator<>(PositiveAge.class, config(mode));
        ObjectGenerator<PatternedAge> patterned = new ObjectGenerator<>(PatternedAge.class, config(mode));
        for (int i = 0; i < SAMPLES; i++) {
            RangedAge rangedAge = ranged.generate();
            assertValid(rangedAge);
            assertEquals(ChronoUnit.YEARS.between(rangedAge.birthDate(), TODAY), rangedAge.age(), rangedAge.toString());
            PositiveAge positiveAge = positive.generate();
            assertValid(positiveAge);
            assertEquals(
                    ChronoUnit.YEARS.between(positiveAge.birthDate().toLocalDate(), TODAY),
                    positiveAge.age().longValue(),
                    positiveAge.toString());
            PatternedAge patternedAge = patterned.generate();
            assertValid(patternedAge);
            assertEquals(
                    ChronoUnit.YEARS.between(patternedAge.birthDate().toLocalDate(), TODAY),
                    Long.parseLong(patternedAge.age()),
                    patternedAge.toString());
        }
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(
            value = ObjectGenerationSemanticMode.class,
            names = {"RELAXED", "STRICT"})
    @DisplayName("a birthDate or an age keeps a value its constraints require beside one that cannot follow it")
    void birthDateOrAgeKeepsAValueItsConstraintsRequire(ObjectGenerationSemanticMode mode) {
        // An age of 30 gives a birthDate 30 years ago, which a @Future one cannot take.
        ObjectGenerator<FutureBirthDate> future =
                new ObjectGenerator<>(FutureBirthDate.class, pinnedAge(FutureBirthDate.class, mode, 30));
        // An age of 0 gives a birthDate of today, which a @Past one cannot take.
        ObjectGenerator<PastBirthDate> past =
                new ObjectGenerator<>(PastBirthDate.class, pinnedAge(PastBirthDate.class, mode, 0));
        ObjectGenerator<NullBirthDate> nullBirthDate = new ObjectGenerator<>(NullBirthDate.class, config(mode));
        ObjectGenerator<NullAge> nullAge = new ObjectGenerator<>(NullAge.class, config(mode));

        for (int i = 0; i < SAMPLES; i++) {
            FutureBirthDate futureValue = future.generate();
            assertValid(futureValue);
            assertTrue(futureValue.birthDate().isAfter(TODAY), futureValue.toString());
            PastBirthDate pastValue = past.generate();
            assertValid(pastValue);
            assertTrue(pastValue.birthDate().isBefore(TODAY), pastValue.toString());
            NullBirthDate nullBirthDateValue = nullBirthDate.generate();
            assertNull(nullBirthDateValue.birthDate(), nullBirthDateValue.toString());
            NullAge nullAgeValue = nullAge.generate();
            assertNull(nullAgeValue.age(), nullAgeValue.toString());
        }
    }

    private static GeneratorConfig config(ObjectGenerationSemanticMode mode) {
        return GeneratorConfig.builder()
                .seed(42L)
                .clock(CLOCK)
                .objectSemanticMode(mode)
                .build();
    }

    private static ObjectGeneratorConfig pinnedAge(Class<?> type, ObjectGenerationSemanticMode mode, int age) {
        return ObjectGeneratorConfig.builder()
                .generatorConfig(config(mode))
                .override(type, "age", () -> age)
                .build();
    }

    /** Every value meets its constraints, and createdAt is not after updatedAt, by UTC date beside a date. */
    private static <T> void assertValidAndInOrder(
            Class<T> type, GeneratorConfig config, Function<T, Object> createdAt, Function<T, Object> updatedAt) {
        ObjectGenerator<T> generator = new ObjectGenerator<>(type, config);
        for (int i = 0; i < SAMPLES; i++) {
            T value = generator.generate();
            assertValid(value);
            Instant created = SemanticCoherenceAdjuster.toInstant(createdAt.apply(value));
            Object updated = updatedAt.apply(value);
            if (updated instanceof LocalDate date) {
                assertFalse(created.atOffset(ZoneOffset.UTC).toLocalDate().isAfter(date), value.toString());
            } else {
                assertFalse(created.isAfter(SemanticCoherenceAdjuster.toInstant(updated)), value.toString());
            }
        }
    }

    /**
     * With updatedAt held in the past by an override, every value meets its constraints, so a createdAt that
     * must stay in the future stays after updatedAt.
     */
    private static <T> void assertValidAndOutOfOrder(
            Class<T> type, ObjectGenerationSemanticMode mode, Object updatedAt, Function<T, Instant> createdAt) {
        ObjectGenerator<T> generator = new ObjectGenerator<>(
                type,
                ObjectGeneratorConfig.builder()
                        .generatorConfig(config(mode))
                        .override(type, "updatedAt", () -> updatedAt)
                        .build());
        for (int i = 0; i < SAMPLES; i++) {
            T value = generator.generate();
            assertValid(value);
            assertTrue(createdAt.apply(value).isAfter(A_DAY_AGO), value.toString());
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
