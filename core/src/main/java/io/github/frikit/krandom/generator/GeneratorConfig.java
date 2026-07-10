/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator;

import io.github.frikit.krandom.generator.failure.GenerationFailureListener;
import io.github.frikit.krandom.generator.finance.BankingSafetyPolicy;
import io.github.frikit.krandom.generator.finance.CryptoAddressSafetyPolicy;
import io.github.frikit.krandom.generator.finance.PaymentCardSafetyPolicy;
import io.github.frikit.krandom.generator.location.PhoneNumberSafetyPolicy;
import io.github.frikit.krandom.generator.user.IdentityDocumentSafetyPolicy;
import io.github.frikit.krandom.generator.user.nationalid.NationalIdSafetyPolicy;
import io.github.frikit.krandom.generator.object.ObjectGenerationSemanticMode;
import io.github.frikit.krandom.generator.object.ObjectConstructionPolicy;
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
 * Configuration whose value fields are immutable after construction.
 *
 * <p>A caller-owned {@link Random} and a caller-supplied random factory are retained by
 * reference. Their state, lifecycle, and thread-safety remain the caller's responsibility.
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
    private static final GenerationFailureListener NO_FAILURE_LISTENER = diagnostic -> {};

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
    private final ObjectConstructionPolicy objectConstructionPolicy;
    private final boolean      objectIgnoreErrors;
    private final GenerationFailureListener generationFailureListener;
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
    private final String       generationProfile;
    private final String       safetyPolicy;
    private final PaymentCardSafetyPolicy paymentCardSafetyPolicy;
    private final BankingSafetyPolicy bankingSafetyPolicy;
    private final BusinessTaxIdentifierSafetyPolicy businessTaxIdentifierSafetyPolicy;
    private final CryptoAddressSafetyPolicy cryptoAddressSafetyPolicy;
    private final PhoneNumberSafetyPolicy phoneNumberSafetyPolicy;
    private final NationalIdSafetyPolicy nationalIdSafetyPolicy;
    private final IdentityDocumentSafetyPolicy identityDocumentSafetyPolicy;
    private final String       providerDatasetVersion;

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
        this.objectConstructionPolicy = b.objectConstructionPolicy;
        this.objectIgnoreErrors = b.objectIgnoreErrors;
        this.generationFailureListener = b.generationFailureListener;
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
        this.generationProfile = b.generationProfile;
        this.safetyPolicy = b.safetyPolicy;
        this.paymentCardSafetyPolicy = b.paymentCardSafetyPolicy;
        this.bankingSafetyPolicy = b.bankingSafetyPolicy;
        this.businessTaxIdentifierSafetyPolicy = b.businessTaxIdentifierSafetyPolicy;
        this.cryptoAddressSafetyPolicy = b.cryptoAddressSafetyPolicy;
        this.phoneNumberSafetyPolicy = b.phoneNumberSafetyPolicy;
        this.nationalIdSafetyPolicy = b.nationalIdSafetyPolicy;
        this.identityDocumentSafetyPolicy = b.identityDocumentSafetyPolicy;
        this.providerDatasetVersion = b.providerDatasetVersion;
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

    /**
     * Label of the profile that supplied this configuration, or {@code "custom"}.
     */
    public String getGenerationProfile() {
        return generationProfile;
    }

    /**
     * Requested output-safety policy label recorded in portable recipes.
     *
     * <p>The initial value is {@value GenerationRecipe#LEGACY_UNCLASSIFIED_SAFETY_POLICY}; the
     * dedicated safety contract defines stricter policies without changing recipe shape.
     */
    public String getSafetyPolicy() {
        return safetyPolicy;
    }

    /**
     * Enforceable policy for generated payment-card numbers.
     *
     * <p>The default produces issuer-shaped numbers that deliberately fail Luhn. Select
     * {@link PaymentCardSafetyPolicy#CHECKSUM_VALID} only for isolated validator fixtures; it is
     * not a processor sandbox credential or authorization to contact external systems.
     */
    public PaymentCardSafetyPolicy getPaymentCardSafetyPolicy() {
        return paymentCardSafetyPolicy;
    }

    /**
     * Enforceable policy for generated banking identifiers and account values.
     *
     * <p>The default fails closed because krandom has no portable non-routable banking-fixture
     * contract. {@link BankingSafetyPolicy#REALISTIC_UNCLASSIFIED} is an explicit compatibility
     * opt-in for isolated tests; it does not make values safe for production or external systems.
     */
    public BankingSafetyPolicy getBankingSafetyPolicy() {
        return bankingSafetyPolicy;
    }

    /**
     * Enforceable policy for generated corporate tax identifiers.
     *
     * <p>The default fails closed because krandom has no portable non-routable CNPJ or EIN
     * fixture contract. {@link BusinessTaxIdentifierSafetyPolicy#REALISTIC_UNCLASSIFIED} is an
     * explicit compatibility opt-in for isolated tests; it does not make values safe for
     * production or external systems.
     */
    public BusinessTaxIdentifierSafetyPolicy getBusinessTaxIdentifierSafetyPolicy() {
        return businessTaxIdentifierSafetyPolicy;
    }

    /**
     * Enforceable policy for generated cryptocurrency destination-address shapes.
     *
     * <p>The default fails closed because a plausible address is not proof of a test network,
     * unspendability, or non-routability. {@link CryptoAddressSafetyPolicy#REALISTIC_UNCLASSIFIED}
     * is an explicit compatibility opt-in for isolated tests; it does not make values safe for a
     * wallet, exchange, or other external system.
     */
    public CryptoAddressSafetyPolicy getCryptoAddressSafetyPolicy() {
        return cryptoAddressSafetyPolicy;
    }

    /**
     * Enforceable policy for locale-style phone-number output.
     *
     * <p>The default uses NANPA's fictional 555-0100 through 555-0199 range for US locales.
     * Other locales and custom phone-number templates remain unclassified rather than
     * being presented as non-routable.
     */
    public PhoneNumberSafetyPolicy getPhoneNumberSafetyPolicy() {
        return phoneNumberSafetyPolicy;
    }

    /**
     * Enforceable policy for generated national identity numbers.
     *
     * <p>The default fails closed because the library has no cross-country non-routable fixture
     * contract. {@link NationalIdSafetyPolicy#REALISTIC_UNCLASSIFIED} is an explicit compatibility
     * opt-in for isolated tests; it does not make values safe for production or external systems.
     */
    public NationalIdSafetyPolicy getNationalIdSafetyPolicy() {
        return nationalIdSafetyPolicy;
    }

    /**
     * Enforceable policy for generated passport and driving-license identifiers.
     *
     * <p>The default fails closed because generic document-number shapes do not establish a
     * portable non-routable fixture. {@link IdentityDocumentSafetyPolicy#REALISTIC_UNCLASSIFIED}
     * is an explicit compatibility opt-in for isolated tests; it does not make values safe for
     * production or external systems.
     */
    public IdentityDocumentSafetyPolicy getIdentityDocumentSafetyPolicy() {
        return identityDocumentSafetyPolicy;
    }

    /**
     * Version label for the provider datasets used by this configuration.
     */
    public String getProviderDatasetVersion() {
        return providerDatasetVersion;
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
     * Construction policy used for non-record object generation.
     */
    public ObjectConstructionPolicy getObjectConstructionPolicy() {
        return objectConstructionPolicy;
    }

    /**
     * Controls whether object-generation population errors are swallowed.
     */
    public boolean isObjectIgnoreErrors() {
        return objectIgnoreErrors;
    }

    /**
     * Listener for strict and lenient value-sanitized object-generation failure diagnostics.
     */
    public GenerationFailureListener getGenerationFailureListener() {
        return generationFailureListener;
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
     * Optional caller-owned random instance.
     *
     * <p>The instance is retained by reference and is never copied or implicitly reseeded.
     */
    public Optional<Random> getRandom() {
        return Optional.ofNullable(random);
    }

    /**
     * Optional caller-owned random factory.
     *
     * <p>The factory is invoked once for each call to {@link #createRandom()} and must
     * return a non-null source. The factory and returned sources are never implicitly reseeded.
     */
    public Optional<Supplier<Random>> getRandomFactory() {
        return Optional.ofNullable(randomFactory);
    }

    /**
     * Returns {@code true} when {@link SecureRandom} is the configured random source.
     */
    public boolean isSecureRandom() {
        return secureRandom;
    }

    /**
     * Obtains the configured random source.
     *
     * <p>The builder permits exactly one explicit source specification:
     * <ol>
     *   <li>A caller-owned instance is returned unchanged on every call.</li>
     *   <li>A configured factory is invoked on every call.</li>
     *   <li>A seed creates a fresh {@link Random} at the same initial state on every call.</li>
     *   <li>Secure mode creates a fresh {@link SecureRandom} on every call.</li>
     *   <li>With no explicit source, a fresh unseeded {@link Random} is created.</li>
     * </ol>
     */
    public Random createRandom() {
        if (random != null) {
            return random;
        }
        if (randomFactory != null) {
            return Objects.requireNonNull(randomFactory.get(), "randomFactory returned null");
        }
        if (seed.isPresent()) {
            return new Random(seed.getAsLong());
        }
        return secureRandom ? new SecureRandom() : new Random();
    }

    /**
     * Returns a portable replay recipe when every output-affecting input is serializable.
     *
     * <p>Unseeded, secure, caller-owned, callback-backed, or custom-registry configurations have
     * state that cannot be replayed safely, so they return an empty result rather than a partial
     * recipe that would misrepresent the generated fixture.
     */
    public Optional<GenerationRecipe> getGenerationRecipe() {
        if (!isPortableRecipeConfiguration()) {
            return Optional.empty();
        }
        GenerationRecipe.Builder recipe = GenerationRecipe.builder()
                                                            .libraryVersion(GenerationRecipe.currentLibraryVersion())
                                                            .seed(seed.getAsLong())
                                                            .locale(locale)
                                                            .clock(clock.instant(), clock.getZone())
                                                            .profile(generationProfile)
                                                            .safetyPolicy(safetyPolicy)
                                                            .constructionPolicy(objectConstructionPolicy)
                                                            .providerDatasetVersion(providerDatasetVersion)
                                                            .setting("payment.card-safety-policy",
                                                                     paymentCardSafetyPolicy.name())
                                                            .setting("banking.safety-policy", bankingSafetyPolicy.name())
                                                            .setting("business-tax-identifier.safety-policy",
                                                                     businessTaxIdentifierSafetyPolicy.name())
                                                            .setting("crypto-address.safety-policy",
                                                                     cryptoAddressSafetyPolicy.name())
                                                            .setting("phone-number.safety-policy",
                                                                     phoneNumberSafetyPolicy.name())
                                                            .setting("national-id.safety-policy",
                                                                     nationalIdSafetyPolicy.name())
                                                            .setting("identity-document.safety-policy",
                                                                     identityDocumentSafetyPolicy.name())
                                                            .setting("charset", charset.name())
                                                            .setting("string.min", Integer.toString(minStringLength))
                                                            .setting("string.max", Integer.toString(maxStringLength))
                                                            .setting("collection.min", Integer.toString(minCollectionSize))
                                                            .setting("collection.max", Integer.toString(maxCollectionSize))
                                                            .setting("object.max-depth", Integer.toString(objectMaxDepth))
                                                            .setting("object.pool-size", Integer.toString(objectPoolSize))
                                                            .setting("object.override-default-initialization",
                                                                     Boolean.toString(objectOverrideDefaultInitialization))
                                                            .setting("object.ignore-errors", Boolean.toString(objectIgnoreErrors))
                                                            .setting("object.semantic-mode", objectSemanticMode.name())
                                                            .setting("object.null-probability",
                                                                     Double.toString(objectNullProbability))
                                                            .setting("object.optional-empty-probability",
                                                                     Double.toString(objectOptionalEmptyProbability))
                                                            .setting("object.unique-fields",
                                                                     String.join(",", objectUniqueFieldNames))
                                                            .setting("object.uniqueness-max-attempts",
                                                                     Integer.toString(objectUniquenessMaxAttempts));
        if (stringSeed.isPresent()) {
            recipe.seedText(stringSeed.get());
        }
        if (objectDateMin != null) {
            recipe.setting("object.date-min", objectDateMin.toString())
                  .setting("object.date-max", Objects.requireNonNull(objectDateMax).toString());
        }
        return Optional.of(recipe.build());
    }

    /**
     * Scoped registry context used by locale-aware generators.
     */
    public DataRegistryContext getRegistryContext() {
        return registryContext;
    }

    private boolean isPortableRecipeConfiguration() {
        // Builder validation makes a present seed mutually exclusive with caller-owned and secure
        // sources, so seed ownership is the only random-source condition required here.
        return seed.isPresent()
            && registryContext == DataRegistryContext.globalDefault()
            && objectSemanticRegistry == SemanticFieldRegistry.defaults()
            && hasNoCustomObjectGenerationState();
    }

    private boolean hasNoCustomObjectGenerationState() {
        boolean[] emptyStates = {
            objectTypeOverrides.isEmpty(),
            objectFieldOverrides.isEmpty(),
            objectContextualTypeOverrides.isEmpty(),
            objectContextualFieldOverrides.isEmpty(),
            objectPredicateFieldOverrides.isEmpty(),
            objectContextualPredicateFieldOverrides.isEmpty(),
            objectExclusionPredicates.isEmpty(),
            objectTypeExclusionPredicates.isEmpty(),
            objectSubtypes.isEmpty()
        };
        for (boolean emptyState : emptyStates) {
            if (!emptyState) {
                return false;
            }
        }
        return true;
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
        private ObjectConstructionPolicy objectConstructionPolicy = ObjectConstructionPolicy.SAFE_CONSTRUCTORS;
        private boolean           objectIgnoreErrors;
        private GenerationFailureListener generationFailureListener = NO_FAILURE_LISTENER;
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
        private String            generationProfile = GenerationRecipe.CUSTOM_PROFILE;
        private String            safetyPolicy = GenerationRecipe.LEGACY_UNCLASSIFIED_SAFETY_POLICY;
        private PaymentCardSafetyPolicy paymentCardSafetyPolicy = PaymentCardSafetyPolicy.TEST_SAFE_NON_ROUTABLE;
        private BankingSafetyPolicy bankingSafetyPolicy = BankingSafetyPolicy.DISABLED;
        private BusinessTaxIdentifierSafetyPolicy businessTaxIdentifierSafetyPolicy =
            BusinessTaxIdentifierSafetyPolicy.DISABLED;
        private CryptoAddressSafetyPolicy cryptoAddressSafetyPolicy = CryptoAddressSafetyPolicy.DISABLED;
        private PhoneNumberSafetyPolicy phoneNumberSafetyPolicy = PhoneNumberSafetyPolicy.TEST_SAFE_WHERE_AVAILABLE;
        private NationalIdSafetyPolicy nationalIdSafetyPolicy = NationalIdSafetyPolicy.DISABLED;
        private IdentityDocumentSafetyPolicy identityDocumentSafetyPolicy = IdentityDocumentSafetyPolicy.DISABLED;
        private String            providerDatasetVersion = GenerationRecipe.BUILTIN_PROVIDER_DATASET_VERSION;

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
            this.objectConstructionPolicy = source.objectConstructionPolicy;
            this.objectIgnoreErrors = source.objectIgnoreErrors;
            this.generationFailureListener = source.generationFailureListener;
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
            this.generationProfile = source.generationProfile;
            this.safetyPolicy = source.safetyPolicy;
            this.paymentCardSafetyPolicy = source.paymentCardSafetyPolicy;
            this.bankingSafetyPolicy = source.bankingSafetyPolicy;
            this.businessTaxIdentifierSafetyPolicy = source.businessTaxIdentifierSafetyPolicy;
            this.cryptoAddressSafetyPolicy = source.cryptoAddressSafetyPolicy;
            this.phoneNumberSafetyPolicy = source.phoneNumberSafetyPolicy;
            this.nationalIdSafetyPolicy = source.nationalIdSafetyPolicy;
            this.identityDocumentSafetyPolicy = source.identityDocumentSafetyPolicy;
            this.providerDatasetVersion = source.providerDatasetVersion;
        }

        /**
         * Fix the PRNG seed for reproducible output.
         *
         * <p>A seed cannot be combined with {@link #random(Random)},
         * {@link #randomFactory(Supplier)}, or {@link #secureRandom()}.
         */
        public Builder seed(long seed) {
            this.numericSeed = OptionalLong.of(seed);
            this.stringSeed = Optional.empty();
            return this;
        }

        /**
         * Derive a deterministic numeric seed from a non-blank string.
         *
         * <p>A seed cannot be combined with another random-source specification.
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
         *
         * <p>The instance is retained by reference, returned unchanged, and never reseeded.
         * It cannot be combined with a seed, random factory, or secure mode.
         */
        public Builder random(Random random) {
            this.random = Objects.requireNonNull(random, "random");
            return this;
        }

        /**
         * Inject a caller-owned random factory for advanced use cases and tests.
         *
         * <p>The factory is invoked once per {@link GeneratorConfig#createRandom()} call.
         * Its result is not reseeded. It cannot be combined with another source specification.
         */
        public Builder randomFactory(Supplier<? extends Random> randomFactory) {
            Objects.requireNonNull(randomFactory, "randomFactory");
            this.randomFactory = () -> randomFactory.get();
            return this;
        }

        /**
         * Use fresh {@link SecureRandom} instances for generation.
         *
         * <p>Secure mode cannot be combined with another random-source specification.
         */
        public Builder secureRandom() {
            return secureRandom(true);
        }

        /**
         * Controls whether generation uses fresh {@link SecureRandom} instances.
         */
        public Builder secureRandom(boolean secureRandom) {
            this.secureRandom = secureRandom;
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
         * Selects how non-record classes are constructed. The default invokes constructors safely.
         */
        public Builder objectConstructionPolicy(ObjectConstructionPolicy objectConstructionPolicy) {
            this.objectConstructionPolicy = Objects.requireNonNull(
                objectConstructionPolicy, "objectConstructionPolicy must not be null");
            return this;
        }

        /**
         * Controls whether object-generation population errors are swallowed.
         *
         * <p>When {@code true}, affected fields are left at {@code null} / their primitive
         * default and each ignored failure is logged at SLF4J {@code DEBUG} level
         * so it remains diagnosable. Default: {@code false} (errors propagate as
         * {@code ObjectGenerationException}).
         */
        public Builder objectIgnoreErrors(boolean objectIgnoreErrors) {
            this.objectIgnoreErrors = objectIgnoreErrors;
            return this;
        }

        /**
         * Sets the synchronous listener for value-sanitized object-generation failures.
         *
         * <p>The listener receives structured context, the cause class name, and an optional
         * replay identity. It never receives generated field values or the throwable itself.
         */
        public Builder generationFailureListener(GenerationFailureListener generationFailureListener) {
            this.generationFailureListener = Objects.requireNonNull(
                generationFailureListener, "generationFailureListener must not be null");
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
         * Registers a type-level override for object generation.
         *
         * <p>The override is also the factory for a root object of {@code type}, so it can safely
         * construct interfaces, abstract types, or immutable values without reflective access.
         * Root factory results must be non-null and assignable to {@code type}.
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
         * Registers a contextual type-level override for object generation.
         *
         * <p>For a root object, the context uses field name {@code "$root"}, the requested type as
         * owner, and depth zero. Contextual type overrides take precedence over plain type
         * overrides. Results must be non-null and assignable to {@code type}.
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
         * Labels this configuration with a named profile for replay diagnostics.
         */
        public Builder generationProfile(String generationProfile) {
            this.generationProfile = requireRecipeToken("generationProfile", generationProfile);
            return this;
        }

        /**
         * Labels the requested output-safety policy for replay diagnostics.
         */
        public Builder safetyPolicy(String safetyPolicy) {
            this.safetyPolicy = requireRecipeToken("safetyPolicy", safetyPolicy);
            return this;
        }

        /**
         * Selects the enforceable policy for generated payment-card numbers.
         *
         * <p>The default deliberately fails Luhn while preserving issuer shape and length. Choose
         * {@link PaymentCardSafetyPolicy#CHECKSUM_VALID} only for isolated validator fixtures; it
         * does not make generated data safe to send to a processor or another external system.
         *
         * @param paymentCardSafetyPolicy card-number safety policy
         * @return this builder
         */
        public Builder paymentCardSafetyPolicy(PaymentCardSafetyPolicy paymentCardSafetyPolicy) {
            this.paymentCardSafetyPolicy = Objects.requireNonNull(
                paymentCardSafetyPolicy, "paymentCardSafetyPolicy must not be null");
            return this;
        }

        /**
         * Selects the enforceable policy for generated banking identifiers and account values.
         *
         * <p>The default is {@link BankingSafetyPolicy#DISABLED}. Select
         * {@link BankingSafetyPolicy#REALISTIC_UNCLASSIFIED} only for isolated fixtures that do
         * not leave the test boundary.
         *
         * @param bankingSafetyPolicy banking generation policy
         * @return this builder
         */
        public Builder bankingSafetyPolicy(BankingSafetyPolicy bankingSafetyPolicy) {
            this.bankingSafetyPolicy = Objects.requireNonNull(
                bankingSafetyPolicy, "bankingSafetyPolicy must not be null");
            return this;
        }

        /**
         * Selects the enforceable policy for generated corporate tax identifiers.
         *
         * <p>The default is {@link BusinessTaxIdentifierSafetyPolicy#DISABLED}. Select
         * {@link BusinessTaxIdentifierSafetyPolicy#REALISTIC_UNCLASSIFIED} only for isolated
         * fixtures that do not leave the test boundary.
         *
         * @param businessTaxIdentifierSafetyPolicy corporate tax-identifier generation policy
         * @return this builder
         */
        public Builder businessTaxIdentifierSafetyPolicy(
            BusinessTaxIdentifierSafetyPolicy businessTaxIdentifierSafetyPolicy) {
            this.businessTaxIdentifierSafetyPolicy = Objects.requireNonNull(
                businessTaxIdentifierSafetyPolicy, "businessTaxIdentifierSafetyPolicy must not be null");
            return this;
        }

        /**
         * Selects the enforceable policy for generated cryptocurrency destination-address shapes.
         *
         * <p>The default is {@link CryptoAddressSafetyPolicy#DISABLED}. Select
         * {@link CryptoAddressSafetyPolicy#REALISTIC_UNCLASSIFIED} only for isolated fixtures that
         * do not leave the test boundary.
         *
         * @param cryptoAddressSafetyPolicy cryptocurrency address generation policy
         * @return this builder
         */
        public Builder cryptoAddressSafetyPolicy(CryptoAddressSafetyPolicy cryptoAddressSafetyPolicy) {
            this.cryptoAddressSafetyPolicy = Objects.requireNonNull(
                cryptoAddressSafetyPolicy, "cryptoAddressSafetyPolicy must not be null");
            return this;
        }

        /**
         * Selects the enforceable policy for locale-style phone-number output.
         *
         * <p>The default uses NANPA's fictional 555-0100 through 555-0199 range for US locales.
         * Other locales and custom templates remain unclassified.
         *
         * @param phoneNumberSafetyPolicy locale-style phone-number safety policy
         * @return this builder
         */
        public Builder phoneNumberSafetyPolicy(PhoneNumberSafetyPolicy phoneNumberSafetyPolicy) {
            this.phoneNumberSafetyPolicy = Objects.requireNonNull(
                phoneNumberSafetyPolicy, "phoneNumberSafetyPolicy must not be null");
            return this;
        }

        /**
         * Selects the enforceable policy for generated national identity numbers.
         *
         * <p>The default is {@link NationalIdSafetyPolicy#DISABLED}. Select
         * {@link NationalIdSafetyPolicy#REALISTIC_UNCLASSIFIED} only for isolated fixtures that
         * do not leave the test boundary.
         *
         * @param nationalIdSafetyPolicy national-ID generation policy
         * @return this builder
         */
        public Builder nationalIdSafetyPolicy(NationalIdSafetyPolicy nationalIdSafetyPolicy) {
            this.nationalIdSafetyPolicy = Objects.requireNonNull(
                nationalIdSafetyPolicy, "nationalIdSafetyPolicy must not be null");
            return this;
        }

        /**
         * Selects the enforceable policy for generated passport and driving-license identifiers.
         *
         * <p>The default is {@link IdentityDocumentSafetyPolicy#DISABLED}. Select
         * {@link IdentityDocumentSafetyPolicy#REALISTIC_UNCLASSIFIED} only for isolated fixtures
         * that do not leave the test boundary.
         *
         * @param identityDocumentSafetyPolicy identity-document generation policy
         * @return this builder
         */
        public Builder identityDocumentSafetyPolicy(IdentityDocumentSafetyPolicy identityDocumentSafetyPolicy) {
            this.identityDocumentSafetyPolicy = Objects.requireNonNull(
                identityDocumentSafetyPolicy, "identityDocumentSafetyPolicy must not be null");
            return this;
        }

        /**
         * Labels the provider dataset version for deterministic replay diagnostics.
         */
        public Builder providerDatasetVersion(String providerDatasetVersion) {
            this.providerDatasetVersion = requireRecipeToken("providerDatasetVersion", providerDatasetVersion);
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

        private static String requireRecipeToken(String name, String value) {
            Objects.requireNonNull(value, name + " must not be null");
            if (value.isBlank() || value.indexOf('\n') >= 0 || value.indexOf('\r') >= 0) {
                throw new IllegalArgumentException(name + " must be a non-blank single-line value");
            }
            return value;
        }

        public GeneratorConfig build() {
            int configuredSources = (numericSeed.isPresent() || stringSeed.isPresent()) ? 1 : 0;
            configuredSources += random == null ? 0 : 1;
            configuredSources += randomFactory == null ? 0 : 1;
            configuredSources += secureRandom ? 1 : 0;
            if (configuredSources > 1) {
                throw new IllegalStateException(
                    "Configure only one random source: seed, random, randomFactory, or secureRandom");
            }
            return new GeneratorConfig(this);
        }
    }
}
