/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.examples.e2e.googlesignup;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.github.frikit.krandom.examples.e2e.support.Emails;
import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.Generators;
import io.github.frikit.krandom.generator.user.FirstNameGenerator;
import io.github.frikit.krandom.generator.user.Gender;
import io.github.frikit.krandom.generator.user.GenderGenerator;
import io.github.frikit.krandom.generator.user.LastNameGenerator;
import io.github.frikit.krandom.generator.user.PasswordGenerator;
import io.github.frikit.krandom.jackson.KrandomJackson;

import java.util.Locale;

/**
 * End-to-end example: fill the "Create your Google Account" registration form and produce the exact
 * JSON payload such a page would POST to its backend.
 *
 * <p>This example is wired to the UK locale ({@code en_GB}); pass a different {@link Locale} to
 * {@link #fake(Locale, long)} to localize it elsewhere. The username and both email addresses are
 * derived from the generated name so the identity is internally consistent. Run
 * {@link #main(String[])} to print a sample payload.
 */
public final class GoogleSignupForm {

    /** This registration form is demonstrated with the UK locale. */
    public static final Locale DEFAULT_LOCALE = Locale.UK;

    private GoogleSignupForm() {
    }

    /** The JSON body the signup page submits. */
    public record SignupPayload(
            String firstName,
            String lastName,
            String username,
            String email,
            String password,
            Birthday birthday,
            String gender,
            String phoneNumber,
            String recoveryEmail,
            String country,
            boolean agreeToTerms) {
    }

    /** Google splits the date of birth into three separate select boxes. */
    public record Birthday(int year, int month, int day) {
    }

    /** Builds one fake-but-realistic signup payload, reproducibly for a given locale and seed. */
    public static SignupPayload fake(Locale locale, long seed) {
        GeneratorConfig config = GeneratorConfig.builder().locale(locale).seed(seed).build();

        // Pick a gender first so the given name and gender label agree.
        Gender gender = Generators.ofBoolean(seed).generate() ? Gender.MALE : Gender.FEMALE;

        String firstName = new FirstNameGenerator(config).generate(gender);
        String lastName = new LastNameGenerator(config).generate();

        Birthday birthday = new Birthday(
                Generators.ofInt(1960, 2005, seed).generate(),
                Generators.ofInt(1, 12, seed + 1).generate(),
                Generators.ofInt(1, 28, seed + 2).generate());

        return new SignupPayload(
                firstName,
                lastName,
                Emails.username(firstName, lastName, seed + 5),
                Emails.fromName(firstName, lastName, seed + 3),
                new PasswordGenerator(config).generate(),
                birthday,
                new GenderGenerator(config).generate(gender),
                Generators.ofPhoneNumber(config).generate(),
                Emails.fromName(firstName, lastName, seed + 4),
                Generators.ofCountry(config).generate(),
                true);
    }

    /** Serializes a freshly generated payload to the JSON the browser would submit. */
    public static String toJson(Locale locale, long seed) {
        try {
            return KrandomJackson.newObjectMapper()
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(fake(locale, seed));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize Google signup payload", e);
        }
    }

    /** Convenience: serialize using this example's default (UK) locale. */
    public static String toJson(long seed) {
        return toJson(DEFAULT_LOCALE, seed);
    }

    public static void main(String[] args) {
        System.out.println(toJson(DEFAULT_LOCALE, System.nanoTime()));
    }
}
