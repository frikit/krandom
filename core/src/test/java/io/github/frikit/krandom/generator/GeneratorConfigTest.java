/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator;

import io.github.frikit.krandom.generator.object.ObjectGenerationSemanticMode;
import io.github.frikit.krandom.generator.object.SemanticFieldRegistry;
import io.github.frikit.krandom.generator.failure.GenerationFailureListener;
import io.github.frikit.krandom.generator.finance.BankingSafetyPolicy;
import io.github.frikit.krandom.generator.finance.PaymentCardSafetyPolicy;
import io.github.frikit.krandom.generator.location.PhoneNumberSafetyPolicy;
import io.github.frikit.krandom.generator.user.nationalid.NationalIdSafetyPolicy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("GeneratorConfig")
class GeneratorConfigTest {

    @Test
    @DisplayName("defaults() returns config with all default values")
    void defaultValues() {
        GeneratorConfig c = GeneratorConfig.defaults();
        assertTrue(c.getSeed().isEmpty());
        assertTrue(c.getStringSeed().isEmpty());
        assertTrue(c.getRandom().isEmpty());
        assertTrue(c.getRandomFactory().isEmpty());
        assertFalse(c.isSecureRandom());
        assertEquals(GeneratorConfig.STRING_SEED_DERIVATION, c.getSeedDerivationVersion());
        assertEquals(StandardCharsets.US_ASCII, c.getCharset());
        assertEquals(5, c.getMinStringLength());
        assertEquals(20, c.getMaxStringLength());
        assertEquals(1, c.getMinCollectionSize());
        assertEquals(10, c.getMaxCollectionSize());
        assertEquals(GeneratorConfig.DEFAULT_OBJECT_MAX_DEPTH, c.getObjectMaxDepth());
        assertEquals(GeneratorConfig.DEFAULT_OBJECT_POOL_SIZE, c.getObjectPoolSize());
        assertFalse(c.isObjectOverrideDefaultInitialization());
        assertFalse(c.isObjectIgnoreErrors());
        assertTrue(c.getGenerationFailureListener() != null);
        assertNull(c.getObjectDateMin());
        assertNull(c.getObjectDateMax());
        assertEquals(ObjectGenerationSemanticMode.RELAXED, c.getObjectSemanticMode());
        assertSame(SemanticFieldRegistry.defaults(), c.getObjectSemanticRegistry());
        assertEquals(0.0, c.getObjectNullProbability());
        assertEquals(0.0, c.getObjectOptionalEmptyProbability());
        assertEquals(Set.of("email", "emailaddress", "username", "userhandle", "uuid", "guid", "id"),
                     c.getObjectUniqueFieldNames());
        assertEquals(256, c.getObjectUniquenessMaxAttempts());
        assertTrue(c.getObjectTypeOverride(String.class).isEmpty());
        assertTrue(c.getObjectFieldOverride(RootObjectConfigFixture.class, "name").isEmpty());
        assertTrue(c.getObjectContextualTypeOverride(String.class).isEmpty());
        assertTrue(c.getObjectContextualFieldOverride(RootObjectConfigFixture.class, "name").isEmpty());
        assertEquals(Locale.US, c.getLocale());
        assertEquals(ZoneId.systemDefault(), c.getClock().getZone());
        assertSame(DataRegistryContext.globalDefault(), c.getRegistryContext());
        assertEquals(PaymentCardSafetyPolicy.TEST_SAFE_NON_ROUTABLE, c.getPaymentCardSafetyPolicy());
        assertEquals(BankingSafetyPolicy.DISABLED, c.getBankingSafetyPolicy());
        assertEquals(PhoneNumberSafetyPolicy.TEST_SAFE_WHERE_AVAILABLE, c.getPhoneNumberSafetyPolicy());
        assertEquals(NationalIdSafetyPolicy.DISABLED, c.getNationalIdSafetyPolicy());
    }

