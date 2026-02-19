package org.github.krandom.common.generators

import org.github.krandom.common.TypeGenerator
import java.security.SecureRandom
import java.util.Random

/**
 * Generates random [Boolean] values with equal probability for true and false.
 */
class BooleanGenerator(seed: Long? = null) : TypeGenerator<Boolean> {

    private val random: Random = if (seed != null) Random(seed) else SecureRandom()

    override fun generate(): Boolean = random.nextBoolean()
}
