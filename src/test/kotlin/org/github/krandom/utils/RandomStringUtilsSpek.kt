package org.github.krandom.utils

import org.github.krandom.utils.apache.RandomStringUtils.random
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

object RandomStringUtilsSpek : Spek({

    describe("a random from a chararray from used") {
        val chars = "abcdfgh".toCharArray()

        (1..10).forEach { index ->
            val res = random(count = 2, start = 0, end = 0, letters = true, numbers = false, chars = chars)

            describe("[$index idx]test if [$res] is valid generated") {
                it("[$res] should not be blank") {
                    assert(res.isNotBlank()) { "[$res] is blank!" }
                }
                it("[$res] should not be empty") {
                    assert(res.isNotEmpty()) { "[$res] is empty!" }
                }
                it("[$res] should be valid size") {
                    assert(res.length > 1)
                }
            }
        }

        describe("test if max code point is set") {
            val res = random(count = 2, start = 0, end = 0, letters = false, numbers = false, chars = null)
            it("[$res] should not be blank") {
                assert(res.isNotBlank()) { "[$res] is blank!" }
            }
            it("[$res] should not be empty") {
                assert(res.isNotEmpty()) { "[$res] is empty!" }
            }
            it("[$res] should be valid size") {
                assert(res.length > 1)
            }
        }

        describe("test if start end flags works well") {
            val res = random(count = 2, start = 0, end = 99, letters = true, numbers = false, chars = null)
            it("[$res] should not be blank") {
                assert(res.isNotBlank()) { "[$res] is blank!" }
            }
            it("[$res] should not be empty") {
                assert(res.isNotEmpty()) { "[$res] is empty!" }
            }
            it("[$res] should be valid size") {
                assert(res.length > 1)
            }
        }
    }
})