    @Test
    @DisplayName("payment card safety policy is configurable and retained by toBuilder")
    void paymentCardSafetyPolicyConfigurable() {
        GeneratorConfig config = GeneratorConfig.builder()
                                                .paymentCardSafetyPolicy(PaymentCardSafetyPolicy.CHECKSUM_VALID)
                                                .build();

        assertEquals(PaymentCardSafetyPolicy.CHECKSUM_VALID,
                     config.toBuilder().build().getPaymentCardSafetyPolicy());
        assertThrows(NullPointerException.class,
                     () -> GeneratorConfig.builder().paymentCardSafetyPolicy(null));
    }

    @Test
    @DisplayName("phone number safety policy is configurable and retained by toBuilder")
    void phoneNumberSafetyPolicyConfigurable() {
        GeneratorConfig config = GeneratorConfig.builder()
                                                .phoneNumberSafetyPolicy(PhoneNumberSafetyPolicy.REALISTIC_UNCLASSIFIED)
                                                .build();

        assertEquals(PhoneNumberSafetyPolicy.REALISTIC_UNCLASSIFIED,
                     config.toBuilder().build().getPhoneNumberSafetyPolicy());
        assertThrows(NullPointerException.class,
                     () -> GeneratorConfig.builder().phoneNumberSafetyPolicy(null));
    }

    @Test
    @DisplayName("national-ID safety policy is configurable and retained by toBuilder")
    void nationalIdSafetyPolicyConfigurable() {
        GeneratorConfig config = GeneratorConfig.builder()
                                                .nationalIdSafetyPolicy(NationalIdSafetyPolicy.REALISTIC_UNCLASSIFIED)
                                                .build();

        assertEquals(NationalIdSafetyPolicy.REALISTIC_UNCLASSIFIED,
                     config.toBuilder().build().getNationalIdSafetyPolicy());
        assertThrows(NullPointerException.class,
                     () -> GeneratorConfig.builder().nationalIdSafetyPolicy(null));
    }

    @Test
    @DisplayName("banking safety policy is configurable and retained by toBuilder")
    void bankingSafetyPolicyConfigurable() {
        GeneratorConfig config = GeneratorConfig.builder()
                                                .bankingSafetyPolicy(BankingSafetyPolicy.REALISTIC_UNCLASSIFIED)
                                                .build();

        assertEquals(BankingSafetyPolicy.REALISTIC_UNCLASSIFIED,
                     config.toBuilder().build().getBankingSafetyPolicy());
        assertThrows(NullPointerException.class,
                     () -> GeneratorConfig.builder().bankingSafetyPolicy(null));
    }

    @Test
    @DisplayName("seed() stores the seed value")
    void seedStored() {
        GeneratorConfig c = GeneratorConfig.builder().seed(42L).build();
        assertTrue(c.getSeed().isPresent());
        assertEquals(42L, c.getSeed().getAsLong());
        assertTrue(c.getStringSeed().isEmpty());
    }

    @Test
    @DisplayName("seed(String) stores raw text and derives deterministic numeric seed")
    void stringSeedStoredAndDerived() {
        GeneratorConfig c = GeneratorConfig.builder().seed("phase2-seed").build();
        assertEquals("phase2-seed", c.getStringSeed().orElseThrow());
        assertEquals(5324094342740825832L, c.getSeed().orElseThrow());
    }

    @Test
    @DisplayName("seed(String) rejects blank value")
    void stringSeedBlankThrows() {
        assertThrows(IllegalArgumentException.class, () -> GeneratorConfig.builder().seed("   "));
    }

    @Test
    @DisplayName("deriveSeed is stable and versioned")
    void deriveSeedStable() {
        assertEquals("fnv1a64-v1", GeneratorConfig.STRING_SEED_DERIVATION);
        assertEquals(3327696251281893669L, GeneratorConfig.deriveSeed("krandom"));
        assertEquals(8288510794048708030L, GeneratorConfig.deriveSeed("مرحبا"));
    }

