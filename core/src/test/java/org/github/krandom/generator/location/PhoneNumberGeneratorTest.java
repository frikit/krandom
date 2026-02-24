/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.location;

import org.github.krandom.generator.GeneratorConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PhoneNumberGenerator")
class PhoneNumberGeneratorTest {

    // ── Constructor tests ─────────────────────────────────────────────────────

    @Test
    @DisplayName("default constructor uses US locale")
    void defaultConstructorUsesUSLocale() {
        PhoneNumberGenerator gen = new PhoneNumberGenerator();
        assertEquals(Locale.US, gen.getLocale());
    }

    @Test
    @DisplayName("constructor with config accepts config")
    void constructorWithConfig() {
        GeneratorConfig config = GeneratorConfig.builder()
                .locale(Locale.GERMANY)
                .seed(42L)
                .build();
        
        PhoneNumberGenerator gen = new PhoneNumberGenerator(config);
        assertEquals(Locale.GERMANY, gen.getLocale());
    }

    @Test
    @DisplayName("constructor with locale accepts locale")
    void constructorWithLocale() {
        PhoneNumberGenerator gen = new PhoneNumberGenerator(Locale.JAPAN);
        assertEquals(Locale.JAPAN, gen.getLocale());
    }

    @Test
    @DisplayName("null config throws NullPointerException")
    void nullConfigThrows() {
        NullPointerException ex = assertThrows(
                NullPointerException.class,
                () -> new PhoneNumberGenerator((GeneratorConfig) null)
        );
        assertTrue(ex.getMessage().contains("config must not be null"));
    }

    @Test
    @DisplayName("null locale throws NullPointerException")
    void nullLocaleThrows() {
        NullPointerException ex = assertThrows(
                NullPointerException.class,
                () -> new PhoneNumberGenerator((Locale) null)
        );
        assertTrue(ex.getMessage().contains("locale must not be null"));
    }

    // ── US phone numbers (en_US) ──────────────────────────────────────────────

    @Test
    @DisplayName("US locale generates formatted phone number")
    void usFormattedPhone() {
        PhoneNumberGenerator gen = new PhoneNumberGenerator(Locale.US);
        String phone = gen.generate();
        
        assertNotNull(phone);
        // Match (555) 123-4567 or 555-123-4567
        assertTrue(phone.matches("\\(\\d{3}\\) \\d{3}-\\d{4}|\\d{3}-\\d{3}-\\d{4}"),
                "Expected US formatted phone, got: " + phone);
    }

    @Test
    @DisplayName("US locale with formatted=false generates unformatted phone")
    void usUnformattedPhone() {
        PhoneNumberGenerator gen = new PhoneNumberGenerator(Locale.US);
        String phone = gen.generate(false);
        
        assertNotNull(phone);
        assertTrue(phone.matches("\\d{10}"), "Expected 10-digit phone, got: " + phone);
    }

    @Test
    @DisplayName("US phone uses realistic area codes")
    void usRealisticAreaCodes() {
        PhoneNumberGenerator gen = new PhoneNumberGenerator(Locale.US);
        
        for (int i = 0; i < 50; i++) {
            String phone = gen.generate(false);
            String areaCode = phone.substring(0, 3);
            int code = Integer.parseInt(areaCode);
            
            // Should not be 555 (reserved for fiction)
            assertNotEquals(555, code, "Should not generate 555 area code");
            // Should be a valid 3-digit code
            assertTrue(code >= 200 && code <= 999, "Area code should be valid: " + areaCode);
        }
    }

    @Test
    @DisplayName("US generates variety of phone numbers")
    void usVariety() {
        PhoneNumberGenerator gen = new PhoneNumberGenerator(Locale.US);
        
        Set<String> seen = new HashSet<>();
        for (int i = 0; i < 100; i++) {
            seen.add(gen.generate(false));
        }
        
        assertTrue(seen.size() > 80, "Expected variety, got " + seen.size() + " unique values");
    }

    // ── UK phone numbers (en_GB) ──────────────────────────────────────────────

    @Test
    @DisplayName("UK locale generates formatted landline")
    void ukFormattedLandline() {
        PhoneNumberGenerator gen = new PhoneNumberGenerator(Locale.UK);
        String phone = gen.generate(true, false);
        
        assertNotNull(phone);
        // Match patterns like "020 7946 0958", "0161 496 0123", "01202 123456"
        assertTrue(phone.matches("0\\d{2,4} \\d{3,4} \\d{4}|0\\d{2,4} \\d{6,8}"),
                "Expected UK landline format, got: " + phone);
    }

    @Test
    @DisplayName("UK locale generates formatted mobile")
    void ukFormattedMobile() {
        PhoneNumberGenerator gen = new PhoneNumberGenerator(Locale.UK);
        String phone = gen.generate(true, true);
        
        assertNotNull(phone);
        assertTrue(phone.matches("07\\d{3} \\d{6}"),
                "Expected UK mobile format (07xxx xxxxxx), got: " + phone);
    }

