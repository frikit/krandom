/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator;

import io.github.frikit.krandom.generator.base.AtomicIntegerGenerator;
import io.github.frikit.krandom.generator.base.AtomicLongGenerator;
import io.github.frikit.krandom.generator.base.NumberGenerator;
import io.github.frikit.krandom.generator.datetime.LegacyTimeZoneGenerator;
import io.github.frikit.krandom.generator.datetime.MonthDayGenerator;
import io.github.frikit.krandom.generator.datetime.OffsetTimeGenerator;
import io.github.frikit.krandom.generator.datetime.PeriodGenerator;
import io.github.frikit.krandom.generator.datetime.YearGenerator;
import io.github.frikit.krandom.generator.datetime.YearMonthGenerator;
import io.github.frikit.krandom.generator.datetime.ZoneIdGenerator;
import io.github.frikit.krandom.generator.datetime.ZoneOffsetGenerator;
import io.github.frikit.krandom.generator.namespace.DateTimeGenerators;
import io.github.frikit.krandom.generator.object.exception.ObjectGenerationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.MonthDay;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.Period;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Locale;
import java.util.Optional;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("built-in generator catalog")
class BuiltInGeneratorCatalogTest {

    @Test
    @DisplayName("scalar generator factories produce numeric values")
    void scalarGeneratorFactoriesProduceNumericValues() {
        assertInstanceOf(Number.class, Generators.ofNumber().generate());
        assertInstanceOf(AtomicInteger.class, Generators.ofAtomicInteger().generate());
        assertInstanceOf(AtomicLong.class, Generators.ofAtomicLong().generate());

        AtomicInteger boundedInteger = Generators.ofAtomicInteger(10, 20, 42L).generate();
        AtomicInteger unseededBoundedInteger = Generators.ofAtomicInteger(10, 20).generate();
        AtomicLong boundedLong = Generators.ofAtomicLong(100L, 200L, 42L).generate();
        AtomicLong unseededBoundedLong = Generators.ofAtomicLong(100L, 200L).generate();

        assertTrue(boundedInteger.get() >= 10 && boundedInteger.get() < 20);
        assertTrue(unseededBoundedInteger.get() >= 10 && unseededBoundedInteger.get() < 20);
        assertTrue(boundedLong.get() >= 100L && boundedLong.get() < 200L);
        assertTrue(unseededBoundedLong.get() >= 100L && unseededBoundedLong.get() < 200L);
        assertEquals(Generators.ofNumber(123L).generate(), Generators.ofNumber(123L).generate());
    }

    @Test
    @DisplayName("new number generators validate bounds and support seeding")
    void newNumberGeneratorsValidateBoundsAndSupportSeeding() {
        NumberGenerator number = new NumberGenerator(GeneratorConfig.builder().seed(1L).build());
        assertTrue(number.generate(20, 10).intValue() >= 10);
        assertThrows(IllegalArgumentException.class, () -> number.generate(5, 5));
        assertThrows(NullPointerException.class, () -> new NumberGenerator(null));
        number.reseed(99L);
        assertEquals(new NumberGenerator(99L).generate(), number.generate());

        AtomicIntegerGenerator atomicInteger = new AtomicIntegerGenerator(20, 10, 5L);
        assertNotNull(new AtomicIntegerGenerator(GeneratorConfig.builder().seed(3L).build()).generate());
        assertNotNull(new AtomicIntegerGenerator(0, 1).generate());
        int integerValue = atomicInteger.generate().get();
        assertTrue(integerValue >= 10 && integerValue < 20);
        assertThrows(IllegalArgumentException.class, () -> new AtomicIntegerGenerator(1, 1));
        assertThrows(NullPointerException.class, () -> new AtomicIntegerGenerator(null));
        atomicInteger.reseed(123L);
        assertEquals(new AtomicIntegerGenerator(20, 10, 123L).generate().get(), atomicInteger.generate().get());

        AtomicLongGenerator atomicLong = new AtomicLongGenerator(20L, 10L, 5L);
        assertNotNull(new AtomicLongGenerator(GeneratorConfig.builder().seed(3L).build()).generate());
        assertNotNull(new AtomicLongGenerator(0L, 1L).generate());
        long longValue = atomicLong.generate().get();
        assertTrue(longValue >= 10L && longValue < 20L);
        assertThrows(IllegalArgumentException.class, () -> new AtomicLongGenerator(1L, 1L));
        assertThrows(NullPointerException.class, () -> new AtomicLongGenerator(null));
        atomicLong.reseed(123L);
        assertEquals(new AtomicLongGenerator(20L, 10L, 123L).generate().get(), atomicLong.generate().get());
    }

