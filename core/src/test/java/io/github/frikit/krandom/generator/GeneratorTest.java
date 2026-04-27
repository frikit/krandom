/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Generator default methods")
class GeneratorTest {

    @Test
    @DisplayName("reseed(long) reseeds non-static Random fields across class hierarchy")
    void reseedLongReseedsHierarchyFields() {
        long staticSeed = 77L;
        Random staticExpected = new Random(staticSeed);
        ReseedableGenerator.STATIC_RANDOM.setSeed(staticSeed);
        assertEquals(staticExpected.nextInt(), ReseedableGenerator.STATIC_RANDOM.nextInt());

        ReseedableGenerator generator = new ReseedableGenerator();

        long seed = 123456789L;
        generator.reseed(seed);

        assertEquals(new Random(seed).nextInt(), generator.nextOwn());
        assertEquals(new Random(seed).nextInt(), generator.nextInherited());

        assertEquals(staticExpected.nextInt(), ReseedableGenerator.STATIC_RANDOM.nextInt());
    }

    @Test
    @DisplayName("reseed(String) derives deterministic seed")
    void reseedStringDelegatesToDerivedSeed() {
        String seedText = "default-reseed-seed";
        ReseedableGenerator generator = new ReseedableGenerator();

        generator.reseed(seedText);

        long derived = GeneratorConfig.deriveSeed(seedText);
        assertEquals(new Random(derived).nextInt(), generator.nextOwn());
        assertEquals(new Random(derived).nextInt(), generator.nextInherited());
    }

    @Test
    @DisplayName("reseed throws when generator has no Random fields")
    void reseedWithoutRandomFieldThrows() {
        UnsupportedOperationException ex = assertThrows(
            UnsupportedOperationException.class,
            () -> new NoRandomFieldGenerator().reseed(1L));
        assertTrue(ex.getMessage().contains(NoRandomFieldGenerator.class.getName()));
    }

    @Test
    @DisplayName("reseed default delegates to Seedable.reseed when implemented")
    void reseedDelegatesToSeedable() {
        SeedableGenerator generator = new SeedableGenerator();
        generator.invokeGeneratorDefault(987654321L);
        assertEquals(987654321L, generator.lastSeed);
        assertEquals(1, generator.reseedCalls);
    }

    @Test
    @DisplayName("reseed wraps reflective field-access failures")
    void reseedWrapsReflectiveAccessFailure() {
        IllegalStateException ex = assertThrows(
            IllegalStateException.class,
            () -> new ProxyBackedGenerator().reseed(1L));
        assertTrue(ex.getMessage().contains("h"));
    }

    private static class ParentGenerator {
        protected final Random inheritedRandom = new Random(11L);
        protected final String label = "no-random";
    }

    private static final class ReseedableGenerator extends ParentGenerator implements Generator<Integer> {
        private static final Random STATIC_RANDOM = new Random(33L);

        private final Random ownRandom = new Random(22L);
        private final Object nonRandom = new Object();

        @Override
        public Integer generate() {
            return ownRandom.nextInt();
        }

        int nextOwn() {
            return ownRandom.nextInt();
        }

        int nextInherited() {
            return inheritedRandom.nextInt();
        }
    }

    private static final class SeedableGenerator implements Generator<Integer>, Seedable {
        long lastSeed;
        int reseedCalls;

        @Override
        public Integer generate() {
            return 0;
        }

        @Override
        public void reseed(long seed) {
            lastSeed = seed;
            reseedCalls++;
        }

        void invokeGeneratorDefault(long seed) {
            Generator.super.reseed(seed);
        }
    }

    private static final class NoRandomFieldGenerator implements Generator<Integer> {
        @Override
        public Integer generate() {
            return 1;
        }
    }

    private static final class ProxyBackedGenerator extends Proxy implements Generator<Integer> {
        private ProxyBackedGenerator() {
            super((proxy, method, args) -> null);
        }

        @Override
        public Integer generate() {
            return 1;
        }
    }
}
