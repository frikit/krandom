/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.finance;

import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.Generators;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("BankInfoGenerator")
class BankInfoGeneratorTest {

    @Test
    @DisplayName("generates a coherent structured bank payload")
    void generateBankInfo() {
        BankInfo info = new BankInfoGenerator(Locale.US).generate();

        assertNotNull(info);
        assertTrue(info.accountNumber().matches("\\d+"));
        assertTrue(info.routingNumber().matches("\\d{9}"));
        assertTrue(!info.bankName().isBlank());
        assertTrue(!info.bankType().isBlank());
        assertTrue(!info.accountName().isBlank());
        assertTrue(!info.transactionType().isBlank());
        assertTrue(isValidAba(info.routingNumber()));
    }

    @Test
    @DisplayName("seeded generation is reproducible")
    void seededGeneration() {
        GeneratorConfig config = GeneratorConfig.builder()
                                                .locale(Locale.GERMANY)
                                                .seed(42L)
                                                .build();

        BankInfoGenerator one = new BankInfoGenerator(config);
        BankInfoGenerator two = new BankInfoGenerator(config);

        assertEquals(one.generate(), two.generate());
    }

    @Test
    @DisplayName("constructors and factories reject nulls and expose locale")
    void constructorValidation() {
        assertThrows(NullPointerException.class, () -> new BankInfoGenerator((Locale) null));
        assertThrows(NullPointerException.class, () -> new BankInfoGenerator((GeneratorConfig) null));
        assertEquals(Locale.US, new BankInfoGenerator(Locale.US).getLocale());
        assertNotNull(Generators.ofBankInfo().generate());
        assertNotNull(Generators.ofBankInfo(Locale.US).generate());
        assertNotNull(Generators.ofBankInfo(GeneratorConfig.defaults()).generate());
    }

    private static boolean isValidAba(String value) {
        int sum = 0;
        for (int i = 0; i < value.length(); i++) {
            int digit = value.charAt(i) - '0';
            sum += switch (i % 3) {
                case 0 -> 3 * digit;
                case 1 -> 7 * digit;
                default -> digit;
            };
        }
        return sum % 10 == 0;
    }
}