    @Test
    @DisplayName("UK mobile starts with 07")
    void ukMobilePrefix() {
        PhoneNumberGenerator gen = new PhoneNumberGenerator(Locale.UK);
        
        for (int i = 0; i < 20; i++) {
            String phone = gen.generate(false, true);
            assertTrue(phone.startsWith("07"), "UK mobile should start with 07, got: " + phone);
        }
    }

    @Test
    @DisplayName("UK landline starts with valid area code")
    void ukLandlineAreaCode() {
        PhoneNumberGenerator gen = new PhoneNumberGenerator(Locale.UK);
        
        for (int i = 0; i < 20; i++) {
            String phone = gen.generate(false, false);
            assertTrue(phone.startsWith("0"), "UK landline should start with 0, got: " + phone);
            assertFalse(phone.startsWith("07"), "UK landline should not start with 07, got: " + phone);
        }
    }

    @Test
    @DisplayName("UK unformatted has no spaces")
    void ukUnformattedNoSpaces() {
        PhoneNumberGenerator gen = new PhoneNumberGenerator(Locale.UK);
        String phone = gen.generate(false, true);
        
        assertFalse(phone.contains(" "), "Unformatted should have no spaces: " + phone);
        assertTrue(phone.matches("\\d+"), "Unformatted should be all digits: " + phone);
    }

    // ── Australian phone numbers (en_AU) ──────────────────────────────────────

    @Test
    @DisplayName("Australian locale generates formatted landline")
    void australianFormattedLandline() {
        PhoneNumberGenerator gen = new PhoneNumberGenerator(Locale.of("en", "AU"));
        String phone = gen.generate(true, false);
        
        assertNotNull(phone);
        assertTrue(phone.matches("0[2378] \\d{4} \\d{4}"),
                "Expected Australian landline format, got: " + phone);
    }

    @Test
    @DisplayName("Australian locale generates formatted mobile")
    void australianFormattedMobile() {
        PhoneNumberGenerator gen = new PhoneNumberGenerator(Locale.of("en", "AU"));
        String phone = gen.generate(true, true);
        
        assertNotNull(phone);
        assertTrue(phone.matches("04\\d{2} \\d{3} \\d{3}"),
                "Expected Australian mobile format (04xx xxx xxx), got: " + phone);
    }

    @Test
    @DisplayName("Australian mobile starts with 04")
    void australianMobilePrefix() {
        PhoneNumberGenerator gen = new PhoneNumberGenerator(Locale.of("en", "AU"));
        
        for (int i = 0; i < 20; i++) {
            String phone = gen.generate(false, true);
            assertTrue(phone.startsWith("04"), "AU mobile should start with 04, got: " + phone);
        }
    }

    @Test
    @DisplayName("Australian landline starts with valid area code")
    void australianLandlineAreaCode() {
        PhoneNumberGenerator gen = new PhoneNumberGenerator(Locale.of("en", "AU"));
        
        Set<String> validPrefixes = Set.of("02", "03", "07", "08");
        for (int i = 0; i < 20; i++) {
            String phone = gen.generate(false, false);
            String prefix = phone.substring(0, 2);
            assertTrue(validPrefixes.contains(prefix),
                    "AU landline should have valid area code, got: " + phone);
        }
    }

    // ── German phone numbers (de_DE) ──────────────────────────────────────────

    @Test
    @DisplayName("German locale generates formatted landline")
    void germanFormattedLandline() {
        PhoneNumberGenerator gen = new PhoneNumberGenerator(Locale.GERMANY);
        String phone = gen.generate(true, false);
        
        assertNotNull(phone);
        assertTrue(phone.matches("0\\d{2,4} \\d{8}"),
                "Expected German landline format, got: " + phone);
    }

    @Test
    @DisplayName("German locale generates formatted mobile")
    void germanFormattedMobile() {
        PhoneNumberGenerator gen = new PhoneNumberGenerator(Locale.GERMANY);
        String phone = gen.generate(true, true);
        
        assertNotNull(phone);
        assertTrue(phone.matches("01[5-7]\\d \\d{8}"),
                "Expected German mobile format (01xx xxxxxxxx), got: " + phone);
    }

    @Test
    @DisplayName("German mobile starts with 01")
    void germanMobilePrefix() {
        PhoneNumberGenerator gen = new PhoneNumberGenerator(Locale.GERMANY);
        
        for (int i = 0; i < 20; i++) {
            String phone = gen.generate(false, true);
            assertTrue(phone.startsWith("01"), "German mobile should start with 01, got: " + phone);
        }
    }

    @Test
    @DisplayName("German unformatted has no spaces")
    void germanUnformattedNoSpaces() {
        PhoneNumberGenerator gen = new PhoneNumberGenerator(Locale.GERMANY);
        String phone = gen.generate(false, false);
        
        assertFalse(phone.contains(" "), "Unformatted should have no spaces: " + phone);
        assertTrue(phone.matches("\\d+"), "Unformatted should be all digits: " + phone);
    }

    // ── French phone numbers (fr_FR) ──────────────────────────────────────────

