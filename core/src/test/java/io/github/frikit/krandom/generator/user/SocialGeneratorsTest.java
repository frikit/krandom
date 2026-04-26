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

@DisplayName("Social generators")
class SocialGeneratorsTest {

    @Test
    @DisplayName("social handle generator supports platform-specific normalization")
    void socialHandleVariants() {
        SocialHandleGenerator generator = new SocialHandleGenerator(
            GeneratorConfig.builder().seed(11L).locale(Locale.US).build()
        );

        String base = generator.generate();
        assertTrue(base.startsWith("@"));
        assertFalse(base.isBlank());

        String github = generator.generateForPlatform("github");
        assertFalse(github.contains("_"));
        assertFalse(github.contains("."));

        String linkedin = generator.generateForPlatform("linkedin");
        assertFalse(linkedin.startsWith("@"));
        assertFalse(linkedin.contains("_"));

        String x = generator.generateForPlatform("x");
        assertTrue(x.startsWith("@"));
    }

    @Test
    @DisplayName("social handle generator validates constructor and platform input")
    void socialHandleValidation() {
        assertThrows(NullPointerException.class, () -> new SocialHandleGenerator((Locale) null));
        assertThrows(NullPointerException.class, () -> new SocialHandleGenerator((GeneratorConfig) null));

        SocialHandleGenerator generator = new SocialHandleGenerator(Locale.US);
        assertThrows(NullPointerException.class, () -> generator.generateForPlatform(null));
        assertThrows(IllegalArgumentException.class, () -> generator.generateForPlatform("  "));
    }

    @Test
    @DisplayName("social profile generator produces non-empty profile data")
    void socialProfileGenerate() {
        SocialProfileGenerator generator = new SocialProfileGenerator(
            GeneratorConfig.builder().seed(22L).locale(Locale.US).build()
        );
        SocialProfile profile = generator.generate();
        assertNotNull(profile);
        assertFalse(profile.platform().isBlank());
        assertFalse(profile.handle().isBlank());
        assertFalse(profile.displayName().isBlank());
        assertFalse(profile.bio().isBlank());
        assertTrue(profile.profileUrl().startsWith("https://"));
    }

    @Test
    @DisplayName("social profile locale constructor works")
    void socialProfileLocaleConstructor() {
        SocialProfileGenerator generator = new SocialProfileGenerator(Locale.US);
        SocialProfile profile = generator.generate();
        assertNotNull(profile);
        assertFalse(profile.platform().isBlank());
    }

    @Test
    @DisplayName("social profile generator is deterministic when seeded")
    void socialProfileDeterministic() {
        GeneratorConfig config = GeneratorConfig.builder().seed(123L).locale(Locale.US).build();
        SocialProfileGenerator a = new SocialProfileGenerator(config);
        SocialProfileGenerator b = new SocialProfileGenerator(config);
        assertEquals(a.generate(), b.generate());
    }

    @Test
    @DisplayName("social profile constructor validates inputs")
    void socialProfileValidation() {
        assertThrows(NullPointerException.class, () -> new SocialProfileGenerator((Locale) null));
        assertThrows(NullPointerException.class, () -> new SocialProfileGenerator((GeneratorConfig) null));
    }

    @Test
    @DisplayName("social profile generation supports handles with and without leading at")
    void socialProfileHandleSlugBranches() {
        boolean seenWithAt = false;
        boolean seenWithoutAt = false;

        for (int seed = 0; seed < 200; seed++) {
            SocialProfileGenerator generator = new SocialProfileGenerator(
                GeneratorConfig.builder().seed(seed).locale(Locale.US).build()
            );
            SocialProfile profile = generator.generate();
            if (profile.handle().startsWith("@")) {
                seenWithAt = true;
            } else {
                seenWithoutAt = true;
            }
            if (seenWithAt && seenWithoutAt) {
                break;
            }
        }

        assertTrue(seenWithAt);
        assertTrue(seenWithoutAt);
    }
}
