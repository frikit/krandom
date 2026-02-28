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
 * Generates valid ISIN codes (ISO 6166) with Luhn checksum.
 */
public final class IsinGenerator implements Generator<String> {

    private static final String ALNUM = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    private final GeneratorConfig config;
    private final Random random;

    public IsinGenerator() {
        this(GeneratorConfig.defaults());
    }

    public IsinGenerator(Locale locale) {
        this(GeneratorConfig.builder().locale(Objects.requireNonNull(locale, "locale must not be null")).build());
    }

    public IsinGenerator(GeneratorConfig config) {
        this.config = Objects.requireNonNull(config, "config must not be null");
        this.random = config.getSeed().isPresent()
                ? new Random(config.getSeed().getAsLong())
                : new SecureRandom();
    }

    @Override
    public String generate() {
        return generate(config.getLocale());
    }

    public String generate(Locale locale) {
        Objects.requireNonNull(locale, "locale must not be null");
        String country = normalizeCountry(locale);
        StringBuilder base = new StringBuilder(11);
        base.append(country);
        for (int i = 0; i < 9; i++) {
            base.append(ALNUM.charAt(random.nextInt(ALNUM.length())));
        }
        int check = computeCheckDigit(base.toString());
        return base.append(check).toString();
    }

    static int computeCheckDigit(String isinWithoutCheck) {
        StringBuilder digits = new StringBuilder(isinWithoutCheck.length() * 2);
        for (int i = 0; i < isinWithoutCheck.length(); i++) {
            char c = Character.toUpperCase(isinWithoutCheck.charAt(i));
            if (Character.isDigit(c)) {
                digits.append(c);
            } else if (c >= 'A' && c <= 'Z') {
                digits.append(c - 'A' + 10);
            } else {
                throw new IllegalArgumentException("invalid ISIN character: " + c);
            }
        }

        int sum = 0;
        boolean doubleDigit = true;
        for (int i = digits.length() - 1; i >= 0; i--) {
            int n = digits.charAt(i) - '0';
            if (doubleDigit) {
                n *= 2;
                if (n > 9) {
                    n -= 9;
                }
            }
            sum += n;
            doubleDigit = !doubleDigit;
        }
        return (10 - (sum % 10)) % 10;
    }

    private static String normalizeCountry(Locale locale) {
        String country = locale.getCountry();
        if (country == null || country.length() != 2) {
            return "US";
        }
        return country.toUpperCase(Locale.ROOT);
    }
}
