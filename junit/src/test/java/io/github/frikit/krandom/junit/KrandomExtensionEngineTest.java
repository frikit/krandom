/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.junit;

import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.GenerationRecipe;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.reporting.ReportEntry;
import org.junit.platform.testkit.engine.EngineExecutionResults;
import org.junit.platform.testkit.engine.EngineTestKit;
import org.junit.platform.testkit.engine.Event;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

/**
 * Runs the {@code @Disabled} fixture classes below through {@link EngineTestKit} (with the
 * built-in {@code @Disabled} condition deactivated) to verify failure-seed reporting and
 * {@code @KrandomSeed} configuration validation.
 */
class KrandomExtensionEngineTest {

    private static final String REPLAY_RECIPE_PROPERTY = "krandom.junit.recipe";
    private static final String REPLAY_SEED_PROPERTY = "krandom.junit.seed";

    @Test
    void failingUnpinnedTestReportsItsSeedWithAReproductionHint() {
        ByteArrayOutputStream captured = new ByteArrayOutputStream();
        EngineExecutionResults results = runFixtureCapturingStderr(UnpinnedFailingFixture.class, captured);

        results.testEvents().assertStatistics(stats -> stats.failed(1));
        long reportedSeed = Long.parseLong(singleSeedReportEntry(results));
        String stderr = captured.toString(StandardCharsets.UTF_8);
        assertTrue(stderr.contains("Annotate it with @KrandomSeed(" + reportedSeed + "L)"),
                "stderr should carry the reproduction hint, was: " + stderr);
        assertTrue(stderr.contains("Replay recipe:"), "stderr should carry the replay recipe, was: " + stderr);
        assertTrue(singleRecipeReportEntry(results).contains("seed=" + reportedSeed));
    }

    @Test
    void failingPinnedTestReportsThePinnedSeed() {
        ByteArrayOutputStream captured = new ByteArrayOutputStream();
        EngineExecutionResults results = runFixtureCapturingStderr(PinnedFailingFixture.class, captured);

        results.testEvents().assertStatistics(stats -> stats.failed(1));
        assertEquals("42", singleSeedReportEntry(results));
        String stderr = captured.toString(StandardCharsets.UTF_8);
        assertTrue(stderr.contains("failed with pinned seed 42"),
                "stderr should mention the pinned seed, was: " + stderr);
        assertTrue(singleRecipeReportEntry(results).contains("seed=42"));
    }

    @Test
    void passingTestPublishesNoSeedReportEntry() {
        EngineExecutionResults results = runFixture(PassingFixture.class);

        results.testEvents().assertStatistics(stats -> stats.succeeded(1).failed(0));
        assertEquals(0, results.allEvents().reportingEntryPublished().count());
    }

    @Test
    void seedWithBothValueAndTextIsAConfigurationError() {
        assertSingleConfigurationError(BothValueAndTextFixture.class, "not both");
    }

    @Test
    void seedWithNeitherValueNorTextIsAConfigurationError() {
        assertSingleConfigurationError(EmptySeedFixture.class, "requires a numeric value or a non-blank text seed");
    }

    @Test
    void blankTextSeedIsAConfigurationError() {
        assertSingleConfigurationError(BlankTextSeedFixture.class, "must not be blank");
    }

    @Test
    void systemRecipeOverrideTakesPrecedenceAndPreservesTextSeedMetadata() throws Exception {
        GenerationRecipe recipe = GenerationRecipe.builder()
                                                    .seed(GeneratorConfig.deriveSeed("checkout-replay"))
                                                    .seedText("checkout-replay")
                                                    .locale(Locale.CANADA_FRENCH)
                                                    .profile("ci-replay")
                                                    .build();

        withSystemProperty(REPLAY_RECIPE_PROPERTY, encodeRecipe(recipe), () -> {
            EngineExecutionResults results = runFixture(RecipeOverrideFixture.class);

            results.testEvents().assertStatistics(statistics -> statistics.succeeded(1).failed(0));
        });
    }

