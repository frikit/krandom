package krandom.common

import krandom.properties.Properties
import java.util.*

open class Randomizer : KRandom {

    override val properties: Properties
        get() = Properties()

    private val random: Random by lazy { Random() }

    //double
    override fun randomDouble(): Double {
        return random.nextDouble()
    }

    override fun randomDouble(rangeTo: ClosedRange<Double>): Double {
        return randomDouble(rangeTo.start, rangeTo.endInclusive)
    }

    override fun randomDouble(start: Number, end: Number): Double {
        val first = start.toDouble()
        val second = end.toDouble()
        return first + (second - first) * randomDouble()
    }

    //float
    override fun randomFloat(): Float {
        return random.nextFloat()
    }

    override fun randomFloat(rangeTo: ClosedRange<Float>): Float {
        return randomFloat(rangeTo.start, rangeTo.endInclusive)
    }

    override fun randomFloat(start: Number, end: Number): Float {
        val first = start.toFloat()
        val second = end.toFloat()
        return (first + (second - first) * randomDouble()).toFloat()
    }

    //long
    override fun randomLong(): Long {
        return random.nextLong()
    }

    override fun randomLong(rangeTo: ClosedRange<Long>): Long {
        return randomLong(rangeTo.start, rangeTo.endInclusive)
    }

    override fun randomLong(start: Number, end: Number): Long {
        return start.toLong() + (randomDouble() * (start.toLong() - end.toLong())).toLong()
    }

    //int
    override fun randomInt(start: Number, end: Number): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun randomShort(start: Number, end: Number): Short {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun randomByte(start: Number, end: Number): Byte {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun randomChar(numberOfChars: Number): Char {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun randomBoolean(): Boolean {
        return random.nextBoolean()
    }

    override fun randomString(length: Number): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}