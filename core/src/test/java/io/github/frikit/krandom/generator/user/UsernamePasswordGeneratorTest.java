/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.user;

import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.Generators;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

        assertDoesNotThrow(() -> {
            Method fallback = UsernameGenerator.class.getDeclaredMethod(
                "fallback", String.class, String.class
            );
            fallback.setAccessible(true);
            assertEquals("default", fallback.invoke(null, "", "default"));
            assertEquals("value", fallback.invoke(null, "value", "default"));
        });
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
    @DisplayName("Password policies enforce required character sets")
    void passwordPolicy() {
        PasswordPolicy policy = PasswordPolicy.builder()
            .length(12, 12)
            .requireLowercase(2)
            .requireUppercase(2)
            .requireDigits(3)
            .requireSymbols("!@#", 2)
            .build();
        String password = new PasswordGenerator(GeneratorConfig.builder().seed(11L).build()).generate(policy);

        assertEquals(12, password.length());
        assertTrue(password.chars().filter(Character::isLowerCase).count() >= 2);
        assertTrue(password.chars().filter(Character::isUpperCase).count() >= 2);
        assertTrue(password.chars().filter(Character::isDigit).count() >= 3);
        assertTrue(password.chars().filter(c -> c == '!' || c == '@' || c == '#').count() >= 2);
        assertThrows(NullPointerException.class, () -> new PasswordGenerator().generate((PasswordPolicy) null));
        assertThrows(IllegalArgumentException.class, () -> PasswordPolicy.builder().length(2).requireDigits(3).build());
        assertThrows(IllegalArgumentException.class, () -> PasswordPolicy.builder().requireSymbols("", 1));
        assertThrows(IllegalArgumentException.class, () -> PasswordPolicy.builder().requireLowercase(-1));
    }

    @Test
    @DisplayName("password policy exposes an immutable default alphabet and validates every builder input")
    void passwordPolicyBuilderCoverage() {
        PasswordPolicy defaultPolicy = PasswordPolicy.builder().length(10).build();
        assertEquals(10, defaultPolicy.minLength());
        assertEquals(10, defaultPolicy.maxLength());
        assertTrue(defaultPolicy.requirements().isEmpty());
        assertTrue(defaultPolicy.alphabet().contains(PasswordPolicy.LOWERCASE));
        assertTrue(defaultPolicy.alphabet().contains(PasswordPolicy.UPPERCASE));
        assertTrue(defaultPolicy.alphabet().contains(PasswordPolicy.DIGITS));
        assertTrue(defaultPolicy.alphabet().contains(PasswordPolicy.SYMBOLS));

        PasswordPolicy custom = PasswordPolicy.builder().length(4).require("ab", 0).build();
        assertEquals("ab", custom.requirements().getFirst().symbols());
        assertEquals(0, custom.requirements().getFirst().minimumCount());
        assertEquals("ab", custom.alphabet());
        assertThrows(IllegalArgumentException.class, () -> PasswordPolicy.builder().length(0));
        assertThrows(IllegalArgumentException.class, () -> PasswordPolicy.builder().length(3, 2));
        assertThrows(NullPointerException.class, () -> PasswordPolicy.builder().require(null, 1));
    }

    @Test
    @DisplayName("Generators factory exposes username/password generators")
    void generatorsFactoryCoverage() {
        assertNotNull(Generators.ofUsername().generate());
        assertNotNull(Generators.ofPassword().generate());
    }
}
