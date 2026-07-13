/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.user;

import io.github.frikit.krandom.generator.Generator;
import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.DataRegistryContext;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;

/**
 * Generates locale-aware personal-pronoun sets, e.g. {@code "they/them"} for English or
 * {@code "они/их"} for Russian.
 *
 * <p>Sets are resolved from the configured
 * {@link io.github.frikit.krandom.generator.DataRegistryContext}; its default view delegates to
 * {@link PronounDataRegistry}. Locales without a built-in file fall back to bundled English sets.
 * {@link #subjective()} and {@link #objective()} return just the subject or object form of a randomly
 * chosen set.
 *
 * <pre>{@code
 *   String en = new PronounGenerator().generate();                   // e.g. "she/her"
 *   String ru = new PronounGenerator(Locale.of("ru","RU")).generate(); // e.g. "они/их"
 * }</pre>
 */
public final class PronounGenerator implements Generator<String> {

    private static final PronounDataProvider DEFAULT_PROVIDER =
        new BuiltInPronounDataProvider(Locale.ROOT, "krandom/pronouns/default.txt");

    private final List<String> sets;
    private final Random random;

    /**
     * Creates a generator using the default configuration (and its locale).
     */
    public PronounGenerator() {
        this(GeneratorConfig.defaults());
    }

    /**
     * Creates a generator for the given locale.
     *
     * @param locale the locale whose pronoun sets to use; falls back to English if no built-in file
     *               exists; must not be {@code null}
     */
    public PronounGenerator(Locale locale) {
        this(GeneratorConfig.builder().locale(Objects.requireNonNull(locale, "locale must not be null")).build());
    }

    /**
     * Creates a generator from explicit configuration (locale + optional seed).
     *
     * @param config the generator configuration; must not be {@code null}
     */
    public PronounGenerator(GeneratorConfig config) {
        Objects.requireNonNull(config, "config must not be null");
        DataRegistryContext registryContext = config.getRegistryContext();
        PronounDataProvider provider = registryContext.pronounProvider(config.getLocale());
        if (provider == null) {
            provider = DEFAULT_PROVIDER;
        }
        this.sets = provider.getPronounSets();
        this.random = config.createRandom();
    }

    /**
     * Generates a random pronoun set in {@code subject/object} form.
     *
     * @return a localized set such as {@code "they/them"}; never {@code null}
     */
    @Override
    public String generate() {
        return sets.get(random.nextInt(sets.size()));
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