    @Test
    @DisplayName("text and regex generators produce strings")
    void textAndRegexGeneratorsProduceStrings() {
        assertNotNull(Generators.ofString().generate());
        assertNotNull(Generators.ofChar().generate());
        assertNotNull(Generators.ofWord().generate());
        assertNotNull(Generators.ofSentence().generate());
        assertNotNull(Generators.ofParagraph().generate());

        String value = Generators.ofRegex("[A-Z]{2}\\d{3}", 99L).generate();
        assertTrue(value.matches("[A-Z]{2}\\d{3}"), "Expected regex-compatible value, got: " + value);
        assertTrue(Generators.ofRegex("\\d{2}").generate().matches("\\d{2}"));
    }

    @Test
    @DisplayName("date-time generator factories produce temporal values")
    void dateTimeGeneratorFactoriesProduceTemporalValues() {
        assertInstanceOf(java.util.Date.class, Generators.ofUtilDate().generate());
        assertInstanceOf(java.sql.Date.class, Generators.ofSqlDate().generate());
        assertInstanceOf(java.sql.Time.class, Generators.ofSqlTime().generate());
        assertInstanceOf(java.sql.Timestamp.class, Generators.ofSqlTimestamp().generate());
        assertInstanceOf(LocalDate.class, Generators.ofLocalDate().generate());
        assertInstanceOf(LocalTime.class, Generators.ofLocalTime().generate());
        assertInstanceOf(LocalDateTime.class, Generators.ofLocalDateTime().generate());
        assertInstanceOf(Instant.class, Generators.ofInstant().generate());
        assertInstanceOf(OffsetDateTime.class, Generators.ofOffsetDateTime().generate());
        assertInstanceOf(OffsetTime.class, Generators.ofOffsetTime().generate());
        assertInstanceOf(ZonedDateTime.class, Generators.ofZonedDateTime().generate());
        assertInstanceOf(Year.class, Generators.ofYear().generate());
        assertInstanceOf(YearMonth.class, Generators.ofYearMonth().generate());
        assertInstanceOf(MonthDay.class, Generators.ofMonthDay().generate());
        assertInstanceOf(Duration.class, Generators.ofDuration().generate());
        assertInstanceOf(Period.class, Generators.ofPeriod().generate());
        assertInstanceOf(ZoneId.class, Generators.ofZoneId().generate());
        assertInstanceOf(ZoneOffset.class, Generators.ofZoneOffset().generate());
        assertInstanceOf(TimeZone.class, Generators.ofTimeZone().generate());

        DateTimeGenerators datetime = Generators.datetime(GeneratorConfig.builder().seed(31L).build());
        assertInstanceOf(java.util.Date.class, datetime.utilDate().generate());
        assertInstanceOf(java.sql.Date.class, datetime.sqlDate().generate());
        assertInstanceOf(java.sql.Time.class, datetime.sqlTime().generate());
        assertInstanceOf(java.sql.Timestamp.class, datetime.sqlTimestamp().generate());
        assertInstanceOf(LocalTime.class, datetime.localTime().generate());
        assertInstanceOf(OffsetDateTime.class, datetime.offsetDateTime().generate());
        assertInstanceOf(OffsetTime.class, datetime.offsetTime().generate());
        assertInstanceOf(Year.class, datetime.year().generate());
        assertInstanceOf(YearMonth.class, datetime.yearMonth().generate());
        assertInstanceOf(MonthDay.class, datetime.monthDay().generate());
        assertInstanceOf(Period.class, datetime.period().generate());
        assertInstanceOf(ZoneId.class, datetime.zoneId().generate());
        assertInstanceOf(ZoneOffset.class, datetime.zoneOffset().generate());
        assertInstanceOf(TimeZone.class, datetime.timeZone().generate());
    }

