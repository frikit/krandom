/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.object;

import org.github.krandom.generator.GeneratorConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("ObjectGeneratorConfig")
class ObjectGeneratorConfigTest {

    @Test
    @DisplayName("defaults() returns config with default values")
    void defaultValues() {
        ObjectGeneratorConfig c = ObjectGeneratorConfig.defaults();
        assertEquals(ObjectGeneratorConfig.DEFAULT_MAX_DEPTH, c.getMaxDepth());
        assertEquals(ObjectGeneratorConfig.DEFAULT_OBJECT_POOL_SIZE, c.getObjectPoolSize());
        assertEquals(Locale.US, c.getGeneratorConfig().getLocale());
        assertFalse(c.isOverrideDefaultInitialization());
        assertFalse(c.isIgnoreErrors());
        assertEquals(ObjectGenerationSemanticMode.RELAXED, c.getSemanticMode());
        assertEquals(0.0, c.getNullProbability());
        assertEquals(0.0, c.getOptionalEmptyProbability());
        assertEquals(Set.of("email", "emailaddress", "username", "userhandle", "uuid", "guid", "id"),
                     c.getUniqueFieldNames());
        assertEquals(256, c.getUniquenessMaxAttempts());
        assertTrue(c.getTypeOverride(String.class).isEmpty());
        assertTrue(c.getFieldOverride(String.class, "value").isEmpty());
    }

    @Test
    @DisplayName("generatorConfig(...) stores the shared root config")
    void generatorConfigStored() {
        GeneratorConfig generatorConfig = GeneratorConfig.builder()
                                                         .locale(Locale.GERMANY)
                                                         .stringLength(8, 8)
                                                         .collectionSize(4, 4)
                                                         .objectMaxDepth(3)
                                                         .objectPoolSize(2)
                                                         .objectOverrideDefaultInitialization(true)
                                                         .objectIgnoreErrors(true)
                                                         .objectSemanticMode(ObjectGenerationSemanticMode.STRICT)
                                                         .objectNullProbability(0.25)
                                                         .objectOptionalEmptyProbability(0.5)
                                                         .objectUniqueFields("email", "accountId")
                                                         .objectUniquenessMaxAttempts(7)
                                                         .seed(42L)
                                                         .build();

        ObjectGeneratorConfig objectConfig = ObjectGeneratorConfig.builder()
                                                                 .generatorConfig(generatorConfig)
                                                                 .build();

        assertSame(generatorConfig, objectConfig.getGeneratorConfig());
        assertEquals(3, objectConfig.getMaxDepth());
        assertEquals(2, objectConfig.getObjectPoolSize());
        assertTrue(objectConfig.isOverrideDefaultInitialization());
        assertTrue(objectConfig.isIgnoreErrors());
        assertEquals(ObjectGenerationSemanticMode.STRICT, objectConfig.getSemanticMode());
        assertEquals(0.25, objectConfig.getNullProbability());
        assertEquals(0.5, objectConfig.getOptionalEmptyProbability());
        assertEquals(Set.of("email", "accountid"), objectConfig.getUniqueFieldNames());
        assertEquals(7, objectConfig.getUniquenessMaxAttempts());
    }

    @Test
    @DisplayName("explicit object settings win over inherited GeneratorConfig defaults")
    void explicitObjectSettingsOverrideInheritedRootDefaults() {
        GeneratorConfig generatorConfig = GeneratorConfig.builder()
                                                         .objectMaxDepth(2)
                                                         .objectPoolSize(1)
                                                         .objectOverrideDefaultInitialization(true)
                                                         .objectIgnoreErrors(true)
                                                         .objectSemanticMode(ObjectGenerationSemanticMode.STRICT)
                                                         .objectNullProbability(0.4)
                                                         .objectOptionalEmptyProbability(0.6)
                                                         .objectUniqueFields("email")
                                                         .objectUniquenessMaxAttempts(9)
                                                         .build();

        ObjectGeneratorConfig objectConfig = ObjectGeneratorConfig.builder()
                                                                 .maxDepth(4)
                                                                 .objectPoolSize(7)
                                                                 .overrideDefaultInitialization(false)
                                                                 .ignoreErrors(false)
                                                                 .semanticMode(ObjectGenerationSemanticMode.STRUCTURAL_ONLY)
                                                                 .nullProbability(0.1)
                                                                 .optionalEmptyProbability(0.2)
                                                                 .uniqueFields("username")
                                                                 .uniquenessMaxAttempts(3)
                                                                 .generatorConfig(generatorConfig)
                                                                 .build();

        assertEquals(4, objectConfig.getMaxDepth());
        assertEquals(7, objectConfig.getObjectPoolSize());
        assertFalse(objectConfig.isOverrideDefaultInitialization());
        assertFalse(objectConfig.isIgnoreErrors());
        assertEquals(ObjectGenerationSemanticMode.STRUCTURAL_ONLY, objectConfig.getSemanticMode());
        assertEquals(0.1, objectConfig.getNullProbability());
        assertEquals(0.2, objectConfig.getOptionalEmptyProbability());
        assertEquals(Set.of("username"), objectConfig.getUniqueFieldNames());
        assertEquals(3, objectConfig.getUniquenessMaxAttempts());
    }

