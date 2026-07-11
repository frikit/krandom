/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.dsl

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.ints.shouldBeInRange
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotBeBlank

class KrandomDslTest : DescribeSpec({

    describe("typed property rules") {

        it("applies a property-reference rule") {
            val person = krandom<SamplePerson> {
                rule(SamplePerson::firstName) { "Ada" }
                rule(SamplePerson::age) { 30 }
            }
            person.firstName shouldBe "Ada"
            person.age shouldBe 30
        }

        it("applies property-reference rules to immutable data classes") {
            val account = krandom<TypedAccount> {
                rule(TypedAccount::owner) { "grace" }
            }
            account.owner shouldBe "grace"
            account.balance shouldNotBe null
        }

        it("excludes a property through a type-safe reference") {
            val person = krandom<SamplePerson> {
                exclude(SamplePerson::email)
            }
            person.email shouldBe ""
        }

        it("rejects duplicate rules for the same property at registration") {
            val failure = shouldThrow<IllegalArgumentException> {
                krandom<SamplePerson> {
                    rule(SamplePerson::firstName) { "Ada" }
                    rule("firstName") { "Grace" }
                }
            }
            failure.message shouldContain "Duplicate rule for field 'firstName'"
        }

        it("rejects duplicate type rules at registration") {
            val failure = shouldThrow<IllegalArgumentException> {
                krandom<SamplePerson> {
                    ruleForType(String::class.java) { "first" }
                    ruleForType(String::class.java) { "second" }
                }
            }
            failure.message shouldContain "Duplicate type rule"
        }

        it("rejects unknown field rules before generation") {
            val failure = shouldThrow<IllegalArgumentException> {
                krandom<SamplePerson> {
                    rule("noSuchField") { "value" }
                }
            }
            failure.message shouldContain "noSuchField"
            failure.message shouldContain "known fields"
        }
    }

    describe("krandom") {

        it("generates a random instance with defaults") {
            val person = krandom<SamplePerson>()
            person shouldNotBe null
            person.firstName.shouldNotBeBlank()
            person.age shouldNotBe 0
        }

        it("applies field-level rules") {
            val person = krandom<SamplePerson> {
                rule("firstName") { "Ada" }
                rule("age") { 30 }
            }
            person.firstName shouldBe "Ada"
            person.age shouldBe 30
        }

        it("applies type-level rules") {
            val person = krandom<SamplePerson> {
                ruleForType(String::class.java) { "fixed" }
            }
            person.firstName shouldBe "fixed"
            person.lastName shouldBe "fixed"
            person.email shouldBe "fixed"
        }

        it("applies reified type-level rules") {
            val person = krandom<SamplePerson> {
                ruleForType<String> { "reified" }
            }
            person.firstName shouldBe "reified"
        }

        it("supports seed for deterministic output") {
            val person1 = krandom<SamplePerson> {
                config { seed(123L) }
            }
            val person2 = krandom<SamplePerson> {
                config { seed(123L) }
            }
            person1.firstName shouldBe person2.firstName
            person1.lastName shouldBe person2.lastName
            person1.age shouldBe person2.age
        }

        it("supports exclude") {
            val person = krandom<SamplePerson> {
                exclude("email")
            }
            person shouldNotBe null
            person.firstName.shouldNotBeBlank()
            person.email shouldBe ""
        }

        it("supports maxDepth") {
            val nested = krandom<NestedPojo> {
                maxDepth(1)
            }
            nested shouldNotBe null
        }

        it("supports config block with multiple settings") {
            val person = krandom<SamplePerson> {
                config {
                    seed(42L)
                    stringLength(5, 10)
                    collectionSize(2, 3)
                }
            }
            person shouldNotBe null
            person.firstName.shouldNotBeBlank()
        }
    }

    describe("krandomList") {

        it("generates a list of random instances") {
            val people = krandomList<SamplePerson>(5)
            people shouldHaveSize 5
            people.forEach { it.firstName.shouldNotBeBlank() }
        }

        it("applies rules to all list elements") {
            val people = krandomList<SamplePerson>(3) {
                rule("firstName") { "Ada" }
            }
            people shouldHaveSize 3
            people.forEach { it.firstName shouldBe "Ada" }
        }

        it("generates empty list for count zero") {
            val people = krandomList<SamplePerson>(0)
            people shouldHaveSize 0
        }
    }

    describe("krandomGenerator") {

        it("creates a reusable generator") {
            val gen = krandomGenerator<SamplePerson> {
                rule("firstName") { "Reusable" }
            }
            val p1 = gen.generate()
            val p2 = gen.generate()
            p1.firstName shouldBe "Reusable"
            p2.firstName shouldBe "Reusable"
        }

        it("creates a seeded reusable generator") {
            val gen = krandomGenerator<SamplePerson> {
                config { seed(99L) }
            }
            val p1 = gen.generate()
            p1 shouldNotBe null
            p1.firstName.shouldNotBeBlank()
        }

        it("generateList on returned generator works") {
            val gen = krandomGenerator<SamplePerson> {
                rule("age") { 25 }
            }
            val people = gen.generateList(4)
            people shouldHaveSize 4
            people.forEach { it.age shouldBe 25 }
        }
    }

    describe("combined overrides") {

        it("field rule takes precedence over type rule") {
            val person = krandom<SamplePerson> {
                ruleForType<String> { "type-level" }
                rule("firstName") { "field-level" }
            }
            person.firstName shouldBe "field-level"
            person.lastName shouldBe "type-level"
            person.email shouldBe "type-level"
        }

        it("rules work with int fields via field rule") {
            val person = krandom<SamplePerson> {
                rule("age") { 42 }
            }
            person.age shouldBe 42
        }

        it("rules work with int fields via reified type rule") {
            val person = krandom<SamplePerson> {
                ruleForType<Int> { 42 }
            }
            person.age shouldBe 42
        }
    }
})

class SamplePerson {
    var firstName: String = ""
    var lastName: String = ""
    var email: String = ""
    var age: Int = 0
}

data class TypedAccount(
    val owner: String,
    val balance: Long
)

class NestedPojo {
    var name: String = ""
    var child: NestedPojo? = null
}
