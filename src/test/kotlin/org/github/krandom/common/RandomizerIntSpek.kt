package org.github.krandom.common

import org.github.krandom.testhelper.Constants.generateValues
import org.github.krandom.testhelper.isBiggerOrEqual
import org.github.krandom.testhelper.isSmaller
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import kotlin.test.assertFailsWith

object RandomizerIntSpek : Spek({

    val randomType = "int"
    var intNumber: Int

    describe("a randomizer") {
        val kRandomCommon: KRandomCommon = Randomizer()

        describe("a random tests for $randomType") {

            describe("generate random $randomType") {
                val expectedOne = Int.MIN_VALUE
                val expectedTwo = Int.MAX_VALUE

                (1..generateValues).forEach { _ ->
                    intNumber = kRandomCommon.randomInt()
                    intNumber.also { number ->
                        it("$number should be in range >=$expectedOne and <$expectedTwo") {
                            isBiggerOrEqual(number, expectedOne)
                            isSmaller(number, expectedTwo)
                        }
                    }
                }
            }
            describe("generate random $randomType in range") {
                val expectedOne = 1
                val expectedTwo = 5

                (1..generateValues).forEach { _ ->
                    intNumber = kRandomCommon.randomInt(expectedOne..expectedTwo)
                    intNumber.also { number ->

                        it("$number should be in range >=$expectedOne and <$expectedTwo") {
                            isBiggerOrEqual(number, expectedOne)
                            isSmaller(number, expectedTwo)
                        }
                    }
                }
            }
            describe("generate random $randomType in range(start, end)") {
                val expectedOne = 1
                val expectedTwo = 500

                (1..generateValues).forEach { _ ->
                    intNumber = kRandomCommon.randomInt(expectedOne, expectedTwo)
                    intNumber.also { number ->

                        it("$number should be in range >=$expectedOne and <$expectedTwo") {
                            isBiggerOrEqual(number, expectedOne)
                            isSmaller(number, expectedTwo)
                        }
                    }
                }
            }
            describe("generate random $randomType in range(0, 0)") {
                assertFailsWith(IllegalArgumentException::class, "should throw illegal argument exception") {
                    kRandomCommon.randomInt(0, 0)
                }
            }
            describe("generate random $randomType in range(-start, end)") {
                val expectedOne = -1
                val expectedTwo = 5

                (1..generateValues).forEach { _ ->
                    intNumber = kRandomCommon.randomInt(expectedOne..expectedTwo)
                    intNumber.also { number ->

                        it("$number should be in range >=$expectedOne and <$expectedTwo") {
                            isBiggerOrEqual(number, expectedOne)
                            isSmaller(number, expectedTwo)
                        }
                    }
                }
            }
            describe("generate random $randomType in range(-start, -end)") {
                val expectedOne = -5
                val expectedTwo = -1

                (1..generateValues).forEach { _ ->
                    intNumber = kRandomCommon.randomInt(expectedOne..expectedTwo)
                    intNumber.also { number ->

                        it("$number should be in range >=$expectedOne and <$expectedTwo") {
                            isSmaller(number, expectedTwo)
                            isBiggerOrEqual(number, expectedOne)
                        }
                    }
                }
            }
            describe("generate random $randomType in range(start, -end)") {
                val expectedOne = -5
                val expectedTwo = 1

                (1..generateValues).forEach { _ ->
                    intNumber = kRandomCommon.randomInt(expectedOne..expectedTwo)
                    intNumber.also { number ->

                        it("$number should be in range >=$expectedOne and <$expectedTwo") {
                            isSmaller(number, expectedTwo)
                            isBiggerOrEqual(number, expectedOne)
                        }
                    }
                }
            }
            describe("generate random $randomType in range(start)") {
                val expectedOne = 1

                (1..generateValues).forEach { _ ->
                    intNumber = kRandomCommon.randomInt(start = expectedOne)
                    intNumber.also { number ->

                        it("$number should be in range >=$expectedOne") {
                            isBiggerOrEqual(number, expectedOne)
                        }
                    }
                }
            }
            describe("generate random $randomType in range(end)") {
                val expectedTwo = 5

                (1..generateValues).forEach { _ ->
                    intNumber = kRandomCommon.randomInt(end = expectedTwo)
                    intNumber.also { number ->

                        it("$number should be in range <$expectedTwo") {
                            isSmaller(number, expectedTwo)
                        }
                    }
                }
            }
            }
    }
})
