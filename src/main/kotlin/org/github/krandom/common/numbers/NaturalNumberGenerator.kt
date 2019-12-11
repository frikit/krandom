package org.github.krandom.common.numbers

import org.github.krandom.common.Randomizer
import java.util.*
import kotlin.collections.ArrayList

var USE_NATURAL_NUMBER_CACHE: Boolean = true

object NaturalNumberGenerator {
    private val kRandomCommon by lazy { Randomizer() }

    private const val MAX_CAPACITY = 999_999
    private val primeCache = ArrayList<Long>()
    private val compositeCache = ArrayList<Long>()

    fun isNaturalNumber(number: Long): Boolean {
        return number > 0 && number < Long.MAX_VALUE
    }

    fun generateNaturalNumbers(size: Int, from: Long = 0, to: Long = Long.MAX_VALUE, excludeNumbers: List<Long> = emptyList()): List<Long> {
        val result = arrayListOf<Long>()

        if (size > 0 || !excludeNumbers.isNullOrEmpty()) {
            val (start, end) = validateStartEnd(from, to, 0, Long.MAX_VALUE - 2)

            for (i in 1..size * 10) {
                val number = randomNaturalLong(start, end)
                if (!excludeNumbers.contains(number)) {
                    result.add(number)
                }

                if (result.size == size) {
                    break
                }
            }
        }

        return result
    }

    fun generateNaturalNumber(from: Long = 0, to: Long = Long.MAX_VALUE, excludeNumbers: List<Long> = emptyList()): Long {
        return generateNaturalNumbers(1, from, to, excludeNumbers)[0]
    }

    //TODO refactor for simplified solution
    fun isPrimeNumber(number: Long): Boolean {
        var isPrime = true
        for (divisor in 2..number / 2) {
            if (number % divisor == 0L) {
                isPrime = false
                break // num is not a prime, no reason to continue checking
            }
        }

        return isPrime
    }

    //TODO refactor for simplified solution
    fun isCompositeNumber(number: Long): Boolean {
        if (number == 2L) return false
        else if (number > 2) {
            var i = 2
            while (i < number) {
                if (number % i == 0L) return true
                i++
            }
        }
        return false
    }

    fun generatePrimeNumbers(from: Long = 2, to: Long = Byte.MAX_VALUE.toLong()): List<Long> {
        val (start, end) = validateStartEnd(from, to)

        //TODO extract method is cache empty
        if (primeCache.isEmpty() || primeCache.first() > start || primeCache.last() < end) {
            //TODO pass cache as param
            fillCache(true, start, end)
        }

        //TODO optimize with index or something
        val primeWithinRange = primeCache.filter { it in start..end }.toList()
        checkClearCache()
        require(primeWithinRange.isNotEmpty()) { "No prime numbers within [$start .. $end]" }

        return primeWithinRange
    }

    fun generatePrimeNumber(from: Long = 2, to: Long = Byte.MAX_VALUE.toLong()): Long {
        val numbers = generatePrimeNumbers(from, to)

        val index = kRandomCommon.randomInt(0, numbers.size - 1)

        return numbers[index]
    }

    fun generateCompositeNumbers(from: Long = 3, to: Long = Byte.MAX_VALUE.toLong()): List<Long> {
        var (start, end) = validateStartEnd(from, to)
        if (start == 2L) start++

        if (compositeCache.isEmpty() || compositeCache.first() > start || compositeCache.last() < end) {
            fillCache(false, start, end)
        }

        val primeWithinRange = compositeCache.filter { it in start..end }.toList()
        checkClearCache()
        require(primeWithinRange.isNotEmpty()) { "No prime numbers within [$start .. $end]" }

        return primeWithinRange
    }

    fun generateCompositeNumber(from: Long = 3, to: Long = Byte.MAX_VALUE.toLong()): Long {
        val numbers = generateCompositeNumbers(from, to)

        val index = kRandomCommon.randomInt(0, numbers.size - 1)

        return numbers[index]
    }

    private fun validateStartEnd(from: Long, to: Long, fromMin: Long = 2, toMax: Long = Int.MAX_VALUE.toLong() - 1): Pair<Long, Long> {
        require(from < to) { "from[$from] < to[$to]!" }
        val start = if (from < fromMin) fromMin else from
        val end = if (to > toMax) toMax else to
        require(from < to) { "from[$from] < to[$to]!" }
        return Pair(start, end)
    }

    private fun fillCache(isPrimeMethod: Boolean, start: Long, end: Long) {
        //TODO use passed cached as param instead change global var for both methods
        if (isPrimeMethod) {
            fillCacheSieveOfEratosthenesMethod(start, end)
        } else {
            fillCompositeCache(start, end)
        }
    }

    private fun fillCompositeCache(start: Long, end: Long) {
        //TODO determine start and end depends on cache data and set only to range only between numbers which are not in cache
        //TODO for e.g. in the cache is 2,3,4 you input is 3..10 the start should be 4 and end is 10
        //TODO for e.g. in the cache is 3,4,5 you input is 2..10 the start should be 2 and end is 10
        //TODO for e.g. in the cache is 2,3,4 you input is 79..99 the start should be 79 and end is 99
        //TODO this will permit optimization for this loop, when you have a big range and cache contains half of numbers it will make it x2 faster roughly

        //TODO measure every line under foreach for how optimize is these values

        (start..end).forEach {
            if (isCompositeNumber(it) && !compositeCache.contains(it)) {
                //TODO change it more performance with different collection
                compositeCache.add(it)
                compositeCache.sort()
            }
        }
    }

    /**
     * sieveOfEratosthenes method
     */
    private fun fillCacheSieveOfEratosthenesMethod(start: Long, end: Long) {
        val prime = BooleanArray(end.toInt() + 1)
        Arrays.fill(prime, true)
        var p = start.toInt()
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
        //TODO for e.g. in the cache is 2,3,4 you input is 3..10 the start should be 4 and end is 10
        //TODO for e.g. in the cache is 3,4,5 you input is 2..10 the start should be 2 and end is 10
        //TODO for e.g. in the cache is 2,3,4 you input is 79..99 the start should be 79 and end is 99
        //TODO this will permit optimization for this loop, when you have a big range and cache contains half of numbers it will make it x2 faster roughly

        for (it in start..end) {
            if (prime[it.toInt()] && isPrimeNumber(it) && !primeCache.contains(it)) {
                //TODO change it more performance with different collection
                primeCache.add(it)
                primeCache.sort()
            }
        }
    }

    private fun checkClearCache() {
        if (USE_NATURAL_NUMBER_CACHE && primeCache.size >= MAX_CAPACITY) primeCache.clear()
        if (!USE_NATURAL_NUMBER_CACHE) primeCache.clear()
        if (USE_NATURAL_NUMBER_CACHE && compositeCache.size >= MAX_CAPACITY) compositeCache.clear()
        if (!USE_NATURAL_NUMBER_CACHE) compositeCache.clear()
    }

    private fun randomNaturalLong(from: Long, to: Long): Long {
        val result = kRandomCommon.randomLong(from, to)
        if (!isNaturalNumber(result)) {
            return randomNaturalLong(from, to)
        }

        return result
    }
}
