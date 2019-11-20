package org.github.krandom.games.dice

import org.github.krandom.games.dice.enum.DiceType
import org.github.krandom.testhelper.Constants
import org.github.krandom.testhelper.DiceUtil.generateExpectedValues
import org.github.krandom.testhelper.DiceUtil.generateInvalidValues
import org.github.krandom.testhelper.DiceUtil.generateRegEx
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import kotlin.test.assertFailsWith

object DiceD4Spek : Spek({

    val diceSize = 4
    val typeOfTest = "dice d$diceSize"
    val expectedValues = generateExpectedValues(-3, 0)
    val expectedRegEx = generateRegEx(-3, 0)
    val diceType = DiceType.D4
    val rollTimes = diceSize * 5

    describe("a $typeOfTest with invalid tests") {
        val invalidScenarios = generateInvalidValues(diceSize)
        invalidScenarios.forEach {
            it("have invalid values size [${it.toList()}]") {
                assertFailsWith(IllegalArgumentException::class, "should throw illegal argument exception") {
                    IDice.init(diceType, it.toList())
                }
            }
        }

        it("have empty values") {
            assertFailsWith(IllegalArgumentException::class, "should throw illegal argument exception") {
                IDice.init(diceType, emptyList<String>())
            }
        }

        it("roll invalid nr of times") {
            assertFailsWith(IllegalArgumentException::class, "should throw illegal argument exception") {
                IDice.init(diceType, expectedValues).rolls(-1)
            }
        }
    }

    describe("a $typeOfTest roll() 1 times") {
        val dice = IDice.init(diceType, expectedValues)
        val generatedValues = arrayListOf<Int>()
        var value: Int

        describe("generate ${Constants.generateValues} values for tests") {
            (1..Constants.generateValues).forEach { _ ->
                generatedValues.add(dice.roll())
            }

            it("should not be empty") {
                assert(generatedValues.isNotEmpty()) { "Generated values are empty! [$generatedValues]" }
            }
        }

        expectedValues.forEach {
            it("should be at least 1 of $it") {
                assert(generatedValues.contains(it))
            }
        }

        describe("a random tests for $typeOfTest") {
            describe("generate random $typeOfTest") {
                generatedValues.forEach {
                    value = it
                    it("$value should be $expectedRegEx") {
                        assert(value.toString().matches(expectedRegEx)) { "Value [$value] don't match regex [$expectedRegEx]" }
                    }
                }
            }
        }
    }

    describe("a $typeOfTest roll() 5 times") {
        val dice = IDice.init(diceType, expectedValues)
        val generatedValues = arrayListOf<Int>()
        var value: Int

        describe("generate ${Constants.generateValues} values for tests") {
            (1..Constants.generateValues).forEach { _ ->
                generatedValues.add(dice.roll(rollTimes))
            }

            it("should not be empty") {
                assert(generatedValues.isNotEmpty()) { "Generated values are empty! [$generatedValues]" }
            }
        }

        expectedValues.forEach {
            it("should be at least 1 of $it") {
                assert(generatedValues.contains(it))
            }
        }

        describe("a random tests for $typeOfTest") {
            describe("generate random $typeOfTest") {
                generatedValues.forEach {
                    value = it
                    it("$value should be $expectedRegEx") {
                        assert(value.toString().matches(expectedRegEx)) { "Value [$value] don't match regex [$expectedRegEx]" }
                    }
                }
            }
        }
    }

    describe("a $typeOfTest rolls() 1 times") {
        val dice = IDice.init(diceType, expectedValues)
        val generatedValues = arrayListOf<List<Int>>()
        var value: Int

        describe("generate ${Constants.generateValues} values for tests") {
            (1..Constants.generateValues).forEach { _ ->
                generatedValues += dice.rolls()
            }

            it("should not be empty") {
                assert(generatedValues.isNotEmpty()) { "Generated values are empty! [$generatedValues]" }
            }
        }

        generatedValues.forEach {
            it("should be size 1") {
                assert(it.size == 1) { "Should be size == 1 {$it}" }
            }
        }

        describe("a random tests for $typeOfTest") {
            describe("generate random $typeOfTest") {
                generatedValues.forEach { vals ->
                    vals.forEach {
                        value = it
                        it("$value should be $expectedRegEx") {
                            assert(value.toString().matches(expectedRegEx)) { "Value [$value] don't match regex [$expectedRegEx]" }
                        }
                    }
                }
            }
        }
    }

    describe("a $typeOfTest rolls() 5 times") {
        val dice = IDice.init(diceType, expectedValues)
        val generatedValues = arrayListOf<List<Int>>()
        var value: Int

        describe("generate ${Constants.generateValues} values for tests") {
            (1..Constants.generateValues).forEach { _ ->
                generatedValues += dice.rolls(rollTimes)
            }

            it("should not be empty") {
                assert(generatedValues.isNotEmpty()) { "Generated values are empty! [$generatedValues]" }
            }
        }

        generatedValues.forEach {
            it("should be size $rollTimes") {
                assert(it.size == rollTimes) { "Should be size == $rollTimes {$it}" }
            }
        }

        describe("a random tests for $typeOfTest") {
            describe("generate random $typeOfTest") {
                generatedValues.forEach { vals ->
                    vals.forEach {
                        value = it
                        it("$value should be $expectedRegEx") {
                            assert(value.toString().matches(expectedRegEx)) { "Value [$value] don't match regex [$expectedRegEx]" }
                        }
                    }
                }
            }
        }
    }
})
