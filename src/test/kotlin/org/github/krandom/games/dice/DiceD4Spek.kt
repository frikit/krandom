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

object DiceD4Spek : Spek({

    val logger: KLogger = KLogging().logger(DiceD4Spek::class.java.simpleName)
    val typeOfTest = "dice d4"
    var value: String

    val generatedValues = arrayListOf<String>()
    val generateExpectedValues = generateExpectedValues(1,4)

    describe("a dice d4") {
        val dice: Dice = Dice.init(DiceType.D4)

        describe("generate ${Constants.generateValues} values for tests") {
            (1..Constants.generateValues).forEach { _ ->
                generatedValues.add(dice.roll())
            }

            it("should not be empty") {
                assert(generatedValues.isNotEmpty()) {"Generated values are empty! [$generatedValues]"}
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
                    it("$value should be [1 or 2 or 3 or 4]") {
                        assert(value.matches(generateRegEx(1, 4)))
                    }
                }
            }
            TestLifecycle.onTestFinish("generate random $typeOfTest")
        }
    }
})
