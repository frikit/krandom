/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.finance;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * ISO 13616 IBAN registry entries of the countries with built-in IBAN support.
 *
 * <p>Each constant is named after its ISO 3166-1 alpha-2 country code and holds the country's BBAN
 * structure in IBAN-registry notation, for example {@code 4!a6!n8!n}: a length, {@code !} for a
 * fixed length, and a character class ({@code n} digits, {@code a} upper-case letters, {@code c}
 * upper-case letters and digits). The IBAN length is the BBAN length plus four. Where a national
 * rule defines check digits or a fixed character inside the BBAN of every account, the constant
 * applies it through {@link BbanNationalRules}.
 *
 * <p>Locales whose country has no entry resolve to {@link #DEFAULT}.
 */
enum IbanCountryFormat {

    AT("5!n11!n"),
    BE("3!n7!n2!n", BbanNationalRules::belgium),
    BG("4!a4!n2!n8!c"),
    BR("8!n5!n10!n1!a1!c"),
    CH("5!n12!c"),
    CZ("4!n6!n10!n", BbanNationalRules::czechOrSlovakia),
    DE("8!n10!n"),
    DK("4!n9!n1!n"),
    ES("4!n4!n1!n1!n10!n", BbanNationalRules::spain),
    FI("3!n11!n", BbanNationalRules::finland),
    FR("5!n5!n11!c2!n", BbanNationalRules::france),
    GB("4!a6!n8!n"),
    GR("3!n4!n16!c"),
    HR("7!n10!n", BbanNationalRules::croatia),
    HU("3!n4!n1!n15!n1!n", BbanNationalRules::hungary),
    IE("4!a6!n8!n"),
    IL("3!n3!n13!n"),
    IT("1!a5!n5!n12!c", BbanNationalRules::italy),
    NL("4!a10!n"),
    NO("4!n6!n1!n", BbanNationalRules::norway),
    PL("8!n16!n", BbanNationalRules::poland),
    PT("4!n4!n11!n2!n", BbanNationalRules::portugal),
    RO("4!a16!c"),
    RU("9!n5!n15!c"),
    SA("2!n18!c"),
    SE("3!n16!n1!n"),
    SK("4!n6!n10!n", BbanNationalRules::czechOrSlovakia),
    TR("5!n1!n16!c", BbanNationalRules::turkey),
    UA("6!n19!c");

    /** IBAN country used for locales whose country has no IBAN format: Germany. */
    static final IbanCountryFormat DEFAULT = DE;

    private static final String DIGITS        = "0123456789";
    private static final String LETTERS       = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String ALPHANUMERICS = DIGITS + LETTERS;

    private static final Map<String, IbanCountryFormat> BY_COUNTRY = Arrays.stream(values())
        .collect(Collectors.toUnmodifiableMap(IbanCountryFormat::name, Function.identity()));

    /** Applies a national rule in place; returns {@code false} when the payload must be redrawn. */
    @FunctionalInterface
    interface NationalRule {

        boolean apply(char[] bban);
    }

    private final String[]     alphabets;
    private final NationalRule nationalRule;

    IbanCountryFormat(String bbanStructure) {
        this(bbanStructure, bban -> true);
    }

    IbanCountryFormat(String bbanStructure, NationalRule nationalRule) {
        this.alphabets = alphabetsPerPosition(bbanStructure);
        this.nationalRule = nationalRule;
    }

    /**
     * Returns the format of the locale's country, or {@link #DEFAULT} when the country has none.
     */
    static IbanCountryFormat forLocale(Locale locale) {
        return BY_COUNTRY.getOrDefault(locale.getCountry(), DEFAULT);
    }

    /** Draws a BBAN that follows the registry structure and the national rule of this country. */
    String randomBban(Random random) {
        char[] bban = new char[alphabets.length];
        do {
            for (int i = 0; i < bban.length; i++) {
                bban[i] = alphabets[i].charAt(random.nextInt(alphabets[i].length()));
            }
        } while (!applyNationalRule(bban));
        return new String(bban);
    }

    /** Applies the national rule in place; returns {@code false} when the payload must be redrawn. */
    boolean applyNationalRule(char[] bban) {
        return nationalRule.apply(bban);
    }

    private static String[] alphabetsPerPosition(String bbanStructure) {
        List<String> alphabets = new ArrayList<>();
        int length = 0;
        for (char ch : bbanStructure.toCharArray()) {
            if (Character.isDigit(ch)) {
                length = length * 10 + ch - '0';
            } else if (ch != '!') {
                String alphabet = ch == 'n' ? DIGITS : ch == 'a' ? LETTERS : ALPHANUMERICS;
                alphabets.addAll(Collections.nCopies(length, alphabet));
                length = 0;
            }
        }
        return alphabets.toArray(String[]::new);
    }
}
