/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.location;

/**
 * Safety contract for locale-style phone-number generation.
 *
 * <p>These policies do not apply to custom templates or MSISDN output, whose caller-selected or
 * synthetic digits cannot be classified as non-routable by this policy.
 */
public enum PhoneNumberSafetyPolicy {

    /**
     * Uses a range that the numbering authority reserves for fiction and never assigns to a
     * subscriber, for the numbering plans whose fiction range this library implements:
     * <ul>
     *   <li>North American Numbering Plan (NANP) locale-style output uses NANPA's reserved
     *       fictional {@code 555-0100} through {@code 555-0199} line-number range: every locale
     *       that resolves to the United States or Canada, such as {@code en_US}, {@code en_CA},
     *       {@code fr_CA}, and the language-only {@code en}.</li>
     *   <li>United Kingdom locale-style output, for every locale whose country is {@code GB}
     *       such as {@code en_GB}, uses Ofcom's ranges for TV and radio drama: mobile numbers
     *       {@code 07700 900000} through {@code 07700 900999}, and landline numbers from 16
     *       geographic blocks of 1,000 numbers, such as {@code 020 7946 0000} through
     *       {@code 020 7946 0999} for London.</li>
     * </ul>
     *
     * <p>This is the default. Locales of other countries retain their realistic output but remain
     * unclassified; they must not be treated as safe merely because this policy is selected.
     */
    TEST_SAFE_WHERE_AVAILABLE,

    /**
     * Produces realistic-looking output without a claim of non-routability: NANP locales use
     * realistic exchange and line numbers, and United Kingdom locales use realistic area codes,
     * mobile prefixes, and subscriber numbers.
     */
    REALISTIC_UNCLASSIFIED
}
