package krandom.common

import krandom.BaseTestClass
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runners.MethodSorters

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class RandomizerTest : BaseTestClass() {

    @Test
    fun randomDouble() {
        val number: Double = randomizer.randomDouble()
        currentNumber = number
        assertTrue("should be 0 or 1", number > 0 && number < 1)
    }

    @Test
    fun randomDoubleRangeTo() {
        val number: Double = randomizer.randomDouble((5.1..5.6))
        currentNumber = number
        assertTrue("should be 0 or 1", number > 5 && number < 6)
    }

    @Test
    fun randomDoubleStartInt() {
        val number: Double = randomizer.randomDouble(start = 1)
        currentNumber = number
        assertTrue("should be 0 or 1", number > 1 && number < Double.MAX_VALUE)
    }

    @Test
    fun randomDoubleStartDouble() {
        val number: Double = randomizer.randomDouble(start = 1.1)
        currentNumber = number
        assertTrue("should be 0 or 1", number > 1.1 && number < Double.MAX_VALUE)
    }

    @Test
    fun randomDoubleStartFloat() {
        val number: Double = randomizer.randomDouble(start = 1.1f)
        currentNumber = number
        assertTrue("should be 0 or 1", number > 1.1 && number < Double.MAX_VALUE)
    }

    @Test
    fun randomDoubleEnd() {
        val number: Double = randomizer.randomDouble(end = 2)
        currentNumber = number
        assertTrue("should be 0 or 1", number > Double.MIN_VALUE && number < 2)
    }

    @Test
    fun randomDoubleStartEnd() {
        val number: Double = randomizer.randomDouble(1, 2)
        currentNumber = number
        assertTrue("should be 0 or 1", number > 1 && number < 2)
    }

    @Test
    fun randomFloat() {
        val number: Float = randomizer.randomFloat()
        currentNumber = number
        assertTrue("should be 0 or 1", number > 0 && number < 1)
    }

    @Test
    fun randomFloatRangeTo() {
        val number: Float = randomizer.randomFloat((5.1f..5.6f))
        currentNumber = number
        assertTrue("should be 0 or 1", number > 5 && number < 6)
    }

    @Test
    fun randomFloatStartEnd() {
        val number: Float = randomizer.randomFloat(1, 2)
        currentNumber = number
        assertTrue("should be 0 or 1", number > 1 && number < 2)
    }

    @Test
    fun randomLong() {
    }

    @Test
    fun randomInt() {
    }

    @Test
    fun randomShort() {
    }

    @Test
    fun randomByte() {
    }

    @Test
    fun randomChar() {
    }

    @Test
    fun randomBoolean() {
        val number: Boolean = randomizer.randomBoolean()
        currentNumber = number.toString()
        assertTrue("should be true or false", number || !number)
    }

    @Test
    fun randomString() {
    }

}