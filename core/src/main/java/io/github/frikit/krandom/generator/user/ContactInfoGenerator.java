/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.user;

import io.github.frikit.krandom.generator.Generator;
import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.location.PhoneNumberGenerator;

import java.text.Normalizer;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;

/**
 * Generates structured contact payloads with coherent names, phones, and email addresses.
 */
public final class ContactInfoGenerator implements Generator<ContactInfo> {

    private static final int DEFAULT_MIN_AGE = 18;
    private static final int DEFAULT_MAX_AGE = 90;

    private final GeneratorConfig      config;
    private final Random               random;
    private final FirstNameGenerator   firstNameGenerator;
    private final LastNameGenerator    lastNameGenerator;
    private final GenderGenerator      genderGenerator;
    private final PhoneNumberGenerator phoneNumberGenerator;
    private final EmailGenerator       emailGenerator;

    /**
     * Creates a contact-info generator using default configuration ({@link Locale#US}).
     */
    public ContactInfoGenerator() {
        this(GeneratorConfig.defaults());
    }

    /**
     * Creates a contact-info generator for the specified locale.
     *
     * @param locale locale to use
     */
    public ContactInfoGenerator(Locale locale) {
        this(GeneratorConfig.builder().locale(Objects.requireNonNull(locale, "locale must not be null")).build());
    }

    /**
     * Creates a contact-info generator using explicit configuration.
     *
     * @param config generator configuration
     */
    public ContactInfoGenerator(GeneratorConfig config) {
        this.config = Objects.requireNonNull(config, "config must not be null");
        this.random = config.createRandom();
        this.firstNameGenerator = new FirstNameGenerator(config);
        this.lastNameGenerator = new LastNameGenerator(config);
        this.genderGenerator = new GenderGenerator(config);
        this.phoneNumberGenerator = new PhoneNumberGenerator(config);
        this.emailGenerator = new EmailGenerator(config);
    }

    @Override
    public ContactInfo generate() {
        Gender gender = random.nextBoolean() ? Gender.MALE : Gender.FEMALE;
        String firstName = firstNameGenerator.generate(gender);
        String lastName = lastNameGenerator.generate();
        String name = firstName + " " + lastName;
        int age = random.nextInt(DEFAULT_MIN_AGE, DEFAULT_MAX_AGE + 1);
        String phoneFormatted = phoneNumberGenerator.generate(true);
        String phone = digitsOnly(phoneFormatted);

        return new ContactInfo(
            firstName,
            lastName,
            name,
            genderGenerator.generate(gender),
            age,
            phone,
            phoneFormatted,
            coherentEmail(firstName, lastName)
        );
    }

    /**
     * Returns the configured locale.
     */
    public Locale getLocale() {
        return config.getLocale();
    }

    private String coherentEmail(String firstName, String lastName) {
        String generatedEmail = emailGenerator.generate();
        int atIndex = generatedEmail.indexOf('@');
        String domain = generatedEmail.substring(atIndex + 1);

        String localPart = localPart(firstName, lastName);
        if (localPart.isBlank()) {
            return generatedEmail;
        }
        return localPart + "@" + domain;
    }

    private String localPart(String firstName, String lastName) {
        String normalizedFirst = normalize(firstName);
        String normalizedLast = normalize(lastName);
        if (normalizedFirst.isBlank() || normalizedLast.isBlank()) {
            return "";
        }
        return switch (random.nextInt(4)) {
            case 0 -> normalizedFirst + "." + normalizedLast;
            case 1 -> normalizedFirst + normalizedLast;
            case 2 -> normalizedFirst.charAt(0) + normalizedLast;
            default -> normalizedFirst + "_" + normalizedLast;
        };
    }

    private static String digitsOnly(String value) {
        return value.replaceAll("\\D", "");
    }

    private static String normalize(String value) {
        return Normalizer.normalize(value, Normalizer.Form.NFD)
                         .replaceAll("\\p{M}+", "")
                         .toLowerCase(Locale.ROOT)
                         .replaceAll("[^a-z0-9]", "");
    }
}
