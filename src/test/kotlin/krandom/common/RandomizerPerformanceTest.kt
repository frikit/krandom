package krandom.common

import krandom.BaseTestClass
import org.junit.Assert.assertTrue
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runners.MethodSorters

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class RandomizerPerformanceTest : BaseTestClass() {

    @Test
    fun randomDouble() {
        val number: Double = randomizer.randomDouble()
        assertTrue("should be 0 or 1", number > 0 && number < 1)
    }

    @Test
    fun randomDouble_1k_times() {
        val number: Double = randomizer.randomDouble()
        for (i in 1..1_000) kDoubles.add(randomizer.randomDouble())
        println(kDoubles.size)
        assertTrue("size should be 1k", kDoubles.size == 1_000)
        assertTrue("should be 0 or 1", number > 0 && number < 1)
    }

    @Test
    fun randomDouble_10k_times() {
        val number: Double = randomizer.randomDouble()
        for (i in 1..10_000) kDoubles.add(randomizer.randomDouble())
        println(kDoubles.size)
        assertTrue("size should be 10k", kDoubles.size == 10_000)
        assertTrue("should be 0 or 1", number > 0 && number < 1)
    }

    @Test
    fun randomDouble_100k_times() {
        val number: Double = randomizer.randomDouble()
        for (i in 1..100_000) kDoubles.add(randomizer.randomDouble())
        println(kDoubles.size)
        assertTrue("size should be 100k", kDoubles.size == 100_000)
        assertTrue("should be 0 or 1", number > 0 && number < 1)
    }
}