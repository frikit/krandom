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
import java.time.MonthDay;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;

/**
 * Generates locale-aware Western (tropical) zodiac signs, e.g. {@code "Scorpio"} for English or
 * {@code "Скорпион"} for Russian.
 *
 * <p>The sign names are resolved from the configured
 * {@link io.github.frikit.krandom.generator.DataRegistryContext}; its default view delegates to
 * {@link ZodiacDataRegistry}. Locales without a built-in file fall back to bundled English names.
 * The date boundaries themselves are universal, so {@link #signFor(LocalDate)} returns the correct
 * sign in the configured language.
 *
 * <pre>{@code
 *   String en = new ZodiacGenerator().signFor(LocalDate.of(1990, 11, 5));            // "Scorpio"
 *   String ru = new ZodiacGenerator(Locale.of("ru", "RU")).signFor(LocalDate.of(1990, 11, 5)); // "Скорпион"
 * }</pre>
 */
public final class ZodiacGenerator implements Generator<String> {

    private static final ZodiacDataProvider DEFAULT_PROVIDER =
        new BuiltInZodiacDataProvider(Locale.ROOT, "krandom/zodiac/default.txt");

    /** First day (inclusive) on which each month's later sign begins, indexed by month - 1. */
    private static final int[] CUTOFF = {20, 19, 21, 20, 21, 21, 23, 23, 23, 23, 22, 22};

    /**
     * Canonical sign index (0 = Aries … 11 = Pisces) of the sign that begins on
     * {@code CUTOFF[month - 1]} within each month, indexed by month - 1.
     */
    private static final int[] SIGN_INDEX_FROM_CUTOFF = {10, 11, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9};

    private final List<String> signs;
    private final Random random;

    /**
     * Creates a generator using the default configuration (and its locale).
     */
    public ZodiacGenerator() {
        this(GeneratorConfig.defaults());
    }

    /**
     * Creates a generator for the given locale.
     *
     * @param locale the locale whose sign names to use; falls back to English if no built-in file
     *               exists; must not be {@code null}
     */
    public ZodiacGenerator(Locale locale) {
        this(GeneratorConfig.builder().locale(Objects.requireNonNull(locale, "locale must not be null")).build());
    }

    /**
     * Creates a generator from explicit configuration (locale + optional seed).
     *
     * @param config the generator configuration; must not be {@code null}
     */
    public ZodiacGenerator(GeneratorConfig config) {
        Objects.requireNonNull(config, "config must not be null");
        DataRegistryContext registryContext = config.getRegistryContext();
        ZodiacDataProvider provider = registryContext.zodiacProvider(config.getLocale());
        if (provider == null) {
            provider = DEFAULT_PROVIDER;
        }
        this.signs = provider.getSigns();
        this.random = config.createRandom();
    }

    /**
     * Generates a uniformly random zodiac sign in the configured locale.
     *
     * @return a localized sign name; never {@code null}
     */
    @Override
    public String generate() {
        return signs.get(random.nextInt(signs.size()));
    }

    /**
     * Returns the zodiac sign for the given date, in the configured locale.
     *
     * @param date the date whose sign to resolve; must not be {@code null}
     * @return the corresponding localized sign; never {@code null}
     */
    public String signFor(LocalDate date) {
        Objects.requireNonNull(date, "date must not be null");
        return signFor(MonthDay.from(date));
    }

    /**
     * Returns the zodiac sign for the given month and day, in the configured locale.
     *
     * @param monthDay the month-day whose sign to resolve; must not be {@code null}
     * @return the corresponding localized sign; never {@code null}
     */
    public String signFor(MonthDay monthDay) {
        Objects.requireNonNull(monthDay, "monthDay must not be null");
        int idx = monthDay.getMonthValue() - 1;
        int signIndex = monthDay.getDayOfMonth() >= CUTOFF[idx]
            ? SIGN_INDEX_FROM_CUTOFF[idx]
            : SIGN_INDEX_FROM_CUTOFF[(idx + 11) % 12];
        return signs.get(signIndex);
    }
}
