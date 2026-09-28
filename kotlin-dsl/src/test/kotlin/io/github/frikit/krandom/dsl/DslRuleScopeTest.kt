/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.dsl

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotBeBlank

class DslRuleScopeTest : DescribeSpec({

    describe("root-scoped exclusions") {

        it("a property-reference exclusion does not remove same-named nested fields") {
            val customer = krandom<ScopedCustomer> {
                exclude(ScopedCustomer::name)
            }
            customer.name shouldBe ""
            customer.company shouldNotBe null
            customer.company!!.name.shouldNotBeBlank()
        }

        it("a field-name exclusion is scoped to the root type as well") {
            val customer = krandom<ScopedCustomer> {
                exclude("name")
            }
            customer.name shouldBe ""
            customer.company!!.name.shouldNotBeBlank()
        }

        it("an inherited field can be excluded from the root type") {
            val entity = krandom<DerivedEntity> {
                exclude(DerivedEntity::id)
            }
            entity.id shouldBe ""
            entity.label.shouldNotBeBlank()
        }

        it("rejects an unknown excluded field before generation") {
            val failure = shouldThrow<IllegalArgumentException> {
                krandom<ScopedCustomer> { exclude("noSuchField") }
            }
            failure.message shouldContain "noSuchField"
            failure.message shouldContain "known fields"
        }
    }

    describe("rule targets") {

        it("applies a rule to an inherited field") {
            val entity = krandom<DerivedEntity> {
                rule(DerivedEntity::id) { "fixed-id" }
            }
            entity.id shouldBe "fixed-id"
        }

        it("rejects a rule on a computed property") {
            val failure = shouldThrow<IllegalArgumentException> {
                krandom<ComputedPerson> { rule(ComputedPerson::fullName) { "Ada Lovelace" } }
            }
            failure.message shouldContain "fullName"
            failure.message shouldContain "no backing field"
        }

        it("rejects an exclusion of a computed property") {
            val failure = shouldThrow<IllegalArgumentException> {
                krandom<ComputedPerson> { exclude(ComputedPerson::fullName) }
            }
            failure.message shouldContain "fullName"
            failure.message shouldContain "no backing field"
        }

        it("rejects a rule on a delegated property") {
            val failure = shouldThrow<IllegalArgumentException> {
                krandom<DelegatedPerson> { rule(DelegatedPerson::nickname) { "ada" } }
            }
            failure.message shouldContain "nickname"
            failure.message shouldContain "no backing field"
        }

        it("still accepts rules on primary-constructor parameters without a property") {
            val badge = krandom<Badge> {
                rule("owner") { "grace" }
            }
            badge.label shouldBe "GRACE"
        }
    }
})

class ScopedCustomer {
    var name: String = ""
    var company: ScopedCompany? = null
}

class ScopedCompany {
    var name: String = ""
}

open class BaseEntity {
    var id: String = ""
}

class DerivedEntity : BaseEntity() {
    var label: String = ""
}

class ComputedPerson {
    var first: String = ""
    var last: String = ""
    val fullName: String get() = "$first $last"
}

class DelegatedPerson {
    var first: String = ""
    val nickname: String by lazy { first.lowercase() }
}

class Badge(owner: String) {
    val label: String = owner.uppercase()
}
