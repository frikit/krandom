/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.object;

import io.github.frikit.krandom.generator.GeneratorConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * End-to-end coverage for field types the coherence pass cannot represent: generation must keep
 * going instead of failing the whole object.
 */
class SemanticCoherenceTypeSupportTest {

    private static final Clock FIXED = Clock.fixed(Instant.parse("2026-09-26T10:00:00Z"), ZoneOffset.UTC);
    private static final LocalDate TODAY = LocalDate.now(FIXED);

    public static class SqlDateRow {
        public java.sql.Date createdAt;
        public java.sql.Date updatedAt;
    }

    public static class SqlTimeRow {
        public java.sql.Time createdAt;
        public java.sql.Time updatedAt;
    }

    public static class StringBirthDate {
        public String dateOfBirth;
        public int age;
    }

    public static class DoubleAge {
        public double age;
        public LocalDate birthDate;
    }

    public static class FloatAge {
        public Float age;
        public LocalDate birthDate;
    }

    public static class ByteAge {
        public byte age;
        public LocalDate birthDate;
    }

    public static class PrimitiveFloatAge {
        public float age;
        public LocalDate birthDate;
    }

    public static class BoxedDoubleAge {
        public Double age;
        public LocalDate birthDate;
    }

    public static class BoxedByteAge {
        public Byte age;
        public LocalDate birthDate;
    }

    public static class DecimalAge {
        public BigDecimal age;
        public LocalDate birthDate;
    }

    public static class RangedAge {
        @FakeRange(min = 18, max = 21)
        public int age;
        public LocalDate birthDate;
    }

    private static GeneratorConfig config(long seed) {
        return GeneratorConfig.builder().seed(seed).clock(FIXED).build();
    }

    private static <T> T generate(Class<T> type, long seed) {
        return new ObjectGenerator<>(type, config(seed)).generate();
    }

    private static long yearsSince(LocalDate birthDate) {
        return ChronoUnit.YEARS.between(birthDate, TODAY);
    }

    @Test
    @DisplayName("java.sql.Date timestamps are harmonized instead of failing")
    void sqlDateTimestampsAreHarmonized() {
        for (long seed = 0; seed < 20; seed++) {
            SqlDateRow row = generate(SqlDateRow.class, seed);
            assertNotNull(row.createdAt);
            assertNotNull(row.updatedAt);
            assertFalse(row.createdAt.toLocalDate().isAfter(row.updatedAt.toLocalDate()),
                "createdAt " + row.createdAt + " must not be after updatedAt " + row.updatedAt);
        }
    }

    @Test
    @DisplayName("java.sql.Date timestamps also generate when errors are ignored")
    void sqlDateTimestampsGenerateLeniently() {
        GeneratorConfig lenient = config(3L).toBuilder().objectIgnoreErrors(true).build();
        SqlDateRow row = new ObjectGenerator<>(SqlDateRow.class, lenient).generate();
        assertNotNull(row.createdAt);
    }

    @Test
    @DisplayName("java.sql.Time timestamps are left alone")
    void sqlTimeTimestampsAreLeftAlone() {
        for (long seed = 0; seed < 20; seed++) {
            long current = seed;
            SqlTimeRow row = assertDoesNotThrow(() -> generate(SqlTimeRow.class, current));
            assertNotNull(row.createdAt);
        }
    }

    @Test
    @DisplayName("a String birth date does not fail the object")
    void stringBirthDateDoesNotFail() {
        for (long seed = 0; seed < 20; seed++) {
            long current = seed;
            StringBirthDate person = assertDoesNotThrow(() -> generate(StringBirthDate.class, current));
            assertNotNull(person.dateOfBirth);
        }
    }

    @Test
    @DisplayName("floating-point and byte ages are derived from the birth date")
    void numericAgesAreDerivedFromBirthDate() {
        for (long seed = 0; seed < 20; seed++) {
            DoubleAge doubleAge = generate(DoubleAge.class, seed);
            assertEquals(yearsSince(doubleAge.birthDate), doubleAge.age);

            FloatAge floatAge = generate(FloatAge.class, seed);
            assertEquals((float) yearsSince(floatAge.birthDate), floatAge.age);

            ByteAge byteAge = generate(ByteAge.class, seed);
            assertEquals(yearsSince(byteAge.birthDate), byteAge.age);

            PrimitiveFloatAge primitiveFloat = generate(PrimitiveFloatAge.class, seed);
            assertEquals((float) yearsSince(primitiveFloat.birthDate), primitiveFloat.age);

            BoxedDoubleAge boxedDouble = generate(BoxedDoubleAge.class, seed);
            assertEquals((double) yearsSince(boxedDouble.birthDate), boxedDouble.age);

            BoxedByteAge boxedByte = generate(BoxedByteAge.class, seed);
            assertEquals((byte) yearsSince(boxedByte.birthDate), boxedByte.age);
        }
    }

