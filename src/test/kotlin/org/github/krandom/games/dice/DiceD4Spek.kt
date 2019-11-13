package org.github.krandom.games.dice

import mu.KLogger
import mu.KLogging
import org.github.krandom.games.dice.enum.DiceType
import org.github.krandom.utils.Constants
import org.github.krandom.utils.DiceUtil.generateExpectedValues
import org.github.krandom.utils.DiceUtil.generateRegEx
import org.github.krandom.utils.TestLifecycle
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import kotlin.test.assertFailsWith

object DiceD4Spek : Spek({

    val logger: KLogger = KLogging().logger(DiceD4Spek::class.java.simpleName)
    val typeOfTest = "dice d4"
    var value: String

    describe("a dice with invalid elems") {
        val invalidScenarios = listOf(
                listOf("-1"),
                listOf("-1", "0"),
                listOf("-1", "0", "1"),
                listOf("-1", "0", "1", "0", "1")
        )
        invalidScenarios.forEach {
            it("should fail with IllegalArgument") {
                assertFailsWith(IllegalArgumentException::class, "should throw illegal argument exception") {
                    Dice.init(DiceType.D4, it)
                }
            }
        }
    }

    describe("a empty dice d4") {
        val emptyDice: Dice = Dice.init(DiceType.D4)
        val generatedValues = arrayListOf<String>()
        val generateExpectedValues = generateExpectedValues(1, 4)
        val expectedRegEx = generateRegEx(1, 4)

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

    describe("a manually filled dice d4") {
        val dice: Dice = Dice.init(DiceType.D4, listOf("-1", "0", "1", "2"))
        val generatedValues = arrayListOf<String>()
        val generateExpectedValues = generateExpectedValues(-1, 2)
        val expectedRegEx = generateRegEx(-1, 2)

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
})