    @Test
    @DisplayName("deriveSeed rejects blank value")
    void deriveSeedBlankThrows() {
        assertThrows(IllegalArgumentException.class, () -> GeneratorConfig.deriveSeed("   "));
    }

    @Test
    @DisplayName("createRandom with long seed preserves java.util.Random sequence compatibility")
    void longSeedCompatibility() {
        GeneratorConfig c = GeneratorConfig.builder().seed(42L).build();
        assertEquals(-1170105035, c.createRandom().nextInt());
    }

    @Test
    @DisplayName("random(Random) stores and reuses caller-owned instance")
    void randomInstanceStoredAndReused() {
        Random random = new Random(7L);
        GeneratorConfig c = GeneratorConfig.builder().random(random).build();

        assertSame(random, c.getRandom().orElseThrow());
        assertSame(random, c.createRandom());
        assertSame(random, c.createRandom());
    }

    @Test
    @DisplayName("random(null) throws NullPointerException")
    void randomNullThrows() {
        assertThrows(NullPointerException.class, () -> GeneratorConfig.builder().random(null));
    }

    @Test
    @DisplayName("toBuilder() can swap caller-owned random instance")
    void toBuilderCanSwapRandomInstance() {
        Random first = new Random(1L);
        Random second = new Random(2L);
        GeneratorConfig base = GeneratorConfig.builder().random(first).build();

        GeneratorConfig swapped = base.toBuilder().random(second).build();

        assertSame(first, base.createRandom());
        assertSame(second, swapped.createRandom());
    }

    @ParameterizedTest(name = "{0} then {2}")
    @MethodSource("conflictingRandomSources")
    @DisplayName("build rejects conflicting random sources regardless of call order")
    void conflictingRandomSourcesRejected(String firstName,
                                           Consumer<GeneratorConfig.Builder> first,
                                           String secondName,
                                           Consumer<GeneratorConfig.Builder> second) {
        GeneratorConfig.Builder builder = GeneratorConfig.builder();
        first.accept(builder);
        second.accept(builder);

        IllegalStateException error = assertThrows(IllegalStateException.class, builder::build);
        assertEquals("Configure only one random source: seed, random, randomFactory, or secureRandom",
                     error.getMessage());
    }

    private static Stream<Arguments> conflictingRandomSources() {
        var sources = java.util.List.of(
            new RandomSourceConfiguration("seed", builder -> builder.seed(42L)),
            new RandomSourceConfiguration("random", builder -> builder.random(new Random(7L))),
            new RandomSourceConfiguration("randomFactory", builder -> builder.randomFactory(() -> new Random(11L))),
            new RandomSourceConfiguration("secureRandom", GeneratorConfig.Builder::secureRandom));

        return IntStream.range(0, sources.size())
                        .boxed()
                        .flatMap(first -> IntStream.range(0, sources.size())
                                                   .filter(second -> second != first)
                                                   .mapToObj(second -> Arguments.of(
                                                       sources.get(first).name(),
                                                       sources.get(first).configure(),
                                                       sources.get(second).name(),
                                                       sources.get(second).configure())));
    }

    @Test
    @DisplayName("randomFactory is used without mutating the returned random source")
    void randomFactoryUsed() {
        AtomicInteger calls = new AtomicInteger();
        GeneratorConfig c = GeneratorConfig.builder()
                                           .randomFactory(() -> {
                                               calls.incrementAndGet();
                                               return new Random(1L);
                                           })
                                           .build();

        Random actual = c.createRandom();
        Random expected = new Random(1L);
        assertEquals(expected.nextInt(), actual.nextInt());
        assertEquals(1, calls.get());
    }

    @Test
    @DisplayName("randomFactory(null) throws NullPointerException")
    void randomFactoryNullThrows() {
        assertThrows(NullPointerException.class, () -> GeneratorConfig.builder().randomFactory(null));
    }

