/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.base;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Comprehensive test suite for {@link StringGenerator}.
 *
 * <p>Tests cover:
 * <ul>
 *   <li>Factory methods (letters, digits, alphanumeric, all)
 *   <li>Custom pool support (variable and fixed length)
 *   <li>Builder pattern
 *   <li>Length control (fixed, min/max)
 *   <li>Seeded generation
 *   <li>Validation (invalid lengths, null inputs)
 * </ul>
 */
@DisplayName("StringGenerator")
class StringGeneratorTest {

    @Nested
    @DisplayName("Factory Methods")
    class FactoryMethods {

        @Test
        @DisplayName("letters() should generate only letter strings")
        void lettersShouldGenerateOnlyLetters() {
            StringGenerator generator = StringGenerator.letters();

            for (int i = 0; i < 50; i++) {
                String s = generator.generate();
                assertTrue(s.length() >= 5 && s.length() <= 20,
                           "Length should be 5-20, got: " + s.length());
                assertTrue(s.chars().allMatch(Character::isLetter),
                           "Should contain only letters, got: " + s);
            }
        }

        @Test
        @DisplayName("letters(min, max) should honor the requested length range")
        void lettersRangeShouldGenerateOnlyLettersWithinBounds() {
            StringGenerator generator = StringGenerator.letters(2, 4);

            for (int i = 0; i < 50; i++) {
                String s = generator.generate();
                assertTrue(s.length() >= 2 && s.length() <= 4,
                           "Length should be 2-4, got: " + s.length());
                assertTrue(s.chars().allMatch(Character::isLetter),
                           "Should contain only letters, got: " + s);
            }
        }

        @Test
        @DisplayName("digits() should generate only digit strings")
        void digitsShouldGenerateOnlyDigits() {
            StringGenerator generator = StringGenerator.digits();

            for (int i = 0; i < 50; i++) {
                String s = generator.generate();
                assertTrue(s.length() >= 5 && s.length() <= 20);
                assertTrue(s.chars().allMatch(Character::isDigit),
                           "Should contain only digits, got: " + s);
            }
        }

        @Test
        @DisplayName("alphanumeric() should generate alphanumeric strings")
        void alphanumericShouldGenerateAlphanumeric() {
            StringGenerator generator = StringGenerator.alphanumeric();

            for (int i = 0; i < 50; i++) {
                String s = generator.generate();
                assertTrue(s.length() >= 5 && s.length() <= 20);
                assertTrue(s.chars().allMatch(Character::isLetterOrDigit),
                           "Should contain only letters or digits, got: " + s);
            }
        }

        @Test
        @DisplayName("all() should generate strings with all character types")
        void allShouldGenerateAllCharacterTypes() {
            StringGenerator generator = StringGenerator.all();
            Set<String> generated = new HashSet<>();

            for (int i = 0; i < 100; i++) {
                String s = generator.generate();
                generated.add(s);
                assertTrue(s.length() >= 5 && s.length() <= 20);
            }

            // Should see variety
            assertTrue(generated.size() > 80, "Should generate diverse strings");
        }
    }


    @Nested
    @DisplayName("Custom Pool - Variable Length")
    class CustomPoolVariableLength {

        @Test
        @DisplayName("pool(String) should use custom characters with default length")
        void poolShouldUseCustomCharacters() {
            StringGenerator vowels = StringGenerator.pool("aeiou");

            for (int i = 0; i < 50; i++) {
                String s = vowels.generate();
                assertTrue(s.length() >= 5 && s.length() <= 20,
                           "Length should be 5-20, got: " + s.length());
                assertTrue(s.chars().allMatch(c -> "aeiou".indexOf((char) c) >= 0),
                           "Should contain only vowels, got: " + s);
            }
        }

        @Test
        @DisplayName("pool(String) with hex characters")
        void poolWithHexCharacters() {
            StringGenerator hex = StringGenerator.pool("0123456789ABCDEF");

            for (int i = 0; i < 50; i++) {
                String s = hex.generate();
                assertTrue(s.length() >= 5 && s.length() <= 20);
                assertTrue(s.chars().allMatch(c -> "0123456789ABCDEF".indexOf((char) c) >= 0),
                           "Should contain only hex digits, got: " + s);
            }
        }

        @Test
        @DisplayName("pool(String) with binary")
        void poolWithBinary() {
            StringGenerator binary = StringGenerator.pool("01");

            for (int i = 0; i < 30; i++) {
                String s = binary.generate();
                assertTrue(s.length() >= 5 && s.length() <= 20);
                assertTrue(s.chars().allMatch(c -> c == '0' || c == '1'),
                           "Should contain only 0 or 1, got: " + s);
            }
        }