    @Test
    void systemSeedOverrideWorksWithoutAnAnnotation() throws Exception {
        withSystemProperty(REPLAY_SEED_PROPERTY, "24680", () -> {
            EngineExecutionResults results = runFixture(NumericOverrideFixture.class);

            results.testEvents().assertStatistics(statistics -> statistics.succeeded(1).failed(0));
        });
    }

    @Test
    void malformedSystemRecipeIsAConfigurationError() throws Exception {
        withSystemProperty(REPLAY_RECIPE_PROPERTY, "base64:not-a-recipe", () ->
            assertSingleConfigurationError(NumericOverrideFixture.class, "Invalid krandom.junit.recipe"));
    }

    @Test
    void conflictingSystemOverridesAreAConfigurationError() throws Exception {
        GenerationRecipe recipe = GenerationRecipe.builder().seed(42L).build();

        withSystemProperty(REPLAY_RECIPE_PROPERTY, encodeRecipe(recipe), () ->
            withSystemProperty(REPLAY_SEED_PROPERTY, "24680", () ->
                assertSingleConfigurationError(NumericOverrideFixture.class, "Configure only one replay override")));
    }

    @Test
    void failingTestPrintsSafeCopyableRecipeOverride() throws Exception {
        GenerationRecipe recipe = GenerationRecipe.builder()
                                                    .seed(GeneratorConfig.deriveSeed("private seed"))
                                                    .seedText("private seed")
                                                    .build();
        ByteArrayOutputStream captured = new ByteArrayOutputStream();

        withSystemProperty(REPLAY_RECIPE_PROPERTY, encodeRecipe(recipe), () -> {
            EngineExecutionResults results = runFixtureCapturingStderr(ReplayOverrideFailingFixture.class, captured);

            results.testEvents().assertStatistics(statistics -> statistics.failed(1));
        });

        String stderr = captured.toString(StandardCharsets.UTF_8);
        assertTrue(stderr.contains("-Dkrandom.junit.recipe=base64:"),
                   "stderr should carry a copyable replay override, was: " + stderr);
        assertFalse(stderr.contains("private seed"), "stderr must not reveal textual seed material");
    }

    @Test
    void printedReplayOptionReproducesTheFailingGeneration() throws Exception {
        RecordingFailingFixture.RECORDED.clear();
        ByteArrayOutputStream captured = new ByteArrayOutputStream();

        EngineExecutionResults first = runFixtureCapturingStderr(RecordingFailingFixture.class, captured);
        first.testEvents().assertStatistics(statistics -> statistics.failed(1));

        String stderr = captured.toString(StandardCharsets.UTF_8);
        Matcher replayOption = Pattern.compile("-Dkrandom\\.junit\\.recipe=(base64:[A-Za-z0-9_-]+)")
                .matcher(stderr);
        assertTrue(replayOption.find(), "stderr should print a copyable replay option, was: " + stderr);

        withSystemProperty(REPLAY_RECIPE_PROPERTY, replayOption.group(1), () -> {
            EngineExecutionResults replay = runFixture(RecordingFailingFixture.class);
            replay.testEvents().assertStatistics(statistics -> statistics.failed(1));
        });

        assertEquals(2, RecordingFailingFixture.RECORDED.size());
        assertEquals(RecordingFailingFixture.RECORDED.get(0), RecordingFailingFixture.RECORDED.get(1),
                "the printed replay option must reproduce the failing run's generated value");
    }

    @Test
    void parameterizedInvocationsGetIsolatedDeterministicSources() {
        ParameterizedPinnedFixture.RECORDED.clear();

        EngineExecutionResults results = runFixture(ParameterizedPinnedFixture.class);

        results.testEvents().assertStatistics(statistics -> statistics.succeeded(3).failed(0));
        assertEquals(3, ParameterizedPinnedFixture.RECORDED.size());
        assertEquals(1, Set.copyOf(ParameterizedPinnedFixture.RECORDED).size(),
                "each invocation must draw from its own source seeded with the pinned seed");
    }

