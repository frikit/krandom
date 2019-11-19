package org.github.krandom.common

import org.github.krandom.testhelper.Constants.generateValues
import org.github.krandom.testhelper.isBigger
import org.github.krandom.testhelper.isBiggerOrEqual
import org.github.krandom.testhelper.isLesserOrEqual
import org.github.krandom.testhelper.isSmaller
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import kotlin.test.assertFailsWith

object RandomizerDoubleSpek : Spek({

    val randomType = "double"
    var doubleNumber: Double

    describe("a randomizer") {
        val kRandomCommon: KRandomCommon = Randomizer()

        describe("a random tests for $randomType") {

            describe("generate random $randomType") {
                val expectedOne = 0.0
                val expectedTwo = 1.0

                (1..generateValues).forEach { _ ->
                    doubleNumber = kRandomCommon.randomDouble()
                    doubleNumber.also { number ->
                        it("$number should be in range >=$expectedOne and <$expectedTwo") {
                            isBiggerOrEqual(number, expectedOne)
                            isSmaller(number, expectedTwo)
                        }
                    }
                }
            }
            describe("generate random $randomType in range") {
                val expectedOne = 1.0
                val expectedTwo = 5.0

                (1..generateValues).forEach { _ ->
                    doubleNumber = kRandomCommon.randomDouble(expectedOne..expectedTwo)
                    doubleNumber.also { number ->

                        it("$number should be in range >=$expectedOne and <$expectedTwo") {
                            isBiggerOrEqual(number, expectedOne)
                            isSmaller(number, expectedTwo)
                        }
                    }
                }
            }
            describe("generate random $randomType in range(start, end)") {
                val expectedOne = 1.0
                val expectedTwo = 5.0

                (1..generateValues).forEach { _ ->
                    doubleNumber = kRandomCommon.randomDouble(expectedOne, expectedTwo)
                    doubleNumber.also { number ->

                        it("$number should be in range >=$expectedOne and <$expectedTwo") {
                            isBiggerOrEqual(number, expectedOne)
                            isSmaller(number, expectedTwo)
                        }
                    }
                }
            }
            describe("generate random $randomType in range(0, 0)") {
                assertFailsWith(IllegalArgumentException::class, "should throw illegal argument exception") {
                    kRandomCommon.randomDouble(0.0, 0.0)
                }
            }
            describe("generate random $randomType in range(-start, end)") {
                val expectedOne = -1.0
                val expectedTwo = 5.0

                (1..generateValues).forEach { _ ->
                    doubleNumber = kRandomCommon.randomDouble(expectedOne..expectedTwo)
                    doubleNumber.also { number ->

                        it("$number should be in range >=$expectedOne and <$expectedTwo") {
                            isBiggerOrEqual(number, expectedOne)
                            isSmaller(number, expectedTwo)
                        }
                    }
                }
            }
            describe("generate random $randomType in range(-start, -end)") {
                val expectedOne = -1.0
                val expectedTwo = -5.0

                (1..generateValues).forEach { _ ->
                    doubleNumber = kRandomCommon.randomDouble(expectedOne..expectedTwo)
                    doubleNumber.also { number ->

                        it("$number should be in range >=$expectedOne and <$expectedTwo") {
                            isLesserOrEqual(number, expectedOne)
                            isBigger(number, expectedTwo)
                        }
                    }
                }
            }
            describe("generate random $randomType in range(start, -end)") {
                val expectedOne = 1.0
                val expectedTwo = -5.0

                (1..generateValues).forEach { _ ->
                    doubleNumber = kRandomCommon.randomDouble(expectedOne..expectedTwo)
                    doubleNumber.also { number ->

                        it("$number should be in range >=$expectedOne and <$expectedTwo") {
                            isLesserOrEqual(number, expectedOne)
                            isBigger(number, expectedTwo)
                        }
                    }
                }
            }
            describe("generate random $randomType in range(start)") {
                val expectedOne = 1.0

                (1..generateValues).forEach { _ ->
                    doubleNumber = kRandomCommon.randomDouble(start = expectedOne)
                    doubleNumber.also { number ->

                        it("$number should be in range >=$expectedOne") {
                            isBiggerOrEqual(number, expectedOne)
                        }
                    }
                }
            }
            describe("generate random $randomType in range(end)") {
                val expectedTwo = 5.0

                (1..generateValues).forEach { _ ->
                    doubleNumber = kRandomCommon.randomDouble(end = expectedTwo)
                    doubleNumber.also { number ->

                        it("$number should be in range <$expectedTwo") {
                            isSmaller(number, expectedTwo)
                        }
                    }
                }
            }
            }
    }
})
