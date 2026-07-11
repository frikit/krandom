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
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Phase 3 finance generators")
class Phase3FinanceGeneratorsTest {

    @Test
    @DisplayName("bank account supports account number/name/transaction type")
    void bankAccountBasics() {
        BankAccountGenerator us = new BankAccountGenerator(GeneratorConfig.builder() .locale(Locale.US) .bankingSafetyPolicy(BankingSafetyPolicy.REALISTIC_UNCLASSIFIED) .build());
        assertTrue(us.generate().matches("\\d{10}"));
        assertFalse(us.generateAccountName().isBlank());
        assertFalse(us.generateTransactionType().isBlank());
        assertEquals(Locale.US, us.getLocale());
    }

    @Test
    @DisplayName("bank account length follows locale country")
    void bankAccountLocaleLengths() {
        assertEquals(8, new BankAccountGenerator(GeneratorConfig.builder() .locale(Locale.UK) .bankingSafetyPolicy(BankingSafetyPolicy.REALISTIC_UNCLASSIFIED) .build()).generate().length());
        assertEquals(7, new BankAccountGenerator(GeneratorConfig.builder() .locale(Locale.JAPAN) .bankingSafetyPolicy(BankingSafetyPolicy.REALISTIC_UNCLASSIFIED) .build()).generate().length());
        assertEquals(12, new BankAccountGenerator(GeneratorConfig.builder() .locale(Locale.ITALY) .bankingSafetyPolicy(BankingSafetyPolicy.REALISTIC_UNCLASSIFIED) .build()).generate().length());
        assertEquals(10, new BankAccountGenerator(GeneratorConfig.builder() .locale(Locale.US) .bankingSafetyPolicy(BankingSafetyPolicy.REALISTIC_UNCLASSIFIED) .build()).generate().length());
        assertEquals(10, new BankAccountGenerator(GeneratorConfig.builder() .locale(Locale.GERMANY) .bankingSafetyPolicy(BankingSafetyPolicy.REALISTIC_UNCLASSIFIED) .build()).generate().length());
        assertEquals(10, new BankAccountGenerator(GeneratorConfig.builder() .locale(Locale.of("es", "ES")) .bankingSafetyPolicy(BankingSafetyPolicy.REALISTIC_UNCLASSIFIED) .build()).generate().length());
        assertEquals(11, new BankAccountGenerator(GeneratorConfig.builder() .locale(Locale.FRANCE) .bankingSafetyPolicy(BankingSafetyPolicy.REALISTIC_UNCLASSIFIED) .build()).generate().length());
        assertEquals(12, new BankAccountGenerator(GeneratorConfig.builder() .locale(Locale.CHINA) .bankingSafetyPolicy(BankingSafetyPolicy.REALISTIC_UNCLASSIFIED) .build()).generate().length());
        assertEquals(9, new BankAccountGenerator(GeneratorConfig.builder() .locale(Locale.of("pt", "BR")) .bankingSafetyPolicy(BankingSafetyPolicy.REALISTIC_UNCLASSIFIED) .build()).generate().length());
        assertEquals(9, new BankAccountGenerator(GeneratorConfig.builder() .locale(Locale.of("en", "AU")) .bankingSafetyPolicy(BankingSafetyPolicy.REALISTIC_UNCLASSIFIED) .build()).generate().length());
        assertEquals(10, new BankAccountGenerator(GeneratorConfig.builder() .locale(Locale.CANADA) .bankingSafetyPolicy(BankingSafetyPolicy.REALISTIC_UNCLASSIFIED) .build()).generate().length());
    }

    @Test
    @DisplayName("bank account locale language branches for names and transaction types")
    void bankAccountLocaleBranches() {
        Locale[] locales = {
            Locale.US,
            Locale.GERMANY,
            Locale.FRANCE,
            Locale.of("es", "ES"),
            Locale.ITALY
        };
        for (Locale locale : locales) {
            BankAccountGenerator generator = new BankAccountGenerator(
                GeneratorConfig.builder().seed(3L).locale(locale).build()
            );
            assertFalse(generator.generateAccountName().isBlank());
            assertFalse(generator.generateTransactionType().isBlank());
        }
    }

    @Test
    @DisplayName("bank account validates null arguments")
    void bankAccountValidation() {
        assertThrows(NullPointerException.class, () -> new BankAccountGenerator((GeneratorConfig) null));
    }

    @Test
    @DisplayName("crypto address generator supports common chains")
    void cryptoAddresses() {
        CryptoAddressGenerator generator = new CryptoAddressGenerator(GeneratorConfig.builder()
                                                                                      .seed(7L)
                                                                                      .cryptoAddressSafetyPolicy(
                                                                                          CryptoAddressSafetyPolicy.REALISTIC_UNCLASSIFIED)
                                                                                      .build());
        assertTrue(generator.generateBitcoin().matches("1[1-9A-HJ-NP-Za-km-z]{33}"));
        assertTrue(generator.generateEthereum().matches("0x[0-9a-f]{40}"));
        assertTrue(generator.generateLitecoin().matches("L[1-9A-HJ-NP-Za-km-z]{33}"));
        assertTrue(generator.generate("bitcoin").startsWith("1"));
        assertTrue(generator.generate("eth").startsWith("0x"));
        assertTrue(generator.generate("ltc").startsWith("L"));
    }

    @Test
    @DisplayName("crypto generator default and validation branches")
    @SuppressWarnings("removal")
    void cryptoDefaultAndValidation() {
        CryptoAddressGenerator generator = new CryptoAddressGenerator(GeneratorConfig.builder() .cryptoAddressSafetyPolicy(CryptoAddressSafetyPolicy.REALISTIC_UNCLASSIFIED) .build());
        String any = generator.generate();
        assertTrue(any.startsWith("1") || any.startsWith("0x") || any.startsWith("L"));
        assertThrows(NullPointerException.class, () -> generator.generate(null));
        assertThrows(IllegalArgumentException.class, () -> generator.generate("doge"));
        assertThrows(NullPointerException.class, () -> new CryptoAddressGenerator(null));
    }

    @Test
    @DisplayName("configured crypto-address generation fails closed by default")
    void cryptoConfiguredGenerationFailsClosedByDefault() {
        assertThrows(IllegalStateException.class,
                     () -> new CryptoAddressGenerator(GeneratorConfig.defaults()).generate());
        assertThrows(IllegalStateException.class, () -> Generators.ofCryptoAddress().generate());
    }

    @Test
    @DisplayName("generators factory exposes phase 3 finance generators")
    void financeFactories() {
        assertThrows(IllegalStateException.class, () -> Generators.ofBankAccount().generate());
        assertTrue(new BankAccountGenerator(GeneratorConfig.builder()
                                                           .bankingSafetyPolicy(
                                                               BankingSafetyPolicy.REALISTIC_UNCLASSIFIED)
                                                           .build()).generate().matches("\\d+"));
        assertThrows(IllegalStateException.class, () -> Generators.ofCryptoAddress().generate());
        String chain = Generators.ofCryptoAddress(GeneratorConfig.builder()
                                                                 .cryptoAddressSafetyPolicy(
                                                                     CryptoAddressSafetyPolicy.REALISTIC_UNCLASSIFIED)
                                                                 .build()).generate();
        assertTrue(Set.of('1', 'L').contains(chain.charAt(0)) || chain.startsWith("0x"));
    }
}
