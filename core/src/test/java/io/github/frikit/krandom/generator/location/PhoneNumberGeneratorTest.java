/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.location;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.locale.SupportedLocale;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("PhoneNumberGenerator")
class PhoneNumberGeneratorTest {

    // Ofcom's ranges for TV and radio drama
    // (https://www.ofcom.org.uk/phones-and-broadband/phone-numbers/numbers-for-drama, last updated
    // 11 May 2023), written out here independently of the generator. Each block holds 1,000
    // numbers, so only the last three digits vary.
    private static final Pattern UK_DRAMA_MOBILE = Pattern.compile("07700 900\\d{3}");
    private static final Pattern UK_DRAMA_LANDLINE = Pattern.compile("0(11[3-8]|1[2-6]1) 496 0\\d{3}|020 7946 0\\d{3}"
            + "|0191 498 0\\d{3}|028 9649 6\\d{3}|029 2018 0\\d{3}|01632 960\\d{3}");
    private static final Pattern UK_DRAMA_ANY =
            Pattern.compile(UK_DRAMA_MOBILE.pattern() + "|" + UK_DRAMA_LANDLINE.pattern());
    private static final Pattern UK_DRAMA_MOBILE_DIGITS = digitsOnly(UK_DRAMA_MOBILE);
    private static final Pattern UK_DRAMA_LANDLINE_DIGITS = digitsOnly(UK_DRAMA_LANDLINE);
    private static final Pattern UK_DRAMA_ANY_DIGITS = digitsOnly(UK_DRAMA_ANY);

    // First and last number of each drama landline block, in the order the generator draws them.
    private static final String[][] UK_DRAMA_LANDLINE_BLOCKS = {
        {"0113 496 0000", "0113 496 0999"},
        {"0114 496 0000", "0114 496 0999"},
        {"0115 496 0000", "0115 496 0999"},
        {"0116 496 0000", "0116 496 0999"},
        {"0117 496 0000", "0117 496 0999"},
        {"0118 496 0000", "0118 496 0999"},
        {"0121 496 0000", "0121 496 0999"},
        {"0131 496 0000", "0131 496 0999"},
        {"0141 496 0000", "0141 496 0999"},
        {"0151 496 0000", "0151 496 0999"},
        {"0161 496 0000", "0161 496 0999"},
        {"020 7946 0000", "020 7946 0999"},
        {"0191 498 0000", "0191 498 0999"},
        {"028 9649 6000", "028 9649 6999"},
        {"029 2018 0000", "029 2018 0999"},
        {"01632 960000", "01632 960999"}
    };