    @Test
    void concurrentTestsGetIsolatedDeterministicSources() {
        ConcurrentPinnedFixture.RECORDED.clear();

        EngineExecutionResults results = EngineTestKit.engine("junit-jupiter")
                .selectors(selectClass(ConcurrentPinnedFixture.class))
                .configurationParameter("junit.jupiter.conditions.deactivate", "org.junit.*")
                .configurationParameter("junit.jupiter.execution.parallel.enabled", "true")
                .configurationParameter("junit.jupiter.execution.parallel.mode.default", "concurrent")
                .execute();

        results.testEvents().assertStatistics(statistics -> statistics.succeeded(4).failed(0));
        assertEquals(4, ConcurrentPinnedFixture.RECORDED.size());
        assertEquals(1, Set.copyOf(ConcurrentPinnedFixture.RECORDED).size(),
                "concurrent tests must not share or advance one another's random source");
    }

    @Test
    void lifecycleCallbacksDoNotLeakSeedsBetweenTests() {
        LeakProbeFixture.RECORDED_SEEDS.clear();

        EngineExecutionResults results = runFixture(LeakProbeFixture.class);

        results.testEvents().assertStatistics(statistics -> statistics.succeeded(2).failed(0));
        assertEquals(2, LeakProbeFixture.RECORDED_SEEDS.size());
        assertEquals(2, Set.copyOf(LeakProbeFixture.RECORDED_SEEDS).size(),
                "unpinned tests must not reuse a previous test's stored seed");
    }

    @Test
    void optInClockSnapshotIsSharedByGenerationAndFailureReport() throws Exception {
        withSystemProperty("krandom.junit.snapshot-clock", "true", () -> {
            EngineExecutionResults results = runFixture(SnapshotFailingFixture.class);
            results.testEvents().assertStatistics(stats -> stats.failed(1));
            GenerationRecipe reported = GenerationRecipe.parse(singleRecipeReportEntry(results));
            assertEquals(SnapshotFailingFixture.CLOCK.toInstant(), reported.getClockInstant());
            assertEquals(SnapshotFailingFixture.CLOCK.getZone(), reported.getClockZone());
        });
    }

    @Test
    void malformedSnapshotFlagFailsBeforeReportingARecipe() throws Exception {
        withSystemProperty("krandom.junit.snapshot-clock", "yes", () ->
            assertSingleConfigurationError(PassingFixture.class, "snapshot-clock must be true or false"));
    }

    @Test
    void snapshotDisabledRetainsLiveClock() throws Exception {
        withSystemProperty("krandom.junit.snapshot-clock", "false", () -> {
            EngineExecutionResults results = runFixture(LiveClockFixture.class);
            results.testEvents().assertStatistics(stats -> stats.succeeded(1));
        });
    }

    @Disabled
    @KrandomSeed(42)
    static class SnapshotFailingFixture {
        static java.time.ZonedDateTime CLOCK;
        @Test
        void failAfterCapturingClock(GeneratorConfig config, GeneratorConfig.Builder builder) {
            CLOCK = java.time.ZonedDateTime.ofInstant(config.getClock().instant(), config.getClock().getZone());
            assertEquals(java.time.Clock.fixed(CLOCK.toInstant(), CLOCK.getZone()), config.getClock());
            assertEquals(config.getClock(), builder.build().getClock());
            fail("report the same captured instant");
        }
    }

    @Disabled
    @KrandomSeed(42)
    static class LiveClockFixture {
        @Test
        void retainsLiveClock(GeneratorConfig config) {
            assertEquals(java.time.Clock.systemDefaultZone(), config.getClock());
        }
    }