    @Test
    @DisplayName("a byte age that cannot hold the derived age keeps its generated value")
    void byteAgeTooLargeForTheFieldIsKept() {
        LocalDate longAgo = LocalDate.of(1800, 1, 1);
        GeneratorConfig config = config(5L).toBuilder()
            .objectOverride(ByteAge.class, "birthDate", () -> longAgo)
            .build();
        ByteAge person = assertDoesNotThrow(() -> new ObjectGenerator<>(ByteAge.class, config).generate());
        assertEquals(longAgo, person.birthDate);
        assertTrue(yearsSince(person.birthDate) > Byte.MAX_VALUE);
    }

    @Test
    @DisplayName("a byte age below the field's range keeps its generated value instead of wrapping")
    void byteAgeBelowTheFieldRangeIsKept() {
        // A derived age of -300 years would wrap in a byte field; like a too-large age of 226 years,
        // it keeps the generated value instead.
        ByteAge future = generateWithBirthDate(TODAY.plusYears(300));
        ByteAge ancient = generateWithBirthDate(LocalDate.of(1800, 1, 1));

        assertEquals(TODAY.plusYears(300), future.birthDate);
        assertEquals(ancient.age, future.age, "both unrepresentable ages keep the generated value");
    }

    private static ByteAge generateWithBirthDate(LocalDate birthDate) {
        GeneratorConfig config = config(5L).toBuilder()
            .objectOverride(ByteAge.class, "birthDate", () -> birthDate)
            .build();
        return assertDoesNotThrow(() -> new ObjectGenerator<>(ByteAge.class, config).generate());
    }

    @Test
    @DisplayName("STRICT mode still lets semantics win over @FakeRange, as documented")
    void strictModeLetsSemanticsWin() {
        GeneratorConfig strict = config(11L).toBuilder()
            .objectSemanticMode(ObjectGenerationSemanticMode.STRICT)
            .build();
        RangedAge person = new ObjectGenerator<>(RangedAge.class, strict).generate();
        assertEquals(person.age, yearsSince(person.birthDate));
    }

    @Test
    @DisplayName("an age type the coherence pass cannot represent is left unchanged")
    void unsupportedAgeTypeIsLeftUnchanged() {
        for (long seed = 0; seed < 20; seed++) {
            long current = seed;
            DecimalAge person = assertDoesNotThrow(() -> generate(DecimalAge.class, current));
            assertNotNull(person.birthDate);
        }
    }

    public static class RangedByteAge {
        @FakeRange(min = 18, max = 21)
        public byte age;
        public LocalDate birthDate;
    }

    public static class RangedDoubleAge {
        @FakeRange(min = 18, max = 21)
        public Double age;
        public LocalDate birthDate;
    }

    @Test
    @DisplayName("protected byte and floating-point ages decide the birth date like int ages")
    void protectedByteAndFloatingAgesDecideTheBirthDate() {
        for (long seed = 0; seed < 20; seed++) {
            RangedByteAge byteAge = generate(RangedByteAge.class, seed);
            assertTrue(byteAge.age >= 18 && byteAge.age < 21, "age out of @FakeRange: " + byteAge.age);
            assertEquals(byteAge.age, yearsSince(byteAge.birthDate), "birthDate must follow the protected age");

            RangedDoubleAge doubleAge = generate(RangedDoubleAge.class, seed);
            assertEquals((long) Math.floor(doubleAge.age), yearsSince(doubleAge.birthDate),
                         "birthDate must follow the protected age " + doubleAge.age);
        }
    }

    @Test
    @DisplayName("@FakeRange age wins over the birth date in relaxed mode")
    void fakeRangeAgeIsProtectedInRelaxedMode() {
        for (long seed = 0; seed < 20; seed++) {
            RangedAge person = generate(RangedAge.class, seed);
            assertTrue(person.age >= 18 && person.age < 21, "age out of @FakeRange: " + person.age);
            assertEquals(person.age, yearsSince(person.birthDate), "birthDate must follow the protected age");
        }
    }
}
