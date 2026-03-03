/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.finance;

import org.github.krandom.generator.GeneratorConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CreditCardGenerator")
class CreditCardGeneratorTest {
    
    private static final DateTimeFormatter EXPIRY_FORMATTER = DateTimeFormatter.ofPattern("MM/yy");
    
    // ── Constructor tests ─────────────────────────────────────────────────────
    
    @Test
    @DisplayName("default constructor generates random card types")
    void defaultConstructorGeneratesRandomTypes() {
        CreditCardGenerator gen = new CreditCardGenerator();
        assertEquals(CardType.RANDOM, gen.getCardType());
    }
    
    @Test
    @DisplayName("constructor with CardType sets card type")
    void constructorWithCardType() {
        CreditCardGenerator gen = new CreditCardGenerator(CardType.VISA);
        assertEquals(CardType.VISA, gen.getCardType());
    }
    
    @Test
    @DisplayName("constructor with GeneratorConfig accepts config")
    void constructorWithConfig() {
        GeneratorConfig config = GeneratorConfig.builder().seed(42L).build();
        CreditCardGenerator gen = new CreditCardGenerator(config);
        assertNotNull(gen);
        assertEquals(CardType.RANDOM, gen.getCardType());
    }
    
    @Test
    @DisplayName("constructor with config and card type")
    void constructorWithConfigAndCardType() {
        GeneratorConfig config = GeneratorConfig.builder().seed(42L).build();
        CreditCardGenerator gen = new CreditCardGenerator(config, CardType.MASTERCARD);
        assertEquals(CardType.MASTERCARD, gen.getCardType());
    }

    @Test
    @DisplayName("faker-style credit-card API aliases are available")
    void fakerStyleAliases() {
        CreditCardGenerator gen = new CreditCardGenerator(
                GeneratorConfig.builder().seed(123L).build(),
                CardType.VISA
        );
        assertTrue(gen.generateNumber().matches("\\d+"));
        assertTrue(gen.generateExpiry().matches("\\d{2}/\\d{2}"));
        assertTrue(gen.generateSecurityCode().matches("\\d{3}"));
        assertEquals("Visa", gen.generateProvider());
        assertEquals("Visa", gen.generateNetwork());
        assertTrue(gen.generateCvv().matches("\\d{3}"));
        assertTrue(gen.generateExpiration().matches("\\d{2}/\\d{2}"));

        String full = gen.generateFull();
        String[] lines = full.split("\\n");
        assertEquals(3, lines.length);
        assertEquals("Visa", lines[0]);
        assertTrue(lines[2].matches("\\d{2}/\\d{2} \\d{3}"));

        CreditCardInfo payload = gen.generateCreditCardInfo();
        assertNotNull(payload.number());
        assertNotNull(payload.type());
        assertNotNull(payload.exp());
        assertNotNull(payload.cvv());
        Map<String, String> map = gen.generateCreditCardAsMap();
        assertEquals(Set.of("number", "type", "exp", "cvv"), map.keySet());
    }
    
    @Test
    @DisplayName("null config throws NullPointerException")
    void nullConfigThrows() {
        NullPointerException ex = assertThrows(
            NullPointerException.class,
            () -> new CreditCardGenerator((GeneratorConfig) null)
        );
        assertTrue(ex.getMessage().contains("config must not be null"));
    }
    
    @Test
    @DisplayName("null card type throws NullPointerException")
    void nullCardTypeThrows() {
        NullPointerException ex = assertThrows(
            NullPointerException.class,
            () -> new CreditCardGenerator((CardType) null)
        );
        assertTrue(ex.getMessage().contains("cardType must not be null"));
    }
    
    @Test
    @DisplayName("null config with card type throws NullPointerException")
    void nullConfigWithCardTypeThrows() {
        NullPointerException ex = assertThrows(
            NullPointerException.class,
            () -> new CreditCardGenerator(null, CardType.VISA)
        );
        assertTrue(ex.getMessage().contains("config must not be null"));
    }
    
