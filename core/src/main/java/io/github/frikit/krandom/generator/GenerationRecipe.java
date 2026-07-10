/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator;

import io.github.frikit.krandom.generator.object.ObjectConstructionPolicy;
import io.github.frikit.krandom.generator.finance.PaymentCardSafetyPolicy;
import io.github.frikit.krandom.generator.location.PhoneNumberSafetyPolicy;
import io.github.frikit.krandom.generator.user.nationalid.NationalIdSafetyPolicy;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.charset.Charset;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.TreeMap;

/**
 * A portable, versioned description of deterministic generation.
 *
 * <p>The serialized form is line-oriented, human-readable, and stable: every line is
 * {@code key=value}, required fields appear in a fixed order, and optional settings are sorted by
 * name. Values are UTF-8 form-encoded, so a recipe can safely carry arbitrary string seed text.
 *
 * <p>Recipe version and random algorithm are separate compatibility boundaries. Version {@value
 * #RECIPE_VERSION} uses {@value #ALGORITHM}; patch releases must preserve its child-stream and
 * {@link Random} behavior. A future algorithm change requires a new recipe version.
 */
public final class GenerationRecipe {

    /** Stable name for the line-oriented recipe format. */
    public static final String FORMAT = "krandom-recipe";

    /** Current recipe format and replay-contract version. */
    public static final String RECIPE_VERSION = "v1";

    /** Random algorithm and named-child derivation used by recipe version {@value #RECIPE_VERSION}. */
    public static final String ALGORITHM = "java.util.Random-v1";

    /** Version label for the built-in provider datasets shipped by this library line. */
    public static final String BUILTIN_PROVIDER_DATASET_VERSION = "builtin-v1";

    /** Default profile label for configurations that were not created through a named profile. */
    public static final String CUSTOM_PROFILE = "custom";

    /** Default label before the dedicated safety-mode contract is introduced. */
    public static final String LEGACY_UNCLASSIFIED_SAFETY_POLICY = "legacy-unclassified";

    private static final String FORMAT_KEY = "format";
    private static final String RECIPE_VERSION_KEY = "recipe-version";
    private static final String LIBRARY_VERSION_KEY = "library-version";
    private static final String ALGORITHM_KEY = "algorithm";
    private static final String SEED_KEY = "seed";
    private static final String SEED_TEXT_KEY = "seed-text";
    private static final String LOCALE_KEY = "locale";
    private static final String CLOCK_INSTANT_KEY = "clock-instant";
    private static final String CLOCK_ZONE_KEY = "clock-zone";
    private static final String PROFILE_KEY = "profile";
    private static final String SAFETY_POLICY_KEY = "safety-policy";
    private static final String CONSTRUCTION_POLICY_KEY = "construction-policy";
    private static final String PROVIDER_DATASET_VERSION_KEY = "provider-dataset-version";
    private static final String SETTING_PREFIX = "setting.";

    private static final long FNV1A_64_OFFSET_BASIS = 0xcbf29ce484222325L;
    private static final long FNV1A_64_PRIME        = 0x100000001b3L;

    private final String                    libraryVersion;
    private final String                    recipeVersion;
    private final String                    algorithm;
    private final long                      seed;
    private final String                    seedText;
    private final Locale                    locale;
    private final Instant                   clockInstant;
    private final ZoneId                    clockZone;
    private final String                    profile;
    private final String                    safetyPolicy;
    private final ObjectConstructionPolicy  constructionPolicy;
    private final String                    providerDatasetVersion;
    private final Map<String, String>       settings;

    private GenerationRecipe(Builder builder) {
        this.libraryVersion = requireToken("libraryVersion", builder.libraryVersion);
        this.recipeVersion = requireSupportedRecipeVersion(builder.recipeVersion);
        this.algorithm = requireSupportedAlgorithm(builder.algorithm);
        this.seed = builder.seed;
        this.seedText = builder.seedText;
        if (seedText != null && GeneratorConfig.deriveSeed(seedText) != seed) {
            throw new IllegalArgumentException("seedText must derive the configured seed");
        }
        this.locale = Objects.requireNonNull(builder.locale, "locale must not be null");
        this.clockInstant = Objects.requireNonNull(builder.clockInstant, "clockInstant must not be null");
        this.clockZone = Objects.requireNonNull(builder.clockZone, "clockZone must not be null");
        this.profile = requireToken("profile", builder.profile);
        this.safetyPolicy = requireToken("safetyPolicy", builder.safetyPolicy);
        this.constructionPolicy = Objects.requireNonNull(builder.constructionPolicy, "constructionPolicy must not be null");
        this.providerDatasetVersion = requireToken("providerDatasetVersion", builder.providerDatasetVersion);
        this.settings = Collections.unmodifiableMap(new TreeMap<>(builder.settings));
    }

