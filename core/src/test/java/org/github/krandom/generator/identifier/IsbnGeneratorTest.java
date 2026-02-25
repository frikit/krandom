/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.identifier;

import org.github.krandom.generator.Generators;
import org.github.krandom.generator.GeneratorConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("IsbnGenerator")
class IsbnGeneratorTest {

    private static final int SAMPLES = 50;

    // ── Validation helpers ────────────────────────────────────────────────────

    private static boolean isValidIsbn10(String isbn) {
        if (isbn == null || isbn.length() != 10) return false;
        if (!isbn.substring(0, 9).matches("\\d{9}")) return false;
        char last = isbn.charAt(9);
        if (last != 'X' && !Character.isDigit(last)) return false;
        int sum = 0;
        for (int i = 0; i < 9; i++) sum += (10 - i) * (isbn.charAt(i) - '0');
        int check = last == 'X' ? 10 : (last - '0');
        return (sum + check) % 11 == 0;
    }

    private static boolean isValidIsbn13(String isbn) {
        if (isbn == null || isbn.length() != 13) return false;
        if (!isbn.matches("\\d{13}")) return false;
        if (!isbn.startsWith("978") && !isbn.startsWith("979")) return false;
        int sum = 0;
        for (int i = 0; i < 12; i++) sum += (isbn.charAt(i) - '0') * (i % 2 == 0 ? 1 : 3);
        int check = isbn.charAt(12) - '0';
        return (sum + check) % 10 == 0;
    }

    @Nested
    @DisplayName("Constructors")
    class ConstructorTests {

        @Test
        @DisplayName("default constructor uses ISBN_13 type")
        void defaultType() {
            assertEquals(IsbnGenerator.IsbnType.ISBN_13, new IsbnGenerator().getType());
        }

        @Test
        @DisplayName("IsbnType constructor stores type")
        void typeConstructor() {
            assertEquals(IsbnGenerator.IsbnType.ISBN_10,
                    new IsbnGenerator(IsbnGenerator.IsbnType.ISBN_10).getType());
        }

        @Test
        @DisplayName("config constructor uses ISBN_13 type")
        void configConstructor() {
            assertEquals(IsbnGenerator.IsbnType.ISBN_13,
                    new IsbnGenerator(GeneratorConfig.defaults()).getType());
        }

        @Test
        @DisplayName("type+config constructor stores both")
        void typeAndConfigConstructor() {
            GeneratorConfig cfg = GeneratorConfig.builder().seed(1L).build();
            IsbnGenerator gen = new IsbnGenerator(IsbnGenerator.IsbnType.ISBN_10, cfg);
            assertEquals(IsbnGenerator.IsbnType.ISBN_10, gen.getType());
        }

        @Test
        @DisplayName("null type throws NullPointerException")
        void nullTypeThrows() {
            assertThrows(NullPointerException.class,
                    () -> new IsbnGenerator((IsbnGenerator.IsbnType) null));
        }

        @Test
        @DisplayName("null config throws NullPointerException")
        void nullConfigThrows() {
            assertThrows(NullPointerException.class,
                    () -> new IsbnGenerator((GeneratorConfig) null));
        }
    }

    @Nested
    @DisplayName("generate() — ISBN-13")
    class Isbn13GenerateTests {

        @Test
        @DisplayName("generate() returns non-null for ISBN_13")
        void notNull() {
            assertNotNull(new IsbnGenerator().generate());
        }

        @Test
        @DisplayName("generate() produces valid ISBN-13 values")
        void validIsbn13() {
            IsbnGenerator gen = new IsbnGenerator();
            for (int i = 0; i < SAMPLES; i++) {
                String isbn = gen.generate();
                assertTrue(isValidIsbn13(isbn), "Invalid ISBN-13: " + isbn);
            }
        }

        @Test
        @DisplayName("generateIsbn13() produces 13-digit strings starting with 978 or 979")
        void isbn13Prefix() {
            boolean saw978 = false;
            boolean saw979 = false;
            IsbnGenerator gen = new IsbnGenerator();
            for (int i = 0; i < 200; i++) {
                String isbn = gen.generateIsbn13();
                assertTrue(isbn.startsWith("978") || isbn.startsWith("979"));
                if (isbn.startsWith("978")) saw978 = true;
                if (isbn.startsWith("979")) saw979 = true;
                if (saw978 && saw979) break;
            }
            assertTrue(saw978, "Expected at least one ISBN-13 with 978 prefix");
            assertTrue(saw979, "Expected at least one ISBN-13 with 979 prefix");
        }
    }

    @Nested
    @DisplayName("generate() — ISBN-10")
    class Isbn10GenerateTests {

