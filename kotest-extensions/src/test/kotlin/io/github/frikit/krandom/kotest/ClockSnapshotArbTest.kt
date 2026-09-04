/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.kotest

import io.github.frikit.krandom.generator.Generator
import io.github.frikit.krandom.generator.GeneratorConfig
import io.github.frikit.krandom.generator.GenerationRecipe
import io.github.frikit.krandom.generator.datetime.DateGenerator
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.property.RandomSource
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset

class ClockSnapshotArbTest : DescribeSpec({
    it("one snapshot is shared by Kotest samples and failure diagnostics") {
        val clock = MovingClock()
        val config = GeneratorConfig.builder().seed(42L).clock(clock).build().snapshotClock()
        val arb = krandomArb(config) { sample ->
            Generator { DateGenerator(sample).future(1) }
        }
        val first = arb.samples(RandomSource.seeded(42L)).take(5).map { it.value }.toList()
        clock.now = clock.now.plusSeconds(86400)
        val recipe = krandomKotestRecipe(config)
        val replay = GenerationRecipe.parse(recipe).toGeneratorConfig()
        val repeated = krandomArb(replay) { sample -> Generator { DateGenerator(sample).future(1) } }
            .samples(RandomSource.seeded(42L)).take(5).map { it.value }.toList()
        first shouldBe repeated
        val failure = shouldThrow<AssertionError> {
            checkAllWithRecipe(config, arb) { throw AssertionError("intentional failure") }
        }
        failure.message!! shouldContain recipe
        replay.clock.instant() shouldBe config.clock.instant()
    }
})

private class MovingClock : Clock() {
    var now: Instant = Instant.parse("2026-09-04T23:59:59Z")
    override fun getZone(): ZoneId = ZoneOffset.UTC
    override fun withZone(zone: ZoneId): Clock = fixed(now, zone)
    override fun instant(): Instant = now
}