    @Test
    @DisplayName("config with null card type throws NullPointerException")
    void configWithNullCardTypeThrows() {
        GeneratorConfig config = GeneratorConfig.defaults();
        NullPointerException ex = assertThrows(
            NullPointerException.class,
            () -> new CreditCardGenerator(config, null)
        );
        assertTrue(ex.getMessage().contains("cardType must not be null"));
    }
    
    // ── Visa card tests ───────────────────────────────────────────────────────
    
    @Test
    @DisplayName("Visa generates formatted 16-digit card starting with 4")
    void visaFormattedCard() {
        CreditCardGenerator gen = new CreditCardGenerator(CardType.VISA);
        String card = gen.generate();
        
        assertNotNull(card);
        String unformatted = card.replaceAll("\\s", "");
        assertTrue(unformatted.startsWith("4"), "Visa should start with 4, got: " + card);
        assertTrue(unformatted.length() == 16 || unformatted.length() == 13,
                "Visa should be 16 or 13 digits, got: " + unformatted.length());
        assertTrue(CreditCardGenerator.isValidLuhn(unformatted), "Failed Luhn check: " + card);
    }
    
    @Test
    @DisplayName("Visa generates unformatted card when formatted=false")
    void visaUnformattedCard() {
        CreditCardGenerator gen = new CreditCardGenerator(CardType.VISA);
        String card = gen.generate(false);
        
        assertNotNull(card);
        assertTrue(card.matches("\\d+"), "Should contain only digits, got: " + card);
        assertTrue(card.startsWith("4"), "Visa should start with 4");
        assertTrue(card.length() == 16 || card.length() == 13,
                "Visa should be 16 or 13 digits, got: " + card.length());
        assertTrue(CreditCardGenerator.isValidLuhn(card), "Failed Luhn check: " + card);
    }
    
    @Test
    @DisplayName("Visa formatted card matches pattern")
    void visaFormattedPattern() {
        CreditCardGenerator gen = new CreditCardGenerator(CardType.VISA);
        String card = gen.generate(true);
        
        // 16-digit: "4532 1488 0343 6467" or 13-digit: "4532 1488 0343 6"
        assertTrue(card.matches("4\\d{3} \\d{4} \\d{4} \\d{4}") ||
                   card.matches("4\\d{3} \\d{4} \\d{4} \\d"),
                "Invalid Visa format: " + card);
    }
    
    @Test
    @DisplayName("Visa multiple generations pass Luhn")
    void visaMultipleLuhnCheck() {
        CreditCardGenerator gen = new CreditCardGenerator(CardType.VISA);
        
        for (int i = 0; i < 100; i++) {
            String card = gen.generate(false);
            assertTrue(CreditCardGenerator.isValidLuhn(card),
                    "Card failed Luhn validation: " + card);
        }
    }
    
    // ── Mastercard tests ──────────────────────────────────────────────────────
    
    @Test
    @DisplayName("Mastercard generates valid card with correct prefix")
    void mastercardValidPrefix() {
        CreditCardGenerator gen = new CreditCardGenerator(CardType.MASTERCARD);
        String card = gen.generate(false);
        
        assertNotNull(card);
        assertEquals(16, card.length(), "Mastercard should be 16 digits");
        
        // Check if it starts with valid Mastercard prefix
        boolean validPrefix = card.startsWith("51") || card.startsWith("52") ||
                             card.startsWith("53") || card.startsWith("54") ||
                             card.startsWith("55") ||
                             (card.startsWith("2") && Integer.parseInt(card.substring(0, 4)) >= 2221 &&
                              Integer.parseInt(card.substring(0, 4)) <= 2720);
        
        assertTrue(validPrefix, "Invalid Mastercard prefix: " + card.substring(0, 4));
        assertTrue(CreditCardGenerator.isValidLuhn(card), "Failed Luhn check: " + card);
    }
    
