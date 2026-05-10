/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.object;

import io.github.frikit.krandom.generator.GenerationContext;
import io.github.frikit.krandom.generator.Generator;
import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.base.BigDecimalGenerator;
import io.github.frikit.krandom.generator.base.BigIntegerGenerator;
import io.github.frikit.krandom.generator.base.BooleanGenerator;
import io.github.frikit.krandom.generator.base.ByteGenerator;
import io.github.frikit.krandom.generator.base.CharGenerator;
import io.github.frikit.krandom.generator.base.DoubleGenerator;
import io.github.frikit.krandom.generator.base.EnumGenerator;
import io.github.frikit.krandom.generator.base.FloatGenerator;
import io.github.frikit.krandom.generator.base.IntGenerator;
import io.github.frikit.krandom.generator.base.LongGenerator;
import io.github.frikit.krandom.generator.base.ShortGenerator;
import io.github.frikit.krandom.generator.base.StringGenerator;
import io.github.frikit.krandom.generator.datetime.DateGenerator;
import io.github.frikit.krandom.generator.datetime.InstantGenerator;
import io.github.frikit.krandom.generator.datetime.LocalDateTimeGenerator;
import io.github.frikit.krandom.generator.datetime.SqlDateGenerator;
import io.github.frikit.krandom.generator.datetime.SqlTimeGenerator;
import io.github.frikit.krandom.generator.datetime.SqlTimestampGenerator;
import io.github.frikit.krandom.generator.datetime.TimeGenerator;
import io.github.frikit.krandom.generator.datetime.UtilDateGenerator;
import io.github.frikit.krandom.generator.datetime.ZonedDateTimeGenerator;
import io.github.frikit.krandom.generator.finance.CurrencyGenerator;
import io.github.frikit.krandom.generator.identifier.UUIDGenerator;
import io.github.frikit.krandom.generator.location.CoordinatesGenerator;
import io.github.frikit.krandom.generator.object.exception.ObjectGenerationException;
import io.github.frikit.krandom.generator.provider.ProviderHub;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
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
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Optional;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.Stack;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;
import java.util.Vector;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Internal resolver: maps a field's ({@link Type}, {@link Class}, name, owner) tuple
 * to a generated value.
 *
 * <p>Resolution order (first match wins):
 * <ol>
 *   <li>Contextual field-level override from {@link ObjectGeneratorConfig}</li>
 *   <li>Contextual type-level override from {@link ObjectGeneratorConfig}</li>
 *   <li>Field-level override from {@link ObjectGeneratorConfig} ({@code "OwnerType.fieldName"})</li>
 *   <li>Type-level override from {@link ObjectGeneratorConfig}</li>
 *   <li>Declarative {@link Randomizer} annotation on the field/component</li>
 *   <li>Bean Validation constraint annotation (e.g. {@code @Size}, {@code @Min}, {@code @Max})</li>
 *   <li>Built-in generator for Java primitives / wrappers / {@code String} / JSR-310 types /
 *       {@link UUID} / {@link BigDecimal} / {@link BigInteger}</li>
 *   <li>Enum: random constant</li>
 *   <li>Array ({@code T[]}): auto-populated with the shared collection-size defaults
 *       (default 1 to 10 elements)</li>
 *   <li>{@code List<T>} or {@code Set<T>}: auto-populated with the shared collection-size defaults
 *       and elements resolved from the declared generic element type</li>
 *   <li>{@code Map<K,V>}: auto-populated with the shared collection-size defaults</li>
 *   <li>{@code Optional<T>}: populated as {@code Optional.ofNullable(value)}, optionally
 *       respecting the configured empty-rate</li>
 *   <li>Depth guard: if {@code currentDepth >= maxDepth} return primitive zero / {@code null}</li>
 *   <li>Nested class or record: delegate to a child {@link ObjectGenerator} (cycle-safe via
 *       {@link ObjectPool})</li>
 *   <li>Unsupported type: return primitive zero / {@code null}</li>
 * </ol>
 */
final class FieldGeneratorResolver {

    static final int DEFAULT_MIN_ELEMENT_COUNT = GeneratorConfig.defaults().getMinCollectionSize();
    static final int DEFAULT_MAX_ELEMENT_COUNT = GeneratorConfig.defaults().getMaxCollectionSize();

    /**
     * Safe zero-values used when a primitive field cannot be assigned its resolved value
     * (e.g. at max depth, or when {@code ignoreErrors=true}).
     */
    private static final Map<Class<?>, Object>                   PRIMITIVE_DEFAULTS = new HashMap<>();
    private static final Map<Class<?>, Function<String, Object>> ARGUMENT_PARSERS   = new HashMap<>();

    static {
        PRIMITIVE_DEFAULTS.put(byte.class, (byte) 0);
        PRIMITIVE_DEFAULTS.put(short.class, (short) 0);
        PRIMITIVE_DEFAULTS.put(int.class, 0);
        PRIMITIVE_DEFAULTS.put(long.class, 0L);
        PRIMITIVE_DEFAULTS.put(float.class, 0.0f);
        PRIMITIVE_DEFAULTS.put(double.class, 0.0);
        PRIMITIVE_DEFAULTS.put(char.class, '\0');
        PRIMITIVE_DEFAULTS.put(boolean.class, false);
    }

    static {
        ARGUMENT_PARSERS.put(String.class, s -> s);
        ARGUMENT_PARSERS.put(int.class, Integer::parseInt);
        ARGUMENT_PARSERS.put(Integer.class, Integer::valueOf);
        ARGUMENT_PARSERS.put(long.class, Long::parseLong);
        ARGUMENT_PARSERS.put(Long.class, Long::valueOf);
        ARGUMENT_PARSERS.put(double.class, Double::parseDouble);
        ARGUMENT_PARSERS.put(Double.class, Double::valueOf);
        ARGUMENT_PARSERS.put(float.class, Float::parseFloat);
        ARGUMENT_PARSERS.put(Float.class, Float::valueOf);
        ARGUMENT_PARSERS.put(boolean.class, Boolean::parseBoolean);
        ARGUMENT_PARSERS.put(Boolean.class, Boolean::valueOf);
        ARGUMENT_PARSERS.put(short.class, Short::parseShort);
        ARGUMENT_PARSERS.put(Short.class, Short::valueOf);
        ARGUMENT_PARSERS.put(byte.class, Byte::parseByte);
        ARGUMENT_PARSERS.put(Byte.class, Byte::valueOf);
        ARGUMENT_PARSERS.put(char.class, FieldGeneratorResolver::parseChar);
        ARGUMENT_PARSERS.put(Character.class, FieldGeneratorResolver::parseChar);
        ARGUMENT_PARSERS.put(BigInteger.class, BigInteger::new);
        ARGUMENT_PARSERS.put(BigDecimal.class, BigDecimal::new);
        ARGUMENT_PARSERS.put(java.util.Date.class, FieldGeneratorResolver::parseUtilDate);
        ARGUMENT_PARSERS.put(java.sql.Date.class, java.sql.Date::valueOf);
        ARGUMENT_PARSERS.put(java.sql.Time.class, java.sql.Time::valueOf);
        ARGUMENT_PARSERS.put(java.sql.Timestamp.class, java.sql.Timestamp::valueOf);
        ARGUMENT_PARSERS.put(LocalDate.class, LocalDate::parse);
        ARGUMENT_PARSERS.put(LocalTime.class, LocalTime::parse);
        ARGUMENT_PARSERS.put(LocalDateTime.class, LocalDateTime::parse);
    }

    private final ObjectGeneratorConfig config;
    private final GeneratorConfig       generatorConfig;
    private final ObjectPool            pool;
    private final UniqueFieldTracker    uniqueFieldTracker;
    private final Random                sequenceRandom;
    private final Map<Class<?>, Generator<?>> builtins;
    private final Map<String, Generator<?>>   semanticStringGenerators;
    private final Map<String, Map<Class<?>, Generator<?>>> semanticTypedGenerators;
    private final ObjectGenerationSemanticMode semanticMode;
    private final Set<String>                 uniqueFieldNames;

    private static final Map<String, String> SEMANTIC_KEYS_BY_ALIAS = buildSemanticKeysByAlias();
    private static final Map<String, Set<String>> SEMANTIC_ALIASES_BY_KEY = buildSemanticAliasesByKey();
    private static final Map<String, String> SEMANTIC_PROVIDER_NAMES_BY_KEY = buildSemanticProviderNames();