    /**
     * Returns a builder populated with portable default metadata.
     *
     * @return recipe builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Parses the stable line-oriented recipe representation.
     *
     * @param serialized serialized recipe text
     * @return parsed recipe
     * @throws IllegalArgumentException if the form is malformed, incomplete, or unsupported
     */
    public static GenerationRecipe parse(String serialized) {
        Objects.requireNonNull(serialized, "serialized must not be null");
        if (serialized.isBlank()) {
            throw new IllegalArgumentException("serialized recipe must not be blank");
        }

        Map<String, String> entries = new LinkedHashMap<>();
        String[] lines = serialized.split("\\n", -1);
        for (int lineNumber = 0; lineNumber < lines.length; lineNumber++) {
            String line = lines[lineNumber];
            if (line.isEmpty() && lineNumber == lines.length - 1) {
                continue;
            }
            int separator = line.indexOf('=');
            if (separator <= 0) {
                throw new IllegalArgumentException("Invalid recipe line " + (lineNumber + 1));
            }
            String key = line.substring(0, separator);
            if (!key.matches("[a-z][a-z0-9.-]*")) {
                throw new IllegalArgumentException("Invalid recipe key: " + key);
            }
            if (entries.putIfAbsent(key, decode(line.substring(separator + 1))) != null) {
                throw new IllegalArgumentException("Duplicate recipe key: " + key);
            }
        }

        requireExact(entries, FORMAT_KEY, FORMAT);
        String recipeVersion = required(entries, RECIPE_VERSION_KEY);
        String algorithm = required(entries, ALGORITHM_KEY);
        Builder builder = builder()
            .recipeVersion(recipeVersion)
            .libraryVersion(required(entries, LIBRARY_VERSION_KEY))
            .algorithm(algorithm)
            .seed(parseLong(entries, SEED_KEY))
            .locale(Locale.forLanguageTag(required(entries, LOCALE_KEY)))
            .clock(Instant.parse(required(entries, CLOCK_INSTANT_KEY)), ZoneId.of(required(entries, CLOCK_ZONE_KEY)))
            .profile(required(entries, PROFILE_KEY))
            .safetyPolicy(required(entries, SAFETY_POLICY_KEY))
            .constructionPolicy(parseConstructionPolicy(required(entries, CONSTRUCTION_POLICY_KEY)))
            .providerDatasetVersion(required(entries, PROVIDER_DATASET_VERSION_KEY));
        String seedText = entries.remove(SEED_TEXT_KEY);
        if (seedText != null) {
            builder.seedText(seedText);
        }

        entries.remove(FORMAT_KEY);
        entries.remove(RECIPE_VERSION_KEY);
        entries.remove(LIBRARY_VERSION_KEY);
        entries.remove(ALGORITHM_KEY);
        entries.remove(SEED_KEY);
        entries.remove(LOCALE_KEY);
        entries.remove(CLOCK_INSTANT_KEY);
        entries.remove(CLOCK_ZONE_KEY);
        entries.remove(PROFILE_KEY);
        entries.remove(SAFETY_POLICY_KEY);
        entries.remove(CONSTRUCTION_POLICY_KEY);
        entries.remove(PROVIDER_DATASET_VERSION_KEY);
        for (Map.Entry<String, String> entry : entries.entrySet()) {
            if (!entry.getKey().startsWith(SETTING_PREFIX) || entry.getKey().length() == SETTING_PREFIX.length()) {
                throw new IllegalArgumentException("Unknown recipe key: " + entry.getKey());
            }
            builder.setting(entry.getKey().substring(SETTING_PREFIX.length()), entry.getValue());
        }
        return builder.build();
    }

