package org.github.krandom.common

import org.github.krandom.properties.Properties

interface KRandomCommon {

    //Numbers

    //double
    fun randomDouble(): Double

    fun randomDouble(rangeTo: ClosedRange<Double>): Double

    fun randomDouble(start: Double = Properties.minDouble, end: Double = Properties.maxDouble): Double

    //float
    fun randomFloat(): Float

    fun randomFloat(rangeTo: ClosedRange<Float>): Float

    fun randomFloat(start: Float = Properties.minFloat, end: Float = Properties.maxFloat): Float

    //long
    fun randomLong(): Long

    fun randomLong(rangeTo: ClosedRange<Long>): Long

    fun randomLong(start: Long = Properties.minLong, end: Long = Properties.maxLong): Long

    //int
    fun randomInt(): Int

    fun randomInt(rangeTo: ClosedRange<Int>): Int

    fun randomInt(start: Int = Properties.minInt, end: Int = Properties.maxInt): Int

    //Chars
    fun randomChar(upperLetters: Boolean = true,
                   lowerLetters: Boolean = true,
                   numbers: Boolean = false,
                   specialCharacters: Boolean = false): Char

    //Boolean
    fun randomBoolean(): Boolean

    //Strings
    fun randomString(length: Int = 5, specialCharacters: Boolean = true, numbers: Boolean = true): String
}
