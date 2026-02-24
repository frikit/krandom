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

@DisplayName("PostalCodeGenerator")
class PostalCodeGeneratorTest {

    // ── Constructor tests ─────────────────────────────────────────────────────

    @Test
    @DisplayName("default constructor uses US locale")
    void defaultConstructorUsesUSLocale() {
        PostalCodeGenerator gen = new PostalCodeGenerator();
        assertEquals(Locale.US, gen.getLocale());
    }

    @Test
    @DisplayName("constructor with config accepts config")
    void constructorWithConfig() {
        GeneratorConfig config = GeneratorConfig.builder()
                .locale(Locale.GERMANY)
                .seed(42L)
                .build();
        
        PostalCodeGenerator gen = new PostalCodeGenerator(config);
        assertEquals(Locale.GERMANY, gen.getLocale());
    }

    @Test
    @DisplayName("constructor with locale accepts locale")
    void constructorWithLocale() {
        PostalCodeGenerator gen = new PostalCodeGenerator(Locale.JAPAN);
        assertEquals(Locale.JAPAN, gen.getLocale());
    }

    @Test
    @DisplayName("null config throws NullPointerException")
    void nullConfigThrows() {
        NullPointerException ex = assertThrows(
                NullPointerException.class,
                () -> new PostalCodeGenerator((GeneratorConfig) null)
        );
        assertTrue(ex.getMessage().contains("config must not be null"));
    }

    @Test
    @DisplayName("null locale throws NullPointerException")
    void nullLocaleThrows() {
        NullPointerException ex = assertThrows(
                NullPointerException.class,
                () -> new PostalCodeGenerator((Locale) null)
        );
        assertTrue(ex.getMessage().contains("locale must not be null"));
    }

    // ── US postal codes (en_US) ───────────────────────────────────────────────

    @Test
    @DisplayName("US locale generates 5-digit ZIP code")
    void usBasicFormat() {
        PostalCodeGenerator gen = new PostalCodeGenerator(Locale.US);
        String zip = gen.generate();
        
        assertNotNull(zip);
        assertTrue(zip.matches("\\d{5}"), "Expected 5-digit ZIP, got: " + zip);
    }

    @Test
    @DisplayName("US locale with extended=true generates ZIP+4")
    void usExtendedFormat() {
        PostalCodeGenerator gen = new PostalCodeGenerator(Locale.US);
        String zipPlus4 = gen.generate(true);
        
        assertNotNull(zipPlus4);
        assertTrue(zipPlus4.matches("\\d{5}-\\d{4}"), 
                "Expected ZIP+4 format, got: " + zipPlus4);
    }

    @Test
    @DisplayName("US locale generates variety of ZIP codes")
    void usVariety() {
        PostalCodeGenerator gen = new PostalCodeGenerator(Locale.US);
        
        Set<String> seen = new HashSet<>();
        for (int i = 0; i < 100; i++) {
            seen.add(gen.generate());
        }
        
        assertTrue(seen.size() > 50, "Expected variety, got " + seen.size() + " unique values");
    }

    // ── UK postal codes (en_GB) ───────────────────────────────────────────────

    @Test
    @DisplayName("UK locale generates valid postcode format")
    void ukBasicFormat() {
        PostalCodeGenerator gen = new PostalCodeGenerator(Locale.UK);
        String postcode = gen.generate();
        
        assertNotNull(postcode);
        // UK format: outward code (2-4 chars) + space + inward code (3 chars: digit + 2 letters)
        assertTrue(postcode.matches("[A-Z]{1,2}\\d{1,2}[A-Z]? \\d[A-Z]{2}"),
                "Expected valid UK postcode format, got: " + postcode);
    }

