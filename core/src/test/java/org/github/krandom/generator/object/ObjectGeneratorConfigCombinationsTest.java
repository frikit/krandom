/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.object;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@DisplayName("ObjectGeneratorConfig — parameter combinations")
class ObjectGeneratorConfigCombinationsTest {

    @Test
    @DisplayName("combined settings interact correctly in one config")
    void combinedSettingsWorkTogether() {
        LocalDate min = LocalDate.of(2022, 1, 1);
        LocalDate max = LocalDate.of(2022, 12, 31);

        ObjectGeneratorConfig config = ObjectGeneratorConfig.builder()
                                                            .maxDepth(2)
                                                            .objectPoolSize(1)
                                                            .overrideDefaultInitialization(false)
                                                            .ignoreErrors(true)
                                                            .dateRange(min, max)
                                                            .override(String.class, () -> "GEN")
                                                            .excludeType(TypePredicates.inPackage("java.util.concurrent.atomic"))
                                                            .build();

        CombinedConfigTarget value = new ObjectGenerator<>(CombinedConfigTarget.class, config).generate();
        assertEquals("KEEP", value.getPreset(), "preset value should be preserved");
        assertEquals("GEN", value.getGenerated(), "default-initialized field should still be generated");
        assertNull(value.getCounter(), "atomic field should be excluded by type predicate");
        assertNotNull(value.getCreatedAt(), "date should still be generated under combined config");

        LocalDate created = value.getCreatedAt().toInstant().atZone(ZoneOffset.UTC).toLocalDate();
        assertFalse(created.isBefore(min));
        assertFalse(created.isAfter(max));
    }

    @Test
    @DisplayName("overrideDefaultInitialization(true) with other settings overwrites preset values")
    void overrideEnabledWithOtherSettingsOverwritesPreset() {
        ObjectGeneratorConfig config = ObjectGeneratorConfig.builder()
                                                            .objectPoolSize(2)
                                                            .overrideDefaultInitialization(true)
                                                            .override(String.class, () -> "OVERRIDE")
                                                            .build();

        CombinedConfigTarget value = new ObjectGenerator<>(CombinedConfigTarget.class, config).generate();
        assertEquals("OVERRIDE", value.getPreset());
        assertEquals("OVERRIDE", value.getGenerated());
    }


    static class CombinedConfigTarget {

        String         preset = "KEEP";
        String         generated;
        java.util.Date createdAt;
        AtomicInteger  counter;

        String getPreset() {
            return preset;
        }

        String getGenerated() {
            return generated;
        }

        java.util.Date getCreatedAt() {
            return createdAt;
        }

        AtomicInteger getCounter() {
            return counter;
        }
    }
}
