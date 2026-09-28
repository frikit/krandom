/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.finance;

/**
 * National BBAN rules that hold for every account of a country, applied to a BBAN whose other
 * positions were drawn according to the country's IBAN registry structure.
 *
 * <p>Each rule overwrites only its own positions (national check digits or a fixed character) in
 * place. A rule returns {@code false} when the drawn payload admits no valid check digit, in which
 * case the caller draws a new payload. Bank-specific schemes (for example German, British, Danish,
 * or Swedish account checks) are not universal national rules and are not modelled.
 */
final class BbanNationalRules {

    private static final int[] SPAIN_WEIGHTS         = {1, 2, 4, 8, 5, 10, 9, 7, 3, 6};
    private static final int[] NORWAY_WEIGHTS        = {5, 4, 3, 2, 7, 6, 5, 4, 3, 2, 1};
    private static final int[] CZECH_PREFIX_WEIGHTS  = {10, 5, 8, 4, 2, 1};
    private static final int[] CZECH_ACCOUNT_WEIGHTS = {6, 3, 7, 9, 10, 5, 8, 4, 2, 1};
    private static final int[] HUNGARY_WEIGHTS       = {9, 7, 3, 1};
    private static final int[] POLAND_WEIGHTS        = {3, 9, 7, 1};

    /** Italian CIN values of the characters at odd positions, indexed by digit or letter value. */
    private static final int[] ITALY_ODD_VALUES = {
        1, 0, 5, 7, 9, 13, 15, 17, 19, 21, 2, 4, 18, 20, 11, 3, 6, 8, 12, 14, 16, 10, 22, 25, 24, 23
    };

    /** French RIB digit values of the letters {@code A} to {@code Z}. */
    private static final String FRANCE_LETTER_VALUES = "12345678912345678923456789";

    private BbanNationalRules() {
    }

    /**
     * Belgium ({@code 3!n7!n2!n}): the last two digits are the first ten digits modulo 97, or 97
     * when that remainder is 0.
     */
    static boolean belgium(char[] bban) {
        int remainder = mod97(bban, 0, 10);
        setTwoDigits(bban, 10, remainder == 0 ? 97 : remainder);
        return true;
    }

    /**
     * Czechia and Slovakia ({@code 4!n6!n10!n}): the account prefix and the account number are each
     * weighted multiples of 11, with weights 10, 5, 8, 4, 2, 1 and 6, 3, 7, 9, 10, 5, 8, 4, 2, 1.
     */
    static boolean czechOrSlovakia(char[] bban) {
        return mod11CheckDigit(bban, 4, CZECH_PREFIX_WEIGHTS) && mod11CheckDigit(bban, 10, CZECH_ACCOUNT_WEIGHTS);
    }

    /**
     * Spain ({@code 4!n4!n1!n1!n10!n}): the two control digits check {@code 00} + bank + branch and
     * the account number with weights 1, 2, 4, 8, 5, 10, 9, 7, 3, 6; a value of 10 becomes 1.
     */
    static boolean spain(char[] bban) {
        bban[8] = digitChar(spanishControlDigit(bban, 0, 2));
        bban[9] = digitChar(spanishControlDigit(bban, 10, 0));
        return true;
    }

    /** Finland ({@code 3!n11!n}): the last digit is the Luhn check digit of the first thirteen. */
    static boolean finland(char[] bban) {
        int sum = 0;
        for (int i = 0; i < 13; i++) {
            int value = digit(bban, i) * (i % 2 == 0 ? 2 : 1);
            sum += value > 9 ? value - 9 : value;
        }
        bban[13] = digitChar((10 - sum % 10) % 10);
        return true;
    }

    /**
     * France ({@code 5!n5!n11!c2!n}): the RIB key makes bank + branch + account + key, with letters
     * converted to RIB digits, a multiple of 97.
     */
    static boolean france(char[] bban) {
        int remainder = 0;
        for (int i = 0; i < 21; i++) {
            char ch = bban[i];
            int value = ch <= '9' ? ch - '0' : FRANCE_LETTER_VALUES.charAt(ch - 'A') - '0';
            remainder = (remainder * 10 + value) % 97;
        }
        setTwoDigits(bban, 21, 97 - remainder * 100 % 97);
        return true;
    }

    /**
     * Croatia ({@code 7!n10!n}): the bank code and the account number each end with an
     * ISO 7064 MOD 11,10 check digit.
     */
    static boolean croatia(char[] bban) {
        bban[6] = digitChar(iso7064Mod1110CheckDigit(bban, 0, 6));
        bban[16] = digitChar(iso7064Mod1110CheckDigit(bban, 7, 9));
        return true;
    }

