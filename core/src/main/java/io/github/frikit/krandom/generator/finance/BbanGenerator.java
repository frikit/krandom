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
 * Generates basic bank account numbers (BBAN-like) for locale countries.
 *
 * <p>Use an explicit {@link GeneratorConfig} and select
 * {@link BankingSafetyPolicy#REALISTIC_UNCLASSIFIED} only for isolated compatibility fixtures.
 * The default configured policy is {@link BankingSafetyPolicy#DISABLED}.
 */
public final class BbanGenerator implements Generator<String> {

    private final Locale                locale;
    private final Random                random;
    private final BankingSafetyPolicy bankingSafetyPolicy;



    public BbanGenerator(GeneratorConfig config) {
        GeneratorConfig effective = Objects.requireNonNull(config, "config must not be null");
        this.locale = effective.getLocale();
        this.random = effective.createRandom();
        this.bankingSafetyPolicy = effective.getBankingSafetyPolicy();
    }

    @Override
    public String generate() {
        bankingSafetyPolicy.requireRealisticOutput();
        int length = switch (locale.getCountry()) {
            case "DE", "FR", "ES", "IT" -> 18;
            case "GB" -> 18;
            case "BR" -> 20;
            default -> 16;
        };
        StringBuilder out = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            out.append(random.nextInt(10));
        }
        return out.toString();
    }
}
