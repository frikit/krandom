/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.object;

import io.github.frikit.krandom.generator.GenerationContext;
import io.github.frikit.krandom.generator.Generator;
import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.failure.GenerationFailureCategory;
import io.github.frikit.krandom.generator.failure.GenerationFailureContext;
import io.github.frikit.krandom.generator.failure.GenerationOperation;
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
import io.github.frikit.krandom.generator.base.NumberGenerator;
import io.github.frikit.krandom.generator.base.ShortGenerator;
import io.github.frikit.krandom.generator.base.StringGenerator;
import io.github.frikit.krandom.generator.datetime.DateGenerator;
import io.github.frikit.krandom.generator.datetime.InstantGenerator;
import io.github.frikit.krandom.generator.datetime.LegacyTimeZoneGenerator;
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
import io.github.frikit.krandom.generator.network.URLGenerator;
import io.github.frikit.krandom.generator.network.UriGenerator;
import io.github.frikit.krandom.generator.object.exception.ObjectGenerationException;
import io.github.frikit.krandom.generator.provider.ProviderHub;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.MalformedURLException;
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
import java.util.TimeZone;
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
 *   <li>Unsupported type: fail with field context, or return primitive zero / {@code null}
 *       in explicit lenient mode</li>
 * </ol>
 */
final class FieldGeneratorResolver {

    private static final String OBJECT_CHARACTER_POOL = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

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
    private final SemanticFieldRegistry       semanticRegistry;
    private final Set<String>                 uniqueFieldNames;
    private final ObjectGenerationFailurePolicy failurePolicy;
    private final Map<TypeVariable<?>, Type> typeBindings;

    FieldGeneratorResolver(ObjectGeneratorConfig config,
                           ObjectPool pool,
                           UniqueFieldTracker uniqueFieldTracker,
                           Long generationSeed,
                           Class<?> rootType) {
        this.config = config;
        this.generatorConfig = config.getGeneratorConfig();
        this.pool = pool;
        this.uniqueFieldTracker = uniqueFieldTracker;
        this.sequenceRandom = generationSeed != null ? new Random(generationSeed) : this.generatorConfig.createRandom();
        this.builtins = buildBuiltins(config, this.generatorConfig, this.sequenceRandom);
        this.semanticRegistry = config.getSemanticRegistry();
        this.semanticStringGenerators = buildSemanticStringGenerators(this.generatorConfig, this.sequenceRandom, this.semanticRegistry);
        this.semanticTypedGenerators = buildSemanticTypedGenerators(this.config, this.generatorConfig, this.sequenceRandom);
        this.semanticMode = config.getSemanticMode();
        this.uniqueFieldNames = config.getUniqueFieldNames();
        this.failurePolicy = new ObjectGenerationFailurePolicy(
            config.isIgnoreErrors(), generatorConfig.getGenerationFailureListener());
        this.typeBindings = ResolvedType.bindingsFor(rootType);
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

        Generator<Byte> byteGenerator = byteGenerator(byteSeed, seedSource, Byte.MIN_VALUE, Byte.MAX_VALUE);
        Generator<Short> shortGenerator = shortGenerator(shortSeed, seedSource, Short.MIN_VALUE, Short.MAX_VALUE);
        Generator<Integer> intGenerator = intGenerator(intSeed, seedSource, Integer.MIN_VALUE, Integer.MAX_VALUE);
        Generator<Long> longGenerator = longGenerator(longSeed, seedSource, Long.MIN_VALUE, Long.MAX_VALUE);
        Generator<Float> floatGenerator = floatGenerator(floatSeed, seedSource, 0f, 1f, null);
        Generator<Double> doubleGenerator = doubleGenerator(doubleSeed, seedSource, 0.0, 1.0, null);
        Generator<Character> charGenerator = charGenerator(charSeed, seedSource);
        Generator<Boolean> booleanGenerator = booleanGenerator(booleanSeed, seedSource);
        Generator<String> stringGenerator = buildStringGenerator(
            generatorConfig, nextDeterministicSeed(generatorConfig, seedSource), seedSource);
        Generator<BigDecimal> bigDecimalGenerator = bigDecimalGenerator(
            nextDeterministicSeed(generatorConfig, seedSource), seedSource, "0", "1000000", 2);
        Generator<BigInteger> bigIntegerGenerator = bigIntegerGenerator(
            nextDeterministicSeed(generatorConfig, seedSource), seedSource, 0L, Long.MAX_VALUE);
        Generator<Number> numberGenerator = new NumberGenerator(derivedGeneratorConfig(generatorConfig, seedSource));
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
        m.put(Number.class, numberGenerator);
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
        Generator<TimeZone> timeZoneGenerator = new LegacyTimeZoneGenerator(derivedGeneratorConfig(generatorConfig, seedSource));
        UriGenerator uriStringGenerator = new UriGenerator(derivedGeneratorConfig(generatorConfig, seedSource));
        URLGenerator urlStringGenerator = new URLGenerator(derivedGeneratorConfig(generatorConfig, seedSource));
        Generator<URI> uriGenerator = () -> URI.create(uriStringGenerator.generate());
        Generator<java.net.URL> urlGenerator = () -> toUrl(URI.create(urlStringGenerator.generate("https")));

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
        m.put(TimeZone.class, timeZoneGenerator);
        m.put(URI.class, uriGenerator);
        m.put(java.net.URL.class, urlGenerator);
        return Collections.unmodifiableMap(m);
    }

    private static java.net.URL toUrl(URI uri) {
        try {
            return uri.toURL();
        } catch (MalformedURLException | IllegalArgumentException e) {
            throw new ObjectGenerationException("Generated URI could not be converted to URL", e);
        }
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
        return seed != null ? new Random(seed) : seedSource;
    }

    private static CharGenerator buildCharGenerator(long seed) {
        return CharGenerator.builder().uppercase().lowercase().seed(seed).build();
    }

