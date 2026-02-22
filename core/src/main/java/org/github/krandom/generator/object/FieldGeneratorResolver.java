/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.object;

import org.github.krandom.generator.Generator;
import org.github.krandom.generator.base.*;
import org.github.krandom.generator.object.exception.ObjectGenerationException;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Internal resolver: maps a field's ({@link Class}, name, owner) tuple to a generated value.
 *
 * <p>Resolution order (first match wins):
 * <ol>
 *   <li>Field-level override from {@link ObjectGeneratorConfig} ({@code "OwnerType.fieldName"})</li>
 *   <li>Type-level override from {@link ObjectGeneratorConfig}</li>
 *   <li>Built-in generator for Java primitives / wrappers / {@code String}</li>
 *   <li>Enum: random constant</li>
 *   <li>Depth guard: if {@code currentDepth >= maxDepth} return {@code null}</li>
 *   <li>Nested class or record: delegate to a child {@link ObjectGenerator}</li>
 *   <li>Unsupported type: return {@code null} (or throw if {@code ignoreErrors=false})</li>
 * </ol>
 */
final class FieldGeneratorResolver {

    /**
     * Factories for all built-in Java base types (both primitive and wrapper forms).
     * Each call to the supplier creates a fresh generator instance.
     */
    private static final Map<Class<?>, Supplier<Generator<?>>> BUILTINS = new HashMap<>();

    static {
        BUILTINS.put(byte.class,      ByteGenerator::new);
        BUILTINS.put(Byte.class,      ByteGenerator::new);
        BUILTINS.put(short.class,     ShortGenerator::new);
        BUILTINS.put(Short.class,     ShortGenerator::new);
        BUILTINS.put(int.class,       IntGenerator::new);
        BUILTINS.put(Integer.class,   IntGenerator::new);
        BUILTINS.put(long.class,      LongGenerator::new);
        BUILTINS.put(Long.class,      LongGenerator::new);
        BUILTINS.put(float.class,     FloatGenerator::new);
        BUILTINS.put(Float.class,     FloatGenerator::new);
        BUILTINS.put(double.class,    DoubleGenerator::new);
        BUILTINS.put(Double.class,    DoubleGenerator::new);
        BUILTINS.put(char.class,      CharGenerator::letters);
        BUILTINS.put(Character.class, CharGenerator::letters);
        BUILTINS.put(boolean.class,   BooleanGenerator::new);
        BUILTINS.put(Boolean.class,   BooleanGenerator::new);
        BUILTINS.put(String.class,    StringGenerator::letters);
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

    FieldGeneratorResolver(ObjectGeneratorConfig config) {
        this.config = config;
    }

    /**
     * Resolve and generate a value for a field.
     *
     * @param type         declared type of the field / record component
     * @param fieldName    name of the field (used for field-level override lookup)
     * @param ownerType    class that declares the field
     * @param currentDepth nesting depth of the parent {@link ObjectGenerator} (0 = root)
     * @return generated value, or a safe default / {@code null} when the type is unsupported
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    Object resolveAndGenerate(Class<?> type, String fieldName, Class<?> ownerType, int currentDepth) {

        // ── 1. Field-level override ───────────────────────────────────────────
        var fieldOverride = config.getFieldOverride(ownerType, fieldName);
        if (fieldOverride.isPresent()) {
            return fieldOverride.get().generate();
        }

        // ── 2. Type-level override ────────────────────────────────────────────
        var typeOverride = config.getTypeOverride(type);
        if (typeOverride.isPresent()) {
            return typeOverride.get().generate();
        }

        // ── 3. Built-in (primitives, wrappers, String) ────────────────────────
        var builtinFactory = BUILTINS.get(type);
        if (builtinFactory != null) {
            return builtinFactory.get().generate();
        }

        // ── 4. Enum ───────────────────────────────────────────────────────────
        if (type.isEnum()) {
            Object[] constants = type.getEnumConstants();
            if (constants.length == 0) return null;
            // Use a transient EnumGenerator (cheap since it holds an array reference)
            return new EnumGenerator((Class<? extends Enum>) type).generate();
        }

        // ── 5. Depth guard ────────────────────────────────────────────────────
        if (currentDepth >= config.getMaxDepth()) {
            // Return safe default for primitives; null for reference types
            return PRIMITIVE_DEFAULTS.getOrDefault(type, null);
        }

        // ── 6. Nested class or record ─────────────────────────────────────────
        if (isNestableType(type)) {
            try {
                return new ObjectGenerator<>(type, config, currentDepth + 1).generate();
            } catch (ObjectGenerationException e) {
                if (config.isIgnoreErrors()) return null;
                throw e;
            } catch (Exception e) {
                if (config.isIgnoreErrors()) return null;
                throw new ObjectGenerationException(
                        "Failed to generate nested type " + type.getName() + " for field '"
                                + ownerType.getSimpleName() + "." + fieldName + "'", e);
            }
        }

        // ── 7. Unsupported type ───────────────────────────────────────────────
        // Arrays, interfaces, annotations, etc. — leave as null for now.
        return PRIMITIVE_DEFAULTS.getOrDefault(type, null);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * Return {@code true} for concrete classes and records that {@link ObjectGenerator}
     * can instantiate. Excludes arrays, interfaces, abstract classes, and JDK types
     * (bootstrap-loaded classes) to avoid recursing into platform internals.
     */
    private boolean isNestableType(Class<?> type) {
        if (type.isArray())     return false;
        if (type.isInterface()) return false;
        if (java.lang.reflect.Modifier.isAbstract(type.getModifiers())) return false;
        // Bootstrap ClassLoader (null) loads all JDK platform classes — skip them
        if (type.getClassLoader() == null) return false;
        return true;
    }
}
