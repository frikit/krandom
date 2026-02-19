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
 * Generates random [Int] values.
 *
 * - [generate()] generates within the configured [start, end) range (defaults: full Int range).
 * - [generate(start, end)] uses a half-open range [start, end): start is inclusive, end is exclusive.
 */
class IntGenerator(
    private val start: Int = Int.MIN_VALUE,
    private val end: Int = Int.MAX_VALUE,
    seed: Long? = null
) : RangedTypeGenerator<Int> {

    private val random: Random = if (seed != null) Random(seed) else SecureRandom()

    override fun generate(): Int = generate(start, end)

    override fun generate(start: Int, end: Int): Int {
        require(start != end) { "start ($start) and end ($end) must differ" }
        val lo = minOf(start, end)
        val hi = maxOf(start, end)
        return random.ints(1, lo, hi).findFirst().orElse(lo)
    }
}
