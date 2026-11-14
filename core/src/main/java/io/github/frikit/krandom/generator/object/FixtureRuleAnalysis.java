/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.object;

import io.github.frikit.krandom.generator.object.FixtureRuleProblem.Category;
import io.github.frikit.krandom.generator.object.ObjectFaker.RulePath;
import io.github.frikit.krandom.generator.object.ObjectFaker.RuleTarget;
import io.github.frikit.krandom.generator.object.exception.FixtureRuleException;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.lang.reflect.RecordComponent;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import org.jspecify.annotations.Nullable;

/**
 * Finds the unused, ambiguous and shadowed rules of one {@link ObjectFaker} configuration, as
 * {@link ObjectFaker#assertConfigurationIsValid()} reports them. It reads the faker's rules and the
 * {@code GeneratorConfig} it generates with, and generates nothing.
 *
 * <p>It models the faker's evaluation order: root rules and root context rules run during generation as
 * field overrides; then nested rules, nested context rules and dependent rules run in that order, each
 * kind in registration order, where a replacement of the same kind keeps its place; then nested includes
 * prune and nested ignores clear.
 */
final class FixtureRuleAnalysis {

    private static final int MAX_LISTED_ROUTES = 5;

    private final Class<?> rootType;
    private final ObjectGeneratorConfig config;
    private final Map<String, RulePath> rulePaths;
    private final List<FixtureRule> registrations;
    private final Set<String> includedRoots;
    private final boolean followConstructorArguments;
    private final List<String> evaluationOrder = new ArrayList<>();
    private final Map<String, FixtureRule> activeRules = new LinkedHashMap<>();
    private final Map<String, FixtureRule> activeIncludes = new LinkedHashMap<>();
    private final Map<String, FixtureRule> activeIgnores = new LinkedHashMap<>();
    private final Set<FixtureRule> inertRules = new HashSet<>();
    private final UniqueFieldTracker adapters = new UniqueFieldTracker();
    private final Map<Class<?>, Optional<Set<Field>>> initializedFields = new HashMap<>();
    private final List<Finding> findings = new ArrayList<>();
    private final List<String> undecided;

    /**
     * Analyses one configuration.
     *
     * @param plainRules     the paths of the faker's plain rules, in evaluation order
     * @param contextRules   the paths of its context rules, in evaluation order
     * @param dependentRules the paths of its dependent rules, in evaluation order
     * @param includedRoots  the root properties that include mode generates, or none outside include mode
     * @param followConstructorArguments whether a constructor is taken to store each argument as given, so that
     *     the parents a nested rule builds behind a constructor argument or a record component are followed
     */
    FixtureRuleAnalysis(
            Class<?> rootType,
            ObjectGeneratorConfig config,
            Map<String, RulePath> rulePaths,
            List<FixtureRule> registrations,
            List<String> plainRules,
            List<String> contextRules,
            List<String> dependentRules,
            Set<String> includedRoots,
            boolean followConstructorArguments) {
        this.rootType = rootType;
        this.config = config;
        this.rulePaths = rulePaths;
        this.registrations = registrations;
        this.includedRoots = includedRoots;
        this.followConstructorArguments = followConstructorArguments;
        for (FixtureRule registration : registrations) {
            switch (registration.kind()) {
                case INCLUDE -> activeIncludes.putIfAbsent(registration.path(), registration);
                case IGNORE -> activeIgnores.putIfAbsent(registration.path(), registration);
                default -> activeRules.put(registration.path(), registration);
            }
        }
        for (FixtureRule rule : activeRules.values()) {
            if (isInstalledRootRule(rule)) {
                evaluationOrder.add(rule.path());
            }
        }
        addNested(plainRules);
        addNested(contextRules);
        evaluationOrder.addAll(dependentRules);
        findReplacedRules();
        findOverwrittenRules();
        findRulesUnderIgnores();
        findConfigurationOverlaps();
        findRootFactoryRules();
        findIgnoresWithoutValues();
        findRouteLeaks();
        this.undecided = findUndecided();
    }

    private void addNested(List<String> paths) {
        for (String path : paths) {
            if (!path(path).isRoot()) {
                evaluationOrder.add(path);
            }
        }
    }

    /**
     * Throws the problems for {@code generate()}, or for {@code populate(existing)}, which never builds
     * the root with a type override, if there are any.
     */
    void check(boolean populating) {
        List<FixtureRuleProblem> problems = problems(populating);
        if (!undecided.isEmpty() || !problems.isEmpty()) {
            throw new FixtureRuleException(rootType, undecided, problems);
        }
    }

    /** The problems, ordered by registration with {@code GeneratorConfig} entries last. */
    private List<FixtureRuleProblem> problems(boolean populating) {
        List<FixtureRuleProblem> problems = new ArrayList<>();
        Scope operation = populating ? Scope.POPULATE : Scope.GENERATE;
        for (Finding finding : findings) {
            if (finding.scope() == Scope.ALWAYS || finding.scope() == operation) {
                problems.add(finding.problem());
            }
        }
        problems.sort(Comparator.comparingInt(problem -> problem.rule().registration() < 0
                ? Integer.MAX_VALUE
                : problem.rule().registration()));
        return problems;
    }

    // ── Undecided root properties ─────────────────────────────────────────────

    /**
     * The settable root fields and record components with no rule, include or ignore. In include mode
     * only the included root properties need a rule.
     */
    private List<String> findUndecided() {
        Set<String> required = new LinkedHashSet<>();
        for (RuleTarget target : ObjectFaker.allRuleTargets(rootType)) {
            Field field = target.field();
            if (field == null || !Modifier.isFinal(field.getModifiers())) {
                required.add(target.fieldName());
            }
        }
        if (activeIncludes.isEmpty()) {
            required.removeAll(roots(activeIgnores.keySet()));
        } else {
            required.retainAll(roots(activeIncludes.keySet()));
        }
        required.removeAll(roots(activeRules.keySet()));
        return List.copyOf(required);
    }

    private static Set<String> roots(Set<String> paths) {
        Set<String> roots = new LinkedHashSet<>();
        for (String path : paths) {
            int separator = path.indexOf('.');
            roots.add(separator < 0 ? path : path.substring(0, separator));
        }
        return roots;
    }

