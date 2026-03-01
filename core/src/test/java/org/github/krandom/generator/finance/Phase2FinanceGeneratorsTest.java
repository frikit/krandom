/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.finance;

import org.github.krandom.generator.GeneratorConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Phase 2 finance generators")
class Phase2FinanceGeneratorsTest {

    @Test
    @DisplayName("IBAN generator returns country+check+body format")
    void iban() {
        String iban = new IbanGenerator(Locale.GERMANY).generate();
        assertTrue(iban.matches("[A-Z]{2}\\d{2}\\d+"));
        assertTrue(iban.length() >= 16);
    }

    @Test
    @DisplayName("BBAN generator returns numeric account body")
    void bban() {
        assertTrue(new BbanGenerator(Locale.FRANCE).generate().matches("\\d+"));
    }

    @Test
    @DisplayName("BBAN length varies by locale country")
    void bbanLengths() {
        assertEquals(18, new BbanGenerator(Locale.UK).generate().length());
        assertEquals(20, new BbanGenerator(Locale.of("pt", "BR")).generate().length());
        assertEquals(16, new BbanGenerator(Locale.CANADA).generate().length());
    }

    @Test
    @DisplayName("ABA routing generator returns 9 digits")
    void aba() {
        String aba = new AbaRoutingGenerator().generate();
        assertTrue(aba.matches("\\d{9}"));
    }

    @Test
    @DisplayName("seeded banking generators are reproducible")
    void seededBankingGenerators() {
        GeneratorConfig cfg = GeneratorConfig.builder().seed(123L).locale(Locale.US).build();

        assertEquals(new BbanGenerator(cfg).generate(), new BbanGenerator(cfg).generate());
        assertEquals(new IbanGenerator(cfg).generate(), new IbanGenerator(cfg).generate());
        assertEquals(new AbaRoutingGenerator(cfg).generate(), new AbaRoutingGenerator(cfg).generate());
    }

    @Test
    @DisplayName("bank country generator honors locale when available")
    void bankCountry() {
        assertEquals("JP", new BankCountryGenerator(Locale.JAPAN).generate());
        assertNotNull(new BankCountryGenerator(Locale.ENGLISH).generate());
    }

    @Test
    @DisplayName("bank country generator falls back for unsupported locale country")
    void bankCountryFallback() {
        BankCountryGenerator generator = new BankCountryGenerator(GeneratorConfig.builder()
                .seed(7L)
                .locale(Locale.CANADA)
                .build());
        String code = generator.generate();
        assertNotEquals("CA", code);
        assertTrue(Set.of("US", "GB", "DE", "FR", "ES", "IT", "BR", "JP", "CN", "AU").contains(code));
    }

    @Test
    @DisplayName("currency map contract provides faker-like keys")
    void currencyMap() {
        CurrencyGenerator generator = new CurrencyGenerator(GeneratorConfig.builder().seed(11L).build());
        Map<String, String> payload = generator.generateAsMap(Locale.US);
        assertEquals("USD", payload.get("code"));
        assertTrue(payload.containsKey("name"));
        assertTrue(payload.containsKey("symbol"));
        assertTrue(payload.containsKey("numeric_code"));
    }

    @Test
    @DisplayName("null config validations")
    void nullConfig() {
        assertThrows(NullPointerException.class, () -> new IbanGenerator((GeneratorConfig) null));
        assertThrows(NullPointerException.class, () -> new BbanGenerator((GeneratorConfig) null));
        assertThrows(NullPointerException.class, () -> new AbaRoutingGenerator(null));
        assertThrows(NullPointerException.class, () -> new BankCountryGenerator((GeneratorConfig) null));
    }
}
