/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.base;

import io.github.frikit.krandom.generator.Generators;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

@DisplayName("ConstantGenerator")
class ConstantGeneratorTest {

    @Test
    @DisplayName("generate returns same value repeatedly")
    void generateReturnsSameValueRepeatedly() {
        ConstantGenerator<String> generator = new ConstantGenerator<>("fixed");

        assertSame(generator.generate(), generator.generate());
    }

    @Test
    @DisplayName("null constants are supported")
    void nullConstantsAreSupported() {
        assertNull(new ConstantGenerator<>(null).generate());
    }

    @Test
    @DisplayName("object identity is preserved")
    void objectIdentityIsPreserved() {
        List<String> value = new ArrayList<>();
        ConstantGenerator<List<String>> generator = Generators.ofConstant(value);

        assertSame(value, generator.generate());
        assertSame(value, generator.value());
    }

    @Test
    @DisplayName("ofConstant exposes generic constant generator")
    void ofConstantExposesGenericConstantGenerator() {
        Integer value = 42;
        ConstantGenerator<Integer> generator = Generators.ofConstant(value);

        assertSame(value, generator.generate());
    }
}
