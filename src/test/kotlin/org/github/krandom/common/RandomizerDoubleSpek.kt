package org.github.krandom.common

import mu.KLogger
import mu.KLogging
import org.github.krandom.testhelper.*
import org.github.krandom.testhelper.Constants.generateValues
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import kotlin.test.assertFailsWith

object RandomizerDoubleSpek : Spek({

    val logger: KLogger = KLogging().logger(RandomizerDoubleSpek::class.java.simpleName)
    val randomType = "double"
    var doubleNumber: Double

    describe("a randomizer") {
        val kRandomCommon: KRandomCommon = Randomizer()

        describe("a random tests for $randomType") {

            TestLifecycle.onTestStart("generate random $randomType")
            describe("generate random $randomType") {
                val expectedOne = 0.0
                val expectedTwo = 1.0

                (1..generateValues).forEach { _ ->
                    doubleNumber = kRandomCommon.randomDouble()
                    doubleNumber.also { number ->
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
                val expectedOne = 1.0
                val expectedTwo = 5.0

                (1..generateValues).forEach { _ ->
                    doubleNumber = kRandomCommon.randomDouble(expectedOne..expectedTwo)
                    doubleNumber.also { number ->

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
                val expectedOne = 1.0
                val expectedTwo = 5.0

                (1..generateValues).forEach { _ ->
                    doubleNumber = kRandomCommon.randomDouble(expectedOne, expectedTwo)
                    doubleNumber.also { number ->

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
                assertFailsWith(IllegalArgumentException::class, "should throw illegal argument exception") {
                    kRandomCommon.randomDouble(0.0, 0.0)
                }
            }
            TestLifecycle.onTestFinish("generate random $randomType in range(0, 0)")

            TestLifecycle.onTestStart("generate random $randomType in range(-start, end)")
            describe("generate random $randomType in range(-start, end)") {
                val expectedOne = -1.0
                val expectedTwo = 5.0

                (1..generateValues).forEach { _ ->
                    doubleNumber = kRandomCommon.randomDouble(expectedOne..expectedTwo)
                    doubleNumber.also { number ->

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
                val expectedOne = -1.0
                val expectedTwo = -5.0

                (1..generateValues).forEach { _ ->
                    doubleNumber = kRandomCommon.randomDouble(expectedOne..expectedTwo)
                    doubleNumber.also { number ->

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
                val expectedOne = 1.0
                val expectedTwo = -5.0

                (1..generateValues).forEach { _ ->
                    doubleNumber = kRandomCommon.randomDouble(expectedOne..expectedTwo)
                    doubleNumber.also { number ->

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
                val expectedOne = 1.0

                (1..generateValues).forEach { _ ->
                    doubleNumber = kRandomCommon.randomDouble(start = expectedOne)
                    doubleNumber.also { number ->

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
                val expectedTwo = 5.0

                (1..generateValues).forEach { _ ->
                    doubleNumber = kRandomCommon.randomDouble(end = expectedTwo)
                    doubleNumber.also { number ->

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
