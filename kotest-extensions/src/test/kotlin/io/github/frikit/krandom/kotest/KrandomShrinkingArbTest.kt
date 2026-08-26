/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.kotest

import io.github.frikit.krandom.generator.GeneratorConfig
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.assertions.throwables.shouldThrowAny
import io.kotest.common.ExperimentalKotest
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.ints.shouldBeInRange
import io.kotest.matchers.ints.shouldBeLessThan
import io.kotest.matchers.longs.shouldBeInRange
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.property.PropTestConfig
import io.kotest.property.RandomSource
import io.kotest.property.checkAll
import kotlin.math.nextDown
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

@OptIn(ExperimentalKotest::class)
class KrandomShrinkingArbTest : DescribeSpec({

    describe("krandomIntArb") {

        it("stays within the half-open range") {
            val samples = krandomIntArb(10, 20)
                .samples(RandomSource.seeded(1L)).take(500).map { it.value }.toList()
            samples.forEach { it shouldBeInRange 10..19 }
        }

        it("replays deterministically from the same Kotest seed") {
            val first = krandomIntArb(1, 1_000_000)
                .samples(RandomSource.seeded(99L)).take(50).map { it.value }.toList()
            val second = krandomIntArb(1, 1_000_000)
                .samples(RandomSource.seeded(99L)).take(50).map { it.value }.toList()
            first shouldBe second
        }

        it("offers only the in-range edge cases including the attainable bounds") {
            val arb = krandomIntArb(10, 20)
            val edges = (1..200)
                .mapNotNull { arb.edgecase(RandomSource.seeded(it.toLong()))?.value }
                .toSet()
            edges shouldBe setOf(10, 19)
        }

        it("shrinks a failing property to the smallest counterexample") {
            val failure = shouldThrowAny {
                checkAll(PropTestConfig(seed = 4242L), krandomIntArb(0, 1000)) { value ->
                    value shouldBeLessThan 500
                }
            }
            val message = failure.message ?: ""
            message shouldContain Regex("""Arg 0: 500 \(shrunk from \d+\)""")
        }

        it("rejects equal bounds at creation time") {
            shouldThrow<IllegalArgumentException> { krandomIntArb(5, 5) }
        }
    }

    describe("krandomLongArb") {

        it("stays within the half-open range and replays deterministically") {
            val first = krandomLongArb(10L, 1_000_000L)
                .samples(RandomSource.seeded(7L)).take(200).map { it.value }.toList()
            val second = krandomLongArb(10L, 1_000_000L)
                .samples(RandomSource.seeded(7L)).take(200).map { it.value }.toList()
            first shouldBe second
            first.forEach { it shouldBeInRange 10L..999_999L }
        }

        it("shrinks a failing property to the smallest counterexample") {
            val failure = shouldThrowAny {
                checkAll(PropTestConfig(seed = 4242L), krandomLongArb(0L, 1000L)) { value ->
                    (value < 500L).shouldBeTrue()
                }
            }
            val message = failure.message ?: ""
            message shouldContain Regex("""Arg 0: 500L \(shrunk from \d+L\)""")
        }

        it("rejects equal bounds at creation time") {
            shouldThrow<IllegalArgumentException> { krandomLongArb(5L, 5L) }
        }
    }

    describe("krandomDoubleArb") {

        it("stays within the half-open range and replays deterministically") {
            val first = krandomDoubleArb(0.5, 2.5)
                .samples(RandomSource.seeded(11L)).take(200).map { it.value }.toList()
            val second = krandomDoubleArb(0.5, 2.5)
                .samples(RandomSource.seeded(11L)).take(200).map { it.value }.toList()
            first shouldBe second
            first.forEach { (it >= 0.5 && it < 2.5).shouldBeTrue() }
        }

        it("proposes only in-range shrink candidates") {
            val arb = krandomDoubleArb(250.0, 1000.0)
            val candidates = (1..50).flatMap { seed ->
                val sample = arb.sample(RandomSource.seeded(seed.toLong()))
                sample.shrinks.children.value.map { it.value() }
            }
            candidates.shouldNotBeEmpty()
            candidates.forEach { (it >= 250.0 && it < 1000.0).shouldBeTrue() }
        }

        it("offers only in-range edge cases") {
            val arb = krandomDoubleArb(0.5, 2.5)
            val edges = (1..200)
                .mapNotNull { arb.edgecase(RandomSource.seeded(it.toLong()))?.value }
                .toSet()
            edges.forEach { (it >= 0.5 && it < 2.5).shouldBeTrue() }
            edges shouldBe setOf(0.5, 2.5.nextDown(), 1.0)
        }

        it("rejects equal bounds at creation time") {
            shouldThrow<IllegalArgumentException> { krandomDoubleArb(1.5, 1.5) }
        }
    }

    describe("krandomPickArb") {

        it("samples only source elements and replays deterministically") {
            val source = listOf("basic", "premium", "enterprise")
            val first = krandomPickArb(source)
                .samples(RandomSource.seeded(21L)).take(100).map { it.value }.toList()
            val second = krandomPickArb(source)
                .samples(RandomSource.seeded(21L)).take(100).map { it.value }.toList()
            first shouldBe second
            first.forEach { (it in source).shouldBeTrue() }
        }

        it("uses the first element as the edge case") {
            val arb = krandomPickArb(listOf("alpha", "beta", "gamma"))
            val edges = (1..50)
                .mapNotNull { arb.edgecase(RandomSource.seeded(it.toLong()))?.value }
                .toSet()
            edges shouldBe setOf("alpha")
        }

        it("shrinks selections toward the earliest failing source element") {
            val source = (0 until 100).toList()
            val failure = shouldThrowAny {
                checkAll(PropTestConfig(seed = 4242L), krandomPickArb(source)) { value ->
                    value shouldBeLessThan 50
                }
            }
            val message = failure.message ?: ""
            message shouldContain Regex("""Arg 0: 50 \(shrunk from \d+\)""")
        }

        it("rejects an empty source at creation time") {
            shouldThrow<IllegalArgumentException> { krandomPickArb(emptyList<String>()) }
        }
    }

    describe("parallel property isolation") {

        it("concurrent object sampling matches serial replay for each Kotest source") {
            val config = GeneratorConfig.builder().seed(42L).build()
            val expected = (0 until 8).map { index ->
                krandomReplayObjectArb<SamplePojo>(config)
                    .samples(RandomSource.seeded(1000L + index))
                    .take(25).map { it.value.name to it.value.age }.toList()
            }

            val actual = coroutineScope {
                (0 until 8).map { index ->
                    async(Dispatchers.Default) {
                        krandomReplayObjectArb<SamplePojo>(config)
                            .samples(RandomSource.seeded(1000L + index))
                            .take(25).map { it.value.name to it.value.age }.toList()
                    }
                }.awaitAll()
            }

            actual shouldBe expected
        }

        it("concurrent primitive sampling matches serial replay for each Kotest source") {
            val expected = (0 until 8).map { index ->
                krandomIntArb(0, 1_000_000)
                    .samples(RandomSource.seeded(2000L + index))
                    .take(100).map { it.value }.toList()
            }

            val actual = coroutineScope {
                (0 until 8).map { index ->
                    async(Dispatchers.Default) {
                        krandomIntArb(0, 1_000_000)
                            .samples(RandomSource.seeded(2000L + index))
                            .take(100).map { it.value }.toList()
                    }
                }.awaitAll()
            }

            actual shouldBe expected
        }
    }
})
