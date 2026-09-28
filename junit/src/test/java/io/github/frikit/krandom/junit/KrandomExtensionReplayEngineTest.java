/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.junit;

import io.github.frikit.krandom.generator.GenerationRecipe;
import io.github.frikit.krandom.generator.GeneratorConfig;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.testkit.engine.EngineExecutionResults;
import org.junit.platform.testkit.engine.Event;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Verifies the printed replay commands, JUnit configuration-parameter support, the documented
 * source precedence, and how the failure output explains the recorded clock instant.
 */
class KrandomExtensionReplayEngineTest {

    private static final Pattern GRADLE_REPLAY = Pattern.compile(
        "KRANDOM_JUNIT_RECIPE=(base64:[A-Za-z0-9_-]+) \\./gradlew test --tests '([^']+)' --rerun");
    private static final Pattern MAVEN_REPLAY = Pattern.compile(
        "mvn test -Dtest='([^']+)' -Dkrandom\\.junit\\.recipe=(base64:[A-Za-z0-9_-]+)");
    private static final Pattern GRADLE_CLASS_REPLAY = Pattern.compile(
        "KRANDOM_JUNIT_CLASS_RECIPE='([^']+)' KRANDOM_JUNIT_RECIPE=(base64:[A-Za-z0-9_-]+) \\./gradlew test");
    private static final Pattern MAVEN_CLASS_REPLAY = Pattern.compile(
        "-Dkrandom\\.junit\\.recipe=base64:[A-Za-z0-9_-]+ -Dkrandom\\.junit\\.class-recipe='([^']+)'");

    @Test
    void failurePrintsAGradleCommandWhoseEnvironmentVariableReachesTheTestJvm() {
        EngineTestSupport.Captured run = EngineTestSupport.runCapturingStderr(RecordingFailingFixture.class);

        Matcher gradle = GRADLE_REPLAY.matcher(run.stderr());
        assertTrue(gradle.find(), "stderr should print a Gradle replay command, was: " + run.stderr());
        assertEquals(RecordingFailingFixture.class.getName(), gradle.group(2));
        assertFalse(run.stderr().contains("./gradlew test -Dkrandom.junit"),
                    "Gradle -D sets a property on the Gradle JVM, not on the forked test JVM");
    }

    @Test
    void failurePrintsAMavenCommandForSurefire() {
        EngineTestSupport.Captured run = EngineTestSupport.runCapturingStderr(RecordingFailingFixture.class);

        Matcher gradle = GRADLE_REPLAY.matcher(run.stderr());
        Matcher maven = MAVEN_REPLAY.matcher(run.stderr());
        assertTrue(gradle.find(), run.stderr());
        assertTrue(maven.find(), "stderr should print a Maven replay command, was: " + run.stderr());
        assertTrue(maven.group(1).startsWith(RecordingFailingFixture.class.getName()), maven.group(1));
        assertEquals(gradle.group(1), maven.group(2), "both commands must carry the same recipe");
    }

    @Test
    void nestedTestsAreSelectedThroughTheirTopLevelClass() {
        EngineTestSupport.Captured run = EngineTestSupport.runCapturingStderr(NestedFailingFixture.class);

        run.results().testEvents().assertStatistics(stats -> stats.failed(1));
        Matcher gradle = GRADLE_REPLAY.matcher(run.stderr());
        assertTrue(gradle.find(), run.stderr());
        assertEquals(NestedFailingFixture.class.getName(), gradle.group(2),
                     "Gradle selects @Nested tests through the class that JUnit discovered");
    }

    @Test
    void printedRecipeReplaysThroughAJUnitConfigurationParameter() {
        RecordingFailingFixture.RECORDED.clear();
        EngineTestSupport.Captured first = EngineTestSupport.runCapturingStderr(RecordingFailingFixture.class);
        first.results().testEvents().assertStatistics(stats -> stats.failed(1));
        Matcher gradle = GRADLE_REPLAY.matcher(first.stderr());
        assertTrue(gradle.find(), first.stderr());

        EngineExecutionResults replay = EngineTestSupport.run(
            RecordingFailingFixture.class, Map.of("krandom.junit.recipe", gradle.group(1)));

        replay.testEvents().assertStatistics(stats -> stats.failed(1));
        assertEquals(2, RecordingFailingFixture.RECORDED.size());
        assertEquals(RecordingFailingFixture.RECORDED.get(0), RecordingFailingFixture.RECORDED.get(1),
                     "a recipe supplied as a JUnit configuration parameter must reproduce the failure");
    }