    private static Generator<Character> charGenerator(Long seed, Random source) {
        if (seed != null) {
            return buildCharGenerator(seed);
        }
        return () -> OBJECT_CHARACTER_POOL.charAt(source.nextInt(OBJECT_CHARACTER_POOL.length()));
    }

    private static Generator<Boolean> booleanGenerator(Long seed, Random source) {
        if (seed != null) {
            return new BooleanGenerator(seed);
        }
        return source::nextBoolean;
    }

    private static Generator<String> buildStringGenerator(GeneratorConfig config, Long seed, Random source) {
        if (seed == null) {
            Generator<Character> characters = charGenerator(null, source);
            return () -> {
                int length = config.getMinStringLength() == config.getMaxStringLength()
                             ? config.getMinStringLength()
                             : (int) source.nextLong(config.getMinStringLength(),
                                                     (long) config.getMaxStringLength() + 1L);
                StringBuilder value = new StringBuilder(length);
                for (int i = 0; i < length; i++) {
                    value.append(characters.generate());
                }
                return value.toString();
            };
        }
        StringGenerator.Builder builder = StringGenerator.builder()
                                                        .minLength(config.getMinStringLength())
                                                        .maxLength(config.getMaxStringLength())
                                                        .charGenerator(buildCharGenerator(seed));
        return builder.seed(seed).build();
    }

    private static Generator<Byte> byteGenerator(Long seed, Random source, byte min, byte maxExclusive) {
        return seed != null ? new ByteGenerator(min, maxExclusive, seed)
                            : () -> (byte) source.nextInt(min, maxExclusive);
    }

    private static Generator<Short> shortGenerator(Long seed, Random source, short min, short maxExclusive) {
        return seed != null ? new ShortGenerator(min, maxExclusive, seed)
                            : () -> (short) source.nextInt(min, maxExclusive);
    }

    private static Generator<LocalDate> buildDateGenerator(GeneratorConfig config,
                                                           Random seedSource,
                                                           LocalDate min,
                                                           LocalDate max) {
        if (min.equals(LocalDate.of(1970, 1, 1)) && max.equals(LocalDate.of(2100, 12, 31))) {
            return new DateGenerator(derivedGeneratorConfig(config, seedSource));
        }
        Long seed = nextDeterministicSeed(config, seedSource);
        if (seed != null) {
            DateGenerator generator = new DateGenerator(min, max);
            generator.reseed(seed);
            return generator;
        }
        return () -> randomDate(seedSource, min, max);
    }

    private static Generator<LocalDateTime> buildLocalDateTimeGenerator(GeneratorConfig config,
                                                                        Random seedSource,
                                                                        LocalDate min,
                                                                        LocalDate max) {
        if (min.equals(LocalDate.of(1970, 1, 1)) && max.equals(LocalDate.of(2100, 12, 31))) {
            return new LocalDateTimeGenerator(derivedGeneratorConfig(config, seedSource));
        }
        Long seed = nextDeterministicSeed(config, seedSource);
        if (seed != null) {
            LocalDateTimeGenerator generator = new LocalDateTimeGenerator(min, max);
            generator.reseed(seed);
            return generator;
        }
        return () -> randomLocalDateTime(seedSource, min, max);
    }

    private static Generator<Instant> buildInstantGenerator(GeneratorConfig config,
                                                            Random seedSource,
                                                            LocalDate min,
                                                            LocalDate max) {
        if (min.equals(LocalDate.of(1970, 1, 1)) && max.equals(LocalDate.of(2100, 12, 31))) {
            return new InstantGenerator(derivedGeneratorConfig(config, seedSource));
        }
        Long seed = nextDeterministicSeed(config, seedSource);
        if (seed != null) {
            InstantGenerator generator = new InstantGenerator(min, max);
            generator.reseed(seed);
            return generator;
        }
        return () -> randomDate(seedSource, min, max).atStartOfDay().toInstant(ZoneOffset.UTC);
    }

    private static Generator<ZonedDateTime> buildZonedDateTimeGenerator(GeneratorConfig config,
                                                                        Random seedSource,
                                                                        LocalDate min,
                                                                        LocalDate max) {
        if (min.equals(LocalDate.of(1970, 1, 1)) && max.equals(LocalDate.of(2100, 12, 31))) {
            return new ZonedDateTimeGenerator(derivedGeneratorConfig(config, seedSource));
        }
        Long seed = nextDeterministicSeed(config, seedSource);
        if (seed != null) {
            ZonedDateTimeGenerator generator = new ZonedDateTimeGenerator(min, max);
            generator.reseed(seed);
            return generator;
        }
        List<String> zoneIds = new ArrayList<>(ZoneId.getAvailableZoneIds());
        zoneIds.sort(String::compareTo);
        return () -> ZonedDateTime.of(
            randomLocalDateTime(seedSource, min, max),
            ZoneId.of(zoneIds.get(seedSource.nextInt(zoneIds.size()))));
    }

    private static Generator<java.util.Date> buildUtilDateGenerator(GeneratorConfig config,
                                                                    Random seedSource,
                                                                    LocalDate min,
                                                                    LocalDate max) {
        if (min.equals(LocalDate.of(1970, 1, 1)) && max.equals(LocalDate.of(2100, 12, 31))) {
            return new UtilDateGenerator(derivedGeneratorConfig(config, seedSource));
        }
        Long seed = nextDeterministicSeed(config, seedSource);
        if (seed != null) {
            UtilDateGenerator generator = new UtilDateGenerator(min, max);
            generator.reseed(seed);
            return generator;
        }
        long minMillis = min.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli();
        long maxExclusiveMillis = max.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli();
        return () -> new java.util.Date(seedSource.nextLong(minMillis, maxExclusiveMillis));
    }