    // ── Shadowed: replaced and overwritten rules ─────────────────────────────

    private void findReplacedRules() {
        Map<String, FixtureRule> latest = new LinkedHashMap<>();
        for (FixtureRule registration : registrations) {
            if (isRule(registration.kind())) {
                FixtureRule replaced = latest.put(registration.path(), registration);
                if (replaced != null) {
                    report(
                            Category.SHADOWED,
                            replaced,
                            "replaced by " + registration.describe() + ", registered later for the same path");
                }
            }
        }
    }

    private void findOverwrittenRules() {
        for (int position = 0; position < evaluationOrder.size(); position++) {
            String path = evaluationOrder.get(position);
            FixtureRule overwriting = null;
            for (int later = position + 1; later < evaluationOrder.size(); later++) {
                String laterPath = evaluationOrder.get(later);
                if (isStrictAncestor(laterPath, path)) {
                    overwriting = activeRules.get(laterPath);
                }
            }
            if (overwriting != null) {
                report(
                        Category.SHADOWED,
                        rule(path),
                        overwriting.describe() + " runs after it and replaces the whole of '" + overwriting.path()
                                + "'");
            }
        }
    }

    // ── Unused: rules under a later ignore ───────────────────────────────────

    private void findRulesUnderIgnores() {
        for (FixtureRule rule : activeRules.values()) {
            for (FixtureRule ignore : activeIgnores.values()) {
                if (isStrictAncestor(ignore.path(), rule.path())) {
                    report(
                            Category.UNUSED,
                            rule,
                            ignore.describe() + " clears '" + ignore.path() + "' after the rules run");
                    break;
                }
            }
        }
    }

    // ── Overlaps with the GeneratorConfig ────────────────────────────────────

    private void findConfigurationOverlaps() {
        for (FixtureRule rule : activeRules.values()) {
            RuleTarget leaf = path(rule.path()).leaf();
            if (isInstalledRootRule(rule)) {
                rootOverrideOverlaps(rule, leaf, memberField(leaf));
            } else {
                configuredValueOverwritten(rule, leaf);
            }
        }
    }

    /** A root rule or root context rule is installed as a field override for its member's owner and name. */
    private void rootOverrideOverlaps(FixtureRule rule, RuleTarget leaf, Field field) {
        String member = memberName(leaf);
        if (config.shouldExclude(field)) {
            inertRules.add(rule);
            report(
                    Category.UNUSED,
                    rule,
                    "the GeneratorConfig excludes " + member + ", so generation never asks the rule for a value");
            return;
        }
        boolean contextField = config.getContextualFieldOverride(leaf.ownerType(), leaf.fieldName())
                .isPresent();
        boolean plainField =
                config.getFieldOverride(leaf.ownerType(), leaf.fieldName()).isPresent();
        if (rule.kind() == FixtureRuleKind.RULE) {
            if (contextField) {
                inertRules.add(rule);
                report(
                        Category.SHADOWED,
                        rule,
                        "the GeneratorConfig context field override for " + member + " comes first");
            } else if (config.getContextualFieldPredicateOverride(field).isPresent()) {
                String reason = "a GeneratorConfig context predicate override that matches " + member + " comes first";
                if (fillsByConstructor(leaf)) {
                    // generate() fills the constructor argument, which no predicate reaches, and keeps it.
                    findings.add(new Finding(
                            new FixtureRuleProblem(
                                    Category.SHADOWED, rule, reason + " when populate(existing) fills the field"),
                            Scope.POPULATE));
                } else {
                    inertRules.add(rule);
                    report(Category.SHADOWED, rule, reason);
                }
            } else if (plainField) {
                report(
                        Category.SHADOWED,
                        FixtureRule.configured(FixtureRuleKind.FIELD_OVERRIDE, rule.path()),
                        "replaced by " + rule.describe() + ", which ObjectFaker installs for " + member);
            }
            return;
        }
        if (contextField) {
            report(
                    Category.SHADOWED,
                    FixtureRule.configured(FixtureRuleKind.CONTEXT_FIELD_OVERRIDE, rule.path()),
                    "replaced by " + rule.describe() + ", which ObjectFaker installs for " + member);
        }
        if (plainField) {
            report(
                    Category.SHADOWED,
                    FixtureRule.configured(FixtureRuleKind.FIELD_OVERRIDE, rule.path()),
                    rule.describe() + ", which ObjectFaker installs for " + member + ", comes first");
        }
    }

    /**
     * Whether the root's constructor takes this member as an argument of its name, which the rule supplies
     * and no predicate override reaches; the field then keeps that argument.
     */
    private boolean fillsByConstructor(RuleTarget leaf) {
        Constructor<?> constructor = filledConstructor(rootType);
        if (rootType.isRecord() || leaf.ownerType() != rootType || constructor == null) {
            return false;
        }
        for (Parameter parameter : constructor.getParameters()) {
            if (parameter.getName().equals(leaf.fieldName())) {
                return true;
            }
        }
        return false;
    }

    /** A rule that runs after generation replaces whatever the configuration produced at its path. */
    private void configuredValueOverwritten(FixtureRule rule, RuleTarget leaf) {
        String reason = "overwritten by " + rule.describe() + ", which runs after generation";
        if (config.getContextualFieldOverride(leaf.ownerType(), leaf.fieldName())
                .isPresent()) {
            report(
                    Category.SHADOWED,
                    FixtureRule.configured(FixtureRuleKind.CONTEXT_FIELD_OVERRIDE, rule.path()),
                    reason);
        }
        if (config.getFieldOverride(leaf.ownerType(), leaf.fieldName()).isPresent()) {
            report(Category.SHADOWED, FixtureRule.configured(FixtureRuleKind.FIELD_OVERRIDE, rule.path()), reason);
        }
    }

