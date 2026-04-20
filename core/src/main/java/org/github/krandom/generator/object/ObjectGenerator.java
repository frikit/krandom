/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.object;

import org.github.krandom.generator.Generator;
import org.github.krandom.generator.GeneratorConfig;
import org.github.krandom.generator.object.exception.ObjectGenerationException;
import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Random;

/**
 * Generates random instances of any Java class or record by introspecting its structure
 * at runtime and delegating field/component population to {@link FieldGeneratorResolver}.
 *
 * <p><b>Supported types</b>
 * <ul>
 *   <li><b>Records</b> — all components are populated and the canonical constructor is invoked.</li>
 *   <li><b>Plain classes</b> — instantiated via a public or package-private no-arg constructor
 *       when available; when absent, Objenesis bypasses the constructor entirely so that classes
 *       with only all-args constructors can still be populated via field reflection.</li>
 *   <li><b>Nested objects</b> — resolved recursively up to {@link ObjectGeneratorConfig#getMaxDepth()}.</li>
 *   <li><b>Enum fields</b> — a random constant is selected.</li>
 *   <li><b>Arrays</b> — auto-populated using the shared collection-size defaults
 *       (default 1 to 10 elements).</li>
 *   <li><b>Collections ({@code List}, {@code Set}, {@code Map})</b> — auto-populated using the
 *       shared collection-size defaults and the declared generic type.</li>
 *   <li><b>Circular references</b> — detected via an {@link ObjectPool} and broken by
 *       returning a previously cached instance (or {@code null}) instead of recursing.</li>
 * </ul>
 *
 * <p><b>Usage</b>
 * <pre>{@code
 *   // Minimal
 *   Person p = new ObjectGenerator<>(Person.class).generate();
 *
 *   // With configuration
 *   ObjectGeneratorConfig config = ObjectGeneratorConfig.builder()
 *       .maxDepth(3)
 *       .override(String.class, () -> "test-value")
 *       .override(Person.class, "firstName", () -> "Alice")
 *       .excludeField("password")
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

    /**
     * Thread-safe Objenesis instance; caches instantiation strategies per class.
     */
    private static final Objenesis OBJENESIS = new ObjenesisStd();

    private final Class<T>              type;
    private final ObjectGeneratorConfig config;
    private final ObjectPool            pool;
    private final UniqueFieldTracker    uniqueFieldTracker;
    private final int                   depth;
    private final Long                  generationSeed;
    private final Random                topLevelSeedSequence;

    // ── Public constructors ───────────────────────────────────────────────────

    /**
     * Creates a generator with default configuration.
     */
    public ObjectGenerator(Class<T> type) {
        this(type, ObjectGeneratorConfig.defaults());
    }

    /**
     * Creates a generator that uses the shared root configuration defaults.
     */
    public ObjectGenerator(Class<T> type, GeneratorConfig config) {
        this(type, ObjectGeneratorConfig.builder().generatorConfig(config).build());
    }

    /**
     * Creates a generator with custom configuration.
     */
    public ObjectGenerator(Class<T> type, ObjectGeneratorConfig config) {
        this(type, config, 0, null, null, new UniqueFieldTracker());
    }

    /**
     * Internal constructor — depth and pool are managed by {@link FieldGeneratorResolver}.
     */
    ObjectGenerator(Class<T> type,
                    ObjectGeneratorConfig config,
                    int depth,
                    ObjectPool pool,
                    Long generationSeed,
                    UniqueFieldTracker uniqueFieldTracker) {
        this.type = Objects.requireNonNull(type, "type must not be null");
        this.config = Objects.requireNonNull(config, "config must not be null");
        this.depth = depth;
        this.pool = pool;
        this.generationSeed = generationSeed;
        this.uniqueFieldTracker = Objects.requireNonNull(uniqueFieldTracker, "uniqueFieldTracker must not be null");
        this.topLevelSeedSequence = depth == 0 && pool == null
                                    ? config.getGeneratorConfig().getSeed().isPresent()
                                      ? new Random(config.getGeneratorConfig().getSeed().getAsLong())
                                      : null
                                    : null;
    }

    // ── Generator<T> ─────────────────────────────────────────────────────────

    /**
     * Returns the JVM default value for a primitive type, or {@code null} for reference types.
     * Used when a record component is excluded from generation.
     * Each primitive type gets its exact wrapper to satisfy {@link java.lang.reflect.Constructor#newInstance}.
     */
    private static Object defaultForType(Class<?> type) {
        if (type == boolean.class) return false;
        if (type == byte.class) return (byte) 0;
        if (type == short.class) return (short) 0;
        if (type == int.class) return 0;
        if (type == long.class) return 0L;
        if (type == float.class) return 0.0f;
        if (type == double.class) return 0.0;
        if (type == char.class) return '\0';
        return null;
    }

    private static boolean isDefaultValue(Object value, Class<?> type) {
        if (!type.isPrimitive()) return value == null;
        return Objects.equals(value, defaultForType(type));
    }

    // ── Record population ─────────────────────────────────────────────────────

    @Override
    public T generate() {
        if (depth == 0 && pool == null) {
            // Fresh pool for each top-level generation call to prevent cross-call leakage.
            ObjectGenerator<T> scoped = new ObjectGenerator<>(
                type, config, 0, new ObjectPool(config.getObjectPoolSize()), nextGenerationSeed(), uniqueFieldTracker);
            return scoped.generateWithPool();
        }
        return generateWithPool();
    }

    /**
     * Populates an existing mutable instance in place using this generator's configuration.
     *
     * <p>Records are not supported because they are immutable after construction.
     */
    T populate(T instance) {
        Objects.requireNonNull(instance, "instance must not be null");
        if (!type.isInstance(instance)) {
            throw new IllegalArgumentException(
                "instance must be assignable to " + type.getName() + ", got " + instance.getClass().getName());
        }
        if (type.isRecord()) {
            throw new IllegalArgumentException("populate(existing) does not support record types: " + type.getName());
        }
        if (depth == 0 && pool == null) {
            ObjectGenerator<T> scoped = new ObjectGenerator<>(
                type, config, 0, new ObjectPool(config.getObjectPoolSize()), nextGenerationSeed(), uniqueFieldTracker);
            return scoped.populateWithPool(instance);
        }
        return populateWithPool(instance);
    }

    // ── Class population ──────────────────────────────────────────────────────

    private T generateWithPool() {
        FieldGeneratorResolver resolver =
            new FieldGeneratorResolver(
                config,
                Objects.requireNonNull(pool, "pool must not be null"),
                uniqueFieldTracker,
                generationSeed);
        SemanticCoherenceAdjuster coherenceAdjuster = new SemanticCoherenceAdjuster(config, uniqueFieldTracker);
        try {
            return type.isRecord() ? generateRecord(resolver, coherenceAdjuster) : generateClass(resolver, coherenceAdjuster);
        } catch (ReflectiveOperationException e) {
            throw new ObjectGenerationException(
                "Failed to generate instance of " + type.getName() + ": " + e.getMessage(), e);
        }
    }

    private T populateWithPool(T instance) {
        FieldGeneratorResolver resolver =
            new FieldGeneratorResolver(
                config,
                Objects.requireNonNull(pool, "pool must not be null"),
                uniqueFieldTracker,
                generationSeed);
        populateClass(instance, resolver, new SemanticCoherenceAdjuster(config, uniqueFieldTracker), config.isOverrideDefaultInitialization());
        return instance;
    }

    private Long nextGenerationSeed() {
        return topLevelSeedSequence != null ? Long.valueOf(topLevelSeedSequence.nextLong()) : generationSeed;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private T generateRecord(FieldGeneratorResolver resolver,
                             SemanticCoherenceAdjuster coherenceAdjuster) throws ReflectiveOperationException {
        RecordComponent[] components = type.getRecordComponents();

        // Build parallel arrays: types[] for the constructor lookup, args[] for invocation
        Class<?>[] paramTypes = Arrays.stream(components)
                                      .map(RecordComponent::getType)
                                      .toArray(Class[]::new);

        Object[] args = new Object[components.length];
        Field[] backingFields = new Field[components.length];
        for (int i = 0; i < components.length; i++) {
            RecordComponent comp = components[i];
            // For a well-formed record, getDeclaredField(comp.getName()) never throws.
            // NoSuchFieldException (a ReflectiveOperationException) propagates to generate().
            Field backingField = type.getDeclaredField(comp.getName());
            backingFields[i] = backingField;
            if (config.shouldExclude(backingField)) {
                args[i] = defaultForType(comp.getType());
            } else {
                args[i] = resolver.resolveAndGenerate(
                    comp.getGenericType(),
                    comp.getType(),
                    comp.getName(),
                    type,
                    depth,
                    backingField);
            }
        }
        coherenceAdjuster.adjustRecordArguments(type, components, backingFields, args);

        Constructor<T> canonical = type.getDeclaredConstructor(paramTypes);
        canonical.setAccessible(true);
        return canonical.newInstance(args);
    }

    private T generateClass(FieldGeneratorResolver resolver,
                            SemanticCoherenceAdjuster coherenceAdjuster) throws ReflectiveOperationException {
        T instance = instantiate(); // may throw ReflectiveOperationException for throwing constructors
        populateClass(instance, resolver, coherenceAdjuster, true);
        return instance;
    }

    private void populateClass(T instance,
                               FieldGeneratorResolver resolver,
                               SemanticCoherenceAdjuster coherenceAdjuster,
                               boolean allowOverwriteExisting) {
        List<Field> settableFields = collectSettableFields(type);
        for (Field field : settableFields) {
            if (config.shouldExclude(field)) continue; // exclusion check
            field.setAccessible(true);
            if (!config.isOverrideDefaultInitialization() && hasNonDefaultValue(instance, field)) {
                continue;
            }
            Object value = resolver.resolveAndGenerate(
                field.getGenericType(),
                field.getType(),
                field.getName(),
                field.getDeclaringClass(),
                depth,
                field);
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
        coherenceAdjuster.adjustInstance(type, instance, settableFields, allowOverwriteExisting);
    }

    /**
     * Instantiate {@code type} without populating fields.
     *
     * <p>Attempts to use a no-arg constructor first (public or package-private).
     * If none is found, falls back to Objenesis which bypasses the constructor entirely —
     * allowing generation for classes that only have all-args constructors.
     *
     * <p>Any {@link ReflectiveOperationException} thrown by {@link Constructor#newInstance}
     * (e.g. {@link java.lang.reflect.InvocationTargetException} when the constructor body
     * throws) propagates to the caller and is wrapped by {@link #generate()}.
     *
     * @throws ReflectiveOperationException if the constructor is found but throws at runtime
     */
    private T instantiate() throws ReflectiveOperationException {
        try {
            Constructor<T> ctor = type.getDeclaredConstructor();
            ctor.setAccessible(true);
            return ctor.newInstance();
        } catch (NoSuchMethodException ignored) {
            return OBJENESIS.newInstance(type);
        }
    }

    /**
     * Collect all instance fields that can be set after construction:
     * non-static and non-final. Walks the full class hierarchy up to
     * (but not including) {@link Object}.
     */
    private List<Field> collectSettableFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        Class<?> current = clazz;
        while (current != Object.class) {
            for (Field f : current.getDeclaredFields()) {
                int mods = f.getModifiers();
                if (Modifier.isStatic(mods)) continue;  // class-level, not instance
                if (Modifier.isFinal(mods)) continue;  // immutable after construction
                fields.add(f);
            }
            current = current.getSuperclass();
        }
        return fields;
    }

    private boolean hasNonDefaultValue(T instance, Field field) {
        try {
            Object currentValue = field.get(instance);
            return !isDefaultValue(currentValue, field.getType());
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Field should be accessible: "
                                            + field.getDeclaringClass().getSimpleName() + "." + field.getName(), e);
        }
    }

    // ── Diagnostic ───────────────────────────────────────────────────────────

    @Override
    public String toString() {
        return "ObjectGenerator{type=" + type.getName() + ", depth=" + depth +
               ", maxDepth=" + config.getMaxDepth() + "}";
    }
}
