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

@DisplayName("object generator determinism guarantees")
class ObjectGeneratorDeterminismTest {

    @Test
    @DisplayName("single object generation repeats with the same configured seed")
    void singleObjectGenerationRepeatsWithSameSeed() {
        GeneratorConfig config = deterministicConfig(42L);

        OrderFixture first = Generators.ofObject(OrderFixture.class, config).generate();
        OrderFixture second = Generators.ofObject(OrderFixture.class, config).generate();
        OrderFixture different = Generators.ofObject(OrderFixture.class, deterministicConfig(43L)).generate();

        assertEquals(first, second);
        assertNotEquals(first, different);
    }

    @Test
    @DisplayName("bulk object generation repeats with the same configured seed")
    void bulkObjectGenerationRepeatsWithSameSeed() {
        List<OrderFixture> first = Generators.ofObject(OrderFixture.class, deterministicConfig(100L)).generateList(4);
        List<OrderFixture> second = Generators.ofObject(OrderFixture.class, deterministicConfig(100L)).generateList(4);

        assertEquals(first, second);
    }

    @Test
    @DisplayName("field and type overrides remain repeatable")
    void overrideExamplesRemainRepeatable() {
        GeneratorConfig firstConfig = deterministicConfig(77L).toBuilder()
                                                          .objectOverride(OrderFixture.class,
                                                                          "trackingCode",
                                                                          () -> "TRACK-42")
                                                          .objectOverride(ExternalId.class,
                                                                          () -> new ExternalId("external-42"))
                                                          .build();
        GeneratorConfig secondConfig = deterministicConfig(77L).toBuilder()
                                                           .objectOverride(OrderFixture.class,
                                                                           "trackingCode",
                                                                           () -> "TRACK-42")
                                                           .objectOverride(ExternalId.class,
                                                                           () -> new ExternalId("external-42"))
                                                           .build();

        assertEquals(Generators.ofObject(OrderFixture.class, firstConfig).generate(),
                     Generators.ofObject(OrderFixture.class, secondConfig).generate());
    }

    @Test
    @DisplayName("domain generators repeat with the same configured seed")
    void domainGeneratorsRepeatWithSameSeed() {
        GeneratorConfig config = GeneratorConfig.builder()
                                                .seed(20260511L)
                                                .locale(Locale.US)
                                                .build();

        List<String> first = Generators.ofEmail(config).generateList(5);
        List<String> second = Generators.ofEmail(config).generateList(5);

        assertEquals(first, second);
    }

    private static GeneratorConfig deterministicConfig(long seed) {
        return GeneratorConfig.builder()
                              .seed(seed)
                              .stringLength(3, 8)
                              .collectionSize(2, 3)
                              .objectMaxDepth(3)
                              .objectOptionalEmptyProbability(0.0)
                              .objectSemanticMode(ObjectGenerationSemanticMode.STRUCTURAL_ONLY)
                              .build();
    }

    record OrderFixture(
        String trackingCode,
        int quantity,
        List<Integer> scores,
        Map<String, Integer> attributes,
        Optional<String> note,
        CustomerFixture customer,
        ExternalId externalId
    ) {}

    record CustomerFixture(String name, boolean active) {}

    record ExternalId(String value) {}
}