    private void findRootFactoryRules() {
        if (!typeOverridden(rootType)) {
            return;
        }
        String reason = "generate() builds " + rootType.getSimpleName()
                + " with the GeneratorConfig type override, which applies no root rule or root ignore";
        List<FixtureRule> rootDecisions = new ArrayList<>();
        for (FixtureRule rule : activeRules.values()) {
            if (isInstalledRootRule(rule)) {
                rootDecisions.add(rule);
            }
        }
        for (FixtureRule ignore : activeIgnores.values()) {
            if (path(ignore.path()).isRoot()) {
                rootDecisions.add(ignore);
            }
        }
        for (FixtureRule decision : rootDecisions) {
            findings.add(new Finding(new FixtureRuleProblem(Category.UNUSED, decision, reason), Scope.GENERATE));
        }
    }

    // ── Unused: ignores of members that never hold a value ───────────────────

    private void findIgnoresWithoutValues() {
        for (FixtureRule ignore : activeIgnores.values()) {
            RulePath path = path(ignore.path());
            if (!path.isRoot()) {
                String reason = whyAlwaysEmpty(path.segments());
                if (reason != null) {
                    // populate(existing) keeps the objects that the instance already holds.
                    findings.add(new Finding(new FixtureRuleProblem(Category.UNUSED, ignore, reason), Scope.GENERATE));
                }
            }
        }
    }

    /**
     * Why the member that a nested ignore clears never holds a value, or {@code null} when it may. Each
     * object on the path exists only where a rule supplies it or passes through it, an override or an
     * initializer may produce it, or generation reaches it within {@code objectMaxDepth}.
     */
    private @Nullable String whyAlwaysEmpty(List<RuleTarget> segments) {
        if (typeOverridden(rootType)) {
            // The type override that builds the root may supply any object below it.
            return null;
        }
        int leaf = segments.size() - 1;
        @Nullable Class<?> owner = SealedTypes.isAbstract(rootType) ? null : rootType;
        for (int index = 0; index <= leaf; index++) {
            if (ruleSuppliesWithin(segments, index)) {
                return null;
            }
            RuleTarget segment = segments.get(index);
            Class<?> next = config.resolveSubtype(segment.valueType());
            @Nullable Class<?> nextOwner = SealedTypes.isAbstract(next) ? null : next;
            if (rulePassesThrough(segments, index)) {
                owner = nextOwner;
                continue;
            }
            String prefix = prefix(segments, index);
            String below = index == leaf ? ", so it always keeps its default" : ", so nothing below it is generated";
            if (index == 0 && !includedRoots.isEmpty() && !includedRoots.contains(segment.fieldName())) {
                return "include mode leaves out '" + prefix + "'" + below;
            }
            Field field = memberField(segment);
            if (owner == null
                    || suppliedByOverride(segment.ownerType(), segment.fieldName(), field)
                    || typeOverridden(segment.valueType())
                    || typeOverridden(next)) {
                return null;
            }
            if (config.shouldExclude(field)) {
                return startsAtDefault(owner, field) ? "the GeneratorConfig excludes '" + prefix + "'" + below : null;
            }
            if (!isGeneratedAsNestedObject(segment.valueType()) || !generationFills(owner, field)) {
                return null;
            }
            if (index >= config.getMaxDepth()) {
                return "'" + prefix + "' would hold an object at depth " + (index + 1) + ", past objectMaxDepth "
                        + config.getMaxDepth() + ", so it is always null";
            }
            owner = nextOwner;
        }
        return null;
    }

    /** Whether a rule supplies the value of one of the first {@code index + 1} segments, whatever it holds. */
    private boolean ruleSuppliesWithin(List<RuleTarget> segments, int index) {
        for (String rulePath : activeRules.keySet()) {
            List<RuleTarget> ruleSegments = path(rulePath).segments();
            if (ruleSegments.size() <= index + 1 && sharesPrefix(ruleSegments, segments, ruleSegments.size())) {
                return true;
            }
        }
        return false;
    }

    /** Whether a nested rule below the first {@code index + 1} segments materializes them. */
    private boolean rulePassesThrough(List<RuleTarget> segments, int index) {
        for (String rulePath : activeRules.keySet()) {
            List<RuleTarget> ruleSegments = path(rulePath).segments();
            if (ruleSegments.size() > index + 1 && sharesPrefix(ruleSegments, segments, index + 1)) {
                return true;
            }
        }
        return false;
    }

