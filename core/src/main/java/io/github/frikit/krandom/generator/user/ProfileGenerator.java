/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.user;

import io.github.frikit.krandom.generator.Generator;
import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.network.URLGenerator;

import java.util.Locale;
import java.util.Objects;

/**
 * Generates locale-aware extended user profiles.
 */
public final class ProfileGenerator implements Generator<UserProfile> {

    private final SimpleProfileGenerator simpleProfileGenerator;
    private final CompanyNameGenerator   companyNameGenerator;
    private final ProfessionGenerator    professionGenerator;
    private final URLGenerator           urlGenerator;

    public ProfileGenerator() {
        this(GeneratorConfig.defaults());
    }

    public ProfileGenerator(Locale locale) {
        this(GeneratorConfig.builder().locale(locale).build());
    }

    public ProfileGenerator(GeneratorConfig config) {
        GeneratorConfig effective = Objects.requireNonNull(config, "config must not be null");
        this.simpleProfileGenerator = new SimpleProfileGenerator(effective);
        this.companyNameGenerator = new CompanyNameGenerator(effective);
        this.professionGenerator = new ProfessionGenerator(effective);
        this.urlGenerator = new URLGenerator(effective);
    }

    /**
     * Generates a simple-profile view.
     *
     * @return simple profile
     */
    public SimpleProfile generateSimpleProfile() {
        return simpleProfileGenerator.generate();
    }

    @Override
    public UserProfile generate() {
        SimpleProfile base = simpleProfileGenerator.generate();
        return new UserProfile(
            base.username(),
            base.name(),
            base.sex(),
            base.address(),
            base.mail(),
            base.birthdate(),
            companyNameGenerator.generate(),
            professionGenerator.generate(),
            urlGenerator.generate()
        );
    }
}