    @Test
    @DisplayName("Mastercard formatted card has correct pattern")
    void mastercardFormattedPattern() {
        CreditCardGenerator gen = new CreditCardGenerator(CardType.MASTERCARD);
        String card = gen.generate(true);
        
        // Should match "XXXX XXXX XXXX XXXX"
        assertTrue(card.matches("\\d{4} \\d{4} \\d{4} \\d{4}"),
                "Invalid Mastercard format: " + card);
    }
    
    @Test
    @DisplayName("Mastercard 2221-2720 range prefix")
    void mastercardNewRangePrefix() {
        CreditCardGenerator gen = new CreditCardGenerator(
            GeneratorConfig.builder().seed(123L).build(),
            CardType.MASTERCARD
        );
        
        // Generate multiple cards to ensure we get some from the new range
        Set<String> prefixes = new HashSet<>();
        for (int i = 0; i < 1000; i++) {
            String card = gen.generate(false);
            prefixes.add(card.substring(0, 4));
        }
        
        // Should have variety of prefixes
        assertTrue(prefixes.size() > 5, "Should generate variety of prefixes");
    }
    
    @Test
    @DisplayName("Mastercard multiple generations pass Luhn")
    void mastercardMultipleLuhnCheck() {
        CreditCardGenerator gen = new CreditCardGenerator(CardType.MASTERCARD);
        
        for (int i = 0; i < 100; i++) {
            String card = gen.generate(false);
            assertTrue(CreditCardGenerator.isValidLuhn(card),
                    "Card failed Luhn validation: " + card);
        }
    }
    
    // ── American Express tests ────────────────────────────────────────────────
    
    @Test
    @DisplayName("Amex generates 15-digit card starting with 34 or 37")
    void amexValidCard() {
        CreditCardGenerator gen = new CreditCardGenerator(CardType.AMEX);
        String card = gen.generate(false);
        
        assertNotNull(card);
        assertEquals(15, card.length(), "Amex should be 15 digits");
        assertTrue(card.startsWith("34") || card.startsWith("37"),
                "Amex should start with 34 or 37, got: " + card.substring(0, 2));
        assertTrue(CreditCardGenerator.isValidLuhn(card), "Failed Luhn check: " + card);
    }
    
    @Test
    @DisplayName("Amex formatted card follows 4-6-5 pattern")
    void amexFormattedPattern() {
        CreditCardGenerator gen = new CreditCardGenerator(CardType.AMEX);
        String card = gen.generate(true);
        
        // Should match "XXXX XXXXXX XXXXX"
        assertTrue(card.matches("\\d{4} \\d{6} \\d{5}"),
                "Invalid Amex format (expected 4-6-5): " + card);
    }
    
    @Test
    @DisplayName("Amex CVV is 4 digits")
    void amexCvvLength() {
        CreditCardGenerator gen = new CreditCardGenerator(CardType.AMEX);
        String cvv = gen.getCvv();
        
        assertNotNull(cvv);
        assertEquals(4, cvv.length(), "Amex CVV should be 4 digits");
        assertTrue(cvv.matches("\\d{4}"), "CVV should be digits only");
    }
    
    @Test
    @DisplayName("Amex multiple generations pass Luhn")
    void amexMultipleLuhnCheck() {
        CreditCardGenerator gen = new CreditCardGenerator(CardType.AMEX);
        
        for (int i = 0; i < 100; i++) {
            String card = gen.generate(false);
            assertTrue(CreditCardGenerator.isValidLuhn(card),
                    "Card failed Luhn validation: " + card);
        }
    }
    
    // ── Discover tests ────────────────────────────────────────────────────────
    
    @Test
    @DisplayName("Discover generates valid card with correct prefix")
    void discoverValidPrefix() {
        CreditCardGenerator gen = new CreditCardGenerator(CardType.DISCOVER);
        String card = gen.generate(false);
        
        assertNotNull(card);
        assertEquals(16, card.length(), "Discover should be 16 digits");
        
        // Check if it starts with valid Discover prefix
        boolean validPrefix = card.startsWith("6011") ||
                             card.startsWith("65") ||
                             (card.startsWith("64") && card.charAt(2) >= '4' && card.charAt(2) <= '9');
        
        assertTrue(validPrefix, "Invalid Discover prefix: " + card.substring(0, 4));
        assertTrue(CreditCardGenerator.isValidLuhn(card), "Failed Luhn check: " + card);
    }
    
