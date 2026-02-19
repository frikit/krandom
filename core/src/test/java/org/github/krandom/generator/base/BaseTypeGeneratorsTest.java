package org.github.krandom.generator.base;

import org.github.krandom.generator.Generator;
import org.github.krandom.generator.Generators;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Base-type generators")
class BaseTypeGeneratorsTest {

    private static final int SAMPLES = 200;

    // ── ByteGenerator ─────────────────────────────────────────────────────────

    @Nested
    @DisplayName("ByteGenerator")
    class ByteGeneratorTest {

        @Test
        @DisplayName("default range covers [-128, 127)")
        void defaultRange() {
            ByteGenerator gen = new ByteGenerator();
            for (int i = 0; i < SAMPLES; i++) {
                byte v = gen.generate();
                assertTrue(v >= Byte.MIN_VALUE && v < Byte.MAX_VALUE,
                        "Expected value in [-128, 127), got: " + v);
            }
        }

        @Test
        @DisplayName("custom range [0, 50)")
        void customRange() {
            ByteGenerator gen = new ByteGenerator((byte) 0, (byte) 50);
            for (int i = 0; i < SAMPLES; i++) {
                byte v = gen.generate();
                assertTrue(v >= 0 && v < 50, "Expected [0, 50), got: " + v);
            }
        }

        @Test
        @DisplayName("same seed produces identical sequence")
        void seededReproducibility() {
            ByteGenerator a = new ByteGenerator((byte) -10, (byte) 10, 42L);
            ByteGenerator b = new ByteGenerator((byte) -10, (byte) 10, 42L);
            for (int i = 0; i < 50; i++) {
                assertEquals(a.generate(), b.generate());
            }
        }

        @Test
        @DisplayName("min == max throws IllegalArgumentException")
        void minEqualsMaxThrows() {
            ByteGenerator gen = new ByteGenerator((byte) 5, (byte) 5);
            assertThrows(IllegalArgumentException.class, gen::generate);
        }

        @Test
        @DisplayName("generateList returns correct count")
        void generateList() {
            List<Byte> list = new ByteGenerator().generateList(10);
            assertEquals(10, list.size());
        }
    }

    // ── ShortGenerator ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("ShortGenerator")
    class ShortGeneratorTest {

        @Test
        @DisplayName("custom range [100, 200)")
        void customRange() {
            ShortGenerator gen = new ShortGenerator((short) 100, (short) 200);
            for (int i = 0; i < SAMPLES; i++) {
                short v = gen.generate();
                assertTrue(v >= 100 && v < 200, "Expected [100, 200), got: " + v);
            }
        }

        @Test
        @DisplayName("same seed produces identical sequence")
        void seededReproducibility() {
            ShortGenerator a = new ShortGenerator((short) 0, (short) 1000, 7L);
            ShortGenerator b = new ShortGenerator((short) 0, (short) 1000, 7L);
            for (int i = 0; i < 50; i++) {
                assertEquals(a.generate(), b.generate());
            }
        }

        @Test
        @DisplayName("min == max throws IllegalArgumentException")
        void minEqualsMaxThrows() {
            ShortGenerator gen = new ShortGenerator((short) 1, (short) 1);
            assertThrows(IllegalArgumentException.class, gen::generate);
        }
    }

    // ── IntGenerator ──────────────────────────────────────────────────────────

    @Nested
    @DisplayName("IntGenerator")
    class IntGeneratorTest {

        @Test
        @DisplayName("range [1, 7) for die roll")
        void dieRange() {
            IntGenerator die = new IntGenerator(1, 7);
            for (int i = 0; i < SAMPLES; i++) {
                int v = die.generate();
                assertTrue(v >= 1 && v < 7, "Expected [1, 7), got: " + v);
            }
        }

        @Test
        @DisplayName("negative range [-50, -1)")
        void negativeRange() {
            IntGenerator gen = new IntGenerator(-50, -1);
            for (int i = 0; i < SAMPLES; i++) {
                int v = gen.generate();
                assertTrue(v >= -50 && v < -1, "Expected [-50, -1), got: " + v);
            }
        }