    /**
     * Returns the current runtime library version when it is known.
     *
     * <p>Published jars expose their version through the manifest. Test and IDE classpaths may
     * supply {@code krandom.version}; the final fallback deliberately identifies that development
     * metadata is unavailable rather than claiming a release version.
     *
     * @return runtime library version label
     */
    public static String currentLibraryVersion() {
        Package packageInfo = GenerationRecipe.class.getPackage();
        return currentLibraryVersion(packageInfo.getImplementationVersion());
    }

    static String currentLibraryVersion(String manifestVersion) {
        return manifestVersion != null ? manifestVersion : System.getProperty("krandom.version", "development");
    }

    /** @return library version that produced this recipe */
    public String getLibraryVersion() {
        return libraryVersion;
    }

    /** @return recipe compatibility version */
    public String getRecipeVersion() {
        return recipeVersion;
    }

    /** @return random and child-stream algorithm identifier */
    public String getAlgorithm() {
        return algorithm;
    }

    /** @return deterministic root seed */
    public long getSeed() {
        return seed;
    }

    /** @return original textual seed, or {@code null} when a numeric seed was used */
    public String getSeedText() {
        return seedText;
    }

    /** @return locale used for locale-aware generators */
    public Locale getLocale() {
        return locale;
    }

    /** @return captured clock instant */
    public Instant getClockInstant() {
        return clockInstant;
    }

    /** @return captured clock zone */
    public ZoneId getClockZone() {
        return clockZone;
    }

    /** @return configuration profile label */
    public String getProfile() {
        return profile;
    }

    /** @return requested safety-policy label */
    public String getSafetyPolicy() {
        return safetyPolicy;
    }

    /** @return object construction policy */
    public ObjectConstructionPolicy getConstructionPolicy() {
        return constructionPolicy;
    }

    /** @return provider dataset version label */
    public String getProviderDatasetVersion() {
        return providerDatasetVersion;
    }

    /** @return immutable replay settings beyond the top-level recipe fields */
    public Map<String, String> getSettings() {
        return settings;
    }

    /**
     * Creates an independent child random source identified by a stable structural name.
     *
     * @param streamIdentity non-blank structural stream identity
     * @return fresh child random source
     */
    public Random childRandom(String streamIdentity) {
        return new Random(deriveChildSeed(seed, streamIdentity));
    }

    /**
     * Recreates a portable generator configuration with a fixed clock.
     *
     * @return replay configuration
     * @throws IllegalArgumentException if this library does not recognize a recorded setting
     */
    public GeneratorConfig toGeneratorConfig() {
        GeneratorConfig.Builder builder = GeneratorConfig.builder()
                                                         .seed(seed)
                                                         .locale(locale)
                                                         .clock(Clock.fixed(clockInstant, clockZone))
                                                         .generationProfile(profile)
                                                         .safetyPolicy(safetyPolicy)
                                                         .providerDatasetVersion(providerDatasetVersion)
                                                         .objectConstructionPolicy(constructionPolicy);
        if (seedText != null) {
            builder.seed(seedText);
        }
        applySettings(builder);
        return builder.build();
    }

    /**
     * Serializes this recipe in its stable portable form.
     *
     * @return stable human-readable recipe text
     */
    public String serialize() {
        return serialize(true);
    }

    /**
     * Serializes this recipe for logs, failure diagnostics, and test reports.
     *
     * <p>The numeric derived seed remains sufficient for exact replay, while original textual seed
     * material is omitted so diagnostics cannot reveal user-provided text.
     *
     * @return safe stable recipe text
     */
    public String serializeForDiagnostics() {
        return serialize(false);
    }

    private String serialize(boolean includeSeedText) {
        StringBuilder builder = new StringBuilder();
        append(builder, FORMAT_KEY, FORMAT);
        append(builder, RECIPE_VERSION_KEY, recipeVersion);
        append(builder, LIBRARY_VERSION_KEY, libraryVersion);
        append(builder, ALGORITHM_KEY, algorithm);
        append(builder, SEED_KEY, Long.toString(seed));
        if (includeSeedText && seedText != null) {
            append(builder, SEED_TEXT_KEY, seedText);
        }
        append(builder, LOCALE_KEY, locale.toLanguageTag());
        append(builder, CLOCK_INSTANT_KEY, clockInstant.toString());
        append(builder, CLOCK_ZONE_KEY, clockZone.getId());
        append(builder, PROFILE_KEY, profile);
        append(builder, SAFETY_POLICY_KEY, safetyPolicy);
        append(builder, CONSTRUCTION_POLICY_KEY, constructionPolicyId(constructionPolicy));
        append(builder, PROVIDER_DATASET_VERSION_KEY, providerDatasetVersion);
        for (Map.Entry<String, String> setting : settings.entrySet()) {
            append(builder, SETTING_PREFIX + setting.getKey(), setting.getValue());
        }
        return builder.toString();
    }