    @Test
    @DisplayName("new date-time generators validate arguments and support reseeding")
    void newDateTimeGeneratorsValidateArgumentsAndSupportReseeding() {
        assertThrows(NullPointerException.class, () -> new OffsetTimeGenerator(null));
        assertThrows(NullPointerException.class, () -> new YearGenerator(null));
        assertThrows(NullPointerException.class, () -> new YearMonthGenerator(null));
        assertThrows(NullPointerException.class, () -> new MonthDayGenerator(null));
        assertThrows(NullPointerException.class, () -> new PeriodGenerator(null));
        assertThrows(NullPointerException.class, () -> new ZoneIdGenerator(null));
        assertThrows(NullPointerException.class, () -> new ZoneOffsetGenerator(null));
        assertThrows(NullPointerException.class, () -> new LegacyTimeZoneGenerator(null));
        assertThrows(IllegalArgumentException.class, () -> new YearGenerator(2030, 2020));
        assertThrows(IllegalArgumentException.class, () -> new YearMonthGenerator(2030, 2020));

        YearGenerator year = new YearGenerator(2020, 2025, 9L);
        assertNotNull(new YearGenerator(2020, 2025).generate());
        Year generatedYear = year.generate();
        assertTrue(generatedYear.getValue() >= 2020 && generatedYear.getValue() <= 2025);
        year.reseed(12L);
        assertEquals(new YearGenerator(2020, 2025, 12L).generate(), year.generate());

        YearMonthGenerator yearMonth = new YearMonthGenerator(2020, 2025, 9L);
        assertNotNull(new YearMonthGenerator(2020, 2025).generate());
        YearMonth generatedYearMonth = yearMonth.generate();
        assertTrue(generatedYearMonth.getYear() >= 2020 && generatedYearMonth.getYear() <= 2025);
        yearMonth.reseed(12L);
        assertEquals(new YearMonthGenerator(2020, 2025, 12L).generate(), yearMonth.generate());

        OffsetTimeGenerator offsetTime = new OffsetTimeGenerator(9L);
        offsetTime.reseed(12L);
        assertEquals(new OffsetTimeGenerator(12L).generate(), offsetTime.generate());

        MonthDayGenerator monthDay = new MonthDayGenerator(9L);
        monthDay.reseed(12L);
        assertEquals(new MonthDayGenerator(12L).generate(), monthDay.generate());

        PeriodGenerator period = new PeriodGenerator(9L);
        period.reseed(12L);
        assertEquals(new PeriodGenerator(12L).generate(), period.generate());

        ZoneIdGenerator zoneId = new ZoneIdGenerator(9L);
        zoneId.reseed(12L);
        assertEquals(new ZoneIdGenerator(12L).generate(), zoneId.generate());

        ZoneOffsetGenerator zoneOffset = new ZoneOffsetGenerator(9L);
        zoneOffset.reseed(12L);
        assertEquals(new ZoneOffsetGenerator(12L).generate(), zoneOffset.generate());

        LegacyTimeZoneGenerator timeZone = new LegacyTimeZoneGenerator(9L);
        timeZone.reseed(12L);
        assertEquals(new LegacyTimeZoneGenerator(12L).generate(), timeZone.generate());
    }

    @Test
    @DisplayName("misc, network, identifier, and domain randomizers have native replacements")
    void miscNetworkIdentifierAndDomainRandomizersHaveNativeReplacements() {
        assertInstanceOf(Boolean.class, Generators.ofBoolean().generate());
        assertEquals("fixed", Generators.ofConstant("fixed").generate());
        assertInstanceOf(Locale.class, Generators.ofLocale().generate());
        assertInstanceOf(UUID.class, Generators.ofUuid().generate());
        assertInstanceOf(URI.class, Generators.ofURI().generate());
        assertInstanceOf(java.net.URL.class, Generators.ofURL().generate());
        assertInstanceOf(URI.class, Generators.ofURI(GeneratorConfig.builder().seed(7L).build()).generate());
        assertInstanceOf(java.net.URL.class, Generators.ofURL(GeneratorConfig.builder().seed(7L).build()).generate());

        assertNotNull(Generators.ofUri().generate());
        assertNotNull(Generators.ofUrl().generate());
        assertNotNull(Generators.ofIPv4().generate());
        assertNotNull(Generators.ofIPv6().generate());
        assertNotNull(Generators.ofMacAddress().generate());
        assertNotNull(Generators.ofEmail().generate());
        assertNotNull(Generators.ofPassword().generate());
        assertNotNull(Generators.ofPhoneNumber().generate());
        assertNotNull(Generators.ofCity().generate());
        assertNotNull(Generators.ofState().generate());
        assertNotNull(Generators.ofCountry().generate());
        assertNotNull(Generators.ofStreetAddress().generate());
        assertNotNull(Generators.ofPostalCode().generate());
        assertNotNull(Generators.ofCompanyName().generate());
        assertNotNull(Generators.ofCreditCard().generate());
        assertNotNull(Generators.ofIsbn().generate());
        assertNotNull(Generators.location().coordinates().generateLatitude());
        assertNotNull(Generators.location().coordinates().generateLongitude());
    }