    @Test
    @DisplayName("UK postcode contains valid area code")
    void ukValidAreaCode() {
        PostalCodeGenerator gen = new PostalCodeGenerator(Locale.UK);
        
        Set<String> areaCodes = Set.of("SW", "EC", "N", "W", "E", "SE", "NW", "WC", "M", "B", 
                "L", "G", "EH", "AB", "BD", "BS", "CB", "CF", "CR", "CV", "LE", "LS", "OX", 
                "RG", "S", "SO", "TN", "YO");
        
        boolean foundValidAreaCode = false;
        for (int i = 0; i < 50; i++) {
            String postcode = gen.generate();
            String outward = postcode.split(" ")[0];
            String firstChar = outward.substring(0, 1);
            String firstTwoChars = outward.length() >= 2 && Character.isLetter(outward.charAt(1)) 
                    ? outward.substring(0, 2) 
                    : firstChar;
            
            if (areaCodes.contains(firstChar) || areaCodes.contains(firstTwoChars)) {
                foundValidAreaCode = true;
                break;
            }
        }
        
        assertTrue(foundValidAreaCode, "Expected at least one valid UK area code");
    }

    @Test
    @DisplayName("UK postcode generates variety")
    void ukVariety() {
        PostalCodeGenerator gen = new PostalCodeGenerator(Locale.UK);
        
        Set<String> seen = new HashSet<>();
        for (int i = 0; i < 100; i++) {
            seen.add(gen.generate());
        }
        
        assertTrue(seen.size() > 80, "Expected variety, got " + seen.size() + " unique values");
    }

    // ── Australian postal codes (en_AU) ───────────────────────────────────────

    @Test
    @DisplayName("Australian locale generates 4-digit postcode")
    void australianFormat() {
        PostalCodeGenerator gen = new PostalCodeGenerator(Locale.of("en", "AU"));
        String postcode = gen.generate();
        
        assertNotNull(postcode);
        assertTrue(postcode.matches("\\d{4}"), "Expected 4-digit postcode, got: " + postcode);
    }

    @Test
    @DisplayName("Australian postcode is in valid range")
    void australianValidRange() {
        PostalCodeGenerator gen = new PostalCodeGenerator(Locale.of("en", "AU"));
        
        for (int i = 0; i < 100; i++) {
            String postcode = gen.generate();
            int value = Integer.parseInt(postcode);
            assertTrue(value >= 200 && value <= 9999,
                    "Expected postcode in range 0200-9999, got: " + postcode);
        }
    }

    // ── German postal codes (de_DE) ───────────────────────────────────────────

    @Test
    @DisplayName("German locale generates 5-digit postcode")
    void germanFormat() {
        PostalCodeGenerator gen = new PostalCodeGenerator(Locale.GERMANY);
        String plz = gen.generate();
        
        assertNotNull(plz);
        assertTrue(plz.matches("\\d{5}"), "Expected 5-digit PLZ, got: " + plz);
    }

    @Test
    @DisplayName("German postcode is in valid range")
    void germanValidRange() {
        PostalCodeGenerator gen = new PostalCodeGenerator(Locale.GERMANY);
        
        for (int i = 0; i < 100; i++) {
            String plz = gen.generate();
            int value = Integer.parseInt(plz);
            assertTrue(value >= 1000 && value <= 99999,
                    "Expected PLZ >= 01000, got: " + plz);
        }
    }

    // ── French postal codes (fr_FR) ───────────────────────────────────────────

    @Test
    @DisplayName("French locale generates 5-digit postcode")
    void frenchFormat() {
        PostalCodeGenerator gen = new PostalCodeGenerator(Locale.FRANCE);
        String codePostal = gen.generate();
        
        assertNotNull(codePostal);
        assertTrue(codePostal.matches("\\d{5}"), 
                "Expected 5-digit code postal, got: " + codePostal);
    }

    @Test
    @DisplayName("French postcode starts with valid department number")
    void frenchValidDepartment() {
        PostalCodeGenerator gen = new PostalCodeGenerator(Locale.FRANCE);
        
        for (int i = 0; i < 100; i++) {
            String codePostal = gen.generate();
            int dept = Integer.parseInt(codePostal.substring(0, 2));
            assertTrue(dept >= 1 && dept <= 95,
                    "Expected department 01-95, got: " + codePostal);
        }
    }

    // ── Spanish postal codes (es_ES) ──────────────────────────────────────────

    @Test
    @DisplayName("Spanish locale generates 5-digit postcode")
    void spanishFormat() {
        PostalCodeGenerator gen = new PostalCodeGenerator(Locale.of("es", "ES"));
        String codigoPostal = gen.generate();
        
        assertNotNull(codigoPostal);
        assertTrue(codigoPostal.matches("\\d{5}"), 
                "Expected 5-digit código postal, got: " + codigoPostal);
    }