    @Test
    @DisplayName("createRandom throws when randomFactory returns null")
    void randomFactoryReturningNullThrows() {
        GeneratorConfig c = GeneratorConfig.builder().randomFactory(() -> null).build();
        assertThrows(NullPointerException.class, c::createRandom);
    }

    @Test
    @DisplayName("createRandom uses fast Random by default")
    void createRandomDefaultUsesFastRandom() {
        assertFalse(GeneratorConfig.defaults().createRandom() instanceof SecureRandom);
    }

    @Test
    @DisplayName("secureRandom() opts unseeded generation into SecureRandom")
    void secureRandomOptInUsesSecureRandom() {
        GeneratorConfig c = GeneratorConfig.builder().secureRandom().build();

        assertTrue(c.isSecureRandom());
        assertTrue(c.createRandom() instanceof SecureRandom);
    }

    @Test
    @DisplayName("secureRandom(false) disables secure random opt-in")
    void secureRandomCanBeDisabled() {
        GeneratorConfig c = GeneratorConfig.builder().secureRandom().secureRandom(false).build();

        assertFalse(c.isSecureRandom());
        assertFalse(c.createRandom() instanceof SecureRandom);
    }

    @Test
    @DisplayName("toBuilder() copies secure random opt-in")
    void toBuilderCopiesSecureRandomOptIn() {
        GeneratorConfig derived = GeneratorConfig.builder().secureRandom().build().toBuilder().build();

        assertTrue(derived.isSecureRandom());
        assertTrue(derived.createRandom() instanceof SecureRandom);
    }

    @Test
    @DisplayName("latest seed call wins between long and string overloads")
    void latestSeedWins() {
        GeneratorConfig a = GeneratorConfig.builder().seed(1L).seed("my-seed").build();
        assertTrue(a.getStringSeed().isPresent());
        assertEquals(-4581536756751041509L, a.getSeed().orElseThrow());

        GeneratorConfig b = GeneratorConfig.builder().seed("my-seed").seed(99L).build();
        assertTrue(b.getStringSeed().isEmpty());
        assertEquals(99L, b.getSeed().orElseThrow());
    }

    @Test
    @DisplayName("charset() stores the charset")
    void charsetStored() {
        GeneratorConfig c = GeneratorConfig.builder().charset(StandardCharsets.UTF_8).build();
        assertEquals(StandardCharsets.UTF_8, c.getCharset());
    }

    @Test
    @DisplayName("charset(null) throws NullPointerException")
    void charsetNullThrows() {
        assertThrows(NullPointerException.class,
                     () -> GeneratorConfig.builder().charset(null));
    }

    @Test
    @DisplayName("stringLength(8, 32) stores the values")
    void stringLengthValid() {
        GeneratorConfig c = GeneratorConfig.builder().stringLength(8, 32).build();
        assertEquals(8, c.getMinStringLength());
        assertEquals(32, c.getMaxStringLength());
    }

    @Test
    @DisplayName("stringLength(0, 10) throws — min must be >= 1")
    void stringLengthMinBelowOneThrows() {
        assertThrows(IllegalArgumentException.class,
                     () -> GeneratorConfig.builder().stringLength(0, 10));
    }

    @Test
    @DisplayName("stringLength(10, 5) throws — max must be >= min")
    void stringLengthMaxBelowMinThrows() {
        assertThrows(IllegalArgumentException.class,
                     () -> GeneratorConfig.builder().stringLength(10, 5));
    }

    @Test
    @DisplayName("collectionSize(0, 20) stores the values")
    void collectionSizeValid() {
        GeneratorConfig c = GeneratorConfig.builder().collectionSize(0, 20).build();
        assertEquals(0, c.getMinCollectionSize());
        assertEquals(20, c.getMaxCollectionSize());
    }

