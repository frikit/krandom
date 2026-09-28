/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator;

import io.github.frikit.krandom.generator.base.AtomicIntegerGenerator;
import io.github.frikit.krandom.generator.base.AtomicLongGenerator;
import io.github.frikit.krandom.generator.base.BigDecimalGenerator;
import io.github.frikit.krandom.generator.base.BigIntegerGenerator;
import io.github.frikit.krandom.generator.base.BooleanGenerator;
import io.github.frikit.krandom.generator.base.ByteGenerator;
import io.github.frikit.krandom.generator.base.CharGenerator;
import io.github.frikit.krandom.generator.base.IntGenerator;
import io.github.frikit.krandom.generator.base.LongGenerator;
import io.github.frikit.krandom.generator.base.NaturalNumberGenerator;
import io.github.frikit.krandom.generator.base.NormalDistributionGenerator;
import io.github.frikit.krandom.generator.base.NumberGenerator;
import io.github.frikit.krandom.generator.base.PrimeGenerator;
import io.github.frikit.krandom.generator.base.RegexGenerator;
import io.github.frikit.krandom.generator.base.ShortGenerator;
import io.github.frikit.krandom.generator.base.StringGenerator;
import io.github.frikit.krandom.generator.datetime.CalendarGenerator;
import io.github.frikit.krandom.generator.datetime.LegacyTimeZoneGenerator;
import io.github.frikit.krandom.generator.datetime.MonthDayGenerator;
import io.github.frikit.krandom.generator.datetime.OffsetTimeGenerator;
import io.github.frikit.krandom.generator.datetime.PeriodGenerator;
import io.github.frikit.krandom.generator.datetime.YearGenerator;
import io.github.frikit.krandom.generator.datetime.YearMonthGenerator;
import io.github.frikit.krandom.generator.datetime.ZoneIdGenerator;
import io.github.frikit.krandom.generator.datetime.ZoneOffsetGenerator;
import io.github.frikit.krandom.generator.locale.RandomLocaleGenerator;
import io.github.frikit.krandom.generator.location.GeohashGenerator;
import io.github.frikit.krandom.generator.selection.FinitePoolGenerator;
import io.github.frikit.krandom.generator.selection.PickGenerator;
import io.github.frikit.krandom.generator.selection.PickSetGenerator;
import io.github.frikit.krandom.generator.selection.ShuffleGenerator;
import io.github.frikit.krandom.generator.selection.WeightedGenerator;
import io.github.frikit.krandom.generator.text.ProviderTemplateGenerator;
import io.github.frikit.krandom.generator.text.TemplateStringGenerator;
import io.github.frikit.krandom.generator.user.AgeGenerator;
import io.github.frikit.krandom.generator.user.AgeType;
import io.github.frikit.krandom.generator.user.BirthdayGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.reflect.Constructor;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Raw {@code long seed} constructors bypass replayable recipes. Each one is deprecated in favour
 * of a {@link GeneratorConfig} form that produces exactly the same values for the same seed, so
 * callers can migrate without changing their fixtures.
 */
@DisplayName("Legacy long-seed constructors migrate to GeneratorConfig forms")
@SuppressWarnings({"deprecation", "removal"})
class LegacySeedMigrationTest {

    private static final long SEED = 424242L;
    private static final List<String> ITEMS = List.of("alpha", "beta", "gamma", "delta", "epsilon");

    private record Migration(String name, Supplier<Generator<?>> legacy, Supplier<Generator<?>> configured) {

        @Override
        public String toString() {
            return name;
        }
    }

    private static GeneratorConfig cfg() {
        return GeneratorConfig.builder().seed(SEED).build();
    }

    private static GeneratorConfig cfg(Locale locale) {
        return GeneratorConfig.builder().seed(SEED).locale(locale).build();
    }

