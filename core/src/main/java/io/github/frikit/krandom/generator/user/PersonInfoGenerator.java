/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.user;

import io.github.frikit.krandom.generator.Generator;
import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.location.AddressInfo;
import io.github.frikit.krandom.generator.location.AddressInfoGenerator;

import java.util.Locale;
import java.util.Objects;

/**
 * Generates structured person payloads composed from contact and address generators.
 */
public final class PersonInfoGenerator implements Generator<PersonInfo> {

    private final GeneratorConfig      config;
    private final ContactInfoGenerator contactInfoGenerator;
    private final AddressInfoGenerator addressInfoGenerator;
    private final PasswordGenerator    passwordGenerator;

    /**
     * Creates a person-info generator using default configuration ({@link Locale#US}).
     */
    public PersonInfoGenerator() {
        this(GeneratorConfig.defaults());
    }

    /**
     * Creates a person-info generator for the specified locale.
     *
     * @param locale locale to use
     */
    public PersonInfoGenerator(Locale locale) {
        this(GeneratorConfig.builder().locale(Objects.requireNonNull(locale, "locale must not be null")).build());
    }

    /**
     * Creates a person-info generator using explicit configuration.
     *
     * @param config generator configuration
     */
    public PersonInfoGenerator(GeneratorConfig config) {
        this.config = Objects.requireNonNull(config, "config must not be null");
        this.contactInfoGenerator = new ContactInfoGenerator(config);
        this.addressInfoGenerator = new AddressInfoGenerator(config);
        this.passwordGenerator = new PasswordGenerator(config);
    }

    @Override
    public PersonInfo generate() {
        ContactInfo contact = contactInfoGenerator.generate();
        AddressInfo address = addressInfoGenerator.generate();
        return new PersonInfo(contact, address, usernameFrom(contact), passwordGenerator.generate());
    }

    /**
     * Returns the configured locale.
     */
    public Locale getLocale() {
        return config.getLocale();
    }

    private static String usernameFrom(ContactInfo contact) {
        return contact.email().substring(0, contact.email().indexOf('@'));
    }
}
