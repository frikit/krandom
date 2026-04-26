/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.identifier;

import io.github.frikit.krandom.generator.Generator;
import io.github.frikit.krandom.generator.GeneratorConfig;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;

/**
 * Generates UUIDs (Universally Unique Identifiers) in various versions.
 *
 * <p>This generator supports UUID version 4 (random) and version 5 (name-based SHA-1).
 * All generated UUIDs conform to RFC 4122 standards.
 *
 * <p><strong>Basic Usage (v4 - Random):</strong>
 * <pre>{@code
 * UUIDGenerator gen = new UUIDGenerator();
 * UUID uuid = gen.generate();  // Random UUID v4
 * String uuidStr = gen.generateString();  // "f47ac10b-58cc-4372-a567-0e02b2c3d479"
 * }</pre>
 *
 * <p><strong>Version 5 (Name-based SHA-1):</strong>
 * <pre>{@code
 * UUIDGenerator gen = new UUIDGenerator();
 * UUID uuid = gen.generateV5("example.com");
 * String uuidStr = gen.generateV5String("my-namespace", "my-name");
 * }</pre>
 *
 * <p><strong>Seeded Generation (v4 only):</strong>
 * <pre>{@code
 * GeneratorConfig config = GeneratorConfig.builder().seed(12345L).build();
 * UUIDGenerator gen = new UUIDGenerator(config);
 * UUID uuid = gen.generate();  // Reproducible UUIDv4
 * }</pre>
 *
 * <p><strong>Version Control:</strong>
 * <pre>{@code
 * UUIDGenerator gen = new UUIDGenerator();
 * UUID v4 = gen.generateV4();  // Random UUID
 * UUID v5 = gen.generateV5("example.com");  // Name-based UUID
 * }</pre>
 *
 * <p><strong>Thread Safety:</strong>
 * This generator is thread-safe and can be shared across threads.
 *
 * @see UUID
 */
public final class UUIDGenerator implements Generator<UUID> {

    // Standard UUID v5 namespace for DNS (as per RFC 4122)
    private static final UUID NAMESPACE_DNS = UUID.fromString("6ba7b810-9dad-11d1-80b4-00c04fd430c8");
    // Standard UUID v5 namespace for URLs (as per RFC 4122)
    private static final UUID NAMESPACE_URL = UUID.fromString("6ba7b811-9dad-11d1-80b4-00c04fd430c8");
    private final GeneratorConfig config;
    private final Random          random;

    /**
     * Creates a UUID generator with default configuration.
     * <p>Default version is UUIDv4 (random).
     */
    public UUIDGenerator() {
        this(GeneratorConfig.defaults());
    }

    /**
     * Creates a UUID generator with the specified configuration.
     *
     * @param config the generator configuration; must not be {@code null}
     * @throws NullPointerException if {@code config} is {@code null}
     */
    public UUIDGenerator(GeneratorConfig config) {
        this.config = Objects.requireNonNull(config, "config must not be null");
        this.random = config.createRandom();
    }

    /**
     * Gets the standard DNS namespace UUID.
     *
     * @return the DNS namespace UUID
     */
    public static UUID getDnsNamespace() {
        return NAMESPACE_DNS;
    }

    /**
     * Gets the standard URL namespace UUID.
     *
     * @return the URL namespace UUID
     */
    public static UUID getUrlNamespace() {
        return NAMESPACE_URL;
    }

