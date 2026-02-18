package org.github.krandom.common.numbers

import org.github.krandom.testhelper.isNaturalCompositeNumberInRange
import org.github.krandom.testhelper.isSorted
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.assertions.throwables.shouldThrow

class CompositeNumberGeneratorWithoutCacheSpek : DescribeSpec({

    val testCaseName = "composite number"

    USE_NATURAL_NUMBER_CACHE = false

    describe("a random tests for list") {
        describe("a random $testCaseName default") {
            val from = 2L
            val to = Long.MAX_VALUE - 1
            val generated = NaturalNumberGenerator.generateCompositeNumbers()

            it("should not be empty or nulls") {
                assert(generated.isNotEmpty()) { "Generated values should not be empty!" }
                assert(!generated.isNullOrEmpty()) { "Generated values should not be null!" }
            }

            it("should be sorted") {
                isSorted(generated)
            }

            generated.forEach {
                it("Number [$it] should be $testCaseName!") {
                    isNaturalCompositeNumberInRange(testCaseName, it, from, to)
                }
            }
        }

        describe("a random $testCaseName with from") {
            val from = 100L
            val to = Long.MAX_VALUE - 1
            val generated = NaturalNumberGenerator.generateCompositeNumbers(from = from)

            it("should not be empty or nulls") {
                assert(generated.isNotEmpty()) { "Generated values should not be empty!" }
                assert(!generated.isNullOrEmpty()) { "Generated values should not be null!" }
            }

            it("should be sorted") {
                isSorted(generated)
            }

            generated.forEach {
                it("Number [$it] should be $testCaseName!") {
                    isNaturalCompositeNumberInRange(testCaseName, it, from, to)
                }
            }
        }

        describe("a random $testCaseName with to") {
            val from = 2L
            val to = 100L
            val generated = NaturalNumberGenerator.generateCompositeNumbers(to = to)

            it("should not be empty or nulls") {
                assert(generated.isNotEmpty()) { "Generated values should not be empty!" }
                assert(!generated.isNullOrEmpty()) { "Generated values should not be null!" }
            }

            it("should be sorted") {
                isSorted(generated)
            }

            generated.forEach {
                it("Number [$it] should be $testCaseName!") {
                    isNaturalCompositeNumberInRange(testCaseName, it, from, to)
                }
            }
        }

        describe("a random $testCaseName with from -> to") {
            val from = 10L
            val to = 100L
            val generated = NaturalNumberGenerator.generateCompositeNumbers(from = from, to = to)

            it("should not be empty or nulls") {
                assert(generated.isNotEmpty()) { "Generated values should not be empty!" }
                assert(!generated.isNullOrEmpty()) { "Generated values should not be null!" }
            }

            it("should be sorted") {
                isSorted(generated)
            }

            generated.forEach {
                it("Number [$it] should be $testCaseName!") {
                    isNaturalCompositeNumberInRange(testCaseName, it, from, to)
                }
            }
        }

        describe("a random $testCaseName with from > to") {
            it("should throw exception") {
                shouldThrow<IllegalArgumentException> {
                    NaturalNumberGenerator.generateCompositeNumbers(from = 100, to = 10)
                }
            }
        }
    }

    describe("a random tests for element") {
        describe("a random $testCaseName default") {
            val from = 2L
            val to = Long.MAX_VALUE - 1
            val generated = listOf(NaturalNumberGenerator.generateCompositeNumber())

            it("should not be empty or nulls") {
                assert(generated.isNotEmpty()) { "Generated values should not be empty!" }
                assert(!generated.isNullOrEmpty()) { "Generated values should not be null!" }
            }

            generated.forEach {
                it("Number [$it] should be $testCaseName!") {
                    isNaturalCompositeNumberInRange(testCaseName, it, from, to)
                }
            }
        }

        describe("a random $testCaseName with from") {
            val from = 100L
            val to = Long.MAX_VALUE - 1
            val generated = listOf(NaturalNumberGenerator.generateCompositeNumber(from = from))

            it("should not be empty or nulls") {
                assert(generated.isNotEmpty()) { "Generated values should not be empty!" }
                assert(!generated.isNullOrEmpty()) { "Generated values should not be null!" }
            }

            generated.forEach {
                it("Number [$it] should be $testCaseName!") {
                    isNaturalCompositeNumberInRange(testCaseName, it, from, to)
                }
            }
        }

        describe("a random $testCaseName with to") {
            val from = 2L
            val to = 100L
            val generated = listOf(NaturalNumberGenerator.generateCompositeNumber(to = to))

            it("should not be empty or nulls") {
                assert(generated.isNotEmpty()) { "Generated values should not be empty!" }
                assert(!generated.isNullOrEmpty()) { "Generated values should not be null!" }
            }

            generated.forEach {
                it("Number [$it] should be $testCaseName!") {
                    isNaturalCompositeNumberInRange(testCaseName, it, from, to)
                }
            }
        }

        describe("a random $testCaseName with from -> to") {
            val from = 2L
            val to = 100L
            val generated = listOf(NaturalNumberGenerator.generateCompositeNumber(from = 10, to = 100))

            it("should not be empty or nulls") {
                assert(generated.isNotEmpty()) { "Generated values should not be empty!" }
                assert(!generated.isNullOrEmpty()) { "Generated values should not be null!" }
            }

            generated.forEach {
                it("Number [$it] should be $testCaseName!") {
                    isNaturalCompositeNumberInRange(testCaseName, it, from, to)
                }
            }
        }

        describe("a random $testCaseName with from > to") {
            it("should throw exception") {
                shouldThrow<IllegalArgumentException> {
                    NaturalNumberGenerator.generateCompositeNumber(from = 100, to = 10)
                }
            }
        }
    }
})