    private static Generator<java.sql.Date> buildSqlDateGenerator(GeneratorConfig config,
                                                                  Random seedSource,
                                                                  LocalDate min,
                                                                  LocalDate max) {
        if (min.equals(LocalDate.of(1970, 1, 1)) && max.equals(LocalDate.of(2100, 12, 31))) {
            return new SqlDateGenerator(derivedGeneratorConfig(config, seedSource));
        }
        Long seed = nextDeterministicSeed(config, seedSource);
        if (seed != null) {
            SqlDateGenerator generator = new SqlDateGenerator(min, max);
            generator.reseed(seed);
            return generator;
        }
        return () -> java.sql.Date.valueOf(randomDate(seedSource, min, max));
    }

    private static Generator<java.sql.Timestamp> buildSqlTimestampGenerator(GeneratorConfig config,
                                                                            Random seedSource,
                                                                            LocalDate min,
                                                                            LocalDate max) {
        if (min.equals(LocalDate.of(1970, 1, 1)) && max.equals(LocalDate.of(2100, 12, 31))) {
            return new SqlTimestampGenerator(derivedGeneratorConfig(config, seedSource));
        }
        Long seed = nextDeterministicSeed(config, seedSource);
        if (seed != null) {
            SqlTimestampGenerator generator = new SqlTimestampGenerator(min, max);
            generator.reseed(seed);
            return generator;
        }
        long minMillis = min.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli();
        long maxExclusiveMillis = max.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli();
        return () -> new java.sql.Timestamp(seedSource.nextLong(minMillis, maxExclusiveMillis));
    }

    private static LocalDate randomDate(Random source, LocalDate min, LocalDate max) {
        return LocalDate.ofEpochDay(source.nextLong(min.toEpochDay(), max.toEpochDay() + 1L));
    }

    private static LocalDateTime randomLocalDateTime(Random source, LocalDate min, LocalDate max) {
        return LocalDateTime.of(
            randomDate(source, min, max),
            LocalTime.of(source.nextInt(24), source.nextInt(60), source.nextInt(60)));
    }

