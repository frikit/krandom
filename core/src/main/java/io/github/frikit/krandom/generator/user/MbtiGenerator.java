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
 * Generates Myers–Briggs Type Indicator (MBTI) personality types such as {@code "INTJ"}.
 *
 * <p>{@link #generate()} returns one of the 16 four-letter codes. {@link #withNickname()} returns the
 * code together with its common nickname, e.g. {@code "INTJ (Architect)"}.
 *
 * <pre>{@code
 *   String type = new MbtiGenerator().generate();     // e.g. "ENFP"
 *   String full = new MbtiGenerator().withNickname();  // e.g. "ENFP (Campaigner)"
 * }</pre>
 */
public final class MbtiGenerator implements Generator<String> {

    /** The 16 MBTI type codes. */
    private static final String[] TYPES = {
        "ISTJ", "ISFJ", "INFJ", "INTJ", "ISTP", "ISFP", "INFP", "INTP",
        "ESTP", "ESFP", "ENFP", "ENTP", "ESTJ", "ESFJ", "ENFJ", "ENTJ"
    };

    /** Common nicknames, parallel to {@link #TYPES}. */
    private static final String[] NICKNAMES = {
        "Logistician", "Defender", "Advocate", "Architect", "Virtuoso", "Adventurer",
        "Mediator", "Logician", "Entrepreneur", "Entertainer", "Campaigner", "Debater",
        "Executive", "Consul", "Protagonist", "Commander"
    };

    private final Random random;

    /**
     * Creates a generator using the default configuration.
     */
    public MbtiGenerator() {
        this(GeneratorConfig.defaults());
    }

    /**
     * Creates a generator from explicit configuration (optional seed).
     *
     * @param config the generator configuration; must not be {@code null}
     */
    public MbtiGenerator(GeneratorConfig config) {
        Objects.requireNonNull(config, "config must not be null");
        this.random = config.createRandom();
    }

    /**
     * Generates a uniformly random MBTI type code.
     *
     * @return a four-letter code such as {@code "INTJ"}; never {@code null}
     */
    @Override
    public String generate() {
        return TYPES[random.nextInt(TYPES.length)];
    }

    /**
     * Generates a random type code paired with its nickname (e.g. {@code "INTJ (Architect)"}).
     *
     * @return the code and nickname; never {@code null}
     */
    public String withNickname() {
        int i = random.nextInt(TYPES.length);
        return TYPES[i] + " (" + NICKNAMES[i] + ')';
    }
}
