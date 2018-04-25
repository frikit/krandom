package krandom.common

import krandom.properties.Properties

interface KRandom {

    val properties: Properties

    //Numbers
    //double
    fun randomDouble(): Double

    fun randomDouble(rangeTo: ClosedRange<Double>): Double

    fun randomDouble(start: Number = properties.minDouble, end: Number = properties.maxDouble): Double

    //float
    fun randomFloat(): Float

    fun randomFloat(rangeTo: ClosedRange<Float>): Float

    fun randomFloat(start: Number = properties.minFloat, end: Number = properties.maxFloat): Float

    //long
    fun randomLong(start: Number = properties.minLong, end: Number = properties.maxLong): Long

    //int
    fun randomInt(start: Number = properties.minInt, end: Number = properties.maxInt): Int

    //short
    fun randomShort(start: Number = properties.minShort, end: Number = properties.maxShort): Short

    //byte
    fun randomByte(start: Number = properties.minByte, end: Number = properties.maxByte): Byte

    //Chars
    fun randomChar(numberOfChars: Number = 1): Char

    //Boolean
    fun randomBoolean(): Boolean

    //Strings
    fun randomString(length: Number = 1): String
}