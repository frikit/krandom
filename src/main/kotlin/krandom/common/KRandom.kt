package krandom.common

import krandom.properties.Properties

interface KRandom {

    val properties: Properties

    //Numbers

    //double
    fun randomDouble(): Double

    fun randomDouble(rangeTo: ClosedRange<Double>): Double

    fun randomDouble(start: Double = properties.minDouble, end: Double = properties.maxDouble): Double

    //float
    fun randomFloat(): Float

    fun randomFloat(rangeTo: ClosedRange<Float>): Float

    fun randomFloat(start: Float = properties.minFloat, end: Float = properties.maxFloat): Float

    //long
    fun randomLong(): Long

    fun randomLong(rangeTo: ClosedRange<Long>): Long

    fun randomLong(start: Long = properties.minLong, end: Long = properties.maxLong): Long

    //int
    fun randomInt(): Int

    fun randomInt(rangeTo: ClosedRange<Int>): Int

    fun randomInt(start: Int = properties.minInt, end: Int = properties.maxInt): Int

    //short
    fun randomShort(): Short

    fun randomShort(rangeTo: ClosedRange<Short>): Short

    fun randomShort(start: Short = properties.minShort, end: Short = properties.maxShort): Short

    //byte
    fun randomByte(rangeTo: ClosedRange<Byte>): Byte

    fun randomByte(start: Byte = properties.minByte, end: Byte = properties.maxByte): Byte

    //Chars
    fun randomChar(numberOfChars: Number = 1): Char

    //Boolean
    fun randomBoolean(): Boolean

    //Strings
    fun randomString(length: Int = 1, specialCharacters: Boolean = true, numbers: Boolean = true): String
}