/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.object;

import org.github.krandom.generator.ContextualGenerator;
import org.github.krandom.generator.Generator;
import org.github.krandom.generator.GeneratorConfig;
import org.github.krandom.generator.GenerationContext;
import org.github.krandom.generator.object.exception.ObjectGenerationException;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
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
 * <p>Field rules can target nested paths such as {@code "address.city"} or
 * {@code "billingAddress.postalCode"}. Include/ignore rules stay root-field scoped
 * so the top-level shape remains explicit.
 *
 * @param <T> the root type being generated
 */
public final class ObjectFaker<T> implements Generator<T> {

    private final Class<T> type;
    private final ObjectGeneratorConfig baseConfig;
    private final Map<String, RulePath>                   rulePaths            = new LinkedHashMap<>();
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
        RulePath target = requireRulePath(fieldName, "ruleFor");
        ensureFieldAvailable(fieldName);
        fieldRules.put(target.path(), Objects.requireNonNull(generator, "generator must not be null"));
        invalidateGeneratorState();
        return this;
    }

    /**
     * Registers a context-aware field rule evaluated during base object generation.
     */
    public <V> ObjectFaker<T> ruleForContext(String fieldName, ContextualGenerator<? extends V> generator) {
        RulePath target = requireRulePath(fieldName, "ruleFor");
        ensureFieldAvailable(fieldName);
        contextualFieldRules.put(target.path(), Objects.requireNonNull(generator, "generator must not be null"));
        invalidateGeneratorState();
        return this;
    }

    /**
     * Registers a dependent field rule evaluated after the base object is generated.
     */
    public <V> ObjectFaker<T> ruleFor(String fieldName, Function<? super T, ? extends V> generator) {
        RulePath target = requireRulePath(fieldName, "dependent ruleFor");
        ensureFieldAvailable(fieldName);
        dependentFieldRules.put(target.path(), Objects.requireNonNull(generator, "generator must not be null"));
        return this;
    }

    /**
     * Excludes one root field from generation.
     */
    public ObjectFaker<T> ignore(String fieldName) {
        RulePath target = requireRootRulePath(fieldName, "ignore");
        if (includedFields.contains(target.fieldName())) {
            throw new IllegalStateException("Field '" + fieldName + "' is already included");
        }
        ensureRootFieldNotUsedByNestedRule(fieldName, "ignored");
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
        RulePath target = requireRootRulePath(fieldName, "include");
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
        for (RulePath target : directRootFieldRulePaths()) {
            builder.override(target.leaf().ownerType(), target.leaf().fieldName(), fieldRules.get(target.path()));
        }
        for (RulePath target : contextualRootFieldRulePaths()) {
            builder.override(target.leaf().ownerType(), target.leaf().fieldName(), contextualFieldRules.get(target.path()));
        }
        return builder.build();
    }

    private List<RuleTarget> excludedByIncludeRuleTargets() {
        if (includedFields.isEmpty()) {
            return List.of();
        }
        Set<String> effectiveIncluded = new LinkedHashSet<>(includedFields);
        effectiveIncluded.addAll(rootFieldNames(fieldRules.keySet()));
        effectiveIncluded.addAll(rootFieldNames(contextualFieldRules.keySet()));
        effectiveIncluded.addAll(rootFieldNames(dependentFieldRules.keySet()));

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
            targets.add(rulePaths.get(fieldName).leaf());
        }
        return targets;
    }

    private List<RulePath> directRootFieldRulePaths() {
        List<RulePath> targets = new ArrayList<>(fieldRules.size());
        for (String fieldName : fieldRules.keySet()) {
            RulePath path = rulePaths.get(fieldName);
            if (path.isRoot()) {
                targets.add(path);
            }
        }
        return targets;
    }

    private List<RulePath> contextualRootFieldRulePaths() {
        List<RulePath> targets = new ArrayList<>(contextualFieldRules.size());
        for (String fieldName : contextualFieldRules.keySet()) {
            RulePath path = rulePaths.get(fieldName);
            if (path.isRoot()) {
                targets.add(path);
            }
        }
        return targets;
    }

    private T applyPostGenerationRules(T value) {
        T current = value;
        for (Map.Entry<String, Generator<?>> entry : fieldRules.entrySet()) {
            RulePath path = rulePaths.get(entry.getKey());
            if (!path.isRoot()) {
                current = assignFieldValue(current, path, entry.getValue().generate());
            }
        }
        for (Map.Entry<String, ContextualGenerator<?>> entry : contextualFieldRules.entrySet()) {
            RulePath path = rulePaths.get(entry.getKey());
            if (!path.isRoot()) {
                RuleTarget target = path.leaf();
                Object fieldValue = entry.getValue().generate(
                    new GenerationContext(target.fieldName(), target.ownerType(), path.depth()));
                current = assignFieldValue(current, path, fieldValue);
            }
        }
        for (Map.Entry<String, Function<? super T, ?>> entry : dependentFieldRules.entrySet()) {
            RulePath path = rulePaths.get(entry.getKey());
            Object fieldValue = entry.getValue().apply(current);
            current = assignFieldValue(current, path, fieldValue);
        }
        for (UnaryOperator<T> postProcessor : postProcessors) {
            current = Objects.requireNonNull(postProcessor.apply(current), "postProcessor must not return null");
        }
        return current;
    }

    private T assignFieldValue(T instance, RulePath path, Object value) {
        try {
            return type.cast(assignNestedValue(instance, path, 0, value));
        } catch (ReflectiveOperationException | IllegalArgumentException e) {
            throw new ObjectGenerationException(
                "Failed to apply fixture rule for field path '" + path.path() + "' on " + type.getName(), e);
        }
    }

    private Object assignNestedValue(Object current, RulePath path, int index, Object fieldValue)
        throws ReflectiveOperationException {
        RuleTarget segment = path.segments().get(index);
        if (index == path.segments().size() - 1) {
            return writeTargetValue(current, segment, fieldValue);
        }

        Object nestedValue = readTargetValue(current, segment);
        boolean materialized = false;
        if (nestedValue == null) {
            nestedValue = materializeValue(segment.valueType());
            materialized = true;
        }

        Object updatedNested = assignNestedValue(nestedValue, path, index + 1, fieldValue);
        if (materialized || updatedNested != nestedValue) {
            return writeTargetValue(current, segment, updatedNested);
        }
        return current;
    }

    private Object materializeValue(Class<?> rawType) {
        return new ObjectGenerator<>(rawType, buildRuntimeConfig()).generate();
    }

    private Object readTargetValue(Object instance, RuleTarget target) throws ReflectiveOperationException {
        if (target.accessor() != null) {
            target.accessor().setAccessible(true);
            return target.accessor().invoke(instance);
        }
        Field field = Objects.requireNonNull(target.field(), "field must not be null");
        field.setAccessible(true);
        return field.get(instance);
    }

    private Object writeTargetValue(Object instance, RuleTarget target, Object fieldValue) throws ReflectiveOperationException {
        if (target.accessor() != null) {
            return rebuildRecord(instance, target.fieldName(), fieldValue);
        }
        Field field = Objects.requireNonNull(target.field(), "field must not be null");
        field.setAccessible(true);
        field.set(instance, fieldValue);
        return instance;
    }

    private Object rebuildRecord(Object instance, String fieldName, Object fieldValue) throws ReflectiveOperationException {
        Class<?> recordType = instance.getClass();
        RecordComponent[] components = recordType.getRecordComponents();
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
        Constructor<?> canonical = recordType.getDeclaredConstructor(parameterTypes);
        canonical.setAccessible(true);
        return canonical.newInstance(args);
    }

    private RulePath requireRulePath(String fieldName, String operation) {
        Objects.requireNonNull(fieldName, "fieldName must not be null");
        RulePath path = rulePaths.computeIfAbsent(fieldName, this::resolveRulePath);
        RuleTarget leaf = path.leaf();
        if (leaf.field() != null && Modifier.isFinal(leaf.field().getModifiers())) {
            throw new IllegalArgumentException(
                "Field path '" + fieldName + "' on " + type.getName()
                + " is final and cannot be used with ObjectFaker " + operation);
        }
        return path;
    }

    private RulePath requireRootRulePath(String fieldName, String operation) {
        if (fieldName.indexOf('.') >= 0) {
            throw new IllegalArgumentException(
                "ObjectFaker " + operation + " only supports root fields; nested path '" + fieldName
                + "' should use ruleFor(...) instead");
        }
        return requireRulePath(fieldName, operation);
    }

    private void ensureRootFieldNotUsedByNestedRule(String fieldName, String action) {
        String prefix = fieldName + ".";
        for (String path : rulePaths.keySet()) {
            if (path.startsWith(prefix)) {
                throw new IllegalStateException(
                    "Root field '" + fieldName + "' cannot be " + action
                    + " because nested fixture rules already target '" + path + "'");
            }
        }
    }

    private void ensureFieldAvailable(String fieldName) {
        String rootFieldName = rootFieldName(fieldName);
        if (ignoredFields.contains(rootFieldName)) {
            throw new IllegalStateException("Field '" + rootFieldName + "' is already ignored");
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
                targets.add(new RuleTarget(type, component.getName(), component.getType(), null, component.getAccessor()));
            }
            return targets;
        }
        for (Class<?> current = type; current != Object.class; current = current.getSuperclass()) {
            for (Field field : current.getDeclaredFields()) {
                if (!Modifier.isStatic(field.getModifiers())) {
                    targets.add(new RuleTarget(field.getDeclaringClass(), field.getName(), field.getType(), field, null));
                }
            }
        }
        return targets;
    }

    private RulePath resolveRulePath(String fieldName) {
        List<RuleTarget> segments = new ArrayList<>();
        Class<?> currentType = type;
        List<String> pathSegments = splitPath(fieldName);
        for (int i = 0; i < pathSegments.size(); i++) {
            String segmentName = pathSegments.get(i);
            RuleTarget segment = resolveRuleTarget(currentType, segmentName);
            segments.add(segment);
            currentType = segment.valueType();
            if (currentType.isPrimitive() && i < pathSegments.size() - 1) {
                throw new IllegalArgumentException(
                    "Nested field path '" + fieldName + "' crosses primitive segment '" + segmentName + "'");
            }
        }
        return new RulePath(fieldName, List.copyOf(segments));
    }

    private RuleTarget resolveRuleTarget(Class<?> ownerType, String fieldName) {
        if (ownerType.isRecord()) {
            for (RecordComponent component : ownerType.getRecordComponents()) {
                if (component.getName().equals(fieldName)) {
                    return new RuleTarget(ownerType, fieldName, component.getType(), null, component.getAccessor());
                }
            }
            throw new IllegalArgumentException("Unknown record component '" + fieldName + "' on " + ownerType.getName());
        }

        List<Field> matches = new ArrayList<>();
        for (Class<?> current = ownerType; current != Object.class; current = current.getSuperclass()) {
            for (Field field : current.getDeclaredFields()) {
                if (!Modifier.isStatic(field.getModifiers()) && field.getName().equals(fieldName)) {
                    matches.add(field);
                }
            }
        }
        if (matches.isEmpty()) {
            throw new IllegalArgumentException("Unknown field '" + fieldName + "' on " + ownerType.getName());
        }
        if (matches.size() > 1) {
            throw new IllegalArgumentException(
                "Field '" + fieldName + "' is ambiguous on " + ownerType.getName()
                + "; use ObjectGeneratorConfig for owner-specific overrides");
        }
        Field field = matches.getFirst();
        return new RuleTarget(field.getDeclaringClass(), field.getName(), field.getType(), field, null);
    }

    private static List<String> splitPath(String fieldName) {
        String[] rawSegments = fieldName.split("\\.");
        List<String> segments = new ArrayList<>(rawSegments.length);
        for (String segment : rawSegments) {
            if (segment.isBlank()) {
                throw new IllegalArgumentException("Invalid field path '" + fieldName + "'");
            }
            segments.add(segment);
        }
        return segments;
    }

    private static String rootFieldName(String fieldName) {
        int separator = fieldName.indexOf('.');
        return separator >= 0 ? fieldName.substring(0, separator) : fieldName;
    }

    private static Set<String> rootFieldNames(Set<String> fieldNames) {
        Set<String> roots = new LinkedHashSet<>(fieldNames.size());
        for (String fieldName : fieldNames) {
            roots.add(rootFieldName(fieldName));
        }
        return roots;
    }

    private record RulePath(String path, List<RuleTarget> segments) {

        private RuleTarget leaf() {
            return segments.getLast();
        }

        private String fieldName() {
            return leaf().fieldName();
        }

        private boolean isRoot() {
            return segments.size() == 1;
        }

        private int depth() {
            return segments.size() - 1;
        }
    }

    private record RuleTarget(Class<?> ownerType,
                              String fieldName,
                              Class<?> valueType,
                              Field field,
                              Method accessor) {
    }
}