    @Test
    void printedCommandsReplayClassScopedAndTestConfigurations() {
        ClassScopedFailingFixture.reset();
        EngineTestSupport.Captured first = EngineTestSupport.runCapturingStderr(ClassScopedFailingFixture.class);
        first.results().testEvents().assertStatistics(stats -> stats.failed(1));
        Matcher gradle = GRADLE_CLASS_REPLAY.matcher(first.stderr());
        Matcher maven = MAVEN_CLASS_REPLAY.matcher(first.stderr());
        assertTrue(gradle.find(), "stderr should print class-scoped recipes, was: " + first.stderr());
        assertTrue(maven.find(), first.stderr());
        assertEquals(gradle.group(1), maven.group(1), "both commands must carry the same class-scoped recipes");

        EngineExecutionResults replay = EngineTestSupport.run(ClassScopedFailingFixture.class, Map.of(
            "krandom.junit.class-recipe", gradle.group(1), "krandom.junit.recipe", gradle.group(2)));

        replay.testEvents().assertStatistics(stats -> stats.failed(1));
        assertReplayed(ClassScopedFailingFixture.OUTER_VALUES, "outer @BeforeAll data");
        assertReplayed(ClassScopedFailingFixture.INNER_VALUES, "nested @BeforeAll data");
        assertReplayed(ClassScopedFailingFixture.TEST_VALUES, "the failing test's data");
    }

    @Test
    void classRecipeAppliesOnlyToTheClassItNames() {
        ClassScopedFailingFixture.reset();
        String entry = ClassScopedFailingFixture.class.getCanonicalName() + "="
                       + encoded(GenerationRecipe.builder().seed(5L).build());

        EngineTestSupport.run(ClassScopedFailingFixture.class, Map.of("krandom.junit.class-recipe", entry));

        long recipeValue = new Random(5L).nextLong();
        assertEquals(List.of(recipeValue), ClassScopedFailingFixture.OUTER_VALUES);
        assertNotEquals(List.of(recipeValue), ClassScopedFailingFixture.INNER_VALUES,
                        "a nested class without an entry keeps its own class-scoped configuration");
        assertNotEquals(List.of(recipeValue), ClassScopedFailingFixture.TEST_VALUES,
                        "class recipes never apply to test methods");
    }

    @Test
    void malformedClassRecipesAreConfigurationErrors() {
        String recipe = encoded(GenerationRecipe.builder().seed(5L).build());
        String owner = ClassScopedFailingFixture.class.getCanonicalName();
        for (String value : List.of("not-an-entry", owner + "=plain", owner + "=" + recipe + "," + owner + "=" + recipe)) {
            EngineExecutionResults results = EngineTestSupport.run(
                ClassScopedFailingFixture.class, Map.of("krandom.junit.class-recipe", value));

            List<Event> failed = results.containerEvents().failed().list();
            assertEquals(1, failed.size(), value);
            Throwable thrown = failed.get(0).getRequiredPayload(TestExecutionResult.class).getThrowable().orElseThrow();
            while (!(thrown instanceof ExtensionConfigurationException) && thrown.getCause() != null) {
                thrown = thrown.getCause();
            }
            assertInstanceOf(ExtensionConfigurationException.class, thrown, value);
            assertTrue(thrown.getMessage().contains("krandom.junit.class-recipe"), thrown.getMessage());
        }
    }

    private static void assertReplayed(List<Long> values, String what) {
        assertEquals(2, values.size(), what);
        assertEquals(values.get(0), values.get(1), what + " must replay");
    }