    static Stream<Migration> migrations() {
        return Stream.of(
            new Migration("AtomicInteger", () -> new AtomicIntegerGenerator(1, 1000, SEED),
                          () -> new AtomicIntegerGenerator(1, 1000, cfg())),
            new Migration("AtomicLong", () -> new AtomicLongGenerator(1L, 1000L, SEED),
                          () -> new AtomicLongGenerator(1L, 1000L, cfg())),
            new Migration("BigDecimal", () -> new BigDecimalGenerator(BigDecimal.ONE, BigDecimal.TEN, 3, SEED),
                          () -> new BigDecimalGenerator(BigDecimal.ONE, BigDecimal.TEN, 3, cfg())),
            new Migration("BigInteger", () -> new BigIntegerGenerator(BigInteger.ONE, BigInteger.valueOf(99_999L), SEED),
                          () -> new BigIntegerGenerator(BigInteger.ONE, BigInteger.valueOf(99_999L), cfg())),
            new Migration("Boolean", () -> new BooleanGenerator(SEED), () -> new BooleanGenerator(cfg())),
            new Migration("Byte", () -> new ByteGenerator((byte) -5, (byte) 90, SEED),
                          () -> new ByteGenerator((byte) -5, (byte) 90, cfg())),
            new Migration("Int", () -> new IntGenerator(-50, 5000, SEED), () -> new IntGenerator(-50, 5000, cfg())),
            new Migration("Long", () -> new LongGenerator(-50L, 5_000_000L, SEED),
                          () -> new LongGenerator(-50L, 5_000_000L, cfg())),
            new Migration("NaturalNumber", () -> new NaturalNumberGenerator(0, 700, SEED),
                          () -> new NaturalNumberGenerator(0, 700, cfg())),
            new Migration("Number", () -> new NumberGenerator(SEED), () -> new NumberGenerator(cfg())),
            new Migration("NormalDistribution", () -> new NormalDistributionGenerator(10.0, 2.5, SEED),
                          () -> new NormalDistributionGenerator(cfg(), 10.0, 2.5)),
            new Migration("Prime", () -> new PrimeGenerator(2, 5000, SEED), () -> new PrimeGenerator(2, 5000, cfg())),
            new Migration("Regex", () -> new RegexGenerator("[A-Z]{3}-\\d{4}", SEED),
                          () -> new RegexGenerator("[A-Z]{3}-\\d{4}", cfg())),
            new Migration("Short", () -> new ShortGenerator((short) -90, (short) 900, SEED),
                          () -> new ShortGenerator((short) -90, (short) 900, cfg())),
            new Migration("Calendar", () -> new CalendarGenerator(SEED), () -> new CalendarGenerator(cfg())),
            new Migration("LegacyTimeZone", () -> new LegacyTimeZoneGenerator(SEED), () -> new LegacyTimeZoneGenerator(cfg())),
            new Migration("MonthDay", () -> new MonthDayGenerator(SEED), () -> new MonthDayGenerator(cfg())),
            new Migration("OffsetTime", () -> new OffsetTimeGenerator(SEED), () -> new OffsetTimeGenerator(cfg())),
            new Migration("Period", () -> new PeriodGenerator(SEED), () -> new PeriodGenerator(cfg())),
            new Migration("Year", () -> new YearGenerator(1900, 2100, SEED), () -> new YearGenerator(1900, 2100, cfg())),
            new Migration("YearMonth", () -> new YearMonthGenerator(1900, 2100, SEED),
                          () -> new YearMonthGenerator(1900, 2100, cfg())),
            new Migration("ZoneId", () -> new ZoneIdGenerator(SEED), () -> new ZoneIdGenerator(cfg())),
            new Migration("ZoneOffset", () -> new ZoneOffsetGenerator(SEED), () -> new ZoneOffsetGenerator(cfg())),
            new Migration("RandomLocale", () -> new RandomLocaleGenerator(SEED), () -> new RandomLocaleGenerator(cfg())),
            new Migration("Geohash", () -> new GeohashGenerator(SEED), () -> new GeohashGenerator(cfg())),
            new Migration("GeohashPrecision", () -> new GeohashGenerator(9, SEED), () -> new GeohashGenerator(9, cfg())),
            new Migration("Pick", () -> new PickGenerator<>(ITEMS, SEED), () -> new PickGenerator<>(ITEMS, cfg())),
            new Migration("PickSet", () -> new PickSetGenerator<>(ITEMS, 3, SEED),
                          () -> new PickSetGenerator<>(ITEMS, 3, cfg())),
            new Migration("FinitePool", () -> new FinitePoolGenerator<>(ITEMS, SEED),
                          () -> new FinitePoolGenerator<>(ITEMS, cfg())),
            new Migration("Shuffle", () -> new ShuffleGenerator<>(ITEMS, SEED), () -> new ShuffleGenerator<>(ITEMS, cfg())),
            new Migration("Weighted", () -> new WeightedGenerator<>(ITEMS, List.of(5, 1, 1, 3, 2), SEED),
                          () -> new WeightedGenerator<>(ITEMS, List.of(5, 1, 1, 3, 2), cfg())),
            new Migration("TemplateString", () -> new TemplateStringGenerator("##-??-##", SEED),
                          () -> new TemplateStringGenerator("##-??-##", cfg())),
            new Migration("ProviderTemplate", () -> new ProviderTemplateGenerator("{firstname} {lastname}", SEED),
                          () -> new ProviderTemplateGenerator("{firstname} {lastname}", cfg())),
            new Migration("Age", () -> new AgeGenerator(SEED), () -> new AgeGenerator(cfg())),
            new Migration("AgeType", () -> new AgeGenerator(AgeType.TEEN, SEED), () -> new AgeGenerator(AgeType.TEEN, cfg())),
            new Migration("Birthday", () -> new BirthdayGenerator(SEED), () -> new BirthdayGenerator(cfg())),
            new Migration("BirthdayType", () -> new BirthdayGenerator(AgeType.ADULT, SEED),
                          () -> new BirthdayGenerator(AgeType.ADULT, cfg())),
            new Migration("BirthdayLocale", () -> new BirthdayGenerator(Locale.GERMANY, SEED),
                          () -> new BirthdayGenerator(cfg(Locale.GERMANY))),
            new Migration("BirthdayTypeLocale", () -> new BirthdayGenerator(AgeType.SENIOR, Locale.JAPAN, SEED),
                          () -> new BirthdayGenerator(AgeType.SENIOR, cfg(Locale.JAPAN))),
            new Migration("BirthdayRangeLocale", () -> new BirthdayGenerator(20, 30, Locale.FRANCE, SEED),
                          () -> new BirthdayGenerator(20, 30, cfg(Locale.FRANCE)))
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("migrations")
    @DisplayName("GeneratorConfig form reproduces the legacy seeded output")
    void configFormMatchesLegacySeed(Migration migration) {
        assertEquals(sample(migration.legacy().get()), sample(migration.configured().get()), migration.name());
    }

    @Test
    @DisplayName("every public raw long-seed constructor is deprecated")
    void rawSeedConstructorsAreDeprecated() throws NoSuchMethodException {
        List<Constructor<?>> legacy = List.of(
            AtomicIntegerGenerator.class.getConstructor(int.class, int.class, long.class),
            AtomicLongGenerator.class.getConstructor(long.class, long.class, long.class),
            BigDecimalGenerator.class.getConstructor(BigDecimal.class, BigDecimal.class, int.class, long.class),
            BigIntegerGenerator.class.getConstructor(BigInteger.class, BigInteger.class, long.class),
            BooleanGenerator.class.getConstructor(long.class),
            ByteGenerator.class.getConstructor(byte.class, byte.class, long.class),
            IntGenerator.class.getConstructor(int.class, int.class, long.class),
            LongGenerator.class.getConstructor(long.class, long.class, long.class),
            NaturalNumberGenerator.class.getConstructor(int.class, int.class, long.class),
            NumberGenerator.class.getConstructor(long.class),
            NormalDistributionGenerator.class.getConstructor(double.class, double.class, Long.class),
            PrimeGenerator.class.getConstructor(int.class, int.class, long.class),
            RegexGenerator.class.getConstructor(String.class, long.class),
            ShortGenerator.class.getConstructor(short.class, short.class, long.class),
            CalendarGenerator.class.getConstructor(long.class),
            LegacyTimeZoneGenerator.class.getConstructor(long.class),
            MonthDayGenerator.class.getConstructor(long.class),
            OffsetTimeGenerator.class.getConstructor(long.class),
            PeriodGenerator.class.getConstructor(long.class),
            YearGenerator.class.getConstructor(int.class, int.class, long.class),
            YearMonthGenerator.class.getConstructor(int.class, int.class, long.class),
            ZoneIdGenerator.class.getConstructor(long.class),
            ZoneOffsetGenerator.class.getConstructor(long.class),
            RandomLocaleGenerator.class.getConstructor(long.class),
            GeohashGenerator.class.getConstructor(long.class),
            GeohashGenerator.class.getConstructor(int.class, long.class),
            PickGenerator.class.getConstructor(List.class, long.class),
            PickSetGenerator.class.getConstructor(List.class, int.class, long.class),
            FinitePoolGenerator.class.getConstructor(List.class, long.class),
            ShuffleGenerator.class.getConstructor(List.class, long.class),
            WeightedGenerator.class.getConstructor(List.class, List.class, long.class),
            TemplateStringGenerator.class.getConstructor(String.class, long.class),
            ProviderTemplateGenerator.class.getConstructor(String.class, long.class),
            AgeGenerator.class.getConstructor(long.class),
            AgeGenerator.class.getConstructor(AgeType.class, long.class),
            BirthdayGenerator.class.getConstructor(long.class),
            BirthdayGenerator.class.getConstructor(AgeType.class, long.class),
            BirthdayGenerator.class.getConstructor(Locale.class, long.class),
            BirthdayGenerator.class.getConstructor(AgeType.class, Locale.class, long.class),
            BirthdayGenerator.class.getConstructor(int.class, int.class, Locale.class, long.class));
        List<String> notDeprecated = new ArrayList<>();
        for (Constructor<?> constructor : legacy) {
            if (!constructor.isAnnotationPresent(Deprecated.class)) {
                notDeprecated.add(constructor.toGenericString());
            }
        }
        assertEquals(List.of(), notDeprecated);
    }

    @Test
    @DisplayName("configuration forms reject a null configuration")
    void configFormsRejectNull() {
        GeneratorConfig none = null;
        List<Runnable> constructors = List.of(
            () -> new AtomicIntegerGenerator(1, 2, none),
            () -> new AtomicLongGenerator(1L, 2L, none),
            () -> new BigDecimalGenerator(none),
            () -> new BigDecimalGenerator(BigDecimal.ONE, BigDecimal.TEN, 2, none),
            () -> new BigIntegerGenerator(none),
            () -> new BigIntegerGenerator(BigInteger.ONE, BigInteger.TEN, none),
            () -> new BooleanGenerator(none),
            () -> new ByteGenerator(none),
            () -> new ByteGenerator((byte) 1, (byte) 2, none),
            () -> new IntGenerator(none),
            () -> new IntGenerator(1, 2, none),
            () -> new LongGenerator(none),
            () -> new LongGenerator(1L, 2L, none),
            () -> new NaturalNumberGenerator(none),
            () -> new NaturalNumberGenerator(1, 2, none),
            () -> new NormalDistributionGenerator(none),
            () -> new NormalDistributionGenerator(none, 0.0, 1.0),
            () -> new PrimeGenerator(none),
            () -> new PrimeGenerator(2, 10, none),
            () -> new RegexGenerator("a", none),
            () -> new ShortGenerator(none),
            () -> new ShortGenerator((short) 1, (short) 2, none),
            () -> new YearGenerator(1, 2, none),
            () -> new YearMonthGenerator(1, 2, none),
            () -> new PickGenerator<>(ITEMS, none),
            () -> new PickSetGenerator<>(ITEMS, 1, none),
            () -> new FinitePoolGenerator<>(ITEMS, none),
            () -> new ShuffleGenerator<>(ITEMS, none),
            () -> new WeightedGenerator<>(ITEMS, List.of(1, 1, 1, 1, 1), none),
            () -> CharGenerator.letters(none),
            () -> StringGenerator.letters(none),
            () -> StringGenerator.builder().config(none));
        for (Runnable constructor : constructors) {
            assertThrows(NullPointerException.class, constructor::run);
        }
    }

    @Test
    @DisplayName("default-range configuration forms use the documented default ranges")
    void defaultRangeConfigForms() {
        GeneratorConfig config = cfg();
        assertEquals(sample(new ByteGenerator()).size(), sample(new ByteGenerator(config)).size());
        for (Object value : sample(new PrimeGenerator(config))) {
            int prime = Integer.parseInt(value.toString());
            assertTrue(prime >= 2 && prime < 1000, value.toString());
        }
        for (Object value : sample(new NaturalNumberGenerator(config))) {
            assertTrue(Integer.parseInt(value.toString()) >= 0, value.toString());
        }
        for (Object value : sample(new NormalDistributionGenerator(config))) {
            assertTrue(Double.isFinite(Double.parseDouble(value.toString())), value.toString());
        }
        for (Object value : sample(new BigDecimalGenerator(config))) {
            BigDecimal decimal = new BigDecimal(value.toString());
            assertTrue(decimal.signum() >= 0 && decimal.scale() == 2, value.toString());
        }
        for (Object value : sample(new BigIntegerGenerator(config))) {
            assertTrue(new BigInteger(value.toString()).signum() >= 0, value.toString());
        }
        assertEquals(sample(new IntGenerator(cfg())), sample(new IntGenerator(cfg())));
        assertEquals(sample(new LongGenerator(cfg())), sample(new LongGenerator(cfg())));
        assertEquals(sample(new ShortGenerator(cfg())), sample(new ShortGenerator(cfg())));
    }

    @Test
    @DisplayName("character and string configuration factories reproduce seeded builders")
    void charAndStringConfigFactories() {
        assertEquals(sample(CharGenerator.letters().withSeed(SEED)), sample(CharGenerator.letters(cfg())));
        assertEquals(sample(StringGenerator.builder().charGenerator(CharGenerator.letters()).seed(SEED).build()),
                     sample(StringGenerator.letters(cfg())));
        assertEquals(sample(StringGenerator.builder().charGenerator(CharGenerator.digits()).length(6).seed(SEED).build()),
                     sample(StringGenerator.builder().charGenerator(CharGenerator.digits()).length(6).config(cfg()).build()));
    }

    private static List<String> sample(Generator<?> generator) {
        List<String> values = new ArrayList<>();
        for (int i = 0; i < 25; i++) {
            Object value;
            try {
                value = generator.generate();
            } catch (RuntimeException ex) {
                values.add("!" + ex.getClass().getName());
                continue;
            }
            if (value instanceof Calendar calendar) {
                values.add(calendar.toInstant() + "@" + calendar.getTimeZone().getID());
            } else if (value instanceof TimeZone zone) {
                values.add(zone.getID());
            } else if (value instanceof Object[] array) {
                values.add(Arrays.deepToString(array));
            } else {
                values.add(String.valueOf(value));
            }
        }
        return values;
    }
}
