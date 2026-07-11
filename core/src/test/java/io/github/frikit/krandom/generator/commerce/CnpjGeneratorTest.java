/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.commerce;

import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.Generators;
import io.github.frikit.krandom.generator.BusinessTaxIdentifierSafetyPolicy;
import io.github.frikit.krandom.generator.user.nationalid.NationalIdSafetyPolicy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("CnpjGenerator")
@SuppressWarnings("removal")
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

    /** Independent implementation of the Receita Federal alphanumeric CNPJ check-digit rules. */
    private static boolean isValidAlphanumericCnpj(String bare) {
        if (!bare.matches("[0-9A-Z]{14}")) {
            return false;
        }
        int v1 = alphanumericCheckDigit(bare.substring(0, 12), WEIGHTS_1);
        int v2 = alphanumericCheckDigit(bare.substring(0, 12) + v1, WEIGHTS_2);
        return bare.charAt(12) == (char) ('0' + v1) && bare.charAt(13) == (char) ('0' + v2);
    }

    private static int alphanumericCheckDigit(String body, int[] weights) {
        int sum = 0;
        for (int index = 0; index < body.length(); index++) {
            sum += (body.charAt(index) - '0') * weights[index];
        }
        int remainder = sum % 11;
        return remainder < 2 ? 0 : 11 - remainder;
    }

    @RepeatedTest(200)
    @DisplayName("deprecated no-argument constructor preserves formatted valid output")
    void formattedValid() {
        String cnpj = new CnpjGenerator(GeneratorConfig.builder() .businessTaxIdentifierSafetyPolicy( BusinessTaxIdentifierSafetyPolicy.REALISTIC_UNCLASSIFIED) .build()).generate();
        assertTrue(cnpj.matches("\\d{2}\\.\\d{3}\\.\\d{3}/\\d{4}-\\d{2}"), cnpj);
        assertTrue(isValidCnpj(cnpj.replaceAll("\\D", "")), cnpj);
    }

    @RepeatedTest(200)
    @DisplayName("deprecated no-argument constructor preserves unformatted valid output")
    void unformattedValid() {
        String cnpj = new CnpjGenerator(GeneratorConfig.builder() .businessTaxIdentifierSafetyPolicy( BusinessTaxIdentifierSafetyPolicy.REALISTIC_UNCLASSIFIED) .build()).withoutFormatting().generate();
        assertTrue(cnpj.matches("\\d{14}"), cnpj);
        assertEquals("0001", cnpj.substring(8, 12), "headquarters branch");
        assertTrue(isValidCnpj(cnpj), cnpj);
    }

    @Test
    @DisplayName("Receita Federal's fictitious alphanumeric simulator vector has valid check digits")
    void officialFictitiousAlphanumericVectorValidates() {
        String cnpj = "65.9BR.JGJ/0001-03";

        assertTrue(isValidAlphanumericCnpj(cnpj.replaceAll("[./-]", "")), cnpj);
    }

    @Test
    @DisplayName("explicit compatibility mode can generate formatted and bare alphanumeric CNPJs")
    void alphanumericCompatibilityMode() {
        CnpjGenerator alphanumeric = new CnpjGenerator(realisticConfig(123L)).withAlphanumericFormat();

        String formatted = alphanumeric.generate();
        String bare = alphanumeric.withoutFormatting().generate();

        assertTrue(formatted.matches("[0-9A-Z]{2}\\.[0-9A-Z]{3}\\.[0-9A-Z]{3}/[0-9A-Z]{4}-\\d{2}"), formatted);
        assertTrue(isValidAlphanumericCnpj(formatted.replaceAll("[./-]", "")), formatted);
        assertTrue(bare.matches("[0-9A-Z]{14}"), bare);
        assertTrue(bare.substring(0, 12).matches(".*[A-Z].*"), bare);
        assertTrue(isValidAlphanumericCnpj(bare), bare);
        assertEquals(
            new CnpjGenerator(realisticConfig(456L)).withAlphanumericFormat().generateList(10),
            new CnpjGenerator(realisticConfig(456L)).withAlphanumericFormat().generateList(10));
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
        List<String> a = new CnpjGenerator(realisticConfig(77L)).generateList(25);
        List<String> b = new CnpjGenerator(realisticConfig(77L)).generateList(25);
        assertEquals(a, b);
    }

    @Test
    @DisplayName("configured generation fails closed by default")
    void configuredGenerationFailsClosedByDefault() {
        assertThrows(IllegalStateException.class,
                     () -> new CnpjGenerator(GeneratorConfig.defaults()).generate());
        assertThrows(IllegalStateException.class,
                     () -> new CnpjGenerator(GeneratorConfig.defaults()).withAlphanumericFormat().generate());
        assertThrows(IllegalStateException.class, () -> Generators.ofCnpj().generate());
    }

    @Test
    @DisplayName("null config is rejected")
    void nullConfig() {
        assertThrows(NullPointerException.class, () -> new CnpjGenerator(null));
    }

    @Test
    @DisplayName("facade requires an explicit realistic compatibility policy")
    void facadeCnpj() {
        assertThrows(IllegalStateException.class, () -> Generators.ofCnpj().generate());
        assertTrue(isValidCnpj(
            Generators.ofCnpj(realisticConfig(1L)).generate().replaceAll("\\D", "")));
    }

    @Test
    @DisplayName("facade ofCpf fails closed while explicit fixtures remain reproducible")
    void facadeCpf() {
        assertThrows(IllegalStateException.class, () -> Generators.ofCpf().generate());
        assertThrows(IllegalStateException.class, () -> Generators.ofCpf(5L).generate());

        GeneratorConfig config = GeneratorConfig.builder()
                                                .locale(java.util.Locale.of("pt", "BR"))
                                                .seed(5L)
                                                .nationalIdSafetyPolicy(NationalIdSafetyPolicy.REALISTIC_UNCLASSIFIED)
                                                .build();
        assertEquals(
            Generators.ofNationalId(config).generateList(10),
            Generators.ofNationalId(config).generateList(10));
    }

    private static GeneratorConfig realisticConfig(long seed) {
        return GeneratorConfig.builder()
                              .seed(seed)
                              .businessTaxIdentifierSafetyPolicy(
                                  BusinessTaxIdentifierSafetyPolicy.REALISTIC_UNCLASSIFIED)
                              .build();
    }
}
