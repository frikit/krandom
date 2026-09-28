/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.location;

import java.util.Map;
import java.util.Random;
import java.util.Set;

import static io.github.frikit.krandom.generator.location.FormatTemplate.template;
import static io.github.frikit.krandom.generator.location.FormatTemplate.withLetters;

/**
 * Postal-code layouts for built-in countries that have no dedicated formatting method in
 * {@link PostalCodeGenerator}.
 */
final class PostalCodeLayouts {

    private static final String[] NON_ZERO_DIGITS = { "1", "2", "3", "4", "5", "6", "7", "8", "9" };

    /** Canadian forward sortation areas never start with D, F, I, O, Q, U, W or Z. */
    private static final String[] CA_FIRST_LETTERS = {
        "A", "B", "C", "E", "G", "H", "J", "K", "L", "M", "N", "P", "R", "S", "T", "V", "X", "Y"
    };
    private static final String CA_LETTERS = "ABCEGHJKLMNPRSTVWXYZ";

    /** Eircode routing keys (a representative subset) and the unique-identifier alphabet. */
    private static final String[] IE_ROUTING_KEYS = {
        "A94", "A96", "C15", "D01", "D02", "D04", "D06", "D08", "D12", "D15", "D18", "D24", "D6W",
        "E91", "F91", "H91", "K78", "N91", "P31", "R95", "T12", "T23", "V94", "W91", "X91", "Y35"
    };
    private static final String IE_IDENTIFIER_CHARACTERS = "0123456789ACDEFHKNPRTVWXY";

    /** Argentine CPA province letters (every letter except I and O) and block-face letters. */
    private static final String AR_LETTERS = "ABCDEFGHJKLMNPQRSTUVWXYZ";

    private static final Map<String, Layout> LAYOUTS = Map.ofEntries(
        // Argentina: legacy 4-digit code; extended is the CPA "C1425DKF"
        layout("AR", template("@###", NON_ZERO_DIGITS), withLetters("?@###???", AR_LETTERS, NON_ZERO_DIGITS)),
        layout("AT", template("@###", NON_ZERO_DIGITS)),
        layout("BE", template("@###", NON_ZERO_DIGITS)),
        layout("BG", template("@###", NON_ZERO_DIGITS)),
        layout("CA", withLetters("@#? #?#", CA_LETTERS, CA_FIRST_LETTERS)),
        layout("CH", template("@###", NON_ZERO_DIGITS)),
        layout("DK", template("@###", NON_ZERO_DIGITS)),
        layout("FI", template("#####")),
        layout("GR", template("@## ##", "1", "2", "3", "4", "5", "6", "7", "8")),
        layout("HR", template("@###", "10", "20", "21", "22", "23", "31", "32", "33", "34", "35", "40", "42", "43",
                        "44", "47", "48", "49", "51", "52", "53")),
        layout("HU", template("@###", NON_ZERO_DIGITS)),
        layout("ID", template("@####", NON_ZERO_DIGITS)),
        layout("IE", withLetters("@ ????", IE_IDENTIFIER_CHARACTERS, IE_ROUTING_KEYS)),
        layout("IL", template("@######", NON_ZERO_DIGITS)),
        layout("MX", template("#####")),
        layout("MY", template("#####")),
        layout("NZ", template("####")),
        layout("PT", template("@###-###", NON_ZERO_DIGITS)),
        layout("RO", template("######")),
        layout("SK", template("@## ##", "0", "8", "9")),
        layout("TH", template("@####", NON_ZERO_DIGITS)),
        // Taiwan: 3-digit code; extended is the 3+2 five-digit code
        layout("TW", template("@##", NON_ZERO_DIGITS), template("@####", NON_ZERO_DIGITS)),
        layout("UA", template("#####")),
        layout("VN", template("@####", NON_ZERO_DIGITS)),
        layout("ZA", template("####"))
    );

    private PostalCodeLayouts() {
    }

    static Set<String> countries() {
        return LAYOUTS.keySet();
    }

    static Layout forCountry(String country) {
        return LAYOUTS.get(country);
    }

    private static Map.Entry<String, Layout> layout(String country, FormatTemplate basicAndExtended) {
        return layout(country, basicAndExtended, basicAndExtended);
    }

    private static Map.Entry<String, Layout> layout(String country, FormatTemplate basic, FormatTemplate extended) {
        return Map.entry(country, new Layout(basic, extended));
    }

    /**
     * Basic and extended postal-code layouts of one country; most countries use one layout for both.
     *
     * @param basic    layout returned by {@code generate(false)}
     * @param extended layout returned by {@code generate(true)}
     */
    record Layout(FormatTemplate basic, FormatTemplate extended) {

        String generate(Random random, boolean extendedFormat) {
            return (extendedFormat ? extended() : basic()).render(random, true);
        }
    }
}
