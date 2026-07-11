/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.finance;

import io.github.frikit.krandom.generator.Generator;
import io.github.frikit.krandom.generator.GeneratorConfig;

import java.security.SecureRandom;
import java.util.Objects;
import java.util.Random;

/**
 * Generates sample crypto wallet addresses for common chains.
 */
public final class CryptoAddressGenerator implements Generator<String> {

    private static final char[]   HEX_LOWER = "0123456789abcdef".toCharArray();
    private static final char[]   BASE58    = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz".toCharArray();
    private static final String[] CHAINS    = { "btc", "eth", "ltc" };

    private final Random random;
    private final CryptoAddressSafetyPolicy safetyPolicy;


    /**
     * Creates a generator from explicit configuration.
     *
     * @param config the generator configuration; must not be {@code null}
     */
    public CryptoAddressGenerator(GeneratorConfig config) {
        Objects.requireNonNull(config, "config must not be null");
        this.random = config.createRandom();
        this.safetyPolicy = config.getCryptoAddressSafetyPolicy();
    }

    @Override
    public String generate() {
        safetyPolicy.requireRealisticOutput();
        String chain = CHAINS[random.nextInt(CHAINS.length)];
        return generateForChain(chain);
    }

    public String generate(String chain) {
        Objects.requireNonNull(chain, "chain must not be null");
        safetyPolicy.requireRealisticOutput();
        return generateForChain(chain);
    }

    public String generateBitcoin() {
        safetyPolicy.requireRealisticOutput();
        return generateBitcoinUnchecked();
    }

    public String generateEthereum() {
        safetyPolicy.requireRealisticOutput();
        return generateEthereumUnchecked();
    }

    public String generateLitecoin() {
        safetyPolicy.requireRealisticOutput();
        return generateLitecoinUnchecked();
    }

    private String generateForChain(String chain) {
        return switch (chain.toLowerCase()) {
            case "btc", "bitcoin" -> generateBitcoinUnchecked();
            case "eth", "ethereum" -> generateEthereumUnchecked();
            case "ltc", "litecoin" -> generateLitecoinUnchecked();
            default -> throw new IllegalArgumentException("unsupported chain: " + chain);
        };
    }

    private String generateBitcoinUnchecked() {
        // Legacy/base58 shape.
        return "1" + randomChars(BASE58, 33);
    }

    private String generateEthereumUnchecked() {
        return "0x" + randomChars(HEX_LOWER, 40);
    }

    private String generateLitecoinUnchecked() {
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
