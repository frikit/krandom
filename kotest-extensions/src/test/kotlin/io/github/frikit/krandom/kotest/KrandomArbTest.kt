/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.kotest

import io.github.frikit.krandom.generator.GeneratorConfig
import io.github.frikit.krandom.generator.Generators
import io.kotest.core.spec.style.DescribeSpec
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
})

/** Simple POJO for ObjectGenerator-based tests. */
class SamplePojo {
    var name: String = ""
    var age: Int = 0
    var email: String = ""
}
