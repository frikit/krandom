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
 * Generates random IEEE 802 MAC addresses.
 *
 * <p>The default format is uppercase hexadecimal octets separated by colons
 * ({@code "XX:XX:XX:XX:XX:XX"}).
 *
 * <pre>{@code
 * MacAddressGenerator gen = new MacAddressGenerator();
 * String mac       = gen.generate();            // "A1:B2:C3:D4:E5:F6"
 * String dashMac   = gen.generate('-');          // "A1-B2-C3-D4-E5-F6"
 * String lower     = gen.generateLowercase();    // "a1:b2:c3:d4:e5:f6"
 * String lowerDash = gen.generateLowercase('-'); // "a1-b2-c3-d4-e5-f6"
 * }</pre>
 *
 * <p><strong>Seeded Generation:</strong>
 * <pre>{@code
 * GeneratorConfig config = GeneratorConfig.builder().seed(42L).build();
 * MacAddressGenerator gen = new MacAddressGenerator(config);
 * String mac = gen.generate();  // Reproducible output
 * }</pre>
 *
 * <p><strong>Thread Safety:</strong>
 * This generator is thread-safe and can be shared across threads.
 */
public final class MacAddressGenerator implements Generator<String> {

    private final GeneratorConfig config;
    private final Random random;

    /** Creates a MAC-address generator with default configuration. */
    public MacAddressGenerator() {
        this(GeneratorConfig.defaults());
    }

    /**
     * Creates a MAC-address generator with the specified configuration.
     *
     * @param config the generator configuration; must not be {@code null}
     * @throws NullPointerException if {@code config} is {@code null}
     */
    public MacAddressGenerator(GeneratorConfig config) {
        this.config = Objects.requireNonNull(config, "config must not be null");
        this.random = config.getSeed().isPresent()
                ? new Random(config.getSeed().getAsLong())
                : new SecureRandom();
    }

    /**
     * {@inheritDoc}
     *
     * <p>Generates a MAC address in the form {@code "XX:XX:XX:XX:XX:XX"}
     * (uppercase, colon-separated).
     *
     * @return a MAC-address string; never {@code null}
     */
    @Override
    public String generate() {
        return generate(':');
    }

    /**
     * Generates a MAC address (uppercase) using the supplied separator character.
     *
     * @param separator the character inserted between each octet pair
     * @return a MAC-address string; never {@code null}
     */
    public String generate(char separator) {
        StringBuilder sb = new StringBuilder(17);
        for (int i = 0; i < 6; i++) {
            if (i > 0) sb.append(separator);
            sb.append(String.format("%02X", random.nextInt(256)));
        }
        return sb.toString();
    }

    /**
     * Generates a lowercase MAC address, colon-separated ({@code "xx:xx:xx:xx:xx:xx"}).
     *
     * @return a lowercase MAC-address string; never {@code null}
     */
    public String generateLowercase() {
        return generateLowercase(':');
    }

    /**
     * Generates a lowercase MAC address using the supplied separator character.
     *
     * @param separator the character inserted between each octet pair
     * @return a lowercase MAC-address string; never {@code null}
     */
    public String generateLowercase(char separator) {
        return generate(separator).toLowerCase();
    }
}
