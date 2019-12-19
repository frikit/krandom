package org.github.krandom.common

import org.github.krandom.utils.apache.RandomStringUtils
import org.github.krandom.utils.checkThrowException
import org.github.krandom.utils.generateRandomString
import org.github.krandom.utils.getCharCombinations
import org.github.krandom.utils.validateLength
import java.security.SecureRandom
import java.util.*

open class Randomizer : KRandomCommon {

    private val random: Random by lazy { SecureRandom() }

    //double
    /**
     * This return standard implementation of Random.class().toDouble() in java
     * @return Double in range >=0 and <1
     */
    override fun randomDouble(): Double {
        return random.nextDouble()
    }

    override fun randomDouble(rangeTo: ClosedRange<Double>): Double {
        return randomDouble(rangeTo.start, rangeTo.endInclusive)
    }

    override fun randomDouble(start: Double, end: Double): Double {
        checkThrowException(start, end)
        val (startElem, endElem) = normalizeStartEnd(start, end)

        return random.doubles(1, startElem, endElem).findFirst().orElse(999.99)
    }

    //float
    override fun randomFloat(): Float {
        return random.nextFloat()
    }

    override fun randomFloat(rangeTo: ClosedRange<Float>): Float {
        return randomFloat(rangeTo.start, rangeTo.endInclusive)
    }

    override fun randomFloat(start: Float, end: Float): Float {
        checkThrowException(start, end)
        return (start + (end - start) * randomDouble()).toFloat()
    }

    //long
    override fun randomLong(): Long {
        return random.nextLong()
    }

    override fun randomLong(rangeTo: ClosedRange<Long>): Long {
        return randomLong(rangeTo.start, rangeTo.endInclusive)
    }

    override fun randomLong(start: Long, end: Long): Long {
        checkThrowException(start, end)
        val (startElem, endElem) = normalizeStartEnd(start, end)

        return random.longs(1, startElem, endElem).findFirst().orElse(999L)
    }

    //int
    override fun randomInt(): Int {
        return random.nextInt()
    }

    override fun randomInt(rangeTo: ClosedRange<Int>): Int {
        return randomInt(rangeTo.start, rangeTo.endInclusive)
    }

    override fun randomInt(start: Int, end: Int): Int {
        checkThrowException(start, end)
        val (startElem, endElem) = normalizeStartEnd(start, end)

        return random.ints(1, startElem, endElem).findFirst().orElse(999)
    }

    //char
    override fun randomChar(upperLetters: Boolean,
                            lowerLetters: Boolean,
                            numbers: Boolean,
                            specialCharacters: Boolean): Char {
        val combinations: List<Pair<Int, Int>> = getCharCombinations(upperLetters, lowerLetters, numbers, specialCharacters)
        val chooseWhich: Int = chooseWhichIndex(combinations)

        val first = combinations[chooseWhich].first
        val second = combinations[chooseWhich].second

        val number = randomInt(first, second)
        return number.toChar()
    }

    //boolean
    override fun randomBoolean(): Boolean {
        return random.nextBoolean()
    }

    //string
    override fun randomString(length: Int, specialCharacters: Boolean, numbers: Boolean): String {
        validateLength(length)

        val function: () -> String = { RandomStringUtils.random(length, 0, 0, true, numbers) }
        return generateRandomString(function, numbers)
    }

    private fun <T : Comparable<T>> normalizeStartEnd(start: T, end: T): Pair<T, T> {
        var startElem = start
        var endElem = end
        //swap vars to act easier
        if (startElem > endElem) startElem = endElem.also { endElem = startElem }
        return Pair(startElem, endElem)
    }

    private fun chooseWhichIndex(combinations: List<Pair<Int, Int>>): Int {
        var chooseWhich: Int = chooseWhichOne(combinations)

        repeat(10) {
            if (chooseWhich >= 0 && chooseWhich < combinations.size) return@repeat
            chooseWhich = chooseWhichOne(combinations)
        }

        if (chooseWhich < 0) {
            //TODO test somehow this method, if there is no way to test it with mocks
            throw IllegalArgumentException("Index can't be < 0 ==> [$chooseWhich]")
        }
        if (chooseWhich >= combinations.size) {
            //TODO test somehow this method, if there is no way to test it with mocks
            throw IllegalArgumentException("Index can't be >= ${combinations.size} ==> [$chooseWhich]")
        }

        return chooseWhich
    }

    private fun chooseWhichOne(combinations: List<Pair<Int, Int>>): Int =
            if (combinations.size == 1) {
                0
            } else {
                randomInt(0, combinations.size)
            }
}
