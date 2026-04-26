/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.algorithms;

import io.github.frikit.krandom.generator.Generators;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("LuhnGenerator")
class LuhnGeneratorTest {

    private static final int SAMPLES = 200;

    private static boolean isValidLuhn(String number) {
        int[] digits = new int[number.length()];
        for (int i = 0; i < number.length(); i++) {
            digits[i] = Integer.parseInt(number.substring(i, i + 1));
        }
        for (int i = digits.length - 2; i >= 0; i -= 2) {
            int d = digits[i] * 2;
            if (d > 9) d = d % 10 + 1;
            digits[i] = d;
        }
        int sum = 0;
        for (int d : digits) sum += d;
        return sum % 10 == 0;
    }

    @RepeatedTest(50)
    @DisplayName("generate() produces exactly 10 characters")
    void generateLength() {
        assertEquals(10, new LuhnGenerator().generate().length());
    }

    @RepeatedTest(50)
    @DisplayName("generate() produces only digit characters")
    void generateOnlyDigits() {
        String value = new LuhnGenerator().generate();
        assertTrue(value.matches("[0-9]+"), "Expected digits only, got: " + value);
    }

    @RepeatedTest(50)
    @DisplayName("generate() passes the Luhn validity check")
    void generatePassesLuhnCheck() {
        String value = new LuhnGenerator().generate();
        assertTrue(isValidLuhn(value), "Luhn check failed for: " + value);
    }

    @Test
    @DisplayName("generate() produces distinct values across samples")
    void generateProducesVariety() {
        LuhnGenerator gen = new LuhnGenerator();
        Set<String> seen = new HashSet<>();
        for (int i = 0; i < SAMPLES; i++) seen.add(gen.generate());
        assertTrue(seen.size() > 1, "Expected distinct values over " + SAMPLES + " samples");
    }

    @Test
    @DisplayName("generateList returns all Luhn-valid strings")
    void generateList() {
        List<String> list = new LuhnGenerator().generateList(20);
        assertEquals(20, list.size());
        list.forEach(v -> {
            assertEquals(10, v.length(), "Wrong length: " + v);
            assertTrue(v.matches("[0-9]+"), "Not all digits: " + v);
            assertTrue(isValidLuhn(v), "Luhn check failed: " + v);
        });
    }

    @Test
    @DisplayName("stream produces on-demand Luhn-valid values")
    void streamUsage() {
        new LuhnGenerator().stream().limit(30)
                           .forEach(v -> assertTrue(isValidLuhn(v), "Luhn check failed: " + v));
    }

    @Test
    @DisplayName("map can transform generated strings")
    void mapTransformation() {
        String value = new LuhnGenerator().map(s -> "LUHN-" + s).generate();
        assertTrue(value.startsWith("LUHN-"), "Expected 'LUHN-' prefix, got: " + value);
    }

    // ── Luhn validation helper ─────────────────────────────────────────────────

    @Test
    @DisplayName("Generators.ofLuhn() factory returns a LuhnGenerator")
    void factoryMethod() {
        assertInstanceOf(LuhnGenerator.class, Generators.ofLuhn());
    }
}
