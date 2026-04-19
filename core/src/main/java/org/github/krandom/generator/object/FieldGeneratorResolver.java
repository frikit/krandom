/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.object;

import org.github.krandom.generator.GenerationContext;
import org.github.krandom.generator.Generator;
import org.github.krandom.generator.GeneratorConfig;
import org.github.krandom.generator.base.BigDecimalGenerator;
import org.github.krandom.generator.base.BigIntegerGenerator;
import org.github.krandom.generator.base.BooleanGenerator;
import org.github.krandom.generator.base.ByteGenerator;
import org.github.krandom.generator.base.CharGenerator;
import org.github.krandom.generator.base.DoubleGenerator;
import org.github.krandom.generator.base.EnumGenerator;
import org.github.krandom.generator.base.FloatGenerator;
import org.github.krandom.generator.base.IntGenerator;
import org.github.krandom.generator.base.LongGenerator;
import org.github.krandom.generator.base.ShortGenerator;
import org.github.krandom.generator.base.StringGenerator;
import org.github.krandom.generator.datetime.DateGenerator;
import org.github.krandom.generator.datetime.InstantGenerator;
import org.github.krandom.generator.datetime.LocalDateTimeGenerator;
import org.github.krandom.generator.datetime.SqlDateGenerator;
import org.github.krandom.generator.datetime.SqlTimeGenerator;
import org.github.krandom.generator.datetime.SqlTimestampGenerator;
import org.github.krandom.generator.datetime.TimeGenerator;
import org.github.krandom.generator.datetime.UtilDateGenerator;
import org.github.krandom.generator.datetime.ZonedDateTimeGenerator;
import org.github.krandom.generator.finance.CurrencyGenerator;
import org.github.krandom.generator.identifier.UUIDGenerator;
import org.github.krandom.generator.location.CityGenerator;
import org.github.krandom.generator.location.CountryGenerator;
import org.github.krandom.generator.location.PhoneNumberGenerator;
import org.github.krandom.generator.location.PostalCodeGenerator;
import org.github.krandom.generator.location.StateGenerator;
import org.github.krandom.generator.location.StreetAddressGenerator;
import org.github.krandom.generator.network.DomainGenerator;
import org.github.krandom.generator.network.URLGenerator;
import org.github.krandom.generator.object.exception.ObjectGenerationException;
import org.github.krandom.generator.user.CompanyNameGenerator;
import org.github.krandom.generator.user.EmailGenerator;
import org.github.krandom.generator.user.FirstNameGenerator;
import org.github.krandom.generator.user.FullNameGenerator;
import org.github.krandom.generator.user.LastNameGenerator;
import org.github.krandom.generator.user.PasswordGenerator;
import org.github.krandom.generator.user.UsernameGenerator;

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
    }

    private final ObjectGeneratorConfig config;
    private final GeneratorConfig       generatorConfig;
    private final ObjectPool            pool;
    private final UniqueFieldTracker    uniqueFieldTracker;
    private final Random                sequenceRandom;
    private final Map<Class<?>, Generator<?>> builtins;
    private final Map<String, Generator<?>>   semanticStringGenerators;
    private final ObjectGenerationSemanticMode semanticMode;
    private final Set<String>                 uniqueFieldNames;

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
        registerSemantic(generators, config, seedSource, FieldGeneratorResolver::newFirstNameGenerator, "firstname", "givenname");
        registerSemantic(generators, config, seedSource, FieldGeneratorResolver::newLastNameGenerator, "lastname", "surname", "familyname");
        registerSemantic(generators, config, seedSource, FieldGeneratorResolver::newFullNameGenerator, "fullname", "displayname");
        registerSemantic(generators, config, seedSource, FieldGeneratorResolver::newEmailGenerator, "email", "emailaddress");
        registerSemantic(generators, config, seedSource, FieldGeneratorResolver::newUsernameGenerator, "username", "userhandle");
        registerSemantic(generators, config, seedSource, FieldGeneratorResolver::newPhoneNumberGenerator, "phone", "phonenumber", "mobile", "mobilephone", "telephone");
        registerSemantic(generators, config, seedSource, FieldGeneratorResolver::newStreetAddressGenerator, "street", "streetaddress", "addressline1");
        registerSemantic(generators, config, seedSource, FieldGeneratorResolver::newCityGenerator, "city", "town");
        registerSemantic(generators, config, seedSource, FieldGeneratorResolver::newStateGenerator, "state", "province", "region");
        registerSemantic(generators, config, seedSource, FieldGeneratorResolver::newPostalCodeGenerator, "postalcode", "postcode", "zipcode", "zip");
        registerSemantic(generators, config, seedSource, FieldGeneratorResolver::newCountryGenerator, "country", "countryname");
        registerSemantic(generators, config, seedSource, FieldGeneratorResolver::newCompanyNameGenerator, "company", "companyname", "organization", "organisation");
        registerSemantic(generators, config, seedSource, FieldGeneratorResolver::newPasswordGenerator, "password", "passcode");
        registerSemantic(generators, config, seedSource, FieldGeneratorResolver::newUrlGenerator, "url", "website", "homepage", "link");
        registerSemantic(generators, config, seedSource, FieldGeneratorResolver::newDomainGenerator, "domain", "hostname");
        registerSemantic(generators, config, seedSource, FieldGeneratorResolver::newCurrencyGenerator, "currency", "currencycode");
        registerSemantic(generators, config, seedSource, FieldGeneratorResolver::newUuidStringGenerator, "uuid", "guid");
        return Collections.unmodifiableMap(generators);
    }

    private static void registerSemantic(Map<String, Generator<?>> generators,
                                         GeneratorConfig config,
                                         Random seedSource,
                                         Function<GeneratorConfig, Generator<?>> factory,
                                         String... fieldNames) {
        try {
            Generator<?> generator = factory.apply(derivedGeneratorConfig(config, seedSource));
            for (String fieldName : fieldNames) {
                generators.put(normalizeFieldName(fieldName), generator);
            }
        } catch (UnsupportedOperationException ignored) {
            // Locale/provider not available — fall back to generic type resolution.
        }
    }

    private static Generator<?> newFirstNameGenerator(GeneratorConfig config) {
        return new FirstNameGenerator(config);
    }

    private static Generator<?> newLastNameGenerator(GeneratorConfig config) {
        return new LastNameGenerator(config);
    }

    private static Generator<?> newFullNameGenerator(GeneratorConfig config) {
        return new FullNameGenerator(config);
    }

    private static Generator<?> newEmailGenerator(GeneratorConfig config) {
        return new EmailGenerator(config);
    }

    private static Generator<?> newUsernameGenerator(GeneratorConfig config) {
        return new UsernameGenerator(config);
    }

    private static Generator<?> newPhoneNumberGenerator(GeneratorConfig config) {
        return new PhoneNumberGenerator(config);
    }

    private static Generator<?> newStreetAddressGenerator(GeneratorConfig config) {
        return new StreetAddressGenerator(config);
    }

    private static Generator<?> newCityGenerator(GeneratorConfig config) {
        return new CityGenerator(config);
    }

    private static Generator<?> newStateGenerator(GeneratorConfig config) {
        return new StateGenerator(config);
    }

    private static Generator<?> newPostalCodeGenerator(GeneratorConfig config) {
        return new PostalCodeGenerator(config);
    }

    private static Generator<?> newCountryGenerator(GeneratorConfig config) {
        return new CountryGenerator(config);
    }

    private static Generator<?> newCompanyNameGenerator(GeneratorConfig config) {
        return new CompanyNameGenerator(config);
    }

    private static Generator<?> newPasswordGenerator(GeneratorConfig config) {
        return new PasswordGenerator(config);
    }

    private static Generator<?> newUrlGenerator(GeneratorConfig config) {
        return new URLGenerator(config);
    }

    private static Generator<?> newDomainGenerator(GeneratorConfig config) {
        return new DomainGenerator(config);
    }

    private static Generator<?> newCurrencyGenerator(GeneratorConfig config) {
        return new CurrencyGenerator(config);
    }

    private static Generator<?> newUuidStringGenerator(GeneratorConfig config) {
        UUIDGenerator generator = new UUIDGenerator(config);
        return () -> generator.generate().toString();
    }

    private static String normalizeFieldName(String fieldName) {
        StringBuilder normalized = new StringBuilder(fieldName.length());
        for (int i = 0; i < fieldName.length(); i++) {
            char ch = fieldName.charAt(i);
            if (Character.isLetterOrDigit(ch)) {
                normalized.append(Character.toLowerCase(ch));
            }
        }
        return normalized.toString();
    }

    private Generator<?> semanticGeneratorFor(Class<?> rawType, String fieldName) {
        if (semanticMode == ObjectGenerationSemanticMode.STRUCTURAL_ONLY) {
            return null;
        }
        if (rawType != String.class) {
            return null;
        }
        return semanticStringGenerators.get(normalizeFieldName(fieldName));
    }

    private Object generateWithUniqueness(String fieldName, Generator<?> generator) {
        String normalizedFieldName = normalizeFieldName(fieldName);
        if (!uniqueFieldNames.contains(normalizedFieldName)) {
            return generator.generate();
        }
        return uniqueFieldTracker.nextUnique(
            normalizedFieldName,
            generator::generate,
            config.getUniquenessMaxAttempts());
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
        Function<String, Object> parser = ARGUMENT_PARSERS.get(type);
        if (parser != null) return parser.apply(rawValue);
        if (type.isEnum()) {
            return Enum.valueOf((Class<? extends Enum>) type.asSubclass(Enum.class), rawValue);
        }
        throw new IllegalArgumentException("Unsupported @" + RandomizerArgument.class.getSimpleName()
                                           + " type: " + type.getName());
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

        // ── 2. Type-level override ────────────────────────────────────────────
        var typeOverride = config.getTypeOverride(rawType);
        if (typeOverride.isPresent()) {
            return typeOverride.get().generate();
        }

        Generator<?> annotationGenerator = element != null ? annotationRandomizerFor(element) : null;
        Generator<?> bvGen = element != null ? BeanValidationSupport.constraintGeneratorFor(element, rawType) : null;

        // ── 3a. Semantic field-name resolver ─────────────────────────────────
        Generator<?> semanticGenerator = semanticGeneratorFor(rawType, fieldName);
        if (semanticGenerator != null
            && (semanticMode == ObjectGenerationSemanticMode.STRICT
                || (semanticMode == ObjectGenerationSemanticMode.RELAXED
                    && annotationGenerator == null
                    && bvGen == null))) {
            return generateWithUniqueness(fieldName, semanticGenerator);
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
