/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator;

import io.github.frikit.krandom.generator.base.AtomicIntegerGenerator;
import io.github.frikit.krandom.generator.base.AtomicLongGenerator;
import io.github.frikit.krandom.generator.base.BigDecimalGenerator;
import io.github.frikit.krandom.generator.base.BigIntegerGenerator;
import io.github.frikit.krandom.generator.base.BooleanGenerator;
import io.github.frikit.krandom.generator.base.ByteGenerator;
import io.github.frikit.krandom.generator.base.CharGenerator;
import io.github.frikit.krandom.generator.base.IntGenerator;
import io.github.frikit.krandom.generator.base.LongGenerator;
import io.github.frikit.krandom.generator.base.NaturalNumberGenerator;
import io.github.frikit.krandom.generator.base.NormalDistributionGenerator;
import io.github.frikit.krandom.generator.base.PrimeGenerator;
import io.github.frikit.krandom.generator.base.RegexGenerator;
import io.github.frikit.krandom.generator.base.ShortGenerator;
import io.github.frikit.krandom.generator.base.StringGenerator;
import io.github.frikit.krandom.generator.datetime.YearGenerator;
import io.github.frikit.krandom.generator.datetime.YearMonthGenerator;
import io.github.frikit.krandom.generator.selection.FinitePoolGenerator;
import io.github.frikit.krandom.generator.selection.PickGenerator;
import io.github.frikit.krandom.generator.selection.PickSetGenerator;
import io.github.frikit.krandom.generator.selection.ShuffleGenerator;
import io.github.frikit.krandom.generator.selection.WeightedGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("GeneratorConfig constructor forms")
class ConfigurationFormsTest {

    private static final long SEED = 424242L;
    private static final List<String> ITEMS = List.of("alpha", "beta", "gamma", "delta", "epsilon");

    private static GeneratorConfig cfg() {
        return GeneratorConfig.builder().seed(SEED).build();
    }

    @Test
    @DisplayName("configuration forms reject a null configuration")
    void configFormsRejectNull() {
        GeneratorConfig none = null;
        List<Runnable> constructors = List.of(
            () -> new AtomicIntegerGenerator(1, 2, none),
            () -> new AtomicLongGenerator(1L, 2L, none),
            () -> new BigDecimalGenerator(none),
            () -> new BigDecimalGenerator(BigDecimal.ONE, BigDecimal.TEN, 2, none),
            () -> new BigIntegerGenerator(none),
            () -> new BigIntegerGenerator(BigInteger.ONE, BigInteger.TEN, none),
            () -> new BooleanGenerator(none),
            () -> new ByteGenerator(none),
            () -> new ByteGenerator((byte) 1, (byte) 2, none),
            () -> new IntGenerator(none),
            () -> new IntGenerator(1, 2, none),
            () -> new LongGenerator(none),
            () -> new LongGenerator(1L, 2L, none),
            () -> new NaturalNumberGenerator(none),
            () -> new NaturalNumberGenerator(1, 2, none),
            () -> new NormalDistributionGenerator(none),
            () -> new NormalDistributionGenerator(0.0, 1.0, none),
            () -> new PrimeGenerator(none),
            () -> new PrimeGenerator(2, 10, none),
            () -> new RegexGenerator("a", none),
            () -> new ShortGenerator(none),
            () -> new ShortGenerator((short) 1, (short) 2, none),
            () -> new YearGenerator(1, 2, none),
            () -> new YearMonthGenerator(1, 2, none),
            () -> new PickGenerator<>(ITEMS, none),
            () -> new PickSetGenerator<>(ITEMS, 1, none),
            () -> new FinitePoolGenerator<>(ITEMS, none),
            () -> new ShuffleGenerator<>(ITEMS, none),
            () -> new WeightedGenerator<>(ITEMS, List.of(1, 1, 1, 1, 1), none),
            () -> CharGenerator.letters(none),
            () -> StringGenerator.letters(none),
            () -> StringGenerator.builder().config(none));
        for (Runnable constructor : constructors) {
            assertThrows(NullPointerException.class, constructor::run);
        }
    }

    @Test
    @DisplayName("default-range configuration forms use the documented default ranges")
    void defaultRangeConfigForms() {
        GeneratorConfig config = cfg();
        assertEquals(sample(new ByteGenerator()).size(), sample(new ByteGenerator(config)).size());
        for (Object value : sample(new PrimeGenerator(config))) {
            int prime = Integer.parseInt(value.toString());
            assertTrue(prime >= 2 && prime < 1000, value.toString());
        }
        for (Object value : sample(new NaturalNumberGenerator(config))) {
            assertTrue(Integer.parseInt(value.toString()) >= 0, value.toString());
        }
        for (Object value : sample(new NormalDistributionGenerator(config))) {
            assertTrue(Double.isFinite(Double.parseDouble(value.toString())), value.toString());
        }
        for (Object value : sample(new BigDecimalGenerator(config))) {
            BigDecimal decimal = new BigDecimal(value.toString());
            assertTrue(decimal.signum() >= 0 && decimal.scale() == 2, value.toString());
        }
        for (Object value : sample(new BigIntegerGenerator(config))) {
            assertTrue(new BigInteger(value.toString()).signum() >= 0, value.toString());
        }
        assertEquals(sample(new IntGenerator(cfg())), sample(new IntGenerator(cfg())));
        assertEquals(sample(new LongGenerator(cfg())), sample(new LongGenerator(cfg())));
        assertEquals(sample(new ShortGenerator(cfg())), sample(new ShortGenerator(cfg())));
    }

    @Test
    @DisplayName("character and string configuration factories reproduce seeded builders")
    void charAndStringConfigFactories() {
        assertEquals(sample(CharGenerator.letters().withSeed(SEED)), sample(CharGenerator.letters(cfg())));
        assertEquals(sample(StringGenerator.builder().charGenerator(CharGenerator.letters()).seed(SEED).build()),
                     sample(StringGenerator.letters(cfg())));
        assertEquals(sample(StringGenerator.builder().charGenerator(CharGenerator.digits()).length(6).seed(SEED).build()),
                     sample(StringGenerator.builder().charGenerator(CharGenerator.digits()).length(6).config(cfg()).build()));
    }

    private static List<String> sample(Generator<?> generator) {
        List<String> values = new ArrayList<>();
        for (int i = 0; i < 25; i++) {
            values.add(String.valueOf(generator.generate()));
        }
        return values;
    }
}