        @Test
        @DisplayName("default range spans full 32-bit space")
        void defaultRangeProducesVariety() {
            IntGenerator gen = new IntGenerator();
            Set<Integer> seen = new HashSet<>();
            for (int i = 0; i < SAMPLES; i++) seen.add(gen.generate());
            assertTrue(seen.size() > 1, "Expected variety across " + SAMPLES + " samples");
        }

        @Test
        @DisplayName("same seed produces identical sequence")
        void seededReproducibility() {
            IntGenerator a = new IntGenerator(0, 1000, 99L);
            IntGenerator b = new IntGenerator(0, 1000, 99L);
            for (int i = 0; i < 50; i++) {
                assertEquals(a.generate(), b.generate());
            }
        }

        @Test
        @DisplayName("min == max throws IllegalArgumentException")
        void minEqualsMaxThrows() {
            IntGenerator gen = new IntGenerator(5, 5);
            assertThrows(IllegalArgumentException.class, gen::generate);
        }

        @Test
        @DisplayName("generate(min, max) can be called ad-hoc")
        void adHocBoundedGenerate() {
            IntGenerator gen = new IntGenerator();
            for (int i = 0; i < SAMPLES; i++) {
                int v = gen.generate(10, 20);
                assertTrue(v >= 10 && v < 20, "Expected [10, 20), got: " + v);
            }
        }

        @Test
        @DisplayName("generateList returns immutable list of correct size")
        void generateList() {
            List<Integer> list = new IntGenerator(1, 100).generateList(25);
            assertEquals(25, list.size());
            assertThrows(UnsupportedOperationException.class, () -> list.add(0));
        }

        @Test
        @DisplayName("stream produces on-demand values")
        void streamUsage() {
            List<Integer> values = new IntGenerator(1, 10).stream().limit(30).toList();
            assertEquals(30, values.size());
            values.forEach(v -> assertTrue(v >= 1 && v < 10));
        }

        @Test
        @DisplayName("map transforms generated values")
        void mapTransformation() {
            Generator<String> strGen = new IntGenerator(1, 100).map(Object::toString);
            String value = strGen.generate();
            assertNotNull(value);
            assertTrue(Integer.parseInt(value) >= 1);
        }
    }

    // ── LongGenerator ─────────────────────────────────────────────────────────

    @Nested
    @DisplayName("LongGenerator")
    class LongGeneratorTest {

        @Test
        @DisplayName("range [1_000L, 2_000L)")
        void customRange() {
            LongGenerator gen = new LongGenerator(1_000L, 2_000L);
            for (int i = 0; i < SAMPLES; i++) {
                long v = gen.generate();
                assertTrue(v >= 1_000L && v < 2_000L, "Expected [1000, 2000), got: " + v);
            }
        }

        @Test
        @DisplayName("same seed produces identical sequence")
        void seededReproducibility() {
            LongGenerator a = new LongGenerator(0L, Long.MAX_VALUE, 123L);
            LongGenerator b = new LongGenerator(0L, Long.MAX_VALUE, 123L);
            for (int i = 0; i < 50; i++) {
                assertEquals(a.generate(), b.generate());
            }
        }

        @Test
        @DisplayName("min == max throws IllegalArgumentException")
        void minEqualsMaxThrows() {
            LongGenerator gen = new LongGenerator(1L, 1L);
            assertThrows(IllegalArgumentException.class, gen::generate);
        }
    }

    // ── FloatGenerator ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("FloatGenerator")
    class FloatGeneratorTest {

        @Test
        @DisplayName("default range [0.0f, 1.0f)")
        void defaultRange() {
            FloatGenerator gen = new FloatGenerator();
            for (int i = 0; i < SAMPLES; i++) {
                float v = gen.generate();
                assertTrue(v >= 0.0f && v < 1.0f, "Expected [0, 1), got: " + v);
            }
        }

