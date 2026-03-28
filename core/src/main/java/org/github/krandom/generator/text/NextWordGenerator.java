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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

/**
 * Generates coherent next words from a corpus word sequence.
 *
 * <p>Builds a first-order transition map (current word -> possible next words). This allows
 * sentence-like sequences that look more natural than independent random word selection.
 */
public final class NextWordGenerator implements Generator<String> {

    private final Random                    random;
    private final List<String>              starters;
    private final Map<String, List<String>> transitions;

    /**
     * Constructs with default config.
     *
     * @param corpusWords ordered corpus words used to build transitions
     */
    public NextWordGenerator(String[] corpusWords) {
        this(GeneratorConfig.defaults(), corpusWords);
    }

    /**
     * Constructs with explicit config.
     *
     * @param config      generator config
     * @param corpusWords ordered corpus words used to build transitions
     */
    public NextWordGenerator(GeneratorConfig config, String[] corpusWords) {
        Objects.requireNonNull(config, "config must not be null");
        Objects.requireNonNull(corpusWords, "corpusWords must not be null");
        if (corpusWords.length < 2) {
            throw new IllegalArgumentException("corpusWords must contain at least 2 words");
        }

        this.random = config.getSeed().isPresent()
                      ? new Random(config.getSeed().getAsLong())
                      : new SecureRandom();
        this.transitions = new HashMap<>();
        this.starters = new ArrayList<>();

        for (int i = 0; i < corpusWords.length; i++) {
            String word = normalize(corpusWords[i], "corpusWords[" + i + "]");
            starters.add(word);
            if (i < corpusWords.length - 1) {
                String next = normalize(corpusWords[i + 1], "corpusWords[" + (i + 1) + "]");
                transitions.computeIfAbsent(word, ignored -> new ArrayList<>()).add(next);
            }
        }
    }

    private static String normalize(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value.trim().toLowerCase();
    }

    /**
     * Utility for creating a generator from plain whitespace-separated text.
     */
    public static NextWordGenerator fromText(String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("text must not be blank");
        }
        String normalized = text.toLowerCase().replaceAll("[^a-z\\s]", " ");
        String[] words = Arrays.stream(normalized.trim().split("\\s+")).toArray(String[]::new);
        return new NextWordGenerator(words);
    }

    /**
     * Returns a random corpus starter word.
     */
    @Override
    public String generate() {
        return starters.get(random.nextInt(starters.size()));
    }

    /**
     * Returns a probabilistic next word for {@code currentWord}.
     *
     * <p>If the word is unknown (no transition entry), returns a random starter.
     */
    public String generateNext(String currentWord) {
        String current = normalize(currentWord, "currentWord");
        List<String> nextOptions = transitions.get(current);
        if (nextOptions == null) {
            return generate();
        }
        return nextOptions.get(random.nextInt(nextOptions.size()));
    }

    /**
     * Generates a sentence-like sequence of exactly {@code wordCount} words.
     *
     * @param wordCount number of words
     * @return capitalized sentence ending with a period
     */
    public String generateSentence(int wordCount) {
        if (wordCount <= 0) {
            throw new IllegalArgumentException("wordCount must be positive, got: " + wordCount);
        }
        List<String> words = generateWordSequence(wordCount);
        StringBuilder sb = new StringBuilder(String.join(" ", words));
        sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
        sb.append('.');
        return sb.toString();
    }

    /**
     * Generates exactly {@code wordCount} linked words.
     */
    public List<String> generateWordSequence(int wordCount) {
        if (wordCount <= 0) {
            throw new IllegalArgumentException("wordCount must be positive, got: " + wordCount);
        }
        List<String> out = new ArrayList<>(wordCount);
        String current = generate();
        out.add(current);
        for (int i = 1; i < wordCount; i++) {
            current = generateNext(current);
            out.add(current);
        }
        return out;
    }

    /**
     * Returns a defensive copy of corpus starter words.
     */
    public String[] getCorpusWords() {
        return starters.toArray(String[]::new);
    }

    /**
     * Returns transition map size (distinct current-word keys).
     */
    public int getTransitionKeyCount() {
        return transitions.size();
    }
}
