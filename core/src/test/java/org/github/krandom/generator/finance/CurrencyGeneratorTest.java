/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.finance;

import org.github.krandom.generator.GeneratorConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class CurrencyGeneratorTest {
    
    private CurrencyGenerator generator;
    
    @BeforeEach
    void setUp() {
        generator = new CurrencyGenerator();
    }
    
    // Constructor Tests
    
    @Test
    void testDefaultConstructor() {
        assertNotNull(generator);
        assertNotNull(generator.generate());
    }
    
    @Test
    void testConfigConstructor() {
        CurrencyGenerator gen = new CurrencyGenerator(
            GeneratorConfig.builder().seed(12345L).build());
        assertNotNull(gen);
        assertNotNull(gen.generate());
    }
    
    @Test
    void testNullConfig() {
        assertThrows(NullPointerException.class, () -> new CurrencyGenerator(null));
    }
    
    // Basic Generation Tests
    
    @Test
    void testGenerate() {
        String code = generator.generate();
        assertNotNull(code);
        assertEquals(3, code.length());
        assertTrue(code.matches("[A-Z]{3}"));
    }
    
    @Test
    void testGenerateMultiple() {
        for (int i = 0; i < 100; i++) {
            String code = generator.generate();
            assertNotNull(code);
            assertEquals(3, code.length());
        }
    }
    
    @Test
    void testGenerateVariety() {
        Set<String> codes = new HashSet<>();
        for (int i = 0; i < 1000; i++) {
            codes.add(generator.generate());
        }
        assertTrue(codes.size() > 10, "Should generate variety of currencies");
    }
    
    // Locale-Specific Tests
    
    @Test
    void testGenerateForLocaleUS() {
        Locale locale = new Locale("en", "US");
        String code = generator.generate(locale);
        assertEquals("USD", code);
    }
    
    @Test
    void testGenerateForLocaleGB() {
        Locale locale = new Locale("en", "GB");
        String code = generator.generate(locale);
        assertEquals("GBP", code);
    }
    
    @Test
    void testGenerateForLocaleAU() {
        Locale locale = new Locale("en", "AU");
        String code = generator.generate(locale);
        assertEquals("AUD", code);
    }
    
    @Test
    void testGenerateForLocaleDE() {
        Locale locale = new Locale("de", "DE");
        String code = generator.generate(locale);
        assertEquals("EUR", code);
    }
    
    @Test
    void testGenerateForLocaleFR() {
        Locale locale = new Locale("fr", "FR");
        String code = generator.generate(locale);
        assertEquals("EUR", code);
    }
    
    @Test
    void testGenerateForLocaleES() {
        Locale locale = new Locale("es", "ES");
        String code = generator.generate(locale);
        assertEquals("EUR", code);
    }
    
    @Test
    void testGenerateForLocaleIT() {
        Locale locale = new Locale("it", "IT");
        String code = generator.generate(locale);
        assertEquals("EUR", code);
    }
    
    @Test
    void testGenerateForLocaleBR() {
        Locale locale = new Locale("pt", "BR");
        String code = generator.generate(locale);
        assertEquals("BRL", code);
    }
    
    @Test
    void testGenerateForLocaleJP() {
        Locale locale = new Locale("ja", "JP");
        String code = generator.generate(locale);
        assertEquals("JPY", code);
    }
    
    @Test
    void testGenerateForLocaleCN() {
        Locale locale = new Locale("zh", "CN");
        String code = generator.generate(locale);
        assertEquals("CNY", code);
    }
    
    @Test
    void testGenerateForNullLocale() {
        String code = generator.generate(null);
        assertNotNull(code);
        assertEquals(3, code.length());
    }
    
    @Test
    void testGenerateForUnknownLocale() {
        Locale locale = new Locale("xx", "XX");
        String code = generator.generate(locale);
        assertNotNull(code);
        assertEquals(3, code.length());
    }
    
    // CurrencyInfo Tests
    
    @Test
    void testGenerateWithInfo() {
        CurrencyInfo info = generator.generateWithInfo();
        assertNotNull(info);
        assertNotNull(info.code());
        assertNotNull(info.name());
        assertNotNull(info.symbol());
        assertNotNull(info.numericCode());
        assertEquals(3, info.code().length());
        assertEquals(3, info.numericCode().length());
    }
    
    @Test
    void testGenerateWithInfoForLocaleUS() {
        Locale locale = new Locale("en", "US");
        CurrencyInfo info = generator.generateWithInfo(locale);
        assertEquals("USD", info.code());
        assertEquals("United States Dollar", info.name());
        assertEquals("$", info.symbol());
        assertEquals("840", info.numericCode());
    }
    
    @Test
    void testGenerateWithInfoForLocaleGB() {
        Locale locale = new Locale("en", "GB");
        CurrencyInfo info = generator.generateWithInfo(locale);
        assertEquals("GBP", info.code());
        assertEquals("British Pound Sterling", info.name());
        assertEquals("£", info.symbol());
        assertEquals("826", info.numericCode());
    }
    
    @Test
    void testGenerateWithInfoForLocaleJP() {
        Locale locale = new Locale("ja", "JP");
        CurrencyInfo info = generator.generateWithInfo(locale);
        assertEquals("JPY", info.code());
        assertEquals("Japanese Yen", info.name());
        assertEquals("¥", info.symbol());
        assertEquals("392", info.numericCode());
    }
    
    @Test
    void testGenerateWithInfoForNullLocale() {
        CurrencyInfo info = generator.generateWithInfo(null);
        assertNotNull(info);
        assertNotNull(info.code());
    }

    @Test
    void testFakerStyleAliases() {
        CurrencyGenerator gen = new CurrencyGenerator(GeneratorConfig.builder().seed(77L).build());
        assertNotNull(gen.generateCurrency());
        assertEquals("USD", gen.generateCurrencyCode(Locale.US));
        assertEquals("United States Dollar", gen.generateCurrencyName(Locale.US));
        assertEquals("$", gen.generateCurrencySymbol(Locale.US));
        assertFalse(gen.generatePriceTag().isBlank());
        assertFalse(gen.generatePriceTag(Locale.GERMANY).isBlank());
    }
    
    // Name Tests
    
    @Test
    void testGetName() {
        String name = generator.getName();
        assertNotNull(name);
        assertFalse(name.isEmpty());
    }
    
    @Test
    void testGetNameForLocaleUS() {
        Locale locale = new Locale("en", "US");
        String name = generator.getName(locale);
        assertEquals("United States Dollar", name);
    }
    
    @Test
    void testGetNameForLocaleEUR() {
        Locale locale = new Locale("de", "DE");
        String name = generator.getName(locale);
        assertEquals("Euro", name);
    }
    
    @Test
    void testGetNameForNullLocale() {
        String name = generator.getName(null);
        assertNotNull(name);
        assertFalse(name.isEmpty());
    }
    
    // Symbol Tests
    
    @Test
    void testGetSymbol() {
        String symbol = generator.getSymbol();
        assertNotNull(symbol);
        assertFalse(symbol.isEmpty());
    }
    
    @Test
    void testGetSymbolForLocaleUS() {
        Locale locale = new Locale("en", "US");
        String symbol = generator.getSymbol(locale);
        assertEquals("$", symbol);
    }
    
    @Test
    void testGetSymbolForLocaleGB() {
        Locale locale = new Locale("en", "GB");
        String symbol = generator.getSymbol(locale);
        assertEquals("£", symbol);
    }
    
    @Test
    void testGetSymbolForLocaleEUR() {
        Locale locale = new Locale("fr", "FR");
        String symbol = generator.getSymbol(locale);
        assertEquals("€", symbol);
    }
    
    @Test
    void testGetSymbolForNullLocale() {
        String symbol = generator.getSymbol(null);
        assertNotNull(symbol);
        assertFalse(symbol.isEmpty());
    }
    
    // Numeric Code Tests
    
    @Test
    void testGetNumericCode() {
        String numericCode = generator.getNumericCode();
        assertNotNull(numericCode);
        assertEquals(3, numericCode.length());
        assertTrue(numericCode.matches("\\d{3}"));
    }
    
    @Test
    void testGetNumericCodeForLocaleUS() {
        Locale locale = new Locale("en", "US");
        String numericCode = generator.getNumericCode(locale);
        assertEquals("840", numericCode);
    }
    
    @Test
    void testGetNumericCodeForLocaleEUR() {
        Locale locale = new Locale("es", "ES");
        String numericCode = generator.getNumericCode(locale);
        assertEquals("978", numericCode);
    }
    
    @Test
    void testGetNumericCodeForNullLocale() {
        String numericCode = generator.getNumericCode(null);
        assertNotNull(numericCode);
        assertEquals(3, numericCode.length());
    }
    
    // Seeding Tests
    
    @Test
    void testSeededGeneration() {
        CurrencyGenerator gen1 = new CurrencyGenerator(
            GeneratorConfig.builder().seed(12345L).build());
        CurrencyGenerator gen2 = new CurrencyGenerator(
            GeneratorConfig.builder().seed(12345L).build());
        
        assertEquals(gen1.generate(), gen2.generate());
        assertEquals(gen1.generate(), gen2.generate());
        assertEquals(gen1.generate(), gen2.generate());
    }
    
    @Test
    void testSeededGenerationWithInfo() {
        CurrencyGenerator gen1 = new CurrencyGenerator(
            GeneratorConfig.builder().seed(67890L).build());
        CurrencyGenerator gen2 = new CurrencyGenerator(
            GeneratorConfig.builder().seed(67890L).build());
        
        CurrencyInfo info1 = gen1.generateWithInfo();
        CurrencyInfo info2 = gen2.generateWithInfo();
        
        assertEquals(info1.code(), info2.code());
        assertEquals(info1.name(), info2.name());
        assertEquals(info1.symbol(), info2.symbol());
        assertEquals(info1.numericCode(), info2.numericCode());
    }
    
    @Test
    void testDifferentSeeds() {
        CurrencyGenerator gen1 = new CurrencyGenerator(
            GeneratorConfig.builder().seed(111L).build());
        CurrencyGenerator gen2 = new CurrencyGenerator(
            GeneratorConfig.builder().seed(222L).build());
        
        Set<String> codes1 = new HashSet<>();
        Set<String> codes2 = new HashSet<>();
        
        for (int i = 0; i < 50; i++) {
            codes1.add(gen1.generate());
            codes2.add(gen2.generate());
        }
        
        // Different seeds should produce different sequences
        assertNotEquals(codes1, codes2);
    }
    
    // Stream Tests
    
    @Test
    void testStream() {
        Stream<String> stream = generator.stream();
        assertNotNull(stream);
        List<String> codes = stream.limit(10).toList();
        assertEquals(10, codes.size());
        codes.forEach(code -> assertEquals(3, code.length()));
    }
    
    @Test
    void testStreamWithInfo() {
        Stream<CurrencyInfo> stream = generator.streamWithInfo();
        assertNotNull(stream);
        List<CurrencyInfo> infos = stream.limit(5).toList();
        assertEquals(5, infos.size());
        infos.forEach(info -> {
            assertNotNull(info.code());
            assertNotNull(info.name());
            assertNotNull(info.symbol());
            assertNotNull(info.numericCode());
        });
    }
    
    // List Generation Tests
    
    @Test
    void testGenerateList() {
        List<String> codes = generator.generateList(20);
        assertEquals(20, codes.size());
        codes.forEach(code -> assertEquals(3, code.length()));
    }
    
    @Test
    void testGenerateListWithInfo() {
        List<CurrencyInfo> infos = generator.generateListWithInfo(15);
        assertEquals(15, infos.size());
        infos.forEach(info -> {
            assertNotNull(info.code());
            assertEquals(3, info.code().length());
        });
    }
    
    @Test
    void testGenerateEmptyList() {
        List<String> codes = generator.generateList(0);
        assertTrue(codes.isEmpty());
    }
    
    @Test
    void testGenerateListWithInfoEmpty() {
        List<CurrencyInfo> infos = generator.generateListWithInfo(0);
        assertTrue(infos.isEmpty());
    }
    
    @Test
    void testGenerateListNegativeCount() {
        assertThrows(IllegalArgumentException.class, () -> generator.generateList(-1));
    }
    
    @Test
    void testGenerateListWithInfoNegativeCount() {
        assertThrows(IllegalArgumentException.class, () -> generator.generateListWithInfo(-1));
    }
    
    // Currency Enum Tests
    
    @Test
    void testCurrencyForLocaleUS() {
        Locale locale = new Locale("en", "US");
        Currency currency = Currency.forLocale(locale);
        assertEquals(Currency.USD, currency);
    }
    
    @Test
    void testCurrencyForLocaleGB() {
        Locale locale = new Locale("en", "GB");
        Currency currency = Currency.forLocale(locale);
        assertEquals(Currency.GBP, currency);
    }
    
    @Test
    void testCurrencyForLocaleEUR() {
        Locale locale = new Locale("de", "DE");
        Currency currency = Currency.forLocale(locale);
        assertEquals(Currency.EUR, currency);
    }
    
    @Test
    void testCurrencyForNullLocale() {
        Currency currency = Currency.forLocale(null);
        assertNull(currency);
    }
    
    @Test
    void testCurrencyFromCode() {
        Currency usd = Currency.fromCode("USD");
        assertEquals(Currency.USD, usd);
        assertEquals("USD", usd.getCode());
        assertEquals("United States Dollar", usd.getName());
        assertEquals("$", usd.getSymbol());
        assertEquals("840", usd.getNumericCode());
    }
    
    @Test
    void testCurrencyFromCodeEUR() {
        Currency eur = Currency.fromCode("EUR");
        assertEquals(Currency.EUR, eur);
        assertEquals("EUR", eur.getCode());
        assertEquals("Euro", eur.getName());
        assertEquals("€", eur.getSymbol());
        assertEquals("978", eur.getNumericCode());
    }
    
    @Test
    void testCurrencyFromCodeNull() {
        Currency currency = Currency.fromCode(null);
        assertNull(currency);
    }
    
    @Test
    void testCurrencyFromCodeNotFound() {
        Currency currency = Currency.fromCode("XXX");
        assertNull(currency);
    }
    
    @Test
    void testCurrencyToInfo() {
        CurrencyInfo info = Currency.USD.toInfo();
        assertEquals("USD", info.code());
        assertEquals("United States Dollar", info.name());
        assertEquals("$", info.symbol());
        assertEquals("840", info.numericCode());
    }
    
    // CurrencyInfo Record Tests
    
    @Test
    void testCurrencyInfoCreation() {
        CurrencyInfo info = new CurrencyInfo("USD", "United States Dollar", "$", "840");
        assertEquals("USD", info.code());
        assertEquals("United States Dollar", info.name());
        assertEquals("$", info.symbol());
        assertEquals("840", info.numericCode());
    }
    
    @Test
    void testCurrencyInfoNullCode() {
        assertThrows(NullPointerException.class, 
            () -> new CurrencyInfo(null, "Name", "$", "123"));
    }
    
    @Test
    void testCurrencyInfoNullName() {
        assertThrows(NullPointerException.class, 
            () -> new CurrencyInfo("USD", null, "$", "123"));
    }
    
    @Test
    void testCurrencyInfoNullSymbol() {
        assertThrows(NullPointerException.class, 
            () -> new CurrencyInfo("USD", "Name", null, "123"));
    }
    
    @Test
    void testCurrencyInfoNullNumericCode() {
        assertThrows(NullPointerException.class, 
            () -> new CurrencyInfo("USD", "Name", "$", null));
    }
    
    // Validation Tests
    
    @Test
    void testAllCurrenciesHaveValidData() {
        for (Currency currency : Currency.values()) {
            assertNotNull(currency.getCode());
            assertNotNull(currency.getName());
            assertNotNull(currency.getSymbol());
            assertNotNull(currency.getNumericCode());
            assertEquals(3, currency.getCode().length());
            assertEquals(3, currency.getNumericCode().length());
            assertTrue(currency.getCode().matches("[A-Z]{3}"));
            assertTrue(currency.getNumericCode().matches("\\d{3}"));
        }
    }
    
    @Test
    void testPrimaryCurrenciesExist() {
        assertNotNull(Currency.USD);
        assertNotNull(Currency.EUR);
        assertNotNull(Currency.GBP);
        assertNotNull(Currency.AUD);
        assertNotNull(Currency.BRL);
        assertNotNull(Currency.JPY);
        assertNotNull(Currency.CNY);
    }
    
    @Test
    void testAllLocalesHaveCurrency() {
        Locale[] locales = {
            new Locale("en", "US"),
            new Locale("en", "GB"),
            new Locale("en", "AU"),
            new Locale("de", "DE"),
            new Locale("fr", "FR"),
            new Locale("es", "ES"),
            new Locale("it", "IT"),
            new Locale("pt", "BR"),
            new Locale("ja", "JP"),
            new Locale("zh", "CN")
        };
        
        for (Locale locale : locales) {
            Currency currency = Currency.forLocale(locale);
            assertNotNull(currency, "Locale " + locale + " should have a currency");
        }
    }
}
