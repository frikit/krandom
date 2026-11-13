/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.base;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.user.PasswordGenerator;
import io.github.frikit.krandom.generator.user.PasswordPolicy;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * A caller-supplied pool that holds a supplementary character, one outside the Basic Multilingual Plane that
 * UTF-16 writes as a surrogate pair, is drawn from as whole code points.
 */
@DisplayName("Pools with supplementary characters")
class SupplementaryPoolTest {

    private static final String GRIN = "😀";
    private static final String PARTY = "🎉";
    private static final String POOL = "a" + GRIN + "b" + PARTY;

    /** Whether every code point of the text is a whole character, with no surrogate on its own. */
    private static boolean wellFormed(String text) {
        return text.codePoints()
                .noneMatch(codePoint -> codePoint >= Character.MIN_SURROGATE && codePoint <= Character.MAX_SURROGATE);
    }

    private static Set<Integer> codePoints(String text) {
        Set<Integer> codePoints = new HashSet<>();
        text.codePoints().forEach(codePoints::add);
        return codePoints;
    }

    @Test
    @DisplayName("seeded draws from a pool with supplementary characters are pinned")
    void seededSupplementaryDrawsArePinned() {
        StringGenerator strings = StringGenerator.builder()
                .charGenerator(CharGenerator.pool(POOL))
                .length(4)
                .seed(42L)
                .build();
        assertEquals("baba", strings.generate());
        assertEquals(GRIN + PARTY + GRIN + "b", strings.generate());
        assertEquals("ba" + PARTY + GRIN, strings.generate());

        PasswordGenerator passwords =
                new PasswordGenerator(GeneratorConfig.builder().seed(42L).build());
        PasswordPolicy policy = PasswordPolicy.builder()
                .length(5)
                .require(GRIN + PARTY, 2)
                .requireDigits(1)
                .build();
        assertEquals("4" + PARTY + PARTY + "4" + GRIN, passwords.generate(policy));
        assertEquals(GRIN + "266" + GRIN, passwords.generate(policy));
        assertEquals("37" + GRIN + "9" + GRIN, passwords.generate(policy));
    }

    @Test
    @DisplayName("StringGenerator pools draw whole supplementary characters and count each as one")
    void stringGeneratorPoolsDrawWholeCodePoints() {
        Set<Integer> allowed = codePoints(POOL);
        Set<Integer> seen = new HashSet<>();
        StringGenerator seeded = StringGenerator.builder()
                .charGenerator(CharGenerator.pool(POOL))
                .minLength(3)
                .maxLength(7)
                .seed(42L)
                .build();
        StringGenerator fixed = StringGenerator.pool(POOL, 5);
        for (int i = 0; i < 200; i++) {
            String ranged = seeded.generate();
            String exact = fixed.generate();
            assertTrue(wellFormed(ranged), ranged);
            assertTrue(wellFormed(exact), exact);
            int count = ranged.codePointCount(0, ranged.length());
            assertTrue(count >= 3 && count <= 7, ranged);
            assertEquals(5, exact.codePointCount(0, exact.length()), exact);
            assertTrue(allowed.containsAll(codePoints(ranged)), ranged);
            seen.addAll(codePoints(ranged));
        }
        assertEquals(allowed, seen);
    }

    @Test
    @DisplayName("a surrogate pair given as two chars is one character of the pool")
    void charArrayPairIsOneCharacter() {
        StringGenerator generator = StringGenerator.builder()
                .charGenerator(CharGenerator.pool(3L, 'x', '\uD83D', '\uDE00'))
                .length(6)
                .seed(3L)
                .build();
        for (int i = 0; i < 100; i++) {
            String value = generator.generate();
            assertTrue(wellFormed(value), value);
            assertEquals(6, value.codePointCount(0, value.length()), value);
        }
    }

    @Test
    @DisplayName(
            "CharGenerator.generate() refuses a pool with a supplementary character, which a Character cannot hold")
    void charGeneratorRefusesSupplementaryPools() {
        IllegalStateException fromString = assertThrows(
                IllegalStateException.class, () -> CharGenerator.pool(1L, POOL).generate());
        assertEquals(
                "the pool holds U+1F600, which a Character cannot hold; draw strings from it with StringGenerator",
                fromString.getMessage());
        assertThrows(
                IllegalStateException.class,
                () -> CharGenerator.pool(PARTY + "z").generate());
        assertThrows(
                IllegalStateException.class,
                () -> CharGenerator.pool('\uD83C', '\uDF89').withSeed(1L).generate());
    }

    @Test
    @DisplayName("PasswordPolicy.require draws whole supplementary characters and counts each as one")
    void passwordPolicyDrawsWholeCodePoints() {
        PasswordPolicy policy = PasswordPolicy.builder()
                .length(4, 6)
                .require(GRIN + PARTY, 2)
                .requireDigits(1)
                .build();
        PasswordGenerator generator =
                new PasswordGenerator(GeneratorConfig.builder().seed(42L).build());
        for (int i = 0; i < 200; i++) {
            String password = generator.generate(policy);
            assertTrue(wellFormed(password), password);
            int count = password.codePointCount(0, password.length());
            assertTrue(count >= 4 && count <= 6, password);
            long emoji = password.codePoints()
                    .filter(Character::isSupplementaryCodePoint)
                    .count();
            assertTrue(emoji >= 2, password);
            assertTrue(password.chars().anyMatch(Character::isDigit), password);
        }
    }
}