    @Test
    @DisplayName("Discover multiple generations pass Luhn")
    void discoverMultipleLuhnCheck() {
        CreditCardGenerator gen = new CreditCardGenerator(CardType.DISCOVER);
        
        for (int i = 0; i < 100; i++) {
            String card = gen.generate(false);
            assertTrue(CreditCardGenerator.isValidLuhn(card),
                    "Card failed Luhn validation: " + card);
        }
    }
    
    // ── JCB tests ─────────────────────────────────────────────────────────────
    
    @Test
    @DisplayName("JCB generates valid card with 3528-3589 prefix")
    void jcbValidPrefix() {
        CreditCardGenerator gen = new CreditCardGenerator(CardType.JCB);
        String card = gen.generate(false);
        
        assertNotNull(card);
        assertEquals(16, card.length(), "JCB should be 16 digits");
        
        int prefix = Integer.parseInt(card.substring(0, 4));
        assertTrue(prefix >= 3528 && prefix <= 3589,
                "JCB prefix should be 3528-3589, got: " + prefix);
        assertTrue(CreditCardGenerator.isValidLuhn(card), "Failed Luhn check: " + card);
    }
    
    @Test
    @DisplayName("JCB multiple generations pass Luhn")
    void jcbMultipleLuhnCheck() {
        CreditCardGenerator gen = new CreditCardGenerator(CardType.JCB);
        
        for (int i = 0; i < 100; i++) {
            String card = gen.generate(false);
            assertTrue(CreditCardGenerator.isValidLuhn(card),
                    "Card failed Luhn validation: " + card);
        }
    }
    
    // ── Diners Club tests ─────────────────────────────────────────────────────
    
    @Test
    @DisplayName("Diners Club generates 14-digit card with valid prefix")
    void dinersClubValidCard() {
        CreditCardGenerator gen = new CreditCardGenerator(CardType.DINERS_CLUB);
        String card = gen.generate(false);
        
        assertNotNull(card);
        assertEquals(14, card.length(), "Diners Club should be 14 digits");
        
        // Check if it starts with valid Diners prefix
        boolean validPrefix = card.startsWith("36") || card.startsWith("38") ||
                             (card.startsWith("30") && card.charAt(2) >= '0' && card.charAt(2) <= '5');
        
        assertTrue(validPrefix, "Invalid Diners Club prefix: " + card.substring(0, 3));
        assertTrue(CreditCardGenerator.isValidLuhn(card), "Failed Luhn check: " + card);
    }
    
    @Test
    @DisplayName("Diners Club formatted card follows 4-6-4 pattern")
    void dinersClubFormattedPattern() {
        CreditCardGenerator gen = new CreditCardGenerator(CardType.DINERS_CLUB);
        String card = gen.generate(true);
        
        // Should match "XXXX XXXXXX XXXX"
        assertTrue(card.matches("\\d{4} \\d{6} \\d{4}"),
                "Invalid Diners Club format (expected 4-6-4): " + card);
    }
    
    @Test
    @DisplayName("Diners Club multiple generations pass Luhn")
    void dinersClubMultipleLuhnCheck() {
        CreditCardGenerator gen = new CreditCardGenerator(CardType.DINERS_CLUB);
        
        for (int i = 0; i < 100; i++) {
            String card = gen.generate(false);
            assertTrue(CreditCardGenerator.isValidLuhn(card),
                    "Card failed Luhn validation: " + card);
        }
    }
    
    // ── Luhn validation tests ─────────────────────────────────────────────────
    
    @Test
    @DisplayName("isValidLuhn returns true for valid card numbers")
    void luhnValidNumbers() {
        // Known valid test card numbers
        assertTrue(CreditCardGenerator.isValidLuhn("4532148803436464"));
        assertTrue(CreditCardGenerator.isValidLuhn("5425233430109903"));
        assertTrue(CreditCardGenerator.isValidLuhn("378282246310005"));
        assertTrue(CreditCardGenerator.isValidLuhn("6011111111111117"));
    }
    
