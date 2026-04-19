/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator;

import org.github.krandom.generator.object.ObjectGenerationSemanticMode;

import java.nio.charset.StandardCharsets;
import java.nio.charset.Charset;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.Random;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Immutable configuration shared across generators.
 *
 * <p>Obtain an instance via the fluent {@link Builder}:
 * <pre>{@code
 *   GeneratorConfig config = GeneratorConfig.builder()
 *       .seed(42L)
 *       .charset(StandardCharsets.UTF_8)
 *       .stringLength(8, 32)
 *       .locale(Locale.GERMANY)
 *       .build();
 * }</pre>
 * <p>
 * or use {@link #defaults()} for a zero-configuration instance.
 */
public final class GeneratorConfig {

    /**
     * Stable algorithm ID used to derive {@code long} seeds from string seeds.
     */
    public static final String STRING_SEED_DERIVATION = "fnv1a64-v1";
    public static final int    DEFAULT_OBJECT_MAX_DEPTH = 5;
    public static final int    DEFAULT_OBJECT_POOL_SIZE = 10;
    private static final int   DEFAULT_OBJECT_UNIQUENESS_ATTEMPTS = 256;

    private static final long FNV1A_64_OFFSET_BASIS = 0xcbf29ce484222325L;
    private static final long FNV1A_64_PRIME        = 0x100000001b3L;

    private final OptionalLong seed;
    private final Optional<String> stringSeed;
    private final Charset      charset;
    private final int          minStringLength;
    private final int          maxStringLength;
    private final int          minCollectionSize;
    private final int          maxCollectionSize;
    private final int          objectMaxDepth;
    private final int          objectPoolSize;
    private final boolean      objectOverrideDefaultInitialization;
    private final boolean      objectIgnoreErrors;
    private final LocalDate    objectDateMin;
    private final LocalDate    objectDateMax;
    private final ObjectGenerationSemanticMode objectSemanticMode;
    private final double       objectNullProbability;
    private final double       objectOptionalEmptyProbability;
    private final Set<String>  objectUniqueFieldNames;
    private final int          objectUniquenessMaxAttempts;
    private final Locale       locale;
    private final Supplier<Random> randomFactory;
    private final DataRegistryContext registryContext;

    private GeneratorConfig(Builder b) {
        this.seed = effectiveSeed(b.numericSeed, b.stringSeed);
        this.stringSeed = b.stringSeed;
        this.charset = b.charset;
        this.minStringLength = b.minStringLength;
        this.maxStringLength = b.maxStringLength;
        this.minCollectionSize = b.minCollectionSize;
        this.maxCollectionSize = b.maxCollectionSize;
        this.objectMaxDepth = b.objectMaxDepth;
        this.objectPoolSize = b.objectPoolSize;
        this.objectOverrideDefaultInitialization = b.objectOverrideDefaultInitialization;
        this.objectIgnoreErrors = b.objectIgnoreErrors;
        this.objectDateMin = b.objectDateMin;
        this.objectDateMax = b.objectDateMax;
        this.objectSemanticMode = b.objectSemanticMode;
        this.objectNullProbability = b.objectNullProbability;
        this.objectOptionalEmptyProbability = b.objectOptionalEmptyProbability;
        this.objectUniqueFieldNames = Collections.unmodifiableSet(new LinkedHashSet<>(b.objectUniqueFieldNames));
        this.objectUniquenessMaxAttempts = b.objectUniquenessMaxAttempts;
        this.locale = b.locale;
        this.randomFactory = b.randomFactory;
        this.registryContext = b.registryContext;
    }

    /**
     * Config with all defaults applied.
     */
    public static GeneratorConfig defaults() {
        return builder().build();
    }

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

    /**
     * Seed for deterministic generation; empty means {@link java.security.SecureRandom}.
     */
    public OptionalLong getSeed() {
        return seed;
    }

    /**
     * Original string seed used to derive {@link #getSeed()}, when configured.
     */
    public Optional<String> getStringSeed() {
        return stringSeed;
    }

    /**
     * Identifier of the string-seed derivation algorithm.
     */
    public String getSeedDerivationVersion() {
        return STRING_SEED_DERIVATION;
    }

    public Charset getCharset() {
        return charset;
    }

    public int getMinStringLength() {
        return minStringLength;
    }

    public int getMaxStringLength() {
        return maxStringLength;
    }

    public int getMinCollectionSize() {
        return minCollectionSize;
    }

    public int getMaxCollectionSize() {
        return maxCollectionSize;
    }

    /**
     * Maximum nesting depth used by object generation.
     */
    public int getObjectMaxDepth() {
        return objectMaxDepth;
    }

    /**
     * Maximum number of completed instances retained per type during object generation.
     */
    public int getObjectPoolSize() {
        return objectPoolSize;
    }

    /**
     * Controls whether non-default initialized fields are overwritten during object generation.
     */
    public boolean isObjectOverrideDefaultInitialization() {
        return objectOverrideDefaultInitialization;
    }

    /**
     * Controls whether object-generation population errors are swallowed.
     */
    public boolean isObjectIgnoreErrors() {
        return objectIgnoreErrors;
    }

    /**
     * Earliest date used for object-generated temporal fields, or {@code null} for built-in defaults.
     */
    public LocalDate getObjectDateMin() {
        return objectDateMin;
    }

    /**
     * Latest date used for object-generated temporal fields, or {@code null} for built-in defaults.
     */
    public LocalDate getObjectDateMax() {
        return objectDateMax;
    }

    /**
     * Semantic mode used by object generation.
     */
    public ObjectGenerationSemanticMode getObjectSemanticMode() {
        return objectSemanticMode;
    }

    /**
     * Probability that a nullable reference field resolves to {@code null}.
     */
    public double getObjectNullProbability() {
        return objectNullProbability;
    }

    /**
     * Probability that an {@code Optional<T>} field resolves to {@code Optional.empty()}.
     */
    public double getObjectOptionalEmptyProbability() {
        return objectOptionalEmptyProbability;
    }

    /**
     * Field names that should be unique within one object-generator sequence.
     */
    public Set<String> getObjectUniqueFieldNames() {
        return objectUniqueFieldNames;
    }

    /**
     * Maximum attempts used when generating a unique object field value.
     */
    public int getObjectUniquenessMaxAttempts() {
        return objectUniquenessMaxAttempts;
    }

    /**
     * Locale for locale-aware generators (names, addresses, etc.). Default: {@link Locale#US}.
     */
    public Locale getLocale() {
        return locale;
    }

    /**
     * Optional random-factory override configured by callers.
     */
    public Optional<Supplier<Random>> getRandomFactory() {
        return Optional.ofNullable(randomFactory);
    }

    /**
     * Creates a random instance honoring custom factory and configured seed.
     *
     * <p>Precedence:
     * <ol>
     *   <li>If a random factory is configured, it is used.</li>
     *   <li>Otherwise, if a seed is configured, {@link Random} is used.</li>
     *   <li>Otherwise, {@link SecureRandom} is used.</li>
     * </ol>
     */
    public Random createRandom() {
        if (randomFactory != null) {
            Random random = Objects.requireNonNull(randomFactory.get(), "randomFactory returned null");
            seed.ifPresent(random::setSeed);
            return random;
        }
        return seed.isPresent() ? new Random(seed.getAsLong()) : new SecureRandom();
    }

    /**
     * Scoped registry context used by locale-aware generators.
     */
    public DataRegistryContext getRegistryContext() {
        return registryContext;
    }

    /**
     * Derives a stable 64-bit seed from a non-blank string using FNV-1a.
     *
     * <p>The algorithm contract is versioned by {@link #STRING_SEED_DERIVATION}
     * and must remain stable for deterministic replay.
     *
     * @param seedText textual seed
     * @return deterministic 64-bit seed
     */
    public static long deriveSeed(String seedText) {
        Objects.requireNonNull(seedText, "seedText");
        if (seedText.isBlank()) {
            throw new IllegalArgumentException("seedText must not be blank");
        }
        long hash = FNV1A_64_OFFSET_BASIS;
        byte[] bytes = seedText.getBytes(StandardCharsets.UTF_8);
        for (byte b : bytes) {
            hash ^= (b & 0xffL);
            hash *= FNV1A_64_PRIME;
        }
        return hash;
    }

    private static OptionalLong effectiveSeed(OptionalLong numericSeed, Optional<String> stringSeed) {
        if (numericSeed.isPresent()) {
            return numericSeed;
        }
        if (stringSeed.isPresent()) {
            return OptionalLong.of(deriveSeed(stringSeed.get()));
        }
        return OptionalLong.empty();
    }

    // ── Builder ───────────────────────────────────────────────────────────────


    public static final class Builder {

        private OptionalLong      numericSeed       = OptionalLong.empty();
        private Optional<String>  stringSeed        = Optional.empty();
        private Charset           charset           = StandardCharsets.US_ASCII;
        private int               minStringLength   = 5;
        private int               maxStringLength   = 20;
        private int               minCollectionSize = 1;
        private int               maxCollectionSize = 10;
        private int               objectMaxDepth    = DEFAULT_OBJECT_MAX_DEPTH;
        private int               objectPoolSize    = DEFAULT_OBJECT_POOL_SIZE;
        private boolean           objectOverrideDefaultInitialization;
        private boolean           objectIgnoreErrors;
        private LocalDate         objectDateMin;
        private LocalDate         objectDateMax;
        private ObjectGenerationSemanticMode objectSemanticMode = ObjectGenerationSemanticMode.RELAXED;
        private double            objectNullProbability;
        private double            objectOptionalEmptyProbability;
        private Set<String>       objectUniqueFieldNames = new LinkedHashSet<>(
            Set.of("email", "emailaddress", "username", "userhandle", "uuid", "guid"));
        private int               objectUniquenessMaxAttempts = DEFAULT_OBJECT_UNIQUENESS_ATTEMPTS;
        private Locale            locale            = Locale.US;
        private Supplier<Random>  randomFactory;
        private DataRegistryContext registryContext = DataRegistryContext.globalDefault();

        private Builder() {
        }

        private Builder(GeneratorConfig source) {
            this.numericSeed = source.seed;
            this.stringSeed = source.stringSeed;
            this.charset = source.charset;
            this.minStringLength = source.minStringLength;
            this.maxStringLength = source.maxStringLength;
            this.minCollectionSize = source.minCollectionSize;
            this.maxCollectionSize = source.maxCollectionSize;
            this.objectMaxDepth = source.objectMaxDepth;
            this.objectPoolSize = source.objectPoolSize;
            this.objectOverrideDefaultInitialization = source.objectOverrideDefaultInitialization;
            this.objectIgnoreErrors = source.objectIgnoreErrors;
            this.objectDateMin = source.objectDateMin;
            this.objectDateMax = source.objectDateMax;
            this.objectSemanticMode = source.objectSemanticMode;
            this.objectNullProbability = source.objectNullProbability;
            this.objectOptionalEmptyProbability = source.objectOptionalEmptyProbability;
            this.objectUniqueFieldNames = new LinkedHashSet<>(source.objectUniqueFieldNames);
            this.objectUniquenessMaxAttempts = source.objectUniquenessMaxAttempts;
            this.locale = source.locale;
            this.randomFactory = source.randomFactory;
            this.registryContext = source.registryContext;
        }

        /**
         * Fix the PRNG seed for reproducible output.
         */
        public Builder seed(long seed) {
            this.numericSeed = OptionalLong.of(seed);
            this.stringSeed = Optional.empty();
            return this;
        }

        /**
         * Derive a deterministic numeric seed from a non-blank string.
         */
        public Builder seed(String seedText) {
            Objects.requireNonNull(seedText, "seedText");
            if (seedText.isBlank()) {
                throw new IllegalArgumentException("seedText must not be blank");
            }
            this.stringSeed = Optional.of(seedText);
            this.numericSeed = OptionalLong.empty();
            return this;
        }

        /**
         * Inject custom random strategy for advanced use cases and tests.
         */
        public Builder randomFactory(Supplier<? extends Random> randomFactory) {
            Objects.requireNonNull(randomFactory, "randomFactory");
            this.randomFactory = () -> randomFactory.get();
            return this;
        }

        /**
         * Character set used by string / char generators.
         */
        public Builder charset(Charset charset) {
            this.charset = Objects.requireNonNull(charset, "charset");
            return this;
        }

        /**
         * Length range (inclusive on both ends) for generated strings.
         */
        public Builder stringLength(int min, int max) {
            if (min < 1) throw new IllegalArgumentException("min string length must be >= 1");
            if (max < min) throw new IllegalArgumentException("max string length must be >= min");
            this.minStringLength = min;
            this.maxStringLength = max;
            return this;
        }

        /**
         * Size range (inclusive on both ends) for generated collections / arrays.
         */
        public Builder collectionSize(int min, int max) {
            if (min < 0) throw new IllegalArgumentException("min collection size must be >= 0");
            if (max < min) throw new IllegalArgumentException("max collection size must be >= min");
            this.minCollectionSize = min;
            this.maxCollectionSize = max;
            return this;
        }

        /**
         * Maximum nesting depth for object generation.
         */
        public Builder objectMaxDepth(int objectMaxDepth) {
            if (objectMaxDepth < 1) {
                throw new IllegalArgumentException("objectMaxDepth must be >= 1");
            }
            this.objectMaxDepth = objectMaxDepth;
            return this;
        }

        /**
         * Maximum number of completed instances retained per type for cycle handling.
         */
        public Builder objectPoolSize(int objectPoolSize) {
            if (objectPoolSize < 0) {
                throw new IllegalArgumentException("objectPoolSize must be >= 0");
            }
            this.objectPoolSize = objectPoolSize;
            return this;
        }

        /**
         * Controls whether non-default initialized fields are overwritten during object generation.
         */
        public Builder objectOverrideDefaultInitialization(boolean objectOverrideDefaultInitialization) {
            this.objectOverrideDefaultInitialization = objectOverrideDefaultInitialization;
            return this;
        }

        /**
         * Controls whether object-generation population errors are swallowed.
         */
        public Builder objectIgnoreErrors(boolean objectIgnoreErrors) {
            this.objectIgnoreErrors = objectIgnoreErrors;
            return this;
        }

        /**
         * Global date range for object-generated temporal fields.
         */
        public Builder objectDateRange(LocalDate min, LocalDate max) {
            Objects.requireNonNull(min, "min must not be null");
            Objects.requireNonNull(max, "max must not be null");
            if (min.isAfter(max)) {
                throw new IllegalArgumentException("objectDateMin must be <= objectDateMax, got " + min + " > " + max);
            }
            this.objectDateMin = min;
            this.objectDateMax = max;
            return this;
        }

        /**
         * Controls how strongly semantic field-name matching influences object generation.
         */
        public Builder objectSemanticMode(ObjectGenerationSemanticMode objectSemanticMode) {
            this.objectSemanticMode = Objects.requireNonNull(objectSemanticMode, "objectSemanticMode");
            return this;
        }

        /**
         * Sets the probability that nullable reference fields are generated as {@code null}.
         */
        public Builder objectNullProbability(double objectNullProbability) {
            this.objectNullProbability = requireProbability("objectNullProbability", objectNullProbability);
            return this;
        }

        /**
         * Sets the probability that {@code Optional<T>} fields are generated as {@code Optional.empty()}.
         */
        public Builder objectOptionalEmptyProbability(double objectOptionalEmptyProbability) {
            this.objectOptionalEmptyProbability =
                requireProbability("objectOptionalEmptyProbability", objectOptionalEmptyProbability);
            return this;
        }

        /**
         * Replaces the set of field names that should be unique within one object-generator sequence.
         */
        public Builder objectUniqueFields(String... fieldNames) {
            Objects.requireNonNull(fieldNames, "fieldNames");
            LinkedHashSet<String> normalized = new LinkedHashSet<>();
            for (String fieldName : fieldNames) {
                normalized.add(normalizeObjectFieldName(fieldName));
            }
            this.objectUniqueFieldNames = normalized;
            return this;
        }

        /**
         * Adds one field name to the object-level uniqueness set.
         */
        public Builder objectUniqueField(String fieldName) {
            this.objectUniqueFieldNames.add(normalizeObjectFieldName(fieldName));
            return this;
        }

        /**
         * Maximum attempts used when generating a unique object field value.
         */
        public Builder objectUniquenessMaxAttempts(int objectUniquenessMaxAttempts) {
            if (objectUniquenessMaxAttempts < 1) {
                throw new IllegalArgumentException("objectUniquenessMaxAttempts must be >= 1");
            }
            this.objectUniquenessMaxAttempts = objectUniquenessMaxAttempts;
            return this;
        }

        /**
         * Locale for locale-aware generators (names, addresses, phone numbers, etc.).
         */
        public Builder locale(Locale locale) {
            this.locale = Objects.requireNonNull(locale, "locale");
            return this;
        }

        /**
         * Registry context used by locale-aware generators.
         */
        public Builder registryContext(DataRegistryContext registryContext) {
            this.registryContext = Objects.requireNonNull(registryContext, "registryContext");
            return this;
        }

        private static double requireProbability(String name, double value) {
            if (Double.isNaN(value) || value < 0.0 || value > 1.0) {
                throw new IllegalArgumentException(name + " must be between 0.0 and 1.0");
            }
            return value;
        }

        private static String normalizeObjectFieldName(String fieldName) {
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

        public GeneratorConfig build() {
            return new GeneratorConfig(this);
        }
    }
}
