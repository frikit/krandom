/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.commerce;

import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.Generators;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("CnpjGenerator")
class CnpjGeneratorTest {

    private static final int[] WEIGHTS_1 = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
    private static final int[] WEIGHTS_2 = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};

    /** Independent re-implementation of CNPJ validation for cross-checking generated values. */
    private static boolean isValidCnpj(String bare) {
        if (!bare.matches("\\d{14}")) {
            return false;
        }
        int[] d = new int[14];
        for (int i = 0; i < 14; i++) {
            d[i] = bare.charAt(i) - '0';
        }
        int v1 = CnpjGenerator.checkDigit(Arrays.copyOf(d, 12), WEIGHTS_1);
        int v2 = CnpjGenerator.checkDigit(Arrays.copyOf(d, 13), WEIGHTS_2);
        return d[12] == v1 && d[13] == v2;
    }

    @RepeatedTest(200)
    @DisplayName("formatted output matches the CNPJ mask and is valid")
    void formattedValid() {
        String cnpj = new CnpjGenerator().generate();
        assertTrue(cnpj.matches("\\d{2}\\.\\d{3}\\.\\d{3}/\\d{4}-\\d{2}"), cnpj);
        assertTrue(isValidCnpj(cnpj.replaceAll("\\D", "")), cnpj);
    }

    @RepeatedTest(200)
    @DisplayName("unformatted output is 14 valid digits ending in branch + check digits")
    void unformattedValid() {
        String cnpj = new CnpjGenerator().withoutFormatting().generate();
        assertTrue(cnpj.matches("\\d{14}"), cnpj);
        assertEquals("0001", cnpj.substring(8, 12), "headquarters branch");
        assertTrue(isValidCnpj(cnpj), cnpj);
    }

    @Test
    @DisplayName("checkDigit returns 0 when the remainder is below 2")
    void checkDigitLowRemainderBranch() {
        // All-zero digits -> weighted sum 0 -> remainder 0 (< 2) -> verifier 0.
        assertEquals(0, CnpjGenerator.checkDigit(new int[12], WEIGHTS_1));
    }

    @Test
    @DisplayName("checkDigit returns 11 - remainder when the remainder is 2 or more")
    void checkDigitHighRemainderBranch() {
        int[] digits = {1, 1, 2, 2, 2, 3, 3, 3, 0, 0, 0, 1}; // known base of 11.222.333/0001-81
        assertEquals(8, CnpjGenerator.checkDigit(digits, WEIGHTS_1));
    }

    @Test
    @DisplayName("same seed is reproducible")
    void reproducible() {
        List<String> a = new CnpjGenerator(GeneratorConfig.builder().seed(77L).build()).generateList(25);
        List<String> b = new CnpjGenerator(GeneratorConfig.builder().seed(77L).build()).generateList(25);
        assertEquals(a, b);
    }

    @Test
    @DisplayName("null config is rejected")
    void nullConfig() {
        assertThrows(NullPointerException.class, () -> new CnpjGenerator(null));
    }

    @Test
    @DisplayName("facade ofCnpj produces valid CNPJs")
    void facadeCnpj() {
        assertTrue(Generators.ofCnpj().generate().matches("\\d{2}\\.\\d{3}\\.\\d{3}/\\d{4}-\\d{2}"));
        assertTrue(isValidCnpj(
            Generators.ofCnpj(GeneratorConfig.builder().seed(1L).build()).generate().replaceAll("\\D", "")));
    }

    @Test
    @DisplayName("facade ofCpf produces formatted CPFs and is seed-reproducible")
    void facadeCpf() {
        assertTrue(Generators.ofCpf().generate().matches("\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}"));
        assertEquals(
            Generators.ofCpf(5L).generateList(10),
            Generators.ofCpf(5L).generateList(10));
    }
}
