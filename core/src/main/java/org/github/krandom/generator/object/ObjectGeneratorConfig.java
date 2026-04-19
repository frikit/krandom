/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.object;

import org.github.krandom.generator.ContextualGenerator;
import org.github.krandom.generator.Generator;
import org.github.krandom.generator.GeneratorConfig;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Immutable configuration for {@link ObjectGenerator}.
 *
 * <p>Build via the fluent {@link Builder}:
 * <pre>{@code
 *   ObjectGeneratorConfig config = ObjectGeneratorConfig.builder()
 *       .maxDepth(3)
 *       .override(String.class, () -> "fixed")
 *       .override(Person.class, "firstName", () -> "Alice")
 *       .excludeField("password")
 *       .excludeType(String.class)
 *       .ignoreErrors(true)
 *       .dateRange(LocalDate.of(2020, 1, 1), LocalDate.of(2023, 12, 31))
 *       .build();
 * }</pre>
 */
public final class ObjectGeneratorConfig {

    static final int DEFAULT_MAX_DEPTH        = GeneratorConfig.DEFAULT_OBJECT_MAX_DEPTH;
    static final int DEFAULT_OBJECT_POOL_SIZE = GeneratorConfig.DEFAULT_OBJECT_POOL_SIZE;

    private final GeneratorConfig generatorConfig;
    private final int     maxDepth;
    private final int     objectPoolSize;
    private final boolean overrideDefaultInitialization;
    private final boolean ignoreErrors;
    private final ObjectGenerationSemanticMode semanticMode;
    private final double                       nullProbability;
    private final double                       optionalEmptyProbability;
    private final Set<String>                  uniqueFieldNames;
    private final int                          uniquenessMaxAttempts;

    /**
     * Global date range applied to all JSR-310 date/time fields.
     * {@code null} means use each generator's built-in default.
     */
    private final LocalDate dateMin;
    private final LocalDate dateMax;

    /**
     * Type-level overrides: when a field has this type, use this generator instead of
     * the built-in or recursively generated one.
     * Key: exact {@link Class} object.
     */
    private final Map<Class<?>, Generator<?>> typeOverrides;

    /**
     * Field-level overrides: keyed by {@code "fully.qualified.ClassName.fieldName"}.
     * Takes precedence over type overrides.
     */
    private final Map<String, Generator<?>> fieldOverrides;

    /**
     * Context-aware type-level overrides.  Checked before plain type overrides.
     */
    private final Map<Class<?>, ContextualGenerator<?>> contextualTypeOverrides;

    /**
     * Context-aware field-level overrides keyed by {@code "fully.qualified.ClassName.fieldName"}.
     * Checked before plain field overrides.
     */
    private final Map<String, ContextualGenerator<?>> contextualFieldOverrides;

    /**
     * Predicates that identify fields to skip during population.
     * Also honours the {@link Exclude} annotation directly.
     */
    private final List<Predicate<Field>>    exclusionPredicates;
    /**
     * Predicates that identify field types to skip during population.
     */
    private final List<Predicate<Class<?>>> typeExclusionPredicates;

    private ObjectGeneratorConfig(Builder b) {
        this.generatorConfig = b.generatorConfig;
        this.maxDepth = b.maxDepth;
        this.objectPoolSize = b.objectPoolSize;
        this.overrideDefaultInitialization = b.overrideDefaultInitialization;
        this.ignoreErrors = b.ignoreErrors;
        this.semanticMode = b.semanticMode;
        this.nullProbability = b.nullProbability;
        this.optionalEmptyProbability = b.optionalEmptyProbability;
        this.uniqueFieldNames = Collections.unmodifiableSet(new LinkedHashSet<>(b.uniqueFieldNames));
        this.uniquenessMaxAttempts = b.uniquenessMaxAttempts;
        this.dateMin = b.dateMin;
        this.dateMax = b.dateMax;
        this.typeOverrides = Collections.unmodifiableMap(new HashMap<>(b.typeOverrides));
        this.fieldOverrides = Collections.unmodifiableMap(new HashMap<>(b.fieldOverrides));
        this.contextualTypeOverrides = Collections.unmodifiableMap(new HashMap<>(b.contextualTypeOverrides));
        this.contextualFieldOverrides = Collections.unmodifiableMap(new HashMap<>(b.contextualFieldOverrides));
        this.exclusionPredicates = Collections.unmodifiableList(new ArrayList<>(b.exclusionPredicates));
        this.typeExclusionPredicates = Collections.unmodifiableList(new ArrayList<>(b.typeExclusionPredicates));
    }

