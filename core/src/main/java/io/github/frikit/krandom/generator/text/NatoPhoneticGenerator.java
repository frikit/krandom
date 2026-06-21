/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.text;

import io.github.frikit.krandom.generator.Generator;
import io.github.frikit.krandom.generator.GeneratorConfig;

import java.util.Objects;
import java.util.Random;

/**
 * Generates NATO phonetic alphabet code words (ICAO spelling), such as {@code "Bravo"}.
 *
 * <p>{@link #generate()} returns a uniformly random code word. {@link #wordFor(char)} maps a single
 * Latin letter to its code word, and {@link #spell(CharSequence)} spells out an entire string
 * (non-letters are skipped).
 *
 * <pre>{@code
 *   String w = new NatoPhoneticGenerator().generate();        // e.g. "Tango"
 *   String a = new NatoPhoneticGenerator().wordFor('a');      // "Alfa"
 *   String s = new NatoPhoneticGenerator().spell("AB-12");    // "Alfa Bravo"
 * }</pre>
 */
public final class NatoPhoneticGenerator implements Generator<String> {

    /** ICAO code words, indexed by {@code letter - 'A'}. */
    private static final String[] WORDS = {
        "Alfa", "Bravo", "Charlie", "Delta", "Echo", "Foxtrot", "Golf", "Hotel", "India",
        "Juliett", "Kilo", "Lima", "Mike", "November", "Oscar", "Papa", "Quebec", "Romeo",
        "Sierra", "Tango", "Uniform", "Victor", "Whiskey", "X-ray", "Yankee", "Zulu"
    };

    private final Random random;

    /**
     * Creates a generator using the default configuration.
     */
    public NatoPhoneticGenerator() {
        this(GeneratorConfig.defaults());
    }

    /**
     * Creates a generator from explicit configuration (optional seed).
     *
     * @param config the generator configuration; must not be {@code null}
     */
    public NatoPhoneticGenerator(GeneratorConfig config) {
        Objects.requireNonNull(config, "config must not be null");
        this.random = config.createRandom();
    }

    /**
     * Generates a uniformly random NATO code word.
     *
     * @return a code word such as {@code "Bravo"}; never {@code null}
     */
    @Override
    public String generate() {
        return WORDS[random.nextInt(WORDS.length)];
    }

    /**
     * Returns the code word for a single Latin letter (case-insensitive).
     *
     * @param letter a letter in {@code A–Z} or {@code a–z}
     * @return the code word, e.g. {@code "Alfa"} for {@code 'a'}
     * @throws IllegalArgumentException if {@code letter} is not a Latin letter
     */
    public String wordFor(char letter) {
        char upper = Character.toUpperCase(letter);
        if (upper < 'A' || upper > 'Z') {
            throw new IllegalArgumentException("not a Latin letter: " + letter);
        }
        return WORDS[upper - 'A'];
    }

    /**
     * Spells out a string as space-separated code words. Characters that are not Latin letters
     * (digits, punctuation, whitespace) are skipped.
     *
     * @param text the text to spell; must not be {@code null}
     * @return the phonetic spelling, e.g. {@code "Alfa Bravo"} for {@code "AB-12"}; possibly empty
     */
    public String spell(CharSequence text) {
        Objects.requireNonNull(text, "text must not be null");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char upper = Character.toUpperCase(text.charAt(i));
            if (upper >= 'A' && upper <= 'Z') {
                if (sb.length() > 0) {
                    sb.append(' ');
                }
                sb.append(WORDS[upper - 'A']);
            }
        }
        return sb.toString();
    }
}
