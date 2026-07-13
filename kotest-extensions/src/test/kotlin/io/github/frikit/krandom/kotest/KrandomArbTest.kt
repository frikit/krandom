/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.kotest

import io.github.frikit.krandom.generator.GeneratorConfig
import io.github.frikit.krandom.generator.user.EmailGenerator
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import io.kotest.property.Arb
import io.kotest.property.RandomSource
import io.kotest.property.arbitrary.take
import io.kotest.property.checkAll
import java.util.Random

class KrandomArbTest : DescribeSpec({

    describe("failure recipes") {

        it("krandomKotestRecipe serializes the portable configuration") {
            val recipe = krandomKotestRecipe(GeneratorConfig.builder().locale(java.util.Locale.GERMANY).build())
            recipe shouldContain "locale=de-DE"
        }

        it("checkAllWithRecipe appends the recipe to a failing property") {
            val failure = shouldThrow<AssertionError> {
                checkAllWithRecipe(GeneratorConfig.defaults(), krandomIntArb(0, 10)) { value ->
                    value shouldBeGreaterThan 100
                }
            }
            (failure.message ?: "") shouldContain "kRandom recipe"
            (failure.message ?: "") shouldContain "format=krandom-recipe"
        }

        it("checkAllWithRecipe stays silent for passing properties") {
            checkAllWithRecipe(GeneratorConfig.defaults(), krandomIntArb(0, 10)) { value ->
                value shouldBeGreaterThan -1
            }
        }
    }

    describe("krandomArb factory") {

        it("uses Kotest random-source draws to create fresh reproducible generators") {
            var factoryCalls = 0
            val arb = krandomArb(GeneratorConfig.defaults()) { config ->
                factoryCalls++
                io.github.frikit.krandom.generator.Generator { config.createRandom().nextInt() }
            }

            val first = arb.samples(RandomSource.seeded(314159L)).take(20).map { it.value }.toList()
            val second = arb.samples(RandomSource.seeded(314159L)).take(20).map { it.value }.toList()

            first shouldBe second
            factoryCalls shouldBe 40
        }

        it("creates replay-safe scalar arbs from generator factories") {
            val emailArb: Arb<String> = krandomArb(GeneratorConfig.defaults()) { config ->
                EmailGenerator(config)
            }
            val samples = emailArb.samples(RandomSource.seeded(7L)).take(50).map { it.value }.toList()
            samples shouldHaveSize 50
            samples.forEach { it shouldContain "@" }
        }

        it("works with checkAll property testing") {
            checkAll(50, krandomIntArb(1, 1000)) { value ->
                value shouldBeGreaterThan 0
            }
        }
    }

    describe("krandomReplayObjectArb") {

        it("generates random instances of a data class") {
            val arb = krandomReplayObjectArb<SamplePojo>()
            val samples = arb.take(20).toList()
            samples shouldHaveSize 20
            samples.forEach {
                it.name shouldNotBe null
            }
        }

        it("respects GeneratorConfig") {
            val config = GeneratorConfig.builder()
                .seed(42L)
                .build()
            val arb = krandomReplayObjectArb<SamplePojo>(config)
            val samples = arb.take(10).toList()
            samples shouldHaveSize 10
            samples.forEach {
                it.name shouldNotBe null
            }
        }

        it("works with checkAll") {
            val arb = krandomReplayObjectArb<SamplePojo>()
            checkAll(20, arb) { pojo ->
                pojo shouldNotBe null
                pojo.name shouldNotBe null
            }
        }
    }

    describe("edge cases") {

        it("krandomIntArb supports zero and single-sample takes") {
            val intArb = krandomIntArb(1, 100)
            intArb.take(0).toList() shouldHaveSize 0
            intArb.take(1).toList() shouldHaveSize 1
        }

        it("krandomObjectArb with identical Kotest sources is deterministic across instances") {
            val config = GeneratorConfig.builder().seed(42L).build()
            val first = krandomReplayObjectArb<SamplePojo>(config).samples(RandomSource.seeded(42L)).take(10)
                .map { it.value.name to it.value.age }.toList()
            val second = krandomReplayObjectArb<SamplePojo>(config).samples(RandomSource.seeded(42L)).take(10)
                .map { it.value.name to it.value.age }.toList()
            first shouldBe second
        }

        it("krandomReplayObjectArb replays from the Kotest random source") {
            val config = GeneratorConfig.builder().seed(42L).build()
            val arb = krandomReplayObjectArb<SamplePojo>(config)

            val first = arb.samples(RandomSource.seeded(271828L)).take(10)
                .map { it.value.name to it.value.age }.toList()
            val second = arb.samples(RandomSource.seeded(271828L)).take(10)
                .map { it.value.name to it.value.age }.toList()

            first shouldBe second
        }

        it("krandomObjectArb rejects a caller-owned random source") {
            val config = GeneratorConfig.builder().random(Random(42L)).build()

            shouldThrow<IllegalArgumentException> {
                krandomReplayObjectArb<SamplePojo>(config).sample(RandomSource.seeded(1L))
            }
        }
    }
})

/** Simple POJO for ObjectGenerator-based tests. */
class SamplePojo {
    var name: String = ""
    var age: Int = 0
    var email: String = ""
}
