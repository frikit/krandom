/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.object;

import org.github.krandom.generator.ContextualGenerator;
import org.github.krandom.generator.Generator;
import org.github.krandom.generator.GeneratorConfig;
import org.github.krandom.generator.object.exception.ObjectGenerationException;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;

/**
 * Fluent fixture authoring API built on top of {@link ObjectGenerator}.
 *
 * <p>Use this when the default semantic object generation is close but you need a few
 * precise rules on top:
 *
 * <pre>{@code
 *   ObjectFaker<User> faker = new ObjectFaker<>(User.class)
 *       .ruleFor("firstName", () -> "Ada")
 *       .ruleFor("lastName", () -> "Lovelace")
 *       .ruleFor("email",
 *           user -> user.getFirstName().toLowerCase() + "." + user.getLastName().toLowerCase() + "@example.com")
 *       .ignore("password");
 *
 *   User one = faker.generate();
 *   List<User> many = faker.generateList(10);
 * }</pre>
 *
 * <p>This first version keeps rules at the root-object field level and reuses the same
 * configuration and semantic resolver as {@link ObjectGenerator}.
 *
 * @param <T> the root type being generated
 */
public final class ObjectFaker<T> implements Generator<T> {

    private final Class<T> type;
    private final ObjectGeneratorConfig baseConfig;
    private final Map<String, RuleTarget>                 ruleTargets          = new LinkedHashMap<>();
    private final Map<String, Generator<?>>               fieldRules           = new LinkedHashMap<>();
    private final Map<String, ContextualGenerator<?>>     contextualFieldRules = new LinkedHashMap<>();
    private final Map<String, Function<? super T, ?>>     dependentFieldRules  = new LinkedHashMap<>();
    private final Map<String, Consumer<ObjectFaker<T>>>   namedProfiles        = new LinkedHashMap<>();
    private final Set<String>                             ignoredFields        = new LinkedHashSet<>();
    private final Set<String>                             includedFields       = new LinkedHashSet<>();
    private final List<UnaryOperator<T>>                  postProcessors       = new ArrayList<>();
    private final Set<String>                             applyingProfiles     = new LinkedHashSet<>();
    private final Set<String>                             appliedProfiles      = new LinkedHashSet<>();

    private UniqueFieldTracker uniqueFieldTracker = new UniqueFieldTracker();
    private ObjectGenerator<T>  objectGenerator;

    /**
     * Creates a fixture authoring API with default object-generation settings.
     */
    public ObjectFaker(Class<T> type) {
        this(type, ObjectGeneratorConfig.defaults());
    }

    /**
     * Creates a fixture authoring API using the shared root configuration.
     */
    public ObjectFaker(Class<T> type, GeneratorConfig config) {
        this(type, ObjectGeneratorConfig.builder().generatorConfig(config).build());
    }

    /**
     * Creates a fixture authoring API with explicit object-generation configuration.
     */
    public ObjectFaker(Class<T> type, ObjectGeneratorConfig config) {
        this.type = Objects.requireNonNull(type, "type must not be null");
        this.baseConfig = Objects.requireNonNull(config, "config must not be null");
    }

    /**
     * Static factory with default configuration.
     */
    public static <T> ObjectFaker<T> of(Class<T> type) {
        return new ObjectFaker<>(type);
    }

    /**
     * Registers a deterministic field rule evaluated during base object generation.
     */
    public <V> ObjectFaker<T> ruleFor(String fieldName, Generator<? extends V> generator) {
        RuleTarget target = requireRuleTarget(fieldName, "ruleFor");
        ensureFieldAvailable(fieldName);
        fieldRules.put(target.fieldName(), Objects.requireNonNull(generator, "generator must not be null"));
        invalidateGeneratorState();
        return this;
    }

    /**
     * Registers a context-aware field rule evaluated during base object generation.
     */
    public <V> ObjectFaker<T> ruleForContext(String fieldName, ContextualGenerator<? extends V> generator) {
        RuleTarget target = requireRuleTarget(fieldName, "ruleFor");
        ensureFieldAvailable(fieldName);
        contextualFieldRules.put(target.fieldName(), Objects.requireNonNull(generator, "generator must not be null"));
        invalidateGeneratorState();
        return this;
    }

