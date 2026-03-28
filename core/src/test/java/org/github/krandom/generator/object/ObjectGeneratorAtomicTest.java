/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.object;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DisplayName("ObjectGenerator — atomic types")
class ObjectGeneratorAtomicTest {

    @Test
    @DisplayName("AtomicInteger and AtomicLong fields are populated")
    void atomicsArePopulated() {
        AtomicHolder holder = new ObjectGenerator<>(AtomicHolder.class).generate();
        assertNotNull(holder.getAtomicInteger());
        assertNotNull(holder.getAtomicLong());
    }

    @Test
    @DisplayName("type overrides apply to AtomicInteger and AtomicLong")
    void atomicTypeOverridesApply() {
        ObjectGeneratorConfig config = ObjectGeneratorConfig.builder()
                                                            .override(AtomicInteger.class, () -> new AtomicInteger(123))
                                                            .override(AtomicLong.class, () -> new AtomicLong(456L))
                                                            .build();
        AtomicHolder holder = new ObjectGenerator<>(AtomicHolder.class, config).generate();
        assertEquals(123, holder.getAtomicInteger().get());
        assertEquals(456L, holder.getAtomicLong().get());
    }


    static class AtomicHolder {

        private AtomicInteger atomicInteger;
        private AtomicLong    atomicLong;

        AtomicInteger getAtomicInteger() {
            return atomicInteger;
        }

        AtomicLong getAtomicLong() {
            return atomicLong;
        }
    }
}
