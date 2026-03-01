/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.user;

import org.github.krandom.generator.Generator;
import org.github.krandom.generator.GeneratorConfig;

import java.security.SecureRandom;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;

/**
 * Generates company buzzword phrases similar to Faker's {@code bs()}.
 */
public final class CompanyBuzzwordGenerator implements Generator<String> {

    private static final String[] LEAD = {
            "streamline", "empower", "leverage", "optimize", "synergize", "scale", "deliver", "enable"
    };
    private static final String[] MIDDLE = {
            "cross-platform", "end-to-end", "best-in-class", "frictionless", "cloud-native", "data-driven"
    };
    private static final String[] TAIL = {
            "solutions", "workflows", "infrastructure", "platforms", "experiences", "capabilities"
    };

    private final Locale locale;
    private final Random random;

    public CompanyBuzzwordGenerator() {
        this(GeneratorConfig.defaults());
    }

    public CompanyBuzzwordGenerator(Locale locale) {
        this(GeneratorConfig.builder().locale(locale).build());
    }

    public CompanyBuzzwordGenerator(GeneratorConfig config) {
        GeneratorConfig effective = Objects.requireNonNull(config, "config must not be null");
        this.locale = effective.getLocale();
        this.random = effective.getSeed().isPresent()
                ? new Random(effective.getSeed().getAsLong())
                : new SecureRandom();
    }

    @Override
    public String generate() {
        if ("de".equals(locale.getLanguage())) {
            return "digitale " + TAIL[random.nextInt(TAIL.length)];
        }
        if ("fr".equals(locale.getLanguage())) {
            return "solutions " + MIDDLE[random.nextInt(MIDDLE.length)];
        }
        return LEAD[random.nextInt(LEAD.length)] + " "
                + MIDDLE[random.nextInt(MIDDLE.length)] + " "
                + TAIL[random.nextInt(TAIL.length)];
    }
}
