/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.object;

import io.github.frikit.krandom.generator.ContextualGenerator;
import io.github.frikit.krandom.generator.GenerationContext;
import io.github.frikit.krandom.generator.Generator;
import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.failure.GenerationFailureCategory;
import io.github.frikit.krandom.generator.failure.GenerationFailureContext;
import io.github.frikit.krandom.generator.failure.GenerationOperation;
import io.github.frikit.krandom.generator.object.exception.FixtureRuleException;
import io.github.frikit.krandom.generator.object.exception.ObjectGenerationException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import org.jspecify.annotations.Nullable;

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
 * <p>A rule replaces any earlier rule for the identical field path, whatever the kind of either
 * rule, so defaults from a profile, a shared fixture, or an earlier {@link ObjectModel} can be
 * refined per test. A replacement of the same kind keeps the earlier rule's place in the evaluation
 * order. Rules on nested paths below the replaced path still apply, unless the new rule is a
 * dependent rule on the parent, which runs last and overwrites the whole value. A rule or include
 * for an ignored path or a path below one fails, as does {@code ignore} below an ignored path or on
 * a field that has a rule.
 *
 * <p>Rules run in this order. Root rules and root context rules run during generation, as field
 * overrides for the member's declaring class and name, so they also apply wherever generation fills
 * that member in a nested object: in a nested object of the root's type, or of another subclass of
 * the class that declares an inherited member. Root ignores, and in include mode the root properties
 * left out, are exclusions with the same scope. Then nested rules, nested context rules and dependent
 * rules run, each kind in registration order, and each at its own path only; a rule creates an object
 * on its path that generation left {@code null}, such as one past {@code objectMaxDepth}, as a root of
 * its own, which the root rules, root ignores and include-mode exclusions reach too. Nested includes prune
 * and nested ignores clear last, so an ignore clears the value of any rule below it. Against the
 * {@code GeneratorConfig}'s overrides for the same member, a root context rule comes first; a root
 * rule comes after a contextual field override and a contextual predicate override, and before the
 * others. {@link #assertConfigurationIsValid()} reports where rules overlap in these ways.
 *
 * @param <T> the root type being generated
 */
public final class ObjectFaker<T> implements Generator<T> {

    private final Class<T> type;
    private final ObjectGeneratorConfig baseConfig;
    private final Map<String, RulePath> rulePaths = new LinkedHashMap<>();
    private final List<FixtureRule> registrations = new ArrayList<>();
    private final Map<String, Generator<?>> fieldRules = new LinkedHashMap<>();
    private final Map<String, ContextualGenerator<?>> contextualFieldRules = new LinkedHashMap<>();
    private final Map<String, Function<? super T, ?>> dependentFieldRules = new LinkedHashMap<>();
    private final Map<String, Consumer<ObjectFaker<T>>> namedProfiles = new LinkedHashMap<>();
    private final Set<String> ignoredFields = new LinkedHashSet<>();
    private final Set<String> includedFields = new LinkedHashSet<>();
    private final List<UnaryOperator<T>> postProcessors = new ArrayList<>();
    private final Set<String> applyingProfiles = new LinkedHashSet<>();
    private final Set<String> appliedProfiles = new LinkedHashSet<>();

    private UniqueFieldTracker uniqueFieldTracker = new UniqueFieldTracker();
    private @Nullable ObjectGenerator<T> objectGenerator;
    private @Nullable FixtureRuleAnalysis analysis;
    private boolean strictValidation;

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
        this(type, ObjectGeneratorConfig.from(config), true);
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
     * Registers a deterministic field rule evaluated during base object generation. It replaces any
     * earlier rule for the same field path. The generator may return {@code null} for a reference field,
     * so {@code V} may be nullable.
     */
    public <V extends @Nullable Object> ObjectFaker<T> ruleFor(String fieldName, Generator<? extends V> generator) {
        RulePath target = requireRulePath(fieldName, "ruleFor");
        ensureNotIgnored(fieldName);
        Objects.requireNonNull(generator, "generator must not be null");
        contextualFieldRules.remove(target.path());
        dependentFieldRules.remove(target.path());
        fieldRules.put(target.path(), generator);
        register(FixtureRuleKind.RULE, target.path());
        invalidateGeneratorState();
        return this;
    }

    /**
     * Registers a deterministic root field rule through a type-safe accessor reference. As with a rule by
     * field name, the property and the generator's values may be nullable.
     */
    public <V extends @Nullable Object> ObjectFaker<T> ruleFor(
            PropertySelector<T, V> property, Generator<? extends V> generator) {
        return ruleFor(PropertyPath.of(property), generator);
    }

    /**
     * Registers a deterministic field rule through a type-safe root or nested property path. As with a rule
     * by field name, the property and the generator's values may be nullable.
     */
    public <V extends @Nullable Object> ObjectFaker<T> ruleFor(
            PropertyPath<T, V> property, Generator<? extends V> generator) {
        return ruleFor(
                Objects.requireNonNull(property, "property must not be null").path(), generator);
    }

    /**
     * Registers a context-aware field rule evaluated during base object generation. It replaces any
     * earlier rule for the same field path. The generator may return {@code null} for a reference field,
     * so {@code V} may be nullable.
     */
    public <V extends @Nullable Object> ObjectFaker<T> ruleForContext(
            String fieldName, ContextualGenerator<? extends V> generator) {
        RulePath target = requireRulePath(fieldName, "ruleFor");
        ensureNotIgnored(fieldName);
        Objects.requireNonNull(generator, "generator must not be null");
        fieldRules.remove(target.path());
        dependentFieldRules.remove(target.path());
        contextualFieldRules.put(target.path(), generator);
        register(FixtureRuleKind.CONTEXT_RULE, target.path());
        invalidateGeneratorState();
        return this;
    }

    /**
     * Registers a context-aware root field rule through a type-safe accessor reference. As with a rule by
     * field name, the property and the generator's values may be nullable.
     */
    public <V extends @Nullable Object> ObjectFaker<T> ruleForContext(
            PropertySelector<T, V> property, ContextualGenerator<? extends V> generator) {
        return ruleForContext(PropertyPath.of(property), generator);
    }

    /**
     * Registers a context-aware field rule through a type-safe root or nested property path. As with a rule
     * by field name, the property and the generator's values may be nullable.
     */
    public <V extends @Nullable Object> ObjectFaker<T> ruleForContext(
            PropertyPath<T, V> property, ContextualGenerator<? extends V> generator) {
        return ruleForContext(
                Objects.requireNonNull(property, "property must not be null").path(), generator);
    }

    /**
     * Registers a dependent field rule evaluated after the base object is generated. It replaces any
     * earlier rule for the same field path. The function may return {@code null} for a reference field,
     * so {@code V} may be nullable.
     */
    public <V extends @Nullable Object> ObjectFaker<T> ruleFor(
            String fieldName, Function<? super T, ? extends V> generator) {
        RulePath target = requireRulePath(fieldName, "dependent ruleFor");
        ensureNotIgnored(fieldName);
        Objects.requireNonNull(generator, "generator must not be null");
        boolean replacedBaseRule = fieldRules.remove(target.path()) != null;
        replacedBaseRule |= contextualFieldRules.remove(target.path()) != null;
        dependentFieldRules.put(target.path(), generator);
        register(FixtureRuleKind.DEPENDENT_RULE, target.path());
        if (replacedBaseRule || !includedFields.isEmpty()) {
            // A replaced base or context rule is baked into the cached generator, and in include mode
            // dependent rules decide which root fields are generated, so rebuild it then. Otherwise a
            // dependent rule leaves the generator, and with it a seeded sequence, untouched.
            invalidateGeneratorState();
        }
        return this;
    }

    /**
     * Registers a correlated root assignment through a type-safe accessor reference. As with a rule by
     * field name, the property and the function's values may be nullable.
     */
    public <V extends @Nullable Object> ObjectFaker<T> ruleFor(
            PropertySelector<T, V> property, Function<? super T, ? extends V> generator) {
        return ruleFor(PropertyPath.of(property), generator);
    }

    /**
     * Registers a correlated root or nested assignment through a type-safe property path. As with a rule by
     * field name, the property and the function's values may be nullable.
     */
    public <V extends @Nullable Object> ObjectFaker<T> ruleFor(
            PropertyPath<T, V> property, Function<? super T, ? extends V> generator) {
        return ruleFor(
                Objects.requireNonNull(property, "property must not be null").path(), generator);
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
            register(FixtureRuleKind.IGNORE, target.path());
            return this;
        }
        if (target.isRoot()) {
            ensureRootFieldNotUsedByNestedInclude(fieldName, "ignored");
            ensureRootFieldNotUsedByNestedRule(fieldName, "ignored");
        }
        ensureFieldAvailable(fieldName);
        ignoredFields.add(target.path());
        register(FixtureRuleKind.IGNORE, target.path());
        invalidateGeneratorState();
        return this;
    }

    /**
     * Excludes a root property selected by an accessor method reference.
     */
    public ObjectFaker<T> ignore(PropertySelector<T, ?> property) {
        return ignore(PropertyPath.of(property));
    }

    /**
     * Excludes a root or nested type-safe property path.
     */
    public ObjectFaker<T> ignore(PropertyPath<T, ?> property) {
        return ignore(
                Objects.requireNonNull(property, "property must not be null").path());
    }

    /**
     * Restricts generation to one field or nested field path. Once at least one include rule
     * exists, root fields not explicitly included or covered by a field rule are left untouched,
     * and nested include paths prune sibling fields beneath the included root object.
     */
    public ObjectFaker<T> include(String fieldName) {
        RulePath target = requireRulePath(fieldName, "include");
        ensureNotIgnored(fieldName);
        includedFields.add(target.path());
        register(FixtureRuleKind.INCLUDE, target.path());
        invalidateGeneratorState();
        return this;
    }

    /**
     * Restricts generation to a root property selected by an accessor method reference.
     */
    public ObjectFaker<T> include(PropertySelector<T, ?> property) {
        return include(PropertyPath.of(property));
    }

    /**
     * Restricts generation to a root or nested type-safe property path.
     */
    public ObjectFaker<T> include(PropertyPath<T, ?> property) {
        return include(
                Objects.requireNonNull(property, "property must not be null").path());
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
     *
     * <p>Profiles compose: a profile body may itself call {@code useProfile} to apply
     * another profile, building larger fixture rule sets from smaller ones. Cyclic
     * application is rejected at apply time.
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
     *
     * <p>If the profile fails, library-owned configuration is restored, including nested profiles
     * and cached generator references. External callback effects and random values consumed by a
     * callback are not rolled back. Successfully applied profiles keep their existing behavior.
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
        if (appliedProfiles.contains(name)) {
            throw new IllegalStateException("Profile '" + name + "' is already applied");
        }
        ObjectFaker<T> snapshot = new ObjectFaker<>(type, baseConfig);
        snapshot.copyConfigurationFrom(this);
        appliedProfiles.add(name);
        applyingProfiles.add(name);
        try {
            profile.accept(this);
            return this;
        } catch (RuntimeException | Error e) {
            copyConfigurationFrom(snapshot);
            throw e;
        } finally {
            applyingProfiles.remove(name);
        }
    }

    private void copyConfigurationFrom(ObjectFaker<T> source) {
        rulePaths.clear();
        rulePaths.putAll(source.rulePaths);
        registrations.clear();
        registrations.addAll(source.registrations);
        analysis = null;
        fieldRules.clear();
        fieldRules.putAll(source.fieldRules);
        contextualFieldRules.clear();
        contextualFieldRules.putAll(source.contextualFieldRules);
        dependentFieldRules.clear();
        dependentFieldRules.putAll(source.dependentFieldRules);
        namedProfiles.clear();
        namedProfiles.putAll(source.namedProfiles);
        ignoredFields.clear();
        ignoredFields.addAll(source.ignoredFields);
        includedFields.clear();
        includedFields.addAll(source.includedFields);
        postProcessors.clear();
        postProcessors.addAll(source.postProcessors);
        applyingProfiles.clear();
        applyingProfiles.addAll(source.applyingProfiles);
        appliedProfiles.clear();
        appliedProfiles.addAll(source.appliedProfiles);
        strictValidation = source.strictValidation;
        uniqueFieldTracker = source.uniqueFieldTracker;
        objectGenerator = source.objectGenerator;
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
     * Checks the fixture configuration as {@link #generate()} uses it, and throws one
     * {@link FixtureRuleException} that lists every problem it finds. It extends Bogus's
     * {@code AssertConfigurationIsValid()}, and {@link #strict()} runs it before every generation.
     *
     * <ul>
     *   <li><b>Undecided</b>: a settable root field or record component with no rule, include or ignore.
     *       In include mode only the included root properties need a rule.</li>
     *   <li><b>Shadowed</b>: a rule that a later rule for the same path replaces, including one from a
     *       profile or a composed {@link ObjectModel}; a nested rule that a rule on an ancestor path
     *       overwrites because it runs later; a root rule that a {@code GeneratorConfig} contextual field
     *       override, or contextual predicate override, for the same member comes before; and a
     *       {@code GeneratorConfig} field override for a member that a root rule replaces or comes before,
     *       or that a nested or dependent rule overwrites.</li>
     *   <li><b>Unused</b>: a rule below a path that a later nested ignore clears; a root rule on a member
     *       that the {@code GeneratorConfig} excludes; a root rule or root ignore when a
     *       {@code GeneratorConfig} type override builds the root, which {@code populate(existing)}
     *       never does; and a nested ignore of a member that {@code generate()} never gives a value,
     *       because {@code objectMaxDepth}, an exclusion or include mode leaves an object on its path
     *       {@code null}, which an instance given to {@code populate(existing)} may still hold. A nested
     *       rule past {@code objectMaxDepth} is not unused: it creates the objects on its path.</li>
     *   <li><b>Ambiguous</b>: a root rule, root ignore or include-mode exclusion that also applies at
     *       another path, because it applies by its member's declaring class and name, as the class
     *       documentation describes. A dependent rule applies at its own path only.</li>
     * </ul>
     *
     * <p>A string path that matches no member, or more than one, already fails when it is registered.
     * Each problem names its path, its rule kind and its registration number, never a value. To see
     * which members keep an initializer, the check creates one object of each class it follows with
     * the class's no-argument constructor, as generation does, and it generates no value.
     *
     * @return this faker
     * @throws FixtureRuleException listing every problem found
     */
    public ObjectFaker<T> assertConfigurationIsValid() {
        validate(false);
        return this;
    }

    /**
     * Makes {@link #generate()} and {@link #populate(Object)} check the configuration first, as
     * {@link #assertConfigurationIsValid()} describes, and fail with one {@link FixtureRuleException}
     * that lists every problem. {@code ObjectFaker.strict()} has nothing to do with
     * {@link ObjectGenerationSemanticMode#STRICT}.
     *
     * <p>The check runs once for each configuration: a later rule, include or ignore runs it again.
     *
     * @return this faker
     */
    public ObjectFaker<T> strict() {
        strictValidation = true;
        return this;
    }

    /**
     * Populates an existing mutable instance using the configured fixture rules.
     */
    public T populate(T instance) {
        validateIfStrict(true);
        Objects.requireNonNull(instance, "instance must not be null");
        if (type.isRecord()) {
            throw new UnsupportedOperationException(
                    "ObjectFaker.populate(existing) does not support records: " + type.getName());
        }
        if (!type.isInstance(instance)) {
            throw new IllegalArgumentException("instance must be assignable to " + type.getName() + ", got "
                    + instance.getClass().getName());
        }
        ObjectGenerator<T> objectGenerator = generator();
        uniqueFieldTracker.startObject();
        return applyPostGenerationRules(objectGenerator.populate(type.cast(instance)));
    }

    @Override
    public T generate() {
        validateIfStrict(false);
        ObjectGenerator<T> objectGenerator = generator();
        uniqueFieldTracker.startObject();
        return applyPostGenerationRules(objectGenerator.generate());
    }

    private void validateIfStrict(boolean populating) {
        if (strictValidation) {
            validate(populating);
        }
    }

    private void validate(boolean populating) {
        FixtureRuleAnalysis current = analysis;
        if (current == null) {
            current = new FixtureRuleAnalysis(
                    type,
                    baseConfig,
                    Map.copyOf(rulePaths),
                    List.copyOf(registrations),
                    List.copyOf(fieldRules.keySet()),
                    List.copyOf(contextualFieldRules.keySet()),
                    List.copyOf(dependentFieldRules.keySet()),
                    includedRoots());
            analysis = current;
        }
        current.check(populating);
    }

    private void register(FixtureRuleKind kind, String path) {
        registrations.add(FixtureRule.registered(kind, path, registrations.size() + 1));
        analysis = null;
    }

    /** The root properties that include mode generates, or none outside include mode. */
    private Set<String> includedRoots() {
        if (includedFields.isEmpty()) {
            return Set.of();
        }
        Set<String> roots = new LinkedHashSet<>(rootFieldNames(includedFields));
        roots.addAll(rootFieldNames(fieldRules.keySet()));
        roots.addAll(rootFieldNames(contextualFieldRules.keySet()));
        roots.addAll(rootFieldNames(dependentFieldRules.keySet()));
        return roots;
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
        // With a report listener, a root rule is tagged so that the report names it; the tag draws as it does.
        boolean reporting =
                baseConfig.getGeneratorConfig().getGenerationReportListener().isPresent();
        for (RulePath target : directRootFieldRulePaths()) {
            Generator<?> generator = Objects.requireNonNull(fieldRules.get(target.path()));
            Generator<?> installed = reporting ? tagged(generator, activeRule(target.path(), false)) : generator;
            builder.override(target.leaf().ownerType(), target.leaf().fieldName(), installed);
        }
        for (RulePath target : contextualRootFieldRulePaths()) {
            ContextualGenerator<?> generator = Objects.requireNonNull(contextualFieldRules.get(target.path()));
            ContextualGenerator<?> installed =
                    reporting ? tagged(generator, activeRule(target.path(), false)) : generator;
            builder.override(target.leaf().ownerType(), target.leaf().fieldName(), installed);
        }
        return builder.build();
    }

    private List<RuleTarget> excludedByIncludeRuleTargets() {
        Set<String> effectiveIncluded = includedRoots();
        if (effectiveIncluded.isEmpty()) {
            return List.of();
        }
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
            RulePath path = Objects.requireNonNull(rulePaths.get(fieldName));
            if (path.isRoot()) {
                targets.add(path.leaf());
            }
        }
        return targets;
    }

    private List<RulePath> directRootFieldRulePaths() {
        List<RulePath> targets = new ArrayList<>(fieldRules.size());
        for (String fieldName : fieldRules.keySet()) {
            RulePath path = Objects.requireNonNull(rulePaths.get(fieldName));
            if (path.isRoot()) {
                targets.add(path);
            }
        }
        return targets;
    }

    private List<RulePath> contextualRootFieldRulePaths() {
        List<RulePath> targets = new ArrayList<>(contextualFieldRules.size());
        for (String fieldName : contextualFieldRules.keySet()) {
            RulePath path = Objects.requireNonNull(rulePaths.get(fieldName));
            if (path.isRoot()) {
                targets.add(path);
            }
        }
        return targets;
    }

    private static <V> Generator<V> tagged(Generator<V> generator, FixtureRule rule) {
        return new FixtureRuleGenerator<>(generator, rule);
    }

    private static <V> ContextualGenerator<V> tagged(ContextualGenerator<V> generator, FixtureRule rule) {
        return new FixtureRuleContextualGenerator<>(generator, rule);
    }

    /**
     * The registration of the rule now active for a path, the last one, or of the ignore of that path, the
     * first, as repeats of an ignore change nothing. A path has rules or ignores, never both.
     */
    private FixtureRule activeRule(String path, boolean ignore) {
        FixtureRule active = null;
        for (FixtureRule registration : registrations) {
            if (registration.path().equals(path)
                    && registration.kind() != FixtureRuleKind.INCLUDE
                    && (active == null || !ignore)) {
                active = registration;
            }
        }
        return Objects.requireNonNull(active, "a registered rule");
    }

    /** Reports a rule or ignore that ran after generation, when a report listener is set. */
    private void reportApplied(@Nullable GenerationReporter reporter, String path, boolean ignore) {
        reportApplied(reporter, path, ignore, false);
    }

    /** Reports a rule that ran after generation, which left its field at the default where it {@code ranOut}. */
    private void reportApplied(@Nullable GenerationReporter reporter, String path, boolean ignore, boolean ranOut) {
        if (reporter != null) {
            reporter.applied(type.getSimpleName() + "." + path, activeRule(path, ignore), ranOut);
        }
    }

    private T applyPostGenerationRules(T value) {
        GenerationReporter reporter = GenerationReporter.of(baseConfig, false);
        T current = value;
        for (Map.Entry<String, Generator<?>> entry : fieldRules.entrySet()) {
            RulePath path = Objects.requireNonNull(rulePaths.get(entry.getKey()));
            if (!path.isRoot()) {
                Applied<T> applied = applyRule(current, path, entry.getValue()::generate);
                current = applied.root();
                reportApplied(reporter, entry.getKey(), false, applied.ranOut());
            }
        }
        for (Map.Entry<String, ContextualGenerator<?>> entry : contextualFieldRules.entrySet()) {
            RulePath path = Objects.requireNonNull(rulePaths.get(entry.getKey()));
            if (!path.isRoot()) {
                RuleTarget target = path.leaf();
                GenerationContext context = new GenerationContext(
                        target.fieldName(),
                        target.ownerType(),
                        path.depth(),
                        type.getSimpleName() + "." + path.path(),
                        target.field() != null ? target.field().getGenericType() : target.valueType(),
                        target.field() != null ? target.field() : target.accessor(),
                        baseConfig.getGeneratorConfig());
                ContextualGenerator<?> rule = entry.getValue();
                Applied<T> applied = applyRule(current, path, () -> rule.generate(context));
                current = applied.root();
                reportApplied(reporter, entry.getKey(), false, applied.ranOut());
            }
        }
        for (Map.Entry<String, Function<? super T, ?>> entry : dependentFieldRules.entrySet()) {
            RulePath path = Objects.requireNonNull(rulePaths.get(entry.getKey()));
            T generated = current;
            Function<? super T, ?> rule = entry.getValue();
            Applied<T> applied = applyRule(current, path, () -> rule.apply(generated));
            current = applied.root();
            reportApplied(reporter, entry.getKey(), false, applied.ranOut());
        }
        current = applyNestedIncludeRules(current);
        current = applyNestedIgnoreRules(current, reporter);
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
        return type.cast(pruneToIncludedPaths(value, includeTree, type.getSimpleName(), -1));
    }

    private T applyNestedIgnoreRules(T value, @Nullable GenerationReporter reporter) {
        T current = value;
        for (String fieldName : ignoredFields) {
            RulePath path = Objects.requireNonNull(rulePaths.get(fieldName));
            if (!path.isRoot()) {
                current = clearFieldValue(current, path);
                reportApplied(reporter, fieldName, true);
            }
        }
        return current;
    }

    /** The root after a rule ran after generation, and whether the rule ran out of values that do not repeat. */
    private record Applied<R>(R root, boolean ranOut) {}

    /** The value a rule writes at its path, from the object that holds its member, once the path exists. */
    @FunctionalInterface
    private interface LeafValue {

        @Nullable Object valueFor(Object holder) throws ReflectiveOperationException;
    }

    /**
     * Writes a rule's value at its path. A field whose name {@code objectUniqueField} requested takes a value
     * that does not repeat, as {@link #uniqueRuleValue} chooses it; any other takes the rule's value as it is.
     */
    private Applied<T> applyRule(T instance, RulePath path, Supplier<?> rule) {
        Object value = rule.get();
        if (!baseConfig.isRequestedUniqueField(path.fieldName())) {
            return new Applied<>(assignFieldValue(instance, path, value), false);
        }
        AtomicBoolean ranOut = new AtomicBoolean();
        T root = assignLeaf(
                instance,
                path,
                holder -> uniqueRuleValue(path, readTargetValue(holder, path.leaf()), value, rule, ranOut),
                true);
        return new Applied<>(root, ranOut.get());
    }

    /**
     * The value a rule gives a field whose name {@code objectUniqueField} requested, which must not repeat a value
     * the field took on another object, as a root rule's must not. The value the object holds is no repeat, so a
     * rule may give it back. Otherwise the value the rule replaces is given back where generation gave it to this
     * member in this object, and a repeat applies the rule again, a dependent rule to the same object, up to
     * {@code objectUniquenessMaxAttempts} times in all. Then generation fails, or with
     * {@code objectIgnoreErrors(true)} the field takes its default.
     */
    private @Nullable Object uniqueRuleValue(
            RulePath path, @Nullable Object replaced, @Nullable Object first, Supplier<?> rule, AtomicBoolean ranOut) {
        if (Objects.equals(first, replaced)) {
            return first;
        }
        RuleTarget leaf = path.leaf();
        String fieldKey = UniqueFieldTracker.fieldKey(leaf.ownerType(), leaf.fieldName());
        // The member's path as generation names it, so that only a value generation gave this member goes back.
        String memberPath = ObjectGenerator.typePath(type) + "." + path.path();
        uniqueFieldTracker.releaseTaken(fieldKey, replaced, memberPath);
        AtomicBoolean drawn = new AtomicBoolean();
        try {
            return uniqueFieldTracker.nextUnique(
                    fieldKey,
                    memberPath,
                    () -> drawn.getAndSet(true) ? rule.get() : first,
                    baseConfig.getUniquenessMaxAttempts());
        } catch (UniqueFieldTracker.ExhaustedException exhausted) {
            ranOut.set(true);
            return new ObjectGenerationFailurePolicy(
                            baseConfig.isIgnoreErrors(),
                            baseConfig.getGeneratorConfig().getGenerationFailureListener())
                    .handle(
                            fakerFailure(GenerationFailureCategory.UNSUPPORTED_TYPE, path, exhausted),
                            defaultValue(leaf.valueType()));
        }
    }

    private T assignFieldValue(T instance, RulePath path, @Nullable Object value) {
        return assignLeaf(instance, path, holder -> value, true);
    }

    private T assignLeaf(T instance, RulePath path, LeafValue value, boolean materializeMissingParents) {
        try {
            return type.cast(assignNestedValue(instance, path, 0, value, materializeMissingParents));
        } catch (ReflectiveOperationException | IllegalArgumentException e) {
            throw fakerFailure(GenerationFailureCategory.ASSIGNMENT, path, e);
        }
    }

    private Object assignNestedValue(
            Object current, RulePath path, int index, LeafValue leafValue, boolean materializeMissingParents)
            throws ReflectiveOperationException {
        RuleTarget segment = path.segments().get(index);
        if (index == path.segments().size() - 1) {
            return writeTargetValue(current, segment, leafValue.valueFor(current));
        }

        Object nestedValue = readTargetValue(current, segment);
        boolean materialized = false;
        if (nestedValue == null) {
            if (!materializeMissingParents) {
                return current;
            }
            nestedValue = materializeValue(segment.valueType(), path, index);
            materialized = true;
        }

        Object updatedNested = assignNestedValue(nestedValue, path, index + 1, leafValue, materializeMissingParents);
        if (materialized || updatedNested != nestedValue) {
            return writeTargetValue(current, segment, updatedNested);
        }
        return current;
    }

    private T clearFieldValue(T instance, RulePath path) {
        return assignLeaf(instance, path, holder -> defaultValue(path.leaf().valueType()), false);
    }

    private ObjectGenerationException fakerFailure(GenerationFailureCategory category, RulePath path, Throwable cause) {
        RuleTarget target = path.leaf();
        return fakerFailure(category, type.getSimpleName() + "." + path.path(), target, path.depth(), cause);
    }

    private ObjectGenerationException fakerFailure(
            GenerationFailureCategory category, String path, RuleTarget target, int depth, Throwable cause) {
        String declaredType = target.valueType().getTypeName();
        GenerationFailureContext context = new GenerationFailureContext(
                category, GenerationOperation.APPLY_RULE, path, target.ownerType(), declaredType, depth, -1);
        Throwable originalCause =
                cause instanceof InvocationTargetException invocation ? invocation.getTargetException() : cause;
        return new ObjectGenerationException(
                "Could not apply fixture rule at '" + path + "' (declared type " + declaredType + ", depth " + depth
                        + ")",
                context,
                originalCause);
    }

    /** Builds the missing parent at {@code index} on {@code path}, which a nested rule writes into. */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private Object materializeValue(Class<?> rawType, RulePath path, int index) {
        ObjectGeneratorConfig runtime = buildRuntimeConfig();
        StringBuilder parentPath = new StringBuilder();
        for (RuleTarget segment : path.segments().subList(0, index + 1)) {
            parentPath.append('.').append(segment.fieldName());
        }
        if (runtime.getGeneratorConfig().getGenerationReportListener().isPresent()) {
            // Built as a root of its own, the parent is still reported at its path in the fixture.
            runtime = runtime.reportedAt(ObjectGenerator.typePath(rawType), type.getSimpleName() + parentPath);
        }
        ObjectGenerator generator = new ObjectGenerator(rawType, runtime, 0, null, null, uniqueFieldTracker);
        // So are the values its members take, under the path that a rule names them by.
        return uniqueFieldTracker.buildParent(
                ObjectGenerator.typePath(rawType), ObjectGenerator.typePath(type) + parentPath, generator::generate);
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

    private Object writeTargetValue(Object instance, RuleTarget target, @Nullable Object fieldValue)
            throws ReflectiveOperationException {
        if (target.accessor() != null) {
            return rebuildRecord(instance, target.fieldName(), fieldValue);
        }
        Field field = Objects.requireNonNull(target.field(), "field must not be null");
        field.setAccessible(true);
        field.set(instance, fieldValue);
        return instance;
    }

    private Object rebuildRecord(Object instance, String fieldName, @Nullable Object fieldValue)
            throws ReflectiveOperationException {
        Class<?> recordType = instance.getClass();
        RecordComponent[] components = recordType.getRecordComponents();
        Class<?>[] parameterTypes = new Class<?>[components.length];
        @Nullable Object[] args = new @Nullable Object[components.length];
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
            throw new IllegalArgumentException("Field path '" + fieldName + "' on " + type.getName()
                    + " is final and cannot be used with ObjectFaker " + operation);
        }
        return path;
    }

    private void ensureRootFieldNotUsedByNestedRule(String fieldName, String action) {
        String prefix = fieldName + ".";
        for (String path : rulePaths.keySet()) {
            if (path.startsWith(prefix)) {
                throw new IllegalStateException("Root field '" + fieldName + "' cannot be " + action
                        + " because nested fixture rules already target '" + path + "'");
            }
        }
    }

    private void ensureRootFieldNotUsedByNestedInclude(String fieldName, String action) {
        String prefix = fieldName + ".";
        for (String path : includedFields) {
            if (path.startsWith(prefix)) {
                throw new IllegalStateException("Root field '" + fieldName + "' cannot be " + action
                        + " because nested include rules already target '" + path + "'");
            }
        }
    }

    /** Rejects a path that is ignored itself or lies below an ignored path. */
    private void ensureNotIgnored(String fieldName) {
        for (String ignoredPath : ignoredFields) {
            if (fieldName.equals(ignoredPath) || fieldName.startsWith(ignoredPath + ".")) {
                throw new IllegalStateException("Field '" + ignoredPath + "' is already ignored");
            }
        }
    }

    private void ensureFieldAvailable(String fieldName) {
        ensureNotIgnored(fieldName);
        if (fieldRules.containsKey(fieldName)
                || contextualFieldRules.containsKey(fieldName)
                || dependentFieldRules.containsKey(fieldName)) {
            throw new IllegalStateException("Field '" + fieldName + "' already has a registered rule");
        }
    }

    private List<RuleTarget> allRuleTargets() {
        return allRuleTargets(type);
    }

    /** The record components, or the instance fields of the class and its superclasses, final ones too. */
    static List<RuleTarget> allRuleTargets(Class<?> targetType) {
        List<RuleTarget> targets = new ArrayList<>();
        if (targetType.isRecord()) {
            for (RecordComponent component : targetType.getRecordComponents()) {
                targets.add(new RuleTarget(
                        targetType, component.getName(), component.getType(), null, component.getAccessor()));
            }
            return targets;
        }
        // An interface has no superclass, and no instance fields either.
        for (Class<?> current = targetType;
                current != null && current != Object.class;
                current = current.getSuperclass()) {
            for (Field field : current.getDeclaredFields()) {
                if (!Modifier.isStatic(field.getModifiers())) {
                    targets.add(
                            new RuleTarget(field.getDeclaringClass(), field.getName(), field.getType(), field, null));
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
            throw new IllegalArgumentException(
                    "Unknown record component '" + fieldName + "' on " + ownerType.getName());
        }

        List<Field> matches = new ArrayList<>();
        for (Class<?> current = ownerType;
                current != null && current != Object.class;
                current = current.getSuperclass()) {
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
            throw new IllegalArgumentException("Field '" + fieldName + "' is ambiguous on " + ownerType.getName()
                    + "; use GeneratorConfig.Builder.objectOverride(ownerType, fieldName, generator) "
                    + "for owner-specific overrides");
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
            RulePath rulePath = Objects.requireNonNull(rulePaths.get(path));
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

    private Object pruneToIncludedPaths(Object current, IncludeNode includeNode) {
        String currentPath = current != null ? current.getClass().getSimpleName() : type.getSimpleName();
        return pruneToIncludedPaths(current, includeNode, currentPath, -1);
    }

    private Object pruneToIncludedPaths(Object current, IncludeNode includeNode, String currentPath, int currentDepth) {
        if (current == null || includeNode.keepAll || includeNode.children.isEmpty()) {
            return current;
        }
        Object updated = current;
        for (RuleTarget target : allRuleTargets(current.getClass())) {
            String targetPath = currentPath + "." + target.fieldName();
            int targetDepth = currentDepth + 1;
            try {
                IncludeNode child = includeNode.children.get(target.fieldName());
                if (child == null) {
                    updated = writeTargetValue(updated, target, defaultValue(target.valueType()));
                    continue;
                }
                if (!child.keepAll && supportsNestedPruning(target.valueType())) {
                    Object nested = readTargetValue(updated, target);
                    Object pruned = pruneToIncludedPaths(nested, child, targetPath, targetDepth);
                    if (pruned != nested) {
                        updated = writeTargetValue(updated, target, pruned);
                    }
                }
            } catch (ReflectiveOperationException e) {
                throw fakerFailure(GenerationFailureCategory.REFLECTION, targetPath, target, targetDepth, e);
            }
        }
        return updated;
    }

    private static boolean supportsNestedPruning(Class<?> valueType) {
        if (valueType.isPrimitive() || valueType.isEnum() || valueType.isArray()) {
            return false;
        }
        return !valueType.getPackageName().startsWith("java.");
    }

    static @Nullable Object defaultValue(Class<?> valueType) {
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

    /** A registered path, resolved to the members it passes through. */
    record RulePath(String path, List<RuleTarget> segments) {

        RuleTarget leaf() {
            return segments.getLast();
        }

        String fieldName() {
            return leaf().fieldName();
        }

        boolean isRoot() {
            return segments.size() == 1;
        }

        int depth() {
            return segments.size() - 1;
        }
    }

    /** A field, or a record component through its accessor, and the class that declares it. */
    record RuleTarget(
            Class<?> ownerType,
            String fieldName,
            Class<?> valueType,
            @Nullable Field field,
            @Nullable Method accessor) {}

    private static final class IncludeNode {

        private boolean keepAll;
        private final Map<String, IncludeNode> children = new LinkedHashMap<>();
    }
}