    @Test
    @DisplayName("Spanish postcode starts with valid province number")
    void spanishValidProvince() {
        PostalCodeGenerator gen = new PostalCodeGenerator(Locale.of("es", "ES"));
        
        for (int i = 0; i < 100; i++) {
            String codigoPostal = gen.generate();
            int province = Integer.parseInt(codigoPostal.substring(0, 2));
            assertTrue(province >= 1 && province <= 52,
                    "Expected province 01-52, got: " + codigoPostal);
        }
    }

    // ── Italian postal codes (it_IT) ──────────────────────────────────────────

    @Test
    @DisplayName("Italian locale generates 5-digit postcode")
    void italianFormat() {
        PostalCodeGenerator gen = new PostalCodeGenerator(Locale.ITALY);
        String cap = gen.generate();
        
        assertNotNull(cap);
        assertTrue(cap.matches("\\d{5}"), "Expected 5-digit CAP, got: " + cap);
    }

    @Test
    @DisplayName("Italian postcode is in valid range")
    void italianValidRange() {
        PostalCodeGenerator gen = new PostalCodeGenerator(Locale.ITALY);
        
        for (int i = 0; i < 100; i++) {
            String cap = gen.generate();
            int value = Integer.parseInt(cap);
            assertTrue(value >= 0 && value <= 98999,
                    "Expected CAP in range 00000-98999, got: " + cap);
        }
    }

    // ── Brazilian postal codes (pt_BR) ────────────────────────────────────────

    @Test
    @DisplayName("Brazilian locale generates CEP without hyphen by default")
    void brazilianBasicFormat() {
        PostalCodeGenerator gen = new PostalCodeGenerator(Locale.of("pt", "BR"));
        String cep = gen.generate();
        
        assertNotNull(cep);
        assertTrue(cep.matches("\\d{8}"), "Expected 8-digit CEP, got: " + cep);
    }

    @Test
    @DisplayName("Brazilian locale with extended=true generates CEP with hyphen")
    void brazilianExtendedFormat() {
        PostalCodeGenerator gen = new PostalCodeGenerator(Locale.of("pt", "BR"));
        String cep = gen.generate(true);
        
        assertNotNull(cep);
        assertTrue(cep.matches("\\d{5}-\\d{3}"), 
                "Expected CEP with hyphen (00000-000), got: " + cep);
    }

    // ── Japanese postal codes (ja_JP) ─────────────────────────────────────────

    @Test
    @DisplayName("Japanese locale generates postcode without hyphen by default")
    void japaneseBasicFormat() {
        PostalCodeGenerator gen = new PostalCodeGenerator(Locale.JAPAN);
        String postcode = gen.generate();
        
        assertNotNull(postcode);
        assertTrue(postcode.matches("\\d{7}"), "Expected 7-digit postcode, got: " + postcode);
    }

    @Test
    @DisplayName("Japanese locale with extended=true generates postcode with hyphen")
    void japaneseExtendedFormat() {
        PostalCodeGenerator gen = new PostalCodeGenerator(Locale.JAPAN);
        String postcode = gen.generate(true);
        
        assertNotNull(postcode);
        assertTrue(postcode.matches("\\d{3}-\\d{4}"), 
                "Expected postcode with hyphen (000-0000), got: " + postcode);
    }

    // ── Chinese postal codes (zh_CN) ──────────────────────────────────────────

    @Test
    @DisplayName("Chinese locale generates 6-digit postcode")
    void chineseFormat() {
        PostalCodeGenerator gen = new PostalCodeGenerator(Locale.CHINA);
        String postcode = gen.generate();
        
        assertNotNull(postcode);
        assertTrue(postcode.matches("\\d{6}"), "Expected 6-digit postcode, got: " + postcode);
    }