    /**
     * en_GB output under {@link PhoneNumberSafetyPolicy#REALISTIC_UNCLASSIFIED}, recorded from the
     * implementation before the default policy moved UK numbers to Ofcom's drama ranges (main at
     * 582e295c, when the UK output ignored the policy). Per seed: three rounds of
     * {@code generate()}, {@code generate(false)}, {@code generate(true, true)},
     * {@code generate(true, false)}, {@code generate(false, false)} and
     * {@code generate(false, true)}, then {@code generateMsisdn()}, which draws from the same
     * random stream and so also pins how many draws the phone calls make.
     */
    private static final Map<Long, List<String>> UK_REALISTIC_GOLDEN = Map.of(
            42L,
            List.of(
                    "07500 211248",
                    "01303087525",
                    "07502 796918",
                    "01202 118093",
                    "02069067502",
                    "07700572292",
                    "0191 568 8456",
                    "07901572209",
                    "07502 327863",
                    "020 4446 2913",
                    "01418515241",
                    "07801307458",
                    "01308 356730",
                    "01342291085",
                    "07402 177727",
                    "01342 708093",
                    "01202781164",
                    "07902168065",
                    "447547573308600"),
            7L,
            List.of(
                    "07902 829485",
                    "01306566254",
                    "07901 696649",
                    "01279 239534",
                    "01915813712",
                    "07500588911",
                    "07701 712961",
                    "07900738279",
                    "07701 143924",
                    "01305 577051",
                    "01235062004",
                    "07800337400",
                    "01307 700123",
                    "07501863891",
                    "07400 616492",
                    "01293 317506",
                    "01352723234",
                    "07502048880",
                    "44735296225404"));

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
        GeneratorConfig config =
                GeneratorConfig.builder().locale(Locale.GERMANY).seed(42L).build();

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
        NullPointerException ex =
                assertThrows(NullPointerException.class, () -> new PhoneNumberGenerator((GeneratorConfig) null));
        assertTrue(ex.getMessage().contains("config must not be null"));
    }

    @Test
    @DisplayName("null locale throws NullPointerException")
    void nullLocaleThrows() {
        NullPointerException ex =
                assertThrows(NullPointerException.class, () -> new PhoneNumberGenerator((Locale) null));
        assertTrue(ex.getMessage().contains("locale must not be null"));
    }

    @Test
    @DisplayName("language-only locale is accepted")
    void languageOnlyLocaleAccepted() {
        PhoneNumberGenerator gen = new PhoneNumberGenerator(Locale.of("en"));
        String phone = gen.generate(false, false);
        assertNotNull(phone);
        assertFalse(phone.isBlank());
    }

    @Test
    @DisplayName("language-only locales still use locale-specific phone logic")
    void languageOnlyLocalesUseSpecificPhoneLogic() {
        assertTrue(
                new PhoneNumberGenerator(Locale.of("de")).generate(true, true).matches("01[5-7]\\d \\d{8}"));
        assertTrue(new PhoneNumberGenerator(Locale.of("fr"))
                .generate(true, true)
                .matches("0[67] \\d{2} \\d{2} \\d{2} \\d{2}"));
        assertTrue(new PhoneNumberGenerator(Locale.of("es"))
                .generate(true, true)
                .matches("[67]\\d{2} \\d{2} \\d{2} \\d{2}"));
        assertTrue(
                new PhoneNumberGenerator(Locale.of("it")).generate(true, true).matches("3\\d{2} \\d{3} \\d{4}"));
        assertTrue(
                new PhoneNumberGenerator(Locale.of("pt")).generate(true, true).matches("\\(\\d{2}\\) 9\\d{4}-\\d{4}"));
        assertTrue(
                new PhoneNumberGenerator(Locale.of("ja")).generate(true, true).matches("0[789]0-\\d{4}-\\d{4}"));
        assertTrue(
                new PhoneNumberGenerator(Locale.of("zh")).generate(true, true).matches("1\\d{2} \\d{4} \\d{4}"));
    }

    @Test
    @DisplayName("country calling code follows locale country")
    void countryCallingCode() {
        assertEquals("+1", new PhoneNumberGenerator(Locale.US).generateCountryCallingCode());
        assertEquals("+44", new PhoneNumberGenerator(Locale.UK).generateCountryCallingCode());
        assertEquals("+49", new PhoneNumberGenerator(Locale.GERMANY).generateCountryCallingCode());
    }

    @Test
    @DisplayName("country calling code supports every built-in locale")
    void countryCallingCodeSupportsEveryBuiltInLocale() {
        assertEquals("+31", new PhoneNumberGenerator(Locale.of("nl", "NL")).generateCountryCallingCode());
        assertEquals("+48", new PhoneNumberGenerator(Locale.of("pl", "PL")).generateCountryCallingCode());
        assertEquals("+7", new PhoneNumberGenerator(Locale.of("ru", "RU")).generateCountryCallingCode());
        assertEquals("+82", new PhoneNumberGenerator(Locale.of("ko", "KR")).generateCountryCallingCode());
        assertEquals("+90", new PhoneNumberGenerator(Locale.of("tr", "TR")).generateCountryCallingCode());
        assertEquals("+46", new PhoneNumberGenerator(Locale.of("sv", "SE")).generateCountryCallingCode());
        assertEquals("+47", new PhoneNumberGenerator(Locale.of("nb", "NO")).generateCountryCallingCode());
        assertEquals("+420", new PhoneNumberGenerator(Locale.of("cs", "CZ")).generateCountryCallingCode());
        assertEquals("+966", new PhoneNumberGenerator(Locale.of("ar", "SA")).generateCountryCallingCode());
        assertEquals("+91", new PhoneNumberGenerator(Locale.of("hi", "IN")).generateCountryCallingCode());
    }

    @Test
    @DisplayName("msisdn returns 14 or 15 digits")
    void msisdn() {
        String value = new PhoneNumberGenerator(Locale.US).generateMsisdn();
        assertTrue(value.matches("\\d{14,15}"), "Expected 14-15 digits, got: " + value);
    }

    @Test
    @DisplayName("custom phone-number format template is supported")
    void customFormatTemplate() {
        PhoneNumberGenerator gen = new PhoneNumberGenerator(Locale.US);
        String value = gen.generateWithFormat("###-###-####");
        assertTrue(value.matches("\\d{3}-\\d{3}-\\d{4}"));
    }

    @Test
    @DisplayName("custom phone-number format rejects null/blank")
    void customFormatValidation() {
        PhoneNumberGenerator gen = new PhoneNumberGenerator(Locale.US);
        assertThrows(NullPointerException.class, () -> gen.generateWithFormat(null));
        assertThrows(IllegalArgumentException.class, () -> gen.generateWithFormat("   "));
    }

    @Test
    @DisplayName("telephone aliases delegate to core methods")
    void telephoneAliases() {
        GeneratorConfig config =
                GeneratorConfig.builder().locale(Locale.US).seed(1234L).build();
        PhoneNumberGenerator alias = new PhoneNumberGenerator(config);
        PhoneNumberGenerator core = new PhoneNumberGenerator(config);

        assertEquals(core.generate(), alias.generateTelephone());
        assertEquals(core.generate(true, false), alias.generateTelephone(true, false));
        assertEquals(core.generateWithFormat("###-###"), alias.generateTelephone("###-###"));
    }

    // ── US phone numbers (en_US) ──────────────────────────────────────────────

    @Test
    @DisplayName("US locale generates formatted phone number")
    void usFormattedPhone() {
        PhoneNumberGenerator gen = new PhoneNumberGenerator(Locale.US);
        String phone = gen.generate();

        assertNotNull(phone);
        // Match (212) 555-0100 or 212-555-0100.
        assertTrue(
                phone.matches("\\(\\d{3}\\) \\d{3}-\\d{4}|\\d{3}-\\d{3}-\\d{4}"),
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
    @DisplayName("US locale-style output uses NANPA's fictional 555-01xx range by default")
    void usOutputUsesFictionalNANPARangeByDefault() {
        Locale[] locales = {Locale.US, Locale.of("en")};

        for (Locale locale : locales) {
            String phone = new PhoneNumberGenerator(
                            GeneratorConfig.builder().locale(locale).seed(42L).build())
                    .generate(false);

            assertTrue(phone.matches("\\d{3}55501\\d{2}"), "Expected NANPA fictional number, got: " + phone);
        }
    }

    @Test
    @DisplayName("realistic unclassified policy preserves non-fictitious NANPA output")
    void realisticUnclassifiedPolicyPreservesNANPAOutput() {
        Random zeroRandom = new Random() {
            @Override
            public int nextInt(int bound) {
                return 0;
            }
        };
        GeneratorConfig config = GeneratorConfig.builder()
                .locale(Locale.US)
                .random(zeroRandom)
                .phoneNumberSafetyPolicy(PhoneNumberSafetyPolicy.REALISTIC_UNCLASSIFIED)
                .build();

        assertEquals("2122000000", new PhoneNumberGenerator(config).generate(false));
    }

    @Test
    @DisplayName("unknown language-only locales resolve to en_US and use the fictional NANPA range by default")
    void unknownLanguageOnlyLocalesUseFictionalNANPARangeByDefault() {
        Random zeroRandom = new Random() {
            @Override
            public int nextInt(int bound) {
                return 0;
            }
        };
        GeneratorConfig config = GeneratorConfig.builder()
                .locale(Locale.of("zz"))
                .random(zeroRandom)
                .build();

        assertEquals("2125550100", new PhoneNumberGenerator(config).generate(false));
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
    @DisplayName("UK locale generates a formatted landline from Ofcom's drama ranges")
    void ukFormattedLandline() {
        PhoneNumberGenerator gen = new PhoneNumberGenerator(Locale.UK);
        String phone = gen.generate(true, false);

        assertNotNull(phone);
        // Patterns like "020 7946 0958", "0161 496 0123", "01632 960123"
        assertMatches(UK_DRAMA_LANDLINE, phone, "UK formatted landline");
    }

    @Test
    @DisplayName("UK locale generates a formatted mobile from Ofcom's drama range")
    void ukFormattedMobile() {
        PhoneNumberGenerator gen = new PhoneNumberGenerator(Locale.UK);
        String phone = gen.generate(true, true);

        assertNotNull(phone);
        assertMatches(UK_DRAMA_MOBILE, phone, "UK formatted mobile");
    }

    @Test
    @DisplayName("UK mobile numbers run from 07700 900000 to 07700 900999")
    void ukMobilePrefix() {
        PhoneNumberGenerator gen = new PhoneNumberGenerator(Locale.UK);

        for (int i = 0; i < 20; i++) {
            assertMatches(UK_DRAMA_MOBILE_DIGITS, gen.generate(false, true), "UK unformatted mobile");
        }
    }

    @Test
    @DisplayName("UK landlines come from a drama landline block, never a mobile range")
    void ukLandlineAreaCode() {
        PhoneNumberGenerator gen = new PhoneNumberGenerator(Locale.UK);

        for (int i = 0; i < 20; i++) {
            String phone = gen.generate(false, false);
            assertMatches(UK_DRAMA_LANDLINE_DIGITS, phone, "UK unformatted landline");
            assertFalse(phone.startsWith("07"), "UK landline should not start with 07, got: " + phone);
        }
    }

    @Test
    @DisplayName("UK unformatted has no spaces")
    void ukUnformattedNoSpaces() {
        PhoneNumberGenerator gen = new PhoneNumberGenerator(Locale.UK);
        String phone = gen.generate(false, true);

        assertFalse(phone.contains(" "), "Unformatted should have no spaces: " + phone);
        assertTrue(phone.matches("\\d{11}"), "Unformatted should be 11 digits: " + phone);
    }

    @Test
    @DisplayName("every default-policy UK number falls inside one of Ofcom's drama blocks")
    void ukDefaultPolicyNumbersStayInsideOfcomDramaBlocks() {
        for (long seed = 0; seed < 25; seed++) {
            PhoneNumberGenerator gen = new PhoneNumberGenerator(
                    GeneratorConfig.builder().locale(Locale.UK).seed(seed).build());
            String label = "seed " + seed + ": ";

            for (int i = 0; i < 40; i++) {
                assertMatches(UK_DRAMA_MOBILE, gen.generate(true, true), label + "formatted mobile");
                assertMatches(UK_DRAMA_MOBILE_DIGITS, gen.generate(false, true), label + "unformatted mobile");
                assertMatches(UK_DRAMA_LANDLINE, gen.generate(true, false), label + "formatted landline");
                assertMatches(UK_DRAMA_LANDLINE_DIGITS, gen.generate(false, false), label + "unformatted landline");
                assertMatches(UK_DRAMA_ANY, gen.generate(), label + "generate()");
                assertMatches(UK_DRAMA_ANY_DIGITS, gen.generate(false), label + "generate(false)");
            }
        }
    }

    @Test
    @DisplayName("all 16 Ofcom landline blocks and the mobile block are reachable")
    void everyUkDramaBlockIsReachable() {
        PhoneNumberGenerator gen = new PhoneNumberGenerator(
                GeneratorConfig.builder().locale(Locale.UK).seed(2026L).build());
        Set<String> expectedBlocks = new TreeSet<>();
        for (String[] block : UK_DRAMA_LANDLINE_BLOCKS) {
            expectedBlocks.add(blockOf(block[0]));
        }

        Set<String> landlineBlocks = new TreeSet<>();
        Set<String> mobileBlocks = new TreeSet<>();
        for (int i = 0; i < 2_000; i++) {
            landlineBlocks.add(blockOf(gen.generate(true, false)));
            mobileBlocks.add(blockOf(gen.generate(true, true)));
        }

        assertEquals(16, expectedBlocks.size());
        assertEquals(expectedBlocks, landlineBlocks);
        assertEquals(Set.of("07700 900"), mobileBlocks);
    }

    @Test
    @DisplayName("UK drama numbers draw a block out of 16, then a number out of 1,000, edges included")
    void ukDramaBlockEdges() {
        for (int i = 0; i < UK_DRAMA_LANDLINE_BLOCKS.length; i++) {
            String first = UK_DRAMA_LANDLINE_BLOCKS[i][0];
            String last = UK_DRAMA_LANDLINE_BLOCKS[i][1];

            assertEquals(first, ukPhone(scripted(16, i, 1000, 0), true, false), "first of block " + i);
            assertEquals(last, ukPhone(scripted(16, i, 1000, 999), true, false), "last of block " + i);
            assertEquals(first.replace(" ", ""), ukPhone(scripted(16, i, 1000, 0), false, false));
            assertEquals(last.replace(" ", ""), ukPhone(scripted(16, i, 1000, 999), false, false));
        }
        // A mobile number needs no block draw.
        assertEquals("07700 900000", ukPhone(scripted(1000, 0), true, true));
        assertEquals("07700 900999", ukPhone(scripted(1000, 999), true, true));
        assertEquals("07700900000", ukPhone(scripted(1000, 0), false, true));
        assertEquals("07700900999", ukPhone(scripted(1000, 999), false, true));
    }

    @Test
    @DisplayName("the class Javadoc's UK examples are Ofcom drama numbers")
    void ukJavadocExamplesAreDramaNumbers() {
        assertEquals("07700 900123", ukPhone(scripted(1000, 123), true, true));
        assertEquals("020 7946 0958", ukPhone(scripted(16, 11, 1000, 958), true, false));
    }

    @Test
    @DisplayName("realistic unclassified policy keeps the recorded UK output draw for draw")
    void realisticUnclassifiedPolicyKeepsRecordedUkOutput() {
        UK_REALISTIC_GOLDEN.forEach((seed, expected) -> {
            PhoneNumberGenerator gen = new PhoneNumberGenerator(GeneratorConfig.builder()
                    .locale(Locale.UK)
                    .seed(seed)
                    .phoneNumberSafetyPolicy(PhoneNumberSafetyPolicy.REALISTIC_UNCLASSIFIED)
                    .build());
            List<String> actual = new ArrayList<>();
            for (int round = 0; round < 3; round++) {
                actual.add(gen.generate());
                actual.add(gen.generate(false));
                actual.add(gen.generate(true, true));
                actual.add(gen.generate(true, false));
                actual.add(gen.generate(false, false));
                actual.add(gen.generate(false, true));
            }
            actual.add(gen.generateMsisdn());

            assertEquals(expected, actual, "seed " + seed);
        });
    }

    @Test
    @DisplayName("realistic unclassified UK output keeps its draws, bounds and layouts")
    void ukRealisticDrawsBoundsAndLayouts() {
        // 46 landline codes: index 0 is "020", 1 is "0121", and 7 is "01202". The local number is
        // drawn below 10^(11 - code length).
        assertEquals("020 0123 4567", realisticUkPhone(scripted(46, 0, 100_000_000, 1_234_567), true, false));
        assertEquals("02001234567", realisticUkPhone(scripted(46, 0, 100_000_000, 1_234_567), false, false));
        assertEquals("0121 023 4567", realisticUkPhone(scripted(46, 1, 10_000_000, 234_567), true, false));
        assertEquals("01210234567", realisticUkPhone(scripted(46, 1, 10_000_000, 234_567), false, false));
        assertEquals("01202 045678", realisticUkPhone(scripted(46, 7, 1_000_000, 45_678), true, false));
        assertEquals("01202045678", realisticUkPhone(scripted(46, 7, 1_000_000, 45_678), false, false));
        // 15 mobile prefixes: index 0 is "07400". The subscriber number is drawn below 10^6.
        assertEquals("07400 012345", realisticUkPhone(scripted(15, 0, 1_000_000, 12_345), true, true));
        assertEquals("07400012345", realisticUkPhone(scripted(15, 0, 1_000_000, 12_345), false, true));
    }

    // ── Australian phone numbers (en_AU) ──────────────────────────────────────

    @Test
    @DisplayName("Australian locale generates formatted landline")
    void australianFormattedLandline() {
        PhoneNumberGenerator gen = new PhoneNumberGenerator(Locale.of("en", "AU"));
        String phone = gen.generate(true, false);

        assertNotNull(phone);
        assertTrue(phone.matches("0[2378] \\d{4} \\d{4}"), "Expected Australian landline format, got: " + phone);
    }

    @Test
    @DisplayName("Australian locale generates formatted mobile")
    void australianFormattedMobile() {
        PhoneNumberGenerator gen = new PhoneNumberGenerator(Locale.of("en", "AU"));
        String phone = gen.generate(true, true);

        assertNotNull(phone);
        assertTrue(
                phone.matches("04\\d{2} \\d{3} \\d{3}"),
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
            assertTrue(validPrefixes.contains(prefix), "AU landline should have valid area code, got: " + phone);
        }
    }

    // ── German phone numbers (de_DE) ──────────────────────────────────────────

    @Test
    @DisplayName("German locale generates formatted landline")
    void germanFormattedLandline() {
        PhoneNumberGenerator gen = new PhoneNumberGenerator(Locale.GERMANY);
        String phone = gen.generate(true, false);

        assertNotNull(phone);
        assertTrue(phone.matches("0\\d{2,4} \\d{8}"), "Expected German landline format, got: " + phone);
    }

    @Test
    @DisplayName("German locale generates formatted mobile")
    void germanFormattedMobile() {
        PhoneNumberGenerator gen = new PhoneNumberGenerator(Locale.GERMANY);
        String phone = gen.generate(true, true);

        assertNotNull(phone);
        assertTrue(phone.matches("01[5-7]\\d \\d{8}"), "Expected German mobile format (01xx xxxxxxxx), got: " + phone);
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
        assertTrue(
                phone.matches("0[1-5] \\d{2} \\d{2} \\d{2} \\d{2}"), "Expected French landline format, got: " + phone);
    }

    @Test
    @DisplayName("French locale generates formatted mobile")
    void frenchFormattedMobile() {
        PhoneNumberGenerator gen = new PhoneNumberGenerator(Locale.FRANCE);
        String phone = gen.generate(true, true);

        assertNotNull(phone);
        assertTrue(
                phone.matches("0[67] \\d{2} \\d{2} \\d{2} \\d{2}"),
                "Expected French mobile format (06/07 xx xx xx xx), got: " + phone);
    }

    @Test
    @DisplayName("French mobile starts with 06 or 07")
    void frenchMobilePrefix() {
        PhoneNumberGenerator gen = new PhoneNumberGenerator(Locale.FRANCE);

        for (int i = 0; i < 20; i++) {
            String phone = gen.generate(false, true);
            assertTrue(
                    phone.startsWith("06") || phone.startsWith("07"),
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
            assertTrue(prefixNum >= 1 && prefixNum <= 5, "French landline should start with 01-05, got: " + phone);
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
        assertTrue(phone.matches("\\d{2} \\d{3} \\d{2} \\d{2}"), "Expected Spanish landline format, got: " + phone);
    }

    @Test
    @DisplayName("Spanish locale generates formatted mobile")
    void spanishFormattedMobile() {
        PhoneNumberGenerator gen = new PhoneNumberGenerator(Locale.of("es", "ES"));
        String phone = gen.generate(true, true);

        assertNotNull(phone);
        assertTrue(
                phone.matches("[67]\\d{2} \\d{2} \\d{2} \\d{2}"),
                "Expected Spanish mobile format (6xx/7xx xx xx xx), got: " + phone);
    }

    @Test
    @DisplayName("Spanish mobile starts with 6 or 7")
    void spanishMobilePrefix() {
        PhoneNumberGenerator gen = new PhoneNumberGenerator(Locale.of("es", "ES"));

        for (int i = 0; i < 20; i++) {
            String phone = gen.generate(false, true);
            char firstChar = phone.charAt(0);
            assertTrue(firstChar == '6' || firstChar == '7', "Spanish mobile should start with 6 or 7, got: " + phone);
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
        assertTrue(phone.matches("0\\d{1,3} \\d{4} \\d{4}"), "Expected Italian landline format, got: " + phone);
    }

    @Test
    @DisplayName("Italian locale generates formatted mobile")
    void italianFormattedMobile() {
        PhoneNumberGenerator gen = new PhoneNumberGenerator(Locale.ITALY);
        String phone = gen.generate(true, true);

        assertNotNull(phone);
        assertTrue(
                phone.matches("3\\d{2} \\d{3} \\d{4}"), "Expected Italian mobile format (3xx xxx xxxx), got: " + phone);
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
        assertTrue(
                phone.matches("\\(\\d{2}\\) [23]\\d{3}-\\d{4}"), "Expected Brazilian landline format, got: " + phone);
    }

    @Test
    @DisplayName("Brazilian locale generates formatted mobile")
    void brazilianFormattedMobile() {
        PhoneNumberGenerator gen = new PhoneNumberGenerator(Locale.of("pt", "BR"));
        String phone = gen.generate(true, true);

        assertNotNull(phone);
        assertTrue(
                phone.matches("\\(\\d{2}\\) 9\\d{4}-\\d{4}"),
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
        assertTrue(phone.matches("0\\d{1,2}-\\d{4}-\\d{4}"), "Expected Japanese landline format, got: " + phone);
    }

    @Test
    @DisplayName("Japanese locale generates formatted mobile")
    void japaneseFormattedMobile() {
        PhoneNumberGenerator gen = new PhoneNumberGenerator(Locale.JAPAN);
        String phone = gen.generate(true, true);

        assertNotNull(phone);
        assertTrue(
                phone.matches("0[789]0-\\d{4}-\\d{4}"),
                "Expected Japanese mobile format (0x0-xxxx-xxxx), got: " + phone);
    }

    @Test
    @DisplayName("Japanese mobile starts with 070, 080, or 090")
    void japaneseMobilePrefix() {
        PhoneNumberGenerator gen = new PhoneNumberGenerator(Locale.JAPAN);

        for (int i = 0; i < 30; i++) {
            String phone = gen.generate(false, true);
            assertTrue(
                    phone.startsWith("070") || phone.startsWith("080") || phone.startsWith("090"),
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
            assertFalse(
                    phone.startsWith("070") || phone.startsWith("080") || phone.startsWith("090"),
                    "Japanese landline should not be mobile prefix, got: " + phone);
        }
    }

    @Test
    @DisplayName("Japanese phone has correct length")
    void japanesePhoneLength() {
        PhoneNumberGenerator gen = new PhoneNumberGenerator(Locale.JAPAN);
        String phone = gen.generate(false);

        assertTrue(phone.length() >= 10 && phone.length() <= 11, "Japanese phone should have 10-11 digits: " + phone);
    }

    // ── Chinese phone numbers (zh_CN) ─────────────────────────────────────────

    @Test
    @DisplayName("Chinese locale generates formatted landline")
    void chineseFormattedLandline() {
        PhoneNumberGenerator gen = new PhoneNumberGenerator(Locale.CHINA);
        String phone = gen.generate(true, false);

        assertNotNull(phone);
        assertTrue(phone.matches("0\\d{2,3}-\\d{8}"), "Expected Chinese landline format, got: " + phone);
    }

    @Test
    @DisplayName("Chinese locale generates formatted mobile")
    void chineseFormattedMobile() {
        PhoneNumberGenerator gen = new PhoneNumberGenerator(Locale.CHINA);
        String phone = gen.generate(true, true);

        assertNotNull(phone);
        assertTrue(
                phone.matches("1[3-9]\\d \\d{4} \\d{4}"),
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
            assertTrue(
                    secondDigit >= '3' && secondDigit <= '9',
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
        GeneratorConfig config1 =
                GeneratorConfig.builder().locale(Locale.US).seed(42L).build();
        GeneratorConfig config2 =
                GeneratorConfig.builder().locale(Locale.US).seed(42L).build();

        PhoneNumberGenerator gen1 = new PhoneNumberGenerator(config1);
        PhoneNumberGenerator gen2 = new PhoneNumberGenerator(config2);

        List<String> list1 = gen1.generateList(50);
        List<String> list2 = gen2.generateList(50);

        assertEquals(list1, list2);
    }

    @Test
    @DisplayName("seeded generator with UK locale is reproducible")
    void seededUKReproducibility() {
        GeneratorConfig config1 =
                GeneratorConfig.builder().locale(Locale.UK).seed(123L).build();
        GeneratorConfig config2 =
                GeneratorConfig.builder().locale(Locale.UK).seed(123L).build();

        PhoneNumberGenerator gen1 = new PhoneNumberGenerator(config1);
        PhoneNumberGenerator gen2 = new PhoneNumberGenerator(config2);

        List<String> list1 = gen1.generateList(30);
        List<String> list2 = gen2.generateList(30);

        assertEquals(list1, list2);
    }

    @Test
    @DisplayName("different seeds produce different sequences")
    void differentSeeds() {
        GeneratorConfig config1 =
                GeneratorConfig.builder().locale(Locale.FRANCE).seed(111L).build();
        GeneratorConfig config2 =
                GeneratorConfig.builder().locale(Locale.FRANCE).seed(222L).build();

        PhoneNumberGenerator gen1 = new PhoneNumberGenerator(config1);
        PhoneNumberGenerator gen2 = new PhoneNumberGenerator(config2);

        List<String> list1 = gen1.generateList(50);
        List<String> list2 = gen2.generateList(50);

        assertNotEquals(list1, list2);
    }

    @Test
    @DisplayName("seeded generator with mobile/landline choice is reproducible")
    void seededMobileLandlineReproducibility() {
        GeneratorConfig config1 =
                GeneratorConfig.builder().locale(Locale.UK).seed(999L).build();
        GeneratorConfig config2 =
                GeneratorConfig.builder().locale(Locale.UK).seed(999L).build();

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

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> gen.generateList(-1));
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
            assertMatches(UK_DRAMA_ANY, phone, "UK stream value");
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
    @DisplayName("an unknown country and language use the documented en_US default")
    void unknownCountryAndLanguageUseTheDocumentedDefault() {
        assertEquals(
                new PhoneNumberGenerator(GeneratorConfig.builder()
                                .locale(Locale.US)
                                .seed(3L)
                                .build())
                        .generateList(20),
                new PhoneNumberGenerator(GeneratorConfig.builder()
                                .locale(Locale.of("xx", "YY"))
                                .seed(3L)
                                .build())
                        .generateList(20));
    }

    @Test
    @DisplayName("all supported locales generate non-null, non-empty phones")
    void allLocalesGenerateValidPhones() {
        for (SupportedLocale supportedLocale : SupportedLocale.values()) {
            Locale locale = supportedLocale.locale();
            PhoneNumberGenerator gen = new PhoneNumberGenerator(locale);
            String phone = gen.generate();

            assertNotNull(phone, "Locale " + locale + " generated null");
            assertFalse(phone.isEmpty(), "Locale " + locale + " generated empty string");
        }
    }

    @Test
    @DisplayName("expanded built-in locales no longer fall back to US phone formats")
    void expandedBuiltInLocalesUseLocaleSpecificFormats() {
        assertTrue(new PhoneNumberGenerator(Locale.of("nl", "NL"))
                .generate(true, true)
                .matches("06\\d \\d{7}"));
        assertTrue(new PhoneNumberGenerator(Locale.of("nl", "NL"))
                .generate(true, false)
                .matches("0\\d{2} \\d{7}"));
        assertTrue(new PhoneNumberGenerator(Locale.of("pl", "PL"))
                .generate(true, false)
                .matches("\\d{2} \\d{3} \\d{2} \\d{2}"));
        assertTrue(new PhoneNumberGenerator(Locale.of("ru", "RU"))
                .generate(true, true)
                .matches("9\\d{2} \\d{3}-\\d{2}-\\d{2}"));
        assertTrue(new PhoneNumberGenerator(Locale.of("ko", "KR"))
                .generate(true, true)
                .matches("01\\d-\\d{4}-\\d{4}"));
        assertTrue(new PhoneNumberGenerator(Locale.of("tr", "TR"))
                .generate(true, true)
                .matches("05\\d{2} \\d{3} \\d{2} \\d{2}"));
        assertTrue(new PhoneNumberGenerator(Locale.of("sv", "SE"))
                .generate(true, true)
                .matches("07\\d-\\d{3} \\d{2} \\d{2}"));
        assertTrue(new PhoneNumberGenerator(Locale.of("nb", "NO"))
                .generate(true, false)
                .matches("\\d{2} \\d{2} \\d{2} \\d{2}"));
        assertTrue(new PhoneNumberGenerator(Locale.of("cs", "CZ"))
                .generate(true, true)
                .matches("\\d{3} \\d{3} \\d{3}"));
        assertTrue(new PhoneNumberGenerator(Locale.of("ar", "SA"))
                .generate(true, true)
                .matches("05\\d \\d{3} \\d{4}"));
        assertTrue(new PhoneNumberGenerator(Locale.of("hi", "IN"))
                .generate(true, true)
                .matches("[6-9]\\d{4} \\d{5}"));
    }

    @Test
    @DisplayName("expanded built-in locales cover remaining mobile and landline branches")
    void expandedBuiltInLocalesCoverRemainingBranches() {
        assertTrue(new PhoneNumberGenerator(Locale.of("nl", "NL"))
                .generate(false, true)
                .matches("06\\d{8}"));
        assertTrue(new PhoneNumberGenerator(Locale.of("nl", "NL"))
                .generate(false, false)
                .matches("0\\d{9}"));
        assertTrue(new PhoneNumberGenerator(Locale.of("pl", "PL"))
                .generate(true, true)
                .matches("\\d{3} \\d{3} \\d{3}"));
        assertTrue(new PhoneNumberGenerator(Locale.of("pl", "PL"))
                .generate(false, true)
                .matches("\\d{9}"));
        assertTrue(new PhoneNumberGenerator(Locale.of("pl", "PL"))
                .generate(false, false)
                .matches("\\d{9}"));
        assertTrue(new PhoneNumberGenerator(Locale.of("ru", "RU"))
                .generate(false, false)
                .matches("\\d{10}"));
        assertTrue(new PhoneNumberGenerator(Locale.of("ko", "KR"))
                .generate(false, false)
                .matches("0\\d{9,10}"));
        assertTrue(new PhoneNumberGenerator(Locale.of("tr", "TR"))
                .generate(false, false)
                .matches("0\\d{10}"));
        assertTrue(new PhoneNumberGenerator(Locale.of("sv", "SE"))
                .generate(false, false)
                .matches("0\\d{8,9}"));
        assertTrue(new PhoneNumberGenerator(Locale.of("nb", "NO"))
                .generate(true, true)
                .matches("\\d{3} \\d{2} \\d{3}"));
        assertTrue(new PhoneNumberGenerator(Locale.of("nb", "NO"))
                .generate(false, true)
                .matches("\\d{8}"));
        assertTrue(new PhoneNumberGenerator(Locale.of("cs", "CZ"))
                .generate(false, false)
                .matches("[2-5]\\d{8}"));
        assertTrue(new PhoneNumberGenerator(Locale.of("ar", "SA"))
                .generate(false, false)
                .matches("0\\d{9}"));
        assertTrue(new PhoneNumberGenerator(Locale.of("hi", "IN"))
                .generate(false, true)
                .matches("[6-9]\\d{9}"));
        assertTrue(new PhoneNumberGenerator(Locale.of("hi", "IN"))
                .generate(true, false)
                .matches("0\\d{2,3} \\d{8}"));
        assertTrue(new PhoneNumberGenerator(Locale.of("hi", "IN"))
                .generate(false, false)
                .matches("0\\d{10,11}"));
    }

    @Test
    @DisplayName("all locales produce variety of values")
    void allLocalesProduceVariety() {
        Locale[] locales = {Locale.US, Locale.UK, Locale.GERMANY, Locale.FRANCE, Locale.JAPAN, Locale.CHINA};

        for (Locale locale : locales) {
            PhoneNumberGenerator gen = new PhoneNumberGenerator(locale);
            Set<String> seen = new HashSet<>();

            for (int i = 0; i < 100; i++) {
                seen.add(gen.generate(false));
            }

            assertTrue(
                    seen.size() > 50,
                    "Locale " + locale + " should produce variety, got " + seen.size() + " unique values");
        }
    }

    @Test
    @DisplayName("formatted phone contains separators")
    void formattedHasSeparators() {
        Locale[] locales = {Locale.US, Locale.UK, Locale.GERMANY, Locale.FRANCE};

        for (Locale locale : locales) {
            PhoneNumberGenerator gen = new PhoneNumberGenerator(locale);
            String phone = gen.generate(true);

            // Formatted should have at least one separator (space, hyphen, or parenthesis)
            assertTrue(
                    phone.matches(".*[ \\-()].*"),
                    "Formatted phone should have separators for " + locale + ", got: " + phone);
        }
    }

    @Test
    @DisplayName("unformatted phone is digits only for most locales")
    void unformattedDigitsOnly() {
        Locale[] locales = {
            Locale.US,
            Locale.GERMANY,
            Locale.FRANCE,
            Locale.of("es", "ES"),
            Locale.ITALY,
            Locale.of("pt", "BR"),
            Locale.CHINA
        };

        for (Locale locale : locales) {
            PhoneNumberGenerator gen = new PhoneNumberGenerator(locale);
            String phone = gen.generate(false);

            assertTrue(
                    phone.matches("\\d+"), "Unformatted phone should be digits only for " + locale + ", got: " + phone);
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

            mobileSet.add(mobile.substring(0, 8));
            landlineSet.add(landline.substring(0, 2));
        }

        // All mobiles come from Ofcom's 07700 900xxx drama range
        assertEquals(Set.of("07700900"), mobileSet, "UK mobiles should all start with 07700 900");

        // No landlines should start with 07
        assertTrue(landlineSet.stream().noneMatch(p -> p.equals("07")), "UK landlines should not start with 07");
    }

    @Test
    @DisplayName("generate() with no args matches generate(true)")
    void generateNoArgsMatchesTrue() {
        GeneratorConfig config =
                GeneratorConfig.builder().locale(Locale.US).seed(999L).build();

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
            assertTrue(
                    phone.contains(" 2") || phone.contains(" 3"),
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
    @DisplayName("UK landlines use the layout of their area-code length")
    void ukAllFormatTypes() {
        PhoneNumberGenerator gen = new PhoneNumberGenerator(
                GeneratorConfig.builder().locale(Locale.UK).seed(5L).build());

        Set<String> layouts = new HashSet<>();
        for (int i = 0; i < 200; i++) {
            String phone = gen.generate(true, false);
            String layout = switch (phone.indexOf(' ')) {
                case 3 -> "0\\d{2} \\d{4} \\d{4}"; // 020, 028, 029
                case 4 -> "0\\d{3} \\d{3} \\d{4}"; // 0113 to 0191
                default -> "0\\d{4} \\d{6}"; // 01632
            };
            assertTrue(phone.matches(layout), "UK landline should match " + layout + ", got: " + phone);
            layouts.add(layout);
        }

        assertEquals(3, layouts.size(), "expected all three UK landline layouts, got " + layouts);
    }

    @Test
    @DisplayName("Brazilian both mobile and landline work with formatted parameter")
    void brazilianFormattedBothTypes() {
        PhoneNumberGenerator gen = new PhoneNumberGenerator(Locale.of("pt", "BR"));

        // Test mobile formatted
        String mobileFormatted = gen.generate(true, true);
        assertTrue(mobileFormatted.matches("\\(\\d{2}\\) 9\\d{4}-\\d{4}"), "BR mobile formatted: " + mobileFormatted);

        // Test mobile unformatted
        String mobileUnformatted = gen.generate(false, true);
        assertTrue(mobileUnformatted.matches("\\d{2}9\\d{8}"), "BR mobile unformatted: " + mobileUnformatted);

        // Test landline formatted
        String landlineFormatted = gen.generate(true, false);
        assertTrue(
                landlineFormatted.matches("\\(\\d{2}\\) [23]\\d{3}-\\d{4}"),
                "BR landline formatted: " + landlineFormatted);

        // Test landline unformatted
        String landlineUnformatted = gen.generate(false, false);
        assertTrue(landlineUnformatted.matches("\\d{2}[23]\\d{7}"), "BR landline unformatted: " + landlineUnformatted);
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
                assertMatches(UK_DRAMA_MOBILE_DIGITS, mobile, "UK mobile");
                assertMatches(UK_DRAMA_LANDLINE_DIGITS, landline, "UK landline");
            } else if (locale.getCountry().equals("AU")) {
                assertTrue(mobile.startsWith("04"), "AU mobile should start with 04");
                assertFalse(landline.startsWith("04"), "AU landline should not start with 04");
            } else if (locale.equals(Locale.GERMANY)) {
                assertTrue(mobile.startsWith("01"), "DE mobile should start with 01");
                assertFalse(landline.startsWith("01"), "DE landline should not start with 01");
            } else if (locale.equals(Locale.FRANCE)) {
                assertTrue(mobile.startsWith("06") || mobile.startsWith("07"), "FR mobile should start with 06 or 07");
                assertFalse(
                        mobile.startsWith("06") && landline.startsWith("06")
                                || mobile.startsWith("07") && landline.startsWith("07"),
                        "FR mobile and landline should differ");
            } else if (locale.getCountry().equals("ES")) {
                char firstChar = mobile.charAt(0);
                assertTrue(firstChar == '6' || firstChar == '7', "ES mobile should start with 6 or 7");
                char landlineFirst = landline.charAt(0);
                assertTrue(
                        landlineFirst >= '8' && landlineFirst <= '9' || landlineFirst == '9',
                        "ES landline should not start with 6 or 7");
            } else if (locale.equals(Locale.ITALY)) {
                assertTrue(mobile.startsWith("3"), "IT mobile should start with 3");
                assertTrue(landline.startsWith("0"), "IT landline should start with 0");
            } else if (locale.equals(Locale.JAPAN)) {
                assertTrue(
                        mobile.startsWith("070") || mobile.startsWith("080") || mobile.startsWith("090"),
                        "JP mobile should start with 070/080/090");
                assertFalse(
                        landline.startsWith("070") || landline.startsWith("080") || landline.startsWith("090"),
                        "JP landline should not start with mobile prefix");
            } else if (locale.equals(Locale.CHINA)) {
                assertTrue(mobile.startsWith("1"), "CN mobile should start with 1");
                assertTrue(landline.startsWith("0"), "CN landline should start with 0");
            }
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static void assertMatches(Pattern pattern, String value, String label) {
        assertTrue(pattern.matcher(value).matches(), label + " expected /" + pattern.pattern() + "/ but got: " + value);
    }

    private static Pattern digitsOnly(Pattern formatted) {
        return Pattern.compile(formatted.pattern().replace(" ", ""));
    }

    /** The fixed part of a formatted drama number: all but the three digits that vary in a block. */
    private static String blockOf(String formatted) {
        return formatted.substring(0, formatted.length() - 3);
    }

    private static String ukPhone(ScriptedRandom random, boolean formatted, boolean mobile) {
        return ukPhone(PhoneNumberSafetyPolicy.TEST_SAFE_WHERE_AVAILABLE, random, formatted, mobile);
    }

    private static String realisticUkPhone(ScriptedRandom random, boolean formatted, boolean mobile) {
        return ukPhone(PhoneNumberSafetyPolicy.REALISTIC_UNCLASSIFIED, random, formatted, mobile);
    }

    private static String ukPhone(
            PhoneNumberSafetyPolicy policy, ScriptedRandom random, boolean formatted, boolean mobile) {
        String phone = new PhoneNumberGenerator(GeneratorConfig.builder()
                        .locale(Locale.UK)
                        .random(random)
                        .phoneNumberSafetyPolicy(policy)
                        .build())
                .generate(formatted, mobile);
        random.assertExhausted();
        return phone;
    }

    /** Scripts {@code nextInt(bound)} as {bound, value} pairs, in draw order. */
    private static ScriptedRandom scripted(int... boundsAndValues) {
        return new ScriptedRandom(boundsAndValues);
    }

    /** A random source that answers scripted draws and fails on an unexpected bound or draw. */
    private static final class ScriptedRandom extends Random {

        private final int[] boundsAndValues;
        private int position;

        ScriptedRandom(int... boundsAndValues) {
            this.boundsAndValues = boundsAndValues.clone();
        }

        @Override
        public int nextInt(int bound) {
            int draw = position / 2 + 1;
            assertTrue(position < boundsAndValues.length, "unscripted draw " + draw + " with bound " + bound);
            assertEquals(boundsAndValues[position], bound, "bound of draw " + draw);
            int value = boundsAndValues[position + 1];
            position += 2;
            return value;
        }

        void assertExhausted() {
            assertEquals(boundsAndValues.length, position, "scripted draws left unused");
        }
    }
}