    /**
     * Hungary ({@code 3!n4!n1!n15!n1!n}): the eighth and the last digit check bank + branch and the
     * account number with the repeating weights 9, 7, 3, 1.
     */
    static boolean hungary(char[] bban) {
        bban[7] = digitChar(weightedMod10CheckDigit(bban, 0, 7, HUNGARY_WEIGHTS));
        bban[23] = digitChar(weightedMod10CheckDigit(bban, 8, 15, HUNGARY_WEIGHTS));
        return true;
    }

    /**
     * Italy ({@code 1!a5!n5!n12!c}): the leading CIN letter checks ABI, CAB, and account number.
     */
    static boolean italy(char[] bban) {
        int sum = 0;
        for (int i = 1; i < 23; i++) {
            char ch = bban[i];
            int value = ch <= '9' ? ch - '0' : ch - 'A';
            sum += i % 2 == 1 ? ITALY_ODD_VALUES[value] : value;
        }
        bban[0] = (char) ('A' + sum % 26);
        return true;
    }

    /**
     * Norway ({@code 4!n6!n1!n}): MOD 11 check digit with weights 5, 4, 3, 2, 7, 6, 5, 4, 3, 2;
     * payloads that would need the check value 10 are not valid account numbers.
     */
    static boolean norway(char[] bban) {
        return mod11CheckDigit(bban, 0, NORWAY_WEIGHTS);
    }

    /**
     * Poland ({@code 8!n16!n}): the eighth digit of the bank sort code checks its first seven digits
     * with weights 3, 9, 7, 1, 3, 9, 7.
     */
    static boolean poland(char[] bban) {
        bban[7] = digitChar(weightedMod10CheckDigit(bban, 0, 7, POLAND_WEIGHTS));
        return true;
    }

    /**
     * Portugal ({@code 4!n4!n11!n2!n}): the NIB check digits are ISO 7064 MOD 97-10 check digits of
     * bank + branch + account.
     */
    static boolean portugal(char[] bban) {
        setTwoDigits(bban, 19, 98 - mod97(bban, 0, 19) * 100 % 97);
        return true;
    }

    /** Turkey ({@code 5!n1!n16!c}): the digit after the bank code is reserved and always 0. */
    static boolean turkey(char[] bban) {
        bban[5] = '0';
        return true;
    }

    /** Sets the digit weighted 1 at the end of the group so the weighted sum is a multiple of 11. */
    private static boolean mod11CheckDigit(char[] bban, int offset, int[] weights) {
        int last = weights.length - 1;
        int sum = 0;
        for (int i = 0; i < last; i++) {
            sum += digit(bban, offset + i) * weights[i];
        }
        int check = (11 - sum % 11) % 11;
        if (check == 10) {
            return false;
        }
        bban[offset + last] = digitChar(check);
        return true;
    }

    /** Check digit, weighted 1, that makes the repeating-weight sum of the group a multiple of 10. */
    private static int weightedMod10CheckDigit(char[] bban, int offset, int length, int[] weights) {
        int sum = 0;
        for (int i = 0; i < length; i++) {
            sum += digit(bban, offset + i) * weights[i % weights.length];
        }
        return (10 - sum % 10) % 10;
    }

    private static int spanishControlDigit(char[] bban, int offset, int firstWeight) {
        int sum = 0;
        for (int i = firstWeight; i < SPAIN_WEIGHTS.length; i++) {
            sum += digit(bban, offset + i - firstWeight) * SPAIN_WEIGHTS[i];
        }
        int check = (11 - sum % 11) % 11;
        return check == 10 ? 1 : check;
    }

    private static int iso7064Mod1110CheckDigit(char[] bban, int offset, int length) {
        int product = 10;
        for (int i = offset; i < offset + length; i++) {
            int sum = (product + digit(bban, i)) % 10;
            product = (sum == 0 ? 10 : sum) * 2 % 11;
        }
        return (11 - product) % 10;
    }

    private static int mod97(char[] bban, int offset, int length) {
        int remainder = 0;
        for (int i = offset; i < offset + length; i++) {
            remainder = (remainder * 10 + digit(bban, i)) % 97;
        }
        return remainder;
    }

    private static void setTwoDigits(char[] bban, int offset, int value) {
        bban[offset] = digitChar(value / 10);
        bban[offset + 1] = digitChar(value % 10);
    }

    private static int digit(char[] bban, int index) {
        return bban[index] - '0';
    }

    private static char digitChar(int value) {
        return (char) ('0' + value);
    }
}
