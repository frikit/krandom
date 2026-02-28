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

/**
 * Generates random IPv4 addresses in dotted-decimal notation (RFC 791).
 *
 * <p><b>RFC compliance</b>
 * <ul>
 *   <li>RFC 791 — address format: four decimal octets in [0, 255] separated by {@code .}</li>
 *   <li>First octet restricted to [0, 223] — excludes Class D multicast (224–239, RFC 3171)
 *       and Class E reserved (240–255, RFC 1112).</li>
 *   <li>Octets 2–4 are in the full [0, 255] range.</li>
 * </ul>
 *
 * <p><strong>Basic Usage:</strong>
 * <pre>{@code
 * IPv4Generator gen = new IPv4Generator();
 * String ip = gen.generate();             // "192.0.2.1"
 * var list = gen.generateList(10);
 * }</pre>
 *
 * <p><strong>Seeded Generation:</strong>
 * <pre>{@code
 * GeneratorConfig config = GeneratorConfig.builder().seed(12345L).build();
 * IPv4Generator gen = new IPv4Generator(config);
 * String ip = gen.generate();  // Reproducible output
 * }</pre>
 *
 * <p><strong>Thread Safety:</strong>
 * This generator is thread-safe and can be shared across threads.
 */
public final class IPv4Generator implements Generator<String> {

    private final GeneratorConfig config;
    private final Random random;

    /**
     * Creates an IPv4 generator with default configuration.
     */
    public IPv4Generator() {
        this(GeneratorConfig.defaults());
    }

    /**
     * Creates an IPv4 generator with the specified configuration.
     *
     * @param config the generator configuration; must not be {@code null}
     * @throws NullPointerException if {@code config} is {@code null}
     */
    public IPv4Generator(GeneratorConfig config) {
        this.config = Objects.requireNonNull(config, "config must not be null");
        this.random = config.getSeed().isPresent()
                ? new Random(config.getSeed().getAsLong())
                : new SecureRandom();
    }

    /**
     * {@inheritDoc}
     *
     * <p>Generates a random IPv4 address.
     *
     * @return an IPv4 address in dotted-decimal notation; never {@code null}
     */
    @Override
    public String generate() {
        // nextInt(n) returns [0, n-1] — all ranges are inclusive on both ends as intended.
        int octet1 = random.nextInt(224);   // [0, 223]: unicast only (excludes multicast/reserved)
        int octet2 = random.nextInt(256);   // [0, 255]
        int octet3 = random.nextInt(256);   // [0, 255]
        int octet4 = random.nextInt(256);   // [0, 255]
        return octet1 + "." + octet2 + "." + octet3 + "." + octet4;
    }
}
