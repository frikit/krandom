/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.user;

import io.github.frikit.krandom.generator.Generator;
import io.github.frikit.krandom.generator.GeneratorConfig;

import java.util.Objects;
import java.util.Random;

/**
 * Generates English personal-pronoun sets such as {@code "they/them"}.
 *
 * <p>{@link #generate()} returns a {@code subject/object} pair. {@link #subjective()} and
 * {@link #objective()} return just the subject or object form of a randomly chosen set.
 *
 * <pre>{@code
 *   String set  = new PronounGenerator().generate();   // e.g. "she/her"
 *   String subj = new PronounGenerator().subjective(); // e.g. "they"
 * }</pre>
 */
public final class PronounGenerator implements Generator<String> {

    /** Common English pronoun sets in {@code subject/object} form. */
    private static final String[] SETS = {
        "he/him", "she/her", "they/them", "ze/zir", "xe/xem", "ey/em"
    };

    private final Random random;

    /**
     * Creates a generator using the default configuration.
     */
    public PronounGenerator() {
        this(GeneratorConfig.defaults());
    }

    /**
     * Creates a generator from explicit configuration (optional seed).
     *
     * @param config the generator configuration; must not be {@code null}
     */
    public PronounGenerator(GeneratorConfig config) {
        Objects.requireNonNull(config, "config must not be null");
        this.random = config.createRandom();
    }

    /**
     * Generates a random pronoun set in {@code subject/object} form.
     *
     * @return a set such as {@code "they/them"}; never {@code null}
     */
    @Override
    public String generate() {
        return SETS[random.nextInt(SETS.length)];
    }

    /**
     * Returns the subject form of a randomly chosen pronoun set (e.g. {@code "they"}).
     *
     * @return the subject pronoun; never {@code null}
     */
    public String subjective() {
        String set = generate();
        return set.substring(0, set.indexOf('/'));
    }

    /**
     * Returns the object form of a randomly chosen pronoun set (e.g. {@code "them"}).
     *
     * @return the object pronoun; never {@code null}
     */
    public String objective() {
        String set = generate();
        return set.substring(set.indexOf('/') + 1);
    }
}
