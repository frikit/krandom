/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.junit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.Krandom;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Verifies the warnings printed when a replay cannot reproduce what a {@code @MethodSource} factory
 * generated: a recipe replayed without the class recipe that the failure output printed beside it,
 * and a {@code @KrandomSeed} on a parameterized test whose factory takes an unpinned class-scoped
 * configuration.
 */
class KrandomExtensionFactoryWarningEngineTest {

    private static final Pattern GRADLE_CLASS_REPLAY = Pattern.compile(
            "KRANDOM_JUNIT_CLASS_RECIPE='([^']+)' KRANDOM_JUNIT_RECIPE=(base64:[A-Za-z0-9_-]+) \\./gradlew test");
    private static final String RECIPE_ONLY_WARNING = "krandom: krandom.junit.recipe replays the class-scoped"
            + " configuration of 'FactoryFixture' (@BeforeAll/@AfterAll, PER_CLASS constructor and @MethodSource"
            + " factory parameters) as well, so @MethodSource arguments and other data created there come from"
            + " that recipe and may differ from the failing run. To replay them, also set"
            + " KRANDOM_JUNIT_CLASS_RECIPE (Maven: -Dkrandom.junit.class-recipe) to the value that the failure"
            + " output printed.";
    private static final String METHOD_SEED_WARNING = "krandom: @KrandomSeed on 'boom(int, GeneratorConfig)'"
            + " pins its invocations, not the arguments of its @MethodSource factory, which takes the unpinned"
            + " class-scoped configuration of 'MethodPinnedFixture'. To reproduce the arguments of a failing run,"
            + " also annotate 'MethodPinnedFixture' with the class-scoped seed that the failure output printed, or"
            + " replay with the printed commands.";

    @Test
    void aRecipeReplayedWithoutItsClassRecipeWarnsThatTheFactoryArgumentsMayDiffer() {
        FactoryFixture.VALUES.clear();
        EngineTestSupport.Captured first = EngineTestSupport.runCapturingStderr(FactoryFixture.class);
        Matcher printed = GRADLE_CLASS_REPLAY.matcher(first.stderr());
        assertTrue(printed.find(), first.stderr());

        EngineTestSupport.Captured replay = EngineTestSupport.runCapturingStderr(
                FactoryFixture.class, Map.of("krandom.junit.recipe", printed.group(2)));

        replay.results().testEvents().assertStatistics(stats -> stats.failed(1));
        assertNotEquals(
                FactoryFixture.VALUES.get(0),
                FactoryFixture.VALUES.get(1),
                "without the class recipe the factory takes the test's recipe");
        assertEquals(1, occurrences(replay.stderr(), RECIPE_ONLY_WARNING), replay.stderr());
        assertFalse(first.stderr().contains("replays the class-scoped configuration"), first.stderr());
    }

    @Test
    void theRecipeOverrideSourceIsNamedInTheWarning() {
        EngineTestSupport.Captured run = EngineTestSupport.runCapturingStderr(EnvironmentRecipeFixture.class);

        run.results().testEvents().assertStatistics(stats -> stats.succeeded(1));
        assertTrue(
                run.stderr()
                        .contains("krandom: KRANDOM_JUNIT_RECIPE replays the class-scoped configuration of"
                                + " 'EnvironmentRecipeFixture'"),
                run.stderr());
    }

    @Test
    void thePrintedCommandsReplayTheFactoryWithoutAWarning() {
        FactoryFixture.VALUES.clear();
        EngineTestSupport.Captured first = EngineTestSupport.runCapturingStderr(FactoryFixture.class);
        Matcher printed = GRADLE_CLASS_REPLAY.matcher(first.stderr());
        assertTrue(printed.find(), first.stderr());

        EngineTestSupport.Captured replay = EngineTestSupport.runCapturingStderr(
                FactoryFixture.class,
                Map.of("krandom.junit.class-recipe", printed.group(1), "krandom.junit.recipe", printed.group(2)));

        assertEquals(FactoryFixture.VALUES.get(0), FactoryFixture.VALUES.get(1));
        assertFalse(replay.stderr().contains("replays the class-scoped configuration"), replay.stderr());
    }

    @Test
    void aSeedOverrideDoesNotWarnBecauseABuildMayKeepItSet() {
        EngineTestSupport.Captured run =
                EngineTestSupport.runCapturingStderr(FactoryFixture.class, Map.of("krandom.junit.seed", "42"));

        run.results().testEvents().assertStatistics(stats -> stats.failed(1));
        assertFalse(run.stderr().contains("replays the class-scoped configuration"), run.stderr());
    }

    @Test
    void aRecipeOverrideWarnsOnceForEachClassThatCreatesClassScopedData() {
        EngineTestSupport.Captured run = EngineTestSupport.runCapturingStderr(
                NestedFactoryFixture.class, Map.of("krandom.junit.recipe", "base64:" + recipeOf(42L)));

        run.results().testEvents().assertStatistics(stats -> stats.succeeded(2));
        assertEquals(1, occurrences(run.stderr(), "class-scoped configuration of 'NestedFactoryFixture'"));
        assertEquals(1, occurrences(run.stderr(), "class-scoped configuration of 'Inner'"));
    }