    /**
     * Registers a dependent field rule evaluated after the base object is generated.
     */
    public <V> ObjectFaker<T> ruleFor(String fieldName, Function<? super T, ? extends V> generator) {
        RuleTarget target = requireRuleTarget(fieldName, "dependent ruleFor");
        ensureFieldAvailable(fieldName);
        dependentFieldRules.put(target.fieldName(), Objects.requireNonNull(generator, "generator must not be null"));
        return this;
    }

    /**
     * Excludes one root field from generation.
     */
    public ObjectFaker<T> ignore(String fieldName) {
        RuleTarget target = requireRuleTarget(fieldName, "ignore");
        if (includedFields.contains(target.fieldName())) {
            throw new IllegalStateException("Field '" + fieldName + "' is already included");
        }
        ensureFieldAvailable(fieldName);
        ignoredFields.add(target.fieldName());
        invalidateGeneratorState();
        return this;
    }

    /**
     * Restricts generation to one root field. Once at least one include rule exists, root fields
     * not explicitly included or covered by a field rule are left untouched.
     */
    public ObjectFaker<T> include(String fieldName) {
        RuleTarget target = requireRuleTarget(fieldName, "include");
        if (ignoredFields.contains(target.fieldName())) {
            throw new IllegalStateException("Field '" + fieldName + "' is already ignored");
        }
        includedFields.add(target.fieldName());
        invalidateGeneratorState();
        return this;
    }

    /**
     * Restricts generation to multiple root fields.
     */
    public ObjectFaker<T> include(String... fieldNames) {
        Objects.requireNonNull(fieldNames, "fieldNames must not be null");
        for (String fieldName : fieldNames) {
            include(fieldName);
        }
        return this;
    }

    /**
     * Defines a reusable named profile that can later be applied with {@link #useProfile(String)}.
     */
    public ObjectFaker<T> profile(String name, Consumer<ObjectFaker<T>> profile) {
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(profile, "profile must not be null");
        if (namedProfiles.containsKey(name)) {
            throw new IllegalStateException("Profile '" + name + "' is already defined");
        }
        namedProfiles.put(name, profile);
        return this;
    }

    /**
     * Applies a previously defined named profile exactly once.
     */
    public ObjectFaker<T> useProfile(String name) {
        Objects.requireNonNull(name, "name must not be null");
        Consumer<ObjectFaker<T>> profile = namedProfiles.get(name);
        if (profile == null) {
            throw new IllegalArgumentException("Unknown profile '" + name + "' for " + type.getName());
        }
        if (applyingProfiles.contains(name)) {
            throw new IllegalStateException("Profile '" + name + "' is already being applied");
        }
        if (!appliedProfiles.add(name)) {
            throw new IllegalStateException("Profile '" + name + "' is already applied");
        }
        applyingProfiles.add(name);
        try {
            profile.accept(this);
            return this;
        } catch (RuntimeException e) {
            appliedProfiles.remove(name);
            throw e;
        } finally {
            applyingProfiles.remove(name);
        }
    }

    /**
     * Applies multiple named profiles in order.
     */
    public ObjectFaker<T> useProfile(String... names) {
        Objects.requireNonNull(names, "names must not be null");
        for (String name : names) {
            useProfile(name);
        }
        return this;
    }

    /**
     * Excludes multiple root fields from generation.
     */
    public ObjectFaker<T> ignore(String... fieldNames) {
        Objects.requireNonNull(fieldNames, "fieldNames must not be null");
        for (String fieldName : fieldNames) {
            ignore(fieldName);
        }
        return this;
    }

    /**
     * Applies a post-processor after generation. The returned object must not be {@code null}.
     */
    public ObjectFaker<T> postProcess(UnaryOperator<T> postProcessor) {
        postProcessors.add(Objects.requireNonNull(postProcessor, "postProcessor must not be null"));
        return this;
    }

    /**
     * Convenience hook for in-place mutation after generation.
     */
    public ObjectFaker<T> afterGenerate(Consumer<? super T> consumer) {
        Objects.requireNonNull(consumer, "consumer must not be null");
        return postProcess(value -> {
            consumer.accept(value);
            return value;
        });
    }

