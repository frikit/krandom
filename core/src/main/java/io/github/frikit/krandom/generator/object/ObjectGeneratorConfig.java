/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.object;

import io.github.frikit.krandom.generator.ContextualGenerator;
import io.github.frikit.krandom.generator.Generator;
import io.github.frikit.krandom.generator.GeneratorConfig;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Internal read-only object-generation view of {@link GeneratorConfig}.
 *
 * <p>{@link GeneratorConfig} is the single owner of all object-generation settings. This adapter
 * exists only to avoid spreading object-setting accessor names throughout the object-generation
 * implementation while that implementation is package-local. It never stores or merges a second
 * copy of those settings.
 */
final class ObjectGeneratorConfig {

    static final int DEFAULT_MAX_DEPTH        = GeneratorConfig.DEFAULT_OBJECT_MAX_DEPTH;
    static final int DEFAULT_OBJECT_POOL_SIZE = GeneratorConfig.DEFAULT_OBJECT_POOL_SIZE;

    private final GeneratorConfig generatorConfig;

    private ObjectGeneratorConfig(GeneratorConfig generatorConfig) {
        this.generatorConfig = Objects.requireNonNull(generatorConfig, "generatorConfig must not be null");
    }

    /**
     * Returns a builder for the internal compatibility adapter.
     */
    public static Builder builder() {
        return new Builder(GeneratorConfig.defaults());
    }

    /**
     * Returns an adapter over an already-built root configuration.
     */
    static ObjectGeneratorConfig from(GeneratorConfig generatorConfig) {
        return new ObjectGeneratorConfig(generatorConfig);
    }

    /**
     * Creates a builder initialized from this adapter's root configuration.
     */
    public Builder toBuilder() {
        return new Builder(generatorConfig);
    }

    public int getMaxDepth() {
        return generatorConfig.getObjectMaxDepth();
    }

    public GeneratorConfig getGeneratorConfig() {
        return generatorConfig;
    }

    public int getObjectPoolSize() {
        return generatorConfig.getObjectPoolSize();
    }

    public boolean isOverrideDefaultInitialization() {
        return generatorConfig.isObjectOverrideDefaultInitialization();
    }

    public ObjectConstructionPolicy getConstructionPolicy() {
        return generatorConfig.getObjectConstructionPolicy();
    }

    public boolean isIgnoreErrors() {
        return generatorConfig.isObjectIgnoreErrors();
    }

    public Class<?> resolveSubtype(Class<?> declaredType) {
        return generatorConfig.getObjectSubtypes().getOrDefault(declaredType, declaredType);
    }

    public ObjectGenerationSemanticMode getSemanticMode() {
        return generatorConfig.getObjectSemanticMode();
    }

    public SemanticFieldRegistry getSemanticRegistry() {
        return generatorConfig.getObjectSemanticRegistry();
    }

    public double getNullProbability() {
        return generatorConfig.getObjectNullProbability();
    }

    public double getOptionalEmptyProbability() {
        return generatorConfig.getObjectOptionalEmptyProbability();
    }

    public Set<String> getUniqueFieldNames() {
        return generatorConfig.getObjectUniqueFieldNames();
    }

    public int getUniquenessMaxAttempts() {
        return generatorConfig.getObjectUniquenessMaxAttempts();
    }

    public LocalDate getDateMin() {
        return generatorConfig.getObjectDateMin();
    }

    public LocalDate getDateMax() {
        return generatorConfig.getObjectDateMax();
    }

    public Optional<Generator<?>> getTypeOverride(Class<?> type) {
        return generatorConfig.getObjectTypeOverride(type);
    }

    public Optional<Generator<?>> getFieldOverride(Class<?> ownerType, String fieldName) {
        return generatorConfig.getObjectFieldOverride(ownerType, fieldName);
    }

    public Optional<Generator<?>> getFieldPredicateOverride(Field field) {
        return generatorConfig.getObjectFieldPredicateOverride(field);
    }

    public Optional<ContextualGenerator<?>> getContextualTypeOverride(Class<?> type) {
        return generatorConfig.getObjectContextualTypeOverride(type);
    }

    public Optional<ContextualGenerator<?>> getContextualFieldOverride(Class<?> ownerType, String fieldName) {
        return generatorConfig.getObjectContextualFieldOverride(ownerType, fieldName);
    }

    public Optional<ContextualGenerator<?>> getContextualFieldPredicateOverride(Field field) {
        return generatorConfig.getObjectContextualFieldPredicateOverride(field);
    }

    public boolean shouldExclude(Field field) {
        return field.isAnnotationPresent(Exclude.class) || generatorConfig.shouldObjectExclude(field);
    }

    /**
     * Returns the single root configuration without reconstructing it.
     */
    public GeneratorConfig toGeneratorConfig() {
        return generatorConfig;
    }

    /**
     * Internal fluent bridge kept for package-local callers and tests.
     *
     * <p>Each method immediately updates one root {@link GeneratorConfig.Builder}. The operation
     * list exists only so a later {@link #generatorConfig(GeneratorConfig)} call can retain the
     * established internal-builder precedence without storing another configuration value.
     */
    public static final class Builder {

