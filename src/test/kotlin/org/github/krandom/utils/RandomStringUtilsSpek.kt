package org.github.krandom.utils

import org.github.krandom.utils.apache.RandomStringUtils.random
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import kotlin.test.assertFailsWith

object RandomStringUtilsSpek : Spek({

    describe("a random test for random method apache") {
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

        describe("test if start end flags works well with chars null") {
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

        describe("test if start=0 and end=0 not null chars") {
            val res = random(count = 2, start = 0, end = 0, letters = true, numbers = false, chars = null)
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

        describe("test if start=0 and end=0 not null chars") {
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

        describe("test if start=0 and end=0 not null chars") {
            val res = random(count = 2, start = 0, end = 0, letters = false, numbers = true, chars = null)
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

    describe("test if exception is thrown") {
        it("should throw IllegalArgumentException exception because end char is < 48") {
            assertFailsWith(IllegalArgumentException::class, "Should fail because end char is < 48") {
                random(count = 2, start = 0, end = 47, letters = true, numbers = false, chars = null)
            }
        }

        it("should throw IllegalArgumentException exception because end char is == 48") {
            assertFailsWith(IllegalArgumentException::class, "Should fail because end char is == 48") {
                random(count = 2, start = 0, end = 48, letters = false, numbers = true, chars = null)
            }
        }

        it("should throw IllegalArgumentException exception because end char is < start char") {
            assertFailsWith(IllegalArgumentException::class, "Should fail because end char is < 48") {
                random(count = 2, start = 47, end = 0, letters = true, numbers = false, chars = null)
            }
        }

        it("should throw IllegalArgumentException exception because chars is empty") {
            assertFailsWith(IllegalArgumentException::class, "Should fail because chars is empty") {
                random(count = 2, start = 0, end = 47, letters = true, numbers = false, chars = CharArray(0))
            }
        }
    }

})