        @Test
        @DisplayName("custom range [-10f, 10f)")
        void customRange() {
            FloatGenerator gen = new FloatGenerator(-10f, 10f);
            for (int i = 0; i < SAMPLES; i++) {
                float v = gen.generate();
                assertTrue(v >= -10f && v < 10f, "Expected [-10, 10), got: " + v);
            }
        }

        @Test
        @DisplayName("same seed produces identical sequence")
        void seededReproducibility() {
            FloatGenerator a = new FloatGenerator(0f, 100f, 55L);
            FloatGenerator b = new FloatGenerator(0f, 100f, 55L);
            for (int i = 0; i < 50; i++) {
                assertEquals(a.generate(), b.generate());
            }
        }

        @Test
        @DisplayName("min == max throws IllegalArgumentException")
        void minEqualsMaxThrows() {
            FloatGenerator gen = new FloatGenerator(3.14f, 3.14f);
            assertThrows(IllegalArgumentException.class, gen::generate);
        }
    }

    // ── DoubleGenerator ───────────────────────────────────────────────────────

    @Nested
    @DisplayName("DoubleGenerator")
    class DoubleGeneratorTest {

        @Test
        @DisplayName("default range [0.0, 1.0)")
        void defaultRange() {
            DoubleGenerator gen = new DoubleGenerator();
            for (int i = 0; i < SAMPLES; i++) {
                double v = gen.generate();
                assertTrue(v >= 0.0 && v < 1.0, "Expected [0, 1), got: " + v);
            }
        }

        @Test
        @DisplayName("custom range [-180.0, 180.0) for longitude")
        void longitudeRange() {
            DoubleGenerator gen = new DoubleGenerator(-180.0, 180.0);
            for (int i = 0; i < SAMPLES; i++) {
                double v = gen.generate();
                assertTrue(v >= -180.0 && v < 180.0, "Expected [-180, 180), got: " + v);
            }
        }

        @Test
        @DisplayName("same seed produces identical sequence")
        void seededReproducibility() {
            DoubleGenerator a = new DoubleGenerator(0.0, 1.0, 77L);
            DoubleGenerator b = new DoubleGenerator(0.0, 1.0, 77L);
            for (int i = 0; i < 50; i++) {
                assertEquals(a.generate(), b.generate());
            }
        }

        @Test
        @DisplayName("min == max throws IllegalArgumentException")
        void minEqualsMaxThrows() {
            DoubleGenerator gen = new DoubleGenerator(2.71, 2.71);
            assertThrows(IllegalArgumentException.class, gen::generate);
        }
    }

    // ── CharGenerator ─────────────────────────────────────────────────────────

    @Nested
    @DisplayName("CharGenerator")
    class CharGeneratorTest {

        @Test
        @DisplayName("uppercase-only produces A–Z")
        void uppercaseOnly() {
            CharGenerator gen = CharGenerator.builder().uppercase().build();
            for (int i = 0; i < SAMPLES; i++) {
                char v = gen.generate();
                assertTrue(Character.isUpperCase(v), "Expected uppercase, got: " + v);
            }
        }

        @Test
        @DisplayName("lowercase-only produces a–z")
        void lowercaseOnly() {
            CharGenerator gen = CharGenerator.builder().lowercase().build();
            for (int i = 0; i < SAMPLES; i++) {
                char v = gen.generate();
                assertTrue(Character.isLowerCase(v), "Expected lowercase, got: " + v);
            }
        }

        @Test
        @DisplayName("digits-only produces 0–9")
        void digitsOnly() {
            CharGenerator gen = CharGenerator.builder().digits().build();
            for (int i = 0; i < SAMPLES; i++) {
                char v = gen.generate();
                assertTrue(Character.isDigit(v), "Expected digit, got: " + v);
            }
        }