    private static void assertSingleConfigurationError(Class<?> fixture, String expectedMessagePart) {
        EngineExecutionResults results = runFixture(fixture);

        List<Event> failed = results.testEvents().failed().list();
        assertEquals(1, failed.size());
        Throwable thrown = failed.get(0)
                .getRequiredPayload(TestExecutionResult.class)
                .getThrowable()
                .orElseGet(() -> fail("failed event should carry a throwable"));
        assertInstanceOf(ExtensionConfigurationException.class, thrown);
        assertTrue(thrown.getMessage().contains(expectedMessagePart),
                "unexpected message: " + thrown.getMessage());
        // A configuration error aborts the test before a seed exists, so nothing is reported.
        assertEquals(0, results.allEvents().reportingEntryPublished().count());
    }

    private static String encodeRecipe(GenerationRecipe recipe) {
        byte[] serialized = recipe.serialize().getBytes(StandardCharsets.UTF_8);
        return "base64:" + Base64.getUrlEncoder().withoutPadding().encodeToString(serialized);
    }

    private static void withSystemProperty(String key, String value, ThrowingRunnable action) throws Exception {
        String previous = System.getProperty(key);
        System.setProperty(key, value);
        try {
            action.run();
        } finally {
            if (previous == null) {
                System.clearProperty(key);
            } else {
                System.setProperty(key, previous);
            }
        }
    }

    @FunctionalInterface
    private interface ThrowingRunnable {

        void run() throws Exception;
    }

    private static EngineExecutionResults runFixture(Class<?> fixture) {
        return EngineTestKit.engine("junit-jupiter")
                .selectors(selectClass(fixture))
                .configurationParameter("junit.jupiter.conditions.deactivate", "org.junit.*")
                .execute();
    }

    private static EngineExecutionResults runFixtureCapturingStderr(Class<?> fixture, ByteArrayOutputStream captured) {
        PrintStream original = System.err;
        System.setErr(new PrintStream(captured, true, StandardCharsets.UTF_8));
        try {
            return runFixture(fixture);
        } finally {
            System.setErr(original);
        }
    }

    private static String singleSeedReportEntry(EngineExecutionResults results) {
        return singleReportEntry(results, KrandomExtension.REPORT_ENTRY_KEY);
    }

    private static String singleRecipeReportEntry(EngineExecutionResults results) {
        return singleReportEntry(results, KrandomExtension.RECIPE_REPORT_ENTRY_KEY);
    }

    private static String singleReportEntry(EngineExecutionResults results, String key) {
        List<ReportEntry> entries = results.allEvents().reportingEntryPublished().stream()
                .map(event -> event.getRequiredPayload(ReportEntry.class))
                .filter(entry -> entry.getKeyValuePairs().containsKey(key))
                .toList();
        assertEquals(1, entries.size());
        return entries.get(0).getKeyValuePairs().get(key);
    }

    // ── Fixtures: disabled in the regular suite, executed only via EngineTestKit ────────────

    @Disabled("fixture for KrandomExtensionEngineTest")
    @ExtendWith(KrandomExtension.class)
    static class UnpinnedFailingFixture {
        @Test
        void boom(GeneratorConfig config) {
            fail("intentional fixture failure with seed " + config.getSeed().getAsLong());
        }
    }

    @Disabled("fixture for KrandomExtensionEngineTest")
    static class PinnedFailingFixture {
        @KrandomSeed(42L)
        @Test
        void boom() {
            fail("intentional fixture failure");
        }
    }

    @Disabled("fixture for KrandomExtensionEngineTest")
    @ExtendWith(KrandomExtension.class)
    static class PassingFixture {
        @Test
        void fine(GeneratorConfig config) {
            assertTrue(config.getSeed().isPresent());
        }
    }

    @Disabled("fixture for KrandomExtensionEngineTest")
    static class BothValueAndTextFixture {
        @KrandomSeed(value = 1L, text = "also-set")
        @Test
        void invalid() {
        }
    }

