/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.junit;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.frikit.krandom.generator.GenerationRecipe;
import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.Krandom;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.platform.testkit.engine.EngineExecutionResults;

/**
 * Pins the values that {@code @BeforeAll}, a {@code @MethodSource} factory and the invocations of
 * its parameterized test receive for each way of fixing their seeds, so that changes to the failure
 * output and its warnings provably leave every seeded value and every replay as it was.
 */
class KrandomExtensionFactoryPinEngineTest {

    @Test
    void annotationsPinTheFactoryThroughTheClassAndTheInvocationsThroughTheMethod() {
        Recorded recorded = Recorded.of(EngineTestSupport.run(PinnedFixture.class), PinnedFixture.VALUES);

        assertEquals(List.of(1540467787, -1512221895), recorded.values());
    }

    @Test
    void aMethodLevelSeedPinsTheInvocationsButNotTheFactory() {
        MethodPinnedFixture.VALUES.clear();
        EngineTestSupport.run(MethodPinnedFixture.class);

        assertEquals(2, MethodPinnedFixture.VALUES.size());
        assertEquals("invocation -1512221895", MethodPinnedFixture.VALUES.get(1));
    }

    @Test
    void aRecipeOverrideSeedsTheClassScopedConfigurationAndTheInvocations() {
        Recorded recorded = Recorded.of(
                EngineTestSupport.run(UnpinnedFixture.class, Map.of("krandom.junit.recipe", recipe(42L))),
                UnpinnedFixture.VALUES);

        assertEquals(List.of(349977663, 349977663), recorded.values());
    }

    @Test
    void aSeedOverrideSeedsTheClassScopedConfigurationAndTheInvocations() {
        Recorded recorded = Recorded.of(
                EngineTestSupport.run(UnpinnedFixture.class, Map.of("krandom.junit.seed", "42")),
                UnpinnedFixture.VALUES);

        assertEquals(List.of(349977663, 349977663), recorded.values());
    }

    @Test
    void aClassRecipeSeedsTheClassScopedConfigurationBesideTheRecipeOverride() {
        Recorded recorded = Recorded.of(
                EngineTestSupport.run(
                        UnpinnedFixture.class,
                        Map.of(
                                "krandom.junit.class-recipe",
                                UnpinnedFixture.class.getCanonicalName() + "=" + recipe(5L),
                                "krandom.junit.recipe",
                                recipe(42L))),
                UnpinnedFixture.VALUES);

        assertEquals(List.of(538574320, 349977663), recorded.values());
    }

    private static String recipe(long seed) {
        GenerationRecipe recipe = GeneratorConfig.builder()
                .seed(seed)
                .build()
                .getGenerationRecipe()
                .orElseThrow();
        return "base64:"
                + Base64.getUrlEncoder()
                        .withoutPadding()
                        .encodeToString(recipe.serialize().getBytes(StandardCharsets.UTF_8));
    }

    /** The values that one run recorded, in the order {@code @BeforeAll}, factory, invocation. */
    private record Recorded(List<String> raw) {

        static Recorded of(EngineExecutionResults results, List<String> values) {
            results.testEvents().assertStatistics(stats -> stats.failed(1));
            Recorded recorded = new Recorded(List.copyOf(values));
            values.clear();
            return recorded;
        }

        /** The {@code @BeforeAll} draw, which is also the factory's first draw, and the invocation's draw. */
        List<Integer> values() {
            assertEquals(3, raw.size(), raw.toString());
            int beforeAll = Integer.parseInt(raw.get(0).substring("beforeAll ".length()));
            int factory = Integer.parseInt(raw.get(1).substring("factory ".length()));
            assertEquals(beforeAll, factory, "a factory session starts the class-level streams again");
            return List.of(factory, Integer.parseInt(raw.get(2).substring("invocation ".length())));
        }
    }

    @Disabled("fixture for KrandomExtensionFactoryPinEngineTest")
    @KrandomSeed(7)
    static class PinnedFixture {

        static final List<String> VALUES = new CopyOnWriteArrayList<>();

        @BeforeAll
        static void setUp(Krandom fake) {
            VALUES.add("beforeAll " + EngineTestSupport.draw(fake));
        }

        static Stream<Integer> values(Krandom fake) {
            int value = EngineTestSupport.draw(fake);
            VALUES.add("factory " + value);
            return Stream.of(value);
        }

        @KrandomSeed(11)
        @ParameterizedTest
        @MethodSource("values")
        void boom(int value, Krandom fake) {
            VALUES.add("invocation " + EngineTestSupport.draw(fake));
            throw new AssertionError("intentional fixture failure");
        }
    }

    @Disabled("fixture for KrandomExtensionFactoryPinEngineTest")
    @ExtendWith(KrandomExtension.class)
    static class MethodPinnedFixture {

        static final List<String> VALUES = new CopyOnWriteArrayList<>();

        static Stream<Integer> values(Krandom fake) {
            int value = EngineTestSupport.draw(fake);
            VALUES.add("factory " + value);
            return Stream.of(value);
        }

        @KrandomSeed(11)
        @ParameterizedTest
        @MethodSource("values")
        void boom(int value, Krandom fake) {
            VALUES.add("invocation " + EngineTestSupport.draw(fake));
            throw new AssertionError("intentional fixture failure");
        }
    }

    @Disabled("fixture for KrandomExtensionFactoryPinEngineTest")
    @ExtendWith(KrandomExtension.class)
    static class UnpinnedFixture {

        static final List<String> VALUES = new CopyOnWriteArrayList<>();

        @BeforeAll
        static void setUp(Krandom fake) {
            VALUES.add("beforeAll " + EngineTestSupport.draw(fake));
        }

        static Stream<Integer> values(Krandom fake) {
            int value = EngineTestSupport.draw(fake);
            VALUES.add("factory " + value);
            return Stream.of(value);
        }

        @ParameterizedTest
        @MethodSource("values")
        void boom(int value, Krandom fake) {
            VALUES.add("invocation " + EngineTestSupport.draw(fake));
            throw new AssertionError("intentional fixture failure");
        }
    }
}
