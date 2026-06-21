/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.text;

import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.Generators;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("NatoPhoneticGenerator")
class NatoPhoneticGeneratorTest {

    private static final Set<String> WORDS = Set.of(
        "Alfa", "Bravo", "Charlie", "Delta", "Echo", "Foxtrot", "Golf", "Hotel", "India",
        "Juliett", "Kilo", "Lima", "Mike", "November", "Oscar", "Papa", "Quebec", "Romeo",
        "Sierra", "Tango", "Uniform", "Victor", "Whiskey", "X-ray", "Yankee", "Zulu");

    @RepeatedTest(200)
    @DisplayName("generate() returns a valid code word")
    void generateValid() {
        assertTrue(WORDS.contains(new NatoPhoneticGenerator().generate()));
    }

    @Test
    @DisplayName("generate() can surface every code word over many draws")
    void generateCoversAll() {
        NatoPhoneticGenerator gen = new NatoPhoneticGenerator(GeneratorConfig.builder().seed(11L).build());
        Set<String> seen = new HashSet<>();
        for (int i = 0; i < 5000; i++) {
            seen.add(gen.generate());
        }
        assertEquals(WORDS, seen);
    }

    @Test
    @DisplayName("same seed is reproducible")
    void reproducible() {
        List<String> a = new NatoPhoneticGenerator(GeneratorConfig.builder().seed(5L).build()).generateList(40);
        List<String> b = new NatoPhoneticGenerator(GeneratorConfig.builder().seed(5L).build()).generateList(40);
        assertEquals(a, b);
    }

    @Test
    @DisplayName("wordFor maps letters case-insensitively")
    void wordForLetters() {
        NatoPhoneticGenerator gen = new NatoPhoneticGenerator();
        assertEquals("Alfa", gen.wordFor('a'));
        assertEquals("Alfa", gen.wordFor('A'));
        assertEquals("Zulu", gen.wordFor('z'));
        assertEquals("Zulu", gen.wordFor('Z'));
    }

    @Test
    @DisplayName("wordFor rejects non-letters on both sides of the range")
    void wordForRejectsNonLetters() {
        NatoPhoneticGenerator gen = new NatoPhoneticGenerator();
        assertThrows(IllegalArgumentException.class, () -> gen.wordFor('1')); // below 'A'
        assertThrows(IllegalArgumentException.class, () -> gen.wordFor('[')); // above 'Z'
        assertThrows(IllegalArgumentException.class, () -> gen.wordFor(' '));
    }

    @Test
    @DisplayName("spell renders letters and skips everything else")
    void spell() {
        NatoPhoneticGenerator gen = new NatoPhoneticGenerator();
        assertEquals("Alfa Bravo", gen.spell("AB-12"));
        assertEquals("Alfa Bravo", gen.spell("a b"));
        assertEquals("Alfa Bravo", gen.spell("A[B")); // '[' is >= 'A' but past 'Z' -> skipped
        assertEquals("", gen.spell(""));
        assertEquals("", gen.spell("-123!"));
    }

    @Test
    @DisplayName("null arguments are rejected")
    void nullsRejected() {
        assertThrows(NullPointerException.class, () -> new NatoPhoneticGenerator(null));
        assertThrows(NullPointerException.class, () -> new NatoPhoneticGenerator().spell(null));
    }

    @Test
    @DisplayName("facade ofNatoPhonetic produces valid words")
    void facade() {
        assertTrue(WORDS.contains(Generators.ofNatoPhonetic().generate()));
        assertTrue(WORDS.contains(
            Generators.ofNatoPhonetic(GeneratorConfig.builder().seed(1L).build()).generate()));
    }
}
