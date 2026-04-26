/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.datetime;

import io.github.frikit.krandom.generator.GeneratorConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Legacy date/time generators")
class LegacyDateGeneratorsTest {

    private static final LocalDate MIN            = LocalDate.of(2020, 1, 1);
    private static final LocalDate MAX            = LocalDate.of(2020, 1, 3);
    private static final long      MILLIS_PER_DAY = 24L * 60L * 60L * 1000L;

    @Test
    @DisplayName("default constructors generate non-null values")
    void defaultConstructorsGenerateNonNull() {
        assertNotNull(new UtilDateGenerator().generate());
        assertNotNull(new SqlDateGenerator().generate());
        assertNotNull(new SqlTimeGenerator().generate());
        assertNotNull(new SqlTimestampGenerator().generate());
    }

    @Test
    @DisplayName("config constructors validate null")
    void configConstructorsValidateNull() {
        assertThrows(NullPointerException.class, () -> new UtilDateGenerator(null));
        assertThrows(NullPointerException.class, () -> new SqlDateGenerator(null));
        assertThrows(NullPointerException.class, () -> new SqlTimeGenerator(null));
        assertThrows(NullPointerException.class, () -> new SqlTimestampGenerator(null));
    }

    @Test
    @DisplayName("seeded config constructors produce reproducible values")
    void configConstructorsSeeded() {
        GeneratorConfig cfg = GeneratorConfig.builder().seed(123L).build();

        UtilDateGenerator utilA = new UtilDateGenerator(cfg);
        UtilDateGenerator utilB = new UtilDateGenerator(cfg);
        assertEquals(utilA.generate(), utilB.generate());

        SqlDateGenerator sqlDateA = new SqlDateGenerator(cfg);
        SqlDateGenerator sqlDateB = new SqlDateGenerator(cfg);
        assertEquals(sqlDateA.generate(), sqlDateB.generate());

        SqlTimestampGenerator tsA = new SqlTimestampGenerator(cfg);
        SqlTimestampGenerator tsB = new SqlTimestampGenerator(cfg);
        assertEquals(tsA.generate(), tsB.generate());
    }

    @Test
    @DisplayName("range constructors validate min <= max")
    void rangeConstructorsValidateOrder() {
        assertThrows(IllegalArgumentException.class, () -> new UtilDateGenerator(MAX, MIN));
        assertThrows(IllegalArgumentException.class, () -> new SqlDateGenerator(MAX, MIN));
        assertThrows(IllegalArgumentException.class, () -> new SqlTimestampGenerator(MAX, MIN));
    }

    @Test
    @DisplayName("range constructors generate values inside configured date bounds")
    void rangeConstructorsRespectBounds() {
        UtilDateGenerator utilDateGenerator = new UtilDateGenerator(MIN, MAX);
        SqlDateGenerator sqlDateGenerator = new SqlDateGenerator(MIN, MAX);
        SqlTimestampGenerator sqlTimestampGenerator = new SqlTimestampGenerator(MIN, MAX);

        for (int i = 0; i < 50; i++) {
            LocalDate utilDate = utilDateGenerator.generate().toInstant().atZone(ZoneOffset.UTC).toLocalDate();
            LocalDate sqlDate = sqlDateGenerator.generate().toLocalDate();
            LocalDate sqlTimestamp = sqlTimestampGenerator.generate().toInstant().atZone(ZoneOffset.UTC).toLocalDate();

            assertFalse(utilDate.isBefore(MIN));
            assertFalse(utilDate.isAfter(MAX));
            assertFalse(sqlDate.isBefore(MIN));
            assertFalse(sqlDate.isAfter(MAX));
            assertFalse(sqlTimestamp.isBefore(MIN));
            assertFalse(sqlTimestamp.isAfter(MAX));
        }
    }

    @Test
    @DisplayName("SqlTimeGenerator stays within one day and supports seeded reproducibility")
    void sqlTimeGeneratorWithinDayAndSeeded() {
        SqlTimeGenerator a = new SqlTimeGenerator(GeneratorConfig.builder().seed(42L).build());
        SqlTimeGenerator b = new SqlTimeGenerator(GeneratorConfig.builder().seed(42L).build());

        long firstA = a.generate().getTime();
        long firstB = b.generate().getTime();
        assertEquals(firstA, firstB);
        assertTrue(firstA >= 0);
        assertTrue(firstA < MILLIS_PER_DAY);
    }
}
