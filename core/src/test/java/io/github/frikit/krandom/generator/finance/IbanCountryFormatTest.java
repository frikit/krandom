/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.finance;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Locale;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("IBAN country formats")
class IbanCountryFormatTest {

    @ParameterizedTest(name = "{0}: {1} -> {2}")
    @CsvSource({
        // Published IBAN registry samples with their national check positions blanked to zero.
        "BE, 539007547000, 539007547034",
        "CZ, 08000000102000145390, 08000000192000145399",
        "ES, 21000418000200051332, 21000418450200051332",
        "FI, 12345600000780, 12345600000785",
        "FR, 20041010050500013M02600, 20041010050500013M02606",
        "HR, 10010001863000169, 10010051863000160",
        "HU, 117730101111101800000009, 117730161111101800000000",
        "IT, A0542811101000000123456, X0542811101000000123456",
        "NO, 86011117940, 86011117947",
        "PL, 109010100000071219812874, 109010140000071219812874",
        "PT, 000201231234567890100, 000201231234567890154",
        "SK, 12000000108742637540, 12000000198742637541",
        "TR, 0006170519786457841326, 0006100519786457841326",
        // Further published samples.
        "ES, 21000813000123456789, 21000813610123456789",
        "FR, 30006000011234567890100, 30006000011234567890189",
        "NO, 30001234560, 30001234567",
        "HU, 116000000000000012345670, 116000060000000012345676",
        "CZ, 08000000001234567890, 08000000001234567899"
    })
    @DisplayName("national rules reproduce the check digits of published IBANs")
    void nationalRulesReproducePublishedCheckDigits(String country, String blanked, String expected) {
        char[] bban = blanked.toCharArray();

        assertTrue(IbanCountryFormat.valueOf(country).applyNationalRule(bban));
        assertEquals(expected, new String(bban));
    }

    @ParameterizedTest(name = "{0}")
    @CsvSource({
        "DE, 370400440532013000",
        "GB, NWBK60161331926819",
        "NL, ABNA0417164300",
        "BR, 00360305000010009795493C1",
        "RU, 04452522540817810538091310419"
    })
    @DisplayName("countries without a universal national rule leave the BBAN unchanged")
    void countriesWithoutNationalRuleLeaveBbanUnchanged(String country, String sample) {
        char[] bban = sample.toCharArray();

        assertTrue(IbanCountryFormat.valueOf(country).applyNationalRule(bban));
        assertEquals(sample, new String(bban));
    }

    @Test
    @DisplayName("Belgian check digits are 97 when the first ten digits are a multiple of 97")
    void belgianCheckDigitsUse97ForZeroRemainder() {
        char[] bban = "000000000000".toCharArray();

        assertTrue(IbanCountryFormat.BE.applyNationalRule(bban));
        assertEquals("000000000097", new String(bban));
    }

    @Test
    @DisplayName("Spanish control digits map 11 to 0 and 10 to 1")
    void spanishControlDigitsMapElevenAndTen() {
        char[] bban = "00000000991000000000".toCharArray();

        assertTrue(IbanCountryFormat.ES.applyNationalRule(bban));
        assertEquals("00000000011000000000", new String(bban));
    }

    @Test
    @DisplayName("Norwegian payloads whose check digit would be 10 are rejected")
    void norwegianPayloadWithoutCheckDigitIsRejected() {
        assertFalse(IbanCountryFormat.NO.applyNationalRule("03000000000".toCharArray()));
    }

    @Test
    @DisplayName("Czech and Slovak prefixes or accounts whose check digit would be 10 are rejected")
    void czechSlovakPayloadWithoutCheckDigitIsRejected() {
        assertFalse(IbanCountryFormat.CZ.applyNationalRule("08000003000000000000".toCharArray()));
        assertFalse(IbanCountryFormat.SK.applyNationalRule("12000000000000000300".toCharArray()));
    }

    @Test
    @DisplayName("a rejected payload is redrawn")
    void rejectedPayloadIsRedrawn() {
        Random scripted = new ScriptedRandom(
            0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0,   // rejected: check digit would be 10
            8, 6, 0, 1, 1, 1, 1, 7, 9, 4, 0);  // accepted: 8601111794 + 7

        assertEquals("86011117947", IbanCountryFormat.NO.randomBban(scripted));
    }

    @Test
    @DisplayName("locale countries without an IBAN format resolve to the German default")
    void forLocale() {
        assertSame(IbanCountryFormat.DE, IbanCountryFormat.DEFAULT);
        assertSame(IbanCountryFormat.BE, IbanCountryFormat.forLocale(Locale.of("nl", "BE")));
        assertSame(IbanCountryFormat.GB, IbanCountryFormat.forLocale(Locale.UK));
        assertSame(IbanCountryFormat.DEFAULT, IbanCountryFormat.forLocale(Locale.US));
        assertSame(IbanCountryFormat.DEFAULT, IbanCountryFormat.forLocale(Locale.JAPAN));
        assertSame(IbanCountryFormat.DEFAULT, IbanCountryFormat.forLocale(Locale.ENGLISH));
        assertSame(IbanCountryFormat.DEFAULT, IbanCountryFormat.forLocale(Locale.ROOT));
    }

    @Test
    @DisplayName("random BBANs use the registry character classes")
    void randomBbanUsesRegistryCharacterClasses() {
        Random random = new Random(5L);
        for (int i = 0; i < 200; i++) {
            assertTrue(IbanCountryFormat.GB.randomBban(random).matches("[A-Z]{4}[0-9]{14}"));
            assertTrue(IbanCountryFormat.BG.randomBban(random).matches("[A-Z]{4}[0-9]{6}[0-9A-Z]{8}"));
            assertTrue(IbanCountryFormat.BR.randomBban(random).matches("[0-9]{23}[A-Z][0-9A-Z]"));
        }
    }

    /** Returns the scripted values from {@link #nextInt(int)} in order. */
    private static final class ScriptedRandom extends Random {

        private final int[] values;
        private int next;

        ScriptedRandom(int... values) {
            this.values = values;
        }

        @Override
        public int nextInt(int bound) {
            return values[next++];
        }
    }
}
