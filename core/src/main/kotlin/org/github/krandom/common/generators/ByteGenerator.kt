package org.github.krandom.common.generators

import org.github.krandom.common.RangedTypeGenerator
import java.security.SecureRandom
import java.util.Random

/**
 * Generates random [Byte] values.
 *
 * [generate(start, end)] uses a half-open range [start, end): start is inclusive, end is exclusive.
 */
class ByteGenerator(
    private val start: Byte = Byte.MIN_VALUE,
    private val end: Byte = Byte.MAX_VALUE,
    seed: Long? = null
) : RangedTypeGenerator<Byte> {

    private val random: Random = if (seed != null) Random(seed) else SecureRandom()

    override fun generate(): Byte = generate(start, end)

    override fun generate(start: Byte, end: Byte): Byte {
        require(start != end) { "start ($start) and end ($end) must differ" }
        val lo = minOf(start, end).toInt()
        val hi = maxOf(start, end).toInt()
        return random.ints(1, lo, hi).findFirst().orElse(lo).toByte()
    }
}
