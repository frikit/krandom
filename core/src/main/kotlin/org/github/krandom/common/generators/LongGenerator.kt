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
 * Generates random [Long] values.
 *
 * - [generate()] generates within the configured [start, end) range (defaults: full Long range).
 * - [generate(start, end)] uses a half-open range [start, end): start is inclusive, end is exclusive.
 */
class LongGenerator(
    private val start: Long = Long.MIN_VALUE,
    private val end: Long = Long.MAX_VALUE,
    seed: Long? = null
) : RangedTypeGenerator<Long> {

    private val random: Random = if (seed != null) Random(seed) else SecureRandom()

    override fun generate(): Long = generate(start, end)

    override fun generate(start: Long, end: Long): Long {
        require(start != end) { "start ($start) and end ($end) must differ" }
        val lo = minOf(start, end)
        val hi = maxOf(start, end)
        return random.longs(1, lo, hi).findFirst().orElse(lo)
    }
}
