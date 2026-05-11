/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.object;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import io.github.frikit.krandom.generator.Generator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.RecordComponent;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.MonthDay;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZonedDateTime;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        assertFalse(BeanValidationSupport.hasNullConstraint(null));
        assertEquals(4, BeanValidationSupport.sizeFor(null, new Random(1L), 4, 4));
        assertEquals(0, BeanValidationSupport.sizeFor(field("emptyText"), new Random(1L), 1, 10));

        String empty = (String) generatorFor("emptyText", String.class).generate();
        assertEquals("", empty);

        int cappedSize = BeanValidationSupport.sizeFor(field("unboundedText"), new Random(1L), 1, 10);
        assertTrue(cappedSize >= 1 && cappedSize <= 255);

        String numericText = (String) generatorFor("numericText", String.class).generate();
        assertTrue(Long.parseLong(numericText) >= 5L && Long.parseLong(numericText) <= 7L);

        String decimalText = (String) generatorFor("decimalText", String.class).generate();
        assertEquals(0, new BigDecimal(decimalText).compareTo(new BigDecimal("1.25")));

        String decimalMaxText = (String) generatorFor("decimalMaxText", String.class).generate();
        assertTrue(new BigDecimal(decimalMaxText).compareTo(new BigDecimal("9.50")) <= 0);

        BigDecimal exclusiveMin = (BigDecimal) generatorFor("exclusiveMin", BigDecimal.class).generate();
        assertTrue(exclusiveMin.compareTo(new BigDecimal("1.00")) > 0);

        BigDecimal exclusiveMax = (BigDecimal) generatorFor("exclusiveMax", BigDecimal.class).generate();
        assertTrue(exclusiveMax.compareTo(new BigDecimal("5.00")) < 0);

        BigDecimal decimalMinWithMax = (BigDecimal) generatorFor("decimalMinWithMax", BigDecimal.class).generate();
        assertTrue(decimalMinWithMax.compareTo(new BigDecimal("1.00")) >= 0);
        assertTrue(decimalMinWithMax.compareTo(new BigDecimal("2.00")) <= 0);

        BigDecimal decimalMaxWithMin = (BigDecimal) generatorFor("decimalMaxWithMin", BigDecimal.class).generate();
        assertTrue(decimalMaxWithMin.compareTo(new BigDecimal("1.00")) >= 0);
        assertTrue(decimalMaxWithMin.compareTo(new BigDecimal("2.00")) <= 0);

        BigDecimal invertedDecimal = (BigDecimal) generatorFor("invertedDecimal", BigDecimal.class).generate();
        assertEquals(0, invertedDecimal.compareTo(new BigDecimal("4.00")));

        assertEquals(5L, generatorFor("exactLong", long.class).generate());
        assertNull(generatorFor("nullPrimitive", int.class));
        assertTrue((Byte) generatorFor("boxedByte", Byte.class).generate() <= (byte) 10);
        assertTrue((Short) generatorFor("boxedShort", Short.class).generate() >= (short) 3);
        assertTrue((Long) generatorFor("boxedLong", Long.class).generate() >= 5L);
        assertTrue(((Number) generatorFor("boundedNumber", Number.class).generate()).longValue() >= 5L);
        assertEquals(BigInteger.valueOf(5L), generatorFor("exactBigInteger", BigInteger.class).generate());
        assertEquals(BigInteger.valueOf(4L), generatorFor("invertedBigInteger", BigInteger.class).generate());
        assertTrue(((BigInteger) generatorFor("positiveBigInteger", BigInteger.class).generate()).signum() > 0);
        assertTrue((Float) generatorFor("positivePrimitiveFloat", float.class).generate() > 0.0f);
        assertTrue((Float) generatorFor("positiveFloat", Float.class).generate() > 0.0f);
        assertEquals(2.0d, (Double) generatorFor("exactDouble", Double.class).generate());
        assertTrue((Double) generatorFor("negativeDouble", Double.class).generate() < 0.0d);
        assertNull(generatorFor("numericUnsupported", Object.class));
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
        assertNull(generatorFor("futureUnsupported", Object.class).generate());
    }

    @Test
    @DisplayName("finds record component and interface accessor annotations")
    void recordAndInterfaceAccessorAnnotations() throws Exception {
        RecordComponent component = ComponentSample.class.getRecordComponents()[0];
        Generator<?> recordGenerator = BeanValidationSupport.constraintGeneratorFor(component, String.class, new Random(1L));
        assertNotNull(recordGenerator);
        assertEquals(2, ((String) recordGenerator.generate()).length());

        Generator<?> interfaceGetterGenerator = generatorFor("interfaceSizedText", String.class);
        assertNotNull(interfaceGetterGenerator);
        assertEquals(4, ((String) interfaceGetterGenerator.generate()).length());

        assertNull(BeanValidationSupport.constraintGeneratorFor(
            Sample.class.getDeclaredMethod("plainMethod"), String.class, new Random(1L)));
    }

    private static Field field(String name) throws NoSuchFieldException {
        return Sample.class.getDeclaredField(name);
    }

    private static Generator<?> generatorFor(String fieldName, Class<?> rawType) throws NoSuchFieldException {
        return BeanValidationSupport.constraintGeneratorFor(field(fieldName), rawType, new Random(1L));
    }


    interface InterfaceSizedText {

        @Size(min = 4, max = 4)
        String getInterfaceSizedText();
    }

    record ComponentSample(@Size(min = 2, max = 2) String code) {}

    static final class Sample implements InterfaceSizedText {

        @Size(min = 2, max = 4)
        String sizedText;

        @DecimalMin("1.5")
        BigDecimal decimalMinOnly;

        String getterSizedText;

        @Null
        String nullOnly;

        @Size(min = 0, max = 0)
        String emptyText;

        @Size(min = 1)
        String unboundedText;

        @Min(5)
        @Max(7)
        String numericText;

        @DecimalMin("1.25")
        @jakarta.validation.constraints.DecimalMax("1.25")
        String decimalText;

        @jakarta.validation.constraints.DecimalMax("9.50")
        String decimalMaxText;

        @DecimalMin(value = "1.00", inclusive = false)
        BigDecimal exclusiveMin;

        @jakarta.validation.constraints.DecimalMax(value = "5.00", inclusive = false)
        BigDecimal exclusiveMax;

        @DecimalMin("1.00")
        @Max(2)
        BigDecimal decimalMinWithMax;

        @Min(1)
        @jakarta.validation.constraints.DecimalMax("2.00")
        BigDecimal decimalMaxWithMin;

        @DecimalMin("4.00")
        @jakarta.validation.constraints.DecimalMax("3.00")
        BigDecimal invertedDecimal;

        @Min(5)
        @Max(5)
        long exactLong;

        @Null
        int nullPrimitive;

        @Min(5)
        @Max(5)
        BigInteger exactBigInteger;

        @Min(4)
        @Max(3)
        BigInteger invertedBigInteger;

        @Max(10)
        Byte boxedByte;

        @Min(3)
        Short boxedShort;

        @Min(5)
        Long boxedLong;

        @Min(5)
        Number boundedNumber;

        @Positive
        BigInteger positiveBigInteger;

        @Positive
        float positivePrimitiveFloat;

        @Positive
        Float positiveFloat;

        @Min(2)
        @Max(2)
        Double exactDouble;

        @jakarta.validation.constraints.Negative
        Double negativeDouble;

        @Min(1)
        Object numericUnsupported;

        Object unconstrained;

        @Future
        LocalDateTime futureLocalDateTime;

        @Past
        LocalDateTime pastLocalDateTime;

        @Future
        ZonedDateTime futureZonedDateTime;

        @Future
        OffsetDateTime futureOffsetDateTime;

        @Future
        java.sql.Date futureSqlDate;

        @Future
        Timestamp futureTimestamp;

        @Future
        LocalTime futureLocalTime;

        @Past
        LocalTime pastLocalTime;

        @Future
        OffsetTime futureOffsetTime;

        @Future
        Year futureYear;

        @Past
        Year pastYear;

        @Future
        YearMonth futureYearMonth;

        @Past
        YearMonth pastYearMonth;

        @Future
        MonthDay futureMonthDay;

        @Future
        Object futureUnsupported;

        String interfaceSizedText;

        @Size(min = 3, max = 5)
        String getGetterSizedText() {
            return getterSizedText;
        }

        @Override
        public String getInterfaceSizedText() {
            return interfaceSizedText;
        }

        String plainMethod() {
            return "";
        }
    }
}