    @Test
    @DisplayName("maxDepth(3) stores the value")
    void maxDepthStored() {
        ObjectGeneratorConfig c = ObjectGeneratorConfig.builder().maxDepth(3).build();
        assertEquals(3, c.getMaxDepth());
    }

    @Test
    @DisplayName("objectPoolSize(3) stores the value")
    void objectPoolSizeStored() {
        ObjectGeneratorConfig c = ObjectGeneratorConfig.builder().objectPoolSize(3).build();
        assertEquals(3, c.getObjectPoolSize());
    }

    @Test
    @DisplayName("maxDepth(0) throws IllegalArgumentException")
    void maxDepthZeroThrows() {
        assertThrows(IllegalArgumentException.class,
                     () -> ObjectGeneratorConfig.builder().maxDepth(0));
    }

    @Test
    @DisplayName("objectPoolSize(-1) throws IllegalArgumentException")
    void objectPoolSizeNegativeThrows() {
        assertThrows(IllegalArgumentException.class,
                     () -> ObjectGeneratorConfig.builder().objectPoolSize(-1));
    }

    @Test
    @DisplayName("ignoreErrors(true) stores the flag")
    void ignoreErrorsStored() {
        ObjectGeneratorConfig c = ObjectGeneratorConfig.builder().ignoreErrors(true).build();
        assertTrue(c.isIgnoreErrors());
    }

    @Test
    @DisplayName("overrideDefaultInitialization(false) stores the flag")
    void overrideDefaultInitializationStored() {
        ObjectGeneratorConfig c = ObjectGeneratorConfig.builder()
                                                       .overrideDefaultInitialization(false)
                                                       .build();
        assertFalse(c.isOverrideDefaultInitialization());
    }

    @Test
    @DisplayName("type-level override is stored and retrievable")
    void typeOverrideStored() {
        ObjectGeneratorConfig c = ObjectGeneratorConfig.builder()
                                                       .override(String.class, () -> "fixed")
                                                       .build();
        assertTrue(c.getTypeOverride(String.class).isPresent());
        assertEquals("fixed", c.getTypeOverride(String.class).get().generate());
    }

    @Test
    @DisplayName("field-level override is stored and retrievable")
    void fieldOverrideStored() {
        ObjectGeneratorConfig c = ObjectGeneratorConfig.builder()
                                                       .override(String.class, "value", () -> "hello")
                                                       .build();
        assertTrue(c.getFieldOverride(String.class, "value").isPresent());
        assertEquals("hello", c.getFieldOverride(String.class, "value").get().generate());
    }

    @Test
    @DisplayName("override(null type) throws NullPointerException")
    void typeOverrideNullTypeThrows() {
        assertThrows(NullPointerException.class,
                     () -> ObjectGeneratorConfig.builder().override(null, () -> "x"));
    }

    @Test
    @DisplayName("override(null field name) throws NullPointerException")
    void fieldOverrideNullFieldThrows() {
        assertThrows(NullPointerException.class,
                     () -> ObjectGeneratorConfig.builder().override(String.class, null, () -> "x"));
    }

    @Test
    @DisplayName("excludeType(null predicate) throws NullPointerException")
    void excludeTypeNullPredicateThrows() {
        assertThrows(NullPointerException.class,
                     () -> ObjectGeneratorConfig.builder().excludeType((java.util.function.Predicate<Class<?>>) null));
    }

    @Test
    @DisplayName("generatorConfig(null) throws NullPointerException")
    void generatorConfigNullThrows() {
        assertThrows(NullPointerException.class,
                     () -> ObjectGeneratorConfig.builder().generatorConfig(null));
    }

    @Test
    @DisplayName("semantic and nullability controls are stored and validated")
    void semanticAndNullabilityControlsStored() {
        ObjectGeneratorConfig config = ObjectGeneratorConfig.builder()
                                                            .semanticMode(ObjectGenerationSemanticMode.STRICT)
                                                            .nullProbability(0.25)
                                                            .optionalEmptyProbability(0.5)
                                                            .uniqueFields("Email", "account_id")
                                                            .uniquenessMaxAttempts(5)
                                                            .build();
        assertEquals(ObjectGenerationSemanticMode.STRICT, config.getSemanticMode());
        assertEquals(0.25, config.getNullProbability());
        assertEquals(0.5, config.getOptionalEmptyProbability());
        assertEquals(Set.of("email", "accountid"), config.getUniqueFieldNames());
        assertEquals(5, config.getUniquenessMaxAttempts());
        assertThrows(IllegalArgumentException.class,
                     () -> ObjectGeneratorConfig.builder().nullProbability(1.1));
        assertThrows(IllegalArgumentException.class,
                     () -> ObjectGeneratorConfig.builder().nullProbability(Double.NaN));
        assertThrows(IllegalArgumentException.class,
                     () -> ObjectGeneratorConfig.builder().optionalEmptyProbability(-0.1));
        assertThrows(IllegalArgumentException.class,
                     () -> ObjectGeneratorConfig.builder().uniquenessMaxAttempts(0));
        assertThrows(IllegalArgumentException.class,
                     () -> ObjectGeneratorConfig.builder().optionalEmptyProbability(Double.NaN));
    }

