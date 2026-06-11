/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.kotest

import io.github.frikit.krandom.generator.GeneratorConfig
import io.github.frikit.krandom.generator.Generators
import io.github.frikit.krandom.generator.base.IntGenerator
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.ints.shouldBeInRange
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotBeBlank
import io.kotest.property.Arb
import io.kotest.property.arbitrary.take
import io.kotest.property.checkAll

class KrandomArbTest : DescribeSpec({

    describe("toArb extension") {

        it("converts a scalar generator to an Arb") {
            val emailArb: Arb<String> = Generators.ofEmail().toArb()
            val samples = emailArb.take(50).toList()
            samples shouldHaveSize 50
            samples.forEach { it shouldContain "@" }
        }

        it("converts an int generator to an Arb") {
            val intArb = Generators.ofInt(1, 100).toArb()
            val samples = intArb.take(100).toList()
            samples shouldHaveSize 100
            samples.forEach { it shouldBeInRange 1..99 }
        }

        it("converts a name generator to an Arb") {
            val nameArb = Generators.ofFullName().toArb()
            val samples = nameArb.take(20).toList()
            samples shouldHaveSize 20
            samples.forEach { it.shouldNotBeBlank() }
        }

        it("works with checkAll property testing") {
            val intArb = Generators.ofInt(1, 1000).toArb()
            checkAll(50, intArb) { value ->
                value shouldBeGreaterThan 0
            }
        }
    }

    describe("krandomArb factory") {

        it("creates an Arb from a generator factory") {
            val nameArb = krandomArb { Generators.ofFullName() }
            val samples = nameArb.take(20).toList()
            samples shouldHaveSize 20
            samples.forEach { it.shouldNotBeBlank() }
        }

        it("creates an Arb from a bounded generator factory") {
            val doubleArb = krandomArb { Generators.ofDouble(0.0, 1.0) }
            checkAll(50, doubleArb) { value ->
                value shouldNotBe null
            }
        }
    }

    describe("krandomObjectArb") {

        it("generates random instances of a data class") {
            val arb = krandomObjectArb<SamplePojo>()
            val samples = arb.take(20).toList()
            samples shouldHaveSize 20
            samples.forEach {
                it.name shouldNotBe null
                it.age shouldNotBe 0
            }
        }

        it("respects GeneratorConfig") {
            val config = GeneratorConfig.builder()
                .seed(42L)
                .build()
            val arb = krandomObjectArb<SamplePojo>(config)
            val samples = arb.take(10).toList()
            samples shouldHaveSize 10
            samples.forEach {
                it.name shouldNotBe null
            }
        }

        it("works with checkAll") {
            val arb = krandomObjectArb<SamplePojo>()
            checkAll(20, arb) { pojo ->
                pojo shouldNotBe null
                pojo.name shouldNotBe null
            }
        }
    }

    describe("edge cases") {

        it("toArb supports zero and single-sample takes") {
            val intArb = Generators.ofInt(1, 100).toArb()
            intArb.take(0).toList() shouldHaveSize 0
            intArb.take(1).toList() shouldHaveSize 1
        }

        it("toArb stays within generator bounds across a large sample") {
            val samples = Generators.ofInt(10, 20).toArb().take(500).toList()
            samples shouldHaveSize 500
            samples.forEach { it shouldBeInRange 10..19 }
        }

        it("krandomArb preserves seeded sequence progression across repeated sampling") {
            val first = krandomArb { IntGenerator(1, 1_000_000, 42L) }.take(50).toList()
            val second = krandomArb { IntGenerator(1, 1_000_000, 42L) }.take(50).toList()
            first shouldBe second
        }

        it("krandomObjectArb with a seeded config is deterministic across instances") {
            val config = GeneratorConfig.builder().seed(42L).build()
            val first = krandomObjectArb<SamplePojo>(config).take(10).toList().map { it.name to it.age }
            val second = krandomObjectArb<SamplePojo>(config).take(10).toList().map { it.name to it.age }
            first shouldBe second
        }
    }
})

/** Simple POJO for ObjectGenerator-based tests. */
class SamplePojo {
    var name: String = ""
    var age: Int = 0
    var email: String = ""
}
