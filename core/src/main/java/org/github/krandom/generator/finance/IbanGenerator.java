/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.finance;

import org.github.krandom.generator.Generator;
import org.github.krandom.generator.GeneratorConfig;

import java.security.SecureRandom;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;

/**
 * Generates IBAN-like values with valid mod-97 check digits.
 */
public final class IbanGenerator implements Generator<String> {

    private final Random               random;
    private final BankCountryGenerator bankCountryGenerator;
    private final BbanGenerator        bbanGenerator;

    public IbanGenerator() {
        this(GeneratorConfig.defaults());
    }

    public IbanGenerator(Locale locale) {
        this(GeneratorConfig.builder().locale(locale).build());
    }

    public IbanGenerator(GeneratorConfig config) {
        GeneratorConfig effective = Objects.requireNonNull(config, "config must not be null");
        this.random = effective.getSeed().isPresent()
                      ? new Random(effective.getSeed().getAsLong())
                      : new SecureRandom();
        this.bankCountryGenerator = new BankCountryGenerator(effective);
        this.bbanGenerator = new BbanGenerator(effective);
    }

    @Override
    public String generate() {
        String country = bankCountryGenerator.generate();
        String bban = bbanGenerator.generate();
        int checkDigits = computeCheckDigits(country, bban);
        return country + String.format("%02d", checkDigits) + bban;
    }

    private int computeCheckDigits(String country, String bban) {
        String rearranged = bban + country + "00";
        StringBuilder numeric = new StringBuilder(rearranged.length() * 2);
        for (int i = 0; i < rearranged.length(); i++) {
            char ch = rearranged.charAt(i);
            if (Character.isLetter(ch)) {
                numeric.append(ch - 'A' + 10);
            } else {
                numeric.append(ch);
            }
        }
        int mod = 0;
        for (int i = 0; i < numeric.length(); i++) {
            mod = (mod * 10 + (numeric.charAt(i) - '0')) % 97;
        }
        return 98 - mod;
    }
}
