/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.object;

import org.github.krandom.generator.Generator;
import org.github.krandom.generator.base.*;
import org.github.krandom.generator.datetime.DateGenerator;
import org.github.krandom.generator.datetime.InstantGenerator;
import org.github.krandom.generator.datetime.LocalDateTimeGenerator;
import org.github.krandom.generator.datetime.TimeGenerator;
import org.github.krandom.generator.datetime.ZonedDateTimeGenerator;
import org.github.krandom.generator.identifier.UUIDGenerator;
import org.github.krandom.generator.object.exception.ObjectGenerationException;

import java.lang.reflect.Array;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Internal resolver: maps a field's ({@link Type}, {@link Class}, name, owner) tuple
 * to a generated value.
 *
 * <p>Resolution order (first match wins):
 * <ol>
 *   <li>Field-level override from {@link ObjectGeneratorConfig} ({@code "OwnerType.fieldName"})</li>
 *   <li>Type-level override from {@link ObjectGeneratorConfig}</li>
 *   <li>Built-in generator for Java primitives / wrappers / {@code String} / JSR-310 types /
 *       {@link UUID}</li>
 *   <li>Enum: random constant</li>
 *   <li>Array ({@code T[]}): auto-populated with {@value #DEFAULT_ELEMENT_COUNT} elements</li>
 *   <li>{@code List<T>} or {@code Set<T>}: auto-populated with {@value #DEFAULT_ELEMENT_COUNT}
 *       elements resolved from the declared generic element type</li>
 *   <li>{@code Map<K,V>}: auto-populated with {@value #DEFAULT_ELEMENT_COUNT} entries</li>
 *   <li>Depth guard: if {@code currentDepth >= maxDepth} return primitive zero / {@code null}</li>
 *   <li>Nested class or record: delegate to a child {@link ObjectGenerator} (cycle-safe via
 *       {@link ObjectPool})</li>
 *   <li>Unsupported type: return primitive zero / {@code null}</li>
 * </ol>
 */
final class FieldGeneratorResolver {

    /** Number of elements generated for arrays, lists, sets, and map entries. */
    static final int DEFAULT_ELEMENT_COUNT = 3;

    /**
     * Factories for all built-in Java base types (both primitive and wrapper forms),
     * plus JSR-310 date/time types and {@link UUID}.
     * Each call to the supplier creates a fresh generator instance.
     */
    private static final Map<Class<?>, Supplier<Generator<?>>> BUILTINS = new HashMap<>();

    static {
        BUILTINS.put(byte.class,       ByteGenerator::new);
        BUILTINS.put(Byte.class,       ByteGenerator::new);
        BUILTINS.put(short.class,      ShortGenerator::new);
        BUILTINS.put(Short.class,      ShortGenerator::new);
        BUILTINS.put(int.class,        IntGenerator::new);
        BUILTINS.put(Integer.class,    IntGenerator::new);
        BUILTINS.put(long.class,       LongGenerator::new);
        BUILTINS.put(Long.class,       LongGenerator::new);
        BUILTINS.put(float.class,      FloatGenerator::new);
        BUILTINS.put(Float.class,      FloatGenerator::new);
        BUILTINS.put(double.class,     DoubleGenerator::new);
        BUILTINS.put(Double.class,     DoubleGenerator::new);
        BUILTINS.put(char.class,       CharGenerator::letters);
        BUILTINS.put(Character.class,  CharGenerator::letters);
        BUILTINS.put(boolean.class,    BooleanGenerator::new);
        BUILTINS.put(Boolean.class,    BooleanGenerator::new);
        BUILTINS.put(String.class,     StringGenerator::letters);
        BUILTINS.put(LocalDate.class,  DateGenerator::new);
        BUILTINS.put(LocalTime.class,  TimeGenerator::new);
        BUILTINS.put(LocalDateTime.class, LocalDateTimeGenerator::new);
        BUILTINS.put(Instant.class,    InstantGenerator::new);
        BUILTINS.put(ZonedDateTime.class, ZonedDateTimeGenerator::new);
        BUILTINS.put(UUID.class,       UUIDGenerator::new);
    }

    /**
     * Safe zero-values used when a primitive field cannot be assigned its resolved value
     * (e.g. at max depth, or when {@code ignoreErrors=true}).
     */
    private static final Map<Class<?>, Object> PRIMITIVE_DEFAULTS = new HashMap<>();

    static {
        PRIMITIVE_DEFAULTS.put(byte.class,    (byte)  0);
        PRIMITIVE_DEFAULTS.put(short.class,   (short) 0);
        PRIMITIVE_DEFAULTS.put(int.class,     0);
        PRIMITIVE_DEFAULTS.put(long.class,    0L);
        PRIMITIVE_DEFAULTS.put(float.class,   0.0f);
        PRIMITIVE_DEFAULTS.put(double.class,  0.0);
        PRIMITIVE_DEFAULTS.put(char.class,    '\0');
        PRIMITIVE_DEFAULTS.put(boolean.class, false);
    }

    private final ObjectGeneratorConfig config;
    private final ObjectPool pool;

    FieldGeneratorResolver(ObjectGeneratorConfig config, ObjectPool pool) {
        this.config = config;
        this.pool   = pool;
    }

    // ── Primary entry point ───────────────────────────────────────────────────

    /**
     * Resolve and generate a value for a field whose generic type is known.
     *
     * @param genericType  the full generic type (e.g. {@code List<String>}) for collection resolution
     * @param rawType      the erasure of {@code genericType}
     * @param fieldName    name of the field (used for field-level override lookup)
     * @param ownerType    class that declares the field
     * @param currentDepth nesting depth of the parent {@link ObjectGenerator} (0 = root)
     * @return generated value, or a safe default / {@code null} when the type is unsupported
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    Object resolveAndGenerate(Type genericType, Class<?> rawType,
                              String fieldName, Class<?> ownerType, int currentDepth) {

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

        // ── 3. Built-in (primitives, wrappers, String, JSR-310, UUID) ─────────
        var builtinFactory = BUILTINS.get(rawType);
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
        if (List.class.isAssignableFrom(rawType) || Set.class.isAssignableFrom(rawType)) {
            Class<?> elem = typeArg(genericType, 0);
            List<Object> els = new ArrayList<>(DEFAULT_ELEMENT_COUNT);
            for (int i = 0; i < DEFAULT_ELEMENT_COUNT; i++) {
                els.add(resolveAndGenerate(elem, elem, fieldName + "[]", ownerType, currentDepth));
            }
            return List.class.isAssignableFrom(rawType)
                    ? Collections.unmodifiableList(els)
                    : new LinkedHashSet<>(els);
        }

        // ── 5c. Map ───────────────────────────────────────────────────────────
        if (Map.class.isAssignableFrom(rawType)) {
            Class<?> k = typeArg(genericType, 0);
            Class<?> v = typeArg(genericType, 1);
            Map<Object, Object> map = new LinkedHashMap<>();
            for (int i = 0; i < DEFAULT_ELEMENT_COUNT; i++) {
                Object key = resolveAndGenerate(k, k, fieldName + ".key", ownerType, currentDepth);
                Object val = resolveAndGenerate(v, v, fieldName + ".val", ownerType, currentDepth);
                if (key != null) map.put(key, val);
            }
            return Collections.unmodifiableMap(map);
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
        return resolveAndGenerate(rawType, rawType, fieldName, ownerType, currentDepth);
    }

    // ── Array generation ──────────────────────────────────────────────────────

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

    // ── Helpers ───────────────────────────────────────────────────────────────

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
