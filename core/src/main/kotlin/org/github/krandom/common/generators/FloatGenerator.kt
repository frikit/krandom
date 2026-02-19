package org.github.krandom.common.generators

import org.github.krandom.common.RangedTypeGenerator
import java.security.SecureRandom
import java.util.Random

/**
 * Generates random [Float] values.
 *
 * - [generate()] returns a value in [0.0, 1.0).
 * - [generate(start, end)] uses a half-open range [start, end): start is inclusive, end is exclusive.
 */
class FloatGenerator(
    private val start: Float = 0f,
    private val end: Float = 1f,
    seed: Long? = null
) : RangedTypeGenerator<Float> {

    private val random: Random = if (seed != null) Random(seed) else SecureRandom()

    override fun generate(): Float = generate(start, end)

    override fun generate(start: Float, end: Float): Float {
        require(start != end) { "start ($start) and end ($end) must differ" }
        val lo = minOf(start, end)
        val hi = maxOf(start, end)
        return (lo + (hi - lo) * random.nextDouble()).toFloat()
    }
}
