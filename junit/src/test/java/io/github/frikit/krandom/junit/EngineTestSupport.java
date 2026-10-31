/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.junit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.Krandom;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import org.junit.platform.engine.reporting.ReportEntry;
import org.junit.platform.testkit.engine.EngineExecutionResults;
import org.junit.platform.testkit.engine.EngineTestKit;

/** Shared helpers for running {@code @Disabled} fixtures through {@link EngineTestKit}. */
final class EngineTestSupport {

    private EngineTestSupport() {}

    static EngineExecutionResults run(Class<?> fixture) {
        return run(fixture, Map.of());
    }

    static EngineExecutionResults run(Class<?> fixture, Map<String, String> configurationParameters) {
        return EngineTestKit.engine("junit-jupiter")
                .selectors(selectClass(fixture))
                .configurationParameter("junit.jupiter.conditions.deactivate", "org.junit.*")
                .configurationParameters(configurationParameters)
                .execute();
    }

    static Captured runCapturingStderr(Class<?> fixture) {
        return runCapturingStderr(fixture, Map.of());
    }

    static Captured runCapturingStderr(Class<?> fixture, Map<String, String> configurationParameters) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        PrintStream original = System.err;
        System.setErr(new PrintStream(buffer, true, StandardCharsets.UTF_8));
        try {
            EngineExecutionResults results = run(fixture, configurationParameters);
            return new Captured(results, buffer.toString(StandardCharsets.UTF_8));
        } finally {
            System.setErr(original);
        }
    }

    static String singleReportEntry(EngineExecutionResults results, String key) {
        List<ReportEntry> entries = results.allEvents().reportingEntryPublished().stream()
                .map(event -> event.getRequiredPayload(ReportEntry.class))
                .filter(entry -> entry.getKeyValuePairs().containsKey(key))
                .toList();
        assertEquals(1, entries.size(), "expected exactly one '" + key + "' report entry");
        return entries.get(0).getKeyValuePairs().get(key);
    }

    /** Draws the next value of one session stream, wide enough that a repeat means a restarted stream. */
    static int draw(Krandom session) {
        return session.intBetween(Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    /** Builds a new session with this seed, whose streams start from their beginning. */
    static Krandom newSession(long seed) {
        return Krandom.of(GeneratorConfig.builder().seed(seed).build());
    }

    record Captured(EngineExecutionResults results, String stderr) {}
}
