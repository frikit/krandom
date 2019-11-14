package org.github.krandom.common

import mu.KLogger
import mu.KLogging
import org.github.krandom.utils.AssertionHelper.isBigger
import org.github.krandom.utils.AssertionHelper.isBiggerOrEqual
import org.github.krandom.utils.AssertionHelper.isLesserOrEqual
import org.github.krandom.utils.AssertionHelper.isSmaller
import org.github.krandom.utils.Constants.generateValues
import org.github.krandom.utils.TestLifecycle
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

object RandomizerFloatSpek : Spek({

    val logger: KLogger = KLogging().logger(RandomizerFloatSpek::class.java.simpleName)
    val randomType = "float"
    var floatNumber: Float

    describe("a randomizer") {
        val kRandomCommon: KRandomCommon = Randomizer()

        describe("a random tests for $randomType") {

            TestLifecycle.onTestStart("generate random $randomType")
            describe("generate random $randomType") {
                val expectedOne = 0.0f
                val expectedTwo = 1.0f

                (1..generateValues).forEach {
                    floatNumber = kRandomCommon.randomFloat()
                    floatNumber.also { number ->
                        TestLifecycle.onTestStep(logger, "generated : [$number]")
                        it("$number should be in range >=$expectedOne and <$expectedTwo") {
                            isBiggerOrEqual(number, expectedOne)
                            isSmaller(number, expectedTwo)
                        }
                    }
                }
            }
            TestLifecycle.onTestFinish("generate random $randomType")

            TestLifecycle.onTestStart("generate random $randomType in range")
            describe("generate random $randomType in range") {
                val expectedOne = 1.0f
                val expectedTwo = 5.0f

                (1..generateValues).forEach {
                    floatNumber = kRandomCommon.randomFloat(expectedOne..expectedTwo)
                    floatNumber.also { number ->

                        TestLifecycle.onTestStep(logger, "generated : [$number]")
                        it("$number should be in range >=$expectedOne and <$expectedTwo") {
                            isBiggerOrEqual(number, expectedOne)
                            isSmaller(number, expectedTwo)
                        }
                    }
                }
            }
            TestLifecycle.onTestFinish("generate random $randomType in range")

            TestLifecycle.onTestStart("generate random $randomType in range(start, end)")
            describe("generate random $randomType in range(start, end)") {
                val expectedOne = 1.0f
                val expectedTwo = 5.0f

                (1..generateValues).forEach {
                    floatNumber = kRandomCommon.randomFloat(expectedOne, expectedTwo)
                    floatNumber.also { number ->

                        TestLifecycle.onTestStep(logger, "generated : [$number]")
                        it("$number should be in range >=$expectedOne and <$expectedTwo") {
                            isBiggerOrEqual(number, expectedOne)
                            isSmaller(number, expectedTwo)
                        }
                    }
                }
            }
            TestLifecycle.onTestFinish("generate random $randomType in range(start, end)")

            TestLifecycle.onTestStart("generate random $randomType in range(0, 0)")
            describe("generate random $randomType in range(0, 0)") {
                //TODO update to kotlin-test
                try {
                    kRandomCommon.randomFloat(0.0f, 0.0f)
                } catch (exception: IllegalArgumentException) {
                    assert(exception.message!!.startsWith(prefix = "Illegal argument passed start = 0.0 and end = 0.0, they should be different!", ignoreCase = false))
                }
            }
            TestLifecycle.onTestFinish("generate random $randomType in range(0, 0)")

            TestLifecycle.onTestStart("generate random $randomType in range(-start, end)")
            describe("generate random $randomType in range(-start, end)") {
                val expectedOne = -1.0f
                val expectedTwo = 5.0f

                (1..generateValues).forEach {
                    floatNumber = kRandomCommon.randomFloat(expectedOne..expectedTwo)
                    floatNumber.also { number ->

                        TestLifecycle.onTestStep(logger, "generated : [$number]")
                        it("$number should be in range >=$expectedOne and <$expectedTwo") {
                            isBiggerOrEqual(number, expectedOne)
                            isSmaller(number, expectedTwo)
                        }
                    }
                }
            }
            TestLifecycle.onTestFinish("generate random $randomType in range(-start, end)")

            TestLifecycle.onTestStart("generate random $randomType in range(-start, -end)")
            describe("generate random $randomType in range(-start, -end)") {
                val expectedOne = -1.0f
                val expectedTwo = -5.0f

                (1..generateValues).forEach {
                    floatNumber = kRandomCommon.randomFloat(expectedOne..expectedTwo)
                    floatNumber.also { number ->

                        TestLifecycle.onTestStep(logger, "generated : [$number]")
                        it("$number should be in range >=$expectedOne and <$expectedTwo") {
                            isLesserOrEqual(number, expectedOne)
                            isBigger(number, expectedTwo)
                        }
                    }
                }
            }
            TestLifecycle.onTestFinish("generate random $randomType in range(-start, -end)")

            TestLifecycle.onTestStart("generate random $randomType in range(start, -end)")
            describe("generate random $randomType in range(start, -end)") {
                val expectedOne = 1.0f
                val expectedTwo = -5.0f

                (1..generateValues).forEach {
                    floatNumber = kRandomCommon.randomFloat(expectedOne..expectedTwo)
                    floatNumber.also { number ->

                        TestLifecycle.onTestStep(logger, "generated : [$number]")
                        it("$number should be in range >=$expectedOne and <$expectedTwo") {
                            isLesserOrEqual(number, expectedOne)
                            isBigger(number, expectedTwo)
                        }
                    }
                }
            }
            TestLifecycle.onTestFinish("generate random $randomType in range(start, -end)")

            TestLifecycle.onTestStart("generate random $randomType in range(start)")
            describe("generate random $randomType in range(start)") {
                val expectedOne = 1.0f

                (1..generateValues).forEach {
                    floatNumber = kRandomCommon.randomFloat(start = expectedOne)
                    floatNumber.also { number ->

                        TestLifecycle.onTestStep(logger, "generated : [$number]")
                        it("$number should be in range >=$expectedOne") {
                            isBiggerOrEqual(number, expectedOne)
                        }
                    }
                }
            }
            TestLifecycle.onTestFinish("generate random $randomType in range(start)")

            TestLifecycle.onTestStart("generate random $randomType in range(end)")
            describe("generate random $randomType in range(end)") {
                val expectedTwo = 5.0f

                (1..generateValues).forEach {
                    floatNumber = kRandomCommon.randomFloat(end = expectedTwo)
                    floatNumber.also { number ->

                        TestLifecycle.onTestStep(logger, "generated : [$number]")
                        it("$number should be in range <$expectedTwo") {
                            isSmaller(number, expectedTwo)
                        }
                    }
                }
            }
            TestLifecycle.onTestFinish("generate random $randomType in range(end)")
        }
    }
})
