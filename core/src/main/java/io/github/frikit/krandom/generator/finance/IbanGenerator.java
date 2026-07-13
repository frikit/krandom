/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.finance;

import io.github.frikit.krandom.generator.Generator;
import io.github.frikit.krandom.generator.GeneratorConfig;

import java.util.Locale;
import java.util.Objects;
import java.util.Random;

/**
 * Generates IBAN-like values with valid mod-97 check digits.
 *
 * <p>Use an explicit {@link GeneratorConfig} and select
 * {@link BankingSafetyPolicy#REALISTIC_UNCLASSIFIED} only for isolated compatibility fixtures.
 * The default configured policy is {@link BankingSafetyPolicy#DISABLED}.
 */
public final class IbanGenerator implements Generator<String> {

    private final Random               random;
    private final BankCountryGenerator bankCountryGenerator;
    private final BbanGenerator        bbanGenerator;
    private final BankingSafetyPolicy  bankingSafetyPolicy;



    public IbanGenerator(GeneratorConfig config) {
        GeneratorConfig effective = Objects.requireNonNull(config, "config must not be null");
        this.random = effective.createRandom();
        this.bankCountryGenerator = new BankCountryGenerator(effective);
        this.bbanGenerator = new BbanGenerator(effective);
        this.bankingSafetyPolicy = effective.getBankingSafetyPolicy();
    }

    @Override
    public String generate() {
        bankingSafetyPolicy.requireRealisticOutput();
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
