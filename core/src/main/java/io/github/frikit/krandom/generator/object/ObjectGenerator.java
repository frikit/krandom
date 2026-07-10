/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.object;

import io.github.frikit.krandom.generator.Generator;
import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.GenerationContext;
import io.github.frikit.krandom.generator.failure.GenerationFailureCategory;
import io.github.frikit.krandom.generator.failure.GenerationFailureContext;
import io.github.frikit.krandom.generator.failure.GenerationOperation;
import io.github.frikit.krandom.generator.object.exception.ObjectGenerationException;
import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.lang.reflect.RecordComponent;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Generates random instances of any Java class or record by introspecting its structure
 * at runtime and delegating field/component population to {@link FieldGeneratorResolver}.
 *
 * <p><b>Supported types</b>
 * <ul>
 *   <li><b>Records</b> — all components are populated and the canonical constructor is invoked.</li>
 *   <li><b>Plain classes</b> — constructors are invoked by default. A no-arg constructor is
 *       preferred; otherwise one unambiguous declared constructor is populated through the same
 *       resolver used for fields. Objenesis bypass is available only through {@link
 *       ObjectConstructionPolicy#UNSAFE_CONSTRUCTOR_BYPASS}.</li>
 *   <li><b>Nested objects</b> — resolved recursively up to the configured object max depth.</li>
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
 *   // Preferred public configuration path
 *   GeneratorConfig config = GeneratorConfig.builder()
 *       .objectMaxDepth(3)
 *       .objectOverride(String.class, () -> "test-value")
 *       .objectOverride(Person.class, "firstName", () -> "Alice")
 *       .objectExcludeField("password")
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

    /**
     * Classloader-aware cache of settable field lists per class.
     */
    private static final ClassValue<List<Field>> SETTABLE_FIELDS = new ClassValue<>() {
        @Override
        protected List<Field> computeValue(Class<?> type) {
            return doCollectSettableFields(type);
        }
    };

    /**
     * Classloader-aware cache of record component metadata per record class.
     */
    private static final ClassValue<RecordMeta> RECORD_META = new ClassValue<>() {
        @Override
        protected RecordMeta computeValue(Class<?> type) {
            return buildRecordMeta(type);
        }
    };

    private record RecordMeta(RecordComponent[] components, Field[] backingFields, Class<?>[] paramTypes) {}

    private final Class<T>              type;
    private final ObjectGeneratorConfig config;
    private final ObjectPool            pool;
    private final UniqueFieldTracker    uniqueFieldTracker;
    private final int                   depth;
    private final Long                  generationSeed;
    private final Random                topLevelSeedSequence;
    private final Map<TypeVariable<?>, Type> typeBindings;

    // ── Public constructors ───────────────────────────────────────────────────

    /**
     * Creates a generator with default configuration.
     */
    public ObjectGenerator(Class<T> type) {
        this(type,
             ObjectGeneratorConfig.builder().generatorConfig(GeneratorConfig.defaults()).build(),
             0,
             null,
             null,
             new UniqueFieldTracker());
    }

    /**
     * Creates a generator that uses the shared root configuration defaults.
     */
    public ObjectGenerator(Class<T> type, GeneratorConfig config) {
        this(type,
             ObjectGeneratorConfig.builder().generatorConfig(config).build(),
             0,
             null,
             null,
             new UniqueFieldTracker());
    }

    /**
     * Package-private bridge for object-local config adapters.
     */
    ObjectGenerator(Class<T> type, ObjectGeneratorConfig config) {
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
        this(type,
             config,
             depth,
             pool,
             generationSeed,
             uniqueFieldTracker,
             ResolvedType.bindingsFor(Objects.requireNonNull(type, "type must not be null")));
    }

    ObjectGenerator(Class<T> type,
                    ObjectGeneratorConfig config,
                    int depth,
                    ObjectPool pool,
                    Long generationSeed,
                    UniqueFieldTracker uniqueFieldTracker,
                    Map<? extends TypeVariable<?>, ? extends Type> typeBindings) {
        this.type = Objects.requireNonNull(type, "type must not be null");
        this.config = Objects.requireNonNull(config, "config must not be null");
        this.depth = depth;
        this.pool = pool;
        this.generationSeed = generationSeed;
        this.uniqueFieldTracker = Objects.requireNonNull(uniqueFieldTracker, "uniqueFieldTracker must not be null");
        this.typeBindings = Map.copyOf(Objects.requireNonNull(typeBindings, "typeBindings must not be null"));
        this.topLevelSeedSequence = depth == 0 && pool == null
                                    ? config.getGeneratorConfig().getSeed().isPresent()
                                      ? config.getGeneratorConfig().createRandom()
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
                type,
                config,
                0,
                new ObjectPool(config.getObjectPoolSize()),
                nextGenerationSeed(),
                uniqueFieldTracker,
                typeBindings);
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
                type,
                config,
                0,
                new ObjectPool(config.getObjectPoolSize()),
                nextGenerationSeed(),
                uniqueFieldTracker,
                typeBindings);
            return scoped.populateWithPool(instance);
        }
        return populateWithPool(instance);
    }

    // ── Class population ──────────────────────────────────────────────────────

    private T generateWithPool() {
        var contextualFactory = config.getContextualTypeOverride(type);
        if (contextualFactory.isPresent()) {
            return generateFromRootFactory(
                "contextual type override",
                () -> contextualFactory.get().generate(new GenerationContext("$root", type, depth)));
        }
        var factory = config.getTypeOverride(type);
        if (factory.isPresent()) {
            return generateFromRootFactory("type override", factory.get()::generate);
        }

        FieldGeneratorResolver resolver =
            new FieldGeneratorResolver(
                config,
                Objects.requireNonNull(pool, "pool must not be null"),
                uniqueFieldTracker,
                generationSeed,
                typeBindings);
        SemanticCoherenceAdjuster coherenceAdjuster =
            new SemanticCoherenceAdjuster(config, uniqueFieldTracker, generationSeed, depth);
        try {
            return type.isRecord() ? generateRecord(resolver, coherenceAdjuster) : generateClass(resolver, coherenceAdjuster);
        } catch (InvocationTargetException e) {
            throw constructionFailure(e.getTargetException());
        } catch (ReflectiveOperationException e) {
            throw constructionFailure(e);
        }
    }

    private T generateFromRootFactory(String factoryKind, Supplier<?> factory) {
        try {
            Object value = factory.get();
            if (value == null) {
                throw new IllegalStateException(factoryKind + " returned null");
            }
            if (!type.isInstance(value)) {
                throw new IllegalArgumentException(
                    factoryKind + " returned " + value.getClass().getName()
                    + " for " + type.getName());
            }
            return type.cast(value);
        } catch (RuntimeException factoryFailure) {
            String path = typePath();
            GenerationFailureContext context = new GenerationFailureContext(
                GenerationFailureCategory.CUSTOM_GENERATOR,
                GenerationOperation.CONSTRUCT,
                path,
                type,
                type.getTypeName(),
                depth,
                -1);
            ObjectGenerationFailurePolicy failurePolicy = new ObjectGenerationFailurePolicy(
                config.isIgnoreErrors(), config.getGeneratorConfig().getGenerationFailureListener());
            Object fallback = failurePolicy.handle(
                new ObjectGenerationException(
                    "Root " + factoryKind + " failed for '" + path + "' (declared type "
                    + type.getTypeName() + ", depth " + depth + ")",
                    context,
                    factoryFailure),
                null);
            return type.cast(fallback);
        }
    }

    private ObjectGenerationException constructionFailure(Throwable cause) {
        String path = typePath();
        GenerationFailureContext context = new GenerationFailureContext(
            GenerationFailureCategory.CONSTRUCTION,
            GenerationOperation.CONSTRUCT,
            path,
            type,
            type.getTypeName(),
            depth,
            -1);
        return new ObjectGenerationException(
            "Could not construct type at '" + path + "' (declared type "
            + type.getTypeName() + ", depth " + depth + ")",
            context,
            cause);
    }

    private String typePath() {
        String simpleName = type.getSimpleName();
        return simpleName.isBlank() ? type.getName() : simpleName;
    }

    private T populateWithPool(T instance) {
        FieldGeneratorResolver resolver =
            new FieldGeneratorResolver(
                config,
                Objects.requireNonNull(pool, "pool must not be null"),
                uniqueFieldTracker,
                generationSeed,
                typeBindings);
        populateClass(instance,
                      resolver,
                      new SemanticCoherenceAdjuster(config, uniqueFieldTracker, generationSeed, depth),
                      collectSettableFields(type),
                      config.isOverrideDefaultInitialization());
        return instance;
    }

    private Long nextGenerationSeed() {
        return topLevelSeedSequence != null ? Long.valueOf(topLevelSeedSequence.nextLong()) : generationSeed;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private T generateRecord(FieldGeneratorResolver resolver,
                             SemanticCoherenceAdjuster coherenceAdjuster) throws ReflectiveOperationException {
        RecordMeta meta = RECORD_META.get(type);
        RecordComponent[] components = meta.components();
        Field[] backingFields = meta.backingFields();

        Object[] args = new Object[components.length];
        for (int i = 0; i < components.length; i++) {
            RecordComponent comp = components[i];
            if (config.shouldExclude(backingFields[i])) {
                args[i] = defaultForType(comp.getType());
            } else {
                args[i] = resolver.resolveAndGenerate(
                    comp.getGenericType(),
                    comp.getType(),
                    comp.getName(),
                    type,
                    depth,
                    backingFields[i]);
            }
        }
        coherenceAdjuster.adjustRecordArguments(type, components, backingFields, args);

        Constructor<T> canonical = type.getDeclaredConstructor(meta.paramTypes());
        canonical.setAccessible(true);
        return canonical.newInstance(args);
    }

    private static RecordMeta buildRecordMeta(Class<?> recordType) {
        RecordComponent[] components = recordType.getRecordComponents();
        Class<?>[] paramTypes = Arrays.stream(components)
                                      .map(RecordComponent::getType)
                                      .toArray(Class[]::new);
        Map<String, Field> fieldsByName = Arrays.stream(recordType.getDeclaredFields())
                                                .collect(Collectors.toMap(Field::getName, field -> field));
        Field[] backingFields = Arrays.stream(components)
            .map(c -> Objects.requireNonNull(fieldsByName.get(c.getName()),
                                             "record backing field missing for " + recordType.getName() + "." + c.getName()))
            .toArray(Field[]::new);
        return new RecordMeta(components, backingFields, paramTypes);
    }

    private T generateClass(FieldGeneratorResolver resolver,
                            SemanticCoherenceAdjuster coherenceAdjuster) throws ReflectiveOperationException {
        validateConstructibleType();
        List<Field> settableFields = collectSettableFields(type);
        T instance = instantiate(resolver); // may throw ReflectiveOperationException for throwing constructors
        populateClass(instance, resolver, coherenceAdjuster, settableFields, true);
        return instance;
    }

    private void populateClass(T instance,
                               FieldGeneratorResolver resolver,
                               SemanticCoherenceAdjuster coherenceAdjuster,
                               List<Field> settableFields,
                               boolean allowOverwriteExisting) {
        ObjectGenerationFailurePolicy failurePolicy = new ObjectGenerationFailurePolicy(
            config.isIgnoreErrors(), config.getGeneratorConfig().getGenerationFailureListener());
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
                String fieldContext = "field '" + field.getDeclaringClass().getSimpleName()
                                      + "." + field.getName() + "' (declared type "
                                      + field.getGenericType().getTypeName() + ", depth " + depth + ")";
                GenerationFailureContext failureContext = new GenerationFailureContext(
                    GenerationFailureCategory.ASSIGNMENT,
                    GenerationOperation.ASSIGN,
                    field.getDeclaringClass().getSimpleName() + "." + field.getName(),
                    field.getDeclaringClass(),
                    field.getGenericType().getTypeName(),
                    depth,
                    -1);
                failurePolicy.handle(
                    new ObjectGenerationException("Could not set " + fieldContext, failureContext, e),
                    null);
            }
        }
        coherenceAdjuster.adjustInstance(type, instance, settableFields, allowOverwriteExisting);
    }

    /**
     * Instantiate {@code type} without populating fields.
     *
     * <p>Attempts to use a no-arg constructor first (public or package-private). In safe mode, one
     * unambiguous declared constructor is resolved when no no-arg constructor exists. The unsafe
     * compatibility policy instead preserves the legacy Objenesis fallback.
     *
     * <p>Any {@link ReflectiveOperationException} thrown by {@link Constructor#newInstance}
     * (e.g. {@link java.lang.reflect.InvocationTargetException} when the constructor body
     * throws) propagates to the caller and is wrapped by {@link #generate()}.
     *
     * @throws ReflectiveOperationException if the constructor is found but throws at runtime
     */
    private T instantiate(FieldGeneratorResolver resolver) throws ReflectiveOperationException {
        try {
            Constructor<T> ctor = type.getDeclaredConstructor();
            ctor.setAccessible(true);
            return ctor.newInstance();
        } catch (NoSuchMethodException ignored) {
            if (config.getConstructionPolicy() == ObjectConstructionPolicy.UNSAFE_CONSTRUCTOR_BYPASS) {
                return OBJENESIS.newInstance(type);
            }
            return invokeUniqueDeclaredConstructor(resolver);
        }
    }

    private void validateConstructibleType() throws ReflectiveOperationException {
        int modifiers = type.getModifiers();
        boolean unsupported = type.isArray()
                              || type.isPrimitive()
                              || type.isEnum()
                              || type.isAnnotation()
                              || type.isInterface()
                              || Modifier.isAbstract(modifiers)
                              || type.isLocalClass()
                              || type.isAnonymousClass()
                              || (type.isMemberClass() && !Modifier.isStatic(modifiers));
        if (unsupported) {
            throw new ReflectiveOperationException(
                "Construction policy " + config.getConstructionPolicy()
                + " does not support root type " + type.getName());
        }
    }

    @SuppressWarnings("unchecked")
    private T invokeUniqueDeclaredConstructor(FieldGeneratorResolver resolver) throws ReflectiveOperationException {
        Constructor<?>[] candidates = type.getDeclaredConstructors();
        if (candidates.length != 1) {
            throw new ReflectiveOperationException(
                "Construction policy " + ObjectConstructionPolicy.SAFE_CONSTRUCTORS
                + " requires one unambiguous declared constructor for " + type.getName()
                + "; found " + candidates.length);
        }

        Constructor<T> constructor = (Constructor<T>) candidates[0];
        constructor.setAccessible(true);
        Parameter[] parameters = constructor.getParameters();
        Object[] arguments = new Object[parameters.length];
        for (int index = 0; index < parameters.length; index++) {
            Parameter parameter = parameters[index];
            arguments[index] = resolver.resolveAndGenerate(
                parameter.getParameterizedType(),
                parameter.getType(),
                "constructorArg" + index,
                type,
                depth,
                parameter);
        }
        return constructor.newInstance(arguments);
    }

    /**
     * Collect all instance fields that can be set after construction:
     * non-static and non-final. Walks the full class hierarchy up to
     * (but not including) {@link Object}. Results are cached per class.
     */
    private List<Field> collectSettableFields(Class<?> clazz) {
        return SETTABLE_FIELDS.get(clazz);
    }

    private static List<Field> doCollectSettableFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        Class<?> current = clazz;
        while (current != Object.class) {
            // getDeclaredFields() order is JVM-implementation-specific; sort each class's fields by
            // name so per-field seed allocation (and thus seeded output) is reproducible across JDK
            // builds, vendors, and instrumentation agents. Hierarchy order (subclass before
            // superclass) is already deterministic via getSuperclass().
            Field[] declared = current.getDeclaredFields();
            Arrays.sort(declared, Comparator.comparing(Field::getName));
            for (Field f : declared) {
                int mods = f.getModifiers();
                if (Modifier.isStatic(mods)) continue;  // class-level, not instance
                if (Modifier.isFinal(mods)) continue;  // immutable after construction
                f.setAccessible(true);
                fields.add(f);
            }
            current = current.getSuperclass();
        }
        return Collections.unmodifiableList(fields);
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