    /**
     * Default configuration.
     */
    public static ObjectGeneratorConfig defaults() {
        return builder().build();
    }

    /**
     * Returns a new {@link Builder}.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Creates a builder pre-populated from this config.
     */
    public Builder toBuilder() {
        return new Builder(this);
    }

    // ── Accessors ─────────────────────────────────────────────────────────────

    private static String fieldKey(Class<?> ownerType, String fieldName) {
        return ownerType.getName() + "." + fieldName;
    }

    private static String legacyFieldKey(Class<?> ownerType, String fieldName) {
        return ownerType.getSimpleName() + "." + fieldName;
    }

    /**
     * Maximum object-graph depth. Nested objects beyond this depth are set to {@code null}.
     * Default: {@value DEFAULT_MAX_DEPTH}.
     */
    public int getMaxDepth() {
        return maxDepth;
    }

    /**
     * Shared generator configuration applied to built-in field generators.
     */
    public GeneratorConfig getGeneratorConfig() {
        return generatorConfig;
    }

    /**
     * Maximum number of completed instances cached per type for cycle handling.
     * Default: {@value DEFAULT_OBJECT_POOL_SIZE}.
     */
    public int getObjectPoolSize() {
        return objectPoolSize;
    }

    /**
     * When {@code true}, existing field values are overwritten during population.
     * When {@code false}, only fields currently at JVM defaults are populated.
     * Default: {@code false}.
     */
    public boolean isOverrideDefaultInitialization() {
        return overrideDefaultInitialization;
    }

    /**
     * When {@code true}, exceptions during field population are swallowed and the field
     * is left as {@code null} / its primitive default. Default: {@code false}.
     */
    public boolean isIgnoreErrors() {
        return ignoreErrors;
    }

    /**
     * Semantic mode used by object generation.
     */
    public ObjectGenerationSemanticMode getSemanticMode() {
        return semanticMode;
    }

    /**
     * Probability that a nullable reference field resolves to {@code null}.
     */
    public double getNullProbability() {
        return nullProbability;
    }

    /**
     * Probability that an {@code Optional<T>} field resolves to {@code Optional.empty()}.
     */
    public double getOptionalEmptyProbability() {
        return optionalEmptyProbability;
    }

    /**
     * Normalized field names that should be unique within one generator sequence.
     */
    public Set<String> getUniqueFieldNames() {
        return uniqueFieldNames;
    }

    /**
     * Maximum attempts used when generating a unique field value.
     */
    public int getUniquenessMaxAttempts() {
        return uniquenessMaxAttempts;
    }

    /**
     * Earliest date (inclusive) used for all JSR-310 date/time fields.
     * {@code null} means use the generator's built-in default (1970-01-01).
     */
    public LocalDate getDateMin() {
        return dateMin;
    }

    /**
     * Latest date (inclusive) used for all JSR-310 date/time fields.
     * {@code null} means use the generator's built-in default (2100-12-31).
     */
    public LocalDate getDateMax() {
        return dateMax;
    }

    /**
     * Return the type-level override for {@code type}, if any.
     */
    public Optional<Generator<?>> getTypeOverride(Class<?> type) {
        return Optional.ofNullable(typeOverrides.get(type));
    }

    /**
     * Return the field-level override for {@code fieldName} declared in {@code ownerType}, if any.
     *
     * <p>Primary lookup key is {@code "fully.qualified.ClassName.fieldName"}.
     * Legacy simple-name keys are still supported for backward compatibility.
     */
    public Optional<Generator<?>> getFieldOverride(Class<?> ownerType, String fieldName) {
        String key = fieldKey(ownerType, fieldName);
        Generator<?> direct = fieldOverrides.get(key);
        if (direct != null) {
            return Optional.of(direct);
        }
        return Optional.ofNullable(fieldOverrides.get(legacyFieldKey(ownerType, fieldName)));
    }