    /**
     * Populates an existing mutable instance using the configured fixture rules.
     */
    public T populate(T instance) {
        Objects.requireNonNull(instance, "instance must not be null");
        if (type.isRecord()) {
            throw new UnsupportedOperationException(
                "ObjectFaker.populate(existing) does not support records: " + type.getName());
        }
        if (!type.isInstance(instance)) {
            throw new IllegalArgumentException(
                "instance must be assignable to " + type.getName() + ", got " + instance.getClass().getName());
        }
        return applyPostGenerationRules(generator().populate(type.cast(instance)));
    }

    @Override
    public T generate() {
        return applyPostGenerationRules(generator().generate());
    }

    private void invalidateGeneratorState() {
        this.objectGenerator = null;
        this.uniqueFieldTracker = new UniqueFieldTracker();
    }

    private ObjectGenerator<T> generator() {
        if (objectGenerator == null) {
            objectGenerator = new ObjectGenerator<>(type, buildRuntimeConfig(), 0, null, null, uniqueFieldTracker);
        }
        return objectGenerator;
    }

    private ObjectGeneratorConfig buildRuntimeConfig() {
        ObjectGeneratorConfig.Builder builder = baseConfig.toBuilder();
        for (RuleTarget excludedField : excludedByIncludeRuleTargets()) {
            builder.exclude(field -> field.getDeclaringClass() == excludedField.ownerType()
                                     && field.getName().equals(excludedField.fieldName()));
        }
        for (RuleTarget ignoredField : ignoredRuleTargets()) {
            builder.exclude(field -> field.getDeclaringClass() == ignoredField.ownerType()
                                     && field.getName().equals(ignoredField.fieldName()));
        }
        for (RuleTarget target : fieldRuleTargets()) {
            builder.override(target.ownerType(), target.fieldName(), fieldRules.get(target.fieldName()));
        }
        for (RuleTarget target : contextualFieldRuleTargets()) {
            builder.override(target.ownerType(), target.fieldName(), contextualFieldRules.get(target.fieldName()));
        }
        return builder.build();
    }

    private List<RuleTarget> excludedByIncludeRuleTargets() {
        if (includedFields.isEmpty()) {
            return List.of();
        }
        Set<String> effectiveIncluded = new LinkedHashSet<>(includedFields);
        effectiveIncluded.addAll(fieldRules.keySet());
        effectiveIncluded.addAll(contextualFieldRules.keySet());
        effectiveIncluded.addAll(dependentFieldRules.keySet());

        List<RuleTarget> targets = new ArrayList<>();
        for (RuleTarget target : allRuleTargets()) {
            if (!effectiveIncluded.contains(target.fieldName()) && !ignoredFields.contains(target.fieldName())) {
                targets.add(target);
            }
        }
        return targets;
    }

    private List<RuleTarget> ignoredRuleTargets() {
        List<RuleTarget> targets = new ArrayList<>(ignoredFields.size());
        for (String fieldName : ignoredFields) {
            targets.add(ruleTargets.get(fieldName));
        }
        return targets;
    }

    private List<RuleTarget> fieldRuleTargets() {
        List<RuleTarget> targets = new ArrayList<>(fieldRules.size());
        for (String fieldName : fieldRules.keySet()) {
            targets.add(ruleTargets.get(fieldName));
        }
        return targets;
    }

    private List<RuleTarget> contextualFieldRuleTargets() {
        List<RuleTarget> targets = new ArrayList<>(contextualFieldRules.size());
        for (String fieldName : contextualFieldRules.keySet()) {
            targets.add(ruleTargets.get(fieldName));
        }
        return targets;
    }

    private T applyPostGenerationRules(T value) {
        T current = value;
        for (Map.Entry<String, Function<? super T, ?>> entry : dependentFieldRules.entrySet()) {
            RuleTarget target = ruleTargets.get(entry.getKey());
            Object fieldValue = entry.getValue().apply(current);
            current = assignFieldValue(current, target, fieldValue);
        }
        for (UnaryOperator<T> postProcessor : postProcessors) {
            current = Objects.requireNonNull(postProcessor.apply(current), "postProcessor must not return null");
        }
        return current;
    }

