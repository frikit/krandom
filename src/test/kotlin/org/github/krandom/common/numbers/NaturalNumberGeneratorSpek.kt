package org.github.krandom.common.numbers

import org.github.krandom.testhelper.Constants
import org.github.krandom.testhelper.isNaturalNumberInRange
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import kotlin.test.assertFailsWith

object NaturalNumberGeneratorSpek : Spek({

    val testCaseName = "natural number"

    describe("a random tests for list") {
        describe("a random $testCaseName default") {
            val from = 0L
            val to = Long.MAX_VALUE - 1
            val generated = NaturalNumberGenerator.generateNaturalNumbers(Constants.generateValues)

            it("should not be empty or nulls") {
                assert(generated.isNotEmpty()) { "Generated values should not be empty!" }
                assert(!generated.isNullOrEmpty()) { "Generated values should not be null!" }
            }

            generated.forEach {
                it("Number [$it] should be $testCaseName!") {
                    isNaturalNumberInRange(testCaseName, it, from, to)
                }
            }
        }

        describe("a random $testCaseName with from") {
            val from = 100L
            val to = Long.MAX_VALUE - 1
            val generated = NaturalNumberGenerator.generateNaturalNumbers(Constants.generateValues, from = from)

            it("should not be empty or nulls") {
                assert(generated.isNotEmpty()) { "Generated values should not be empty!" }
                assert(!generated.isNullOrEmpty()) { "Generated values should not be null!" }
            }

            generated.forEach {
                it("Number [$it] should be $testCaseName!") {
                    isNaturalNumberInRange(testCaseName, it, from, to)
                }
            }
        }

        describe("a random $testCaseName with to") {
            val from = 0L
            val to = 100L
            val generated = NaturalNumberGenerator.generateNaturalNumbers(Constants.generateValues, to = to)

            it("should not be empty or nulls") {
                assert(generated.isNotEmpty()) { "Generated values should not be empty!" }
                assert(!generated.isNullOrEmpty()) { "Generated values should not be null!" }
            }

            generated.forEach {
                it("Number [$it] should be $testCaseName!") {
                    isNaturalNumberInRange(testCaseName, it, from, to)
                }
            }
        }

        describe("a random $testCaseName with from -> to") {
            val from = 10L
            val to = 100L
            val generated = NaturalNumberGenerator.generateNaturalNumbers(Constants.generateValues, from = from, to = to)

            it("should not be empty or nulls") {
                assert(generated.isNotEmpty()) { "Generated values should not be empty!" }
                assert(!generated.isNullOrEmpty()) { "Generated values should not be null!" }
            }

            generated.forEach {
                it("Number [$it] should be $testCaseName!") {
                    isNaturalNumberInRange(testCaseName, it, from, to)
                }
            }
        }

        describe("a random $testCaseName with from > to") {
            it("should throw exception") {
                assertFailsWith(IllegalArgumentException::class, "should throw illegal argument exception") {
                    NaturalNumberGenerator.generateNaturalNumbers(1, from = 100L, to = 10L)
                }
            }
        }
    }

    describe("a random tests for element") {
        describe("a random $testCaseName default") {
            val from = 0L
            val to = Long.MAX_VALUE - 1
            val generated = listOf(NaturalNumberGenerator.generateNaturalNumber())

            it("should not be empty or nulls") {
                assert(generated.isNotEmpty()) { "Generated values should not be empty!" }
                assert(!generated.isNullOrEmpty()) { "Generated values should not be null!" }
            }

            generated.forEach {
                it("Number [$it] should be $testCaseName!") {
                    isNaturalNumberInRange(testCaseName, it, from, to)
                }
            }
        }

        describe("a random $testCaseName with from") {
            val from = 100L
            val to = Long.MAX_VALUE - 1
            val generated = listOf(NaturalNumberGenerator.generateNaturalNumber(from = from))

            it("should not be empty or nulls") {
                assert(generated.isNotEmpty()) { "Generated values should not be empty!" }
                assert(!generated.isNullOrEmpty()) { "Generated values should not be null!" }
            }

            generated.forEach {
                it("Number [$it] should be $testCaseName!") {
                    isNaturalNumberInRange(testCaseName, it, from, to)
                }
            }
        }

        describe("a random $testCaseName with to") {
            val from = 0L
            val to = 100L
            val generated = listOf(NaturalNumberGenerator.generateNaturalNumber(to = to))

            it("should not be empty or nulls") {
                assert(generated.isNotEmpty()) { "Generated values should not be empty!" }
                assert(!generated.isNullOrEmpty()) { "Generated values should not be null!" }
            }

            generated.forEach {
                it("Number [$it] should be $testCaseName!") {
                    isNaturalNumberInRange(testCaseName, it, from, to)
                }
            }
        }

        describe("a random $testCaseName with from -> to") {
            val from = 10L
            val to = 100L
            val generated = listOf(NaturalNumberGenerator.generateNaturalNumber(from = from, to = to))

            it("should not be empty or nulls") {
                assert(generated.isNotEmpty()) { "Generated values should not be empty!" }
                assert(!generated.isNullOrEmpty()) { "Generated values should not be null!" }
            }

            generated.forEach {
                it("Number [$it] should be $testCaseName!") {
                    isNaturalNumberInRange(testCaseName, it, from, to)
                }
            }
        }

        describe("a random $testCaseName with from > to") {
            it("should throw exception") {
                assertFailsWith(IllegalArgumentException::class, "should throw illegal argument exception") {
                    NaturalNumberGenerator.generateNaturalNumber(from = 100, to = 10)
                }
            }
        }
    }
})
