/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.user;

import org.github.krandom.generator.Generator;
import org.github.krandom.generator.GeneratorConfig;
import org.github.krandom.generator.text.SentenceGenerator;

import java.security.SecureRandom;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;

/**
 * Generates social-profile fixtures.
 */
public final class SocialProfileGenerator implements Generator<SocialProfile> {

    private static final String[] PLATFORMS = {
        "x", "instagram", "github", "linkedin", "tiktok", "youtube"
    };

    private final Random                random;
    private final SocialHandleGenerator handleGenerator;
    private final FullNameGenerator     fullNameGenerator;
    private final SentenceGenerator     sentenceGenerator;

    public SocialProfileGenerator() {
        this(GeneratorConfig.defaults());
    }

    public SocialProfileGenerator(Locale locale) {
        this(GeneratorConfig.builder().locale(Objects.requireNonNull(locale, "locale must not be null")).build());
    }

    public SocialProfileGenerator(GeneratorConfig config) {
        GeneratorConfig effective = Objects.requireNonNull(config, "config must not be null");
        this.random = effective.getSeed().isPresent()
                      ? new Random(effective.getSeed().getAsLong())
                      : new SecureRandom();
        this.handleGenerator = new SocialHandleGenerator(effective);
        this.fullNameGenerator = new FullNameGenerator(effective);
        this.sentenceGenerator = new SentenceGenerator(effective);
    }

    @Override
    public SocialProfile generate() {
        String platform = PLATFORMS[random.nextInt(PLATFORMS.length)];
        String handle = handleGenerator.generateForPlatform(platform);
        String slug = handle.startsWith("@") ? handle.substring(1) : handle;
        String profileUrl = "https://" + platform + ".com/" + slug;
        String bio = sentenceGenerator.generate().replaceAll("\\s+", " ").trim();
        return new SocialProfile(platform, handle, profileUrl, fullNameGenerator.generate(), bio);
    }
}
