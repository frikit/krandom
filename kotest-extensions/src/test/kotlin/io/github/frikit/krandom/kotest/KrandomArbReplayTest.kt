/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.kotest

import io.github.frikit.krandom.generator.Generator
import io.github.frikit.krandom.generator.GeneratorConfig
import io.github.frikit.krandom.generator.GenerationRecipe
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import io.kotest.property.PropTestConfig
import io.kotest.property.RandomSource
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.Collections

class KrandomArbReplayTest : DescribeSpec({

    describe("configured seeds") {

        it("krandomArb honours the configuration seed") {
            fun samples(seed: Long) = krandomArb(GeneratorConfig.builder().seed(seed).build()) { sample ->
                Generator { sample.createRandom().nextLong() }
            }.samples(RandomSource.seeded(7L)).take(10).map { it.value }.toList()

            samples(1L) shouldBe samples(1L)
            samples(1L) shouldNotBe samples(2L)
        }

        it("krandomReplayObjectArb honours the configuration seed") {
            fun samples(seed: Long) = krandomReplayObjectArb<SamplePojo>(GeneratorConfig.builder().seed(seed).build())
                .samples(RandomSource.seeded(7L)).take(10).map { it.value.name to it.value.age }.toList()

            samples(42L) shouldBe samples(42L)
            samples(42L) shouldNotBe samples(43L)
        }

        it("an unseeded configuration keeps the seed-zero sample stream") {
            fun samples(config: GeneratorConfig) = krandomArb(config) { sample ->
                Generator { sample.createRandom().nextLong() }
            }.samples(RandomSource.seeded(11L)).take(10).map { it.value }.toList()

            samples(GeneratorConfig.defaults()) shouldBe samples(GeneratorConfig.builder().seed(0L).build())
        }

        it("the recipe records the configured seed so it replays the same samples") {
            val config = GeneratorConfig.builder().seed(42L).build()
            val recipe = krandomKotestRecipe(config)
            recipe shouldContain "seed=42"

            val replay = GenerationRecipe.parse(recipe).toGeneratorConfig()
            fun samples(source: GeneratorConfig) = krandomReplayObjectArb<SamplePojo>(source)
                .samples(RandomSource.seeded(5L)).take(10).map { it.value.name to it.value.age }.toList()
            samples(replay) shouldBe samples(config)
        }
    }

    describe("clock-snapshotting checkAllWithRecipe") {

        it("snapshots a live clock once for every sample and the failure recipe") {
            val clock = TickingClock()
            val config = GeneratorConfig.builder().seed(42L).clock(clock).build()
            val seen = Collections.synchronizedList(mutableListOf<Instant>())

            val failure = shouldThrow<AssertionError> {
                checkAllWithRecipe(config, { session ->
                    krandomArb(session) { sample -> Generator { sample.clock.instant() } }
                }) { instant ->
                    seen += instant
                    throw AssertionError("intentional failure")
                }
            }

            seen.toSet() shouldHaveSize 1
            val message = failure.message ?: ""
            message shouldContain "clock snapshotted before sampling"
            val recipe = GenerationRecipe.parse(message.substring(message.indexOf("format=krandom-recipe")))
            recipe.clockInstant shouldBe seen.first()
            recipe.seed shouldBe 42L
        }

        it("replays deterministically with a pinned Kotest seed") {
            val config = GeneratorConfig.builder().seed(9L).clock(Clock.fixed(Instant.EPOCH, ZoneOffset.UTC)).build()
            suspend fun run(): List<Long> {
                val values = Collections.synchronizedList(mutableListOf<Long>())
                checkAllWithRecipe(config, { session ->
                    krandomArb(session) { sample -> Generator { sample.createRandom().nextLong() } }
                }, PropTestConfig(seed = 1234L, iterations = 20)) { value -> values += value }
                return values.toList()
            }

            run() shouldBe run()
        }

        it("stays silent for passing properties") {
            checkAllWithRecipe(GeneratorConfig.defaults(), { _ -> krandomIntArb(0, 10) }) { value ->
                (value in 0 until 10) shouldBe true
            }
        }
    }

    describe("single-configuration checkAllWithRecipe") {

        it("states that the recipe clock is read when the failure is reported") {
            val failure = shouldThrow<AssertionError> {
                checkAllWithRecipe(GeneratorConfig.defaults(), krandomIntArb(0, 10)) { value ->
                    (value > 100) shouldBe true
                }
            }
            (failure.message ?: "") shouldContain "clock read when the failure was reported"
        }
    }
})

/** A live clock that moves one day forward on every read. */
private class TickingClock : Clock() {
    private var now: Instant = Instant.parse("2026-09-04T23:59:59Z")
    override fun getZone(): ZoneId = ZoneOffset.UTC
    override fun withZone(zone: ZoneId): Clock = fixed(now, zone)

    @Synchronized
    override fun instant(): Instant {
        val current = now
        now = now.plusSeconds(86_400)
        return current
    }
}