    private static Map<String, Generator<?>> buildSemanticStringGenerators(GeneratorConfig config,
                                                                          Random seedSource,
                                                                          SemanticFieldRegistry semanticRegistry) {
        Map<String, Generator<?>> generators = new HashMap<>();
        for (String semanticKey : semanticRegistry.providerBackedSemanticKeys()) {
            registerSemantic(generators, config, seedSource,
                             derivedConfig -> buildProviderBackedSemanticGenerator(derivedConfig, semanticRegistry, semanticKey),
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

        LocalDate today = LocalDate.now(config.getClock());
        registerTemporalSemantic(generators, objectConfig, config, seedSource, "createdat", today.minusYears(10), today);
        registerTemporalSemantic(generators, objectConfig, config, seedSource, "updatedat", today.minusYears(10), today);
        registerTemporalSemantic(generators, objectConfig, config, seedSource, "birthdate", today.minusYears(90), today.minusYears(18));
        Generator<Integer> ageGenerator = intGenerator(nextDeterministicSeed(config, seedSource), seedSource, 18, 91);
        registerTypedSemantic(generators, "age", int.class, ageGenerator);
        registerTypedSemantic(generators, "age", Integer.class, ageGenerator);
        registerTypedSemantic(generators, "age", long.class, (Generator<Long>) () -> Long.valueOf(ageGenerator.generate()));
        registerTypedSemantic(generators, "age", Long.class, (Generator<Long>) () -> Long.valueOf(ageGenerator.generate()));
        registerTypedSemantic(generators, "age", short.class, (Generator<Short>) () -> Short.valueOf(ageGenerator.generate().shortValue()));
        registerTypedSemantic(generators, "age", Short.class, (Generator<Short>) () -> Short.valueOf(ageGenerator.generate().shortValue()));
        registerTypedSemantic(generators, "age", String.class, (Generator<String>) () -> Integer.toString(ageGenerator.generate()));

        registerNumericSemantic(generators, "amount",
                                bigDecimalGenerator(nextDeterministicSeed(config, seedSource), seedSource,
                                                    "0", "1000000", 2),
                                bigIntegerGenerator(nextDeterministicSeed(config, seedSource), seedSource,
                                                    1L, Long.MAX_VALUE),
                                intGenerator(nextDeterministicSeed(config, seedSource), seedSource,
                                             1, Integer.MAX_VALUE),
                                longGenerator(nextDeterministicSeed(config, seedSource), seedSource,
                                              1L, Long.MAX_VALUE),
                                doubleGenerator(nextDeterministicSeed(config, seedSource), seedSource,
                                                0.01, 1000000.0, 2),
                                floatGenerator(nextDeterministicSeed(config, seedSource), seedSource,
                                               0.01f, 1000000.0f, 2));

        registerNumericSemantic(generators, "balance",
                                bigDecimalGenerator(nextDeterministicSeed(config, seedSource), seedSource,
                                                    "0", "10000000", 2),
                                bigIntegerGenerator(nextDeterministicSeed(config, seedSource), seedSource,
                                                    1L, Long.MAX_VALUE),
                                intGenerator(nextDeterministicSeed(config, seedSource), seedSource,
                                             1, Integer.MAX_VALUE),
                                longGenerator(nextDeterministicSeed(config, seedSource), seedSource,
                                              1L, Long.MAX_VALUE),
                                doubleGenerator(nextDeterministicSeed(config, seedSource), seedSource,
                                                0.01, 10000000.0, 2),
                                floatGenerator(nextDeterministicSeed(config, seedSource), seedSource,
                                               0.01f, 10000000.0f, 2));

        registerNumericSemantic(generators, "price",
                                bigDecimalGenerator(nextDeterministicSeed(config, seedSource), seedSource,
                                                    "0", "10000", 2),
                                bigIntegerGenerator(nextDeterministicSeed(config, seedSource), seedSource, 1L, 10000L),
                                intGenerator(nextDeterministicSeed(config, seedSource), seedSource, 1, 10000),
                                longGenerator(nextDeterministicSeed(config, seedSource), seedSource, 1L, 10000L),
                                doubleGenerator(nextDeterministicSeed(config, seedSource), seedSource,
                                                0.01, 10000.0, 2),
                                floatGenerator(nextDeterministicSeed(config, seedSource), seedSource,
                                               0.01f, 10000.0f, 2));

        Generator<String> currencyCodeGenerator = buildLocaleCurrencyCodeGenerator(config, seedSource);
        registerTypedSemantic(generators, "currency", String.class, currencyCodeGenerator);
        registerTypedSemantic(generators, "currency", io.github.frikit.krandom.generator.finance.Currency.class,
                              buildLibraryCurrencyGenerator(config, seedSource));
        registerTypedSemantic(generators, "currency", java.util.Currency.class, buildJavaCurrencyGenerator(config, seedSource));

        Generator<Long> stringIdGenerator = longGenerator(
            nextDeterministicSeed(config, seedSource), seedSource, 1L, Long.MAX_VALUE);
        registerTypedSemantic(generators, "id", UUID.class, new UUIDGenerator(derivedGeneratorConfig(config, seedSource)));
        registerTypedSemantic(generators, "id", String.class, () -> Long.toString(stringIdGenerator.generate()));
        registerTypedSemantic(generators, "id", BigInteger.class,
                              bigIntegerGenerator(nextDeterministicSeed(config, seedSource), seedSource,
                                                  1L, Long.MAX_VALUE));
        registerTypedSemantic(generators, "id", int.class,
                              intGenerator(nextDeterministicSeed(config, seedSource), seedSource,
                                           1, Integer.MAX_VALUE));
        registerTypedSemantic(generators, "id", Integer.class,
                              intGenerator(nextDeterministicSeed(config, seedSource), seedSource,
                                           1, Integer.MAX_VALUE));
        registerTypedSemantic(generators, "id", long.class,
                              longGenerator(nextDeterministicSeed(config, seedSource), seedSource,
                                            1L, Long.MAX_VALUE));
        registerTypedSemantic(generators, "id", Long.class,
                              longGenerator(nextDeterministicSeed(config, seedSource), seedSource,
                                            1L, Long.MAX_VALUE));

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

    private static Generator<?> buildProviderBackedSemanticGenerator(GeneratorConfig config,
                                                                     SemanticFieldRegistry semanticRegistry,
                                                                     String semanticKey) {
        ProviderHub hub = new ProviderHub(config);
        String providerName = Objects.requireNonNull(semanticRegistry.semanticProviderNameFor(semanticKey),
                                                     "No provider mapping for semantic key: " + semanticKey);
        Generator<?> provider = hub.get(providerName, Generator.class);
        if ("uuid".equals(semanticKey)) {
            return () -> provider.generate().toString();
        }
        return provider;
    }

    private static Generator<BigDecimal> bigDecimalGenerator(Long seed,
                                                             Random source,
                                                             String min,
                                                             String max,
                                                             int scale) {
        BigDecimal lower = new BigDecimal(min);
        BigDecimal upper = new BigDecimal(max);
        if (seed != null) {
            return new BigDecimalGenerator(lower, upper, scale, seed);
        }
        long originInclusive = lower.scaleByPowerOfTen(scale).toBigInteger().longValueExact();
        long boundExclusive = Math.addExact(
            upper.scaleByPowerOfTen(scale).toBigInteger().longValueExact(), 1L);
        return () -> BigDecimal.valueOf(source.nextLong(originInclusive, boundExclusive), scale);
    }

    private static Generator<BigInteger> bigIntegerGenerator(Long seed,
                                                             Random source,
                                                             long min,
                                                             long maxExclusive) {
        if (seed != null) {
            BigInteger lower = BigInteger.valueOf(min);
            BigInteger upper = BigInteger.valueOf(Math.max(min + 1, maxExclusive));
            return new BigIntegerGenerator(lower, upper, seed);
        }
        return () -> BigInteger.valueOf(source.nextLong(min, maxExclusive));
    }

    private static Generator<Integer> intGenerator(Long seed, Random source, int min, int maxExclusive) {
        return seed != null ? new IntGenerator(min, maxExclusive, seed) : () -> source.nextInt(min, maxExclusive);
    }

    private static Generator<Long> longGenerator(Long seed, Random source, long min, long maxExclusive) {
        return seed != null ? new LongGenerator(min, maxExclusive, seed) : () -> source.nextLong(min, maxExclusive);
    }

    private static Generator<Double> doubleGenerator(Long seed,
                                                     Random source,
                                                     double min,
                                                     double max,
                                                     Integer precision) {
        if (seed != null) {
            DoubleGenerator generator = new DoubleGenerator(min, max, seed);
            return precision == null ? generator : () -> round(generator.generate(), precision);
        }
        return () -> {
            double value = source.nextDouble(min, max);
            return precision == null ? value : round(value, precision);
        };
    }

    private static Generator<Float> floatGenerator(Long seed,
                                                   Random source,
                                                   float min,
                                                   float max,
                                                   Integer precision) {
        if (seed != null) {
            FloatGenerator generator = new FloatGenerator(min, max, seed);
            return precision == null ? generator : () -> (float) round(generator.generate(), precision);
        }
        return () -> {
            float value = source.nextFloat(min, max);
            return precision == null ? value : (float) round(value, precision);
        };
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

    static String normalizeSemanticFieldName(String fieldName) {
        return SemanticFieldRegistry.normalizeFieldName(fieldName);
    }

    static String semanticKeyForFieldName(String fieldName) {
        return SemanticFieldRegistry.defaults().semanticKeyForFieldName(fieldName);
    }

    static Set<String> semanticAliasesFor(String semanticKey) {
        return SemanticFieldRegistry.defaults().semanticAliasesFor(semanticKey);
    }

    static String semanticProviderNameFor(String semanticKey) {
        return SemanticFieldRegistry.defaults().semanticProviderNameFor(semanticKey);
    }

    private Generator<?> semanticGeneratorFor(Class<?> rawType, String fieldName) {
        if (semanticMode == ObjectGenerationSemanticMode.STRUCTURAL_ONLY) {
            return null;
        }
        String semanticKey = semanticRegistry.semanticKeyForFieldName(fieldName);
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
        for (String alias : semanticRegistry.semanticAliasesFor(semanticKey)) {
            if (uniqueFieldNames.contains(alias)) {
                return true;
            }
        }
        return false;
    }

    private boolean shouldReturnNull(AnnotatedElement element,
                                     Class<?> rawType,
                                     Generator<?> annotationGenerator,
                                     Generator<?> bvGen,
                                     boolean hasSizeConstraint) {
        if (element == null || rawType.isPrimitive() || rawType == Optional.class) {
            return false;
        }
        if (annotationGenerator != null || bvGen != null || hasSizeConstraint) {
            return false;
        }
        double probability = config.getNullProbability();
        return probability > 0.0 && sequenceRandom.nextDouble() < probability;
    }

    private boolean shouldReturnNull(AnnotatedElement element,
                                     Class<?> rawType,
                                     Generator<?> annotationGenerator,
                                     Generator<?> bvGen) {
        return shouldReturnNull(element, rawType, annotationGenerator, bvGen, false);
    }

    private boolean shouldReturnEmptyOptional(AnnotatedElement element) {
        if (element == null) {
            return false;
        }
        double probability = config.getOptionalEmptyProbability();
        return probability > 0.0 && sequenceRandom.nextDouble() < probability;
    }

    // ── Primary entry point ───────────────────────────────────────────────────

    private ResolvedType containerArgument(Type type, Class<?> containerContract, int index) {
        ResolvedType resolved = ResolvedType.resolve(type, typeBindings);
        Type hierarchyType = resolved.effectiveType() != null
                             ? resolved.effectiveType().declaredType()
                             : type;
        Map<TypeVariable<?>, Type> bindings = new LinkedHashMap<>(typeBindings);
        bindings.putAll(ResolvedType.bindingsFor(hierarchyType));
        return ResolvedType.resolve(containerContract.getTypeParameters()[index], bindings);
    }

    private boolean hasResolvedArguments(Type type, Class<?> containerContract) {
        for (int i = 0; i < containerContract.getTypeParameters().length; i++) {
            if (!containerArgument(type, containerContract, i).isResolved()) {
                return false;
            }
        }
        return true;
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
        if (concrete != null) {
            addAllOrThrow(concrete, values);
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
        if (concrete != null) {
            addAllOrThrow(concrete, values);
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
        if (rawType == PriorityQueue.class) {
            Queue<Object> queue = new PriorityQueue<>(Comparator.comparing(String::valueOf));
            queue.addAll(values);
            return queue;
        }
        Queue<Object> concrete = instantiateCollectionType(rawType, Queue.class);
        if (concrete != null) {
            addAllOrThrow(concrete, values);
            return concrete;
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
        } catch (NoSuchMethodException ignored) {
            return null;
        } catch (InvocationTargetException e) {
            throw new CollectionConstructionFailure(e.getTargetException());
        } catch (ReflectiveOperationException | RuntimeException e) {
            throw new CollectionConstructionFailure(e);
        }
    }

    private static void addAllOrThrow(Collection<Object> target, List<Object> values) {
        try {
            target.addAll(values);
        } catch (RuntimeException firstFailure) {
            if (target instanceof Queue<?>) {
                try {
                    target.clear();
                    for (Object value : values) {
                        if (value != null) {
                            target.add(value);
                        }
                    }
                    return;
                } catch (RuntimeException fallbackFailure) {
                    throw new CollectionInsertionFailure(fallbackFailure);
                }
            }
            throw new CollectionInsertionFailure(firstFailure);
        }
    }

    private static final class CollectionInsertionFailure extends RuntimeException {

        private static final long serialVersionUID = 1L;

        private final RuntimeException insertionCause;

        private CollectionInsertionFailure(RuntimeException insertionCause) {
            super(insertionCause);
            this.insertionCause = insertionCause;
        }

        private RuntimeException insertionCause() {
            return insertionCause;
        }
    }

    private static final class CollectionConstructionFailure extends RuntimeException {

        private static final long serialVersionUID = 1L;

        private final Throwable constructionCause;

        private CollectionConstructionFailure(Throwable constructionCause) {
            super(constructionCause);
            this.constructionCause = constructionCause;
        }

        private Throwable constructionCause() {
            return constructionCause;
        }
    }

    private Generator<?> fakeAnnotationGeneratorFor(AnnotatedElement element, Class<?> rawType) {
        Fake annotation = element.getAnnotation(Fake.class);
        if (annotation == null) return null;
        String key = normalizeSemanticFieldName(annotation.value());
        String canonicalKey = semanticRegistry.semanticKeyForFieldName(annotation.value());
        if (canonicalKey == null) {
            canonicalKey = key;
        }
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
            return intGenerator(seed, sequenceRandom, (int) min, (int) max);
        }
        if (rawType == long.class || rawType == Long.class) {
            return longGenerator(seed, sequenceRandom, min, max);
        }
        if (rawType == double.class || rawType == Double.class) {
            return doubleGenerator(seed, sequenceRandom, min, max, null);
        }
        if (rawType == float.class || rawType == Float.class) {
            return floatGenerator(seed, sequenceRandom, min, max, null);
        }
        if (rawType == short.class || rawType == Short.class) {
            return shortGenerator(seed, sequenceRandom, (short) min, (short) max);
        }
        if (rawType == byte.class || rawType == Byte.class) {
            return byteGenerator(seed, sequenceRandom, (byte) min, (byte) max);
        }
        return null;
    }

    private static Generator<?> annotationRandomizerFor(AnnotatedElement element,
                                                        Class<?> ownerType,
                                                        String fieldName,
                                                        int depth) {
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
            Generator<?> generator = ctor.newInstance(parameterValues);
            return () -> generateWithRandomizerContext(generator, generatorType, ownerType, fieldName, depth);
        } catch (InvocationTargetException e) {
            throw randomizerFailure(
                GenerationOperation.CONSTRUCT, generatorType, ownerType, fieldName, depth, e.getTargetException());
        } catch (ReflectiveOperationException | RuntimeException e) {
            throw randomizerFailure(
                GenerationOperation.CONSTRUCT, generatorType, ownerType, fieldName, depth, e);
        }
    }

    private static Object generateWithRandomizerContext(Generator<?> generator,
                                                        Class<? extends Generator<?>> generatorType,
                                                        Class<?> ownerType,
                                                        String fieldName,
                                                        int depth) {
        try {
            return generator.generate();
        } catch (ObjectGenerationException e) {
            if (e.getContext().isPresent()) {
                throw e;
            }
            throw randomizerFailure(
                GenerationOperation.GENERATE, generatorType, ownerType, fieldName, depth, e);
        } catch (RuntimeException e) {
            throw randomizerFailure(
                GenerationOperation.GENERATE, generatorType, ownerType, fieldName, depth, e);
        }
    }

    private static ObjectGenerationException randomizerFailure(GenerationOperation operation,
                                                               Class<? extends Generator<?>> generatorType,
                                                               Class<?> ownerType,
                                                               String fieldName,
                                                               int depth,
                                                               Throwable cause) {
        String path = ownerType.getSimpleName() + "." + fieldName;
        String generatorTypeName = generatorType.getName();
        GenerationFailureContext context = new GenerationFailureContext(
            GenerationFailureCategory.CUSTOM_GENERATOR,
            operation,
            path,
            ownerType,
            generatorTypeName,
            depth,
            -1);
        return new ObjectGenerationException(
            "Could not " + operation.name().toLowerCase(java.util.Locale.ROOT)
            + " @" + Randomizer.class.getSimpleName() + " generator at '" + path
            + "' (generator type " + generatorTypeName + ", depth " + depth + ")",
            context,
            cause);
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

        ResolvedType resolvedType = ResolvedType.resolve(genericType, typeBindings);
        rawType = Objects.requireNonNullElse(resolvedType.rawClass(), rawType);

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

        Generator<?> annotationGenerator = element != null
                                           ? annotationRandomizerFor(element, ownerType, fieldName, currentDepth)
                                           : null;
        Generator<?> fakeAnnotationGenerator = element != null ? fakeAnnotationGeneratorFor(element, rawType) : null;
        Generator<?> fakeRangeGenerator = element != null ? fakeRangeGeneratorFor(element, rawType) : null;
        Generator<?> bvGen = element != null
                              ? BeanValidationSupport.constraintGeneratorFor(
                                  element, rawType, sequenceRandom, generatorConfig.getClock())
                              : null;
        boolean hasSizeConstraint = element != null && BeanValidationSupport.hasSizeConstraint(element);

        // ── 3a. Semantic field-name resolver ─────────────────────────────────
        String semanticKey = semanticRegistry.semanticKeyForFieldName(fieldName);
        Generator<?> semanticGenerator = semanticGeneratorFor(rawType, fieldName);
        if (semanticGenerator != null
            && (semanticMode == ObjectGenerationSemanticMode.STRICT
                || (annotationGenerator == null && fakeAnnotationGenerator == null
                    && fakeRangeGenerator == null && bvGen == null))) {
            return generateWithUniqueness(fieldName, semanticKey, semanticGenerator);
        }

        // ── 3aa. Configured null/optional behavior ────────────────────────────
        if (bvGen != null && BeanValidationSupport.hasNullConstraint(element)) {
            return bvGen.generate();
        }
        if (Optional.class == rawType) {
            if (!hasResolvedArguments(genericType, Optional.class)) {
                return handleUnsupportedType(rawType, genericType, ownerType, fieldName, currentDepth);
            }
            if (shouldReturnEmptyOptional(element)) {
                return Optional.empty();
            }
            ResolvedType valueType = containerArgument(genericType, Optional.class, 0);
            Object value = resolveAndGenerate(valueType, fieldName + ".value", ownerType, currentDepth);
            return Optional.ofNullable(value);
        }
        if (shouldReturnNull(element, rawType, annotationGenerator, bvGen, hasSizeConstraint)) {
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
            Generator<?> enumGenerator = enumSeed != null
                                         ? new EnumGenerator((Class<? extends Enum>) rawType, enumSeed)
                                         : () -> constants[sequenceRandom.nextInt(constants.length)];
            return generateWithUniqueness(fieldName, enumGenerator);
        }

        // ── 6a. Array ─────────────────────────────────────────────────────────
        if (rawType.isArray()) {
            if (!resolvedType.isResolved()) {
                return handleUnsupportedType(rawType, genericType, ownerType, fieldName, currentDepth);
            }
            return generateArray(resolvedType, rawType, ownerType, fieldName, currentDepth, element);
        }

        // ── 6b. Set ───────────────────────────────────────────────────────────
        if (Set.class.isAssignableFrom(rawType)) {
            if (!hasResolvedArguments(genericType, Set.class)) {
                return handleUnsupportedType(rawType, genericType, ownerType, fieldName, currentDepth);
            }
            ResolvedType elem = containerArgument(genericType, Set.class, 0);
            int elementCount = nextCollectionSize(element);
            Set<Object> values = new LinkedHashSet<>();
            int attempts = 0;
            int maxAttempts = Math.max(10, elementCount * 10);
            while (values.size() < elementCount && attempts++ < maxAttempts) {
                values.add(resolveAndGenerate(elem, fieldName + "[]", ownerType, currentDepth));
            }
            try {
                return toSetType(rawType, new ArrayList<>(values));
            } catch (CollectionInsertionFailure failure) {
                return handleCollectionInsertionFailure(
                    ownerType, fieldName, genericType, currentDepth, failure.insertionCause());
            } catch (CollectionConstructionFailure failure) {
                return handleCollectionConstructionFailure(
                    ownerType, fieldName, genericType, currentDepth, failure.constructionCause());
            }
        }

        // ── 6c. List / Queue ──────────────────────────────────────────────────
        if (List.class.isAssignableFrom(rawType) || Queue.class.isAssignableFrom(rawType)) {
            Class<?> containerContract = List.class.isAssignableFrom(rawType) ? List.class : Queue.class;
            if (!hasResolvedArguments(genericType, containerContract)) {
                return handleUnsupportedType(rawType, genericType, ownerType, fieldName, currentDepth);
            }
            ResolvedType elem = containerArgument(genericType, containerContract, 0);
            int elementCount = nextCollectionSize(element);
            List<Object> els = new ArrayList<>(elementCount);
            for (int i = 0; i < elementCount; i++) {
                els.add(resolveAndGenerate(elem, fieldName + "[]", ownerType, currentDepth));
            }
            try {
                if (List.class.isAssignableFrom(rawType)) {
                    return toListType(rawType, els);
                }
                return toQueueType(rawType, els);
            } catch (CollectionInsertionFailure failure) {
                return handleCollectionInsertionFailure(
                    ownerType, fieldName, genericType, currentDepth, failure.insertionCause());
            } catch (CollectionConstructionFailure failure) {
                return handleCollectionConstructionFailure(
                    ownerType, fieldName, genericType, currentDepth, failure.constructionCause());
            }
        }

        // ── 6d. Map ───────────────────────────────────────────────────────────
        if (Map.class.isAssignableFrom(rawType)) {
            if (!hasResolvedArguments(genericType, Map.class)) {
                return handleUnsupportedType(rawType, genericType, ownerType, fieldName, currentDepth);
            }
            ResolvedType k = containerArgument(genericType, Map.class, 0);
            ResolvedType v = containerArgument(genericType, Map.class, 1);
            Map<Object, Object> map;
            try {
                map = toMapType(rawType);
            } catch (CollectionConstructionFailure failure) {
                return handleCollectionConstructionFailure(
                    ownerType, fieldName, genericType, currentDepth, failure.constructionCause());
            }
            if (map == null) {
                return null;
            }
            int elementCount = nextCollectionSize(element);
            int attempts = 0;
            int maxAttempts = Math.max(10, elementCount * 10);
            while (map.size() < elementCount && attempts++ < maxAttempts) {
                Object key = resolveAndGenerate(k, fieldName + ".key", ownerType, currentDepth);
                Object val = resolveAndGenerate(v, fieldName + ".value", ownerType, currentDepth);
                if (key != null) {
                    String entryPath = ownerType.getSimpleName() + "." + fieldName + "[" + map.size() + "]";
                    try {
                        map.put(key, val);
                    } catch (RuntimeException e) {
                        String declaredType = genericType.getTypeName();
                        GenerationFailureContext context = new GenerationFailureContext(
                            GenerationFailureCategory.COLLECTION_INSERTION,
                            GenerationOperation.INSERT,
                            entryPath,
                            ownerType,
                            declaredType,
                            currentDepth,
                            -1);
                        return failurePolicy.handle(
                            new ObjectGenerationException(
                                "Could not insert map entry at '" + entryPath + "' (declared type "
                                + declaredType + ", depth " + currentDepth + ")",
                                context,
                                e),
                            null);
                    }
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
        // Abstract/interface field types may be mapped to a concrete implementation
        // via GeneratorConfig.objectSubtype / ObjectGeneratorConfig.subtype.
        Class<?> nestedType = config.resolveSubtype(rawType);
        if (isNestableType(nestedType)) {
            if (pool.isInProgress(nestedType)) {
                return pool.getCached(nestedType); // break circular reference
            }
            pool.begin(nestedType);
            try {
                Object instance = new ObjectGenerator<>(
                    nestedType,
                    config,
                    currentDepth + 1,
                    pool,
                    nextDeterministicSeed(generatorConfig, sequenceRandom),
                    uniqueFieldTracker).generate();
                pool.end(nestedType, instance);
                return instance;
            } catch (ObjectGenerationException e) {
                pool.end(nestedType, null);
                return failurePolicy.handle(
                    contextualizeNestedFailure(
                        e, nestedType, ownerType, fieldName, genericType, currentDepth),
                    null);
            } catch (Exception e) {
                pool.end(nestedType, null);
                return failurePolicy.handle(
                    nestedFailure(
                        GenerationFailureCategory.REFLECTION,
                        GenerationOperation.GENERATE,
                        ownerType.getSimpleName() + "." + fieldName,
                        ownerType,
                        genericType.getTypeName(),
                        currentDepth,
                        e),
                    null);
            }
        }

        // ── 9. Unsupported type ───────────────────────────────────────────────
        return handleUnsupportedType(rawType, genericType, ownerType, fieldName, currentDepth);
    }

    private static ObjectGenerationException contextualizeNestedFailure(ObjectGenerationException failure,
                                                                        Class<?> nestedType,
                                                                        Class<?> ownerType,
                                                                        String fieldName,
                                                                        Type declaredType,
                                                                        int depth) {
        String parentPath = ownerType.getSimpleName() + "." + fieldName;
        Optional<GenerationFailureContext> existing = failure.getContext();
        if (existing.isEmpty()) {
            return nestedFailure(
                GenerationFailureCategory.REFLECTION,
                GenerationOperation.GENERATE,
                parentPath,
                ownerType,
                declaredType.getTypeName(),
                depth,
                failure.getCause());
        }

        GenerationFailureContext child = existing.orElseThrow();
        String nestedRoot = nestedType.getSimpleName();
        String childSuffix;
        if (child.path().equals(nestedRoot)) {
            childSuffix = "";
        } else if (child.path().startsWith(nestedRoot + ".")) {
            childSuffix = child.path().substring(nestedRoot.length());
        } else {
            childSuffix = "." + child.path();
        }
        return nestedFailure(
            child.category(),
            child.operation(),
            parentPath + childSuffix,
            child.ownerType(),
            child.declaredType(),
            child.depth(),
            failure.getCause());
    }

    private static ObjectGenerationException nestedFailure(GenerationFailureCategory category,
                                                           GenerationOperation operation,
                                                           String path,
                                                           Class<?> ownerType,
                                                           String declaredType,
                                                           int depth,
                                                           Throwable cause) {
        GenerationFailureContext context = new GenerationFailureContext(
            category,
            operation,
            path,
            ownerType,
            declaredType,
            depth,
            -1);
        return new ObjectGenerationException(
            "Could not generate nested value at '" + path + "' (declared type "
            + declaredType + ", depth " + depth + ")",
            context,
            cause);
    }

    private Object handleUnsupportedType(Class<?> rawType,
                                         Type declaredType,
                                         Class<?> ownerType,
                                         String fieldName,
                                         int depth) {
        String path = ownerType.getSimpleName() + "." + fieldName;
        String declaredTypeName = declaredType.getTypeName();
        GenerationFailureContext context = new GenerationFailureContext(
            GenerationFailureCategory.UNSUPPORTED_TYPE,
            GenerationOperation.GENERATE,
            path,
            ownerType,
            declaredTypeName,
            depth,
            -1);
        UnsupportedOperationException cause = new UnsupportedOperationException(
            "No generator is registered for the declared type");
        return failurePolicy.handle(
            new ObjectGenerationException(
                "Unsupported type at '" + path + "' (declared type "
                + declaredTypeName + ", depth " + depth + ")",
                context,
                cause),
            PRIMITIVE_DEFAULTS.getOrDefault(rawType, null));
    }

    private Object handleCollectionInsertionFailure(Class<?> ownerType,
                                                    String fieldName,
                                                    Type declaredType,
                                                    int depth,
                                                    RuntimeException cause) {
        String path = ownerType.getSimpleName() + "." + fieldName;
        String declaredTypeName = declaredType.getTypeName();
        GenerationFailureContext context = new GenerationFailureContext(
            GenerationFailureCategory.COLLECTION_INSERTION,
            GenerationOperation.INSERT,
            path,
            ownerType,
            declaredTypeName,
            depth,
            -1);
        return failurePolicy.handle(
            new ObjectGenerationException(
                "Could not populate collection at '" + path + "' (declared type "
                + declaredTypeName + ", depth " + depth + ")",
                context,
                cause),
            null);
    }

    private Object handleCollectionConstructionFailure(Class<?> ownerType,
                                                       String fieldName,
                                                       Type declaredType,
                                                       int depth,
                                                       Throwable cause) {
        String path = ownerType.getSimpleName() + "." + fieldName;
        String declaredTypeName = declaredType.getTypeName();
        GenerationFailureContext context = new GenerationFailureContext(
            GenerationFailureCategory.CONSTRUCTION,
            GenerationOperation.CONSTRUCT,
            path,
            ownerType,
            declaredTypeName,
            depth,
            -1);
        return failurePolicy.handle(
            new ObjectGenerationException(
                "Could not construct collection at '" + path + "' (declared type "
                + declaredTypeName + ", depth " + depth + ")",
                context,
                cause),
            null);
    }

    private Object resolveAndGenerate(ResolvedType type,
                                      String fieldName,
                                      Class<?> ownerType,
                                      int currentDepth) {
        ResolvedType generationType = Objects.requireNonNullElse(type.effectiveType(), type);
        Class<?> rawType = Objects.requireNonNullElse(generationType.rawClass(), Object.class);
        return resolveAndGenerate(generationType.declaredType(), rawType, fieldName, ownerType, currentDepth, null);
    }

    private Object generateArray(ResolvedType declaredArrayType,
                                 Class<?> runtimeArrayType,
                                 Class<?> ownerType,
                                 String fieldName, int depth, AnnotatedElement element) {
        Class<?> comp = runtimeArrayType.getComponentType();
        ResolvedType declaredComponentType = Objects.requireNonNull(declaredArrayType.componentType());
        int elementCount = nextCollectionSize(element);
        Object arr = Array.newInstance(comp, elementCount);
        for (int i = 0; i < elementCount; i++) {
            Object el = resolveAndGenerate(declaredComponentType, fieldName + "[]", ownerType, depth);
            try {
                Array.set(arr, i, el);
            } catch (IllegalArgumentException e) {
                String elementPath = ownerType.getSimpleName() + "." + fieldName + "[" + i + "]";
                String declaredType = declaredArrayType.signature();
                GenerationFailureContext context = new GenerationFailureContext(
                    GenerationFailureCategory.COLLECTION_INSERTION,
                    GenerationOperation.INSERT,
                    elementPath,
                    ownerType,
                    declaredType,
                    depth,
                    -1);
                failurePolicy.handle(
                    new ObjectGenerationException(
                        "Could not insert array element at '" + elementPath + "' (declared type "
                        + declaredType + ", depth " + depth + ")",
                        context,
                        e),
                    null);
            }
        }
        return arr;
    }

    private int nextCollectionSize(AnnotatedElement element) {
        return BeanValidationSupport.sizeFor(element,
                                             sequenceRandom,
                                             generatorConfig.getMinCollectionSize(),
                                             generatorConfig.getMaxCollectionSize());
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
