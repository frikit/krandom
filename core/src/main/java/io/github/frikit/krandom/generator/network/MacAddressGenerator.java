/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.network;

import io.github.frikit.krandom.generator.Generator;
import io.github.frikit.krandom.generator.GeneratorConfig;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;

/**
 * Generates random IEEE 802 MAC addresses.
 *
 * <p>The default format is uppercase hexadecimal octets separated by colons
 * ({@code "XX:XX:XX:XX:XX:XX"}). Under {@link NetworkSafetyPolicy#TEST_SAFE_RESERVED} the first octet
 * has the locally administered bit set and the multicast bit clear.
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
 * This generator holds a single mutable PRNG. Concurrent calls are memory-safe but interleave the
 * random sequence (destroying reproducibility) and contend on the PRNG, so an instance is <em>not</em>
 * safe to share across threads. Confine one instance per thread, or wrap construction with
 * {@code Generators.threadLocal(...)} for deterministic concurrent use.
 */
public final class MacAddressGenerator implements Generator<String> {

    private final GeneratorConfig config;
    private final Random random;

    /**
     * Creates a MAC-address generator with default configuration.
     */
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
        this.random = config.createRandom();
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
            int octet = random.nextInt(256);
            // Locally administered and unicast, so that it cannot collide with a vendor's prefix.
            boolean first = i == 0 && config.getNetworkSafetyPolicy() == NetworkSafetyPolicy.TEST_SAFE_RESERVED;
            sb.append(String.format(Locale.ROOT, "%02X", first ? (octet | 0x02) & 0xFE : octet));
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
        return generate(separator).toLowerCase(Locale.ROOT);
    }
}
