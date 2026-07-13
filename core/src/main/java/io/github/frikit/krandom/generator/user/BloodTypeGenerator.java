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
 * Generates locale-aware blood types ({@code "O+"}, {@code "A-"}, …) weighted by real-world ABO/Rh
 * frequencies.
 *
 * <p>The distribution is resolved from the configured
 * {@link io.github.frikit.krandom.generator.DataRegistryContext}; its default view delegates to
 * {@link BloodTypeDataRegistry}. Locales without a built-in file fall back to a bundled global
 * distribution. Output is reproducible for a fixed seed and locale.
 *
 * <pre>{@code
 *   String us = new BloodTypeGenerator(Locale.US).generate();        // weighted by US frequencies
 *   String jp = Generators.ofBloodType(Locale.JAPAN).generate();     // weighted by Japanese frequencies
 * }</pre>
 */
public final class BloodTypeGenerator implements Generator<String> {

    private static final BloodTypeDataProvider DEFAULT_PROVIDER =
        new BuiltInBloodTypeDataProvider(Locale.ROOT, "krandom/bloodtypes/default.txt");

    private final List<String> types;
    private final int[] cumulativeWeights;
    private final int totalWeight;
    private final Random random;

    /**
     * Creates a generator using the default configuration (and its locale).
     */
    public BloodTypeGenerator() {
        this(GeneratorConfig.defaults());
    }

    /**
     * Creates a generator for the given locale.
     *
     * @param locale the locale whose distribution to use; falls back to the global distribution if
     *               no built-in file exists
     */
    public BloodTypeGenerator(Locale locale) {
        this(GeneratorConfig.builder().locale(locale).build());
    }

    /**
     * Creates a generator from explicit configuration (locale + optional seed).
     *
     * @param config the generator configuration; must not be {@code null}
     */
    public BloodTypeGenerator(GeneratorConfig config) {
        Objects.requireNonNull(config, "config must not be null");
        DataRegistryContext registryContext = config.getRegistryContext();
        BloodTypeDataProvider provider = registryContext.bloodTypeProvider(config.getLocale());
        if (provider == null) {
            provider = DEFAULT_PROVIDER;
        }
        this.types = provider.getTypes();
        List<Integer> weights = provider.getWeights();
        this.cumulativeWeights = new int[weights.size()];
        int sum = 0;
        for (int i = 0; i < weights.size(); i++) {
            sum += weights.get(i);
            cumulativeWeights[i] = sum;
        }
        this.totalWeight = sum;
        this.random = config.createRandom();
    }

    /**
     * Generates a single blood type, weighted by the resolved locale's distribution.
     *
     * @return a blood-type label such as {@code "O+"}; never {@code null}
     */
    @Override
    public String generate() {
        int r = random.nextInt(totalWeight);
        int index = 0;
        while (r >= cumulativeWeights[index]) {
            index++;
        }
        return types.get(index);
    }
}