    @Test
    @DisplayName("isValidLuhn returns false for invalid card numbers")
    void luhnInvalidNumbers() {
        assertFalse(CreditCardGenerator.isValidLuhn("4532148803436467")); // Wrong check digit
        assertFalse(CreditCardGenerator.isValidLuhn("1234567890123456")); // Invalid
        assertFalse(CreditCardGenerator.isValidLuhn("9999999999999999")); // Invalid
    }
    
    @Test
    @DisplayName("isValidLuhn handles formatted numbers")
    void luhnFormattedNumbers() {
        assertTrue(CreditCardGenerator.isValidLuhn("4532 1488 0343 6464"));
        assertTrue(CreditCardGenerator.isValidLuhn("3782 822463 10005"));
    }
    
    @Test
    @DisplayName("isValidLuhn returns false for null")
    void luhnNullInput() {
        assertFalse(CreditCardGenerator.isValidLuhn(null));
    }
    
    @Test
    @DisplayName("isValidLuhn returns false for empty string")
    void luhnEmptyInput() {
        assertFalse(CreditCardGenerator.isValidLuhn(""));
    }
    
    @Test
    @DisplayName("all card types pass Luhn validation")
    void allCardTypesPassLuhn() {
        CardType[] types = {CardType.VISA, CardType.MASTERCARD, CardType.AMEX,
                           CardType.DISCOVER, CardType.JCB, CardType.DINERS_CLUB};
        
        for (CardType type : types) {
            CreditCardGenerator gen = new CreditCardGenerator(type);
            
            for (int i = 0; i < 50; i++) {
                String card = gen.generate(false);
                assertTrue(CreditCardGenerator.isValidLuhn(card),
                        type + " card failed Luhn: " + card);
            }
        }
    }
    
    // ── CVV tests ─────────────────────────────────────────────────────────────
    
    @Test
    @DisplayName("CVV is 3 digits for most card types")
    void cvvThreeDigits() {
        CardType[] types = {CardType.VISA, CardType.MASTERCARD, CardType.DISCOVER,
                           CardType.JCB, CardType.DINERS_CLUB};
        
        for (CardType type : types) {
            CreditCardGenerator gen = new CreditCardGenerator(type);
            String cvv = gen.getCvv();
            
            assertNotNull(cvv);
            assertEquals(3, cvv.length(), type + " CVV should be 3 digits");
            assertTrue(cvv.matches("\\d{3}"), type + " CVV should be numeric");
        }
    }
    
    @Test
    @DisplayName("CVV includes leading zeros")
    void cvvLeadingZeros() {
        CreditCardGenerator gen = new CreditCardGenerator(
            GeneratorConfig.builder().seed(1L).build(),
            CardType.VISA
        );
        
        Set<String> cvvs = new HashSet<>();
        for (int i = 0; i < 1000; i++) {
            cvvs.add(gen.getCvv());
        }
        
        // Should have some CVVs with leading zeros
        boolean hasLeadingZero = cvvs.stream().anyMatch(cvv -> cvv.startsWith("0"));
        assertTrue(hasLeadingZero, "Should generate CVVs with leading zeros");
    }
    
    // ── Expiration date tests ─────────────────────────────────────────────────
    
    @Test
    @DisplayName("expiration date is in MM/YY format")
    void expirationDateFormat() {
        CreditCardGenerator gen = new CreditCardGenerator();
        String expiry = gen.getExpirationDate();
        
        assertNotNull(expiry);
        assertTrue(expiry.matches("\\d{2}/\\d{2}"), "Expected MM/YY format, got: " + expiry);
    }
    
