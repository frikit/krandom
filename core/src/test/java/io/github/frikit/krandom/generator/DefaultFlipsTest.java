/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.frikit.krandom.generator.base.NormalDistributionGenerator;
import io.github.frikit.krandom.generator.finance.CurrencyGenerator;
import io.github.frikit.krandom.generator.finance.MoneyGenerator;
import java.util.List;
import java.util.function.UnaryOperator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * From 4.0.0, {@code advancingConvenienceStreams}, {@code strictMathGaussian},
 * {@code objectReseedBuiltInRandomizers} and {@code germanStreetLayout} are on by default, and setting one to
 * {@code false} keeps its 3.x behaviour: a default configuration gives the values {@link DefaultFlipPinsTest}
 * pins with all four on.
 */
@DisplayName("from 4.0.0 the four seeding settings are on by default")
class DefaultFlipsTest {

    @Test
    @DisplayName("a default configuration, a builder and its copies have all four on")
    void allFourAreOnByDefault() {
        for (GeneratorConfig config : List.of(
                GeneratorConfig.defaults(),
                GeneratorConfig.builder().build(),
                GeneratorConfig.builder().seed(1L).build().forChildStream("child"),
                GeneratorConfig.defaults().toBuilder().build())) {
            assertTrue(config.isAdvancingConvenienceStreams());
            assertTrue(config.isStrictMathGaussian());
            assertTrue(config.isObjectReseedBuiltInRandomizers());
            assertTrue(config.isGermanStreetLayout());
        }
    }

    @Test
    @DisplayName("each can still be turned off on its own")
    void eachCanBeTurnedOff() {
        GeneratorConfig off =
                DefaultFlipPinsTest.ALL_OFF.apply(GeneratorConfig.builder()).build();
        assertFalse(off.isAdvancingConvenienceStreams());
        assertFalse(off.isStrictMathGaussian());
        assertFalse(off.isObjectReseedBuiltInRandomizers());
        assertFalse(off.isGermanStreetLayout());
    }

    @Test
    @DisplayName("a default seeded configuration gives the values pinned with all four on")
    void defaultsGiveTheOnValues() {
        assertEquals(DefaultFlipPinsTest.ON_VALUES, DefaultFlipPinsTest.values(UnaryOperator.identity()));
    }

    @Test
    @DisplayName("by default a price tag advances its money stream and a Gaussian draw uses StrictMath")
    void defaultsAdvanceAndDrawWithStrictMath() {
        for (long seed = 0; seed < 50; seed++) {
            GeneratorConfig defaults = DefaultFlipPinsTest.seeded(seed, "en-US", UnaryOperator.identity());
            GeneratorConfig on = DefaultFlipPinsTest.seeded(seed, "en-US", DefaultFlipPinsTest.ALL_ON);
            assertEquals(
                    new NormalDistributionGenerator(on).generateList(6),
                    new NormalDistributionGenerator(defaults).generateList(6),
                    "seed " + seed);
            CurrencyGenerator tags = new CurrencyGenerator(defaults);
            MoneyGenerator money = new MoneyGenerator(on.forChildStream("money"));
            for (int i = 0; i < 3; i++) {
                assertEquals(money.generate(), tags.generatePriceTag(), "seed " + seed);
            }
        }
    }
}
