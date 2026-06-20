/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.examples.e2e.googlesignup;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.object.ObjectFaker;
import io.github.frikit.krandom.jackson.KrandomJackson;

import java.util.Locale;

/**
 * End-to-end example: produce the JSON payload the "Create your Google Account" page submits.
 *
 * <p>The whole form is filled with a single {@link ObjectFaker} call -- every field is resolved by
 * name and type, nested records included -- so there is one pattern to remember:
 * {@code new ObjectFaker<>(Form.class, config).generate()}. Only {@code agreeToTerms} is pinned
 * (you must accept the terms to register). Wired to the UK locale ({@code en_GB}); pass another
 * {@link Locale} to {@link #fake(Locale, long)} to localize it.
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

    /** Fills the whole payload in one call, reproducibly for a given locale and seed. */
    public static SignupPayload fake(Locale locale, long seed) {
        GeneratorConfig config = GeneratorConfig.builder().locale(locale).seed(seed).build();
        return new ObjectFaker<>(SignupPayload.class, config)
                .ruleFor("agreeToTerms", () -> true)
                .generate();
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
