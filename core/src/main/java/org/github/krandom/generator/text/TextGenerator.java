/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.text;

import org.github.krandom.generator.Generator;
import org.github.krandom.generator.GeneratorConfig;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

/**
 * Generates char-limited text blocks and text collections.
 */
public final class TextGenerator implements Generator<String> {

    private static final String[] DEFAULT_WORDS = {
            "alpha", "beta", "gamma", "delta", "vector", "signal", "stream", "token",
            "cloud", "matrix", "engine", "system", "future", "global", "local", "secure"
    };

    private final Random random;
    private final Locale locale;

    public TextGenerator() {
        this(GeneratorConfig.defaults());
    }

    public TextGenerator(Locale locale) {
        this(GeneratorConfig.builder().locale(locale).build());
    }

    public TextGenerator(GeneratorConfig config) {
        Objects.requireNonNull(config, "config must not be null");
        this.locale = config.getLocale();
        this.random = config.getSeed().isPresent()
                ? new Random(config.getSeed().getAsLong())
                : new SecureRandom();
    }

    @Override
    public String generate() {
        return generate(200);
    }

    public String generate(int maxChars) {
        return generate(new TextOptions(maxChars, null, false, true));
    }

    public List<String> generateTexts(int count, int maxChars) {
        if (count <= 0) {
            throw new IllegalArgumentException("count must be positive, got: " + count);
        }
        List<String> texts = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            texts.add(generate(maxChars));
        }
        return texts;
    }

    public String generate(TextOptions options) {
        Objects.requireNonNull(options, "options must not be null");
        List<String> vocabulary = options.extWordList() == null || options.extWordList().isEmpty()
                ? List.of(DEFAULT_WORDS)
                : options.extWordList();

        int targetChars = Math.max(1, options.maxChars());
        int wordCount = options.variableWordCount()
                ? 4 + random.nextInt(10)
                : 8;

        List<String> words = pickWords(vocabulary, wordCount, options.uniqueWords());
        StringBuilder out = new StringBuilder(targetChars + 8);
        for (String word : words) {
            if (!out.isEmpty()) {
                out.append(' ');
            }
            out.append(word);
            if (out.length() >= targetChars) {
                out.setLength(targetChars);
                break;
            }
        }
        if (out.isEmpty()) {
            out.append(locale.getLanguage().equals("de") ? "text" : "lorem");
        }
        if (!out.toString().endsWith(".")) {
            if (out.length() == targetChars) {
                out.setCharAt(out.length() - 1, '.');
            } else {
                out.append('.');
            }
        }
        return out.toString();
    }

    private List<String> pickWords(List<String> vocabulary, int count, boolean unique) {
        if (!unique) {
            List<String> words = new ArrayList<>(count);
            for (int i = 0; i < count; i++) {
                words.add(vocabulary.get(random.nextInt(vocabulary.size())));
            }
            return words;
        }
        Set<String> selected = new LinkedHashSet<>();
        while (selected.size() < count && selected.size() < vocabulary.size()) {
            selected.add(vocabulary.get(random.nextInt(vocabulary.size())));
        }
        return new ArrayList<>(selected);
    }

    public record TextOptions(
            int maxChars,
            List<String> extWordList,
            boolean uniqueWords,
            boolean variableWordCount
    ) {
        public TextOptions {
            if (maxChars <= 0) {
                throw new IllegalArgumentException("maxChars must be positive, got: " + maxChars);
            }
        }
    }
}
