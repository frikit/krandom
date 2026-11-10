/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.examples;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.Generators;
import io.github.frikit.krandom.generator.Krandom;
import io.github.frikit.krandom.junit.KrandomExtension;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.platform.engine.reporting.ReportEntry;
import org.junit.platform.testkit.engine.EngineExecutionResults;
import org.junit.platform.testkit.engine.EngineTestKit;

/**
 * Runs unpinned failing tests, copies the replay values that krandom-junit prints for Maven, and
 * replays the runs with them: each replay generates exactly the data of the failing run.
 */
class JunitReplayExampleTest {

    private static final Pattern MAVEN_REPLAY =
            Pattern.compile("mvn test -Dtest='([^']+)' -Dkrandom\\.junit\\.recipe=(base64:[A-Za-z0-9_-]+)");
    private static final Pattern MAVEN_CLASS_REPLAY = Pattern.compile("mvn test -Dtest='([^']+)' "
            + "-Dkrandom\\.junit\\.recipe=(base64:[A-Za-z0-9_-]+) -Dkrandom\\.junit\\.class-recipe='([^']+)'");

    @Test
    void printedMavenReplayCommandReproducesTheFailingData() {
        CheckoutFixture.EMAILS.clear();
        ByteArrayOutputStream stderr = new ByteArrayOutputStream();

        EngineExecutionResults failing = runCapturingStderr(CheckoutFixture.class, stderr);
        failing.testEvents().assertStatistics(stats -> stats.failed(1));
        Matcher replay = MAVEN_REPLAY.matcher(stderr.toString(StandardCharsets.UTF_8));
        assertTrue(replay.find(), "krandom-junit prints a Maven replay command: " + stderr);
        assertEquals(CheckoutFixture.class.getName(), replay.group(1));

        // Surefire forwards -Dkrandom.junit.recipe to the forked test JVM, where it arrives as this setting.
        EngineExecutionResults replayed = run(CheckoutFixture.class, Map.of("krandom.junit.recipe", replay.group(2)));

        replayed.testEvents().assertStatistics(stats -> stats.failed(1));
        assertEquals(reportedSeed(failing), reportedSeed(replayed));
        assertEquals(2, CheckoutFixture.EMAILS.size());
        assertEquals(CheckoutFixture.EMAILS.get(0), CheckoutFixture.EMAILS.get(1));
    }

    @Test
    void printedMavenReplayCommandReproducesMethodSourceArguments() {
        DiscountFixture.CUSTOMERS.clear();
        ByteArrayOutputStream stderr = new ByteArrayOutputStream();

        EngineExecutionResults failing = runCapturingStderr(DiscountFixture.class, stderr);
        failing.testEvents().assertStatistics(stats -> stats.failed(1));
        Matcher replay = MAVEN_CLASS_REPLAY.matcher(stderr.toString(StandardCharsets.UTF_8));
        assertTrue(replay.find(), "krandom-junit prints the factory's class-scoped recipe: " + stderr);
        assertEquals(DiscountFixture.class.getName(), replay.group(1));

        // The factory takes the class-scoped configuration, which -Dkrandom.junit.class-recipe restores, and
        // the invocation takes the test's own, which -Dkrandom.junit.recipe restores.
        EngineExecutionResults replayed = run(
                DiscountFixture.class,
                Map.of("krandom.junit.class-recipe", replay.group(3), "krandom.junit.recipe", replay.group(2)));

        replayed.testEvents().assertStatistics(stats -> stats.failed(1));
        assertEquals(2, DiscountFixture.CUSTOMERS.size());
        assertEquals(DiscountFixture.CUSTOMERS.get(0), DiscountFixture.CUSTOMERS.get(1));
    }

    private static EngineExecutionResults runCapturingStderr(Class<?> fixture, ByteArrayOutputStream buffer) {
        PrintStream original = System.err;
        System.setErr(new PrintStream(buffer, true, StandardCharsets.UTF_8));
        try {
            return run(fixture, Map.of());
        } finally {
            System.setErr(original);
        }
    }

    private static EngineExecutionResults run(Class<?> fixture, Map<String, String> configurationParameters) {
        return EngineTestKit.engine("junit-jupiter")
                .selectors(selectClass(fixture))
                .configurationParameter("junit.jupiter.conditions.deactivate", "org.junit.*DisabledCondition")
                .configurationParameters(configurationParameters)
                .execute();
    }

    private static String reportedSeed(EngineExecutionResults results) {
        return results.allEvents().reportingEntryPublished().stream()
                .map(event -> event.getRequiredPayload(ReportEntry.class).getKeyValuePairs())
                .filter(entries -> entries.containsKey(KrandomExtension.REPORT_ENTRY_KEY))
                .map(entries -> entries.get(KrandomExtension.REPORT_ENTRY_KEY))
                .findFirst()
                .orElseThrow();
    }

    /** A realistic unpinned test that fails; executed only through EngineTestKit above. */
    @Disabled("fixture executed through EngineTestKit by JunitReplayExampleTest")
    @ExtendWith(KrandomExtension.class)
    static class CheckoutFixture {

        static final List<String> EMAILS = new CopyOnWriteArrayList<>();

        @Test
        void rejectsDuplicateCustomers(GeneratorConfig config) {
            String email = Generators.ofEmail(config).generate();
            EMAILS.add(email);
            fail("simulated checkout failure for " + email);
        }
    }

    /** An unpinned parameterized test whose factory generates its arguments; executed only through EngineTestKit. */
    @Disabled("fixture executed through EngineTestKit by JunitReplayExampleTest")
    @ExtendWith(KrandomExtension.class)
    static class DiscountFixture {

        static final List<String> CUSTOMERS = new CopyOnWriteArrayList<>();

        static Stream<String> customers(Krandom fake) {
            String customer = fake.email();
            CUSTOMERS.add(customer);
            return Stream.of(customer);
        }

        @ParameterizedTest
        @MethodSource("customers")
        void appliesTheWelcomeDiscount(String customer) {
            fail("simulated discount failure for " + customer);
        }
    }
}