    FieldGeneratorResolver(ObjectGeneratorConfig config,
                           ObjectPool pool,
                           UniqueFieldTracker uniqueFieldTracker,
                           Long generationSeed) {
        this.config = config;
        this.generatorConfig = config.getGeneratorConfig();
        this.pool = pool;
        this.uniqueFieldTracker = uniqueFieldTracker;
        this.sequenceRandom = generationSeed != null ? new Random(generationSeed) : this.generatorConfig.createRandom();
        this.builtins = buildBuiltins(config, this.generatorConfig, this.sequenceRandom);
        this.semanticStringGenerators = buildSemanticStringGenerators(this.generatorConfig, this.sequenceRandom);
        this.semanticTypedGenerators = buildSemanticTypedGenerators(this.config, this.generatorConfig, this.sequenceRandom);
        this.semanticMode = config.getSemanticMode();
        this.uniqueFieldNames = config.getUniqueFieldNames();
    }

    private static Map<Class<?>, Generator<?>> buildBuiltins(ObjectGeneratorConfig cfg,
                                                             GeneratorConfig generatorConfig,
                                                             Random seedSource) {
        Map<Class<?>, Generator<?>> m = new HashMap<>();

        Long byteSeed = nextDeterministicSeed(generatorConfig, seedSource);
        Long shortSeed = nextDeterministicSeed(generatorConfig, seedSource);
        Long intSeed = nextDeterministicSeed(generatorConfig, seedSource);
        Long longSeed = nextDeterministicSeed(generatorConfig, seedSource);
        Long floatSeed = nextDeterministicSeed(generatorConfig, seedSource);
        Long doubleSeed = nextDeterministicSeed(generatorConfig, seedSource);
        Long charSeed = nextDeterministicSeed(generatorConfig, seedSource);
        Long booleanSeed = nextDeterministicSeed(generatorConfig, seedSource);

        Generator<Byte> byteGenerator = byteSeed != null ? new ByteGenerator(Byte.MIN_VALUE, Byte.MAX_VALUE, byteSeed) : new ByteGenerator();
        Generator<Short> shortGenerator = shortSeed != null ? new ShortGenerator(Short.MIN_VALUE, Short.MAX_VALUE, shortSeed) : new ShortGenerator();
        Generator<Integer> intGenerator = intSeed != null ? new IntGenerator(Integer.MIN_VALUE, Integer.MAX_VALUE, intSeed) : new IntGenerator();
        Generator<Long> longGenerator = longSeed != null ? new LongGenerator(Long.MIN_VALUE, Long.MAX_VALUE, longSeed) : new LongGenerator();
        Generator<Float> floatGenerator = floatSeed != null ? new FloatGenerator(0f, 1f, floatSeed) : new FloatGenerator();
        Generator<Double> doubleGenerator = doubleSeed != null ? new DoubleGenerator(0.0, 1.0, doubleSeed) : new DoubleGenerator();
        Generator<Character> charGenerator = buildCharGenerator(charSeed);
        Generator<Boolean> booleanGenerator = booleanSeed != null ? new BooleanGenerator(booleanSeed) : new BooleanGenerator();
        Generator<String> stringGenerator = buildStringGenerator(generatorConfig, nextDeterministicSeed(generatorConfig, seedSource));
        Generator<BigDecimal> bigDecimalGenerator = buildBigDecimalGenerator(nextDeterministicSeed(generatorConfig, seedSource));
        Generator<BigInteger> bigIntegerGenerator = buildBigIntegerGenerator(nextDeterministicSeed(generatorConfig, seedSource));
        Generator<AtomicInteger> atomicIntegerGenerator = () -> new AtomicInteger(intGenerator.generate());
        Generator<AtomicLong> atomicLongGenerator = () -> new AtomicLong(longGenerator.generate());

        m.put(byte.class, byteGenerator);
        m.put(Byte.class, byteGenerator);
        m.put(short.class, shortGenerator);
        m.put(Short.class, shortGenerator);
        m.put(int.class, intGenerator);
        m.put(Integer.class, intGenerator);
        m.put(long.class, longGenerator);
        m.put(Long.class, longGenerator);
        m.put(float.class, floatGenerator);
        m.put(Float.class, floatGenerator);
        m.put(double.class, doubleGenerator);
        m.put(Double.class, doubleGenerator);
        m.put(char.class, charGenerator);
        m.put(Character.class, charGenerator);
        m.put(boolean.class, booleanGenerator);
        m.put(Boolean.class, booleanGenerator);
        m.put(String.class, stringGenerator);
        m.put(BigDecimal.class, bigDecimalGenerator);
        m.put(BigInteger.class, bigIntegerGenerator);
        m.put(AtomicInteger.class, atomicIntegerGenerator);
        m.put(AtomicLong.class, atomicLongGenerator);

        LocalDate lo = cfg.getDateMin() != null ? cfg.getDateMin() : LocalDate.of(1970, 1, 1);
        LocalDate hi = cfg.getDateMax() != null ? cfg.getDateMax() : LocalDate.of(2100, 12, 31);
        Generator<LocalDate> localDateGenerator = buildDateGenerator(generatorConfig, seedSource, lo, hi);
        Generator<LocalTime> localTimeGenerator = new TimeGenerator(derivedGeneratorConfig(generatorConfig, seedSource));
        Generator<LocalDateTime> localDateTimeGenerator = buildLocalDateTimeGenerator(generatorConfig, seedSource, lo, hi);
        Generator<Instant> instantGenerator = buildInstantGenerator(generatorConfig, seedSource, lo, hi);
        Generator<ZonedDateTime> zonedDateTimeGenerator = buildZonedDateTimeGenerator(generatorConfig, seedSource, lo, hi);
        Generator<OffsetDateTime> offsetDateTimeGenerator = () -> zonedDateTimeGenerator.generate().toOffsetDateTime();
        Random offsetTimeRandom = randomFor(generatorConfig, seedSource);
        Generator<OffsetTime> offsetTimeGenerator = () ->
            localTimeGenerator.generate().atOffset(ZoneOffset.ofTotalSeconds(offsetTimeRandom.nextInt(-72, 73) * 15 * 60));
        Random yearRandom = randomFor(generatorConfig, seedSource);
        Generator<Year> yearGenerator = () -> Year.of(yearRandom.nextInt(lo.getYear(), hi.getYear() + 1));
        Generator<YearMonth> yearMonthGenerator = () -> YearMonth.from(localDateGenerator.generate());
        Generator<MonthDay> monthDayGenerator = () -> MonthDay.from(localDateGenerator.generate());
        Random durationRandom = randomFor(generatorConfig, seedSource);
        Generator<Duration> durationGenerator = () -> Duration.ofSeconds(durationRandom.nextLong(0, 10L * 365 * 24 * 60 * 60 + 1));
        Random periodRandom = randomFor(generatorConfig, seedSource);
        Generator<Period> periodGenerator = () -> Period.of(
            periodRandom.nextInt(0, 11),
            periodRandom.nextInt(0, 12),
            periodRandom.nextInt(0, 31));
        Random zoneRandom = randomFor(generatorConfig, seedSource);
        List<String> zoneIds = new ArrayList<>(ZoneId.getAvailableZoneIds());
        zoneIds.sort(String::compareTo);
        Generator<ZoneId> zoneIdGenerator = () -> ZoneId.of(zoneIds.get(zoneRandom.nextInt(zoneIds.size())));
        Random zoneOffsetRandom = randomFor(generatorConfig, seedSource);
        Generator<ZoneOffset> zoneOffsetGenerator = () -> ZoneOffset.ofTotalSeconds(zoneOffsetRandom.nextInt(-72, 73) * 15 * 60);
        Generator<java.util.Date> utilDateGenerator = buildUtilDateGenerator(generatorConfig, seedSource, lo, hi);
        Generator<java.sql.Date> sqlDateGenerator = buildSqlDateGenerator(generatorConfig, seedSource, lo, hi);
        Generator<java.sql.Time> sqlTimeGenerator = new SqlTimeGenerator(derivedGeneratorConfig(generatorConfig, seedSource));
        Generator<java.sql.Timestamp> sqlTimestampGenerator = buildSqlTimestampGenerator(generatorConfig, seedSource, lo, hi);
        Generator<UUID> uuidGenerator = new UUIDGenerator(derivedGeneratorConfig(generatorConfig, seedSource));

        m.put(LocalDate.class, localDateGenerator);
        m.put(LocalTime.class, localTimeGenerator);
        m.put(LocalDateTime.class, localDateTimeGenerator);
        m.put(Instant.class, instantGenerator);
        m.put(ZonedDateTime.class, zonedDateTimeGenerator);
        m.put(OffsetDateTime.class, offsetDateTimeGenerator);
        m.put(OffsetTime.class, offsetTimeGenerator);
        m.put(Year.class, yearGenerator);
        m.put(YearMonth.class, yearMonthGenerator);
        m.put(MonthDay.class, monthDayGenerator);
        m.put(Duration.class, durationGenerator);
        m.put(Period.class, periodGenerator);
        m.put(ZoneId.class, zoneIdGenerator);
        m.put(ZoneOffset.class, zoneOffsetGenerator);
        m.put(java.util.Date.class, utilDateGenerator);
        m.put(java.sql.Date.class, sqlDateGenerator);
        m.put(java.sql.Time.class, sqlTimeGenerator);
        m.put(java.sql.Timestamp.class, sqlTimestampGenerator);
        m.put(UUID.class, uuidGenerator);
        return Collections.unmodifiableMap(m);
    }

