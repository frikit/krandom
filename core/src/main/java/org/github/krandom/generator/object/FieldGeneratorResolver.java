/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.object;

import org.github.krandom.generator.GenerationContext;
import org.github.krandom.generator.Generator;
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
import org.github.krandom.generator.identifier.UUIDGenerator;
import org.github.krandom.generator.object.exception.ObjectGenerationException;

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
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.Stack;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;
import java.util.Vector;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;
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
 *   <li>Array ({@code T[]}): auto-populated with {@value #DEFAULT_ELEMENT_COUNT} elements</li>
 *   <li>{@code List<T>} or {@code Set<T>}: auto-populated with {@value #DEFAULT_ELEMENT_COUNT}
 *       elements resolved from the declared generic element type</li>
 *   <li>{@code Map<K,V>}: auto-populated with {@value #DEFAULT_ELEMENT_COUNT} entries</li>
 *   <li>{@code Optional<T>}: populated as {@code Optional.ofNullable(value)}</li>
 *   <li>Depth guard: if {@code currentDepth >= maxDepth} return primitive zero / {@code null}</li>
 *   <li>Nested class or record: delegate to a child {@link ObjectGenerator} (cycle-safe via
 *       {@link ObjectPool})</li>
 *   <li>Unsupported type: return primitive zero / {@code null}</li>
 * </ol>
 */
final class FieldGeneratorResolver {

    /**
     * Number of elements generated for arrays, lists, sets, and map entries.
     */
    static final int DEFAULT_ELEMENT_COUNT = 3;

    /**
     * Factories for non-date built-in Java base types (both primitive and wrapper forms),
     * plus {@link BigDecimal} and {@link BigInteger}.
     */
    private static final Map<Class<?>, Supplier<Generator<?>>> STATIC_BUILTINS = new HashMap<>();
    /**
     * Safe zero-values used when a primitive field cannot be assigned its resolved value
     * (e.g. at max depth, or when {@code ignoreErrors=true}).
     */
    private static final Map<Class<?>, Object>                   PRIMITIVE_DEFAULTS = new HashMap<>();
    private static final Map<Class<?>, Function<String, Object>> ARGUMENT_PARSERS   = new HashMap<>();

    static {
        STATIC_BUILTINS.put(byte.class, ByteGenerator::new);
        STATIC_BUILTINS.put(Byte.class, ByteGenerator::new);
        STATIC_BUILTINS.put(short.class, ShortGenerator::new);
        STATIC_BUILTINS.put(Short.class, ShortGenerator::new);
        STATIC_BUILTINS.put(int.class, IntGenerator::new);
        STATIC_BUILTINS.put(Integer.class, IntGenerator::new);
        STATIC_BUILTINS.put(long.class, LongGenerator::new);
        STATIC_BUILTINS.put(Long.class, LongGenerator::new);
        STATIC_BUILTINS.put(float.class, FloatGenerator::new);
        STATIC_BUILTINS.put(Float.class, FloatGenerator::new);
        STATIC_BUILTINS.put(double.class, DoubleGenerator::new);
        STATIC_BUILTINS.put(Double.class, DoubleGenerator::new);
        STATIC_BUILTINS.put(char.class, CharGenerator::letters);
        STATIC_BUILTINS.put(Character.class, CharGenerator::letters);
        STATIC_BUILTINS.put(boolean.class, BooleanGenerator::new);
        STATIC_BUILTINS.put(Boolean.class, BooleanGenerator::new);
        STATIC_BUILTINS.put(String.class, StringGenerator::letters);
        STATIC_BUILTINS.put(BigDecimal.class, BigDecimalGenerator::new);
        STATIC_BUILTINS.put(BigInteger.class, BigIntegerGenerator::new);
        STATIC_BUILTINS.put(AtomicInteger.class, () -> () -> new AtomicInteger(new IntGenerator().generate()));
        STATIC_BUILTINS.put(AtomicLong.class, () -> () -> new AtomicLong(new LongGenerator().generate()));
    }

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
    private final ObjectPool            pool;

    /**
     * Instance-level map that combines STATIC_BUILTINS with config-specific date factories.
     */
    private final Map<Class<?>, Supplier<Generator<?>>> builtins;

    FieldGeneratorResolver(ObjectGeneratorConfig config, ObjectPool pool) {
        this.config = config;
        this.pool = pool;
        this.builtins = buildBuiltins(config);
    }

    private static Map<Class<?>, Supplier<Generator<?>>> buildBuiltins(ObjectGeneratorConfig cfg) {
        Map<Class<?>, Supplier<Generator<?>>> m = new HashMap<>(STATIC_BUILTINS);
        LocalDate lo = cfg.getDateMin() != null ? cfg.getDateMin() : LocalDate.of(1970, 1, 1);
        LocalDate hi = cfg.getDateMax() != null ? cfg.getDateMax() : LocalDate.of(2100, 12, 31);
        m.put(LocalDate.class, () -> new DateGenerator(lo, hi));
        m.put(LocalTime.class, TimeGenerator::new);
        m.put(LocalDateTime.class, () -> new LocalDateTimeGenerator(lo, hi));
        m.put(Instant.class, () -> new InstantGenerator(lo, hi));
        m.put(ZonedDateTime.class, () -> new ZonedDateTimeGenerator(lo, hi));
        m.put(OffsetDateTime.class, () -> () -> new ZonedDateTimeGenerator(lo, hi).generate().toOffsetDateTime());
        m.put(OffsetTime.class, () -> () -> {
            int quarterHours = ThreadLocalRandom.current().nextInt(-72, 73);
            return new TimeGenerator().generate().atOffset(ZoneOffset.ofTotalSeconds(quarterHours * 15 * 60));
        });
        m.put(Year.class, () -> () -> Year.of(ThreadLocalRandom.current().nextInt(lo.getYear(), hi.getYear() + 1)));
        m.put(YearMonth.class, () -> () -> YearMonth.from(new DateGenerator(lo, hi).generate()));
        m.put(MonthDay.class, () -> () -> MonthDay.from(new DateGenerator(lo, hi).generate()));
        m.put(Duration.class, () -> () -> Duration.ofSeconds(ThreadLocalRandom.current().nextLong(0, 10L * 365 * 24 * 60 * 60 + 1)));
        m.put(Period.class, () -> () -> Period.of(
            ThreadLocalRandom.current().nextInt(0, 11),
            ThreadLocalRandom.current().nextInt(0, 12),
            ThreadLocalRandom.current().nextInt(0, 31)));
        m.put(ZoneId.class, () -> () -> {
            List<String> ids = new ArrayList<>(ZoneId.getAvailableZoneIds());
            return ZoneId.of(ids.get(ThreadLocalRandom.current().nextInt(ids.size())));
        });
        m.put(ZoneOffset.class, () -> () -> {
            int quarterHours = ThreadLocalRandom.current().nextInt(-72, 73);
            return ZoneOffset.ofTotalSeconds(quarterHours * 15 * 60);
        });
        m.put(java.util.Date.class, () -> new UtilDateGenerator(lo, hi));
        m.put(java.sql.Date.class, () -> new SqlDateGenerator(lo, hi));
        m.put(java.sql.Time.class, SqlTimeGenerator::new);
        m.put(java.sql.Timestamp.class, () -> new SqlTimestampGenerator(lo, hi));
        m.put(UUID.class, UUIDGenerator::new);
        return Collections.unmodifiableMap(m);
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
        return new LinkedHashSet<>(values);
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
        if (rawType.isInterface() || java.lang.reflect.Modifier.isAbstract(rawType.getModifiers())) {
            return new ArrayList<>(values);
        }
        // Unknown concrete List subtype; fallback to mutable list that remains assignable
        // for abstract/interface declarations and avoids assignment failures for common concrete types.
        return new ArrayList<>(values);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static Queue<Object> toQueueType(Class<?> rawType, List<Object> values) {
        Queue<Object> queue;
        if (rawType == PriorityQueue.class) {
            queue = new PriorityQueue<>(Comparator.comparing(String::valueOf));
        } else {
            queue = new java.util.ArrayDeque<>();
        }
        queue.addAll(values);
        return queue;
    }

    private static Map<Object, Object> toMapType(Class<?> rawType) {
        if (rawType == TreeMap.class
            || rawType == SortedMap.class
            || rawType == NavigableMap.class) {
            return new TreeMap<>(Comparator.comparing(String::valueOf));
        }
        return new LinkedHashMap<>();
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

        // ── 3a. Declarative @Randomizer override ─────────────────────────────
        if (element != null) {
            Generator<?> annotationGenerator = annotationRandomizerFor(element);
            if (annotationGenerator != null) return annotationGenerator.generate();
        }

        // ── 3b. Bean Validation constraint override ───────────────────────────
        if (element != null) {
            Generator<?> bvGen = BeanValidationSupport.constraintGeneratorFor(element, rawType);
            if (bvGen != null) return bvGen.generate();
        }

        // ── 3. Built-in (primitives, wrappers, String, JSR-310, UUID, BigDecimal, BigInteger) ──
        var builtinFactory = builtins.get(rawType);
        if (builtinFactory != null) {
            return builtinFactory.get().generate();
        }

        // ── 4. Enum ───────────────────────────────────────────────────────────
        if (rawType.isEnum()) {
            Object[] constants = rawType.getEnumConstants();
            if (constants.length == 0) return null;
            return new EnumGenerator((Class<? extends Enum>) rawType).generate();
        }

        // ── 5a. Array ─────────────────────────────────────────────────────────
        if (rawType.isArray()) {
            return generateArray(rawType, ownerType, fieldName, currentDepth);
        }

        // ── 5b. List / Set ────────────────────────────────────────────────────
        if (List.class.isAssignableFrom(rawType)
            || Set.class.isAssignableFrom(rawType)
            || Queue.class.isAssignableFrom(rawType)) {
            Class<?> elem = typeArg(genericType, 0);
            List<Object> els = new ArrayList<>(DEFAULT_ELEMENT_COUNT);
            for (int i = 0; i < DEFAULT_ELEMENT_COUNT; i++) {
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

        // ── 5c. Map ───────────────────────────────────────────────────────────
        if (Map.class.isAssignableFrom(rawType)) {
            Class<?> k = typeArg(genericType, 0);
            Class<?> v = typeArg(genericType, 1);
            Map<Object, Object> map = toMapType(rawType);
            for (int i = 0; i < DEFAULT_ELEMENT_COUNT; i++) {
                Object key = resolveAndGenerate(k, k, fieldName + ".key", ownerType, currentDepth, null);
                Object val = resolveAndGenerate(v, v, fieldName + ".val", ownerType, currentDepth, null);
                if (key != null) map.put(key, val);
            }
            if (rawType == Map.class) {
                return Collections.unmodifiableMap(map);
            }
            return map;
        }

        // ── 5d. Optional ──────────────────────────────────────────────────────
        if (Optional.class == rawType) {
            Class<?> valueType = typeArg(genericType, 0);
            Object value = resolveAndGenerate(valueType, valueType, fieldName + ".value", ownerType, currentDepth, null);
            return Optional.ofNullable(value);
        }

        // ── 6. Depth guard ────────────────────────────────────────────────────
        if (currentDepth >= config.getMaxDepth()) {
            return PRIMITIVE_DEFAULTS.getOrDefault(rawType, null);
        }

        // ── 7. Nested class or record (cycle-safe) ────────────────────────────
        if (isNestableType(rawType)) {
            if (pool.isInProgress(rawType)) {
                return pool.getCached(rawType); // break circular reference
            }
            pool.begin(rawType);
            try {
                Object instance = new ObjectGenerator<>(rawType, config, currentDepth + 1, pool).generate();
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

        // ── 8. Unsupported type ───────────────────────────────────────────────
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
        Object arr = Array.newInstance(comp, DEFAULT_ELEMENT_COUNT);
        for (int i = 0; i < DEFAULT_ELEMENT_COUNT; i++) {
            Object el = resolveAndGenerate(comp, fieldName + "[]", ownerType, depth);
            try {
                Array.set(arr, i, el);
            } catch (IllegalArgumentException ignored) {
                // null into a primitive slot — leave the JVM default (0 / false)
            }
        }
        return arr;
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