    @Test
    @DisplayName("French locale generates formatted landline")
    void frenchFormattedLandline() {
        PhoneNumberGenerator gen = new PhoneNumberGenerator(Locale.FRANCE);
        String phone = gen.generate(true, false);
        
        assertNotNull(phone);
        assertTrue(phone.matches("0[1-5] \\d{2} \\d{2} \\d{2} \\d{2}"),
                "Expected French landline format, got: " + phone);
    }

    @Test
    @DisplayName("French locale generates formatted mobile")
    void frenchFormattedMobile() {
        PhoneNumberGenerator gen = new PhoneNumberGenerator(Locale.FRANCE);
        String phone = gen.generate(true, true);
        
        assertNotNull(phone);
        assertTrue(phone.matches("0[67] \\d{2} \\d{2} \\d{2} \\d{2}"),
                "Expected French mobile format (06/07 xx xx xx xx), got: " + phone);
    }

    @Test
    @DisplayName("French mobile starts with 06 or 07")
    void frenchMobilePrefix() {
        PhoneNumberGenerator gen = new PhoneNumberGenerator(Locale.FRANCE);
        
        for (int i = 0; i < 20; i++) {
            String phone = gen.generate(false, true);
            assertTrue(phone.startsWith("06") || phone.startsWith("07"),
                    "French mobile should start with 06 or 07, got: " + phone);
        }
    }

    @Test
    @DisplayName("French landline starts with 01-05")
    void frenchLandlinePrefix() {
        PhoneNumberGenerator gen = new PhoneNumberGenerator(Locale.FRANCE);
        
        for (int i = 0; i < 20; i++) {
            String phone = gen.generate(false, false);
            String prefix = phone.substring(0, 2);
            int prefixNum = Integer.parseInt(prefix);
            assertTrue(prefixNum >= 1 && prefixNum <= 5,
                    "French landline should start with 01-05, got: " + phone);
        }
    }

    @Test
    @DisplayName("French phone has 10 digits")
    void frenchPhoneLength() {
        PhoneNumberGenerator gen = new PhoneNumberGenerator(Locale.FRANCE);
        String phone = gen.generate(false);
        
        assertEquals(10, phone.length(), "French phone should have 10 digits: " + phone);
    }

    // ── Spanish phone numbers (es_ES) ─────────────────────────────────────────

    @Test
    @DisplayName("Spanish locale generates formatted landline")
    void spanishFormattedLandline() {
        PhoneNumberGenerator gen = new PhoneNumberGenerator(Locale.of("es", "ES"));
        String phone = gen.generate(true, false);
        
        assertNotNull(phone);
        assertTrue(phone.matches("\\d{2} \\d{3} \\d{2} \\d{2}"),
                "Expected Spanish landline format, got: " + phone);
    }

    @Test
    @DisplayName("Spanish locale generates formatted mobile")
    void spanishFormattedMobile() {
        PhoneNumberGenerator gen = new PhoneNumberGenerator(Locale.of("es", "ES"));
        String phone = gen.generate(true, true);
        
        assertNotNull(phone);
        assertTrue(phone.matches("[67]\\d{2} \\d{2} \\d{2} \\d{2}"),
                "Expected Spanish mobile format (6xx/7xx xx xx xx), got: " + phone);
    }

    @Test
    @DisplayName("Spanish mobile starts with 6 or 7")
    void spanishMobilePrefix() {
        PhoneNumberGenerator gen = new PhoneNumberGenerator(Locale.of("es", "ES"));
        
        for (int i = 0; i < 20; i++) {
            String phone = gen.generate(false, true);
            char firstChar = phone.charAt(0);
            assertTrue(firstChar == '6' || firstChar == '7',
                    "Spanish mobile should start with 6 or 7, got: " + phone);
        }
    }

    @Test
    @DisplayName("Spanish phone has 9 digits")
    void spanishPhoneLength() {
        PhoneNumberGenerator gen = new PhoneNumberGenerator(Locale.of("es", "ES"));
        String phone = gen.generate(false);
        
        assertEquals(9, phone.length(), "Spanish phone should have 9 digits: " + phone);
    }

    // ── Italian phone numbers (it_IT) ─────────────────────────────────────────

    @Test
    @DisplayName("Italian locale generates formatted landline")
    void italianFormattedLandline() {
        PhoneNumberGenerator gen = new PhoneNumberGenerator(Locale.ITALY);
        String phone = gen.generate(true, false);
        
        assertNotNull(phone);
        assertTrue(phone.matches("0\\d{1,3} \\d{4} \\d{4}"),
                "Expected Italian landline format, got: " + phone);
    }

    @Test
    @DisplayName("Italian locale generates formatted mobile")
    void italianFormattedMobile() {
        PhoneNumberGenerator gen = new PhoneNumberGenerator(Locale.ITALY);
        String phone = gen.generate(true, true);
        
        assertNotNull(phone);
        assertTrue(phone.matches("3\\d{2} \\d{3} \\d{4}"),
                "Expected Italian mobile format (3xx xxx xxxx), got: " + phone);
    }