    @Test
    @DisplayName("uniqueField normalizes one field name and rejects non-alphanumeric input")
    void uniqueFieldNormalizesSingleFieldAndRejectsNonAlphanumericInput() {
        ObjectGeneratorConfig config = ObjectGeneratorConfig.builder()
                                                            .uniqueField("Account_ID")
                                                            .build();

        assertTrue(config.getUniqueFieldNames().contains("accountid"));
        assertThrows(IllegalArgumentException.class,
                     () -> ObjectGeneratorConfig.builder().uniqueField("___"));
    }

    @Test
    @DisplayName("toBuilder preserves unset date range state")
    void toBuilderPreservesUnsetDateRangeState() {
        ObjectGeneratorConfig config = ObjectGeneratorConfig.defaults()
                                                            .toBuilder()
                                                            .maxDepth(2)
                                                            .build();

        assertNull(config.getDateMin());
        assertNull(config.getDateMax());
        assertEquals(2, config.getMaxDepth());
    }

    @Test
    @DisplayName("toBuilder preserves explicit date range state")
    void toBuilderPreservesExplicitDateRangeState() {
        LocalDate min = LocalDate.of(2024, 1, 1);
        LocalDate max = LocalDate.of(2024, 12, 31);

        ObjectGeneratorConfig config = ObjectGeneratorConfig.builder()
                                                            .dateRange(min, max)
                                                            .build()
                                                            .toBuilder()
                                                            .build();

        assertEquals(min, config.getDateMin());
        assertEquals(max, config.getDateMax());
    }

    @Test
    @DisplayName("toBuilder treats one-sided reflected date state as explicit")
    void toBuilderTreatsOneSidedReflectedDateStateAsExplicit() throws Exception {
        LocalDate reflectedMax = LocalDate.of(2025, 6, 30);
        GeneratorConfig inheritedGeneratorConfig = GeneratorConfig.builder()
                                                                 .objectDateRange(LocalDate.of(1999, 1, 1), LocalDate.of(1999, 12, 31))
                                                                 .build();
        ObjectGeneratorConfig source = ObjectGeneratorConfig.defaults();

        Field dateMaxField = ObjectGeneratorConfig.class.getDeclaredField("dateMax");
        dateMaxField.setAccessible(true);
        dateMaxField.set(source, reflectedMax);

        Constructor<ObjectGeneratorConfig.Builder> copyConstructor =
            ObjectGeneratorConfig.Builder.class.getDeclaredConstructor(ObjectGeneratorConfig.class);
        copyConstructor.setAccessible(true);

        ObjectGeneratorConfig copy = copyConstructor.newInstance(source)
                                                    .generatorConfig(inheritedGeneratorConfig)
                                                    .build();

        assertNull(copy.getDateMin());
        assertEquals(reflectedMax, copy.getDateMax());
    }

    @Test
    @DisplayName("field overrides for classes with same simple name do not collide")
    void fieldOverridesDoNotCollideAcrossPackages() {
        ObjectGeneratorConfig config = ObjectGeneratorConfig.builder()
                                                            .override(org.github.krandom.generator.object.collision.left.SameNameHolder.class, "value", () -> "LEFT")
                                                            .override(org.github.krandom.generator.object.collision.right.SameNameHolder.class, "value", () -> "RIGHT")
                                                            .build();

        org.github.krandom.generator.object.collision.left.SameNameHolder left =
            new ObjectGenerator<>(org.github.krandom.generator.object.collision.left.SameNameHolder.class, config)
                .generate();
        org.github.krandom.generator.object.collision.right.SameNameHolder right =
            new ObjectGenerator<>(org.github.krandom.generator.object.collision.right.SameNameHolder.class, config)
                .generate();

        assertEquals("LEFT", left.getValue());
        assertEquals("RIGHT", right.getValue());
    }

    @Test
    @DisplayName("legacy simple-name field override key remains supported")
    void legacySimpleNameFieldOverrideKeyStillWorks() throws Exception {
        ObjectGeneratorConfig.Builder builder = ObjectGeneratorConfig.builder();

        Field overridesField = ObjectGeneratorConfig.Builder.class.getDeclaredField("fieldOverrides");
        overridesField.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<String, org.github.krandom.generator.Generator<?>> fieldOverrides =
            (Map<String, org.github.krandom.generator.Generator<?>>) overridesField.get(builder);
        fieldOverrides.put("SameNameHolder.value", () -> "LEGACY");

        ObjectGeneratorConfig config = builder.build();
        org.github.krandom.generator.object.collision.left.SameNameHolder value =
            new ObjectGenerator<>(org.github.krandom.generator.object.collision.left.SameNameHolder.class, config)
                .generate();

        assertEquals("LEGACY", value.getValue());
    }
}
