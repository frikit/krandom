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
 * Generates locale-aware employment type values, e.g. {@code "Full-time"} or {@code "Vollzeit"}.
 *
 * <p>Values are resolved through the configuration's {@code DataRegistryContext}, which defaults to
 * {@link JobTypeDataRegistry}; locales without built-in data fall back to the bundled English list.
 * Output is reproducible for a fixed seed and locale.
 */
public final class JobTypeGenerator implements Generator<String> {

    private static final JobTypeDataProvider DEFAULT_PROVIDER =
        new BuiltInJobTypeDataProvider(Locale.ROOT, "default");

    private final GeneratorConfig config;
    private final Random          random;
    private final List<String>    jobTypes;

    public JobTypeGenerator() {
        this(GeneratorConfig.defaults());
    }

    public JobTypeGenerator(Locale locale) {
        this(GeneratorConfig.builder().locale(Objects.requireNonNull(locale, "locale must not be null")).build());
    }

    public JobTypeGenerator(GeneratorConfig config) {
        this.config = Objects.requireNonNull(config, "config must not be null");
        this.random = config.createRandom();
        JobTypeDataProvider provider = config.getRegistryContext().jobTypeProvider(config.getLocale());
        this.jobTypes = (provider != null ? provider : DEFAULT_PROVIDER).getJobTypes();
    }

    @Override
    public String generate() {
        return jobTypes.get(random.nextInt(jobTypes.size()));
    }

    public Locale getLocale() {
        return config.getLocale();
    }
}
