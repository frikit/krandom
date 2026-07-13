/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.object;

import org.jspecify.annotations.Nullable;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Type;
import java.util.Objects;

/**
 * Resolver context passed to an {@link ObjectConstructionAdapter}.
 *
 * <p>Adapters should call {@link #generate(Type, Class, String, AnnotatedElement)} for each
 * constructor parameter rather than creating scalar generators themselves. That preserves the
 * active root {@code GeneratorConfig} contract for type/field overrides, constraints, generic
 * containers, semantics, nullability, and deterministic source use.
 *
 * @param <T> requested constructed type
 */
public final class ObjectConstructionContext<T> {

    @FunctionalInterface
    interface ValueResolver {

        Object generate(Type genericType,
                        Class<?> rawType,
                        String memberName,
                        @Nullable AnnotatedElement element);
    }

    @FunctionalInterface
    interface ExplicitOverrideLookup {

        boolean hasExplicitOverride(String memberName, Class<?> rawType);
    }

    private final Class<T>              type;
    private final String                path;
    private final Class<?>              ownerType;
    private final int                   depth;
    private final ValueResolver         valueResolver;
    private final ExplicitOverrideLookup explicitOverrideLookup;

    ObjectConstructionContext(Class<T> type,
                              String path,
                              Class<?> ownerType,
                              int depth,
                              ValueResolver valueResolver,
                              ExplicitOverrideLookup explicitOverrideLookup) {
        this.type = Objects.requireNonNull(type, "type must not be null");
        this.path = Objects.requireNonNull(path, "path must not be null");
        this.ownerType = Objects.requireNonNull(ownerType, "ownerType must not be null");
        this.depth = depth;
        this.valueResolver = Objects.requireNonNull(valueResolver, "valueResolver must not be null");
        this.explicitOverrideLookup = Objects.requireNonNull(
            explicitOverrideLookup, "explicitOverrideLookup must not be null");
    }

    /**
     * Requested constructed type.
     */
    public Class<T> getType() {
        return type;
    }

    /**
     * Sanitized root-relative construction path.
     */
    public String getPath() {
        return path;
    }

    /**
     * Type that owns constructor parameters generated through this context.
     */
    public Class<?> getOwnerType() {
        return ownerType;
    }

    /**
     * Object-recursion depth of this construction request.
     */
    public int getDepth() {
        return depth;
    }

    /**
     * Resolves one constructor parameter through the active object-generation pipeline.
     *
     * @param genericType parameter generic type
     * @param rawType parameter runtime type
     * @param memberName parameter name used for semantic and explicit field overrides
     * @param element parameter/property annotations, or {@code null} when unavailable
     * @return generated parameter value
     */
    public @Nullable Object generate(Type genericType,
                                     Class<?> rawType,
                                     String memberName,
                                     @Nullable AnnotatedElement element) {
        return valueResolver.generate(
            Objects.requireNonNull(genericType, "genericType must not be null"),
            Objects.requireNonNull(rawType, "rawType must not be null"),
            Objects.requireNonNull(memberName, "memberName must not be null"),
            element);
    }

    /**
     * Returns whether a field or type override explicitly targets this constructor parameter.
     *
     * <p>Adapters can preserve a language-level default when this returns {@code false} and a
     * parameter is optional. Explicit field and type overrides must remain stronger than defaults.
     */
    public boolean hasExplicitOverride(String memberName, Class<?> rawType) {
        return explicitOverrideLookup.hasExplicitOverride(
            Objects.requireNonNull(memberName, "memberName must not be null"),
            Objects.requireNonNull(rawType, "rawType must not be null"));
    }
}
