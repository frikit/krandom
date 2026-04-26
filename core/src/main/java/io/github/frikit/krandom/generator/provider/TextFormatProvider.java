/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.provider;

import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.base.RegexGenerator;
import io.github.frikit.krandom.generator.text.TemplateStringGenerator;

import java.util.Objects;
import java.util.Random;

/**
 * Provider-style string formatting helpers for template and regex-based generation.
 */
public final class TextFormatProvider {

    private static final int PRINTABLE_ASCII_MIN = 33;
    private static final int PRINTABLE_ASCII_MAX = 126;
    private static final int LOWER_ALPHA_SIZE    = 26;

    private final TemplateStringGenerator templateGenerator;
    private final Random                  random;

    /**
     * Creates formatting helpers with default configuration.
     */
    public TextFormatProvider() {
        this(GeneratorConfig.defaults());
    }

    /**
     * Creates formatting helpers with explicit generator configuration.
     */
    public TextFormatProvider(GeneratorConfig config) {
        GeneratorConfig generatorConfig = Objects.requireNonNull(config, "config must not be null");
        this.templateGenerator = new TemplateStringGenerator("", generatorConfig);
        this.random = generatorConfig.createRandom();
    }

    /**
     * Replaces {@code #} and {@code ?} placeholders using template semantics.
     */
    public String template(String input) {
        Objects.requireNonNull(input, "input must not be null");
        return templateGenerator.bothify(input);
    }

    /**
     * Replaces {@code #} placeholders with random digits.
     */
    public String numerify(String input) {
        Objects.requireNonNull(input, "input must not be null");
        return templateGenerator.numerify(input);
    }

    /**
     * Replaces {@code ?} placeholders with random lowercase letters.
     */
    public String lexify(String input) {
        Objects.requireNonNull(input, "input must not be null");
        return templateGenerator.letterify(input);
    }

    /**
     * Replaces {@code ?} placeholders with random letters, optionally uppercase.
     */
    public String lexify(String input, boolean uppercase) {
        Objects.requireNonNull(input, "input must not be null");
        return templateGenerator.letterify(input, uppercase);
    }

    /**
     * Replaces both {@code #} and {@code ?} placeholders.
     */
    public String bothify(String input) {
        Objects.requireNonNull(input, "input must not be null");
        return templateGenerator.bothify(input);
    }

    /**
     * Replaces {@code *} placeholders with printable ASCII characters.
     */
    public String asciify(String input) {
        return asciify(input, '*');
    }

    /**
     * Replaces the given placeholder with printable ASCII characters.
     */
    public String asciify(String input, char placeholder) {
        Objects.requireNonNull(input, "input must not be null");
        StringBuilder out = new StringBuilder(input.length());
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            out.append(c == placeholder ? randomPrintableAscii() : c);
        }
        return out.toString();
    }

    /**
     * Generates a string that matches the simplified regex pattern.
     */
    public String regexify(String pattern) {
        Objects.requireNonNull(pattern, "pattern must not be null");
        return new RegexGenerator(pattern, random.nextLong()).generate();
    }

    /**
     * Generates a string that follows the character-type pattern of an example value.
     *
     * <p>Digits stay digits, uppercase letters stay uppercase, lowercase letters stay
     * lowercase, and punctuation / separators are preserved literally.
     */
    public String examplify(String example) {
        Objects.requireNonNull(example, "example must not be null");
        StringBuilder out = new StringBuilder(example.length());
        for (int i = 0; i < example.length(); i++) {
            char c = example.charAt(i);
            if (Character.isDigit(c)) {
                out.append((char) ('0' + random.nextInt(10)));
            } else if (Character.isUpperCase(c)) {
                out.append(randomAlpha(true));
            } else if (Character.isLowerCase(c)) {
                out.append(randomAlpha(false));
            } else if (Character.isLetter(c)) {
                out.append(randomAlpha(false));
            } else {
                out.append(c);
            }
        }
        return out.toString();
    }

    private char randomPrintableAscii() {
        return (char) (PRINTABLE_ASCII_MIN + random.nextInt(PRINTABLE_ASCII_MAX - PRINTABLE_ASCII_MIN + 1));
    }

    private char randomAlpha(boolean uppercase) {
        int offset = random.nextInt(LOWER_ALPHA_SIZE);
        return (char) ((uppercase ? 'A' : 'a') + offset);
    }
}