    @Test
    @DisplayName("Chinese postcode is in valid range")
    void chineseValidRange() {
        PostalCodeGenerator gen = new PostalCodeGenerator(Locale.CHINA);
        
        for (int i = 0; i < 100; i++) {
            String postcode = gen.generate();
            int value = Integer.parseInt(postcode);
            assertTrue(value >= 100000 && value <= 999999,
                    "Expected postcode in range 100000-999999, got: " + postcode);
        }
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
        
        PostalCodeGenerator gen1 = new PostalCodeGenerator(config1);
        PostalCodeGenerator gen2 = new PostalCodeGenerator(config2);
        
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
        
        PostalCodeGenerator gen1 = new PostalCodeGenerator(config1);
        PostalCodeGenerator gen2 = new PostalCodeGenerator(config2);
        
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
        
        PostalCodeGenerator gen1 = new PostalCodeGenerator(config1);
        PostalCodeGenerator gen2 = new PostalCodeGenerator(config2);
        
        List<String> list1 = gen1.generateList(50);
        List<String> list2 = gen2.generateList(50);
        
        assertNotEquals(list1, list2);
    }

    // ── generateList() and stream() ───────────────────────────────────────────

    @Test
    @DisplayName("generateList() produces correct count")
    void generateListCount() {
        PostalCodeGenerator gen = new PostalCodeGenerator(Locale.GERMANY);
        List<String> codes = gen.generateList(25);
        
        assertEquals(25, codes.size());
        codes.forEach(code -> {
            assertNotNull(code);
            assertFalse(code.isEmpty());
        });
    }

    @Test
    @DisplayName("generateList(0) returns empty list")
    void generateListZero() {
        PostalCodeGenerator gen = new PostalCodeGenerator();
        List<String> codes = gen.generateList(0);
        
        assertNotNull(codes);
        assertEquals(0, codes.size());
    }

