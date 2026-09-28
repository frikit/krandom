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
     * Uses NANPA's reserved fictional {@code 555-0100} through {@code 555-0199} line-number
     * range for North American Numbering Plan (NANP) locale-style output: every locale that
     * resolves to the United States or Canada, such as {@code en_US}, {@code en_CA},
     * {@code fr_CA}, and the language-only {@code en}.
     *
     * <p>This is the default. Locales of other countries retain their realistic output but remain
     * unclassified; they must not be treated as safe merely because this policy is selected.
     */
    TEST_SAFE_WHERE_AVAILABLE,

    /**
     * Produces realistic-looking output without a claim of non-routability; NANP locales use
     * realistic exchange and line numbers.
     */
    REALISTIC_UNCLASSIFIED
}
