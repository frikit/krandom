/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.file;

import org.github.krandom.generator.Generator;
import org.github.krandom.generator.GeneratorConfig;

import java.util.Locale;
import java.util.Objects;

/**
 * Generates locale-aware file paths by composing directory, file name and extension generators.
 */
public final class FilePathGenerator implements Generator<String> {

    private final DirPathGenerator dirPathGenerator;
    private final FileNameGenerator fileNameGenerator;
    private final FileExtensionGenerator fileExtensionGenerator;
    private final Locale locale;

    public FilePathGenerator() {
        this(GeneratorConfig.defaults());
    }

    public FilePathGenerator(Locale locale) {
        this(GeneratorConfig.builder().locale(Objects.requireNonNull(locale, "locale must not be null")).build());
    }

    public FilePathGenerator(GeneratorConfig config) {
        Objects.requireNonNull(config, "config must not be null");
        this.locale = config.getLocale();
        this.dirPathGenerator = new DirPathGenerator(config);
        this.fileNameGenerator = new FileNameGenerator(config);
        this.fileExtensionGenerator = new FileExtensionGenerator(config);
    }

    @Override
    public String generate() {
        return generateWithExtension(fileExtensionGenerator.generate());
    }

    public String generateWithExtension(String extension) {
        if (extension == null || extension.isBlank()) {
            throw new IllegalArgumentException("extension must not be blank");
        }
        String normalized = extension.startsWith(".") ? extension.substring(1) : extension;
        return dirPathGenerator.generate() + "/" + fileNameGenerator.generate() + "." + normalized;
    }

    public Locale getLocale() {
        return locale;
    }
}