        private GeneratorConfig                             rootConfig;
        private GeneratorConfig.Builder                     rootBuilder;
        private final List<Consumer<GeneratorConfig.Builder>> localOperations = new ArrayList<>();

        private Builder(GeneratorConfig generatorConfig) {
            this.rootConfig = Objects.requireNonNull(generatorConfig, "generatorConfig must not be null");
        }

        public Builder generatorConfig(GeneratorConfig generatorConfig) {
            rootConfig = Objects.requireNonNull(generatorConfig, "generatorConfig must not be null");
            rootBuilder = null;
            if (!localOperations.isEmpty()) {
                GeneratorConfig.Builder builder = rootConfig.toBuilder();
                localOperations.forEach(operation -> operation.accept(builder));
                rootBuilder = builder;
            }
            return this;
        }

        public Builder maxDepth(int maxDepth) {
            return apply(builder -> builder.objectMaxDepth(maxDepth));
        }

        public Builder objectPoolSize(int objectPoolSize) {
            return apply(builder -> builder.objectPoolSize(objectPoolSize));
        }

        public Builder overrideDefaultInitialization(boolean overrideDefaultInitialization) {
            return apply(builder -> builder.objectOverrideDefaultInitialization(overrideDefaultInitialization));
        }

        public Builder semanticMode(ObjectGenerationSemanticMode semanticMode) {
            return apply(builder -> builder.objectSemanticMode(semanticMode));
        }

        public Builder semanticRegistry(SemanticFieldRegistry semanticRegistry) {
            return apply(builder -> builder.objectSemanticRegistry(semanticRegistry));
        }

        public Builder nullProbability(double nullProbability) {
            return apply(builder -> builder.objectNullProbability(nullProbability));
        }

        public Builder optionalEmptyProbability(double optionalEmptyProbability) {
            return apply(builder -> builder.objectOptionalEmptyProbability(optionalEmptyProbability));
        }

        public Builder uniqueFields(String... fieldNames) {
            Objects.requireNonNull(fieldNames, "fieldNames must not be null");
            String[] copiedFieldNames = fieldNames.clone();
            return apply(builder -> builder.objectUniqueFields(copiedFieldNames));
        }

        public Builder uniqueField(String fieldName) {
            return apply(builder -> builder.objectUniqueField(fieldName));
        }

        public Builder uniquenessMaxAttempts(int uniquenessMaxAttempts) {
            return apply(builder -> builder.objectUniquenessMaxAttempts(uniquenessMaxAttempts));
        }

        public Builder dateRange(LocalDate min, LocalDate max) {
            return apply(builder -> builder.objectDateRange(min, max));
        }

        public <T> Builder override(Class<T> type, Generator<? extends T> generator) {
            return apply(builder -> builder.objectOverride(type, generator));
        }

        public <T> Builder override(Class<?> ownerType, String fieldName, Generator<T> generator) {
            return apply(builder -> builder.objectOverride(ownerType, fieldName, generator));
        }

        public <T> Builder override(Predicate<Field> predicate, Generator<T> generator) {
            return apply(builder -> builder.objectOverride(predicate, generator));
        }

        public <T> Builder override(Class<T> type, ContextualGenerator<? extends T> generator) {
            return apply(builder -> builder.objectOverride(type, generator));
        }

        public <T> Builder override(Class<?> ownerType, String fieldName, ContextualGenerator<T> generator) {
            return apply(builder -> builder.objectOverride(ownerType, fieldName, generator));
        }

        public <T> Builder override(Predicate<Field> predicate, ContextualGenerator<T> generator) {
            return apply(builder -> builder.objectOverride(predicate, generator));
        }

        public Builder exclude(Predicate<Field> predicate) {
            return apply(builder -> builder.objectExclude(predicate));
        }

        public Builder excludeField(String name) {
            return apply(builder -> builder.objectExcludeField(name));
        }

        public Builder excludeType(Class<?> type) {
            return apply(builder -> builder.objectExcludeType(type));
        }

        public Builder excludeType(Predicate<Class<?>> predicate) {
            return apply(builder -> builder.objectExcludeType(predicate));
        }

        public Builder ignoreErrors(boolean ignoreErrors) {
            return apply(builder -> builder.objectIgnoreErrors(ignoreErrors));
        }

        public Builder subtype(Class<?> declaredType, Class<?> implementationType) {
            return apply(builder -> builder.objectSubtype(declaredType, implementationType));
        }

        private Builder apply(Consumer<GeneratorConfig.Builder> operation) {
            if (rootBuilder == null) {
                rootBuilder = rootConfig.toBuilder();
            }
            operation.accept(rootBuilder);
            localOperations.add(operation);
            return this;
        }

        public ObjectGeneratorConfig build() {
            return new ObjectGeneratorConfig(rootBuilder == null ? rootConfig : rootBuilder.build());
        }
    }
}
