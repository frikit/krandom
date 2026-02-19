package org.github.krandom.common.generators

import org.github.krandom.common.TypeGenerator
import java.security.SecureRandom
import java.util.Random

/**
 * Generates random [Char] values drawn from a configurable character set.
 *
 * At least one character group must be enabled.
 */
class CharGenerator(
    val includeUppercase: Boolean = true,
    val includeLowercase: Boolean = true,
    val includeDigits: Boolean = false,
    val includeSpecial: Boolean = false,
    seed: Long? = null
) : TypeGenerator<Char> {

    private val random: Random = if (seed != null) Random(seed) else SecureRandom()

    private val charset: List<Char> by lazy {
        val chars = mutableListOf<Char>()
        if (includeUppercase) chars += ('A'..'Z').toList()
        if (includeLowercase) chars += ('a'..'z').toList()
        if (includeDigits)    chars += ('0'..'9').toList()
        if (includeSpecial)   chars += "!@#\$%^&*()-_=+[]{}|;:',.<>?/`~".toList()
        require(chars.isNotEmpty()) { "At least one character group must be enabled" }
        chars
    }

    override fun generate(): Char = charset[random.nextInt(charset.size)]
}
