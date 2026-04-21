/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator;

import org.github.krandom.generator.object.ObjectGenerationSemanticMode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

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
        assertTrue(c.getRandomFactory().isEmpty());
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
        assertNull(c.getObjectDateMin());
        assertNull(c.getObjectDateMax());
        assertEquals(ObjectGenerationSemanticMode.RELAXED, c.getObjectSemanticMode());
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
        assertSame(DataRegistryContext.globalDefault(), c.getRegistryContext());
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
    @DisplayName("randomFactory is used and receives configured seed")
    void randomFactoryUsed() {
        AtomicInteger calls = new AtomicInteger();
        GeneratorConfig c = GeneratorConfig.builder()
                                           .seed("phase2-seed")
                                           .randomFactory(() -> {
                                               calls.incrementAndGet();
                                               return new Random(1L);
                                           })
                                           .build();

        Random actual = c.createRandom();
        Random expected = new Random(c.getSeed().orElseThrow());
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
        GeneratorConfig c = GeneratorConfig.builder()
                                           .objectMaxDepth(3)
                                           .objectPoolSize(2)
                                           .objectOverrideDefaultInitialization(true)
                                           .objectIgnoreErrors(true)
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
    @DisplayName("toBuilder() copies all fields and allows deriving new config")
    void toBuilderCopiesAndDerives() {
        DataRegistryContext context = DataRegistryContext.builder().isolated().build();
        AtomicInteger calls = new AtomicInteger();
        GeneratorConfig base = GeneratorConfig.builder()
                                              .seed("my-seed")
                                              .charset(StandardCharsets.UTF_8)
                                              .stringLength(8, 16)
                                              .collectionSize(2, 4)
                                              .objectMaxDepth(3)
                                              .objectPoolSize(2)
                                              .objectOverrideDefaultInitialization(true)
                                              .objectIgnoreErrors(true)
                                              .objectSemanticMode(ObjectGenerationSemanticMode.STRICT)
                                              .objectNullProbability(0.25)
                                              .objectOptionalEmptyProbability(0.5)
                                              .objectUniqueFields("email", "accountId")
                                              .objectUniquenessMaxAttempts(7)
                                              .objectDateRange(LocalDate.of(2022, 1, 1), LocalDate.of(2022, 12, 31))
                                              .objectOverride(String.class, () -> "root-string")
                                              .objectOverride(RootObjectConfigFixture.class, "name", () -> "field-value")
                                              .objectExcludeField("password")
                                              .locale(Locale.FRANCE)
                                              .randomFactory(() -> {
                                                  calls.incrementAndGet();
                                                  return new Random(5L);
                                              })
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
        assertEquals(ObjectGenerationSemanticMode.STRICT, derived.getObjectSemanticMode());
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
        assertSame(context, derived.getRegistryContext());
        assertTrue(derived.getRandomFactory().isPresent());

        // Ensure copied factory remains active.
        derived.createRandom();
        assertEquals(1, calls.get());
    }

    @Test
    @DisplayName("locale() accepts various locales")
    void variousLocales() {
        GeneratorConfig japan = GeneratorConfig.builder().locale(Locale.JAPAN).build();
        assertEquals(Locale.JAPAN, japan.getLocale());

        GeneratorConfig custom = GeneratorConfig.builder()
                                                .locale(new Locale("es", "MX"))
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
}
