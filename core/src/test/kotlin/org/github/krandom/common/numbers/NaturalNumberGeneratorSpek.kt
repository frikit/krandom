/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.common.numbers

import org.github.krandom.testhelper.Constants
import org.github.krandom.testhelper.isNaturalNumberInRange
import org.github.krandom.testhelper.isNotSorted
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.assertions.throwables.shouldThrow

class NaturalNumberGeneratorSpek : DescribeSpec({

    val testCaseName = "natural number"

    describe("a random tests for validation") {

        it("2 is valid $testCaseName") {
            assert(NaturalNumberGenerator.isNaturalNumber(2)) { "2 is valid $testCaseName!!!" }
        }

        it("0 is invalid $testCaseName") {
            assert(!NaturalNumberGenerator.isNaturalNumber(0)) { "0 is invalid $testCaseName!!!" }
        }

        it("${Long.MAX_VALUE} is invalid $testCaseName") {
            assert(!NaturalNumberGenerator.isNaturalNumber(Long.MAX_VALUE)) { "${Long.MAX_VALUE} is invalid $testCaseName!!!" }
        }

    }

    describe("a random tests for list") {

        describe("a random $testCaseName default") {
            val from = 0L
            val to = Long.MAX_VALUE - 1
            val generated = NaturalNumberGenerator.generateNaturalNumbers(Constants.generateValues)

            it("should not be empty or nulls") {
                assert(generated.isNotEmpty()) { "Generated values should not be empty!" }
                assert(!generated.isNullOrEmpty()) { "Generated values should not be null!" }
            }

            it("should not be sorted") {
                isNotSorted(generated)
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

            it("should not be sorted") {
                isNotSorted(generated)
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

            it("should not be sorted") {
                isNotSorted(generated)
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

            it("should not be sorted") {
                isNotSorted(generated)
            }

            generated.forEach {
                it("Number [$it] should be $testCaseName!") {
                    isNaturalNumberInRange(testCaseName, it, from, to)
                }
            }
        }

        describe("a random $testCaseName with -from -> to") {
            val from = 0L
            val to = 100L
            val generated = NaturalNumberGenerator.generateNaturalNumbers(Constants.generateValues, from = -10L, to = to)

            it("should not be empty or nulls") {
                assert(generated.isNotEmpty()) { "Generated values should not be empty!" }
                assert(!generated.isNullOrEmpty()) { "Generated values should not be null!" }
            }

            it("should not be sorted") {
                isNotSorted(generated)
            }

            generated.forEach {
                it("Number [$it] should be $testCaseName!") {
                    isNaturalNumberInRange(testCaseName, it, from, to)
                }
            }
        }

        describe("a random $testCaseName with from -> to, exclude some") {
            val from = 10L
            val to = 100L
            val exclude = (from..55L).toSet()
            val generated = NaturalNumberGenerator.generateNaturalNumbers(Constants.generateValues, from = from, to = to, excludeNumbers = exclude)

            it("should not be empty or nulls") {
                assert(generated.isNotEmpty()) { "Generated values should not be empty!" }
                assert(!generated.isNullOrEmpty()) { "Generated values should not be null!" }
            }

            it("should not be sorted") {
                isNotSorted(generated)
            }

            generated.forEach {
                it("Number [$it] should be $testCaseName and not in excludes!") {
                    isNaturalNumberInRange(testCaseName, it, from, to)
                    assert(!exclude.contains(it)) { "Number [$it] should not be from [$exclude]" }
                }
            }
        }

        describe("a random $testCaseName with from -> to with size 0") {
            val generated = NaturalNumberGenerator.generateNaturalNumbers(0)

            it("should be empty") {
                assert(generated.isEmpty()) { "Generated values should be empty!" }
            }
        }

        describe("a random $testCaseName with from -> to with size 10 and exclude all") {
            val range = (1L..10L)
            val from = range.first
            val to = range.last
            val generated = NaturalNumberGenerator.generateNaturalNumbers(10, from, to, range.toSet())

            it("should be empty") {
                assert(generated.isEmpty()) { "Generated values should be empty!" }
            }
        }

        describe("a random $testCaseName with from > to") {
            it("should throw exception") {
                shouldThrow<IllegalArgumentException> {
                    NaturalNumberGenerator.generateNaturalNumbers(1, from = 100L, to = 10L)
                }
            }
        }

        describe("a random $testCaseName with -from > -to") {
            it("should throw exception") {
                shouldThrow<IllegalArgumentException> {
                    NaturalNumberGenerator.generateNaturalNumbers(1, from = -10L, to = -100L)
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
                shouldThrow<IllegalArgumentException> {
                    NaturalNumberGenerator.generateNaturalNumber(from = 100, to = 10)
                }
            }
        }

    }
})
