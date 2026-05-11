/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.object;

import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.Generators;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@DisplayName("k-random reference parity — determinism guarantees")
class KRandomReferenceDeterminismParityTest {

    @Test
    @DisplayName("migrated nextObject examples are repeatable with the same krandom seed")
    void migratedNextObjectExamplesAreRepeatableWithSameSeed() {
        GeneratorConfig config = migrationConfig(42L);

        MigratedOrder first = Generators.ofObject(MigratedOrder.class, config).generate();
        MigratedOrder second = Generators.ofObject(MigratedOrder.class, config).generate();
        MigratedOrder different = Generators.ofObject(MigratedOrder.class, migrationConfig(43L)).generate();

        assertEquals(first, second);
        assertNotEquals(first, different);
    }

    @Test
    @DisplayName("migrated objects(Class, size) examples are repeatable with the same krandom seed")
    void migratedBulkObjectExamplesAreRepeatableWithSameSeed() {
        List<MigratedOrder> first = Generators.ofObject(MigratedOrder.class, migrationConfig(100L)).generateList(4);
        List<MigratedOrder> second = Generators.ofObject(MigratedOrder.class, migrationConfig(100L)).generateList(4);

        assertEquals(first, second);
    }

    @Test
    @DisplayName("migrated field and type override examples remain repeatable")
    void migratedOverrideExamplesRemainRepeatable() {
        GeneratorConfig firstConfig = migrationConfig(77L).toBuilder()
                                                          .objectOverride(MigratedOrder.class,
                                                                          "trackingCode",
                                                                          () -> "TRACK-42")
                                                          .objectOverride(ExternalId.class,
                                                                          () -> new ExternalId("external-42"))
                                                          .build();
        GeneratorConfig secondConfig = migrationConfig(77L).toBuilder()
                                                           .objectOverride(MigratedOrder.class,
                                                                           "trackingCode",
                                                                           () -> "TRACK-42")
                                                           .objectOverride(ExternalId.class,
                                                                           () -> new ExternalId("external-42"))
                                                           .build();

        assertEquals(Generators.ofObject(MigratedOrder.class, firstConfig).generate(),
                     Generators.ofObject(MigratedOrder.class, secondConfig).generate());
    }

    @Test
    @DisplayName("migrated faker/domain generator examples are repeatable with the same krandom seed")
    void migratedDomainGeneratorExamplesAreRepeatableWithSameSeed() {
        GeneratorConfig config = GeneratorConfig.builder()
                                                .seed(20260511L)
                                                .locale(Locale.US)
                                                .build();

        List<String> first = Generators.ofEmail(config).generateList(5);
        List<String> second = Generators.ofEmail(config).generateList(5);

        assertEquals(first, second);
    }

    private static GeneratorConfig migrationConfig(long seed) {
        return GeneratorConfig.builder()
                              .seed(seed)
                              .stringLength(3, 8)
                              .collectionSize(2, 3)
                              .objectMaxDepth(3)
                              .objectOptionalEmptyProbability(0.0)
                              .objectSemanticMode(ObjectGenerationSemanticMode.STRUCTURAL_ONLY)
                              .build();
    }

    record MigratedOrder(
        String trackingCode,
        int quantity,
        List<Integer> scores,
        Map<String, Integer> attributes,
        Optional<String> note,
        MigratedCustomer customer,
        ExternalId externalId
    ) {}

    record MigratedCustomer(String name, boolean active) {}

    record ExternalId(String value) {}
}
