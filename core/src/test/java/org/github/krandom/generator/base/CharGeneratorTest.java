/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.base;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Comprehensive test suite for {@link CharGenerator}.
 *
 * <p>Tests cover:
 * <ul>
 *   <li>Factory methods (letters, digits, alphanumeric, all)
 *   <li>Custom pool support (String and varargs)
 *   <li>Seeded custom pools
 *   <li>Builder pattern
 *   <li>Validation (empty pools, null inputs)
 *   <li>Character set correctness
 * </ul>
 */
@DisplayName("CharGenerator")
class CharGeneratorTest {

    @Nested
    @DisplayName("Factory Methods")
    class FactoryMethods {

        @Test
        @DisplayName("letters() should generate only A-Z and a-z")
        void lettersShouldGenerateOnlyLetters() {
            CharGenerator generator = CharGenerator.letters();
            Set<Character> generated = new HashSet<>();

            for (int i = 0; i < 500; i++) {
                char c = generator.generate();
                generated.add(c);
                assertTrue(Character.isLetter(c), "Should only generate letters, got: " + c);
            }

            // Should see both uppercase and lowercase eventually
            assertTrue(generated.stream().anyMatch(Character::isUpperCase));
            assertTrue(generated.stream().anyMatch(Character::isLowerCase));
        }

        @Test
        @DisplayName("digits() should generate only 0-9")
        void digitsShouldGenerateOnlyDigits() {
            CharGenerator generator = CharGenerator.digits();
            Set<Character> generated = new HashSet<>();

            for (int i = 0; i < 100; i++) {
                char c = generator.generate();
                generated.add(c);
                assertTrue(Character.isDigit(c), "Should only generate digits, got: " + c);
                assertTrue(c >= '0' && c <= '9', "Should be 0-9, got: " + c);
            }

            // Should see all digits eventually
            assertEquals(10, generated.size(), "Should generate all 10 digits");
        }

        @Test
        @DisplayName("alphanumeric() should generate A-Z, a-z, 0-9")
        void alphanumericShouldGenerateLettersAndDigits() {
            CharGenerator generator = CharGenerator.alphanumeric();
            Set<Character> generated = new HashSet<>();

            for (int i = 0; i < 1000; i++) {
                char c = generator.generate();
                generated.add(c);
                assertTrue(Character.isLetterOrDigit(c),
                           "Should only generate letters or digits, got: " + c);
            }

            // Should see uppercase, lowercase, and digits
            assertTrue(generated.stream().anyMatch(Character::isUpperCase));
            assertTrue(generated.stream().anyMatch(Character::isLowerCase));
            assertTrue(generated.stream().anyMatch(Character::isDigit));
        }

        @Test
        @DisplayName("all() should generate letters, digits, and special chars")
        void allShouldGenerateAllCharacterTypes() {
            CharGenerator generator = CharGenerator.all();
            Set<Character> generated = new HashSet<>();

            for (int i = 0; i < 2000; i++) {
                generated.add(generator.generate());
            }

            // Should see all types
            assertTrue(generated.stream().anyMatch(Character::isUpperCase));
            assertTrue(generated.stream().anyMatch(Character::isLowerCase));
            assertTrue(generated.stream().anyMatch(Character::isDigit));
            // Special chars are not letters or digits
            assertTrue(generated.stream().anyMatch(c -> !Character.isLetterOrDigit(c)));
        }
    }


    @Nested
    @DisplayName("Custom Pool - String")
    class CustomPoolString {

        @Test
        @DisplayName("pool(String) should generate only from given characters")
        void poolStringShouldUseGivenCharacters() {
            CharGenerator vowels = CharGenerator.pool("aeiou");
            Set<Character> generated = new HashSet<>();

            for (int i = 0; i < 100; i++) {
                char c = vowels.generate();
                generated.add(c);
                assertTrue("aeiou".indexOf(c) >= 0, "Should be vowel, got: " + c);
            }

            assertEquals(5, generated.size(), "Should generate all 5 vowels");
        }

