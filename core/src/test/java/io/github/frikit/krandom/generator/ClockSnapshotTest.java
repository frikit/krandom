/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator;

import io.github.frikit.krandom.generator.datetime.DateGenerator;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class ClockSnapshotTest {
    @ParameterizedTest
    @ValueSource(strings = {"UTC", "Pacific/Auckland", "America/New_York"})
    void snapshotReplaysAllDatesAfterOriginalClockAdvances(String zone) {
        MovingClock clock = new MovingClock(ZoneId.of(zone));
        GeneratorConfig original = GeneratorConfig.builder().seed(42).clock(clock).build();
        GeneratorConfig session = original.snapshotClock();
        DateGenerator generator = new DateGenerator(session);
        List<LocalDate> dates = java.util.stream.IntStream.range(0, 5).mapToObj(i -> generator.future(1)).toList();
        clock.now = clock.now.plusSeconds(86400);
        GeneratorConfig replay = session.getGenerationRecipe().orElseThrow().toGeneratorConfig();
        DateGenerator replayGenerator = new DateGenerator(replay);
        assertEquals(dates, java.util.stream.IntStream.range(0, 5).mapToObj(i -> replayGenerator.future(1)).toList());
        assertSame(clock, original.getClock());
        assertNotEquals(clock.instant(), session.getClock().instant());
        assertEquals(ZoneId.of(zone), session.getClock().getZone());
        assertEquals(session.getClock(), session.snapshotClock().getClock());
    }

    private static final class MovingClock extends Clock {
        private Instant now = Instant.parse("2026-09-04T23:59:59Z");
        private final ZoneId zone;
        private MovingClock(ZoneId zone) { this.zone = zone; }
        public ZoneId getZone() { return zone; }
        public Clock withZone(ZoneId target) { return Clock.fixed(now, target); }
        public Instant instant() { return now; }
    }
}