    @Override
    public String toString() {
        return serialize();
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof GenerationRecipe recipe && serialize().equals(recipe.serialize());
    }

    @Override
    public int hashCode() {
        return serialize().hashCode();
    }

    /**
     * Derives the stable child seed used by recipe version {@value #RECIPE_VERSION}.
     *
     * <p>This is the low-level form of {@link #childRandom(String)} for internal generation
     * boundaries that already hold a deterministic parent seed.
     *
     * @param parentSeed parent stream seed
     * @param streamIdentity non-blank structural stream identity
     * @return deterministic child seed
     */
    public static long deriveChildSeed(long parentSeed, String streamIdentity) {
        String identity = requireToken("streamIdentity", streamIdentity);
        long hash = FNV1A_64_OFFSET_BASIS;
        for (int shift = 0; shift < Long.SIZE; shift += Byte.SIZE) {
            hash ^= (parentSeed >>> shift) & 0xffL;
            hash *= FNV1A_64_PRIME;
        }
        for (byte value : identity.getBytes(StandardCharsets.UTF_8)) {
            hash ^= value & 0xffL;
            hash *= FNV1A_64_PRIME;
        }
        return hash;
    }

    private void applySettings(GeneratorConfig.Builder builder) {
        String charset = settings.get("charset");
        if (charset != null) {
            builder.charset(Charset.forName(charset));
        }
        Integer stringMin = integerSetting("string.min");
        Integer stringMax = integerSetting("string.max");
        if (stringMin != null || stringMax != null) {
            builder.stringLength(requirePair("string.min", stringMin), requirePair("string.max", stringMax));
        }
        Integer collectionMin = integerSetting("collection.min");
        Integer collectionMax = integerSetting("collection.max");
        if (collectionMin != null || collectionMax != null) {
            builder.collectionSize(requirePair("collection.min", collectionMin),
                                   requirePair("collection.max", collectionMax));
        }
        applyInteger(builder, "object.max-depth", builder::objectMaxDepth);
        applyInteger(builder, "object.pool-size", builder::objectPoolSize);
        applyBoolean(builder, "object.override-default-initialization", builder::objectOverrideDefaultInitialization);
        applyBoolean(builder, "object.ignore-errors", builder::objectIgnoreErrors);
        applyPaymentCardSafetyPolicy(builder);
        applyPhoneNumberSafetyPolicy(builder);
        applyNationalIdSafetyPolicy(builder);
        applyEnum(builder, "object.semantic-mode", builder::objectSemanticMode);
        applyDouble(builder, "object.null-probability", builder::objectNullProbability);
        applyDouble(builder, "object.optional-empty-probability", builder::objectOptionalEmptyProbability);
        applyInteger(builder, "object.uniqueness-max-attempts", builder::objectUniquenessMaxAttempts);
        applyUniqueFields(builder);
        applyDateRange(builder);
        for (String key : settings.keySet()) {
            if (!isKnownSetting(key)) {
                throw new IllegalArgumentException("Unsupported recipe setting: " + key);
            }
        }
    }

    private void applyUniqueFields(GeneratorConfig.Builder builder) {
        String value = settings.get("object.unique-fields");
        if (value != null) {
            builder.objectUniqueFields(value.split(",", -1));
        }
    }

    private void applyPaymentCardSafetyPolicy(GeneratorConfig.Builder builder) {
        String value = settings.get("payment.card-safety-policy");
        PaymentCardSafetyPolicy policy = value == null
            ? PaymentCardSafetyPolicy.CHECKSUM_VALID
            : PaymentCardSafetyPolicy.valueOf(value);
        builder.paymentCardSafetyPolicy(policy);
    }

    private void applyPhoneNumberSafetyPolicy(GeneratorConfig.Builder builder) {
        String value = settings.get("phone-number.safety-policy");
        PhoneNumberSafetyPolicy policy = value == null
            ? PhoneNumberSafetyPolicy.REALISTIC_UNCLASSIFIED
            : PhoneNumberSafetyPolicy.valueOf(value);
        builder.phoneNumberSafetyPolicy(policy);
    }