    private static boolean sharesPrefix(List<RuleTarget> first, List<RuleTarget> second, int length) {
        for (int i = 0; i < length; i++) {
            if (!first.get(i).fieldName().equals(second.get(i).fieldName())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Whether a field override, a predicate override or a {@code @Randomizer} supplies this member, which
     * a constructor parameter, with no field, can take only by name.
     */
    private boolean suppliedByOverride(Class<?> owner, String name, @Nullable Field field) {
        return field == null
                ? overriddenByName(owner, name)
                : overridesField(owner, name, field) || field.isAnnotationPresent(Randomizer.class);
    }

    private boolean overriddenByName(Class<?> owner, String name) {
        return config.getContextualFieldOverride(owner, name).isPresent()
                || config.getFieldOverride(owner, name).isPresent();
    }

    /**
     * Whether a field override or a predicate override supplies this field, which generation then fills even
     * where the constructor set it; a {@code @Randomizer} leaves what the constructor set.
     */
    private boolean overridesField(Class<?> owner, String name, Field field) {
        return overriddenByName(owner, name)
                || config.getContextualFieldPredicateOverride(field).isPresent()
                || config.getFieldPredicateOverride(field).isPresent();
    }

    /** Whether a type override, contextual or not, builds every value of this type. */
    private boolean typeOverridden(Class<?> type) {
        return config.getContextualTypeOverride(type).isPresent()
                || config.getTypeOverride(type).isPresent();
    }

    /** Whether a member of this declared type is a nested object, which {@code objectMaxDepth} cuts. */
    private boolean isGeneratedAsNestedObject(Class<?> declaredType) {
        Class<?> nestedType = config.resolveSubtype(declaredType);
        return FieldGeneratorResolver.isNestableType(nestedType) || SealedTypes.isSealed(nestedType, adapters);
    }

    // ── Ambiguous: root decisions that also apply at other routes ────────────

    private void findRouteLeaks() {
        List<Subject> subjects = new ArrayList<>();
        Set<String> installedKeys = new HashSet<>();
        Set<Field> fakerExcluded = new HashSet<>();
        for (FixtureRule rule : activeRules.values()) {
            if (isInstalledRootRule(rule)) {
                RuleTarget target = path(rule.path()).leaf();
                installedKeys.add(key(target.ownerType(), target.fieldName()));
                if (!inertRules.contains(rule)) {
                    subjects.add(new Subject(rule, target));
                }
            }
        }
        for (FixtureRule ignore : activeIgnores.values()) {
            RulePath path = path(ignore.path());
            if (path.isRoot()) {
                fakerExcluded.add(memberField(path.leaf()));
                subjects.add(new Subject(ignore, path.leaf()));
            }
        }
        if (!includedRoots.isEmpty()) {
            int firstInclude = activeIncludes.values().iterator().next().registration();
            for (RuleTarget target : ObjectFaker.allRuleTargets(rootType)) {
                Field field = memberField(target);
                if (!includedRoots.contains(target.fieldName()) && !activeIgnores.containsKey(target.fieldName())) {
                    fakerExcluded.add(field);
                    if (!Modifier.isFinal(field.getModifiers())) {
                        subjects.add(new Subject(
                                FixtureRule.registered(FixtureRuleKind.INCLUDE, target.fieldName(), firstInclude),
                                target));
                    }
                }
            }
        }
        if (subjects.isEmpty()) {
            return;
        }
        new RouteWalk(subjects, installedKeys, fakerExcluded, false).visitRoot();
        new RouteWalk(subjects, installedKeys, fakerExcluded, true).visitRoot();
        for (Subject subject : subjects) {
            String installed = subject.rule.kind() == FixtureRuleKind.INCLUDE
                    ? "include mode leaves out " + memberName(subject.target)
                    : "ObjectFaker installs it for " + memberName(subject.target);
            if (subject.generateRoutes.equals(subject.populateRoutes)) {
                routeLeak(subject, installed, subject.generateRoutes, Scope.ALWAYS);
            } else {
                routeLeak(subject, installed, subject.generateRoutes, Scope.GENERATE);
                routeLeak(subject, installed, subject.populateRoutes, Scope.POPULATE);
            }
        }
    }

    private void routeLeak(Subject subject, String installed, Set<String> routes, Scope scope) {
        if (!routes.isEmpty()) {
            findings.add(new Finding(
                    new FixtureRuleProblem(
                            Category.AMBIGUOUS,
                            subject.rule,
                            installed + " wherever generation reaches that member, so it also applies at "
                                    + listed(routes)),
                    scope));
        }
    }

    private static String listed(Set<String> routes) {
        StringBuilder listed = new StringBuilder();
        int shown = 0;
        for (String route : routes) {
            if (shown == MAX_LISTED_ROUTES) {
                break;
            }
            listed.append(shown == 0 ? "" : ", ").append('\'').append(route).append('\'');
            shown++;
        }
        if (routes.size() > shown) {
            listed.append(" and ").append(routes.size() - shown).append(" more");
        }
        return listed.toString();
    }

    /** A root rule, root ignore or include-mode exclusion, which applies by its member's owner and name. */
    private static final class Subject {

        private final FixtureRule rule;
        private final RuleTarget target;
        private final Set<String> generateRoutes = new LinkedHashSet<>();
        private final Set<String> populateRoutes = new LinkedHashSet<>();

        private Subject(FixtureRule rule, RuleTarget target) {
            this.rule = rule;
            this.target = target;
        }
    }

    /**
     * Follows the nested objects that generation reaches from the root, as the resolver does, and records
     * where each subject's member appears in them. It stops wherever generation would: past
     * {@code objectMaxDepth}, at a type already being generated on the path, at a type it cannot construct,
     * and at a member that an exclusion, an override, a {@code @Randomizer} or a kept initializer decides.
     * Then it follows the parents that a nested rule builds where generation leaves them {@code null}, as
     * {@link ObjectFaker} builds them: each as a root of its own, which the root decisions reach too.
     */
    private final class RouteWalk {

        private final List<Subject> subjects;
        private final Set<String> installedKeys;
        private final Set<Field> fakerExcluded;
        private final boolean populating;
        /** The paths of the objects that generation builds, each ending in a dot, such as {@code "client."}. */
        private final Set<String> built = new HashSet<>();
        /** The paths, ending in a dot, of the object members that generation reaches but leaves {@code null}. */
        private final Set<String> leftNull = new HashSet<>();

        private RouteWalk(
                List<Subject> subjects, Set<String> installedKeys, Set<Field> fakerExcluded, boolean populating) {
            this.subjects = subjects;
            this.installedKeys = installedKeys;
            this.fakerExcluded = fakerExcluded;
            this.populating = populating;
        }

        /**
         * Visits the root: {@code populate(existing)} fills the given object's fields, and {@code generate()}
         * builds the root as a member of its type is built, or with its type override, which fills nothing.
         * Then visits the parents that the nested rules build.
         */
        private void visitRoot() {
            if (populating) {
                visit(rootType, ResolvedType.bindingsFor(rootType), "", 0, Set.of());
            } else {
                visitBuilt(rootType, "");
            }
            visitBuiltParents();
        }

        /**
         * Visits an object that generation builds as a root of its own, at {@code prefix}: the fixture's root,
         * or a parent that a nested rule builds. A type override builds it and fills nothing, and an abstract
         * type is built as the subtype it maps to or as one class of its sealed hierarchy.
         */
        private void visitBuilt(Class<?> type, String prefix) {
            if (typeOverridden(type)) {
                return;
            }
            if (!SealedTypes.isAbstract(type)) {
                visit(type, ResolvedType.bindingsFor(type), prefix, 0, Set.of());
                return;
            }
            Class<?> mapped = config.resolveSubtype(type);
            if (mapped != type) {
                if (!typeOverridden(mapped)) {
                    visit(mapped, ResolvedType.bindingsFor(mapped), prefix, 0, Set.of());
                }
                return;
            }
            for (Class<?> chosen : sealedChoices(type, Map.of(), null, Set.of())) {
                // A sealed root's choice counts as being generated, as a member's choice does.
                Map<TypeVariable<?>, Type> bindings = ResolvedType.bindingsFor(chosen);
                visit(
                        chosen,
                        bindings,
                        prefix,
                        0,
                        Set.of(cycleKey(chosen, ResolvedType.resolvedSignature(chosen, bindings))));
            }
        }

        /**
         * Visits the parents that a nested rule builds. {@link ObjectFaker} builds a parent on a rule's path
         * that generation leaves {@code null} as a root of its own, with the root rules, root ignores and
         * include-mode exclusions, and with a depth of its own, so the root decisions reach its members and
         * the objects below it. Where a rule supplies a parent or an ignore clears it, or nothing is known of
         * it, as of a member that an override supplies or that {@code populate(existing)} is given, nothing
         * below it is built.
         */
        private void visitBuiltParents() {
            for (String rulePath : activeRules.keySet()) {
                List<RuleTarget> segments = path(rulePath).segments();
                for (int index = 0; index < segments.size() - 1; index++) {
                    String parent = prefix(segments, index);
                    String member = parent + ".";
                    if (activeRules.containsKey(parent) || activeIgnores.containsKey(parent)) {
                        break;
                    }
                    // One class of a sealed hierarchy may leave a parent null where another builds it, so a parent
                    // that any class leaves null is built here, once, whatever the others do.
                    if (!(populating && index == 0) && leftNull.remove(member)) {
                        visitBuilt(segments.get(index).valueType(), member);
                    } else if (!built.contains(member)) {
                        break;
                    }
                }
            }
        }

        /** Visits the members of an object of {@code type} that generation builds at {@code depth}. */
        private void visit(
                Class<?> type, Map<TypeVariable<?>, Type> bindings, String prefix, int depth, Set<String> inProgress) {
            built.add(prefix);
            if (!type.isRecord()
                    && (adapters.constructionAdapter(type, ObjectGenerator::constructionAdapterFor)
                                    .isPresent()
                            || ObjectGenerator.requiresKotlinConstructionAdapter(type))) {
                // A construction adapter decides such a type's members, or generation cannot build it.
                return;
            }
            if (!prefix.isEmpty()) {
                recordRoutes(type, prefix);
            }
            // With followConstructorArguments, what each constructor argument left null, by name: the constructor
            // stores it in the field of that name, which generation may fill again afterwards.
            Map<String, Set<String>> storedArguments = new HashMap<>();
            for (Member member : members(type, !(populating && prefix.isEmpty()))) {
                // A constructor argument, a record component's too, may be replaced by the constructor, unless
                // followConstructorArguments takes every constructor to store its arguments as given.
                boolean argument = member.parameter() || type.isRecord();
                boolean replaceable = argument && !followConstructorArguments;
                String path = prefix + member.name() + ".";
                Set<String> stored = argument ? null : storedArguments.get(member.name());
                if (stored != null) {
                    // Generation fills the field again where the constructor stored the default, as it fills a
                    // field that holds its default, and where objectOverrideDefaultInitialization, a field or
                    // predicate override or a root rule says so; then what the argument left is gone. Otherwise,
                    // with a @Randomizer too, the field keeps the argument.
                    Field field = Objects.requireNonNull(member.field(), "a field member");
                    if (!isGenerated(member)
                            || !(stored.contains(path)
                                    || config.isOverrideDefaultInitialization()
                                    || overridesField(member.owner(), member.name(), field)
                                    || installedKeys.contains(key(member.owner(), member.name())))) {
                        continue;
                    }
                    leftNull.removeAll(stored);
                }
                Set<String> before = argument ? Set.copyOf(leftNull) : Set.of();
                if (!isGenerated(member)) {
                    // An excluded field keeps its default unless an initializer that generation keeps sets it. An
                    // excluded argument passes its default, which the constructor may replace, so it is unknown,
                    // or which it stores.
                    if (argument
                            ? followConstructorArguments
                            : startsAtDefault(type, Objects.requireNonNull(member.field(), "a field member"))) {
                        leftNull.add(path);
                    }
                } else if (!hasMemberGenerator(member)) {
                    descend(
                            member.genericType(),
                            member.element(),
                            member.element(),
                            bindings,
                            prefix + member.name(),
                            depth,
                            inProgress);
                }
                if (argument) {
                    // What another class of a sealed hierarchy recorded stays.
                    Set<String> added = new HashSet<>();
                    for (String marker : leftNull) {
                        if (marker.startsWith(path) && !before.contains(marker)) {
                            added.add(marker);
                        }
                    }
                    if (replaceable) {
                        // The constructor may replace an argument that generation leaves null, or what it left
                        // null below one, so nothing this argument records is known; what a field of its name
                        // that generation fills afterwards records stays.
                        leftNull.removeAll(added);
                    } else {
                        storedArguments.put(member.name(), added);
                    }
                }
            }
        }

        private void recordRoutes(Class<?> type, String prefix) {
            for (Subject subject : subjects) {
                RuleTarget target = subject.target;
                Field field = memberField(target);
                boolean present = type.isRecord()
                        ? type == target.ownerType()
                        : target.ownerType().isAssignableFrom(type);
                // A contextual predicate override comes before a plain rule wherever generation fills the field.
                boolean outranked = subject.rule.kind() == FixtureRuleKind.RULE
                        && config.getContextualFieldPredicateOverride(field).isPresent();
                // The configuration's own exclusion of the field, by name too, comes first on either route.
                boolean excluded = config.shouldExclude(field);
                if (!excluded && ((present && !outranked) || takesArgument(type, subject, field))) {
                    (populating ? subject.populateRoutes : subject.generateRoutes).add(prefix + target.fieldName());
                }
            }
        }

        /**
         * Whether the subject reaches a constructor argument of its member's name when generation builds an
         * object of {@code type}: a rule, as a by-name override of that type's member, and an exclusion of the
         * field that the argument initializes. No predicate override reaches an argument.
         */
        private boolean takesArgument(Class<?> type, Subject subject, Field field) {
            Constructor<?> constructor = type.isRecord() ? null : filledConstructor(type);
            if (constructor == null) {
                return false;
            }
            String name = subject.target.fieldName();
            for (Parameter parameter : constructor.getParameters()) {
                if (parameter.getName().equals(name)) {
                    return isRule(subject.rule.kind())
                            ? type == subject.target.ownerType()
                            : field.equals(nearestField(type, name));
                }
            }
            return false;
        }

        /**
         * Follows a member or element of {@code declaredType}, as the resolver does: {@code element} is its
         * declaration or annotated type, whose type arguments carry the elements' annotations, and
         * {@code constraintSource} declares its constraints, which an array's component does not.
         */
        private void descend(
                Type declaredType,
                @Nullable AnnotatedElement element,
                @Nullable AnnotatedElement constraintSource,
                Map<TypeVariable<?>, Type> bindings,
                String path,
                int depth,
                Set<String> inProgress) {
            ResolvedType resolved = ResolvedType.resolve(declaredType, bindings);
            Class<?> rawType = resolved.rawClass();
            if (rawType == null || typeOverridden(rawType)) {
                return;
            }
            BeanValidationSupport.ConstraintModel constraints;
            try {
                constraints = BeanValidationSupport.constraintModelFor(constraintSource, rawType);
            } catch (BeanValidationSupport.ConstraintConflictException conflict) {
                // Generation leaves a member with conflicting constraints at its default, or fails.
                return;
            }
            if (constraints.nullOnly()
                    || (isContainer(rawType) && holdsNoElements(constraints))
                    || alwaysAbsent(element, rawType, constraints)) {
                // Such an object member is always null, as an empty container leaves no path below it.
                leftNull.add(path + ".");
                return;
            }
            if (rawType == Optional.class) {
                element(declaredType, element, bindings, Optional.class, 0, path + ".value", depth, inProgress);
            } else if (rawType.isArray()) {
                ResolvedType component = Objects.requireNonNull(resolved.componentType(), "an array's component");
                descend(
                        FieldGeneratorResolver.mostSpecific(component).declaredType(),
                        FieldGeneratorResolver.arrayComponentElement(element),
                        null,
                        bindings,
                        path + "[]",
                        depth,
                        inProgress);
            } else if (Set.class.isAssignableFrom(rawType)) {
                elements(declaredType, rawType, element, bindings, Set.class, path, depth, inProgress);
            } else if (List.class.isAssignableFrom(rawType)) {
                elements(declaredType, rawType, element, bindings, List.class, path, depth, inProgress);
            } else if (Queue.class.isAssignableFrom(rawType)) {
                elements(declaredType, rawType, element, bindings, Queue.class, path, depth, inProgress);
            } else if (Map.class.isAssignableFrom(rawType)) {
                elements(declaredType, rawType, element, bindings, Map.class, path, depth, inProgress);
            } else if (!rawType.isEnum()
                    && resolved.isResolved()
                    && !(resolved.kind() == ResolvedType.Kind.CLASS && rawType.getTypeParameters().length > 0)) {
                if (depth < config.getMaxDepth()) {
                    nested(resolved, rawType, bindings, path, depth, inProgress);
                } else {
                    // Generation leaves an object past objectMaxDepth null.
                    leftNull.add(path + ".");
                }
            }
        }

        /**
         * Follows a collection's or map's elements, which generation makes only when all of the contract's
         * type arguments resolve and, for a sorted container, when its elements or keys have an order.
         */
        private void elements(
                Type containerType,
                Class<?> rawType,
                @Nullable AnnotatedElement container,
                Map<TypeVariable<?>, Type> bindings,
                Class<?> contract,
                String path,
                int depth,
                Set<String> inProgress) {
            int count = contract.getTypeParameters().length;
            List<ResolvedType> arguments = new ArrayList<>(count);
            for (int index = 0; index < count; index++) {
                ResolvedType argument =
                        FieldGeneratorResolver.containerArgument(bindings, containerType, contract, index);
                if (!argument.isResolved()) {
                    return;
                }
                arguments.add(argument);
            }
            if (FieldGeneratorResolver.isSortedContainer(rawType)
                    && !FieldGeneratorResolver.hasDeterministicOrder(arguments.get(0))) {
                return;
            }
            if (count == 1) {
                element(containerType, container, bindings, contract, 0, path + "[]", depth, inProgress);
            } else {
                element(containerType, container, bindings, contract, 0, path + ".key", depth, inProgress);
                element(containerType, container, bindings, contract, 1, path + ".value", depth, inProgress);
            }
        }

        private void element(
                Type containerType,
                @Nullable AnnotatedElement container,
                Map<TypeVariable<?>, Type> bindings,
                Class<?> contract,
                int index,
                String path,
                int depth,
                Set<String> inProgress) {
            ResolvedType element = FieldGeneratorResolver.containerArgument(bindings, containerType, contract, index);
            if (element.isResolved()) {
                AnnotatedElement argument = FieldGeneratorResolver.typeArgumentElement(container, index);
                descend(
                        FieldGeneratorResolver.mostSpecific(element).declaredType(),
                        argument,
                        argument,
                        bindings,
                        path,
                        depth,
                        inProgress);
            }
        }

        private void nested(
                ResolvedType resolved,
                Class<?> rawType,
                Map<TypeVariable<?>, Type> bindings,
                String path,
                int depth,
                Set<String> inProgress) {
            Class<?> nestedType = config.resolveSubtype(rawType);
            boolean sealed = SealedTypes.isSealed(nestedType, adapters);
            List<Class<?>> classes;
            if (sealed) {
                classes = sealedChoices(nestedType, bindings, resolved, inProgress);
            } else if (!FieldGeneratorResolver.isNestableType(nestedType)) {
                classes = List.of();
            } else if (inProgress.contains(cycleKey(nestedType, resolved.resolvedSignature()))) {
                // Generation cuts the cycle there, with an object of the type that it built before, or null.
                leftNull.add(path + ".");
                classes = List.of();
            } else {
                classes = List.of(nestedType);
            }
            for (Class<?> generated : classes) {
                if (!isConstructible(generated) || typeOverridden(generated)) {
                    continue;
                }
                Map<TypeVariable<?>, Type> nestedBindings =
                        FieldGeneratorResolver.nestedTypeBindings(bindings, resolved, generated);
                // As generation keys its cycle guard: a sealed type's choice by its own type, any other class
                // by the member's resolved type, so that generic specializations of one class differ.
                String signature = sealed
                        ? ResolvedType.resolvedSignature(generated, nestedBindings)
                        : resolved.resolvedSignature();
                Set<String> nestedInProgress = new HashSet<>(inProgress);
                nestedInProgress.add(cycleKey(generated, signature));
                visit(generated, nestedBindings, path + ".", depth + 1, nestedInProgress);
            }
        }

        /**
         * The classes a sealed type may be generated as here: those without a type override, that are not
         * enums, and that are not already being generated on this path, which generation leaves out.
         */
        private List<Class<?>> sealedChoices(
                Class<?> sealedType,
                Map<TypeVariable<?>, Type> bindings,
                @Nullable ResolvedType declared,
                Set<String> inProgress) {
            List<Class<?>> subclasses;
            try {
                subclasses = SealedTypes.concreteSubclasses(sealedType, config, adapters);
            } catch (RuntimeException unsupported) {
                return List.of();
            }
            List<Class<?>> choices = new ArrayList<>();
            for (Class<?> subclass : subclasses) {
                Map<TypeVariable<?>, Type> choiceBindings = declared == null
                        ? ResolvedType.bindingsFor(subclass)
                        : FieldGeneratorResolver.nestedTypeBindings(bindings, declared, subclass);
                String key = cycleKey(subclass, ResolvedType.resolvedSignature(subclass, choiceBindings));
                if (!subclass.isEnum() && !typeOverridden(subclass) && !inProgress.contains(key)) {
                    choices.add(subclass);
                }
            }
            return choices;
        }

        private boolean isGenerated(Member member) {
            if (member.parameter() && config.getGeneratorConfig().isObjectFieldNameExcluded(member.name())) {
                return false;
            }
            Field field = member.field();
            return field == null || (!config.shouldExclude(field) && !fakerExcluded.contains(field));
        }

        /** Whether an override, a {@code @Randomizer} or a root rule supplies the member, so generation stops. */
        private boolean hasMemberGenerator(Member member) {
            return suppliedByOverride(member.owner(), member.name(), member.parameter() ? null : member.field())
                    || installedKeys.contains(key(member.owner(), member.name()));
        }

        /** The members generation fills; {@code populate(existing)} constructs no root, so it fills no argument. */
        private List<Member> members(Class<?> type, boolean constructed) {
            List<Member> members = new ArrayList<>();
            if (type.isRecord()) {
                for (RecordComponent component : type.getRecordComponents()) {
                    Field field = backingField(type, component.getName());
                    members.add(new Member(type, component.getName(), component.getGenericType(), field, field, false));
                }
                return members;
            }
            Constructor<?> constructor = constructed ? filledConstructor(type) : null;
            Set<String> argumentNames = new HashSet<>();
            if (constructor != null) {
                Parameter[] parameters = constructor.getParameters();
                for (int index = 0; index < parameters.length; index++) {
                    Parameter parameter = parameters[index];
                    if (parameter.isNamePresent()) {
                        argumentNames.add(parameter.getName());
                        members.add(new Member(
                                type,
                                parameter.getName(),
                                parameter.getParameterizedType(),
                                nearestField(type, parameter.getName()),
                                parameter,
                                true));
                    } else {
                        members.add(new Member(
                                type,
                                "constructorArg" + index,
                                parameter.getParameterizedType(),
                                null,
                                parameter,
                                true));
                    }
                }
            }
            for (Class<?> current = type; current != Object.class; current = current.getSuperclass()) {
                // In name order, as generation fills them, so that routes are listed the same on every JVM.
                Field[] declared = current.getDeclaredFields();
                Arrays.sort(declared, Comparator.comparing(Field::getName));
                for (Field field : declared) {
                    int modifiers = field.getModifiers();
                    // With followConstructorArguments, a field holds what the constructor stored for the argument of
                    // its name, which generation fills again if it is the default.
                    if (!Modifier.isStatic(modifiers)
                            && !Modifier.isFinal(modifiers)
                            && (generationFills(type, field)
                                    || (followConstructorArguments && argumentNames.contains(field.getName())))) {
                        members.add(new Member(
                                field.getDeclaringClass(),
                                field.getName(),
                                field.getGenericType(),
                                field,
                                field,
                                false));
                    }
                }
            }
            return members;
        }
    }

    /**
     * A field, record component or constructor parameter that generation fills. A parameter's field is the
     * nearest field of its name, whose exclusions it follows, or {@code null} without a name or a field.
     */
    private record Member(
            Class<?> owner,
            String name,
            Type genericType,
            @Nullable Field field,
            AnnotatedElement element,
            boolean parameter) {}

    // ── Helpers ──────────────────────────────────────────────────────────────

    private static String key(Class<?> owner, String name) {
        return owner.getName() + '#' + name;
    }

    private static String cycleKey(Class<?> type, String signature) {
        return type.getName() + '|' + signature;
    }

    private static boolean isContainer(Class<?> type) {
        return type.isArray() || java.util.Collection.class.isAssignableFrom(type) || Map.class.isAssignableFrom(type);
    }

    /** Whether a container always comes out empty, as the configured size within its declared size gives. */
    private boolean holdsNoElements(BeanValidationSupport.ConstraintModel constraints) {
        BeanValidationSupport.SizeRange declared = constraints.size();
        int configuredMin = config.getGeneratorConfig().getMinCollectionSize();
        int configuredMax = config.getGeneratorConfig().getMaxCollectionSize();
        int max = declared == null
                ? configuredMax
                : declared.within(configuredMin, configuredMax).max();
        return max == 0;
    }

    /**
     * Whether a member always comes out absent: an {@code Optional} that {@code objectOptionalEmptyProbability}
     * of one empties, or a nullable member that {@code objectNullProbability} of one leaves {@code null}.
     * Elements, whose element is an annotated type, take neither probability.
     */
    private boolean alwaysAbsent(
            @Nullable AnnotatedElement element, Class<?> rawType, BeanValidationSupport.ConstraintModel constraints) {
        if (element == null || element instanceof java.lang.reflect.AnnotatedType) {
            return false;
        }
        if (rawType == Optional.class) {
            return config.getOptionalEmptyProbability() >= 1.0;
        }
        // As FieldGeneratorResolver.shouldReturnNull: a required member, and one that a constraint generator
        // fills, are never null; a collection's @Size gives no generator, so it can be.
        return config.getNullProbability() >= 1.0
                && !rawType.isPrimitive()
                && !constraints.required()
                && BeanValidationSupport.constraintGeneratorFor(element, rawType) == null;
    }

    /** The declared constructor that generation fills, for a type without a no-argument constructor. */
    private @Nullable Constructor<?> filledConstructor(Class<?> type) {
        Constructor<?>[] constructors = type.getDeclaredConstructors();
        return config.getConstructionPolicy() == ObjectConstructionPolicy.SAFE_CONSTRUCTORS
                        && constructors.length == 1
                        && constructors[0].getParameterCount() > 0
                ? constructors[0]
                : null;
    }

    /** Whether generation can build an object of this concrete class, which it would otherwise fail on. */
    private boolean isConstructible(Class<?> type) {
        if (type.isRecord()) {
            return true;
        }
        if (type.isMemberClass() && !Modifier.isStatic(type.getModifiers())) {
            return false;
        }
        return noArgumentConstructor(type) != null
                || config.getConstructionPolicy() == ObjectConstructionPolicy.UNSAFE_CONSTRUCTOR_BYPASS
                || type.getDeclaredConstructors().length == 1;
    }

    /** Whether generation fills this field of an object of {@code owner}, rather than keep an initializer. */
    private boolean generationFills(Class<?> owner, Field field) {
        return config.isOverrideDefaultInitialization() || startsAtDefault(owner, field);
    }

    /**
     * Whether this field of an object of {@code owner} holds its default before generation fills it: always
     * for a record component and for an object built without a constructor, and otherwise as in an object
     * that the no-argument constructor builds, as generation builds it. Without such an object it is unknown,
     * and taken to hold an initializer.
     */
    private boolean startsAtDefault(Class<?> owner, Field field) {
        if (owner.isRecord()) {
            return true;
        }
        if (noArgumentConstructor(owner) == null) {
            return config.getConstructionPolicy() == ObjectConstructionPolicy.UNSAFE_CONSTRUCTOR_BYPASS;
        }
        Optional<Set<Field>> initialized = initializedFields.computeIfAbsent(owner, FixtureRuleAnalysis::initialized);
        return initialized.isPresent() && !initialized.get().contains(field);
    }

    /**
     * The instance fields that an object built with the no-argument constructor holds a value other than
     * the default in, or nothing when the object cannot be built or read.
     */
    private static Optional<Set<Field>> initialized(Class<?> type) {
        try {
            Constructor<?> constructor = type.getDeclaredConstructor();
            constructor.setAccessible(true);
            Object prototype = constructor.newInstance();
            Set<Field> initialized = new HashSet<>();
            for (RuleTarget target : ObjectFaker.allRuleTargets(type)) {
                Field field = Objects.requireNonNull(target.field(), "a class member is a field");
                field.setAccessible(true);
                if (!Objects.equals(field.get(prototype), ObjectFaker.defaultValue(field.getType()))) {
                    initialized.add(field);
                }
            }
            return Optional.of(initialized);
        } catch (ReflectiveOperationException | RuntimeException | LinkageError failed) {
            return Optional.empty();
        }
    }

    private static @Nullable Constructor<?> noArgumentConstructor(Class<?> type) {
        for (Constructor<?> constructor : type.getDeclaredConstructors()) {
            if (constructor.getParameterCount() == 0) {
                return constructor;
            }
        }
        return null;
    }

    /** The field that a constructor parameter of this name initializes, as generation finds it. */
    private static @Nullable Field nearestField(Class<?> type, String name) {
        for (RuleTarget target : ObjectFaker.allRuleTargets(type)) {
            if (target.fieldName().equals(name)) {
                return target.field();
            }
        }
        return null;
    }

    /** A root rule or root context rule, which ObjectFaker installs as a field override. */
    private boolean isInstalledRootRule(FixtureRule rule) {
        return rule.kind() != FixtureRuleKind.DEPENDENT_RULE
                && path(rule.path()).isRoot();
    }

    private static boolean isRule(FixtureRuleKind kind) {
        return kind == FixtureRuleKind.RULE
                || kind == FixtureRuleKind.CONTEXT_RULE
                || kind == FixtureRuleKind.DEPENDENT_RULE;
    }

    /** Whether {@code ancestor} names a strict ancestor of {@code path}, segment by segment. */
    private static boolean isStrictAncestor(String ancestor, String path) {
        return path.startsWith(ancestor + ".");
    }

    private static String prefix(List<RuleTarget> segments, int index) {
        StringBuilder prefix = new StringBuilder();
        for (int i = 0; i <= index; i++) {
            prefix.append(i == 0 ? "" : ".").append(segments.get(i).fieldName());
        }
        return prefix.toString();
    }

    private static String memberName(RuleTarget target) {
        return target.ownerType().getSimpleName() + "." + target.fieldName();
    }

    /** The field that a member's exclusions and predicate overrides read: a record component's backing field. */
    private static Field memberField(RuleTarget target) {
        Field field = target.field();
        return field != null ? field : backingField(target.ownerType(), target.fieldName());
    }

    private static Field backingField(Class<?> recordType, String name) {
        Field backing = null;
        for (Field field : recordType.getDeclaredFields()) {
            if (field.getName().equals(name)) {
                backing = field;
            }
        }
        return Objects.requireNonNull(backing, "a record component has a backing field");
    }

    private RulePath path(String path) {
        return Objects.requireNonNull(rulePaths.get(path), "registered path");
    }

    private FixtureRule rule(String path) {
        return Objects.requireNonNull(activeRules.get(path), "active rule");
    }

    private void report(Category category, FixtureRule rule, String reason) {
        findings.add(new Finding(new FixtureRuleProblem(category, rule, reason), Scope.ALWAYS));
    }

    /** A problem, and the operations it holds for. */
    private record Finding(FixtureRuleProblem problem, Scope scope) {}

    /** The operations a problem holds for. */
    private enum Scope {
        ALWAYS,
        GENERATE,
        POPULATE
    }
}