    private static String encoded(GenerationRecipe recipe) {
        return "base64:" + Base64.getUrlEncoder().withoutPadding()
                                 .encodeToString(recipe.serialize().getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void seedConfigurationParameterOverridesKrandomSeed() {
        EngineExecutionResults results = EngineTestSupport.run(
            PinnedExpectingOverrideFixture.class, Map.of("krandom.junit.seed", "24680"));

        results.testEvents().assertStatistics(stats -> stats.succeeded(1).failed(0));
    }

    @Test
    void snapshotClockConfigurationParameterIsHonoured() {
        EngineExecutionResults results = EngineTestSupport.run(
            SnapshotExpectingFixture.class, Map.of("krandom.junit.snapshot-clock", "true"));

        results.testEvents().assertStatistics(stats -> stats.succeeded(1).failed(0));
    }

    @Test
    void malformedSnapshotConfigurationParameterIsAConfigurationError() {
        EngineExecutionResults results = EngineTestSupport.run(
            SnapshotExpectingFixture.class, Map.of("krandom.junit.snapshot-clock", "yes"));

        assertSingleConfigurationError(results, "snapshot-clock must be true or false");
    }

    @Test
    void environmentVariableAppliesWhenNoConfigurationParameterIsSet() {
        EnvironmentSeedFixture.SEEDS.clear();

        EngineExecutionResults results = EngineTestSupport.run(EnvironmentSeedFixture.class);

        results.testEvents().assertStatistics(stats -> stats.succeeded(1).failed(0));
        assertEquals(List.of(13579L), EnvironmentSeedFixture.SEEDS);
    }

    @Test
    void configurationParameterTakesPrecedenceOverTheEnvironmentVariable() {
        EnvironmentSeedFixture.SEEDS.clear();

        EngineExecutionResults results = EngineTestSupport.run(
            EnvironmentSeedFixture.class, Map.of("krandom.junit.seed", "24680"));

        results.testEvents().assertStatistics(stats -> stats.succeeded(1).failed(0));
        assertEquals(List.of(24680L), EnvironmentSeedFixture.SEEDS);
    }

    @Test
    void recipeAndSeedFromDifferentSourcesConflict() {
        EngineExecutionResults results = EngineTestSupport.run(
            EnvironmentRecipeFixture.class, Map.of("krandom.junit.seed", "24680"));

        assertSingleConfigurationError(results, "Configure only one replay override");
    }

    @Test
    void defaultOutputStatesThatTheClockWasReadAtFailureTime() {
        EngineTestSupport.Captured run = EngineTestSupport.runCapturingStderr(RecordingFailingFixture.class);

        assertTrue(run.stderr().contains("read when the failure was reported"), run.stderr());
        assertTrue(run.stderr().contains("krandom.junit.snapshot-clock=true"),
                   "time-sensitive tests should be pointed at the snapshot option, was: " + run.stderr());
    }

    @Test
    void snapshotOutputStatesThatTheClockWasCapturedBeforeInjection() {
        EngineTestSupport.Captured run = EngineTestSupport.runCapturingStderr(
            RecordingFailingFixture.class, Map.of("krandom.junit.snapshot-clock", "true"));

        assertTrue(run.stderr().contains("captured before parameter injection"), run.stderr());
        assertFalse(run.stderr().contains("read when the failure was reported"), run.stderr());
    }

    @Test
    void replayedRecipeOutputStatesThatTheClockCameFromTheRecipe() {
        GenerationRecipe recipe = GenerationRecipe.builder().seed(42L).build();
        String encoded = "base64:" + Base64.getUrlEncoder().withoutPadding()
                                           .encodeToString(recipe.serialize().getBytes(StandardCharsets.UTF_8));

        EngineTestSupport.Captured run = EngineTestSupport.runCapturingStderr(
            RecordingFailingFixture.class, Map.of("krandom.junit.recipe", encoded));

        assertTrue(run.stderr().contains("fixed by the replay recipe"), run.stderr());
    }

    private static void assertSingleConfigurationError(EngineExecutionResults results, String expectedMessagePart) {
        List<Event> failed = results.testEvents().failed().list();
        assertEquals(1, failed.size());
        Throwable thrown = failed.get(0)
                                 .getRequiredPayload(TestExecutionResult.class)
                                 .getThrowable()
                                 .orElseGet(() -> fail("failed event should carry a throwable"));
        assertInstanceOf(ExtensionConfigurationException.class, thrown);
        assertTrue(thrown.getMessage().contains(expectedMessagePart), "unexpected message: " + thrown.getMessage());
    }

    // ── Fixtures: disabled in the regular suite, executed only via EngineTestKit ────────────

    @Disabled("fixture for KrandomExtensionReplayEngineTest")
    @ExtendWith(KrandomExtension.class)
    static class RecordingFailingFixture {

        static final List<Long> RECORDED = new CopyOnWriteArrayList<>();

        @Test
        void boom(GeneratorConfig config) {
            RECORDED.add(config.createRandom().nextLong());
            fail("intentional recording fixture failure");
        }
    }

    @Disabled("fixture for KrandomExtensionReplayEngineTest")
    @ExtendWith(KrandomExtension.class)
    static class ClassScopedFailingFixture {

        static final List<Long> OUTER_VALUES = new CopyOnWriteArrayList<>();
        static final List<Long> INNER_VALUES = new CopyOnWriteArrayList<>();
        static final List<Long> TEST_VALUES = new CopyOnWriteArrayList<>();

        static void reset() {
            OUTER_VALUES.clear();
            INNER_VALUES.clear();
            TEST_VALUES.clear();
        }

        @BeforeAll
        static void outerSetUp(GeneratorConfig config) {
            OUTER_VALUES.add(config.createRandom().nextLong());
        }

        @Nested
        class Inner {

            @BeforeAll
            static void innerSetUp(GeneratorConfig config) {
                INNER_VALUES.add(config.createRandom().nextLong());
            }

            @Test
            void boom(GeneratorConfig config) {
                TEST_VALUES.add(config.createRandom().nextLong());
                fail("intentional fixture failure that depends on class-scoped and test data");
            }
        }
    }

    @Disabled("fixture for KrandomExtensionReplayEngineTest")
    @ExtendWith(KrandomExtension.class)
    static class NestedFailingFixture {

        @Nested
        class Inner {

            @Test
            void boom(GeneratorConfig config) {
                fail("intentional nested fixture failure");
            }
        }
    }

    @Disabled("fixture for KrandomExtensionReplayEngineTest")
    @KrandomSeed(99L)
    static class PinnedExpectingOverrideFixture {

        @Test
        void usesConfigurationParameterSeed(GeneratorConfig config) {
            assertEquals(24680L, config.getSeed().orElseThrow());
        }
    }

    @Disabled("fixture for KrandomExtensionReplayEngineTest")
    @KrandomSeed(42L)
    static class SnapshotExpectingFixture {

        @Test
        void clockIsFixed(GeneratorConfig config) {
            Clock clock = config.getClock();
            assertEquals(Clock.fixed(clock.instant(), clock.getZone()), clock);
        }
    }

    @Disabled("fixture for KrandomExtensionReplayEngineTest")
    static class EnvironmentSeedFixture {

        static final List<Long> SEEDS = new CopyOnWriteArrayList<>();

        @RegisterExtension
        static final KrandomExtension EXTENSION =
            new KrandomExtension(name -> "KRANDOM_JUNIT_SEED".equals(name) ? "13579" : null);

        @Test
        void record(GeneratorConfig config) {
            SEEDS.add(config.getSeed().orElseThrow());
        }
    }

    @Disabled("fixture for KrandomExtensionReplayEngineTest")
    static class EnvironmentRecipeFixture {

        @RegisterExtension
        static final KrandomExtension EXTENSION = new KrandomExtension(name -> "KRANDOM_JUNIT_RECIPE".equals(name)
            ? "base64:" + Base64.getUrlEncoder().withoutPadding().encodeToString(
                GenerationRecipe.builder().seed(7L).build().serialize().getBytes(StandardCharsets.UTF_8))
            : null);

        @Test
        void unreachable(GeneratorConfig config) {
            fail("a conflicting replay configuration must fail before injection");
        }
    }
}
