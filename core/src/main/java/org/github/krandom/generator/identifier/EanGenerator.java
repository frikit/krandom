/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.identifier;

import org.github.krandom.generator.Generator;
import org.github.krandom.generator.GeneratorConfig;

import java.security.SecureRandom;
import java.util.Objects;
import java.util.Random;

/**
 * Generates EAN-8 and EAN-13 barcodes.
 */
public final class EanGenerator implements Generator<String> {

    private final Random random;

    public EanGenerator() {
        this(GeneratorConfig.defaults());
    }

    public EanGenerator(GeneratorConfig config) {
        GeneratorConfig effective = Objects.requireNonNull(config, "config must not be null");
        this.random = effective.getSeed().isPresent()
                      ? new Random(effective.getSeed().getAsLong())
                      : new SecureRandom();
    }

    private static int checksum(String body) {
        int sum = 0;
        boolean multiplyByThree = true;
        for (int i = body.length() - 1; i >= 0; i--) {
            int digit = body.charAt(i) - '0';
            sum += multiplyByThree ? digit * 3 : digit;
            multiplyByThree = !multiplyByThree;
        }
        return (10 - (sum % 10)) % 10;
    }

    @Override
    public String generate() {
        return random.nextBoolean() ? generateEan8() : generateEan13();
    }

    public String generateEan8() {
        return generateWithLength(8, null);
    }

    public String generateEan13() {
        return generateWithLength(13, null);
    }

    public String generateLocalizedEan8(String prefix) {
        return generateWithLength(8, prefix);
    }

    public String generateLocalizedEan13(String prefix) {
        return generateWithLength(13, prefix);
    }

    private String generateWithLength(int length, String prefix) {
        if (length != 8 && length != 13) {
            throw new IllegalArgumentException("length must be 8 or 13, got: " + length);
        }
        String safePrefix = prefix == null ? "" : prefix.replaceAll("\\D", "");
        if (safePrefix.length() >= length) {
            throw new IllegalArgumentException("prefix too long for EAN-" + length);
        }
        int bodyLength = length - 1;
        StringBuilder body = new StringBuilder(bodyLength);
        body.append(safePrefix);
        while (body.length() < bodyLength) {
            body.append(random.nextInt(10));
        }
        int checksum = checksum(body.toString());
        return body.append(checksum).toString();
    }
}
