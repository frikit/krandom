/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.common.numbers

import io.kotest.core.spec.style.DescribeSpec

class PrimeCompositeNumberMethodCheckSpek : DescribeSpec({
    val primeNumbers = listOf<Long>(2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41, 43, 47, 53, 59, 61, 67, 71)
    val compositeNumbers = listOf<Long>(4, 6, 8, 9, 10, 12, 14, 15, 16, 18, 20, 21, 22, 24, 25, 26, 27, 28, 30)

    val invalidPrimeNumbers = listOf(0, 1 , compositeNumbers[0], compositeNumbers[1], compositeNumbers[2])
    val invalidCompositeNumbers = listOf(0, 1, primeNumbers[0], primeNumbers[1], primeNumbers[2])

    describe("a test for check prime numbers are valid") {
        primeNumbers.forEach {
            it("[$it] should be prime number") {
                assert(NaturalNumberGenerator.isPrimeNumber(it)) { "This number [$it] should be prime!" }
            }
        }
    }

    describe("a test for check composite numbers are valid") {
        primeNumbers.forEach {
            it("[$it] should be composite number") {
                assert(NaturalNumberGenerator.isPrimeNumber(it)) { "This number [$it] should be composite!" }
            }
        }
    }

    describe("a test for check prime numbers are invalid") {
        invalidPrimeNumbers.forEach {
            it("[$it] should be invalid prime number") {
                assert(!NaturalNumberGenerator.isPrimeNumber(it)) { "This number [$it] should be invalid prime!" }
            }
        }
    }

    describe("a test for check composite numbers are invalid") {
        invalidCompositeNumbers.forEach {
            it("[$it] should be invalid composite number") {
                assert(!NaturalNumberGenerator.isCompositeNumber(it)) { "This number [$it] should be invalid composite!" }
            }
        }
    }

})
