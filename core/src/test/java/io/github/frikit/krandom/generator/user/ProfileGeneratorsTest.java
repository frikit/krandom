/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.user;

import io.github.frikit.krandom.generator.GeneratorConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Profile generators")
class ProfileGeneratorsTest {

    @Test
    @DisplayName("simple profile generator returns populated record")
    void simpleProfile() {
        SimpleProfile profile = new SimpleProfileGenerator(Locale.US).generate();
        assertNotNull(profile);
        assertFalse(profile.username().isBlank());
        assertFalse(profile.name().isBlank());
        assertTrue(profile.sex().equals("M") || profile.sex().equals("F"));
        assertTrue(profile.mail().contains("@"));
        assertNotNull(profile.birthdate());
    }

    @Test
    @DisplayName("profile generator returns populated extended record")
    void profile() {
        UserProfile profile = new ProfileGenerator(Locale.GERMANY).generate();
        assertNotNull(profile);
        assertFalse(profile.company().isBlank());
        assertFalse(profile.job().isBlank());
        assertTrue(profile.website().contains("://"));
    }

    @Test
    @DisplayName("profile generator exposes simple-profile API")
    void simpleProfileMethod() {
        ProfileGenerator generator = new ProfileGenerator();
        SimpleProfile simple = generator.generateSimpleProfile();
        assertNotNull(simple);
        assertFalse(simple.address().isBlank());
    }

    @Test
    @DisplayName("seeded simple profile generation is reproducible")
    void seededSimpleProfile() {
        GeneratorConfig config = GeneratorConfig.builder().seed(1234L).locale(Locale.US).build();
        SimpleProfileGenerator one = new SimpleProfileGenerator(config);
        SimpleProfileGenerator two = new SimpleProfileGenerator(config);
        assertEquals(one.generate(), two.generate());
    }

    @Test
    @DisplayName("simple and full profile birthdays follow the configured clock")
    void birthdaysFollowTheConfiguredClock() {
        java.time.Clock clock = java.time.Clock.fixed(java.time.Instant.parse("1990-06-15T00:00:00Z"),
                                                      java.time.ZoneOffset.UTC);
        GeneratorConfig config = GeneratorConfig.builder().seed(7L).clock(clock).locale(Locale.US).build();
        java.time.LocalDate today = java.time.LocalDate.now(clock);
        SimpleProfileGenerator simple = new SimpleProfileGenerator(config);
        ProfileGenerator full = new ProfileGenerator(config);
        for (int i = 0; i < 20; i++) {
            for (java.time.LocalDate birthdate : java.util.List.of(simple.generate().birthdate(),
                                                                   full.generate().birthdate())) {
                assertTrue(birthdate.isBefore(today) && birthdate.isAfter(today.minusYears(102)),
                           "birthdate " + birthdate + " must be an age of 1-100 years on " + today);
            }
        }
    }

    @Test
    @DisplayName("simple profile can generate both male and female profiles")
    void simpleProfileBothSexes() {
        SimpleProfileGenerator generator = new SimpleProfileGenerator(
            GeneratorConfig.builder().seed(99L).locale(Locale.US).build());
        boolean sawMale = false;
        boolean sawFemale = false;
        for (int i = 0; i < 200 && !(sawMale && sawFemale); i++) {
            String sex = generator.generate().sex();
            sawMale |= "M".equals(sex);
            sawFemale |= "F".equals(sex);
        }
        assertTrue(sawMale && sawFemale);
    }

    @Test
    @DisplayName("constructors reject null config")
    void nullConfig() {
        assertThrows(NullPointerException.class, () -> new SimpleProfileGenerator((GeneratorConfig) null));
        assertThrows(NullPointerException.class, () -> new ProfileGenerator((GeneratorConfig) null));
    }
}
