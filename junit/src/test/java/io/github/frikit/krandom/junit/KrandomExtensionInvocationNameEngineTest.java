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
 * Verifies how the failure line names a test, and that {@code krandom.junit.redact-invocation-names}
 * names an invocation of a test template by its template and index rather than by a display name
 * that JUnit formats from the generated arguments.
 */
class KrandomExtensionInvocationNameEngineTest {

    private static final Map<String, String> REDACTED = Map.of("krandom.junit.redact-invocation-names", "true");

    @Test
    void byDefaultTheFailureLineQuotesTheDisplayNameOfEachTest() {
        ParameterizedFixture.EMAILS.clear();
        EngineTestSupport.Captured run = EngineTestSupport.runCapturingStderr(ParameterizedFixture.class);

        run.results().testEvents().assertStatistics(stats -> stats.failed(2));
        assertEquals(2, ParameterizedFixture.EMAILS.size());
        for (int i = 0; i < 2; i++) {
            String line = "krandom: test '[" + (i + 1) + "] \"" + ParameterizedFixture.EMAILS.get(i) + "\"' failed";
            assertTrue(run.stderr().contains(line), run.stderr());
        }
    }

    @Test
    void redactedInvocationNamesNameTheTemplateAndTheInvocationIndexWithoutTheArguments() {
        ParameterizedFixture.EMAILS.clear();
        EngineTestSupport.Captured run = EngineTestSupport.runCapturingStderr(ParameterizedFixture.class, REDACTED);

        run.results().testEvents().assertStatistics(stats -> stats.failed(2));
        assertTrue(run.stderr().contains("krandom: test 'rejects(String) [1]' failed with seed "), run.stderr());
        assertTrue(run.stderr().contains("krandom: test 'rejects(String) [2]' failed with seed "), run.stderr());
        assertEquals(2, ParameterizedFixture.EMAILS.size());
        for (String email : ParameterizedFixture.EMAILS) {
            assertFalse(run.stderr().contains(email), "the generated argument must not be printed: " + run.stderr());
        }
    }

    @Test
    void redactedInvocationNamesNameARepetitionByItsTemplateAndIndex() {
        EngineTestSupport.Captured run = EngineTestSupport.runCapturingStderr(RepeatedFixture.class, REDACTED);

        run.results().testEvents().assertStatistics(stats -> stats.failed(2));
        assertTrue(run.stderr().contains("krandom: test 'repeats(Krandom) [2]' failed with pinned seed 5."));
        assertFalse(run.stderr().contains("repetition"), run.stderr());
    }

    @Test
    void redactedInvocationNamesKeepTheDisplayNameOfATestThatIsNotAnInvocation() {
        EngineTestSupport.Captured run = EngineTestSupport.runCapturingStderr(MethodFixture.class, REDACTED);

        run.results().testEvents().assertStatistics(stats -> stats.failed(2));
        assertTrue(run.stderr().contains("krandom: test 'plain(Krandom)' failed with pinned seed 5."), run.stderr());
        assertTrue(run.stderr().contains("krandom: test 'Custom name' failed with pinned seed 5."), run.stderr());
    }

    @Test
    void falseKeepsTheDisplayNameOfAnInvocation() {
        EngineTestSupport.Captured run = EngineTestSupport.runCapturingStderr(
                RepeatedFixture.class, Map.of("krandom.junit.redact-invocation-names", "false"));

        assertTrue(run.stderr().contains("krandom: test 'repetition 2 of 2' failed"), run.stderr());
    }

    @Test
    void theEnvironmentVariableRedactsInvocationNames() {
        EngineTestSupport.Captured run = EngineTestSupport.runCapturingStderr(EnvironmentFixture.class);

        assertTrue(run.stderr().contains("krandom: test 'repeats() [1]' failed"), run.stderr());
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
        static final KrandomExtension EXTENSION =
                new KrandomExtension(name -> "KRANDOM_JUNIT_REDACT_INVOCATION_NAMES".equals(name) ? "true" : null);

        @RepeatedTest(1)
        void repeats() {
            throw new AssertionError("intentional fixture failure");
        }
    }
}
