/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.examples.e2e.jobapplication;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.object.ObjectFaker;
import io.github.frikit.krandom.jackson.KrandomJackson;

import java.util.List;
import java.util.Locale;

/**
 * End-to-end example: a larger, nested job-application form as the JSON payload it submits --
 * nested objects and repeated (list) sections, all filled by a single {@link ObjectFaker} call.
 *
 * <p>Wired to the German locale ({@code de_DE}) to show a different page targeting a different
 * {@link Locale} than the {@code googlesignup} example.
 */
public final class JobApplicationForm {

    /** This example is demonstrated with the German locale. */
    public static final Locale DEFAULT_LOCALE = Locale.GERMANY;

    private JobApplicationForm() {
    }

    public record Application(
            Applicant applicant,
            Address address,
            Position position,
            List<Education> education,
            List<Reference> references,
            String summary,
            boolean willRelocate) {
    }

    public record Applicant(String fullName, String gender, String email, String phoneNumber, String dateOfBirth) {
    }

    public record Address(String city, String state, String postalCode, String country) {
    }

    public record Position(String desiredTitle, int desiredSalary, String availableFrom) {
    }

    public record Education(String institution, String degree, String fieldOfStudy, int graduationYear) {
    }

    public record Reference(String name, String relationship, String email, String phoneNumber) {
    }

    /** Fills the whole application -- nested objects and lists -- in one call. */
    public static Application fake(Locale locale, long seed) {
        GeneratorConfig config = GeneratorConfig.builder().locale(locale).seed(seed).build();
        return new ObjectFaker<>(Application.class, config).generate();
    }

    /** Serializes a freshly generated application to its final JSON. */
    public static String toJson(Locale locale, long seed) {
        try {
            return KrandomJackson.newObjectMapper()
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(fake(locale, seed));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize job application", e);
        }
    }

    /** Convenience: serialize using this example's default (German) locale. */
    public static String toJson(long seed) {
        return toJson(DEFAULT_LOCALE, seed);
    }

    public static void main(String[] args) {
        System.out.println(toJson(DEFAULT_LOCALE, System.nanoTime()));
    }
}