    @Test
    @DisplayName("Italian mobile starts with 3")
    void italianMobilePrefix() {
        PhoneNumberGenerator gen = new PhoneNumberGenerator(Locale.ITALY);
        
        for (int i = 0; i < 20; i++) {
            String phone = gen.generate(false, true);
            assertTrue(phone.startsWith("3"), "Italian mobile should start with 3, got: " + phone);
        }
    }

    @Test
    @DisplayName("Italian landline starts with 0")
    void italianLandlinePrefix() {
        PhoneNumberGenerator gen = new PhoneNumberGenerator(Locale.ITALY);
        
        for (int i = 0; i < 20; i++) {
            String phone = gen.generate(false, false);
            assertTrue(phone.startsWith("0"), "Italian landline should start with 0, got: " + phone);
        }
    }

    @Test
    @DisplayName("Italian phone has 10 digits")
    void italianPhoneLength() {
        PhoneNumberGenerator gen = new PhoneNumberGenerator(Locale.ITALY);
        String phone = gen.generate(false);
        
        assertEquals(10, phone.length(), "Italian phone should have 10 digits: " + phone);
    }

    // ── Brazilian phone numbers (pt_BR) ───────────────────────────────────────

    @Test
    @DisplayName("Brazilian locale generates formatted landline")
    void brazilianFormattedLandline() {
        PhoneNumberGenerator gen = new PhoneNumberGenerator(Locale.of("pt", "BR"));
        String phone = gen.generate(true, false);
        
        assertNotNull(phone);
        assertTrue(phone.matches("\\(\\d{2}\\) [23]\\d{3}-\\d{4}"),
                "Expected Brazilian landline format, got: " + phone);
    }

    @Test
    @DisplayName("Brazilian locale generates formatted mobile")
    void brazilianFormattedMobile() {
        PhoneNumberGenerator gen = new PhoneNumberGenerator(Locale.of("pt", "BR"));
        String phone = gen.generate(true, true);
        
        assertNotNull(phone);
        assertTrue(phone.matches("\\(\\d{2}\\) 9\\d{4}-\\d{4}"),
                "Expected Brazilian mobile format ((xx) 9xxxx-xxxx), got: " + phone);
    }

    @Test
    @DisplayName("Brazilian mobile has 9 digits after area code")
    void brazilianMobileDigits() {
        PhoneNumberGenerator gen = new PhoneNumberGenerator(Locale.of("pt", "BR"));
        
        for (int i = 0; i < 20; i++) {
            String phone = gen.generate(false, true);
            // Format: xxxxxxxxxxxx (2 area + 9 number)
            assertEquals(11, phone.length(), "Brazilian mobile should have 11 digits total: " + phone);
            // Third digit should be 9
            assertEquals('9', phone.charAt(2), "Brazilian mobile third digit should be 9: " + phone);
        }
    }

    @Test
    @DisplayName("Brazilian landline has 8 digits after area code")
    void brazilianLandlineDigits() {
        PhoneNumberGenerator gen = new PhoneNumberGenerator(Locale.of("pt", "BR"));
        
        for (int i = 0; i < 20; i++) {
            String phone = gen.generate(false, false);
            // Format: xxxxxxxxxx (2 area + 8 number)
            assertEquals(10, phone.length(), "Brazilian landline should have 10 digits total: " + phone);
        }
    }

    @Test
    @DisplayName("Brazilian unformatted has no special characters")
    void brazilianUnformattedPlain() {
        PhoneNumberGenerator gen = new PhoneNumberGenerator(Locale.of("pt", "BR"));
        String phone = gen.generate(false);
        
        assertTrue(phone.matches("\\d+"), "Unformatted should be all digits: " + phone);
    }

    // ── Japanese phone numbers (ja_JP) ────────────────────────────────────────

    @Test
    @DisplayName("Japanese locale generates formatted landline")
    void japaneseFormattedLandline() {
        PhoneNumberGenerator gen = new PhoneNumberGenerator(Locale.JAPAN);
        String phone = gen.generate(true, false);
        
        assertNotNull(phone);
        assertTrue(phone.matches("0\\d{1,2}-\\d{4}-\\d{4}"),
                "Expected Japanese landline format, got: " + phone);
    }

    @Test
    @DisplayName("Japanese locale generates formatted mobile")
    void japaneseFormattedMobile() {
        PhoneNumberGenerator gen = new PhoneNumberGenerator(Locale.JAPAN);
        String phone = gen.generate(true, true);
        
        assertNotNull(phone);
        assertTrue(phone.matches("0[789]0-\\d{4}-\\d{4}"),
                "Expected Japanese mobile format (0x0-xxxx-xxxx), got: " + phone);
    }

    @Test
    @DisplayName("Japanese mobile starts with 070, 080, or 090")
    void japaneseMobilePrefix() {
        PhoneNumberGenerator gen = new PhoneNumberGenerator(Locale.JAPAN);
        
        for (int i = 0; i < 30; i++) {
            String phone = gen.generate(false, true);
            assertTrue(phone.startsWith("070") || phone.startsWith("080") || phone.startsWith("090"),
                    "Japanese mobile should start with 070/080/090, got: " + phone);
        }
    }