    @Test
    @DisplayName("collectionSize(-1, 5) throws — min must be >= 0")
    void collectionSizeMinNegativeThrows() {
        assertThrows(IllegalArgumentException.class,
                     () -> GeneratorConfig.builder().collectionSize(-1, 5));
    }

    @Test
    @DisplayName("collectionSize(5, 3) throws — max must be >= min")
    void collectionSizeMaxBelowMinThrows() {
        assertThrows(IllegalArgumentException.class,
                     () -> GeneratorConfig.builder().collectionSize(5, 3));
    }

    @Test
    @DisplayName("object generation settings are stored on the root config")
    void objectGenerationSettingsStored() {
        LocalDate min = LocalDate.of(2021, 1, 1);
        LocalDate max = LocalDate.of(2021, 12, 31);
        GenerationFailureListener listener = diagnostic -> {};
        GeneratorConfig c = GeneratorConfig.builder()
                                           .objectMaxDepth(3)
                                           .objectPoolSize(2)
                                           .objectOverrideDefaultInitialization(true)
                                           .objectIgnoreErrors(true)
                                           .generationFailureListener(listener)
                                           .objectSemanticMode(ObjectGenerationSemanticMode.STRICT)
                                           .objectNullProbability(0.25)
                                           .objectOptionalEmptyProbability(0.5)
                                           .objectUniqueFields("email", "accountId")
                                           .objectUniquenessMaxAttempts(7)
                                           .objectDateRange(min, max)
                                           .build();
        assertEquals(3, c.getObjectMaxDepth());
        assertEquals(2, c.getObjectPoolSize());
        assertTrue(c.isObjectOverrideDefaultInitialization());
        assertTrue(c.isObjectIgnoreErrors());
        assertSame(listener, c.getGenerationFailureListener());
        assertEquals(ObjectGenerationSemanticMode.STRICT, c.getObjectSemanticMode());
        assertEquals(0.25, c.getObjectNullProbability());
        assertEquals(0.5, c.getObjectOptionalEmptyProbability());
        assertEquals(Set.of("email", "accountid"), c.getObjectUniqueFieldNames());
        assertEquals(7, c.getObjectUniquenessMaxAttempts());
        assertEquals(min, c.getObjectDateMin());
        assertEquals(max, c.getObjectDateMax());
    }

    @Test
    @DisplayName("objectMaxDepth(0) throws — min must be >= 1")
    void objectMaxDepthZeroThrows() {
        assertThrows(IllegalArgumentException.class,
                     () -> GeneratorConfig.builder().objectMaxDepth(0));
    }

    @Test
    @DisplayName("objectPoolSize(-1) throws — size must be >= 0")
    void objectPoolSizeNegativeThrows() {
        assertThrows(IllegalArgumentException.class,
                     () -> GeneratorConfig.builder().objectPoolSize(-1));
    }

    @Test
    @DisplayName("objectDateRange(min > max) throws")
    void objectDateRangeInvalidThrows() {
        LocalDate min = LocalDate.of(2024, 1, 1);
        LocalDate max = LocalDate.of(2023, 1, 1);
        assertThrows(IllegalArgumentException.class,
                     () -> GeneratorConfig.builder().objectDateRange(min, max));
    }

    @Test
    @DisplayName("object null and optional probabilities must be within [0, 1]")
    void objectProbabilityValidation() {
        assertThrows(IllegalArgumentException.class,
                     () -> GeneratorConfig.builder().objectNullProbability(-0.1));
        assertThrows(IllegalArgumentException.class,
                     () -> GeneratorConfig.builder().objectNullProbability(1.1));
        assertThrows(IllegalArgumentException.class,
                     () -> GeneratorConfig.builder().objectNullProbability(Double.NaN));
        assertThrows(IllegalArgumentException.class,
                     () -> GeneratorConfig.builder().objectOptionalEmptyProbability(-0.1));
        assertThrows(IllegalArgumentException.class,
                     () -> GeneratorConfig.builder().objectOptionalEmptyProbability(1.1));
        assertThrows(IllegalArgumentException.class,
                     () -> GeneratorConfig.builder().objectOptionalEmptyProbability(Double.NaN));
    }

