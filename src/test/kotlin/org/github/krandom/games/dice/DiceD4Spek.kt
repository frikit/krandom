package org.github.krandom.games.dice

import mu.KLogger
import mu.KLogging
import org.github.krandom.games.dice.enum.DiceType
import org.github.krandom.testhelper.Constants
import org.github.krandom.testhelper.DiceUtil.generateExpectedValues
import org.github.krandom.testhelper.DiceUtil.generateInvalidValues
import org.github.krandom.testhelper.DiceUtil.generateRegEx
import org.github.krandom.testhelper.TestLifecycle
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import kotlin.test.assertFailsWith

object DiceD4Spek : Spek({

    val logger: KLogger = KLogging().logger(DiceD4Spek::class.java.simpleName)
    val diceSize = 4
    val typeOfTest = "dice d$diceSize"

    describe("a dice with invalid elems") {
        val invalidScenarios = generateInvalidValues(diceSize)
        invalidScenarios.forEach {
            it("should fail with IllegalArgument") {
                assertFailsWith(IllegalArgumentException::class, "should throw illegal argument exception") {
                    IDice.init(DiceType.D4, it.toList())
                }
            }
        }
    }

    describe("a empty $typeOfTest") {
        val emptyDice: IDice = IDice.init(DiceType.D4)
        val generatedValues = arrayListOf<String>()
        val generateExpectedValues = generateExpectedValues(1, diceSize)
        val expectedRegEx = generateRegEx(1, diceSize)
        var value: String

        describe("generate ${Constants.generateValues} values for tests") {
            (1..Constants.generateValues).forEach { _ ->
                generatedValues.add(emptyDice.roll())
            }

            it("should not be empty") {
                assert(generatedValues.isNotEmpty()) { "Generated values are empty! [$generatedValues]" }
            }
        }

        generateExpectedValues.forEach {
            it("should be at least 1 of $it") {
                assert(generatedValues.contains(it))
            }
        }

        describe("a random tests for $typeOfTest") {
            TestLifecycle.onTestStart("generate random $typeOfTest")
            describe("generate random $typeOfTest") {
                generatedValues.forEach {
                    value = it
                    TestLifecycle.onTestStep(logger, "generated : [$value]")
                    it("$value should be $expectedRegEx") {
                        assert(value.matches(expectedRegEx)) {"Value [$value] don't match regex [$expectedRegEx]"}
                    }
                }
            }
            TestLifecycle.onTestFinish("generate random $typeOfTest")
        }
    }

    describe("a manually filled $typeOfTest throw 1 times") {
        val dice: IDice = IDice.init(DiceType.D4, listOf("-3", "-2", "-1", "0"))
        val generatedValues = arrayListOf<String>()
        val generateExpectedValues = generateExpectedValues(-3, 0)
        val expectedRegEx = generateRegEx(-3, 0)
        var value: String

        describe("generate ${Constants.generateValues} values for tests") {
            (1..Constants.generateValues).forEach { _ ->
                generatedValues.add(dice.roll())
            }

            it("should not be empty") {
                assert(generatedValues.isNotEmpty()) { "Generated values are empty! [$generatedValues]" }
            }
        }

        generateExpectedValues.forEach {
            it("should be at least 1 of $it") {
                assert(generatedValues.contains(it))
            }
        }

        describe("a random tests for $typeOfTest") {
            TestLifecycle.onTestStart("generate random $typeOfTest")
            describe("generate random $typeOfTest") {
                generatedValues.forEach {
                    value = it
                    TestLifecycle.onTestStep(logger, "generated : [$value]")
                    it("$value should be $expectedRegEx") {
                        assert(value.matches(expectedRegEx)) {"Value [$value] don't match regex [$expectedRegEx]"}
                    }
                }
            }
            TestLifecycle.onTestFinish("generate random $typeOfTest")
        }
    }

    describe("a manually filled $typeOfTest throw 5 times") {
        val dice: IDice = IDice.init(DiceType.D4, listOf("-3", "-2", "-1", "0"))
        val generatedValues = arrayListOf<String>()
        val generateExpectedValues = generateExpectedValues(-3, 0)
        val expectedRegEx = generateRegEx(-3, 0)
        var value: String

        describe("generate ${Constants.generateValues} values for tests") {
            (1..Constants.generateValues).forEach { _ ->
                generatedValues.add(dice.roll(5))
            }

            it("should not be empty") {
                assert(generatedValues.isNotEmpty()) { "Generated values are empty! [$generatedValues]" }
            }
        }

        generateExpectedValues.forEach {
            it("should be at least 1 of $it") {
                assert(generatedValues.contains(it))
            }
        }

        describe("a random tests for $typeOfTest") {
            TestLifecycle.onTestStart("generate random $typeOfTest")
            describe("generate random $typeOfTest") {
                generatedValues.forEach {
                    value = it
                    TestLifecycle.onTestStep(logger, "generated : [$value]")
                    it("$value should be $expectedRegEx") {
                        assert(value.matches(expectedRegEx)) {"Value [$value] don't match regex [$expectedRegEx]"}
                    }
                }
            }
            TestLifecycle.onTestFinish("generate random $typeOfTest")
        }
    }
})