    @Test
    @DisplayName("Japanese landline starts with 0")
    void japaneseLandlinePrefix() {
        PhoneNumberGenerator gen = new PhoneNumberGenerator(Locale.JAPAN);
        
        for (int i = 0; i < 20; i++) {
            String phone = gen.generate(false, false);
            assertTrue(phone.startsWith("0"), "Japanese landline should start with 0, got: " + phone);
            // Should not be mobile prefix
            assertFalse(phone.startsWith("070") || phone.startsWith("080") || phone.startsWith("090"),
                    "Japanese landline should not be mobile prefix, got: " + phone);
        }
    }

    @Test
    @DisplayName("Japanese phone has correct length")
    void japanesePhoneLength() {
        PhoneNumberGenerator gen = new PhoneNumberGenerator(Locale.JAPAN);
        String phone = gen.generate(false);
        
        assertTrue(phone.length() >= 10 && phone.length() <= 11,
                "Japanese phone should have 10-11 digits: " + phone);
    }

    // ── Chinese phone numbers (zh_CN) ─────────────────────────────────────────

    @Test
    @DisplayName("Chinese locale generates formatted landline")
    void chineseFormattedLandline() {
        PhoneNumberGenerator gen = new PhoneNumberGenerator(Locale.CHINA);
        String phone = gen.generate(true, false);
        
        assertNotNull(phone);
        assertTrue(phone.matches("0\\d{2,3}-\\d{8}"),
                "Expected Chinese landline format, got: " + phone);
    }

    @Test
    @DisplayName("Chinese locale generates formatted mobile")
    void chineseFormattedMobile() {
        PhoneNumberGenerator gen = new PhoneNumberGenerator(Locale.CHINA);
        String phone = gen.generate(true, true);
        
        assertNotNull(phone);
        assertTrue(phone.matches("1[3-9]\\d \\d{4} \\d{4}"),
                "Expected Chinese mobile format (1xx xxxx xxxx), got: " + phone);
    }

    @Test
    @DisplayName("Chinese mobile starts with 1")
    void chineseMobilePrefix() {
        PhoneNumberGenerator gen = new PhoneNumberGenerator(Locale.CHINA);
        
        for (int i = 0; i < 20; i++) {
            String phone = gen.generate(false, true);
            assertTrue(phone.startsWith("1"), "Chinese mobile should start with 1, got: " + phone);
            // Second digit should be 3-9
            char secondDigit = phone.charAt(1);
            assertTrue(secondDigit >= '3' && secondDigit <= '9',
                    "Chinese mobile second digit should be 3-9, got: " + phone);
        }
    }

    @Test
    @DisplayName("Chinese landline starts with 0")
    void chineseLandlinePrefix() {
        PhoneNumberGenerator gen = new PhoneNumberGenerator(Locale.CHINA);
        
        for (int i = 0; i < 20; i++) {
            String phone = gen.generate(false, false);
            assertTrue(phone.startsWith("0"), "Chinese landline should start with 0, got: " + phone);
        }
    }

    @Test
    @DisplayName("Chinese mobile has 11 digits")
    void chineseMobileLength() {
        PhoneNumberGenerator gen = new PhoneNumberGenerator(Locale.CHINA);
        String phone = gen.generate(false, true);
        
        assertEquals(11, phone.length(), "Chinese mobile should have 11 digits: " + phone);
    }

    // ── Seeding and reproducibility ───────────────────────────────────────────

    @Test
    @DisplayName("seeded generator produces reproducible results")
    void seededReproducibility() {
        GeneratorConfig config1 = GeneratorConfig.builder()
                .locale(Locale.US)
                .seed(42L)
                .build();
        GeneratorConfig config2 = GeneratorConfig.builder()
                .locale(Locale.US)
                .seed(42L)
                .build();
        
        PhoneNumberGenerator gen1 = new PhoneNumberGenerator(config1);
        PhoneNumberGenerator gen2 = new PhoneNumberGenerator(config2);
        
        List<String> list1 = gen1.generateList(50);
        List<String> list2 = gen2.generateList(50);
        
        assertEquals(list1, list2);
    }

    @Test
    @DisplayName("seeded generator with UK locale is reproducible")
    void seededUKReproducibility() {
        GeneratorConfig config1 = GeneratorConfig.builder()
                .locale(Locale.UK)
                .seed(123L)
                .build();
        GeneratorConfig config2 = GeneratorConfig.builder()
                .locale(Locale.UK)
                .seed(123L)
                .build();
        
        PhoneNumberGenerator gen1 = new PhoneNumberGenerator(config1);
        PhoneNumberGenerator gen2 = new PhoneNumberGenerator(config2);
        
        List<String> list1 = gen1.generateList(30);
        List<String> list2 = gen2.generateList(30);
        
        assertEquals(list1, list2);
    }

