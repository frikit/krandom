/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.user;

import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.Generators;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("PassportGenerator")
class PassportGeneratorTest {

    @RepeatedTest(200)
    @DisplayName("output matches the generic passport format")
    void formatMatches() {
        String passport = new PassportGenerator().generate();
        assertTrue(passport.matches("[A-Z][0-9]{8}"), passport);
    }

    @Test
    @DisplayName("same seed is reproducible")
    void reproducible() {
        List<String> a = new PassportGenerator(GeneratorConfig.builder().seed(77L).build()).generateList(25);
        List<String> b = new PassportGenerator(GeneratorConfig.builder().seed(77L).build()).generateList(25);
        assertEquals(a, b);
    }

    @Test
    @DisplayName("null config is rejected")
    void nullConfig() {
        assertThrows(NullPointerException.class, () -> new PassportGenerator(null));
    }

    @Test
    @DisplayName("facade ofPassport produces valid passport numbers and is seed-reproducible")
    void facade() {
        assertTrue(Generators.ofPassport().generate().matches("[A-Z][0-9]{8}"));
        assertEquals(
            Generators.ofPassport(GeneratorConfig.builder().seed(1L).build()).generateList(10),
            Generators.ofPassport(GeneratorConfig.builder().seed(1L).build()).generateList(10));
    }
}