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
    fun randomDoubleStartEnd() {
        val number: Double = randomizer.randomDouble(1, 2)
        currentNumber = number
        assertTrue("should be 0 or 1", number > 1 && number < 2)
    }

    @Test
    fun randomDouble1() {
    }

    @Test
    fun randomFloat() {
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
    }

    @Test
    fun randomString() {
    }

}