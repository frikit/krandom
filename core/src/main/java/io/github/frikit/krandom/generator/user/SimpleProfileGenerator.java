/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.user;

import io.github.frikit.krandom.generator.Generator;
import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.location.StreetAddressGenerator;

import java.security.SecureRandom;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;

/**
 * Generates locale-aware simple user profiles.
 */
public final class SimpleProfileGenerator implements Generator<SimpleProfile> {

    private final Random                 random;
    private final UsernameGenerator      usernameGenerator;
    private final FullNameGenerator      fullNameGenerator;
    private final StreetAddressGenerator addressGenerator;
    private final EmailGenerator         emailGenerator;
    private final BirthdayGenerator      birthdayGenerator;

    public SimpleProfileGenerator() {
        this(GeneratorConfig.defaults());
    }

    public SimpleProfileGenerator(Locale locale) {
        this(GeneratorConfig.builder().locale(locale).build());
    }

    public SimpleProfileGenerator(GeneratorConfig config) {
        GeneratorConfig effective = Objects.requireNonNull(config, "config must not be null");
        this.random = effective.createRandom();
        this.usernameGenerator = new UsernameGenerator(effective);
        this.fullNameGenerator = new FullNameGenerator(effective);
        this.addressGenerator = new StreetAddressGenerator(effective);
        this.emailGenerator = new EmailGenerator(effective);
        this.birthdayGenerator = effective.getSeed().isPresent()
                                 ? new BirthdayGenerator(effective.getLocale(), effective.getSeed().getAsLong())
                                 : new BirthdayGenerator(effective.getLocale());
    }

    @Override
    public SimpleProfile generate() {
        Gender gender = random.nextBoolean() ? Gender.MALE : Gender.FEMALE;
        String sex = gender == Gender.MALE ? "M" : "F";

        return new SimpleProfile(
            usernameGenerator.generate(),
            fullNameGenerator.generate(gender),
            sex,
            addressGenerator.generateFullAddress(),
            emailGenerator.generate(),
            birthdayGenerator.generate()
        );
    }
}