        @Test
        @DisplayName("pool(String) with hex digits")
        void poolWithHexDigits() {
            CharGenerator hex = CharGenerator.pool("0123456789ABCDEF");
            Set<Character> generated = new HashSet<>();

            for (int i = 0; i < 200; i++) {
                char c = hex.generate();
                generated.add(c);
                assertTrue("0123456789ABCDEF".indexOf(c) >= 0,
                           "Should be hex digit, got: " + c);
            }

            assertEquals(16, generated.size(), "Should generate all 16 hex digits");
        }

        @Test
        @DisplayName("pool(String) with consonants")
        void poolWithConsonants() {
            String consonants = "bcdfghjklmnpqrstvwxyz";
            CharGenerator gen = CharGenerator.pool(consonants);

            for (int i = 0; i < 100; i++) {
                char c = gen.generate();
                assertTrue(consonants.indexOf(c) >= 0,
                           "Should be consonant, got: " + c);
            }
        }

        @Test
        @DisplayName("pool(String) with single character")
        void poolWithSingleCharacter() {
            CharGenerator gen = CharGenerator.pool("X");

            for (int i = 0; i < 10; i++) {
                assertEquals('X', gen.generate(), "Should always return X");
            }
        }

        @Test
        @DisplayName("pool(String) with duplicate characters")
        void poolWithDuplicateCharacters() {
            CharGenerator gen = CharGenerator.pool("aaa");

            for (int i = 0; i < 10; i++) {
                assertEquals('a', gen.generate(), "Should return a");
            }
        }

        @Test
        @DisplayName("pool(String) should reject null")
        void poolShouldRejectNull() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                                                       () -> CharGenerator.pool((String) null));