    @Test
    @DisplayName("different seeds produce different sequences")
    void differentSeeds() {
        GeneratorConfig config1 = GeneratorConfig.builder()
                .locale(Locale.FRANCE)
                .seed(111L)
                .build();
        GeneratorConfig config2 = GeneratorConfig.builder()
                .locale(Locale.FRANCE)
                .seed(222L)
                .build();
        
        PhoneNumberGenerator gen1 = new PhoneNumberGenerator(config1);
        PhoneNumberGenerator gen2 = new PhoneNumberGenerator(config2);
        
        List<String> list1 = gen1.generateList(50);
        List<String> list2 = gen2.generateList(50);
        
        assertNotEquals(list1, list2);
    }

    @Test
    @DisplayName("seeded generator with mobile/landline choice is reproducible")
    void seededMobileLandlineReproducibility() {
        GeneratorConfig config1 = GeneratorConfig.builder()
                .locale(Locale.UK)
                .seed(999L)
                .build();
        GeneratorConfig config2 = GeneratorConfig.builder()
                .locale(Locale.UK)
                .seed(999L)
                .build();
        
        PhoneNumberGenerator gen1 = new PhoneNumberGenerator(config1);
        PhoneNumberGenerator gen2 = new PhoneNumberGenerator(config2);
        
        // generate() internally uses random for mobile/landline choice
        List<String> list1 = gen1.generateList(30);
        List<String> list2 = gen2.generateList(30);
        
        assertEquals(list1, list2);
    }

    // ── generateList() and stream() ───────────────────────────────────────────

    @Test
    @DisplayName("generateList() produces correct count")
    void generateListCount() {
        PhoneNumberGenerator gen = new PhoneNumberGenerator(Locale.GERMANY);
        List<String> phones = gen.generateList(25);
        
        assertEquals(25, phones.size());
        phones.forEach(phone -> {
            assertNotNull(phone);
            assertFalse(phone.isEmpty());
        });
    }

    @Test
    @DisplayName("generateList(0) returns empty list")
    void generateListZero() {
        PhoneNumberGenerator gen = new PhoneNumberGenerator();
        List<String> phones = gen.generateList(0);
        
        assertNotNull(phones);
        assertEquals(0, phones.size());
    }

