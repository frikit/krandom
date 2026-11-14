/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.junit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import io.github.frikit.krandom.generator.Krandom;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Stream;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.testkit.engine.Event;

/**
 * Verifies how the failure line names a test: an invocation of a test template by its template and
 * index rather than by a display name that JUnit formats from the generated arguments, unless
 * {@code krandom.junit.redact-invocation-names=false} restores the display name of 3.1.
 */
class KrandomExtensionInvocationNameEngineTest {

    private static final Map<String, String> DISPLAY_NAMES = Map.of("krandom.junit.redact-invocation-names", "false");

    @Test
    void byDefaultTheFailureLineNamesAnInvocationByItsTemplateAndIndexWithoutTheArguments() {
        ParameterizedFixture.EMAILS.clear();
        EngineTestSupport.Captured run = EngineTestSupport.runCapturingStderr(ParameterizedFixture.class);

        run.results().testEvents().assertStatistics(stats -> stats.failed(2));
        assertTrue(run.stderr().contains("krandom: test 'rejects(String) [1]' failed with seed "), run.stderr());
        assertTrue(run.stderr().contains("krandom: test 'rejects(String) [2]' failed with seed "), run.stderr());
        assertEquals(2, ParameterizedFixture.EMAILS.size());
        for (String email : ParameterizedFixture.EMAILS) {
            assertFalse(run.stderr().contains(email), "the generated argument must not be printed: " + run.stderr());
        }
    }

    @Test
    void trueNamesAnInvocationAsTheDefaultDoes() {
        EngineTestSupport.Captured run = EngineTestSupport.runCapturingStderr(
                RepeatedFixture.class, Map.of("krandom.junit.redact-invocation-names", "true"));

        assertEquals(
                List.of(
                        "krandom: test 'repeats(Krandom) [1]' failed with pinned seed 5.",
                        "krandom: test 'repeats(Krandom) [2]' failed with pinned seed 5."),
                failureLines(run.stderr()));
    }

    @Test
    void byDefaultARepetitionIsNamedByItsTemplateAndIndex() {
        EngineTestSupport.Captured run = EngineTestSupport.runCapturingStderr(RepeatedFixture.class);

        run.results().testEvents().assertStatistics(stats -> stats.failed(2));
        assertEquals(
                List.of(
                        "krandom: test 'repeats(Krandom) [1]' failed with pinned seed 5.",
                        "krandom: test 'repeats(Krandom) [2]' failed with pinned seed 5."),
                failureLines(run.stderr()));
    }

    @Test
    void byDefaultATestThatIsNotAnInvocationKeepsItsDisplayName() {
        EngineTestSupport.Captured run = EngineTestSupport.runCapturingStderr(MethodFixture.class);

        run.results().testEvents().assertStatistics(stats -> stats.failed(2));
        assertTrue(run.stderr().contains("krandom: test 'plain(Krandom)' failed with pinned seed 5."), run.stderr());
        assertTrue(run.stderr().contains("krandom: test 'Custom name' failed with pinned seed 5."), run.stderr());
    }

    @Test
    void falseRestoresTheFailureLinesOf31ByteForByte() {
        EngineTestSupport.Captured repeated =
                EngineTestSupport.runCapturingStderr(RepeatedFixture.class, DISPLAY_NAMES);
        assertEquals(
                List.of(
                        "krandom: test 'repetition 1 of 2' failed with pinned seed 5.",
                        "krandom: test 'repetition 2 of 2' failed with pinned seed 5."),
                failureLines(repeated.stderr()));

        PinnedParameterizedFixture.EMAILS.clear();
        EngineTestSupport.Captured pinned =
                EngineTestSupport.runCapturingStderr(PinnedParameterizedFixture.class, DISPLAY_NAMES);
        assertEquals(
                List.of("krandom: test '[1] \"" + PinnedParameterizedFixture.EMAILS.get(0)
                        + "\"' failed with pinned seed 5."),
                failureLines(pinned.stderr()));

        EngineTestSupport.Captured unpinned =
                EngineTestSupport.runCapturingStderr(ConstantParameterizedFixture.class, DISPLAY_NAMES);
        String seed = EngineTestSupport.singleReportEntry(unpinned.results(), KrandomExtension.REPORT_ENTRY_KEY);
        assertEquals(
                List.of("krandom: test '[1] \"alice@example.com\"' failed with seed " + seed
                        + ". Annotate it with @KrandomSeed(" + seed + "L) to reproduce this run."),
                failureLines(unpinned.stderr()));
    }

