/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.location;

import java.util.List;
import java.util.Random;

/**
 * Compact national number or code layout.
 *
 * <p>In {@code pattern}, {@code @} is replaced by one randomly selected entry of
 * {@code prefixes}, {@code #} by a random digit, and {@code ?} by a random character of
 * {@code letters}. Literal digits, such as a trunk prefix, are always kept; every other character
 * is a separator that is kept only when separators are requested.
 *
 * @param pattern  the layout pattern
 * @param prefixes values for the {@code @} placeholder
 * @param letters  characters for the {@code ?} placeholder
 */
record FormatTemplate(String pattern, List<String> prefixes, String letters) {

    static FormatTemplate template(String pattern, String... prefixes) {
        return new FormatTemplate(pattern, List.of(prefixes), "");
    }

    static FormatTemplate withLetters(String pattern, String letters, String... prefixes) {
        return new FormatTemplate(pattern, List.of(prefixes), letters);
    }

    String render(Random random, boolean separators) {
        StringBuilder out = new StringBuilder(pattern().length() + 4);
        for (int i = 0; i < pattern().length(); i++) {
            char symbol = pattern().charAt(i);
            switch (symbol) {
                case '@' -> out.append(prefixes().get(random.nextInt(prefixes().size())));
                case '#' -> out.append((char) ('0' + random.nextInt(10)));
                case '?' -> out.append(letters().charAt(random.nextInt(letters().length())));
                default -> {
                    if (separators || Character.isDigit(symbol)) {
                        out.append(symbol);
                    }
                }
            }
        }
        return out.toString();
    }
}
