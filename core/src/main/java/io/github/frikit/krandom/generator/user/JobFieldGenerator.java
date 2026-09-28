/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.user;

import io.github.frikit.krandom.generator.Generator;
import io.github.frikit.krandom.generator.GeneratorConfig;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;

/**
 * Generates locale-aware job field (department) names, e.g. {@code "Engineering"}.
 *
 * <p>Names are resolved through the configuration's {@code DataRegistryContext}, which defaults to
 * {@link JobFieldDataRegistry}; locales without built-in data fall back to the bundled English
 * list. Output is reproducible for a fixed seed and locale.
 */
public final class JobFieldGenerator implements Generator<String> {

    private static final JobFieldDataProvider DEFAULT_PROVIDER =
        new BuiltInJobFieldDataProvider(Locale.ROOT, "default");

    private final List<String> fields;
    private final Random random;

    /**
     * Creates a job-field generator with default configuration.
     */
    public JobFieldGenerator() {
        this(GeneratorConfig.defaults());
    }

    /**
     * Creates a job-field generator for the given locale.
     *
     * @param locale locale whose job fields to use; must not be {@code null}
     */
    public JobFieldGenerator(Locale locale) {
        this(GeneratorConfig.builder().locale(Objects.requireNonNull(locale, "locale must not be null")).build());
    }

    /**
     * Creates a job-field generator with the specified configuration (locale + optional seed).
     *
     * @param config generator configuration; must not be {@code null}
     */
    public JobFieldGenerator(GeneratorConfig config) {
        Objects.requireNonNull(config, "config must not be null");
        JobFieldDataProvider provider = config.getRegistryContext().jobFieldProvider(config.getLocale());
        this.fields = (provider != null ? provider : DEFAULT_PROVIDER).getJobFields();
        this.random = config.createRandom();
    }

    @Override
    public String generate() {
        return fields.get(random.nextInt(fields.size()));
    }
}
