package org.github.krandom.common

import mu.KLogger
import mu.KLogging
import org.github.krandom.testhelper.isBiggerOrEqual
import org.github.krandom.testhelper.isSmaller
import org.github.krandom.testhelper.Constants.generateValues
import org.github.krandom.testhelper.TestLifecycle
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

object RandomizerLongSpek : Spek({

    val logger: KLogger = KLogging().logger(RandomizerLongSpek::class.java.simpleName)
    val randomType = "long"
    var longNumber: Long

    describe("a randomizer") {
        val kRandomCommon: KRandomCommon = Randomizer()

        describe("a random tests for $randomType") {

            TestLifecycle.onTestStart("generate random $randomType")
            describe("generate random $randomType") {
                val expectedOne = Long.MIN_VALUE
                val expectedTwo = Long.MAX_VALUE

                (1..generateValues).forEach { _ ->
                    longNumber = kRandomCommon.randomLong()
                    longNumber.also { number ->
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
                val expectedOne = 1L
                val expectedTwo = 5L

                (1..generateValues).forEach { _ ->
                    longNumber = kRandomCommon.randomLong(expectedOne..expectedTwo)
                    longNumber.also { number ->

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
                val expectedOne = 1L
                val expectedTwo = 5L

                (1..generateValues).forEach { _ ->
                    longNumber = kRandomCommon.randomLong(expectedOne, expectedTwo)
                    longNumber.also { number ->

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
                    kRandomCommon.randomLong(0L, 0L)
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
                val expectedOne = -1L
                val expectedTwo = 5L

                (1..generateValues).forEach { _ ->
                    longNumber = kRandomCommon.randomLong(expectedOne..expectedTwo)
                    longNumber.also { number ->

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
                val expectedOne = -1L
                val expectedTwo = -5L

                (1..generateValues).forEach { _ ->
                    longNumber = kRandomCommon.randomLong(expectedTwo..expectedOne)
                    longNumber.also { number ->

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
                val expectedOne = 1L
                val expectedTwo = -5L

                (1..generateValues).forEach { _ ->
                    longNumber = kRandomCommon.randomLong(expectedTwo..expectedOne)
                    longNumber.also { number ->

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
                val expectedOne = 1L

                (1..generateValues).forEach { _ ->
                    longNumber = kRandomCommon.randomLong(start = expectedOne)
                    longNumber.also { number ->

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
                val expectedTwo = 5L

                (1..generateValues).forEach { _ ->
                    longNumber = kRandomCommon.randomLong(end = expectedTwo)
                    longNumber.also { number ->

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
