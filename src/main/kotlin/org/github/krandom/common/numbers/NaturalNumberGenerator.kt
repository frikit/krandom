package org.github.krandom.common.numbers

import org.github.krandom.common.Randomizer
import java.util.*
import kotlin.collections.ArrayList

var USE_NATURAL_NUMBER_CACHE: Boolean = true

object NaturalNumberGenerator {
    //TODO add method to generate random natural number
    private val kRandomCommon by lazy { Randomizer() }

    private const val MAX_CAPACITY = 999_999
    private val primeCache = ArrayList<Int>()
    private val compositeCache = ArrayList<Int>()

    //TODO refactor for simplified solution
    fun isPrimeNumber(number: Int): Boolean {
        var isPrime = true
        for (divisor in 2..number / 2) {
            if (number % divisor == 0) {
                isPrime = false
                break // num is not a prime, no reason to continue checking
            }
        }

        return isPrime
    }

    //TODO refactor for simplified solution
    fun isCompositeNumber(number: Int): Boolean {
        if (number == 2) return false
        else if (number > 2) {
            var i = 2
            while (i < number) {
                if (number % i == 0) return true
                i++
            }
        }
        return false
    }

    fun generatePrimeNumbers(from: Int = 2, to: Int = Byte.MAX_VALUE.toInt()): List<Int> {
        val (start, end) = validateStartEnd(from, to)

        if (primeCache.isEmpty() || primeCache.first() > start || primeCache.last() < end) {
            fillCache(true, start, end)
        }

        val primeWithinRange = primeCache.filter { it in start..end }.toList()
        checkClearCache()
        require(primeWithinRange.isNotEmpty()) { "No prime numbers within [$start .. $end]" }

        return primeWithinRange
    }

    fun generatePrimeNumber(from: Int = 2, to: Int = Byte.MAX_VALUE.toInt()): Int {
        val numbers = generatePrimeNumbers(from, to)

        val index = kRandomCommon.randomInt(0, numbers.size - 1)

        return numbers[index]
    }

    fun generateCompositeNumbers(from: Int = 3, to: Int = Byte.MAX_VALUE.toInt()): List<Int> {
        var (start, end) = validateStartEnd(from, to)
        if (start == 2) start++

        if (compositeCache.isEmpty() || compositeCache.first() > start || compositeCache.last() < end) {
            fillCache(false, start, end)
        }

        val primeWithinRange = compositeCache.filter { it in start..end }.toList()
        checkClearCache()
        require(primeWithinRange.isNotEmpty()) { "No prime numbers within [$start .. $end]" }

        return primeWithinRange
    }

    fun generateCompositeNumber(from: Int = 3, to: Int = Byte.MAX_VALUE.toInt()): Int {
        val numbers = generateCompositeNumbers(from, to)

        val index = kRandomCommon.randomInt(0, numbers.size - 1)

        return numbers[index]
    }

    private fun validateStartEnd(from: Int, to: Int): Pair<Int, Int> {
        require(from < to) { "from[$from] < to[$to]!" }
        val start = if (from < 2) 2 else from
        val end = if (to > Int.MAX_VALUE - 1) Int.MAX_VALUE else to
        return Pair(start, end)
    }

    private fun fillCache(isPrimeMethod: Boolean, start: Int, end: Int) {
        isCacheHaveEnoughSpace()

        if (isPrimeMethod) {
            fillCacheSieveOfEratosthenesMethod(start, end)
        } else {
            //TODO determine start and end depends on cache data and set only to range only between numbers which are not in cache
            //TODO for e.g. in cache is 2,3,4 you input is 3..10 the start should be 4 and end is 10
            //TODO for e.g. in cache is 3,4,5 you input is 2..10 the start should be 2 and end is 10
            //TODO for e.g. in cache is 2,3,4 you input is 79..99 the start should be 79 and end is 99
            //TODO this will permit optimization for this loop, when you have a big range and cache contains half of numbers it will make it x2 faster roughly

            //TODO measure every line under foreach for how optimize is these values

            (start..end).forEach {
                if (isCompositeNumber(it) && !compositeCache.contains(it)) {
                    compositeCache.add(it)
                    compositeCache.sort()
                }
            }
        }
    }

    /**
     * sieveOfEratosthenes method
     */
    private fun fillCacheSieveOfEratosthenesMethod(start: Int, end: Int) {
        val prime = BooleanArray(end + 1)
        Arrays.fill(prime, true)
        var p = start
        while (p * p <= end) {
            if (prime[p]) {
                var i = p * 2
                while (i <= end) {
                    prime[i] = false
                    i += p
                }
            }
            p++
        }
        //TODO determine start and end depends on cache data and set only to range only between numbers which are not in cache
        //TODO for e.g. in cache is 2,3,4 you input is 3..10 the start should be 4 and end is 10
        //TODO for e.g. in cache is 3,4,5 you input is 2..10 the start should be 2 and end is 10
        //TODO for e.g. in cache is 2,3,4 you input is 79..99 the start should be 79 and end is 99
        //TODO this will permit optimization for this loop, when you have a big range and cache contains half of numbers it will make it x2 faster roughly

        for (it in start..end) {
            if (prime[it] && isPrimeNumber(it) && !primeCache.contains(it)) {
                primeCache.add(it)
                primeCache.sort()
            }
        }
    }

    private fun isCacheHaveEnoughSpace() {
        if (primeCache.size >= MAX_CAPACITY) primeCache.clear()
        if (compositeCache.size >= MAX_CAPACITY) compositeCache.clear()
    }

    private fun checkClearCache() {
        if (!USE_NATURAL_NUMBER_CACHE) {
            primeCache.clear()
            compositeCache.clear()
        }
    }
}
