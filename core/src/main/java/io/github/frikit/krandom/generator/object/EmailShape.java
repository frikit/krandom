/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.object;

import java.net.IDN;
import java.util.regex.Pattern;

/**
 * The shape an {@code @Email} candidate must have: the rules Hibernate Validator's {@code @Email}
 * applies to an address with a dot-atom local part and a domain name.
 *
 * <p>The address splits at its last {@code @}. The local part has at most 64 characters and is atoms
 * joined by single dots, each atom made of ASCII letters and digits, the symbols
 * {@code !#$%&'*+/=?^_`{|}~-}, and characters from U+0080 to U+FFFF. The domain is labels joined by
 * single dots, each made of the same characters with a dash only between two others. It must convert
 * with {@link IDN#toASCII(String)}, which rejects an empty label and one of more than 63 characters,
 * and have at most 255 characters once converted. As in Hibernate Validator, a domain needs no dot.
 *
 * <p>Hibernate Validator also accepts a quoted local part, such as {@code "a b"@example.com}, and an
 * address literal, such as {@code a@[192.0.2.1]}. This shape does not: no source generates them, and
 * validators disagree on them, so such a candidate is drawn again. The empty string, which Bean
 * Validation treats as valid, is no address either. Every candidate this shape accepts passes
 * Hibernate Validator's {@code @Email}.
 */
final class EmailShape {

    private static final int MAX_LOCAL_PART_LENGTH = 64;
    private static final int MAX_DOMAIN_LENGTH = 255;

    /** A character of an unquoted atom. */
    private static final String ATOM = "[a-z0-9!#$%&'*+/=?^_`{|}~\\u0080-\\uFFFF-]";

    /** A character of a domain label other than the dash. */
    private static final String LABEL = "[a-z0-9!#$%&'*+/=?^_`{|}~\\u0080-\\uFFFF]";

    private static final Pattern LOCAL_PART =
            Pattern.compile(ATOM + "+(?:\\." + ATOM + "+)*", Pattern.CASE_INSENSITIVE);
    private static final Pattern DOMAIN_NAME = Pattern.compile(
            LABEL + "+(?:-+" + LABEL + "+)*(?:\\." + LABEL + "+(?:-+" + LABEL + "+)*)*", Pattern.CASE_INSENSITIVE);

    private EmailShape() {}

    /** Whether a candidate is an address in this shape. */
    static boolean accepts(String candidate) {
        int at = candidate.lastIndexOf('@');
        if (at < 0) {
            return false;
        }
        String localPart = candidate.substring(0, at);
        String domain = candidate.substring(at + 1);
        return localPart.length() <= MAX_LOCAL_PART_LENGTH
                && LOCAL_PART.matcher(localPart).matches()
                && isDomainName(domain);
    }

    private static boolean isDomainName(String domain) {
        String ascii;
        try {
            ascii = IDN.toASCII(domain);
        } catch (IllegalArgumentException notConvertible) {
            return false;
        }
        return ascii.length() <= MAX_DOMAIN_LENGTH
                && DOMAIN_NAME.matcher(domain).matches();
    }
}