        @Test
        @DisplayName("pool(String) should reject null")
        void poolShouldRejectNull() {
            assertThrows(IllegalArgumentException.class,
                         () -> StringGenerator.pool(null));
        }

        @Test
        @DisplayName("pool(String) should reject empty string")
        void poolShouldRejectEmptyString() {
            assertThrows(IllegalArgumentException.class,
                         () -> StringGenerator.pool(""));
        }
    }


    @Nested
    @DisplayName("Custom Pool - Fixed Length")
    class CustomPoolFixedLength {

        @Test
        @DisplayName("pool(String, length) should generate fixed-length strings")
        void poolWithLengthShouldGenerateFixedLength() {
            StringGenerator gen = StringGenerator.pool("ABC123", 8);

            for (int i = 0; i < 50; i++) {
                String s = gen.generate();
                assertEquals(8, s.length(), "Should always be 8 characters");
                assertTrue(s.chars().allMatch(c -> "ABC123".indexOf((char) c) >= 0),
                           "Should contain only pool characters, got: " + s);
            }
        }

        @Test
        @DisplayName("pool(String, length) with length 1")
        void poolWithLength1() {
            StringGenerator gen = StringGenerator.pool("xyz", 1);

            for (int i = 0; i < 20; i++) {
                String s = gen.generate();
                assertEquals(1, s.length());
                assertTrue("xyz".contains(s));
            }
        }

        @Test
        @DisplayName("pool(String, length) with large length")
        void poolWithLargeLength() {
            StringGenerator gen = StringGenerator.pool("01", 100);
            String s = gen.generate();

            assertEquals(100, s.length());
            assertTrue(s.chars().allMatch(c -> c == '0' || c == '1'));
        }

        @Test
        @DisplayName("pool(String, length) should reject null characters")
        void poolWithLengthShouldRejectNull() {
            assertThrows(IllegalArgumentException.class,
                         () -> StringGenerator.pool(null, 10));
        }

        @Test
        @DisplayName("pool(String, length) should reject empty characters")
        void poolWithLengthShouldRejectEmpty() {
            assertThrows(IllegalArgumentException.class,
                         () -> StringGenerator.pool("", 10));
        }

        @Test
        @DisplayName("pool(String, length) should reject length < 1")
        void poolWithLengthShouldRejectInvalidLength() {
            assertThrows(IllegalArgumentException.class,
                         () -> StringGenerator.pool("abc", 0));
            assertThrows(IllegalArgumentException.class,
                         () -> StringGenerator.pool("abc", -1));
        }
    }


    @Nested
    @DisplayName("Custom Pool - Min/Max Length")
    class CustomPoolMinMaxLength {

        @Test
        @DisplayName("pool(String, min, max) should generate variable-length strings")
        void poolWithMinMaxShouldGenerateVariableLength() {
            StringGenerator gen = StringGenerator.pool("ABCD", 4, 8);
            Set<Integer> lengths = new HashSet<>();

            for (int i = 0; i < 100; i++) {
                String s = gen.generate();
                lengths.add(s.length());
                assertTrue(s.length() >= 4 && s.length() <= 8,
                           "Length should be 4-8, got: " + s.length());
                assertTrue(s.chars().allMatch(c -> "ABCD".indexOf((char) c) >= 0));
            }

            // Should see multiple lengths
            assertTrue(lengths.size() >= 2, "Should generate various lengths");
        }

        @Test
        @DisplayName("pool(String, min, max) with min == max")
        void poolWithMinEqualsMax() {
            StringGenerator gen = StringGenerator.pool("xyz", 5, 5);

            for (int i = 0; i < 20; i++) {
                assertEquals(5, gen.generate().length());
            }
        }

        @Test
        @DisplayName("pool(String, min, max) should reject null")
        void poolMinMaxShouldRejectNull() {
            assertThrows(IllegalArgumentException.class,
                         () -> StringGenerator.pool(null, 1, 10));
        }

        @Test
        @DisplayName("pool(String, min, max) should reject empty")
        void poolMinMaxShouldRejectEmpty() {
            assertThrows(IllegalArgumentException.class,
                         () -> StringGenerator.pool("", 1, 10));
        }

        @Test
        @DisplayName("pool(String, min, max) should reject invalid min")
        void poolMinMaxShouldRejectInvalidMin() {
            assertThrows(IllegalArgumentException.class,
                         () -> StringGenerator.pool("abc", 0, 10));
            assertThrows(IllegalArgumentException.class,
                         () -> StringGenerator.pool("abc", -1, 10));
        }