    private static Long nextDeterministicSeed(GeneratorConfig config, Random seedSource) {
        return config.getSeed().isPresent() ? seedSource.nextLong() : null;
    }

    private static GeneratorConfig derivedGeneratorConfig(GeneratorConfig config, Random seedSource) {
        Long seed = nextDeterministicSeed(config, seedSource);
        return seed != null ? config.toBuilder().seed(seed).build() : config;
    }

    private static Random randomFor(GeneratorConfig config, Random seedSource) {
        Long seed = nextDeterministicSeed(config, seedSource);
        return seed != null ? new Random(seed) : config.createRandom();
    }

    private static CharGenerator buildCharGenerator(Long seed) {
        CharGenerator.Builder builder = CharGenerator.builder().uppercase().lowercase();
        if (seed != null) {
            builder.seed(seed);
        }
        return builder.build();
    }

    private static StringGenerator buildStringGenerator(GeneratorConfig config, Long seed) {
        StringGenerator.Builder builder = StringGenerator.builder()
                                                        .minLength(config.getMinStringLength())
                                                        .maxLength(config.getMaxStringLength())
                                                        .charGenerator(buildCharGenerator(seed));
        if (seed != null) {
            builder.seed(seed);
        }
        return builder.build();
    }

    private static BigDecimalGenerator buildBigDecimalGenerator(Long seed) {
        return seed != null ? new BigDecimalGenerator(new BigDecimal("0"), new BigDecimal("1000000"), 2, seed)
                            : new BigDecimalGenerator();
    }

    private static BigIntegerGenerator buildBigIntegerGenerator(Long seed) {
        return seed != null ? new BigIntegerGenerator(BigInteger.ZERO, BigInteger.valueOf(Long.MAX_VALUE), seed)
                            : new BigIntegerGenerator();
    }

    private static Generator<LocalDate> buildDateGenerator(GeneratorConfig config,
                                                           Random seedSource,
                                                           LocalDate min,
                                                           LocalDate max) {
        if (min.equals(LocalDate.of(1970, 1, 1)) && max.equals(LocalDate.of(2100, 12, 31))) {
            return new DateGenerator(derivedGeneratorConfig(config, seedSource));
        }
        DateGenerator generator = new DateGenerator(min, max);
        Long seed = nextDeterministicSeed(config, seedSource);
        if (seed != null) {
            generator.reseed(seed);
        }
        return generator;
    }

    private static Generator<LocalDateTime> buildLocalDateTimeGenerator(GeneratorConfig config,
                                                                        Random seedSource,
                                                                        LocalDate min,
                                                                        LocalDate max) {
        if (min.equals(LocalDate.of(1970, 1, 1)) && max.equals(LocalDate.of(2100, 12, 31))) {
            return new LocalDateTimeGenerator(derivedGeneratorConfig(config, seedSource));
        }
        LocalDateTimeGenerator generator = new LocalDateTimeGenerator(min, max);
        Long seed = nextDeterministicSeed(config, seedSource);
        if (seed != null) {
            generator.reseed(seed);
        }
        return generator;
    }

    private static Generator<Instant> buildInstantGenerator(GeneratorConfig config,
                                                            Random seedSource,
                                                            LocalDate min,
                                                            LocalDate max) {
        if (min.equals(LocalDate.of(1970, 1, 1)) && max.equals(LocalDate.of(2100, 12, 31))) {
            return new InstantGenerator(derivedGeneratorConfig(config, seedSource));
        }
        InstantGenerator generator = new InstantGenerator(min, max);
        Long seed = nextDeterministicSeed(config, seedSource);
        if (seed != null) {
            generator.reseed(seed);
        }
        return generator;
    }

    private static Generator<ZonedDateTime> buildZonedDateTimeGenerator(GeneratorConfig config,
                                                                        Random seedSource,
                                                                        LocalDate min,
                                                                        LocalDate max) {
        if (min.equals(LocalDate.of(1970, 1, 1)) && max.equals(LocalDate.of(2100, 12, 31))) {
            return new ZonedDateTimeGenerator(derivedGeneratorConfig(config, seedSource));
        }
        ZonedDateTimeGenerator generator = new ZonedDateTimeGenerator(min, max);
        Long seed = nextDeterministicSeed(config, seedSource);
        if (seed != null) {
            generator.reseed(seed);
        }
        return generator;
    }

    private static Generator<java.util.Date> buildUtilDateGenerator(GeneratorConfig config,
                                                                    Random seedSource,
                                                                    LocalDate min,
                                                                    LocalDate max) {
        if (min.equals(LocalDate.of(1970, 1, 1)) && max.equals(LocalDate.of(2100, 12, 31))) {
            return new UtilDateGenerator(derivedGeneratorConfig(config, seedSource));
        }
        UtilDateGenerator generator = new UtilDateGenerator(min, max);
        Long seed = nextDeterministicSeed(config, seedSource);
        if (seed != null) {
            generator.reseed(seed);
        }
        return generator;
    }

    private static Generator<java.sql.Date> buildSqlDateGenerator(GeneratorConfig config,
                                                                  Random seedSource,
                                                                  LocalDate min,
                                                                  LocalDate max) {
        if (min.equals(LocalDate.of(1970, 1, 1)) && max.equals(LocalDate.of(2100, 12, 31))) {
            return new SqlDateGenerator(derivedGeneratorConfig(config, seedSource));
        }
        SqlDateGenerator generator = new SqlDateGenerator(min, max);
        Long seed = nextDeterministicSeed(config, seedSource);
        if (seed != null) {
            generator.reseed(seed);
        }
        return generator;
    }

    private static Generator<java.sql.Timestamp> buildSqlTimestampGenerator(GeneratorConfig config,
                                                                            Random seedSource,
                                                                            LocalDate min,
                                                                            LocalDate max) {
        if (min.equals(LocalDate.of(1970, 1, 1)) && max.equals(LocalDate.of(2100, 12, 31))) {
            return new SqlTimestampGenerator(derivedGeneratorConfig(config, seedSource));
        }
        SqlTimestampGenerator generator = new SqlTimestampGenerator(min, max);
        Long seed = nextDeterministicSeed(config, seedSource);
        if (seed != null) {
            generator.reseed(seed);
        }
        return generator;
    }

    private static Map<String, Generator<?>> buildSemanticStringGenerators(GeneratorConfig config, Random seedSource) {
        Map<String, Generator<?>> generators = new HashMap<>();
        for (String semanticKey : SEMANTIC_PROVIDER_NAMES_BY_KEY.keySet()) {
            registerSemantic(generators, config, seedSource,
                             derivedConfig -> buildProviderBackedSemanticGenerator(derivedConfig, semanticKey),
                             semanticKey);
        }
        registerSemantic(generators, config, seedSource, FieldGeneratorResolver::newStatusStringGenerator, "status");
        return Collections.unmodifiableMap(generators);
    }

    private static void registerSemantic(Map<String, Generator<?>> generators,
                                         GeneratorConfig config,
                                         Random seedSource,
                                         Function<GeneratorConfig, Generator<?>> factory,
                                         String semanticKey) {
        try {
            generators.put(semanticKey, factory.apply(derivedGeneratorConfig(config, seedSource)));
        } catch (UnsupportedOperationException ignored) {
            // Locale/provider not available — fall back to generic type resolution.
        }
    }

