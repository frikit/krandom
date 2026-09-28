package io.github.frikit.krandom.examples;

import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.Generators;
import io.github.frikit.krandom.junit.KrandomExtension;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.engine.reporting.ReportEntry;
import org.junit.platform.testkit.engine.EngineExecutionResults;
import org.junit.platform.testkit.engine.EngineTestKit;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

/**
 * Runs an unpinned failing test, copies the replay value that krandom-junit prints for Maven,
 * and replays the run with it: the replay generates exactly the data of the failing run.
 */
class JunitReplayExampleTest {

    private static final Pattern MAVEN_REPLAY =
        Pattern.compile("mvn test -Dtest='([^']+)' -Dkrandom\\.junit\\.recipe=(base64:[A-Za-z0-9_-]+)");

    @Test
    void printedMavenReplayCommandReproducesTheFailingData() {
        CheckoutFixture.EMAILS.clear();
        ByteArrayOutputStream stderr = new ByteArrayOutputStream();

        EngineExecutionResults failing = runCapturingStderr(stderr);
        failing.testEvents().assertStatistics(stats -> stats.failed(1));
        Matcher replay = MAVEN_REPLAY.matcher(stderr.toString(StandardCharsets.UTF_8));
        assertTrue(replay.find(), "krandom-junit prints a Maven replay command: " + stderr);
        assertEquals(CheckoutFixture.class.getName(), replay.group(1));

        // Surefire forwards -Dkrandom.junit.recipe to the forked test JVM, where it arrives as this setting.
        EngineExecutionResults replayed = run(Map.of("krandom.junit.recipe", replay.group(2)));

        replayed.testEvents().assertStatistics(stats -> stats.failed(1));
        assertEquals(reportedSeed(failing), reportedSeed(replayed));
        assertEquals(2, CheckoutFixture.EMAILS.size());
        assertEquals(CheckoutFixture.EMAILS.get(0), CheckoutFixture.EMAILS.get(1));
    }

    private static EngineExecutionResults runCapturingStderr(ByteArrayOutputStream buffer) {
        PrintStream original = System.err;
        System.setErr(new PrintStream(buffer, true, StandardCharsets.UTF_8));
        try {
            return run(Map.of());
        } finally {
            System.setErr(original);
        }
    }

    private static EngineExecutionResults run(Map<String, String> configurationParameters) {
        return EngineTestKit.engine("junit-jupiter")
                            .selectors(selectClass(CheckoutFixture.class))
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
}
