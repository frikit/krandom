/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.base;

import io.github.frikit.krandom.generator.Generator;
import io.github.frikit.krandom.generator.GeneratorConfig;

import java.security.SecureRandom;
import java.util.Objects;
import java.util.Random;

/**
 * Generates numbers from a format string using {@code '#'} placeholders.
 *
 * <p>Each {@code '#'} is replaced by a random digit 0..9; all other characters are copied.
 */
public final class NumberWithFormatGenerator implements Generator<String> {

    private final Random random;
    private final String defaultFormat;

    public NumberWithFormatGenerator() {
        this("###-###-####", GeneratorConfig.defaults());
    }

    public NumberWithFormatGenerator(String format) {
        this(format, GeneratorConfig.defaults());
    }

    public NumberWithFormatGenerator(GeneratorConfig config) {
        this("###-###-####", config);
    }

    public NumberWithFormatGenerator(String format, GeneratorConfig config) {
        this.defaultFormat = validateFormat(format);
        Objects.requireNonNull(config, "config must not be null");
        this.random = config.createRandom();
    }

    private static String validateFormat(String format) {
        Objects.requireNonNull(format, "format must not be null");
        if (format.isBlank()) {
            throw new IllegalArgumentException("format must not be blank");
        }
        if (format.indexOf('#') < 0) {
            throw new IllegalArgumentException("format must contain at least one '#' placeholder");
        }
        return format;
    }

    @Override
    public String generate() {
        return generate(defaultFormat);
    }

    /**
     * Generates a value using the supplied format.
     *
     * @param format pattern containing at least one {@code '#'}
     * @return formatted numeric string
     */
    public String generate(String format) {
        String normalized = validateFormat(format);
        StringBuilder out = new StringBuilder(normalized.length());
        for (int i = 0; i < normalized.length(); i++) {
            char c = normalized.charAt(i);
            if (c == '#') {
                out.append(random.nextInt(10));
            } else {
                out.append(c);
            }
        }
        return out.toString();
    }
}
