/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.color;

import io.github.frikit.krandom.generator.GeneratorConfig;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ColorGeneratorTest {

    @Test
    void testDefaultConstructor() {
        ColorGenerator generator = new ColorGenerator();
        assertNotNull(generator);
    }

    @Test
    void testConfigConstructor() {
        GeneratorConfig config = GeneratorConfig.builder().seed(12345L).build();
        ColorGenerator generator = new ColorGenerator(config);
        assertNotNull(generator);
    }

    @Test
    void testNullConfigThrowsException() {
        assertThrows(NullPointerException.class, () -> new ColorGenerator(null));
    }

    @Test
    void testGenerateNotNull() {
        ColorGenerator generator = new ColorGenerator();
        String color = generator.generate();
        assertNotNull(color);
    }

    @Test
    void testGenerateHexFormat() {
        ColorGenerator generator = new ColorGenerator();
        String color = generator.generate();
        assertTrue(color.matches("#[0-9a-f]{6}"), "Expected hex format: " + color);
    }

    @Test
    void testGenerateWithHexFormat() {
        ColorGenerator generator = new ColorGenerator();
        String color = generator.generate(ColorFormat.HEX);
        assertTrue(color.matches("#[0-9a-f]{6}"), "Expected hex format: " + color);
    }

    @Test
    void testGenerateWithShortHexFormat() {
        ColorGenerator generator = new ColorGenerator();
        String color = generator.generate(ColorFormat.SHORT_HEX);
        assertTrue(color.matches("#[0-9a-f]{3}"), "Expected short hex format: " + color);
    }

    @Test
    void testGenerateWithRGBFormat() {
        ColorGenerator generator = new ColorGenerator();
        String color = generator.generate(ColorFormat.RGB);
        assertTrue(color.matches("rgb\\(\\d{1,3},\\d{1,3},\\d{1,3}\\)"), "Expected RGB format: " + color);
    }

    @Test
    void testGenerateWithRGBAFormat() {
        ColorGenerator generator = new ColorGenerator();
        String color = generator.generate(ColorFormat.RGBA);
        assertTrue(color.matches("rgba\\(\\d{1,3},\\d{1,3},\\d{1,3},\\d\\.\\d{3}\\)"), "Expected RGBA format: " + color);
    }

    @Test
    void testGenerateWithHSLFormat() {
        ColorGenerator generator = new ColorGenerator();
        String color = generator.generate(ColorFormat.HSL);
        assertTrue(color.matches("hsl\\(\\d{1,3},\\d{1,3}%,\\d{1,3}%\\)"), "Expected HSL format: " + color);
    }

    @Test
    void testGenerateWithHSLAFormat() {
        ColorGenerator generator = new ColorGenerator();
        String color = generator.generate(ColorFormat.HSLA);
        assertTrue(color.matches("hsla\\(\\d{1,3},\\d{1,3}%,\\d{1,3}%,\\d\\.\\d{3}\\)"), "Expected HSLA format: " + color);
    }

    @Test
    void testGenerateWithHex0xFormat() {
        ColorGenerator generator = new ColorGenerator();
        String color = generator.generate(ColorFormat.HEX_0X);
        assertTrue(color.matches("0x[0-9a-f]{6}"), "Expected 0x hex format: " + color);
    }

    @Test
    void testGenerateWithNullFormatThrowsException() {
        ColorGenerator generator = new ColorGenerator();
        assertThrows(NullPointerException.class, () -> generator.generate(null));
    }

    @Test
    void testGenerateGrayscale() {
        ColorGenerator generator = new ColorGenerator();
        String color = generator.generateGrayscale();
        assertNotNull(color);
        assertTrue(color.matches("#[0-9a-f]{6}"), "Expected hex format: " + color);

        // Extract RGB components and verify they're equal
        String hex = color.substring(1);
        int r = Integer.parseInt(hex.substring(0, 2), 16);
        int g = Integer.parseInt(hex.substring(2, 4), 16);
        int b = Integer.parseInt(hex.substring(4, 6), 16);

        assertEquals(r, g, "Grayscale R and G should be equal");
        assertEquals(g, b, "Grayscale G and B should be equal");
    }

    @Test
    void testGenerateGrayscaleWithFormat() {
        ColorGenerator generator = new ColorGenerator();
        String color = generator.generateGrayscale(ColorFormat.RGB);
        assertTrue(color.matches("rgb\\(\\d{1,3},\\d{1,3},\\d{1,3}\\)"), "Expected RGB format: " + color);

        // Extract RGB components and verify they're equal
        String[] parts = color.substring(4, color.length() - 1).split(",");
        int r = Integer.parseInt(parts[0]);
        int g = Integer.parseInt(parts[1]);
        int b = Integer.parseInt(parts[2]);

        assertEquals(r, g, "Grayscale R and G should be equal");
        assertEquals(g, b, "Grayscale G and B should be equal");
    }

    @Test
    void testGenerateGrayscaleWithNullFormatThrowsException() {
        ColorGenerator generator = new ColorGenerator();
        assertThrows(NullPointerException.class, () -> generator.generateGrayscale(null));
    }

    @Test
    void testGenerateUppercase() {
        ColorGenerator generator = new ColorGenerator();
        String color = generator.generateUppercase();
        assertTrue(color.matches("#[0-9A-F]{6}"), "Expected uppercase hex format: " + color);
    }

    @Test
    void testGenerateUppercaseWithFormat() {
        ColorGenerator generator = new ColorGenerator();
        String color = generator.generateUppercase(ColorFormat.HEX);
        assertTrue(color.matches("#[0-9A-F]{6}"), "Expected uppercase hex format: " + color);
    }

    @Test
    void testGenerateUppercaseWithShortHex() {
        ColorGenerator generator = new ColorGenerator();
        String color = generator.generateUppercase(ColorFormat.SHORT_HEX);
        assertTrue(color.matches("#[0-9A-F]{3}"), "Expected uppercase short hex format: " + color);
    }

    @Test
    void testGenerateUppercaseWith0x() {
        ColorGenerator generator = new ColorGenerator();
        String color = generator.generateUppercase(ColorFormat.HEX_0X);
        assertTrue(color.matches("0x[0-9A-F]{6}"), "Expected uppercase 0x hex format: " + color);
    }

    @Test
    void testGenerateUppercaseWithNullFormatThrowsException() {
        ColorGenerator generator = new ColorGenerator();
        assertThrows(NullPointerException.class, () -> generator.generateUppercase(null));
    }

    @Test
    void testSeededGeneratorProducesSameResults() {
        ColorGenerator gen1 = new ColorGenerator(GeneratorConfig.builder().seed(42L).build());
        ColorGenerator gen2 = new ColorGenerator(GeneratorConfig.builder().seed(42L).build());

        assertEquals(gen1.generate(), gen2.generate());
        assertEquals(gen1.generate(), gen2.generate());
        assertEquals(gen1.generate(), gen2.generate());
    }

    @Test
    void testSeededGeneratorWithFormatProducesSameResults() {
        ColorGenerator gen1 = new ColorGenerator(GeneratorConfig.builder().seed(999L).build());
        ColorGenerator gen2 = new ColorGenerator(GeneratorConfig.builder().seed(999L).build());

        assertEquals(gen1.generate(ColorFormat.RGB), gen2.generate(ColorFormat.RGB));
        assertEquals(gen1.generate(ColorFormat.SHORT_HEX), gen2.generate(ColorFormat.SHORT_HEX));
    }

    @Test
    void testDifferentSeedsProduceDifferentResults() {
        ColorGenerator gen1 = new ColorGenerator(GeneratorConfig.builder().seed(100L).build());
        ColorGenerator gen2 = new ColorGenerator(GeneratorConfig.builder().seed(200L).build());

        assertNotEquals(gen1.generate(), gen2.generate());
    }

    @Test
    void testColorNameGenerators() {
        ColorGenerator generator = new ColorGenerator();
        assertFalse(generator.generateColorName().isBlank());
        assertFalse(generator.generateSafeColorName().isBlank());
    }

    @Test
    void testGenerateMultipleColors() {
        ColorGenerator generator = new ColorGenerator();
        Set<String> colors = new HashSet<>();
        for (int i = 0; i < 50; i++) {
            colors.add(generator.generate());
        }
        assertTrue(colors.size() > 20, "Should generate diverse colors");
    }

    @Test
    void testRGBComponentsInValidRange() {
        ColorGenerator generator = new ColorGenerator();
        for (int i = 0; i < 100; i++) {
            String color = generator.generate(ColorFormat.RGB);
            String[] parts = color.substring(4, color.length() - 1).split(",");

            int r = Integer.parseInt(parts[0]);
            int g = Integer.parseInt(parts[1]);
            int b = Integer.parseInt(parts[2]);

            assertTrue(r >= 0 && r <= 255, "R out of range: " + r);
            assertTrue(g >= 0 && g <= 255, "G out of range: " + g);
            assertTrue(b >= 0 && b <= 255, "B out of range: " + b);
        }
    }

    @Test
    void testHexComponentsInValidRange() {
        ColorGenerator generator = new ColorGenerator();
        for (int i = 0; i < 100; i++) {
            String color = generator.generate(ColorFormat.HEX);
            String hex = color.substring(1);

            int r = Integer.parseInt(hex.substring(0, 2), 16);
            int g = Integer.parseInt(hex.substring(2, 4), 16);
            int b = Integer.parseInt(hex.substring(4, 6), 16);

            assertTrue(r >= 0 && r <= 255, "R out of range: " + r);
            assertTrue(g >= 0 && g <= 255, "G out of range: " + g);
            assertTrue(b >= 0 && b <= 255, "B out of range: " + b);
        }
    }

    @Test
    void testShortHexProducesValidColors() {
        ColorGenerator generator = new ColorGenerator();
        for (int i = 0; i < 50; i++) {
            String color = generator.generate(ColorFormat.SHORT_HEX);
            assertTrue(color.matches("#[0-9a-f]{3}"), "Invalid short hex: " + color);
        }
    }

    @Test
    void testAllFormatsProduceValidOutput() {
        ColorGenerator generator = new ColorGenerator(GeneratorConfig.builder().seed(12345L).build());

        String hex = generator.generate(ColorFormat.HEX);
        assertTrue(hex.matches("#[0-9a-f]{6}"));

        String shortHex = generator.generate(ColorFormat.SHORT_HEX);
        assertTrue(shortHex.matches("#[0-9a-f]{3}"));

        String rgb = generator.generate(ColorFormat.RGB);
        assertTrue(rgb.matches("rgb\\(\\d{1,3},\\d{1,3},\\d{1,3}\\)"));

        String rgba = generator.generate(ColorFormat.RGBA);
        assertTrue(rgba.matches("rgba\\(\\d{1,3},\\d{1,3},\\d{1,3},\\d\\.\\d{3}\\)"));

        String hsl = generator.generate(ColorFormat.HSL);
        assertTrue(hsl.matches("hsl\\(\\d{1,3},\\d{1,3}%,\\d{1,3}%\\)"));

        String hsla = generator.generate(ColorFormat.HSLA);
        assertTrue(hsla.matches("hsla\\(\\d{1,3},\\d{1,3}%,\\d{1,3}%,\\d\\.\\d{3}\\)"));

        String hex0x = generator.generate(ColorFormat.HEX_0X);
        assertTrue(hex0x.matches("0x[0-9a-f]{6}"));
    }

    @Test
    void testGrayscaleProducesMultipleShades() {
        ColorGenerator generator = new ColorGenerator();
        Set<String> grays = new HashSet<>();
        for (int i = 0; i < 100; i++) {
            grays.add(generator.generateGrayscale());
        }
        assertTrue(grays.size() > 50, "Should generate diverse grayscale shades");
    }

    @Test
    void testUppercaseContainsOnlyUppercaseHex() {
        ColorGenerator generator = new ColorGenerator();
        for (int i = 0; i < 50; i++) {
            String color = generator.generateUppercase();
            assertFalse(color.matches(".*[a-f].*"), "Should not contain lowercase hex: " + color);
            assertTrue(color.matches("#[0-9A-F]{6}"), "Should be uppercase hex: " + color);
        }
    }

    @Test
    void testSeededGrayscaleProducesSameResults() {
        ColorGenerator gen1 = new ColorGenerator(GeneratorConfig.builder().seed(777L).build());
        ColorGenerator gen2 = new ColorGenerator(GeneratorConfig.builder().seed(777L).build());

        assertEquals(gen1.generateGrayscale(), gen2.generateGrayscale());
        assertEquals(gen1.generateGrayscale(), gen2.generateGrayscale());
    }

    @Test
    void testSeededUppercaseProducesSameResults() {
        ColorGenerator gen1 = new ColorGenerator(GeneratorConfig.builder().seed(888L).build());
        ColorGenerator gen2 = new ColorGenerator(GeneratorConfig.builder().seed(888L).build());

        assertEquals(gen1.generateUppercase(), gen2.generateUppercase());
        assertEquals(gen1.generateUppercase(), gen2.generateUppercase());
    }

    @Test
    void testRGBFormatDoesNotHaveCase() {
        ColorGenerator generator = new ColorGenerator();
        // RGB format should be the same regardless of uppercase flag
        String rgb1 = generator.generate(ColorFormat.RGB);
        String rgb2 = generator.generateUppercase(ColorFormat.RGB);

        // Both should be valid RGB format
        assertTrue(rgb1.matches("rgb\\(\\d{1,3},\\d{1,3},\\d{1,3}\\)"));
        assertTrue(rgb2.matches("rgb\\(\\d{1,3},\\d{1,3},\\d{1,3}\\)"));
    }

    @Test
    void test0xFormatStartsWith0x() {
        ColorGenerator generator = new ColorGenerator();
        for (int i = 0; i < 20; i++) {
            String color = generator.generate(ColorFormat.HEX_0X);
            assertTrue(color.startsWith("0x"), "Should start with 0x: " + color);
        }
    }

    @Test
    void testHexFormatStartsWithHash() {
        ColorGenerator generator = new ColorGenerator();
        for (int i = 0; i < 20; i++) {
            String color = generator.generate(ColorFormat.HEX);
            assertTrue(color.startsWith("#"), "Should start with #: " + color);
        }
    }

    @Test
    void testShortHexFormatStartsWithHash() {
        ColorGenerator generator = new ColorGenerator();
        for (int i = 0; i < 20; i++) {
            String color = generator.generate(ColorFormat.SHORT_HEX);
            assertTrue(color.startsWith("#"), "Should start with #: " + color);
        }
    }

    @Test
    void testRGBFormatStartsWithRgb() {
        ColorGenerator generator = new ColorGenerator();
        for (int i = 0; i < 20; i++) {
            String color = generator.generate(ColorFormat.RGB);
            assertTrue(color.startsWith("rgb("), "Should start with rgb(: " + color);
            assertTrue(color.endsWith(")"), "Should end with ): " + color);
        }
    }

    @Test
    void testHslValuesInRange() {
        ColorGenerator generator = new ColorGenerator();
        for (int i = 0; i < 50; i++) {
            String color = generator.generate(ColorFormat.HSL);
            String[] parts = color.substring(4, color.length() - 1).split(",");
            int h = Integer.parseInt(parts[0]);
            int s = Integer.parseInt(parts[1].replace("%", ""));
            int l = Integer.parseInt(parts[2].replace("%", ""));
            assertTrue(h >= 0 && h <= 359);
            assertTrue(s >= 0 && s <= 100);
            assertTrue(l >= 0 && l <= 100);
        }
    }
}
