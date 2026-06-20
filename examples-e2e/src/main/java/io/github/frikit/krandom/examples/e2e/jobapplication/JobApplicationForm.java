/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.examples.e2e.jobapplication;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.github.frikit.krandom.examples.e2e.support.Emails;
import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.Generators;
import io.github.frikit.krandom.generator.user.FirstNameGenerator;
import io.github.frikit.krandom.generator.user.Gender;
import io.github.frikit.krandom.generator.user.GenderGenerator;
import io.github.frikit.krandom.generator.user.LastNameGenerator;
import io.github.frikit.krandom.jackson.KrandomJackson;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * End-to-end example: a larger, nested job-application form rendered as the JSON payload it would
 * submit -- showing nested objects and repeated (list) sections.
 *
 * <p>This example is wired to the German locale ({@code de_DE}) to demonstrate that a different page
 * can target a different {@link Locale} than the {@code googlesignup} example. Applicant and
 * reference emails are derived from their names so each identity stays consistent.
 */
public final class JobApplicationForm {

    /** This example is demonstrated with the German locale. */
    public static final Locale DEFAULT_LOCALE = Locale.GERMANY;

    private static final String[] DEGREES = {"BSc", "MSc", "BA", "MBA", "PhD"};
    private static final String[] FIELDS = {
        "Computer Science", "Business Administration", "Mechanical Engineering", "Economics", "Design"
    };
    private static final String[] RELATIONSHIPS = {"Former Manager", "Colleague", "Mentor", "Team Lead"};

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

    /** Builds one fake-but-realistic application, reproducibly for a given locale and seed. */
    public static Application fake(Locale locale, long seed) {
        GeneratorConfig config = GeneratorConfig.builder().locale(locale).seed(seed).build();

        Gender gender = Generators.ofBoolean(seed).generate() ? Gender.MALE : Gender.FEMALE;
        String firstName = new FirstNameGenerator(config).generate(gender);
        String lastName = new LastNameGenerator(config).generate();

        Applicant applicant = new Applicant(
                firstName + " " + lastName,
                new GenderGenerator(config).generate(gender),
                Emails.fromName(firstName, lastName, seed + 5),
                Generators.ofPhoneNumber(config).generate(),
                isoDate(1965, 2000, seed));

        Address address = new Address(
                Generators.ofCity(config).generate(),
                Generators.ofState(config).generate(),
                Generators.ofPostalCode(config).generate(),
                Generators.ofCountry(config).generate());

        Position position = new Position(
                Generators.ofProfession(config).generate(),
                Generators.ofInt(45_000, 180_000, seed + 3).generate(),
                isoDate(2025, 2026, seed + 7));

        // Reuse one generator instance per field so each loop iteration yields a new value.
        var cityGen = Generators.ofCity(config);
        var degreeIdx = Generators.ofInt(0, DEGREES.length - 1, seed + 10);
        var fieldIdx = Generators.ofInt(0, FIELDS.length - 1, seed + 11);
        var gradYear = Generators.ofInt(1995, 2024, seed + 12);
        List<Education> education = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            education.add(new Education(
                    cityGen.generate() + " University",
                    DEGREES[degreeIdx.generate()],
                    FIELDS[fieldIdx.generate()],
                    gradYear.generate()));
        }

        var refFirst = new FirstNameGenerator(config);
        var refLast = new LastNameGenerator(config);
        var refPhone = Generators.ofPhoneNumber(config);
        var relIdx = Generators.ofInt(0, RELATIONSHIPS.length - 1, seed + 13);
        List<Reference> references = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            String rFirst = refFirst.generate();
            String rLast = refLast.generate();
            references.add(new Reference(
                    rFirst + " " + rLast,
                    RELATIONSHIPS[relIdx.generate()],
                    Emails.fromName(rFirst, rLast, seed + 20 + i),
                    refPhone.generate()));
        }

        String summary = "Experienced " + position.desiredTitle() + " seeking a new opportunity.";

        return new Application(
                applicant, address, position, education, references, summary,
                Generators.ofBoolean(seed + 1).generate());
    }

    /** Builds an ISO {@code yyyy-MM-dd} date string from independent, seeded integer generators. */
    private static String isoDate(int minYear, int maxYear, long seed) {
        return String.format(
                "%04d-%02d-%02d",
                Generators.ofInt(minYear, maxYear, seed).generate(),
                Generators.ofInt(1, 12, seed + 1).generate(),
                Generators.ofInt(1, 28, seed + 2).generate());
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