    static MessageDigest messageDigest(String algorithm) {
        try {
            return MessageDigest.getInstance(algorithm);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(algorithm + " algorithm not available", e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>Generates a random UUID version 4.
     *
     * @return a random UUIDv4; never {@code null}
     */
    @Override
    public UUID generate() {
        return generateV4();
    }

    /**
     * Generates a UUID as a string.
     * <p>Default version is UUIDv4 (random).
     *
     * @return a UUID string in standard format; never {@code null}
     */
    public String generateString() {
        return generate().toString();
    }

    /**
     * Generates a random UUID version 4.
     * <p>UUIDv4 uses random or pseudo-random numbers as per RFC 4122 §4.4.
     *
     * @return a random UUIDv4; never {@code null}
     */
    public UUID generateV4() {
        byte[] randomBytes = new byte[16];
        random.nextBytes(randomBytes);

        // Set version to 4 (bits 12-15 of time_hi_and_version field)
        randomBytes[6] &= 0x0f;  // Clear version bits
        randomBytes[6] |= 0x40;  // Set to version 4

        // Set variant to RFC 4122 (bits 6-7 of clock_seq_hi_and_reserved field)
        randomBytes[8] &= 0x3f;  // Clear variant bits
        randomBytes[8] |= 0x80;  // Set to variant 1 (10x)

        return bytesToUuid(randomBytes);
    }

    /**
     * Generates a UUID version 4 as a string.
     *
     * @return a UUIDv4 string; never {@code null}
     */
    public String generateV4String() {
        return generateV4().toString();
    }

    /**
     * Generates a UUID version 7 (time-ordered).
     */
    public UUID generateV7() {
        byte[] bytes = new byte[16];
        long unixMillis = System.currentTimeMillis();

        // 48-bit unix_ts_ms
        bytes[0] = (byte) (unixMillis >>> 40);
        bytes[1] = (byte) (unixMillis >>> 32);
        bytes[2] = (byte) (unixMillis >>> 24);
        bytes[3] = (byte) (unixMillis >>> 16);
        bytes[4] = (byte) (unixMillis >>> 8);
        bytes[5] = (byte) unixMillis;

        // Fill remaining 10 bytes with randomness
        byte[] rnd = new byte[10];
        random.nextBytes(rnd);
        System.arraycopy(rnd, 0, bytes, 6, 10);

        // Set version 7
        bytes[6] &= 0x0f;
        bytes[6] |= 0x70;

        // Set RFC4122 variant
        bytes[8] &= 0x3f;
        bytes[8] |= (byte) 0x80;

        return bytesToUuid(bytes);
    }

    /**
     * Generates a UUID version 7 as a string.
     */
    public String generateV7String() {
        return generateV7().toString();
    }

    /**
     * Generates a UUID version 5 using the DNS namespace and the given name.
     * <p>UUIDv5 uses SHA-1 hashing as per RFC 4122 §4.3.
     *
     * @param name the name to hash; must not be {@code null}
     * @return a UUIDv5; never {@code null}
     * @throws NullPointerException if {@code name} is {@code null}
     */
    public UUID generateV5(String name) {
        return generateV5(NAMESPACE_DNS, name);
    }

    /**
     * Generates a UUID version 5 as a string using the DNS namespace.
     *
     * @param name the name to hash; must not be {@code null}
     * @return a UUIDv5 string; never {@code null}
     * @throws NullPointerException if {@code name} is {@code null}
     */
    public String generateV5String(String name) {
        return generateV5(name).toString();
    }

    /**
     * Generates a UUID version 5 using the specified namespace and name.
     * <p>UUIDv5 uses SHA-1 hashing as per RFC 4122 §4.3.
     *
     * @param namespace the namespace UUID; must not be {@code null}
     * @param name      the name to hash; must not be {@code null}
     * @return a UUIDv5; never {@code null}
     * @throws NullPointerException if {@code namespace} or {@code name} is {@code null}
     */
    public UUID generateV5(UUID namespace, String name) {
        Objects.requireNonNull(namespace, "namespace must not be null");
        Objects.requireNonNull(name, "name must not be null");

        MessageDigest md = messageDigest("SHA-1");

        // Hash namespace + name
        md.update(uuidToBytes(namespace));
        md.update(name.getBytes(StandardCharsets.UTF_8));
        byte[] hash = md.digest();

        // Use first 16 bytes of hash
        byte[] uuidBytes = new byte[16];
        System.arraycopy(hash, 0, uuidBytes, 0, 16);

        // Set version to 5 (bits 12-15 of time_hi_and_version field)
        uuidBytes[6] &= 0x0f;  // Clear version bits
        uuidBytes[6] |= 0x50;  // Set to version 5

        // Set variant to RFC 4122 (bits 6-7 of clock_seq_hi_and_reserved field)
        uuidBytes[8] &= 0x3f;  // Clear variant bits
        uuidBytes[8] |= 0x80;  // Set to variant 1 (10x)

        return bytesToUuid(uuidBytes);
    }

    /**
     * Generates a UUID version 5 as a string using the specified namespace.
     *
     * @param namespace the namespace UUID; must not be {@code null}
     * @param name      the name to hash; must not be {@code null}
     * @return a UUIDv5 string; never {@code null}
     * @throws NullPointerException if {@code namespace} or {@code name} is {@code null}
     */
    public String generateV5String(UUID namespace, String name) {
        return generateV5(namespace, name).toString();
    }

    /**
     * Converts a UUID to a byte array.
     *
     * @param uuid the UUID to convert
     * @return a 16-byte array
     */
    private byte[] uuidToBytes(UUID uuid) {
        byte[] bytes = new byte[16];
        long msb = uuid.getMostSignificantBits();
        long lsb = uuid.getLeastSignificantBits();

        for (int i = 0; i < 8; i++) {
            bytes[i] = (byte) (msb >>> (8 * (7 - i)));
            bytes[8 + i] = (byte) (lsb >>> (8 * (7 - i)));
        }

        return bytes;
    }

    /**
     * Converts a byte array to a UUID.
     *
     * @param bytes the 16-byte array
     * @return the UUID
     */
    private UUID bytesToUuid(byte[] bytes) {
        long msb = 0;
        long lsb = 0;

        for (int i = 0; i < 8; i++) {
            msb = (msb << 8) | (bytes[i] & 0xff);
            lsb = (lsb << 8) | (bytes[8 + i] & 0xff);
        }

        return new UUID(msb, lsb);
    }
}
