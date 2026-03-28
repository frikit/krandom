/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.object;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
        assertFalse(c.isOverrideDefaultInitialization());
        assertFalse(c.isIgnoreErrors());
        assertTrue(c.getTypeOverride(String.class).isEmpty());
        assertTrue(c.getFieldOverride(String.class, "value").isEmpty());
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
