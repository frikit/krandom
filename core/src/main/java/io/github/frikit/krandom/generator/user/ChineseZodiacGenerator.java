/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.user;

import io.github.frikit.krandom.generator.Generator;
import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.DataRegistryContext;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;

/**
 * Generates locale-aware Chinese zodiac animals, e.g. {@code "Dragon"} for English or {@code "龙"}
 * for Chinese.
 *
 * <p>Animal names are resolved from the configured
 * {@link io.github.frikit.krandom.generator.DataRegistryContext}; its default view delegates to
 * {@link ChineseZodiacDataRegistry}. Locales without a built-in file fall back to bundled English
 * names. The 12-year cycle is universal, so {@link #animalFor(int)} returns the animal in the
 * configured language.
 *
 * <p><b>Note:</b> the mapping is by Gregorian year, so dates between January 1 and the lunar New Year
 * are attributed to the Gregorian year's animal. This matches common fixture libraries and keeps the
 * result deterministic without a lunar calendar.
 *
 * <pre>{@code
 *   String en = new ChineseZodiacGenerator().animalFor(2024);                 // "Dragon"
 *   String zh = new ChineseZodiacGenerator(Locale.CHINA).animalFor(2024);     // "龙"
 * }</pre>
 */
public final class ChineseZodiacGenerator implements Generator<String> {

    private static final ChineseZodiacDataProvider DEFAULT_PROVIDER =
        new BuiltInChineseZodiacDataProvider(Locale.ROOT, "krandom/chinese_zodiac/default.txt");

    private final List<String> animals;
    private final Random random;

    /**
     * Creates a generator using the default configuration (and its locale).
     */
    public ChineseZodiacGenerator() {
        this(GeneratorConfig.defaults());
    }

    /**
     * Creates a generator for the given locale.
     *
     * @param locale the locale whose animal names to use; falls back to English if no built-in file
     *               exists; must not be {@code null}
     */
    public ChineseZodiacGenerator(Locale locale) {
        this(GeneratorConfig.builder().locale(Objects.requireNonNull(locale, "locale must not be null")).build());
    }

    /**
     * Creates a generator from explicit configuration (locale + optional seed).
     *
     * @param config the generator configuration; must not be {@code null}
     */
    public ChineseZodiacGenerator(GeneratorConfig config) {
        Objects.requireNonNull(config, "config must not be null");
        DataRegistryContext registryContext = config.getRegistryContext();
        ChineseZodiacDataProvider provider = registryContext.chineseZodiacProvider(config.getLocale());
        if (provider == null) {
            provider = DEFAULT_PROVIDER;
        }
        this.animals = provider.getAnimals();
        this.random = config.createRandom();
    }

    /**
     * Generates a uniformly random Chinese zodiac animal in the configured locale.
     *
     * @return a localized animal name; never {@code null}
     */
    @Override
    public String generate() {
        return animals.get(random.nextInt(animals.size()));
    }

    /**
     * Returns the Chinese zodiac animal for the given Gregorian year, in the configured locale.
     *
     * @param year the Gregorian year (negative years are supported)
     * @return the corresponding localized animal; never {@code null}
     */
    public String animalFor(int year) {
        return animals.get(Math.floorMod(year, 12));
    }

    /**
     * Returns the Chinese zodiac animal for the given date's Gregorian year, in the configured locale.
     *
     * @param date the date whose year's animal to resolve; must not be {@code null}
     * @return the corresponding localized animal; never {@code null}
     */
    public String animalFor(LocalDate date) {
        Objects.requireNonNull(date, "date must not be null");
        return animalFor(date.getYear());
    }
}
