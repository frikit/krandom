/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.object;

import org.github.krandom.generator.Generator;
import org.github.krandom.generator.object.exception.ObjectGenerationException;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Generates random instances of any Java class or record by introspecting its structure
 * at runtime and delegating field/component population to {@link FieldGeneratorResolver}.
 *
 * <h3>Supported types</h3>
 * <ul>
 *   <li><b>Records</b> — all components are populated and the canonical constructor is invoked.</li>
 *   <li><b>Plain classes</b> — instantiated via a public or package-private no-arg constructor;
 *       all non-static, non-final, non-synthetic instance fields are then populated via
 *       reflection (parent class fields included).</li>
 *   <li><b>Nested objects</b> — resolved recursively up to {@link ObjectGeneratorConfig#getMaxDepth()}.</li>
 *   <li><b>Enum fields</b> — a random constant is selected.</li>
 * </ul>
 *
 * <h3>Usage</h3>
 * <pre>{@code
 *   // Minimal
 *   Person p = new ObjectGenerator<>(Person.class).generate();
 *
 *   // With configuration
 *   ObjectGeneratorConfig config = ObjectGeneratorConfig.builder()
 *       .maxDepth(3)
 *       .override(String.class, () -> "test-value")
 *       .override(Person.class, "firstName", () -> "Alice")
 *       .build();
 *
 *   ObjectGenerator<Person> gen = new ObjectGenerator<>(Person.class, config);
 *   List<Person> people = gen.generateList(100);
 *
 *   // Records
 *   PersonRecord r = new ObjectGenerator<>(PersonRecord.class).generate();
 * }</pre>
 *
 * @param <T> the type to generate
 */
public final class ObjectGenerator<T> implements Generator<T> {

    private final Class<T> type;
    private final ObjectGeneratorConfig config;
    private final FieldGeneratorResolver resolver;
    private final int depth;

    // ── Public constructors ───────────────────────────────────────────────────

    /** Creates a generator with default configuration. */
    public ObjectGenerator(Class<T> type) {
        this(type, ObjectGeneratorConfig.defaults(), 0);
    }

    /** Creates a generator with custom configuration. */
    public ObjectGenerator(Class<T> type, ObjectGeneratorConfig config) {
        this(type, config, 0);
    }

    /** Internal constructor — depth is managed by {@link FieldGeneratorResolver}. */
    ObjectGenerator(Class<T> type, ObjectGeneratorConfig config, int depth) {
        this.type     = Objects.requireNonNull(type,   "type must not be null");
        this.config   = Objects.requireNonNull(config, "config must not be null");
        this.depth    = depth;
        this.resolver = new FieldGeneratorResolver(config);
    }

    // ── Generator<T> ─────────────────────────────────────────────────────────

    @Override
    public T generate() {
        try {
            return type.isRecord() ? generateRecord() : generateClass();
        } catch (ObjectGenerationException e) {
            throw e; // re-throw as-is
        } catch (ReflectiveOperationException e) {
            throw new ObjectGenerationException(
                    "Failed to generate instance of " + type.getName() + ": " + e.getMessage(), e);
        }
    }

    // ── Record population ─────────────────────────────────────────────────────

    private T generateRecord() throws ReflectiveOperationException {
        RecordComponent[] components = type.getRecordComponents();

        // Build parallel arrays: types[] for the constructor lookup, args[] for invocation
        Class<?>[] paramTypes = Arrays.stream(components)
                .map(RecordComponent::getType)
                .toArray(Class[]::new);

        Object[] args = new Object[components.length];
        for (int i = 0; i < components.length; i++) {
            args[i] = resolver.resolveAndGenerate(
                    components[i].getType(),
                    components[i].getName(),
                    type,
                    depth);
        }

        Constructor<T> canonical = type.getDeclaredConstructor(paramTypes);
        canonical.setAccessible(true);
        return canonical.newInstance(args);
    }

    // ── Class population ──────────────────────────────────────────────────────

    private T generateClass() throws ReflectiveOperationException {
        Constructor<T> ctor = findNoArgConstructor();
        ctor.setAccessible(true);
        T instance = ctor.newInstance();

        for (Field field : collectSettableFields(type)) {
            field.setAccessible(true);
            Object value = resolver.resolveAndGenerate(
                    field.getType(),
                    field.getName(),
                    field.getDeclaringClass(),
                    depth);
            try {
                field.set(instance, value);
            } catch (IllegalAccessException | IllegalArgumentException e) {
                if (!config.isIgnoreErrors()) {
                    throw new ObjectGenerationException(
                            "Could not set field '" + field.getDeclaringClass().getSimpleName()
                                    + "." + field.getName() + "' to value " + value, e);
                }
                // ignoreErrors=true: silently leave field at its initialized value
            }
        }
        return instance;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private Constructor<T> findNoArgConstructor() {
        try {
            return type.getDeclaredConstructor();
        } catch (NoSuchMethodException e) {
            throw new ObjectGenerationException(
                    "No no-arg constructor found on class '" + type.getName() + "'. " +
                    "Add a no-arg constructor, or use a record if all fields are set at construction time.",
                    e);
        }
    }

    /**
     * Collect all instance fields that can be set after construction:
     * non-static, non-final, non-synthetic. Walks the full class hierarchy up to
     * (but not including) {@link Object}.
     */
    private List<Field> collectSettableFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            for (Field f : current.getDeclaredFields()) {
                int mods = f.getModifiers();
                if (Modifier.isStatic(mods))  continue;  // class-level, not instance
                if (Modifier.isFinal(mods))   continue;  // immutable after construction
                if (f.isSynthetic())           continue;  // compiler-generated (e.g. this$0)
                fields.add(f);
            }
            current = current.getSuperclass();
        }
        return fields;
    }

    // ── Diagnostic ───────────────────────────────────────────────────────────

    @Override
    public String toString() {
        return "ObjectGenerator{type=" + type.getName() + ", depth=" + depth +
               ", maxDepth=" + config.getMaxDepth() + "}";
    }
}