    private static Map<String, Map<Class<?>, Generator<?>>> buildSemanticTypedGenerators(ObjectGeneratorConfig objectConfig,
                                                                                          GeneratorConfig config,
                                                                                          Random seedSource) {
        Map<String, Map<Class<?>, Generator<?>>> generators = new HashMap<>();

        LocalDate today = LocalDate.now();
        registerTemporalSemantic(generators, objectConfig, config, seedSource, "createdat", today.minusYears(10), today);
        registerTemporalSemantic(generators, objectConfig, config, seedSource, "updatedat", today.minusYears(10), today);
        registerTemporalSemantic(generators, objectConfig, config, seedSource, "birthdate", today.minusYears(90), today.minusYears(18));
        Generator<Integer> ageGenerator = intGenerator(nextDeterministicSeed(config, seedSource), 18, 91);
        registerTypedSemantic(generators, "age", int.class, ageGenerator);
        registerTypedSemantic(generators, "age", Integer.class, ageGenerator);
        registerTypedSemantic(generators, "age", long.class, (Generator<Long>) () -> Long.valueOf(ageGenerator.generate()));
        registerTypedSemantic(generators, "age", Long.class, (Generator<Long>) () -> Long.valueOf(ageGenerator.generate()));
        registerTypedSemantic(generators, "age", short.class, (Generator<Short>) () -> Short.valueOf(ageGenerator.generate().shortValue()));
        registerTypedSemantic(generators, "age", Short.class, (Generator<Short>) () -> Short.valueOf(ageGenerator.generate().shortValue()));
        registerTypedSemantic(generators, "age", String.class, (Generator<String>) () -> Integer.toString(ageGenerator.generate()));

        registerNumericSemantic(generators, "amount",
                                bigDecimalGenerator(nextDeterministicSeed(config, seedSource), "0", "1000000", 2),
                                bigIntegerGenerator(nextDeterministicSeed(config, seedSource), 1L, Long.MAX_VALUE),
                                intGenerator(nextDeterministicSeed(config, seedSource), 1, Integer.MAX_VALUE),
                                longGenerator(nextDeterministicSeed(config, seedSource), 1L, Long.MAX_VALUE),
                                doubleGenerator(nextDeterministicSeed(config, seedSource), 0.01, 1000000.0, 2),
                                floatGenerator(nextDeterministicSeed(config, seedSource), 0.01f, 1000000.0f, 2));

        registerNumericSemantic(generators, "balance",
                                bigDecimalGenerator(nextDeterministicSeed(config, seedSource), "0", "10000000", 2),
                                bigIntegerGenerator(nextDeterministicSeed(config, seedSource), 1L, Long.MAX_VALUE),
                                intGenerator(nextDeterministicSeed(config, seedSource), 1, Integer.MAX_VALUE),
                                longGenerator(nextDeterministicSeed(config, seedSource), 1L, Long.MAX_VALUE),
                                doubleGenerator(nextDeterministicSeed(config, seedSource), 0.01, 10000000.0, 2),
                                floatGenerator(nextDeterministicSeed(config, seedSource), 0.01f, 10000000.0f, 2));

        registerNumericSemantic(generators, "price",
                                bigDecimalGenerator(nextDeterministicSeed(config, seedSource), "0", "10000", 2),
                                bigIntegerGenerator(nextDeterministicSeed(config, seedSource), 1L, 10000L),
                                intGenerator(nextDeterministicSeed(config, seedSource), 1, 10000),
                                longGenerator(nextDeterministicSeed(config, seedSource), 1L, 10000L),
                                doubleGenerator(nextDeterministicSeed(config, seedSource), 0.01, 10000.0, 2),
                                floatGenerator(nextDeterministicSeed(config, seedSource), 0.01f, 10000.0f, 2));

        Generator<String> currencyCodeGenerator = buildLocaleCurrencyCodeGenerator(config, seedSource);
        registerTypedSemantic(generators, "currency", String.class, currencyCodeGenerator);
        registerTypedSemantic(generators, "currency", io.github.frikit.krandom.generator.finance.Currency.class,
                              buildLibraryCurrencyGenerator(config, seedSource));
        registerTypedSemantic(generators, "currency", java.util.Currency.class, buildJavaCurrencyGenerator(config, seedSource));

        Generator<Long> stringIdGenerator = longGenerator(nextDeterministicSeed(config, seedSource), 1L, Long.MAX_VALUE);
        registerTypedSemantic(generators, "id", UUID.class, new UUIDGenerator(derivedGeneratorConfig(config, seedSource)));
        registerTypedSemantic(generators, "id", String.class, () -> Long.toString(stringIdGenerator.generate()));
        registerTypedSemantic(generators, "id", BigInteger.class,
                              bigIntegerGenerator(nextDeterministicSeed(config, seedSource), 1L, Long.MAX_VALUE));
        registerTypedSemantic(generators, "id", int.class, intGenerator(nextDeterministicSeed(config, seedSource), 1, Integer.MAX_VALUE));
        registerTypedSemantic(generators, "id", Integer.class, intGenerator(nextDeterministicSeed(config, seedSource), 1, Integer.MAX_VALUE));
        registerTypedSemantic(generators, "id", long.class, longGenerator(nextDeterministicSeed(config, seedSource), 1L, Long.MAX_VALUE));
        registerTypedSemantic(generators, "id", Long.class, longGenerator(nextDeterministicSeed(config, seedSource), 1L, Long.MAX_VALUE));

        Generator<Boolean> activeGenerator = buildActiveGenerator(config, seedSource);
        registerTypedSemantic(generators, "active", boolean.class, activeGenerator);
        registerTypedSemantic(generators, "active", Boolean.class, activeGenerator);

        CoordinatesGenerator coordinatesGenerator = new CoordinatesGenerator(derivedGeneratorConfig(config, seedSource));
        registerTypedSemantic(generators, "latitude", double.class, (Generator<Double>) coordinatesGenerator::generateLatitude);
        registerTypedSemantic(generators, "latitude", Double.class, (Generator<Double>) coordinatesGenerator::generateLatitude);
        registerTypedSemantic(generators, "latitude", float.class, (Generator<Float>) () -> (float) coordinatesGenerator.generateLatitude());
        registerTypedSemantic(generators, "latitude", Float.class, (Generator<Float>) () -> (float) coordinatesGenerator.generateLatitude());
        registerTypedSemantic(generators, "latitude", BigDecimal.class,
                              () -> BigDecimal.valueOf(coordinatesGenerator.generateLatitude())
                                              .setScale(6, java.math.RoundingMode.HALF_UP));

        registerTypedSemantic(generators, "longitude", double.class, (Generator<Double>) coordinatesGenerator::generateLongitude);
        registerTypedSemantic(generators, "longitude", Double.class, (Generator<Double>) coordinatesGenerator::generateLongitude);
        registerTypedSemantic(generators, "longitude", float.class, (Generator<Float>) () -> (float) coordinatesGenerator.generateLongitude());
        registerTypedSemantic(generators, "longitude", Float.class, (Generator<Float>) () -> (float) coordinatesGenerator.generateLongitude());
        registerTypedSemantic(generators, "longitude", BigDecimal.class,
                              () -> BigDecimal.valueOf(coordinatesGenerator.generateLongitude())
                                              .setScale(6, java.math.RoundingMode.HALF_UP));

        Map<String, Map<Class<?>, Generator<?>>> unmodifiable = new HashMap<>(generators.size());
        for (Map.Entry<String, Map<Class<?>, Generator<?>>> entry : generators.entrySet()) {
            unmodifiable.put(entry.getKey(), Collections.unmodifiableMap(new HashMap<>(entry.getValue())));
        }
        return Collections.unmodifiableMap(unmodifiable);
    }

    private static void registerTemporalSemantic(Map<String, Map<Class<?>, Generator<?>>> generators,
                                                 ObjectGeneratorConfig objectConfig,
                                                 GeneratorConfig config,
                                                 Random seedSource,
                                                 String semanticKey,
                                                 LocalDate min,
                                                 LocalDate max) {
        LocalDate effectiveMin = objectConfig.getDateMin() != null ? objectConfig.getDateMin() : min;
        LocalDate effectiveMax = objectConfig.getDateMax() != null ? objectConfig.getDateMax() : max;
        registerTypedSemantic(generators, semanticKey, LocalDate.class,
                              buildDateGenerator(config, seedSource, effectiveMin, effectiveMax));
        registerTypedSemantic(generators, semanticKey, LocalDateTime.class,
                              buildLocalDateTimeGenerator(config, seedSource, effectiveMin, effectiveMax));
        registerTypedSemantic(generators, semanticKey, Instant.class,
                              buildInstantGenerator(config, seedSource, effectiveMin, effectiveMax));
        Generator<ZonedDateTime> zonedGenerator = buildZonedDateTimeGenerator(config, seedSource, effectiveMin, effectiveMax);
        registerTypedSemantic(generators, semanticKey, ZonedDateTime.class, zonedGenerator);
        registerTypedSemantic(generators, semanticKey, OffsetDateTime.class,
                              (Generator<OffsetDateTime>) () -> zonedGenerator.generate().toOffsetDateTime());
        registerTypedSemantic(generators, semanticKey, java.util.Date.class,
                              buildUtilDateGenerator(config, seedSource, effectiveMin, effectiveMax));
        registerTypedSemantic(generators, semanticKey, java.sql.Date.class,
                              buildSqlDateGenerator(config, seedSource, effectiveMin, effectiveMax));
        registerTypedSemantic(generators, semanticKey, java.sql.Timestamp.class,
                              buildSqlTimestampGenerator(config, seedSource, effectiveMin, effectiveMax));
    }

