/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.examples.e2e;

import com.fasterxml.jackson.databind.JsonNode;
import io.github.frikit.krandom.examples.e2e.googlesignup.GoogleSignupForm;
import io.github.frikit.krandom.examples.e2e.jobapplication.JobApplicationForm;
import io.github.frikit.krandom.examples.e2e.support.Emails;
import io.github.frikit.krandom.jackson.KrandomJackson;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("End-to-end form examples")
class E2eExamplesTest {

    private static JsonNode parse(String json) throws Exception {
        return KrandomJackson.newObjectMapper().readTree(json);
    }

    @Test
    @DisplayName("Google signup (UK) produces a fully populated payload")
    void googleSignupProducesPopulatedJson() throws Exception {
        JsonNode node = parse(GoogleSignupForm.toJson(42L));
        assertFalse(node.get("firstName").asText().isBlank());
        assertFalse(node.get("username").asText().isBlank());
        assertTrue(node.get("birthday").has("year"));
        assertTrue(node.get("agreeToTerms").asBoolean());
        assertEquals(Locale.UK, GoogleSignupForm.DEFAULT_LOCALE);
    }

    @Test
    @DisplayName("email is derived from the generated name")
    void emailMatchesName() throws Exception {
        JsonNode node = parse(GoogleSignupForm.toJson(42L));
        String first = node.get("firstName").asText();
        String last = node.get("lastName").asText();
        String expectedLocalPart = Emails.localPart(first, last);
        assertTrue(node.get("email").asText().startsWith(expectedLocalPart + "@"),
                "email local-part should be derived from the name");
        assertTrue(node.get("recoveryEmail").asText().startsWith(expectedLocalPart + "@"),
                "recovery email should be derived from the name");
    }

    @Test
    @DisplayName("Job application (Germany) produces nested objects and lists")
    void jobApplicationHasNestedStructure() throws Exception {
        JsonNode node = parse(JobApplicationForm.toJson(7L));
        assertFalse(node.get("applicant").get("fullName").asText().isBlank());
        assertFalse(node.get("position").get("desiredTitle").asText().isBlank());
        assertTrue(node.get("education").isArray());
        assertEquals(2, node.get("education").size());
        assertEquals(2, node.get("references").size());
        assertEquals(Locale.GERMANY, JobApplicationForm.DEFAULT_LOCALE);
    }

    @Test
    @DisplayName("same seed + locale is fully reproducible")
    void reproducibleForSameSeed() {
        assertEquals(GoogleSignupForm.toJson(99L), GoogleSignupForm.toJson(99L));
        assertEquals(JobApplicationForm.toJson(99L), JobApplicationForm.toJson(99L));
    }

    @Test
    @DisplayName("examples honor the requested locale")
    void honorsLocale() throws Exception {
        JsonNode uk = parse(GoogleSignupForm.toJson(Locale.UK, 5L));
        JsonNode fr = parse(GoogleSignupForm.toJson(Locale.FRANCE, 5L));
        assertFalse(uk.get("firstName").asText().isBlank());
        assertFalse(fr.get("firstName").asText().isBlank());
    }
}
