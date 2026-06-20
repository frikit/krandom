/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.examples.e2e.jobapplication;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.Generators;
import io.github.frikit.krandom.generator.object.ObjectFaker;
import io.github.frikit.krandom.generator.user.BirthdayGenerator;
import io.github.frikit.krandom.jackson.KrandomJackson;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

/**
 * End-to-end example: a large, real-world job-application form rendered as the JSON payload it would
 * submit -- personal details, residence, right-to-work, driving, the role applied for, full
 * employment history, education, skills, languages, references, and screening questions.
 *
 * <p>The whole graph is filled with a single {@link ObjectFaker} call. The few fields a real form
 * actually validates are pinned with {@code ruleFor} using dotted nested paths -- a realistic
 * 18..99 date of birth, bounded salary/notice/years, near-future availability, and the consents an
 * applicant would affirm. Everything else (names, address, companies, lists, booleans, …) is
 * resolved automatically by name and type. Wired to the German locale ({@code de_DE}).
 */
public final class JobApplicationForm {

    /** This example is demonstrated with the German locale. */
    public static final Locale DEFAULT_LOCALE = Locale.GERMANY;

    private JobApplicationForm() {
    }

    /** The full application payload the page submits. */
    public record Application(
            Personal personal,
            Address address,
            RightToWork rightToWork,
            Driving driving,
            Role role,
            List<PastJob> employmentHistory,
            List<Education> education,
            List<Skill> skills,
            List<Language> languages,
            List<Reference> references,
            Screening screening) {
    }

    public record Personal(
            String firstName,
            String lastName,
            String gender,
            String dateOfBirth,
            String nationality,
            String maritalStatus,
            String email,
            String phoneNumber) {
    }

    public record Address(
            String line1,
            String city,
            String state,
            String postalCode,
            String country,
            int yearsAtAddress,
            String residencyStatus) {
    }

    public record RightToWork(
            boolean eligibleToWorkInCountry,
            boolean requiresSponsorship,
            String workAuthorizationType) {
    }

    public record Driving(
            boolean hasDrivingLicence,
            String licenceType,
            String licenceNumber,
            boolean hasOwnVehicle) {
    }

    public record Role(
            String desiredTitle,
            int salaryExpectation,
            int noticePeriodWeeks,
            String availableFrom,
            boolean willingToRelocate,
            boolean willingToTravel,
            String remotePreference,
            String employmentTypeSought) {
    }

    public record PastJob(
            String company,
            String jobTitle,
            String location,
            String employmentType,
            String reasonForLeaving) {
    }

    public record Education(String institution, String degree, String fieldOfStudy, String grade) {
    }

    public record Skill(String name, String proficiency) {
    }

    public record Language(String language, String proficiency) {
    }

    public record Reference(String name, String relationship, String company, String email, String phoneNumber) {
    }

    public record Screening(
            boolean hasCriminalRecord,
            boolean consentToBackgroundCheck,
            boolean consentToDataProcessing,
            String howDidYouHearAboutUs,
            String coverNote) {
    }

    /** Fills the whole application in one call, with the handful of fields a real form validates pinned. */
    public static Application fake(Locale locale, long seed) {
        GeneratorConfig config = GeneratorConfig.builder().locale(locale).seed(seed).build();
        BirthdayGenerator dateOfBirth = new BirthdayGenerator(18, 99, config);

        return new ObjectFaker<>(Application.class, config)
                // Realistic 18..99-year-old (nested dotted path).
                .ruleFor("personal.dateOfBirth", () -> dateOfBirth.generate().toString())
                // Numbers and dates a form would actually validate.
                .ruleFor("address.yearsAtAddress", Generators.ofInt(0, 40, seed + 1))
                .ruleFor("role.salaryExpectation", Generators.ofInt(30_000, 200_000, seed + 2))
                .ruleFor("role.noticePeriodWeeks", Generators.ofInt(0, 12, seed + 3))
                .ruleFor("role.availableFrom",
                        () -> LocalDate.now().plusDays(Generators.ofInt(0, 90, seed + 4).generate()).toString())
                // Consents and eligibility an applicant would affirm.
                .ruleFor("rightToWork.eligibleToWorkInCountry", () -> true)
                .ruleFor("screening.consentToBackgroundCheck", () -> true)
                .ruleFor("screening.consentToDataProcessing", () -> true)
                .generate();
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