    private static void registerNumericSemantic(Map<String, Map<Class<?>, Generator<?>>> generators,
                                                String semanticKey,
                                                Generator<BigDecimal> bigDecimalGenerator,
                                                Generator<BigInteger> bigIntegerGenerator,
                                                Generator<Integer> intGenerator,
                                                Generator<Long> longGenerator,
                                                Generator<Double> doubleGenerator,
                                                Generator<Float> floatGenerator) {
        registerTypedSemantic(generators, semanticKey, BigDecimal.class, bigDecimalGenerator);
        registerTypedSemantic(generators, semanticKey, BigInteger.class, bigIntegerGenerator);
        registerTypedSemantic(generators, semanticKey, int.class, intGenerator);
        registerTypedSemantic(generators, semanticKey, Integer.class, intGenerator);
        registerTypedSemantic(generators, semanticKey, long.class, longGenerator);
        registerTypedSemantic(generators, semanticKey, Long.class, longGenerator);
        registerTypedSemantic(generators, semanticKey, double.class, doubleGenerator);
        registerTypedSemantic(generators, semanticKey, Double.class, doubleGenerator);
        registerTypedSemantic(generators, semanticKey, float.class, floatGenerator);
        registerTypedSemantic(generators, semanticKey, Float.class, floatGenerator);
    }

    private static void registerTypedSemantic(Map<String, Map<Class<?>, Generator<?>>> generators,
                                              String semanticKey,
                                              Class<?> rawType,
                                              Generator<?> generator) {
        generators.computeIfAbsent(semanticKey, ignored -> new HashMap<>()).put(rawType, generator);
    }

    private static Generator<?> newStatusStringGenerator(GeneratorConfig config) {
        List<String> values = List.of("ACTIVE", "INACTIVE", "PENDING", "SUSPENDED", "ENABLED", "DISABLED");
        Random random = config.createRandom();
        return () -> values.get(random.nextInt(values.size()));
    }

    private static Generator<?> buildProviderBackedSemanticGenerator(GeneratorConfig config, String semanticKey) {
        ProviderHub hub = new ProviderHub(config);
        String providerName = Objects.requireNonNull(semanticProviderNameFor(semanticKey),
                                                     "No provider mapping for semantic key: " + semanticKey);
        Generator<?> provider = hub.get(providerName, Generator.class);
        if ("uuid".equals(semanticKey)) {
            return () -> provider.generate().toString();
        }
        return provider;
    }

    private static Generator<BigDecimal> bigDecimalGenerator(Long seed, String min, String max, int scale) {
        return seed != null
               ? new BigDecimalGenerator(new BigDecimal(min), new BigDecimal(max), scale, seed)
               : new BigDecimalGenerator(new BigDecimal(min), new BigDecimal(max), scale);
    }

    private static Generator<BigInteger> bigIntegerGenerator(Long seed, long min, long maxExclusive) {
        BigInteger lower = BigInteger.valueOf(min);
        BigInteger upper = BigInteger.valueOf(Math.max(min + 1, maxExclusive));
        return seed != null ? new BigIntegerGenerator(lower, upper, seed) : new BigIntegerGenerator(lower, upper);
    }

    private static Generator<Integer> intGenerator(Long seed, int min, int maxExclusive) {
        return seed != null ? new IntGenerator(min, maxExclusive, seed) : new IntGenerator(min, maxExclusive);
    }

    private static Generator<Long> longGenerator(Long seed, long min, long maxExclusive) {
        return seed != null ? new LongGenerator(min, maxExclusive, seed) : new LongGenerator(min, maxExclusive);
    }

    private static Generator<Double> doubleGenerator(Long seed, double min, double max, int precision) {
        DoubleGenerator generator = seed != null ? new DoubleGenerator(min, max, seed) : new DoubleGenerator(min, max);
        return () -> round(generator.generate(), precision);
    }

    private static Generator<Float> floatGenerator(Long seed, float min, float max, int precision) {
        FloatGenerator generator = seed != null ? new FloatGenerator(min, max, seed) : new FloatGenerator(min, max);
        return () -> (float) round(generator.generate(), precision);
    }

    private static double round(double value, int precision) {
        return BigDecimal.valueOf(value).setScale(precision, java.math.RoundingMode.HALF_UP).doubleValue();
    }

    private static Generator<Boolean> buildActiveGenerator(GeneratorConfig config, Random seedSource) {
        Random random = randomFor(config, seedSource);
        return () -> random.nextDouble() < 0.85d;
    }

    private static Generator<String> buildLocaleCurrencyCodeGenerator(GeneratorConfig config, Random seedSource) {
        CurrencyGenerator generator =
            new ProviderHub(derivedGeneratorConfig(config, seedSource)).get("finance.currency", CurrencyGenerator.class);
        return () -> generator.generateCurrencyIsoCode(config.getLocale());
    }

    private static Generator<io.github.frikit.krandom.generator.finance.Currency> buildLibraryCurrencyGenerator(GeneratorConfig config,
                                                                                                           Random seedSource) {
        Generator<String> codeGenerator = buildLocaleCurrencyCodeGenerator(config, seedSource);
        return () -> {
            io.github.frikit.krandom.generator.finance.Currency localeCurrency =
                io.github.frikit.krandom.generator.finance.Currency.forLocale(config.getLocale());
            return localeCurrency != null ? localeCurrency
                                          : io.github.frikit.krandom.generator.finance.Currency.fromCode(codeGenerator.generate());
        };
    }

    private static Generator<java.util.Currency> buildJavaCurrencyGenerator(GeneratorConfig config, Random seedSource) {
        Generator<String> codeGenerator = buildLocaleCurrencyCodeGenerator(config, seedSource);
        return () -> java.util.Currency.getInstance(codeGenerator.generate());
    }

    private static Map<String, String> buildSemanticKeysByAlias() {
        Map<String, String> aliases = new HashMap<>();
        registerSemanticAliases(aliases, "firstname", "firstname", "givenname");
        registerSemanticAliases(aliases, "lastname", "lastname", "surname", "familyname");
        registerSemanticAliases(aliases, "fullname", "fullname", "displayname");
        registerSemanticAliases(aliases, "email", "email", "emailaddress");
        registerSemanticAliases(aliases, "username", "username", "userhandle");
        registerSemanticAliases(aliases, "phone", "phone", "phonenumber", "mobile", "mobilephone", "telephone");
        registerSemanticAliases(aliases, "streetaddress", "street", "streetaddress", "addressline1");
        registerSemanticAliases(aliases, "city", "city", "town");
        registerSemanticAliases(aliases, "state", "state", "province", "region");
        registerSemanticAliases(aliases, "postalcode", "postalcode", "postcode", "zipcode", "zip");
        registerSemanticAliases(aliases, "country", "country", "countryname");
        registerSemanticAliases(aliases, "companyname", "company", "companyname", "organization", "organisation");
        registerSemanticAliases(aliases, "industry", "industry", "sector");
        registerSemanticAliases(aliases, "companyemail", "companyemail", "businessemail", "corporateemail");
        registerSemanticAliases(aliases, "companyurl", "companyurl", "companywebsite", "businesswebsite");
        registerSemanticAliases(aliases, "password", "password", "passcode");
        registerSemanticAliases(aliases, "url", "url", "website", "homepage", "link");
        registerSemanticAliases(aliases, "domain", "domain", "hostname");
        registerSemanticAliases(aliases, "currency", "currency", "currencycode");
        registerSemanticAliases(aliases, "uuid", "uuid", "guid");
        registerSemanticAliases(aliases, "createdat", "createdat", "createdon", "createddate", "createdtimestamp", "creationdate");
        registerSemanticAliases(aliases, "updatedat", "updatedat", "updatedon", "updateddate", "updatedtimestamp", "lastupdated", "modifiedat", "modifiedon");
        registerSemanticAliases(aliases, "birthdate", "birthdate", "dateofbirth", "dob", "birthday", "bornon");
        registerSemanticAliases(aliases, "age", "age", "ageyears", "yearsold");
        registerSemanticAliases(aliases, "amount", "amount", "totalamount", "subtotal", "paymentamount");
        registerSemanticAliases(aliases, "balance", "balance", "accountbalance", "currentbalance");
        registerSemanticAliases(aliases, "price", "price", "unitprice", "cost");
        registerSemanticAliases(aliases, "id", "id", "userid", "accountid", "orderid", "customerid", "productid", "geoid", "identifier");
        registerSemanticAliases(aliases, "active", "active", "isactive", "enabled", "isenabled");
        registerSemanticAliases(aliases, "status", "status", "accountstatus", "orderstatus");
        registerSemanticAliases(aliases, "latitude", "latitude", "lat");
        registerSemanticAliases(aliases, "longitude", "longitude", "lon", "lng");
        return Collections.unmodifiableMap(aliases);
    }

    private static Map<String, Set<String>> buildSemanticAliasesByKey() {
        Map<String, Set<String>> aliasesByKey = new HashMap<>();
        for (Map.Entry<String, String> entry : SEMANTIC_KEYS_BY_ALIAS.entrySet()) {
            aliasesByKey.computeIfAbsent(entry.getValue(), ignored -> new LinkedHashSet<>()).add(entry.getKey());
        }
        Map<String, Set<String>> unmodifiable = new HashMap<>(aliasesByKey.size());
        for (Map.Entry<String, Set<String>> entry : aliasesByKey.entrySet()) {
            unmodifiable.put(entry.getKey(), Collections.unmodifiableSet(new LinkedHashSet<>(entry.getValue())));
        }
        return Collections.unmodifiableMap(unmodifiable);
    }

