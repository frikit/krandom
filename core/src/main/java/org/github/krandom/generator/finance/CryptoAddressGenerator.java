/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.finance;

import org.github.krandom.generator.Generator;
import org.github.krandom.generator.GeneratorConfig;

import java.security.SecureRandom;
import java.util.Objects;
import java.util.Random;

/**
 * Generates sample crypto wallet addresses for common chains.
 */
public final class CryptoAddressGenerator implements Generator<String> {

    private static final char[] HEX_LOWER = "0123456789abcdef".toCharArray();
    private static final char[] BASE58 = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz".toCharArray();
    private static final String[] CHAINS = {"btc", "eth", "ltc"};

    private final Random random;

    public CryptoAddressGenerator() {
        this(GeneratorConfig.defaults());
    }

    public CryptoAddressGenerator(GeneratorConfig config) {
        Objects.requireNonNull(config, "config must not be null");
        this.random = config.getSeed().isPresent()
                ? new Random(config.getSeed().getAsLong())
                : new SecureRandom();
    }

    @Override
    public String generate() {
        String chain = CHAINS[random.nextInt(CHAINS.length)];
        return generate(chain);
    }

    public String generate(String chain) {
        Objects.requireNonNull(chain, "chain must not be null");
        return switch (chain.toLowerCase()) {
            case "btc", "bitcoin" -> generateBitcoin();
            case "eth", "ethereum" -> generateEthereum();
            case "ltc", "litecoin" -> generateLitecoin();
            default -> throw new IllegalArgumentException("unsupported chain: " + chain);
        };
    }

    public String generateBitcoin() {
        // Legacy/base58 shape.
        return "1" + randomChars(BASE58, 33);
    }

    public String generateEthereum() {
        return "0x" + randomChars(HEX_LOWER, 40);
    }

    public String generateLitecoin() {
        return "L" + randomChars(BASE58, 33);
    }

    private String randomChars(char[] alphabet, int length) {
        StringBuilder out = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            out.append(alphabet[random.nextInt(alphabet.length)]);
        }
        return out.toString();
    }
}
