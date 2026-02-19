package org.github.krandom.generator.base;

/**
 * Generates random {@link Double} values.
 *
 * <p>Default range: [{@code 0.0}, {@code 1.0}) — matching Java's {@code Random.nextDouble()}.
 * Specify a custom range via the two-/three-arg constructors.
 *
 * <pre>{@code
 *   double unit        = new DoubleGenerator().generate();          // [0, 1)
 *   double probability = new DoubleGenerator(0.0, 1.0).generate();
 *   double coordinate  = new DoubleGenerator(-180.0, 180.0).generate();
 * }</pre>
 */
public final class DoubleGenerator extends AbstractBoundedGenerator<Double> {

    public DoubleGenerator() {
        super(0.0, 1.0, null);
    }

    public DoubleGenerator(double min, double max) {
        super(min, max, null);
    }

    public DoubleGenerator(double min, double max, long seed) {
        super(min, max, seed);
    }

    /**
     * Generate a double in the half-open range [{@code min}, {@code max}).
     *
     * @throws IllegalArgumentException if {@code min == max}
     */
    @Override
    public Double generate(Double min, Double max) {
        validate(min, max);
        double lo = lo(min, max);
        double hi = hi(min, max);
        return random.nextDouble(lo, hi);
    }
}
