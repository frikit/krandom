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
    private final Random          random;

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

    static boolean isPrivate(String ip) {
        String[] parts = ip.split("\\.");
        int a = Integer.parseInt(parts[0]);
        int b = Integer.parseInt(parts[1]);
        return a == 10
               || (a == 172 && b >= 16 && b <= 31)
               || (a == 192 && b == 168);
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

    /**
     * Generates a private RFC1918 IPv4 address.
     */
    public String generatePrivate() {
        int choice = random.nextInt(3);
        return switch (choice) {
            case 0 -> "10." + random.nextInt(256) + "." + random.nextInt(256) + "." + random.nextInt(256);
            case 1 -> "172." + (16 + random.nextInt(16)) + "." + random.nextInt(256) + "." + random.nextInt(256);
            default -> "192.168." + random.nextInt(256) + "." + random.nextInt(256);
        };
    }

    /**
     * Generates a public (non-private, non-loopback, non-link-local) IPv4 address.
     */
    public String generatePublic() {
        int[] allowedFirstOctets = {
            1, 2, 3, 4, 5, 6, 7, 8, 9,
            11, 12, 13, 14, 15,
            16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26,
            27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39,
            40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50,
            51, 52, 53, 54, 55, 56, 57, 58, 59, 60,
            61, 62, 63, 64, 65, 66, 67, 68, 69, 70,
            71, 72, 73, 74, 75, 76, 77, 78, 79, 80,
            81, 82, 83, 84, 85, 86, 87, 88, 89, 90,
            91, 92, 93, 94, 95, 96, 97, 98, 99,
            100, 101, 102, 103, 104, 105, 106, 107, 108, 109,
            110, 111, 112, 113, 114, 115, 116, 117, 118, 119,
            120, 121, 122, 123, 124, 125, 126,
            128, 129, 130, 131, 132, 133, 134, 135, 136, 137,
            138, 139, 140, 141, 142, 143, 144, 145, 146, 147,
            148, 149, 150, 151, 152, 153, 154, 155, 156, 157,
            158, 159, 160, 161, 162, 163, 164, 165, 166, 167,
            170, 171,
            172, // second octet filtered below
            173, 174, 175, 176, 177, 178, 179, 180, 181, 182,
            183, 184, 185, 186, 187, 188, 189, 190, 191,
            192, // second octet filtered below
            193, 194, 195, 196, 197, 198, 199, 200,
            201, 202, 203, 204, 205, 206, 207, 208, 209,
            210, 211, 212, 213, 214, 215, 216, 217, 218,
            219, 220, 221, 222, 223
        };
        int first = allowedFirstOctets[random.nextInt(allowedFirstOctets.length)];
        int second;
        if (first == 172) {
            // Exclude private 172.16/12 by keeping second octet outside [16,31].
            second = random.nextBoolean() ? random.nextInt(16) : 32 + random.nextInt(224);
        } else if (first == 192) {
            // Exclude private 192.168/16.
            second = random.nextInt(255);
            if (second >= 168) {
                second++;
            }
        } else {
            second = random.nextInt(256);
        }
        return first + "." + second + "." + random.nextInt(256) + "." + random.nextInt(256);
    }

    /**
     * Generates IPv4 CIDR notation such as {@code 203.0.113.0/24}.
     */
    public String generateCidr() {
        int prefix = 8 + random.nextInt(23); // [8, 30]
        return generatePublic() + "/" + prefix;
    }
}
