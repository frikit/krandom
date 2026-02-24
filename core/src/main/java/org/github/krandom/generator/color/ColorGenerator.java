/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.color;

import org.github.krandom.generator.Generator;
import org.github.krandom.generator.GeneratorConfig;

import java.security.SecureRandom;
import java.util.Objects;
import java.util.Random;

/**
 * Generates random colors in various formats including hex, RGB, and grayscale.
 *
 * <p>This generator produces color values in multiple representations:
 * hex (#79c157), short hex (#60f), RGB (rgb(110,52,164)), 0x format (0x79c157),
 * and grayscale variations.
 *
 * <p><strong>Basic Usage:</strong>
 * <pre>{@code
 * ColorGenerator gen = new ColorGenerator();
 * String color = gen.generate();  // "#79c157" (random hex color)
 * }</pre>
 *
 * <p><strong>Format Selection:</strong>
 * <pre>{@code
 * ColorGenerator gen = new ColorGenerator();
 * String hex = gen.generate(ColorFormat.HEX);        // "#79c157"
 * String shortHex = gen.generate(ColorFormat.SHORT_HEX);  // "#60f"
 * String rgb = gen.generate(ColorFormat.RGB);        // "rgb(110,52,164)"
 * String hex0x = gen.generate(ColorFormat.HEX_0X);   // "0x79c157"
 * }</pre>
 *
 * <p><strong>Grayscale Colors:</strong>
 * <pre>{@code
 * ColorGenerator gen = new ColorGenerator();
 * String gray = gen.generateGrayscale();  // "#e2e2e2" (all RGB components equal)
 * }</pre>
 *
 * <p><strong>Uppercase Hex:</strong>
 * <pre>{@code
 * ColorGenerator gen = new ColorGenerator();
 * String upper = gen.generateUppercase();  // "#79C157"
 * }</pre>
 *
 * <p><strong>Seeded Generation:</strong>
 * <pre>{@code
 * GeneratorConfig config = GeneratorConfig.builder().seed(12345L).build();
 * ColorGenerator gen = new ColorGenerator(config);
 * String color = gen.generate();  // Reproducible output
 * }</pre>
 *
 * <p><strong>Thread Safety:</strong>
 * This generator is thread-safe and can be shared across threads.
 */
public final class ColorGenerator implements Generator<String> {

    private final GeneratorConfig config;
    private final Random random;

    /**
     * Creates a color generator with default configuration.
     */
    public ColorGenerator() {
        this(GeneratorConfig.defaults());
    }

    /**
     * Creates a color generator with the specified configuration.
     *
     * @param config the generator configuration; must not be {@code null}
     * @throws NullPointerException if {@code config} is {@code null}
     */
    public ColorGenerator(GeneratorConfig config) {
        this.config = Objects.requireNonNull(config, "config must not be null");
        this.random = config.getSeed().isPresent()
                ? new Random(config.getSeed().getAsLong())
                : new SecureRandom();
    }

    /**
     * {@inheritDoc}
     *
     * <p>Generates a random color in standard hex format (#RRGGBB).
     *
     * @return a hex color string; never {@code null}
     */
    @Override
    public String generate() {
        return generate(ColorFormat.HEX);
    }

    /**
     * Generates a random color in the specified format.
     *
     * @param format the color format; must not be {@code null}
     * @return a color string in the specified format; never {@code null}
     * @throws NullPointerException if {@code format} is {@code null}
     */
    public String generate(ColorFormat format) {
        Objects.requireNonNull(format, "format must not be null");
        
        int r = random.nextInt(256);
        int g = random.nextInt(256);
        int b = random.nextInt(256);
        
        return formatColor(r, g, b, format, false);
    }

    /**
     * Generates a random grayscale color.
     * <p>All RGB components will have the same value, producing a shade of gray.
     *
     * @return a grayscale hex color string; never {@code null}
     */
    public String generateGrayscale() {
        return generateGrayscale(ColorFormat.HEX);
    }

    /**
     * Generates a random grayscale color in the specified format.
     *
     * @param format the color format; must not be {@code null}
     * @return a grayscale color string in the specified format; never {@code null}
     * @throws NullPointerException if {@code format} is {@code null}
     */
    public String generateGrayscale(ColorFormat format) {
        Objects.requireNonNull(format, "format must not be null");
        
        int gray = random.nextInt(256);
        return formatColor(gray, gray, gray, format, false);
    }

    /**
     * Generates a random color in hex format with uppercase letters.
     *
     * @return an uppercase hex color string; never {@code null}
     */
    public String generateUppercase() {
        return generateUppercase(ColorFormat.HEX);
    }

    /**
     * Generates a random color in the specified format with uppercase letters.
     *
     * @param format the color format; must not be {@code null}
     * @return a color string in the specified format with uppercase letters; never {@code null}
     * @throws NullPointerException if {@code format} is {@code null}
     */
    public String generateUppercase(ColorFormat format) {
        Objects.requireNonNull(format, "format must not be null");
        
        int r = random.nextInt(256);
        int g = random.nextInt(256);
        int b = random.nextInt(256);
        
        return formatColor(r, g, b, format, true);
    }

    /**
     * Formats RGB components into the specified color format.
     *
     * @param r red component [0-255]
     * @param g green component [0-255]
     * @param b blue component [0-255]
     * @param format the output format
     * @param uppercase whether to use uppercase hex letters
     * @return formatted color string
     */
    private String formatColor(int r, int g, int b, ColorFormat format, boolean uppercase) {
        return switch (format) {
            case HEX -> formatHex(r, g, b, uppercase);
            case SHORT_HEX -> formatShortHex(r, g, b, uppercase);
            case RGB -> formatRGB(r, g, b);
            case HEX_0X -> format0x(r, g, b, uppercase);
        };
    }

    /**
     * Formats RGB as standard hex (#RRGGBB).
     */
    private String formatHex(int r, int g, int b, boolean uppercase) {
        String hex = String.format("#%02x%02x%02x", r, g, b);
        return uppercase ? hex.toUpperCase() : hex;
    }

    /**
     * Formats RGB as short hex (#RGB).
     * <p>Each component is reduced to a single hex digit by dividing by 16.
     */
    private String formatShortHex(int r, int g, int b, boolean uppercase) {
        // Convert to 0-15 range by dividing by 16
        int rShort = r / 16;
        int gShort = g / 16;
        int bShort = b / 16;
        String hex = String.format("#%x%x%x", rShort, gShort, bShort);
        return uppercase ? hex.toUpperCase() : hex;
    }

    /**
     * Formats RGB as CSS rgb() function.
     */
    private String formatRGB(int r, int g, int b) {
        return String.format("rgb(%d,%d,%d)", r, g, b);
    }

    /**
     * Formats RGB as 0x hex number.
     */
    private String format0x(int r, int g, int b, boolean uppercase) {
        String formatStr = uppercase ? "0x%02X%02X%02X" : "0x%02x%02x%02x";
        return String.format(formatStr, r, g, b);
    }
}
