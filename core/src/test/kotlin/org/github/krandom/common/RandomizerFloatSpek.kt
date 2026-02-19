/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.common

import org.github.krandom.testhelper.Constants.generateValues
import org.github.krandom.testhelper.isBigger
import org.github.krandom.testhelper.isBiggerOrEqual
import org.github.krandom.testhelper.isLesserOrEqual
import org.github.krandom.testhelper.isSmaller
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.assertions.throwables.shouldThrow

class RandomizerFloatSpek : DescribeSpec({

    val randomType = "float"
    var floatNumber: Float

    describe("a randomizer") {
        val kRandomCommon: KRandomCommon = Randomizer()

        describe("a random tests for $randomType") {

            describe("generate random $randomType") {
                val expectedOne = 0.0f
                val expectedTwo = 1.0f

                (1..generateValues).forEach { _ ->
                    floatNumber = kRandomCommon.randomFloat()
                    floatNumber.also { number ->
                        it("$number should be in range >=$expectedOne and <$expectedTwo") {
                            isBiggerOrEqual(number, expectedOne)
                            isSmaller(number, expectedTwo)
                        }
                    }
                }
            }
            describe("generate random $randomType in range") {
                val expectedOne = 1.0f
                val expectedTwo = 5.0f

                (1..generateValues).forEach { _ ->
                    floatNumber = kRandomCommon.randomFloat(expectedOne..expectedTwo)
                    floatNumber.also { number ->

                        it("$number should be in range >=$expectedOne and <$expectedTwo") {
                            isBiggerOrEqual(number, expectedOne)
                            isSmaller(number, expectedTwo)
                        }
                    }
                }
            }
            describe("generate random $randomType in range(start, end)") {
                val expectedOne = 1.0f
                val expectedTwo = 5.0f

                (1..generateValues).forEach { _ ->
                    floatNumber = kRandomCommon.randomFloat(expectedOne, expectedTwo)
                    floatNumber.also { number ->

                        it("$number should be in range >=$expectedOne and <$expectedTwo") {
                            isBiggerOrEqual(number, expectedOne)
                            isSmaller(number, expectedTwo)
                        }
                    }
                }
            }
            describe("generate random $randomType in range(0, 0)") {
                shouldThrow<IllegalArgumentException> {
                    kRandomCommon.randomFloat(0.0f, 0.0f)
                }
            }
            describe("generate random $randomType in range(-start, end)") {
                val expectedOne = -1.0f
                val expectedTwo = 5.0f

                (1..generateValues).forEach { _ ->
                    floatNumber = kRandomCommon.randomFloat(expectedOne..expectedTwo)
                    floatNumber.also { number ->

                        it("$number should be in range >=$expectedOne and <$expectedTwo") {
                            isBiggerOrEqual(number, expectedOne)
                            isSmaller(number, expectedTwo)
                        }
                    }
                }
            }
            describe("generate random $randomType in range(-start, -end)") {
                val expectedOne = -1.0f
                val expectedTwo = -5.0f

                (1..generateValues).forEach { _ ->
                    floatNumber = kRandomCommon.randomFloat(expectedOne..expectedTwo)
                    floatNumber.also { number ->

                        it("$number should be in range >=$expectedOne and <$expectedTwo") {
                            isLesserOrEqual(number, expectedOne)
                            isBigger(number, expectedTwo)
                        }
                    }
                }
            }
            describe("generate random $randomType in range(start, -end)") {
                val expectedOne = 1.0f
                val expectedTwo = -5.0f

                (1..generateValues).forEach { _ ->
                    floatNumber = kRandomCommon.randomFloat(expectedOne..expectedTwo)
                    floatNumber.also { number ->

                        it("$number should be in range >=$expectedOne and <$expectedTwo") {
                            isLesserOrEqual(number, expectedOne)
                            isBigger(number, expectedTwo)
                        }
                    }
                }
            }
            describe("generate random $randomType in range(start)") {
                val expectedOne = 1.0f

                (1..generateValues).forEach { _ ->
                    floatNumber = kRandomCommon.randomFloat(start = expectedOne)
                    floatNumber.also { number ->

                        it("$number should be in range >=$expectedOne") {
                            isBiggerOrEqual(number, expectedOne)
                        }
                    }
                }
            }
            describe("generate random $randomType in range(end)") {
                val expectedTwo = 5.0f

                (1..generateValues).forEach { _ ->
                    floatNumber = kRandomCommon.randomFloat(end = expectedTwo)
                    floatNumber.also { number ->

                        it("$number should be in range <$expectedTwo") {
                            isSmaller(number, expectedTwo)
                        }
                    }
                }
            }
            }
    }
})
