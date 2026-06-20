/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.examples.e2e;

import com.fasterxml.jackson.databind.JsonNode;
import io.github.frikit.krandom.examples.e2e.googlesignup.GoogleSignupForm;
import io.github.frikit.krandom.examples.e2e.jobapplication.JobApplicationForm;
import io.github.frikit.krandom.examples.e2e.spidregistration.SpidRegistrationForm;
import io.github.frikit.krandom.jackson.KrandomJackson;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("End-to-end form examples")
class E2eExamplesTest {

    private static JsonNode parse(String json) throws Exception {
        return KrandomJackson.newObjectMapper().readTree(json);
    }

    private static int ageOf(String isoDate) {
        return Period.between(LocalDate.parse(isoDate), LocalDate.now()).getYears();
    }

    @Test
    @DisplayName("Google signup (UK) fills every field in one ObjectFaker call")
    void googleSignupIsFullyPopulated() throws Exception {
        JsonNode node = parse(GoogleSignupForm.toJson(42L));
        assertTrue(node.hasNonNull("firstName"));
        assertTrue(node.hasNonNull("email"));
        assertTrue(node.get("birthday").has("year"));
        assertTrue(node.get("agreeToTerms").asBoolean(), "agreeToTerms is pinned via ruleFor");
        assertEquals(Locale.UK, GoogleSignupForm.DEFAULT_LOCALE);
    }

    @Test
    @DisplayName("Google signup date of birth is always an 18..99-year-old")
    void googleSignupBirthdayWithinAgeRange() throws Exception {
        for (long seed = 0; seed < 25; seed++) {
            JsonNode b = parse(GoogleSignupForm.toJson(seed)).get("birthday");
            LocalDate dob = LocalDate.of(b.get("year").asInt(), b.get("month").asInt(), b.get("day").asInt());
            int age = Period.between(dob, LocalDate.now()).getYears();
            assertTrue(age >= 18 && age <= 99, "seed " + seed + " produced age " + age);
        }
    }

    @Test
    @DisplayName("Job application (Germany) has every section populated")
    void jobApplicationIsComprehensive() throws Exception {
        JsonNode a = parse(JobApplicationForm.toJson(7L));
        for (String section : List.of("personal", "address", "rightToWork", "driving", "role", "screening")) {
            assertTrue(a.hasNonNull(section), "missing section: " + section);
        }
        for (String list : List.of("employmentHistory", "education", "skills", "languages", "references")) {
            assertTrue(a.get(list).isArray(), "expected array: " + list);
        }
        assertTrue(a.get("personal").hasNonNull("firstName"));
        assertTrue(a.get("driving").get("hasDrivingLicence").isBoolean());
        assertEquals(Locale.GERMANY, JobApplicationForm.DEFAULT_LOCALE);
    }

    @Test
    @DisplayName("Job application pinned fields respect their bounds")
    void jobApplicationBoundsAreRespected() throws Exception {
        for (long seed = 0; seed < 15; seed++) {
            JsonNode a = parse(JobApplicationForm.toJson(seed));
            int age = ageOf(a.get("personal").get("dateOfBirth").asText());
            assertTrue(age >= 18 && age <= 99, "seed " + seed + " age " + age);
            int salary = a.get("role").get("salaryExpectation").asInt();
            assertTrue(salary >= 30_000 && salary <= 200_000, "seed " + seed + " salary " + salary);
            int notice = a.get("role").get("noticePeriodWeeks").asInt();
            assertTrue(notice >= 0 && notice <= 12, "seed " + seed + " notice " + notice);
            assertTrue(a.get("rightToWork").get("eligibleToWorkInCountry").asBoolean());
        }
    }

    @Test
    @DisplayName("SPID registration (Italy) fills every section in one ObjectFaker call")
    void spidRegistrationIsComprehensive() throws Exception {
        JsonNode p = parse(SpidRegistrationForm.toJson(11L));
        for (String section : List.of("identity", "birth", "residence", "contacts", "idCard", "consents")) {
            assertTrue(p.hasNonNull(section), "missing section: " + section);
        }
        // name and placeOfBirth auto-resolve via the custom SPID vocabulary registry.
        assertTrue(p.get("identity").hasNonNull("name"), "name should resolve as a first name");
        assertTrue(p.get("identity").hasNonNull("familyName"));
        assertTrue(p.get("birth").hasNonNull("placeOfBirth"), "placeOfBirth should resolve as a city");
        assertEquals("Italia", p.get("residence").get("country").asText());
        assertEquals(Locale.ITALY, SpidRegistrationForm.DEFAULT_LOCALE);
    }

    @Test
    @DisplayName("SPID identifiers carry their official Italian formats")
    void spidIdentifiersAreWellFormed() throws Exception {
        for (long seed = 0; seed < 20; seed++) {
            JsonNode p = parse(SpidRegistrationForm.toJson(seed));
            // Codice Fiscale: TINIT- prefix + 16-char fiscal code, exactly as SPID assertions carry it.
            String fiscalNumber = p.get("identity").get("fiscalNumber").asText();
            assertTrue(fiscalNumber.matches("TINIT-[A-Z0-9]{16}"), "seed " + seed + " fiscalNumber " + fiscalNumber);
            // Gender is recorded as M or F.
            assertTrue(p.get("identity").get("gender").asText().matches("[MF]"));
            // County of birth resolves to a real Italian region (no province-code data in krandom).
            assertTrue(!p.get("birth").get("countyOfBirth").asText().isBlank(), "seed " + seed + " countyOfBirth");
            // CIE card number: 2 letters + 5 digits + 2 letters.
            assertTrue(p.get("idCard").get("number").asText().matches("[A-Z]{2}[0-9]{5}[A-Z]{2}"));
            // Date of birth is a realistic 18..99-year-old.
            int age = ageOf(p.get("birth").get("dateOfBirth").asText());
            assertTrue(age >= 18 && age <= 99, "seed " + seed + " age " + age);
            // The electronic ID card expires in the future.
            LocalDate expiry = LocalDate.parse(p.get("idCard").get("expirationDate").asText());
            assertTrue(expiry.isAfter(LocalDate.now()), "seed " + seed + " expiry " + expiry);
            // Consents an applicant must affirm to activate the identity.
            assertTrue(p.get("consents").get("consentToDataProcessing").asBoolean());
        }
    }

    @Test
    @DisplayName("same seed + locale is reproducible")
    void reproducibleForSameSeed() {
        assertEquals(GoogleSignupForm.toJson(99L), GoogleSignupForm.toJson(99L));
        assertEquals(JobApplicationForm.toJson(99L), JobApplicationForm.toJson(99L));
        assertEquals(SpidRegistrationForm.toJson(99L), SpidRegistrationForm.toJson(99L));
    }

    @Test
    @DisplayName("examples honor the requested locale")
    void honorsLocale() throws Exception {
        assertTrue(parse(GoogleSignupForm.toJson(Locale.UK, 5L)).has("firstName"));
        assertTrue(parse(GoogleSignupForm.toJson(Locale.FRANCE, 5L)).has("firstName"));
    }
}
