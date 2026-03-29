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
 * Generates basic bank account numbers (BBAN-like) for locale countries.
 */
public final class BbanGenerator implements Generator<String> {

    private final Locale locale;
    private final Random random;

    public BbanGenerator() {
        this(GeneratorConfig.defaults());
    }

    public BbanGenerator(Locale locale) {
        this(GeneratorConfig.builder().locale(locale).build());
    }

    public BbanGenerator(GeneratorConfig config) {
        GeneratorConfig effective = Objects.requireNonNull(config, "config must not be null");
        this.locale = effective.getLocale();
        this.random = effective.createRandom();
    }

    @Override
    public String generate() {
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
