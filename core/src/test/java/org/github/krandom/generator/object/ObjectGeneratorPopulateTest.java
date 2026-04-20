/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.object;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("ObjectGenerator - populate existing")
class ObjectGeneratorPopulateTest {

    @Test
    @DisplayName("populate preserves initialized values when overwrite is disabled")
    void populatePreservesInitializedValuesWhenOverwriteDisabled() {
        ObjectGeneratorConfig config = ObjectGeneratorConfig.builder()
            .override(String.class, () -> "generated")
            .build();

        PopulateTarget target = new PopulateTarget();
        target.preset = "keep";

        new ObjectGenerator<>(PopulateTarget.class, config).populate(target);

        assertEquals("keep", target.preset);
        assertEquals("generated", target.blank);
    }

    @Test
    @DisplayName("populate overwrites initialized values when overwrite is enabled")
    void populateOverwritesInitializedValuesWhenOverwriteEnabled() {
        ObjectGeneratorConfig config = ObjectGeneratorConfig.builder()
            .overrideDefaultInitialization(true)
            .override(String.class, () -> "generated")
            .build();

        PopulateTarget target = new PopulateTarget();
        target.preset = "keep";

        new ObjectGenerator<>(PopulateTarget.class, config).populate(target);

        assertEquals("generated", target.preset);
        assertEquals("generated", target.blank);
    }

    @Test
    @DisplayName("populate rejects record instances")
    void populateRejectsRecordInstances() {
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> new ObjectGenerator<>(PopulateRecord.class).populate(new PopulateRecord("Ada")));

        assertEquals(
            "populate(existing) does not support record types: "
            + PopulateRecord.class.getName(),
            ex.getMessage());
    }

    @Test
    @DisplayName("populate rejects incompatible instances")
    @SuppressWarnings({ "rawtypes", "unchecked" })
    void populateRejectsIncompatibleInstances() {
        ObjectGenerator raw = new ObjectGenerator(PopulateTarget.class);

        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> raw.populate(new WrongPopulateType()));

        assertEquals(
            "instance must be assignable to " + PopulateTarget.class.getName()
            + ", got " + WrongPopulateType.class.getName(),
            ex.getMessage());
    }

    @Test
    @DisplayName("populate uses the pooled internal path when already scoped")
    void populateUsesPooledInternalPathWhenAlreadyScoped() {
        ObjectGeneratorConfig config = ObjectGeneratorConfig.builder()
            .override(String.class, () -> "generated")
            .build();

        PopulateTarget target = new PopulateTarget();
        ObjectGenerator<PopulateTarget> scoped = new ObjectGenerator<>(
            PopulateTarget.class,
            config,
            1,
            new ObjectPool(config.getObjectPoolSize()),
            123L,
            new UniqueFieldTracker());

        scoped.populate(target);

        assertEquals("generated", target.blank);
    }

    @Test
    @DisplayName("populate skips the fresh-scope branch when depth is zero but a pool already exists")
    void populateSkipsFreshScopeWhenDepthZeroAndPoolAlreadyExists() {
        ObjectGeneratorConfig config = ObjectGeneratorConfig.builder()
            .override(String.class, () -> "generated")
            .build();

        PopulateTarget target = new PopulateTarget();
        ObjectGenerator<PopulateTarget> scoped = new ObjectGenerator<>(
            PopulateTarget.class,
            config,
            0,
            new ObjectPool(config.getObjectPoolSize()),
            123L,
            new UniqueFieldTracker());

        scoped.populate(target);

        assertEquals("generated", target.blank);
    }

    static final class PopulateTarget {
        String preset;
        String blank;
    }

    static final class WrongPopulateType {
        String value;
    }

    record PopulateRecord(String name) {
    }
}