        @Test
        @DisplayName("generate() returns non-null for ISBN_10")
        void notNull() {
            assertNotNull(new IsbnGenerator(IsbnGenerator.IsbnType.ISBN_10).generate());
        }

        @Test
        @DisplayName("generate() produces valid ISBN-10 values")
        void validIsbn10() {
            IsbnGenerator gen = new IsbnGenerator(IsbnGenerator.IsbnType.ISBN_10);
            for (int i = 0; i < 200; i++) {
                String isbn = gen.generate();
                assertTrue(isValidIsbn10(isbn), "Invalid ISBN-10: " + isbn);
            }
        }

        @Test
        @DisplayName("generateIsbn10() produces 'X' check digit for at least one of 200 samples")
        void isbn10XCheckDigit() {
            IsbnGenerator gen = new IsbnGenerator(IsbnGenerator.IsbnType.ISBN_10);
            boolean foundX = false;
            for (int i = 0; i < 200; i++) {
                if (gen.generateIsbn10().endsWith("X")) {
                    foundX = true;
                    break;
                }
            }
            assertTrue(foundX, "Expected at least one ISBN-10 ending in 'X'");
        }
    }

    @Nested
    @DisplayName("computeIsbn10Check")
    class Isbn10CheckTests {

        @Test
        @DisplayName("check digit is 10 (X) for digits [1,0,0,0,0,0,0,0,1]")
        void checkIs10() {
            assertEquals(10, IsbnGenerator.computeIsbn10Check(new int[]{1, 0, 0, 0, 0, 0, 0, 0, 1}));
        }

        @Test
        @DisplayName("check digit is 2 for known ISBN-10 0306406152")
        void checkKnownIsbn10() {
            // 0306406152: digits [0,3,0,6,4,0,6,1,5], check=2
            assertEquals(2, IsbnGenerator.computeIsbn10Check(new int[]{0, 3, 0, 6, 4, 0, 6, 1, 5}));
        }
    }

    @Nested
    @DisplayName("computeIsbn13Check")
    class Isbn13CheckTests {

        @Test
        @DisplayName("check digit is 7 for known ISBN-13 9780306406157")
        void checkKnownIsbn13() {
            // 9780306406157: first 12 digits = [9,7,8,0,3,0,6,4,0,6,1,5], check=7
            assertEquals(7, IsbnGenerator.computeIsbn13Check(
                    new int[]{9, 7, 8, 0, 3, 0, 6, 4, 0, 6, 1, 5}));
        }

        @Test
        @DisplayName("check digit is 0 when sum is divisible by 10")
        void checkIsZero() {
            // All zeros: sum=0, check=(10-0)%10=0
            assertEquals(0, IsbnGenerator.computeIsbn13Check(
                    new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}));
        }
    }

    @Nested
    @DisplayName("Seeded generation")
    class SeededTests {

        @Test
        @DisplayName("seeded ISBN-13 generators produce identical output")
        void seededIsbn13Reproducibility() {
            GeneratorConfig cfg1 = GeneratorConfig.builder().seed(42L).build();
            GeneratorConfig cfg2 = GeneratorConfig.builder().seed(42L).build();
            IsbnGenerator a = new IsbnGenerator(cfg1);
            IsbnGenerator b = new IsbnGenerator(cfg2);
            for (int i = 0; i < SAMPLES; i++) {
                assertEquals(a.generate(), b.generate());
            }
        }

        @Test
        @DisplayName("seeded ISBN-10 generators produce identical output")
        void seededIsbn10Reproducibility() {
            GeneratorConfig cfg1 = GeneratorConfig.builder().seed(99L).build();
            GeneratorConfig cfg2 = GeneratorConfig.builder().seed(99L).build();
            IsbnGenerator a = new IsbnGenerator(IsbnGenerator.IsbnType.ISBN_10, cfg1);
            IsbnGenerator b = new IsbnGenerator(IsbnGenerator.IsbnType.ISBN_10, cfg2);
            for (int i = 0; i < SAMPLES; i++) {
                assertEquals(a.generate(), b.generate());
            }
        }
    }

    @Nested
    @DisplayName("Generators factory")
    class GeneratorsFactoryTest {

        @Test
        @DisplayName("Generators.ofIsbn() returns valid ISBN-13")
        void ofIsbnDefault() {
            String isbn = Generators.ofIsbn().generate();
            assertTrue(isValidIsbn13(isbn), "Expected valid ISBN-13: " + isbn);
        }

        @Test
        @DisplayName("Generators.ofIsbn(ISBN_10) returns valid ISBN-10")
        void ofIsbnType() {
            String isbn = Generators.ofIsbn(IsbnGenerator.IsbnType.ISBN_10).generate();
            assertTrue(isValidIsbn10(isbn), "Expected valid ISBN-10: " + isbn);
        }
    }
}
