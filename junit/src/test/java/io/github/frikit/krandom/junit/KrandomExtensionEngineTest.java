/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.junit;

import io.github.frikit.krandom.generator.GeneratorConfig;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.reporting.ReportEntry;
import org.junit.platform.testkit.engine.EngineExecutionResults;
import org.junit.platform.testkit.engine.EngineTestKit;
import org.junit.platform.testkit.engine.Event;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
}