    @Test
    @DisplayName("expiration date is always in the future")
    void expirationDateInFuture() {
        CreditCardGenerator gen = new CreditCardGenerator();
        LocalDate now = LocalDate.now();
        
        for (int i = 0; i < 100; i++) {
            String expiry = gen.getExpirationDate();
            LocalDate expiryDate = LocalDate.parse("01/" + expiry,
                    DateTimeFormatter.ofPattern("dd/MM/yy"));
            
            assertTrue(expiryDate.isAfter(now) || expiryDate.isEqual(now),
                    "Expiration date should be in future: " + expiry);
        }
    }
    
    @Test
    @DisplayName("expiration date is within 5 years")
    void expirationDateWithinFiveYears() {
        CreditCardGenerator gen = new CreditCardGenerator();
        LocalDate now = LocalDate.now();
        LocalDate fiveYearsLater = now.plusYears(5).plusMonths(1);
        
        for (int i = 0; i < 100; i++) {
            String expiry = gen.getExpirationDate();
            LocalDate expiryDate = LocalDate.parse("01/" + expiry,
                    DateTimeFormatter.ofPattern("dd/MM/yy"));
            
            assertTrue(expiryDate.isBefore(fiveYearsLater),
                    "Expiration date should be within 5 years: " + expiry);
        }
    }
    
    // ── CardInfo tests ────────────────────────────────────────────────────────
    
    @Test
    @DisplayName("generateWithType returns complete CardInfo")
    void generateWithTypeReturnsCardInfo() {
        CreditCardGenerator gen = new CreditCardGenerator(CardType.VISA);
        CardInfo info = gen.generateWithType();
        
        assertNotNull(info);
        assertNotNull(info.cardNumber());
        assertEquals(CardType.VISA, info.cardType());
        assertNotNull(info.cvv());
        assertNotNull(info.expirationDate());
    }
    
    @Test
    @DisplayName("CardInfo has valid card number")
    void cardInfoValidCardNumber() {
        CreditCardGenerator gen = new CreditCardGenerator(CardType.MASTERCARD);
        CardInfo info = gen.generateWithType();
        
        String unformatted = info.cardNumber().replaceAll("\\s", "");
        assertTrue(CreditCardGenerator.isValidLuhn(unformatted),
                "CardInfo card number failed Luhn: " + info.cardNumber());
    }
    
    @Test
    @DisplayName("CardInfo has correct CVV length for card type")
    void cardInfoCorrectCvvLength() {
        CreditCardGenerator amexGen = new CreditCardGenerator(CardType.AMEX);
        CardInfo amexInfo = amexGen.generateWithType();
        assertEquals(4, amexInfo.cvv().length(), "Amex CVV should be 4 digits");
        
        CreditCardGenerator visaGen = new CreditCardGenerator(CardType.VISA);
        CardInfo visaInfo = visaGen.generateWithType();
        assertEquals(3, visaInfo.cvv().length(), "Visa CVV should be 3 digits");
    }
    
    @Test
    @DisplayName("CardInfo expiration date is valid")
    void cardInfoValidExpirationDate() {
        CreditCardGenerator gen = new CreditCardGenerator(CardType.VISA);
        CardInfo info = gen.generateWithType();
        
        assertTrue(info.expirationDate().matches("\\d{2}/\\d{2}"),
                "Expected MM/YY format");
    }
    
    @Test
    @DisplayName("CardInfo constructor validates null parameters")
    void cardInfoNullParametersThrow() {
        assertThrows(NullPointerException.class,
                () -> new CardInfo(null, CardType.VISA, "123", "12/28"));
        assertThrows(NullPointerException.class,
                () -> new CardInfo("4532148803436467", null, "123", "12/28"));
        assertThrows(NullPointerException.class,
                () -> new CardInfo("4532148803436467", CardType.VISA, null, "12/28"));
        assertThrows(NullPointerException.class,
                () -> new CardInfo("4532148803436467", CardType.VISA, "123", null));
    }
    
    // ── Random card type tests ────────────────────────────────────────────────
    