    @Test
    void theEnvironmentVariableFalseRestoresTheDisplayName() {
        EngineTestSupport.Captured run = EngineTestSupport.runCapturingStderr(EnvironmentFixture.class);

        assertEquals(
                List.of("krandom: test 'repetition 1 of 1' failed with pinned seed 5."), failureLines(run.stderr()));
    }

    /** The first line of each failure report, which names the test. */
    private static List<String> failureLines(String stderr) {
        return stderr.lines().filter(line -> line.startsWith("krandom: test '")).toList();
    }

    @Test
    void aMalformedValueIsAConfigurationError() {
        EngineTestSupport.Captured run = EngineTestSupport.runCapturingStderr(
                MethodFixture.class, Map.of("krandom.junit.redact-invocation-names", "yes"));

        List<Event> failed = run.results().testEvents().failed().list();
        assertEquals(2, failed.size());
        Throwable thrown = failed.get(0)
                .getRequiredPayload(TestExecutionResult.class)
                .getThrowable()
                .orElseGet(() -> fail("failed event should carry a throwable"));
        assertInstanceOf(ExtensionConfigurationException.class, thrown);
        assertEquals(
                "krandom.junit.redact-invocation-names must be true or false, got 'yes' from"
                        + " krandom.junit.redact-invocation-names",
                thrown.getMessage());
        assertEquals(0, run.results().allEvents().reportingEntryPublished().count());
    }

    @Disabled("fixture for KrandomExtensionInvocationNameEngineTest")
    @ExtendWith(KrandomExtension.class)
    static class ParameterizedFixture {

        static final List<String> EMAILS = new CopyOnWriteArrayList<>();

        static Stream<String> emails(Krandom fake) {
            String first = fake.email();
            String second = fake.email();
            EMAILS.add(first);
            EMAILS.add(second);
            return Stream.of(first, second);
        }

        @ParameterizedTest
        @MethodSource("emails")
        void rejects(String email) {
            throw new AssertionError("intentional fixture failure");
        }
    }

    @Disabled("fixture for KrandomExtensionInvocationNameEngineTest")
    @KrandomSeed(5)
    static class PinnedParameterizedFixture {

        static final List<String> EMAILS = new CopyOnWriteArrayList<>();

        static Stream<String> emails(Krandom fake) {
            String email = fake.email();
            EMAILS.add(email);
            return Stream.of(email);
        }

        @ParameterizedTest
        @MethodSource("emails")
        void rejects(String email) {
            throw new AssertionError("intentional fixture failure");
        }
    }

    @Disabled("fixture for KrandomExtensionInvocationNameEngineTest")
    @ExtendWith(KrandomExtension.class)
    static class ConstantParameterizedFixture {

        static Stream<String> emails() {
            return Stream.of("alice@example.com");
        }

        @ParameterizedTest
        @MethodSource("emails")
        void rejects(String email, Krandom fake) {
            throw new AssertionError("intentional fixture failure");
        }
    }

    @Disabled("fixture for KrandomExtensionInvocationNameEngineTest")
    @KrandomSeed(5)
    static class RepeatedFixture {

        @RepeatedTest(2)
        void repeats(Krandom fake) {
            throw new AssertionError("intentional fixture failure");
        }
    }

    @Disabled("fixture for KrandomExtensionInvocationNameEngineTest")
    @KrandomSeed(5)
    static class MethodFixture {

        @Test
        void plain(Krandom fake) {
            throw new AssertionError("intentional fixture failure");
        }

        @Test
        @DisplayName("Custom name")
        void named(Krandom fake) {
            throw new AssertionError("intentional fixture failure");
        }
    }

    @Disabled("fixture for KrandomExtensionInvocationNameEngineTest")
    static class EnvironmentFixture {

        @RegisterExtension
        static final KrandomExtension EXTENSION = new KrandomExtension(name -> switch (name) {
            case "KRANDOM_JUNIT_REDACT_INVOCATION_NAMES" -> "false";
            case "KRANDOM_JUNIT_SEED" -> "5";
            default -> null;
        });

        @RepeatedTest(1)
        void repeats() {
            throw new AssertionError("intentional fixture failure");
        }
    }
}
