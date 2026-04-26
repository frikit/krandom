/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.object;

import io.github.frikit.krandom.generator.core.model.PersonWithDateTimes;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("ObjectGenerator — JSR-310 and UUID auto-population")
class ObjectGeneratorDateTimeTest {

    @Test
    @DisplayName("LocalDate field is auto-populated")
    void localDatePopulated() {
        PersonWithDateTimes p = new ObjectGenerator<>(PersonWithDateTimes.class).generate();
        assertNotNull(p.getDob(), "LocalDate field must not be null");
    }

    @Test
    @DisplayName("LocalDateTime field is auto-populated")
    void localDateTimePopulated() {
        PersonWithDateTimes p = new ObjectGenerator<>(PersonWithDateTimes.class).generate();
        assertNotNull(p.getCreatedAt(), "LocalDateTime field must not be null");
    }

    @Test
    @DisplayName("Instant field is auto-populated with non-negative epoch second")
    void instantPopulated() {
        PersonWithDateTimes p = new ObjectGenerator<>(PersonWithDateTimes.class).generate();
        assertNotNull(p.getUpdatedAt(), "Instant field must not be null");
        assertTrue(p.getUpdatedAt().getEpochSecond() >= 0,
                   "Instant must have a non-negative epoch second");
    }

    @Test
    @DisplayName("ZonedDateTime field is auto-populated")
    void zonedDateTimePopulated() {
        PersonWithDateTimes p = new ObjectGenerator<>(PersonWithDateTimes.class).generate();
        assertNotNull(p.getScheduledAt(), "ZonedDateTime field must not be null");
        assertNotNull(p.getScheduledAt().getZone(), "ZonedDateTime must have a zone");
    }

    @Test
    @DisplayName("UUID field is auto-populated")
    void uuidPopulated() {
        PersonWithDateTimes p = new ObjectGenerator<>(PersonWithDateTimes.class).generate();
        assertNotNull(p.getId(), "UUID field must not be null");
    }

    @Test
    @DisplayName("all date/time and UUID fields are populated in a single generate() call")
    void allFieldsPopulatedTogether() {
        PersonWithDateTimes p = new ObjectGenerator<>(PersonWithDateTimes.class).generate();
        assertNotNull(p.getDob(), "LocalDate");
        assertNotNull(p.getCreatedAt(), "LocalDateTime");
        assertNotNull(p.getUpdatedAt(), "Instant");
        assertNotNull(p.getScheduledAt(), "ZonedDateTime");
        assertNotNull(p.getId(), "UUID");
    }

    @Test
    @DisplayName("generateList produces correctly populated instances")
    void generateListPopulated() {
        var list = new ObjectGenerator<>(PersonWithDateTimes.class).generateList(10);
        assertEquals(10, list.size());
        list.forEach(p -> {
            assertNotNull(p.getDob());
            assertNotNull(p.getCreatedAt());
            assertNotNull(p.getUpdatedAt());
            assertNotNull(p.getScheduledAt());
            assertNotNull(p.getId());
        });
    }
}
