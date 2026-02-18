package org.github.krandom.common

import org.github.krandom.testhelper.Constants.generateValues
import org.github.krandom.testhelper.isBiggerOrEqual
import org.github.krandom.testhelper.isSmaller
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.assertions.throwables.shouldThrow

class RandomizerLongSpek : DescribeSpec({

    val randomType = "long"
    var longNumber: Long

    describe("a randomizer") {
        val kRandomCommon: KRandomCommon = Randomizer()

        describe("a random tests for $randomType") {

            describe("generate random $randomType") {
                val expectedOne = Long.MIN_VALUE
                val expectedTwo = Long.MAX_VALUE

                (1..generateValues).forEach { _ ->
                    longNumber = kRandomCommon.randomLong()
                    longNumber.also { number ->
                        it("$number should be in range >=$expectedOne and <$expectedTwo") {
                            isBiggerOrEqual(number, expectedOne)
                            isSmaller(number, expectedTwo)
                        }
                    }
                }
            }
            describe("generate random $randomType in range") {
                val expectedOne = 1L
                val expectedTwo = 5L

                (1..generateValues).forEach { _ ->
                    longNumber = kRandomCommon.randomLong(expectedOne..expectedTwo)
                    longNumber.also { number ->

                        it("$number should be in range >=$expectedOne and <$expectedTwo") {
                            isBiggerOrEqual(number, expectedOne)
                            isSmaller(number, expectedTwo)
                        }
                    }
                }
            }
            describe("generate random $randomType in range(start, end)") {
                val expectedOne = 1L
                val expectedTwo = 5L

                (1..generateValues).forEach { _ ->
                    longNumber = kRandomCommon.randomLong(expectedOne, expectedTwo)
                    longNumber.also { number ->

                        it("$number should be in range >=$expectedOne and <$expectedTwo") {
                            isBiggerOrEqual(number, expectedOne)
                            isSmaller(number, expectedTwo)
                        }
                    }
                }
            }
            describe("generate random $randomType in range(0, 0)") {
                shouldThrow<IllegalArgumentException> {
                    kRandomCommon.randomLong(0L, 0L)
                }
            }
            describe("generate random $randomType in range(-start, end)") {
                val expectedOne = -1L
                val expectedTwo = 5L

                (1..generateValues).forEach { _ ->
                    longNumber = kRandomCommon.randomLong(expectedOne..expectedTwo)
                    longNumber.also { number ->

                        it("$number should be in range >=$expectedOne and <$expectedTwo") {
                            isBiggerOrEqual(number, expectedOne)
                            isSmaller(number, expectedTwo)
                        }
                    }
                }
            }
            describe("generate random $randomType in range(-start, -end)") {
                val expectedOne = -5L
                val expectedTwo = -1L

                (1..generateValues).forEach { _ ->
                    longNumber = kRandomCommon.randomLong(expectedOne..expectedTwo)
                    longNumber.also { number ->

                        it("$number should be in range >=$expectedOne and <$expectedTwo") {
                            isSmaller(number, expectedTwo)
                            isBiggerOrEqual(number, expectedOne)
                        }
                    }
                }
            }
            describe("generate random $randomType in range(start, -end)") {
                val expectedOne = -5L
                val expectedTwo = 1L

                (1..generateValues).forEach { _ ->
                    longNumber = kRandomCommon.randomLong(expectedOne..expectedTwo)
                    longNumber.also { number ->

                        it("$number should be in range >=$expectedOne and <$expectedTwo") {
                            isSmaller(number, expectedTwo)
                            isBiggerOrEqual(number, expectedOne)
                        }
                    }
                }
            }
            describe("generate random $randomType in range(start)") {
                val expectedOne = 1L

                (1..generateValues).forEach { _ ->
                    longNumber = kRandomCommon.randomLong(start = expectedOne)
                    longNumber.also { number ->

                        it("$number should be in range >=$expectedOne") {
                            isBiggerOrEqual(number, expectedOne)
                        }
                    }
                }
            }
            describe("generate random $randomType in range(end)") {
                val expectedTwo = 5L

                (1..generateValues).forEach { _ ->
                    longNumber = kRandomCommon.randomLong(end = expectedTwo)
                    longNumber.also { number ->

                        it("$number should be in range <$expectedTwo") {
                            isSmaller(number, expectedTwo)
                        }
                    }
                }
            }
            }
    }
})
