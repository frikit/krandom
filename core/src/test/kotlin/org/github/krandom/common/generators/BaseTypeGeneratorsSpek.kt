package org.github.krandom.common.generators

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.comparables.shouldBeGreaterThanOrEqualTo
import io.kotest.matchers.comparables.shouldBeLessThan
import io.kotest.matchers.ints.shouldBeInRange
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldHaveLength
import io.kotest.matchers.string.shouldHaveLengthBetween

class BaseTypeGeneratorsSpek : DescribeSpec({

    val samples = 200

    // ── ByteGenerator ─────────────────────────────────────────────────────────
    describe("ByteGenerator") {

        it("generates distinct values over $samples calls") {
            val gen = Generators.byte(seed = null)
            val values = List(samples) { gen.generate() }.toSet()
            (values.size > 1).shouldBeTrue()
        }

        it("honours a custom range [0, 50)") {
            val gen = Generators.byte(0, 50)
            repeat(samples) {
                val v = gen.generate()
                v shouldBeGreaterThanOrEqualTo 0
                v shouldBeLessThan 50
            }
        }

        it("produces identical sequences for the same seed") {
            val a = Generators.byte(seed = 42L)
            val b = Generators.byte(seed = 42L)
            List(20) { a.generate() } shouldBe List(20) { b.generate() }
        }

        it("throws when start == end") {
            val gen = ByteGenerator(10, 10)
            shouldThrow<IllegalArgumentException> { gen.generate() }
        }
    }

    // ── ShortGenerator ────────────────────────────────────────────────────────
    describe("ShortGenerator") {

        it("generates distinct values over $samples calls") {
            val gen = Generators.short()
            val values = List(samples) { gen.generate() }.toSet()
            (values.size > 1).shouldBeTrue()
        }

        it("honours a custom range [100, 200)") {
            val gen = Generators.short(100, 200)
            repeat(samples) {
                val v = gen.generate()
                v shouldBeGreaterThanOrEqualTo 100
                v shouldBeLessThan 200
            }
        }

        it("produces identical sequences for the same seed") {
            val a = Generators.short(seed = 7L)
            val b = Generators.short(seed = 7L)
            List(20) { a.generate() } shouldBe List(20) { b.generate() }
        }

        it("throws when start == end") {
            val gen = ShortGenerator(5, 5)
            shouldThrow<IllegalArgumentException> { gen.generate() }
        }
    }

    // ── IntGenerator ──────────────────────────────────────────────────────────
    describe("IntGenerator") {

        it("generates distinct values over $samples calls") {
            val gen = Generators.int()
            val values = List(samples) { gen.generate() }.toSet()
            (values.size > 1).shouldBeTrue()
        }

        it("honours a custom range [1, 10)") {
            val gen = Generators.int(1, 10)
            repeat(samples) {
                gen.generate() shouldBeInRange (1 until 10)
            }
        }

        it("supports negative ranges [-50, -1)") {
            val gen = Generators.int(-50, -1)
            repeat(samples) {
                val v = gen.generate()
                v shouldBeGreaterThanOrEqualTo -50
                v shouldBeLessThan -1
            }
        }

        it("produces identical sequences for the same seed") {
            val a = Generators.int(seed = 99L)
            val b = Generators.int(seed = 99L)
            List(30) { a.generate() } shouldBe List(30) { b.generate() }
        }

        it("throws when start == end") {
            val gen = IntGenerator(5, 5)
            shouldThrow<IllegalArgumentException> { gen.generate() }
        }
    }

    // ── LongGenerator ─────────────────────────────────────────────────────────
    describe("LongGenerator") {

        it("generates distinct values over $samples calls") {
            val gen = Generators.long()
            val values = List(samples) { gen.generate() }.toSet()
            (values.size > 1).shouldBeTrue()
        }

        it("honours a custom range [1000L, 2000L)") {
            val gen = Generators.long(1000L, 2000L)
            repeat(samples) {
                val v = gen.generate()
                v shouldBeGreaterThanOrEqualTo 1000L
                v shouldBeLessThan 2000L
            }
        }

        it("produces identical sequences for the same seed") {
            val a = Generators.long(seed = 123L)
            val b = Generators.long(seed = 123L)
            List(30) { a.generate() } shouldBe List(30) { b.generate() }
        }

        it("throws when start == end") {
            val gen = LongGenerator(1L, 1L)
            shouldThrow<IllegalArgumentException> { gen.generate() }
        }
    }

    // ── FloatGenerator ────────────────────────────────────────────────────────
    describe("FloatGenerator") {

        it("generates values in default [0, 1) range") {
            val gen = Generators.float()
            repeat(samples) {
                val v = gen.generate()
                v shouldBeGreaterThanOrEqualTo 0f
                v shouldBeLessThan 1f
            }
        }

        it("honours a custom range [10f, 20f)") {
            val gen = Generators.float(10f, 20f)
            repeat(samples) {
                val v = gen.generate()
                v shouldBeGreaterThanOrEqualTo 10f
                v shouldBeLessThan 20f
            }
        }

        it("produces identical sequences for the same seed") {
            val a = Generators.float(seed = 55L)
            val b = Generators.float(seed = 55L)
            List(20) { a.generate() } shouldBe List(20) { b.generate() }
        }

        it("throws when start == end") {
            val gen = FloatGenerator(1f, 1f)
            shouldThrow<IllegalArgumentException> { gen.generate() }
        }
    }

    // ── DoubleGenerator ───────────────────────────────────────────────────────
    describe("DoubleGenerator") {

        it("generates values in default [0.0, 1.0) range") {
            val gen = Generators.double()
            repeat(samples) {
                val v = gen.generate()
                v shouldBeGreaterThanOrEqualTo 0.0
                v shouldBeLessThan 1.0
            }
        }

        it("honours a custom range [-5.0, 5.0)") {
            val gen = Generators.double(-5.0, 5.0)
            repeat(samples) {
                val v = gen.generate()
                v shouldBeGreaterThanOrEqualTo -5.0
                v shouldBeLessThan 5.0
            }
        }

        it("produces identical sequences for the same seed") {
            val a = Generators.double(seed = 77L)
            val b = Generators.double(seed = 77L)
            List(20) { a.generate() } shouldBe List(20) { b.generate() }
        }

        it("throws when start == end") {
            val gen = DoubleGenerator(3.14, 3.14)
            shouldThrow<IllegalArgumentException> { gen.generate() }
        }
    }

    // ── CharGenerator ─────────────────────────────────────────────────────────
    describe("CharGenerator") {

        it("generates only uppercase letters by default") {
            val gen = Generators.char(includeLowercase = false)
            repeat(samples) {
                gen.generate().isUpperCase().shouldBeTrue()
            }
        }

        it("generates only lowercase letters when configured") {
            val gen = Generators.char(includeUppercase = false)
            repeat(samples) {
                gen.generate().isLowerCase().shouldBeTrue()
            }
        }

        it("generates only digits when configured") {
            val gen = Generators.char(includeUppercase = false, includeLowercase = false, includeDigits = true)
            repeat(samples) {
                gen.generate().isDigit().shouldBeTrue()
            }
        }

        it("generates from all groups when all enabled") {
            val gen = Generators.char(includeUppercase = true, includeLowercase = true, includeDigits = true, includeSpecial = true)
            val chars = List(500) { gen.generate() }
            chars.any { it.isUpperCase() }.shouldBeTrue()
            chars.any { it.isLowerCase() }.shouldBeTrue()
            chars.any { it.isDigit() }.shouldBeTrue()
        }

        it("throws when no character group is enabled") {
            val gen = CharGenerator(
                includeUppercase = false,
                includeLowercase = false,
                includeDigits = false,
                includeSpecial = false
            )
            shouldThrow<IllegalArgumentException> { gen.generate() }
        }

        it("produces identical sequences for the same seed") {
            val a = Generators.char(seed = 11L)
            val b = Generators.char(seed = 11L)
            List(20) { a.generate() } shouldBe List(20) { b.generate() }
        }
    }

    // ── BooleanGenerator ──────────────────────────────────────────────────────
    describe("BooleanGenerator") {

        it("generates both true and false over $samples calls") {
            val gen = Generators.boolean()
            val values = List(samples) { gen.generate() }.toSet()
            values.contains(true).shouldBeTrue()
            values.contains(false).shouldBeTrue()
        }

        it("produces identical sequences for the same seed") {
            val a = Generators.boolean(seed = 3L)
            val b = Generators.boolean(seed = 3L)
            List(20) { a.generate() } shouldBe List(20) { b.generate() }
        }
    }

    // ── StringGenerator ───────────────────────────────────────────────────────
    describe("StringGenerator") {

        it("generates strings with fixed length when minLength == maxLength") {
            val gen = Generators.string(minLength = 10, maxLength = 10)
            repeat(samples) { gen.generate() shouldHaveLength 10 }
        }

        it("generates strings within the configured length range") {
            val gen = Generators.string(minLength = 3, maxLength = 8)
            repeat(samples) { gen.generate().shouldHaveLengthBetween(3, 8) }
        }

        it("generates strings containing only digits when charGenerator is digits-only") {
            val charGen = CharGenerator(includeUppercase = false, includeLowercase = false, includeDigits = true)
            val gen = Generators.string(minLength = 5, maxLength = 5, charGenerator = charGen)
            repeat(samples) {
                gen.generate().all { it.isDigit() }.shouldBeTrue()
            }
        }

        it("generates distinct strings over $samples calls") {
            val gen = Generators.string()
            val values = List(samples) { gen.generate() }.toSet()
            (values.size > 1).shouldBeTrue()
        }

        it("produces identical sequences for the same seed and same charGenerator seed") {
            val charA = CharGenerator(seed = 1L)
            val charB = CharGenerator(seed = 1L)
            val a = Generators.string(minLength = 8, maxLength = 8, charGenerator = charA, seed = 1L)
            val b = Generators.string(minLength = 8, maxLength = 8, charGenerator = charB, seed = 1L)
            List(10) { a.generate() } shouldBe List(10) { b.generate() }
        }

        it("throws when minLength < 1") {
            shouldThrow<IllegalArgumentException> { StringGenerator(minLength = 0) }
        }

        it("throws when maxLength < minLength") {
            shouldThrow<IllegalArgumentException> { StringGenerator(minLength = 10, maxLength = 5) }
        }
    }

    // ── TypeGenerator as lambda ────────────────────────────────────────────────
    describe("TypeGenerator functional interface") {

        it("can be expressed as a lambda") {
            val gen: org.github.krandom.common.TypeGenerator<String> =
                org.github.krandom.common.TypeGenerator { "hello" }
            gen.generate() shouldBe "hello"
        }

        it("can wrap any generator") {
            val intGen = Generators.int(1, 100)
            val gen: org.github.krandom.common.TypeGenerator<Int> =
                org.github.krandom.common.TypeGenerator { intGen.generate() }
            repeat(samples) {
                val v = gen.generate()
                v shouldBeGreaterThanOrEqualTo 1
                v shouldBeLessThan 100
            }
        }
    }
})
