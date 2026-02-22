/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.network;

import org.github.krandom.generator.Generator;

import java.security.SecureRandom;
import java.util.StringJoiner;

/**
 * Generates random IPv6 addresses in colon-separated notation (RFC 4291, RFC 5952).
 *
 * <p><b>RFC compliance</b>
 * <ul>
 *   <li>RFC 4291 §2.2 — 128-bit address represented as eight 16-bit groups separated by
 *       {@code :}, each group expressed in hexadecimal.</li>
 *   <li>RFC 5952 §4.1 — leading zeros within each group are suppressed
 *       (e.g., {@code db8} not {@code 0db8}).</li>
 *   <li>RFC 5952 §4.3 — lowercase hexadecimal digits.</li>
 *   <li>RFC 5952 §4.2 — {@code ::} compression is intentionally omitted.  Random 128-bit
 *       values almost never contain consecutive all-zero groups; omitting compression
 *       produces unambiguous addresses that remain valid per RFC 4291 and are accepted
 *       by all standard validators.</li>
 * </ul>
 *
 * <pre>{@code
 *   IPv6Generator gen = new IPv6Generator();
 *   String ip   = gen.generate();             // "2001:db8:85a3:0:0:8a2e:370:7334"
 *   var    list = gen.generateList(10);
 * }</pre>
 */
public final class IPv6Generator implements Generator<String> {

    private final SecureRandom random = new SecureRandom();

    /** Number of 16-bit groups in an IPv6 address (RFC 4291 §2.2). */
    private static final int GROUPS = 8;

    /** Maximum value of one 16-bit group (0xFFFF = 65535). */
    private static final int GROUP_MAX = 0x10000; // nextInt(exclusive upper bound)

    @Override
    public String generate() {
        StringJoiner joiner = new StringJoiner(":");
        for (int i = 0; i < GROUPS; i++) {
            // nextInt(0x10000) → [0x0000, 0xFFFF]; Integer.toHexString suppresses leading zeros.
            joiner.add(Integer.toHexString(random.nextInt(GROUP_MAX)));
        }
        return joiner.toString();
    }
}
