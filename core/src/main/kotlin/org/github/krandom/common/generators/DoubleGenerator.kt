/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.common.generators

import org.github.krandom.common.RangedTypeGenerator
import java.security.SecureRandom
import java.util.Random

/**
 * Generates random [Double] values.
 *
 * - [generate()] returns a value in [0.0, 1.0).
 * - [generate(start, end)] uses a half-open range [start, end): start is inclusive, end is exclusive.
 */
class DoubleGenerator(
    private val start: Double = 0.0,
    private val end: Double = 1.0,
    seed: Long? = null
) : RangedTypeGenerator<Double> {

    private val random: Random = if (seed != null) Random(seed) else SecureRandom()

    override fun generate(): Double = generate(start, end)

    override fun generate(start: Double, end: Double): Double {
        require(start != end) { "start ($start) and end ($end) must differ" }
        val lo = minOf(start, end)
        val hi = maxOf(start, end)
        return random.doubles(1, lo, hi).findFirst().orElse(lo)
    }
}
