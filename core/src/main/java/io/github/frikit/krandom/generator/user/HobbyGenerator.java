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
 * Generates locale-aware hobby names, e.g. {@code "Photography"} for English or {@code "Фотография"}
 * for Russian.
 *
 * <p>Names are resolved from the configured
 * {@link io.github.frikit.krandom.generator.DataRegistryContext}; its default view delegates to
 * {@link HobbyDataRegistry}. Locales without a built-in file fall back to bundled English hobbies.
 *
 * <pre>{@code
 *   String en = new HobbyGenerator().generate();                    // e.g. "Gardening"
 *   String ru = new HobbyGenerator(Locale.of("ru","RU")).generate(); // e.g. "Садоводство"
 * }</pre>
 */
public final class HobbyGenerator implements Generator<String> {

    private static final HobbyDataProvider DEFAULT_PROVIDER =
        new BuiltInHobbyDataProvider(Locale.ROOT, "krandom/hobbies/default.txt");

    private final List<String> hobbies;
    private final Random random;

    /**
     * Creates a generator using the default configuration (and its locale).
     */
    public HobbyGenerator() {
        this(GeneratorConfig.defaults());
    }

    /**
     * Creates a generator for the given locale.
     *
     * @param locale the locale whose hobby names to use; falls back to English if no built-in file
     *               exists; must not be {@code null}
     */
    public HobbyGenerator(Locale locale) {
        this(GeneratorConfig.builder().locale(Objects.requireNonNull(locale, "locale must not be null")).build());
    }

    /**
     * Creates a generator from explicit configuration (locale + optional seed).
     *
     * @param config the generator configuration; must not be {@code null}
     */
    public HobbyGenerator(GeneratorConfig config) {
        Objects.requireNonNull(config, "config must not be null");
        DataRegistryContext registryContext = config.getRegistryContext();
        HobbyDataProvider provider = registryContext.hobbyProvider(config.getLocale());
        if (provider == null) {
            provider = DEFAULT_PROVIDER;
        }
        this.hobbies = provider.getHobbies();
        this.random = config.createRandom();
    }

    /**
     * Generates a uniformly random hobby name in the configured locale.
     *
     * @return a localized hobby such as {@code "Photography"}; never {@code null}
     */
    @Override
    public String generate() {
        return hobbies.get(random.nextInt(hobbies.size()));
    }
}