    private void applyNationalIdSafetyPolicy(GeneratorConfig.Builder builder) {
        String value = settings.get("national-id.safety-policy");
        NationalIdSafetyPolicy policy = value == null
            ? NationalIdSafetyPolicy.REALISTIC_UNCLASSIFIED
            : NationalIdSafetyPolicy.valueOf(value);
        builder.nationalIdSafetyPolicy(policy);
    }

    private void applyDateRange(GeneratorConfig.Builder builder) {
        String min = settings.get("object.date-min");
        String max = settings.get("object.date-max");
        if (min != null || max != null) {
            builder.objectDateRange(java.time.LocalDate.parse(requirePair("object.date-min", min)),
                                    java.time.LocalDate.parse(requirePair("object.date-max", max)));
        }
    }

    private Integer integerSetting(String key) {
        String value = settings.get(key);
        return value == null ? null : Integer.valueOf(value);
    }

    private static int requirePair(String key, Integer value) {
        if (value == null) {
            throw new IllegalArgumentException("Recipe requires setting: " + key);
        }
        return value;
    }

    private static String requirePair(String key, String value) {
        if (value == null) {
            throw new IllegalArgumentException("Recipe requires setting: " + key);
        }
        return value;
    }

    private static boolean isKnownSetting(String key) {
        return switch (key) {
            case "charset", "string.min", "string.max", "collection.min", "collection.max", "object.max-depth",
                 "object.pool-size", "object.override-default-initialization", "object.ignore-errors",
                 "object.semantic-mode", "object.null-probability", "object.optional-empty-probability",
                 "object.unique-fields", "object.uniqueness-max-attempts", "object.date-min", "object.date-max",
                 "payment.card-safety-policy", "phone-number.safety-policy", "national-id.safety-policy" -> true;
            default -> false;
        };
    }

    private static void append(StringBuilder builder, String key, String value) {
        builder.append(key).append('=').append(encode(value)).append('\n');
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private static String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }

