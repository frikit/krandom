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
        val combinations: List<Pair<Int, Int>> =
                getCharCombinations(upperLetters, lowerLetters, numbers, specialCharacters)
        val chooseWhich: Int = if (combinations.size == 1) {
            0
        } else {
            randomInt(0, combinations.size - 1)
        }

        require(!(chooseWhich < 0 || chooseWhich >= combinations.size)) {
            //TODO test somehow this method, if there is no way to test it with mocks
            "Index which was choose to get combination pair is wrong $chooseWhich"
        }

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

    private fun normalizeStartEnd(start: Double, end: Double): Pair<Double, Double> {
        var startElem: Double = start
        var endElem = end
        //swap vars to act easier
        if (startElem > endElem) startElem = endElem.also { endElem = startElem }
        return Pair(startElem, endElem)
    }

    private fun normalizeStartEnd(start: Long, end: Long): Pair<Long, Long> {
        var startElem: Long = start
        var endElem = end
        //swap vars to act easier
        if (startElem > endElem) startElem = endElem.also { endElem = startElem }
        return Pair(startElem, endElem)
    }

    private fun normalizeStartEnd(start: Int, end: Int): Pair<Int, Int> {
        var startElem = start
        var endElem = end
        //swap vars to act easier
        if (startElem > endElem) startElem = endElem.also { endElem = startElem }
        return Pair(startElem, endElem)
    }
}