    /**
     * Return the contextual type-level override for {@code type}, if any.
     */
    public Optional<ContextualGenerator<?>> getContextualTypeOverride(Class<?> type) {
        return Optional.ofNullable(contextualTypeOverrides.get(type));
    }

    /**
     * Return the contextual field-level override for {@code fieldName} declared in
     * {@code ownerType}, if any.
     *
     * <p>Primary lookup key is {@code "fully.qualified.ClassName.fieldName"}.
     * Legacy simple-name keys are still supported for backward compatibility.
     */
    public Optional<ContextualGenerator<?>> getContextualFieldOverride(Class<?> ownerType, String fieldName) {
        String key = fieldKey(ownerType, fieldName);
        ContextualGenerator<?> direct = contextualFieldOverrides.get(key);
        if (direct != null) {
            return Optional.of(direct);
        }
        return Optional.ofNullable(contextualFieldOverrides.get(legacyFieldKey(ownerType, fieldName)));
    }

    /**
     * Returns {@code true} if the given field should be excluded from population.
     *
     * <p>A field is excluded when:
     * <ul>
     *   <li>it carries the {@link Exclude} annotation, or</li>
     *   <li>any registered field exclusion predicate returns {@code true} for it, or</li>
     *   <li>any registered type exclusion predicate returns {@code true} for its type.</li>
     * </ul>
     *
     * @param field the field to test; must not be {@code null}
     * @return {@code true} if the field must be skipped
     */
    public boolean shouldExclude(Field field) {
        if (field.isAnnotationPresent(Exclude.class)) return true;
        for (Predicate<Field> predicate : exclusionPredicates) {
            if (predicate.test(field)) return true;
        }
        for (Predicate<Class<?>> predicate : typeExclusionPredicates) {
            if (predicate.test(field.getType())) return true;
        }
        return false;
    }

    // ── Builder ───────────────────────────────────────────────────────────────


    /**
     * Fluent builder for {@link ObjectGeneratorConfig}.
     */
    public static final class Builder {

        private final Map<Class<?>, Generator<?>>           typeOverrides                 = new HashMap<>();
        private final Map<String, Generator<?>>             fieldOverrides                = new HashMap<>();
        private final Map<Class<?>, ContextualGenerator<?>> contextualTypeOverrides       = new HashMap<>();
        private final Map<String, ContextualGenerator<?>>   contextualFieldOverrides      = new HashMap<>();
        private final List<Predicate<Field>>                exclusionPredicates           = new ArrayList<>();
        private final List<Predicate<Class<?>>>             typeExclusionPredicates       = new ArrayList<>();
        private       GeneratorConfig                       generatorConfig               = GeneratorConfig.defaults();
        private       int                                   maxDepth                      = DEFAULT_MAX_DEPTH;
        private       int                                   objectPoolSize                = DEFAULT_OBJECT_POOL_SIZE;
        private       boolean                               overrideDefaultInitialization = false;
        private       boolean                               ignoreErrors                  = false;
        private       ObjectGenerationSemanticMode          semanticMode                  = ObjectGenerationSemanticMode.RELAXED;
        private       double                                nullProbability;
        private       double                                optionalEmptyProbability;
        private       Set<String>                           uniqueFieldNames              =
            new LinkedHashSet<>(GeneratorConfig.defaults().getObjectUniqueFieldNames());
        private       int                                   uniquenessMaxAttempts         =
            GeneratorConfig.defaults().getObjectUniquenessMaxAttempts();
        private       LocalDate                             dateMin                       = null;
        private       LocalDate                             dateMax                       = null;
        private       boolean                               maxDepthExplicit;
        private       boolean                               objectPoolSizeExplicit;
        private       boolean                               overrideDefaultInitializationExplicit;
        private       boolean                               ignoreErrorsExplicit;
        private       boolean                               semanticModeExplicit;
        private       boolean                               nullProbabilityExplicit;
        private       boolean                               optionalEmptyProbabilityExplicit;
        private       boolean                               uniqueFieldNamesExplicit;
        private       boolean                               uniquenessMaxAttemptsExplicit;
        private       boolean                               dateRangeExplicit;

        private Builder() {
        }