            assertTrue(ex.getMessage().contains("must not be null"));
        }

        @Test
        @DisplayName("pool(String) should reject empty string")
        void poolShouldRejectEmptyString() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                                                       () -> CharGenerator.pool(""));

            assertTrue(ex.getMessage().contains("must not be null or empty"));
        }
    }


    @Nested
    @DisplayName("Custom Pool - Varargs")
    class CustomPoolVarargs {

        @Test
        @DisplayName("pool(char...) should generate only from given characters")
        void poolVarargsShouldUseGivenCharacters() {
            CharGenerator binary = CharGenerator.pool('0', '1');
            Set<Character> generated = new HashSet<>();

            for (int i = 0; i < 50; i++) {
                char c = binary.generate();
                generated.add(c);
                assertTrue(c == '0' || c == '1', "Should be 0 or 1, got: " + c);
            }

            assertEquals(2, generated.size(), "Should generate both 0 and 1");
        }

        @Test
        @DisplayName("pool(char...) with arrows")
        void poolWithArrows() {
            CharGenerator arrows = CharGenerator.pool('←', '↑', '→', '↓');
            Set<Character> generated = new HashSet<>();

            for (int i = 0; i < 100; i++) {
                char c = arrows.generate();
                generated.add(c);
                assertTrue(c == '←' || c == '↑' || c == '→' || c == '↓',
                           "Should be arrow, got: " + c);
            }

            assertEquals(4, generated.size(), "Should generate all 4 arrows");
        }

        @Test
        @DisplayName("pool(char...) with single character")
        void poolVarargsWithSingleCharacter() {
            CharGenerator gen = CharGenerator.pool('Z');

            for (int i = 0; i < 10; i++) {
                assertEquals('Z', gen.generate(), "Should always return Z");
            }
        }

        @Test
        @DisplayName("pool(char...) should reject null")
        void poolVarargsShouldRejectNull() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                                                       () -> CharGenerator.pool((char[]) null));

            assertTrue(ex.getMessage().contains("must not be null"));
        }

        @Test
        @DisplayName("pool(char...) should reject empty array")
        void poolVarargsShouldRejectEmptyArray() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                                                       () -> CharGenerator.pool(new char[0]));

            assertTrue(ex.getMessage().contains("must not be null or empty"));
        }
    }


    @Nested
    @DisplayName("Seeded Custom Pool")
    class SeededCustomPool {

        @Test
        @DisplayName("seeded pool(String) should be deterministic")
        void seededPoolStringShouldBeDeterministic() {
            long seed = 42L;
            CharGenerator gen1 = CharGenerator.pool(seed, "aeiou");
            CharGenerator gen2 = CharGenerator.pool(seed, "aeiou");

            List<Character> list1 = gen1.generateList(50);
            List<Character> list2 = gen2.generateList(50);

            assertEquals(list1, list2, "Same seed should produce same sequence");
        }

        @Test
        @DisplayName("seeded pool(char...) should be deterministic")
        void seededPoolVarargsShouldBeDeterministic() {
            long seed = 123L;
            CharGenerator gen1 = CharGenerator.pool(seed, '0', '1');
            CharGenerator gen2 = CharGenerator.pool(seed, '0', '1');

            List<Character> list1 = gen1.generateList(50);
            List<Character> list2 = gen2.generateList(50);

            assertEquals(list1, list2, "Same seed should produce same sequence");
        }

        @Test
        @DisplayName("different seeds should produce different sequences")
        void differentSeedsShouldProduceDifferentSequences() {
            CharGenerator gen1 = CharGenerator.pool(111L, "xyz");
            CharGenerator gen2 = CharGenerator.pool(222L, "xyz");

            List<Character> list1 = gen1.generateList(50);
            List<Character> list2 = gen2.generateList(50);

            assertNotEquals(list1, list2, "Different seeds should differ");
        }

        @Test
        @DisplayName("seeded pool(String) should reject null characters")
        void seededPoolStringShouldRejectNull() {
            assertThrows(IllegalArgumentException.class,
                         () -> CharGenerator.pool(42L, (String) null));
        }

        @Test
        @DisplayName("seeded pool(String) should reject empty string")
        void seededPoolStringShouldRejectEmptyString() {
            assertThrows(IllegalArgumentException.class,
                         () -> CharGenerator.pool(42L, ""));
        }

        @Test
        @DisplayName("seeded pool(char...) should reject null array")
        void seededPoolVarargsShouldRejectNull() {
            assertThrows(IllegalArgumentException.class,
                         () -> CharGenerator.pool(42L, (char[]) null));
        }

        @Test
        @DisplayName("seeded pool(char...) should reject empty array")
        void seededPoolVarargsShouldRejectEmptyArray() {
            assertThrows(IllegalArgumentException.class,
                         () -> CharGenerator.pool(42L, new char[0]));
        }
    }


    @Nested
    @DisplayName("Builder Pattern")
    class BuilderPattern {

        @Test
        @DisplayName("builder with uppercase only")
        void builderWithUppercaseOnly() {
            CharGenerator gen = CharGenerator.builder().uppercase().build();

            for (int i = 0; i < 50; i++) {
                char c = gen.generate();
                assertTrue(Character.isUpperCase(c), "Should be uppercase, got: " + c);
                assertTrue(c >= 'A' && c <= 'Z', "Should be A-Z, got: " + c);
            }
        }

        @Test
        @DisplayName("builder with lowercase only")
        void builderWithLowercaseOnly() {
            CharGenerator gen = CharGenerator.builder().lowercase().build();

            for (int i = 0; i < 50; i++) {
                char c = gen.generate();
                assertTrue(Character.isLowerCase(c), "Should be lowercase, got: " + c);
                assertTrue(c >= 'a' && c <= 'z', "Should be a-z, got: " + c);
            }
        }

        @Test
        @DisplayName("builder with digits only")
        void builderWithDigitsOnly() {
            CharGenerator gen = CharGenerator.builder().digits().build();

            for (int i = 0; i < 50; i++) {
                char c = gen.generate();
                assertTrue(Character.isDigit(c), "Should be digit, got: " + c);
            }
        }

        @Test
        @DisplayName("builder with special only")
        void builderWithSpecialOnly() {
            CharGenerator gen = CharGenerator.builder().special().build();

            for (int i = 0; i < 50; i++) {
                char c = gen.generate();
                assertFalse(Character.isLetterOrDigit(c),
                            "Should not be letter or digit, got: " + c);
            }
        }

        @Test
        @DisplayName("builder with seeded generation")
        void builderWithSeed() {
            long seed = 999L;
            CharGenerator gen1 = CharGenerator.builder().uppercase().seed(seed).build();
            CharGenerator gen2 = CharGenerator.builder().uppercase().seed(seed).build();

            List<Character> list1 = gen1.generateList(50);
            List<Character> list2 = gen2.generateList(50);

            assertEquals(list1, list2, "Same seed should produce same sequence");
        }

        @Test
        @DisplayName("builder should reject empty configuration")
        void builderShouldRejectEmptyConfiguration() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                                                       () -> CharGenerator.builder().build());

            assertTrue(ex.getMessage().contains("At least one character group"));
        }

        @Test
        @DisplayName("builder with all groups")
        void builderWithAllGroups() {
            CharGenerator gen = CharGenerator.builder()
                                             .uppercase()
                                             .lowercase()
                                             .digits()
                                             .special()
                                             .build();

            Set<Character> generated = new HashSet<>();
            for (int i = 0; i < 2000; i++) {
                generated.add(gen.generate());
            }

            // Should see all types
            assertTrue(generated.stream().anyMatch(Character::isUpperCase));
            assertTrue(generated.stream().anyMatch(Character::isLowerCase));
            assertTrue(generated.stream().anyMatch(Character::isDigit));
            assertTrue(generated.stream().anyMatch(c -> !Character.isLetterOrDigit(c)));
        }
    }


    @Nested
    @DisplayName("Integration Tests")
    class Integration {

        @Test
        @DisplayName("should work with stream operations")
        void shouldWorkWithStreamOperations() {
            CharGenerator vowels = CharGenerator.pool("aeiou");

            List<Character> chars = vowels.stream()
                                          .limit(100)
                                          .distinct()
                                          .toList();

            assertTrue(chars.size() <= 5, "Should have at most 5 distinct vowels");
            assertTrue(chars.stream().allMatch(c -> "aeiou".indexOf(c) >= 0));
        }

        @Test
        @DisplayName("should work with generateList")
        void shouldWorkWithGenerateList() {
            CharGenerator hex = CharGenerator.pool("0123456789ABCDEF");
            List<Character> chars = hex.generateList(100);

            assertEquals(100, chars.size());
            assertTrue(chars.stream().allMatch(c -> "0123456789ABCDEF".indexOf(c) >= 0));
        }

        @Test
        @DisplayName("multiple generators should be independent")
        void multipleGeneratorsShouldBeIndependent() {
            CharGenerator vowels = CharGenerator.pool("aeiou");
            CharGenerator consonants = CharGenerator.pool("bcdfg");

            for (int i = 0; i < 10; i++) {
                char v = vowels.generate();
                char c = consonants.generate();

                assertTrue("aeiou".indexOf(v) >= 0);
                assertTrue("bcdfg".indexOf(c) >= 0);
            }
        }
    }


    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {

        @Test
        @DisplayName("pool with Unicode characters")
        void poolWithUnicodeCharacters() {
            CharGenerator gen = CharGenerator.pool("αβγδε"); // Greek letters

            for (int i = 0; i < 50; i++) {
                char c = gen.generate();
                assertTrue("αβγδε".indexOf(c) >= 0, "Should be Greek letter, got: " + c);
            }
        }

        @Test
        @DisplayName("pool with emojis should work")
        void poolWithEmojis() {
            // Note: emojis are multi-char in Java (surrogate pairs), so single char works
            CharGenerator gen = CharGenerator.pool('☺', '♥', '★');

            for (int i = 0; i < 10; i++) {
                char c = gen.generate();
                assertTrue(c == '☺' || c == '♥' || c == '★');
            }
        }

        @Test
        @DisplayName("empty list generation should work")
        void emptyListGeneration() {
            CharGenerator gen = CharGenerator.letters();
            List<Character> chars = gen.generateList(0);

            assertEquals(0, chars.size());
        }
    }
}
