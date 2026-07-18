/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.selection;

import io.github.frikit.krandom.generator.Generator;
import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.Generators;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.ArrayList;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Sequence and finite-pool generators")
class SequenceAndFinitePoolGeneratorTest {

    @Test
    void sequenceRespectsLengthAndNullProbability() {
        Generator<String> first = () -> "first";
        Generator<String> second = () -> "second";
        SequenceGenerator<String> generator = new SequenceGenerator<>(
            GeneratorConfig.builder().seed(42L).build(), List.of(first, second), 3, 5, 0.0);

        List<String> values = generator.generate();

        assertTrue(values.size() >= 3 && values.size() <= 5);
        assertTrue(values.stream().allMatch(value -> value.equals("first") || value.equals("second")));
        assertTrue(new SequenceGenerator<>(GeneratorConfig.builder().seed(1L).build(), List.of(first), 2, 2, 1.0)
                       .generate()
                       .stream()
                       .allMatch(value -> value == null));
    }

    @Test
    void sequenceIsSeededAndValidatesArguments() {
        Generator<String> source = () -> "value";
        GeneratorConfig config = GeneratorConfig.builder().seed(9L).build();

        assertEquals(new SequenceGenerator<>(config, List.of(source), 1, 4, 0.5).generate(),
                     new SequenceGenerator<>(config, List.of(source), 1, 4, 0.5).generate());
        assertThrows(NullPointerException.class, () -> new SequenceGenerator<String>(null, List.of(source), 1, 1, 0));
        assertThrows(IllegalArgumentException.class, () -> new SequenceGenerator<>(config, List.of(), 1, 1, 0));
        assertThrows(IllegalArgumentException.class, () -> new SequenceGenerator<>(config, List.of(source), -1, 1, 0));
        assertThrows(IllegalArgumentException.class, () -> new SequenceGenerator<>(config, List.of(source), 2, 1, 0));
        assertThrows(IllegalArgumentException.class, () -> new SequenceGenerator<>(config, List.of(source), 1, 1, -0.1));
        assertThrows(IllegalArgumentException.class, () -> new SequenceGenerator<>(config, List.of(source), 1, 1, 1.1));
        assertEquals(2, new SequenceGenerator<>(List.of(source), 2, 2).generate().size());
        assertEquals(1, Generators.sequence(List.of(source), 1, 1).generate().size());
        assertEquals(1, Generators.sequence(config, List.of(source), 1, 1, 0.0).generate().size());
        List<Generator<? extends String>> nullSource = new ArrayList<>();
        nullSource.add(null);
        assertThrows(NullPointerException.class, () -> new SequenceGenerator<>(config, nullSource, 1, 1, 0));
    }

    @Test
    void finitePoolEmitsEveryValueOnceThenExhausts() {
        FinitePoolGenerator<String> pool = new FinitePoolGenerator<>(List.of("one", "two", "three"), 2L);

        List<String> values = List.of(pool.generate(), pool.generate(), pool.generate());

        assertEquals(3, values.stream().distinct().count());
        assertEquals(0, pool.remaining());
        assertThrows(NoSuchElementException.class, pool::generate);
        pool.reset();
        assertEquals(3, pool.remaining());
        assertTrue(List.of("one", "two", "three").contains(pool.generate()));
        assertThrows(NullPointerException.class, () -> new FinitePoolGenerator<String>(null));
        assertThrows(IllegalArgumentException.class, () -> new FinitePoolGenerator<>(List.of()));
        assertThrows(NullPointerException.class, () -> new FinitePoolGenerator<>(List.of("one", null)));
        assertTrue(List.of("a", "b").contains(Generators.pool(List.of("a", "b")).generate()));
        assertTrue(List.of("a", "b").contains(new FinitePoolGenerator<>(List.of("a", "b")).generate()));
    }
}