        private Builder(ObjectGeneratorConfig source) {
            this.generatorConfig = source.generatorConfig;
            this.maxDepth = source.maxDepth;
            this.objectPoolSize = source.objectPoolSize;
            this.overrideDefaultInitialization = source.overrideDefaultInitialization;
            this.ignoreErrors = source.ignoreErrors;
            this.semanticMode = source.semanticMode;
            this.nullProbability = source.nullProbability;
            this.optionalEmptyProbability = source.optionalEmptyProbability;
            this.uniqueFieldNames = new LinkedHashSet<>(source.uniqueFieldNames);
            this.uniquenessMaxAttempts = source.uniquenessMaxAttempts;
            this.dateMin = source.dateMin;
            this.dateMax = source.dateMax;
            this.maxDepthExplicit = true;
            this.objectPoolSizeExplicit = true;
            this.overrideDefaultInitializationExplicit = true;
            this.ignoreErrorsExplicit = true;
            this.semanticModeExplicit = true;
            this.nullProbabilityExplicit = true;
            this.optionalEmptyProbabilityExplicit = true;
            this.uniqueFieldNamesExplicit = true;
            this.uniquenessMaxAttemptsExplicit = true;
            this.dateRangeExplicit = source.dateMin != null || source.dateMax != null;
            this.typeOverrides.putAll(source.typeOverrides);
            this.fieldOverrides.putAll(source.fieldOverrides);
            this.contextualTypeOverrides.putAll(source.contextualTypeOverrides);
            this.contextualFieldOverrides.putAll(source.contextualFieldOverrides);
            this.exclusionPredicates.addAll(source.exclusionPredicates);
            this.typeExclusionPredicates.addAll(source.typeExclusionPredicates);
        }

        /**
         * Shared root configuration for built-in generators used during object population.
         */
        public Builder generatorConfig(GeneratorConfig generatorConfig) {
            this.generatorConfig = Objects.requireNonNull(generatorConfig, "generatorConfig must not be null");
            inheritObjectDefaults(generatorConfig);
            return this;
        }

        /**
         * Maximum nesting depth for object-graph generation.
         * Objects beyond this depth are assigned {@code null}.
         */
        public Builder maxDepth(int maxDepth) {
            if (maxDepth < 1) throw new IllegalArgumentException("maxDepth must be >= 1, was: " + maxDepth);
            this.maxDepth = maxDepth;
            this.maxDepthExplicit = true;
            return this;
        }

        /**
         * Maximum number of completed instances to retain per type in the object pool.
         * Use {@code 0} to disable completed-instance caching while still detecting cycles.
         */
        public Builder objectPoolSize(int objectPoolSize) {
            if (objectPoolSize < 0) {
                throw new IllegalArgumentException("objectPoolSize must be >= 0, was: " + objectPoolSize);
            }
            this.objectPoolSize = objectPoolSize;
            this.objectPoolSizeExplicit = true;
            return this;
        }

        /**
         * Controls whether non-default field values are overwritten.
         *
         * <p>When set to {@code false}, fields that already hold non-default values
         * (constructor/initializer assigned) are preserved.
         */
        public Builder overrideDefaultInitialization(boolean overrideDefaultInitialization) {
            this.overrideDefaultInitialization = overrideDefaultInitialization;
            this.overrideDefaultInitializationExplicit = true;
            return this;
        }

        /**
         * Controls how strongly semantic field-name matching influences object generation.
         */
        public Builder semanticMode(ObjectGenerationSemanticMode semanticMode) {
            this.semanticMode = Objects.requireNonNull(semanticMode, "semanticMode");
            this.semanticModeExplicit = true;
            return this;
        }

        /**
         * Sets the probability that nullable reference fields resolve to {@code null}.
         */
        public Builder nullProbability(double nullProbability) {
            this.nullProbability = requireProbability("nullProbability", nullProbability);
            this.nullProbabilityExplicit = true;
            return this;
        }

        /**
         * Sets the probability that {@code Optional<T>} fields resolve to {@code Optional.empty()}.
         */
        public Builder optionalEmptyProbability(double optionalEmptyProbability) {
            this.optionalEmptyProbability = requireProbability("optionalEmptyProbability", optionalEmptyProbability);
            this.optionalEmptyProbabilityExplicit = true;
            return this;
        }