        @Test
        @DisplayName("pool(String, min, max) should reject max < min")
        void poolMinMaxShouldRejectMaxLessThanMin() {
            assertThrows(IllegalArgumentException.class,
                         () -> StringGenerator.pool("abc", 10, 5));
        }
    }


    @Nested
    @DisplayName("Builder Pattern")
    class BuilderPattern {

        @Test
        @DisplayName("builder with fixed length")
        void builderWithFixedLength() {
            StringGenerator gen = StringGenerator.builder()
                                                 .length(10)
                                                 .charGenerator(CharGenerator.digits())
                                                 .build();

            for (int i = 0; i < 20; i++) {
                String s = gen.generate();
                assertEquals(10, s.length());
                assertTrue(s.chars().allMatch(Character::isDigit));
            }
        }

        @Test
        @DisplayName("builder with min and max length")
        void builderWithMinMaxLength() {
            StringGenerator gen = StringGenerator.builder()
                                                 .minLength(3)
                                                 .maxLength(7)
                                                 .charGenerator(CharGenerator.letters())
                                                 .build();

            for (int i = 0; i < 50; i++) {
                String s = gen.generate();
                assertTrue(s.length() >= 3 && s.length() <= 7);
                assertTrue(s.chars().allMatch(Character::isLetter));
            }
        }

        @Test
        @DisplayName("builder with seeded generation")
        void builderWithSeed() {
            long seed = 42L;
            StringGenerator gen1 = StringGenerator.builder()
                                                  .length(10)
                                                  .seed(seed)
                                                  .build();
            StringGenerator gen2 = StringGenerator.builder()
                                                  .length(10)
                                                  .seed(seed)
                                                  .build();

            List<String> list1 = gen1.generateList(50);
            List<String> list2 = gen2.generateList(50);

            assertEquals(list1, list2, "Same seed should produce same strings");
        }

        @Test
        @DisplayName("builder should use default charGenerator")
        void builderShouldUseDefaultCharGenerator() {
            StringGenerator gen = StringGenerator.builder().length(10).build();
            String s = gen.generate();

            assertEquals(10, s.length());
            assertTrue(s.chars().allMatch(Character::isLetter),
                       "Default should be letters");
        }

        @Test
        @DisplayName("builder should reject null charGenerator")
        void builderShouldRejectNullCharGenerator() {
            assertThrows(NullPointerException.class,
                         () -> StringGenerator.builder().charGenerator(null));
        }

        @Test
        @DisplayName("builder should reject length < 1")
        void builderShouldRejectInvalidLength() {
            assertThrows(IllegalArgumentException.class,
                         () -> StringGenerator.builder().length(0).build());
            assertThrows(IllegalArgumentException.class,
                         () -> StringGenerator.builder().length(-1).build());
        }

        @Test
        @DisplayName("builder should reject minLength < 1")
        void builderShouldRejectInvalidMinLength() {
            assertThrows(IllegalArgumentException.class,
                         () -> StringGenerator.builder().minLength(0).build());
        }

        @Test
        @DisplayName("builder should reject maxLength < 1")
        void builderShouldRejectInvalidMaxLength() {
            assertThrows(IllegalArgumentException.class,
                         () -> StringGenerator.builder().maxLength(0).build());
        }

        @Test
        @DisplayName("builder should reject maxLength < minLength")
        void builderShouldRejectMaxLessThanMin() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                                                       () -> StringGenerator.builder()
                                                                            .minLength(10)
                                                                            .maxLength(5)
                                                                            .build());

