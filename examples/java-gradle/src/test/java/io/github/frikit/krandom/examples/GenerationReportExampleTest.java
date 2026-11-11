/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.examples;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.object.FixtureRule;
import io.github.frikit.krandom.generator.object.FixtureRuleKind;
import io.github.frikit.krandom.generator.object.ObjectFaker;
import io.github.frikit.krandom.generator.report.GenerationReportEntry;
import io.github.frikit.krandom.generator.report.GenerationStep;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * A {@code GenerationReportListener} set on the {@link GeneratorConfig} receives one entry for each value
 * that object generation produces: its path, the step and generator that produced it, and the rule that
 * matched it. An entry never holds the value itself.
 */
class GenerationReportExampleTest {

    @Test
    void listenerReportsHowEachMemberWasProduced() {
        List<GenerationReportEntry> entries = new ArrayList<>();
        GeneratorConfig config = GeneratorConfig.builder()
                .seed(42L)
                .generationReportListener(entries::add)
                .build();

        new ObjectFaker<>(UserFixture.class, config)
                .ruleFor(UserFixture::country, () -> "United Kingdom")
                .generate();

        GenerationReportEntry email = entryAt(entries, "UserFixture.email");
        assertEquals(GenerationStep.SEMANTIC, email.step());
        assertEquals("email", email.generator());
        assertEquals(Optional.empty(), email.matchedRule());

        GenerationReportEntry country = entryAt(entries, "UserFixture.country");
        assertEquals(GenerationStep.OVERRIDE, country.step());
        assertEquals(Optional.of(FixtureRule.registered(FixtureRuleKind.RULE, "country", 1)), country.matchedRule());
    }

    private static GenerationReportEntry entryAt(List<GenerationReportEntry> entries, String path) {
        return entries.stream()
                .filter(entry -> entry.path().equals(path))
                .findFirst()
                .orElseThrow();
    }
}