        /**
         * Replaces the normalized field names that should be unique within one generator sequence.
         */
        public Builder uniqueFields(String... fieldNames) {
            Objects.requireNonNull(fieldNames, "fieldNames");
            LinkedHashSet<String> normalized = new LinkedHashSet<>();
            for (String fieldName : fieldNames) {
                normalized.add(normalizeFieldName(fieldName));
            }
            this.uniqueFieldNames = normalized;
            this.uniqueFieldNamesExplicit = true;
            return this;
        }

        /**
         * Adds one field name to the uniqueness set.
         */
        public Builder uniqueField(String fieldName) {
            this.uniqueFieldNames.add(normalizeFieldName(fieldName));
            this.uniqueFieldNamesExplicit = true;
            return this;
        }

        /**
         * Maximum attempts used when generating unique field values.
         */
        public Builder uniquenessMaxAttempts(int uniquenessMaxAttempts) {
            if (uniquenessMaxAttempts < 1) {
                throw new IllegalArgumentException("uniquenessMaxAttempts must be >= 1");
            }
            this.uniquenessMaxAttempts = uniquenessMaxAttempts;
            this.uniquenessMaxAttemptsExplicit = true;
            return this;
        }

        /**
         * Global date range applied to all JSR-310 date/time fields generated by
         * {@link ObjectGenerator}.
         *
         * @param min earliest date (inclusive); must not be {@code null}
         * @param max latest date (inclusive); must not be {@code null} and must be &gt;= {@code min}
         */
        public Builder dateRange(LocalDate min, LocalDate max) {
            Objects.requireNonNull(min, "min must not be null");
            Objects.requireNonNull(max, "max must not be null");
            if (min.isAfter(max)) {
                throw new IllegalArgumentException("dateMin must be <= dateMax, got " + min + " > " + max);
            }
            this.dateMin = min;
            this.dateMax = max;
            this.dateRangeExplicit = true;
            return this;
        }

        /**
         * Register a {@link Generator} for all fields of the given type.
         * <pre>{@code .override(String.class, () -> "hello") }</pre>
         */
        public <T> Builder override(Class<T> type, Generator<? extends T> generator) {
            Objects.requireNonNull(type, "type must not be null");
            Objects.requireNonNull(generator, "generator must not be null");
            typeOverrides.put(type, generator);
            return this;
        }

        /**
         * Register a {@link Generator} for a specific field on a specific class.
         * <pre>{@code .override(Person.class, "firstName", () -> "Alice") }</pre>
         */
        public <T> Builder override(Class<?> ownerType, String fieldName, Generator<T> generator) {
            Objects.requireNonNull(ownerType, "ownerType must not be null");
            Objects.requireNonNull(fieldName, "fieldName must not be null");
            Objects.requireNonNull(generator, "generator must not be null");
            fieldOverrides.put(fieldKey(ownerType, fieldName), generator);
            return this;
        }

        /**
         * Register a context-aware {@link ContextualGenerator} for all fields of the given type.
         * The generator receives a {@link org.github.krandom.generator.GenerationContext}
         * describing the field being populated.
         * <pre>{@code .override(String.class, ctx -> ctx.getFieldName() + "_value") }</pre>
         */
        public <T> Builder override(Class<T> type, ContextualGenerator<? extends T> generator) {
            Objects.requireNonNull(type, "type must not be null");
            Objects.requireNonNull(generator, "generator must not be null");
            contextualTypeOverrides.put(type, generator);
            return this;
        }

        /**
         * Register a context-aware {@link ContextualGenerator} for a specific field on a
         * specific class.
         * <pre>{@code .override(Person.class, "name", ctx -> "Alice_" + ctx.getDepth()) }</pre>
         */
        public <T> Builder override(Class<?> ownerType, String fieldName, ContextualGenerator<T> generator) {
            Objects.requireNonNull(ownerType, "ownerType must not be null");
            Objects.requireNonNull(fieldName, "fieldName must not be null");
            Objects.requireNonNull(generator, "generator must not be null");
            contextualFieldOverrides.put(fieldKey(ownerType, fieldName), generator);
            return this;
        }

