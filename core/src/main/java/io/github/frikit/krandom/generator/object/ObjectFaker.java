/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.object;

import io.github.frikit.krandom.generator.ContextualGenerator;
import io.github.frikit.krandom.generator.Generator;
import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.GenerationContext;
import io.github.frikit.krandom.generator.object.exception.ObjectGenerationException;

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
 * {@code "billingAddress.postalCode"}. Include/ignore rules can also target nested paths,
 * allowing the top-level shape and nested payload slices to be constrained declaratively.
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
        this(type, GeneratorConfig.defaults());
    }

    /**
     * Creates a fixture authoring API using the shared root configuration.
     */
    public ObjectFaker(Class<T> type, GeneratorConfig config) {
        this(type, ObjectGeneratorConfig.builder().generatorConfig(config).build(), true);
    }

    /**
     * Package-private bridge for object-local config adapters.
     */
    ObjectFaker(Class<T> type, ObjectGeneratorConfig config) {
        this(type, config, true);
    }

    private ObjectFaker(Class<T> type, ObjectGeneratorConfig config, boolean ignored) {
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
     * Excludes one field or nested field path from generation.
     */
    public ObjectFaker<T> ignore(String fieldName) {
        RulePath target = requireRulePath(fieldName, "ignore");
        if (includedFields.contains(target.path())) {
            throw new IllegalStateException("Field '" + fieldName + "' is already included");
        }
        if (ignoredFields.contains(target.path())) {
            return this;
        }
        if (target.isRoot()) {
            ensureRootFieldNotUsedByNestedInclude(fieldName, "ignored");
            ensureRootFieldNotUsedByNestedRule(fieldName, "ignored");
        }
        ensureFieldAvailable(fieldName);
        ignoredFields.add(target.path());
        invalidateGeneratorState();
        return this;
    }

    /**
     * Restricts generation to one field or nested field path. Once at least one include rule
     * exists, root fields not explicitly included or covered by a field rule are left untouched,
     * and nested include paths prune sibling fields beneath the included root object.
     */
    public ObjectFaker<T> include(String fieldName) {
        RulePath target = requireRulePath(fieldName, "include");
        if (ignoredFields.contains(target.path()) || ignoredFields.contains(rootFieldName(fieldName))) {
            throw new IllegalStateException("Field '" + fieldName + "' is already ignored");
        }
        includedFields.add(target.path());
        invalidateGeneratorState();
        return this;
    }

    /**
     * Restricts generation to multiple field or nested field paths.
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
     * Asserts that every settable root field or record component has an explicit rule, ignore,
     * or include directive — Bogus's {@code AssertConfigurationIsValid()} equivalent.
     *
     * <p>When {@link #include(String)} has been used, only the included root fields are required
     * to have rules; unincluded fields are treated as implicitly excluded. Otherwise, every
     * non-static, non-final field (and every record component) must either have a rule registered
     * or appear in {@link #ignore(String)}.
     */
    public ObjectFaker<T> assertConfigurationIsValid() {
        Set<String> rootFieldNames = new LinkedHashSet<>();
        for (RuleTarget target : allRuleTargets()) {
            if (target.field() != null && Modifier.isFinal(target.field().getModifiers())) {
                continue;
            }
            rootFieldNames.add(target.fieldName());
        }

        Set<String> ruleRoots = new LinkedHashSet<>();
        ruleRoots.addAll(rootFieldNames(fieldRules.keySet()));
        ruleRoots.addAll(rootFieldNames(contextualFieldRules.keySet()));
        ruleRoots.addAll(rootFieldNames(dependentFieldRules.keySet()));

        Set<String> required = new LinkedHashSet<>(rootFieldNames);
        if (!includedFields.isEmpty()) {
            required.retainAll(rootFieldNames(includedFields));
        } else {
            required.removeAll(rootFieldNames(ignoredFields));
        }
        required.removeAll(ruleRoots);

        if (!required.isEmpty()) {
            throw new IllegalStateException(
                "ObjectFaker for " + type.getName() + " has no rules for: " + required);
        }
        return this;
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
        Set<String> effectiveIncluded = new LinkedHashSet<>(rootFieldNames(includedFields));
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
            RulePath path = rulePaths.get(fieldName);
            if (path.isRoot()) {
                targets.add(path.leaf());
            }
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
        current = applyNestedIncludeRules(current);
        current = applyNestedIgnoreRules(current);
        for (UnaryOperator<T> postProcessor : postProcessors) {
            current = Objects.requireNonNull(postProcessor.apply(current), "postProcessor must not return null");
        }
        return current;
    }

    private T applyNestedIncludeRules(T value) {
        if (includedFields.isEmpty()) {
            return value;
        }
        IncludeNode includeTree = buildIncludeTree();
        try {
            return type.cast(pruneToIncludedPaths(value, includeTree));
        } catch (ReflectiveOperationException e) {
            throw new ObjectGenerationException(
                "Failed to apply nested include rules on " + type.getName(), e);
        }
    }

    private T applyNestedIgnoreRules(T value) {
        T current = value;
        for (String fieldName : ignoredFields) {
            RulePath path = rulePaths.get(fieldName);
            if (!path.isRoot()) {
                current = clearFieldValue(current, path);
            }
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
        return assignNestedValue(current, path, index, fieldValue, true);
    }

    private Object assignNestedValue(Object current, RulePath path, int index, Object fieldValue, boolean materializeMissingParents)
        throws ReflectiveOperationException {
        RuleTarget segment = path.segments().get(index);
        if (index == path.segments().size() - 1) {
            return writeTargetValue(current, segment, fieldValue);
        }

        Object nestedValue = readTargetValue(current, segment);
        boolean materialized = false;
        if (nestedValue == null) {
            if (!materializeMissingParents) {
                return current;
            }
            nestedValue = materializeValue(segment.valueType());
            materialized = true;
        }

        Object updatedNested = assignNestedValue(nestedValue, path, index + 1, fieldValue, materializeMissingParents);
        if (materialized || updatedNested != nestedValue) {
            return writeTargetValue(current, segment, updatedNested);
        }
        return current;
    }

    private T clearFieldValue(T instance, RulePath path) {
        try {
            return type.cast(assignNestedValue(instance, path, 0, defaultValue(path.leaf().valueType()), false));
        } catch (ReflectiveOperationException | IllegalArgumentException e) {
            throw new ObjectGenerationException(
                "Failed to apply ignore rule for field path '" + path.path() + "' on " + type.getName(), e);
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private Object materializeValue(Class<?> rawType) {
        return new ObjectGenerator(rawType, buildRuntimeConfig(), 0, null, null, uniqueFieldTracker).generate();
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

    private void ensureRootFieldNotUsedByNestedInclude(String fieldName, String action) {
        String prefix = fieldName + ".";
        for (String path : includedFields) {
            if (path.startsWith(prefix)) {
                throw new IllegalStateException(
                    "Root field '" + fieldName + "' cannot be " + action
                    + " because nested include rules already target '" + path + "'");
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
        return allRuleTargets(type);
    }

    private List<RuleTarget> allRuleTargets(Class<?> targetType) {
        List<RuleTarget> targets = new ArrayList<>();
        if (targetType.isRecord()) {
            for (RecordComponent component : targetType.getRecordComponents()) {
                targets.add(new RuleTarget(targetType, component.getName(), component.getType(), null, component.getAccessor()));
            }
            return targets;
        }
        for (Class<?> current = targetType; current != Object.class; current = current.getSuperclass()) {
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

    private IncludeNode buildIncludeTree() {
        IncludeNode root = new IncludeNode();
        registerIncludedPaths(root, includedFields);
        registerIncludedPaths(root, fieldRules.keySet());
        registerIncludedPaths(root, contextualFieldRules.keySet());
        registerIncludedPaths(root, dependentFieldRules.keySet());
        return root;
    }

    private void registerIncludedPaths(IncludeNode root, Set<String> paths) {
        for (String path : paths) {
            RulePath rulePath = rulePaths.get(path);
            IncludeNode current = root;
            for (RuleTarget segment : rulePath.segments()) {
                if (current.keepAll) {
                    break;
                }
                IncludeNode child = current.children.computeIfAbsent(segment.fieldName(), ignored -> new IncludeNode());
                current = child;
            }
            current.keepAll = true;
            current.children.clear();
        }
    }

    private Object pruneToIncludedPaths(Object current, IncludeNode includeNode) throws ReflectiveOperationException {
        if (current == null || includeNode.keepAll || includeNode.children.isEmpty()) {
            return current;
        }
        Object updated = current;
        for (RuleTarget target : allRuleTargets(current.getClass())) {
            IncludeNode child = includeNode.children.get(target.fieldName());
            if (child == null) {
                updated = writeTargetValue(updated, target, defaultValue(target.valueType()));
                continue;
            }
            if (!child.keepAll && supportsNestedPruning(target.valueType())) {
                Object nested = readTargetValue(updated, target);
                Object pruned = pruneToIncludedPaths(nested, child);
                if (pruned != nested) {
                    updated = writeTargetValue(updated, target, pruned);
                }
            }
        }
        return updated;
    }

    private static boolean supportsNestedPruning(Class<?> valueType) {
        if (valueType.isPrimitive() || valueType.isEnum() || valueType.isArray()) {
            return false;
        }
        Package valuePackage = valueType.getPackage();
        return valuePackage == null || !valuePackage.getName().startsWith("java.");
    }

    private static Object defaultValue(Class<?> valueType) {
        if (!valueType.isPrimitive()) {
            return null;
        }
        return switch (valueType.getName()) {
            case "boolean" -> false;
            case "byte" -> (byte) 0;
            case "short" -> (short) 0;
            case "int" -> 0;
            case "long" -> 0L;
            case "float" -> 0f;
            case "double" -> 0d;
            case "char" -> '\0';
            default -> throw new IllegalArgumentException("Unsupported primitive type " + valueType.getName());
        };
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

    private static final class IncludeNode {

        private boolean keepAll;
        private final Map<String, IncludeNode> children = new LinkedHashMap<>();
    }
}
