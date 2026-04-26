/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.user;

import io.github.frikit.krandom.generator.Generator;
import io.github.frikit.krandom.generator.GeneratorConfig;

import java.util.Locale;
import java.util.Objects;

/**
 * Generates social-media style handles.
 */
public final class SocialHandleGenerator implements Generator<String> {

    private final UsernameGenerator usernameGenerator;

    public SocialHandleGenerator() {
        this(GeneratorConfig.defaults());
    }

    public SocialHandleGenerator(Locale locale) {
        this(GeneratorConfig.builder().locale(Objects.requireNonNull(locale, "locale must not be null")).build());
    }

    public SocialHandleGenerator(GeneratorConfig config) {
        this.usernameGenerator = new UsernameGenerator(Objects.requireNonNull(config, "config must not be null"));
    }

    @Override
    public String generate() {
        return "@" + usernameGenerator.generate().replace(".", "_");
    }

    public String generateForPlatform(String platform) {
        Objects.requireNonNull(platform, "platform must not be null");
        String normalized = platform.trim().toLowerCase(Locale.ROOT);
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("platform must not be blank");
        }
        String base = generate();
        return switch (normalized) {
            case "github" -> base.replace('.', '-').replace('_', '-');
            case "linkedin" -> base.replace('@', ' ').trim().replace("_", "-");
            default -> base;
        };
    }
}