    @Test
    @DisplayName("generateList() with negative count throws IllegalArgumentException")
    void generateListNegativeThrows() {
        PhoneNumberGenerator gen = new PhoneNumberGenerator();
        
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> gen.generateList(-1)
        );
        assertTrue(ex.getMessage().contains("count must be >= 0"));
    }

    @Test
    @DisplayName("stream() generates continuous values")
    void streamGeneration() {
        PhoneNumberGenerator gen = new PhoneNumberGenerator(Locale.ITALY);
        
        List<String> phones = gen.stream().limit(40).toList();
        
        assertEquals(40, phones.size());
        phones.forEach(phone -> {
            assertNotNull(phone);
            assertFalse(phone.isEmpty());
        });
    }

    @Test
    @DisplayName("stream() with UK locale generates valid phones")
    void streamUK() {
        PhoneNumberGenerator gen = new PhoneNumberGenerator(Locale.UK);
        
        List<String> phones = gen.stream().limit(20).toList();
        
        assertEquals(20, phones.size());
        phones.forEach(phone -> {
            assertNotNull(phone);
            assertTrue(phone.startsWith("0"), "UK phone should start with 0: " + phone);
        });
    }

    // ── getLocale() ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("getLocale() returns configured locale")
    void getLocale() {
        assertEquals(Locale.US, new PhoneNumberGenerator(Locale.US).getLocale());
        assertEquals(Locale.UK, new PhoneNumberGenerator(Locale.UK).getLocale());
        assertEquals(Locale.GERMANY, new PhoneNumberGenerator(Locale.GERMANY).getLocale());
        assertEquals(Locale.JAPAN, new PhoneNumberGenerator(Locale.JAPAN).getLocale());
    }

    // ── Edge cases and variety ────────────────────────────────────────────────

    @Test
    @DisplayName("unsupported locale defaults to US format")
    void unsupportedLocaleDefaultsToUS() {
        PhoneNumberGenerator gen = new PhoneNumberGenerator(Locale.of("xx", "YY"));
        String phone = gen.generate();
        
        assertNotNull(phone);
        // Should use US format as default
        assertTrue(phone.matches("\\(\\d{3}\\) \\d{3}-\\d{4}|\\d{3}-\\d{3}-\\d{4}"),
                "Expected US format for unknown locale, got: " + phone);
    }

    @Test
    @DisplayName("all supported locales generate non-null, non-empty phones")
    void allLocalesGenerateValidPhones() {
        Locale[] locales = {
            Locale.US, Locale.UK, Locale.of("en", "AU"),
            Locale.GERMANY, Locale.FRANCE, Locale.of("es", "ES"),
            Locale.ITALY, Locale.of("pt", "BR"), Locale.JAPAN,
            Locale.CHINA
        };
        
        for (Locale locale : locales) {
            PhoneNumberGenerator gen = new PhoneNumberGenerator(locale);
            String phone = gen.generate();
            
            assertNotNull(phone, "Locale " + locale + " generated null");
            assertFalse(phone.isEmpty(), "Locale " + locale + " generated empty string");
        }
    }

    @Test
    @DisplayName("all locales produce variety of values")
    void allLocalesProduceVariety() {
        Locale[] locales = {
            Locale.US, Locale.UK, Locale.GERMANY, Locale.FRANCE, 
            Locale.JAPAN, Locale.CHINA
        };
        
        for (Locale locale : locales) {
            PhoneNumberGenerator gen = new PhoneNumberGenerator(locale);
            Set<String> seen = new HashSet<>();
            
            for (int i = 0; i < 100; i++) {
                seen.add(gen.generate(false));
            }
            
            assertTrue(seen.size() > 50, 
                    "Locale " + locale + " should produce variety, got " + seen.size() + " unique values");
        }
    }

    @Test
    @DisplayName("formatted phone contains separators")
    void formattedHasSeparators() {
        Locale[] locales = {
            Locale.US, Locale.UK, Locale.GERMANY, Locale.FRANCE
        };
        
        for (Locale locale : locales) {
            PhoneNumberGenerator gen = new PhoneNumberGenerator(locale);
            String phone = gen.generate(true);
            
            // Formatted should have at least one separator (space, hyphen, or parenthesis)
            assertTrue(phone.matches(".*[ \\-()].*"),
                    "Formatted phone should have separators for " + locale + ", got: " + phone);
        }
    }

    @Test
    @DisplayName("unformatted phone is digits only for most locales")
    void unformattedDigitsOnly() {
        Locale[] locales = {
            Locale.US, Locale.GERMANY, Locale.FRANCE, Locale.of("es", "ES"),
            Locale.ITALY, Locale.of("pt", "BR"), Locale.CHINA
        };
        
        for (Locale locale : locales) {
            PhoneNumberGenerator gen = new PhoneNumberGenerator(locale);
            String phone = gen.generate(false);
            
            assertTrue(phone.matches("\\d+"),
                    "Unformatted phone should be digits only for " + locale + ", got: " + phone);
        }
    }

    @Test
    @DisplayName("mobile parameter produces different prefixes where applicable")
    void mobileParameterEffect() {
        PhoneNumberGenerator gen = new PhoneNumberGenerator(Locale.UK);
        
        // Generate 20 mobile and 20 landline, check they're different
        Set<String> mobileSet = new HashSet<>();
        Set<String> landlineSet = new HashSet<>();
        
        for (int i = 0; i < 20; i++) {
            String mobile = gen.generate(false, true);
            String landline = gen.generate(false, false);
            
            mobileSet.add(mobile.substring(0, 2));
            landlineSet.add(landline.substring(0, 2));
        }
        
        // All mobiles should start with 07
        assertTrue(mobileSet.stream().allMatch(p -> p.equals("07")),
                "UK mobiles should all start with 07");
        
        // No landlines should start with 07
        assertTrue(landlineSet.stream().noneMatch(p -> p.equals("07")),
                "UK landlines should not start with 07");
    }

    @Test
    @DisplayName("generate() with no args matches generate(true)")
    void generateNoArgsMatchesTrue() {
        GeneratorConfig config = GeneratorConfig.builder()
                .locale(Locale.US)
                .seed(999L)
                .build();
        
        PhoneNumberGenerator gen1 = new PhoneNumberGenerator(config);
        PhoneNumberGenerator gen2 = new PhoneNumberGenerator(config);
        
        assertEquals(gen1.generate(), gen2.generate(true));
    }

    @Test
    @DisplayName("Brazilian mobile always has 9 as first digit of number")
    void brazilianMobileFirstDigitIs9() {
        PhoneNumberGenerator gen = new PhoneNumberGenerator(Locale.of("pt", "BR"));
        
        for (int i = 0; i < 30; i++) {
            String phone = gen.generate(true, true);
            // Format: (xx) 9xxxx-xxxx
            assertTrue(phone.contains(" 9"), "Brazilian mobile should have ' 9' after area code: " + phone);
        }
    }

    @Test
    @DisplayName("Brazilian landline has 2 or 3 as first digit of number")
    void brazilianLandlineFirstDigit() {
        PhoneNumberGenerator gen = new PhoneNumberGenerator(Locale.of("pt", "BR"));
        
        for (int i = 0; i < 30; i++) {
            String phone = gen.generate(true, false);
            // Format: (xx) 2xxx-xxxx or (xx) 3xxx-xxxx
            assertTrue(phone.contains(" 2") || phone.contains(" 3"),
                    "Brazilian landline should have ' 2' or ' 3' after area code: " + phone);
        }
    }

    @Test
    @DisplayName("US both format styles are generated")
    void usBothFormatStyles() {
        PhoneNumberGenerator gen = new PhoneNumberGenerator(Locale.US);
        
        Set<String> patterns = new HashSet<>();
        for (int i = 0; i < 100; i++) {
            String phone = gen.generate(true);
            if (phone.startsWith("(")) {
                patterns.add("parentheses");
            } else {
                patterns.add("hyphens");
            }
        }
        
        // Both styles should appear
        assertEquals(2, patterns.size(), "Expected both US format styles to appear");
    }

    @Test
    @DisplayName("UK all format types generate correctly")
    void ukAllFormatTypes() {
        PhoneNumberGenerator gen = new PhoneNumberGenerator(Locale.UK);
        
        // Test London format specifically
        boolean foundLondon = false;
        for (int i = 0; i < 100; i++) {
            String phone = gen.generate(true, false);
            if (phone.startsWith("020 ")) {
                foundLondon = true;
                assertTrue(phone.matches("020 \\d{4} \\d{4}"),
                        "London format should be 020 xxxx xxxx: " + phone);
            }
        }
        assertTrue(foundLondon, "Should generate at least one London number");
    }

    @Test
    @DisplayName("Brazilian both mobile and landline work with formatted parameter")
    void brazilianFormattedBothTypes() {
        PhoneNumberGenerator gen = new PhoneNumberGenerator(Locale.of("pt", "BR"));
        
        // Test mobile formatted
        String mobileFormatted = gen.generate(true, true);
        assertTrue(mobileFormatted.matches("\\(\\d{2}\\) 9\\d{4}-\\d{4}"),
                "BR mobile formatted: " + mobileFormatted);
        
        // Test mobile unformatted
        String mobileUnformatted = gen.generate(false, true);
        assertTrue(mobileUnformatted.matches("\\d{2}9\\d{8}"),
                "BR mobile unformatted: " + mobileUnformatted);
        
        // Test landline formatted
        String landlineFormatted = gen.generate(true, false);
        assertTrue(landlineFormatted.matches("\\(\\d{2}\\) [23]\\d{3}-\\d{4}"),
                "BR landline formatted: " + landlineFormatted);
        
        // Test landline unformatted
        String landlineUnformatted = gen.generate(false, false);
        assertTrue(landlineUnformatted.matches("\\d{2}[23]\\d{7}"),
                "BR landline unformatted: " + landlineUnformatted);
    }

    @Test
    @DisplayName("all locales mobile/landline distinction works")
    void allLocalesMobileLandlineDistinction() {
        Locale[] localesWithDistinction = {
            Locale.UK, Locale.of("en", "AU"), Locale.GERMANY,
            Locale.FRANCE, Locale.of("es", "ES"), Locale.ITALY,
            Locale.JAPAN, Locale.CHINA
        };
        
        for (Locale locale : localesWithDistinction) {
            PhoneNumberGenerator gen = new PhoneNumberGenerator(locale);
            
            String mobile = gen.generate(false, true);
            String landline = gen.generate(false, false);
            
            assertNotNull(mobile);
            assertNotNull(landline);
            
            // Verify they have characteristics of mobile vs landline
            if (locale.equals(Locale.UK)) {
                assertTrue(mobile.startsWith("07"), "UK mobile should start with 07");
                assertFalse(landline.startsWith("07"), "UK landline should not start with 07");
            } else if (locale.getCountry().equals("AU")) {
                assertTrue(mobile.startsWith("04"), "AU mobile should start with 04");
                assertFalse(landline.startsWith("04"), "AU landline should not start with 04");
            } else if (locale.equals(Locale.GERMANY)) {
                assertTrue(mobile.startsWith("01"), "DE mobile should start with 01");
                assertFalse(landline.startsWith("01"), "DE landline should not start with 01");
            } else if (locale.equals(Locale.FRANCE)) {
                assertTrue(mobile.startsWith("06") || mobile.startsWith("07"), 
                        "FR mobile should start with 06 or 07");
                assertFalse(mobile.startsWith("06") && landline.startsWith("06") ||
                           mobile.startsWith("07") && landline.startsWith("07"),
                        "FR mobile and landline should differ");
            } else if (locale.getCountry().equals("ES")) {
                char firstChar = mobile.charAt(0);
                assertTrue(firstChar == '6' || firstChar == '7', "ES mobile should start with 6 or 7");
                char landlineFirst = landline.charAt(0);
                assertTrue(landlineFirst >= '8' && landlineFirst <= '9' || landlineFirst == '9',
                        "ES landline should not start with 6 or 7");
            } else if (locale.equals(Locale.ITALY)) {
                assertTrue(mobile.startsWith("3"), "IT mobile should start with 3");
                assertTrue(landline.startsWith("0"), "IT landline should start with 0");
            } else if (locale.equals(Locale.JAPAN)) {
                assertTrue(mobile.startsWith("070") || mobile.startsWith("080") || mobile.startsWith("090"),
                        "JP mobile should start with 070/080/090");
                assertFalse(landline.startsWith("070") || landline.startsWith("080") || landline.startsWith("090"),
                        "JP landline should not start with mobile prefix");
            } else if (locale.equals(Locale.CHINA)) {
                assertTrue(mobile.startsWith("1"), "CN mobile should start with 1");
                assertTrue(landline.startsWith("0"), "CN landline should start with 0");
            }
        }
    }
}