    @Test
    void aSeedOnlyOnTheMethodWarnsOnceThatTheFactoryArgumentsAreNotPinned() {
        EngineTestSupport.Captured run = EngineTestSupport.runCapturingStderr(MethodPinnedFixture.class);

        run.results().testEvents().assertStatistics(stats -> stats.succeeded(2));
        assertEquals(1, occurrences(run.stderr(), METHOD_SEED_WARNING), run.stderr());
    }

    @Test
    void aSeedOnTheClassAndTheMethodPinsTheFactoryWithoutAWarning() {
        EngineTestSupport.Captured run = EngineTestSupport.runCapturingStderr(ClassAndMethodPinnedFixture.class);

        run.results().testEvents().assertStatistics(stats -> stats.succeeded(1));
        assertFalse(run.stderr().contains("pins its invocations"), run.stderr());
    }

    @Test
    void anUnpinnedFactoryOfAnUnpinnedTestDoesNotWarn() {
        EngineTestSupport.Captured run = EngineTestSupport.runCapturingStderr(UnpinnedPassingFixture.class);

        run.results().testEvents().assertStatistics(stats -> stats.succeeded(1));
        assertEquals("", run.stderr());
    }

    private static int occurrences(String text, String part) {
        int count = 0;
        for (int at = text.indexOf(part); at >= 0; at = text.indexOf(part, at + part.length())) {
            count++;
        }
        return count;
    }

    private static String recipeOf(long seed) {
        String serialized = GeneratorConfig.builder()
                .seed(seed)
                .build()
                .getGenerationRecipe()
                .orElseThrow()
                .serialize();
        return Base64.getUrlEncoder().withoutPadding().encodeToString(serialized.getBytes(StandardCharsets.UTF_8));
    }

    @Disabled("fixture for KrandomExtensionFactoryWarningEngineTest")
    @ExtendWith(KrandomExtension.class)
    static class FactoryFixture {

        static final List<Integer> VALUES = new CopyOnWriteArrayList<>();

        static Stream<Integer> values(Krandom fake) {
            int value = EngineTestSupport.draw(fake);
            VALUES.add(value);
            return Stream.of(value);
        }

        @ParameterizedTest
        @MethodSource("values")
        void boom(int value) {
            throw new AssertionError("intentional fixture failure");
        }
    }

    @Disabled("fixture for KrandomExtensionFactoryWarningEngineTest")
    static class EnvironmentRecipeFixture {

        @RegisterExtension
        static final KrandomExtension EXTENSION =
                new KrandomExtension(name -> "KRANDOM_JUNIT_RECIPE".equals(name) ? "base64:" + recipeOf(42L) : null);

        @BeforeAll
        static void setUp(Krandom fake) {
            fake.email();
        }

        @Test
        void passes() {}
    }

    @Disabled("fixture for KrandomExtensionFactoryWarningEngineTest")
    @ExtendWith(KrandomExtension.class)
    static class NestedFactoryFixture {

        @BeforeAll
        static void setUp(Krandom fake) {
            fake.email();
        }

        static Stream<Integer> values(Krandom fake) {
            return Stream.of(EngineTestSupport.draw(fake));
        }

        @ParameterizedTest
        @MethodSource("values")
        void outer(int value) {}

        @Nested
        class Inner {

            static Stream<Integer> values(GeneratorConfig config) {
                return Stream.of(config.createRandom().nextInt());
            }

            @ParameterizedTest
            @MethodSource("values")
            void inner(int value) {}
        }
    }

    @Disabled("fixture for KrandomExtensionFactoryWarningEngineTest")
    @ExtendWith(KrandomExtension.class)
    static class MethodPinnedFixture {

        static Stream<Integer> values(Krandom fake, GeneratorConfig config) {
            return Stream.of(EngineTestSupport.draw(fake), config.createRandom().nextInt());
        }

        @KrandomSeed(11)
        @ParameterizedTest
        @MethodSource("values")
        void boom(int value, GeneratorConfig config) {}
    }

    @Disabled("fixture for KrandomExtensionFactoryWarningEngineTest")
    @KrandomSeed(7)
    static class ClassAndMethodPinnedFixture {

        static Stream<Integer> values(Krandom fake) {
            return Stream.of(EngineTestSupport.draw(fake));
        }

        @KrandomSeed(11)
        @ParameterizedTest
        @MethodSource("values")
        void boom(int value) {}
    }

    @Disabled("fixture for KrandomExtensionFactoryWarningEngineTest")
    @ExtendWith(KrandomExtension.class)
    static class UnpinnedPassingFixture {

        static Stream<Integer> values(Krandom fake) {
            return Stream.of(EngineTestSupport.draw(fake));
        }

        @ParameterizedTest
        @MethodSource("values")
        void boom(int value, Krandom fake) {}
    }
}
