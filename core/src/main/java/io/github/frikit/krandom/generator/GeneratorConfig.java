/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator;

import io.github.frikit.krandom.generator.object.ObjectGenerationSemanticMode;
import io.github.frikit.krandom.generator.object.SemanticFieldRegistry;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.charset.Charset;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.Random;
import java.util.Set;
import java.util.function.Predicate;
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
    private final SemanticFieldRegistry objectSemanticRegistry;
    private final double       objectNullProbability;
    private final double       objectOptionalEmptyProbability;
    private final Set<String>  objectUniqueFieldNames;
    private final int          objectUniquenessMaxAttempts;
    private final Map<Class<?>, Generator<?>>           objectTypeOverrides;
    private final Map<String, Generator<?>>             objectFieldOverrides;
    private final Map<Class<?>, ContextualGenerator<?>> objectContextualTypeOverrides;
    private final Map<String, ContextualGenerator<?>>   objectContextualFieldOverrides;
    private final List<FieldGeneratorOverride>          objectPredicateFieldOverrides;
    private final List<ContextualFieldGeneratorOverride> objectContextualPredicateFieldOverrides;
    private final List<Predicate<Field>>                objectExclusionPredicates;
    private final List<Predicate<Class<?>>>             objectTypeExclusionPredicates;
    private final Map<Class<?>, Class<?>>               objectSubtypes;
    private final Locale       locale;
    private final Random       random;
    private final Supplier<Random> randomFactory;
    private final DataRegistryContext registryContext;
    private final Clock        clock;
    private final boolean      secureRandom;

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
        this.objectSemanticRegistry = b.objectSemanticRegistry;
        this.objectNullProbability = b.objectNullProbability;
        this.objectOptionalEmptyProbability = b.objectOptionalEmptyProbability;
        this.objectUniqueFieldNames = Collections.unmodifiableSet(new LinkedHashSet<>(b.objectUniqueFieldNames));
        this.objectUniquenessMaxAttempts = b.objectUniquenessMaxAttempts;
        this.objectTypeOverrides = Collections.unmodifiableMap(new HashMap<>(b.objectTypeOverrides));
        this.objectFieldOverrides = Collections.unmodifiableMap(new HashMap<>(b.objectFieldOverrides));
        this.objectContextualTypeOverrides = Collections.unmodifiableMap(new HashMap<>(b.objectContextualTypeOverrides));
        this.objectContextualFieldOverrides = Collections.unmodifiableMap(new HashMap<>(b.objectContextualFieldOverrides));
        this.objectPredicateFieldOverrides = Collections.unmodifiableList(new ArrayList<>(b.objectPredicateFieldOverrides));
        this.objectContextualPredicateFieldOverrides =
            Collections.unmodifiableList(new ArrayList<>(b.objectContextualPredicateFieldOverrides));
        this.objectExclusionPredicates = Collections.unmodifiableList(new ArrayList<>(b.objectExclusionPredicates));
        this.objectTypeExclusionPredicates = Collections.unmodifiableList(new ArrayList<>(b.objectTypeExclusionPredicates));
        this.objectSubtypes = Collections.unmodifiableMap(new HashMap<>(b.objectSubtypes));
        this.locale = b.locale;
        this.random = b.random;
        this.randomFactory = b.randomFactory;
        this.registryContext = b.registryContext;
        this.clock = b.clock;
        this.secureRandom = b.secureRandom;
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
     * Seed for deterministic generation; empty means unseeded generation.
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
     * Mapping of abstract/interface field types to the concrete implementations that
     * object generation instantiates for them.
     */
    public Map<Class<?>, Class<?>> getObjectSubtypes() {
        return objectSubtypes;
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
     * Registry used to resolve semantic object field names.
     */
    public SemanticFieldRegistry getObjectSemanticRegistry() {
        return objectSemanticRegistry;
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
     * Return the root-configured type-level override for object generation, if any.
     */
    public Optional<Generator<?>> getObjectTypeOverride(Class<?> type) {
        return Optional.ofNullable(objectTypeOverrides.get(type));
    }

    /**
     * Return the root-configured field-level override for object generation, if any.
     *
     * <p>Primary lookup key is {@code "fully.qualified.ClassName.fieldName"}.
     * Legacy simple-name keys are still supported for backward compatibility.
     */
    public Optional<Generator<?>> getObjectFieldOverride(Class<?> ownerType, String fieldName) {
        String key = objectFieldKey(ownerType, fieldName);
        Generator<?> direct = objectFieldOverrides.get(key);
        if (direct != null) {
            return Optional.of(direct);
        }
        return Optional.ofNullable(objectFieldOverrides.get(objectLegacyFieldKey(ownerType, fieldName)));
    }

    /**
     * Return the root-configured contextual type-level override for object generation, if any.
     */
    public Optional<ContextualGenerator<?>> getObjectContextualTypeOverride(Class<?> type) {
        return Optional.ofNullable(objectContextualTypeOverrides.get(type));
    }

    /**
     * Return the root-configured contextual field-level override for object generation, if any.
     */
    public Optional<ContextualGenerator<?>> getObjectContextualFieldOverride(Class<?> ownerType, String fieldName) {
        String key = objectFieldKey(ownerType, fieldName);
        ContextualGenerator<?> direct = objectContextualFieldOverrides.get(key);
        if (direct != null) {
            return Optional.of(direct);
        }
        return Optional.ofNullable(objectContextualFieldOverrides.get(objectLegacyFieldKey(ownerType, fieldName)));
    }

    /**
     * Return the first root-configured predicate field override matching {@code field}, if any.
     */
    public Optional<Generator<?>> getObjectFieldPredicateOverride(Field field) {
        Objects.requireNonNull(field, "field must not be null");
        for (FieldGeneratorOverride override : objectPredicateFieldOverrides) {
            if (override.predicate().test(field)) {
                return Optional.of(override.generator());
            }
        }
        return Optional.empty();
    }

    /**
     * Return the first root-configured contextual predicate field override matching {@code field}, if any.
     */
    public Optional<ContextualGenerator<?>> getObjectContextualFieldPredicateOverride(Field field) {
        Objects.requireNonNull(field, "field must not be null");
        for (ContextualFieldGeneratorOverride override : objectContextualPredicateFieldOverrides) {
            if (override.predicate().test(field)) {
                return Optional.of(override.generator());
            }
        }
        return Optional.empty();
    }

    /**
     * Returns {@code true} if the root config excludes the given field from object generation.
     */
    public boolean shouldObjectExclude(Field field) {
        Objects.requireNonNull(field, "field must not be null");
        for (Predicate<Field> predicate : objectExclusionPredicates) {
            if (predicate.test(field)) {
                return true;
            }
        }
        for (Predicate<Class<?>> predicate : objectTypeExclusionPredicates) {
            if (predicate.test(field.getType())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Locale for locale-aware generators (names, addresses, etc.). Default: {@link Locale#US}.
     */
    public Locale getLocale() {
        return locale;
    }

    /**
     * Clock used by relative temporal generators. Default: {@link Clock#systemDefaultZone()}.
     */
    public Clock getClock() {
        return clock;
    }

    /**
     * Optional caller-owned random instance configured by callers.
     */
    public Optional<Random> getRandom() {
        return Optional.ofNullable(random);
    }

    /**
     * Optional random-factory override configured by callers.
     */
    public Optional<Supplier<Random>> getRandomFactory() {
        return Optional.ofNullable(randomFactory);
    }

    /**
     * Returns {@code true} when unseeded generation should use {@link SecureRandom}.
     */
    public boolean isSecureRandom() {
        return secureRandom;
    }

    /**
     * Creates a random instance honoring custom factory and configured seed.
     *
     * <p>Precedence:
     * <ol>
     *   <li>If a caller-owned random instance is configured, it is used.</li>
     *   <li>Otherwise, if a random factory is configured, it is used.</li>
     *   <li>Otherwise, if a seed is configured, {@link Random} is used.</li>
     *   <li>Otherwise, if secure random is explicitly enabled, {@link SecureRandom} is used.</li>
     *   <li>Otherwise, {@link Random} is used.</li>
     * </ol>
     */
    public Random createRandom() {
        if (random != null) {
            return random;
        }
        if (randomFactory != null) {
            Random random = Objects.requireNonNull(randomFactory.get(), "randomFactory returned null");
            seed.ifPresent(random::setSeed);
            return random;
        }
        if (seed.isPresent()) {
            return new Random(seed.getAsLong());
        }
        return secureRandom ? new SecureRandom() : new Random();
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

    private static String objectFieldKey(Class<?> ownerType, String fieldName) {
        return ownerType.getName() + "." + fieldName;
    }

    private static String objectLegacyFieldKey(Class<?> ownerType, String fieldName) {
        return ownerType.getSimpleName() + "." + fieldName;
    }

    private record FieldGeneratorOverride(Predicate<Field> predicate, Generator<?> generator) {
    }

    private record ContextualFieldGeneratorOverride(Predicate<Field> predicate, ContextualGenerator<?> generator) {
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
        private SemanticFieldRegistry objectSemanticRegistry = SemanticFieldRegistry.defaults();
        private double            objectNullProbability;
        private double            objectOptionalEmptyProbability;
        private Set<String>       objectUniqueFieldNames = new LinkedHashSet<>(
            Set.of("email", "emailaddress", "username", "userhandle", "uuid", "guid", "id"));
        private int               objectUniquenessMaxAttempts = DEFAULT_OBJECT_UNIQUENESS_ATTEMPTS;
        private final Map<Class<?>, Generator<?>>           objectTypeOverrides           = new HashMap<>();
        private final Map<String, Generator<?>>             objectFieldOverrides          = new HashMap<>();
        private final Map<Class<?>, ContextualGenerator<?>> objectContextualTypeOverrides = new HashMap<>();
        private final Map<String, ContextualGenerator<?>>   objectContextualFieldOverrides = new HashMap<>();
        private final List<FieldGeneratorOverride>          objectPredicateFieldOverrides = new ArrayList<>();
        private final List<ContextualFieldGeneratorOverride> objectContextualPredicateFieldOverrides =
            new ArrayList<>();
        private final List<Predicate<Field>>                objectExclusionPredicates     = new ArrayList<>();
        private final List<Predicate<Class<?>>>             objectTypeExclusionPredicates = new ArrayList<>();
        private final Map<Class<?>, Class<?>>               objectSubtypes                = new HashMap<>();
        private Locale            locale            = Locale.US;
        private Random            random;
        private Supplier<Random>  randomFactory;
        private DataRegistryContext registryContext = DataRegistryContext.globalDefault();
        private Clock             clock             = Clock.systemDefaultZone();
        private boolean           secureRandom;

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
            this.objectSemanticRegistry = source.objectSemanticRegistry;
            this.objectNullProbability = source.objectNullProbability;
            this.objectOptionalEmptyProbability = source.objectOptionalEmptyProbability;
            this.objectUniqueFieldNames = new LinkedHashSet<>(source.objectUniqueFieldNames);
            this.objectUniquenessMaxAttempts = source.objectUniquenessMaxAttempts;
            this.objectTypeOverrides.putAll(source.objectTypeOverrides);
            this.objectFieldOverrides.putAll(source.objectFieldOverrides);
            this.objectContextualTypeOverrides.putAll(source.objectContextualTypeOverrides);
            this.objectContextualFieldOverrides.putAll(source.objectContextualFieldOverrides);
            this.objectPredicateFieldOverrides.addAll(source.objectPredicateFieldOverrides);
            this.objectContextualPredicateFieldOverrides.addAll(source.objectContextualPredicateFieldOverrides);
            this.objectExclusionPredicates.addAll(source.objectExclusionPredicates);
            this.objectTypeExclusionPredicates.addAll(source.objectTypeExclusionPredicates);
            this.objectSubtypes.putAll(source.objectSubtypes);
            this.locale = source.locale;
            this.random = source.random;
            this.randomFactory = source.randomFactory;
            this.registryContext = source.registryContext;
            this.clock = source.clock;
            this.secureRandom = source.secureRandom;
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
         * Use a caller-owned random instance for generated values.
         */
        public Builder random(Random random) {
            this.random = Objects.requireNonNull(random, "random");
            this.randomFactory = null;
            this.secureRandom = false;
            return this;
        }

        /**
         * Inject custom random factory for advanced use cases and tests.
         */
        public Builder randomFactory(Supplier<? extends Random> randomFactory) {
            Objects.requireNonNull(randomFactory, "randomFactory");
            this.random = null;
            this.secureRandom = false;
            this.randomFactory = () -> randomFactory.get();
            return this;
        }

        /**
         * Opt in to {@link SecureRandom} for unseeded generation.
         */
        public Builder secureRandom() {
            return secureRandom(true);
        }

        /**
         * Controls whether unseeded generation uses {@link SecureRandom}.
         */
        public Builder secureRandom(boolean secureRandom) {
            this.secureRandom = secureRandom;
            if (secureRandom) {
                this.random = null;
                this.randomFactory = null;
            }
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
         *
         * <p>When {@code true}, affected fields are left at {@code null} / their primitive
         * default and each ignored failure is logged at {@link java.util.logging.Level#FINE}
         * so it remains diagnosable. Default: {@code false} (errors propagate as
         * {@code ObjectGenerationException}).
         */
        public Builder objectIgnoreErrors(boolean objectIgnoreErrors) {
            this.objectIgnoreErrors = objectIgnoreErrors;
            return this;
        }

        /**
         * Maps an abstract or interface field type to the concrete implementation that
         * object generation instantiates for it.
         *
         * <p>kRandom does not scan the classpath for implementations; this explicit
         * mapping is the supported way to populate abstract and interface fields.
         *
         * @param declaredType abstract class or interface declared by fields
         * @param implementationType concrete subtype to instantiate for those fields
         */
        public Builder objectSubtype(Class<?> declaredType, Class<?> implementationType) {
            Objects.requireNonNull(declaredType, "declaredType must not be null");
            Objects.requireNonNull(implementationType, "implementationType must not be null");
            if (!declaredType.isAssignableFrom(implementationType)) {
                throw new IllegalArgumentException("implementationType " + implementationType.getName()
                                                   + " is not assignable to " + declaredType.getName());
            }
            if (implementationType.isInterface() || Modifier.isAbstract(implementationType.getModifiers())) {
                throw new IllegalArgumentException("implementationType must be a concrete class, got: "
                                                   + implementationType.getName());
            }
            objectSubtypes.put(declaredType, implementationType);
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
         * Sets the registry used to resolve semantic object field names.
         */
        public Builder objectSemanticRegistry(SemanticFieldRegistry objectSemanticRegistry) {
            this.objectSemanticRegistry = Objects.requireNonNull(objectSemanticRegistry, "objectSemanticRegistry");
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
         * Register a type-level override for object generation.
         */
        public <T> Builder objectOverride(Class<T> type, Generator<? extends T> generator) {
            Objects.requireNonNull(type, "type must not be null");
            Objects.requireNonNull(generator, "generator must not be null");
            objectTypeOverrides.put(type, generator);
            return this;
        }

        /**
         * Register a field-level override for object generation.
         */
        public <T extends @Nullable Object> Builder objectOverride(Class<?> ownerType, String fieldName, Generator<T> generator) {
            Objects.requireNonNull(ownerType, "ownerType must not be null");
            Objects.requireNonNull(fieldName, "fieldName must not be null");
            Objects.requireNonNull(generator, "generator must not be null");
            objectFieldOverrides.put(objectFieldKey(ownerType, fieldName), generator);
            return this;
        }

        /**
         * Register a contextual type-level override for object generation.
         */
        public <T> Builder objectOverride(Class<T> type, ContextualGenerator<? extends T> generator) {
            Objects.requireNonNull(type, "type must not be null");
            Objects.requireNonNull(generator, "generator must not be null");
            objectContextualTypeOverrides.put(type, generator);
            return this;
        }

        /**
         * Register a contextual field-level override for object generation.
         */
        public <T> Builder objectOverride(Class<?> ownerType, String fieldName, ContextualGenerator<T> generator) {
            Objects.requireNonNull(ownerType, "ownerType must not be null");
            Objects.requireNonNull(fieldName, "fieldName must not be null");
            Objects.requireNonNull(generator, "generator must not be null");
            objectContextualFieldOverrides.put(objectFieldKey(ownerType, fieldName), generator);
            return this;
        }

        /**
         * Register a predicate field override for object generation.
         */
        public <T extends @Nullable Object> Builder objectOverride(Predicate<Field> predicate, Generator<T> generator) {
            Objects.requireNonNull(predicate, "predicate must not be null");
            Objects.requireNonNull(generator, "generator must not be null");
            objectPredicateFieldOverrides.add(new FieldGeneratorOverride(predicate, generator));
            return this;
        }

        /**
         * Register a context-aware predicate field override for object generation.
         */
        public <T> Builder objectOverride(Predicate<Field> predicate, ContextualGenerator<T> generator) {
            Objects.requireNonNull(predicate, "predicate must not be null");
            Objects.requireNonNull(generator, "generator must not be null");
            objectContextualPredicateFieldOverrides.add(new ContextualFieldGeneratorOverride(predicate, generator));
            return this;
        }

        /**
         * Add a field-exclusion predicate for object generation.
         */
        public Builder objectExclude(Predicate<Field> predicate) {
            Objects.requireNonNull(predicate, "predicate must not be null");
            objectExclusionPredicates.add(predicate);
            return this;
        }

        /**
         * Exclude all object fields whose name equals {@code name}.
         */
        public Builder objectExcludeField(String name) {
            Objects.requireNonNull(name, "name must not be null");
            return objectExclude(field -> field.getName().equals(name));
        }

        /**
         * Exclude all object fields whose declared type is exactly {@code type}.
         */
        public Builder objectExcludeType(Class<?> type) {
            Objects.requireNonNull(type, "type must not be null");
            objectTypeExclusionPredicates.add(candidate -> candidate == type);
            return this;
        }

        /**
         * Exclude all object fields whose declared type matches {@code predicate}.
         */
        public Builder objectExcludeType(Predicate<Class<?>> predicate) {
            Objects.requireNonNull(predicate, "predicate must not be null");
            objectTypeExclusionPredicates.add(predicate);
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
         * Clock used by relative temporal generators and semantic object generation.
         */
        public Builder clock(Clock clock) {
            this.clock = Objects.requireNonNull(clock, "clock");
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
