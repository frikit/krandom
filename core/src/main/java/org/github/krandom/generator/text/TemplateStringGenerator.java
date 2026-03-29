/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.text;

import org.github.krandom.generator.Generator;
import org.github.krandom.generator.GeneratorConfig;

import java.security.SecureRandom;
import java.util.Objects;
import java.util.Random;

/**
 * Generates strings from DataFaker-style templates.
 *
 * <p>Supported placeholders:
 * <ul>
 *   <li>{@code #} -> digit ({@code 0-9})</li>
 *   <li>{@code ?} -> lowercase letter ({@code a-z})</li>
 * </ul>
 *
 * <p>Example:
 * <pre>{@code
 * TemplateStringGenerator gen = new TemplateStringGenerator("???-###");
 * String value = gen.generate();  // "abc-527"
 * }</pre>
 */
public final class TemplateStringGenerator implements Generator<String> {

    private final Random random;
    private final String template;

    /**
     * Creates a generator with the provided template and default config.
     *
     * @param template template with {@code #} and/or {@code ?} placeholders
     */
    public TemplateStringGenerator(String template) {
        this(template, GeneratorConfig.defaults());
    }

    /**
     * Creates a generator with the provided template and seed.
     *
     * @param template template with {@code #} and/or {@code ?} placeholders
     * @param seed     deterministic seed
     */
    public TemplateStringGenerator(String template, long seed) {
        this(template, GeneratorConfig.builder().seed(seed).build());
    }

    /**
     * Creates a generator with the provided template and config.
     *
     * @param template template with {@code #} and/or {@code ?} placeholders
     * @param config   generator configuration
     */
    public TemplateStringGenerator(String template, GeneratorConfig config) {
        this.template = Objects.requireNonNull(template, "template must not be null");
        Objects.requireNonNull(config, "config must not be null");
        this.random = config.createRandom();
    }

    /**
     * Applies both digit and letter substitutions to the configured template.
     */
    @Override
    public String generate() {
        return bothify(template);
    }

    /**
     * Replaces all {@code #} characters with random digits.
     *
     * @param input template text
     * @return transformed string
     */
    public String numerify(String input) {
        Objects.requireNonNull(input, "input must not be null");
        StringBuilder out = new StringBuilder(input.length());
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            out.append(c == '#' ? randomDigit() : c);
        }
        return out.toString();
    }

    /**
     * Replaces all {@code ?} characters with random lowercase letters.
     *
     * @param input template text
     * @return transformed string
     */
    public String letterify(String input) {
        return letterify(input, false);
    }

    /**
     * Replaces all {@code ?} characters with random letters.
     *
     * @param input     template text
     * @param uppercase whether generated letters should be uppercase
     * @return transformed string
     */
    public String letterify(String input, boolean uppercase) {
        Objects.requireNonNull(input, "input must not be null");
        StringBuilder out = new StringBuilder(input.length());
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            out.append(c == '?' ? randomLetter(uppercase) : c);
        }
        return out.toString();
    }

    /**
     * Replaces both {@code #} and {@code ?} placeholders.
     *
     * @param input template text
     * @return transformed string
     */
    public String bothify(String input) {
        return numerify(letterify(input));
    }

    private char randomDigit() {
        return (char) ('0' + random.nextInt(10));
    }

    private char randomLetter(boolean uppercase) {
        char base = uppercase ? 'A' : 'a';
        return (char) (base + random.nextInt(26));
    }
}
