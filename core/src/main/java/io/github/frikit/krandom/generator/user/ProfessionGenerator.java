/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.user;

import io.github.frikit.krandom.generator.Generator;
import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.DataRegistryContext;

import java.security.SecureRandom;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;

/**
 * Generates locale-aware profession/job-title values.
 *
 * <p>Supports both uniform and ranked generation:
 * <ul>
 *   <li>{@link #generate()} and {@link #generate(boolean)} with {@code false} pick uniformly.</li>
 *   <li>{@link #generateRanked()} and {@link #generate(boolean)} with {@code true} bias toward common jobs.</li>
 * </ul>
 *
 * <p>This is the Java-core equivalent of Chance.js {@code profession({ranked})}.
 */
public final class ProfessionGenerator implements Generator<String> {

    private final GeneratorConfig config;
    private final Random          random;
    private final ProfessionData  data;

    /**
     * Uses {@link GeneratorConfig#defaults()} — locale defaults to {@link Locale#US}.
     */
    public ProfessionGenerator() {
        this(GeneratorConfig.defaults());
    }

    /**
     * Constructs a generator for the given locale.
     */
    public ProfessionGenerator(Locale locale) {
        this(GeneratorConfig.builder().locale(locale).build());
    }

    /**
     * Constructs a generator with per-instance custom professions and uniform weight.
     *
     * <p>This does not mutate global registry state.
     */
    public ProfessionGenerator(Locale locale, String[] professions) {
        this(GeneratorConfig.builder().locale(locale).build(), professions, uniformWeights(professions));
    }

    /**
     * Constructs a generator with per-instance custom professions and ranked weights.
     *
     * <p>This does not mutate global registry state.
     */
    public ProfessionGenerator(Locale locale, String[] professions, int[] weights) {
        this(GeneratorConfig.builder().locale(locale).build(), professions, weights);
    }

    /**
     * Full constructor using a {@link GeneratorConfig} (locale + optional seed).
     */
    public ProfessionGenerator(GeneratorConfig config) {
        this.config = Objects.requireNonNull(config, "config must not be null");
        DataRegistryContext registryContext = config.getRegistryContext();
        this.random = config.createRandom();

        ProfessionDataProvider provider = registryContext.professionProvider(config.getLocale());
        if (provider == null) {
            throw new UnsupportedOperationException(
                "Profession data is not supported for locale "
                + config.getLocale()
                + " (" + localeKey(config.getLocale()) + ")");
        }
        this.data = ProfessionData.of(provider.getProfessions(), provider.getWeights());
    }

    /**
     * Full constructor using custom per-instance professions (locale + optional seed).
     */
    public ProfessionGenerator(GeneratorConfig config, String[] professions, int[] weights) {
        this.config = Objects.requireNonNull(config, "config must not be null");
        this.random = config.createRandom();
        this.data = ProfessionData.of(professions, weights);
    }

    private static int[] uniformWeights(String[] professions) {
        Objects.requireNonNull(professions, "professions");
        int[] weights = new int[professions.length];
        for (int i = 0; i < weights.length; i++) {
            weights[i] = 1;
        }
        return weights;
    }

    private static String localeKey(Locale locale) {
        return locale.getLanguage() + "_" + locale.getCountry();
    }

    /**
     * Generates a uniformly distributed profession title.
     */
    @Override
    public String generate() {
        return data.professions[random.nextInt(data.professions.length)];
    }

    /**
     * Generates a profession title, optionally ranked by popularity.
     *
     * @param ranked {@code true} to bias toward common professions
     * @return profession title
     */
    public String generate(boolean ranked) {
        return ranked ? generateRanked() : generate();
    }

    /**
     * Generates a ranked (weighted) profession title.
     */
    public String generateRanked() {
        int roll = random.nextInt(data.totalWeight) + 1; // [1, totalWeight]
        int idx = 0;
        while (roll > data.cumulativeWeights[idx]) {
            idx++;
        }
        return data.professions[idx];
    }

    /**
     * Returns the locale this generator was configured with.
     */
    public Locale getLocale() {
        return config.getLocale();
    }

    /**
     * Returns count of professions available to this generator instance.
     */
    public int getProfessionCount() {
        return data.professions.length;
    }

    /**
     * Returns {@code true} if locale has exact profession provider (not just language fallback).
     */
    public boolean isLocaleExplicitlySupported() {
        ProfessionDataProvider provider = config.getRegistryContext().professionProvider(config.getLocale());
        return provider != null && localeKey(provider.getLocale()).equals(localeKey(config.getLocale()));
    }


    private static final class ProfessionData {

        private final String[] professions;
        private final int[]    cumulativeWeights;
        private final int      totalWeight;

        private ProfessionData(String[] professions, int[] cumulativeWeights, int totalWeight) {
            this.professions = professions;
            this.cumulativeWeights = cumulativeWeights;
            this.totalWeight = totalWeight;
        }

        private static ProfessionData of(String[] professions, int[] weights) {
            Objects.requireNonNull(professions, "professions");
            Objects.requireNonNull(weights, "weights");
            if (professions.length == 0) {
                throw new IllegalArgumentException("professions must not be empty");
            }
            if (professions.length != weights.length) {
                throw new IllegalArgumentException("professions and weights length must match");
            }

            String[] copiedProfessions = professions.clone();
            int[] cumulative = new int[weights.length];
            int running = 0;
            for (int i = 0; i < weights.length; i++) {
                String profession = copiedProfessions[i];
                if (profession == null || profession.isBlank()) {
                    throw new IllegalArgumentException("profession at index " + i + " must not be blank");
                }
                if (weights[i] <= 0) {
                    throw new IllegalArgumentException("weight at index " + i + " must be > 0");
                }
                running += weights[i];
                cumulative[i] = running;
            }
            return new ProfessionData(copiedProfessions, cumulative, running);
        }
    }
}
