/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator;

import io.github.frikit.krandom.generator.base.IntGenerator;
import io.github.frikit.krandom.generator.base.LongGenerator;
import io.github.frikit.krandom.generator.base.DoubleGenerator;
import io.github.frikit.krandom.generator.base.FloatGenerator;
import io.github.frikit.krandom.generator.base.StringGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SeedableTest {

    @Test
    @DisplayName("AbstractBoundedGenerator implements Seedable")
    void boundedGeneratorIsSeedable() {
        IntGenerator gen = new IntGenerator(1, 100, 42L);
        assertInstanceOf(Seedable.class, gen);
    }

    @Test
    @DisplayName("reseed produces deterministic sequence")
    void reseedProducesDeterministicSequence() {
        IntGenerator gen = new IntGenerator(1, 1000, 42L);
        int first = gen.generate();
        int second = gen.generate();

        gen.reseed(42L);
        assertEquals(first, gen.generate());
        assertEquals(second, gen.generate());
    }

    @Test
    @DisplayName("Seedable.reseed works on multiple generator types")
    void seedableWorksOnMultipleTypes() {
        LongGenerator longGen = new LongGenerator(1L, 1000L, 42L);
        long longVal = longGen.generate();
        longGen.reseed(42L);
        assertEquals(longVal, longGen.generate());

        DoubleGenerator doubleGen = new DoubleGenerator(0.0, 1.0, 42L);
        double doubleVal = doubleGen.generate();
        doubleGen.reseed(42L);
        assertEquals(doubleVal, doubleGen.generate());

        FloatGenerator floatGen = new FloatGenerator(0f, 1f, 42L);
        float floatVal = floatGen.generate();
        floatGen.reseed(42L);
        assertEquals(floatVal, floatGen.generate());
    }

    @Test
    @DisplayName("Generator.reseed dispatches to Seedable implementation")
    void generatorReseedDispatchesToSeedable() {
        IntGenerator gen = new IntGenerator(1, 1000, 42L);
        int first = gen.generate();

        // Call through Generator.reseed which should dispatch to Seedable
        Generator<Integer> asGenerator = gen;
        asGenerator.reseed(42L);
        assertEquals(first, gen.generate());
    }

    @Test
    @DisplayName("Generator.reseed falls back to reflection for non-Seedable generators")
    void generatorReseedReflectionFallback() {
        // StringGenerator uses reflection-based reseed (not Seedable)
        // Reflection path should not throw
        StringGenerator gen = StringGenerator.builder().seed(42L).build();
        assertDoesNotThrow(() -> gen.reseed(42L));
        assertNotNull(gen.generate());
    }

    @Test
    @DisplayName("reseed on non-seeded generator works via Random downcast")
    void reseedOnSecureRandomThrows() {
        IntGenerator gen = new IntGenerator(); // uses SecureRandom
        // SecureRandom extends Random, so setSeed should work
        assertDoesNotThrow(() -> gen.reseed(42L));
    }
}