        /**
         * Add a custom predicate that identifies fields to skip.
         *
         * @param predicate a test applied to each field; must not be {@code null}
         * @see FieldPredicates
         */
        public Builder exclude(Predicate<Field> predicate) {
            Objects.requireNonNull(predicate, "predicate must not be null");
            exclusionPredicates.add(predicate);
            return this;
        }

        /**
         * Exclude all fields whose name equals {@code name}.
         *
         * <pre>{@code .excludeField("password") }</pre>
         */
        public Builder excludeField(String name) {
            return exclude(FieldPredicates.named(name));
        }

        /**
         * Exclude all fields whose declared type is exactly {@code type}.
         *
         * <pre>{@code .excludeType(String.class) }</pre>
         */
        public Builder excludeType(Class<?> type) {
            return exclude(FieldPredicates.ofType(type));
        }

        /**
         * Exclude all fields whose declared type matches {@code predicate}.
         *
         * <pre>{@code .excludeType(TypePredicates.inPackage("java.time")) }</pre>
         */
        public Builder excludeType(Predicate<Class<?>> predicate) {
            Objects.requireNonNull(predicate, "predicate must not be null");
            typeExclusionPredicates.add(predicate);
            return this;
        }

        /**
         * When {@code true}, population errors are swallowed; fields are left {@code null}.
         * When {@code false} (default), errors propagate as {@link
         * org.github.krandom.generator.object.exception.ObjectGenerationException}.
         */
        public Builder ignoreErrors(boolean ignoreErrors) {
            this.ignoreErrors = ignoreErrors;
            this.ignoreErrorsExplicit = true;
            return this;
        }

        private void inheritObjectDefaults(GeneratorConfig generatorConfig) {
            if (!maxDepthExplicit) {
                this.maxDepth = generatorConfig.getObjectMaxDepth();
            }
            if (!objectPoolSizeExplicit) {
                this.objectPoolSize = generatorConfig.getObjectPoolSize();
            }
            if (!overrideDefaultInitializationExplicit) {
                this.overrideDefaultInitialization = generatorConfig.isObjectOverrideDefaultInitialization();
            }
            if (!ignoreErrorsExplicit) {
                this.ignoreErrors = generatorConfig.isObjectIgnoreErrors();
            }
            if (!semanticModeExplicit) {
                this.semanticMode = generatorConfig.getObjectSemanticMode();
            }
            if (!nullProbabilityExplicit) {
                this.nullProbability = generatorConfig.getObjectNullProbability();
            }
            if (!optionalEmptyProbabilityExplicit) {
                this.optionalEmptyProbability = generatorConfig.getObjectOptionalEmptyProbability();
            }
            if (!uniqueFieldNamesExplicit) {
                this.uniqueFieldNames = new LinkedHashSet<>(generatorConfig.getObjectUniqueFieldNames());
            }
            if (!uniquenessMaxAttemptsExplicit) {
                this.uniquenessMaxAttempts = generatorConfig.getObjectUniquenessMaxAttempts();
            }
            if (!dateRangeExplicit) {
                this.dateMin = generatorConfig.getObjectDateMin();
                this.dateMax = generatorConfig.getObjectDateMax();
            }
        }

        private static double requireProbability(String name, double value) {
            if (Double.isNaN(value) || value < 0.0 || value > 1.0) {
                throw new IllegalArgumentException(name + " must be between 0.0 and 1.0");
            }
            return value;
        }

        private static String normalizeFieldName(String fieldName) {
            Objects.requireNonNull(fieldName, "fieldName");
            StringBuilder normalized = new StringBuilder(fieldName.length());
            for (int i = 0; i < fieldName.length(); i++) {
                char ch = fieldName.charAt(i);
                if (Character.isLetterOrDigit(ch)) {
                    normalized.append(Character.toLowerCase(ch));
                }
            }
            if (normalized.isEmpty()) {
                throw new IllegalArgumentException("fieldName must contain at least one letter or digit");
            }
            return normalized.toString();
        }

        /**
         * Build the immutable config.
         */
        public ObjectGeneratorConfig build() {
            return new ObjectGeneratorConfig(this);
        }
    }
}