    private static Map<String, String> buildSemanticProviderNames() {
        Map<String, String> providerNames = new LinkedHashMap<>();
        providerNames.put("firstname", "person.first_name");
        providerNames.put("lastname", "person.last_name");
        providerNames.put("fullname", "person.full_name");
        providerNames.put("email", "person.email");
        providerNames.put("username", "person.username");
        providerNames.put("phone", "address.phone_number");
        providerNames.put("streetaddress", "address.street_address");
        providerNames.put("city", "address.city");
        providerNames.put("state", "address.state");
        providerNames.put("postalcode", "address.postal_code");
        providerNames.put("country", "address.country");
        providerNames.put("companyname", "company.name");
        providerNames.put("industry", "company.industry");
        providerNames.put("companyemail", "company.email");
        providerNames.put("companyurl", "company.url");
        providerNames.put("password", "security.password");
        providerNames.put("url", "internet.url");
        providerNames.put("domain", "internet.domain");
        providerNames.put("currency", "finance.currency");
        providerNames.put("uuid", "code.uuid");
        return Collections.unmodifiableMap(providerNames);
    }

    private static void registerSemanticAliases(Map<String, String> aliases, String semanticKey, String... fieldNames) {
        for (String fieldName : fieldNames) {
            aliases.put(normalizeSemanticFieldName(fieldName), semanticKey);
        }
    }

    static String normalizeSemanticFieldName(String fieldName) {
        StringBuilder normalized = new StringBuilder(fieldName.length());
        for (int i = 0; i < fieldName.length(); i++) {
            char ch = fieldName.charAt(i);
            if (Character.isLetterOrDigit(ch)) {
                normalized.append(Character.toLowerCase(ch));
            }
        }
        return normalized.toString();
    }

    static String semanticKeyForFieldName(String fieldName) {
        return SEMANTIC_KEYS_BY_ALIAS.get(normalizeSemanticFieldName(fieldName));
    }

    static Set<String> semanticAliasesFor(String semanticKey) {
        return SEMANTIC_ALIASES_BY_KEY.getOrDefault(normalizeSemanticFieldName(semanticKey), Set.of());
    }

    static String semanticProviderNameFor(String semanticKey) {
        String normalized = normalizeSemanticFieldName(semanticKey);
        String canonicalKey = SEMANTIC_KEYS_BY_ALIAS.getOrDefault(normalized, normalized);
        return SEMANTIC_PROVIDER_NAMES_BY_KEY.get(canonicalKey);
    }

