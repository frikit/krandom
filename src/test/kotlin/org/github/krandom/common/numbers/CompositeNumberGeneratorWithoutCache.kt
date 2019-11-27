package org.github.krandom.common.numbers

import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import kotlin.test.assertFailsWith

object CompositeNumberGeneratorWithoutCache : Spek({

    val testCaseName = "composite number"

    USE_NATURAL_NUMBER_CACHE = false

    describe("a random tests for list") {
        describe("a random $testCaseName default") {
            val generated = NaturalNumberGenerator.generateCompositeNumbers()

            it("should not be empty or nulls") {
                assert(generated.isNotEmpty()) { "Generated values should not be empty!" }
                assert(!generated.isNullOrEmpty()) { "Generated values should not be null!" }
            }

            generated.forEach {
                it("Number [$it] should be $testCaseName!") {
                    assert(NaturalNumberGenerator.isCompositeNumber(it)) { "Number [$it] should be $testCaseName!" }
                }
            }
        }

        describe("a random $testCaseName with from") {
            val generated = NaturalNumberGenerator.generateCompositeNumbers(from = 100)

            it("should not be empty or nulls") {
                assert(generated.isNotEmpty()) { "Generated values should not be empty!" }
                assert(!generated.isNullOrEmpty()) { "Generated values should not be null!" }
            }

            generated.forEach {
                it("Number [$it] should be $testCaseName!") {
                    assert(NaturalNumberGenerator.isCompositeNumber(it)) { "Number [$it] should be $testCaseName!" }
                }
            }
        }

        describe("a random $testCaseName with to") {
            val generated = NaturalNumberGenerator.generateCompositeNumbers(to = 100)

            it("should not be empty or nulls") {
                assert(generated.isNotEmpty()) { "Generated values should not be empty!" }
                assert(!generated.isNullOrEmpty()) { "Generated values should not be null!" }
            }

            generated.forEach {
                it("Number [$it] should be $testCaseName!") {
                    assert(NaturalNumberGenerator.isCompositeNumber(it)) { "Number [$it] should be $testCaseName!" }
                }
            }
        }

        describe("a random $testCaseName with from -> to") {
            val generated = NaturalNumberGenerator.generateCompositeNumbers(from = 10, to = 100)

            it("should not be empty or nulls") {
                assert(generated.isNotEmpty()) { "Generated values should not be empty!" }
                assert(!generated.isNullOrEmpty()) { "Generated values should not be null!" }
            }

            generated.forEach {
                it("Number [$it] should be $testCaseName!") {
                    assert(NaturalNumberGenerator.isCompositeNumber(it)) { "Number [$it] should be $testCaseName!" }
                }
            }
        }

        describe("a random $testCaseName with from > to") {
            it("should throw exception") {
                assertFailsWith(IllegalArgumentException::class, "should throw illegal argument exception") {
                    NaturalNumberGenerator.generateCompositeNumbers(from = 100, to = 10)
                }
            }
        }
    }

    describe("a random tests for element") {
        describe("a random $testCaseName default") {
            val generated = listOf(NaturalNumberGenerator.generateCompositeNumber())

            it("should not be empty or nulls") {
                assert(generated.isNotEmpty()) { "Generated values should not be empty!" }
                assert(!generated.isNullOrEmpty()) { "Generated values should not be null!" }
            }

            generated.forEach {
                it("Number [$it] should be $testCaseName!") {
                    assert(NaturalNumberGenerator.isCompositeNumber(it)) { "Number [$it] should be $testCaseName!" }
                }
            }
        }

        describe("a random $testCaseName with from") {
            val generated = listOf(NaturalNumberGenerator.generateCompositeNumber(from = 100))

            it("should not be empty or nulls") {
                assert(generated.isNotEmpty()) { "Generated values should not be empty!" }
                assert(!generated.isNullOrEmpty()) { "Generated values should not be null!" }
            }

            generated.forEach {
                it("Number [$it] should be $testCaseName!") {
                    assert(NaturalNumberGenerator.isCompositeNumber(it)) { "Number [$it] should be $testCaseName!" }
                }
            }
        }

        describe("a random $testCaseName with to") {
            val generated = listOf(NaturalNumberGenerator.generateCompositeNumber(to = 100))

            it("should not be empty or nulls") {
                assert(generated.isNotEmpty()) { "Generated values should not be empty!" }
                assert(!generated.isNullOrEmpty()) { "Generated values should not be null!" }
            }

            generated.forEach {
                it("Number [$it] should be $testCaseName!") {
                    assert(NaturalNumberGenerator.isCompositeNumber(it)) { "Number [$it] should be $testCaseName!" }
                }
            }
        }

        describe("a random $testCaseName with from -> to") {
            val generated = listOf(NaturalNumberGenerator.generateCompositeNumber(from = 10, to = 100))

            it("should not be empty or nulls") {
                assert(generated.isNotEmpty()) { "Generated values should not be empty!" }
                assert(!generated.isNullOrEmpty()) { "Generated values should not be null!" }
            }

            generated.forEach {
                it("Number [$it] should be $testCaseName!") {
                    assert(NaturalNumberGenerator.isCompositeNumber(it)) { "Number [$it] should be $testCaseName!" }
                }
            }
        }

        describe("a random $testCaseName with from > to") {
            it("should throw exception") {
                assertFailsWith(IllegalArgumentException::class, "should throw illegal argument exception") {
                    NaturalNumberGenerator.generateCompositeNumber(from = 100, to = 10)
                }
            }
        }
    }
})