    @Test
    @DisplayName("object uniqueness settings validate and normalize field names")
    void objectUniquenessSettingsValidateAndNormalize() {
        GeneratorConfig config = GeneratorConfig.builder()
                                                .objectUniqueFields("Email", "user_name")
                                                .objectUniqueField("Guid")
                                                .objectUniquenessMaxAttempts(3)
                                                .build();
        assertEquals(Set.of("email", "username", "guid"), config.getObjectUniqueFieldNames());
        assertEquals(3, config.getObjectUniquenessMaxAttempts());
        assertThrows(IllegalArgumentException.class,
                     () -> GeneratorConfig.builder().objectUniquenessMaxAttempts(0));
        assertThrows(IllegalArgumentException.class,
                     () -> GeneratorConfig.builder().objectUniqueField("___"));
    }

    @Test
    @DisplayName("advanced object overrides and exclusions are stored on the root config")
    void advancedObjectOverridesAndExclusionsStored() throws Exception {
        GeneratorConfig config = GeneratorConfig.builder()
                                                .objectOverride(String.class, () -> "root-string")
                                                .objectOverride(RootObjectConfigFixture.class, "name", () -> "field-value")
                                                .objectOverride(Integer.class, ctx -> 11)
                                                .objectOverride(RootObjectConfigFixture.class, "score", ctx -> 19)
                                                .objectExcludeField("password")
                                                .objectExcludeType(type -> type == LocalDate.class)
                                                .build();

        assertEquals("root-string", config.getObjectTypeOverride(String.class).orElseThrow().generate());
        assertEquals("field-value",
                     config.getObjectFieldOverride(RootObjectConfigFixture.class, "name").orElseThrow().generate());
        assertTrue(config.getObjectContextualTypeOverride(Integer.class).isPresent());
        assertTrue(config.getObjectContextualFieldOverride(RootObjectConfigFixture.class, "score").isPresent());

        Field password = RootObjectConfigFixture.class.getDeclaredField("password");
        Field createdAt = RootObjectConfigFixture.class.getDeclaredField("createdAt");
        Field name = RootObjectConfigFixture.class.getDeclaredField("name");
        assertTrue(config.shouldObjectExclude(password));
        assertTrue(config.shouldObjectExclude(createdAt));
        assertFalse(config.shouldObjectExclude(name));
    }

    @Test
    @DisplayName("legacy simple-name object field override key remains supported on the root config")
    void legacySimpleNameObjectFieldOverrideKeyStillWorks() throws Exception {
        GeneratorConfig.Builder builder = GeneratorConfig.builder();

        Field overridesField = GeneratorConfig.Builder.class.getDeclaredField("objectFieldOverrides");
        overridesField.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<String, Generator<?>> fieldOverrides = (Map<String, Generator<?>>) overridesField.get(builder);
        fieldOverrides.put("RootObjectConfigFixture.name", () -> "LEGACY");

        GeneratorConfig config = builder.build();
        assertEquals("LEGACY",
                     config.getObjectFieldOverride(RootObjectConfigFixture.class, "name").orElseThrow().generate());
    }

