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
 * Generates random [Short] values.
 *
 * [generate(start, end)] uses a half-open range [start, end): start is inclusive, end is exclusive.
 */
class ShortGenerator(
    private val start: Short = Short.MIN_VALUE,
    private val end: Short = Short.MAX_VALUE,
    seed: Long? = null
) : RangedTypeGenerator<Short> {

    private val random: Random = if (seed != null) Random(seed) else SecureRandom()

    override fun generate(): Short = generate(start, end)

    override fun generate(start: Short, end: Short): Short {
        require(start != end) { "start ($start) and end ($end) must differ" }
        val lo = minOf(start, end).toInt()
        val hi = maxOf(start, end).toInt()
        return random.ints(1, lo, hi).findFirst().orElse(lo).toShort()
    }
}
