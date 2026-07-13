/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator;

import io.github.frikit.krandom.generator.base.DigitGenerator;
import io.github.frikit.krandom.generator.object.ObjectGenerator;
import io.github.frikit.krandom.generator.schema.Schema;
import io.github.frikit.krandom.generator.schema.SchemaValueProvider;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GenerationRecipeGoldenStreamTest {

    private static final GeneratorConfig CONFIG = GeneratorConfig.builder()
                                                               .seed(24680L)
                                                               .clock(Clock.fixed(
                                                                   Instant.parse("2026-07-10T12:00:00Z"), ZoneOffset.UTC))
                                                               .stringLength(6, 6)
                                                               .collectionSize(2, 2)
                                                               .build();

    @Test
    void scalarGoldenStream() {
        assertEquals(List.of("8", "6", "5", "6", "8", "0", "1", "7"),
                     new DigitGenerator(CONFIG).generateList(8));
    }

    @Test
    void objectGoldenStream() {
        assertEquals(new GoldenPerson("YyauCX", -1_007_300_327),
                     new ObjectGenerator<>(GoldenPerson.class, CONFIG).generate());
    }

    @Test
    void schemaGoldenStream() {
        Map<String, SchemaValueProvider> fields = new LinkedHashMap<>();
        fields.put("id", context -> context.random().nextInt(1_000));
        fields.put("active", context -> context.random().nextBoolean());
        assertEquals(List.of(Map.of("id", 515, "active", true), Map.of("id", 978, "active", false)),
                     new Schema(CONFIG, fields).generateBatch(2));
    }

    record GoldenPerson(String code, int score) {
    }
}
