/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator;

import org.jspecify.annotations.Nullable;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Type;
import java.util.Objects;
import java.util.Optional;

/**
 * Provides context about the field currently being generated.
 *
 * <p>An instance is passed to every {@link ContextualGenerator} at generation time,
 * allowing the generator to produce values that depend on the field name, the declaring
 * class, or the current nesting depth.
 *
 * <pre>{@code
 *   config.override(String.class, ctx -> ctx.getFieldName() + "_value");
 *   config.override(Person.class, "name", ctx -> "Alice_" + ctx.getDepth());
 * }</pre>
 */
public final class GenerationContext {

    private final String   fieldName;
    private final Class<?> ownerType;
    private final int      depth;
    private final String   path;
    private final @Nullable Type declaredType;
    private final @Nullable AnnotatedElement declaration;
    private final @Nullable GeneratorConfig config;

    public GenerationContext(String fieldName, Class<?> ownerType, int depth) {
        this(fieldName, ownerType, depth, simpleName(ownerType) + "." + fieldName, null, null, null);
    }

    /**
     * Creates a metadata-complete generation context.
     *
     * @param fieldName field or component name
     * @param ownerType declaring type
     * @param depth nesting depth
     * @param path full root-to-field path
     * @param declaredType reflected declared type, when available
     * @param declaration reflected declaration, when available
     * @param config active generator configuration, when available
     */
    public GenerationContext(String fieldName,
                             Class<?> ownerType,
                             int depth,
                             String path,
                             @Nullable Type declaredType,
                             @Nullable AnnotatedElement declaration,
                             @Nullable GeneratorConfig config) {
        this.fieldName = Objects.requireNonNull(fieldName, "fieldName must not be null");
        this.ownerType = Objects.requireNonNull(ownerType, "ownerType must not be null");
        if (depth < 0) {
            throw new IllegalArgumentException("depth must be >= 0");
        }
        this.depth = depth;
        this.path = Objects.requireNonNull(path, "path must not be null");
        if (path.isBlank()) {
            throw new IllegalArgumentException("path must not be blank");
        }
        this.declaredType = declaredType;
        this.declaration = declaration;
        this.config = config;
    }

    /**
     * Name of the field being populated.
     */
    public String getFieldName() {
        return fieldName;
    }

    /**
     * Class that declares the field.
     */
    public Class<?> getOwnerType() {
        return ownerType;
    }

    /**
     * Current nesting depth.
     * {@code 0} means the field belongs to the root object passed to
     * {@link io.github.frikit.krandom.generator.object.ObjectGenerator}.
     */
    public int getDepth() {
        return depth;
    }

    /**
     * Full root-to-field path used for diagnostics and targeted behavior.
     *
     * @return generation path
     */
    public String getPath() {
        return path;
    }

    /**
     * Reflected declared type, including generic arguments when available.
     *
     * @return optional declared type
     */
    public Optional<Type> getDeclaredType() {
        return Optional.ofNullable(declaredType);
    }

    /**
     * Field, record component, parameter, or other declaration being generated.
     *
     * @return optional reflected declaration
     */
    public Optional<AnnotatedElement> getDeclaration() {
        return Optional.ofNullable(declaration);
    }

    /**
     * Active generator configuration.
     *
     * @return optional active configuration
     */
    public Optional<GeneratorConfig> getConfig() {
        return Optional.ofNullable(config);
    }

    private static String simpleName(Class<?> type) {
        Class<?> value = Objects.requireNonNull(type, "ownerType must not be null");
        return value.getSimpleName().isBlank() ? value.getName() : value.getSimpleName();
    }
}
