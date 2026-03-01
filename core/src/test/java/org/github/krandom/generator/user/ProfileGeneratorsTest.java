/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.user;

import org.github.krandom.generator.GeneratorConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

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
