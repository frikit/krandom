package krandom.common

import krandom.properties.Properties
import java.nio.charset.Charset
import java.util.Random



open class Randomizer : KRandom {

    override val properties: Properties
        get() = Properties.getInstance()

    private val random: Random by lazy { Random() }

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
        return start + (end - start) * randomDouble()
    }

    //float
    override fun randomFloat(): Float {
        return random.nextFloat()
    }

    override fun randomFloat(rangeTo: ClosedRange<Float>): Float {
        return randomFloat(rangeTo.start, rangeTo.endInclusive)
    }

    override fun randomFloat(start: Float, end: Float): Float {
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
        return start + (randomDouble() * (start - end)).toLong()
    }

    //int
    override fun randomInt(): Int {
        return random.nextInt()
    }

    override fun randomInt(rangeTo: ClosedRange<Int>): Int {
        return randomInt(rangeTo.start, rangeTo.endInclusive)
    }

    override fun randomInt(start: Int, end: Int): Int {
        return random.nextInt((start - end) + 1) + end
    }

    //short
    override fun randomShort(): Short {
        return randomShort(start = properties.minShort, end = properties.maxShort)
    }

    override fun randomShort(rangeTo: ClosedRange<Short>): Short {
        return randomShort(rangeTo.start, rangeTo.endInclusive)
    }

    override fun randomShort(start: Short, end: Short): Short {
        return (random.nextInt((start.toInt() - end.toInt()) + 1) + end).toShort()
    }

    //byte
    override fun randomByte(rangeTo: ClosedRange<Byte>): Byte {
        return randomByte(rangeTo.start, rangeTo.endInclusive)
    }

    override fun randomByte(start: Byte, end: Byte): Byte {
        return (random.nextInt((start.toInt() - end.toInt()) + 1) + end).toByte()
    }

    //char
    override fun randomChar(numberOfChars: Number): Char {
        return randomInt().toChar()
    }

    //boolean
    override fun randomBoolean(): Boolean {
        return random.nextBoolean()
    }

    //string
    override fun randomString(length: Int, specialCharacters: Boolean, numbers: Boolean): String {
        val array = ByteArray(length) // length is bounded by 7
        random.nextBytes(array)
        return String(array, Charset.forName("UTF-8"))
    }



}