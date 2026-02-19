/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.common.generators

/**
 * Factory for all built-in base-type generators.
 *
 * Every factory method accepts an optional [seed] for reproducible output — useful in tests.
 * Without a seed, [java.security.SecureRandom] is used.
 *
 * Usage:
 *   val intGen    = Generators.int(0, 100)
 *   val stringGen = Generators.string(minLength = 8, maxLength = 16)
 *   val values    = List(10) { intGen.generate() }
 */
object Generators {

    fun byte(
        start: Byte = Byte.MIN_VALUE,
        end: Byte = Byte.MAX_VALUE,
        seed: Long? = null
    ): ByteGenerator = ByteGenerator(start, end, seed)

    fun short(
        start: Short = Short.MIN_VALUE,
        end: Short = Short.MAX_VALUE,
        seed: Long? = null
    ): ShortGenerator = ShortGenerator(start, end, seed)

    fun int(
        start: Int = Int.MIN_VALUE,
        end: Int = Int.MAX_VALUE,
        seed: Long? = null
    ): IntGenerator = IntGenerator(start, end, seed)

    fun long(
        start: Long = Long.MIN_VALUE,
        end: Long = Long.MAX_VALUE,
        seed: Long? = null
    ): LongGenerator = LongGenerator(start, end, seed)

    fun float(
        start: Float = 0f,
        end: Float = 1f,
        seed: Long? = null
    ): FloatGenerator = FloatGenerator(start, end, seed)

    fun double(
        start: Double = 0.0,
        end: Double = 1.0,
        seed: Long? = null
    ): DoubleGenerator = DoubleGenerator(start, end, seed)

    fun char(
        includeUppercase: Boolean = true,
        includeLowercase: Boolean = true,
        includeDigits: Boolean = false,
        includeSpecial: Boolean = false,
        seed: Long? = null
    ): CharGenerator = CharGenerator(includeUppercase, includeLowercase, includeDigits, includeSpecial, seed)

    fun boolean(seed: Long? = null): BooleanGenerator = BooleanGenerator(seed)

    fun string(
        minLength: Int = 5,
        maxLength: Int = 15,
        charGenerator: CharGenerator = CharGenerator(),
        seed: Long? = null
    ): StringGenerator = StringGenerator(minLength, maxLength, charGenerator, seed)
}
