/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.network;

import org.github.krandom.generator.Generator;
import org.github.krandom.generator.GeneratorConfig;

import java.security.SecureRandom;
import java.util.Objects;
import java.util.Random;
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
 * <p><strong>Basic Usage:</strong>
 * <pre>{@code
 * IPv6Generator gen = new IPv6Generator();
 * String ip = gen.generate();             // "2001:db8:85a3:0:0:8a2e:370:7334"
 * var list = gen.generateList(10);
 * }</pre>
 *
 * <p><strong>Seeded Generation:</strong>
 * <pre>{@code
 * GeneratorConfig config = GeneratorConfig.builder().seed(12345L).build();
 * IPv6Generator gen = new IPv6Generator(config);
 * String ip = gen.generate();  // Reproducible output
 * }</pre>
 *
 * <p><strong>Thread Safety:</strong>
 * This generator is thread-safe and can be shared across threads.
 */
public final class IPv6Generator implements Generator<String> {

    /**
     * Number of 16-bit groups in an IPv6 address (RFC 4291 §2.2).
     */
    private static final int GROUPS = 8;
    /**
     * Maximum value of one 16-bit group (0xFFFF = 65535).
     */
    private static final int GROUP_MAX = 0x10000; // nextInt(exclusive upper bound)
    private final GeneratorConfig config;
    private final Random          random;

    /**
     * Creates an IPv6 generator with default configuration.
     */
    public IPv6Generator() {
        this(GeneratorConfig.defaults());
    }

    /**
     * Creates an IPv6 generator with the specified configuration.
     *
     * @param config the generator configuration; must not be {@code null}
     * @throws NullPointerException if {@code config} is {@code null}
     */
    public IPv6Generator(GeneratorConfig config) {
        this.config = Objects.requireNonNull(config, "config must not be null");
        this.random = config.createRandom();
    }

    /**
     * {@inheritDoc}
     *
     * <p>Generates a random IPv6 address.
     *
     * @return an IPv6 address in colon-separated notation; never {@code null}
     */
    @Override
    public String generate() {
        StringJoiner joiner = new StringJoiner(":");
        for (int i = 0; i < GROUPS; i++) {
            // nextInt(0x10000) → [0x0000, 0xFFFF]; Integer.toHexString suppresses leading zeros.
            joiner.add(Integer.toHexString(random.nextInt(GROUP_MAX)));
        }
        return joiner.toString();
    }

    /**
     * Generates IPv6 CIDR notation such as {@code 2001:db8::/64} (without zero-compression).
     */
    public String generateCidr() {
        int prefix = 16 + random.nextInt(113); // [16, 128]
        return generate() + "/" + prefix;
    }
}
