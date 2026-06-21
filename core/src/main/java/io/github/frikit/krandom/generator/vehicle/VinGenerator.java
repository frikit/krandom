/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.vehicle;

import io.github.frikit.krandom.generator.Generator;
import io.github.frikit.krandom.generator.GeneratorConfig;

import java.util.Objects;
import java.util.Random;

/**
 * Generates 17-character Vehicle Identification Numbers (VINs) with a valid check digit.
 *
 * <p>The output uses the ISO 3779 character set (letters {@code I}, {@code O}, and {@code Q} are
 * excluded) and a position-9 check digit computed with the North American mod-11 algorithm, so the
 * result passes standard VIN validators.
 *
 * <pre>{@code
 *   String vin = new VinGenerator().generate(); // e.g. "1M8GDM9AXKP042788"
 * }</pre>
 */
public final class VinGenerator implements Generator<String> {

    /** Characters allowed in a VIN (excludes I, O, Q). */
    private static final char[] ALLOWED =
        "ABCDEFGHJKLMNPRSTUVWXYZ0123456789".toCharArray();

    /** Transliteration values for letters A–Z, indexed by {@code letter - 'A'} (I/O/Q unused). */
    private static final int[] LETTER_VALUES = {
        1, 2, 3, 4, 5, 6, 7, 8, 0, 1, 2, 3, 4, 5, 0, 7, 0, 9, 2, 3, 4, 5, 6, 7, 8, 9
    };

    /** Positional weights for the check-digit calculation; position 9 (index 8) carries weight 0. */
    private static final int[] WEIGHTS = {8, 7, 6, 5, 4, 3, 2, 10, 0, 9, 8, 7, 6, 5, 4, 3, 2};

    private final Random random;

    /**
     * Creates a generator using the default configuration.
     */
    public VinGenerator() {
        this(GeneratorConfig.defaults());
    }

    /**
     * Creates a generator from explicit configuration (optional seed).
     *
     * @param config the generator configuration; must not be {@code null}
     */
    public VinGenerator(GeneratorConfig config) {
        Objects.requireNonNull(config, "config must not be null");
        this.random = config.createRandom();
    }

    /**
     * Returns the transliteration value of a VIN character.
     *
     * @param c a digit or an allowed VIN letter
     * @return the numeric value used in the check-digit calculation
     */
    static int transliterate(char c) {
        // VIN characters are digits (0–9) or letters (A–Z minus I/O/Q); digits sort at or below '9'.
        if (c <= '9') {
            return c - '0';
        }
        return LETTER_VALUES[c - 'A'];
    }

    /**
     * Computes the position-9 check character for the given 17-character VIN array. The existing value
     * at index 8 is ignored (its weight is 0).
     *
     * @param chars a 17-element VIN character array
     * @return {@code 'X'} when the remainder is 10, otherwise a digit {@code '0'}–{@code '9'}
     */
    static char computeCheckChar(char[] chars) {
        int sum = 0;
        for (int i = 0; i < chars.length; i++) {
            sum += transliterate(chars[i]) * WEIGHTS[i];
        }
        int remainder = sum % 11;
        return remainder == 10 ? 'X' : (char) ('0' + remainder);
    }

    @Override
    public String generate() {
        char[] vin = new char[17];
        for (int i = 0; i < vin.length; i++) {
            vin[i] = ALLOWED[random.nextInt(ALLOWED.length)];
        }
        vin[8] = computeCheckChar(vin);
        return new String(vin);
    }
}
