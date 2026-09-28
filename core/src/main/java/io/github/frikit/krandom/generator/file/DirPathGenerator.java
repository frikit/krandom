/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.file;

import io.github.frikit.krandom.generator.Generator;
import io.github.frikit.krandom.generator.GeneratorConfig;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;

/**
 * Generates locale-aware directory paths.
 *
 * <p>Directory names are resolved through the configuration's {@code DataRegistryContext}, which
 * defaults to {@link DirectoryNameDataRegistry}; locales without built-in data fall back to the
 * bundled English names.
 */
public final class DirPathGenerator implements Generator<String> {

    private static final DirectoryNameDataProvider DEFAULT_PROVIDER =
        new BuiltInDirectoryNameDataProvider(Locale.ROOT, "default");

    private final Locale       locale;
    private final Random       random;
    private final List<String> directoryNames;

    public DirPathGenerator() {
        this(GeneratorConfig.defaults());
    }

    public DirPathGenerator(Locale locale) {
        this(GeneratorConfig.builder().locale(Objects.requireNonNull(locale, "locale must not be null")).build());
    }

    public DirPathGenerator(GeneratorConfig config) {
        Objects.requireNonNull(config, "config must not be null");
        this.locale = config.getLocale();
        this.random = config.createRandom();
        DirectoryNameDataProvider provider = config.getRegistryContext().directoryNameProvider(locale);
        this.directoryNames = (provider != null ? provider : DEFAULT_PROVIDER).getDirectoryNames();
    }

    @Override
    public String generate() {
        int depth = random.nextInt(2, 5); // [2,4]
        StringBuilder path = new StringBuilder("/");
        for (int i = 0; i < depth; i++) {
            if (i > 0) {
                path.append('/');
            }
            path.append(directoryNames.get(random.nextInt(directoryNames.size())));
        }
        return path.toString();
    }

    public Locale getLocale() {
        return locale;
    }
}
