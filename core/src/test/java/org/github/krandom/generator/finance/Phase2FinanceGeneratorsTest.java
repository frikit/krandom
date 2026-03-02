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
    @DisplayName("ein generator supports formatted and unformatted output")
    void ein() {
        EinGenerator generator = new EinGenerator();
        String formatted = generator.generate();
        assertTrue(formatted.matches("\\d{2}-\\d{7}"));

        String plain = generator.generateUnformatted();
        assertTrue(plain.matches("\\d{9}"));
    }

    @Test
    @DisplayName("ein generator seeded output is deterministic")
    void einSeeded() {
        GeneratorConfig config = GeneratorConfig.builder().seed(777L).build();
        EinGenerator a = new EinGenerator(config);
        EinGenerator b = new EinGenerator(config);
        assertEquals(a.generate(), b.generate());
        assertEquals(a.generateUnformatted(), b.generateUnformatted());
    }

    @Test
    @DisplayName("cusip generator outputs 9 chars with valid check digit")
    void cusip() {
        CusipGenerator generator = new CusipGenerator(GeneratorConfig.builder().seed(4L).build());
        String cusip = generator.generate();
        assertEquals(9, cusip.length());
        assertTrue(cusip.matches("[0-9A-Z]{9}"));

        int expected = CusipGenerator.computeCheckDigit(cusip.substring(0, 8));
        assertEquals(expected, cusip.charAt(8) - '0');
    }

    @Test
    @DisplayName("bank name generator uses locale map with default fallback")
    void bankName() {
        String german = new BankNameGenerator(Locale.GERMANY).generate();
        assertFalse(german.isBlank());

        String japaneseFallback = new BankNameGenerator(Locale.JAPAN).generate();
        assertFalse(japaneseFallback.isBlank());
    }

    @Test
    @DisplayName("bank type generator uses locale map with default fallback")
    void bankType() {
        String french = new BankTypeGenerator(Locale.FRANCE).generate();
        assertFalse(french.isBlank());

        String japaneseFallback = new BankTypeGenerator(Locale.JAPAN).generate();
        assertFalse(japaneseFallback.isBlank());
    }

    @Test
    @DisplayName("bank generator seeded output is deterministic")
    void bankSeeded() {
        GeneratorConfig config = GeneratorConfig.builder().seed(101L).locale(Locale.ITALY).build();
        BankNameGenerator aName = new BankNameGenerator(config);
        BankNameGenerator bName = new BankNameGenerator(config);
        assertEquals(aName.generate(), bName.generate());

        BankTypeGenerator aType = new BankTypeGenerator(config);
        BankTypeGenerator bType = new BankTypeGenerator(config);
        assertEquals(aType.generate(), bType.generate());
    }

    @Test
    @DisplayName("null config validations")
    void nullConfig() {
        assertThrows(NullPointerException.class, () -> new IbanGenerator((GeneratorConfig) null));
        assertThrows(NullPointerException.class, () -> new BbanGenerator((GeneratorConfig) null));
        assertThrows(NullPointerException.class, () -> new AbaRoutingGenerator(null));
        assertThrows(NullPointerException.class, () -> new BankCountryGenerator((GeneratorConfig) null));
        assertThrows(NullPointerException.class, () -> new EinGenerator(null));
        assertThrows(NullPointerException.class, () -> new CusipGenerator(null));
        assertThrows(NullPointerException.class, () -> new BankNameGenerator((GeneratorConfig) null));
        assertThrows(NullPointerException.class, () -> new BankTypeGenerator((GeneratorConfig) null));
        assertThrows(NullPointerException.class, () -> new BankNameGenerator((Locale) null));
        assertThrows(NullPointerException.class, () -> new BankTypeGenerator((Locale) null));
    }
}