    @Disabled("fixture for KrandomExtensionEngineTest")
    static class EmptySeedFixture {
        @KrandomSeed
        @Test
        void invalid() {
        }
    }

    @Disabled("fixture for KrandomExtensionEngineTest")
    static class BlankTextSeedFixture {
        @KrandomSeed(text = "   ")
        @Test
        void invalid() {
        }
    }

    @Disabled("fixture for KrandomExtensionEngineTest")
    @KrandomSeed(99L)
    static class RecipeOverrideFixture {

        @Test
        void configUsesSystemRecipe(GeneratorConfig config, GeneratorConfig.Builder builder) {
            assertEquals(Locale.CANADA_FRENCH, config.getLocale());
            assertEquals("checkout-replay", config.getStringSeed().orElseThrow());
            assertEquals("ci-replay", config.getGenerationProfile());
            GeneratorConfig rebuilt = builder.build();
            assertEquals(config.getSeed(), rebuilt.getSeed());
            assertEquals(config.getStringSeed(), rebuilt.getStringSeed());
            assertEquals(config.getLocale(), rebuilt.getLocale());
            assertEquals(config.getGenerationProfile(), rebuilt.getGenerationProfile());
        }
    }

    @Disabled("fixture for KrandomExtensionEngineTest")
    @ExtendWith(KrandomExtension.class)
    static class NumericOverrideFixture {

        @Test
        void configUsesSystemSeed(GeneratorConfig config) {
            assertEquals(24680L, config.getSeed().orElseThrow());
        }
    }

    @Disabled("fixture for KrandomExtensionEngineTest")
    @ExtendWith(KrandomExtension.class)
    static class ReplayOverrideFailingFixture {

        @Test
        void boom() {
            fail("intentional replay fixture failure");
        }
    }

    @Disabled("fixture for KrandomExtensionEngineTest")
    @ExtendWith(KrandomExtension.class)
    static class RecordingFailingFixture {

        static final List<Long> RECORDED = new CopyOnWriteArrayList<>();

        @Test
        void boom(GeneratorConfig config) {
            RECORDED.add(config.createRandom().nextLong());
            fail("intentional recording fixture failure");
        }
    }

    @Disabled("fixture for KrandomExtensionEngineTest")
    @KrandomSeed(4242L)
    static class ParameterizedPinnedFixture {

        static final List<Long> RECORDED = new CopyOnWriteArrayList<>();

        @ParameterizedTest
        @ValueSource(ints = {1, 2, 3})
        void invocation(int ignored, GeneratorConfig config) {
            RECORDED.add(config.createRandom().nextLong());
        }
    }

    @Disabled("fixture for KrandomExtensionEngineTest")
    @KrandomSeed(7L)
    @Execution(ExecutionMode.CONCURRENT)
    static class ConcurrentPinnedFixture {

        static final List<Long> RECORDED = new CopyOnWriteArrayList<>();

        @Test
        void first(GeneratorConfig config) {
            RECORDED.add(config.createRandom().nextLong());
        }

        @Test
        void second(GeneratorConfig config) {
            RECORDED.add(config.createRandom().nextLong());
        }

        @Test
        void third(GeneratorConfig config) {
            RECORDED.add(config.createRandom().nextLong());
        }

        @Test
        void fourth(GeneratorConfig config) {
            RECORDED.add(config.createRandom().nextLong());
        }
    }

    @Disabled("fixture for KrandomExtensionEngineTest")
    @ExtendWith(KrandomExtension.class)
    static class LeakProbeFixture {

        static final List<Long> RECORDED_SEEDS = new CopyOnWriteArrayList<>();

        @Test
        void firstProbe(GeneratorConfig config) {
            RECORDED_SEEDS.add(config.getSeed().orElseThrow());
        }

        @Test
        void secondProbe(GeneratorConfig config) {
            RECORDED_SEEDS.add(config.getSeed().orElseThrow());
        }
    }
}