    @Test
    @DisplayName("URL conversion failures are surfaced as native exceptions")
    void urlConversionFailuresAreSurfacedAsNativeExceptions() throws Exception {
        Method generatorToUrl = Generators.class.getDeclaredMethod("toUrl", URI.class);
        generatorToUrl.setAccessible(true);
        InvocationTargetException generatorFailure = assertThrows(
            InvocationTargetException.class,
            () -> generatorToUrl.invoke(null, URI.create("relative/path"))
        );
        assertInstanceOf(IllegalStateException.class, generatorFailure.getCause());

        Class<?> resolverClass = Class.forName("io.github.frikit.krandom.generator.object.FieldGeneratorResolver");
        Method resolverToUrl = resolverClass.getDeclaredMethod("toUrl", URI.class);
        resolverToUrl.setAccessible(true);
        InvocationTargetException resolverFailure = assertThrows(
            InvocationTargetException.class,
            () -> resolverToUrl.invoke(null, URI.create("relative/path"))
        );
        assertInstanceOf(ObjectGenerationException.class, resolverFailure.getCause());
    }

    @Test
    @DisplayName("collection, map, optional, and Java built-ins resolve during object generation")
    void objectGenerationResolvesBuiltInFamilies() {
        BuiltInFixture fixture = Generators.ofObject(
            BuiltInFixture.class,
            GeneratorConfig.builder().seed(777L).collectionSize(1, 2).build()
        ).generate();

        assertNotNull(fixture.number);
        assertNotNull(fixture.atomicInteger);
        assertNotNull(fixture.atomicLong);
        assertNotNull(fixture.values);
        assertTrue(fixture.maybeName.isPresent() || fixture.maybeName.isEmpty());
        assertNotNull(fixture.timeZone);
        assertNotNull(fixture.uri);
        assertNotNull(fixture.url);
    }

    @Test
    @DisplayName("generic forType lookup includes built-in generator targets")
    void forTypeIncludesBuiltInGeneratorTargets() {
        assertInstanceOf(Number.class, Generators.forType(Number.class).generate());
        assertInstanceOf(AtomicInteger.class, Generators.forType(AtomicInteger.class).generate());
        assertInstanceOf(AtomicLong.class, Generators.forType(AtomicLong.class).generate());
        assertInstanceOf(LocalTime.class, Generators.forType(LocalTime.class).generate());
        assertInstanceOf(OffsetTime.class, Generators.forType(OffsetTime.class).generate());
        assertInstanceOf(Year.class, Generators.forType(Year.class).generate());
        assertInstanceOf(YearMonth.class, Generators.forType(YearMonth.class).generate());
        assertInstanceOf(MonthDay.class, Generators.forType(MonthDay.class).generate());
        assertInstanceOf(Period.class, Generators.forType(Period.class).generate());
        assertInstanceOf(ZoneId.class, Generators.forType(ZoneId.class).generate());
        assertInstanceOf(ZoneOffset.class, Generators.forType(ZoneOffset.class).generate());
        assertInstanceOf(TimeZone.class, Generators.forType(TimeZone.class).generate());
        assertInstanceOf(URI.class, Generators.forType(URI.class).generate());
        assertInstanceOf(java.net.URL.class, Generators.forType(java.net.URL.class).generate());
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    static final class BuiltInFixture {
        Number number;
        AtomicInteger atomicInteger;
        AtomicLong atomicLong;
        java.util.List<String> values;
        Optional<String> maybeName;
        TimeZone timeZone;
        URI uri;
        java.net.URL url;
    }
}