        @Test
        @DisplayName("all groups produce variety of character types")
        void allGroups() {
            CharGenerator gen = CharGenerator.all();
            List<Character> chars = gen.generateList(500);
            assertTrue(chars.stream().anyMatch(Character::isUpperCase), "No uppercase found");
            assertTrue(chars.stream().anyMatch(Character::isLowerCase), "No lowercase found");
            assertTrue(chars.stream().anyMatch(Character::isDigit),     "No digit found");
        }

        @Test
        @DisplayName("no groups enabled throws IllegalArgumentException")
        void noGroupsThrows() {
            CharGenerator.Builder builder = CharGenerator.builder(); // nothing enabled
            assertThrows(IllegalArgumentException.class, builder::build);
        }

        @Test
        @DisplayName("same seed produces identical sequence")
        void seededReproducibility() {
            CharGenerator a = CharGenerator.builder().uppercase().lowercase().seed(11L).build();
            CharGenerator b = CharGenerator.builder().uppercase().lowercase().seed(11L).build();
            for (int i = 0; i < 50; i++) {
                assertEquals(a.generate(), b.generate());
            }
        }
    }

    // ── BooleanGenerator ──────────────────────────────────────────────────────

    @Nested
    @DisplayName("BooleanGenerator")
    class BooleanGeneratorTest {

        @Test
        @DisplayName("produces both true and false over many samples")
        void producesBothValues() {
            BooleanGenerator gen = new BooleanGenerator();
            Set<Boolean> seen = new HashSet<>();
            for (int i = 0; i < SAMPLES; i++) seen.add(gen.generate());
            assertTrue(seen.contains(true),  "Never saw true");
            assertTrue(seen.contains(false), "Never saw false");
        }

        @Test
        @DisplayName("same seed produces identical sequence")
        void seededReproducibility() {
            BooleanGenerator a = new BooleanGenerator(3L);
            BooleanGenerator b = new BooleanGenerator(3L);
            for (int i = 0; i < 50; i++) {
                assertEquals(a.generate(), b.generate());
            }
        }
    }

    // ── StringGenerator ───────────────────────────────────────────────────────

    @Nested
    @DisplayName("StringGenerator")
    class StringGeneratorTest {

        @Test
        @DisplayName("fixed length via .length(n)")
        void fixedLength() {
            StringGenerator gen = StringGenerator.builder().length(10).build();
            for (int i = 0; i < SAMPLES; i++) {
                assertEquals(10, gen.generate().length());
            }
        }

        @Test
        @DisplayName("length stays within [min, max] range")
        void lengthRange() {
            StringGenerator gen = StringGenerator.builder().minLength(5).maxLength(15).build();
            for (int i = 0; i < SAMPLES; i++) {
                int len = gen.generate().length();
                assertTrue(len >= 5 && len <= 15, "Expected length 5–15, got: " + len);
            }
        }

        @Test
        @DisplayName("digits-only charGenerator produces numeric strings")
        void numericString() {
            StringGenerator gen = StringGenerator.builder()
                    .charGenerator(CharGenerator.digits())
                    .length(8)
                    .build();
            for (int i = 0; i < SAMPLES; i++) {
                String v = gen.generate();
                assertTrue(v.chars().allMatch(Character::isDigit),
                        "Expected digits only, got: " + v);
            }
        }

        @Test
        @DisplayName("produces distinct strings across samples")
        void producesDistinctStrings() {
            Set<String> seen = new HashSet<>(Generators.ofString().generateList(SAMPLES));
            assertTrue(seen.size() > 1, "Expected distinct strings");
        }

        @Test
        @DisplayName("same seed produces identical sequence")
        void seededReproducibility() {
            CharGenerator charA = CharGenerator.builder().uppercase().lowercase().seed(1L).build();
            CharGenerator charB = CharGenerator.builder().uppercase().lowercase().seed(1L).build();
            StringGenerator a = StringGenerator.builder().minLength(8).maxLength(8).charGenerator(charA).seed(1L).build();
            StringGenerator b = StringGenerator.builder().minLength(8).maxLength(8).charGenerator(charB).seed(1L).build();
            for (int i = 0; i < 20; i++) {
                assertEquals(a.generate(), b.generate());
            }
        }

