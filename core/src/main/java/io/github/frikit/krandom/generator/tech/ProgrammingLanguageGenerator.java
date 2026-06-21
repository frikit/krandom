/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.tech;

import io.github.frikit.krandom.generator.Generator;
import io.github.frikit.krandom.generator.GeneratorConfig;

import java.util.Objects;
import java.util.Random;

/**
 * Generates programming language names such as {@code "Kotlin"}.
 *
 * <pre>{@code
 *   String lang = new ProgrammingLanguageGenerator().generate(); // e.g. "Rust"
 * }</pre>
 */
public final class ProgrammingLanguageGenerator implements Generator<String> {

    private static final String[] LANGUAGES = {
        "Java", "Kotlin", "Scala", "Groovy", "Clojure", "Python", "JavaScript", "TypeScript",
        "Ruby", "Go", "Rust", "C", "C++", "C#", "Swift", "Objective-C", "PHP", "Perl",
        "Haskell", "Elixir", "Erlang", "Dart", "R", "Julia", "Lua", "Shell", "SQL", "F#"
    };

    private final Random random;

    /**
     * Creates a generator using the default configuration.
     */
    public ProgrammingLanguageGenerator() {
        this(GeneratorConfig.defaults());
    }

    /**
     * Creates a generator from explicit configuration (optional seed).
     *
     * @param config the generator configuration; must not be {@code null}
     */
    public ProgrammingLanguageGenerator(GeneratorConfig config) {
        Objects.requireNonNull(config, "config must not be null");
        this.random = config.createRandom();
    }

    /**
     * Generates a uniformly random programming language name.
     *
     * @return a language such as {@code "Kotlin"}; never {@code null}
     */
    @Override
    public String generate() {
        return LANGUAGES[random.nextInt(LANGUAGES.length)];
    }
}
