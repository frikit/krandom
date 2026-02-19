/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.common.generators

import org.github.krandom.common.TypeGenerator
import java.security.SecureRandom
import java.util.Random

/**
 * Generates random [String] values using a [CharGenerator] as its character source.
 *
 * Length is chosen uniformly in the range [[minLength], [maxLength]] (both inclusive).
 *
 * Example:
 *   val gen = StringGenerator(minLength = 8, maxLength = 16, charGenerator = CharGenerator(includeDigits = true))
 *   val password = gen.generate()
 */
class StringGenerator(
    val minLength: Int = 5,
    val maxLength: Int = 15,
    private val charGenerator: CharGenerator = CharGenerator(),
    seed: Long? = null
) : TypeGenerator<String> {

    init {
        require(minLength >= 1) { "minLength must be >= 1, was $minLength" }
        require(maxLength >= minLength) { "maxLength ($maxLength) must be >= minLength ($minLength)" }
    }

    private val random: Random = if (seed != null) Random(seed) else SecureRandom()

    override fun generate(): String {
        val length = if (minLength == maxLength) minLength
                     else random.nextInt(maxLength - minLength + 1) + minLength
        return buildString(length) { repeat(length) { append(charGenerator.generate()) } }
    }
}
