package krandom.common

import krandom.properties.Properties
import java.util.*

open class Randomizer : KRandom {

    override val properties: Properties
        get() = Properties()

    private val random: Random by lazy { Random() }

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

    override fun randomFloat(start: Number, end: Number): Float {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun randomLong(start: Number, end: Number): Long {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

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
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun randomString(length: Number): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}