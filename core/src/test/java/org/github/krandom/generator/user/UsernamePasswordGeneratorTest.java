/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.user;

import org.github.krandom.generator.GeneratorConfig;
import org.github.krandom.generator.Generators;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Username and Password generators")
class UsernamePasswordGeneratorTest {

    @Test
    @DisplayName("Username generator creates non-empty values and supports seeding")
    void usernameGenerator() {
        UsernameGenerator gen = new UsernameGenerator();
        String username = gen.generate();
        assertNotNull(username);
        assertFalse(username.isBlank());
        assertTrue(username.length() >= 3);

        GeneratorConfig cfg = GeneratorConfig.builder().seed(42L).build();
        UsernameGenerator a = new UsernameGenerator(cfg);
        UsernameGenerator b = new UsernameGenerator(cfg);
        for (int i = 0; i < 20; i++) {
            assertEquals(a.generate(), b.generate());
        }

        assertThrows(NullPointerException.class, () -> new UsernameGenerator((GeneratorConfig) null));

        UsernameGenerator de = new UsernameGenerator(Locale.GERMANY);
        assertEquals(Locale.GERMANY, de.getLocale());
        assertNotNull(de.generate());
    }

    @Test
    @DisplayName("Password generator supports default, fixed and ranged lengths")
    void passwordGenerator() {
        PasswordGenerator gen = new PasswordGenerator();
        String pwd = gen.generate();
        assertNotNull(pwd);
        assertTrue(pwd.length() >= 8 && pwd.length() <= 16);

        String fixed = gen.generate(12);
        assertEquals(12, fixed.length());

        for (int i = 0; i < 20; i++) {
            String ranged = gen.generate(10, 14);
            assertTrue(ranged.length() >= 10 && ranged.length() <= 14);
        }

        assertThrows(IllegalArgumentException.class, () -> gen.generate(0));
        assertThrows(IllegalArgumentException.class, () -> gen.generate(-1));
        assertThrows(IllegalArgumentException.class, () -> gen.generate(0, 10));
        assertThrows(IllegalArgumentException.class, () -> gen.generate(10, 9));
        assertThrows(NullPointerException.class, () -> new PasswordGenerator(null));

        GeneratorConfig cfg = GeneratorConfig.builder().seed(999L).build();
        PasswordGenerator seededA = new PasswordGenerator(cfg);
        PasswordGenerator seededB = new PasswordGenerator(cfg);
        assertEquals(seededA.generate(), seededB.generate());
    }

    @Test
    @DisplayName("Generators factory exposes username/password generators")
    void generatorsFactoryCoverage() {
        assertNotNull(Generators.ofUsername().generate());
        assertNotNull(Generators.ofPassword().generate());
    }
}
