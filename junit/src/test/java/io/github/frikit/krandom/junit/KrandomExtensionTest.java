/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.junit;

import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.Generators;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KrandomExtensionTest {

    @KrandomSeed(42L)
    @Test
    void pinnedNumericSeedIsInjectedAndMetaRegistersTheExtension(GeneratorConfig config) {
        assertEquals(42L, config.getSeed().getAsLong());
    }

    @KrandomSeed(text = "checkout-flow")
    @Test
    void pinnedTextSeedDerivesLikeGeneratorConfig(GeneratorConfig config) {
        assertEquals(GeneratorConfig.deriveSeed("checkout-flow"), config.getSeed().getAsLong());
    }

    @KrandomSeed(42L)
    @Test
    void builderInjectionCarriesThePinnedSeed(GeneratorConfig.Builder builder) {
        assertEquals(42L, builder.build().getSeed().getAsLong());
    }

    @KrandomSeed(1234L)
    @Test
    void sameSeedProducesSameData(GeneratorConfig first, GeneratorConfig second) {
        assertEquals(
                Generators.ofFullName(first).generate(),
                Generators.ofFullName(second).generate());
    }

    @ExtendWith(KrandomExtension.class)
    @Nested
    class Unpinned {

        @Test
        void unpinnedTestStillRunsWithAConcreteSeed(GeneratorConfig config) {
            assertTrue(config.getSeed().isPresent());
        }

        @Test
        void allInjectionsInOneTestShareTheSameSeed(GeneratorConfig first, GeneratorConfig.Builder second) {
            assertEquals(first.getSeed(), second.build().getSeed());
        }
    }

    @KrandomSeed(7L)
    @Nested
    class ClassLevelSeed {

        @Test
        void classLevelSeedAppliesToUnannotatedTests(GeneratorConfig config) {
            assertEquals(7L, config.getSeed().getAsLong());
        }

        @KrandomSeed(8L)
        @Test
        void methodLevelSeedOverridesClassLevelSeed(GeneratorConfig config) {
            assertEquals(8L, config.getSeed().getAsLong());
        }
    }
}
