/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.base;

/**
 * Generates random {@link Float} values.
 *
 * <p>Default range: [{@code 0.0f}, {@code 1.0f}) — matching Java's {@code Random.nextFloat()}.
 * Specify a custom range via the two-/three-arg constructors.
 *
 * <p><b>Note:</b> avoid ranges where {@code max - min} overflows {@code Float.MAX_VALUE};
 * use {@link DoubleGenerator} for very wide ranges.
 *
 * <pre>{@code
 *   float unit   = new FloatGenerator().generate();         // [0, 1)
 *   float celsius = new FloatGenerator(-40f, 50f).generate();
 * }</pre>
 */
public final class FloatGenerator extends AbstractBoundedGenerator<Float> {

    public FloatGenerator() {
        super(0f, 1f, null);
    }

    public FloatGenerator(float min, float max) {
        super(min, max, null);
    }

    public FloatGenerator(float min, float max, long seed) {
        super(min, max, seed);
    }

    /**
     * Generate a float in the half-open range [{@code min}, {@code max}).
     *
     * @throws IllegalArgumentException if {@code min == max}
     */
    @Override
    public Float generate(Float min, Float max) {
        validate(min, max);
        float lo = lo(min, max);
        float hi = hi(min, max);
        return random.nextFloat(lo, hi);
    }
}