    private T assignFieldValue(T instance, RuleTarget target, Object value) {
        try {
            if (type.isRecord()) {
                return rebuildRecord(instance, target.fieldName(), value);
            }
            Field field = Objects.requireNonNull(target.field(), "field must not be null");
            field.setAccessible(true);
            field.set(instance, value);
            return instance;
        } catch (ReflectiveOperationException | IllegalArgumentException e) {
            throw new ObjectGenerationException(
                "Failed to apply fixture rule for field '" + target.ownerType().getSimpleName()
                + "." + target.fieldName() + "'", e);
        }
    }

    private T rebuildRecord(T instance, String fieldName, Object fieldValue) throws ReflectiveOperationException {
        RecordComponent[] components = type.getRecordComponents();
        Class<?>[] parameterTypes = new Class<?>[components.length];
        Object[] args = new Object[components.length];
        for (int i = 0; i < components.length; i++) {
            RecordComponent component = components[i];
            parameterTypes[i] = component.getType();
            if (component.getName().equals(fieldName)) {
                args[i] = fieldValue;
            } else {
                component.getAccessor().setAccessible(true);
                args[i] = component.getAccessor().invoke(instance);
            }
        }
        Constructor<T> canonical = type.getDeclaredConstructor(parameterTypes);
        canonical.setAccessible(true);
        return canonical.newInstance(args);
    }

    private RuleTarget requireRuleTarget(String fieldName, String operation) {
        Objects.requireNonNull(fieldName, "fieldName must not be null");
        RuleTarget target = ruleTargets.computeIfAbsent(fieldName, this::resolveRuleTarget);
        if (!type.isRecord()) {
            Field field = Objects.requireNonNull(target.field(), "field must not be null");
            if (Modifier.isFinal(field.getModifiers())) {
                throw new IllegalArgumentException(
                    "Field '" + fieldName + "' on " + type.getName()
                    + " is final and cannot be used with ObjectFaker " + operation);
            }
        }
        return target;
    }

    private void ensureFieldAvailable(String fieldName) {
        if (ignoredFields.contains(fieldName)) {
            throw new IllegalStateException("Field '" + fieldName + "' is already ignored");
        }
        if (fieldRules.containsKey(fieldName)
            || contextualFieldRules.containsKey(fieldName)
            || dependentFieldRules.containsKey(fieldName)) {
            throw new IllegalStateException("Field '" + fieldName + "' already has a registered rule");
        }
    }

    private List<RuleTarget> allRuleTargets() {
        List<RuleTarget> targets = new ArrayList<>();
        if (type.isRecord()) {
            for (RecordComponent component : type.getRecordComponents()) {
                targets.add(new RuleTarget(type, component.getName(), null));
            }
            return targets;
        }
        for (Class<?> current = type; current != Object.class; current = current.getSuperclass()) {
            for (Field field : current.getDeclaredFields()) {
                if (!Modifier.isStatic(field.getModifiers())) {
                    targets.add(new RuleTarget(field.getDeclaringClass(), field.getName(), field));
                }
            }
        }
        return targets;
    }

    private RuleTarget resolveRuleTarget(String fieldName) {
        if (type.isRecord()) {
            for (RecordComponent component : type.getRecordComponents()) {
                if (component.getName().equals(fieldName)) {
                    return new RuleTarget(type, fieldName, null);
                }
            }
            throw new IllegalArgumentException("Unknown record component '" + fieldName + "' on " + type.getName());
        }

        List<Field> matches = new ArrayList<>();
        for (Class<?> current = type; current != Object.class; current = current.getSuperclass()) {
            for (Field field : current.getDeclaredFields()) {
                if (!Modifier.isStatic(field.getModifiers()) && field.getName().equals(fieldName)) {
                    matches.add(field);
                }
            }
        }
        if (matches.isEmpty()) {
            throw new IllegalArgumentException("Unknown field '" + fieldName + "' on " + type.getName());
        }
        if (matches.size() > 1) {
            throw new IllegalArgumentException(
                "Field '" + fieldName + "' is ambiguous on " + type.getName()
                + "; use ObjectGeneratorConfig for owner-specific overrides");
        }
        Field field = matches.getFirst();
        return new RuleTarget(field.getDeclaringClass(), field.getName(), field);
    }

    private record RuleTarget(Class<?> ownerType, String fieldName, Field field) {
    }
}
