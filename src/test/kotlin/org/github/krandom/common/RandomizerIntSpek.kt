package org.github.krandom.common

import mu.KLogger
import mu.KLogging
import org.github.krandom.utils.AssertionHelper.isBiggerOrEqual
import org.github.krandom.utils.AssertionHelper.isSmaller
import org.github.krandom.utils.Constants.generateValues
import org.github.krandom.utils.TestLifecycle
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

object RandomizerIntSpek : Spek({

    val logger: KLogger = KLogging().logger(RandomizerIntSpek::class.java.simpleName)
    val randomType = "int"
    var intNumber: Int

    describe("a randomizer") {
        val kRandomCommon: KRandomCommon = Randomizer()

        describe("a random tests for $randomType") {

            TestLifecycle.onTestStart("generate random $randomType")
            describe("generate random $randomType") {
                val expectedOne = Int.MIN_VALUE
                val expectedTwo = Int.MAX_VALUE

                (1..generateValues).forEach { _ ->
                    intNumber = kRandomCommon.randomInt()
                    intNumber.also { number ->
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
                val expectedOne = 1
                val expectedTwo = 5

                (1..generateValues).forEach { _ ->
                    intNumber = kRandomCommon.randomInt(expectedOne..expectedTwo)
                    intNumber.also { number ->

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
                val expectedOne = 1
                val expectedTwo = 500

                (1..generateValues).forEach { _ ->
                    intNumber = kRandomCommon.randomInt(expectedOne, expectedTwo)
                    intNumber.also { number ->

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
                //TODO use kotlin test
                try {
                    kRandomCommon.randomInt(0, 0)
                    assert(false) { "Exception should be thrown!" }
                } catch (exception: IllegalArgumentException) {
                    assert(exception
                            .message!!
                            .startsWith(
                                    prefix = "Illegal argument passed start = 0 and end = 0, they should be different!",
                                    ignoreCase = false)
                    )
                }
            }
            TestLifecycle.onTestFinish("generate random $randomType in range(0, 0)")

            TestLifecycle.onTestStart("generate random $randomType in range(-start, end)")
            describe("generate random $randomType in range(-start, end)") {
                val expectedOne = -1
                val expectedTwo = 5

                (1..generateValues).forEach { _ ->
                    intNumber = kRandomCommon.randomInt(expectedOne..expectedTwo)
                    intNumber.also { number ->

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
                val expectedOne = -1
                val expectedTwo = -5

                (1..generateValues).forEach { _ ->
                    intNumber = kRandomCommon.randomInt(expectedTwo..expectedOne)
                    intNumber.also { number ->

                        TestLifecycle.onTestStep(logger, "generated : [$number]")
                        it("$number should be in range >=$expectedOne and <$expectedTwo") {
                            isSmaller(number, expectedOne)
                            isBiggerOrEqual(number, expectedTwo)
                        }
                    }
                }
            }
            TestLifecycle.onTestFinish("generate random $randomType in range(-start, -end)")

            TestLifecycle.onTestStart("generate random $randomType in range(start, -end)")
            describe("generate random $randomType in range(start, -end)") {
                val expectedOne = 1
                val expectedTwo = -5

                (1..generateValues).forEach { _ ->
                    intNumber = kRandomCommon.randomInt(expectedTwo..expectedOne)
                    intNumber.also { number ->

                        TestLifecycle.onTestStep(logger, "generated : [$number]")
                        it("$number should be in range >=$expectedOne and <$expectedTwo") {
                            isSmaller(number, expectedOne)
                            isBiggerOrEqual(number, expectedTwo)
                        }
                    }
                }
            }
            TestLifecycle.onTestFinish("generate random $randomType in range(start, -end)")

            TestLifecycle.onTestStart("generate random $randomType in range(start)")
            describe("generate random $randomType in range(start)") {
                val expectedOne = 1

                (1..generateValues).forEach { _ ->
                    intNumber = kRandomCommon.randomInt(start = expectedOne)
                    intNumber.also { number ->

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
                val expectedTwo = 5

                (1..generateValues).forEach { _ ->
                    intNumber = kRandomCommon.randomInt(end = expectedTwo)
                    intNumber.also { number ->

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