            assertTrue(ex.getMessage().contains("maxLength"));
            assertTrue(ex.getMessage().contains("minLength"));
        }
    }


    @Nested
    @DisplayName("Seeded Generation")
    class SeededGeneration {

        @Test
        @DisplayName("seeded generators should be deterministic")
        void seededGeneratorsShouldBeDeterministic() {
            long seed = 123L;
            StringGenerator gen1 = StringGenerator.builder()
                                                  .charGenerator(CharGenerator.alphanumeric())
                                                  .minLength(8)
                                                  .maxLength(12)
                                                  .seed(seed)
                                                  .build();
            StringGenerator gen2 = StringGenerator.builder()
                                                  .charGenerator(CharGenerator.alphanumeric())
                                                  .minLength(8)
                                                  .maxLength(12)
                                                  .seed(seed)
                                                  .build();

            List<String> list1 = gen1.generateList(100);
            List<String> list2 = gen2.generateList(100);

            assertEquals(list1, list2, "Same seed should produce identical sequences");
        }

        @Test
        @DisplayName("different seeds should produce different strings")
        void differentSeedsShouldProduceDifferentStrings() {
            StringGenerator gen1 = StringGenerator.builder()
                                                  .length(20)
                                                  .seed(111L)
                                                  .build();
            StringGenerator gen2 = StringGenerator.builder()
                                                  .length(20)
                                                  .seed(222L)
                                                  .build();

            List<String> list1 = gen1.generateList(50);
            List<String> list2 = gen2.generateList(50);

            assertNotEquals(list1, list2, "Different seeds should differ");
        }

        @Test
        @DisplayName("seeded generator preserves digits-only char source")
        void seededGeneratorPreservesDigits() {
            StringGenerator gen = StringGenerator.builder()
                                                 .charGenerator(CharGenerator.digits())
                                                 .length(24)
                                                 .seed(42L)
                                                 .build();

            for (String s : gen.generateList(40)) {
                assertTrue(s.chars().allMatch(Character::isDigit),
                           "Expected digits-only output, got: " + s);
            }
        }

        @Test
        @DisplayName("seeded generator preserves custom pool char source")
        void seededGeneratorPreservesCustomPool() {
            StringGenerator gen = StringGenerator.builder()
                                                 .charGenerator(CharGenerator.pool("XYZ"))
                                                 .length(30)
                                                 .seed(7L)
                                                 .build();

            for (String s : gen.generateList(40)) {
                assertTrue(s.chars().allMatch(c -> "XYZ".indexOf((char) c) >= 0),
                           "Expected custom-pool output, got: " + s);
            }
        }
    }


    @Nested
    @DisplayName("Integration Tests")
    class Integration {

        @Test
        @DisplayName("should work with stream operations")
        void shouldWorkWithStreamOperations() {
            StringGenerator gen = StringGenerator.pool("ABC", 5);

            List<String> strings = gen.stream()
                                      .limit(50)
                                      .toList();

            assertEquals(50, strings.size());
            assertTrue(strings.stream().allMatch(s -> s.length() == 5));
            assertTrue(strings.stream().allMatch(s ->
                                                     s.chars().allMatch(c -> "ABC".indexOf((char) c) >= 0)));
        }

        @Test
        @DisplayName("should work with generateList")
        void shouldWorkWithGenerateList() {
            StringGenerator gen = StringGenerator.digits();
            List<String> strings = gen.generateList(100);

            assertEquals(100, strings.size());
            assertTrue(strings.stream().allMatch(s ->
                                                     s.chars().allMatch(Character::isDigit)));
        }

        @Test
        @DisplayName("multiple generators should be independent")
        void multipleGeneratorsShouldBeIndependent() {
            StringGenerator letters = StringGenerator.letters();
            StringGenerator digits = StringGenerator.digits();

            for (int i = 0; i < 10; i++) {
                String l = letters.generate();
                String d = digits.generate();

                assertTrue(l.chars().allMatch(Character::isLetter));
                assertTrue(d.chars().allMatch(Character::isDigit));
            }
        }
    }


    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {

        @Test
        @DisplayName("length 1 should work")
        void length1ShouldWork() {
            StringGenerator gen = StringGenerator.builder().length(1).build();
            String s = gen.generate();

            assertEquals(1, s.length());
        }

        @Test
        @DisplayName("very long strings should work")
        void veryLongStringsShouldWork() {
            StringGenerator gen = StringGenerator.builder()
                                                 .length(1000)
                                                 .charGenerator(CharGenerator.digits())
                                                 .build();
            String s = gen.generate();

            assertEquals(1000, s.length());
            assertTrue(s.chars().allMatch(Character::isDigit));
        }

        @Test
        @DisplayName("single character pool should work")
        void singleCharacterPoolShouldWork() {
            StringGenerator gen = StringGenerator.pool("X", 10);
            String s = gen.generate();

            assertEquals("XXXXXXXXXX", s);
        }

        @Test
        @DisplayName("empty list generation should work")
        void emptyListGenerationShouldWork() {
            StringGenerator gen = StringGenerator.letters();
            List<String> strings = gen.generateList(0);

            assertEquals(0, strings.size());
        }

        @Test
        @DisplayName("pool with Unicode characters")
        void poolWithUnicodeCharacters() {
            StringGenerator gen = StringGenerator.pool("αβγδε", 10);
            String s = gen.generate();

            assertEquals(10, s.length());
            assertTrue(s.chars().allMatch(c -> "αβγδε".indexOf((char) c) >= 0));
        }
    }
}