        @Test
        @DisplayName("minLength < 1 throws IllegalArgumentException")
        void invalidMinLengthThrows() {
            assertThrows(IllegalArgumentException.class,
                    () -> StringGenerator.builder().minLength(0).build());
        }

        @Test
        @DisplayName("maxLength < minLength throws IllegalArgumentException")
        void maxLessThanMinThrows() {
            assertThrows(IllegalArgumentException.class,
                    () -> StringGenerator.builder().minLength(10).maxLength(5).build());
        }
    }

    // ── Generators factory ────────────────────────────────────────────────────

    @Nested
    @DisplayName("Generators factory")
    class GeneratorsFactoryTest {

        @Test
        @DisplayName("ofInt(min, max) produces values in range")
        void ofIntRange() {
            IntGenerator gen = Generators.ofInt(0, 100);
            for (int i = 0; i < SAMPLES; i++) {
                int v = gen.generate();
                assertTrue(v >= 0 && v < 100, "Expected [0, 100), got: " + v);
            }
        }

        @Test
        @DisplayName("ofLong() returns a LongGenerator")
        void ofLongReturnsType() {
            assertInstanceOf(LongGenerator.class, Generators.ofLong());
        }

        @Test
        @DisplayName("ofDouble() default produces [0.0, 1.0)")
        void ofDoubleDefault() {
            DoubleGenerator gen = Generators.ofDouble();
            for (int i = 0; i < SAMPLES; i++) {
                double v = gen.generate();
                assertTrue(v >= 0.0 && v < 1.0);
            }
        }

        @Test
        @DisplayName("forType resolves generator for all primitive wrapper types")
        void forTypeResolvesAllTypes() {
            assertNotNull(Generators.forType(Byte.class).generate());
            assertNotNull(Generators.forType(Short.class).generate());
            assertNotNull(Generators.forType(Integer.class).generate());
            assertNotNull(Generators.forType(Long.class).generate());
            assertNotNull(Generators.forType(Float.class).generate());
            assertNotNull(Generators.forType(Double.class).generate());
            assertNotNull(Generators.forType(Character.class).generate());
            assertNotNull(Generators.forType(Boolean.class).generate());
            assertNotNull(Generators.forType(String.class).generate());
        }

        @Test
        @DisplayName("forType with primitive class also resolves")
        void forTypePrimitive() {
            assertNotNull(Generators.forType(int.class).generate());
            assertNotNull(Generators.forType(long.class).generate());
            assertNotNull(Generators.forType(boolean.class).generate());
        }

        @Test
        @DisplayName("forType with unknown type throws IllegalArgumentException")
        void forTypeUnknownThrows() {
            assertThrows(IllegalArgumentException.class,
                    () -> Generators.forType(Object.class));
        }

        @Test
        @DisplayName("Generator.map composes correctly")
        void generatorMap() {
            Generator<String> gen = Generators.ofInt(1, 100).map(n -> "id-" + n);
            String value = gen.generate();
            assertTrue(value.startsWith("id-"), "Expected 'id-' prefix, got: " + value);
        }

        @Test
        @DisplayName("Generator.filter retains only matching values")
        void generatorFilter() {
            Generator<Integer> evens = Generators.ofInt(0, 100).filter(n -> n % 2 == 0);
            for (int i = 0; i < 50; i++) {
                assertEquals(0, evens.generate() % 2, "Expected even number");
            }
        }

        @Test
        @DisplayName("generateList with count=0 returns empty list")
        void generateListZero() {
            List<Integer> list = Generators.ofInt().generateList(0);
            assertTrue(list.isEmpty());
        }

        @Test
        @DisplayName("generateList with negative count throws")
        void generateListNegativeThrows() {
            assertThrows(IllegalArgumentException.class,
                    () -> Generators.ofInt().generateList(-1));
        }
    }
}
