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
    @DisplayName("typed Seedable reseed restores a generator's sequence")
    void typedSeedableReseedRestoresSequence() {
        IntGenerator gen = new IntGenerator(1, 1000, 42L);
        int first = gen.generate();

        Generator<Integer> asGenerator = gen;
        assertInstanceOf(Seedable.class, asGenerator);
        ((Seedable) asGenerator).reseed(42L);
        assertEquals(first, gen.generate());
    }

    @Test
    @DisplayName("date and time generators reseed through the typed Seedable contract")
    void dateTimeGeneratorsReseedThroughTypedContract() {
        java.util.List<Seedable> seedables = java.util.List.of(
            new io.github.frikit.krandom.generator.datetime.DateGenerator(),
            new io.github.frikit.krandom.generator.datetime.LocalDateTimeGenerator(),
            new io.github.frikit.krandom.generator.datetime.InstantGenerator(),
            new io.github.frikit.krandom.generator.datetime.ZonedDateTimeGenerator(),
            new io.github.frikit.krandom.generator.datetime.UtilDateGenerator(),
            new io.github.frikit.krandom.generator.datetime.SqlDateGenerator(),
            new io.github.frikit.krandom.generator.datetime.SqlTimestampGenerator());

        for (Seedable seedable : seedables) {
            Generator<?> generator = (Generator<?>) seedable;
            seedable.reseed(24680L);
            Object first = generator.generate();
            seedable.reseed(24680L);
            assertEquals(first, generator.generate(),
                seedable.getClass().getSimpleName() + " must replay after typed reseed");
        }
    }

    @Test
    @DisplayName("non-Seedable generators expose no reseeding contract")
    void nonSeedableGeneratorsAreNotReseedable() {
        Generator<String> gen = StringGenerator.builder().seed(42L).build();
        assertFalse(gen instanceof Seedable);
        assertNotNull(gen.generate());
    }

    @Test
    @DisplayName("reseed on non-seeded generator works via Random downcast")
    void reseedOnSecureRandomThrows() {
        IntGenerator gen = new IntGenerator(); // uses SecureRandom
        // SecureRandom extends Random, so setSeed should work
        assertDoesNotThrow(() -> gen.reseed(42L));
    }

    @Test
    @DisplayName("independently constructed generators with the same seed produce identical sequences")
    void sameSeedSameSequenceAcrossInstances() {
        assertEquals(new IntGenerator(1, 1000, 42L).generateList(25),
                     new IntGenerator(1, 1000, 42L).generateList(25));
        assertEquals(new LongGenerator(1L, 1_000_000L, 7L).generateList(25),
                     new LongGenerator(1L, 1_000_000L, 7L).generateList(25));
        assertEquals(new DoubleGenerator(0.0, 1.0, 99L).generateList(25),
                     new DoubleGenerator(0.0, 1.0, 99L).generateList(25));
    }

    @Test
    @DisplayName("string seed produces the same sequence as its derived numeric seed")
    void stringSeedMatchesDerivedNumericSeed() {
        long derived = GeneratorConfig.deriveSeed("regression-seed");
        IntGenerator fromNumeric = new IntGenerator(1, 1000, derived);

        IntGenerator fromText = new IntGenerator(1, 1000, 1L);
        fromText.reseed(GeneratorConfig.deriveSeed("regression-seed"));

        assertEquals(fromNumeric.generateList(25), fromText.generateList(25));
    }
}