    private static String required(Map<String, String> entries, String key) {
        String value = entries.get(key);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Recipe requires key: " + key);
        }
        return value;
    }

    private static long parseLong(Map<String, String> entries, String key) {
        try {
            return Long.parseLong(required(entries, key));
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Recipe key '" + key + "' must be a long", ex);
        }
    }

    private static void requireExact(Map<String, String> entries, String key, String expected) {
        String actual = required(entries, key);
        if (!expected.equals(actual)) {
            throw new IllegalArgumentException("Unsupported " + key + ": " + actual);
        }
    }

    private static String requireSupportedRecipeVersion(String value) {
        String version = requireToken("recipeVersion", value);
        if (!RECIPE_VERSION.equals(version)) {
            throw new IllegalArgumentException("Unsupported recipe version: " + version);
        }
        return version;
    }

    private static String requireSupportedAlgorithm(String value) {
        String algorithm = requireToken("algorithm", value);
        if (!ALGORITHM.equals(algorithm)) {
            throw new IllegalArgumentException("Unsupported recipe algorithm: " + algorithm);
        }
        return algorithm;
    }

    private static String requireToken(String name, String value) {
        Objects.requireNonNull(value, name + " must not be null");
        if (value.isBlank() || value.indexOf('\n') >= 0 || value.indexOf('\r') >= 0) {
            throw new IllegalArgumentException(name + " must be a non-blank single-line value");
        }
        return value;
    }

    private static String constructionPolicyId(ObjectConstructionPolicy policy) {
        return switch (policy) {
            case SAFE_CONSTRUCTORS -> "safe-constructors";
            case UNSAFE_CONSTRUCTOR_BYPASS -> "unsafe-constructor-bypass";
        };
    }

    private static ObjectConstructionPolicy parseConstructionPolicy(String value) {
        return switch (value) {
            case "safe-constructors" -> ObjectConstructionPolicy.SAFE_CONSTRUCTORS;
            case "unsafe-constructor-bypass" -> ObjectConstructionPolicy.UNSAFE_CONSTRUCTOR_BYPASS;
            default -> throw new IllegalArgumentException("Unsupported construction policy: " + value);
        };
    }

    private void applyInteger(GeneratorConfig.Builder builder,
                              String key,
                              java.util.function.IntFunction<GeneratorConfig.Builder> setter) {
        String value = settings.get(key);
        if (value != null) {
            setter.apply(Integer.parseInt(value));
        }
    }

    private void applyBoolean(GeneratorConfig.Builder builder,
                              String key,
                              java.util.function.Function<Boolean, GeneratorConfig.Builder> setter) {
        String value = settings.get(key);
        if (value != null) {
            if (!"true".equals(value) && !"false".equals(value)) {
                throw new IllegalArgumentException("Recipe setting '" + key + "' must be true or false");
            }
            setter.apply(Boolean.parseBoolean(value));
        }
    }

    private void applyEnum(GeneratorConfig.Builder builder,
                           String key,
                           java.util.function.Function<
                               io.github.frikit.krandom.generator.object.ObjectGenerationSemanticMode,
                               GeneratorConfig.Builder> setter) {
        String value = settings.get(key);
        if (value != null) {
            setter.apply(io.github.frikit.krandom.generator.object.ObjectGenerationSemanticMode.valueOf(value));
        }
    }

    private void applyDouble(GeneratorConfig.Builder builder,
                             String key,
                             java.util.function.Function<Double, GeneratorConfig.Builder> setter) {
        String value = settings.get(key);
        if (value != null) {
            setter.apply(Double.parseDouble(value));
        }
    }

    /** Builder for a {@link GenerationRecipe}. */
    public static final class Builder {

        private String                    libraryVersion = currentLibraryVersion();
        private String                    recipeVersion = RECIPE_VERSION;
        private String                    algorithm = ALGORITHM;
        private long                      seed;
        private String                    seedText;
        private Locale                    locale = Locale.US;
        private Instant                   clockInstant = Instant.EPOCH;
        private ZoneId                    clockZone = ZoneId.of("UTC");
        private String                    profile = CUSTOM_PROFILE;
        private String                    safetyPolicy = LEGACY_UNCLASSIFIED_SAFETY_POLICY;
        private ObjectConstructionPolicy  constructionPolicy = ObjectConstructionPolicy.SAFE_CONSTRUCTORS;
        private String                    providerDatasetVersion = BUILTIN_PROVIDER_DATASET_VERSION;
        private final Map<String, String> settings = new TreeMap<>();

        private Builder() {
        }

        public Builder libraryVersion(String libraryVersion) {
            this.libraryVersion = libraryVersion;
            return this;
        }

        public Builder recipeVersion(String recipeVersion) {
            this.recipeVersion = recipeVersion;
            return this;
        }

        public Builder algorithm(String algorithm) {
            this.algorithm = algorithm;
            return this;
        }

        public Builder seed(long seed) {
            this.seed = seed;
            return this;
        }

        public Builder seedText(String seedText) {
            this.seedText = requireToken("seedText", seedText);
            return this;
        }

        public Builder locale(Locale locale) {
            this.locale = Objects.requireNonNull(locale, "locale must not be null");
            return this;
        }

        public Builder clock(Instant clockInstant, ZoneId clockZone) {
            this.clockInstant = Objects.requireNonNull(clockInstant, "clockInstant must not be null");
            this.clockZone = Objects.requireNonNull(clockZone, "clockZone must not be null");
            return this;
        }

        public Builder profile(String profile) {
            this.profile = requireToken("profile", profile);
            return this;
        }

        public Builder safetyPolicy(String safetyPolicy) {
            this.safetyPolicy = requireToken("safetyPolicy", safetyPolicy);
            return this;
        }

        public Builder constructionPolicy(ObjectConstructionPolicy constructionPolicy) {
            this.constructionPolicy = Objects.requireNonNull(constructionPolicy, "constructionPolicy must not be null");
            return this;
        }

        public Builder providerDatasetVersion(String providerDatasetVersion) {
            this.providerDatasetVersion = requireToken("providerDatasetVersion", providerDatasetVersion);
            return this;
        }

        public Builder setting(String name, String value) {
            String settingName = requireToken("setting name", name);
            if (!settingName.matches("[a-z][a-z0-9.-]*")) {
                throw new IllegalArgumentException("setting name has an invalid format: " + settingName);
            }
            settings.put(settingName, requireToken("setting value", value));
            return this;
        }

        public GenerationRecipe build() {
            return new GenerationRecipe(this);
        }
    }
}
