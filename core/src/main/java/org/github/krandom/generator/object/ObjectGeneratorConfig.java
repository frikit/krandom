/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.object;

import org.github.krandom.generator.Generator;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Immutable configuration for {@link ObjectGenerator}.
 *
 * <p>Build via the fluent {@link Builder}:
 * <pre>{@code
 *   ObjectGeneratorConfig config = ObjectGeneratorConfig.builder()
 *       .maxDepth(3)
 *       .override(String.class, () -> "fixed")
 *       .override(Person.class, "firstName", () -> "Alice")
 *       .ignoreErrors(true)
 *       .build();
 * }</pre>
 */
public final class ObjectGeneratorConfig {

    static final int DEFAULT_MAX_DEPTH = 5;

    private final int maxDepth;
    private final boolean ignoreErrors;

    /**
     * Type-level overrides: when a field has this type, use this generator instead of
     * the built-in or recursively generated one.
     * Key: exact {@link Class} object.
     */
    private final Map<Class<?>, Generator<?>> typeOverrides;

    /**
     * Field-level overrides: keyed by {@code "SimpleClassName.fieldName"}.
     * Takes precedence over type overrides.
     */
    private final Map<String, Generator<?>> fieldOverrides;

    private ObjectGeneratorConfig(Builder b) {
        this.maxDepth      = b.maxDepth;
        this.ignoreErrors  = b.ignoreErrors;
        this.typeOverrides  = Collections.unmodifiableMap(new HashMap<>(b.typeOverrides));
        this.fieldOverrides = Collections.unmodifiableMap(new HashMap<>(b.fieldOverrides));
    }

    /** Default configuration. */
    public static ObjectGeneratorConfig defaults() {
        return builder().build();
    }

    public static Builder builder() {
        return new Builder();
    }

    // ── Accessors ─────────────────────────────────────────────────────────────

    /**
     * Maximum object-graph depth. Nested objects beyond this depth are set to {@code null}.
     * Default: {@value DEFAULT_MAX_DEPTH}.
     */
    public int getMaxDepth() { return maxDepth; }

    /**
     * When {@code true}, exceptions during field population are swallowed and the field
     * is left as {@code null} / its primitive default. Default: {@code false}.
     */
    public boolean isIgnoreErrors() { return ignoreErrors; }

    /** Return the type-level override for {@code type}, if any. */
    public Optional<Generator<?>> getTypeOverride(Class<?> type) {
        return Optional.ofNullable(typeOverrides.get(type));
    }

    /**
     * Return the field-level override for {@code fieldName} declared in {@code ownerType}, if any.
     * The lookup key is {@code "SimpleClassName.fieldName"}.
     */
    public Optional<Generator<?>> getFieldOverride(Class<?> ownerType, String fieldName) {
        String key = ownerType.getSimpleName() + "." + fieldName;
        return Optional.ofNullable(fieldOverrides.get(key));
    }

    // ── Builder ───────────────────────────────────────────────────────────────

    public static final class Builder {

        private int  maxDepth     = DEFAULT_MAX_DEPTH;
        private boolean ignoreErrors = false;
        private final Map<Class<?>, Generator<?>> typeOverrides  = new HashMap<>();
        private final Map<String, Generator<?>>   fieldOverrides = new HashMap<>();

        /**
         * Maximum nesting depth for object-graph generation.
         * Objects beyond this depth are assigned {@code null}.
         */
        public Builder maxDepth(int maxDepth) {
            if (maxDepth < 1) throw new IllegalArgumentException("maxDepth must be >= 1, was: " + maxDepth);
            this.maxDepth = maxDepth;
            return this;
        }

        /**
         * Register a {@link Generator} for all fields of the given type.
         * <pre>{@code .override(String.class, () -> "hello") }</pre>
         */
        public <T> Builder override(Class<T> type, Generator<? extends T> generator) {
            Objects.requireNonNull(type,      "type must not be null");
            Objects.requireNonNull(generator, "generator must not be null");
            typeOverrides.put(type, generator);
            return this;
        }

        /**
         * Register a {@link Generator} for a specific field on a specific class.
         * <pre>{@code .override(Person.class, "firstName", () -> "Alice") }</pre>
         */
        public <T> Builder override(Class<?> ownerType, String fieldName, Generator<T> generator) {
            Objects.requireNonNull(ownerType,  "ownerType must not be null");
            Objects.requireNonNull(fieldName,  "fieldName must not be null");
            Objects.requireNonNull(generator,  "generator must not be null");
            fieldOverrides.put(ownerType.getSimpleName() + "." + fieldName, generator);
            return this;
        }

        /**
         * When {@code true}, population errors are swallowed; fields are left {@code null}.
         * When {@code false} (default), errors propagate as {@link
         * org.github.krandom.generator.object.exception.ObjectGenerationException}.
         */
        public Builder ignoreErrors(boolean ignoreErrors) {
            this.ignoreErrors = ignoreErrors;
            return this;
        }

        public ObjectGeneratorConfig build() {
            return new ObjectGeneratorConfig(this);
        }
    }
}
