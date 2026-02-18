package org.github.krandom.common.string.hash

import org.github.krandom.testhelper.Constants
import org.github.krandom.testhelper.isValidHexHash
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.assertions.throwables.shouldThrow

class HexHashSpek : DescribeSpec({

    val testCaseName = "hex hash"

    describe("a random $testCaseName all default") {
        val expectedLength = 32
        val expectedReturnType = HashGeneratorResult.NONE

        (1..Constants.generateValues).forEach { id ->
            it("[$id]should be defaulted length and return") {
                val actual = HexHashGenerator.randomHexHash()

                isValidHexHash(actual, expectedReturnType, expectedLength)
            }
        }
    }

    describe("a random $testCaseName all default , upper case return") {
        val expectedLength = 32
        val expectedReturnType = HashGeneratorResult.UPPER_CASE
        val actual = HexHashGenerator.randomHexHash(returnProcessorStrategy = expectedReturnType)

        it("should be defaulted length and custom return") {
            isValidHexHash(actual, expectedReturnType, expectedLength)
        }
    }

    describe("a random $testCaseName all default , lower case return") {
        val expectedLength = 32
        val expectedReturnType = HashGeneratorResult.LOWER_CASE
        val actual = HexHashGenerator.randomHexHash(returnProcessorStrategy = expectedReturnType)

        it("should be defaulted length and custom return") {
            isValidHexHash(actual, expectedReturnType, expectedLength)
        }
    }

    describe("a random $testCaseName for all valid input length") {
        (1..1024).forEach { index ->
            it("should be length=$index and return=default") {
                val expectedReturnType = HashGeneratorResult.NONE
                val actual = HexHashGenerator.randomHexHash(index, expectedReturnType)

                isValidHexHash(actual, expectedReturnType, index)
            }
            it("should be length=$index and return=upper") {
                val expectedReturnType = HashGeneratorResult.UPPER_CASE
                val actual = HexHashGenerator.randomHexHash(index, expectedReturnType)

                isValidHexHash(actual, expectedReturnType, index)
            }
            it("should be length=$index and return=lower") {
                val expectedReturnType = HashGeneratorResult.LOWER_CASE
                val actual = HexHashGenerator.randomHexHash(index, expectedReturnType)

                isValidHexHash(actual, expectedReturnType, index)
            }
        }
    }

    describe("a random $testCaseName with from < 1") {
        it("should throw exception") {
            shouldThrow<IllegalArgumentException> {
                HexHashGenerator.randomHexHash(0)
            }
        }
    }

    describe("a random $testCaseName with from > 1024") {
        it("should throw exception") {
            shouldThrow<IllegalArgumentException> {
                HexHashGenerator.randomHexHash(1025)
            }
        }
    }

})