    @Test
    @DisplayName("advanced object override and exclusion methods validate null inputs")
    void advancedObjectOverrideAndExclusionMethodsValidateNullInputs() {
        assertThrows(NullPointerException.class,
                     () -> GeneratorConfig.builder().objectOverride((Class<String>) null, () -> "x"));
        assertThrows(NullPointerException.class,
                     () -> GeneratorConfig.builder().objectOverride(String.class, (Generator<String>) null));
        assertThrows(NullPointerException.class,
                     () -> GeneratorConfig.builder().objectOverride(RootObjectConfigFixture.class, null, () -> "x"));
        assertThrows(NullPointerException.class,
                     () -> GeneratorConfig.builder().objectOverride(String.class, (ContextualGenerator<String>) null));
        assertThrows(NullPointerException.class,
                     () -> GeneratorConfig.builder().objectExclude(null));
        assertThrows(NullPointerException.class,
                     () -> GeneratorConfig.builder().objectExcludeType((Class<?>) null));
        assertThrows(NullPointerException.class,
                     () -> GeneratorConfig.builder().objectExcludeType((java.util.function.Predicate<Class<?>>) null));
    }

    @Test
    @DisplayName("locale() stores the locale")
    void localeStored() {
        GeneratorConfig c = GeneratorConfig.builder().locale(Locale.GERMANY).build();
        assertEquals(Locale.GERMANY, c.getLocale());
    }

    @Test
    @DisplayName("locale(null) throws NullPointerException")
    void localeNullThrows() {
        assertThrows(NullPointerException.class,
                     () -> GeneratorConfig.builder().locale(null));
    }

    @Test
    @DisplayName("clock() stores the clock")
    void clockStored() {
        Clock clock = Clock.fixed(Instant.parse("2026-06-04T10:15:30Z"), ZoneId.of("UTC"));
        GeneratorConfig config = GeneratorConfig.builder().clock(clock).build();

        assertSame(clock, config.getClock());
    }

    @Test
    @DisplayName("clock(null) throws NullPointerException")
    void clockNullThrows() {
        assertThrows(NullPointerException.class,
                     () -> GeneratorConfig.builder().clock(null));
    }

    @Test
    @DisplayName("registryContext() stores the context")
    void registryContextStored() {
        DataRegistryContext context = DataRegistryContext.builder().isolated().build();
        GeneratorConfig config = GeneratorConfig.builder().registryContext(context).build();
        assertSame(context, config.getRegistryContext());
    }

    @Test
    @DisplayName("registryContext(null) throws NullPointerException")
    void registryContextNullThrows() {
        assertThrows(NullPointerException.class,
                     () -> GeneratorConfig.builder().registryContext(null));
    }

    @Test
    @DisplayName("objectSemanticRegistry() stores the registry")
    void objectSemanticRegistryStored() {
        SemanticFieldRegistry registry = SemanticFieldRegistry.defaults().toBuilder()
                                                              .alias("email", "contactMail")
                                                              .build();
        GeneratorConfig config = GeneratorConfig.builder().objectSemanticRegistry(registry).build();

        assertSame(registry, config.getObjectSemanticRegistry());
    }

    @Test
    @DisplayName("objectSemanticRegistry(null) throws NullPointerException")
    void objectSemanticRegistryNullThrows() {
        assertThrows(NullPointerException.class,
                     () -> GeneratorConfig.builder().objectSemanticRegistry(null));
    }

    @Test
    @DisplayName("generationFailureListener(null) throws")
    void generationFailureListenerNullThrows() {
        assertThrows(NullPointerException.class,
                     () -> GeneratorConfig.builder().generationFailureListener(null));
    }

