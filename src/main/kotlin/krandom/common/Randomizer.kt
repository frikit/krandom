package krandom.common

import krandom.utils.RandomizerUtils.checkThrowException
import krandom.utils.RandomizerUtils.getCharCombinations
import krandom.utils.RandomizerUtils.validateLength
import krandom.utils.apache.RandomStringUtils
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
            randomInt(0, combinations.size - 1) - 1
        }

        if (chooseWhich < 0 || chooseWhich >= combinations.size) {
            //TODO find a way to test this block of code
            throw IllegalArgumentException("Index which was choose to get combination pair is wrong $chooseWhich")
        }

        val first = combinations[chooseWhich].first.toLong()
        val second = combinations[chooseWhich].second.toLong()

        val number = randomLong(first, second)
        return number.toChar()
    }

    //boolean
    override fun randomBoolean(): Boolean {
        return random.nextBoolean()
    }

    //string
    override fun randomString(length: Int, specialCharacters: Boolean, numbers: Boolean): String {
        validateLength(length)

        var res = RandomStringUtils.random(length, true, numbers)
        while (numbers && !res.contains("[0-9]+".toRegex())) {
            //regenerate as should contains at least one number
            res = RandomStringUtils.random(length, true, numbers)
        }
        return res
    }

    private fun normalizeStartEnd(start: Double, end: Double): Pair<Double, Double> {
        var startElem: Double = if (start == 0.0) start + 1.0 else start
        var endElem = if (end == 0.0) end + 1.0 else end
        if (startElem == endElem) endElem++
        //swap vars to act easier
        if (startElem > endElem) startElem = endElem.also { endElem = startElem }
        return Pair(startElem, endElem)
    }

    private fun normalizeStartEnd(start: Long, end: Long): Pair<Long, Long> {
        var startElem: Long = if (start == 0L) start + 1L else start
        var endElem = if (end == 0L) end + 1L else end
        if (startElem == endElem) endElem++
        //swap vars to act easier
        if (startElem > endElem) startElem = endElem.also { endElem = startElem }
        return Pair(startElem, endElem)
    }

    private fun normalizeStartEnd(start: Int, end: Int): Pair<Int, Int> {
        var startElem = if (start == 0) start + 1 else start
        var endElem = if (end == 0) end + 1 else end
        if (startElem == endElem) endElem++
        //swap vars to act easier
        if (startElem > endElem) startElem = endElem.also { endElem = startElem }
        return Pair(startElem, endElem)
    }
}