/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.object;

/**
 * The kinds of rule that decide a generated object's members: those registered on an
 * {@link ObjectFaker} and those carried by the {@link io.github.frikit.krandom.generator.GeneratorConfig}
 * it generates with.
 */
public enum FixtureRuleKind {

    /**
     * {@link ObjectFaker#ruleFor(String, io.github.frikit.krandom.generator.Generator)} or
     * {@link ObjectFaker#ruleForKeepingRecipe(String, io.github.frikit.krandom.generator.Generator)}.
     */
    RULE(true),

    /** {@link ObjectFaker#ruleForContext(String, io.github.frikit.krandom.generator.ContextualGenerator)}. */
    CONTEXT_RULE(true),

    /** {@link ObjectFaker#ruleFor(String, java.util.function.Function)}, evaluated on the generated object. */
    DEPENDENT_RULE(true),

    /** {@link ObjectFaker#include(String)}. */
    INCLUDE(true),

    /** {@link ObjectFaker#ignore(String)}. */
    IGNORE(true),

    /** {@code GeneratorConfig.Builder.objectOverride(ownerType, fieldName, Generator)}. */
    FIELD_OVERRIDE(false),

    /** {@code GeneratorConfig.Builder.objectOverride(ownerType, fieldName, ContextualGenerator)}. */
    CONTEXT_FIELD_OVERRIDE(false),

    /** {@code GeneratorConfig.Builder.objectOverride(Predicate<Field>, Generator)}. */
    PREDICATE_OVERRIDE(false),

    /** {@code GeneratorConfig.Builder.objectOverride(Predicate<Field>, ContextualGenerator)}. */
    CONTEXT_PREDICATE_OVERRIDE(false),

    /** {@code GeneratorConfig.Builder.objectOverride(Class, Generator)}. */
    TYPE_OVERRIDE(false),

    /** {@code GeneratorConfig.Builder.objectOverride(Class, ContextualGenerator)}. */
    CONTEXT_TYPE_OVERRIDE(false),

    /** An {@code objectExclude}, {@code objectExcludeField} or {@code objectExcludeType} exclusion, or {@link Exclude}. */
    EXCLUSION(false),

    /** A field name requested with {@code objectUniqueField} or {@code objectUniqueFields}. */
    UNIQUE_FIELD(false);

    private final boolean registeredOnObjectFaker;

    FixtureRuleKind(boolean registeredOnObjectFaker) {
        this.registeredOnObjectFaker = registeredOnObjectFaker;
    }

    /**
     * Whether rules of this kind are registered on an {@link ObjectFaker}, which numbers them in
     * registration order, rather than on a {@link io.github.frikit.krandom.generator.GeneratorConfig}.
     *
     * @return {@code true} for an {@code ObjectFaker} rule, {@code false} for a configuration entry
     */
    public boolean isRegisteredOnObjectFaker() {
        return registeredOnObjectFaker;
    }
}