    @Test
    @DisplayName("RANDOM card type generates variety of cards")
    void randomCardTypeGeneratesVariety() {
        CreditCardGenerator gen = new CreditCardGenerator(CardType.RANDOM);
        
        Set<String> prefixes = new HashSet<>();
        for (int i = 0; i < 100; i++) {
            String card = gen.generate(false);
            prefixes.add(card.substring(0, 2));
        }
        
        // Should have cards from multiple types
        assertTrue(prefixes.size() >= 3,
                "Should generate variety of card types, got " + prefixes.size() + " different prefixes");
    }
    
    @Test
    @DisplayName("RANDOM card type all cards pass Luhn")
    void randomCardTypeAllPassLuhn() {
        CreditCardGenerator gen = new CreditCardGenerator(CardType.RANDOM);
        
        for (int i = 0; i < 100; i++) {
            String card = gen.generate(false);
            assertTrue(CreditCardGenerator.isValidLuhn(card),
                    "Random card failed Luhn: " + card);
        }
    }
    
    // ── Formatting tests ──────────────────────────────────────────────────────
    
    @Test
    @DisplayName("formatted card contains spaces")
    void formattedCardHasSpaces() {
        CreditCardGenerator gen = new CreditCardGenerator(CardType.VISA);
        String card = gen.generate(true);
        
        assertTrue(card.contains(" "), "Formatted card should contain spaces");
    }
    
    @Test
    @DisplayName("unformatted card contains no spaces")
    void unformattedCardNoSpaces() {
        CreditCardGenerator gen = new CreditCardGenerator(CardType.VISA);
        String card = gen.generate(false);
        
        assertFalse(card.contains(" "), "Unformatted card should not contain spaces");
    }
    
    @Test
    @DisplayName("formatted and unformatted have same digits")
    void formattedUnformattedSameDigits() {
        GeneratorConfig config = GeneratorConfig.builder().seed(42L).build();
        
        CreditCardGenerator gen1 = new CreditCardGenerator(config, CardType.VISA);
        String formatted = gen1.generate(true);
        
        CreditCardGenerator gen2 = new CreditCardGenerator(config, CardType.VISA);
        String unformatted = gen2.generate(false);
        
        assertEquals(unformatted, formatted.replaceAll("\\s", ""),
                "Formatted and unformatted should have same digits");
    }
    
    // ── Seeding and reproducibility tests ─────────────────────────────────────
    
    @Test
    @DisplayName("seeded generator produces reproducible results")
    void seededGeneratorReproducible() {
        GeneratorConfig config = GeneratorConfig.builder().seed(12345L).build();
        
        CreditCardGenerator gen1 = new CreditCardGenerator(config, CardType.VISA);
        String card1 = gen1.generate(false);
        
        CreditCardGenerator gen2 = new CreditCardGenerator(config, CardType.VISA);
        String card2 = gen2.generate(false);
        
        assertEquals(card1, card2, "Seeded generators should produce same output");
    }
    
    @Test
    @DisplayName("seeded generator produces reproducible CardInfo")
    void seededGeneratorReproducibleCardInfo() {
        GeneratorConfig config = GeneratorConfig.builder().seed(98765L).build();
        
        CreditCardGenerator gen1 = new CreditCardGenerator(config, CardType.MASTERCARD);
        CardInfo info1 = gen1.generateWithType();
        
        CreditCardGenerator gen2 = new CreditCardGenerator(config, CardType.MASTERCARD);
        CardInfo info2 = gen2.generateWithType();
        
        assertEquals(info1.cardNumber(), info2.cardNumber());
        assertEquals(info1.cvv(), info2.cvv());
        assertEquals(info1.expirationDate(), info2.expirationDate());
    }
    
    @Test
    @DisplayName("unseeded generators produce different results")
    void unseededGeneratorsDifferent() {
        CreditCardGenerator gen1 = new CreditCardGenerator(CardType.VISA);
        CreditCardGenerator gen2 = new CreditCardGenerator(CardType.VISA);
        
        String card1 = gen1.generate(false);
        String card2 = gen2.generate(false);
        
        // Very unlikely to be the same
        assertNotEquals(card1, card2, "Unseeded generators should produce different output");
    }
    
    // ── List generation tests ─────────────────────────────────────────────────
    