    private Generator<?> semanticGeneratorFor(Class<?> rawType, String fieldName) {
        if (semanticMode == ObjectGenerationSemanticMode.STRUCTURAL_ONLY) {
            return null;
        }
        String semanticKey = semanticKeyForFieldName(fieldName);
        if (semanticKey == null) {
            return null;
        }
        if (rawType.isEnum() && "status".equals(semanticKey)) {
            Generator<?> enumGenerator = semanticStatusEnumGenerator(rawType);
            if (enumGenerator != null) {
                return enumGenerator;
            }
        }
        Map<Class<?>, Generator<?>> typedGenerators = semanticTypedGenerators.get(semanticKey);
        if (typedGenerators != null) {
            Generator<?> typedGenerator = typedGenerators.get(rawType);
            if (typedGenerator != null) {
                return typedGenerator;
            }
        }
        if (rawType != String.class) {
            return null;
        }
        return semanticStringGenerators.get(semanticKey);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private Generator<?> semanticStatusEnumGenerator(Class<?> rawType) {
        Object[] constants = rawType.getEnumConstants();
        if (constants == null || constants.length == 0) {
            return null;
        }
        List<Enum> preferred = new ArrayList<>();
        for (Object constant : constants) {
            Enum<?> enumConstant = (Enum<?>) constant;
            String name = normalizeSemanticFieldName(enumConstant.name());
            if (Set.of("active", "inactive", "pending", "suspended", "enabled", "disabled",
                       "open", "closed", "archived", "deleted").contains(name)) {
                preferred.add((Enum) enumConstant);
            }
        }
        if (preferred.isEmpty()) {
            return null;
        }
        Random random = randomFor(generatorConfig, sequenceRandom);
        return () -> preferred.get(random.nextInt(preferred.size()));
    }

    private Object generateWithUniqueness(String fieldName, Generator<?> generator) {
        return generateWithUniqueness(fieldName, null, generator);
    }

    private Object generateWithUniqueness(String fieldName, String semanticKey, Generator<?> generator) {
        String normalizedFieldName = normalizeSemanticFieldName(fieldName);
        if (!isUniqueField(normalizedFieldName, semanticKey)) {
            return generator.generate();
        }
        return uniqueFieldTracker.nextUnique(
            semanticKey != null ? semanticKey : normalizedFieldName,
            generator::generate,
            config.getUniquenessMaxAttempts());
    }

    private boolean isUniqueField(String normalizedFieldName, String semanticKey) {
        if (uniqueFieldNames.contains(normalizedFieldName)) {
            return true;
        }
        if (semanticKey == null) {
            return false;
        }
        if (uniqueFieldNames.contains(semanticKey)) {
            return true;
        }
        for (String alias : semanticAliasesFor(semanticKey)) {
            if (uniqueFieldNames.contains(alias)) {
                return true;
            }
        }
        return false;
    }

    private boolean shouldReturnNull(AnnotatedElement element, Class<?> rawType, Generator<?> annotationGenerator, Generator<?> bvGen) {
        if (element == null || rawType.isPrimitive() || rawType == Optional.class) {
            return false;
        }
        if (annotationGenerator != null || bvGen != null) {
            return false;
        }
        double probability = config.getNullProbability();
        return probability > 0.0 && sequenceRandom.nextDouble() < probability;
    }

    private boolean shouldReturnEmptyOptional(AnnotatedElement element) {
        if (element == null) {
            return false;
        }
        double probability = config.getOptionalEmptyProbability();
        return probability > 0.0 && sequenceRandom.nextDouble() < probability;
    }

    // ── Primary entry point ───────────────────────────────────────────────────

    /**
     * Extracts the {@code idx}-th type argument from a {@link ParameterizedType}.
     * Returns {@code Object.class} when the type is raw or the argument is not a plain class.
     */
    private static Class<?> typeArg(Type t, int idx) {
        if (t instanceof ParameterizedType pt) {
            var arg = pt.getActualTypeArguments();
            if (arg[idx] instanceof Class<?> c) return c;
        }
        return Object.class; // raw or erased — resolveAndGenerate handles Object gracefully
    }

    private static Set<Object> toSetType(Class<?> rawType, List<Object> values) {
        if (rawType == TreeSet.class
            || rawType == SortedSet.class
            || rawType == NavigableSet.class) {
            Set<Object> set = new TreeSet<>(Comparator.comparing(String::valueOf));
            set.addAll(values);
            return set;
        }
        if (rawType == Set.class) {
            return new LinkedHashSet<>(values);
        }
        Set<Object> concrete = instantiateCollectionType(rawType, Set.class);
        if (concrete != null && addAllSafely(concrete, values)) {
            return concrete;
        }
        if (rawType.isInterface() || java.lang.reflect.Modifier.isAbstract(rawType.getModifiers())) {
            return new LinkedHashSet<>(values);
        }
        return null;
    }

    // ── Array generation ──────────────────────────────────────────────────────

    private static List<Object> toListType(Class<?> rawType, List<Object> values) {
        if (rawType == List.class) {
            return Collections.unmodifiableList(values);
        }
        if (rawType == LinkedList.class) {
            return new LinkedList<>(values);
        }
        if (rawType == ArrayList.class) {
            return new ArrayList<>(values);
        }
        if (rawType == Vector.class) {
            return new Vector<>(values);
        }
        if (rawType == Stack.class) {
            Stack<Object> stack = new Stack<>();
            stack.addAll(values);
            return stack;
        }
        if (rawType == CopyOnWriteArrayList.class) {
            return new CopyOnWriteArrayList<>(values);
        }
        List<Object> concrete = instantiateCollectionType(rawType, List.class);
        if (concrete != null && addAllSafely(concrete, values)) {
            return concrete;
        }
        if (rawType.isInterface() || java.lang.reflect.Modifier.isAbstract(rawType.getModifiers())) {
            return new ArrayList<>(values);
        }
        // Unknown concrete List subtype with no usable constructor — returning null preserves
        // assignability and avoids reflective assignment failures in ObjectGenerator.
        return null;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static Queue<Object> toQueueType(Class<?> rawType, List<Object> values) {
        Queue<Object> concrete = instantiateCollectionType(rawType, Queue.class);
        if (concrete != null && addAllSafely(concrete, values)) {
            return concrete;
        }
        if (rawType == PriorityQueue.class) {
            Queue<Object> queue = new PriorityQueue<>(Comparator.comparing(String::valueOf));
            queue.addAll(values);
            return queue;
        }
        if (rawType.isInterface() || java.lang.reflect.Modifier.isAbstract(rawType.getModifiers())) {
            Queue<Object> queue = new java.util.ArrayDeque<>();
            queue.addAll(values);
            return queue;
        }
        return null;
    }

    private static Map<Object, Object> toMapType(Class<?> rawType) {
        if (rawType == TreeMap.class
            || rawType == SortedMap.class
            || rawType == NavigableMap.class) {
            return new TreeMap<>(Comparator.comparing(String::valueOf));
        }
        Map<Object, Object> concrete = instantiateCollectionType(rawType, Map.class);
        if (concrete != null) {
            return concrete;
        }
        if (rawType.isInterface() || java.lang.reflect.Modifier.isAbstract(rawType.getModifiers())) {
            return new LinkedHashMap<>();
        }
        // Unknown concrete map subtype with no usable constructor.
        return null;
    }

    @SuppressWarnings("unchecked")
    private static <T> T instantiateCollectionType(Class<?> rawType, Class<T> expectedType) {
        if (!expectedType.isAssignableFrom(rawType)) {
            return null;
        }
        if (rawType.isInterface() || java.lang.reflect.Modifier.isAbstract(rawType.getModifiers())) {
            return null;
        }
        try {
            Constructor<?> ctor = rawType.getDeclaredConstructor();
            ctor.setAccessible(true);
            return (T) ctor.newInstance();
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            return null;
        }
    }

    private static boolean addAllSafely(Collection<Object> target, List<Object> values) {
        try {
            target.addAll(values);
            return true;
        } catch (RuntimeException ignored) {
            if (target instanceof Queue<?>) {
                try {
                    target.clear();
                    for (Object value : values) {
                        if (value != null) {
                            target.add(value);
                        }
                    }
                    return true;
                } catch (RuntimeException ignoredAgain) {
                    return false;
                }
            }
            return false;
        }
    }

    private static void putSafely(Map<Object, Object> target, Object key, Object value) {
        try {
            target.put(key, value);
        } catch (RuntimeException ignored) {
            // Keep generation resilient for custom maps with stricter insertion rules.
        }
    }

    private Generator<?> fakeAnnotationGeneratorFor(AnnotatedElement element, Class<?> rawType) {
        Fake annotation = element.getAnnotation(Fake.class);
        if (annotation == null) return null;
        String key = normalizeSemanticFieldName(annotation.value());
        String canonicalKey = SEMANTIC_KEYS_BY_ALIAS.getOrDefault(key, key);
        // Try typed generators first (for non-String types)
        Map<Class<?>, Generator<?>> typedGenerators = semanticTypedGenerators.get(canonicalKey);
        if (typedGenerators != null) {
            Generator<?> typedGen = typedGenerators.get(rawType);
            if (typedGen != null) return typedGen;
        }
        // Fall back to string generator
        if (rawType == String.class) {
            Generator<?> stringGen = semanticStringGenerators.get(canonicalKey);
            if (stringGen != null) return stringGen;
        }
        return null;
    }

    private Generator<?> fakeRangeGeneratorFor(AnnotatedElement element, Class<?> rawType) {
        FakeRange annotation = element.getAnnotation(FakeRange.class);
        if (annotation == null) return null;
        long min = annotation.min();
        long max = annotation.max();
        Long seed = nextDeterministicSeed(generatorConfig, sequenceRandom);
        if (rawType == int.class || rawType == Integer.class) {
            return seed != null ? new IntGenerator((int) min, (int) max, seed)
                                : new IntGenerator((int) min, (int) max);
        }
        if (rawType == long.class || rawType == Long.class) {
            return seed != null ? new LongGenerator(min, max, seed)
                                : new LongGenerator(min, max);
        }
        if (rawType == double.class || rawType == Double.class) {
            return seed != null ? new DoubleGenerator(min, max, seed)
                                : new DoubleGenerator(min, max);
        }
        if (rawType == float.class || rawType == Float.class) {
            return seed != null ? new FloatGenerator(min, max, seed)
                                : new FloatGenerator(min, max);
        }
        if (rawType == short.class || rawType == Short.class) {
            return seed != null ? new ShortGenerator((short) min, (short) max, seed)
                                : new ShortGenerator((short) min, (short) max);
        }
        if (rawType == byte.class || rawType == Byte.class) {
            return seed != null ? new ByteGenerator((byte) min, (byte) max, seed)
                                : new ByteGenerator((byte) min, (byte) max);
        }
        return null;
    }

    private static Generator<?> annotationRandomizerFor(AnnotatedElement element) {
        Randomizer annotation = element.getAnnotation(Randomizer.class);
        if (annotation == null) return null;
        Class<? extends Generator<?>> generatorType = annotation.value();
        try {
            RandomizerArgument[] args = element.getAnnotationsByType(RandomizerArgument.class);
            Class<?>[] parameterTypes = new Class<?>[args.length];
            Object[] parameterValues = new Object[args.length];
            for (int i = 0; i < args.length; i++) {
                parameterTypes[i] = args[i].type();
                parameterValues[i] = convertArgumentValue(args[i].value(), parameterTypes[i]);
            }
            Constructor<? extends Generator<?>> ctor = generatorType.getDeclaredConstructor(parameterTypes);
            ctor.setAccessible(true);
            return ctor.newInstance(parameterValues);
        } catch (ReflectiveOperationException | RuntimeException e) {
            throw new ObjectGenerationException(
                "Failed to instantiate @" + Randomizer.class.getSimpleName()
                + " generator: " + generatorType.getName(), e);
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static Object convertArgumentValue(String rawValue, Class<?> type) {
        if (type.isArray()) {
            return convertArrayArgument(rawValue, type);
        }
        Function<String, Object> parser = ARGUMENT_PARSERS.get(type);
        if (parser != null) return parser.apply(rawValue);
        if (type.isEnum()) {
            return Enum.valueOf((Class<? extends Enum>) type.asSubclass(Enum.class), rawValue);
        }
        throw new IllegalArgumentException("Unsupported @" + RandomizerArgument.class.getSimpleName()
                                           + " type: " + type.getName());
    }

    private static Object convertArrayArgument(String rawValue, Class<?> arrayType) {
        Class<?> componentType = arrayType.getComponentType();
        String[] parts = rawValue.split(",", -1);
        int length = parts.length;
        while (length > 0 && parts[length - 1].isEmpty()) {
            length--;
        }

        Object array = Array.newInstance(componentType, length);
        for (int i = 0; i < length; i++) {
            Array.set(array, i, convertArgumentValue(parts[i].trim(), componentType));
        }
        return array;
    }

    private static java.util.Date parseUtilDate(String rawValue) {
        LocalDateTime dateTime = LocalDateTime.parse(rawValue, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        return java.util.Date.from(dateTime.toInstant(ZoneOffset.UTC));
    }

    private static Character parseChar(String rawValue) {
        if (rawValue.length() != 1) {
            throw new IllegalArgumentException("Expected single character value but got: " + rawValue);
        }
        return rawValue.charAt(0);
    }

    /**
     * Resolve and generate a value for a field whose generic type is known.
     *
     * @param genericType  the full generic type (e.g. {@code List<String>}) for collection resolution
     * @param rawType      the erasure of {@code genericType}
     * @param fieldName    name of the field (used for field-level override lookup)
     * @param ownerType    class that declares the field
     * @param currentDepth nesting depth of the parent {@link ObjectGenerator} (0 = root)
     * @param element      the annotated element (field or record component) for BV constraint lookup;
     *                     {@code null} for synthetic recursive calls (collection elements etc.)
     * @return generated value, or a safe default / {@code null} when the type is unsupported
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    Object resolveAndGenerate(Type genericType, Class<?> rawType,
                              String fieldName, Class<?> ownerType,
                              int currentDepth, AnnotatedElement element) {

        // ── 0a. Contextual field-level override ───────────────────────────────
        var ctxField = config.getContextualFieldOverride(ownerType, fieldName);
        if (ctxField.isPresent()) {
            return ctxField.get().generate(new GenerationContext(fieldName, ownerType, currentDepth));
        }

        // ── 0aa. Contextual predicate field override ─────────────────────────
        if (element instanceof java.lang.reflect.Field field) {
            var ctxPredicateField = config.getContextualFieldPredicateOverride(field);
            if (ctxPredicateField.isPresent()) {
                return ctxPredicateField.get().generate(new GenerationContext(fieldName, ownerType, currentDepth));
            }
        }

        // ── 0b. Contextual type-level override ────────────────────────────────
        var ctxType = config.getContextualTypeOverride(rawType);
        if (ctxType.isPresent()) {
            return ctxType.get().generate(new GenerationContext(fieldName, ownerType, currentDepth));
        }

        // ── 1. Field-level override ───────────────────────────────────────────
        var fieldOverride = config.getFieldOverride(ownerType, fieldName);
        if (fieldOverride.isPresent()) {
            return fieldOverride.get().generate();
        }

        // ── 1a. Predicate field-level override ───────────────────────────────
        if (element instanceof java.lang.reflect.Field field) {
            var predicateFieldOverride = config.getFieldPredicateOverride(field);
            if (predicateFieldOverride.isPresent()) {
                return predicateFieldOverride.get().generate();
            }
        }

        // ── 2. Type-level override ────────────────────────────────────────────
        var typeOverride = config.getTypeOverride(rawType);
        if (typeOverride.isPresent()) {
            return typeOverride.get().generate();
        }

        Generator<?> annotationGenerator = element != null ? annotationRandomizerFor(element) : null;
        Generator<?> fakeAnnotationGenerator = element != null ? fakeAnnotationGeneratorFor(element, rawType) : null;
        Generator<?> fakeRangeGenerator = element != null ? fakeRangeGeneratorFor(element, rawType) : null;
        Generator<?> bvGen = element != null ? BeanValidationSupport.constraintGeneratorFor(element, rawType) : null;

        // ── 3a. Semantic field-name resolver ─────────────────────────────────
        String semanticKey = semanticKeyForFieldName(fieldName);
        Generator<?> semanticGenerator = semanticGeneratorFor(rawType, fieldName);
        if (semanticGenerator != null
            && (semanticMode == ObjectGenerationSemanticMode.STRICT
                || (annotationGenerator == null && fakeAnnotationGenerator == null
                    && fakeRangeGenerator == null && bvGen == null))) {
            return generateWithUniqueness(fieldName, semanticKey, semanticGenerator);
        }

        // ── 3aa. Configured null/optional behavior ────────────────────────────
        if (Optional.class == rawType) {
            if (shouldReturnEmptyOptional(element)) {
                return Optional.empty();
            }
            Class<?> valueType = typeArg(genericType, 0);
            Object value = resolveAndGenerate(valueType, valueType, fieldName + ".value", ownerType, currentDepth, null);
            return Optional.ofNullable(value);
        }
        if (shouldReturnNull(element, rawType, annotationGenerator, bvGen)) {
            return null;
        }

        // ── 3b. Declarative @Randomizer override ─────────────────────────────
        if (annotationGenerator != null) {
            return annotationGenerator.generate();
        }

        // ── 3ba. Declarative @Fake override ──────────────────────────────────
        if (fakeAnnotationGenerator != null) {
            return fakeAnnotationGenerator.generate();
        }

        // ── 3bb. Declarative @FakeRange override ─────────────────────────────
        if (fakeRangeGenerator != null) {
            return fakeRangeGenerator.generate();
        }

        // ── 3c. Bean Validation constraint override ───────────────────────────
        if (bvGen != null) {
            return bvGen.generate();
        }

        // ── 4. Built-in (primitives, wrappers, String, JSR-310, UUID, BigDecimal, BigInteger) ──
        var builtin = builtins.get(rawType);
        if (builtin != null) {
            return generateWithUniqueness(fieldName, builtin);
        }

        // ── 5. Enum ───────────────────────────────────────────────────────────
        if (rawType.isEnum()) {
            Object[] constants = rawType.getEnumConstants();
            if (constants.length == 0) return null;
            Long enumSeed = nextDeterministicSeed(generatorConfig, sequenceRandom);
            return generateWithUniqueness(fieldName, new EnumGenerator((Class<? extends Enum>) rawType, enumSeed));
        }

        // ── 6a. Array ─────────────────────────────────────────────────────────
        if (rawType.isArray()) {
            return generateArray(rawType, ownerType, fieldName, currentDepth);
        }

        // ── 6b. List / Set ────────────────────────────────────────────────────
        if (List.class.isAssignableFrom(rawType)
            || Set.class.isAssignableFrom(rawType)
            || Queue.class.isAssignableFrom(rawType)) {
            Class<?> elem = typeArg(genericType, 0);
            int elementCount = nextCollectionSize();
            List<Object> els = new ArrayList<>(elementCount);
            for (int i = 0; i < elementCount; i++) {
                els.add(resolveAndGenerate(elem, elem, fieldName + "[]", ownerType, currentDepth, null));
            }
            if (List.class.isAssignableFrom(rawType)) {
                return toListType(rawType, els);
            }
            if (Queue.class.isAssignableFrom(rawType)) {
                return toQueueType(rawType, els);
            }
            return toSetType(rawType, els);
        }

        // ── 6c. Map ───────────────────────────────────────────────────────────
        if (Map.class.isAssignableFrom(rawType)) {
            Class<?> k = typeArg(genericType, 0);
            Class<?> v = typeArg(genericType, 1);
            Map<Object, Object> map = toMapType(rawType);
            if (map == null) {
                return null;
            }
            int elementCount = nextCollectionSize();
            for (int i = 0; i < elementCount; i++) {
                Object key = resolveAndGenerate(k, k, fieldName + ".key", ownerType, currentDepth, null);
                Object val = resolveAndGenerate(v, v, fieldName + ".val", ownerType, currentDepth, null);
                if (key != null) {
                    putSafely(map, key, val);
                }
            }
            if (rawType == Map.class) {
                return Collections.unmodifiableMap(map);
            }
            return map;
        }

        // ── 7. Depth guard ────────────────────────────────────────────────────
        if (currentDepth >= config.getMaxDepth()) {
            return PRIMITIVE_DEFAULTS.getOrDefault(rawType, null);
        }

        // ── 8. Nested class or record (cycle-safe) ────────────────────────────
        if (isNestableType(rawType)) {
            if (pool.isInProgress(rawType)) {
                return pool.getCached(rawType); // break circular reference
            }
            pool.begin(rawType);
            try {
                Object instance = new ObjectGenerator<>(
                    rawType,
                    config,
                    currentDepth + 1,
                    pool,
                    nextDeterministicSeed(generatorConfig, sequenceRandom),
                    uniqueFieldTracker).generate();
                pool.end(rawType, instance);
                return instance;
            } catch (ObjectGenerationException e) {
                pool.end(rawType, null);
                if (config.isIgnoreErrors()) return null;
                throw e;
            } catch (Exception e) {
                pool.end(rawType, null);
                if (config.isIgnoreErrors()) return null;
                throw new ObjectGenerationException(
                    "Failed to generate nested type " + rawType.getName() + " for field '"
                    + ownerType.getSimpleName() + "." + fieldName + "'", e);
            }
        }

        // ── 9. Unsupported type ───────────────────────────────────────────────
        return PRIMITIVE_DEFAULTS.getOrDefault(rawType, null);
    }

    /**
     * Convenience overload for callers that do not have a separate generic type
     * (e.g. internal recursive calls for array/collection elements).
     */
    Object resolveAndGenerate(Class<?> rawType, String fieldName,
                              Class<?> ownerType, int currentDepth) {
        return resolveAndGenerate(rawType, rawType, fieldName, ownerType, currentDepth, null);
    }

    private Object generateArray(Class<?> arrayType, Class<?> ownerType,
                                 String fieldName, int depth) {
        Class<?> comp = arrayType.getComponentType();
        int elementCount = nextCollectionSize();
        Object arr = Array.newInstance(comp, elementCount);
        for (int i = 0; i < elementCount; i++) {
            Object el = resolveAndGenerate(comp, fieldName + "[]", ownerType, depth);
            try {
                Array.set(arr, i, el);
            } catch (IllegalArgumentException ignored) {
                // null into a primitive slot — leave the JVM default (0 / false)
            }
        }
        return arr;
    }

    private int nextCollectionSize() {
        int min = generatorConfig.getMinCollectionSize();
        int max = generatorConfig.getMaxCollectionSize();
        return min == max ? min : sequenceRandom.nextInt(min, max + 1);
    }

    /**
     * Return {@code true} for concrete classes and records that {@link ObjectGenerator}
     * can instantiate. Excludes interfaces, abstract classes, and JDK types
     * (bootstrap-loaded classes) to avoid recursing into platform internals.
     *
     * <p>Arrays are excluded at step 5a in {@link #resolveAndGenerate} before this method
     * is ever called, so there is no {@code isArray} check here.
     */
    private boolean isNestableType(Class<?> type) {
        if (type.isInterface()) return false;
        if (java.lang.reflect.Modifier.isAbstract(type.getModifiers())) return false;
        // Bootstrap ClassLoader (null) loads all JDK platform classes — skip them
        if (type.getClassLoader() == null) return false;
        return true;
    }
}
