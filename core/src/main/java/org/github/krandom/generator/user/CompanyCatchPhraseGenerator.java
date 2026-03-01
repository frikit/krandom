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
 * Generates company catch phrases similar to Faker's {@code catch_phrase()}.
 */
public final class CompanyCatchPhraseGenerator implements Generator<String> {

    private static final String[] ADJECTIVES = {
            "Adaptive", "Unified", "Trusted", "Intelligent", "Future-ready", "Effortless", "Secure"
    };
    private static final String[] NOUNS = {
            "Platform", "Network", "Experience", "Engine", "Ecosystem", "Suite", "Framework"
    };
    private static final String[] TAGLINES = {
            "for modern teams", "for digital growth", "for global scale", "for measurable impact"
    };

    private final Locale locale;
    private final Random random;

    public CompanyCatchPhraseGenerator() {
        this(GeneratorConfig.defaults());
    }

    public CompanyCatchPhraseGenerator(Locale locale) {
        this(GeneratorConfig.builder().locale(locale).build());
    }

    public CompanyCatchPhraseGenerator(GeneratorConfig config) {
        GeneratorConfig effective = Objects.requireNonNull(config, "config must not be null");
        this.locale = effective.getLocale();
        this.random = effective.getSeed().isPresent()
                ? new Random(effective.getSeed().getAsLong())
                : new SecureRandom();
    }

    @Override
    public String generate() {
        if ("es".equals(locale.getLanguage())) {
            return "Innovacion confiable para equipos";
        }
        return ADJECTIVES[random.nextInt(ADJECTIVES.length)] + " "
                + NOUNS[random.nextInt(NOUNS.length)] + " "
                + TAGLINES[random.nextInt(TAGLINES.length)];
    }
}