    @Test
    @DisplayName("generateList produces correct number of cards")
    void generateListCorrectCount() {
        CreditCardGenerator gen = new CreditCardGenerator(CardType.VISA);
        List<String> cards = gen.generateList(10);
        
        assertNotNull(cards);
        assertEquals(10, cards.size());
    }
    
    @Test
    @DisplayName("generateList all cards are valid")
    void generateListAllValid() {
        CreditCardGenerator gen = new CreditCardGenerator(CardType.MASTERCARD);
        List<String> cards = gen.generateList(20);
        
        for (String card : cards) {
            String unformatted = card.replaceAll("\\s", "");
            assertTrue(CreditCardGenerator.isValidLuhn(unformatted),
                    "Invalid card in list: " + card);
        }
    }
    
    @Test
    @DisplayName("generateList with zero count returns empty list")
    void generateListZeroCount() {
        CreditCardGenerator gen = new CreditCardGenerator(CardType.VISA);
        List<String> cards = gen.generateList(0);
        
        assertNotNull(cards);
        assertTrue(cards.isEmpty());
    }
    
    @Test
    @DisplayName("generateList with negative count throws exception")
    void generateListNegativeCount() {
        CreditCardGenerator gen = new CreditCardGenerator(CardType.VISA);
        
        assertThrows(IllegalArgumentException.class, () -> gen.generateList(-1));
    }
    
    // ── Stream generation tests ───────────────────────────────────────────────
    
    @Test
    @DisplayName("stream generates valid cards")
    void streamGeneratesValidCards() {
        CreditCardGenerator gen = new CreditCardGenerator(CardType.VISA);
        
        List<String> cards = gen.stream().limit(50).toList();
        
        assertEquals(50, cards.size());
        for (String card : cards) {
            String unformatted = card.replaceAll("\\s", "");
            assertTrue(CreditCardGenerator.isValidLuhn(unformatted),
                    "Invalid card in stream: " + card);
        }
    }
    
    @Test
    @DisplayName("stream produces infinite sequence")
    void streamInfinite() {
        CreditCardGenerator gen = new CreditCardGenerator(CardType.MASTERCARD);
        
        long count = gen.stream().limit(1000).count();
        assertEquals(1000, count);
    }
    
    // ── Edge cases and error handling ─────────────────────────────────────────
    
    @Test
    @DisplayName("all card types have valid configuration")
    void allCardTypesHaveValidConfiguration() {
        CardType[] types = {CardType.VISA, CardType.MASTERCARD, CardType.AMEX,
                           CardType.DISCOVER, CardType.JCB, CardType.DINERS_CLUB};
        
        for (CardType type : types) {
            assertFalse(type.getPrefixPatterns().isEmpty(),
                    type + " should have prefix patterns");
            assertFalse(type.getCardLengths().isEmpty(),
                    type + " should have card lengths");
            assertTrue(type.getCvvLength() >= 3 && type.getCvvLength() <= 4,
                    type + " CVV length should be 3 or 4");
        }
    }
    
    @Test
    @DisplayName("CardType display names are not null")
    void cardTypeDisplayNamesNotNull() {
        for (CardType type : CardType.values()) {
            assertNotNull(type.getDisplayName());
            assertFalse(type.getDisplayName().isEmpty());
        }
    }

    @Test
    @DisplayName("Amex unformatted generates 15 digit number without spaces")
    void amexUnformatted() {
        CreditCardGenerator gen = new CreditCardGenerator(CardType.AMEX);
        String card = gen.generate(false);
        
        assertEquals(15, card.length());
        assertFalse(card.contains(" "));
        assertTrue(card.matches("\\d{15}"));
    }

    @Test
    @DisplayName("Diners unformatted generates 14 digit number without spaces")
    void dinersUnformatted() {
        CreditCardGenerator gen = new CreditCardGenerator(CardType.DINERS_CLUB);
        String card = gen.generate(false);
        
        assertEquals(14, card.length());
        assertFalse(card.contains(" "));
        assertTrue(card.matches("\\d{14}"));
    }
}