    @Test
    @DisplayName("toBuilder() copies all fields and allows deriving new config")
    void toBuilderCopiesAndDerives() {
        DataRegistryContext context = DataRegistryContext.builder().isolated().build();
        Clock clock = Clock.fixed(Instant.parse("2026-06-04T10:15:30Z"), ZoneId.of("Europe/London"));
        SemanticFieldRegistry semanticRegistry = SemanticFieldRegistry.defaults().toBuilder()
                                                                         .alias("email", "contactMail")
                                                                         .build();
        GenerationFailureListener failureListener = diagnostic -> {};
        GeneratorConfig base = GeneratorConfig.builder()
                                              .seed("my-seed")
                                              .charset(StandardCharsets.UTF_8)
                                              .stringLength(8, 16)
                                              .collectionSize(2, 4)
                                              .objectMaxDepth(3)
                                              .objectPoolSize(2)
                                              .objectOverrideDefaultInitialization(true)
                                              .objectIgnoreErrors(true)
                                              .generationFailureListener(failureListener)
                                              .objectSemanticMode(ObjectGenerationSemanticMode.STRICT)
                                              .objectSemanticRegistry(semanticRegistry)
                                              .objectNullProbability(0.25)
                                              .objectOptionalEmptyProbability(0.5)
                                              .objectUniqueFields("email", "accountId")
                                              .objectUniquenessMaxAttempts(7)
                                              .objectDateRange(LocalDate.of(2022, 1, 1), LocalDate.of(2022, 12, 31))
                                              .objectOverride(String.class, () -> "root-string")
                                              .objectOverride(RootObjectConfigFixture.class, "name", () -> "field-value")
                                              .objectExcludeField("password")
                                              .locale(Locale.FRANCE)
                                              .clock(clock)
                                              .registryContext(context)
                                              .build();

        GeneratorConfig derived = base.toBuilder().locale(Locale.JAPAN).build();
        assertTrue(derived.getSeed().isPresent());
        assertEquals(-4581536756751041509L, derived.getSeed().getAsLong());
        assertEquals("my-seed", derived.getStringSeed().orElseThrow());
        assertEquals(StandardCharsets.UTF_8, derived.getCharset());
        assertEquals(8, derived.getMinStringLength());
        assertEquals(16, derived.getMaxStringLength());
        assertEquals(2, derived.getMinCollectionSize());
        assertEquals(4, derived.getMaxCollectionSize());
        assertEquals(3, derived.getObjectMaxDepth());
        assertEquals(2, derived.getObjectPoolSize());
        assertTrue(derived.isObjectOverrideDefaultInitialization());
        assertTrue(derived.isObjectIgnoreErrors());
        assertSame(failureListener, derived.getGenerationFailureListener());
        assertEquals(ObjectGenerationSemanticMode.STRICT, derived.getObjectSemanticMode());
        assertSame(semanticRegistry, derived.getObjectSemanticRegistry());
        assertEquals(0.25, derived.getObjectNullProbability());
        assertEquals(0.5, derived.getObjectOptionalEmptyProbability());
        assertEquals(Set.of("email", "accountid"), derived.getObjectUniqueFieldNames());
        assertEquals(7, derived.getObjectUniquenessMaxAttempts());
        assertEquals(LocalDate.of(2022, 1, 1), derived.getObjectDateMin());
        assertEquals(LocalDate.of(2022, 12, 31), derived.getObjectDateMax());
        assertEquals("root-string", derived.getObjectTypeOverride(String.class).orElseThrow().generate());
        assertEquals("field-value",
                     derived.getObjectFieldOverride(RootObjectConfigFixture.class, "name").orElseThrow().generate());
        assertEquals(Locale.JAPAN, derived.getLocale());
        assertSame(clock, derived.getClock());
        assertSame(context, derived.getRegistryContext());
        assertTrue(derived.getRandom().isEmpty());
    }

    @Test
    @DisplayName("locale() accepts various locales")
    void variousLocales() {
        GeneratorConfig japan = GeneratorConfig.builder().locale(Locale.JAPAN).build();
        assertEquals(Locale.JAPAN, japan.getLocale());

        GeneratorConfig custom = GeneratorConfig.builder()
                                                .locale(Locale.of("es", "MX"))
                                                .build();
        assertEquals("es", custom.getLocale().getLanguage());
        assertEquals("MX", custom.getLocale().getCountry());
    }

    static class RootObjectConfigFixture {

        String    name;
        String    password;
        Integer   score;
        LocalDate createdAt;
    }

    private record RandomSourceConfiguration(String name, Consumer<GeneratorConfig.Builder> configure) {
    }
}