    @Test
    @DisplayName("generateList() with negative count throws IllegalArgumentException")
    void generateListNegativeThrows() {
        PostalCodeGenerator gen = new PostalCodeGenerator();
        
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> gen.generateList(-1)
        );
        assertTrue(ex.getMessage().contains("count must be >= 0"));
    }

    @Test
    @DisplayName("stream() generates continuous values")
    void streamGeneration() {
        PostalCodeGenerator gen = new PostalCodeGenerator(Locale.ITALY);
        
        List<String> codes = gen.stream().limit(40).toList();
        
        assertEquals(40, codes.size());
        codes.forEach(code -> {
            assertNotNull(code);
            assertFalse(code.isEmpty());
            assertTrue(code.matches("\\d{5}"));
        });
    }

    @Test
    @DisplayName("stream() with UK locale generates valid postcodes")
    void streamUK() {
        PostalCodeGenerator gen = new PostalCodeGenerator(Locale.UK);
        
        List<String> codes = gen.stream().limit(20).toList();
        
        assertEquals(20, codes.size());
        Pattern ukPattern = Pattern.compile("[A-Z]{1,2}\\d{1,2}[A-Z]? \\d[A-Z]{2}");
        codes.forEach(code -> {
            assertNotNull(code);
            assertTrue(ukPattern.matcher(code).matches(), 
                    "Expected valid UK postcode, got: " + code);
        });
    }

    // ── getLocale() ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("getLocale() returns configured locale")
    void getLocale() {
        assertEquals(Locale.US, new PostalCodeGenerator(Locale.US).getLocale());
        assertEquals(Locale.UK, new PostalCodeGenerator(Locale.UK).getLocale());
        assertEquals(Locale.GERMANY, new PostalCodeGenerator(Locale.GERMANY).getLocale());
        assertEquals(Locale.JAPAN, new PostalCodeGenerator(Locale.JAPAN).getLocale());
    }

    // ── Edge cases and variety ────────────────────────────────────────────────

    @Test
    @DisplayName("unsupported locale defaults to US format")
    void unsupportedLocaleDefaultsToUS() {
        PostalCodeGenerator gen = new PostalCodeGenerator(Locale.of("xx", "YY"));
        String code = gen.generate();
        
        assertNotNull(code);
        assertTrue(code.matches("\\d{5}"), "Expected US 5-digit format for unknown locale, got: " + code);
    }

    @Test
    @DisplayName("language-only locale uses country-specific logic where applicable")
    void languageOnlyLocale() {
        PostalCodeGenerator genEN = new PostalCodeGenerator(Locale.of("en"));
        PostalCodeGenerator genDE = new PostalCodeGenerator(Locale.of("de"));
        PostalCodeGenerator genFR = new PostalCodeGenerator(Locale.of("fr"));
        PostalCodeGenerator genJA = new PostalCodeGenerator(Locale.of("ja"));
        PostalCodeGenerator genZH = new PostalCodeGenerator(Locale.of("zh"));
        
        // Language-only should fall back to default behavior (US for en, or default)
        assertNotNull(genEN.generate());
        assertNotNull(genDE.generate());
        assertNotNull(genFR.generate());
        assertNotNull(genJA.generate());
        assertNotNull(genZH.generate());
    }

    @Test
    @DisplayName("all supported locales generate non-null, non-empty codes")
    void allLocalesGenerateValidCodes() {
        Locale[] locales = {
            Locale.US, Locale.UK, Locale.of("en", "AU"),
            Locale.GERMANY, Locale.FRANCE, Locale.of("es", "ES"),
            Locale.ITALY, Locale.of("pt", "BR"), Locale.JAPAN,
            Locale.CHINA
        };
        
        for (Locale locale : locales) {
            PostalCodeGenerator gen = new PostalCodeGenerator(locale);
            String code = gen.generate();
            
            assertNotNull(code, "Locale " + locale + " generated null");
            assertFalse(code.isEmpty(), "Locale " + locale + " generated empty string");
        }
    }

    @Test
    @DisplayName("extended format parameter does not affect non-applicable locales")
    void extendedFormatNoEffectOnOtherLocales() {
        PostalCodeGenerator genDE = new PostalCodeGenerator(Locale.GERMANY);
        PostalCodeGenerator genFR = new PostalCodeGenerator(Locale.FRANCE);
        PostalCodeGenerator genAU = new PostalCodeGenerator(Locale.of("en", "AU"));
        
        // Extended parameter should have no effect on these locales
        String deBasic = genDE.generate(false);
        String deExtended = genDE.generate(true);
        assertTrue(deBasic.matches("\\d{5}"));
        assertTrue(deExtended.matches("\\d{5}"));
        
        String frBasic = genFR.generate(false);
        String frExtended = genFR.generate(true);
        assertTrue(frBasic.matches("\\d{5}"));
        assertTrue(frExtended.matches("\\d{5}"));
        
        String auBasic = genAU.generate(false);
        String auExtended = genAU.generate(true);
        assertTrue(auBasic.matches("\\d{4}"));
        assertTrue(auExtended.matches("\\d{4}"));
    }

    @Test
    @DisplayName("generate() with no args matches generate(false)")
    void generateNoArgsMatchesFalse() {
        GeneratorConfig config = GeneratorConfig.builder()
                .locale(Locale.US)
                .seed(999L)
                .build();
        
        PostalCodeGenerator gen1 = new PostalCodeGenerator(config);
        PostalCodeGenerator gen2 = new PostalCodeGenerator(config);
        
        assertEquals(gen1.generate(), gen2.generate(false));
    }

    @Test
    @DisplayName("UK postcode inward code always ends with two letters")
    void ukInwardCodeFormat() {
        PostalCodeGenerator gen = new PostalCodeGenerator(Locale.UK);
        
        for (int i = 0; i < 50; i++) {
            String postcode = gen.generate();
            String[] parts = postcode.split(" ");
            assertEquals(2, parts.length, "Expected space-separated postcode: " + postcode);
            
            String inward = parts[1];
            assertEquals(3, inward.length(), "Inward code should be 3 chars: " + postcode);
            assertTrue(Character.isDigit(inward.charAt(0)), 
                    "Inward code should start with digit: " + postcode);
            assertTrue(Character.isLetter(inward.charAt(1)), 
                    "Inward code char 2 should be letter: " + postcode);
            assertTrue(Character.isLetter(inward.charAt(2)), 
                    "Inward code char 3 should be letter: " + postcode);
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
            PostalCodeGenerator gen = new PostalCodeGenerator(locale);
            Set<String> seen = new HashSet<>();
            
            for (int i = 0; i < 100; i++) {
                seen.add(gen.generate());
            }
            
            assertTrue(seen.size() > 30, 
                    "Locale " + locale + " should produce variety, got " + seen.size() + " unique values");
        }
    }
}
